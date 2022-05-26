package com.hero.sms.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.service.ISessionService;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserService;
import com.hero.sms.utils.StringUtil;
import com.wx.pwd.CheckStrength;
import com.wx.pwd.CheckStrength.LEVEL;

import lombok.extern.slf4j.Slf4j;

/**
 * 商户用户 Controller
 *
 * @author Administrator
 * @date 2020-03-07 21:36:18
 */
@Slf4j
@Validated
@RestController
@RequestMapping("organizationUser")
public class OrganizationUserController extends BaseController {

    @Autowired
    private IOrganizationUserService organizationUserService;
    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private ISessionService sessionService;
    @Autowired
    private IReportService reportService;

    @GetMapping("check/{userAccount}")
    public boolean checkUserAccount(@NotBlank(message = "{required}") @PathVariable String userAccount, String userId){
        OrganizationUser orgUser = this.organizationUserService.findByUserAccount(userAccount);
        return orgUser==null || StringUtils.isNotBlank(userId);
    }

    @GetMapping("queryUserInfo")
    public FebsResponse queryUserInfo(){
        OrganizationUser user = super.getCurrentUser();
        Organization organization = this.organizationService.getOrganizationByCode(user.getOrganizationCode());
        organization.setDataMd5(null);
        organization.setRemark(null);
        organization.setCreateUser(null);
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(),LocalTime.MAX);
        SendRecordQuery sendRecord = new SendRecordQuery();
        sendRecord.setOrgCode(user.getOrganizationCode());
        sendRecord.setCreateStartTime(Date.from( todayStart.atZone(ZoneId.systemDefault()).toInstant()));
        sendRecord.setCreateEndTime(Date.from( todayEnd.atZone(ZoneId.systemDefault()).toInstant()));
        Map<String, Object> result = reportService.statisticSendRecordInfo(sendRecord);
        Long reqSuccess = (Long) result.get("reqSuccess");
        Long receiptSuccess = (Long) result.get("receiptSuccess");
        Long receiptFail = (Long) result.get("receiptFail");
        Long realReqSuccess = reqSuccess + receiptSuccess + receiptFail;
        return new FebsResponse().success().data(organization).put("todaySendSmsTotal",realReqSuccess).put("todayCashAmount",result.get("consumeAmount"));
    }

    @ControllerEndpoint( operation = "用户列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationUser:view")
    public FebsResponse organizationUserList(QueryRequest request, OrganizationUser organizationUser) {
        organizationUser.setOrganizationCode(this.getCurrentUser().getOrganizationCode());
        IPage<OrganizationUser> datas = this.organizationUserService.findOrganizationUsers(request, organizationUser);
        List<OrganizationUser> list = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        list.stream().forEach(item -> {// 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("organizationName",DatabaseCache.getOrgNameByOrgcode(item.getOrganizationCode()));
            map.put("userName",item.getUserName());
            map.put("userAccount",item.getUserAccount());
            map.put("lastLoginIp",item.getLastLoginIp());
            map.put("lastLoginTime",item.getLastLoginTime());
            map.put("loginFaildCount",item.getLoginFaildCount());
            map.put("status",item.getStatus());
            map.put("accountType",item.getAccountType());
            map.put("description",item.getDescription());
            map.put("remark",item.getRemark());
            map.put("createUser",item.getCreateUser());
            map.put("createDate",item.getCreateDate());
            //新增两个字段 2020-12-09
            map.put("needBindGoogleKey",item.getNeedBindGoogleKey());
            map.put("loginIp",item.getLoginIp());
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);

        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint( operation = "新增用户")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organizationUser:add")
    public FebsResponse addOrganizationUser(@Valid OrganizationUser organizationUser) {
        try {
            organizationUser.setOrganizationCode(getCurrentUser().getOrganizationCode());
            organizationUser.setCreateUser(getCurrentUser().getUserAccount());
            organizationUser.setAccountType(DatabaseCache.getCodeBySortCodeAndName("AccountType","子账户").getCode());
            organizationUser.setCreateDate(new Date());
            this.organizationUserService.createOrganizationUser(organizationUser);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("新增用户失败",e);
            return new FebsResponse().message("新增用户失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint( operation = "修改用户")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organizationUser:update")
    public FebsResponse updateOrganizationUser(OrganizationUser organizationUser) {
        try {
            this.organizationUserService.updateOrganizationUser(organizationUser,getCurrentUser());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("修改用户失败",e);
            return new FebsResponse().message("修改用户失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "解锁用户")
    @GetMapping("unlock/{organizationUserIds}")
    @ResponseBody
    @RequiresPermissions("organizationUser:unlock")
    public FebsResponse unlockOrganizationUser(@NotBlank(message = "{required}") @PathVariable String organizationUserIds) {
        String[] ids = organizationUserIds.split(StringPool.COMMA);
        this.organizationUserService.updateUserStatus(ids, OrgStatusEnums.Normal.getCode(),getCurrentUser());
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "锁定用户")
    @GetMapping("lock/{organizationUserIds}")
    @ResponseBody
    @RequiresPermissions("organizationUser:lock")
    public FebsResponse lockOrganizationUser(@NotBlank(message = "{required}") @PathVariable String organizationUserIds) {
        String[] ids = organizationUserIds.split(StringPool.COMMA);
        this.organizationUserService.updateUserStatus(ids,OrgStatusEnums.Lock.getCode(),getCurrentUser());
        this.sessionService.forceLogoutOrgUser(organizationUserIds,getCurrentUser().getOrganizationCode());
        return new FebsResponse().success();
    }

    @PostMapping("password/reset/{userAccounts}")
    @RequiresPermissions("organizationUser:password:reset")
    @ControllerEndpoint(exceptionMessage = "重置用户密码失败" , operation = "重置密码")
    public FebsResponse resetPassword(@NotBlank(message = "{required}") @PathVariable String userAccounts) {
        String[] accounts = userAccounts.split(StringPool.COMMA);
        this.organizationUserService.resetPassword(accounts,getCurrentUser());
        return new FebsResponse().success();
    }

    @PostMapping("password/update")
    @ControllerEndpoint(exceptionMessage = "修改密码失败", operation = "修改密码")
    public FebsResponse updatePassword(
            @NotBlank(message = "{required}") String oldPassword,
            @NotBlank(message = "{required}") String newPassword) {
    	if(StringUtil.isEmpty(newPassword))
    	{
    		return new FebsResponse().message("新密码不能为空").fail();
    	}
//    	LEVEL passwordStrong = LEVEL.MIDIUM;
//     	try 
//     	{
//     		/**
//	        	  *  获取密码强度信息
//	        	 * EASY				简单
//	        	 * MIDIUM			中等
//	        	 * STRONG			强
//	        	 * VERY_STRONG		很强
//	        	 * EXTREMELY_STRONG	极强
//	        	 */
//     		passwordStrong = new CheckStrength().getPasswordLevel(newPassword);
//     		if("EASY".equals(String.valueOf(passwordStrong)))
//     		{
//     			return new FebsResponse().message("新密码安全强度过低，请修改复杂些").fail();
//     		}
//		} catch (Exception e) {}
//     	HttpSession session = request.getSession();
//        session.setAttribute("pwStrong", String.valueOf(passwordStrong));
        
        OrganizationUser user = getCurrentUser();
        if (!StringUtils.equals(user.getUserPassword(), MD5Util.encrypt(user.getUserAccount(), oldPassword))) {
            return new FebsResponse().message("原密码不正确").fail();
        }
        organizationUserService.updatePassword(user.getUserAccount(), newPassword);
        return new FebsResponse().success();
    }

    @PostMapping("password/checkLevel/{newPassword}")
    @ControllerEndpoint(exceptionMessage = "检测密码强度失败", operation = "检测密码强度")
    public FebsResponse passwordCheckLevel(@NotBlank(message = "{required}") @PathVariable String newPassword) 
    {
    	if(StringUtil.isEmpty(newPassword))
    	{
    		return new FebsResponse().message("密码不能为空").fail();
    	}
    	LEVEL passwordStrong = LEVEL.MIDIUM;
     	try 
     	{
     		/**
	        	  *  获取密码强度信息
	        	 * EASY				简单
	        	 * MIDIUM			中等
	        	 * STRONG			强
	        	 * VERY_STRONG		很强
	        	 * EXTREMELY_STRONG	极强
	        	 */
     		passwordStrong = new CheckStrength().getPasswordLevel(newPassword);
			} catch (Exception e) {}
     	
        return new FebsResponse().success().data(String.valueOf(passwordStrong));
    }
    
    @ControllerEndpoint(exceptionMessage = "绑定谷歌失败", operation = "绑定谷歌")
    @PostMapping("bindGoogle")
    @ResponseBody
    @RequiresPermissions("organizationUser:bindGoogle")
    public FebsResponse bindGoogle(@NotBlank(message = "{required}") String goologoVerifyCode,
                                   @NotBlank(message = "{required}") String googleKey,
                                   @NotBlank(message = "{required}") String password){
        try {
            organizationUserService.bindGoogle(getCurrentUser(),goologoVerifyCode,googleKey,password);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(exceptionMessage = "解绑谷歌失败", operation = "解绑谷歌")
    @PostMapping("unbindGoogle")
    @ResponseBody
    @RequiresPermissions("organizationUser:bindGoogle")
    public FebsResponse getGoogleKey(@NotBlank(message = "{required}") String goologoVerifyCode,
                                     @NotBlank(message = "{required}") String password){
        try {
            organizationUserService.removeGoogleKey(getCurrentUser(),goologoVerifyCode,password);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "更新余额")
    @GetMapping("thisOrganizationAvailable")
    @ResponseBody
    @Limit(key = "thisOrganizationAvailable", period = 1, count = 1, name = "更新余额", prefix = "limit")
    public FebsResponse refreshAvailable() {
        OrganizationUser user = super.getCurrentUser();
        Organization organization = this.organizationService.getOrganizationByCode(user.getOrganizationCode());
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("availableAmount",organization.getAvailableAmount());
        return new FebsResponse().success().data(result);
    }
}
