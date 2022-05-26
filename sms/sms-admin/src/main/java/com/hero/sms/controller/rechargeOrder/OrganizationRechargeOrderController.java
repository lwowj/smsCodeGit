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
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrderQuery;
import com.hero.sms.entity.rechargeOrder.StatisticBean;
import com.hero.sms.enums.common.RecordedTypeEnums;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;
import com.hero.sms.system.entity.User;
import com.hero.sms.system.service.IUserService;
import com.hero.sms.utils.AjaxTokenProcessor;
import com.hero.sms.utils.StringUtil;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 商户充值订单 Controller
 *
 * @author Administrator
 * @date 2020-03-12 17:57:48
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organizationRechargeOrder")
public class OrganizationRechargeOrderController extends BaseController {

    @Autowired
    private IOrganizationRechargeOrderService organizationRechargeOrderService;
    @Autowired
    private IUserService userService;

    @ControllerEndpoint(operation = "商户充值订单列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:list")
    public FebsResponse organizationRechargeOrderList(QueryRequest request, OrganizationRechargeOrderQuery organizationRechargeOrder) {
        Map<String, Object> dataTable = getDataTable(this.organizationRechargeOrderService.extPage(request, organizationRechargeOrder));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "商户充值订单统计")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:statistic")
    public FebsResponse organizationRechargeOrderList(OrganizationRechargeOrderQuery organizationRechargeOrder) {

        List<StatisticBean> list = this.organizationRechargeOrderService.statisticBeanList(organizationRechargeOrder);

        return new FebsResponse().success().data(list);
    }

    @ControllerEndpoint(operation = "商户充值订单新增")
    @PostMapping
    @ResponseBody
    @Limit(key = "organizationRechargeOrder", period = 3, count = 1, name = "商户充值", prefix = "limit")
    @RequiresPermissions("organizationRechargeOrder:add")
    public FebsResponse addOrganizationRechargeOrder(@Valid OrganizationRechargeOrder organizationRechargeOrder, @NotBlank(message = "{required}") String verifyCode,String sessionToken,HttpServletRequest request) {
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
            organizationRechargeOrder.setCeateUser(user.getUsername());
            this.organizationRechargeOrderService.createOrganizationRechargeOrder(organizationRechargeOrder);
        } catch (FebsException e) {
        	FebsResponse febsResponse = new FebsResponse();
            febsResponse.message(e.getMessage());
            String newSessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
            febsResponse.put("sessionToken", newSessionToken);
            return febsResponse.fail();
        } catch (Exception e) {
            log.error("商户充值订单新增失败",e);
            FebsResponse febsResponse = new FebsResponse();
            febsResponse.message("商户充值订单新增失败");
            String newSessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
            febsResponse.put("sessionToken", newSessionToken);
            return febsResponse.fail();
        }
        return new FebsResponse().success().data(organizationRechargeOrder);
    }

    @ControllerEndpoint(operation = "商户扣除订单新增")
    @PostMapping("deducting")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:deducting")
    public FebsResponse deductingOrganizationRechargeOrder(@Valid OrganizationRechargeOrder organizationRechargeOrder, @NotBlank(message = "{required}") String verifyCode,String sessionToken,HttpServletRequest request) {
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
            organizationRechargeOrder.setCeateUser(user.getUsername());
            organizationRechargeOrder.setNetwayCode("DEDUCTING");
            this.organizationRechargeOrderService.deducting(organizationRechargeOrder);
        } catch (FebsException e) {
        	 FebsResponse febsResponse = new FebsResponse();
             febsResponse.message(e.getMessage());
             String newSessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
             febsResponse.put("sessionToken", newSessionToken);
             return febsResponse.fail();
        } catch (Exception e) {
            log.error("商户扣除订单新增失败",e);
            FebsResponse febsResponse = new FebsResponse();
            febsResponse.message("商户扣除订单新增失败");
            String newSessionToken = getTokenInputStr(request,AjaxTokenProcessor.RECHARGE_TRANSACTION_TOKEN_KEY);
            febsResponse.put("sessionToken", newSessionToken);
            return febsResponse.fail();
        }
        return new FebsResponse().success().data(organizationRechargeOrder);
    }

    @ControllerEndpoint(operation = "商户充值订单更新")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:update")
    public FebsResponse updateOrganizationRechargeOrder(OrganizationRechargeOrder organizationRechargeOrder) {
        this.organizationRechargeOrderService.updateOrganizationRechargeOrder(organizationRechargeOrder);
        return new FebsResponse().success();
    }

    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:export")
    public void export(QueryRequest queryRequest, OrganizationRechargeOrder organizationRechargeOrder, HttpServletResponse response) {
        List<OrganizationRechargeOrder> organizationRechargeOrders = this.organizationRechargeOrderService.findOrganizationRechargeOrders(queryRequest, organizationRechargeOrder,null,null).getRecords();
        ExcelKit.$Export(OrganizationRechargeOrder.class, response).downXlsx(organizationRechargeOrders, false);
    }
    
    /**
     * 2021-04-07
     * @return
     */
    @ControllerEndpoint(operation = "商户充值订单入账")
    @GetMapping("booked/{organizationRechargeOrderIds}")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:booked")
    public FebsResponse bookedOrganizationRechargeOrder(@NotBlank(message = "{required}") @PathVariable String organizationRechargeOrderIds) {
        String[] orgRechargeOrderIds = organizationRechargeOrderIds.split(StringPool.COMMA);
        String[] recordeTypes = new String[2];
        recordeTypes[0] = RecordedTypeEnums.UnBooked.getCode();
        recordeTypes[1] = RecordedTypeEnums.Giving.getCode();
        
        LambdaUpdateWrapper<OrganizationRechargeOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(OrganizationRechargeOrder::getRecordedType, RecordedTypeEnums.Booked.getCode())
        		.set(OrganizationRechargeOrder::getRechargeCompleteTime, DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN))
