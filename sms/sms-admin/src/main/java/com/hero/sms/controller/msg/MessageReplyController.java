package com.hero.sms.controller.msg;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.msg.MessageReply;
import com.hero.sms.service.msg.IMessageReplyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 站内信回复 Controller
 *
 * @author Administrator
 * @date 2020-08-16 17:26:11
 */
@Slf4j
@Validated
@Controller
@RequestMapping("messageReply")
public class MessageReplyController extends BaseController {

    @Autowired
    private IMessageReplyService messageReplyService;

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("messageReply:list")
    public FebsResponse messageReplyList(MessageReply messageReply) {
        Map<String, Object> dataTable = new HashMap<>(4);
        dataTable.put("rows", this.messageReplyService.findMessageReplys(messageReply,getCurrentUser().getUsername(),true));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增MessageReply", exceptionMessage = "回复消息失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("messageReply:add")
    public FebsResponse addMessageReply(@Valid MessageReply messageReply) {
        try {
            this.messageReplyService.createMessageReply(messageReply,getCurrentUser().getUsername(),true);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("回复消息失败",e);
            return new FebsResponse().message("回复消息失败").fail();
        }
        return new FebsResponse().success().message("回复消息成功");
    }
}
