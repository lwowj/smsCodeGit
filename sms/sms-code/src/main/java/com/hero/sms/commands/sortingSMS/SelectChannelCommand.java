package com.hero.sms.commands.sortingSMS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.chain.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.service.RedisHelper;
import com.hero.sms.common.utils.AppUtil;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.channel.SmsChannelGroup;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.entity.organization.OrganizationGroup;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.service.channel.ISmsChannelGroupService;
import com.hero.sms.service.organization.IOrganizationGroupService;

public class SelectChannelCommand extends BaseCommand {
	
    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {
        SendRecord sendRecord = (SendRecord) context.get(OBJ_SAVE_SENDRECORD_ENTITY);
        List<SmsChannel> channels = (List<SmsChannel>) context.get(LIST_SMS_CHANNEL);
        OrganizationCost organizationCost = (OrganizationCost) context.get(OBJ_ORG_COST);
		ISmsChannelGroupService smsChannelGroupService = SpringContextUtil.getBean(ISmsChannelGroupService.class);
		IOrganizationGroupService organizationGroupService = SpringContextUtil.getBean(IOrganizationGroupService.class);
		String smsNumberProvinceCode = sendRecord.getSmsNumberProvince();
        String smsNumberProvinceName = null;
        String smsNumberOperatorCode = sendRecord.getSmsNumberOperator();
        String smsNumberOperatorName = null;
        if(StringUtils.isNotBlank(smsNumberProvinceCode)) {
        	smsNumberProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(smsNumberProvinceCode);
        }
        if(StringUtils.isNotBlank(smsNumberOperatorCode)) {
        	Code code = DatabaseCache.getCodeBySortCodeAndCodeAndUpCode("PhoneOperator", smsNumberOperatorCode, sendRecord.getSmsNumberArea());
        	if(code != null) {
        		smsNumberOperatorName = code.getName();
        	}
        }
        
        SmsChannel smsChannel = null;
        if(StringUtils.isNotBlank(organizationCost.getChannelId())) {
        	Optional<SmsChannel> findAny = channels.stream().filter(channel -> channel.getId().intValue() == Integer.parseInt(organizationCost.getChannelId())).findAny();
        	if(findAny == null) {
        		context.put(STR_ERROR_INFO, "指定通道未开启");
        		return true;
        	}
        	if(!findAny.isPresent()) {
        		context.put(STR_ERROR_INFO, "指定通道未开启");
        		return true;
        	}
        	smsChannel = findAny.get();
        	String areaRegex = smsChannel.getAreaRegex();
        	if(StringUtils.isNotBlank(areaRegex)) {
        		boolean match = AppUtil.match(areaRegex, smsNumberProvinceName);
        		if(!match) {
        			context.put(STR_ERROR_INFO, String.format("指定通道不支持该地区【%s】",smsNumberProvinceName));
        			return true;
        		}
        	}
        	String operatorRegex = smsChannel.getOperatorRegex();
        	if(StringUtils.isNotBlank(operatorRegex)) {
        		boolean match = AppUtil.match(operatorRegex, smsNumberOperatorName);
        		if(!match) {
        			context.put(STR_ERROR_INFO, String.format("指定通道不支持该运营商【%s】",smsNumberOperatorName));
        			return true;
        		}
        	}
        	String areaOperatorRegex = smsChannel.getAreaOperatorRegex();
        	if(StringUtils.isNotBlank(areaOperatorRegex)) {
        		boolean match = AppUtil.match(areaOperatorRegex, smsNumberProvinceName+smsNumberOperatorName);
        		if(!match) {
        			context.put(STR_ERROR_INFO, String.format("指定通道不支持地区【%s】运营商【%s】",smsNumberProvinceName,smsNumberOperatorName));
        			return true;
        		}
        	}
        }else {
			//如果商户存在分组，过滤出分组可用的所有通道
			LambdaQueryWrapper<OrganizationGroup> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(OrganizationGroup::getOrgCode,sendRecord.getOrgCode());
			List<OrganizationGroup> orgGroups = organizationGroupService.list(queryWrapper);
			if (CollectionUtils.isNotEmpty(orgGroups)){
				List<String> orgGroupIds = orgGroups.stream().map(OrganizationGroup::getGroupId).collect(Collectors.toList());
				//查询商户所属分组的所有可用通道
				LambdaQueryWrapper<SmsChannelGroup> queryWrapper1 = new LambdaQueryWrapper<>();
				queryWrapper1.in(SmsChannelGroup::getGroupId,orgGroupIds);
				List<SmsChannelGroup> smsChannels = smsChannelGroupService.list(queryWrapper1);
				if (CollectionUtils.isNotEmpty(smsChannels)){
					//过滤出分组通道
					Map<Integer, Integer> smsChannelIdMap = smsChannels.stream().collect(Collectors.toMap(SmsChannelGroup::getSmsChannelId, SmsChannelGroup::getSmsChannelId,(e1,e2)->e1));
					channels = channels.stream().filter( channel -> smsChannelIdMap.containsKey(channel.getId())).collect(Collectors.toList());
				}
			}

			if (CollectionUtils.isEmpty(channels)){
				context.put(STR_ERROR_INFO, "暂无可用通道");
				return true;
			}
			/*
			//如果商户存在分组，过滤出分组可用的所有通道
			LambdaQueryWrapper<OrganizationGroup> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(OrganizationGroup::getOrgCode,sendRecord.getOrgCode());
			List<OrganizationGroup> orgGroups = organizationGroupService.list(queryWrapper);

			List<String> orgGroupIds = orgGroups.stream().map(OrganizationGroup::getGroupId).collect(Collectors.toList());
			//查询商户所属分组的所有可用通道
			List<SmsChannelGroup> smsChannelGroups = smsChannelGroupService.list();
			if (CollectionUtils.isNotEmpty(smsChannelGroups)){
				//过滤出分组通道
				Map<Integer, Integer> smsChannelIdMap = smsChannelGroups.stream()
						.collect(Collectors.toMap(SmsChannelGroup::getSmsChannelId, SmsChannelGroup::getSmsChannelId,
								(e1, e2) -> e1));
				Map<Integer, Integer> orgChannelIdMap = smsChannelGroups.stream().filter(item -> orgGroupIds.contains(item.getGroupId()))
						.collect(Collectors.toMap(SmsChannelGroup::getSmsChannelId, SmsChannelGroup::getSmsChannelId,
								(e1, e2) -> e1));
				smsChannelGroups.removeAll(orgChannelIdMap.keySet());
				channels = channels.stream().filter( channel -> !smsChannelIdMap.containsKey(channel.getId())).collect(Collectors.toList());
			}*/

        	final String provinceName = smsNumberProvinceName;
        	final String operatorName = smsNumberOperatorName;
        		channels = channels.stream().filter(channel -> (
        			(StringUtils.isBlank(channel.getAreaRegex()) || 
        			AppUtil.match(channel.getAreaRegex(), provinceName))
        			&&
        			(StringUtils.isBlank(channel.getOperatorRegex()) || 
        			AppUtil.match(channel.getOperatorRegex(), operatorName))
        			&&
        			(StringUtils.isBlank(channel.getAreaOperatorRegex()) || 
        			AppUtil.match(channel.getAreaOperatorRegex(), provinceName+operatorName))
        		)).collect(Collectors.toList());
        		context.put("channels", channels);
        	if(CollectionUtils.isEmpty(channels)) {
        		context.put(STR_ERROR_INFO, String.format("当前通道不支持该地区【%s】运营商【%s】", smsNumberProvinceName,operatorName));
        		return true;
        	}
        	smsChannel = selectWeightChannel(filterChannel(channels));
        }
        if(smsChannel == null) {
        	context.put(STR_ERROR_INFO, "没有可用通道");
			return true;
        }

        sendRecord.setChannelId(smsChannel.getId());
		//日发送量+1
		RedisHelper redisHelper = SpringContextUtil.getBean(RedisHelper.class);
		String key = smsChannel.getId() + "_" + DateUtil.getString(new Date(),"yyyyMMdd");
		long incrNum = 1L;
		if (sendRecord.getSmsCount() != null){
			incrNum = sendRecord.getSmsCount();
		}
		redisHelper.incr(key,incrNum);
        context.put(OBJ_SMS_CHANNEL, smsChannel);
        return false;
    }
    
