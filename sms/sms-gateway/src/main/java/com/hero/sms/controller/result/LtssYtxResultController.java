package com.hero.sms.controller.result;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.service.message.IReturnRecordService;

import lombok.extern.slf4j.Slf4j;

/**
 * LTSS回执
 * 2021-10-18
 * @author Lenovo
 *
 */
@Slf4j
@Validated
@Controller
@RequestMapping("return/ltssYtx")
public class LtssYtxResultController extends BaseController {

	@Autowired
	private IReturnRecordService returnRecordService;
	
	@RequestMapping("{code}/receipt")
	@ResponseBody 
	public void receipt(@PathVariable String code,HttpServletRequest request) {
		String data = request.getParameter("data");
		if(StringUtils.isBlank(data)) {
			return;
		}
		String requestIp = "";
		try {
			requestIp = IPUtil.getIpAddr(request);
		} catch (Exception e) {} 
		
		log.info(String.format("LTSS回执信息：请求IP【%s】【请求参数】：%s",requestIp,data));
		returnRecordService.receiptReturnRecord(code,data);
	}
}
