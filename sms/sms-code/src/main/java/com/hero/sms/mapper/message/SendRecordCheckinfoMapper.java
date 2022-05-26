package com.hero.sms.mapper.message;


import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hero.sms.entity.message.SendRecordCheckinfo;
import com.hero.sms.entity.message.SendRecordCheckinfoQuery;

/**
 * 发送记录提交中间校验表 Mapper
 *
 * @author Administrator
 * @date 2020-12-21 17:46:28
 */
public interface SendRecordCheckinfoMapper extends BaseMapper<SendRecordCheckinfo> 
{
	void updateSendRecordCheckinfoBatch(@Param("srci") SendRecordCheckinfoQuery srci);
}
