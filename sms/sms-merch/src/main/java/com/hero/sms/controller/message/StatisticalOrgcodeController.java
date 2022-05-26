package com.hero.sms.controller.message;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.StatisticalOrgcode;
import com.hero.sms.entity.message.StatisticalOrgcodeQuery;
import com.hero.sms.entity.message.exportModel.StatisticalOrgcodeExcel;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.service.message.IStatisticalOrgcodeService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

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
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode())){
            return new FebsResponse().message("账号异常，请求数据失败");
        }
        statisticalOrgcode.setOrgCode(user.getOrganizationCode());

        IPage<StatisticalOrgcode> datas = this.statisticalOrgcodeService.findStatisticalOrgcodes(request, statisticalOrgcode);
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
            map.put("consumeAmount", item.getConsumeAmount());
            map.put("statisticalDate",item.getStatisticalDate());
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "导出StatisticalOrgcode", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("statisticalOrgcode:export")
    public void export(QueryRequest queryRequest, StatisticalOrgcodeQuery statisticalOrgcode, HttpServletResponse response) {
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode())){
            log.error("userError","导出日报表时，用户账号异常！");
            return;
        }
        statisticalOrgcode.setOrgCode(user.getOrganizationCode());
        List<StatisticalOrgcode> statisticalOrgcodes = this.statisticalOrgcodeService.findStatisticalOrgcodes(queryRequest, statisticalOrgcode).getRecords();
        List<StatisticalOrgcodeExcel> lst = new ArrayList<>();
        statisticalOrgcodes.stream().forEach(item -> {
            StatisticalOrgcodeExcel excel = new StatisticalOrgcodeExcel();
            BeanUtils.copyProperties(item,excel);
            Long reqSuccessCount = item.getReqSuccessCount() + item.getReceiptFailCount() + item.getReceiptSuccessCount();
            excel.setReqSuccessCount(reqSuccessCount);
            lst.add(excel);
        });
        ExcelKit.$Export(StatisticalOrgcodeExcel.class, response).downXlsx(lst, false);
    }
}
