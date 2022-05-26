package com.hero.sms.controller.rechargeOrder;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.common.RecordedTypeEnums;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;
import com.hero.sms.utils.AjaxTokenProcessor;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
@Controller("rechargeOrderView")
public class ViewController extends BaseController {

    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private IAgentService agentService;
    @Autowired
    private ISendBoxService sendBoxService;
    @Autowired
    private IOrganizationRechargeOrderService organizationRechargeOrderService;

    //代理充值管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRechargeOrder/agentRechargeOrder")
    public String agentRechargeOrderIndex(Model model){
    	/**
    	 * 2021-04-07
    	 * 新增入账方式
    	 */
    	model.addAttribute("recordedTypeEnums", RecordedTypeEnums.values());
        return FebsUtil.view("rechargeOrder/agentRechargeOrder");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRechargeOrder/add/{id}")
    @RequiresPermissions("agentRechargeOrder:add")
    public String agentRechargeOrderAdd(HttpServletRequest request,@PathVariable String id, Model model) {
    	/**
         * @begin 2021-01-30
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        resolveAgentModel(id, model);
        return FebsUtil.view("rechargeOrder/agentRechargeOrderAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRechargeOrder/deducting/{id}")
    @RequiresPermissions("agentRechargeOrder:deducting")
    public String agentRechargeOrderDeducting(HttpServletRequest request,@PathVariable String id, Model model) {
    	/**
         * @begin 2021-01-30
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        resolveAgentModel(id, model);
        return FebsUtil.view("rechargeOrder/agentRechargeOrderDeducting");
    }

    //商户充值管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "orgRechargeOrder/orgRechargeOrder")
    public String organizationRechargeOrderIndex(Model model){
    	/**
    	 * 2021-04-07
    	 * 新增入账方式
    	 */
    	model.addAttribute("recordedTypeEnums", RecordedTypeEnums.values());
        return FebsUtil.view("rechargeOrder/organizationRechargeOrder");
    }


    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationRechargeOrder/deducting/{id}")
    @RequiresPermissions("organizationRechargeOrder:deducting")
    public String orgRechargeOrderDeducting(HttpServletRequest request,@PathVariable String id, Model model) {
        /**
         * @begin 2021-01-30
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        resolveOrganizationModel(id, model);
        return FebsUtil.view("rechargeOrder/organizationRechargeOrderDeducting");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationRechargeOrder/add/{id}")
    @RequiresPermissions("organizationRechargeOrder:add")
    public String organizationRechargeOrderAdd(HttpServletRequest request,@PathVariable String id, Model model) {
        /**
         * @begin 2021-01-30
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        resolveOrganizationModel(id, model);
        return FebsUtil.view("rechargeOrder/organizationRechargeOrderAdd");
    }

    //短信退还订单
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnSmsOrder/list")
    @RequiresPermissions("returnSmsOrder:list")
    public String returnSmsOrderIndex( Model model) {
        setTodayTimeStartAndEnd(model);
        return FebsUtil.view("rechargeOrder/returnSmsOrder");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "returnSmsOrder/result")
    public String eximportResult() {
        return FebsUtil.view("rechargeOrder/returnSmsOrderImportResult");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "returnSmsOrder/add/{id}")
    @RequiresPermissions("returnSmsOrder:add")
    public String returnSmsOrderAdd(@PathVariable String id,Model model ) {
        SendBox sendBox = this.sendBoxService.getById(id);
        Integer agentIncome = sendBox.getAgentIncomeAmount();
        Integer upAgentIncome = sendBox.getUpAgentIncomeAmount();
        Integer consumeAmount = sendBox.getConsumeAmount();
        Integer smsCount =sendBox.getSmsCount();

        Integer orgIncomeUnit = 0;
        Integer agentIncomeUnit = 0;
        Integer upAgentIncomeUnit = 0;
        if (smsCount > 0){
            agentIncomeUnit = agentIncome/smsCount;
            upAgentIncomeUnit = upAgentIncome/smsCount;
            orgIncomeUnit = consumeAmount/smsCount;
        }

        //费率信息
        model.addAttribute("orgIncomeUnit",orgIncomeUnit);
        model.addAttribute("agentIncomeUnit",agentIncomeUnit);
        model.addAttribute("upAgentIncomeUnit",upAgentIncomeUnit);
        //商户代理信息
        model.addAttribute("orgName", DatabaseCache.getOrgNameByOrgcode(sendBox.getOrgCode()));
        model.addAttribute("agentName", DatabaseCache.getAgentNameByAgentId(sendBox.getAgentId()));
        String upAgentName = DatabaseCache.getAgentNameByAgentId(sendBox.getUpAgentId());
        model.addAttribute("upAgentName", StringUtils.isNotBlank(upAgentName)?upAgentName:"&#8195;无&#8195;");

        model.addAttribute("maxReturnNum",smsCount);
        model.addAttribute("sendCode",sendBox.getSendCode());
        return FebsUtil.view("rechargeOrder/returnSmsOrderAdd");
    }

    private void resolveOrganizationModel(String id, Model model) {
        Organization organization = this.organizationService.getById(id);
        model.addAttribute("organization",organization);
    }

    private void resolveAgentModel(String id, Model model) {
        Agent agent = this.agentService.getById(id);
        model.addAttribute("agent",agent);
    }

    /**
     * @begin 2021-10-25
     * @param request
     * @param id
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationRechargeOrder/upNetwayCode/{id}")
    @RequiresPermissions("organizationRechargeOrder:upNetwayCode")
    public String organizationRechargeOrderUpNetwayCode(HttpServletRequest request,@PathVariable String id, Model model) 
    {
    	OrganizationRechargeOrder organizationRechargeOrder = organizationRechargeOrderService.getById(id);
    	model.addAttribute("organizationRechargeOrder",organizationRechargeOrder);
        return FebsUtil.view("rechargeOrder/organizationRechargeOrderUpNetwayCode");
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
