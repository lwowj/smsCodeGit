package com.hero.sms.mapper.channel;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.channel.SmsChannelExtGroup;
import com.hero.sms.entity.channel.SmsChannelQuery;
import org.apache.ibatis.annotations.Param;

/**
 * 短信通道 Mapper
 *
 * @author Administrator
 * @date 2020-03-08 17:35:03
 */
public interface SmsChannelMapper extends BaseMapper<SmsChannel> {

	List<SmsChannelExt> findListContainProperty(SmsChannel smsChannel);

	SmsChannelExt findContainPropertyById(Integer id);

	SmsChannelExt findContainPropertyByCode(String code);


	IPage<SmsChannelExtGroup> findListContainGroups(Page<SmsChannelExtGroup> page, @Param("scq") SmsChannelQuery smsChannelQuery);
	
	List<SmsChannel> findSmsChannelsForArea(SmsChannel smsChannel);

}
