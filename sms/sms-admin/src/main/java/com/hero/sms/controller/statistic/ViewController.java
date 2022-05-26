package com.hero.sms.controller.statistic;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Controller("statisticView")
public class ViewController extends BaseController {

    /**
     * 日报表首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "statisticalOrgcode/list")
    @RequiresPermissions("statisticalOrgcode:list")
    public String statisticalOrgcodeIndex(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().minusDays(1),LocalTime.MIN);
        model.addAttribute("dateStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("dateEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return FebsUtil.view("report/statisticalOrgcode");
    }

    /**
     * 商务日报表
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "statisticalOrgcode/businessList")
    @RequiresPermissions("statisticalOrgcode:businessList")
    public String statisticalBusinessList(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().minusDays(1),LocalTime.MIN);
        model.addAttribute("dateStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("dateEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return FebsUtil.view("report/statisticalBusiness");
    }

    /**
     * 商务月报表
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "statisticalOrgcode/businessOnMonthList")
    @RequiresPermissions("statisticalOrgcode:businessOnMonthList")
    public String statisticalBusinessOnMonthList(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusMonths(1), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(),LocalTime.MIN);
        model.addAttribute("dateStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        model.addAttribute("dateEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        List<Map<String,String>> list = new ArrayList<>();
        for (Code code: DatabaseCache.getCodeListBySortCode("Business")) {
            Map<String,String> map = new HashMap<>();
            map.put(code.getCode(),code.getName());
            list.add(map);
        }
        model.addAttribute("businessList",list);
        return FebsUtil.view("report/statisticalBusinessOnMonth");
    }

    /**
     * 商户发送量统计
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "statisticalOrgcode/totalList")
    @RequiresPermissions("statisticalOrgcode:totalList")
    public String statisticalOrgcodeTotalList(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().minusDays(1),LocalTime.MIN);
        model.addAttribute("dateStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("dateEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return FebsUtil.view("report/statisticalOrgcodeTotal");
    }

    /**
     * 日报表
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "statisticalAgent/list")
    @RequiresPermissions("statisticalAgent:list")
    public String statisticalAgentIndex(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().minusDays(1),LocalTime.MIN);
        model.addAttribute("dateStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("dateEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return FebsUtil.view("report/statisticalAgent");
    }
    /**
     * 日报表首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "statisticalOrgcode/sumStatisticList")
    @RequiresPermissions("statisticalOrgcode:sumStatisticList")
    public String sumStatisticalOrgcodeIndex(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(8), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().minusDays(1),LocalTime.MIN);
        model.addAttribute("dateStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("dateEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return FebsUtil.view("report/sumStatisticalOrgcode");
    }

    /**
     * 通道日报表首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "statisticalChannel/list")
    @RequiresPermissions("statisticalChannel:list")
    public String statisticalChannelIndex(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().minusDays(1),LocalTime.MIN);
        model.addAttribute("dateStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("dateEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return FebsUtil.view("report/statisticalChannel");
    }
}
