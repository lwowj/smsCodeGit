package com.hero.sms.controller.message;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.DateUtils;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordCheckinfoQuery;
import com.hero.sms.entity.message.SendRecordExt;
import com.hero.sms.entity.message.SendRecordCheckinfoPlanQuery;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.entity.message.exportModel.RateSuccessByContentExcel;
import com.hero.sms.entity.message.exportModel.RateSuccessBySendCodeExcel;
import com.hero.sms.entity.message.exportModel.SendRecordExtExcel;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SendBoxSubTypeEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.message.ISendRecordCheckinfoService;
import com.hero.sms.service.message.ISendRecordCheckinfoPlanService;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 发送记录 Controller
 *
 * @author Administrator
 * @date 2020-03-12 00:31:45
 */
@Slf4j
@Validated
@Controller
@RequestMapping("sendRecord")
public class SendRecordController extends BaseController {

    @Autowired
    private ISendRecordService sendRecordService;
    @Autowired
    private ISendBoxService sendBoxService;
    @Autowired
    private IReportService reportService;
    @Autowired
    private ISmsChannelService smsChannelService;
    @Autowired
    private ISendRecordCheckinfoService sendRecordCheckinfoService;
    @Autowired
    private ISendRecordCheckinfoPlanService sendRecordCheckinfoPlanService;

