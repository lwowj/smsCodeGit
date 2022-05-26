package com.hero.sms.service.message;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.message.MobileBlack;

/**
 * 手机号码黑名单 Service接口
 *
 * @author Administrator
 * @date 2020-03-17 01:17:22
 */
public interface IMobileBlackService extends IService<MobileBlack> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param mobileBlack mobileBlack
     * @return IPage<MobileBlack>
     */
    IPage<MobileBlack> findMobileBlacks(QueryRequest request, MobileBlack mobileBlack);

    /**
     * 查询（所有）
     *
     * @param mobileBlack mobileBlack
     * @return List<MobileBlack>
     */
    List<MobileBlack> findMobileBlacks(MobileBlack mobileBlack);

    /**
     * 新增
     *
     * @param mobileBlack mobileBlack
     */
    void createMobileBlack(MobileBlack mobileBlack) throws ServiceException;

    /**
     * 修改
     *
     * @param mobileBlack mobileBlack
     */
    void updateMobileBlack(MobileBlack mobileBlack);

    /**
     * 删除
     *
     * @param mobileBlack mobileBlack
     */
    void deleteMobileBlack(MobileBlack mobileBlack);

    /**
    * 删除
    *
    * @param mobileBlackIds mobileBlackIds
    */
    void deleteMobileBlacks(String[] mobileBlackIds);
    
    /**
     * 从缓存中获取
     * @param mobileBlack
     * @return
     */
    MobileBlack getCacheOne(MobileBlack mobileBlack);
}
