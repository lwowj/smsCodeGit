package com.hero.sms.controller.export;

import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.enums.common.ExportTypeEnums;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 *
 */
@Controller("exportView")
public class ViewController extends BaseController {

    /**
     * 管理后台导出下载首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "exportRecord/list")
    @RequiresPermissions("exportRecord:list")
    public String exportRecordIndex(Model model){
        model.addAttribute("exportTypeEnums", ExportTypeEnums.values());
        setTodayTimeStartAndEnd(model);
        return FebsUtil.view("export/exportRecord");
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
