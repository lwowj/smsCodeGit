package com.hero.sms.common.utils;


import java.net.HttpURLConnection;

/**
 * 
 * @author Lenovo
 * 
 */
public class RequestResult {
	private HttpURLConnection connection;
	private String resStr;
	private String errStr;

	public String getResOrErr() {
		return resStr == null ? errStr : resStr;
	}

	public String getErrStr() {
		return errStr;
	}

	public void setErrStr(String errStr) {
		this.errStr = errStr;
	}

	public HttpURLConnection getConnection() {
		return connection;
	}

	public void setConnection(HttpURLConnection connection) {
		this.connection = connection;
	}

	public String getResStr() {
		return resStr;
	}

	public void setResStr(String resStr) {
		this.resStr = resStr;
	}

	public RequestResult(HttpURLConnection connection, String resStr,
			String errStr) {
		super();
		this.connection = connection;
		this.resStr = resStr;
		this.errStr = errStr;
	}

}
