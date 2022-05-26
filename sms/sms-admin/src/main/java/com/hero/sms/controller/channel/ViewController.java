package com.hero.sms.controller.channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.channel.GatewayConnectInfo;
import com.hero.sms.entity.channel.NumberPool;
import com.hero.sms.entity.channel.NumberPoolGroup;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.channel.SmsChannelCost;
import com.hero.sms.entity.channel.SmsChannelGroup;
import com.hero.sms.entity.channel.SmsChannelProperty;
import com.hero.sms.entity.common.Code;
import com.hero.sms.enums.channel.ChannelStateEnums;
import com.hero.sms.enums.channel.NumberPoolPolicyEnums;
import com.hero.sms.enums.channel.SmsChannelProtocolTypeEnums;
import com.hero.sms.enums.channel.SmsChannelSubmitWayEnums;
import com.hero.sms.enums.common.OperatorEnums;
import com.hero.sms.enums.common.SwitchEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.channel.IAreaCodeService;
import com.hero.sms.service.channel.INumberPoolGroupService;
import com.hero.sms.service.channel.INumberPoolService;
import com.hero.sms.service.channel.IPayChannelService;
import com.hero.sms.service.channel.ISmsChannelCostService;
import com.hero.sms.service.channel.ISmsChannelGroupService;
import com.hero.sms.service.channel.ISmsChannelPropertyService;
import com.hero.sms.service.channel.ISmsChannelService;

@Controller("channelView")
public class ViewController extends BaseController {

	@Autowired
	private ISmsChannelService smsChannelService;

	@Autowired
	private ISmsChannelCostService channelCostService;

	@Autowired
	private ISmsChannelPropertyService channelPropertyService;

	@Autowired
	private INumberPoolService numberPoolService;

	@Autowired
	private INumberPoolGroupService numberPoolGroupService;

	@Autowired
	private IPayChannelService payChannelService;

	@Autowired
	private ISmsChannelGroupService smsChannelGroupService;
	
