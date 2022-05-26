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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agentRemitOrder.AgentRemitOrder;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.agentRemitOrder.IAgentRemitOrderService;
import com.hero.sms.utils.FileDownLoadUtil;
import com.hero.sms.utils.StringUtil;

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
    @Autowired
    private IAgentService agentService;

    @ControllerEndpoint(operation = "提现列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("agentRemitOrder:list")
    public FebsResponse agentRemitOrderList(QueryRequest request, AgentRemitOrder agentRemitOrder, Date createStartTime, Date createEndTime) {
        agentRemitOrder.setAgentId(getCurrentAgent().getId() + "");
        
        /**
         * @begin
         * 2021-04-21
         * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
         * 【说明】：这里需要新增校验sendBox.getSmsNumbers() 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
         * 【步骤】：
         * 1、使用文件工具判断文件地址的文件是否存在于本机上
         * 2、获取字典中负载服务器的IP地址
         * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
         */
        IPage<AgentRemitOrder> agentRemitOrderIPage = this.agentRemitOrderService.findAgentRemitOrders(request, agentRemitOrder,createStartTime,createEndTime);
		if(agentRemitOrderIPage!=null&&agentRemitOrderIPage.getRecords().size()>0)
    	{
    		for (int i = 0; i < agentRemitOrderIPage.getRecords().size(); i++) 
    		{
    			String remitPictureUrl = agentRemitOrderIPage.getRecords().get(i).getRemitPictureUrl();
    			if(StringUtil.isNotBlank(remitPictureUrl))
    			{
    				FileDownLoadUtil.downLoadAgentRemitPicture(remitPictureUrl);
    			}
    		}
    	}
        /**
         * @end
         */
        Map<String, Object> dataTable = getDataTable(agentRemitOrderIPage);
        dataTable.remove("dataMd5");
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "代理提现" , exceptionMessage = "代理提现失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("agentRemitOrder:add")
    public FebsResponse addAgentRemitOrder(@Valid AgentRemitOrder agentRemitOrder,String verifyCode,String payPassword) {
        try {
            Agent agent = super.getCurrentAgent();
            agentService.checkGoogleKey(agent.getAgentAccount(), verifyCode);
            agentRemitOrder.setAgentId(getCurrentAgent().getId() + "");
            this.agentRemitOrderService.createAgentRemitOrder(agentRemitOrder,payPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("代理提现失败",e);
            return new FebsResponse().message("代理提现失败").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "利润转额度" , exceptionMessage = "利润转额度失败")
    @PostMapping("transfer")
    @ResponseBody
    @RequiresPermissions("agentRemitOrder:transfer")
    public FebsResponse transfer(Integer remitAmount,String verifyCode,String payPassword) {
        try {
            Agent agent = super.getCurrentAgent();
            agentService.checkGoogleKey(agent.getAgentAccount(), verifyCode);
            this.agentRemitOrderService.transfer(agent,remitAmount,payPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("利润转额度失败",e);
            return new FebsResponse().message("利润转额度失败").fail();
        }
        return new FebsResponse().success();
    }

    @GetMapping("getBanks")
    @ResponseBody
    public FebsResponse getBanks(){
        List<Map<String,String>> list = this.agentRemitOrderService.getBanks();
        return new FebsResponse().success().data(list);
    }

}
