package com.hero.sms.controller.rechargeOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrder;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrderQuery;
import com.hero.sms.service.rechargeOrder.IReturnSmsOrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * 退还短信条数记录 Controller
 *
 * @author Administrator
 * @date 2020-04-20 00:13:55
 */
@Slf4j
@Validated
@Controller
@RequestMapping("returnSmsOrder")
public class ReturnSmsOrderController extends BaseController {

    @Autowired
    private IReturnSmsOrderService returnSmsOrderService;


    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("returnSmsOrder:list")
    public FebsResponse returnSmsOrderList(QueryRequest request, ReturnSmsOrderQuery returnSmsOrder) {
        OrganizationUserExt user = getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode()) || user.getUserAccount() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //只能查询自己的记录
        returnSmsOrder.setOrgCode(user.getOrganizationCode());
        returnSmsOrder.setAgentId(user.getOrganization().getAgentId());
        IPage<ReturnSmsOrder> datas = this.returnSmsOrderService.findReturnSmsOrders(request, returnSmsOrder);
        List<Map<String,Object>> list = Lists.newArrayList();
        datas.getRecords().stream().forEach(item -> {
            Map<String,Object> map = new HashMap<>();
            map.put("createTime",item.getCreateTime());
            map.put("orgReturnAmount",item.getOrgReturnAmount());
            map.put("sendCode",item.getSendCode());
            map.put("orderNo",item.getOrderNo());
            map.put("smsNum",item.getSmsNum());
            //map.put("remark",item.getRemark());
            if (StringUtils.isNotBlank(item.getOrderNo())){
                map.put("orgName",DatabaseCache.getOrgNameByOrgcode(item.getOrgCode()));
            }

            list.add(map);
        });
        Map<String, Object> dataTable = getDataTableTransformMap(datas,list);
        return new FebsResponse().success().data(dataTable);
    }
}
