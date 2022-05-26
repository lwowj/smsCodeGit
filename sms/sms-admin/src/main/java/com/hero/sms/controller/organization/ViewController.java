package com.hero.sms.controller.organization;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.utils.GoogleAuthenticator;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.entity.organization.OrganizationGroup;
import com.hero.sms.entity.organization.OrganizationProperty;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.OrganizationUserLimit;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.common.NeedBindGoogleKeyEnums;
import com.hero.sms.enums.common.OperatorEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.enums.organization.OrgAmountHandleEnums;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.enums.organization.OrgInterfaceTypeEnums;
import com.hero.sms.enums.organization.OrgSmsApproveTypeEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.enums.organization.OrganizationPropertyNameEnums;
import com.hero.sms.service.common.ICodeService;
import com.hero.sms.service.organization.IOrganizationCostService;
import com.hero.sms.service.organization.IOrganizationGroupService;
import com.hero.sms.service.organization.IOrganizationPropertyService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserLimitService;
import com.hero.sms.service.organization.IOrganizationUserService;

/**
 * @author Administrator
 */
@Controller("organizationView")
public class ViewController extends BaseController {

    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private IOrganizationUserService organizationUserService;
    @Autowired
    private IOrganizationCostService organizationCostService;
    @Autowired
    private IOrganizationUserLimitService organizationUserLimitService;
    @Autowired
    IOrganizationPropertyService organizationPropertyService;
    @Autowired
    private IOrganizationGroupService organizationGroupService;
    @Autowired
    private ICodeService codeService;

