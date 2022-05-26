package com.hero.sms.controller.rechargeOrder;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.common.Code;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author Administrator
 */
@Controller("rechargeOrderView")
public class ViewController extends BaseController {

    //商户充值管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "rechargeOrder/list")
    @RequiresPermissions("rechargeOrder:list")
    public String organizationRechargeOrderIndex(){
        return FebsUtil.view("rechargeOrder/organizationRechargeOrder");
    }

    //商户充值管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "rechargeOrder/add")
    @RequiresPermissions("rechargeOrder:add")
    public String organizationRechargeOrderAdd(Model model){
        List<Code> listNetwayCode = DatabaseCache.getCodeListBySortCodeAndUpCode("NetwayCode","SC");
        model.addAttribute("listNetwayCode",listNetwayCode);
        return FebsUtil.view("rechargeOrder/organizationRechargeOrderAdd");
    }

    //短信退还订单
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnSmsOrder/list")
    @RequiresPermissions("returnSmsOrder:list")
    public String returnSmsOrderIndex( Model model) {
        setTodayTimeStartAndEnd(model);
        return FebsUtil.view("rechargeOrder/returnSmsOrder");
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
