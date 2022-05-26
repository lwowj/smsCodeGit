package com.hero.sms.mapper.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.entity.message.StatisticalChannel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 按照通道统计 Mapper
 *
 * @author Administrator
 * @date 2020-05-28 15:59:07
 */
public interface StatisticalChannelMapper extends BaseMapper<StatisticalChannel> {

    List<Map<String,Object>> sumStatisticalChannels(@Param("ew") LambdaQueryWrapper<StatisticalChannel> ew);
}
