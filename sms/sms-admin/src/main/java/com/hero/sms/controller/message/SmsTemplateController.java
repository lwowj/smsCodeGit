package com.hero.sms.controller.message;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SmsTemplate;
import com.hero.sms.service.message.ISmsTemplateService;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
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
 * 短信模板表 Controller
 *
 * @author Administrator
 * @date 2020-03-11 20:08:30
 */
@Slf4j
@Validated
@Controller
@RequestMapping("smsTemplate")
public class SmsTemplateController extends BaseController {

    @Autowired
    private ISmsTemplateService smsTemplateService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "smsTemplate")
    public String smsTemplateIndex(){
        return FebsUtil.view("smsTemplate/smsTemplate");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("smsTemplate:list")
    public FebsResponse getAllSmsTemplates(SmsTemplate smsTemplate) {
        return new FebsResponse().success().data(smsTemplateService.findSmsTemplates(smsTemplate));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("smsTemplate:list")
    public FebsResponse smsTemplateList(QueryRequest request, SmsTemplate smsTemplate) {
        Map<String, Object> dataTable = getDataTable(this.smsTemplateService.findSmsTemplates(request, smsTemplate));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增SmsTemplate", exceptionMessage = "新增SmsTemplate失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("smsTemplate:add")
    public FebsResponse addSmsTemplate(@Valid SmsTemplate smsTemplate) {
        try {
            this.smsTemplateService.createSmsTemplate(smsTemplate);
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除SmsTemplate", exceptionMessage = "删除SmsTemplate失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("smsTemplate:delete")
    public FebsResponse deleteSmsTemplate(SmsTemplate smsTemplate) {
        this.smsTemplateService.deleteSmsTemplate(smsTemplate);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除SmsTemplate", exceptionMessage = "批量删除SmsTemplate失败")
    @GetMapping("delete/{smsTemplateIds}")
    @ResponseBody
    @RequiresPermissions("smsTemplate:delete")
    public FebsResponse deleteSmsTemplate(@NotBlank(message = "{required}") @PathVariable String smsTemplateIds) {
        String[] ids = smsTemplateIds.split(StringPool.COMMA);
        this.smsTemplateService.deleteSmsTemplates(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改SmsTemplate", exceptionMessage = "修改SmsTemplate失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("smsTemplate:update")
    public FebsResponse updateSmsTemplate(SmsTemplate smsTemplate) {
        this.smsTemplateService.updateSmsTemplate(smsTemplate);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改SmsTemplate", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("smsTemplate:export")
    public void export(QueryRequest queryRequest, SmsTemplate smsTemplate, HttpServletResponse response) {
        List<SmsTemplate> smsTemplates = this.smsTemplateService.findSmsTemplates(queryRequest, smsTemplate).getRecords();
        ExcelKit.$Export(SmsTemplate.class, response).downXlsx(smsTemplates, false);
    }
}
