package com.hero.sms.entity.message;

import com.hero.sms.common.annotation.IsDate;
import com.hero.sms.common.annotation.IsMobile;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ApiSendSmsModel {

	@NotBlank(message = "商户编号不能为空")
	private String orgCode;
	
	@NotBlank(message = "手机号码区域不能为空")
	private String phoneArea;

	@NotBlank(message = "手机号码不能为空")
	@IsMobile(message = "包含错误的手机号码")
	@Size(max = 6000, message = "手机号码过多")
	private String phones;
	
	@NotBlank(message = "短信内容不能为空")
	@Size(max = 500,message = "短信内容不能超过500个字")
	private String message;
	
	@NotBlank(message = "随机数不能为空")
	@Size(min = 6,max = 6, message = "随机数只能6位")
	private String rand;
	
	@IsDate(pattern="yyyyMMddHHmm", message = "定时时间格式错误")
	private String timingTime;
	
	@NotBlank(message = "签名不能为空")
	private String sign;
}
