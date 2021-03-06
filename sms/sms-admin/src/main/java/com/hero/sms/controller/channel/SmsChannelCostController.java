package com.hero.sms.controller.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

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
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.channel.SmsChannelCost;
import com.hero.sms.entity.channel.SmsChannelCostExt;
import com.hero.sms.service.channel.ISmsChannelCostService;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * ้้่ต่ดน Controller
 *
 * @author Administrator
 * @date 2020-03-10 15:27:23
 */
@Slf4j
@Validated
@Controller
@RequestMapping("smsChannelCost")
public class SmsChannelCostController extends BaseController {

    @Autowired
    private ISmsChannelCostService smsChannelCostService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "smsChannelCost")
    public String smsChannelCostIndex(){
        return FebsUtil.view("smsChannelCost/smsChannelCost");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("smsChannelCost:list")
    public FebsResponse getAllSmsChannelCosts(SmsChannelCost smsChannelCost) {
        return new FebsResponse().success().data(smsChannelCostService.findSmsChannelCosts(smsChannelCost));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("smsChannelCost:list")
    public FebsResponse smsChannelCostList(QueryRequest request, SmsChannelCost smsChannelCost) {
        IPage<SmsChannelCost> datas = this.smsChannelCostService.findSmsChannelCosts(request, smsChannelCost);
        List<SmsChannelCostExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item ->{
            SmsChannelCostExt ext = new SmsChannelCostExt();
            BeanUtils.copyProperties(item,ext);
            ext.setSmsChannelName(DatabaseCache.getSmsChannelNameById(item.getSmsChannelId()));
            list.add(ext);
        });

        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "ๆฐๅขSmsChannelCost", exceptionMessage = "ๆฐๅขSmsChannelCostๅคฑ่ดฅ")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("smsChannelCost:add")
    public FebsResponse addSmsChannelCost(@Valid SmsChannelCost smsChannelCost) {
        User user = super.getCurrentUser();
        smsChannelCost.setRemark(user.getUsername() + "ๆทปๅ?");
        try {
            this.smsChannelCostService.createSmsChannelCost(smsChannelCost);
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "ๅ?้คSmsChannelCost", exceptionMessage = "ๅ?้คSmsChannelCostๅคฑ่ดฅ")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("smsChannelCost:delete")
    public FebsResponse deleteSmsChannelCost(SmsChannelCost smsChannelCost) {
        this.smsChannelCostService.deleteSmsChannelCost(smsChannelCost);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "ๆน้ๅ?้คSmsChannelCost", exceptionMessage = "ๆน้ๅ?้คSmsChannelCostๅคฑ่ดฅ")
    @GetMapping("delete/{smsChannelCostIds}")
    @ResponseBody
    @RequiresPermissions("smsChannelCost:delete")
    public FebsResponse deleteSmsChannelCost(@NotBlank(message = "{required}") @PathVariable String smsChannelCostIds) {
        String[] ids = smsChannelCostIds.split(StringPool.COMMA);
        this.smsChannelCostService.deleteSmsChannelCosts(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "ไฟฎๆนSmsChannelCost", exceptionMessage = "ไฟฎๆนSmsChannelCostๅคฑ่ดฅ")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("smsChannelCost:update")
    public FebsResponse updateSmsChannelCost(SmsChannelCost smsChannelCost) {
        try {
        	this.smsChannelCostService.updateSmsChannelCost(smsChannelCost);
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "ไฟฎๆนSmsChannelCost", exceptionMessage = "ๅฏผๅบExcelๅคฑ่ดฅ")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("smsChannelCost:export")
    public void export(QueryRequest queryRequest, SmsChannelCost smsChannelCost, HttpServletResponse response) {
        List<SmsChannelCost> smsChannelCosts = this.smsChannelCostService.findSmsChannelCosts(queryRequest, smsChannelCost).getRecords();
        ExcelKit.$Export(SmsChannelCost.class, response).downXlsx(smsChannelCosts, false);
    }
}
