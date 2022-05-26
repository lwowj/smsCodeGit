package com.hero.sms.controller.agentRemitOrder;

import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.service.agent.IAgentService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author Administrator
 */
@Controller("agentRemitOrderView")
public class ViewController extends BaseController {

    @Autowired
    private IAgentService agentService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRemitOrder/agentRemitOrder")
    public String agentRemitOrderIndex(Model model){
        model.addAttribute("AuditStateEnums", AuditStateEnums.values());
        return FebsUtil.view("agentRemitOrder/agentRemitOrder");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRemitOrder/add")
    @RequiresPermissions("agentRemitOrder:add")
    public String agentRemitOrderAdd(Model model){
        Agent agent = this.agentService.getById(getCurrentAgent().getId());
        model.addAttribute("type", StringUtils.isEmpty(agent.getGoogleKey())?"pay":"google");
        model.addAttribute("AvailableAmount", new DecimalFormat("0.00").format(new BigDecimal(agent.getAvailableAmount()).divide(new BigDecimal("100"))));
        return FebsUtil.view("agentRemitOrder/agentRemitOrderAdd");
    }


    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRemitOrder/transfer")
    @RequiresPermissions("agentRemitOrder:transfer")
    public String agentRemitOrderTransfer(Model model){
        Agent agent = this.agentService.getById(getCurrentAgent().getId());
        model.addAttribute("type", StringUtils.isEmpty(agent.getGoogleKey())?"pay":"google");
        model.addAttribute("AvailableAmount", new DecimalFormat("0.00").format(new BigDecimal(agent.getAvailableAmount()).divide(new BigDecimal("100"))));
        return FebsUtil.view("agentRemitOrder/agentRemitOrderTransfer");
    }
}
