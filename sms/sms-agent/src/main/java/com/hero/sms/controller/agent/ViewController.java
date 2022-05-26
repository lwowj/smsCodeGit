package com.hero.sms.controller.agent;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.utils.GoogleAuthenticator;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentCost;
import com.hero.sms.entity.agent.AgentMenuLimit;
import com.hero.sms.entity.agent.AgentSystemConfig;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.enums.common.OperatorEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.agent.IAgentCostService;
import com.hero.sms.service.agent.IAgentMenuLimitService;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.agent.IAgentSystemConfigService;

/**
 * @author Administrator
 */
@Controller("agentView")
public class ViewController extends BaseController {


    @Autowired
    private IAgentService agentService;
    @Autowired
    private IAgentCostService agentCostService;
    @Autowired
    private IAgentMenuLimitService agentMenuLimitService;
    @Autowired
    private IAgentSystemConfigService agentSystemConfigService;


    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/add")
    @RequiresPermissions("agent:add")
    public String systemAgentAdd() {
        return FebsUtil.view("agent/agentAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/update/{id}")
    @RequiresPermissions("agent:update")
    public String agentUpdate(@PathVariable String id, Model model) {
        resolveAgentModel(id, model);
        return FebsUtil.view("agent/agentUpdate");
    }

    // 代理费率
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/updateCost/{id}")
    @RequiresPermissions("agentCost:updates")
    public String updateCost(@PathVariable int id, Model model) {
        resolveAgentCostsModel(id, model);
        model.addAttribute("operatorEnums", OperatorEnums.values());
        return FebsUtil.view("agent/agentCostUpdate");
    }

    // 代理权限
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/updateMenuLimit/{id}")
    @RequiresPermissions("agentMenuLimit:update")
    public String updateMenuLimit(@PathVariable Long id, Model model) {
        resolveAgentMenuLimitModel(id, model);
        return FebsUtil.view("agent/agentMenuLimitUpdate");
    }

    private void resolveAgentModel(String id, Model model) {
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Agent::getUpAgentId,getCurrentAgent().getId());
        queryWrapper.eq(Agent::getId,id);
        Agent agent = this.agentService.getOne(queryWrapper);
        Agent newAgent = new Agent();
        newAgent.setAgentName(agent.getAgentName());
        newAgent.setAgentAccount(agent.getAgentAccount());
        newAgent.setId(agent.getId());
        newAgent.setRemark(agent.getRemark());
        newAgent.setPhoneNumber(agent.getPhoneNumber());
        newAgent.setEmail(agent.getEmail());
        newAgent.setQq(agent.getQq());
        newAgent.setStateCode(agent.getStateCode());
        newAgent.setDescription(agent.getDescription());
        //新增两个字段 2020-12-09
        newAgent.setNeedBindGoogleKey(agent.getNeedBindGoogleKey());
        newAgent.setLoginIp(agent.getLoginIp());
        model.addAttribute("agent",newAgent);
    }

