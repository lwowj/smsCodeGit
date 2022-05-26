package com.hero.sms.controller.organization;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentCost;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.service.agent.IAgentCostService;
import com.hero.sms.service.organization.IOrganizationCostService;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商户用户资费 Controller
 *
 * @author Administrator
 * @date 2020-03-08 00:12:30
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organizationCost")
public class OrganizationCostController extends BaseController {

    @Autowired
    private IOrganizationCostService organizationCostService;
    @Autowired
    private IAgentCostService agentCostService;


    @ControllerEndpoint(operation = "商户用户资费更新")
    @PostMapping("updates")
    @ResponseBody
    @RequiresPermissions("organizationCost:updates")
    public FebsResponse updateOrganizationCosts(@RequestBody List<OrganizationCost> organizationCosts) {
        try {
            Agent agent = getCurrentAgent();
            //指定通道跟随代理
            assignChannelWithAgent(organizationCosts,agent);
            this.organizationCostService.updateOrganizationCostsOnAgent(organizationCosts,agent);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("商户用户资费更新失败",e);
            return new FebsResponse().message("商户用户资费更新失败").fail();
        }
        return new FebsResponse().success();
    }

    /**
     * 默认指定通道
     * 代理资费有指定通道  并且  商户资费未配置通道的情况下
     * 商户资费的指定通道默认跟随代理
     * @param costs
     * @param agent
     */
    private void assignChannelWithAgent(List<OrganizationCost> costs,Agent agent){
        LambdaQueryWrapper<AgentCost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentCost::getAgentId,agent.getId());
        List<AgentCost> list = agentCostService.list(queryWrapper);

        //按SMSTYPE  和 NAME  组合分组
        Map<String, List<String>> collect = list.stream().collect(Collectors.groupingBy(e -> fetchGroupKey(String.valueOf(e.getSmsType()),e.getName()),
                Collectors.mapping(AgentCost::getChannelId, Collectors.toList())));

        if (costs.size() > 0){
            costs.stream().forEach(organizationCost -> {
                String key = fetchGroupKey(String.valueOf(organizationCost.getSmsType()),organizationCost.getCostName());
                //资费没有指定通道 并且 代理有指定通道    默认指定通道跟随代理
                if (StringUtils.isBlank(organizationCost.getChannelId()) && collect.containsKey(key)){
                    List<String> channelIds = collect.get(key);
                    if (channelIds.size() > 1 ){
                        log.error(String.format("代理资费(smsType#name)【%s】不唯一，商户资费默认通道失败！",key));
                        return;
                    }
                    organizationCost.setChannelId(channelIds.get(0));
                }
            });
        }
    }

    private String fetchGroupKey(String smsType,String name){
        return smsType + "#" + name;
    }
}
