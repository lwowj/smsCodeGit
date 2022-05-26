package com.hero.sms.service.impl.channel.payTransfer.transfer;

import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hero.sms.commands.pay.PayBaseCommand;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.utils.CertificateUtil;
import com.hero.sms.common.utils.HttpUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.rechargeOrder.OrganizationRechargeOrder;
import com.hero.sms.service.channel.payTransfer.BasePayChannel;
import com.hero.sms.utils.RandomUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 鑫发
 */
@Slf4j
public class XinFa implements BasePayChannel {
    @Override
    public FebsResponse pay(PayChannel payChannel, OrganizationRechargeOrder rechargeOrder) {
        try {
            Map<String, String> paramMap = new TreeMap<String, String>();
            paramMap.put("orderNo", rechargeOrder.getOrderNo());
            paramMap.put("version", "V3.3.0.0");
            paramMap.put("charsetCode", "UTF-8");//
            paramMap.put("randomNum", RandomUtil.randomStr(4));// 4位随机数
            paramMap.put("merchNo", payChannel.getMerchNo());
            paramMap.put("payType", payChannel.getPayCode());// WX:微信支付,ZFB:支付宝支付
            paramMap.put("amount", rechargeOrder.getRechargeAmount().toString());// 单位:分
            paramMap.put("goodsName", "充值");// 商品名称：20位
            paramMap.put("notifyUrl", payChannel.getCallbackUrl());// 回调地址
            paramMap.put("notifyViewUrl", "http://localhost/view");// 回显地址
            String signStr = JSON.toJSONString(paramMap) + payChannel.getSignKey();
            String sign = MD5Util.MD5(signStr);
            paramMap.put("sign",sign);
            String data = CertificateUtil.encryptByPublicKey(JSON.toJSONString(paramMap),payChannel.getEncryptKey());
            String reqParam = "data=" + URLEncoder.encode(data, "UTF-8") + "&merchNo=" +payChannel.getMerchNo()
                    + "&version=" + paramMap.get("version");
            String result = HttpUtil.sendPostRequest(payChannel.getRequestUrl(),reqParam);
            JSONObject resultJson = JSON.parseObject(result);
            String stateCode = resultJson.getString("stateCode");
            if("00".equals(stateCode)){
                febsResponse.put(PayBaseCommand.PAY_URL,resultJson.getString("qrcodeUrl"));
                febsResponse.put(PayBaseCommand.PAY_URL_TYPE,payChannel.getKey5());
                febsResponse.success();
                return febsResponse;
            }
            rechargeOrder.setReqStateDescription(resultJson.getString("msg"));
            febsResponse.fail();
        }catch (Exception e){
            log.error("鑫发提交报错");
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
            JSONObject resultJson = JSON.parseObject(data);
            String resultData = CertificateUtil.decryptByPrivateKey(resultJson.getString("data"), payChannel.getKey1());
            JSONObject dataJson = JSON.parseObject(resultData);
            Map<String, String> metaSignMap = JSONObject.parseObject(dataJson.toJSONString(), new TypeReference<TreeMap<String, String>>(){});
            String resultSign = metaSignMap.remove("sign");
            String targetString = MD5Util.MD5(JSON.toJSONString(metaSignMap) + payChannel.getSignKey());
            if(resultSign.equals(targetString)){
                if("00".equals(metaSignMap.get("payStateCode"))){
                    febsResponse.success();
                    return febsResponse;
                }
            }
        }catch (Exception e){
            log.error("鑫发回调报错");
        }
        febsResponse.fail();
        return febsResponse;
    }
}
