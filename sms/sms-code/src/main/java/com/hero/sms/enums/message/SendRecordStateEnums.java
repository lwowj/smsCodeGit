package com.hero.sms.enums.message;

/**
 * 发送记录状态
 * @author Lenovo
 *
 */
public enum SendRecordStateEnums {

	SortingFail(1,"分拣失败"),WaitReq(2,"等待提交"),ReqSuccess(4,"提交成功"),ReqFail(8,"提交失败"),ReceiptSuccess(16,"接收成功"),ReceiptFail(32,"接收失败");
	private int code;
	private String msg;
	private SendRecordStateEnums(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}

	public static String getNameByCode(Integer code) {
		if (code == null) return null;
		for (SendRecordStateEnums sendRecordStateEnums : values()) {
			if(sendRecordStateEnums.getCode() == code) {
				return sendRecordStateEnums.getMsg();
			}
		}
		return null;
	}
}
