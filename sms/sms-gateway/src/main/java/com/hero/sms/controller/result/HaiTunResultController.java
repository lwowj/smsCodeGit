package com.hero.sms.controller.result;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hero.sms.common.controller.BaseController;
import com.hero.sms.service.message.IReturnRecordService;

import lombok.extern.slf4j.Slf4j;

/**
 * 海豚回执
 * @author Lenovo
 *
 */
@Slf4j
@Validated
@Controller
@RequestMapping("return/haiTun")
public class HaiTunResultController extends BaseController {

	@Autowired
	private IReturnRecordService returnRecordService;
	
	@RequestMapping("{code}/receipt")
	@ResponseBody
	public void receipt(@PathVariable String code, @RequestBody String body) {
		if(StringUtils.isBlank(body)) {
			return;
		}
		log.info("海豚回执信息："+body);
		returnRecordService.receiptReturnRecord(code,body);
	}
}
