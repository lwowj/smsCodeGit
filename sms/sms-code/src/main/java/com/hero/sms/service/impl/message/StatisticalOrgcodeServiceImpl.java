package com.hero.sms.service.impl.message;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.message.StatisticalOrgcode;
import com.hero.sms.entity.message.StatisticalOrgcodeExt;
import com.hero.sms.entity.message.StatisticalOrgcodeQuery;
import com.hero.sms.mapper.message.StatisticalOrgcodeMapper;
import com.hero.sms.service.message.IStatisticalOrgcodeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 按照商户统计 Service实现
 *
 * @author Administrator
 * @date 2020-03-16 16:35:02
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class StatisticalOrgcodeServiceImpl extends ServiceImpl<StatisticalOrgcodeMapper, StatisticalOrgcode> implements IStatisticalOrgcodeService {

    @Autowired
    private StatisticalOrgcodeMapper statisticalOrgcodeMapper;

    @Override
    public IPage<StatisticalOrgcode> findStatisticalOrgcodes(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        LambdaQueryWrapper<StatisticalOrgcode> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(statisticalOrgcode.getOrgCode())){
            queryWrapper.eq(StatisticalOrgcode::getOrgCode,statisticalOrgcode.getOrgCode());
        }
        if (statisticalOrgcode.getStatisticalStartTime() != null){
            queryWrapper.ge(StatisticalOrgcode::getStatisticalDate,statisticalOrgcode.getStatisticalStartTime());
        }
        if (statisticalOrgcode.getStatisticalEndTime() != null){
            queryWrapper.le(StatisticalOrgcode::getStatisticalDate,statisticalOrgcode.getStatisticalEndTime());
        }
        queryWrapper.orderByDesc(StatisticalOrgcode::getStatisticalDate);
        Page<StatisticalOrgcode> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    /**
     * 查询商户统计及资费，短信退还数
     * @param request
     * @param statisticalOrgcode
     * @return
     */
    @Override
    public IPage<StatisticalOrgcodeExt> selectStatisticalAndCost(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        Page<StatisticalOrgcode> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.selectStatisticalAndCost(page,statisticalOrgcode);
    }

    @Override
    public List<StatisticalOrgcodeExt> selectStatisticalAndCost(StatisticalOrgcodeQuery statisticalOrgcode){
        return this.baseMapper.selectStatisticalAndCostList(statisticalOrgcode);
    }

    @Override
    public IPage<StatisticalOrgcode> sumStatisticalOrgcodes(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        Page<StatisticalOrgcode> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.sumStatisticalOrgcodes(page,statisticalOrgcode);
    }

    /**
     * 根据商务统计
     * @param request
     * @param statisticalOrgcode
     * @return
     */
    @Override
    public IPage<StatisticalOrgcodeExt> sumStatisticalBusiness(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        QueryWrapper<StatisticalOrgcodeExt> queryWrapper = new QueryWrapper();
        // TODO 设置查询条件
        if (statisticalOrgcode.getBusinessUserId() != null){
            queryWrapper.eq("o.`Business_User_Id`",statisticalOrgcode.getBusinessUserId());
        }
        if (statisticalOrgcode.getStatisticalStartTime() != null){
            queryWrapper.ge("s.`statistical_date`",statisticalOrgcode.getStatisticalStartTime());
        }
        if (statisticalOrgcode.getStatisticalEndTime() != null){
            queryWrapper.le("s.`statistical_date`",statisticalOrgcode.getStatisticalEndTime());
        }
        queryWrapper.apply("(o.`Business_User_Id` <> 0 or o.`Business_User_Id` <> NULL)");
        queryWrapper.groupBy("o.`Business_User_Id`");
        queryWrapper.groupBy("s.`statistical_date`");
        Page<StatisticalOrgcode> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.sumStatisticalBusiness(page,queryWrapper);
    }

    /**
     * 根据商务统计
     * @param statisticalOrgcode
     * @return
     */
    @Override
    public List<StatisticalOrgcodeExt> sumStatisticalBusinessList(StatisticalOrgcodeQuery statisticalOrgcode) {
        QueryWrapper<StatisticalOrgcodeExt> queryWrapper = new QueryWrapper();
        // TODO 设置查询条件
        if (statisticalOrgcode.getBusinessUserId() != null){
            queryWrapper.eq("o.`Business_User_Id`",statisticalOrgcode.getBusinessUserId());
        }
        if (statisticalOrgcode.getStatisticalStartTime() != null){
            queryWrapper.ge("s.`statistical_date`",statisticalOrgcode.getStatisticalStartTime());
        }
        if (statisticalOrgcode.getStatisticalEndTime() != null){
            queryWrapper.le("s.`statistical_date`",statisticalOrgcode.getStatisticalEndTime());
        }
        queryWrapper.apply("(o.`Business_User_Id` <> 0 or o.`Business_User_Id` <> NULL)");
        queryWrapper.groupBy("o.`Business_User_Id`");
        queryWrapper.groupBy("s.`statistical_date`");
        return this.baseMapper.sumStatisticalBusinessList(queryWrapper);
    }

    /**
     * 根据商务月统计
     * @param request
     * @param statisticalOrgcode
     * @return
     */
    @Override
    public IPage<StatisticalOrgcodeExt> sumStatisticalBusinessOnMonth(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        QueryWrapper<StatisticalOrgcodeExt> queryWrapper = new QueryWrapper();
        // TODO 设置查询条件
        if (statisticalOrgcode.getBusinessUserId() != null){
            queryWrapper.eq("o.`Business_User_Id`",statisticalOrgcode.getBusinessUserId());
        }
        if (statisticalOrgcode.getStatisticalStartMonth() != null){
            queryWrapper.ge("s.`statistical_date`",statisticalOrgcode.getStatisticalStartMonth());
        }
        if (statisticalOrgcode.getStatisticalEndMonth() != null){
            queryWrapper.le("s.`statistical_date`",statisticalOrgcode.getStatisticalEndMonth()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().minusMonths(-1).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        queryWrapper.apply("(o.`Business_User_Id` <> 0 or o.`Business_User_Id` <> NULL)");
        queryWrapper.groupBy("o.`Business_User_Id`");
        queryWrapper.groupBy("DATE_FORMAT(s.`statistical_date`,'%Y-%m')");
        Page<StatisticalOrgcode> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.sumStatisticalBusiness(page,queryWrapper);
    }

    /**
     * 根据商务月统计
     * @param statisticalOrgcode
     * @return
     */
    @Override
    public List<StatisticalOrgcodeExt> sumStatisticalBusinessOnMonth(StatisticalOrgcodeQuery statisticalOrgcode) {
        QueryWrapper<StatisticalOrgcodeExt> queryWrapper = new QueryWrapper();
        // TODO 设置查询条件
        if (statisticalOrgcode.getBusinessUserId() != null){
            queryWrapper.eq("o.`Business_User_Id`",statisticalOrgcode.getBusinessUserId());
        }
        if (statisticalOrgcode.getStatisticalStartMonth() != null){
            queryWrapper.ge("s.`statistical_date`",statisticalOrgcode.getStatisticalStartMonth());
        }
        if (statisticalOrgcode.getStatisticalEndMonth() != null){
            queryWrapper.le("s.`statistical_date`",statisticalOrgcode.getStatisticalEndMonth()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().minusMonths(-1).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        queryWrapper.apply("(o.`Business_User_Id` <> 0 or o.`Business_User_Id` <> NULL)");
        queryWrapper.groupBy("o.`Business_User_Id`");
        queryWrapper.groupBy("DATE_FORMAT(s.`statistical_date`,'%Y-%m')");
        return this.baseMapper.sumStatisticalBusinessList(queryWrapper);
    }

    /**
     * 根据商户号统计
     * @param request
     * @param statisticalOrgcode
     * @return
     */
    @Override
    public IPage<StatisticalOrgcode> sumStatisticalOrgcodesByOrg(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode) {
        QueryWrapper<StatisticalOrgcode> queryWrapper = new QueryWrapper();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(statisticalOrgcode.getOrgCode())){
            queryWrapper.eq("o.`Organization_Code`",statisticalOrgcode.getOrgCode());
        }
        if (statisticalOrgcode.getStatisticalStartTime() != null){
            queryWrapper.ge("s.`statistical_date`",statisticalOrgcode.getStatisticalStartTime());
        }
        if (statisticalOrgcode.getStatisticalEndTime() != null){
            queryWrapper.le("s.`statistical_date`",statisticalOrgcode.getStatisticalEndTime());
        }
        queryWrapper.groupBy("o.`Organization_Code`");
        Page<StatisticalOrgcode> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.sumStatisticalOrgcodesByOrg(page,queryWrapper);
    }

    @Override
    public List<StatisticalOrgcode> sumStatisticalOrgcodesByOrg( StatisticalOrgcodeQuery statisticalOrgcode) {
        QueryWrapper<StatisticalOrgcode> queryWrapper = new QueryWrapper();
        // TODO 设置查询条件
        if (StringUtils.isNotBlank(statisticalOrgcode.getOrgCode())){
            queryWrapper.eq("o.`Organization_Code`",statisticalOrgcode.getOrgCode());
        }
        if (statisticalOrgcode.getStatisticalStartTime() != null){
            queryWrapper.ge("s.`statistical_date`",statisticalOrgcode.getStatisticalStartTime());
        }
        if (statisticalOrgcode.getStatisticalEndTime() != null){
            queryWrapper.le("s.`statistical_date`",statisticalOrgcode.getStatisticalEndTime());
        }
        queryWrapper.groupBy("o.`Organization_Code`");
        return this.baseMapper.sumStatisticalOrgcodesByOrg(queryWrapper);
    }

        @Override
    public List<StatisticalOrgcode> findStatisticalOrgcodes(StatisticalOrgcodeQuery statisticalOrgcode) {
	    LambdaQueryWrapper<StatisticalOrgcode> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
        if (StringUtils.isNotBlank(statisticalOrgcode.getOrgCode())){
            queryWrapper.eq(StatisticalOrgcode::getOrgCode,statisticalOrgcode.getOrgCode());
        }
        if (statisticalOrgcode.getStatisticalStartTime() != null){
            queryWrapper.ge(StatisticalOrgcode::getStatisticalDate,statisticalOrgcode.getStatisticalStartTime());
        }
        if (statisticalOrgcode.getStatisticalEndTime() != null){
            queryWrapper.le(StatisticalOrgcode::getStatisticalDate,statisticalOrgcode.getStatisticalEndTime());
        }
        queryWrapper.orderByDesc(StatisticalOrgcode::getStatisticalDate);
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createStatisticalOrgcode(StatisticalOrgcode statisticalOrgcode) {
        this.save(statisticalOrgcode);
    }

    @Override
    @Transactional
    public void updateStatisticalOrgcode(StatisticalOrgcodeQuery statisticalOrgcode) {
        this.saveOrUpdate(statisticalOrgcode);
    }

    @Override
    @Transactional
    public void deleteStatisticalOrgcode(StatisticalOrgcodeQuery statisticalOrgcode) {
        LambdaQueryWrapper<StatisticalOrgcode> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteStatisticalOrgcodes(String[] statisticalOrgcodeIds) {
        List<String> list = Arrays.asList(statisticalOrgcodeIds);
        this.removeByIds(list);
    }

    /**
     * 代理后台首页统计
     * @param agent
     * @return
     */
    @Override
    @Transactional
    public String sumStatisticalOrgcodesOnAgent(Agent agent){
        QueryWrapper<StatisticalOrgcode> queryWrapper = new QueryWrapper();
        queryWrapper.eq("o.`Agent_Id`",agent.getId());
        queryWrapper.le("s.`statistical_date`", new Date());
        queryWrapper.ge("s.`statistical_date`", DateUtil.getString(DateUtil.addDay(new Date(),-1),"yyyy-MM-dd 00:00:00"));
        queryWrapper.groupBy("o.`Organization_Code`");
        IPage<StatisticalOrgcode> list = this.statisticalOrgcodeMapper.sumStatisticalOrgcodesByOrg(null,queryWrapper);
        JSONArray data = JSONObject.parseArray("[]");
        for (StatisticalOrgcode s:list.getRecords()) {
            if(s != null){
                JSONObject content = JSONObject.parseObject("{}");
                content.put("orgCode",s.getOrgCode());
                content.put("success",s.getReqSuccessCount());
                content.put("fail",s.getReqFailCount());
                data.add(content);
            }
        }
        return data.toJSONString();
    }

}
