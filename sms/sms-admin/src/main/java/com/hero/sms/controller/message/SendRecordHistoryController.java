package com.hero.sms.controller.message;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.exportModel.AdminSendRecordExcel;
import com.hero.sms.entity.message.history.SendRecordHistory;
import com.hero.sms.entity.message.history.SendRecordHistoryExt;
import com.hero.sms.entity.message.history.SendRecordHistoryQuery;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.message.ISendRecordHistoryService;
import com.wuwenze.poi.ExcelKit;

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
    @Autowired
    private IReportService reportService;

    @ControllerEndpoint(operation = "统计数据", exceptionMessage = "统计数据失败")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("sendRecordHistory:statistic")
    public FebsResponse statistic(SendRecordHistoryQuery sendRecordHistory) {
        Map<String, Object> result = reportService.statisticSendRecordHistoryInfo(sendRecordHistory);
        Long reqSuccess = (Long) result.get("reqSuccess");
        Long receiptSuccess = (Long) result.get("receiptSuccess");
        Long receiptFail = (Long) result.get("receiptFail");
        Long realReqSuccess = reqSuccess + receiptSuccess + receiptFail;
        result.put("allReqSuccess",realReqSuccess);
        return new FebsResponse().success().data(result);
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sendRecordHistory:list")
    public FebsResponse sendRecordHistoryList(QueryRequest request, SendRecordHistoryQuery sendRecordHistory) {

        IPage<SendRecordHistory> datas = this.sendRecordHistoryService.findSendRecordHistorys(request, sendRecordHistory);
        List<SendRecordHistory> list = datas.getRecords();

        List<SendRecordHistoryExt> dataList = new ArrayList<>();
        list.stream().forEach(item -> {// 转成map  过滤掉敏感字段
            SendRecordHistoryExt sendRecordHistoryExt = new SendRecordHistoryExt();
            BeanUtils.copyProperties(item,sendRecordHistoryExt);

            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            sendRecordHistoryExt.setOrgName(orgName);

            //短信详情
            String smsInfo = String.format("字数:%s 有效短信数:%s",item.getSmsWords(),item.getSmsCount());
            sendRecordHistoryExt.setSmsInfo(smsInfo);
            //运营商详情
            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("运营商:%s 地域:%s %s",
                    code != null? code.getName():"未知",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
            sendRecordHistoryExt.setOperatorInfo(operatorInfo);
            //金额详情
            BigDecimal divideNum = new BigDecimal(100);
            BigDecimal consumeAmout = new BigDecimal(item.getConsumeAmount()!=null?item.getConsumeAmount():0).divide(divideNum);
            BigDecimal channelCostAmout = new BigDecimal(item.getChannelCostAmount()!=null?item.getChannelCostAmount():0).divide(divideNum );
            BigDecimal agentIncomeAmout = new BigDecimal(item.getAgentIncomeAmount()!=null?item.getAgentIncomeAmount():0).divide(divideNum);
            BigDecimal incomeAmout = new BigDecimal(item.getIncomeAmount()!=null?item.getIncomeAmount():0).divide(divideNum);
            BigDecimal upAgentIncomeAmout = new BigDecimal(item.getUpAgentIncomeAmount() != null?item.getUpAgentIncomeAmount():0).divide(divideNum);
            String amountInfo = String.format("消费金额:%s元 通道成本:%s元 代理商收益:%s元 上级代理收益:%s元 平台收益:%s元"
                    ,consumeAmout,channelCostAmout,agentIncomeAmout,upAgentIncomeAmout,incomeAmout);
            sendRecordHistoryExt.setAmountInfo(amountInfo);
            sendRecordHistoryExt.setSmsChannelName(DatabaseCache.getSmsChannelNameById(item.getChannelId()));

            dataList.add(sendRecordHistoryExt);
        });

        Map<String, Object> dataTable = getDataTable(datas.getTotal(),dataList);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "导出SendRecordHistory", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("sendRecordHistory:export")
    public void export(QueryRequest queryRequest, SendRecordHistoryQuery sendRecordHistory, HttpServletResponse response) {

        List<SendRecordHistory> sendRecords = this.sendRecordHistoryService.findSendRecordHistorys(sendRecordHistory);
        List<AdminSendRecordExcel> lst = new ArrayList<>();
        sendRecords.stream().forEach(item -> {
            AdminSendRecordExcel excel = new AdminSendRecordExcel();
            BeanUtils.copyProperties(item,excel);

            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            excel.setOrgName(orgName);

            //短信详情
            String smsInfo = String.format("字数:%s 有效短信数:%s",item.getSmsWords(),item.getSmsCount());
            excel.setSmsInfo(smsInfo);
            //运营商详情
            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("运营商:%s 地域:%s %s",
                    code != null? code.getName():"未知",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
            excel.setOperatorInfo(operatorInfo);
            //金额详情
            BigDecimal divideNum = new BigDecimal(100);
            BigDecimal consumeAmout = new BigDecimal(item.getConsumeAmount()).divide(divideNum);
            BigDecimal channelCostAmout = new BigDecimal(item.getChannelCostAmount()).divide(divideNum );
            BigDecimal agentIncomeAmout = new BigDecimal(item.getAgentIncomeAmount()).divide(divideNum);
            BigDecimal incomeAmout = new BigDecimal(item.getIncomeAmount()).divide(divideNum);
            String amountInfo = String.format("消费金额:%s元 通道成本:%s元 代理商收益:%s元 平台收益:%s元"
                    ,consumeAmout,channelCostAmout,agentIncomeAmout,incomeAmout);
            excel.setAmountInfo(amountInfo);
            excel.setSmsTypeName(SmsTypeEnums.getNameByCode(item.getSmsType()));
            excel.setStateName(AuditStateEnums.getNameByCode(item.getState()));
            excel.setChannelName(DatabaseCache.getSmsChannelNameById(item.getChannelId()));
            excel.setReturnTime(DateUtil.getDistanceTimes(item.getCreateTime(),item.getReceiptTime()));
            excel.setReceiptTime(item.getReceiptTime());
            lst.add(excel);
        });
        try {
            ExcelKit.$Export(AdminSendRecordExcel.class, response).downXlsx(lst, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
