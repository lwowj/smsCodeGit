package com.hero.sms.service.impl.channel.payTransfer.transfer;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hero.sms.commands.pay.PayBaseCommand;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.utils.HttpUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.channel.payTransfer.BasePayChannel;
import com.hero.sms.utils.RandomUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * HR
 */
@Slf4j
public class HengRun implements BasePayChannel {
    @Override
    public FebsResponse pay(PayChannel payChannel, OrganizationRechargeOrder rechargeOrder) {
        try 
        {
        	Map<String, String> metaSignMap = new TreeMap<String, String>();
			metaSignMap.put("appID", payChannel.getMerchNo());
			metaSignMap.put("tradeCode", payChannel.getPayCode());
			metaSignMap.put("randomNo", RandomUtil.randomStr(4)); 
			metaSignMap.put("outTradeNo", rechargeOrder.getOrderNo());
			String amount = String.valueOf(rechargeOrder.getRechargeAmount());
			metaSignMap.put("totalAmount", amount); 
			metaSignMap.put("productTitle", "充");
			metaSignMap.put("notifyUrl", payChannel.getCallbackUrl());
			metaSignMap.put("tradeIP", "198.168.0.1");
			String sign = MD5Util.MD5(getSrcSginStr(metaSignMap) + payChannel.getSignKey());
			metaSignMap.put("sign",sign);
			
			String jsonStr = JSON.toJSONString(metaSignMap);
			Map<String, String> req = new TreeMap<String, String>();
			req.put("ApplyParams",URLEncoder.encode(jsonStr, "UTF-8"));
			
            String reqParam = "ApplyParams=" + URLEncoder.encode(jsonStr, "UTF-8");
            String result = HttpUtil.sendPostRequest(payChannel.getRequestUrl(),reqParam);
            if(result!=null&&!"".equals(result))
            {
            	JSONObject retJson = JSONObject.parseObject(result);
                String stateCode = String.valueOf(retJson.get("stateCode"));
     			if (stateCode.equals("0000")) 
     			{
     				febsResponse.put(PayBaseCommand.PAY_URL,retJson.getString("payURL"));
     	            febsResponse.put(PayBaseCommand.PAY_URL_TYPE,payChannel.getKey5());
     	            febsResponse.success();
     	            return febsResponse;
     			}
     			rechargeOrder.setReqStateDescription(String.valueOf(retJson.get("stateInfo")));
            }
            febsResponse.fail();
        }catch (Exception e){
            log.error("HR提交报错");
            febsResponse.fail();
        }
        return febsResponse;
    }

    @Override
    public FebsResponse payQuery(PayChannel payChannel, OrganizationRechargeOrder rechargeOrder) {
        return null;
    }

    @Override
    public FebsResponse payResult(PayChannel payChannel, OrganizationRechargeOrder rechargeOrder, String data) {
        try {
            JSONObject jsonObj = JSON.parseObject(data);
//            String retData = resultJson.getString("data");
//			JSONObject jsonObj = JSONObject.parseObject(retData);
			String paramSign =(String) jsonObj.remove("sign");
			
			Map<String, String> jsonToMap = jsonToMap(jsonObj);
			String sign = MD5Util.MD5(getSrcSginStr(jsonToMap) + payChannel.getSignKey());
			if(paramSign.equals(sign)){
				if("0000".equals(String.valueOf(jsonObj.get("payCode"))))
				{
	               febsResponse.success();
	               return febsResponse;
	            }
	        }
        }catch (Exception e){
            log.error("HR回调报错");
        }
        febsResponse.fail();
        return febsResponse;
    }
    
	public static String getSrcSginStr(Map<String, String> map) {
		String returnStr = "";
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			String value = entry.getValue();
			if(0==returnStr.length()){
				returnStr = value+"|";
			}else{
				returnStr = returnStr+value+"|";
			}
		}
		//LOG.debug("returnStr=" + returnStr);
		return returnStr;
	}
	
	public static Map<String, String> jsonToMap(JSONObject json) {
		Map<String, String> outMap = new TreeMap<String, String>();
		Map<String,Object> map = (Map<String,Object>)json;
		Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) 
		{
			Map.Entry<String, Object> entry = it.next();
			String key = String.valueOf(entry.getKey());
			String value = String.valueOf(entry.getValue());
			outMap.put(key, value);
		}
		return outMap;
	}
}
