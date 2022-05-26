package com.hero.sms.controller.message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SmsTemplate;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.message.ISmsTemplateService;
import com.hero.sms.service.organization.IOrganizationCostService;
import com.hero.sms.utils.AjaxTokenProcessor;

@Controller
public class ViewController extends BaseController {

    @Autowired
    private ISmsTemplateService smsTemplateService;
    
    @Autowired
    private IOrganizationCostService organizationCostService;

    /**
     * 表格发送
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/submit/table")
    @RequiresPermissions("sendBox:add")
    public String sendBoxSubmitByTable(Model model,HttpServletRequest request){
        setTodayStartAndMinusDaysEnd(model,-2);
        Code sendDefaultPromptCode = DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig", "sendDefaultPrompt");
        String sendDefaultPrompt = "";
        if (sendDefaultPromptCode != null && sendDefaultPromptCode.getName() != null)
        {
        	sendDefaultPrompt = sendDefaultPromptCode.getName();
        }
        /**
         * @begin 2020-10-09
         * * 新增号码地区选择
         */
        model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        /**
         * @end
         */
        model.addAttribute("sendDefaultPrompt", sendDefaultPrompt);
        /**
         * @begin 2020-12-12
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        return FebsUtil.view("message/sendBoxSubmitByTable");
    }

    /**
     * 我要发送
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/submit/form")
    @RequiresPermissions("sendBox:add")
    public String sendBoxSubmitByForm(Model model,HttpServletRequest request){
        setTodayStartAndMinusDaysEnd(model,-2);
        Code sendDefaultPromptCode = DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig", "sendDefaultPrompt");
        String sendDefaultPrompt = "";
        if (sendDefaultPromptCode != null && sendDefaultPromptCode.getName() != null)
        {
        	sendDefaultPrompt = sendDefaultPromptCode.getName();
        }
        /**
         * @begin 2020-10-09
         * * 新增号码地区选择
         */
        model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        /**
         * @end
         */
        /**
         * @end
         */
        model.addAttribute("sendDefaultPrompt", sendDefaultPrompt);
        /**
         * @begin 2020-12-12
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        return FebsUtil.view("message/sendBoxSubmitByForm");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/submit/txtform")
    @RequiresPermissions("sendBox:add")
    public String sendBoxSubmitByTxtForm(Model model,HttpServletRequest request){
        setTodayStartAndMinusDaysEnd(model,-2);
        Code sendDefaultPromptCode = DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig", "sendDefaultPrompt");
        String sendDefaultPrompt = "";
        if (sendDefaultPromptCode != null && sendDefaultPromptCode.getName() != null)
        {
        	sendDefaultPrompt = sendDefaultPromptCode.getName();
        }
        /**
         * @begin 2020-10-09
         * * 新增号码地区选择
         */
        model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        /**
         * @end
         */
        model.addAttribute("sendDefaultPrompt", sendDefaultPrompt);
        /**
         * @begin 2020-12-12
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        return FebsUtil.view("message/sendBoxSubmitByTxtForm");
    }
    
    /**
     * 表格发送(国际)
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/submit/tableGJ")
    @RequiresPermissions("sendBox:add")
    public String sendBoxSubmitByTableGJ(Model model,HttpServletRequest request){
        setTodayStartAndMinusDaysEnd(model,-2);
        Code sendDefaultPromptCode = DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig", "sendDefaultPrompt");
        String sendDefaultPrompt = "";
        if (sendDefaultPromptCode != null && sendDefaultPromptCode.getName() != null)
        {
        	sendDefaultPrompt = sendDefaultPromptCode.getName();
        }
        /**
         * @begin 2020-10-09
         * * 新增号码地区选择
         * @update 2022-03-21
         * * 修改号码地域选择方式（仅筛选已配置的地域）
         */
        OrganizationUserExt user = super.getCurrentUser();
        List<AreaCode> smsNumberAreaCode = organizationCostService.getOrgAreaCodeList(user.getOrganizationCode());
        model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        /**
         * @end
         */
        model.addAttribute("sendDefaultPrompt", sendDefaultPrompt);
        /**
         * @begin 2020-12-12
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        return FebsUtil.view("message/sendBoxSubmitByTableGJ");
    }

    /**
     * 我要发送
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/submit/formGJ")
    @RequiresPermissions("sendBox:add")
    public String sendBoxSubmitByFormGJ(Model model,HttpServletRequest request){
        setTodayStartAndMinusDaysEnd(model,-2);
        Code sendDefaultPromptCode = DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig", "sendDefaultPrompt");
        String sendDefaultPrompt = "";
        if (sendDefaultPromptCode != null && sendDefaultPromptCode.getName() != null)
        {
        	sendDefaultPrompt = sendDefaultPromptCode.getName();
        }
        /**
         * @begin 2020-10-09
         * * 新增号码地区选择
         * @update 2022-03-21
         * * 修改号码地域选择方式（仅筛选已配置的地域）
         */
        OrganizationUserExt user = super.getCurrentUser();
        List<AreaCode> smsNumberAreaCode = organizationCostService.getOrgAreaCodeList(user.getOrganizationCode());
        model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        /**
         * @end
         */
        model.addAttribute("sendDefaultPrompt", sendDefaultPrompt);
        /**
         * @begin 2020-12-12
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        return FebsUtil.view("message/sendBoxSubmitByFormGJ");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/submit/txtformGJ")
    @RequiresPermissions("sendBox:add")
    public String sendBoxSubmitByTxtFormGJ(Model model,HttpServletRequest request){
        setTodayStartAndMinusDaysEnd(model,-2);
        Code sendDefaultPromptCode = DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig", "sendDefaultPrompt");
        String sendDefaultPrompt = "";
        if (sendDefaultPromptCode != null && sendDefaultPromptCode.getName() != null)
        {
        	sendDefaultPrompt = sendDefaultPromptCode.getName();
        }
        /**
         * @begin 2020-10-09
         * * 新增号码地区选择
         * @update 2022-03-21
         * * 修改号码地域选择方式（仅筛选已配置的地域）
         */
        OrganizationUserExt user = super.getCurrentUser();
        List<AreaCode> smsNumberAreaCode = organizationCostService.getOrgAreaCodeList(user.getOrganizationCode());
        model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        /**
         * @end
         */
        model.addAttribute("sendDefaultPrompt", sendDefaultPrompt);
        /**
         * @begin 2020-12-12
         * 新增初始token 防止重复提交
         */
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
        model.addAttribute("sessionToken", sessionToken);
        /**
         * @end
         */
        return FebsUtil.view("message/sendBoxSubmitByTxtFormGJ");
    }
    
    /**
        * 隐藏信息发件箱
    * @param model
    * @return
    */
   @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/IList")
   @RequiresPermissions("sendBox:IList")
   public String sendBoxIIndex(Model model){
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
       return FebsUtil.view("message/incompl/sendBox");
   }
    
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
     * 发件箱重发结果页
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/resend/result")
    @RequiresPermissions("sendBox:resend")
    public String eximportResult() {
        return FebsUtil.view("message/resendResult");
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
     * 隐藏信息发送记录首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/IList")
    @RequiresPermissions("sendRecord:IList")
    public String sendRecordIIndex(Model model){
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
        return FebsUtil.view("message/incompl/sendRecord");
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
     * 隐藏信息发送回执首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnRecord/IList")
    @RequiresPermissions("returnRecord:IList")
    public String returnRecordIIndex(Model model){
        setTodayTimeStartAndEnd(model);
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
        return FebsUtil.view("message/incompl/returnRecord");
    }

    /**
     * 发送回执首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnRecord/list")
    @RequiresPermissions("returnRecord:list")
    public String returnRecordIndex(Model model){
        setTodayTimeStartAndEnd(model);
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
        return FebsUtil.view("message/returnRecord");
    }

    /**
     * 短信模板首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "smsTemplate/list")
    @RequiresPermissions("smsTemplate:list")
    public String smsTemplateIndex(Model model){
        model.addAttribute("auditStateEnums", AuditStateEnums.values());
        return FebsUtil.view("message/smsTemplate");
    }

    /**
     * 新增短信模板跳转
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "smsTemplate/add")
    @RequiresPermissions("smsTemplate:add")
    public String smsTemplateAdd(Model model){
        model.addAttribute("auditStateEnums", AuditStateEnums.values());
        return FebsUtil.view("message/smsTemplateAdd");
    }

    /**
     * 修改短信模板跳转
     * @param model
     * @param id
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "smsTemplate/update/{id}")
    @RequiresPermissions("smsTemplate:update")
    public String smsTemplateUpdate(Model model,@PathVariable Integer id){
        SmsTemplate smsTemplate = this.smsTemplateService.getById(id);
        model.addAttribute("auditStateEnums", AuditStateEnums.values());
        model.addAttribute("smsTemplate",smsTemplate);
        return FebsUtil.view("message/smsTemplateUpdate");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "smsTemplate/import")
    @RequiresPermissions("smsTemplate:import")
    public String smsTemplateImport(Model model){
        return FebsUtil.view("message/smsTemplateImport");
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

    protected void setTodayStartAndMinusDaysEnd(Model model,int minusDays){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime minusDaysEnd = LocalDateTime.of(LocalDate.now().minusDays(minusDays),LocalTime.MAX);
        model.addAttribute("todayStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("minusDaysEnd",minusDaysEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * 隐藏信息历史发件箱首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBoxHistory/IList")
    @RequiresPermissions("sendBoxHistory:IList")
    public String sendBoxHistoryIIndex(Model model){
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
        return FebsUtil.view("message/incompl/sendBoxHistory");
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
     * 隐藏信息历史发送记录首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecordHistory/IList")
    @RequiresPermissions("sendRecordHistory:IList")
    public String sendRecordHistoryIIndex(Model model){
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
        return FebsUtil.view("message/incompl/sendRecordHistory");
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
     * 隐藏信息发送回执首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnRecordHistory/IList")
    @RequiresPermissions("returnRecordHistory:IList")
    public String returnRecordHistoryIIndex(Model model){
        setStartTimeAndEndTime(model,11,11);
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
        return FebsUtil.view("message/incompl/returnRecordHistory");
    }

    /**
     * 发送回执首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnRecordHistory/list")
    @RequiresPermissions("returnRecordHistory:list")
    public String returnRecordHistoryIndex(Model model){
        setStartTimeAndEndTime(model,11,11);
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
        return FebsUtil.view("message/returnRecordHistory");
    }


    /**
     * 日报表首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "statisticalOrgcode/list")
    @RequiresPermissions("statisticalOrgcode:list")
    public String statisticalOrgcodeIndex(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(),LocalTime.MIN);
        model.addAttribute("dateStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        model.addAttribute("dateEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        return FebsUtil.view("report/statisticalOrgcode");
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
        return FebsUtil.view("report/rateSuccess");
    }
    
    /**
     * 批次成功率首页
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/contentRateSuccess")
    @RequiresPermissions("sendRecord:contentRateSuccess")
    public String contentRateSuccess(Model model){
    	setTodayTimeStartAndEnd(model);
    	model.addAttribute("smsNumberAreaProvinceEnums", SmsNumberAreaProvinceEnums.values());
    	return FebsUtil.view("report/contentRateSuccess");
    }

    protected void setStartTimeAndEndTime(Model model,int start,int end){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(start), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().minusDays(end),LocalTime.MAX);
        model.addAttribute("todayStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("todayEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