    /**
	 * 选择权重通道
	 * 
	 * @param channels
	 * @return
	 */
	public static SmsChannel selectWeightChannel(List<SmsChannel> channels) {
		if (channels == null || channels.isEmpty()) {
			return null;
		}
		List<Pair<SmsChannel, Integer>> weightList = new ArrayList<Pair<SmsChannel, Integer>>();
		for (int i = 0; i < channels.size(); i++) {
			boolean flag = true;
			/**
			 * @modify 2020-12-09
			  * 过滤权重为0的通道
			 */
			String channelWeightNotZeroSwitch = "OFF";
			Code channelWeightNotZeroSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","channelWeightNotZeroSwitch");
		    if(channelWeightNotZeroSwitchCode!=null&&!"".equals(channelWeightNotZeroSwitchCode.getName()))
		    {
		    	channelWeightNotZeroSwitch = channelWeightNotZeroSwitchCode.getName();
		    }
		    //通道权重为0不放入选择开关，若开启，则将权重为0的通道从列表中移除
		    if("ON".equals(channelWeightNotZeroSwitch))
		    {
		    	if(channels.get(i).getWeight()==0)
				{
		    		flag = false;
				}
		    }
			/**
			 * @end
			 */
		    if(flag)
			{
				weightList.add(new Pair<SmsChannel, Integer>(channels.get(i), channels.get(i).getWeight() + 1));
			}
		}
		/**
		 * @modify 2020-12-09
		 * 过滤权重为0的通道
		 */
		if (weightList == null || weightList.isEmpty()) {
			return null;
		}
		/**
		 * @end
		 */
		SmsChannel channel = new MyWeightRandom<SmsChannel, Integer>(weightList).random();
		return channel;
	}

