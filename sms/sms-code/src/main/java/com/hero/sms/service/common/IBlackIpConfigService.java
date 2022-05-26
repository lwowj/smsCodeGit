package com.hero.sms.service.common;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.common.BlackIpConfig;
import com.hero.sms.entity.common.BlackIpConfigQuery;

/**
 *  Service接口
 *
 * @author Administrator
 * @date 2020-12-21 19:43:55
 */
public interface IBlackIpConfigService extends IService<BlackIpConfig> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param blackIpConfig blackIpConfig
     * @return IPage<BlackIpConfig>
     */
    IPage<BlackIpConfig> findBlackIpConfigs(QueryRequest request, BlackIpConfig blackIpConfig);

    /**
     * 查询（所有）
     *
     * @param blackIpConfig blackIpConfig
     * @return List<BlackIpConfig>
     */
    List<BlackIpConfig> findBlackIpConfigs(BlackIpConfig blackIpConfig);
    
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param blackIpConfig blackIpConfig
     * @return IPage<BlackIpConfig>
     */
    IPage<BlackIpConfig> findBlackIpConfigs(QueryRequest request, BlackIpConfigQuery blackIpConfig);

    /**
     * 查询（所有）
     *
     * @param blackIpConfig blackIpConfig
     * @return List<BlackIpConfig>
     */
    List<BlackIpConfig> findBlackIpConfigs(BlackIpConfigQuery blackIpConfig);

    

    /**
     * 新增
     *
     * @param blackIpConfig blackIpConfig
     */
    void createBlackIpConfig(BlackIpConfig blackIpConfig);

    /**
     * 修改
     *
     * @param blackIpConfig blackIpConfig
     */
    void updateBlackIpConfig(BlackIpConfig blackIpConfig);

    /**
     * 删除
     *
     * @param blackIpConfig blackIpConfig
     */
    void deleteBlackIpConfig(BlackIpConfig blackIpConfig);

    /**
    * 删除
    *
    * @param blackIpConfigIds blackIpConfigIds
    */
    void deleteBlackIpConfigs(String[] blackIpConfigIds);
    
    /**
     * 批量修改黑名单IP状态
     * @param blackIpConfigIds 用逗号间隔的id集合
     * @param isAvailability	状态标识 是否可用:  1:可用，0为不可用;默认为1
     */
    void audit(String blackIpConfigIds, String isAvailability);
}
