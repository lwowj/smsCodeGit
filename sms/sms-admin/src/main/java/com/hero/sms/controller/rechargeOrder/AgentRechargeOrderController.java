package com.hero.sms.controller.rechargeOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.rechargeOrder.AgentRechargeOrder;
import com.hero.sms.entity.rechargeOrder.StatisticBean;
import com.hero.sms.enums.common.RecordedTypeEnums;
import com.hero.sms.service.rechargeOrder.IAgentRechargeOrderService;
import com.hero.sms.system.entity.User;
import com.hero.sms.system.service.IUserService;
import com.hero.sms.utils.AjaxTokenProcessor;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 代理充值订单 Controller
 *
 * @author Administrator
 * @date 2020-03-12 18:00:59
 */
@Slf4j
@Validated
@Controller
@RequestMapping("agentRechargeOrder")
public class AgentRechargeOrderController extends BaseController {

    @Autowired
    private IAgentRechargeOrderService agentRechargeOrderService;

    @Autowired
    private IUserService userService;

    @ControllerEndpoint(operation = "代理充值订单列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("agentRechargeOrder:list")
    public FebsResponse agentRechargeOrderList(QueryRequest request, AgentRechargeOrder agentRechargeOrder, Date agentReqStartTime, Date agentReqEndTime) {
        Map<String, Object> dataTable = getDataTable(this.agentRechargeOrderService.findAgentRechargeOrders(request, agentRechargeOrder,agentReqStartTime,agentReqEndTime));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "代理充值订单统计")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("agentRechargeOrder:statistic")
    public FebsResponse agentRechargeOrderStatistic(AgentRechargeOrder agentRechargeOrder, Date agentReqStartTime, Date agentReqEndTime){
        List<StatisticBean> list = this.agentRechargeOrderService.statisticBeanList(agentRechargeOrder,agentReqStartTime,agentReqEndTime);
        return new FebsResponse().success().data(list);
    }

    @ControllerEndpoint(operation = "代理充值订单新增")
    @PostMapping
    @ResponseBody
    @Limit(key = "agentRechargeOrder", period = 3, count = 1, name = "代理充值", prefix = "limit")
    @RequiresPermissions("agentRechargeOrder:add")
    public FebsResponse addAgentRechargeOrder(@Valid AgentRechargeOrder agentRechargeOrder, @NotBlank(message = "{required}") String verifyCode,String sessionToken,HttpServletRequest request) {
        try {
        	/**
             * @begin 2021-01-30
             * 新增校验token，防止重复提交
             */
            String checkSessionTokenSwitch = "ON";
    		Code checkSessionTokenSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkSessionTokenSwitch");
    	    if(checkSessionTokenSwitchCode!=null&&!"".equals(checkSessionTokenSwitchCode.getName()))
    	    {
    	    	checkSessionTokenSwitch = checkSessionTokenSwitchCode.getName();
    	    }
    	    if(!"OFF".equals(checkSessionTokenSwitch))
    	    {
    	    	AjaxTokenProcessor token = AjaxTokenProcessor.getInstance();
    	        if (!token.isTokenValid(request,true,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY, sessionToken)) 
    			{
    	        	return new FebsResponse().message("当前请求无效,请关闭窗口，重新打开再试").fail();
    			}
    	    }
            /**
             * @end
             */
            User user = super.getCurrentUser();
            userService.checkGoogleKey(user.getUsername(), verifyCode);
            agentRechargeOrder.setCeateUser(user.getUsername());
            this.agentRechargeOrderService.createAgentRechargeOrder(agentRechargeOrder);
        } catch (FebsException e) {
        	FebsResponse febsResponse = new FebsResponse();
            febsResponse.message(e.getMessage());
            String newSessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
            febsResponse.put("sessionToken", newSessionToken);
            return febsResponse.fail();
        } catch (Exception e) {
            log.error("代理充值订单新增失败",e);
            FebsResponse febsResponse = new FebsResponse();
            febsResponse.message("代理充值订单新增失败");
            String newSessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
            febsResponse.put("sessionToken", newSessionToken);
            return febsResponse.fail();
        }
        return new FebsResponse().success().data(agentRechargeOrder);
    }

    @ControllerEndpoint(operation = "代理扣除订单新增")
    @PostMapping("deducting")
    @ResponseBody
    @RequiresPermissions("agentRechargeOrder:deducting")
    public FebsResponse deductingAgentRechargeOrder(@Valid AgentRechargeOrder agentRechargeOrder, @NotBlank(message = "{required}") String verifyCode,String sessionToken,HttpServletRequest request) {
        try {
        	/**
             * @begin 2021-01-30
             * 新增校验token，防止重复提交
             */
            String checkSessionTokenSwitch = "ON";
    		Code checkSessionTokenSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkSessionTokenSwitch");
    	    if(checkSessionTokenSwitchCode!=null&&!"".equals(checkSessionTokenSwitchCode.getName()))
    	    {
    	    	checkSessionTokenSwitch = checkSessionTokenSwitchCode.getName();
    	    }
    	    if(!"OFF".equals(checkSessionTokenSwitch))
    	    {
    	    	AjaxTokenProcessor token = AjaxTokenProcessor.getInstance();
    	        if (!token.isTokenValid(request,true,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY, sessionToken)) 
    			{
    	        	return new FebsResponse().message("当前请求无效,请关闭窗口，重新打开再试").fail();
    			}
    	    }
            /**
             * @end
             */
            User user = super.getCurrentUser();
            userService.checkGoogleKey(user.getUsername(), verifyCode);
            agentRechargeOrder.setCeateUser(user.getUsername());
            agentRechargeOrder.setNetwayCode("DEDUCTING");
            this.agentRechargeOrderService.deducting(agentRechargeOrder);
        } catch (FebsException e) {
        	FebsResponse febsResponse = new FebsResponse();
            febsResponse.message(e.getMessage());
            String newSessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
            febsResponse.put("sessionToken", newSessionToken);
            return febsResponse.fail();
        } catch (Exception e) {
            log.error("代理扣除订单新增失败",e);
            FebsResponse febsResponse = new FebsResponse();
            febsResponse.message("代理扣除订单新增失败");
            String newSessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
            febsResponse.put("sessionToken", newSessionToken);
            return febsResponse.fail();
        }
        return new FebsResponse().success().data(agentRechargeOrder);
    }

    @ControllerEndpoint(operation = "代理充值订单更新")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("agentRechargeOrder:update")
    public FebsResponse updateAgentRechargeOrder(AgentRechargeOrder agentRechargeOrder) {
        this.agentRechargeOrderService.updateAgentRechargeOrder(agentRechargeOrder);
        return new FebsResponse().success();
    }

    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("agentRechargeOrder:export")
    public void export(QueryRequest queryRequest, AgentRechargeOrder agentRechargeOrder, HttpServletResponse response) {
        List<AgentRechargeOrder> agentRechargeOrders = this.agentRechargeOrderService.findAgentRechargeOrders(queryRequest, agentRechargeOrder,null,null).getRecords();
        ExcelKit.$Export(AgentRechargeOrder.class, response).downXlsx(agentRechargeOrders, false);
    }
    
    /**
     * 2021-04-07
     * @return
     */
    @ControllerEndpoint(operation = "代理充值订单入账")
    @GetMapping("booked/{agentRechargeOrderIds}")
    @ResponseBody
    @RequiresPermissions("agentRechargeOrder:booked")
    public FebsResponse bookedAgentRechargeOrder(@NotBlank(message = "{required}") @PathVariable String agentRechargeOrderIds) {
        String[] rechargeOrderIds = agentRechargeOrderIds.split(StringPool.COMMA);
        LambdaUpdateWrapper<AgentRechargeOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(AgentRechargeOrder::getRecordedType, RecordedTypeEnums.Booked.getCode())
        		.set(AgentRechargeOrder::getRechargeCompleteTime, DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN))
                .eq(AgentRechargeOrder::getRecordedType,RecordedTypeEnums.UnBooked.getCode())
                .in(AgentRechargeOrder::getId,rechargeOrderIds);
        this.agentRechargeOrderService.update(wrapper);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理充值订单挂账")
    @GetMapping("unbooked/{agentRechargeOrderIds}")
    @ResponseBody
    @RequiresPermissions("agentRechargeOrder:unbooked")
    public FebsResponse unBookedAgentRechargeOrder(@NotBlank(message = "{required}") @PathVariable String agentRechargeOrderIds) {
        String[] rechargeOrderIds = agentRechargeOrderIds.split(StringPool.COMMA);
        LambdaUpdateWrapper<AgentRechargeOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(AgentRechargeOrder::getRecordedType, RecordedTypeEnums.UnBooked.getCode())
        .set(AgentRechargeOrder::getRechargeCompleteTime, null)
        .eq(AgentRechargeOrder::getRecordedType,RecordedTypeEnums.Booked.getCode())
        .in(AgentRechargeOrder::getId,rechargeOrderIds);
        this.agentRechargeOrderService.update(wrapper);
        return new FebsResponse().success();
    }
}
