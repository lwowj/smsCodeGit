package com.hero.sms.mapper.message;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.history.ReturnRecordHistory;
import com.hero.sms.entity.message.history.SendBoxHistory;
import com.hero.sms.entity.message.history.SendRecordHistory;

@Mapper
public interface ReportMapper {


    List<Map<String,Object>> statisticOrgSendCountToday();

    List<Map<String, Object>> provinceSendCountToday();

    List<Map<String, Object>> operatorSendCountToday();

    /**
     * 实时统计发件箱信息
     * @return
     */
    Map<String, Object> statisticSendBoxInfo(@Param("ew") LambdaQueryWrapper<SendBox> ew);

    /**
     * 实时统计历史发件箱信息
     * @return
     */
    Map<String, Object> statisticSendBoxHistoryInfo(@Param("ew") LambdaQueryWrapper<SendBoxHistory> ew);

    /**
     * 实时统计发送记录信息
     * @return
     */
    Map<String, Object> statisticSendRecordInfo(@Param("ew") LambdaQueryWrapper<SendRecord> ew);

    /**
     * 实时统计历史发送记录信息
     * @return
     */
    Map<String, Object> statisticSendRecordHistoryInfo(@Param("ew") LambdaQueryWrapper<SendRecordHistory> ew);

    /**
     * 实时统计发送回执信息
     * @return
     */
    Map<String, Object> statisticReturnRecordInfo(@Param("ew") LambdaQueryWrapper<ReturnRecord> ew);

    /**
     * 实时统计历史发送回执信息
     * @return
     */
    Map<String, Object> statisticReturnRecordHistoryInfo(@Param("ew") LambdaQueryWrapper<ReturnRecordHistory> ew);

    /**
     * 代理后台实时短信订单情况
     * @param wrapper
     * @return
     */
    List<Map<String, Object>> sumSendRecordByAgent(@Param(Constants.WRAPPER) Wrapper wrapper);

    /**
     * 批次成功率统计
     * @param page
     * @param ew （只用where 条件构造 禁用 groupby  orderby构造）
     * @return
     */
    IPage<Map<String, Object>> statisticRateSuccessGroupBySendCode(IPage<Map<String, Object>> page,@Param("ew") QueryWrapper<SendRecord> ew,@Param("groupId")Integer groupId);

    /**
     * 批次成功率统计(不分页)
     * @param ew （只用where 条件构造 禁用 groupby  orderby构造）
     * @return
     */
    List<Map<String, Object>> statisticRateSuccessGroupBySendCode(@Param("ew") QueryWrapper<SendRecord> ew,@Param("groupId")Integer groupId);

}
