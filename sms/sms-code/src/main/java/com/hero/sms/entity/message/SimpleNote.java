package com.hero.sms.entity.message;

import java.io.Serializable;

import lombok.Data;

@Data
public class SimpleNote implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1863100705257486970L;

	private Long id;
	
	private Object data;
	
	private String checkData;
}
