package com.hero.sms.controller.channel;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.channel.SmsChannelGroup;
import com.hero.sms.service.channel.ISmsChannelGroupService;
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
 * 通道分组表 Controller
 *
 * @author Administrator
 * @date 2020-06-20 22:38:31
 */
@Slf4j
@Validated
@Controller
@RequestMapping("smsChannelGroup")
public class SmsChannelGroupController extends BaseController {

    @Autowired
    private ISmsChannelGroupService smsChannelGroupService;


    @ControllerEndpoint(operation = "编辑商户分组", exceptionMessage = "编辑商户分组失败")
    @PostMapping("incrementSave")
    @ResponseBody
    @RequiresPermissions("smsChannel:updateGroup")
    public FebsResponse incrementSaveSmsChannelGroup(Integer smsChannelId,String groupIds) {
        try {
            this.smsChannelGroupService.incrementSaveSmsChannelGroup(smsChannelId,groupIds);
        } catch (ServiceException e) {
            new FebsResponse().success().message(e.getMessage());
        }
        return new FebsResponse().success().message("数据更新成功");
    }
}