    private void resolveAgentCostsModel(int id, Model model) {
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Agent::getUpAgentId,getCurrentAgent().getId());
        queryWrapper.eq(Agent::getId,id);
        Agent agent = this.agentService.getOne(queryWrapper);
        List<AgentCost> agentCosts = null;
        if(agent != null){
            AgentCost agentCost = new AgentCost();
            agentCost.setAgentId(agent.getId());
            agentCosts = this.agentCostService.findAgentCosts(agentCost);
        }
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = agentCostService.getAgentAreaCodeList(getCurrentAgent().getId());
        model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        model.addAttribute("agentCosts",agentCosts);
        model.addAttribute("id",id);
    }

    private void resolveAgentMenuLimitModel(Long id, Model model) {
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Agent::getUpAgentId,getCurrentAgent().getId());
        queryWrapper.eq(Agent::getId,id);
        Agent agent = this.agentService.getOne(queryWrapper);
        List<AgentMenuLimit> limits = null;
        if(agent != null){
            AgentMenuLimit limit = new AgentMenuLimit();
            limit.setAgentId(id);
            limits = this.agentMenuLimitService.findAgentMenuLimits(limit);
        }
        model.addAttribute("limits",limits);
        model.addAttribute("id",id);
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/agentMenu")
    @RequiresPermissions("agentMenu:list")
    public String systemMenu() {
        return FebsUtil.view("agent/agentMenu");
    }


    //代理修改密码
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/updatePassword")
    @RequiresPermissions("agent:updatePassword")
    public String updatePassword() {
        return FebsUtil.view("agent/agentUpdatePassword");
    }
    //代理修改支付密码
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/updatePayPassword")
    @RequiresPermissions("agent:updatePayPassword")
    public String updatePayPassword() {
        return FebsUtil.view("agent/agentUpdatePayPassword");
    }


    //代理充值管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRechargeOrder/agentRechargeOrder")
    public String agentRechargeOrderIndex(){
        return FebsUtil.view("rechargeOrder/agentRechargeOrder");
    }

    //代理配置
    @GetMapping(FebsConstant.VIEW_PREFIX + "agentSystemConfig/add")
    public String agentSystemConfigAdd(Model model){
        resolveAgentSystemConfigModel(model);
        return FebsUtil.view("agent/agentSystemConfigAdd");
    }

    private void resolveAgentSystemConfigModel(Model model) {
        AgentSystemConfig agentSystemConfig = this.agentSystemConfigService.findAgentSystemConfig(null,getCurrentAgent().getId());
        if(agentSystemConfig != null){
            model.addAttribute("remark",agentSystemConfig.getRemark());
            model.addAttribute("platformName",agentSystemConfig.getSystemName());
            model.addAttribute("domain",agentSystemConfig.getSystemUrl());
        }
    }

    //代理绑定谷歌
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/bindGoogle")
    public String bindGoogle(Model model){
        Agent agent = getCurrentAgent();
        if(agent==null)
        {
        	return "redirect:/logout";
        }
        if(StringUtils.isEmpty(agent.getGoogleKey())){
            String googleKey = GoogleAuthenticator.generateSecretKey();
            String googleQrcode = GoogleAuthenticator.getQRBarcode(getCurrentAgent().getAgentAccount(),googleKey);
            model.addAttribute("googleQrcode", googleQrcode);
            model.addAttribute("googleKey", googleKey);
        }
        return FebsUtil.view("agent/agentBindGoogle");
    }
    
    //代理绑定谷歌
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/compelBindGoogle")
    public String compelBindGoogle(Model model){
        Agent agent = getCurrentAgent();
        if(agent==null)
        {
        	return "redirect:/logout";
        }
        if(StringUtils.isEmpty(agent.getGoogleKey())){
            String googleKey = GoogleAuthenticator.generateSecretKey();
            String googleQrcode = GoogleAuthenticator.getQRBarcode(getCurrentAgent().getAgentAccount(),googleKey);
            model.addAttribute("googleQrcode", googleQrcode);
            model.addAttribute("googleKey", googleKey);
        }
        String agentAccount = agent.getAgentAccount();
        model.addAttribute("agentAccount", agentAccount);
        return FebsUtil.view("agent/agentcompelBindGoogle");
    }

    //代理
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent")
    @RequiresPermissions("agent:view")
    public String agent(Model model){
        model.addAttribute("orgStatusEnums", OrgStatusEnums.values());
        return FebsUtil.view("agent/agent");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRechargeOrder/add/{id}")
    @RequiresPermissions("agentRechargeOrder:add")
    public String agentRechargeOrderAdd(@PathVariable String id, Model model) {
        model.addAttribute("type", StringUtils.isEmpty(getCurrentAgent().getGoogleKey())?"pay":"google");
        resolveAgentModel(id, model);
        return FebsUtil.view("rechargeOrder/agentRechargeOrderAdd");
    }

}
