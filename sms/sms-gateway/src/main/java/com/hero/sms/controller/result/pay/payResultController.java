package com.hero.sms.controller.result.pay;

import com.alibaba.fastjson.JSONObject;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.service.channel.payTransfer.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 回调
 */
@Slf4j
@Validated
@Controller
@RequestMapping("result")
public class payResultController {
    @Autowired
    private IPayService payService;

    @RequestMapping("hrResult")
    @ResponseBody
    public String hrResult(HttpServletRequest request){
        try {
        	JSONObject retData = JSONObject.parseObject(request.getParameter("NoticeParams"));
			// 业务报文
			JSONObject resultJson = JSONObject.parseObject("{}");
			resultJson.put("orderNo", retData.getString("outTradeNo"));
			resultJson.put("ip", IPUtil.getIpAddr(request));
			resultJson.put("data", retData);
			payService.payResult(resultJson.toString());
        }catch (Exception e){
            log.error("HR接收回调失败");
        }
        return "SUCCESS";
    }
//    public static void main(String[] args) {
//		try {
//			Integer gatewayType = 6 ;
////			0111
////			0001 0010  0011 0100 
////          0001 0010  0011 0100 0101
////			0001 0000  0001 0100 0101     
//			System.out.println(gatewayType&1);
//			System.out.println(gatewayType&2);
//			System.out.println(gatewayType&4);
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
//	}

}
