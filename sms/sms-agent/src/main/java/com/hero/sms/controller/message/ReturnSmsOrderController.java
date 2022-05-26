package com.hero.sms.controller.message;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrder;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrderQuery;
import com.hero.sms.service.rechargeOrder.IReturnSmsOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hero.sms.common.utils.FebsUtil.getCurrentUser;

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
        Agent user = getCurrentUser();
        if (user == null || user.getId() == null || user.getAgentAccount() == null) {
            return new FebsResponse().fail().message("账户异常,数据请求失败").fail();
        }
        //只能查询自己的记录
        returnSmsOrder.setAgentIdQueryOr(user.getId());
        IPage<ReturnSmsOrder> datas = this.returnSmsOrderService.findReturnSmsOrders(request, returnSmsOrder);
        List<Map<String,Object>> list = Lists.newArrayList();
        datas.getRecords().stream().forEach(item -> {
            Map<String,Object> map = new HashMap<>();

            Integer agentReturnAmount = 0;
            String agentType = "代理";
            if (user.getId().intValue() == item.getAgentId().intValue()){
                agentReturnAmount = item.getAgentReturnAmount();
            }else if (user.getId().intValue() == item.getUpAgentId().intValue()){
                agentType = "上级代理";
                agentReturnAmount = item.getUpAgentReturnAmount();
            }else {//正常的情况下只可能是上面两种情况，否则就是条件过滤异常,数据置空（保持分页记录数正确）直接跳过
                 list.add(map);
                 return;
            }
            map.put("createTime",item.getCreateTime());
            map.put("agentType",agentType);
            map.put("agentReturnAmount",agentReturnAmount);
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
