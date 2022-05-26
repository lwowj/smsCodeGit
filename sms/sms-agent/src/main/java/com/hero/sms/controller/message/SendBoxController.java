package com.hero.sms.controller.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.entity.message.history.SendBoxHistory;
import com.hero.sms.entity.message.history.SendBoxHistoryQuery;
import com.hero.sms.enums.agent.AgentLevelEnums;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.message.ISendBoxHistoryService;
import com.hero.sms.service.message.ISendBoxService;

@RestController
public class SendBoxController extends BaseController {

    @Autowired
    private ISendBoxService sendBoxService;

    @Autowired
    private ISendBoxHistoryService sendBoxHistoryService;

    @Autowired
    private IReportService reportService;

    @ControllerEndpoint(operation = "发件箱统计数据", exceptionMessage = "统计数据失败")
    @GetMapping("sendBox/statistic")
    @ResponseBody
    @RequiresPermissions("sendBox:statistic")
    @Limit(key = "agentSendBoxStatistic", period = 2, count = 1, name = "发件箱统计数据", prefix = "limit")
    public FebsResponse statistic(SendBoxQuery sendBox) {
        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        sendBox.setAgentId(user.getId());
        Map<String, Object> result = reportService.statisticSendBoxInfo(sendBox);
        result.remove("agentIncomeAmount");
        result.remove("upAgentIncomeAmount");
        result.remove("incomeAmount");
        result.remove("channelCost");
        return new FebsResponse().success().data(result);
    }

