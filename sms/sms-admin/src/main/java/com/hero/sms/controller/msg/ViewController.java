package com.hero.sms.controller.msg;

import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.enums.msg.MessageReadStatusEnums;
import com.hero.sms.enums.msg.MessageTypeEnums;
import com.hero.sms.service.msg.IMsgService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @author Administrator
 */
@Controller("msgView")
public class ViewController extends BaseController {

    @Autowired
    private IMsgService messageService;

    //站内信列表
    @GetMapping(FebsConstant.VIEW_PREFIX + "message/list")
    @RequiresPermissions("message:list")
    public String messageList(Model model){
        getMessageType(model);
        return FebsUtil.view("msg/message");
    }

    //新增消息
    @GetMapping(FebsConstant.VIEW_PREFIX + "message/add")
    @RequiresPermissions("message:add")
    public String messageAdd(Model model){
        getMessageType(model);
        return FebsUtil.view("msg/messageAdd");
    }

    //站内信列表
    @GetMapping(FebsConstant.VIEW_PREFIX + "messageReply/list/{id}")
    @RequiresPermissions("messageReply:list")
    public String messageReplyList(@PathVariable Integer id,Model model){
        getMessageType(model);
        getMessage(model,id);
        return FebsUtil.view("msg/messageReply");
    }

    private void getMessage(Model model, Integer id){
        model.addAttribute("msg", messageService.findMessages(id,null));
    }

    private void getMessageType(Model model){
        model.addAttribute("messageType", MessageTypeEnums.values());
        model.addAttribute("readStatus", MessageReadStatusEnums.values());
    }

}
