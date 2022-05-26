package com.hero.sms.controller.msg;

import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.msg.Message;
import com.hero.sms.enums.msg.MessageTypeEnums;
import com.hero.sms.service.msg.IMsgService;
import com.hero.sms.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 站内信 Controller
 *
 * @author Administrator
 * @date 2020-08-16 17:26:03
 */
@Slf4j
@Validated
@Controller
@RequestMapping("message")
public class MessageController extends BaseController {

    @Autowired
    private IMsgService messageService;

    @ResponseBody
    @PostMapping("queryNewMsg")
    @RequiresPermissions("message:list")
    public FebsResponse queryNewMsg() {
        return new FebsResponse().success().data(messageService.queryNewMsg(getCurrentUser().getUserAccount()));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("message:list")
    public FebsResponse messageList(QueryRequest request, Message message) {
        message.setCreateUser(getCurrentUser().getUserAccount());
        /**
         * @begin
         * 2021-04-21
         * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
         * 【说明】：这里需要新增校验agentSystemConfig.getSystemLogoUrl() 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
         * 【步骤】：
         * 1、使用文件工具判断文件地址的文件是否存在于本机上
         * 2、获取字典中负载服务器的IP地址
         * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
         */
        
        /**
         * @end
         */
        Map<String, Object> dataTable = getDataTable(this.messageService.findMessages(request, message,false));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增Message", exceptionMessage = "新增消息失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("message:add")
    public FebsResponse addMessage(@Valid Message message) {
        try {
            if(StringUtils.isEmpty(message.getMessageType()) || StringUtils.isEmpty(message.getMessageContact()))
            {
            	return new FebsResponse().message("信息不完整");
            }
            try {
            	int messageType  = Integer.valueOf(message.getMessageType());
            	String name = MessageTypeEnums.getNameByCode(messageType);
            	if(StringUtil.isBlank(name))
            	{
            		return new FebsResponse().message("信息输入不正确");
            	}
			} catch (Exception e) {
				return new FebsResponse().message("信息输入不正确");
			}
            this.messageService.createMessage(message,getCurrentUser());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("消息提交失败",e);
            return new FebsResponse().message("消息提交失败").fail();
        }
        return new FebsResponse().success().message("消息提交成功");
    }

}
