package com.hero.sms.service.impl.channel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.service.RedisHelper;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.RegexUtil;
import com.hero.sms.entity.channel.GatewayConnectInfo;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.channel.SmsChannelExtGroup;
import com.hero.sms.entity.channel.SmsChannelQuery;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.channel.ChannelStateEnums;
import com.hero.sms.enums.channel.SmsChannelProtocolTypeEnums;
import com.hero.sms.mapper.channel.SmsChannelMapper;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.service.impl.channel.push.BaseSmsPushService;

/**
 * 短信通道 Service实现
 *
 * @author Administrator
 * @date 2020-03-08 17:35:03
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SmsChannelServiceImpl extends ServiceImpl<SmsChannelMapper, SmsChannel> implements ISmsChannelService {

    @Autowired
    private SmsChannelMapper smsChannelMapper;
    
    @Autowired
    private IBusinessManage businessManage;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public IPage<SmsChannel> findSmsChannels(QueryRequest request, SmsChannel smsChannel) {
        LambdaQueryWrapper<SmsChannel> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        queryWrapper.eq(SmsChannel::getIsDel, 0);
        if (StringUtils.isNotBlank(smsChannel.getProtocolType())){
            queryWrapper.eq(SmsChannel::getProtocolType,smsChannel.getProtocolType());
        }
        if (smsChannel.getState() != null){
            queryWrapper.eq(SmsChannel::getState,smsChannel.getState());
        }
        if(smsChannel.getStateWith() != null) {
        	queryWrapper.apply("state&{0}=state",smsChannel.getStateWith());
        }
        if (smsChannel.getSubmitWay() != null){
            queryWrapper.eq(SmsChannel::getSubmitWay,smsChannel.getSubmitWay());
        }
        Page<SmsChannel> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }
    @Override
    public IPage<SmsChannelExtGroup> findSmsChannelsExtGroups(QueryRequest request, SmsChannelQuery smsChannel) {
        Page<SmsChannelExtGroup> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<SmsChannelExtGroup> result = this.baseMapper.findListContainGroups(page, smsChannel);
        result.getRecords().forEach(item ->{
            String key = item.getId() + "_" + DateUtil.getString(new Date(),"yyyyMMdd");
            Integer daySendNum = (Integer)redisHelper.get(key);
            item.setDaySendNum(daySendNum==null?0:daySendNum);
        });
        return result;
    }


    @Override
    public List<SmsChannel> findSmsChannels(SmsChannel smsChannel) {
	    LambdaQueryWrapper<SmsChannel> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
	    queryWrapper.eq(SmsChannel::getIsDel, 0);
	    if (StringUtils.isNotBlank(smsChannel.getProtocolType())){
            queryWrapper.eq(SmsChannel::getProtocolType,smsChannel.getProtocolType());
        }
	    if(smsChannel.getState() != null) {
	    	queryWrapper.eq(SmsChannel::getState, smsChannel.getState());
	    }
	    if(smsChannel.getStateWith() != null) {
        	queryWrapper.apply("state&{0}=state",smsChannel.getStateWith());
        }
        if (smsChannel.getSubmitWay() != null){
            queryWrapper.eq(SmsChannel::getSubmitWay,smsChannel.getSubmitWay());
        }
	    if(StringUtils.isNotBlank(smsChannel.getSupportArea())) {
	    	queryWrapper.and(
	    			wrapper -> 
	    			wrapper.eq(SmsChannel::getSupportArea, smsChannel.getSupportArea())
	    			.or()
	    			.isNull(SmsChannel::getSupportArea)
	    			.or().eq(SmsChannel::getSupportArea, "")
	    			);
	    }
		return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 关联资费表的地域
     * 2022-03-22
     * @param smsChannel
     * @return
     */
    @Override
    public List<SmsChannel> findSmsChannelsForArea(SmsChannel smsChannel) {
		return this.baseMapper.findSmsChannelsForArea(smsChannel);
    }
    
    @Override
    public List<SmsChannelExt> findListContainProperty(SmsChannel smsChannel) {
    	return smsChannelMapper.findListContainProperty(smsChannel);
    }

    @Override
    @Transactional
    public void createSmsChannel(SmsChannel smsChannel) throws ServiceException {
        //必填项检测
        if (StringUtils.isBlank(smsChannel.getName())){
            throw new ServiceException("通道名称不能为空！");
        }
        if (StringUtils.isBlank(smsChannel.getImplFullClass())){
            throw new ServiceException("通道实现类不能为空！");
        }
        if (StringUtils.isBlank(smsChannel.getCode())){
            throw new ServiceException("通道代码不能为空！");
        }
        /**
         * @begin 2021-01-23
         * 新增限制的格式校验
         */
        if (StringUtils.isNotBlank(smsChannel.getAreaRegex()))
        {
        	if(!RegexUtil.isNumberLimit(smsChannel.getAreaRegex()))
        	{
        		throw new ServiceException("地区限制：表达式格式不正确！");
        	}
        }
        if (StringUtils.isNotBlank(smsChannel.getOperatorRegex()))
        {
        	if(!RegexUtil.isNumberLimit(smsChannel.getOperatorRegex()))
        	{
        		throw new ServiceException("运营商限制：表达式格式不正确！");
        	}
        }
        if (StringUtils.isNotBlank(smsChannel.getAreaOperatorRegex()))
        {
        	if(!RegexUtil.isNumberLimit(smsChannel.getAreaOperatorRegex()))
        	{
        		throw new ServiceException("组合限制：表达式格式不正确！");
        	}
        }
        /**
         * @end
         */
        if (smsChannel.getState() == null){
            //默认关闭
            smsChannel.setState(ChannelStateEnums.STOP.getCode());
        }
        if (StringUtils.isBlank(smsChannel.getProtocolType())){
            throw new ServiceException("协议类型不能为空！");
        }
        //判断code是否存在   确保唯一
        LambdaQueryWrapper<SmsChannel> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(SmsChannel::getCode,smsChannel.getCode());
        int codeCount = count(countWrapper);
        if (codeCount > 0){
            throw new ServiceException("通道代码已存在，添加失败");
        }

        if (smsChannel.getSubmitWay() == null)
        {
            smsChannel.setSubmitWay(0);
        }
       /* //根据  协议类型的实现类  判断通道是否已经存在
        LambdaQueryWrapper<SmsChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsChannel::getProtocolType,smsChannel.getProtocolType())
                .eq(SmsChannel::getImplFullClass, smsChannel.getImplFullClass());
        int count = count(wrapper);
        if (count > 0){
            throw new ServiceException("通道已经存在,请勿重新添加");
        }*/
        smsChannel.setIsDel(0);
        smsChannel.setCreateTime(new Date());
        this.save(smsChannel);
    }

    @Override
    @Transactional
    public void updateSmsChannel(SmsChannel smsChannel) throws ServiceException {
        if (smsChannel.getId() == null){
            throw new ServiceException("通道ID不能为空");
        }
        //必填项检测
        if (StringUtils.isBlank(smsChannel.getName())){
            throw new ServiceException("通道名称不能为空！");
        }
        if (StringUtils.isBlank(smsChannel.getImplFullClass())){
            throw new ServiceException("通道实现类不能为空！");
        }
        if (StringUtils.isBlank(smsChannel.getCode())){
            throw new ServiceException("通道代码不能为空！");
        }
        /**
         * @begin 2021-01-23
         * 新增限制的格式校验
         */
        if (StringUtils.isNotBlank(smsChannel.getAreaRegex()))
        {
        	if(!RegexUtil.isNumberLimit(smsChannel.getAreaRegex()))
        	{
        		throw new ServiceException("地区限制：表达式格式不正确！");
        	}
        }
        if (StringUtils.isNotBlank(smsChannel.getOperatorRegex()))
        {
        	if(!RegexUtil.isNumberLimit(smsChannel.getOperatorRegex()))
        	{
        		throw new ServiceException("运营商限制：表达式格式不正确！");
        	}
        }
        if (StringUtils.isNotBlank(smsChannel.getAreaOperatorRegex()))
        {
        	if(!RegexUtil.isNumberLimit(smsChannel.getAreaOperatorRegex()))
        	{
        		throw new ServiceException("组合限制：表达式格式不正确！");
        	}
        }
        /**
         * @end
         */
        if (smsChannel.getState() == null){
            //默认关闭
            smsChannel.setState(ChannelStateEnums.STOP.getCode());
        }
        if (StringUtils.isBlank(smsChannel.getProtocolType())){
            throw new ServiceException("协议类型不能为空！");
        }
        //判断code是否存在   确保唯一
        LambdaQueryWrapper<SmsChannel> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(SmsChannel::getCode,smsChannel.getCode());
        countWrapper.ne(SmsChannel::getId,smsChannel.getId());
        int codeCount = count(countWrapper);
        if (codeCount > 0){
            throw new ServiceException("通道代码已存在，更新失败");
        }

        if (smsChannel.getSubmitWay() == null)
        {
            smsChannel.setSubmitWay(0);
        }
        //根据  协议类型的实现类  判断通道是否已经存在
        /*LambdaQueryWrapper<SmsChannel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsChannel::getProtocolType,smsChannel.getProtocolType())
                .eq(SmsChannel::getImplFullClass, smsChannel.getImplFullClass())
                .ne(SmsChannel::getId,smsChannel.getId());
        int count = count(wrapper);
        if (count > 0){
            throw new ServiceException("通道信息与其他通道重复【协议类型&实现类唯一限制】,更新失败");
        }*/
        this.saveOrUpdate(smsChannel);
        if(!smsChannel.getProtocolType().equals(SmsChannelProtocolTypeEnums.Http.getCode())) {
        	String data = null; 
        	if(smsChannel.getState().intValue() != ChannelStateEnums.STOP.getCode()) {
        		data = JSON.toJSONString(findContainPropertyById(smsChannel.getId()));
        	}else {
        		data = JSON.toJSONString(smsChannel);
        	}
        	List<FebsResponse> constrolSmsGate = businessManage.serverConstrolSmsGate(data);
        	if(CollectionUtils.isNotEmpty(constrolSmsGate)) {
        		StringBuilder error = new StringBuilder();
        		for (FebsResponse febsResponse : constrolSmsGate) {
        			if(((Integer)febsResponse.get("code")).intValue() != HttpStatus.OK.value()) {
        				error.append(febsResponse.get("message"));
        			}
        		}
        		if(error.length() > 0) {
        			throw new ServiceException("刷新网关能控制失败:"+error);
        		}
        	}
        }
    }
    @Override
    public List<GatewayConnectInfo> getGatewayConnectInfos(Integer id) throws ServiceException{
        List<GatewayConnectInfo> list = null;
        if (id == null) return null;
        String data = JSON.toJSONString(findContainPropertyById(id));
        List<FebsResponse> gatewayConnectInfo = businessManage.serverGetSmsGateConnectInfo(data);
        if(CollectionUtils.isNotEmpty(gatewayConnectInfo)) {
            list = Lists.newArrayList();
            for (FebsResponse febsResponse : gatewayConnectInfo) {
                if (((Integer)febsResponse.get("code")).intValue() == HttpStatus.OK.value()){
                    JSONObject responseData = (JSONObject) febsResponse.get("data");
                    GatewayConnectInfo info = JSONObject.parseObject(responseData.toJSONString(), GatewayConnectInfo.class);

                    list.add(info);
                }
            }
        }
        return list;
    }

    @Override
    @Transactional
    public void deleteSmsChannel(SmsChannel smsChannel) {
        LambdaQueryWrapper<SmsChannel> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSmsChannels(String[] smsChannelIds) {
        //删除主表
        List<String> list = Arrays.asList(smsChannelIds);
        this.removeByIds(list);

        //删除从表
    }

    @Override
    public SmsChannelExt findContainPropertyById(Integer id) {
        return this.baseMapper.findContainPropertyById(id);
    }

	@Override
	public SmsChannelExt findContainPropertyByCode(String code) {
		return this.baseMapper.findContainPropertyByCode(code);
	}

	@Override
	public SmsChannel getByCode(String code) {
		LambdaQueryWrapper<SmsChannel> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SmsChannel::getCode, code);
		return this.getOne(queryWrapper, true);
	}

    @Override
    public FebsResponse testSmsChannel(String channelCode, SendRecord sendRecord) throws ServiceException {
        if (StringUtils.isBlank(sendRecord.getSmsNumber())){
            throw new ServiceException("手机号码不能为空");
        }
        if (StringUtils.isBlank(sendRecord.getSmsContent())){
            throw new ServiceException("短信内容不能为空");
        }
        if (StringUtils.isBlank(sendRecord.getSmsNumberArea())){
            throw new ServiceException("号码区域不能为空");
        }
        SmsChannelExt smsChannelExt = this.findContainPropertyByCode(channelCode);
        if(smsChannelExt == null) {
            throw new ServiceException(String.format("短信通道【%s】不存在",channelCode));
        }
        BaseSmsPushService pushService = null;
        try {
            pushService = (BaseSmsPushService) Class.forName(smsChannelExt.getImplFullClass()).newInstance();
            pushService.init(smsChannelExt);
        } catch (Exception e) {
            String errInfo = String.format("测试通道，初始化通道【%s】失败",channelCode);
            log.error(errInfo,e);
            throw new ServiceException(errInfo);
        }
        boolean result = pushService.push(sendRecord);

        String desc = sendRecord.getStateDesc();
        if (result){
            return new FebsResponse().success().message(desc);
        }
        return new FebsResponse().fail().message(desc);
    }

    @Override
    public void updateSmsChannelStatus(Integer channelId, Integer state) throws ServiceException {

        SmsChannel smsChannel = getById(channelId);
        if (smsChannel == null)
            throw new ServiceException("通道不存在！");

        //更新网关状态
        LambdaUpdateWrapper<SmsChannel> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SmsChannel::getState,state)
                .eq(SmsChannel::getId,channelId);
        update(updateWrapper);

        if (smsChannel.getProtocolType().equals(SmsChannelProtocolTypeEnums.Smpp.getCode())){
            //smpp   操作上游网关状态
            String data = null;
            JSONObject obj = new JSONObject();
            obj.put("channelId",channelId);
            obj.put("state",state);
            data = obj.toJSONString();
            List<FebsResponse> constrolSmsGate = businessManage.switchSmsGateSender(data);
            if(CollectionUtils.isNotEmpty(constrolSmsGate)) {
                StringBuilder error = new StringBuilder();
                for (FebsResponse febsResponse : constrolSmsGate) {
                    if(((Integer)febsResponse.get("code")).intValue() != HttpStatus.OK.value()) {
                        error.append(febsResponse.get("message"));
                    }
                }
                if(error.length() > 0) {
                    throw new ServiceException("刷新网关【"+channelId+"】控制失败:"+error);
                }
            }
        }

    }
    
    @Override
    public void updateSmsChannelStatusForInvalid(Integer channelId) throws ServiceException {

        SmsChannel smsChannel = getById(channelId);
        if (smsChannel == null)
            throw new ServiceException("通道不存在！");

        //更新网关状态
        LambdaUpdateWrapper<SmsChannel> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SmsChannel::getState,ChannelStateEnums.INVALID.getCode())
        		.eq(SmsChannel::getState,ChannelStateEnums.STOP.getCode())
                .eq(SmsChannel::getId,channelId);
        update(updateWrapper);
    }
}
