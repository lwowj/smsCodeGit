package com.hero.sms.controller.message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.DateUtils;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.MobileBlack;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormal;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormalQuery;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SmsTemplate;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.common.ProcessingStateEnums;
import com.hero.sms.enums.message.SendBoxIsSortingFlagEnums;
import com.hero.sms.enums.message.SendBoxIsTimingEnums;
import com.hero.sms.enums.message.SendBoxSubTypeEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.enums.message.SmsContentLengthStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.message.IMobileBlackService;
import com.hero.sms.service.message.IReceiptReturnRecordAbnormalService;
import com.hero.sms.service.message.IReturnRecordService;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.service.message.ISmsTemplateService;

@Controller
public class ViewController extends BaseController {

    @Autowired
    private ISmsTemplateService smsTemplateService;

    @Autowired
    private ISendBoxService sendBoxService;

    @Autowired
    private ISendRecordService sendRecordService;

    @Autowired
    private IReturnRecordService returnRecordService;

    @Autowired
    private IMobileBlackService mobileBlackService;

    @Autowired
    private IReceiptReturnRecordAbnormalService receiptReturnRecordAbnormalService;
    

    //????????????

    /**
     * ??????????????????
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
     * ????????????????????????
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
     * ????????????????????????
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


    //?????????

    /**
     * ???????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/list")
    @RequiresPermissions("sendBox:list")
    public String sendBoxIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("sendBoxSubTypeEnums", SendBoxSubTypeEnums.values());
        model.addAttribute("auditStateEnums",AuditStateEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        model.addAttribute("smsContentLengthStateEnums", SmsContentLengthStateEnums.values());
        model.addAttribute("sendBoxIsTimingEnums", SendBoxIsTimingEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
	    if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
	    }
	    else
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
	    }
	    model.addAttribute("sendBoxIsSortingFlagEnums", SendBoxIsSortingFlagEnums.values());
        return FebsUtil.view("message/sendBox");
    }

    /**
     * ??????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/timing/list")
    @RequiresPermissions("sendBox:list")
    public String timingSendBoxIndex(Model model){
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
     * ???????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/waitApprove/list")
    @RequiresPermissions("sendBox:list")
    public String waitApproveSendBoxIndex(Model model){
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
        return FebsUtil.view("message/waitApproveSendBox");
    }

    /**
     * ?????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/add")
    @RequiresPermissions("sendBox:add")
    public String sendBoxAdd(Model model){
        return FebsUtil.view("message/sendBoxAdd");
    }

    /**
     * ?????????????????????
     * @param model
     * @param id
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox/update/{id}")
    @RequiresPermissions("sendBox:update")
    public String sendBoxUpdate(Model model,@PathVariable Integer id){
        SendBox sendBox = this.sendBoxService.getById(id);
        model.addAttribute("sendBox",sendBox);
        return FebsUtil.view("message/sendBoxUpdate");
    }

    //????????????

    /**
     * ??????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/list")
    @RequiresPermissions("sendRecord:list")
    public String sendRecordIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("sendRecordStateEnums", SendRecordStateEnums.values());
        model.addAttribute("smsNumberAreaProvinceEnums", SmsNumberAreaProvinceEnums.values());
        model.addAttribute("smsContentLengthStateEnums", SmsContentLengthStateEnums.values());
        List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
	    if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
	    }
	    else
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
	    }
	    model.addAttribute("resMsgidIsNullFlagEnums", SendBoxIsSortingFlagEnums.values());
        return FebsUtil.view("message/sendRecord");
    }

    /**
     * ????????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/add")
    @RequiresPermissions("sendRecord:add")
    public String sendRecordAdd(Model model){
        return FebsUtil.view("message/sendRecordAdd");
    }

    /**
     * ?????????????????????
     * @param model
     * @param id
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/update/{id}")
    @RequiresPermissions("sendRecord:update")
    public String sendRecordUpdate(Model model,@PathVariable Integer id){
        SendRecord sendRecord = this.sendRecordService.getById(id);
        model.addAttribute("sendRecord",sendRecord);
        return FebsUtil.view("message/sendRecordUpdate");
    }

    //??????

    /**
     * ??????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnRecord/list")
    @RequiresPermissions("returnRecord:list")
    public String returnRecordIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        model.addAttribute("stateEnums", CommonStateEnums.values());
        model.addAttribute("sendRecordStateEnums",SendRecordStateEnums.values());
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
     * ????????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnRecord/add")
    @RequiresPermissions("returnRecord:add")
    public String returnRecordAdd(Model model){
        return FebsUtil.view("message/returnRecordAdd");
    }

    /**
     * ?????????????????????
     * @param model
     * @param id
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnRecord/update/{id}")
    @RequiresPermissions("returnRecord:update")
    public String returnRecordUpdate(Model model,@PathVariable Integer id){
        ReturnRecord returnRecord = this.returnRecordService.getById(id);
        model.addAttribute("returnRecord",returnRecord);
        return FebsUtil.view("message/returnRecordUpdate");
    }

  //???????????????
    /**
     * ?????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "mobileBlack/list")
    @RequiresPermissions("mobileBlack:list")
    public String mobileBlackIndex(Model model){
        model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        return FebsUtil.view("message/mobileBlack");
    }

    /**
     * ???????????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "mobileBlack/add")
    @RequiresPermissions("mobileBlack:add")
    public String mobileBlackAdd(Model model){
        model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        return FebsUtil.view("message/mobileBlackAdd");
    }

    /**
     * ???????????????????????????
     * @param model
     * @param id
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "mobileBlack/update/{id}")
    @RequiresPermissions("mobileBlack:update")
    public String mobileBlackUpdate(Model model,@PathVariable Integer id){
    	MobileBlack mobileBlack = this.mobileBlackService.getById(id);
        model.addAttribute("mobileBlack",mobileBlack);
        model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
        return FebsUtil.view("message/mobileBlackUpdate");
    }

    //?????????
    /**
     * ???????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sensitiveWord/list")
    @RequiresPermissions("sensitiveWord:list")
    public String sensitiveWordIndex(Model model){
        return FebsUtil.view("message/sensitiveWord");
    }

    /**
     * ?????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sensitiveWord/add")
    @RequiresPermissions("sensitiveWord:add")
    public String sensitiveWordAdd(Model model){
        return FebsUtil.view("message/sensitiveWordAdd");
    }

    /**
     * ?????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBoxHistory/list")
    @RequiresPermissions("sendBoxHistory:list")
    public String sendBoxHistoryIndex(Model model){
        setStartTimeAndEndTime(model,11,11);
        model.addAttribute("sendBoxSubTypeEnums", SendBoxSubTypeEnums.values());
        model.addAttribute("auditStateEnums", AuditStateEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        model.addAttribute("smsContentLengthStateEnums", SmsContentLengthStateEnums.values());
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
     * ????????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecordHistory/list")
    @RequiresPermissions("sendRecordHistory:list")
    public String sendRecordHistoryIndex(Model model){
        setStartTimeAndEndTime(model,11,11);
        model.addAttribute("sendRecordStateEnums", SendRecordStateEnums.values());
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        model.addAttribute("smsNumberAreaProvinceEnums", SmsNumberAreaProvinceEnums.values());
        model.addAttribute("smsContentLengthStateEnums", SmsContentLengthStateEnums.values());
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
     * ??????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "returnRecordHistory/list")
    @RequiresPermissions("returnRecordHistory:list")
    public String returnRecordHistoryIndex(Model model){
        setStartTimeAndEndTime(model,11,11);
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        model.addAttribute("stateEnums", CommonStateEnums.values());
        model.addAttribute("sendRecordStateEnums",SendRecordStateEnums.values());
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
     * ?????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/rateSuccess")
    @RequiresPermissions("sendRecord:rateSuccess")
    public String rateSuccess(Model model){
        setTodayTimeStartAndEnd(model);
        return FebsUtil.view("report/rateSuccess");
    }

    /**
     * ?????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord/contentRateSuccess")
    @RequiresPermissions("sendRecord:rateSuccess")
    public String contentRateSuccess(Model model){
    	setTodayTimeStartAndEnd(model);
    	return FebsUtil.view("report/contentRateSuccess");
    }

    /**
     * ???????????????????????????????????????
     * ???????????? yyyy-MM-dd HH:mm:ss
     * @param model
     */
    protected void setTodayTimeStartAndEnd(Model model){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(),LocalTime.MAX);
        model.addAttribute("todayStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("todayEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    /**
     * ???????????????N?????????????????? ??? ??????????????????
     * ???????????? yyyy-MM-dd HH:mm:ss
     * @param model
     */
    protected void setStartTimeAndTodayEnd(Model model,int n){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(n), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(),LocalTime.MAX);
        model.addAttribute("todayStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("todayEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    protected void setStartTimeAndEndTime(Model model,int start,int end){
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now().minusDays(start), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().minusDays(end),LocalTime.MAX);
        model.addAttribute("todayStart",todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("todayEnd",todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    /**
     * ??????????????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "receiptReturnRecordAbnormal/list")
    @RequiresPermissions("receiptReturnRecordAbnormal:list")
    public String receiptReturnRecordIndex(Model model){
        setTodayTimeStartAndEnd(model);
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
        model.addAttribute("stateEnums", CommonStateEnums.values());
        model.addAttribute("sendRecordStateEnums",SendRecordStateEnums.values());
        model.addAttribute("processingStateEnums", ProcessingStateEnums.values());
        return FebsUtil.view("message/receiptReturnRecordAbnormal");
    }
    
    /**
     * @begin 2020-11-22
     * ??????????????????????????????????????????????????????????????????
     * @param receiptAbnormalIds  ????????????ID??????????????????????????????json?????????????????????????????????ID
     * @return
     */
//    @ControllerEndpoint(operation = "????????????????????????????????????", exceptionMessage = "????????????????????????")
    @GetMapping(FebsConstant.VIEW_PREFIX + "receiptReturnRecordAbnormal/checkSendRecord/{receiptAbnormalIds}")
    @RequiresPermissions("receiptReturnRecordAbnormal:check")
    public String checkSendRecord(@PathVariable("receiptAbnormalIds") String receiptAbnormalIds, Model model) 
    {
    	String message = "";
    	String createtime = "";
    	String falg = "false";
    	SendRecord sendRecord = new SendRecord(); 
    	if (StringUtils.isBlank(receiptAbnormalIds))
        {
    		message = "?????????????????????????????????";
        }
   	 	else
   	 	{
	   	 	List<Long> ids = Arrays.stream(receiptAbnormalIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
	    	if(ids.size()==1)
	    	{
	    		ReceiptReturnRecordAbnormalQuery  query = new ReceiptReturnRecordAbnormalQuery();
	            query.setId(ids.get(0));
	        	List<ReceiptReturnRecordAbnormal> receiptReturnRecordAbnormalList = receiptReturnRecordAbnormalService.findReceiptReturnRecordAbnormals(query);
	        	if(receiptReturnRecordAbnormalList!=null&&receiptReturnRecordAbnormalList.size()>0)
	        	{
	        		ReceiptReturnRecordAbnormal receiptReturnRecordAbnormal = receiptReturnRecordAbnormalList.get(0);
	        		sendRecord = sendRecordService.getByAreaAndNumberAndMsgId(receiptReturnRecordAbnormal.getSmsNumberArea(),receiptReturnRecordAbnormal.getSmsNumber(),receiptReturnRecordAbnormal.getResMsgid());
	        		if(sendRecord==null)
	            	{
	            		message = "?????????????????????????????????????????????????????????????????????";
	            	}
	            	else
	            	{
	            		falg = "true"; 
	            		if(sendRecord.getState()==SendRecordStateEnums.ReqSuccess.getCode())
	            		{
	            			message = "??????????????????????????????????????????????????????????????????";
	            		}
	            		else
	            		{
	            			message = "??????????????????????????????????????????????????????????????????????????????";
	            		}
	            		createtime = DateUtils.getString(sendRecord.getCreateTime(), DateUtils.Y_M_D_H_M_S_1);
	            	}
	        	}
	        	else
	        	{
	        		message = "????????????????????????????????????";
	        	}
	    	}
   	 	}
    	model.addAttribute("createtime",createtime);
    	model.addAttribute("smsChannelList",DatabaseCache.getSmsChannelList());
    	model.addAttribute("sendRecordStateEnums",SendRecordStateEnums.values());
	    model.addAttribute("message", message);
	    model.addAttribute("falg", falg);
	    model.addAttribute("sendRecord", sendRecord);
	    return FebsUtil.view("message/receiptReturnRecordAbnormalCheck");
    }
    /**
     * @end
     */
    
    /**
     * ???????????????????????????
     * @param model
     * @return
     */
    @GetMapping(FebsConstant.VIEW_PREFIX + "mobileBlack/queryNumAddr")
    @RequiresPermissions("mobile:queryNumAddr")
    public String mobileQueryNumAddr(Model model)
    {
        model.addAttribute("phoneMsgGetUrlEnums", DatabaseCache.getCodeListBySortCode("PhoneMsgGetUrl"));
        return FebsUtil.view("message/mobileQueryNumAddr");
    }
}
