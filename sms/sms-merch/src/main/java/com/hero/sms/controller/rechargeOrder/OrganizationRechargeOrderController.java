package com.hero.sms.controller.rechargeOrder;

import com.alibaba.fastjson.JSONObject;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.service.ValidateCodeService;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.channel.payTransfer.IPayService;
import com.hero.sms.service.rechargeOrder.IOrganizationRechargeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.Map;

/**
 * 商户充值订单 Controller
 *
 * @author Administrator
 * @date 2020-03-12 17:57:48
 */
@Slf4j
@Validated
@Controller
@RequestMapping("rechargeOrder")
public class OrganizationRechargeOrderController extends BaseController {

    @Autowired
    private IOrganizationRechargeOrderService organizationRechargeOrderService;
    @Autowired
    private ValidateCodeService validateCodeService;
    @Autowired
    private IPayService payService;

    @ControllerEndpoint( operation = "充值列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("rechargeOrder:list")
    public FebsResponse organizationRechargeOrderList(QueryRequest request, OrganizationRechargeOrder organizationRechargeOrder, Date orgReqStartTime, Date orgReqEndTime) {
        organizationRechargeOrder.setOrganizationCode(getCurrentUser().getOrganizationCode());
        Map<String, Object> dataTable = getDataTable(this.organizationRechargeOrderService.findOrganizationRechargeOrders(request, organizationRechargeOrder,orgReqStartTime,orgReqEndTime));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint( operation = "充值")
    @PostMapping("add")
    @ResponseBody
    @RequiresPermissions("rechargeOrder:add")
    public FebsResponse addOrganizationRechargeOrder(
            @NotBlank(message = "{required}") String rechargeAmount,
            @NotBlank(message = "{required}") String netwayCode,
            @NotBlank(message = "{required}") String verifyCode,
            HttpServletRequest request) {
        JSONObject data = null;
        try {
            HttpSession session = request.getSession();
            validateCodeService.check(session.getId(), verifyCode);
            data = JSONObject.parseObject("{}");
            data.put("rechargeAmount",rechargeAmount);
            data.put("netwayCode",netwayCode);
            data.put("organizationCode",getCurrentUser().getOrganizationCode());
            data.put("ip", IPUtil.getIpAddr(request));
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("充值失败",e);
            return new FebsResponse().message("充值失败").fail();
        }
        return payService.pay(data.toJSONString());
    }

}
