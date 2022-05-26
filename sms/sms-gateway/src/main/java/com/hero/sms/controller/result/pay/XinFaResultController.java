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
 * 鑫发回调
 */
@Slf4j
@Validated
@Controller
@RequestMapping("result")
public class XinFaResultController {
    @Autowired
    private IPayService payService;

    @RequestMapping("xinfa")
    @ResponseBody
    public String pay(HttpServletRequest request){
        try {
            Enumeration<String> e = request.getParameterNames();
            JSONObject parameterStr = JSONObject.parseObject("{}");
            while(e.hasMoreElements()){
                String key = e.nextElement();
                if(StringUtils.isNotEmpty(request.getParameter(key))) {
                    parameterStr.put(key, request.getParameter(key));
                }
            }
            JSONObject data = JSONObject.parseObject("{}");
            data.put("data", parameterStr);
            data.put("orderNo", parameterStr.getString("orderNo"));
            data.put("ip", IPUtil.getIpAddr(request));
            payService.payResult(data.toString());
        }catch (Exception e){
            log.error("鑫发接收回调失败");
        }
        return "SUCCESS";
    }


}
