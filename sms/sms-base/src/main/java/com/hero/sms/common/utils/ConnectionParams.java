package com.hero.sms.common.utils;


import java.util.Map;
import java.util.Properties;

/**
 * http连接参数
 * 
 * @author Lenovo
 * 
 */
public class ConnectionParams {
	private String charset;
	private String requestMethod;
	private int readTimeout;
	private int connectTimeout;
	private Properties requestProperty;
	private String requestUrl;
	private String content;
	private Map<String, String> requestParam;

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public Properties getRequestProperty() {
		return requestProperty;
	}

	public void setRequestProperty(Properties requestProperty) {
		this.requestProperty = requestProperty;
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	

	public Map<String, String> getRequestParam() {
		return requestParam;
	}

	public void setRequestParam(Map<String, String> requestParam) {
		this.requestParam = requestParam;
	}

	public ConnectionParams(String url,String content) {
		super();
		charset = "UTF-8";// 默认
		requestMethod = "POST";// 默认
		readTimeout = 5000;// 默认
		connectTimeout = 5000;// 默认
		requestProperty = new Properties();
		requestProperty
				.put("Content-Type", "application/x-www-form-urlencoded");
		this.requestUrl=url;
		this.content=content;
	}
}
