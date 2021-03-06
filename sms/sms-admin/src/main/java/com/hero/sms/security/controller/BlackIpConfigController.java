package com.hero.sms.security.controller;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.utils.RegexUtil;
import com.hero.sms.entity.common.BlackIpConfig;
import com.hero.sms.entity.common.BlackIpConfigQuery;
import com.hero.sms.enums.common.ModuleTypeEnums;
import com.hero.sms.service.common.IBlackIpConfigService;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 *  Controller
 *
 * @author Administrator
 * @date 2020-12-21 19:43:55
 */
@Slf4j
@Validated
@Controller
@RequestMapping("blackIpConfig")
public class BlackIpConfigController extends BaseController {

    @Autowired
    private IBlackIpConfigService blackIpConfigService;
    
    @Autowired
    private IBusinessManage businessManage;

    @GetMapping(FebsConstant.VIEW_PREFIX + "blackIpConfig")
    public String blackIpConfigIndex(){
        return FebsUtil.view("blackIpConfig/blackIpConfig");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("blackIpConfig:list")
    public FebsResponse getAllBlackIpConfigs(BlackIpConfigQuery blackIpConfig) {
        return new FebsResponse().success().data(blackIpConfigService.findBlackIpConfigs(blackIpConfig));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("blackIpConfig:list")
    public FebsResponse blackIpConfigList(QueryRequest request, BlackIpConfigQuery blackIpConfig) {
        Map<String, Object> dataTable = getDataTable(this.blackIpConfigService.findBlackIpConfigs(request, blackIpConfig));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "??????BlackIpConfig", exceptionMessage = "??????BlackIpConfig??????")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("blackIpConfig:add")
    public FebsResponse addBlackIpConfig(@Valid BlackIpConfig blackIpConfig) {
    	if(StringUtils.isNotBlank(blackIpConfig.getBlackIp()))
    	{
    		if(!RegexUtil.isIP(blackIpConfig.getBlackIp()))
    		{
    			return new FebsResponse().message("IP??????????????????").fail();
    		}
    		BlackIpConfig queryBlackIpConfig = new BlackIpConfig();
    		queryBlackIpConfig.setBlackIp(blackIpConfig.getBlackIp());
    		List<BlackIpConfig> queryBlackIpConfigList =  this.blackIpConfigService.findBlackIpConfigs(queryBlackIpConfig); 
    		if(queryBlackIpConfigList!=null&&queryBlackIpConfigList.size()>0)
    		{
    			return new FebsResponse().message("???IP????????????????????????????????????????????????????????????").fail();
    		}
    		User user = super.getCurrentUser();
        	blackIpConfig.setCreateUser(user.getUsername());
            this.blackIpConfigService.createBlackIpConfig(blackIpConfig);
            return new FebsResponse().success();
    	}
    	else
    	{
    		return new FebsResponse().message("????????????????????????????????????????????????").fail();
    	}
    }

    @ControllerEndpoint(operation = "??????BlackIpConfig", exceptionMessage = "??????BlackIpConfig??????")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("blackIpConfig:delete")
    public FebsResponse deleteBlackIpConfig(BlackIpConfig blackIpConfig) {
        this.blackIpConfigService.deleteBlackIpConfig(blackIpConfig);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "????????????BlackIpConfig", exceptionMessage = "????????????BlackIpConfig??????")
    @GetMapping("delete/{blackIpConfigIds}")
    @ResponseBody
    @RequiresPermissions("blackIpConfig:delete")
    public FebsResponse deleteBlackIpConfig(@NotBlank(message = "{required}") @PathVariable String blackIpConfigIds) {
        String[] ids = blackIpConfigIds.split(StringPool.COMMA);
        this.blackIpConfigService.deleteBlackIpConfigs(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "??????BlackIpConfig", exceptionMessage = "??????BlackIpConfig??????")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("blackIpConfig:update")
    public FebsResponse updateBlackIpConfig(BlackIpConfig blackIpConfig) {
    	if(StringUtils.isNotBlank(blackIpConfig.getBlackIp()))
    	{
    		this.blackIpConfigService.updateBlackIpConfig(blackIpConfig);
            return new FebsResponse().success();
    	}
    	else
    	{
    		return new FebsResponse().message("????????????????????????????????????????????????").fail();
    	}
    }

    @ControllerEndpoint(operation = "??????BlackIpConfig", exceptionMessage = "??????Excel??????")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("blackIpConfig:export")
    public void export(QueryRequest queryRequest, BlackIpConfig blackIpConfig, HttpServletResponse response) {
        List<BlackIpConfig> blackIpConfigs = this.blackIpConfigService.findBlackIpConfigs(queryRequest, blackIpConfig).getRecords();
        ExcelKit.$Export(BlackIpConfig.class, response).downXlsx(blackIpConfigs, false);
    }
    
    @ControllerEndpoint(operation = "?????????????????????", exceptionMessage = "???????????????????????????")
    @GetMapping("batchInvoke/{blackIpConfigIds}")
    @ResponseBody
    @RequiresPermissions("blackIpConfig:batchInvoke")
    public FebsResponse batchInvoke(@PathVariable("blackIpConfigIds") String blackIpConfigIds) 
    {
        if (StringUtils.isBlank(blackIpConfigIds)){
            return new FebsResponse().fail().message("?????????????????????????????????");
        }
        try {
        	this.blackIpConfigService.audit(blackIpConfigIds,"1");//?????? ??????
		} catch (Exception e) {
			// TODO: handle exception
			return new FebsResponse().fail().message("??????????????????");
		}
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "?????????????????????", exceptionMessage = "???????????????????????????")
    @GetMapping("batchNoInvoke/{blackIpConfigIds}")
    @ResponseBody
    @RequiresPermissions("blackIpConfig:batchNoInvoke")
    public FebsResponse batchNoInvoke(@PathVariable("blackIpConfigIds") String blackIpConfigIds) 
    {
        if (StringUtils.isBlank(blackIpConfigIds)){
            return new FebsResponse().fail().message("?????????????????????????????????");
        }
        try {
        	this.blackIpConfigService.audit(blackIpConfigIds,"0");//?????? ??????
		} catch (Exception e) {
			// TODO: handle exception
			return new FebsResponse().fail().message("??????????????????");
		}
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "??????IP???????????????", exceptionMessage = "??????IP?????????????????????")
    @GetMapping("loadBlackIpConfigCache")
    @ResponseBody
    @RequiresPermissions("blackIpConfig:loadBlackIpConfigCache")
    public FebsResponse loadReladBlackIpConfigCache(){
        try {
        	businessManage.reladProjectCacheForModule("merch,agent",String.valueOf(ModuleTypeEnums.BlackIpConfig.getCode().intValue()));
		} catch (ServiceException e) {
			return new FebsResponse().message("?????????????????????????????????????????????");
		}
        return new FebsResponse().success();
    }
}
