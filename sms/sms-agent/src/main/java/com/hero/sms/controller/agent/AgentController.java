package com.hero.sms.controller.agent;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.service.ISessionService;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.service.agent.IAgentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商户代理 Controller
 *
 * @author Administrator
 * @date 2020-03-06 10:05:11
 */
@Slf4j
@Validated
@Controller
public class AgentController extends BaseController {

    @Autowired
    private IAgentService agentService;

    @Autowired
    private ISessionService sessionService;

    @PostMapping("agent/password/update")
    @ControllerEndpoint(operation = "修改密码",exceptionMessage = "修改密码失败")
    @ResponseBody
    public FebsResponse updatePassword(
            @NotBlank(message = "{required}") String oldPassword,
            @NotBlank(message = "{required}") String newPassword) {
        Agent agent = getCurrentAgent();
        if (!StringUtils.equals(agent.getAgentPassword(), MD5Util.encrypt(agent.getAgentAccount(), oldPassword))) {
            return new FebsResponse().message("原密码不正确").fail();
        }
        agentService.updatePassword(agent.getAgentAccount(), newPassword);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改支付密码")
    @PostMapping("agent/payPassword/update")
    @ResponseBody
    public FebsResponse updatePayPassword(
            @NotBlank(message = "{required}") String oldPayPassword,
            @NotBlank(message = "{required}") String newPayPassword) {
        Agent agent = getCurrentAgent();
        if (!StringUtils.equals(agent.getPayPassword(), MD5Util.MD5(oldPayPassword))) {
            return new FebsResponse().message("原支付密码不正确").fail();
        }
        agentService.updatePayPassword(agent.getAgentAccount(), newPayPassword);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "绑定谷歌")
    @PostMapping("agent/bindGoogle")
    @ResponseBody
    @RequiresPermissions("agent:bindGoogle")
    public FebsResponse bindGoogle(@NotBlank(message = "{required}") String goologoVerifyCode,
                                   @NotBlank(message = "{required}") String googleKey,
                                   @NotBlank(message = "{required}") String payPassword){
        try {
            agentService.bindGoogle(getCurrentAgent(),goologoVerifyCode,googleKey,payPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success().message("绑定成功，重新登录后起效");
    }

    @ControllerEndpoint(operation = "解绑谷歌")
    @PostMapping("agent/unbindGoogle")
    @ResponseBody
    @RequiresPermissions("agent:bindGoogle")
    public FebsResponse getGoogleKey(@NotBlank(message = "{required}") String goologoVerifyCode,
                                     @NotBlank(message = "{required}") String payPassword){
        try {
            agentService.removeGoogleKey(getCurrentAgent(),goologoVerifyCode,payPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success().message("解绑成功，重新登录后起效");
    }

    @ControllerEndpoint(operation = "代理列表")
    @GetMapping("agent/list")
    @ResponseBody
    @RequiresPermissions("agent:view")
    public FebsResponse agentList(QueryRequest request, Agent agent) {
        agent.setUpAgentId(getCurrentAgent().getId());
        IPage<Agent> list =  this.agentService.findAgents(request, agent);
        List<Agent> agentList = list.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        agentList.stream().forEach(item -> {
            Map<String,Object> map = new HashMap();
            map.put("id",item.getId());
            map.put("agentName",item.getAgentName());
            map.put("agentAccount",item.getAgentAccount());
            map.put("amount",item.getAmount());
            map.put("quotaAmount",item.getQuotaAmount());
            map.put("availableAmount",item.getAvailableAmount());
            map.put("cashAmount",item.getCashAmount());
            map.put("sendSmsTotal",item.getSendSmsTotal());
            map.put("stateCode",item.getStateCode());
            map.put("loginFaildCount",item.getLoginFaildCount());
            map.put("createDate",item.getCreateDate());
            dataList.add(map);
        });
        Map<String, Object> dataTable = getDataTableTransformMap(list,dataList);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增代理")
    @PostMapping("agent")
    @ResponseBody
    @RequiresPermissions("agent:add")
    public FebsResponse addAgent(@Valid Agent agent, String checkPassword) {
        try {
            Agent user = super.getCurrentAgent();
            agent.setCreateUser(user.getAgentAccount());
            agent.setUpAgentId(user.getId());
            this.agentService.createAgent(agent,checkPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("新增代理失败",e);
            return new FebsResponse().message("新增代理失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "重置代理密码")
    @PostMapping("agent/resetPassword/{agentAccounts}")
    @ResponseBody
    @RequiresPermissions("agent:resetPassword")
    public FebsResponse resetPassword(@NotBlank(message = "{required}") @PathVariable String agentAccounts) {
        String[] usernameArr = agentAccounts.split(StringPool.COMMA);
        this.agentService.resetPasswordAgents(usernameArr,getCurrentAgent());
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "更新代理")
    @PostMapping("agent/update")
    @ResponseBody
    @RequiresPermissions("agent:update")
    public FebsResponse updateAgent(Agent agent) {
        try {
            String state = agent.getStateCode();
            this.agentService.updateAgent(agent,getCurrentAgent());
            if (StringUtils.isNotBlank(state) && (state.equals("0") || state.equals("2"))){
                sessionService.forceLogoutAgent(agent.getId(),super.getCurrentAgent().getId());
            }
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("更新代理失败",e);
            return new FebsResponse().message("更新代理失败").fail();
        }
        return new FebsResponse().success();
    }

    @GetMapping("agent/getAgents")
    @ResponseBody
    public FebsResponse getAgents(){
        List<Map<Integer,String>> list = this.agentService.getCurrentAgents(getCurrentAgent());
        return new FebsResponse().success().data(list);
    }

}
