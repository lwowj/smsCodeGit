package com.hero.sms.controller;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.entity.message.ApiSendSmsModel;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.service.message.ISendBoxService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Validated
@Controller
@RequestMapping("api")
public class APIController {

	@Autowired
	private ISendBoxService sendBoxService;

	@Autowired
	private IBusinessManage businessManage;

	
	/**
	    * showdoc
	    * @catalog 
	    * @title 短信发送接口
	    * @description 短信发送接口
	    * @method post
	    * @url http://域名/api/sendSMS
	    * @param orgCode 必选 string 商户编号 
	    * @param phoneArea 必选 string 号码区域;固定值%2b86
	    * @param phones 必选 string 手机号码集合，用逗号分隔开;最多500个手机号码
	    * @param message 必选 string 短信内容
	    * @param rand 必选 string 6位随机数
	    * @param timingTime 可选 string 定时时间
	    * @param sign 必选 string 签名值;示例：MD5(orgCode%2bcontent%2brand%2bsignKey)
	    * @return {"code":200,"message":"提交成功"}
	    * @return_param code int 状态值;200代表成功;其他值都为不成功
	    * @return_param message string 状态描述
	    * @remark 接口返回值只代表是否提交平台成功，短信是否发送成功请登录商户后台查看
	    * @number 1
	    */
	@RequestMapping(value = "sendMessage",method ={RequestMethod.POST})
	@ResponseBody
	public String sendMessage(HttpServletRequest request,@Valid ApiSendSmsModel model, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			String errMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
			return JSON.toJSONString(new FebsResponse().fail().message(errMsg));
		}
		String mobile = model.getPhones();
		mobile = StringUtils.removeEnd(StringUtils.removeStart(mobile, ","),",");
		int size = mobile.length() - mobile.replaceAll(",", "").length()+1;
		if(size > 500) {
			return JSON.toJSONString(new FebsResponse().fail().message("手机号码最多500个"));
		}
		String clientIp = IPUtil.getIpAddr(request);
		log.info(String.format("api请求：%s",JSON.toJSONString(model)));
		FebsResponse result = sendBoxService.apiSendSMS(model,clientIp);
		return JSON.toJSONString(result);
	}

	@GetMapping(value = "loadConfigCache")
	@ResponseBody
	public String loadConfigCache(HttpServletRequest request,@RequestParam("data") String data) {
		if(StringUtils.isBlank(data)) {
			return JSON.toJSONString(new FebsResponse().fail().message("参数缺失！"));
		}
		log.debug("商户缓存加载请求数据：" + data);
		FebsResponse result =  businessManage.loadConfigCache(data, IPUtil.getIpAddr(request));
		return JSON.toJSONString(result);
	}
	
	@PostMapping(value = "constrolSmsGate")
	@ResponseBody
	public String constrolSmsGate(HttpServletRequest request,@RequestParam("data") String data,@RequestParam("sign") String sign) {
		if(StringUtils.isBlank(data) || StringUtils.isBlank(sign)) {
			return JSON.toJSONString(new FebsResponse().fail().message("参数缺失！"));
		}
		log.debug(String.format("网关客户端控制请求数据【%s】,签名【%s】",data,sign));
		FebsResponse result =  businessManage.constrolSmsGate(data,sign, IPUtil.getIpAddr(request));
		return JSON.toJSONString(result);
	}

	@PostMapping(value = "getSmsGateConnectInfo")
	@ResponseBody
	public String getSmsGateConnectInfo(HttpServletRequest request,@RequestParam("data") String data,@RequestParam("sign") String sign) {
		if(StringUtils.isBlank(data) || StringUtils.isBlank(sign)) {
			return JSON.toJSONString(new FebsResponse().fail().message("参数缺失！"));
		}
		log.debug(String.format("网关客户端控制请求数据【%s】,签名【%s】",data,sign));
		FebsResponse result =  businessManage.getSmsGateConnectInfo(data,sign, IPUtil.getIpAddr(request));
		return JSON.toJSONString(result);
	}

	@PostMapping(value = "constrolSmsGateServer")
	@ResponseBody
	public String constrolSmsGateServer(HttpServletRequest request,@RequestParam("data") String data,@RequestParam("sign") String sign) {
		if(StringUtils.isBlank(data) || StringUtils.isBlank(sign)) {
			return JSON.toJSONString(new FebsResponse().fail().message("参数缺失！"));
		}
		log.debug(String.format("网关服务端控制请求数据【%s】,签名【%s】",data,sign));
		FebsResponse result =  businessManage.constrolSmsGateServer(data,sign, IPUtil.getIpAddr(request));
		return JSON.toJSONString(result);
	}

	@PostMapping(value = "switchSmsGate")
	@ResponseBody
	public String switchSmsGate(HttpServletRequest request,@RequestParam("data") String data,@RequestParam("sign") String sign) {
		if(StringUtils.isBlank(data) || StringUtils.isBlank(sign)) {
			return JSON.toJSONString(new FebsResponse().fail().message("参数缺失！"));
		}
		log.debug(String.format("网关服务端控制请求数据【%s】,签名【%s】",data,sign));
		FebsResponse result =  businessManage.switchSmsGateReciver(data,sign, IPUtil.getIpAddr(request));
		return JSON.toJSONString(result);
	}
	
	
	/**
	    * showdoc
	    * @catalog 
	    * @title 推送状态
	    * @description 推送状态
	    * @method post
	    * @url 商户提供地址
	    * @param orgCode 必选 string 商户编号 
	    * @param sendCode 必选 string 批次号
	    * @param mobileArea 必选 string 号码区域
	    * @param mobile 必选 string 手机号码
	    * @param state 必选 string 状态。1：分拣失败；16:接收成功；32：接收失败
	    * @param sign 必选 string 签名值;示例：MD5(orgCode%2bsendCode%2bstate%2bmobile%2bsignKey)
	    * @return {"msg":"接收成功","mobileArea":"%2b86","orgCode":"20200326161824CLUB","sendCode":"20200326234108NKSI1","mobile":"13086794012","sign":"A62EE8F41C0FFDFB391FBB0073C21894","state":"16"}
	    * @remark 
	    * @number 1
	    */
	@RequestMapping(value = "testNotify")
	@ResponseBody
	public String testNotify(HttpServletRequest request) {
		JSONObject resJsonObj = new JSONObject();
		Enumeration<String> e = request.getParameterNames();
		while(e.hasMoreElements()){
			String key = e.nextElement();
			String value = request.getParameter(key);
				resJsonObj.put(key , value);
		}
		String jsonString = resJsonObj.toJSONString();
		System.out.println(jsonString);
		return jsonString;
	}

}
