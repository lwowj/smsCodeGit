package com.hero.sms.controller.channel;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.channel.SmsChannelProperty;
import com.hero.sms.service.channel.ISmsChannelPropertyService;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 短信通道属性 Controller
 *
 * @author Administrator
 * @date 2020-03-10 15:27:18
 */
@Slf4j
@Validated
@Controller
@RequestMapping("smsChannelProperty")
public class SmsChannelPropertyController extends BaseController {

    @Autowired
    private ISmsChannelPropertyService smsChannelPropertyService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "smsChannelProperty")
    public String smsChannelPropertyIndex(){
        return FebsUtil.view("smsChannelProperty/smsChannelProperty");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("smsChannelProperty:list")
    public FebsResponse getAllSmsChannelPropertys(SmsChannelProperty smsChannelProperty) {
        return new FebsResponse().success().data(smsChannelPropertyService.findSmsChannelPropertys(smsChannelProperty));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("smsChannelProperty:list")
    public FebsResponse smsChannelPropertyList(QueryRequest request, SmsChannelProperty smsChannelProperty) {
        Map<String, Object> dataTable = getDataTable(this.smsChannelPropertyService.findSmsChannelPropertys(request, smsChannelProperty));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增SmsChannelProperty", exceptionMessage = "新增SmsChannelProperty失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("smsChannelProperty:add")
    public FebsResponse addSmsChannelProperty(@Valid SmsChannelProperty smsChannelProperty) {
        this.smsChannelPropertyService.createSmsChannelProperty(smsChannelProperty);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除SmsChannelProperty", exceptionMessage = "删除SmsChannelProperty失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("smsChannelProperty:delete")
    public FebsResponse deleteSmsChannelProperty(SmsChannelProperty smsChannelProperty) {
        this.smsChannelPropertyService.deleteSmsChannelProperty(smsChannelProperty);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除SmsChannelProperty", exceptionMessage = "批量删除SmsChannelProperty失败")
    @GetMapping("delete/{smsChannelPropertyIds}")
    @ResponseBody
    @RequiresPermissions("smsChannelProperty:delete")
    public FebsResponse deleteSmsChannelProperty(@NotBlank(message = "{required}") @PathVariable String smsChannelPropertyIds) {
        String[] ids = smsChannelPropertyIds.split(StringPool.COMMA);
        this.smsChannelPropertyService.deleteSmsChannelPropertys(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改SmsChannelProperty", exceptionMessage = "修改SmsChannelProperty失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("smsChannelProperty:update")
    public FebsResponse updateSmsChannelProperty(SmsChannelProperty smsChannelProperty) {
        this.smsChannelPropertyService.updateSmsChannelProperty(smsChannelProperty);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改SmsChannelProperty", exceptionMessage = "修改SmsChannelProperty失败")
    @PostMapping("updates")
    @ResponseBody
    @RequiresPermissions("smsChannelProperty:update")
    public FebsResponse updateSmsChannelProperties(@RequestBody List<SmsChannelProperty> smsChannelProperties) {
        User user = super.getCurrentUser();
        this.smsChannelPropertyService.updateSmsChannelProperties(smsChannelProperties,user.getUsername());
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改SmsChannelProperty", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("smsChannelProperty:export")
    public void export(QueryRequest queryRequest, SmsChannelProperty smsChannelProperty, HttpServletResponse response) {
        List<SmsChannelProperty> smsChannelPropertys = this.smsChannelPropertyService.findSmsChannelPropertys(queryRequest, smsChannelProperty).getRecords();
        ExcelKit.$Export(SmsChannelProperty.class, response).downXlsx(smsChannelPropertys, false);
    }
}
