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
 * ???????????? Controller
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

    @ControllerEndpoint(operation = "????????????", exceptionMessage = "??????????????????")
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

            //????????????
            String smsInfo = String.format("??????:%s ???????????????:%s",item.getSmsWords(),item.getSmsCount());
            sendRecordExt.setSmsInfo(smsInfo);
            //???????????????
            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("?????????:%s ??????:%s %s",
                    code != null? code.getName():"??????",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"??????" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"??????");
            sendRecordExt.setOperatorInfo(operatorInfo);
            //????????????
            BigDecimal divideNum = new BigDecimal(100);
            BigDecimal consumeAmout = new BigDecimal(item.getConsumeAmount()!=null?item.getConsumeAmount():0).divide(divideNum);
            BigDecimal channelCostAmout = new BigDecimal(item.getChannelCostAmount()!=null?item.getChannelCostAmount():0).divide(divideNum );
            BigDecimal agentIncomeAmout = new BigDecimal(item.getAgentIncomeAmount()!=null?item.getAgentIncomeAmount():0).divide(divideNum);
            BigDecimal incomeAmout = new BigDecimal(item.getIncomeAmount()!=null?item.getIncomeAmount():0).divide(divideNum);
            BigDecimal upAgentIncomeAmout = new BigDecimal(item.getUpAgentIncomeAmount() != null?item.getUpAgentIncomeAmount():0).divide(divideNum);
            String amountInfo = String.format("????????????:%s??? ????????????:%s??? ???????????????:%s??? ??????????????????:%s??? ????????????:%s???"
                    ,consumeAmout,channelCostAmout,agentIncomeAmout,upAgentIncomeAmout,incomeAmout);
            sendRecordExt.setAmountInfo(amountInfo);
            sendRecordExt.setSmsChannelName(DatabaseCache.getSmsChannelNameById(item.getChannelId()));
            list.add(sendRecordExt);
        });

        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "??????SendRecord", exceptionMessage = "??????SendRecord??????")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("sendRecord:add")
    public FebsResponse addSendRecord(@Valid SendRecord sendRecord) {
        this.sendRecordService.createSendRecord(sendRecord);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "??????SendRecord", exceptionMessage = "??????SendRecord??????")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("sendRecord:delete")
    public FebsResponse deleteSendRecord(SendRecordQuery sendRecord) {
        this.sendRecordService.deleteSendRecord(sendRecord);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "????????????SendRecord", exceptionMessage = "????????????SendRecord??????")
    @GetMapping("delete/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:delete")
    public FebsResponse deleteSendRecord(@NotBlank(message = "{required}") @PathVariable String sendRecordIds) {
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        this.sendRecordService.deleteSendRecords(ids);
        return new FebsResponse().success();
    }


    @ControllerEndpoint(operation = "????????????SendRecord", exceptionMessage = "????????????SendRecord??????")
    @GetMapping("reSend/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:reSend")
    public FebsResponse reSend(@NotBlank(message = "{required}") @PathVariable String sendRecordIds) {
        if (StringUtils.isBlank(sendRecordIds)){
            return new FebsResponse().fail().message("???????????????????????????");
        }
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        Integer[] states = {SendRecordStateEnums.WaitReq.getCode(),SendRecordStateEnums.ReqFail.getCode()};
        /**
         * @begin 2020-12-21
         * ??????????????????????????????????????????
         * ????????????????????????????????????????????????????????????
         * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        //????????????????????????????????????
        LambdaQueryWrapper<SendRecord> waitReqWrapper = new LambdaQueryWrapper<>();
        waitReqWrapper.eq(SendRecord::getState,SendRecordStateEnums.WaitReq.getCode()).in(SendRecord::getId,ids);
        //2021-07-02 ????????????resid??????
        waitReqWrapper.isNull(SendRecord::getResMsgid);
        List<SendRecord> waitReqDatas = this.sendRecordService.list(waitReqWrapper);
      //????????????????????????????????????
        LambdaQueryWrapper<SendRecord> reqFailWrapper = new LambdaQueryWrapper<>();
        reqFailWrapper.eq(SendRecord::getState,SendRecordStateEnums.ReqFail.getCode()).in(SendRecord::getId,ids);
      //2021-07-02 ????????????resid??????
        reqFailWrapper.isNull(SendRecord::getResMsgid);
        List<SendRecord> reqFailReqDatas = this.sendRecordService.list(reqFailWrapper);
        
        List<SendRecord> datas = new ArrayList<SendRecord>();
        if(!CollectionUtils.isEmpty(reqFailReqDatas))
        {
        	//????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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
        	    //1???????????????id????????? 0????????? ??????????????????????????????
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
        				log.info(String.format("A????????????reSend????????????%s???????????????",smsNumber.toString()));
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
        				log.info(String.format("B????????????reSend????????????%s???????????????",smsNumber.toString()));
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
        	return new FebsResponse().fail().message("?????????????????????");
        }

        List<SmsChannel> smsChannels = smsChannelService.findSmsChannels(new SmsChannel());
        if(CollectionUtils.isEmpty(smsChannels)) {
        	return new FebsResponse().fail().message("????????????????????????");
        }
        int successNum = sendBoxService.pushMsg(smsChannels,datas);
        return new FebsResponse().success().message(String.format("????????????%s?????????",successNum));
    }

    @ControllerEndpoint(operation = "??????????????????SendRecord", exceptionMessage = "??????????????????SendRecord??????")
    @GetMapping("forceReSend/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:forceReSend")
    public FebsResponse forceReSend(@NotBlank(message = "{required}") @PathVariable String sendRecordIds) {
        if (StringUtils.isBlank(sendRecordIds)){
            return new FebsResponse().fail().message("?????????????????????????????????");
        }
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        Integer[] states = {SendRecordStateEnums.WaitReq.getCode(),SendRecordStateEnums.ReqFail.getCode()};
        /**
         * @begin 2020-12-21
         * ?????????????????????
         * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        LambdaQueryWrapper<SendRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SendRecord::getState,states).in(SendRecord::getId,ids);
      //2021-07-02 ????????????resid??????
        wrapper.isNull(SendRecord::getResMsgid);
        List<SendRecord> datas = this.sendRecordService.list(wrapper);
        if(!CollectionUtils.isEmpty(datas))
        {
        	//????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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
        	    //1???????????????id????????? 0????????? ??????????????????????????????
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
        				log.info(String.format("A????????????reSend????????????%s???????????????",smsNumber.toString()));
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
        				log.info(String.format("B????????????reSend????????????%s???????????????",smsNumber.toString()));
        			}
        	    } 
        	}
        }
        /**
         * @end
         */
        
        if(CollectionUtils.isEmpty(datas)) {
        	return new FebsResponse().fail().message("?????????????????????");
        }

        List<SmsChannel> smsChannels = smsChannelService.findSmsChannels(new SmsChannel());
        if(CollectionUtils.isEmpty(smsChannels)) {
        	return new FebsResponse().fail().message("????????????????????????");
        }
        int successNum = sendBoxService.pushMsg(smsChannels,datas);
        return new FebsResponse().success().message(String.format("????????????%s?????????",successNum));
    }
    
    @ControllerEndpoint(operation = "??????????????????SendRecord", exceptionMessage = "??????????????????SendRecord??????")
    @GetMapping("forceUnLock/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:forceUnLock")
    public FebsResponse forceUnLock(@NotBlank(message = "{required}") @PathVariable String sendRecordIds) {
        if (StringUtils.isBlank(sendRecordIds)){
            return new FebsResponse().fail().message("?????????????????????????????????");
        }
        String[] ids = sendRecordIds.split(StringPool.COMMA);
        Integer[] states = {SendRecordStateEnums.WaitReq.getCode(),SendRecordStateEnums.ReqFail.getCode()};
        /**
         * @begin 2020-12-21
         * ?????????????????????
         * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        LambdaQueryWrapper<SendRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SendRecord::getState,states).in(SendRecord::getId,ids);
        List<SendRecord> datas = this.sendRecordService.list(wrapper);
        int count = 0;
        if(!CollectionUtils.isEmpty(datas))
        {
        	//????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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
        	    //1???????????????id????????? 0????????? ??????????????????????????????
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
        				log.info(String.format("A????????????reSend????????????%s???????????????",smsNumber.toString()));
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
        				log.info(String.format("B????????????reSend????????????%s???????????????",smsNumber.toString()));
        			}
        	    } 
        	}
        }
        /**
         * @end
         */
        return new FebsResponse().success().message(String.format("????????????%s?????????",count));
    }
    
    @ControllerEndpoint(operation = "??????SendRecord", exceptionMessage = "??????SendRecord??????")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("sendRecord:update")
    public FebsResponse updateSendRecord(SendRecord sendRecord) {
        this.sendRecordService.updateSendRecord(sendRecord);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "??????SendRecord", exceptionMessage = "??????Excel??????")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("sendRecord:export")
    public void export(QueryRequest queryRequest, SendRecordQuery sendRecord, HttpServletResponse response) {
        List<SendRecord> sendRecords = this.sendRecordService.findSendRecords(sendRecord);
        List<SendRecordExtExcel> lst = new ArrayList<>();
        sendRecords.stream().forEach(item -> {
            SendRecordExtExcel excel = new SendRecordExtExcel();
            BeanUtils.copyProperties(item,excel);
            //???????????????
            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("?????????:%s ??????:%s %s",
                    code != null? code.getName():"??????",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"??????" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"??????");
            excel.setOperatorInfo(operatorInfo);
            excel.setOrgCode(item.getOrgCode());
            //????????????
            excel.setSmsInfo(String.format("??????:%s ?????????:%s",item.getSmsWords(),item.getSmsCount()));
            lst.add(excel);
        });
        ExcelKit.$Export(SendRecordExtExcel.class, response).downXlsx(lst, false);

    }

    @ControllerEndpoint(operation = "????????????SendRecord", exceptionMessage = "??????Excel??????")
    @GetMapping("export")
    @ResponseBody
    @RequiresPermissions("sendRecord:export")
    public FebsResponse export(QueryRequest queryRequest,SendRecordQuery sendRecord){
        User user = getCurrentUser();
        if (user == null || user.getId() == null){
            return new FebsResponse().fail().message("???????????????");
        }
        Long userId = getCurrentUser().getId();
        this.sendRecordService.exportSendCordFromAdmin(userId,sendRecord);
        return new FebsResponse().success().message("????????????????????????????????????????????????????????????????????????");
    }


    @GetMapping("batchNotifyMsgState/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:batchNotifyMsgState")
    public FebsResponse batchNotifyMsgState(QueryRequest queryRequest,@PathVariable String sendRecordIds) {
        int result = sendRecordService.batchNotifyMsgState(sendRecordIds);
        return new FebsResponse().success().message(String.format("????????????%s???????????????",result));
    }

    @GetMapping("batchNotifyMsgStateByQuery")
    @ResponseBody
    @RequiresPermissions("sendRecord:batchNotifyMsgState")
    public FebsResponse batchNotifyMsgStateByQuery(QueryRequest queryRequest,SendRecordQuery sendRecord) {
        int result = sendRecordService.batchNotifyMsgState(sendRecord);
        return new FebsResponse().success().message(String.format("????????????%s???????????????",result));
    }

    @ControllerEndpoint( operation = "???????????????????????????")
    @GetMapping("rateSuccess")
    @ResponseBody
    @RequiresPermissions("sendRecord:rateSuccess")
    public FebsResponse rateSuccess(QueryRequest request, SendRecordQuery sendRecord) {
    	sendRecord.setSubTypeWith((SendBoxSubTypeEnums.OrgWebSub.getCode()+SendBoxSubTypeEnums.HttpSub.getCode()));
        IPage<Map<String,Object>> datas = this.reportService.statisticRateSuccessGroupBySendCode(request, sendRecord);
        Map<String, Object> dataTable = getDataTable(datas);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "?????????????????????????????????", exceptionMessage = "???????????????????????????????????????")
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


    @ControllerEndpoint( operation = "???????????????????????????")
    @GetMapping("contentRateSuccess")
    @ResponseBody
    @RequiresPermissions("sendRecord:contentRateSuccess")
    public FebsResponse contentRateSuccess(QueryRequest request, SendRecordQuery sendRecord) {
    	sendRecord.setSubTypeWith(SendBoxSubTypeEnums.SmppSub.getCode());
    	IPage<Map<String,Object>> datas = this.reportService.statisticRateSuccessGroupByContent(request, sendRecord);
    	Map<String, Object> dataTable = getDataTable(datas);
    	return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "?????????????????????????????????", exceptionMessage = "???????????????????????????????????????")
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


    @ControllerEndpoint(operation = "????????????????????????", exceptionMessage = "??????????????????????????????")
    @GetMapping("batchUpdateReturnStat/{sendRecordIds}")
    @ResponseBody
    @RequiresPermissions("sendRecord:batchUpdateReturnStat")
    public FebsResponse batchUpdateReturnStat(@RequestParam(value = "success",required = true) boolean success,
                                              @PathVariable String sendRecordIds) {

        if (StringUtils.isBlank(sendRecordIds)){
            return new FebsResponse().fail().message("???????????????????????????");
        }

        String msg = "????????????%s?????????????????????????????????????????????" + (success?"??????":"??????") +"???";
        return new FebsResponse().success().message(String.format(msg,sendRecordService.batchUpdateMsgReturnState(sendRecordIds,success)));
    }

    @ControllerEndpoint(operation = "????????????????????????", exceptionMessage = "??????????????????????????????")
    @GetMapping("batchUpdateReturnStatByRate/{successRate}")
    @ResponseBody
    @RequiresPermissions("sendRecord:batchUpdateReturnStat")
    public FebsResponse batchUpdateReturnStat(@PathVariable BigDecimal successRate,
                                               SendRecordQuery sendRecord) {

        return new FebsResponse().success().message(sendRecordService.batchUpdateMsgReturnState(sendRecord,successRate));
    }
    
    
    /**
     * @begin 2020-11-11
     * ????????????????????????????????????MQ???????????????????????????
     * @param stateTag ???????????? ??????pass
     * @param sendBoxIds  ????????????ID??????????????????????????????json?????????????????????????????????ID
     * @return
     */
    @ControllerEndpoint(operation = "???????????????????????????", exceptionMessage = "????????????????????????")
    @GetMapping("sortingAgain/{stateTag}/{sendBoxIds}")
    @ResponseBody
    @RequiresPermissions("sendBox:sortingAgain")
    public FebsResponse sortingAgainSendBox(@PathVariable("stateTag") String stateTag,@PathVariable("sendBoxIds") String sendBoxIds) 
    {
        if (StringUtils.isBlank(stateTag) || StringUtils.isBlank(sendBoxIds))
        {
            return new FebsResponse().fail().message("?????????????????????????????????");
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
            		//????????????????????????????????????
            		if(thisSendBox.getSortingTime()!=null&&thisSendBox.getSmsCount()>0)
            		{
            			continue;
            		}
            		//?????????????????????????????????????????????
            		SendRecordQuery sendRecord = new SendRecordQuery();
            		sendRecord.setSendCode(thisSendBox.getSendCode());
            		sendRecord.setOrgCode(thisSendBox.getOrgCode());
            		List<SendRecord> sendRecordList =  sendRecordService.findSendRecords(sendRecord);
            		//??????????????????????????????????????????????????????
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
            		 return new FebsResponse().fail().message("?????????????????????????????????");
            	}
        	}
        	else
        	{
        		 return new FebsResponse().fail().message("?????????????????????????????????");
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
