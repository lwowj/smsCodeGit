package com.hero.sms.service.impl.channel.push;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.hero.sms.common.utils.HttpUtil;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;
/**
 * LTSS云通信
 * 2021-10-18
 * @author payBoo
 *
 */
@Slf4j
public class LtssYunTongXin extends BaseSmsPushService{

	@Override
	public void init(SmsChannelExt smsChannelext) {
		super.init(smsChannelext);
	}

	@Override
	public boolean httpPush(SendRecord sendRecord) {
		try {
			Map<String,String> params = new HashMap<String,String>();
			params.put("uid", orgNo);
//			String sign = MD5Util.MD5(signKey);
			params.put("pw", signKey);
			Map<String,String> dataParams = new HashMap<String,String>();
			dataParams.put("mb", sendRecord.getSmsNumber());
			dataParams.put("ms", sendRecord.getSmsContent());
			String[] dataStrings = new String[1];
			dataStrings[0] = HttpUtil.mapToJson(dataParams);
//			params.put("data", dataStrings.toString());
			params.put("data", "["+HttpUtil.mapToJson(dataParams)+"]");
			params.put("dm", "");
			params.put("ex", sendRecord.getSendCode()+"_"+sendRecord.getId());
			
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(connectTimeout);// 设置超时  
			requestFactory.setReadTimeout(readTimeout);
			HttpHeaders headers = new HttpHeaders();
	    	headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
	    	String jsonParams = HttpUtil.mapToJson(params);
			log.info(String.format("批次号【%s】手机号码【%s】通道【%s】LTSS发送HTTP【提交参数】:%s",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),jsonParams));
			String result = HttpUtil.sendPostRequest(reqUrl, requestFactory,headers, params);
			log.info(String.format("批次号【%s】手机号码【%s】通道【%s】LTSS发送HTTP【返回参数】:%s",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),result));
			JSONObject resultJson = JSON.parseObject(result);
			String resultCode = String.valueOf(resultJson.get("status"));
			if(resultCode.equals("0")) {
				JSONArray resultJSONArray = (JSONArray)resultJson.get("ret");
				if(resultJSONArray!=null&&!resultJSONArray.isEmpty()&&resultJSONArray.size()>0&&!"".equals(resultJSONArray.get(0)))
				{
					JSONObject resultParamJson = (JSONObject)resultJSONArray.get(0);
					String mb = String.valueOf(resultParamJson.get("mb"));
					if(mb.equals(sendRecord.getSmsNumber()))
					{
						sendRecord.setResMsgid(String.valueOf(resultParamJson.get("id")));
						return true;
					}
					else
					{
						sendRecord.setStateDesc("号码参数校验不一致");
					}
				}
				else
				{
					sendRecord.setStateDesc("返回参数出错");
				}
			}
			else
			{
				sendRecord.setStateDesc("返回状态status="+resultCode);
			}
			
		} catch (Exception e) {
			log.error("海豚请求失败",e);
			sendRecord.setStateDesc("通道请求失败");
		}
		return false;
	}

	/**
	 * 接收处理HTTP回执
	 */
	@Override
	public List<ReturnRecord> receipt(String resultData) 
	{
		List<ReturnRecord> returnRecords = Lists.newArrayList();
		try {
			String[] resultDatas = resultData.split("\\|");
			if(resultDatas!=null&&resultDatas.length>0)
			{
				for (int i = 0; i < resultDatas.length; i++) 
				{
					String report = resultDatas[i];
					ReturnRecord returnRecord = new ReturnRecord();
					if(!StringUtil.isEmpty(report))
					{
						String[] reports = report.split(",");
						if(reports!=null&&reports.length==3)
						{
							String result = reports[2];
							if(result.equals("DELIVRD")) {
								returnRecord.setReturnState(CommonStateEnums.SUCCESS.getCode());
							}else {
								returnRecord.setReturnState(CommonStateEnums.FAIL.getCode());
							}
							String smsNumber = String.valueOf(reports[1]);
							String resMsgid = String.valueOf(reports[0]);
							if(!StringUtil.isEmpty(smsNumber)&&!StringUtil.isEmpty(resMsgid))
							{
								smsNumber = SmsNumberAreaCodeEnums.China.getInArea()+smsNumber;
								returnRecord.setSmsNumber(smsNumber);
								returnRecord.setResMsgid(resMsgid);
								returnRecord.setCreateTime(new Date());
								returnRecords.add(returnRecord);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return returnRecords;
	}

}
