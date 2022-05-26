package com.hero.sms.service.channel;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.NumberPool;
import org.springframework.scheduling.annotation.Async;

/**
 * 号码池 Service接口
 *
 * @author Administrator
 * @date 2020-04-15 21:08:21
 */
public interface INumberPoolService extends IService<NumberPool> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param numberPool numberPool
     * @return IPage<NumberPool>
     */
    IPage<NumberPool> findNumberPools(QueryRequest request, NumberPool numberPool);

    IPage<NumberPool> findNumbers(QueryRequest request, NumberPool numberPool);

    /**
     * 查询（所有）
     *
     * @param numberPool numberPool
     * @return List<NumberPool>
     */
    List<NumberPool> findNumberPools(NumberPool numberPool);

    /**
     * 新增
     *
     * @param numberPool numberPool
     */
    void createNumberPool(NumberPool numberPool) throws ServiceException;

    /**
     * 修改
     *
     * @param numberPool numberPool
     */
    void updateNumberPool(NumberPool numberPool);

    /**
     * 删除
     *
     * @param numberPool numberPool
     */
    int deleteNumberPool(NumberPool numberPool);

    /**
    * 删除
    *
    * @param numberPoolIds numberPoolIds
    */
    void deleteNumberPools(String[] numberPoolIds);

    void batchCreate(List<String> numberList, Integer groupId, Integer state) throws ServiceException;

    @Async
    void partitionSave(List<NumberPool> numberPools, int partirionSize);

    /**
     * 随机从号码池中获取一个号码（通道关闭号码池 返回null）
     * @param smsChannelId
     * @return
     */
    String randomNumberPoolBySmsChannelId(Integer smsChannelId);

    void batchCreate2Redis(List<String> numberList, Integer groupId);
}