    /**
     * 列表页数据查询
     * @param request
     * @param sendBox
     * @return
     */
    @ControllerEndpoint(operation = "发件箱列表查询")
    @GetMapping("sendBox/list")
    @ResponseBody
    @RequiresPermissions("sendBox:list")
    @Limit(key = "agentSendBoxList", period = 1, count = 1, name = "发件箱列表查询", prefix = "limit")
    public FebsResponse sendBoxList(QueryRequest request, SendBoxQuery sendBox) {
        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己的发件箱

        //sendBox.setCreateUsername(user.getUserAccount());
        sendBox.setAgentId(user.getId());
        IPage<SendBox> datas = this.sendBoxService.findSendBoxs(request, sendBox);
        List<SendBox> sendBoxList = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        sendBoxList.stream().forEach(item -> { // 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("sendCode",item.getSendCode());//批次号
            map.put("smsType",item.getSmsType());//短信类型
            map.put("agentIncomeAmount",item.getAgentIncomeAmount()==null?0:item.getAgentIncomeAmount());
            map.put("consumeAmount",item.getConsumeAmount()==null?0:item.getConsumeAmount());
            map.put("auditState",item.getAuditState());//审核状态
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("smsNumberArea",item.getSmsNumberArea());//所属区域
            map.put("timingTime",item.getTimingTime());//定时时间
            map.put("isTimingTime",item.getIsTimingTime());//定时时间
            map.put("createTime",item.getCreateTime());//提交时间
            map.put("createUsername",item.getCreateUsername());//提交人
            map.put("numberCount",item.getNumberCount() == null?0:item.getNumberCount());
            map.put("refuseCause",item.getRefuseCause());//拒绝原因
            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            map.put("orgName",orgName);
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);

        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "子代理发件箱统计数据", exceptionMessage = "统计数据失败")
    @GetMapping("sendBox/subordinateStatistic")
    @ResponseBody
    @RequiresPermissions("sendBox:subordinateStatistic")
    @Limit(key = "agentSendBoxsubordinateStatistic", period = 2, count = 1, name = "子代理发件箱统计数据", prefix = "limit")
    public FebsResponse subordinateStatistic(SendBoxQuery sendBox) {
        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        if (Integer.valueOf(user.getLevelCode()).intValue() !=  AgentLevelEnums.LEVEL_1.getCode()){
            return new FebsResponse().message("无权限").fail();
        }
        sendBox.setUpAgentId(user.getId());
        Map<String, Object> result = reportService.statisticSendBoxInfo(sendBox);
        result.remove("agentIncomeAmount");
        result.remove("incomeAmount");
        result.remove("channelCost");
        return new FebsResponse().success().data(result);
    }
    /**
     * 列表页数据查询
     * @param request
     * @param sendBox
     * @return
     */
    @ControllerEndpoint(operation = "子代理发件箱列表查询")
    @GetMapping("sendBox/subordinateList")
    @ResponseBody
    @RequiresPermissions("sendBox:subordinateList")
    @Limit(key = "agentSendBoxSubordinateList", period = 1, count = 1, name = "子代理发件箱列表查询", prefix = "limit")
    public FebsResponse sendBoxSubordinateList(QueryRequest request, SendBoxQuery sendBox) {
        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        if (Integer.valueOf(user.getLevelCode()).intValue() !=  AgentLevelEnums.LEVEL_1.getCode()){
            return new FebsResponse().message("无权限").fail();
        }
        sendBox.setUpAgentId(user.getId());
        IPage<SendBox> datas = this.sendBoxService.findSendBoxs(request, sendBox);
        List<SendBox> sendBoxList = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        sendBoxList.stream().forEach(item -> { // 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("sendCode",item.getSendCode());//批次号
            map.put("smsType",item.getSmsType());//短信类型
            map.put("agentIncomeAmount",item.getUpAgentIncomeAmount()==null?0:item.getUpAgentIncomeAmount());//利润
            map.put("consumeAmount",item.getConsumeAmount()==null?0:item.getConsumeAmount());
            map.put("auditState",item.getAuditState());//审核状态
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("smsNumberArea",item.getSmsNumberArea());//所属区域
            map.put("timingTime",item.getTimingTime());//定时时间
            map.put("isTimingTime",item.getIsTimingTime());//定时时间
            map.put("createTime",item.getCreateTime());//提交时间
            map.put("createUsername",item.getCreateUsername());//提交人
            map.put("numberCount",item.getNumberCount() == null?0:item.getNumberCount());
            map.put("refuseCause",item.getRefuseCause());//拒绝原因
            //下级代理
            String subordinateAgentName = DatabaseCache.getAgentNameByAgentId(item.getAgentId());
            map.put("subordinateAgentName",subordinateAgentName);
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);

        return new FebsResponse().success().data(dataTable);
    }


    @ControllerEndpoint(operation = "代理历史发件箱列表查询")
    @GetMapping("sendBoxHistory/list")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:list")
    @Limit(key = "agentSendBoxHistoryList", period = 1, count = 1, name = "代理历史发件箱列表查询", prefix = "limit")
    public FebsResponse sendBoxHistoryList(QueryRequest request, SendBoxHistoryQuery sendBoxHistory) {

        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己的发件箱

        //sendBox.setCreateUsername(user.getUserAccount());
        sendBoxHistory.setAgentId(user.getId());
        IPage<SendBoxHistory> datas = this.sendBoxHistoryService.findSendBoxHistorys(request, sendBoxHistory);
        List<SendBoxHistory> sendBoxList = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        sendBoxList.stream().forEach(item -> { // 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("sendCode",item.getSendCode());//批次号
            map.put("agentIncomeAmount",item.getAgentIncomeAmount()==null?0:item.getAgentIncomeAmount());
            map.put("consumeAmount",item.getConsumeAmount()==null?0:item.getConsumeAmount());
            map.put("auditState",item.getAuditState());//审核状态
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("smsNumberArea",item.getSmsNumberArea());//所属区域
            map.put("timingTime",item.getTimingTime());//定时时间
            map.put("isTimingTime",item.getIsTimingTime());//定时时间
            map.put("createTime",item.getCreateTime());//提交时间
            map.put("numberCount",item.getNumberCount() == null?0:item.getNumberCount());
            map.put("refuseCause",item.getRefuseCause());//拒绝原因
            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            map.put("orgName",orgName);
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);

        return new FebsResponse().success().data(dataTable);
    }

    /**
     * 下级代理发件箱
     * @param request
     * @param sendBoxHistory
     * @return
     */
    @ControllerEndpoint(operation = "下级代理历史发件箱列表查询")
    @GetMapping("sendBoxHistory/subordinateList")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:subordinateList")
    @Limit(key = "agentSendBoxHistorySubordinateList", period = 1, count = 1, name = "下级代理历史发件箱列表查询", prefix = "limit")
    public FebsResponse sendBoxHistorySubordinateList(QueryRequest request, SendBoxHistoryQuery sendBoxHistory) {

        Agent user = super.getCurrentAgent();
        if (user == null || user.getId() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        if (Integer.valueOf(user.getLevelCode()).intValue() !=  AgentLevelEnums.LEVEL_1.getCode()){
            return new FebsResponse().message("无权限").fail();
        }

        sendBoxHistory.setUpAgentId(user.getId());
        IPage<SendBoxHistory> datas = this.sendBoxHistoryService.findSendBoxHistorys(request, sendBoxHistory);
        List<SendBoxHistory> sendBoxList = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        sendBoxList.stream().forEach(item -> { // 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("sendCode",item.getSendCode());//批次号
            map.put("agentIncomeAmount",item.getUpAgentIncomeAmount()==null?0:item.getUpAgentIncomeAmount());//利润
            map.put("consumeAmount",item.getConsumeAmount()==null?0:item.getConsumeAmount());
            map.put("auditState",item.getAuditState());//审核状态
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("smsNumberArea",item.getSmsNumberArea());//所属区域
            map.put("timingTime",item.getTimingTime());//定时时间
            map.put("isTimingTime",item.getIsTimingTime());//定时时间
            map.put("createTime",item.getCreateTime());//提交时间
            map.put("numberCount",item.getNumberCount() == null?0:item.getNumberCount());
            map.put("refuseCause",item.getRefuseCause());//拒绝原因
            //下级代理
            String subordinateAgentName = DatabaseCache.getAgentNameByAgentId(item.getAgentId());
            map.put("subordinateAgentName",subordinateAgentName);
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);

        return new FebsResponse().success().data(dataTable);
    }

}
