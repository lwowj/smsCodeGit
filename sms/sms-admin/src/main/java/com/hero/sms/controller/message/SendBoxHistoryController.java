package com.hero.sms.controller.message;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.exportModel.AdminSendBoxExcel;
import com.hero.sms.entity.message.history.SendBoxHistory;
import com.hero.sms.entity.message.history.SendBoxHistoryExt;
import com.hero.sms.entity.message.history.SendBoxHistoryQuery;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.message.ISendBoxHistoryService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 历史发件箱 Controller
 *
 * @author Administrator
 * @date 2020-03-15 23:31:27
 */
@Slf4j
@Validated
@Controller
@RequestMapping("sendBoxHistory")
public class SendBoxHistoryController extends BaseController {

    @Autowired
    private ISendBoxHistoryService sendBoxHistoryService;
    @Autowired
    private IReportService reportService;


    @ControllerEndpoint(operation = "统计数据", exceptionMessage = "统计数据失败")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:statistic")
    public FebsResponse statistic(SendBoxHistoryQuery sendBoxHistory) {
        return new FebsResponse().success().data(reportService.statisticSendBoxHistoryInfo(sendBoxHistory));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:list")
    public FebsResponse sendBoxHistoryList(QueryRequest request, SendBoxHistoryQuery sendBoxHistory) {

        IPage<SendBoxHistory> datas = this.sendBoxHistoryService.findSendBoxHistorys(request, sendBoxHistory);
        List<SendBoxHistory> sendBoxList = datas.getRecords();

        List<SendBoxHistoryExt> list = new ArrayList<>();
        sendBoxList.stream().forEach(item -> {
            SendBoxHistoryExt ext = new SendBoxHistoryExt();
            BeanUtils.copyProperties(item,ext);
            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            ext.setOrgName(orgName);
            //短信详情
            String smsInfo = String.format("字数:%s 有效短信数:%s 号码数:%s",item.getSmsWords(),item.getSmsCount(),item.getNumberCount()!=null?item.getNumberCount():0);
            ext.setSmsInfo(smsInfo);

            //短信金额详情
            BigDecimal divideNum = new BigDecimal(100);
            BigDecimal consumeAmout = new BigDecimal(item.getConsumeAmount()!=null?item.getConsumeAmount():0).divide(divideNum);
            BigDecimal channelCostAmout = new BigDecimal(item.getChannelCostAmount()!=null?item.getChannelCostAmount():0).divide(divideNum );
            BigDecimal agentIncomeAmout = new BigDecimal(item.getAgentIncomeAmount()!=null?item.getAgentIncomeAmount():0).divide(divideNum);
            BigDecimal incomeAmout = new BigDecimal(item.getIncomeAmount()!=null?item.getIncomeAmount():0).divide(divideNum);
            BigDecimal upAgentIncomeAmout = new BigDecimal(item.getUpAgentIncomeAmount() != null?item.getUpAgentIncomeAmount():0).divide(divideNum);

            String amountInfo = String.format("消费金额:%s元 通道成本:%s元 代理收益:%s元 上级代理收益:%s元 平台收益:%s元"
                    ,consumeAmout,channelCostAmout,agentIncomeAmout,upAgentIncomeAmout,incomeAmout);
            ext.setAmountInfo(amountInfo);
            ext.setAuditName(AuditStateEnums.getNameByCode(item.getAuditState()));
            ext.setMsgTypeName(SmsTypeEnums.getNameByCode(item.getSmsType()));
            list.add(ext);
        });

        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);

        return new FebsResponse().success().data(dataTable);
    }

    /**
     * 获取手机码号
     * @param sendBoxId
     * @return
     */
    @GetMapping("smsNumbers/{sendBoxId}")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:list")
    public FebsResponse getSmsNumbers(@PathVariable("sendBoxId") Long sendBoxId){
        return new FebsResponse().success().data(this.sendBoxHistoryService.getSmsNumbersByID(sendBoxId));
    }


    @ControllerEndpoint(operation = "修改SendBoxHistory", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:export")
    public void export(QueryRequest queryRequest, SendBoxHistoryQuery sendBoxHistory, HttpServletResponse response) {
        List<SendBoxHistory> sendBoxs = this.sendBoxHistoryService.findSendBoxHistorys(sendBoxHistory);

        List<AdminSendBoxExcel> lst = new ArrayList<>();
        sendBoxs.stream().forEach(item -> {
            AdminSendBoxExcel temp = new AdminSendBoxExcel();
            BeanUtils.copyProperties(item,temp);
            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            temp.setOrgName(orgName);
            //短信详情
            String smsInfo = String.format("字数:%s 有效短信数:%s 号码数:%s",item.getSmsWords(),item.getSmsCount(),item.getNumberCount());
            temp.setSmsInfo(smsInfo);

            //短信金额详情
            BigDecimal divideNum = new BigDecimal(100);
            BigDecimal consumeAmout = new BigDecimal(item.getConsumeAmount()!=null?item.getConsumeAmount():0).divide(divideNum);
            BigDecimal channelCostAmout = new BigDecimal(item.getChannelCostAmount()!=null?item.getChannelCostAmount():0).divide(divideNum );
            BigDecimal agentIncomeAmout = new BigDecimal(item.getAgentIncomeAmount()!=null?item.getAgentIncomeAmount():0).divide(divideNum);
            BigDecimal incomeAmout = new BigDecimal(item.getIncomeAmount()!=null?item.getIncomeAmount():0).divide(divideNum);
            String amountInfo = String.format("消费金额:%s元 通道成本:%s元 代理商收益:%s元 平台收益:%s元"
                    ,consumeAmout,channelCostAmout,agentIncomeAmout,incomeAmout);
            temp.setAmountInfo(amountInfo);
            temp.setAuditName(AuditStateEnums.getNameByCode(item.getAuditState()));
            temp.setMsgTypeName(SmsTypeEnums.getNameByCode(item.getSmsType()));
            lst.add(temp);
        });

        ExcelKit.$Export(AdminSendBoxExcel.class, response).downXlsx(lst, false);
    }
}
