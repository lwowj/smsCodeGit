package com.hero.sms.mapper.channel;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hero.sms.entity.channel.SmsChannelProperty;

/**
 * 短信通道属性 Mapper
 *
 * @author Administrator
 * @date 2020-03-08 17:35:16
 */
public interface SmsChannelPropertyMapper extends BaseMapper<SmsChannelProperty> {

	List<SmsChannelProperty> selectByChannelCode(@Param("channelCode")String channelCode);

}
