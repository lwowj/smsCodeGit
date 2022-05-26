package com.hero.sms.controller.agentRemitOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.agentRemitOrder.AgentRemitOrder;
import com.hero.sms.service.agentRemitOrder.IAgentRemitOrderService;

import lombok.extern.slf4j.Slf4j;

/**
 * 代理提现订单 Controller
 *
 * @author Administrator
 * @date 2020-04-02 22:24:19
 */
@Slf4j
@Validated
@Controller
@RequestMapping("agentRemitOrder")
public class AgentRemitOrderController extends BaseController {

    @Autowired
    private IAgentRemitOrderService agentRemitOrderService;


    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("agentRemitOrder:list")
    public FebsResponse agentRemitOrderList(QueryRequest request, AgentRemitOrder agentRemitOrder, Date createStartTime, Date createEndTime) {
    	
        /**
         * 【管理后台一般不做负载，图片存于本机。故暂不需要复制下载】
         * @begin
         * 2021-04-21
         * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
         * 【说明】：这里需要新增校验sendBox.getSmsNumbers() 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
         * 【步骤】：
         * 1、使用文件工具判断文件地址的文件是否存在于本机上
         * 2、获取字典中负载服务器的IP地址
         * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
         */
        /**
         * @end
         */
        IPage<AgentRemitOrder> agentRemitOrderIPage = this.agentRemitOrderService.findAgentRemitOrders(request, agentRemitOrder,createStartTime,createEndTime);
        Map<String, Object> dataTable = getDataTable(agentRemitOrderIPage);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "修改代理提现订单", exceptionMessage = "修改失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("agentRemitOrder:update")
    public FebsResponse updateAgentRemitOrder(AgentRemitOrder agentRemitOrder) {
        this.agentRemitOrderService.updateAgentRemitOrder(agentRemitOrder);
        return new FebsResponse().success();
    }


    @ControllerEndpoint(operation = "代理提现订单审核通过", exceptionMessage = "订单审核失败")
    @PostMapping("auditSuccess")
    @ResponseBody
    @RequiresPermissions("agentRemitOrder:auditSuccess")
    public FebsResponse auditSuccess(@Valid AgentRemitOrder agentRemitOrder) {
        try {
            this.agentRemitOrderService.auditSuccess(agentRemitOrder);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("订单审核失败",e);
            return new FebsResponse().message("订单审核失败").fail();
        }
        return new FebsResponse().success().message("操作成功");
    }

    @ControllerEndpoint(operation = "代理提现订单审核不通过", exceptionMessage = "订单审核失败")
    @GetMapping("auditFail/{id}")
    @ResponseBody
    @RequiresPermissions("agentRemitOrder:auditFail")
    public FebsResponse auditFail(@PathVariable Integer id) {
        try {
            this.agentRemitOrderService.auditFail(id);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("订单审核失败",e);
            return new FebsResponse().message("订单审核失败").fail();
        }
        return new FebsResponse().success().message("操作成功");
    }

    @GetMapping("getBanks")
    @ResponseBody
    public FebsResponse getBanks(){
        List<Map<String,String>> list = this.agentRemitOrderService.getBanks();
        return new FebsResponse().success().data(list);
    }

}
