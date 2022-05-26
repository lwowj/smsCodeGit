package com.hero.sms.controller.statistic;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.StatisticalOrgcode;
import com.hero.sms.entity.message.StatisticalOrgcodeExt;
import com.hero.sms.entity.message.StatisticalOrgcodeQuery;
import com.hero.sms.entity.message.exportModel.StatisticalOrgTotalExcel;
import com.hero.sms.entity.message.exportModel.StatisticalOrgcodeBusinessExcel;
import com.hero.sms.entity.message.exportModel.StatisticalOrgcodeBusinessOnMonthExcel;
import com.hero.sms.service.message.IStatisticalOrgcodeService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 按照商户统计 Controller
 *
 * @author Administrator
 * @date 2020-03-16 16:35:02
 */
@Slf4j
@Validated
@Controller
@RequestMapping("statisticalOrgcode")
public class StatisticalOrgcodeController extends BaseController {

    @Autowired
    private IStatisticalOrgcodeService statisticalOrgcodeService;


    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:list")
    public FebsResponse statisticalOrgcodeList(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {

        IPage<StatisticalOrgcodeExt> datas = this.statisticalOrgcodeService.selectStatisticalAndCost(request, statisticalOrgcode);
        List<StatisticalOrgcodeExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item -> {
            StatisticalOrgcodeExt ext = new StatisticalOrgcodeExt();
            BeanUtils.copyProperties(item,ext);
            ext.setOrgName(DatabaseCache.getOrgNameByOrgcode(item.getOrgCode()));
            ext.setRealReqSuccessCount(ext.getReqSuccessCount() + ext.getReceiptSuccessCount() + ext.getReceiptFailCount());
            list.add(ext);
        });


        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);

        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("businessList")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:businessList")
    public FebsResponse statisticalBusinessList(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        IPage<StatisticalOrgcodeExt> datas = this.statisticalOrgcodeService.sumStatisticalBusiness(request, statisticalOrgcode);
        List<StatisticalOrgcodeExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item -> {
            StatisticalOrgcodeExt ext = new StatisticalOrgcodeExt();
            BeanUtils.copyProperties(item,ext);
            ext.setOrgName(DatabaseCache.getOrgNameByOrgcode(item.getOrgCode()));
            ext.setRealReqSuccessCount(ext.getReqSuccessCount() + ext.getReceiptSuccessCount() + ext.getReceiptFailCount());
            list.add(ext);
        });
        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("businessOnMonthList")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:businessOnMonthList")
    public FebsResponse statisticalBusinessOnMonthList(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        IPage<StatisticalOrgcodeExt> datas = this.statisticalOrgcodeService.sumStatisticalBusinessOnMonth(request, statisticalOrgcode);
        List<StatisticalOrgcodeExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item -> {
            StatisticalOrgcodeExt ext = new StatisticalOrgcodeExt();
            BeanUtils.copyProperties(item,ext);
            ext.setOrgName(DatabaseCache.getOrgNameByOrgcode(item.getOrgCode()));
            ext.setRealReqSuccessCount(ext.getReqSuccessCount() + ext.getReceiptSuccessCount() + ext.getReceiptFailCount());
            list.add(ext);
        });
        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("totalList")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:totalList")
    public FebsResponse statisticalOrgcodeTotalList(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        IPage<StatisticalOrgcode> datas = this.statisticalOrgcodeService.sumStatisticalOrgcodesByOrg(request, statisticalOrgcode);
        List<StatisticalOrgcodeExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item -> {
            StatisticalOrgcodeExt ext = new StatisticalOrgcodeExt();
            BeanUtils.copyProperties(item,ext);
            ext.setOrgName(DatabaseCache.getOrgNameByOrgcode(item.getOrgCode()));
            ext.setRealReqSuccessCount(ext.getReqSuccessCount() + ext.getReceiptSuccessCount() + ext.getReceiptFailCount());
            list.add(ext);
        });
        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "导出商户发送量报表", exceptionMessage = "导出商户发送量报表失败")
    @GetMapping("exportOrgcodeTotalList")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:exportOrgcodeTotalList")
    public void exportOrgcodeTotalList( StatisticalOrgcodeQuery statisticalOrgcode, HttpServletResponse response) {
        List<StatisticalOrgcode> list = this.statisticalOrgcodeService.sumStatisticalOrgcodesByOrg(statisticalOrgcode);
        List<StatisticalOrgTotalExcel> lst = Lists.newArrayList();
        list.stream().forEach(item -> {
            StatisticalOrgTotalExcel ext = new StatisticalOrgTotalExcel();
            BeanUtils.copyProperties(item,ext);
            ext.setRealReqSuccessCount(item.getReqSuccessCount() + item.getReceiptSuccessCount() + item.getReceiptFailCount());
            lst.add(ext);
        });
        ExcelKit.$Export(StatisticalOrgTotalExcel.class, response).downXlsx(lst, false);
    }


    @GetMapping("sumStatisticList")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:list")
    public FebsResponse sumStatisticalOrgcodeList(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {

        IPage<StatisticalOrgcode> datas = this.statisticalOrgcodeService.sumStatisticalOrgcodes(request, statisticalOrgcode);
        List<StatisticalOrgcodeExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item -> {
            StatisticalOrgcodeExt ext = new StatisticalOrgcodeExt();
            BeanUtils.copyProperties(item,ext);
            ext.setOrgName(DatabaseCache.getOrgNameByOrgcode(item.getOrgCode()));
            ext.setRealReqSuccessCount(ext.getReqSuccessCount() + ext.getReceiptSuccessCount() + ext.getReceiptFailCount());
            list.add(ext);
        });


        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);

        return new FebsResponse().success().data(dataTable);
    }


    @ControllerEndpoint(operation = "导出StatisticalOrgcode", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:export")
    public void export(StatisticalOrgcodeQuery statisticalOrgcode, HttpServletResponse response) {
        List<StatisticalOrgcodeExt> statisticalOrgcodes = this.statisticalOrgcodeService.selectStatisticalAndCost(statisticalOrgcode);
        try {
            statisticalOrgcodes.stream().forEach(item -> {
                Long reqSuccessCount = item.getReqSuccessCount() + item.getReceiptFailCount() + item.getReceiptSuccessCount();
                item.setReqSuccessCount(reqSuccessCount);
            });
            ExcelKit.$Export(StatisticalOrgcodeExt.class, response).downXlsx(statisticalOrgcodes, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @ControllerEndpoint(operation = "导出StatisticalOrgcode", exceptionMessage = "导出Excel失败")
    @GetMapping("businessListExcel")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:businessListExcel")
    public void businessListExcel(StatisticalOrgcodeQuery statisticalOrgcode, HttpServletResponse response) {
        List<StatisticalOrgcodeExt> statisticalOrgcodes = this.statisticalOrgcodeService.sumStatisticalBusinessList(statisticalOrgcode);
        try {
            statisticalOrgcodes.stream().forEach(item -> {
                Long reqSuccessCount = item.getReqSuccessCount() + item.getReceiptFailCount() + item.getReceiptSuccessCount();
                item.setReqSuccessCount(reqSuccessCount);
            });
            ExcelKit.$Export(StatisticalOrgcodeBusinessExcel.class, response).downXlsx(statisticalOrgcodes, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @ControllerEndpoint(operation = "导出StatisticalOrgcode", exceptionMessage = "导出Excel失败")
    @GetMapping("businessOnMonthListExcel")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:businessOnMonthListExcel")
    public void businessOnMonthListExcel(StatisticalOrgcodeQuery statisticalOrgcode, HttpServletResponse response) {
        List<StatisticalOrgcodeExt> statisticalOrgcodes = this.statisticalOrgcodeService.sumStatisticalBusinessOnMonth(statisticalOrgcode);
        try {
            statisticalOrgcodes.stream().forEach(item -> {
                Long reqSuccessCount = item.getReqSuccessCount() + item.getReceiptFailCount() + item.getReceiptSuccessCount();
                item.setReqSuccessCount(reqSuccessCount);
            });
            ExcelKit.$Export(StatisticalOrgcodeBusinessOnMonthExcel.class, response).downXlsx(statisticalOrgcodes, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
