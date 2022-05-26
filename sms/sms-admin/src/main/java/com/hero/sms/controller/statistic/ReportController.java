package com.hero.sms.controller.statistic;

import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.enums.BaseEnum;
import com.hero.sms.service.message.IReportService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("report")
public class ReportController extends BaseController {

    @Autowired
    private IReportService reportService;


    @GetMapping("orgSendCountToday")
    //@RequiresPermissions("report:orgSendCountToday")
    public FebsResponse orgSendCountToday(){
        return new FebsResponse().success().data(reportService.orgSendCountToday());
    }

    @GetMapping("operatorSendCountToday")
    //@RequiresPermissions("report:operatorSendCountToday")
    public FebsResponse operatorSendCountToday(){
        return new FebsResponse().success().data(reportService.operatorSendCountToday());
    }

    @GetMapping("provinceSendCountToday")
    //@RequiresPermissions("report:provinceSendCountToday")
    public FebsResponse provinceSendCountToday(){
        return new FebsResponse().success().data(reportService.provinceSendCountToday());
    }
}
