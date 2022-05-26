package com.hero.sms.controller.message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.ReturnRecordExt;
import com.hero.sms.entity.message.ReturnRecordQuery;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.message.IReturnRecordService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 回执记录 Controller
 *
 * @author Administrator
 * @date 2020-03-12 00:40:26
 */
@Slf4j
@Validated
@Controller
@RequestMapping("returnRecord")
public class ReturnRecordController extends BaseController {

    @Autowired
    private IReturnRecordService returnRecordService;

    @Autowired
    private IReportService reportService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "returnRecord")
    public String returnRecordIndex(){
        return FebsUtil.view("returnRecord/returnRecord");
    }

    @ControllerEndpoint(operation = "统计数据", exceptionMessage = "统计数据失败")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("returnRecord:statistic")
    public FebsResponse statistic(ReturnRecordQuery returnRecord) {
        return new FebsResponse().success().data(reportService.statisticReturnRecordInfo(returnRecord));
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("returnRecord:list")
    public FebsResponse getAllReturnRecords(ReturnRecordQuery returnRecord) {
        return new FebsResponse().success().data(returnRecordService.findReturnRecords(returnRecord));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("returnRecord:list")
    public FebsResponse returnRecordList(QueryRequest request, ReturnRecordQuery returnRecord) {
        IPage<ReturnRecord> datas = this.returnRecordService.findReturnRecords(request, returnRecord);
        List<ReturnRecordExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item -> {
            ReturnRecordExt returnRecordExt = new ReturnRecordExt();
            BeanUtils.copyProperties(item,returnRecordExt);

            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            returnRecordExt.setOrgName(orgName);

            //短信详情
            String smsInfo = String.format("字数:%s 有效短信数:%s",item.getSmsWords(),item.getSmsCount());
            returnRecordExt.setMsgInfo(smsInfo);

            //提交详情
            String reqStateName = SendRecordStateEnums.getNameByCode(item.getReqState());
            reqStateName = StringUtils.isNotBlank(reqStateName)?reqStateName:"未知";
            String reqTime = "未知";
            if (item.getReqCreateTime() != null){
                reqTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getReqCreateTime());
            }
            String reqInfo = String.format("状态:%s 提交时间:%s 请求返回信息:%s ",
                    reqStateName,reqTime,StringUtils.isNotBlank(item.getReqDesc())?item.getReqDesc():"");
            returnRecordExt.setReqInfo(reqInfo);

            //接收详情
            String returnStateName = CommonStateEnums.getNameByCode(item.getReturnState());
            returnStateName = StringUtils.isNotBlank(returnStateName)?returnStateName:"未知";
            String returnInfo = String.format("状态:%s 描述:%s",
                    returnStateName,StringUtils.isNotBlank(item.getReturnDesc())?item.getReturnDesc():"无");
            returnRecordExt.setReturnInfo(returnInfo);
            returnRecordExt.setSmsChannelName(DatabaseCache.getSmsChannelNameById(item.getChannelId()));
            list.add(returnRecordExt);
        });

        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增ReturnRecord", exceptionMessage = "新增ReturnRecord失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("returnRecord:add")
    public FebsResponse addReturnRecord(@Valid ReturnRecord returnRecord) {
        this.returnRecordService.createReturnRecord(returnRecord);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除ReturnRecord", exceptionMessage = "删除ReturnRecord失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("returnRecord:delete")
    public FebsResponse deleteReturnRecord(ReturnRecordQuery returnRecord) {
        this.returnRecordService.deleteReturnRecord(returnRecord);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除ReturnRecord", exceptionMessage = "批量删除ReturnRecord失败")
    @GetMapping("delete/{returnRecordIds}")
    @ResponseBody
    @RequiresPermissions("returnRecord:delete")
    public FebsResponse deleteReturnRecord(@NotBlank(message = "{required}") @PathVariable String returnRecordIds) {
        String[] ids = returnRecordIds.split(StringPool.COMMA);
        this.returnRecordService.deleteReturnRecords(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改ReturnRecord", exceptionMessage = "修改ReturnRecord失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("returnRecord:update")
    public FebsResponse updateReturnRecord(ReturnRecord returnRecord) {
        this.returnRecordService.updateReturnRecord(returnRecord);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改ReturnRecord", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("returnRecord:export")
    public void export(QueryRequest queryRequest, ReturnRecordQuery returnRecord, HttpServletResponse response) {
        List<ReturnRecord> returnRecords = this.returnRecordService.findReturnRecords(queryRequest, returnRecord).getRecords();
        ExcelKit.$Export(ReturnRecord.class, response).downXlsx(returnRecords, false);
    }
}
