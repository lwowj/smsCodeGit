package com.hero.sms.service.channel.push;

import java.util.List;

import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;

/**
 * 消息推送接口
 * @author Lenovo
 *
 */
public interface IBaseSmsPushService {

	/**
	 * http协议推送
	 * @param smsChannel
	 */
	public boolean httpPush(SendRecord sendRecord);
	
	/**
	 * smpp协议推送（同步）
	 * @param smsChannel
	 */
	public boolean smppPush(SendRecord sendRecord);
	
	/**
	 * 处理回执信息
	 * @param resultData
	 * @return
	 */
	public List<ReturnRecord> receipt(String resultData);
	
	/**
	 * 查询状态
	 * @param sendRecord
	 * @return
	 */
	public String query(SendRecord sendRecord);

	/**
	 * smpp协议推送(异步)
	 * @param smsChannel
	 */
	public boolean smppPushAsyn(SendRecord sendRecord);
}
