package com.hero.sms.controller.agent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.utils.GoogleAuthenticator;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentCost;
import com.hero.sms.entity.agent.AgentMenuLimit;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.common.NeedBindGoogleKeyEnums;
import com.hero.sms.enums.common.OperatorEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.agent.IAgentCostService;
import com.hero.sms.service.agent.IAgentMenuLimitService;
import com.hero.sms.service.agent.IAgentService;

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


    //代理
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent")
    @RequiresPermissions("agent:view")
    public String agent(Model model){
        model.addAttribute("orgStatusEnums", OrgStatusEnums.values());
        model.addAttribute("needBindGoogleKeyEnums", NeedBindGoogleKeyEnums.values());
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
        model.addAttribute("operatorEnums", OperatorEnums.values());
        return FebsUtil.view("agent/agent");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/add")
    @RequiresPermissions("agent:add")
    public String systemAgentAdd(Model model) {
        model.addAttribute("agents", this.agentService.getUpAgents());
        return FebsUtil.view("agent/agentAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/update/{id}")
    @RequiresPermissions("agent:update")
    public String agentUpdate(@PathVariable String id, Model model) {
        resolveAgentModel(id, model);
        model.addAttribute("needBindGoogleKeyEnums", NeedBindGoogleKeyEnums.values());
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
        Agent agent = this.agentService.getById(id);
        model.addAttribute("agent",agent);
    }

    private void resolveAgentCostsModel(int id, Model model) {
        AgentCost agentCost = new AgentCost();
        agentCost.setAgentId(id);
        List<AgentCost> agentCosts = this.agentCostService.findAgentCosts(agentCost);
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
        model.addAttribute("agentCosts",agentCosts);
        model.addAttribute("id",id);
    }

    private void resolveAgentMenuLimitModel(Long id, Model model) {
        AgentMenuLimit limit = new AgentMenuLimit();
        limit.setAgentId(id);
        List<AgentMenuLimit> limits = this.agentMenuLimitService.findAgentMenuLimits(limit);
        model.addAttribute("limits",limits);
        model.addAttribute("id",id);
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/agentMenu")
    @RequiresPermissions("agentMenu:list")
    public String systemMenu() {
        return FebsUtil.view("agent/agentMenu");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/googleKey/{id}")
    @RequiresPermissions("agent:google:key")
    public String getUserGoogleKey(@PathVariable Long id, Model model) {
        Agent agent = this.agentService.getById(id);
        if(StringUtils.isBlank( agent.getGoogleKey())){
            model.addAttribute("googleQrcode", "null");
        }else{
            String googleQrcode = GoogleAuthenticator.getQRBarcode(agent.getAgentAccount(), agent.getGoogleKey());
            model.addAttribute("googleQrcode", googleQrcode);
            model.addAttribute("agentAccount", agent.getAgentName());
        }
        return FebsUtil.view("agent/googleKey");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentSystemConfig/list")
    public String agentSystemConfigIndex(Model model){
        model.addAttribute("orgApproveStateEnums", OrgApproveStateEnums.values());
        return FebsUtil.view("agent/agentSystemConfig");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/agentLog")
    public String agentLogIndex(){
        return FebsUtil.view("agent/agentLog");
    }
    
    //代理登录日志
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/agentLoginLog")
    @RequiresPermissions("agentLoginLog:view")
    public String agentLoginLog(Model model)
    {
    	setTodayTimeStartAndEnd(model);
    	model.addAttribute("commonStateEnums",  CommonStateEnums.values());
        return FebsUtil.view("agent/agentLoginLog");
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
}
