package com.hero.sms.controller.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.message.StatisticalAgent;
import com.hero.sms.entity.message.StatisticalAgentQuery;
import com.hero.sms.service.message.IStatisticalAgentService;

import lombok.extern.slf4j.Slf4j;

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

        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null){
            return new FebsResponse().message("账号异常，请求数据失败");
        }

        statisticalAgent.setAgentId(user.getId());

        IPage<StatisticalAgent> datas = this.statisticalAgentService.findStatisticalAgents(request, statisticalAgent);
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        datas.getRecords().stream().forEach(item -> {
            Map<String,Object> map = new HashMap<>();
            map.put("totalCount",item.getTotalCount());
            map.put("sortingFailCount",item.getSortingFailCount());
            /*map.put("waitReqCount",item.getWaitReqCount());*/
            Long reqSuccessCount = item.getReqSuccessCount() + item.getReceiptFailCount() + item.getReceiptSuccessCount();
            map.put("reqSuccessCount",reqSuccessCount);
            /*map.put("reqFailCount",item.getReqFailCount());
            map.put("receiptSuccessCount",item.getReceiptSuccessCount());
            map.put("receiptFailCount",item.getReceiptFailCount());*/
            map.put("consumeAmount",item.getConsumeAmount());
            map.put("agentIncomeAmount",item.getAgentIncomeAmount());
            map.put("statisticalDate",item.getStatisticalDate());
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);
        return new FebsResponse().success().data(dataTable);
    }
}