	@Autowired
	private IAreaCodeService areaCodeService;

	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannel/list")
	@RequiresPermissions("smsChannel:list")
	public String smsChannelIndex(Model model) {
		model.addAttribute("smsChannelProtocolTypeEnums", SmsChannelProtocolTypeEnums.values());
		model.addAttribute("channelStateEnums", ChannelStateEnums.values());
		List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
	    if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
	    }
	    else
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
	    }
	    model.addAttribute("smsChannelSubmitWayEnums", SmsChannelSubmitWayEnums.values());
		return FebsUtil.view("smsChannel/smsChannel");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannel/add")
	@RequiresPermissions("smsChannel:add")
	public String smsChannelAdd(Model model) {
		model.addAttribute("smsChannelProtocolTypeEnums", SmsChannelProtocolTypeEnums.values());
		model.addAttribute("channelStateEnums", ChannelStateEnums.values());
		List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
	    if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
	    }
	    else
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
	    }
	    model.addAttribute("smsChannelSubmitWayEnums", SmsChannelSubmitWayEnums.values());
		return FebsUtil.view("smsChannel/smsChannelAdd");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannel/update/{id}")
	@RequiresPermissions("smsChannel:update")
	public String smsChannelUpdate(Model model, @PathVariable Integer id) {
		SmsChannel smsChannel = smsChannelService.getById(id);
		model.addAttribute("smsChannel", smsChannel);
		model.addAttribute("smsChannelProtocolTypeEnums", SmsChannelProtocolTypeEnums.values());
		model.addAttribute("channelStateEnums", ChannelStateEnums.values());
		List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
	    if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
	    }
	    else
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
	    }
	    model.addAttribute("smsChannelSubmitWayEnums", SmsChannelSubmitWayEnums.values());
		return FebsUtil.view("smsChannel/smsChannelUpdate");
	}

	@ControllerEndpoint(operation = "获取通道连接信息", exceptionMessage = "获取通道连接信息失败")
	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannel/getConnectInfo/{id}")
	@RequiresPermissions("smsChannel:getConnectInfo")
	public String getConnectInfo(Model model,@PathVariable Integer id) {
		try {
			List<GatewayConnectInfo> list = this.smsChannelService.getGatewayConnectInfos(id);
			if (list.size()<1) model.addAttribute("errorMsg","通道端口未开启");
			else model.addAttribute("connectInfos",list);
		} catch (ServiceException e) {
			model.addAttribute("errorMsg",e.getMessage());
		}
		return FebsUtil.view("smsChannel/connectInfo");
	}


	@GetMapping(FebsConstant.VIEW_PREFIX + "numberPool/list")
	@RequiresPermissions("numberPool:list")
	public String numberPoolIndex(Model model) {
		List<NumberPoolGroup> list = numberPoolGroupService.list();
		model.addAttribute("groupList", list);
		return FebsUtil.view("smsChannel/numberPool");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "numberPool/add")
	@RequiresPermissions("numberPool:add")
	public String numberPoolAdd(Model model) {
		List<NumberPoolGroup> list = numberPoolGroupService.list();
		model.addAttribute("groupList", list);
		return FebsUtil.view("smsChannel/numberPoolAdd");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "numberPool/import")
	@RequiresPermissions("numberPool:import")
	public String numberPoolImport(Model model) {
		List<NumberPoolGroup> list = numberPoolGroupService.list();
		model.addAttribute("groupList", list);
		return FebsUtil.view("smsChannel/numberPoolImport");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "numberPool/update/{id}")
	@RequiresPermissions("numberPool:update")
	public String numberPoolUpdate(Model model, @PathVariable Integer id) {
		List<NumberPoolGroup> list = numberPoolGroupService.list();
		model.addAttribute("groupList", list);
		NumberPool numberPool = numberPoolService.getById(id);
		model.addAttribute("numberPool", numberPool);
		model.addAttribute("switchEnums", SwitchEnums.values());
		return FebsUtil.view("smsChannel/numberPoolUpdate");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "numberPoolGroup/list")
	@RequiresPermissions("numberPoolGroup:list")
	public String numberPoolGroupIndex(Model model) {
		return FebsUtil.view("smsChannel/numberPoolGroup");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "numberPoolGroup/add")
	@RequiresPermissions("numberPoolGroup:add")
	public String numberPoolGroupAdd(Model model) {
		return FebsUtil.view("smsChannel/numberPoolGroupAdd");
	}


	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannel/test/{smsChannelCode}")
	@RequiresPermissions("smsChannel:test")
	public String smsChannelUpdate(Model model, @PathVariable String smsChannelCode) {
		model.addAttribute("smsChannelCode", smsChannelCode);
		List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
	    if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
	    }
	    else
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
	    }
		return FebsUtil.view("smsChannel/smsChannelTest");
	}

	//通道资费
	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannel/cost/list/{channelId}")
	@RequiresPermissions("smsChannelCost:list")
	public String smsChannelCostIndex(Model model, @PathVariable Integer channelId) {
		model.addAttribute("channelId", channelId);
		model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
		List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
	    if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
	    }
	    else
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
	    }
	    model.addAttribute("operatorEnums", OperatorEnums.values());
		return FebsUtil.view("smsChannel/smsChannelCost");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannelCost/add/{channelId}")
	@RequiresPermissions("smsChannelCost:add")
	public String smsChannelCostAdd(Model model, @PathVariable Integer channelId) {
		model.addAttribute("channelId", channelId);
		model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
		List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
	    if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
	    }
	    else
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
	    }
	    model.addAttribute("operatorEnums", OperatorEnums.values());
		return FebsUtil.view("smsChannel/smsChannelCostAdd");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannelCost/update/{id}")
	@RequiresPermissions("smsChannelCost:update")
	public String smsChannelCostUpdate(Model model, @PathVariable Integer id) {
		SmsChannelCost smsChannelCost = channelCostService.getById(id);
		model.addAttribute("smsChannelCost", smsChannelCost);
		model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
		List<AreaCode> smsNumberAreaCode = DatabaseCache.getAreaCodeList();
	    if(smsNumberAreaCode!=null&&smsNumberAreaCode.size()>0)
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
	    }
	    else
	    {
	    	model.addAttribute("smsNumberAreaCodeEnums", SmsNumberAreaCodeEnums.values());
	    }
	    model.addAttribute("operatorEnums", OperatorEnums.values());
		return FebsUtil.view("smsChannel/smsChannelCostUpdate");
	}

	//通道属性
	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannel/properties/{id}/{protocolType}")
	@RequiresPermissions("smsChannelProperty:update")
	public String smsChannelPropertiesUpdate(Model model, @PathVariable Integer id, @PathVariable String protocolType) {
		resolveChannelPropertiesModel(id, model);
		if (protocolType.equals(SmsChannelProtocolTypeEnums.Http.getCode())) {
			return FebsUtil.view("smsChannel/smsChannelPropertyUpdate");
		} else if (protocolType.equals(SmsChannelProtocolTypeEnums.Smpp.getCode())) {
			List<NumberPoolGroup> list = numberPoolGroupService.list();
			model.addAttribute("groupList", list);
			model.addAttribute("numberPoolPolicyEnums", NumberPoolPolicyEnums.values());
			return FebsUtil.view("smsChannel/smsChannelSmppPropertyUpdate");
		} else {
			return FebsUtil.view("systemView/404");
		}

	}

	private void resolveChannelPropertiesModel(int id, Model model) {
		List<SmsChannelProperty> list = channelPropertyService.queryListByChannelId(id);
		model.addAttribute("channelProperties", list);
		model.addAttribute("smsChannelId", id);
	}

	//支付通道
	@GetMapping(FebsConstant.VIEW_PREFIX + "payChannel/payChannel")
	@RequiresPermissions("payChannel:list")
	public String payChannel(Model model) {
		model.addAttribute("orgStatusEnums", OrgStatusEnums.values());
		model.addAttribute("channelStateEnums", ChannelStateEnums.values());
		return FebsUtil.view("payChannel/payChannel");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "payChannel/add")
	@RequiresPermissions("payChannel:add")
	public String payChannelAdd(Model model) {
		model.addAttribute("channelStateEnums", ChannelStateEnums.values());
		return FebsUtil.view("payChannel/payChannelAdd");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "payChannel/update/{id}")
	@RequiresPermissions("payChannel:update")
	public String payChannelAdd(@PathVariable String id, Model model) {
		resolvePayChannelModel(id, model);
		return FebsUtil.view("payChannel/payChannelUpdate");
	}

	private void resolvePayChannelModel(String id, Model model) {
		PayChannel payChannel = payChannelService.getById(id);
		model.addAttribute("payChannel", payChannel);
		model.addAttribute("channelStateEnums", ChannelStateEnums.values());
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannel/updateGroup/{smsChannelId}")
	@RequiresPermissions("smsChannel:updateGroup")
	public String updateGroup(@PathVariable String smsChannelId, Model model) {
		Map<String,Object> data = new HashMap<>();
		data.put("smsChannelId",smsChannelId);

		//获取已分组数据
		LambdaQueryWrapper<SmsChannelGroup> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SmsChannelGroup::getSmsChannelId,smsChannelId);
		Map<String, Integer> groupMap = smsChannelGroupService.list(queryWrapper).stream()
				.collect(Collectors.toMap(SmsChannelGroup::getGroupId, SmsChannelGroup::getSmsChannelId));

		//获取所有分组
		List<Code> list = DatabaseCache.getCodeListBySortCode("OrgGroup");
		List<Map> smsChannelGroupList = Lists.newArrayList();
		list.stream().forEach(item -> {
			Map<String,String> map = new HashMap<>();
			map.put("name",item.getName());
			map.put("value",item.getCode());

			String selected = "";
			if (groupMap.containsKey(item.getCode())){
				selected = "selected";
			}
			map.put("selected",selected);
			smsChannelGroupList.add(map);
		});
		data.put("smsChannelGroupList",smsChannelGroupList);

		model.addAttribute("data",data);

		return FebsUtil.view("smsChannel/smsChannelGroupUpdate");
	}
	
	@GetMapping(FebsConstant.VIEW_PREFIX + "smsChannel/updateWeight/{smsChannelId}")
	@RequiresPermissions("smsChannel:updateWeight")
	public String updateWeight(@PathVariable String smsChannelId, Model model) {
		SmsChannel smsChannel = smsChannelService.getById(smsChannelId);
		model.addAttribute("smsChannel", smsChannel);
		return FebsUtil.view("smsChannel/smsChannelWeightUpdate");
	}
	
	@GetMapping(FebsConstant.VIEW_PREFIX + "areaCode/list")
	@RequiresPermissions("areaCode:list")
	public String areaCodeIndex(Model model) {
		return FebsUtil.view("smsChannel/areaCode");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "areaCode/add")
	@RequiresPermissions("areaCode:add")
	public String areaCodeAdd(Model model) {
		return FebsUtil.view("smsChannel/areaCodeAdd");
	}

	@GetMapping(FebsConstant.VIEW_PREFIX + "areaCode/update/{id}")
	@RequiresPermissions("areaCode:update")
	public String areaCodeUpdate(Model model, @PathVariable Integer id) {
		AreaCode areaCode = areaCodeService.getById(id);
		model.addAttribute("areaCode", areaCode);
		return FebsUtil.view("smsChannel/areaCodeUpdate");
	}
}