package com.hero.sms.controller.result;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.service.message.IReturnRecordService;

import lombok.extern.slf4j.Slf4j;

/**
 * 神符回执
 * 2022-05-13
 * @author Lenovo
 *
 */
@Slf4j
@Validated
@Controller
@RequestMapping("return/SF")
public class ShenFuResultController extends BaseController {

	@Autowired
	private IReturnRecordService returnRecordService;
	
	@RequestMapping("{code}/receipt")
	@ResponseBody 
	public String receipt(@PathVariable String code,HttpServletRequest request) 
	{
		JSONObject parameterStr = JSONObject.parseObject("{}");
		try {
			 Enumeration<String> e = request.getParameterNames();
	            while(e.hasMoreElements())
	            {
	                String key = e.nextElement();
	                if(StringUtils.isNotEmpty(request.getParameter(key))) 
	                {
	                    parameterStr.put(key, request.getParameter(key));
	                }
	            }
		} catch (Exception e) {} 
		String requestIp = "";
		try {
			requestIp = IPUtil.getIpAddr(request);
		} catch (Exception e) {} 
		
		log.info(String.format("SF回执信息：请求IP【%s】【请求参数】：%s",requestIp,parameterStr.toString()));
		returnRecordService.receiptReturnRecord(code,parameterStr.toString());
		try {
			String status =  String.valueOf(parameterStr.get("status"));
			String messageid =  String.valueOf(parameterStr.get("messageid"));
			String text =  String.valueOf(parameterStr.get("text"));
			JSONObject returnJson = JSONObject.parseObject("{}");
//			JSONObject submitJson = JSONObject.parseObject("{}");
//			submitJson.put("format", "json");
//			submitJson.put("status", status);
//			submitJson.put("successStatus", text);
//			submitJson.put("deliveryStatus", "");
//			submitJson.put("messageId", messageid);
//			submitJson.put("fieldSeparator", "");
//			submitJson.put("pairSeparator", "");
			JSONObject deliverJson = JSONObject.parseObject("{}");
			deliverJson.put("format", "");
			deliverJson.put("status", status);
			if("0".equals(status))
			{
				deliverJson.put("successStatus", text);
				deliverJson.put("failedStatus", "");
			}
			else
			{
				deliverJson.put("successStatus", "");
				deliverJson.put("failedStatus", text);
			}
			deliverJson.put("successStatus", text);
			deliverJson.put("failedStatus", "");
			deliverJson.put("messageId", messageid);
			deliverJson.put("fieldSeparator", "");
//			returnJson.put("submit", submitJson.toString());
			returnJson.put("deliver", deliverJson.toString());
			log.info(String.format("SF回执反馈信息：%s",returnJson.toString()));
			return returnJson.toString();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "";
	}
}
