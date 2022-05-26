package com.hero.sms.controller;

import com.alibaba.fastjson.JSON;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.service.common.IBusinessManage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("api")
public class ApiController extends BaseController {

    @Autowired
    private IBusinessManage businessManage;

    @GetMapping(value = "loadConfigCache")
    public String loadConfigCache(HttpServletRequest request, @RequestParam("data") String data) {
        if(StringUtils.isBlank(data)) {
            return JSON.toJSONString(new FebsResponse().fail().message("参数缺失！"));
        }
        log.debug("商户缓存加载请求数据：" + data);
        FebsResponse result =  businessManage.loadConfigCache(data, IPUtil.getIpAddr(request));
        return JSON.toJSONString(result);
    }
}
