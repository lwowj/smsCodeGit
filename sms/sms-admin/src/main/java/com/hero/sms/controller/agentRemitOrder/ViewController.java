package com.hero.sms.controller.agentRemitOrder;

import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.service.agentRemitOrder.IAgentRemitOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Administrator
 */
@Controller("agentRemitOrderView")
public class ViewController extends BaseController {


    @Autowired
    private IAgentRemitOrderService agentRemitOrderService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRemitOrder/agentRemitOrder")
    public String agentRemitOrderIndex(Model model){
        model.addAttribute("AuditStateEnums", AuditStateEnums.values());
        return FebsUtil.view("agentRemitOrder/agentRemitOrder");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentRemitOrder/auditSuccess/{id}")
    public String auditSuccess(Model model,@PathVariable Integer id){
        model.addAttribute("agentRemitOrder", agentRemitOrderService.getById(id));
        return FebsUtil.view("agentRemitOrder/agentRemitOrderAuditSuccess");
    }

}
