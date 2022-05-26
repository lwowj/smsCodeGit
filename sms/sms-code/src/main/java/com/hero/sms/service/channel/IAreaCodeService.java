package com.hero.sms.service.channel;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.channel.AreaCodeExt;

/**
 * 国家地区表 Service接口
 *
 * @author Administrator
 * @date 2022-03-18 15:41:19
 */
public interface IAreaCodeService extends IService<AreaCode> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param areaCode areaCode
     * @return IPage<AreaCode>
     */
    IPage<AreaCode> findAreaCodes(QueryRequest request, AreaCodeExt areaCode);

    /**
     * 查询（所有）
     *
     * @param areaCode areaCode
     * @return List<AreaCode>
     */
    List<AreaCode> findAreaCodes(AreaCode areaCode);

    /**
     * 新增
     *
     * @param areaCode areaCode
     */
    void createAreaCode(AreaCode areaCode) throws ServiceException ;

    /**
     * 修改
     *
     * @param areaCode areaCode
     */
    void updateAreaCode(AreaCode areaCode) throws ServiceException ;

    /**
     * 删除
     *
     * @param areaCode areaCode
     */
    void deleteAreaCode(AreaCode areaCode);

    /**
    * 删除
    *
    * @param areaCodeIds areaCodeIds
    */
    void deleteAreaCodes(String[] areaCodeIds);
    
	AreaCode getOne(AreaCode areaCode);
	
	AreaCode getOneForId(AreaCode areaCode);
}
