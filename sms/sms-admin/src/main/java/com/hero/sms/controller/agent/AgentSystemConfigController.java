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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.agent.AgentSystemConfig;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.service.agent.IAgentSystemConfigService;
import com.hero.sms.utils.FileDownLoadUtil;
import com.hero.sms.utils.StringUtil;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 代理系统配置 Controller
 *
 * @author Administrator
 * @date 2020-03-18 15:09:53
 */
@Slf4j
@Validated
@Controller
@RequestMapping("agentSystemConfig")
public class AgentSystemConfigController extends BaseController {

    @Autowired
    private IAgentSystemConfigService agentSystemConfigService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "agentSystemConfig")
    public String agentSystemConfigIndex(){
        return FebsUtil.view("agentSystemConfig/agentSystemConfig");
    }

    @ControllerEndpoint(operation = "代理系统配置列表")
    @GetMapping
    @ResponseBody
    @RequiresPermissions("agentSystemConfig:list")
    public FebsResponse getAllAgentSystemConfigs(AgentSystemConfig agentSystemConfig) {
//    	/**
//    	 * @begin
//    	 * 2020-11-04
//    	 * 添加图片显示IP
//    	 */
//    	Code agent_img_ip_code =  DatabaseCache.getCodeBySortCodeAndCode("AgentPropertyConfig","agent_img_ip");
//    	String agent_img_ip = "";
//    	if(agent_img_ip_code!=null&&agent_img_ip_code.getName()!=null&&!"".equals(agent_img_ip_code.getName()))
//    	{
//    		agent_img_ip = agent_img_ip_code.getName();
//    	}
//    	List<AgentSystemConfig> agentSystemConfigList = agentSystemConfigService.findAgentSystemConfigs(agentSystemConfig);
//    	if(agent_img_ip!=null&&!"".equals(agent_img_ip))
//    	{
//    		if(agentSystemConfigList!=null&&agentSystemConfigList.size()>0)
//        	{
//        		for (int i = 0; i < agentSystemConfigList.size(); i++) 
//        		{
//        			if(agentSystemConfigList.get(i).getSystemLogoUrl()!=null&&!"".equals(agentSystemConfigList.get(i).getSystemLogoUrl()))
//        			{
//        				String systemLogoUrl = agent_img_ip + agentSystemConfigList.get(i).getSystemLogoUrl();
//            			agentSystemConfigList.get(i).setSystemLogoUrl(systemLogoUrl);
//        			}
//        		}
//        	}
//    	}
//    	/**
//    	 * @end 
//    	 */
    	
    	 /**
         * @begin
         * 2021-04-21
         * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
         * 【说明】：这里需要新增校验agentSystemConfig.getSystemLogoUrl() 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
         * 【步骤】：
         * 1、使用文件工具判断文件地址的文件是否存在于本机上
         * 2、获取字典中负载服务器的IP地址
         * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
         */
    	List<AgentSystemConfig> agentSystemConfigList = agentSystemConfigService.findAgentSystemConfigs(agentSystemConfig);
		if(agentSystemConfigList!=null&&agentSystemConfigList.size()>0)
    	{
    		for (int i = 0; i < agentSystemConfigList.size(); i++) 
    		{
    			if(agentSystemConfigList.get(i).getSystemLogoUrl()!=null&&!"".equals(agentSystemConfigList.get(i).getSystemLogoUrl()))
    			{
    				String systemLogoUrl = agentSystemConfigList.get(i).getSystemLogoUrl();
        			if(StringUtil.isNotBlank(systemLogoUrl))
        			{
        				FileDownLoadUtil.downLoadAgentLogo(systemLogoUrl);
        			}
    			}
    		}
    	}
        /**
         * @end
         */
        return new FebsResponse().success().data(agentSystemConfigList);
    }

    @ControllerEndpoint(operation = "代理系统配置列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("agentSystemConfig:list")
    public FebsResponse agentSystemConfigList(QueryRequest request, AgentSystemConfig agentSystemConfig) {
//    	/**
//    	 * @begin
//    	 * 2020-11-04
//    	 * 添加图片显示IP
//    	 */
//    	Code agent_img_ip_code =  DatabaseCache.getCodeBySortCodeAndCode("AgentPropertyConfig","agent_img_ip");
//    	String agent_img_ip = "";
//    	if(agent_img_ip_code!=null&&agent_img_ip_code.getName()!=null&&!"".equals(agent_img_ip_code.getName()))
//    	{
//    		agent_img_ip = agent_img_ip_code.getName();
//    	}
//    	IPage<AgentSystemConfig> agentSystemConfigs = this.agentSystemConfigService.findAgentSystemConfigs(request, agentSystemConfig);
//    	if(agent_img_ip!=null&&!"".equals(agent_img_ip))
//    	{
//    		if(agentSystemConfigs!=null&&agentSystemConfigs.getRecords().size()>0)
//        	{
//        		for (int i = 0; i < agentSystemConfigs.getRecords().size(); i++) 
//        		{
//        			String systemLogoUrl = agent_img_ip + agentSystemConfigs.getRecords().get(i).getSystemLogoUrl();
//        			agentSystemConfigs.getRecords().get(i).setSystemLogoUrl(systemLogoUrl);
//        		}
//        	}
//    	}
//    	/**
//    	 * @end 
//    	 */
    	
    	 /**
         * @begin
         * 2021-04-21
         * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
         * 【说明】：这里需要新增校验agentSystemConfig.getSystemLogoUrl() 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
         * 【步骤】：
         * 1、使用文件工具判断文件地址的文件是否存在于本机上
         * 2、获取字典中负载服务器的IP地址
         * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
         */
    	IPage<AgentSystemConfig> agentSystemConfigs = this.agentSystemConfigService.findAgentSystemConfigs(request, agentSystemConfig);
    	if(agentSystemConfigs!=null&&agentSystemConfigs.getRecords().size()>0)
    	{
    		for (int i = 0; i < agentSystemConfigs.getRecords().size(); i++) 
    		{
    			String systemLogoUrl = agentSystemConfigs.getRecords().get(i).getSystemLogoUrl();
    			if(StringUtil.isNotBlank(systemLogoUrl))
    			{
    				FileDownLoadUtil.downLoadAgentLogo(systemLogoUrl);
    			}
    		}
    	}
        /**
         * @end
         */
    	
        Map<String, Object> dataTable = getDataTable(agentSystemConfigs);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "代理系统配置新增")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("agentSystemConfig:add")
    public FebsResponse addAgentSystemConfig(@Valid AgentSystemConfig agentSystemConfig) {
        this.agentSystemConfigService.createAgentSystemConfig(agentSystemConfig);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理系统配置删除")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("agentSystemConfig:delete")
    public FebsResponse deleteAgentSystemConfig(AgentSystemConfig agentSystemConfig) {
        this.agentSystemConfigService.deleteAgentSystemConfig(agentSystemConfig);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理系统配置删除")
    @GetMapping("delete/{agentSystemConfigIds}")
    @ResponseBody
    @RequiresPermissions("agentSystemConfig:delete")
    public FebsResponse deleteAgentSystemConfig(@NotBlank(message = "{required}") @PathVariable String agentSystemConfigIds) {
        String[] ids = agentSystemConfigIds.split(StringPool.COMMA);
        this.agentSystemConfigService.deleteAgentSystemConfigs(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理系统配置更新")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("agentSystemConfig:update")
    public FebsResponse updateAgentSystemConfig(AgentSystemConfig agentSystemConfig) {
        this.agentSystemConfigService.updateAgentSystemConfig(agentSystemConfig);
        return new FebsResponse().success();
    }

    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("agentSystemConfig:export")
    public void export(QueryRequest queryRequest, AgentSystemConfig agentSystemConfig, HttpServletResponse response) {
        List<AgentSystemConfig> agentSystemConfigs = this.agentSystemConfigService.findAgentSystemConfigs(queryRequest, agentSystemConfig).getRecords();
        ExcelKit.$Export(AgentSystemConfig.class, response).downXlsx(agentSystemConfigs, false);
    }

    @ControllerEndpoint(operation = "代理系统配置审核通过")
    @GetMapping("approveSuccessAgentConfigs/{configIds}")
    @ResponseBody
    @RequiresPermissions("agentConfig:approve")
    public FebsResponse approveSuccessAgentConfigs(@NotBlank(message = "{required}") @PathVariable String configIds) {
        String[] ids = configIds.split(StringPool.COMMA);
        this.agentSystemConfigService.approveAgentConfigs(ids, OrgApproveStateEnums.SUCCESS.getCode());
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "代理系统配置审核拒绝")
    @GetMapping("approveFailAgentConfigs/{configIds}")
    @ResponseBody
    @RequiresPermissions("agentConfig:approve")
    public FebsResponse approveFailAgentConfigs(@NotBlank(message = "{required}") @PathVariable String configIds) {
        String[] ids = configIds.split(StringPool.COMMA);
        this.agentSystemConfigService.approveAgentConfigs(ids, OrgApproveStateEnums.FAIL.getCode());
        return new FebsResponse().success();
    }

}
