package com.hero.sms.service.message;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.MobileArea;

/**
 *  Service接口
 *
 * @author Administrator
 * @date 2020-03-24 21:15:09
 */
public interface IMobileAreaService extends IService<MobileArea> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param mobileArea mobileArea
     * @return IPage<MobileArea>
     */
    IPage<MobileArea> findMobileAreas(QueryRequest request, MobileArea mobileArea);

    /**
     * 查询（所有）
     *
     * @param mobileArea mobileArea
     * @return List<MobileArea>
     */
    List<MobileArea> findMobileAreas(MobileArea mobileArea);

    /**
     * 新增
     *
     * @param mobileArea mobileArea
     */
    void createMobileArea(MobileArea mobileArea);

    /**
     * 修改
     *
     * @param mobileArea mobileArea
     */
    void updateMobileArea(MobileArea mobileArea);

    /**
     * 删除
     *
     * @param mobileArea mobileArea
     */
    void deleteMobileArea(MobileArea mobileArea);

    /**
    * 删除
    *
    * @param mobileAreaIds mobileAreaIds
    */
    void deleteMobileAreas(String[] mobileAreaIds);
    
    /**
     * 从缓存中获取
     * @param mobileBlack
     * @return
     */
    MobileArea getCacheOne(MobileArea mobileArea);
}
