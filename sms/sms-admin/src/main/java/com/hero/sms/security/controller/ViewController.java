package com.hero.sms.security.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.common.BlackIpConfig;
import com.hero.sms.enums.blackIpConfig.IsAvailabilityEnums;
import com.hero.sms.enums.blackIpConfig.LImitProjectEnums;
import com.hero.sms.service.common.IBlackIpConfigService;

/**
 * @author Administrator
 */
@Controller("securityView")
public class ViewController extends BaseController {

    @Autowired
    private IBlackIpConfigService blackIpConfigService;

    /**
     * IP黑名单配置
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "security/blackIpConfig")
    @RequiresPermissions("blackIpConfig:list")
    public String securityBlackIpConfig(Model model){
    	model.addAttribute("limitProjectEnums", LImitProjectEnums.values());
    	model.addAttribute("isAvailabilityEnums", IsAvailabilityEnums.values());
        return FebsUtil.view("security/blackIpConfig/blackIpConfig");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "security/blackIpConfig/add")
    @RequiresPermissions("blackIpConfig:add")
    public String securityBlackIpConfigAdd(Model model) {
    	model.addAttribute("limitProjectEnums", LImitProjectEnums.values());
    	model.addAttribute("isAvailabilityEnums", IsAvailabilityEnums.values());
        return FebsUtil.view("security/blackIpConfig/blackIpConfigAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "security/blackIpConfig/update/{id}")
    @RequiresPermissions("blackIpConfig:update")
    public String securityBlackIpConfigUpdate(@PathVariable String id, Model model) {
    	resolveBlackIpConfigModel(id, model);
    	model.addAttribute("limitProjectEnums", LImitProjectEnums.values());
    	model.addAttribute("isAvailabilityEnums", IsAvailabilityEnums.values());
        return FebsUtil.view("security/blackIpConfig/blackIpConfigUpdate");
    }
    
    private void resolveBlackIpConfigModel(String id, Model model) {
    	BlackIpConfig blackIpConfig = this.blackIpConfigService.getById(id);
        model.addAttribute("blackIpConfig",blackIpConfig);
    }
}
