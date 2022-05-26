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
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.HttpUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.common.CommonStateEnums;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HaiTun extends BaseSmsPushService{

	@Override
	public void init(SmsChannelExt smsChannelext) {
		super.init(smsChannelext);
	}
	
	@Override
	public boolean httpPush(SendRecord sendRecord) {
		try {
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("enterprise_no", orgNo);
			params.put("account", account);
			params.put("phones", sendRecord.getSmsNumberArea()+sendRecord.getSmsNumber());
			params.put("content", sendRecord.getSmsContent());
			String timestamp = DateUtil.getDateFormat(new Date(), "yyyyMMddHHmmssSSS");
			params.put("timestamp", timestamp);
			StringBuilder checkParam = new StringBuilder();
			checkParam.append(orgNo).append(account).append(timestamp).append(signKey);
			String sign = MD5Util.MD5(checkParam.toString());
			params.put("sign", sign);
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(connectTimeout);// 设置超时  
			requestFactory.setReadTimeout(readTimeout);
			HttpHeaders headers = new HttpHeaders();
	    	headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			String result = HttpUtil.sendPostRequest(reqUrl, requestFactory,headers, params);
			log.info("【海豚】提交返回："+result);
			JSONObject resultJson = JSON.parseObject(result);
			String resultCode = resultJson.getString("result");
			if(resultCode.equals("0")) {
				sendRecord.setResMsgid(resultJson.getString("msgid"));
				return true;
			}
			String desc = resultJson.getString("desc");
			sendRecord.setStateDesc(desc);
		} catch (Exception e) {
			log.error("海豚请求失败",e);
			sendRecord.setStateDesc("通道请求失败");
		}
		return false;
	}

	@Override
	public List<ReturnRecord> receipt(String resultData) {
		JSONObject resultJson = JSON.parseObject(resultData);
		StringBuilder checkParam = new StringBuilder();
		checkParam.append(orgNo).append(account).append(resultJson.getString("timestamp")).append(signKey);
		String checkSign = MD5Util.MD5(checkParam.toString());
		if(!resultJson.getString("sign").equals(checkSign)) {
			log.info("海豚回执信息验签失败："+resultData);
			return null;
		}
		List<ReturnRecord> returnRecords = Lists.newArrayList();
		JSONArray reportArr = resultJson.getJSONArray("report");
		if(reportArr != null) {
			for (int i = 0 ; i < reportArr.size(); i++) {
				JSONObject report = reportArr.getJSONObject(i);
				ReturnRecord returnRecord = new ReturnRecord();
				String result = report.getString("result");
				if(result.equals("success")) {
					returnRecord.setReturnState(CommonStateEnums.SUCCESS.getCode());
				}else {
					returnRecord.setReturnState(CommonStateEnums.FAIL.getCode());
				}
				returnRecord.setSmsNumber(report.getString("phone"));
				returnRecord.setResMsgid(report.getString("msgid"));
				returnRecord.setReturnSeq(report.getString("seq"));
				returnRecord.setCreateTime(new Date());
				returnRecords.add(returnRecord);
			}
		}
		return returnRecords;
	}

}
