package com.hero.sms.service.impl.channel.push;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.hero.sms.commands.utils.WebUtils;
import com.hero.sms.common.utils.ConnectionParams;
import com.hero.sms.common.utils.HttpUtil;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FengShouQxt extends BaseSmsPushService{

	@Override
	public void init(SmsChannelExt smsChannelext) {
		super.init(smsChannelext);
	}
	
	public static void main(String[] args){
		Map<String,String> params = new HashMap<String,String>();
		params.put("method", "sendSMS");//企业ID
		params.put("username", "dageda88");//账户
		try {
			params.put("password", BASE64Encoder("dageda88"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//密码
		params.put("smstype", "1");
		params.put("mobile", "15590478565");//被叫号码
		params.put("content", urlEncode("今天好冷，还下大雨","GBK"));//发送内容 采用GBK 进行URLENCODE
		params.put("isLongSms", "0");//0-普通短信 1-加长短信
		params.put("extno", String.valueOf(1));

			String reqParam = HttpUtil.mapToParam(params);
			ConnectionParams cp = new ConnectionParams("http://39.108.24.2:9008/servlet/UserServiceAPI", reqParam);
			cp.setReadTimeout(10000);
			cp.setConnectTimeout(10000);
			cp.setCharset("UTF-8");
			String result = HttpUtil.request(cp);
		System.out.println(String.format("通道【%s】【QXT】查询状态【返回参数】:%s",23,result));
//			int checkResult= WebUtils.getCheckResult(result, "XML");
//			if(!"0".equals(String.valueOf(checkResult)))
//			{
//				log.error("QXT查询请求返回参数非协议XML格式");
//				System.exit(0);
//			}
//			else
//			{
//				try {
//					Map<String, Object> resultMap = WebUtils.xmlToMap(result, true);
//					if(resultMap.size() == 0)
//					{
//						log.error("QXT查询请求返回参数为空");
//						System.exit(0);
//					}
//					if(resultMap.get("returnsms")!=null)
//					{
//						try {
//							Map returnsmsMap = (TreeMap)resultMap.get("returnsms");
//							if(returnsmsMap.get("statusbox")!=null)
//							{
//								List statusbox = (ArrayList)returnsmsMap.get("statusbox");
//								if(statusbox!=null&&statusbox.size()>0)
//								{
//									System.out.println("result====="+result);
//								}
//							}
//							System.exit(0);
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//					System.exit(0);
//				} catch (Exception e) {
//					// TODO: handle exception
//					e.printStackTrace();
//				}
//			}
			String str = "你好呀";
			System.out.println(str);
			System.out.println(unicodeToUtf8(str));
	}
	
	/**
	 * url encode
	 * 
	 * @param str
	 *            String
	 * @param charset
	 *            String
	 * @return String
	 */
	public static String urlEncode(String str, String charset) 
	{
		try {
			return java.net.URLEncoder.encode(str, charset);
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			return "";
		}
	}
	 /**
     * encode a specified string useing BASE64Encoder
     *
     * @param str
     *            the string to be encoded
     * @return String
     * @throws Exception
     */
    public static String BASE64Encoder(String str) throws
            Exception {
        return new sun.misc.BASE64Encoder().encode(str.getBytes());

    }	
	@Override
	public boolean httpPush(SendRecord sendRecord) {
		try {
			Map<String,String> params = new HashMap<String,String>();
			params.put("method", orgNo);//企业ID
			params.put("username", account);//账户
			params.put("password", BASE64Encoder(signKey));//密码
			String smstype = property("smstype");//发送短信的类型： 值为0，1,2  O-卡发 1-网关 2-会员
			params.put("smstype", smstype);
			params.put("mobile", sendRecord.getSmsNumber());//被叫号码
			params.put("content", urlEncode(sendRecord.getSmsContent(),"GBK"));//发送内容 采用GBK 进行URLENCODE
			String isLongSms = property("isLongSms");
			params.put("isLongSms", isLongSms);//0-普通短信 1-加长短信
			params.put("extno", String.valueOf(sendRecord.getId()));
			
			String reqParam = HttpUtil.mapToParam(params);
			log.info(String.format("批次号【%s】手机号码【%s】通道【%s】【FS-QXT】发送HTTP【提交参数】:%s",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),reqParam));
			ConnectionParams cp = new ConnectionParams(reqUrl, reqParam);
			cp.setReadTimeout(readTimeout);
			cp.setConnectTimeout(connectTimeout);
			cp.setCharset(charset);
			String result = HttpUtil.request(cp);
			log.info(String.format("批次号【%s】手机号码【%s】通道【%s】【FS-QXT】发送HTTP【返回参数】:%s",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),result));
			int checkResult= WebUtils.getCheckResult(result, "XML");
			if(!"0".equals(String.valueOf(checkResult)))
			{
				log.error("QXT请求返回参数非协议XML格式");
				sendRecord.setStateDesc("通道请求失败");
				return false;
			}
			Map<String, Object> resultMap = WebUtils.xmlToMap(result, true);
			if(resultMap.size() == 0)
			{
				log.error("QXT请求返回参数非协议XML格式");
				sendRecord.setStateDesc("通道请求失败");
				return false;
			}
			if(resultMap.get("returnsms")!=null)
			{
				try {
					Map returnstatusMap = (TreeMap)resultMap.get("returnsms");
					String returnstatus = String.valueOf(returnstatusMap.get("returnstatus"));
					if(returnstatus.equals("Success")) {
						String taskID = String.valueOf(returnstatusMap.get("taskID"));
						sendRecord.setResMsgid(taskID);
						return true;
					}
					String message = String.valueOf(returnstatusMap.get("message"));
					sendRecord.setStateDesc(message);
				} catch (Exception e) {
					// TODO: handle exception
					log.error("QXT请求返回参数非协议XML格式");
					sendRecord.setStateDesc("通道请求失败");
					return false;
				}
			}
		} catch (Exception e) {
			log.error("QXT请求失败",e);
			sendRecord.setStateDesc("通道请求失败");
		}
		return false;
	}

	@Override
	public List<ReturnRecord> receipt(String resultData) 
	{
		List<ReturnRecord> returnRecords = Lists.newArrayList();
		String paramType = property("paramType");
		if(paramType.equals("JSON"))
		{
			int checkResult= WebUtils.getCheckResult(resultData, "JSON");
			if(!"0".equals(String.valueOf(checkResult)))
			{
				log.error("QXT请求返回参数非转换后的JSON格式");
				return null;
			}
			JSONObject resultJson = JSON.parseObject(resultData);
			String returnsms =  String.valueOf(resultJson.get("returnsms"));
			if(StringUtil.isNotBlank(returnsms))
			{
				try {
					JSONObject returnsmsJson = JSON.parseObject(returnsms);
					JSONArray reportArr = returnsmsJson.getJSONArray("statusbox");
					if(reportArr != null) {
						for (int i = 0 ; i < reportArr.size(); i++) {
							JSONObject report = reportArr.getJSONObject(i);
							String status = String.valueOf(report.get("status"));
							if("10".equals(status)||"20".equals(status))
							{
								ReturnRecord returnRecord = new ReturnRecord();
								if("10".equals(status)) {
									returnRecord.setReturnState(CommonStateEnums.SUCCESS.getCode());
								}else {
									returnRecord.setReturnState(CommonStateEnums.FAIL.getCode());
								}
								String smsNumber = SmsNumberAreaCodeEnums.China.getInArea()+String.valueOf(report.get("mobile"));
								returnRecord.setSmsNumber(smsNumber);
								returnRecord.setResMsgid(String.valueOf(report.get("taskid")));
								returnRecord.setReturnSeq(String.valueOf(report.get("errorcode")));
								returnRecord.setCreateTime(new Date());
								returnRecords.add(returnRecord);
							}
						}
					}
				} catch (Exception e) {
					log.error("QXT查询请求返回参数转换出错NO.1",e);
				}
			}
		}
		else
		{
			try 
			{
				int checkResult= WebUtils.getCheckResult(resultData, "XML");
				if(!"0".equals(String.valueOf(checkResult)))
				{
					log.error("QXT请求返回参数非协议XML格式");
					return null;
				}
				Map<String, Object> resultMap = WebUtils.xmlToMap(resultData, true);
				if(resultMap.size() == 0)
				{
					log.error("QXT请求返回参数非协议XML格式");
					return null;
				}
				if(resultMap.get("returnsms")!=null)
				{
					try {
						Map returnsmsMap = (TreeMap)resultMap.get("returnsms");
						
						if(returnsmsMap.get("statusbox")==null)
						{
							log.error("QXT请求返回参数statusbox为空");
							return null;
						}
						Object param = returnsmsMap.get("statusbox");
						if (param instanceof ArrayList)
						{
							List statusbox = (ArrayList)param;
							if(statusbox!=null && statusbox.size()>0)
							{
								for (int i = 0 ; i < statusbox.size(); i++)
								{
									Map report = (TreeMap)statusbox.get(i);
									String status = String.valueOf(report.get("status"));//状态报告----10：发送成功，20：发送失败
									if("10".equals(status)||"20".equals(status))
									{
										ReturnRecord returnRecord = new ReturnRecord();
										if("10".equals(status)) {
											returnRecord.setReturnState(CommonStateEnums.SUCCESS.getCode());
										}else {
											returnRecord.setReturnState(CommonStateEnums.FAIL.getCode());
										}
										String smsNumber = SmsNumberAreaCodeEnums.China.getInArea()+String.valueOf(report.get("mobile"));
										returnRecord.setSmsNumber(smsNumber);
										returnRecord.setResMsgid(String.valueOf(report.get("taskid")));
										returnRecord.setReturnSeq(String.valueOf(report.get("errorcode")));
										returnRecord.setCreateTime(new Date());
										returnRecords.add(returnRecord);
									}
								}
							}
						}
						else if(param instanceof TreeMap)
						{
							Map paramMap = (TreeMap)param;
							if(paramMap != null && paramMap.size() > 0)
							{
								String status = String.valueOf(paramMap.get("status"));//状态报告----10：发送成功，20：发送失败
								if("10".equals(status)||"20".equals(status))
								{
									ReturnRecord returnRecord = new ReturnRecord();
									if("10".equals(status)) {
										returnRecord.setReturnState(CommonStateEnums.SUCCESS.getCode());
									}else {
										returnRecord.setReturnState(CommonStateEnums.FAIL.getCode());
									}
									String smsNumber = SmsNumberAreaCodeEnums.China.getInArea()+String.valueOf(paramMap.get("mobile"));
									returnRecord.setSmsNumber(smsNumber);
									returnRecord.setResMsgid(String.valueOf(paramMap.get("taskid")));
									returnRecord.setReturnSeq(String.valueOf(paramMap.get("errorcode")));
									returnRecord.setCreateTime(new Date());
									returnRecords.add(returnRecord);
								}
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						log.error("QXT查询请求返回参数转换出错NO.2",e);
					}
				}
				
			} catch (Exception e) {
				log.error("QXT解析参数失败",e);
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
		params.put("userid", orgNo);//企业ID
		params.put("account", account);//账户
		params.put("password", signKey);//密码
		params.put("action", "query");//请求动作
		
		String reqParam = HttpUtil.mapToParam(params);
		log.info(String.format("通道【%s】【QXT】查询状态【提交参数】:%s",channelId,reqParam));
		ConnectionParams cp = new ConnectionParams(queryUrl, reqParam);
		cp.setReadTimeout(readTimeout);
		cp.setConnectTimeout(connectTimeout);
		cp.setCharset(charset);
		String result = HttpUtil.request(cp);
		log.info(String.format("通道【%s】【QXT】查询状态【返回参数】:%s",channelId,result));
		int checkResult= WebUtils.getCheckResult(result, "XML");
		if(!"0".equals(String.valueOf(checkResult)))
		{
			log.error("QXT查询请求返回参数非协议XML格式");
			return null;
		}
		else
		{
			try {
				Map<String, Object> resultMap = WebUtils.xmlToMap(result, true);
				if(resultMap.size() == 0)
				{
					log.error("QXT查询请求返回参数为空");
					return null;
				}
				if(resultMap.get("returnsms")!=null)
				{
					try {
						Map returnsmsMap = (TreeMap)resultMap.get("returnsms");
						if(returnsmsMap.get("statusbox")!=null)
						{
							Object param = returnsmsMap.get("statusbox");
							if (param instanceof ArrayList)
							{
								List statusbox = (ArrayList)param;
								if(statusbox!=null && statusbox.size()>0)
								{
									return result;
								}
							}
							else if(param instanceof TreeMap)
							{
								Map paramMap = (TreeMap)param;
								if(paramMap != null && paramMap.size() > 0)
								{
									return result;
								}
							}
						}
						return null;
					} catch (Exception e) {
						return null;
					}
				}
				return null;
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			}
		}
	}
}
