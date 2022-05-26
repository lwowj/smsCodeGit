package com.hero.sms.entity.message;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

@Data
public class MqPushHttpBody implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String notifyUrl;
	
	private String rand;
	
	private Map<String,Object> params;
	
	private String checkData;
}
