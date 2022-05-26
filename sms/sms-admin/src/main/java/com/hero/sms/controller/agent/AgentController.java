package com.hero.sms.controller.agent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.service.RedisHelper;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.enums.common.ModuleTypeEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.monitor.service.ISessionService;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

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
    private IBusinessManage businessManage;
    @Autowired
    private ISessionService sessionService;
    @Autowired
    private DatabaseCache databaseCache;
    
    @ControllerEndpoint(operation = "新增代理")
    @GetMapping(FebsConstant.VIEW_PREFIX + "agent/agent")
    public String agentIndex(){
        return FebsUtil.view("agent");
    }

    @ControllerEndpoint(operation = "代理列表")
    @GetMapping("agent")
    @ResponseBody
    @RequiresPermissions("agent:list")
    public FebsResponse getAllAgents(Agent agent) {
        return new FebsResponse().success().data(agentService.findAgents(agent));
    }

    @ControllerEndpoint(operation = "代理列表")
    @GetMapping("agent/list")
    @ResponseBody
    @RequiresPermissions("agent:view")
    public FebsResponse agentList(QueryRequest request, Agent agent) {
        IPage<Agent> datas = this.agentService.findAgents(request, agent);
        List<Agent> agentList = datas.getRecords();
        List<JSONObject> dataList = new ArrayList<JSONObject>();
        agentList.stream().forEach(item -> {
            JSONObject jsonObj = (JSONObject) JSONObject.toJSON(item);
            String format = "AGNET%s_%s";
            String key = String.format(format
                    ,item.getId()
                    ,DateUtil.getString(new Date(), "yyyyMMdd"));
            RedisHelper redisHelper = SpringContextUtil.getBean(RedisHelper.class);
            Integer daySendNum = (Integer)redisHelper.get(key);
            jsonObj.put("daySendNum",daySendNum==null?0:daySendNum);
            dataList.add(jsonObj);
        });
        Map<String, Object> dataTable = getDataTable(datas.getTotal(),dataList);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增代理")
    @PostMapping("agent")
    @ResponseBody
    @RequiresPermissions("agent:add")
    public FebsResponse addAgent(@Valid Agent agent,String checkPassword) {
        try {
            User user = super.getCurrentUser();
            agent.setCreateUser(user.getUsername());
            this.agentService.createAgent(agent,checkPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("新增代理失败",e);
            return new FebsResponse().message("新增代理失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除代理")
    @GetMapping("agent/delete")
    @ResponseBody
    @RequiresPermissions("agent:delete")
    public FebsResponse deleteAgent(Agent agent) {
        this.agentService.deleteAgent(agent);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除代理")
    @GetMapping("agent/delete/{agentIds}")
    @ResponseBody
    @RequiresPermissions("agent:delete")
    public FebsResponse deleteAgent(@NotBlank(message = "{required}") @PathVariable String agentIds) {
        String[] ids = agentIds.split(StringPool.COMMA);
        this.agentService.deleteAgents(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理重置密码")
    @PostMapping("agent/resetPassword/{agentAccounts}")
    @ResponseBody
    @RequiresPermissions("agent:resetPassword")
    public FebsResponse resetPassword(@NotBlank(message = "{required}") @PathVariable String agentAccounts) {
        String[] usernameArr = agentAccounts.split(StringPool.COMMA);
        this.agentService.resetPasswordAgents(usernameArr,null);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理修改")
    @PostMapping("agent/update")
    @ResponseBody
    @RequiresPermissions("agent:update")
    public FebsResponse updateAgent(Agent agent) {
        try {
            String state = agent.getStateCode();
            this.agentService.updateAgent(agent);
            //如果状态是锁定/作废的话,踢出登录的用户
            if (StringUtils.isNotBlank(state) && (state.equals("0") || state.equals("2"))){
                sessionService.forceLogoutAgent(agent.getId());
            };
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("代理修改失败",e);
            return new FebsResponse().message("代理修改失败").fail();
        }
        return new FebsResponse().success();
    }
    
    @GetMapping("agent/excel")
    @RequiresPermissions("agent:export")
    @ResponseBody
    public void export(QueryRequest queryRequest, Agent agent, HttpServletResponse response) {
        List<Agent> agents = this.agentService.findAgents(queryRequest, agent).getRecords();
        ExcelKit.$Export(Agent.class, response).downXlsx(agents, false);
    }

    @GetMapping("agent/getAgents")
    @ResponseBody
    public FebsResponse getAgents(){
        List<Map<Integer,String>> list = this.agentService.getAgents();
        return new FebsResponse().success().data(list);
    }

    //2021-03-06
    @ControllerEndpoint(operation = "代理锁定")
    @GetMapping("agent/lock/{agentIds}")
    @ResponseBody
    @RequiresPermissions("agent:lock")
    public FebsResponse lockAgent(@NotBlank(message = "{required}") @PathVariable String agentIds) {
        String[] ids = agentIds.split(StringPool.COMMA);
        LambdaUpdateWrapper<Agent> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Agent::getStateCode, OrgStatusEnums.Lock.getCode())
                .eq(Agent::getStateCode,OrgStatusEnums.Normal.getCode())
                .in(Agent::getId,ids);
        this.agentService.update(wrapper);
        //如果状态是锁定/作废的话,踢出登录的用户
        for (int i = 0; i < ids.length; i++) 
        {
        	try 
        	{
				int agentid = Integer.valueOf(ids[i]).intValue();
				sessionService.forceLogoutAgent(agentid);
			} catch (Exception e) {}
		}
        try {
        	reladAgentCache("merch,agent,gateway");
		} catch (Exception e) {} 
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理解锁")
    @GetMapping("agent/unlock/{agentIds}")
    @ResponseBody
    @RequiresPermissions("organization:unlock")
    public FebsResponse unlockAgent(@NotBlank(message = "{required}") @PathVariable String agentIds) {
        String[] ids = agentIds.split(StringPool.COMMA);
        LambdaUpdateWrapper<Agent> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Agent::getStateCode, OrgStatusEnums.Normal.getCode())
                .eq(Agent::getStateCode,OrgStatusEnums.Lock.getCode())
                .in(Agent::getId,ids);
        this.agentService.update(wrapper);
        try {
        	reladAgentCache("merch,agent,gateway");
		} catch (Exception e) {} 
        return new FebsResponse().success();
    }
    
    /**
     * 清理指定项目中的代理信息缓存
     * @param projectName
     * @throws ServiceException
     */
    private void reladAgentCache(String projectName) throws ServiceException 
    {
    	databaseCache.initAgent();
    	businessManage.reladProjectCacheForModule(projectName,String.valueOf(ModuleTypeEnums.AgentList.getCode().intValue()));
    }
}