    //商户管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organization")
    @RequiresPermissions("organization:view")
    public String organization(Model model) {
    	model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
    	List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        model.addAttribute("orgSmsApproveTypeEnums", OrgSmsApproveTypeEnums.values());
        model.addAttribute("orgApproveStateEnums", OrgApproveStateEnums.values());
        model.addAttribute("orgStatusEnums", OrgStatusEnums.values());
        model.addAttribute("operatorEnums", OperatorEnums.values());
        return FebsUtil.view("organization/organization");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/add")
    @RequiresPermissions("organization:add")
    public String organizationAdd(Model model) {
    	//获取所有分组
        List<Code> list = DatabaseCache.getCodeListBySortCode("OrgGroup");
        model.addAttribute("orgGroupList", list);
        model.addAttribute("orgInterfaceTypeEnums", OrgInterfaceTypeEnums.values());
        model.addAttribute("needBindGoogleKeyEnums", NeedBindGoogleKeyEnums.values());
        return FebsUtil.view("organization/organizationAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/update/{id}")
    @RequiresPermissions("organization:update")
    public String organizationUpdate(@PathVariable String id, Model model) {
        resolveOrganizationModel(id, model);
        model.addAttribute("orgInterfaceTypeEnums", OrgInterfaceTypeEnums.values());
        model.addAttribute("orgStatusEnums", OrgStatusEnums.values());
        return FebsUtil.view("organization/organizationUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/cancel/{id}")
    @RequiresPermissions("organization:cancel")
    public String organizationCancel(@PathVariable String id, Model model) {
        resolveOrganizationModel(id, model);
        model.addAttribute("orgAmountHandleEnums", OrgAmountHandleEnums.values());
        return FebsUtil.view("organization/organizationCancel");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationCost/updates/{id}")
    @RequiresPermissions("organizationCost:updates")
    public String organizationCostUpdates(@PathVariable String id, Model model) {
        resolveOrganizationCostModel(id, model);
        model.addAttribute("operatorEnums", OperatorEnums.values());
        return FebsUtil.view("organization/organizationCostUpdate");
    }

    //商户用户管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUser")
    @RequiresPermissions("organizationUser:view")
    public String organizationUser(Model model) {
        model.addAttribute("orgStatusEnums", OrgStatusEnums.values());
        model.addAttribute("needBindGoogleKeyEnums", NeedBindGoogleKeyEnums.values());
        return FebsUtil.view("organization/organizationUser");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUser/add")
    @RequiresPermissions("organizationUser:add")
    public String organizationUserAdd(Model model) {
    	model.addAttribute("needBindGoogleKeyEnums", NeedBindGoogleKeyEnums.values());
        return FebsUtil.view("organization/organizationUserAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUser/update/{id}")
    @RequiresPermissions("organizationUser:update")
    public String organizationUserUpdate(@PathVariable String id, Model model) {
        resolveOrganizationUserModel(id, model);
        model.addAttribute("needBindGoogleKeyEnums", NeedBindGoogleKeyEnums.values());
        return FebsUtil.view("organization/organizationUserUpdate");
    }

    private void resolveOrganizationModel(String id, Model model) {
        Organization organization = this.organizationService.getById(id);
        model.addAttribute("organization",organization);
    }

    /**
     * 获取商户费率
     * @param id
     * @param model
     */
    private void resolveOrganizationCostModel(String id, Model model) {
        Organization organization = this.organizationService.getById(id);
        OrganizationCost cost = new OrganizationCost();
        cost.setOrganizationCode(organization.getOrganizationCode());
        List<OrganizationCost> costs = this.organizationCostService.findOrganizationCosts(cost);
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        model.addAttribute("orgApproveStateEnums", OrgApproveStateEnums.values());
        model.addAttribute("costs",costs);
        model.addAttribute("organization",organization);
    }

    private void resolveOrganizationUserModel(String id, Model model) {
        OrganizationUser organizationUser = this.organizationUserService.getById(id);
        model.addAttribute("organizationName", DatabaseCache.getOrgNameByOrgcode(organizationUser.getOrganizationCode()));
        model.addAttribute("organizationUser",organizationUser);
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser/organizationUserMenu")
    @RequiresPermissions("organizationUserMenu:list")
    public String organizationUserMenu() {
        return FebsUtil.view("organization/organizationUserMenu");
    }

    // 商户用户权限
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser/updateMenuLimit/{id}")
    @RequiresPermissions("organizationUserLimit:update")
    public String updateOrganizationUserLimit(@PathVariable Long id, Model model) {
        resolveOrganizationUserLimitModel(id, model);
        return FebsUtil.view("organization/organizationUserLimitUpdate");
    }
    private void resolveOrganizationUserLimitModel(Long id, Model model) {
        OrganizationUserLimit limit = new OrganizationUserLimit();
        limit.setUserId(id);
        List<OrganizationUserLimit> limits = this.organizationUserLimitService.findOrganizationUserLimits(limit);
        model.addAttribute("limits",limits);
        model.addAttribute("id",id);
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser/googleKey/{id}")
    @RequiresPermissions("organizationUser:google:key")
    public String getUserGoogleKey(@PathVariable Long id, Model model) {
        OrganizationUser organizationUser = this.organizationUserService.getById(id);
        if(StringUtils.isBlank( organizationUser.getGoogleKey())){
            model.addAttribute("googleQrcode", "null");
        }else{
            String googleQrcode = GoogleAuthenticator.getQRBarcode(organizationUser.getUserAccount(), organizationUser.getGoogleKey());
            model.addAttribute("googleQrcode", googleQrcode);
            model.addAttribute("userAccount", organizationUser.getUserAccount());
        }
        return FebsUtil.view("organization/googleKey");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationLog")
    public String organizationLogIndex(){
        return FebsUtil.view("organization/organizationLog");
    }


    //商户属性更改
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationProperty/properties/{orgCode}")
    @RequiresPermissions("organizationProperty:update")
    public String organizationPropertyUpdate(Model model,@PathVariable String orgCode){
        resolveOrgPropertiesModel(orgCode,model);
        return FebsUtil.view("organization/orgPropertyUpdate");
    }

    private void resolveOrgPropertiesModel(String orgCode, Model model) {
        LambdaQueryWrapper<OrganizationProperty> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrganizationProperty::getOrganizationCode,orgCode);
        List<OrganizationProperty> list = this.organizationPropertyService.list(queryWrapper);
        model.addAttribute("orgProperties",list);
        model.addAttribute("orgCode",orgCode);
    }
    //商户接入信息
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationProperty/propertiesInfo/{orgCode}")
    @RequiresPermissions("organizationProperty:info")
    public String organizationPropertyInfo(Model model,@PathVariable String orgCode){
        resolveOrgPropertiesInfoModel(orgCode,model);
        return FebsUtil.view("organization/orgPropertyInfo");
    }
    private void resolveOrgPropertiesInfoModel(String orgCode, Model model) {

        StringBuffer info = new StringBuffer();
        Organization org = organizationService.getOrganizationByCode(orgCode);

        Integer interfaceType = org.getInterfaceType();

        if (interfaceType == 0){
            info.append("暂未开通任何接入权限");
        }else {
            if ((interfaceType&OrgInterfaceTypeEnums.Http.getCode()) == OrgInterfaceTypeEnums.Http.getCode()){
                //http接入信息
                Code host = DatabaseCache.getCodeBySortCodeAndCode("HttpServerConfig", "host");
                Code port = DatabaseCache.getCodeBySortCodeAndCode("HttpServerConfig", "port");
                Code interfaceDocUri = DatabaseCache.getCodeBySortCodeAndCode("HttpServerConfig", "interfaceDocUri");
                info.append("==================================================\n\n")
                        .append("【HTTP】接入信息")
                        .append("\n  IP地址：").append(host!=null?host.getName():null)
                        .append("\n  端口：").append(port!=null?port.getName():null)
                        .append("\n  商户编号：").append(orgCode)
                        .append("\n  秘钥：").append(org.getMd5Key())
                        .append("\n\n  接口文档地址：").append(interfaceDocUri!=null?interfaceDocUri.getName():null)
                        .append("\n\n");
            }
            if ((interfaceType&OrgInterfaceTypeEnums.Smpp.getCode()) == OrgInterfaceTypeEnums.Smpp.getCode()){

                Code host = DatabaseCache.getCodeBySortCodeAndCode("SmppServerConfig", "host");
                Code port = DatabaseCache.getCodeBySortCodeAndCode("SmppServerConfig", "port");

                LambdaQueryWrapper<OrganizationProperty> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(OrganizationProperty::getOrganizationCode,orgCode);
                List<OrganizationProperty> list = this.organizationPropertyService.list(queryWrapper);
                Map<String, String> propertyMap = list.stream()
                        .filter(item -> item.getPropertyType() == OrgInterfaceTypeEnums.Smpp.getCode())
                        .collect(Collectors.toMap(OrganizationProperty::getName, OrganizationProperty::getValue, (e1, e2) -> e1));

                info.append("==================================================\n\n")
                        .append("【SMPP】接入信息")
                        .append("\n  IP地址：").append(host!=null?host.getName():null)
                        .append("\n  端口：").append(port!=null?port.getName():null)
                        .append("\n  账号：").append(propertyMap.get(OrganizationPropertyNameEnums.SystemId.getCode()))
                        .append("\n  密码：").append(propertyMap.get(OrganizationPropertyNameEnums.Password.getCode()))
                        .append("\n  速度：").append(propertyMap.get(OrganizationPropertyNameEnums.ReadLimit.getCode()))
                        .append("\n  连接数：").append(propertyMap.get(OrganizationPropertyNameEnums.MaxChannels.getCode()))
                        .append("\n\n");
            }
        }

        model.addAttribute("propertyInfo",info.toString());
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationGroup/update/{organizationCode}")
    @RequiresPermissions("organization:updateGroup")
    public String updateGroup(@PathVariable String organizationCode, Model model) {

        Map<String,Object> data = new HashMap<>();
        data.put("orgCode",organizationCode);

        //获取已分组数据
        LambdaQueryWrapper<OrganizationGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrganizationGroup::getOrgCode,organizationCode);
        Map<String, String> groupMap = organizationGroupService.list(queryWrapper).stream()
                .collect(Collectors.toMap(OrganizationGroup::getGroupId, OrganizationGroup::getOrgCode));

        //获取所有分组
        List<Code> list = DatabaseCache.getCodeListBySortCode("OrgGroup");
        List<Map> orgGroupDictList = Lists.newArrayList();
        list.stream().forEach(item -> {
            Map<String,String> map = new HashMap<>();
            map.put("name",item.getName());
            map.put("value",item.getCode());

            String selected = "";
            if (groupMap.containsKey(item.getCode())){
                selected = "selected";
            }
            map.put("selected",selected);
            orgGroupDictList.add(map);
        });
        data.put("orgGroupList",orgGroupDictList);

        model.addAttribute("data",data);

        return FebsUtil.view("organization/organizationGroupUpdate");
    }
    
    //商户登录日志管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUserLoginLog")
    @RequiresPermissions("organizationUserLoginLog:view")
    public String organizationUserLoginLog(Model model) 
    {
    	setTodayTimeStartAndEnd(model);
    	model.addAttribute("commonStateEnums",  CommonStateEnums.values());
        return FebsUtil.view("organization/organizationUserLoginLog");
    }
    
    /**
     * 回传当天开始时间和结束时间
     * 时间格式 yyyy-MM-dd HH:mm:ss
     * @param model
     */
    protected void setTodayTimeStartAndEnd(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(),LocalTime.MAX);
        model.addAttribute("todayStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("todayEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    /**
     * 商户分组设置
     * 2021-03-06
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/orgGroup")
    @RequiresPermissions("organization:orgGroupView")
    public String orgGroup(){
        return FebsUtil.view("organization/orgGroup");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/orgGroupAdd")
    @RequiresPermissions("organization:orgGroupAdd")
    public String orgGroupAddaView() {
        return FebsUtil.view("organization/orgGroupAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/orgGroupUpdate/{id}")
    @RequiresPermissions("organization:orgGroupUpdate")
    public String orgGroupUpdateView(@PathVariable String id, Model model) 
    {
    	 resolveCodeModel(id, model);
        return FebsUtil.view("organization/orgGroupUpdate");
    }
    
    private void resolveCodeModel(String id, Model model) {
        Code code = codeService.getById(id);
        model.addAttribute("code",code);
    }
}
