package com.hero.sms.controller.result;

import java.io.BufferedReader;
import java.io.IOException;

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
 * 企信通回执
 * 2021-10-28
 * @author Lenovo
 *
 */
@Slf4j
@Validated
@Controller
@RequestMapping("return/qxt")
public class QxtResultController extends BaseController {

	@Autowired
	private IReturnRecordService returnRecordService;
	
	@RequestMapping("{code}/receipt")
	@ResponseBody 
	public void receipt(@PathVariable String code,HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = request.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append('\n');
			}
		} catch (IOException e) {
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		String str = sb.toString();
		if(StringUtils.isBlank(str)) {
			return;
		}
		String requestIp = "";
		try {
			requestIp = IPUtil.getIpAddr(request);
		} catch (Exception e) {} 
		
		log.info(String.format("QXT回执信息：请求IP【%s】【请求参数】：%s",requestIp,str));
		returnRecordService.receiptReturnRecord(code,str);
	}
}
