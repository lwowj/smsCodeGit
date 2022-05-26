package com.hero.sms.controller.agent;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.agent.AgentCost;
import com.hero.sms.service.agent.IAgentCostService;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 商户代理资费 Controller
 *
 * @author Administrator
 * @date 2020-03-06 10:05:33
 */
@Slf4j
@Validated
@Controller
public class AgentCostController extends BaseController {

    @Autowired
    private IAgentCostService agentCostService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentCost")
    public String agentCostIndex(){
        return FebsUtil.view("agentCost/agentCost");
    }

    @ControllerEndpoint(operation = "代理资费列表")
    @GetMapping("agentCost")
    @ResponseBody
    @RequiresPermissions("agentCost:list")
    public FebsResponse getAllAgentCosts(AgentCost agentCost) {
        return new FebsResponse().success().data(agentCostService.findAgentCosts(agentCost));
    }


    @ControllerEndpoint(operation = "代理资费列表")
    @GetMapping("agentCost/list")
    @ResponseBody
    @RequiresPermissions("agentCost:list")
    public FebsResponse agentCostList(QueryRequest request, AgentCost agentCost) {
        Map<String, Object> dataTable = getDataTable(this.agentCostService.findAgentCosts(request, agentCost));
        return new FebsResponse().success().data(dataTable);
    }


    @ControllerEndpoint(operation = "代理资费新增")
    @PostMapping("agentCost")
    @ResponseBody
    @RequiresPermissions("agentCost:add")
    public FebsResponse addAgentCost(@Valid AgentCost agentCost) {
        this.agentCostService.createAgentCost(agentCost);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理资费删除")
    @GetMapping("agentCost/delete")
    @ResponseBody
    @RequiresPermissions("agentCost:delete")
    public FebsResponse deleteAgentCost(AgentCost agentCost) {
        this.agentCostService.deleteAgentCost(agentCost);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理资费更新")
    @PostMapping("agentCost/update")
    @ResponseBody
    @RequiresPermissions("agentCost:update")
    public FebsResponse updateAgentCost(AgentCost agentCost) {
        this.agentCostService.updateAgentCost(agentCost);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理资费修改")
    @PostMapping("agentCost/updates")
    @ResponseBody
    @RequiresPermissions("agentCost:updates")
    public FebsResponse updateAgentCosts(@RequestBody List<AgentCost> agentCosts) {
        try {
            User user = super.getCurrentUser();
            this.agentCostService.updateAgentCosts(agentCosts,user.getUsername());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("代理资费修改失败",e);
            return new FebsResponse().message("代理资费修改失败").fail();
        }
        return new FebsResponse().success();
    }

    @PostMapping("agentCost/excel")
    @ResponseBody
    @RequiresPermissions("agentCost:export")
    public void export(QueryRequest queryRequest, AgentCost agentCost, HttpServletResponse response) {
        List<AgentCost> agentCosts = this.agentCostService.findAgentCosts(queryRequest, agentCost).getRecords();
        ExcelKit.$Export(AgentCost.class, response).downXlsx(agentCosts, false);
    }
    
    @ControllerEndpoint(operation = "批量修改代理资费")
    @PostMapping("agentCost/updateCosts")
    @ResponseBody
    @RequiresPermissions("agent:updateCosts")
    public FebsResponse updateCosts(@NotBlank(message = "{required}") String agentIds,
                                      @NotBlank(message = "{required}") String costName,
                                      @NotBlank(message = "{required}") String smsType,
                                      String operator,
                                      @NotBlank(message = "{required}") String costValue
    ) {
        try {
        	User user = super.getCurrentUser();
        	agentCostService.updateCosts(agentIds,costName,smsType,costValue,operator,user.getUsername());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("批量修改商户资费失败",e);
            return new FebsResponse().message("批量修改商户资费失败").fail();
        }
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "批量上调代理资费")
    @PostMapping("agentCost/updateUpCosts")
    @ResponseBody
    @RequiresPermissions("agent:updateUpCosts")
    public FebsResponse updateUpCosts(@NotBlank(message = "{required}") String agentIds,
                                      @NotBlank(message = "{required}") String costName,
                                      @NotBlank(message = "{required}") String smsType,
                                      String operator,
                                      @NotBlank(message = "{required}") String costValue
    ) {
        try {
        	User user = super.getCurrentUser();
        	agentCostService.updateFloatCosts(agentIds,costName,smsType,costValue,operator,user.getUsername(),"1");
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("批量修改商户资费失败",e);
            return new FebsResponse().message("批量修改商户资费失败").fail();
        }
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "批量下调代理资费")
    @PostMapping("agentCost/updateCutCosts")
    @ResponseBody
    @RequiresPermissions("agent:updateCutCosts")
    public FebsResponse updateCutCosts(@NotBlank(message = "{required}") String agentIds,
                                      @NotBlank(message = "{required}") String costName,
                                      @NotBlank(message = "{required}") String smsType,
                                      String operator,
                                      @NotBlank(message = "{required}") String costValue
    ) {
        try {
        	User user = super.getCurrentUser();
        	agentCostService.updateFloatCosts(agentIds,costName,smsType,costValue,operator,user.getUsername(),"0");
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("批量修改商户资费失败",e);
            return new FebsResponse().message("批量修改商户资费失败").fail();
        }
        return new FebsResponse().success();
    }
}