	/**
	 * 过滤掉发送量已经达到日限量的通道
	 * @param channels
	 * @return
	 */
	public static List<SmsChannel> filterChannel(List<SmsChannel> channels){
		RedisHelper redisHelper = SpringContextUtil.getBean(RedisHelper.class);

		List<SmsChannel> useableChannels = channels.stream()
				.filter(channel -> {
					String key = channel.getId() + "_" + DateUtil.getString(new Date(), "yyyyMMdd");
					Integer result = (Integer) redisHelper.get(key);
					if (channel.getDayLimit() == null||channel.getDayLimit() == 0) return true; //日限量不设值  表示无限量
					return result == null || Long.valueOf(result) < channel.getDayLimit();
				}).collect(Collectors.toList());
		return useableChannels;
	}

	static class MyWeightRandom<K, V extends Number> {
		private TreeMap<Double, K> weightMap = new TreeMap<Double, K>();

		public MyWeightRandom(List<Pair<K, V>> list) {
			for (Pair<K, V> pair : list) {
				double lastWeight = this.weightMap.size() == 0 ? 0 : this.weightMap.lastKey().doubleValue();// 统一转为double
				this.weightMap.put(pair.getValue().doubleValue() + lastWeight, pair.getKey());// 权重累加
			}
		}

		public K random() {
			double randomWeight = this.weightMap.lastKey() * Math.random();
			SortedMap<Double, K> tailMap = this.weightMap.tailMap(randomWeight, false);
			return this.weightMap.get(tailMap.firstKey());
		}
	}
	
//	public static void main(String[] args) {
//		List<SmsChannel> list = Lists.newArrayList();
//		SmsChannel smsChannel1 = new SmsChannel();
//		smsChannel1.setId(1);
//		smsChannel1.setWeight(50);
//		smsChannel1.setAreaRegex("^((?!.*(北京|福建)).)*$");
//		SmsChannel smsChannel2 = new SmsChannel();
//		smsChannel2.setId(2);
//		smsChannel2.setWeight(50);
//		SmsChannel smsChannel3 = new SmsChannel();
//		smsChannel3.setId(3);
//		smsChannel3.setWeight(50);
//		smsChannel3.setAreaRegex("^((?!.*(四川|北京)).)*$");
//		SmsChannel smsChannel4 = new SmsChannel();
//		smsChannel4.setId(4);
//		smsChannel4.setWeight(50);
//		smsChannel4.setAreaRegex("^((?!.*(湖北)).)*$");
//		list.add(smsChannel1);
//		list.add(smsChannel2);
//		list.add(smsChannel3);
//		list.add(smsChannel4);
//
//		SelectChannelCommand selectChannelCommand = new SelectChannelCommand();
//		System.out.println(selectChannelCommand.selectWeightChannel(list).getId());
//		
//    	String regex = "^((?!.*(北京|福建)).)*$";
//    	boolean match = AppUtil.match(regex, "四川");
//    	System.out.println(match);
//    	System.out.println("-------------------");
//    	ContextBase context = new ContextBase();
//    	try {
//    		SendRecord sendRecord = new SendRecord();
//	        OrganizationCost organizationCost = new OrganizationCost();
//	        //organizationCost.setChannelId("1");
//	        //sendRecord.setSmsNumberProvince("1");//北京
//	        context.put(OBJ_SAVE_SENDRECORD_ENTITY, sendRecord); 
//	        context.put(LIST_SMS_CHANNEL, list);
//	        context.put(OBJ_ORG_COST, organizationCost);
//			selectChannelCommand.execute(context);
//			System.out.println(list.size());
//			System.out.println(((List)context.get("channels")).size());
//			System.out.println(list.size());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
