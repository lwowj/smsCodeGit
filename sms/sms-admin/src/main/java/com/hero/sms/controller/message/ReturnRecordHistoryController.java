package com.hero.sms.controller.message;

import java.text.SimpleDateFormat;
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
import com.hero.sms.entity.message.exportModel.AdminReturnRecordExcel;
import com.hero.sms.entity.message.history.ReturnRecordHistory;
import com.hero.sms.entity.message.history.ReturnRecordHistoryExt;
import com.hero.sms.entity.message.history.ReturnRecordHistoryQuery;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.message.IReturnRecordHistoryService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 历史回执记录 Controller
 *
 * @author Administrator
 * @date 2020-03-15 23:31:41
 */
@Slf4j
@Validated
@Controller
@RequestMapping("returnRecordHistory")
public class ReturnRecordHistoryController extends BaseController {

    @Autowired
    private IReturnRecordHistoryService returnRecordHistoryService;

    @Autowired
    private IReportService reportService;

    @ControllerEndpoint(operation = "统计数据", exceptionMessage = "统计数据失败")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("returnRecordHistory:statistic")
    public FebsResponse statistic(ReturnRecordHistoryQuery returnRecordHistory) {
        return new FebsResponse().success().data(reportService.statisticReturnRecordHistoryInfo(returnRecordHistory));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("returnRecordHistory:list")
    public FebsResponse returnRecordHistoryList(QueryRequest request, ReturnRecordHistoryQuery returnRecordHistory) {

        IPage<ReturnRecordHistory> datas = this.returnRecordHistoryService.findReturnRecordHistorys(request, returnRecordHistory);
        List<ReturnRecordHistory> list = datas.getRecords();
        List<ReturnRecordHistoryExt> dataList = new ArrayList<ReturnRecordHistoryExt>();
        //转成map  过滤掉敏感字段
        list.stream().forEach(item -> {
            ReturnRecordHistoryExt ext = new ReturnRecordHistoryExt();
            BeanUtils.copyProperties(item,ext);
            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            ext.setOrgName(orgName);

            //短信详情
            String smsInfo = String.format("字数:%s 有效短信数:%s",item.getSmsWords(),item.getSmsCount());
            ext.setMsgInfo(smsInfo);

            //提交详情
            String reqStateName = SendRecordStateEnums.getNameByCode(item.getReqState());
            reqStateName = StringUtils.isNotBlank(reqStateName)?reqStateName:"未知";
            String reqTime = "未知";
            if (item.getReqCreateTime() != null){
                reqTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getReqCreateTime());
            }
            String reqInfo = String.format("状态:%s 提交时间:%s 请求返回信息:%s ",
                    reqStateName,reqTime,StringUtils.isNotBlank(item.getReqDesc())?item.getReqDesc():"");
            ext.setReqInfo(reqInfo);

            //接收详情
            String returnStateName = CommonStateEnums.getNameByCode(item.getReturnState());
            returnStateName = StringUtils.isNotBlank(returnStateName)?returnStateName:"未知";
            String returnInfo = String.format("状态:%s 描述:%s",
                    returnStateName,StringUtils.isNotBlank(item.getReturnDesc())?item.getReturnDesc():"无");
            ext.setReturnInfo(returnInfo);
            ext.setSmsTypeName(SmsTypeEnums.getNameByCode(item.getSmsType()));
            ext.setSmsChannelName(DatabaseCache.getSmsChannelNameById(item.getChannelId()));
            dataList.add(ext);
        });

        Map<String, Object> dataTable = getDataTable(datas.getTotal(),dataList);
        return new FebsResponse().success().data(dataTable);
    }


    @ControllerEndpoint(operation = "修改ReturnRecordHistory", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("returnRecordHistory:export")
    public void export(QueryRequest queryRequest, ReturnRecordHistoryQuery returnRecordHistory, HttpServletResponse response) {

        List<ReturnRecordHistory> returnRecords = this.returnRecordHistoryService.findReturnRecordHistorys(returnRecordHistory);
        List<AdminReturnRecordExcel> lst = new ArrayList<>();

        returnRecords.stream().forEach(item -> {
            AdminReturnRecordExcel excel = new AdminReturnRecordExcel();
            BeanUtils.copyProperties(item,excel);

            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            excel.setOrgName(orgName);

            //短信详情
            String smsInfo = String.format("字数:%s 有效短信数:%s",item.getSmsWords(),item.getSmsCount());
            excel.setMsgInfo(smsInfo);

            //提交详情
            String reqStateName = SendRecordStateEnums.getNameByCode(item.getReqState());
            reqStateName = StringUtils.isNotBlank(reqStateName)?reqStateName:"未知";
            String reqTime = "未知";
            if (item.getReqCreateTime() != null){
                reqTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getReqCreateTime());
            }
            String reqInfo = String.format("状态:%s 提交时间:%s 请求返回信息:%s ",
                    reqStateName,reqTime,StringUtils.isNotBlank(item.getReqDesc())?item.getReqDesc():"");
            excel.setReqInfo(reqInfo);

            //接收详情
            String returnStateName = CommonStateEnums.getNameByCode(item.getReturnState());
            returnStateName = StringUtils.isNotBlank(returnStateName)?returnStateName:"未知";
            String returnInfo = String.format("状态:%s 描述:%s",
                    returnStateName,StringUtils.isNotBlank(item.getReturnDesc())?item.getReturnDesc():"无");
            excel.setReturnInfo(returnInfo);
            excel.setSmsTypeName(SmsTypeEnums.getNameByCode(item.getSmsType()));
            excel.setChannelName(DatabaseCache.getSmsChannelNameById(item.getChannelId()));
            lst.add(excel);
        });

        ExcelKit.$Export(AdminReturnRecordExcel.class, response).downXlsx(lst, false);
    }
}
