package com.hero.sms.job.task;

import com.hero.sms.commands.utils.WebUtils;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.HttpUtil;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.common.Code;
import com.hero.sms.enums.channel.ChannelStateEnums;
import com.hero.sms.enums.channel.SmsChannelPushPropertyNameEnums;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.impl.channel.push.BaseSmsPushService;
import com.hero.sms.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 查询指定通道查询发送记录状态并模拟推送的任务
 * 
 * @author Lenovo
 *
 */
@Slf4j
@Component
public class GetChannelRecordTask {

    @Autowired
    private ISmsChannelService smsChannelService;
    

	public void run() 
	{
		String queryChannelSendRecordSwitch = "OFF";
		Code queryChannelSendRecordSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","queryChannelSendRecordSwitch");
	    if(queryChannelSendRecordSwitchCode!=null&&!"".equals(queryChannelSendRecordSwitchCode.getName()))
	    {
	    	queryChannelSendRecordSwitch = queryChannelSendRecordSwitchCode.getName();
	    }
	    if("ON".equals(queryChannelSendRecordSwitch))
	    {
	    	Code queryChannelCode = DatabaseCache.getCodeBySortCodeAndCode("System","queryChannelCode");
		    if(queryChannelCode!=null&&!"".equals(queryChannelCode.getName()))
		    {
		    	String queryChannelCodeStr = queryChannelCode.getName();
		    	String[] queryChannelCodes = queryChannelCodeStr.split(",");
		    	for (int i = 0; i < queryChannelCodes.length; i++) 
		    	{
		    		String channelCode = queryChannelCodes[i];
		    		if(StringUtil.isNotBlank(channelCode))
		    		{
		    			sendNotifyForChannel(channelCode);
		    		}
				}
		    }
	    }
	    else
	    {
	    	log.info("QueryChannelSendRecordTask任务开关未开启");
	    }
	}
	
	public String sendNotifyForChannel(String channelCode)
	{
		 SmsChannelExt smsChannelExt = smsChannelService.findContainPropertyByCode(channelCode);
	     if(smsChannelExt == null) 
	     {
	     	log.info(String.format("通道【%s】查询为空",channelCode));
	     	return null;
	     }
	     if(!smsChannelExt.getState().equals(ChannelStateEnums.START.getCode())&&!smsChannelExt.getState().equals(ChannelStateEnums.Pause.getCode()))
		 {
			 log.info(String.format("通道【%s】未开启",channelCode));
			 return null;
		 }
	     BaseSmsPushService pushService = null;
	     try
	     {
	         pushService = (BaseSmsPushService) Class.forName(smsChannelExt.getImplFullClass()).newInstance();
	         pushService.init(smsChannelExt);
	     }
	     catch (Exception e) 
	     {
	            String errInfo = String.format("初始化通道【%s】失败",channelCode);
	            log.error(errInfo,e);
	            return null;
	     }
	     String resultxml = pushService.query(null);
	     if(StringUtil.isNotBlank(resultxml))
	     {
	    	String notifyUrlStr = pushService.property("notifyUrl");
	    	if(StringUtil.isNotBlank(notifyUrlStr))
	    	{
		    	String[] notifyUrls = notifyUrlStr.split(",");
		    	Long ramdom = RandomUtils.nextLong(0L,notifyUrls.length);
		    	String notifyUrl = notifyUrls[ramdom.intValue()];
		    	int connectTimeout = 1000;
		    	int readTimeout = 1000;
		    	String connectTimeoutP = pushService.property(SmsChannelPushPropertyNameEnums.ConnectTimeout.getCode());
		 		if(StringUtils.isNotBlank(connectTimeoutP)) {
		 			connectTimeout = Integer.valueOf(connectTimeoutP);
		 		}
		 		String readTimeoutP = pushService.property(SmsChannelPushPropertyNameEnums.ReadTimeout.getCode());
		 		if(StringUtils.isNotBlank(readTimeoutP)) {
		 			readTimeout = Integer.valueOf(readTimeoutP);
		 		}
		 		sendNotify(notifyUrl, resultxml, connectTimeout, readTimeout);
		 		try {
					Map<String, Object> resultMap = WebUtils.xmlToMap(resultxml, true);
					if(resultMap.size() > 0)
					{
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
									{
										int maxQueryNum = 4000;
										String maxQueryNumP = pushService.property("maxQueryNum");
										if(StringUtils.isNotBlank(maxQueryNumP))
										{
											maxQueryNum = Integer.valueOf(maxQueryNumP);
										}
										if(!(statusbox.size() < maxQueryNum))
										{
											String needSleep = pushService.property("needSleep");
											if(StringUtil.isNotBlank(needSleep)&&"Y".equals(needSleep))
											{
												int needSleepTime = 10000;
												try {
													String needSleepTimeStr = pushService.property("needSleepTime");
													if(StringUtil.isNotBlank(needSleepTimeStr))
													{
														needSleepTime = Integer.valueOf(needSleepTimeStr);
													}
												} catch (Exception e) {}
												try {
													Thread.sleep(needSleepTime);
												} catch (InterruptedException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
											sendNotifyForChannel(channelCode);
										}
									}
								}
							} catch (Exception e) {
								// TODO: handle exception
								log.error("QXT查询请求返回参数转换出错NO.3",e);
							}
						}
					}
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	     }
		return null;
	}
	
	public void sendNotify(String notifyUrl,String xmlStr,int connectTimeout,int readTimeout)
	{
		try {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(connectTimeout);// 设置超时  
			requestFactory.setReadTimeout(readTimeout);
			HttpHeaders headers = new HttpHeaders();
	    	headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			String result = HttpUtil.sendPostRequest(notifyUrl, requestFactory,headers, xmlStr);
//			log.info("模拟通知请求结果："+ result);
		} catch (Exception e) {
			log.error("模拟通知请求失败",e);
		}
	}
//	public static void main(String[] args) {
//		try {
//			String xmlStr = "<?xml version=\"1.0\" encoding=\"utf-8\" ?> \r\n" + 
//					"<returnsms>\r\n" + 
//					"<statusbox>\r\n" + 
//					"<mobile>15590478565</mobile>\r\n" + 
//					"<taskid>12109</taskid>\r\n" + 
//					"<status>10</status>\r\n" + 
//					"<receivetime>2021-10-30 02:12:11</receivetime>\r\n" + 
//					"<errorcode>DELIVRD</errorcode>\r\n" + 
//					"</statusbox>\r\n" + 
//					"<statusbox>\r\n" + 
//					"<mobile>15023239811</mobile>\r\n" + 
//					"<taskid>1212</taskid>\r\n" + 
//					"<status>20</status>\r\n" + 
//					"<receivetime>2011-12-02 22:12:11</receivetime>\r\n" + 
//					"<errorcode> MK:0011</errorcode>\r\n" + 
//					"</statusbox>\r\n" + 
//					"</returnsms>";
//			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
//			requestFactory.setConnectTimeout(10000);// 设置超时  
//			requestFactory.setReadTimeout(10000);
//			HttpHeaders headers = new HttpHeaders();
//	    	headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//			String result = HttpUtil.sendPostRequest("http://169.254.130.216:8080/return/qxt/FSGJHTTP/receipt", requestFactory,headers, xmlStr);
//			log.info("模拟通知请求结果："+ result);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}
}
