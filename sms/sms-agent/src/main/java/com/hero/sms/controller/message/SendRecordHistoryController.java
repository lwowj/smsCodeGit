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
import com.hero.sms.entity.message.history.SendRecordHistory;
import com.hero.sms.entity.message.history.SendRecordHistoryQuery;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.service.message.ISendRecordHistoryService;

import lombok.extern.slf4j.Slf4j;

/**
 * 历史发送记录 Controller
 *
 * @author Administrator
 * @date 2020-03-15 23:31:38
 */
@Slf4j
@Validated
@Controller
@RequestMapping("sendRecordHistory")
public class SendRecordHistoryController extends BaseController {

    @Autowired
    private ISendRecordHistoryService sendRecordHistoryService;

    @ControllerEndpoint( operation = "历史发件箱列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sendRecordHistory:list")
    @Limit(key = "agentSendRecordHistoryList", period = 1, count = 1, name = "历史发件箱列表", prefix = "limit")
    public FebsResponse sendRecordHistoryList(QueryRequest request, SendRecordHistoryQuery sendRecordHistory) {

        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }

        //限制只能查询自己的发件箱
        sendRecordHistory.setAgentId(user.getId());

        IPage<SendRecordHistory> datas = this.sendRecordHistoryService.findSendRecordHistorys(request, sendRecordHistory);

        List<SendRecordHistory> list = datas.getRecords();
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
            map.put("stateDesc",item.getStateDesc());
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


    @ControllerEndpoint( operation = "历史发件箱列表")
    @GetMapping("subordinateList")
    @ResponseBody
    @RequiresPermissions("sendRecordHistory:subordinateList")
    @Limit(key = "agentSendRecordHistorySubordinateList", period = 1, count = 1, name = "历史发件箱列表", prefix = "limit")
    public FebsResponse subordinateList(QueryRequest request, SendRecordHistoryQuery sendRecordHistory) {

        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }

        //限制只能查询自己的发件箱
        sendRecordHistory.setUpAgentId(user.getId());

        IPage<SendRecordHistory> datas = this.sendRecordHistoryService.findSendRecordHistorys(request, sendRecordHistory);

        List<SendRecordHistory> list = datas.getRecords();
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
            map.put("stateDesc",item.getStateDesc());
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

}
