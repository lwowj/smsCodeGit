package com.hero.sms.controller.channel;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hero.sms.entity.channel.SmsChannelQuery;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.channel.ChannelStateEnums;
import com.hero.sms.enums.common.ModuleTypeEnums;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 短信通道 Controller
 *
 * @author Administrator
 * @date 2020-03-08 17:35:03
 */
@Slf4j
@Validated
@Controller
@RequestMapping("smsChannel")
public class SmsChannelController extends BaseController {

    @Autowired
    private ISmsChannelService smsChannelService;

    @Autowired
    private DatabaseCache databaseCache;
    @Autowired
    private IBusinessManage businessManage;
    
    @GetMapping
    @ResponseBody
    @RequiresPermissions("smsChannel:list")
    public FebsResponse getAllSmsChannels(SmsChannel smsChannel) {
        return new FebsResponse().success().data(smsChannelService.findSmsChannels(smsChannel));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("smsChannel:list")
    public FebsResponse smsChannelList(QueryRequest request, SmsChannelQuery smsChannel) {
        Map<String, Object> dataTable = getDataTable(this.smsChannelService.findSmsChannelsExtGroups(request, smsChannel));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增SmsChannel", exceptionMessage = "新增SmsChannel失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("smsChannel:add")
    public FebsResponse addSmsChannel(@Valid SmsChannel smsChannel) {
        User user = super.getCurrentUser();
        smsChannel.setCreateUserName(user.getUsername());
        try {
            this.smsChannelService.createSmsChannel(smsChannel);
            initDataCacheSmsChannel("gateway");
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除SmsChannel", exceptionMessage = "删除SmsChannel失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("smsChannel:delete")
    public FebsResponse deleteSmsChannel(SmsChannel smsChannel) {
        this.smsChannelService.deleteSmsChannel(smsChannel);
        initDataCacheSmsChannel("gateway");
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除SmsChannel", exceptionMessage = "批量删除SmsChannel失败")
    @GetMapping("delete/{smsChannelIds}")
    @ResponseBody
    @RequiresPermissions("smsChannel:delete")
    public FebsResponse deleteSmsChannel(@NotBlank(message = "{required}") @PathVariable String smsChannelIds) {
        String[] ids = smsChannelIds.split(StringPool.COMMA);
        this.smsChannelService.deleteSmsChannels(ids);
        initDataCacheSmsChannel("gateway");
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改SmsChannel", exceptionMessage = "修改SmsChannel失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("smsChannel:update")
    public FebsResponse updateSmsChannel(SmsChannel smsChannel) {
        try {
            this.smsChannelService.updateSmsChannel(smsChannel);
            initDataCacheSmsChannel("gateway");
        } catch (ServiceException e) {
            return new FebsResponse().fail().message(e.getMessage());
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改日限量", exceptionMessage = "修改日限量失败")
    @PostMapping("updateDayLimit")
    @ResponseBody
    @RequiresPermissions("smsChannel:update")
    public FebsResponse updateDayLimit(@RequestParam(value = "id",required = true) Integer id
            ,@RequestParam(value = "dayLimit") Long dayLimit) {
        LambdaUpdateWrapper<SmsChannel> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(SmsChannel::getDayLimit,dayLimit)
                .eq(SmsChannel::getId,id);
        this.smsChannelService.update(wrapper);
        return new FebsResponse().success();
    }


    @ControllerEndpoint(operation = "网关开/关", exceptionMessage = "网关开/关失败")
    @PostMapping("switch")
    @ResponseBody
    @RequiresPermissions("smsChannel:switch")
    public FebsResponse smsChannelSwitch(@RequestParam(value = "channelId",required = true) String channelIds
                    ,@RequestParam(value = "state",required = true) Integer state) {
        try {
            String[] ids = channelIds.split(StringPool.COMMA);
            if(ids!=null && ids.length>0){
                for(String id:ids){
                	int idint = Integer.parseInt(id);
                	SmsChannel thisSmsChannel = smsChannelService.getById(idint);
                	if(thisSmsChannel!=null)
            		{
            			//当通道状态为“作废”状态时，不可开启或关闭等操作
            			if(thisSmsChannel.getState() != ChannelStateEnums.INVALID.getCode())
            			{
            				this.smsChannelService.updateSmsChannelStatus(idint,state);
            				initDataCacheSmsChannel("gateway");
            			}
            			else
            			{
            				return new FebsResponse().fail().message("通道状态为“作废”状态，不可操作！");
            			}
            		}
            		else
            		{
            			return new FebsResponse().fail().message("验证所选通道信息失败！");
            		}
                    
                }
            }

        } catch (ServiceException e) {
            return new FebsResponse().fail().message(e.getMessage());
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "通道作废", exceptionMessage = "通道作废失败")
    @PostMapping("invalid")
    @ResponseBody
    @RequiresPermissions("smsChannel:invalid")
    public FebsResponse smsChannelInvalid(@RequestParam(value = "channelId",required = true) String channelIds) {
        try {
            String[] ids = channelIds.split(StringPool.COMMA);
            if(ids!=null && ids.length>0){
            	if(ids.length==1)
            	{
            		int id = Integer.parseInt(ids[0]);
            		SmsChannel thisSmsChannel = smsChannelService.getById(id);
            		if(thisSmsChannel!=null)
            		{
            			//仅当通道状态为“停止”状态时，才可作废。确保通道连接已关闭
            			if(thisSmsChannel.getState() == ChannelStateEnums.STOP.getCode())
            			{
            				this.smsChannelService.updateSmsChannelStatusForInvalid(id);
            				initDataCacheSmsChannel("gateway");
            			}
            			else
            			{
            				return new FebsResponse().fail().message("仅当通道状态为“停止”状态时，才可作废！");
            			}
            		}
            		else
            		{
            			return new FebsResponse().fail().message("验证所选通道信息失败！");
            		}
            	}
            	else
            	{
            		return new FebsResponse().fail().message("单次只能操作一个通道！");
            	}
            }

        } catch (ServiceException e) {
            return new FebsResponse().fail().message(e.getMessage());
        }
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "修改SmsChannel", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("smsChannel:export")
    public void export(QueryRequest queryRequest, SmsChannel smsChannel, HttpServletResponse response) {
        List<SmsChannel> smsChannels = this.smsChannelService.findSmsChannels(queryRequest, smsChannel).getRecords();
        ExcelKit.$Export(SmsChannel.class, response).downXlsx(smsChannels, false);
    }

    @ControllerEndpoint(operation = "测试短信通道", exceptionMessage = "测试失败")
    @PostMapping("test/{channelCode}")
    @ResponseBody
    @RequiresPermissions("smsChannel:test")
    public FebsResponse testSmsChannel(@PathVariable String channelCode
            ,SendRecord sendRecord){

        try {
            this.smsChannelService.testSmsChannel(channelCode,sendRecord);
        } catch (ServiceException e) {
            log.error("测试通道失败",e.getMessage());
            return new FebsResponse().fail().message(e.getMessage());
        }
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "修改SmsChannel权重", exceptionMessage = "修改SmsChannel权重失败")
    @PostMapping("weightSave")
    @ResponseBody
    @RequiresPermissions("smsChannel:updateWeight")
    public FebsResponse updateSmsChannelWeight(SmsChannel smsChannel) {
        try {
            this.smsChannelService.updateSmsChannel(smsChannel);
        } catch (ServiceException e) {
            return new FebsResponse().fail().message(e.getMessage());
        }
        return new FebsResponse().success();
    }
    
    public void initDataCacheSmsChannel(String projectName)
    {
    	try {
    		databaseCache.initSmsChannel();
        	businessManage.reladProjectCacheForModule(projectName,String.valueOf(ModuleTypeEnums.SmsChannelList.getCode().intValue()));
		} catch (Exception e) {}
    }
    
    @ControllerEndpoint(operation = "刷新通道列表缓存", exceptionMessage = "刷新通道列表缓存失败")
    @GetMapping("reloadChannelCache")
    @ResponseBody
    @RequiresPermissions("smsChannel:reloadChannelCache")
    public FebsResponse reloadChannelCache(){
        try {
        	initDataCacheSmsChannel("merch,agent,gateway");
		} catch (Exception e) {
			return new FebsResponse().message("重载通道列表缓存失败，请手动清除缓存！");
		}
        return new FebsResponse().success();
    }
}
