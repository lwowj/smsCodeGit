package com.hero.sms.controller.channel;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.service.channel.IPayChannelService;
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
 * 支付通道 Controller
 *
 * @author Administrator
 * @date 2020-03-12 11:02:02
 */
@Slf4j
@Validated
@Controller
@RequestMapping("payChannel")
public class PayChannelController extends BaseController {

    @Autowired
    private IPayChannelService payChannelService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "payChannel")
    public String payChannelIndex(){
        return FebsUtil.view("payChannel/payChannel");
    }

    @ControllerEndpoint(operation = "支付通道列表")
    @GetMapping
    @ResponseBody
    @RequiresPermissions("payChannel:list")
    public FebsResponse getAllPayChannels(PayChannel payChannel) {
        return new FebsResponse().success().data(payChannelService.findPayChannels(payChannel));
    }

    @ControllerEndpoint(operation = "支付通道列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("payChannel:list")
    public FebsResponse payChannelList(QueryRequest request, PayChannel payChannel) {
        Map<String, Object> dataTable = getDataTable(this.payChannelService.findPayChannels(request, payChannel));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "支付通道新增")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("payChannel:add")
    public FebsResponse addPayChannel(@Valid PayChannel payChannel) {
        try {
            this.payChannelService.createPayChannel(payChannel);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("支付通道新增失败",e);
            return new FebsResponse().message("支付通道新增失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "支付通道删除")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("payChannel:delete")
    public FebsResponse deletePayChannel(PayChannel payChannel) {
        this.payChannelService.deletePayChannel(payChannel);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "支付通道删除")
    @GetMapping("delete/{payChannelIds}")
    @ResponseBody
    @RequiresPermissions("payChannel:delete")
    public FebsResponse deletePayChannel(@NotBlank(message = "{required}") @PathVariable String payChannelIds) {
        String[] ids = payChannelIds.split(StringPool.COMMA);
        this.payChannelService.deletePayChannels(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "支付通道更新")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("payChannel:update")
    public FebsResponse updatePayChannel(PayChannel payChannel) {
        try {
            this.payChannelService.updatePayChannel(payChannel);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("支付通道更新失败",e);
            return new FebsResponse().message("支付通道更新失败").fail();
        }
        return new FebsResponse().success();
    }

    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("payChannel:export")
    public void export(QueryRequest queryRequest, PayChannel payChannel, HttpServletResponse response) {
        List<PayChannel> payChannels = this.payChannelService.findPayChannels(queryRequest, payChannel).getRecords();
        ExcelKit.$Export(PayChannel.class, response).downXlsx(payChannels, false);
    }
}
