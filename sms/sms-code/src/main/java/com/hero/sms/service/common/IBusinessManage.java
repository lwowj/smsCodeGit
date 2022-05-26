package com.hero.sms.service.common;

import java.util.List;

import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.exception.ServiceException;

public interface IBusinessManage {
    /**
     * 重新加载缓存
     */
    void loadReapplication();

    /**
     * 加载缓存 请求
     * @param cacheType
     * @return
     */
    String serverLoadConfigCache(String cacheType) throws ServiceException;

    /**
     * 2021-03-06
     * 指定项目指定模块清理缓存
     * @param projectName
     * @param moduleTypeId
     * @throws ServiceException
     */
    void reladProjectCacheForModule(String projectName,String moduleTypeId) throws ServiceException;
    
    /**
     * 2021-03-03
     * 指定模块加载缓存
     * @param cacheType
     * @param moduleName
     * @return
     * @throws ServiceException
     */
    String serverLoadConfigCacheForModule(String cacheType,String moduleTypeId) throws ServiceException;
    
    List<FebsResponse> serverConstrolSmsGate(String data) throws ServiceException;


    /**
     * 加载缓存 接受
     * @param data
     * @param ip
     * @return
     */
    FebsResponse loadConfigCache(String data, String ip);

    FebsResponse constrolSmsGate(String data,String sign,String ip);

    List<FebsResponse> serverGetSmsGateConnectInfo(String data) throws ServiceException;

    FebsResponse getSmsGateConnectInfo(String data, String sign, String ip);

    FebsResponse reqConstrolSmsGateServer(String data);

	FebsResponse constrolSmsGateServer(String data, String sign, String ip);

    List<FebsResponse> switchSmsGateSender(String data) throws ServiceException;

    FebsResponse switchSmsGateReciver(String data, String sign, String ip);
    
    /**
     * 加载缓存 针对模块
     * @param moduleTypeIds  模块ID
     * @return
     */
    FebsResponse loadConfigCacheForOneModule(String moduleTypeIds);
}
