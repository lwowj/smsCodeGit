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
        /**
         * @begin
         * 2021-04-21
         *           【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
         * 【说明】：这里需要新增校验agentSystemConfig.getSystemLogoUrl() 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
         * 【步骤】：
         * 1、使用文件工具判断文件地址的文件是否存在于本机上
         * 2、获取字典中负载服务器的IP地址
         * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
         * 【messageReplyService.findMessageReplys 方法内实现】
         * @end
         */
        dataTable.put("rows", this.messageReplyService.findMessageReplys(messageReply,getCurrentUser().getUserAccount(),false));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增MessageReply", exceptionMessage = "回复消息失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("messageReply:add")
    public FebsResponse addMessageReply(@Valid MessageReply messageReply) {
        try {
            this.messageReplyService.createMessageReply(messageReply,getCurrentUser().getUserAccount(),false);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("回复消息失败",e);
            return new FebsResponse().message("回复消息失败").fail();
        }
        return new FebsResponse().success().message("回复消息成功");
    }
}
