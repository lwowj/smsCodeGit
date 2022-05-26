package com.hero.sms.entity.message;

import java.util.Date;

import lombok.Data;

@Data
public class NotifyMsgStateModel {

	private String orgCode;
	
	private String sendCode;
	
	private Integer subType;

	private String state;
	
	private String msg;
	
	private String mobileArea;

	private String mobile;
	
	private String sourceNumber;
	
	private Date receiptTime;
}
