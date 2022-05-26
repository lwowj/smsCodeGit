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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.msg.Message;
import com.hero.sms.enums.msg.MessageReadStatusEnums;
import com.hero.sms.service.msg.IMsgService;

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
        return new FebsResponse().success().data(messageService.queryNewMsg(null));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("message:list")
    public FebsResponse messageList(QueryRequest request, Message message) {
    	/**
    	 * @begin
    	 * 2020-11-05
    	 * 添加图片显示IP
    	 */
    	Code merch_img_ip_code =  DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","merch_img_ip");
    	String merch_img_ip = "";
    	if(merch_img_ip_code!=null&&merch_img_ip_code.getName()!=null&&!"".equals(merch_img_ip_code.getName()))
    	{
    		merch_img_ip = merch_img_ip_code.getName();
    	}
    	IPage<Message> messageIPage = this.messageService.findMessages(request, message,true);
    	if(merch_img_ip!=null&&!"".equals(merch_img_ip))
    	{
    		if(messageIPage!=null&&messageIPage.getRecords().size()>0)
        	{
        		for (int i = 0; i < messageIPage.getRecords().size(); i++) 
        		{
        			if(messageIPage.getRecords().get(i).getPictureUrl()!=null&&!"".equals(messageIPage.getRecords().get(i).getPictureUrl()))
        			{
        				String pictureUrl = merch_img_ip + messageIPage.getRecords().get(i).getPictureUrl();
            			messageIPage.getRecords().get(i).setPictureUrl(pictureUrl);
        			}
        		}
        	}
    	}
    	/**
    	 * @end 
    	 */
        Map<String, Object> dataTable = getDataTable(messageIPage);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增Message", exceptionMessage = "新增消息失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("message:add")
    public FebsResponse addMessage(@Valid Message message) {
        try {
            String createUser = message.getCreateUser();
            if (StringUtils.isBlank(createUser)){
                return new FebsResponse().fail().message("接收人不能为空");
            }
            if (createUser.contains(",")){
                String[] orgAndUser = createUser.split(",");
                message.setOrgCode(orgAndUser[0]);
                message.setCreateUser(orgAndUser[1]);
            }else {
                return new FebsResponse().fail().message("接收人参数有误");
            }
            message.setAdminReadStatus(MessageReadStatusEnums.READ.getCode());
            message.setMerchReadStatus(MessageReadStatusEnums.UNREAD.getCode());
            message.setRemark("system添加");
            this.messageService.createMessage(message);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("消息提交失败",e);
            return new FebsResponse().message("消息提交失败").fail();
        }
        return new FebsResponse().success().message("消息提交成功");
    }

}
