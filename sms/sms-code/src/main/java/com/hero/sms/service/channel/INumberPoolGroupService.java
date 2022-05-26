package com.hero.sms.service.channel;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.NumberPoolGroup;

/**
 * 号码池组表 Service接口
 *
 * @author Administrator
 * @date 2020-04-15 21:09:40
 */
public interface INumberPoolGroupService extends IService<NumberPoolGroup> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param numberPoolGroup numberPoolGroup
     * @return IPage<NumberPoolGroup>
     */
    IPage<NumberPoolGroup> findNumberPoolGroups(QueryRequest request, NumberPoolGroup numberPoolGroup);

    /**
     * 查询（所有）
     *
     * @param numberPoolGroup numberPoolGroup
     * @return List<NumberPoolGroup>
     */
    List<NumberPoolGroup> findNumberPoolGroups(NumberPoolGroup numberPoolGroup);

    /**
     * 新增
     *
     * @param numberPoolGroup numberPoolGroup
     */
    void createNumberPoolGroup(NumberPoolGroup numberPoolGroup);

    /**
     * 修改
     *
     * @param numberPoolGroup numberPoolGroup
     */
    void updateNumberPoolGroup(NumberPoolGroup numberPoolGroup);

    /**
     * 删除
     *
     * @param numberPoolGroup numberPoolGroup
     */
    void deleteNumberPoolGroup(NumberPoolGroup numberPoolGroup) throws ServiceException;

    /**
    * 删除
    *
    * @param numberPoolGroupIds numberPoolGroupIds
    */
    void deleteNumberPoolGroups(String[] numberPoolGroupIds);
}
