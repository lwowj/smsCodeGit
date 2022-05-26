package com.hero.sms.controller.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.message.ISendRecordService;

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
    private IReportService reportService;



    @ControllerEndpoint(operation = "统计发送记录数据", exceptionMessage = "统计数据失败")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("sendRecord:statistic")
    @Limit(key = "agentSendRecordStatistic", period = 2, count = 1, name = "统计发送记录数据", prefix = "limit")
    public FebsResponse statistic(SendRecordQuery sendRecord) {
        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己的发件箱
        sendRecord.setAgentId(user.getId());
        Map<String, Object> result = reportService.statisticSendRecordInfo(sendRecord);
        Long reqSuccess = (Long) result.get("reqSuccess");
        Long receiptSuccess = (Long) result.get("receiptSuccess");
        Long receiptFail = (Long) result.get("receiptFail");
        Long realReqSuccess = reqSuccess + receiptSuccess + receiptFail;
        result.put("allReqSuccess",realReqSuccess);
        result.remove("channelCost");
        result.remove("agentIncomeAmount");
        result.remove("incomeAmount");
        result.remove("upAgentIncomeAmount");
        return new FebsResponse().success().data(result);
    }

    @ControllerEndpoint( operation = "查询发送记录")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sendRecord:list")
    @Limit(key = "agentSendRecordList", period = 1, count = 1, name = "查询发送记录", prefix = "limit")
    public FebsResponse sendRecordList(QueryRequest request, SendRecordQuery sendRecord) {

        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己的发件箱
        sendRecord.setAgentId(user.getId());

        IPage<SendRecord> datas = this.sendRecordService.findSendRecords(request, sendRecord);

        List<SendRecord> list = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        list.stream().forEach(item -> {// 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("sendCode",item.getSendCode());//批次号
            String phone = StringUtils.isBlank(item.getSmsNumber())?"":item.getSmsNumber();
            String phoneNumber = phone.replaceAll("(\\d{3})\\d{5}(\\d{3})","$1*****$2");
            map.put("smsNumber",phoneNumber);//手机号码
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("state",item.getState());//状态
            map.put("stateDesc",item.getStateDesc());//状态
            map.put("smsNumberOperator",item.getSmsNumberOperator());//运营商
            map.put("smsType",item.getSmsType());//消息类型
            map.put("smsNumberArea",item.getSmsNumberArea());//手机号码归属地区
            map.put("smsNumberProvince",item.getSmsNumberProvince());//手机号码鬼实地（省份）
            map.put("smsCount",item.getSmsCount());//有效短信数
            map.put("smsWords",item.getSmsWords());//短信字数
            map.put("returnTime", DateUtil.getDistanceTimes(item.getCreateTime(),item.getReceiptTime()));
            map.put("receiptTime",item.getReceiptTime());
            map.put("createTime",item.getCreateTime());
            map.put("orgCode",item.getOrgCode());

            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("运营商:%s 地域:%s %s",
                    code != null? code.getName():"未知",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
            map.put("operatorInfo",operatorInfo);
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);
        return new FebsResponse().success().data(dataTable);
    }


    @ControllerEndpoint(operation = "统计下级代理发送记录数据", exceptionMessage = "统计失败")
    @GetMapping("subordinateStatistic")
    @ResponseBody
    @RequiresPermissions("sendRecord:subordinateStatistic")
    @Limit(key = "agentSendRecordSubordinateStatistic", period = 2, count = 1, name = "统计下级代理发送记录数据", prefix = "limit")
    public FebsResponse subordinateStatistic(SendRecordQuery sendRecord) {
        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己下级代理的发件箱
        sendRecord.setUpAgentId(user.getId());
        Map<String, Object> result = reportService.statisticSendRecordInfo(sendRecord);
        Long reqSuccess = (Long) result.get("reqSuccess");
        Long receiptSuccess = (Long) result.get("receiptSuccess");
        Long receiptFail = (Long) result.get("receiptFail");
        Long realReqSuccess = reqSuccess + receiptSuccess + receiptFail;
        result.put("allReqSuccess",realReqSuccess);
        result.remove("channelCost");
        result.remove("agentIncomeAmount");
        result.remove("incomeAmount");
        return new FebsResponse().success().data(result);
    }

    @ControllerEndpoint( operation = "查询发送记录")
    @GetMapping("subordinateList")
    @ResponseBody
    @RequiresPermissions("sendRecord:subordinateList")
    @Limit(key = "agentSendRecordSubordinateList", period = 1, count = 1, name = "查询发送记录", prefix = "limit")
    public FebsResponse subordinateList(QueryRequest request, SendRecordQuery sendRecord) {

        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己下级代理的发件箱
        sendRecord.setUpAgentId(user.getId());

        IPage<SendRecord> datas = this.sendRecordService.findSendRecords(request, sendRecord);

        List<SendRecord> list = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        list.stream().forEach(item -> {// 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("sendCode",item.getSendCode());//批次号
            String phone = StringUtils.isBlank(item.getSmsNumber())?"":item.getSmsNumber();
            String phoneNumber = phone.replaceAll("(\\d{3})\\d{5}(\\d{3})","$1*****$2");
            map.put("smsNumber",phoneNumber);//手机号码
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("state",item.getState());//状态
            map.put("stateDesc",item.getStateDesc());//状态
            map.put("smsNumberOperator",item.getSmsNumberOperator());//运营商
            map.put("smsType",item.getSmsType());//消息类型
            map.put("smsNumberArea",item.getSmsNumberArea());//手机号码归属地区
            map.put("smsNumberProvince",item.getSmsNumberProvince());//手机号码鬼实地（省份）
            map.put("smsCount",item.getSmsCount());//有效短信数
            map.put("smsWords",item.getSmsWords());//短信字数
            map.put("returnTime", DateUtil.getDistanceTimes(item.getCreateTime(),item.getReceiptTime()));
            map.put("receiptTime",item.getReceiptTime());
            map.put("createTime",item.getCreateTime());
            map.put("orgCode",item.getOrgCode());

            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("运营商:%s 地域:%s %s",
                    code != null? code.getName():"未知",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
            map.put("operatorInfo",operatorInfo);

            dataList.add(map);
        });
        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);
        return new FebsResponse().success().data(dataTable);
    }


    @ControllerEndpoint(operation = "查询发送成功率")
    @GetMapping("rateSuccess")
    @ResponseBody
    @RequiresPermissions("sendRecord:rateSuccess")
    @Limit(key = "agentSendRecordRateSuccess", period = 1, count = 1, name = "查询发送成功率", prefix = "limit")
    public FebsResponse rateSuccess(QueryRequest request, SendRecordQuery sendRecord) {

        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己的发件箱
        sendRecord.setAgentId(user.getId());
        IPage<Map<String,Object>> datas = this.reportService.statisticRateSuccessGroupBySendCode(request, sendRecord);
        Map<String, Object> dataTable = getDataTable(datas);
        return new FebsResponse().success().data(dataTable);
    }

}
