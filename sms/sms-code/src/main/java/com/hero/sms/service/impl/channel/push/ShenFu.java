package com.hero.sms.service.impl.channel.push;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.hero.sms.commands.utils.WebUtils;
import com.hero.sms.common.utils.ConnectionParams;
import com.hero.sms.common.utils.HttpUtil;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShenFu extends BaseSmsPushService{

	@Override
	public void init(SmsChannelExt smsChannelext) {
		super.init(smsChannelext);
	}
	
	public static void main(String[] args){
//		Api key:5e13532743523586
//		secret key:fd42daa6
//		端口：6240
//		IP:185.106.240.218
			Map<String,String> params = new HashMap<String,String>();
			params.put("apikey", "5e13532743523586");//账户
			params.put("secretkey", "fd42daa6");//密码
			String callerID = "123456";
			params.put("callerID", callerID);
			params.put("toUser", "15590478565");//被叫号码
			params.put("messageContent", unicodeToUtf8("今天天气是阴天"));//内容

			String reqParam = HttpUtil.mapToParam(params);
			ConnectionParams cp = new ConnectionParams("http://185.106.240.218:6240/sendtext", reqParam);
			cp.setReadTimeout(10000);
			cp.setConnectTimeout(10000);
			cp.setCharset("UTF-8");
			String result = HttpUtil.request(cp);
		System.out.println(String.format("通道【%s】【ShenFu】查询状态【返回参数】:%s",23,result));
		int checkResult= WebUtils.getCheckResult(result, "JSON");
		if(!"0".equals(String.valueOf(checkResult)))
		{
			log.error("ShenFu请求返回参数非协议JSON格式");
			System.exit(0);
		}
		JSONObject resultJson = JSON.parseObject(result);
		String Status = String.valueOf(resultJson.get("Status"));
		System.out.println(resultJson.toString());
		if("0".equals(Status))
		{
			String Message_ID = String.valueOf(resultJson.get("Message_ID"));
			System.out.println(Message_ID);
			System.exit(0);
		}
	}
	
	@Override
	public boolean httpPush(SendRecord sendRecord) {
		try {
			Map<String,String> params = new HashMap<String,String>();
//			params.put("apikey", orgNo);//企业ID
			params.put("apikey", account);//账户
			params.put("secretkey", signKey);//密码
			String callerID = property("callerID");
			if(StringUtil.isBlank(callerID))
			{
				callerID =  String.valueOf(sendRecord.getId());
			}
			params.put("callerID", callerID);
			params.put("toUser", sendRecord.getSmsNumber());//被叫号码
			params.put("messageContent", unicodeToUtf8(sendRecord.getSmsContent()));//内容
			
			
			String reqParam = HttpUtil.mapToParam(params);
			log.info(String.format("批次号【%s】手机号码【%s】通道【%s】【QXT】发送HTTP【提交参数】:%s",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),reqParam));
			ConnectionParams cp = new ConnectionParams(reqUrl, reqParam);
			cp.setReadTimeout(readTimeout);
			cp.setConnectTimeout(connectTimeout);
			cp.setCharset(charset);
			cp.setRequestMethod(requestMethod);
			String result = HttpUtil.request(cp);
			log.info(String.format("批次号【%s】手机号码【%s】通道【%s】【QXT】发送HTTP【返回参数】:%s",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),result));
			int checkResult= WebUtils.getCheckResult(result, "JSON");
			if(!"0".equals(String.valueOf(checkResult)))
			{
				log.error("ShenFu请求返回参数非协议JSON格式");
				sendRecord.setStateDesc("通道请求失败");
				return false;
			}
			JSONObject resultJson = JSON.parseObject(result);
			String Status = String.valueOf(resultJson.get("Status"));
			if("0".equals(Status))
			{
				String Message_ID = String.valueOf(resultJson.get("Message_ID"));
				sendRecord.setResMsgid(Message_ID);
				return true;
			}
		} catch (Exception e) {
			log.error("ShenFu请求失败",e);
			sendRecord.setStateDesc("通道请求失败");
		}
		return false;
	}

	@Override
	public List<ReturnRecord> receipt(String resultData) 
	{
		List<ReturnRecord> returnRecords = Lists.newArrayList();
		if(StringUtil.isNotBlank(resultData))
		{
			JSONObject resultJson = JSON.parseObject(resultData);
			String apikey =  String.valueOf(resultJson.get("apikey"));
			String secretkey =  String.valueOf(resultJson.get("secretkey"));
			
			if(account.equals(apikey)&&signKey.equals(secretkey))
			{
				try {
					String status =  String.valueOf(resultJson.get("status"));
					String messageid =  String.valueOf(resultJson.get("messageid"));
//					String text =  String.valueOf(resultJson.get("text"));
					if("1".equals(status)||"0".equals(status))
					{
						ReturnRecord returnRecord = new ReturnRecord();
						if("0".equals(status)) {
							returnRecord.setReturnState(CommonStateEnums.SUCCESS.getCode());
						}else {
							returnRecord.setReturnState(CommonStateEnums.FAIL.getCode());
						}
						returnRecord.setResMsgid(messageid);
						returnRecord.setCreateTime(new Date());
						returnRecord.setChannelId(channelId);
						returnRecords.add(returnRecord);
					}
				} catch (Exception e) {
					log.error("ShenFu查询请求返回参数转换出错NO.1",e);
				}
			}
		}
		return returnRecords;
	}

	public static String unicodeToUtf8 (String str) 
	{
		try 
		{
			//利用getBytes将unicode字符串转成UTF-8格式的字节数组
			byte[] utf8Bytes = str.getBytes("UTF-8"); 
			//然后用utf-8 对这个字节数组解码成新的字符串
			str = new String(utf8Bytes, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return str;
	}
	
	@Override
	public String query(SendRecord sendRecord)
	{
		Map<String,String> params = new HashMap<String,String>();
		params.put("apikey", account);//账户
		params.put("secretkey", signKey);//密码
		params.put("messageid", sendRecord.getResMsgid());
		String reqParam = HttpUtil.mapToParam(params);
		log.info(String.format("通道【%s】【ShenFu】查询状态【提交参数】:%s",channelId,reqParam));
		ConnectionParams cp = new ConnectionParams(queryUrl, reqParam);
		cp.setReadTimeout(readTimeout);
		cp.setConnectTimeout(connectTimeout);
		cp.setCharset(charset);
		String result = HttpUtil.request(cp);
		log.info(String.format("通道【%s】【ShenFu】查询状态【返回参数】:%s",channelId,result));
		int checkResult= WebUtils.getCheckResult(result, "JSON");
		if(!"0".equals(String.valueOf(checkResult)))
		{
			log.error("ShenFu查询请求返回参数非协议JSON格式");
			return null;
		}
		try 
		{
			JSONObject resultJson = JSON.parseObject(result);
			String Status = String.valueOf(resultJson.get("Status"));
			if("0".equals(Status)||"1".equals(Status))
			{
				return result;
			}
			else
			{
				return null;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
	
	}
}