//                .eq(OrganizationRechargeOrder::getRecordedType,RecordedTypeEnums.UnBooked.getCode())
        		.in(OrganizationRechargeOrder::getRecordedType,recordeTypes)
                .in(OrganizationRechargeOrder::getId,orgRechargeOrderIds);
        this.organizationRechargeOrderService.update(wrapper);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "商户充值订单挂账")
    @GetMapping("unbooked/{organizationRechargeOrderIds}")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:unbooked")
    public FebsResponse unBookedOrganizationRechargeOrder(@NotBlank(message = "{required}") @PathVariable String organizationRechargeOrderIds) {
        String[] orgRechargeOrderIds = organizationRechargeOrderIds.split(StringPool.COMMA);
        String[] recordeTypes = new String[2];
        recordeTypes[0] = RecordedTypeEnums.Booked.getCode();
        recordeTypes[1] = RecordedTypeEnums.Giving.getCode();
        
        LambdaUpdateWrapper<OrganizationRechargeOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(OrganizationRechargeOrder::getRecordedType, RecordedTypeEnums.UnBooked.getCode())
        .set(OrganizationRechargeOrder::getRechargeCompleteTime, null)
//        .eq(OrganizationRechargeOrder::getRecordedType,RecordedTypeEnums.Booked.getCode())
        .in(OrganizationRechargeOrder::getRecordedType,recordeTypes)
        .in(OrganizationRechargeOrder::getId,orgRechargeOrderIds);
        this.organizationRechargeOrderService.update(wrapper);
        return new FebsResponse().success();
    }
    
    /**
     * 2021-09-25
     * @return
     */
    @ControllerEndpoint(operation = "商户充值订单赠送")
    @GetMapping("giving/{organizationRechargeOrderIds}")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:giving")
    public FebsResponse givingOrganizationRechargeOrder(@NotBlank(message = "{required}") @PathVariable String organizationRechargeOrderIds) {
        String[] orgRechargeOrderIds = organizationRechargeOrderIds.split(StringPool.COMMA);
        String[] recordeTypes = new String[2];
        recordeTypes[0] = RecordedTypeEnums.Booked.getCode();
        recordeTypes[1] = RecordedTypeEnums.UnBooked.getCode();
        
        LambdaUpdateWrapper<OrganizationRechargeOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(OrganizationRechargeOrder::getRecordedType, RecordedTypeEnums.Giving.getCode())
        		.set(OrganizationRechargeOrder::getRechargeCompleteTime, DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN))
//        		.eq(OrganizationRechargeOrder::getRecordedType,RecordedTypeEnums.UnBooked.getCode())
        		.in(OrganizationRechargeOrder::getRecordedType,recordeTypes)
                .in(OrganizationRechargeOrder::getId,orgRechargeOrderIds);
        this.organizationRechargeOrderService.update(wrapper);
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "商户充值订单交易方式修改")
    @PostMapping("upNetwayCode")
    @ResponseBody
    @RequiresPermissions("organizationRechargeOrder:upNetwayCode")
    public FebsResponse uncOrganizationRechargeOrder(@Valid OrganizationRechargeOrder organizationRechargeOrder) {
        try {
        	if(StringUtil.isNotBlank(organizationRechargeOrder.getOrderNo())&&StringUtil.isNotBlank(String.valueOf(organizationRechargeOrder.getId())))
        	{
        		 LambdaUpdateWrapper<OrganizationRechargeOrder> wrapper = new LambdaUpdateWrapper<>();
                 wrapper.set(OrganizationRechargeOrder::getNetwayCode, organizationRechargeOrder.getNetwayCode())
                 		.eq(OrganizationRechargeOrder::getOrderNo,organizationRechargeOrder.getOrderNo())
                         .eq(OrganizationRechargeOrder::getId,organizationRechargeOrder.getId());
                 this.organizationRechargeOrderService.update(wrapper);
        	}
        	else
        	{
        		 FebsResponse febsResponse = new FebsResponse();
                 febsResponse.message("商户充值订单交易方式修改失败");
                 return febsResponse.fail();
        	}
        } catch (FebsException e) {
        	FebsResponse febsResponse = new FebsResponse();
            febsResponse.message(e.getMessage());
            return febsResponse.fail();
        } catch (Exception e) {
            log.error("商户充值订单交易方式修改失败",e);
            FebsResponse febsResponse = new FebsResponse();
            febsResponse.message("商户充值订单交易方式修改失败");
            return febsResponse.fail();
        }
        return new FebsResponse().success().data(organizationRechargeOrder);
    }
}
