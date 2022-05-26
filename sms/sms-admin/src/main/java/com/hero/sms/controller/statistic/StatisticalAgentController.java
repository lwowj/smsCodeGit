package com.hero.sms.controller.statistic;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.StatisticalAgent;
import com.hero.sms.entity.message.StatisticalAgentExt;
import com.hero.sms.entity.message.StatisticalAgentQuery;
import com.hero.sms.entity.message.exportModel.StatisticalAgentExcel;
import com.hero.sms.service.message.IStatisticalAgentService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

/**
 * 按照代理统计 Controller
 *
 * @author Administrator
 * @date 2020-03-20 14:55:46
 */
@Slf4j
@Validated
@Controller
@RequestMapping("statisticalAgent")
public class StatisticalAgentController extends BaseController {

    @Autowired
    private IStatisticalAgentService statisticalAgentService;

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("statisticalAgent:list")
    public FebsResponse statisticalAgentList(QueryRequest request, StatisticalAgentQuery statisticalAgent) {

        IPage<StatisticalAgent> datas = this.statisticalAgentService.findStatisticalAgents(request, statisticalAgent);
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        datas.getRecords().stream().forEach(item -> {
            Map<String,Object> map = new HashMap<>();
            String agentName = DatabaseCache.getAgentNameByAgentId(item.getAgentId());
            map.put("agentName",agentName);
            map.put("totalCount",item.getTotalCount());
            map.put("sortingFailCount",item.getSortingFailCount());
            map.put("waitReqCount",item.getWaitReqCount());
            Long reqSuccessCount = item.getReqSuccessCount() + item.getReceiptFailCount() + item.getReceiptSuccessCount();
            map.put("reqSuccessCount",reqSuccessCount);
            map.put("reqFailCount",item.getReqFailCount());
            map.put("receiptSuccessCount",item.getReceiptSuccessCount());
            map.put("receiptFailCount",item.getReceiptFailCount());
            map.put("consumeAmount",item.getConsumeAmount());
            map.put("channelCostAmount",item.getChannelCostAmount());
            map.put("incomeAmount",item.getIncomeAmount());
            map.put("agentIncomeAmount",item.getAgentIncomeAmount());
            map.put("statisticalDate",item.getStatisticalDate());
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTable(datas.getTotal(),dataList);
        return new FebsResponse().success().data(dataTable);
    }



    @ControllerEndpoint(operation = "修改StatisticalAgent", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("statisticalAgent:export")
    public void export(StatisticalAgentQuery statisticalAgent, HttpServletResponse response) {
        List<StatisticalAgent> statisticalAgents = this.statisticalAgentService.findStatisticalAgents(statisticalAgent);
        List<StatisticalAgentExt> list = new LinkedList<>();
        try {
            statisticalAgents.stream().forEach(item -> {
                StatisticalAgentExt ext = new StatisticalAgentExt();
                BeanUtils.copyProperties(item,ext);
                Long reqSuccessCount = item.getReqSuccessCount() + item.getReceiptFailCount() + item.getReceiptSuccessCount();
                ext.setReqSuccessCount(reqSuccessCount);
                String rateSuccess = new DecimalFormat("0.00%").format(
                        new BigDecimal(item.getReceiptSuccessCount()).setScale(8).divide(new BigDecimal(ext.getReqSuccessCount()).setScale(8), RoundingMode.UP)
                );
                ext.setRateSuccess(rateSuccess);
                list.add(ext);
            });
            ExcelKit.$Export(StatisticalAgentExcel.class, response).downXlsx(list, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
