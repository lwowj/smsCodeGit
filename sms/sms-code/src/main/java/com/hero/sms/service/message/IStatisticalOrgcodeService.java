package com.hero.sms.service.message;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.message.StatisticalOrgcode;
import com.hero.sms.entity.message.StatisticalOrgcodeExt;
import com.hero.sms.entity.message.StatisticalOrgcodeQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 按照商户统计 Service接口
 *
 * @author Administrator
 * @date 2020-03-16 16:35:02
 */
public interface IStatisticalOrgcodeService extends IService<StatisticalOrgcode> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param statisticalOrgcode statisticalOrgcode
     * @return IPage<StatisticalOrgcode>
     */
    IPage<StatisticalOrgcode> findStatisticalOrgcodes(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode);

    /**
     * 查询商户统计及资费，短信退还数
     * @param request
     * @param statisticalOrgcode
     * @return
     */
    IPage<StatisticalOrgcodeExt> selectStatisticalAndCost(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode);

    List<StatisticalOrgcodeExt> selectStatisticalAndCost(StatisticalOrgcodeQuery statisticalOrgcode);

    IPage<StatisticalOrgcode> sumStatisticalOrgcodes(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode);

    /**
     * 根据商务统计
     * @param request
     * @param statisticalOrgcode
     * @return
     */
    IPage<StatisticalOrgcodeExt> sumStatisticalBusiness(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode);

    /**
     * 根据商务统计
     * @param statisticalOrgcode
     * @return
     */
    List<StatisticalOrgcodeExt> sumStatisticalBusinessList(StatisticalOrgcodeQuery statisticalOrgcode);

    /**
     * 根据商务月统计
     * @param request
     * @param statisticalOrgcode
     * @return
     */
    IPage<StatisticalOrgcodeExt> sumStatisticalBusinessOnMonth(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode);

    /**
     * 根据商务月统计
     * @param statisticalOrgcode
     * @return
     */
    List<StatisticalOrgcodeExt> sumStatisticalBusinessOnMonth(StatisticalOrgcodeQuery statisticalOrgcode);

    /**
     * 根据商户号统计
     * @param request
     * @param statisticalOrgcode
     * @return
     */
    IPage<StatisticalOrgcode> sumStatisticalOrgcodesByOrg(QueryRequest request, StatisticalOrgcodeQuery statisticalOrgcode);

    List<StatisticalOrgcode> sumStatisticalOrgcodesByOrg(StatisticalOrgcodeQuery statisticalOrgcode);

    /**
     * 查询（所有）
     *
     * @param statisticalOrgcode statisticalOrgcode
     * @return List<StatisticalOrgcode>
     */
    List<StatisticalOrgcode> findStatisticalOrgcodes(StatisticalOrgcodeQuery statisticalOrgcode);

    /**
     * 新增
     *
     * @param statisticalOrgcode statisticalOrgcode
     */
    void createStatisticalOrgcode(StatisticalOrgcode statisticalOrgcode);

    /**
     * 修改
     *
     * @param statisticalOrgcode statisticalOrgcode
     */
    void updateStatisticalOrgcode(StatisticalOrgcodeQuery statisticalOrgcode);

    /**
     * 删除
     *
     * @param statisticalOrgcode statisticalOrgcode
     */
    void deleteStatisticalOrgcode(StatisticalOrgcodeQuery statisticalOrgcode);

    /**
    * 删除
    *
    * @param statisticalOrgcodeIds statisticalOrgcodeIds
    */
    void deleteStatisticalOrgcodes(String[] statisticalOrgcodeIds);

    /**
     * 代理后台首页统计
     * @param agent
     * @return
     */
    @Transactional
    String sumStatisticalOrgcodesOnAgent(Agent agent);
}