    @ControllerEndpoint(operation = "统计数据", exceptionMessage = "统计数据失败")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("sendRecord:statistic")
    public FebsResponse statistic(SendRecordQuery sendRecord) {
        Map<String, Object> result = reportService.statisticSendRecordInfo(sendRecord);
        Long reqSuccess = (Long) result.get("reqSuccess");
        Long receiptSuccess = (Long) result.get("receiptSuccess");
        Long receiptFail = (Long) result.get("receiptFail");
        Long realReqSuccess = reqSuccess + receiptSuccess + receiptFail;
        result.put("allReqSuccess",realReqSuccess);
        return new FebsResponse().success().data(result);
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "sendRecord")
    public String sendRecordIndex(){
        return FebsUtil.view("sendRecord/sendRecord");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("sendRecord:list")
    public FebsResponse getAllSendRecords(SendRecordQuery sendRecord) {
        return new FebsResponse().success().data(sendRecordService.findSendRecords(sendRecord));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sendRecord:list")
    public FebsResponse sendRecordList(QueryRequest request, SendRecordQuery sendRecord) {

        IPage<SendRecord> datas = this.sendRecordService.findSendRecords(request, sendRecord);
        List<SendRecordExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item -> {
            SendRecordExt sendRecordExt = new SendRecordExt();
            BeanUtils.copyProperties(item,sendRecordExt);

            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            sendRecordExt.setOrgName(orgName);

            //短信详情
            String smsInfo = String.format("字数:%s 有效短信数:%s",item.getSmsWords(),item.getSmsCount());
            sendRecordExt.setSmsInfo(smsInfo);
            //运营商详情
            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("运营商:%s 地域:%s %s",
                    code != null? code.getName():"未知",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
            sendRecordExt.setOperatorInfo(operatorInfo);
            //金额详情
            BigDecimal divideNum = new BigDecimal(100);
            BigDecimal consumeAmout = new BigDecimal(item.getConsumeAmount()!=null?item.getConsumeAmount():0).divide(divideNum);
            BigDecimal channelCostAmout = new BigDecimal(item.getChannelCostAmount()!=null?item.getChannelCostAmount():0).divide(divideNum );
            BigDecimal agentIncomeAmout = new BigDecimal(item.getAgentIncomeAmount()!=null?item.getAgentIncomeAmount():0).divide(divideNum);
            BigDecimal incomeAmout = new BigDecimal(item.getIncomeAmount()!=null?item.getIncomeAmount():0).divide(divideNum);
            BigDecimal upAgentIncomeAmout = new BigDecimal(item.getUpAgentIncomeAmount() != null?item.getUpAgentIncomeAmount():0).divide(divideNum);
            String amountInfo = String.format("消费金额:%s元 通道成本:%s元 代理商收益:%s元 上级代理收益:%s元 平台收益:%s元"
                    ,consumeAmout,channelCostAmout,agentIncomeAmout,upAgentIncomeAmout,incomeAmout);
            sendRecordExt.setAmountInfo(amountInfo);
            sendRecordExt.setSmsChannelName(DatabaseCache.getSmsChannelNameById(item.getChannelId()));
            list.add(sendRecordExt);
        });

        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增SendRecord", exceptionMessage = "新增SendRecord失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("sendRecord:add")
    public FebsResponse addSendRecord(@Valid SendRecord sendRecord) {
        this.sendRecordService.createSendRecord(sendRecord);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除SendRecord", exceptionMessage = "删除SendRecord失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("sendRecord:delete")
    public FebsResponse deleteSendRecord(SendRecordQuery sendRecord) {
        this.sendRecordService.deleteSendRecord(sendRecord);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除SendRecord", exceptionMessage = "批量删除SendRecord失败")
    @GetMapping("delete/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:delete")
    public FebsResponse deleteSendRecord(@NotBlank(message = "{required}") @PathVariable String sendRecordIds) {
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        this.sendRecordService.deleteSendRecords(ids);
        return new FebsResponse().success();
    }


    @ControllerEndpoint(operation = "批量重发SendRecord", exceptionMessage = "批量重发SendRecord失败")
    @GetMapping("reSend/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:reSend")
    public FebsResponse reSend(@NotBlank(message = "{required}") @PathVariable String sendRecordIds) {
        if (StringUtils.isBlank(sendRecordIds)){
            return new FebsResponse().fail().message("请选择重发的记录！");
        }
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        Integer[] states = {SendRecordStateEnums.WaitReq.getCode(),SendRecordStateEnums.ReqFail.getCode()};
        /**
         * @begin 2020-12-21
         * 批量重发：不同状态做不同处理
         * 状态为（等待提交）的记录，直接进行发送；
         * 状态为（提交失败）的记录，先将数据校验中间表的状态，调整为解锁状态后，再放入重发集合中，否则无法重新发送
         */
        //状态为（等待提交）的数据
        LambdaQueryWrapper<SendRecord> waitReqWrapper = new LambdaQueryWrapper<>();
        waitReqWrapper.eq(SendRecord::getState,SendRecordStateEnums.WaitReq.getCode()).in(SendRecord::getId,ids);
        //2021-07-02 新增判断resid为空
        waitReqWrapper.isNull(SendRecord::getResMsgid);
        List<SendRecord> waitReqDatas = this.sendRecordService.list(waitReqWrapper);
      //状态为（提交失败）的数据
        LambdaQueryWrapper<SendRecord> reqFailWrapper = new LambdaQueryWrapper<>();
        reqFailWrapper.eq(SendRecord::getState,SendRecordStateEnums.ReqFail.getCode()).in(SendRecord::getId,ids);
      //2021-07-02 新增判断resid为空
        reqFailWrapper.isNull(SendRecord::getResMsgid);
        List<SendRecord> reqFailReqDatas = this.sendRecordService.list(reqFailWrapper);
        
        List<SendRecord> datas = new ArrayList<SendRecord>();
        if(!CollectionUtils.isEmpty(reqFailReqDatas))
        {
        	//状态为（提交失败）的记录，先将数据校验中间表的状态，调整为解锁状态后，再放入重发集合中，否则无法重新发送
        	StringBuffer smsNumber = new StringBuffer();
        	List<Long> idsList = new ArrayList<Long>();
        	for (int i = 0; i < reqFailReqDatas.size(); i++) 
        	{
        		SendRecord reqFailReqSendRecord = reqFailReqDatas.get(i);
        		idsList.add(reqFailReqSendRecord.getId());
        		if(i == 0)
        		{
        			smsNumber.append(reqFailReqSendRecord.getSmsNumber());
        		}
        		else
        		{
        			smsNumber.append(",").append(reqFailReqSendRecord.getSmsNumber());
        		}
			}
        	if(idsList!=null&&idsList.size()>0)
        	{
        		String sendRecordCheckInfoWay = "0";
        		Code sendRecordCheckInfoWayCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckInfoWay");
        	    if(sendRecordCheckInfoWayCode!=null&&!"".equals(sendRecordCheckInfoWayCode.getName()))
        	    {
        	    	sendRecordCheckInfoWay = sendRecordCheckInfoWayCode.getName();
        	    }
        	    //1、通过记录id校验； 0或其他 通过批次号与号码校验
        	    if("1".equals(sendRecordCheckInfoWay))
        	    {
        	    	SendRecordCheckinfoPlanQuery sendRecordCheckinfoPlan = new SendRecordCheckinfoPlanQuery();
                	sendRecordCheckinfoPlan.setState(1);
                	sendRecordCheckinfoPlan.setStates("0");
                	sendRecordCheckinfoPlan.setIdsList(idsList);
                	try 
                	{
                		sendRecordCheckinfoPlanService.updateSendRecordCheckinfoPlanBatch(sendRecordCheckinfoPlan);
        			} catch (Exception e) {
        				e.printStackTrace();
        				log.info(String.format("A批量重发reSend：号码【%s】解锁异常",smsNumber.toString()));
        			}
        	    }
        	    else
        	    {
        	    	SendRecordCheckinfoQuery sendRecordCheckinfo = new SendRecordCheckinfoQuery();
            		sendRecordCheckinfo.setState(1);
            		sendRecordCheckinfo.setStates("0");
                	sendRecordCheckinfo.setIdsList(idsList);
                	try 
                	{
            			sendRecordCheckinfoService.updateSendRecordCheckinfoBatch(sendRecordCheckinfo);
        			} catch (Exception e) {
        				e.printStackTrace();
        				log.info(String.format("B批量重发reSend：号码【%s】解锁异常",smsNumber.toString()));
        			}
        	    }        	    	
        	}
        	datas.addAll(reqFailReqDatas);
        }
        
        if(!CollectionUtils.isEmpty(waitReqDatas)) 
        {
        	datas.addAll(waitReqDatas);
        }
        /**
         * @end
         */
        
        if(CollectionUtils.isEmpty(datas)) {
        	return new FebsResponse().fail().message("发送记录不存在");
        }

        List<SmsChannel> smsChannels = smsChannelService.findSmsChannels(new SmsChannel());
        if(CollectionUtils.isEmpty(smsChannels)) {
        	return new FebsResponse().fail().message("目前没有可用通道");
        }
        int successNum = sendBoxService.pushMsg(smsChannels,datas);
        return new FebsResponse().success().message(String.format("成功重发%s条记录",successNum));
    }

    @ControllerEndpoint(operation = "批量强制重发SendRecord", exceptionMessage = "批量强制重发SendRecord失败")
    @GetMapping("forceReSend/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:forceReSend")
    public FebsResponse forceReSend(@NotBlank(message = "{required}") @PathVariable String sendRecordIds) {
        if (StringUtils.isBlank(sendRecordIds)){
            return new FebsResponse().fail().message("请选择强制重发的记录！");
        }
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        Integer[] states = {SendRecordStateEnums.WaitReq.getCode(),SendRecordStateEnums.ReqFail.getCode()};
        /**
         * @begin 2020-12-21
         * 批量强制重发：
         * 先将数据校验中间表的状态，调整为解锁状态后，再放入重发集合中，否则无法重新发送
         */
        LambdaQueryWrapper<SendRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SendRecord::getState,states).in(SendRecord::getId,ids);
      //2021-07-02 新增判断resid为空
        wrapper.isNull(SendRecord::getResMsgid);
        List<SendRecord> datas = this.sendRecordService.list(wrapper);
        if(!CollectionUtils.isEmpty(datas))
        {
        	//状态为（提交失败）的记录，先将数据校验中间表的状态，调整为解锁状态后，再放入重发集合中，否则无法重新发送
        	StringBuffer smsNumber = new StringBuffer();
        	List<Long> idsList = new ArrayList<Long>();
        	for (int i = 0; i < datas.size(); i++) 
        	{
        		SendRecord reqFailReqSendRecord = datas.get(i);
        		idsList.add(reqFailReqSendRecord.getId());
        		if(i == 0)
        		{
        			smsNumber.append(reqFailReqSendRecord.getSmsNumber());
        		}
        		else
        		{
        			smsNumber.append(",").append(reqFailReqSendRecord.getSmsNumber());
        		}
			}
        	if(idsList!=null&&idsList.size()>0)
        	{
        		String sendRecordCheckInfoWay = "0";
        		Code sendRecordCheckInfoWayCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckInfoWay");
        	    if(sendRecordCheckInfoWayCode!=null&&!"".equals(sendRecordCheckInfoWayCode.getName()))
        	    {
        	    	sendRecordCheckInfoWay = sendRecordCheckInfoWayCode.getName();
        	    }
        	    //1、通过记录id校验； 0或其他 通过批次号与号码校验
        	    if("1".equals(sendRecordCheckInfoWay))
        	    {
        	    	SendRecordCheckinfoPlanQuery sendRecordCheckinfoPlan = new SendRecordCheckinfoPlanQuery();
                	sendRecordCheckinfoPlan.setState(1);
                	sendRecordCheckinfoPlan.setStates("0");
                	sendRecordCheckinfoPlan.setIdsList(idsList);
                	try 
                	{
                		sendRecordCheckinfoPlanService.updateSendRecordCheckinfoPlanBatch(sendRecordCheckinfoPlan);
        			} catch (Exception e) {
        				e.printStackTrace();
        				log.info(String.format("A批量重发reSend：号码【%s】解锁异常",smsNumber.toString()));
        			}
        	    }
        	    else
        	    {
        	    	SendRecordCheckinfoQuery sendRecordCheckinfo = new SendRecordCheckinfoQuery();
            		sendRecordCheckinfo.setState(1);
            		sendRecordCheckinfo.setStates("0");
                	sendRecordCheckinfo.setIdsList(idsList);
                	try 
                	{
            			sendRecordCheckinfoService.updateSendRecordCheckinfoBatch(sendRecordCheckinfo);
        			} catch (Exception e) {
        				e.printStackTrace();
        				log.info(String.format("B批量重发reSend：号码【%s】解锁异常",smsNumber.toString()));
        			}
        	    } 
        	}
        }
        /**
         * @end
         */
        
        if(CollectionUtils.isEmpty(datas)) {
        	return new FebsResponse().fail().message("发送记录不存在");
        }

        List<SmsChannel> smsChannels = smsChannelService.findSmsChannels(new SmsChannel());
        if(CollectionUtils.isEmpty(smsChannels)) {
        	return new FebsResponse().fail().message("目前没有可用通道");
        }
        int successNum = sendBoxService.pushMsg(smsChannels,datas);
        return new FebsResponse().success().message(String.format("成功重发%s条记录",successNum));
    }
    
    @ControllerEndpoint(operation = "批量强制解锁SendRecord", exceptionMessage = "批量强制解锁SendRecord失败")
    @GetMapping("forceUnLock/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:forceUnLock")
    public FebsResponse forceUnLock(@NotBlank(message = "{required}") @PathVariable String sendRecordIds) {
        if (StringUtils.isBlank(sendRecordIds)){
            return new FebsResponse().fail().message("请选择强制解锁的记录！");
        }
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        Integer[] states = {SendRecordStateEnums.WaitReq.getCode(),SendRecordStateEnums.ReqFail.getCode()};
        /**
         * @begin 2020-12-21
         * 批量强制重发：
         * 先将数据校验中间表的状态，调整为解锁状态后，再放入重发集合中，否则无法重新发送
         */
        LambdaQueryWrapper<SendRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SendRecord::getState,states).in(SendRecord::getId,ids);
        List<SendRecord> datas = this.sendRecordService.list(wrapper);
        int count = 0;
        if(!CollectionUtils.isEmpty(datas))
        {
        	//状态为（提交失败）的记录，先将数据校验中间表的状态，调整为解锁状态后，再放入重发集合中，否则无法重新发送
        	StringBuffer smsNumber = new StringBuffer();
        	List<Long> idsList = new ArrayList<Long>();
        	for (int i = 0; i < datas.size(); i++) 
        	{
        		count++;
        		SendRecord reqFailReqSendRecord = datas.get(i);
        		idsList.add(reqFailReqSendRecord.getId());
        		if(i == 0)
        		{
        			smsNumber.append(reqFailReqSendRecord.getSmsNumber());
        		}
        		else
        		{
        			smsNumber.append(",").append(reqFailReqSendRecord.getSmsNumber());
        		}
			}
        	if(idsList!=null&&idsList.size()>0)
        	{
        		String sendRecordCheckInfoWay = "0";
        		Code sendRecordCheckInfoWayCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendRecordCheckInfoWay");
        	    if(sendRecordCheckInfoWayCode!=null&&!"".equals(sendRecordCheckInfoWayCode.getName()))
        	    {
        	    	sendRecordCheckInfoWay = sendRecordCheckInfoWayCode.getName();
        	    }
        	    //1、通过记录id校验； 0或其他 通过批次号与号码校验
        	    if("1".equals(sendRecordCheckInfoWay))
        	    {
        	    	SendRecordCheckinfoPlanQuery sendRecordCheckinfoPlan = new SendRecordCheckinfoPlanQuery();
        	    	sendRecordCheckinfoPlan.setState(1);
            		String thisdate = DateUtils.getString("MMddHHmmss");
            		sendRecordCheckinfoPlan.setStates(thisdate);
            		sendRecordCheckinfoPlan.setIdsList(idsList);
                	try 
                	{
                		sendRecordCheckinfoPlanService.updateSendRecordCheckinfoPlanBatch(sendRecordCheckinfoPlan);
        			} catch (Exception e) {
        				e.printStackTrace();
        				log.info(String.format("A批量重发reSend：号码【%s】解锁异常",smsNumber.toString()));
        			}
        	    }
        	    else
        	    {
        	    	SendRecordCheckinfoQuery sendRecordCheckinfo = new SendRecordCheckinfoQuery();
        	    	sendRecordCheckinfo.setState(1);
            		String thisdate = DateUtils.getString("MMddHHmmss");
            		sendRecordCheckinfo.setStates(thisdate);
                	sendRecordCheckinfo.setIdsList(idsList);
                	try 
                	{
            			sendRecordCheckinfoService.updateSendRecordCheckinfoBatch(sendRecordCheckinfo);
        			} catch (Exception e) {
        				e.printStackTrace();
        				log.info(String.format("B批量重发reSend：号码【%s】解锁异常",smsNumber.toString()));
        			}
        	    } 
        	}
        }
        /**
         * @end
         */
        return new FebsResponse().success().message(String.format("成功解锁%s条记录",count));
    }
    
    @ControllerEndpoint(operation = "修改SendRecord", exceptionMessage = "修改SendRecord失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("sendRecord:update")
    public FebsResponse updateSendRecord(SendRecord sendRecord) {
        this.sendRecordService.updateSendRecord(sendRecord);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "导出SendRecord", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("sendRecord:export")
    public void export(QueryRequest queryRequest, SendRecordQuery sendRecord, HttpServletResponse response) {
        List<SendRecord> sendRecords = this.sendRecordService.findSendRecords(sendRecord);
        List<SendRecordExtExcel> lst = new ArrayList<>();
        sendRecords.stream().forEach(item -> {
            SendRecordExtExcel excel = new SendRecordExtExcel();
            BeanUtils.copyProperties(item,excel);
            //运营商详情
            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("运营商:%s 地域:%s %s",
                    code != null? code.getName():"未知",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
            excel.setOperatorInfo(operatorInfo);
            excel.setOrgCode(item.getOrgCode());
            //短信详情
            excel.setSmsInfo(String.format("字数:%s 短信数:%s",item.getSmsWords(),item.getSmsCount()));
            lst.add(excel);
        });
        ExcelKit.$Export(SendRecordExtExcel.class, response).downXlsx(lst, false);

    }

    @ControllerEndpoint(operation = "异步导出SendRecord", exceptionMessage = "导出Excel失败")
    @GetMapping("export")
    @ResponseBody
    @RequiresPermissions("sendRecord:export")
    public FebsResponse export(QueryRequest queryRequest,SendRecordQuery sendRecord){
        User user = getCurrentUser();
        if (user == null || user.getId() == null){
            return new FebsResponse().fail().message("非法请求！");
        }
        Long userId = getCurrentUser().getId();
        this.sendRecordService.exportSendCordFromAdmin(userId,sendRecord);
        return new FebsResponse().success().message("导出提交成功，请稍后到【导出下载】查看导出结果！");
    }


    @GetMapping("batchNotifyMsgState/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:batchNotifyMsgState")
    public FebsResponse batchNotifyMsgState(QueryRequest queryRequest,@PathVariable String sendRecordIds) {
        int result = sendRecordService.batchNotifyMsgState(sendRecordIds);
        return new FebsResponse().success().message(String.format("成功补发%s条状态通知",result));
    }

    @GetMapping("batchNotifyMsgStateByQuery")
    @ResponseBody
    @RequiresPermissions("sendRecord:batchNotifyMsgState")
    public FebsResponse batchNotifyMsgStateByQuery(QueryRequest queryRequest,SendRecordQuery sendRecord) {
        int result = sendRecordService.batchNotifyMsgState(sendRecord);
        return new FebsResponse().success().message(String.format("成功补发%s条状态通知",result));
    }

    @ControllerEndpoint( operation = "查询批次发送成功率")
    @GetMapping("rateSuccess")
    @ResponseBody
    @RequiresPermissions("sendRecord:rateSuccess")
    public FebsResponse rateSuccess(QueryRequest request, SendRecordQuery sendRecord) {
    	sendRecord.setSubTypeWith((SendBoxSubTypeEnums.OrgWebSub.getCode()+SendBoxSubTypeEnums.HttpSub.getCode()));
        IPage<Map<String,Object>> datas = this.reportService.statisticRateSuccessGroupBySendCode(request, sendRecord);
        Map<String, Object> dataTable = getDataTable(datas);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "导出批次发送成功率报表", exceptionMessage = "导出批次发送成功率报表失败")
    @GetMapping("exportRateSuccess")
    @ResponseBody
    @RequiresPermissions("sendRecord:exportRateSuccess")
    public void exportRateSuccess( SendRecordQuery sendRecord, HttpServletResponse response) {
        sendRecord.setSubTypeWith((SendBoxSubTypeEnums.OrgWebSub.getCode()+SendBoxSubTypeEnums.HttpSub.getCode()));
        List<Map<String, Object>> result = this.reportService.statisticRateSuccessGroupBySendCode(sendRecord);
        List<RateSuccessBySendCodeExcel> lst = Lists.newArrayList();
        result.stream().forEach(item -> {
            RateSuccessBySendCodeExcel excel = new RateSuccessBySendCodeExcel();
            try {
                org.apache.commons.beanutils.BeanUtils.populate(excel,item);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            lst.add(excel);
        });
        ExcelKit.$Export(RateSuccessBySendCodeExcel.class,response).downXlsx(lst,false);
    }


    @ControllerEndpoint( operation = "查询内容发送成功率")
    @GetMapping("contentRateSuccess")
    @ResponseBody
    @RequiresPermissions("sendRecord:contentRateSuccess")
    public FebsResponse contentRateSuccess(QueryRequest request, SendRecordQuery sendRecord) {
    	sendRecord.setSubTypeWith(SendBoxSubTypeEnums.SmppSub.getCode());
    	IPage<Map<String,Object>> datas = this.reportService.statisticRateSuccessGroupByContent(request, sendRecord);
    	Map<String, Object> dataTable = getDataTable(datas);
    	return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "导出内容发送成功率报表", exceptionMessage = "导出内容发送成功率报表失败")
    @GetMapping("exportContentRateSuccess")
    @ResponseBody
    @RequiresPermissions("sendRecord:exportContentRateSuccess")
    public void exportContentRateSuccess( SendRecordQuery sendRecord, HttpServletResponse response) {
        sendRecord.setSubTypeWith(SendBoxSubTypeEnums.SmppSub.getCode());
        List<Map<String, Object>> result = this.reportService.statisticRateSuccessGroupByContent(sendRecord);
        List<RateSuccessByContentExcel> lst = Lists.newArrayList();
        result.stream().forEach(item -> {
            RateSuccessByContentExcel excel = new RateSuccessByContentExcel();
            try {
                org.apache.commons.beanutils.BeanUtils.populate(excel,item);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            lst.add(excel);
        });
        ExcelKit.$Export(RateSuccessByContentExcel.class,response).downXlsx(lst,false);
    }


    @ControllerEndpoint(operation = "批量修改回执状态", exceptionMessage = "批量修改回执状态失败")
    @GetMapping("batchUpdateReturnStat/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:batchUpdateReturnStat")
    public FebsResponse batchUpdateReturnStat(@RequestParam(value = "success",required = true) boolean success,
                                              @PathVariable String sendRecordIds) {

        if (StringUtils.isBlank(sendRecordIds)){
            return new FebsResponse().fail().message("请选择重发的记录！");
        }

        String msg = "成功将【%s条】记录的回执状态修改为【接收" + (success?"成功":"失败") +"】";
        return new FebsResponse().success().message(String.format(msg,sendRecordService.batchUpdateMsgReturnState(sendRecordIds,success)));
    }

    @ControllerEndpoint(operation = "批量修改回执状态", exceptionMessage = "批量修改回执状态失败")
    @GetMapping("batchUpdateReturnStatByRate/{successRate}")
    @ResponseBody
    @RequiresPermissions("sendRecord:batchUpdateReturnStat")
    public FebsResponse batchUpdateReturnStat(@PathVariable BigDecimal successRate,
                                               SendRecordQuery sendRecord) {

        return new FebsResponse().success().message(sendRecordService.batchUpdateMsgReturnState(sendRecord,successRate));
    }
    
    
    /**
     * @begin 2020-11-11
     * 发件箱因其他原因未推送到MQ的记录进行重新推送
     * @param stateTag 推送表示 默认pass
     * @param sendBoxIds  推送记录ID。默认单个，预留类型json，以便后期需要扩展多个ID
     * @return
     */
    @ControllerEndpoint(operation = "发件箱重新推送分拣", exceptionMessage = "重新推送分拣失败")
    @GetMapping("sortingAgain/{stateTag}/{sendBoxIds}")
    @ResponseBody
    @RequiresPermissions("sendBox:sortingAgain")
    public FebsResponse sortingAgainSendBox(@PathVariable("stateTag") String stateTag,@PathVariable("sendBoxIds") String sendBoxIds) 
    {
        if (StringUtils.isBlank(stateTag) || StringUtils.isBlank(sendBoxIds))
        {
            return new FebsResponse().fail().message("缺少必要参数，操作失败");
        }
        if ("pass".equals(stateTag))
        {
        	List<SendBox> sendBoxs =  new ArrayList<SendBox>();
        	List<Long> ids = Arrays.stream(sendBoxIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
	        SendBoxQuery query = new SendBoxQuery();
	        query.setIds(ids);
	        query.setIsTiming(false);
	        query.setAuditState(AuditStateEnums.Pass.getCode().intValue());
        	List<SendBox> sendBoxList = sendBoxService.findSendBoxs(query);
        	if(sendBoxList!=null&&sendBoxList.size()>0)
        	{
        		for (int i = 0; i < sendBoxList.size(); i++) 
            	{
            		SendBox thisSendBox = sendBoxList.get(i);
            		//判断排除已经分拣过的记录
            		if(thisSendBox.getSortingTime()!=null&&thisSendBox.getSmsCount()>0)
            		{
            			continue;
            		}
            		//以批次号与商户号为条件进行查询
            		SendRecordQuery sendRecord = new SendRecordQuery();
            		sendRecord.setSendCode(thisSendBox.getSendCode());
            		sendRecord.setOrgCode(thisSendBox.getOrgCode());
            		List<SendRecord> sendRecordList =  sendRecordService.findSendRecords(sendRecord);
            		//判断发送记录表中没有分拣后的数据记录
            		if(sendRecordList!=null&&sendRecordList.size()>0)
            		{
            			continue;
            		}
            		sendBoxs.add(thisSendBox);
    			}
        		if(sendBoxs!=null&&sendBoxs.size()>0)
        		{
        			sendBoxService.splitRecord(sendBoxs);
        		}
        		else
            	{
            		 return new FebsResponse().fail().message("所选记录不符合重推条件");
            	}
        	}
        	else
        	{
        		 return new FebsResponse().fail().message("所选记录不符合重推条件");
        	}
        }else {
            return new FebsResponse().fail();
        }
        return new FebsResponse().success();
    }
    /**
     * @end
     */
}
