package com.hero.sms.controller.message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.enums.message.SmsTypeEnums;

@Controller
public class ViewController extends BaseController {

    /**
     * 发件箱首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/list")
    @RequiresPermissions("sendBox:list")
    public String sendBoxIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("auditStateEnums", AuditStateEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/sendBox");
    }
    /**
     * 下级代理发件箱首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/subordinateList")
    @RequiresPermissions("sendBox:subordinateList")
    public String subordinateSendBoxIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("auditStateEnums", AuditStateEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/subordinateSendBox");
    }

    /**
     * 历史发件箱首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBoxHistory/list")
    @RequiresPermissions("sendBoxHistory:list")
    public String sendBoxHistoryIndex(Model model){
        setStartTimeAndEndTime(model,11,11);
        model.addAttribute("auditStateEnums", AuditStateEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/sendBoxHistory");
    }

    /**
     * 下级代理历史发件箱首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBoxHistory/subordinateList")
    @RequiresPermissions("sendBoxHistory:subordinateList")
    public String subordinateSendBoxHistoryIndex(Model model){
        setStartTimeAndEndTime(model,11,11);
        model.addAttribute("auditStateEnums", AuditStateEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/subordinateSendBoxHistory");
    }

    /**
     * 日报表
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "statisticalAgent/list")
    @RequiresPermissions("statisticalAgent:list")
    public String statisticalAgentIndex(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(),LocalTime.MIN);
        model.addAttribute("dateStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("dateEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return FebsUtil.view("message/statisticalAgent");
    }

    /**
     * 批次成功率首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/rateSuccess")
    @RequiresPermissions("sendRecord:rateSuccess")
    public String rateSuccess(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("smsNumberAreaProvinceEnums", SmsNumberAreaProvinceEnums.values());
        return FebsUtil.view("message/rateSuccess");
    }


    /**
     * 下级代理发送记录首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/subordinateList")
    @RequiresPermissions("sendRecord:subordinateList")
    public String subordinateSendRecordIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("sendRecordStateEnums", SendRecordStateEnums.values());
        model.addAttribute("smsNumberAreaProvinceEnums", SmsNumberAreaProvinceEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/subordinateSendRecord");
    }

    /**
     * 发送记录首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/list")
    @RequiresPermissions("sendRecord:list")
    public String sendRecordIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("sendRecordStateEnums", SendRecordStateEnums.values());
        model.addAttribute("smsNumberAreaProvinceEnums", SmsNumberAreaProvinceEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/sendRecord");
    }

    /**
     * 历史发送记录首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecordHistory/list")
    @RequiresPermissions("sendRecordHistory:list")
    public String sendRecordHistoryIndex(Model model){
        setStartTimeAndEndTime(model,11,11);
        model.addAttribute("sendRecordStateEnums", SendRecordStateEnums.values());
        model.addAttribute("smsNumberAreaProvinceEnums", SmsNumberAreaProvinceEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/sendRecordHistory");
    }

    /**
     * 下级代理历史发送记录首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecordHistory/subordinateList")
    @RequiresPermissions("sendRecordHistory:subordinateList")
    public String subordinateSendRecordHistoryIndex(Model model){
        setStartTimeAndEndTime(model,11,11);
        model.addAttribute("sendRecordStateEnums", SendRecordStateEnums.values());
        model.addAttribute("smsNumberAreaProvinceEnums", SmsNumberAreaProvinceEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/subordinateSendRecordHistory");
    }


    /**
     * 定时短信首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/timing/list")
    @RequiresPermissions("sendBox:list")
    public String timingSendBoxIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("auditStateEnums",AuditStateEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/timingSendBox");
    }
    /**
     * 子代理定时短信首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/timing/subordinateList")
    @RequiresPermissions("sendBox:subordinateList")
    public String subordinateTimingSendBoxIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("auditStateEnums",AuditStateEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
        if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
        {
        	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        }
        else
        {
        	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        }
        return FebsUtil.view("message/subordinateTimingSendBox");
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

    /**
     * 回传往前推N天的开始时间 和 今天结束时间
     * 时间格式 yyyy-MM-dd HH:mm:ss
     * @param model
     */
    protected void setStartTimeAndTodayEnd(Model model,int n){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(n), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(),LocalTime.MAX);
        model.addAttribute("todayStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("todayEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    /**
     * 回传往前推N天的开始时间 和 往前推N天的结束时间
     * 时间格式 yyyy-MM-dd HH:mm:ss
     * @param model
     */
    protected void setStartTimeAndEndTime(Model model,int start,int end){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(start), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().minusDays(end),LocalTime.MAX);
        model.addAttribute("todayStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("todayEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
