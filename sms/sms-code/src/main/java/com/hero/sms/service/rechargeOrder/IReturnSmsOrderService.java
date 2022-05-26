package com.hero.sms.service.rechargeOrder;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrder;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrderQuery;

/**
 * 退还短信条数记录 Service接口
 *
 * @author Administrator
 * @date 2020-04-20 00:13:55
 */
public interface IReturnSmsOrderService extends IService<ReturnSmsOrder> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param returnSmsOrder returnSmsOrder
     * @return IPage<ReturnSmsOrder>
     */
    IPage<ReturnSmsOrder> findReturnSmsOrders(QueryRequest request, ReturnSmsOrderQuery returnSmsOrder);

    /**
     * 查询（所有）
     *
     * @param returnSmsOrder returnSmsOrder
     * @return List<ReturnSmsOrder>
     */
    List<ReturnSmsOrder> findReturnSmsOrders(ReturnSmsOrderQuery returnSmsOrder);

    /**
     * 新增
     *
     * @param returnSmsOrder returnSmsOrder
     */
    void createReturnSmsOrder(ReturnSmsOrder returnSmsOrder,String createUser)throws ServiceException;

    /**
     * 修改
     *
     * @param returnSmsOrder returnSmsOrder
     */
    void updateReturnSmsOrder(ReturnSmsOrder returnSmsOrder);

    /**
     * 删除
     *
     * @param returnSmsOrder returnSmsOrder
     */
    void deleteReturnSmsOrder(ReturnSmsOrderQuery returnSmsOrder);

    /**
    * 删除
    *
    * @param returnSmsOrderIds returnSmsOrderIds
    */
    void deleteReturnSmsOrders(String[] returnSmsOrderIds);

    void createTask(List<ReturnSmsOrder> data, String username);
}
