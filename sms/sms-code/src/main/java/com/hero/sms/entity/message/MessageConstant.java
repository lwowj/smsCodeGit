package com.hero.sms.entity.message;

/**
 * 消息常量
 * @author Lenovo
 *
 */
public class MessageConstant {

	/**
	 * mq 发件箱消息主题标识
	 */
	public static String MQ_Topic_SendBox = "sendBoxTopic";
	
	/**
	 * mq 发件箱消息主题标识
	 */
	public static String MQ_TAG_SendBox = "sendBoxTag";
	
	/**
	 * mq 短信推送消息主题标识
	 */
	public static String MQ_Topic_SendRecord = "sendRecordTopic";
	
	/**
	 * mq 短信推送消息主题标识
	 */
	public static String MQ_TAG_SendRecord = "sendRecordTag_";
	
	/**
	 * mq 通知商户消息主题标识
	 */
	public static String MQ_Topic_NotifyOrg = "notifyOrgTopic";
	
	/**
	 * mq 通知商户消息主题标识
	 */
	public static String MQ_TAG_NotifyOrg = "notifyOrgTag";

	/**
	 * mq 上游回执主题标识
	 */
	public static String MQ_Topic_UpstreamReceipt = "upstreamReceiptTopic";
	
	/**
	 * mq 上游回执主题标识
	 */
	public static String MQ_TAG_UpstreamReceipt = "upstreamReceiptTag";

	/**
	 * mq消息MD5密钥
	 */
	public static String MQ_MD5_Key = "PjsdfERzdfsqwqcm";
}
