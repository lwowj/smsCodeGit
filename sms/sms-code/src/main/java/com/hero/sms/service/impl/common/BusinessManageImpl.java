package com.hero.sms.service.impl.common;

import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.HttpUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.entity.channel.GatewayConnectInfo;
import com.hero.sms.entity.channel.SmsChannelExt;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.organization.ext.OrganizationExt;
import com.hero.sms.enums.channel.ChannelStateEnums;
import com.hero.sms.enums.common.ModuleTypeEnums;
import com.hero.sms.enums.organization.OrgInterfaceTypeEnums;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.service.message.ISensitiveWordService;
import com.hero.sms.service.mq.SmsGateService;
import com.hero.sms.utils.RandomUtil;
import com.hero.sms.utils.StringUtil;
import com.zx.sms.connect.manager.EndpointConnector;
import com.zx.sms.connect.manager.EndpointEntity;
import com.zx.sms.connect.manager.EndpointManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BusinessManageImpl implements IBusinessManage {

    @Autowired
    private DatabaseCache databaseCache;

    @Autowired
    private ISensitiveWordService sensitiveWordService;

    @Autowired
    private SmsGateService smsGateService;

    @Autowired
    private ISmsChannelService smsChannelService;

    @Override
    public void loadReapplication() {
        databaseCache.init();
    }

    @Override
    public String serverLoadConfigCache(String cacheType) throws ServiceException {
        if(StringUtils.isBlank(cacheType)) {
            throw new ServiceException("????????????");
        }
        Map<String, String> paramMap = new TreeMap<String, String>();
        paramMap.put("serverType", cacheType);
        paramMap.put("randonNum", RandomUtil.randomNum(5));
        String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
        String signStr = JSONObject.toJSONString(paramMap) + key;
        String sign = MD5Util.MD5(signStr);
        paramMap.put("sign", sign);
        String content = JSONObject.toJSONString(paramMap);
        paramMap.clear();
        paramMap.put("data",content);

        String url = null;
        String serverIp = null;
        if(cacheType.equals("merch")) {
            serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","merch_server_ip").getName();
        }
        if(cacheType.equals("agent")) {
            serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","agent_server_ip").getName();
        }
        if(cacheType.equals("gateway")) {
            serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","gateway_server_ip").getName();
        }
        String [] serverIps = serverIp.split(",");
        StringBuffer sb = new StringBuffer();
        for (String ip : serverIps) {
            url = "http://" + ip + "/api/loadConfigCache?data={data}";
            try {
                RestTemplate restTemplate = new RestTemplate();
                String result = restTemplate.getForObject(url,String.class,paramMap);
                if(!result.startsWith("{") && !result.endsWith("}")) {
                    sb.append("[").append(ip).append("]???????????????").append(result).append("<br>");
                    continue;
                }
                JSONObject resJson = JSONObject.parseObject(result);
                if(!resJson.getString("code").equals("200")) {
                    sb.append("[").append(ip).append("]???????????????").append(resJson.getString("message")).append("<br>");
                }
            }catch(Exception e) {
                sb.append("[").append(ip).append("]???????????????").append(e.getMessage()).append("<br>");
            }
        }
        if(StringUtils.isNotBlank(sb.toString())) {
            throw new ServiceException(sb.toString());
        }
        return null;
    }

    @Override
	public List<FebsResponse> serverConstrolSmsGate(String data) throws ServiceException {

        String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
        String signStr = data + key;
        String sign = MD5Util.MD5(signStr);

        String url = null;
        String serverIp = null;
        serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","up_smsgate_ip").getName();
        String [] serverIps = serverIp.split(",");
        StringBuffer sb = new StringBuffer();
        List<FebsResponse> resultList = Lists.newArrayList();
        for (String ip : serverIps) {
            try {
            	url = MessageFormat.format("http://{0}/api/constrolSmsGate",ip);
            	LinkedMultiValueMap map = new LinkedMultiValueMap();
            	map.add("data", data);
            	map.add("sign", sign);

            	SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            	requestFactory.setConnectTimeout(20000);
            	requestFactory.setReadTimeout(20000);
            	HttpHeaders headers = new HttpHeaders();
            	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            	String result = HttpUtil.sendPostRequest(url, requestFactory, headers, map);
            	resultList.add(JSON.parseObject(result, FebsResponse.class));
            }catch(Exception e) {
            	log.error("SMSGATE CONTROL REQ ERR",e);
            	resultList.add((new FebsResponse()).fail().message(String.format("???%s?????????????????????:???%s???",ip,e.getMessage())));
            }
        }
        if(StringUtils.isNotBlank(sb.toString())) {
            throw new ServiceException(sb.toString());
        }
        return resultList;
	}

	@Override
    public FebsResponse loadConfigCache(String data, String ip) {
        String serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","admin_server_ip").getName();
        String [] serverIps = serverIp.split(",");
        boolean isPass = false;
        if(checkIp(serverIps, ip)) {
            isPass = true;
        }
        if(!isPass) {
        	serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","agent_server_ip").getName();
        	serverIps = serverIp.split(",");
        	if(checkIp(serverIps, ip)) {
        		isPass = true;
        	}
        }
        if(!isPass) {
        	log.error("??????IP??????:" + ip);
    		return new FebsResponse().message("??????IP??????").fail();
        }
        try {
            JSONObject reqJson = JSONObject.parseObject(data, Feature.OrderedField);
            String resSign = (String) reqJson.remove("sign");
            String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
            String checkStr = reqJson.toString() + key;
            String checkSign = MD5Util.MD5(checkStr);
            if(!resSign.equals(checkSign)) {
                return new FebsResponse().message("???????????????").fail();
            }
            /**
             * @modify 2021-03-04
                        * ????????????????????????????????????????????????????????????????????????
             * 
             */
            String moduleTypeIds = String.valueOf(reqJson.get("moduleTypeId"));
            if(StringUtil.isNotBlank(moduleTypeIds))
            {
            	int moduleTypeId = 0;
            	try {
            		moduleTypeId = Integer.valueOf(moduleTypeIds).intValue();
				} catch (Exception e) {}
            	if(moduleTypeId==ModuleTypeEnums.SensitiveWord.getCode().intValue())
            	{
            		sensitiveWordService.init();//???????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.CodeList.getCode().intValue())
            	{
            		databaseCache.initCode();//??????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.AgentList.getCode().intValue())
            	{
            		databaseCache.initAgent();//??????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.OrganizationList.getCode().intValue())
            	{
            		databaseCache.initOrganization();//??????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.SmsChannelList.getCode().intValue())
            	{
            		databaseCache.initSmsChannel();//????????????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.PayChannelList.getCode().intValue())
            	{
            		databaseCache.initPayChannel();//????????????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.BlackIpConfig.getCode().intValue())
            	{
            		databaseCache.initBlackIpConfig();//??????IP?????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.MobileAreaList.getCode().intValue())
            	{
            		databaseCache.initMobileArea();//??????????????????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.MobileBlack.getCode().intValue())
            	{
            		databaseCache.initMobileBlack();//?????????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.GatewayType.getCode().intValue())
            	{
            		databaseCache.initGatewayType();//????????????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.AreaCodeList.getCode().intValue())
            	{
            		databaseCache.initAreaCodeList();//??????????????????
            	}
            	else
            	{
            		databaseCache.init();
                    sensitiveWordService.init();
            	}
            }
            else
            {
            	databaseCache.init();
                sensitiveWordService.init();
            }
            /**
             * @end
             */
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            return new FebsResponse().message("???????????????").fail();
        }
        return new FebsResponse().message("???????????????").success();
    }

    @Override
	public FebsResponse constrolSmsGate(String data,String sign, String ip) {
    	String serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","admin_server_ip").getName();
        String [] serverIps = serverIp.split(",");
        if(!checkIp(serverIps, ip)) {
            log.error("??????IP??????:" + ip);
            return new FebsResponse().message("??????IP??????").fail();
        }
        try {
            String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
            String checkStr = data + key;
            String checkSign = MD5Util.MD5(checkStr);
            if(!sign.equals(checkSign)) {
                return new FebsResponse().message("???????????????").fail();
            }
            SmsChannelExt smsChannelExt = JSON.parseObject(data, SmsChannelExt.class);
            EndpointEntity endpointEntity = EndpointManager.INS.getEndpointEntity(smsChannelExt.getId().toString());
            if(smsChannelExt.getState().intValue() == ChannelStateEnums.START.getCode() || smsChannelExt.getState().intValue() == ChannelStateEnums.Pause.getCode()) {
                if (endpointEntity == null){
                    //????????????????????????????????????????????????
                    smsGateService.initGateClient(smsChannelExt);
                }else {
                    Code code = DatabaseCache.getCodeBySortCodeAndCodeAndUpCode("System", "isOpenClientWhenChannelStart", smsChannelExt.getProtocolType());
                    boolean isOpen = false;
                    if(code != null) {
                        isOpen = Boolean.parseBoolean(code.getName());
                    }
                    if(isOpen) {
                        //??????????????????????????????????????????????????????????????????
                        EndpointConnector conn = endpointEntity.getSingletonConnector();
                        int max = endpointEntity.getMaxChannels();
                        int actual = conn.getConnectionNum();
                        log.info(String.format("?????????????????????%d,???????????????%d",max,actual));
                        //???????????????
                        if(actual < max){
                            int reConnectChannelNum = max - actual;
                            log.info(String.format("??????????????????%d?????????",reConnectChannelNum));
                            for (int i=0;i<reConnectChannelNum;i++){
                                try {
                                    conn.open();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }else {
                            log.info(String.format("??????[%s]?????????????????????%d",smsChannelExt.getName(),max));
                        }
                    }
                }
            }
            if(smsChannelExt.getState().intValue() == ChannelStateEnums.STOP.getCode()) {
            	Code code = DatabaseCache.getCodeBySortCodeAndCodeAndUpCode("System", "isCloseClientWhenChannelStop", smsChannelExt.getProtocolType());
            	boolean isClose = false;
            	if(code != null) {
            		isClose = Boolean.parseBoolean(code.getName());
            	}
            	if(isClose) {
            		smsGateService.closeClient(smsChannelExt.getId().toString());
            	}
            }
        }catch(Exception e) {
            log.error("SMSGATE CONTROL ERR???", e);
            return new FebsResponse().message("?????????????????????").fail();
        }
        return new FebsResponse().message("?????????????????????").success();
	}

    @Override
    public List<FebsResponse> serverGetSmsGateConnectInfo(String data) throws ServiceException {

        String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
        String signStr = data + key;
        String sign = MD5Util.MD5(signStr);

        String url = null;
        String serverIp = null;
        serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","up_smsgate_ip").getName();
        String [] serverIps = serverIp.split(",");
        StringBuffer sb = new StringBuffer();
        List<FebsResponse> resultList = Lists.newArrayList();
        for (String ip : serverIps) {
            try {
                url = MessageFormat.format("http://{0}/api/getSmsGateConnectInfo",ip);
                LinkedMultiValueMap map = new LinkedMultiValueMap();
                map.add("data", data);
                map.add("sign", sign);

                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout(20000);
                requestFactory.setReadTimeout(20000);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                String result = HttpUtil.sendPostRequest(url, requestFactory, headers, map);
                resultList.add(JSON.parseObject(result, FebsResponse.class));
            }catch(Exception e) {
                log.error("GET SMSGATE INFO REQ ERR",e);
                resultList.add((new FebsResponse()).fail().message(String.format("???%s?????????????????????????????????:???%s???",ip,e.getMessage())));
            }
        }
        if(StringUtils.isNotBlank(sb.toString())) {
            throw new ServiceException(sb.toString());
        }
        return resultList;
    }
	@Override
    public FebsResponse getSmsGateConnectInfo(String data, String sign, String ip) {
        String serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","admin_server_ip").getName();
        String [] serverIps = serverIp.split(",");
        if(!checkIp(serverIps, ip)) {
            log.error("??????IP??????:" + ip);
            return new FebsResponse().message("??????IP??????").fail();
        }
        try {
            String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
            String checkStr = data + key;
            String checkSign = MD5Util.MD5(checkStr);
            if(!sign.equals(checkSign)) {
                return new FebsResponse().message("???????????????").fail();
            }
            SmsChannelExt smsChannelExt = JSON.parseObject(data, SmsChannelExt.class);
            EndpointEntity endpointEntity = EndpointManager.INS.getEndpointEntity(smsChannelExt.getId().toString());

            if (endpointEntity == null){
                //????????????????????????????????????????????????
                 return new FebsResponse().message("????????????????????????").fail();
            }else {
                GatewayConnectInfo info = new GatewayConnectInfo();
                BeanUtils.copyProperties(endpointEntity,info);
                //??????????????????????????????????????????????????????????????????
                EndpointConnector conn = endpointEntity.getSingletonConnector();
                int actual = conn.getConnectionNum();
                info.setConnectionNum(actual);
                info.setLocalhost(InetAddress.getLocalHost().getHostAddress());
                return new FebsResponse().message("??????????????????????????????").data(info).success();
            }

        }catch(Exception e) {
            log.error("GET SMSGATE INFO ERR???", e);
            return new FebsResponse().message("?????????????????????????????????").fail();
        }
    }

    /**
     * ??????IP
     * @param serverIps
     * @param currentIp
     * @return
     */
    private boolean checkIp(String[] serverIps, String currentIp) {
        String [] ipPort = new String[2];
        for (String ip : serverIps) {
            ipPort = ip.split(":");
            if(ipPort[0].equals(currentIp)) {
                return true;
            }
        }
        return false;
    }

	@Override
	public FebsResponse reqConstrolSmsGateServer(String data) {
		String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
        String signStr = data + key;
        String sign = MD5Util.MD5(signStr);

        String url = null;
        String serverIp = null;
        serverIp = DatabaseCache.getCodeBySortCodeAndCode("SmppServerConfig","serverIp").getName();
        try {
        	url = MessageFormat.format("http://{0}/api/constrolSmsGateServer",serverIp);
        	LinkedMultiValueMap map = new LinkedMultiValueMap();
        	map.add("data", data);
        	map.add("sign", sign);

        	SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        	requestFactory.setConnectTimeout(20000);
        	requestFactory.setReadTimeout(20000);
        	HttpHeaders headers = new HttpHeaders();
        	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        	String result = HttpUtil.sendPostRequest(url, requestFactory, headers, map);
        	return JSON.parseObject(result, FebsResponse.class);
        }catch(Exception e) {
        	log.error("SMSGATE CONTROL REQ ERR",e);
        	return (new FebsResponse()).fail().message(String.format("???%s?????????????????????:???%s???",serverIp,e.getMessage()));
        }
	}

	@Override
	public FebsResponse constrolSmsGateServer(String data, String sign, String ip) {
		String serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","admin_server_ip").getName();
        String [] serverIps = serverIp.split(",");
        if(!checkIp(serverIps, ip)) {
            log.error("??????IP??????:" + ip);
            return new FebsResponse().message("??????IP??????").fail();
        }
        try {
            String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
            String checkStr = data + key;
            String checkSign = MD5Util.MD5(checkStr);
            if(!sign.equals(checkSign)) {
                return new FebsResponse().message("???????????????").fail();
            }
            OrganizationExt organization = JSON.parseObject(data, OrganizationExt.class);
            if (organization.getInterfaceType() != null && ((organization.getInterfaceType() & OrgInterfaceTypeEnums.Smpp.getCode()) == OrgInterfaceTypeEnums.Smpp.getCode())){
            	smsGateService.initGateServerChild(organization);
            }else {
            	smsGateService.deleteGateServerChild(organization);
            }
        }catch(Exception e) {
            log.error("SMSGATE SERVER CONTROL ERR???", e);
            return new FebsResponse().message("??????????????????????????????").fail();
        }
        return new FebsResponse().message("??????????????????????????????").success();
	}


    @Override
    public List<FebsResponse> switchSmsGateSender(String data) throws ServiceException {

        String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
        String signStr = data + key;
        String sign = MD5Util.MD5(signStr);

        String url = null;
        String serverIp = null;
        serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","up_smsgate_ip").getName();
        String [] serverIps = serverIp.split(",");
        StringBuffer sb = new StringBuffer();
        List<FebsResponse> resultList = Lists.newArrayList();
        for (String ip : serverIps) {
            try {
                url = MessageFormat.format("http://{0}/api/switchSmsGate",ip);
                LinkedMultiValueMap map = new LinkedMultiValueMap();
                map.add("data", data);
                map.add("sign", sign);

                SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                requestFactory.setConnectTimeout(20000);
                requestFactory.setReadTimeout(20000);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                String result = HttpUtil.sendPostRequest(url, requestFactory, headers, map);
                resultList.add(JSON.parseObject(result, FebsResponse.class));
            }catch(Exception e) {
                log.error("SMSGATE CONTROL REQ ERR",e);
                resultList.add((new FebsResponse()).fail().message(String.format("???%s?????????????????????:???%s???",ip,e.getMessage())));
            }
        }
        if(StringUtils.isNotBlank(sb.toString())) {
            throw new ServiceException(sb.toString());
        }
        return resultList;
    }


    @Override
    public FebsResponse switchSmsGateReciver(String data, String sign, String ip) {
        String serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","admin_server_ip").getName();
        String [] serverIps = serverIp.split(",");
        if(!checkIp(serverIps, ip)) {
            log.error("??????IP??????:" + ip);
            return new FebsResponse().message("??????IP??????").fail();
        }
        try {
            String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
            String checkStr = data + key;
            String checkSign = MD5Util.MD5(checkStr);
            if(!sign.equals(checkSign)) {
                return new FebsResponse().message("???????????????").fail();
            }
            JSONObject dataObj = JSON.parseObject(data);
            String state = dataObj.getString("state");
            String channelId = dataObj.getString("channelId");
            if (String.valueOf(ChannelStateEnums.STOP.getCode()).equals(state)){//??????
                smsGateService.closeClient(channelId.toString());
            }
            EndpointEntity endpointEntity = EndpointManager.INS.getEndpointEntity(channelId);
            if(String.valueOf(ChannelStateEnums.START.getCode()).equals(state)
                    || String.valueOf(ChannelStateEnums.Pause.getCode()).equals(state)) {
                if (endpointEntity == null){
                    SmsChannelExt smsChannelExt = smsChannelService.findContainPropertyById(Integer.valueOf(channelId));
                    //????????????????????????????????????????????????
                    smsGateService.initGateClient(smsChannelExt);
                }else {
                    //??????????????????????????????????????????????????????????????????
                    EndpointConnector conn = endpointEntity.getSingletonConnector();
                    int max = endpointEntity.getMaxChannels();
                    int actual = conn.getConnectionNum();
                    log.info(String.format("?????????????????????%d,???????????????%d",max,actual));
                    //???????????????
                    if(actual < max){
                        int reConnectChannelNum = max - actual;
                        log.info(String.format("??????????????????%d?????????",reConnectChannelNum));
                        for (int i=0;i<reConnectChannelNum;i++){
                            try {
                                conn.open();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }else {
                        log.info(String.format("??????[%s]?????????????????????%d",channelId,max));
                    }
                }
            }

        }catch(Exception e) {
            log.error("SMSGATE CONTROL ERR???", e);
            return new FebsResponse().message("?????????????????????").fail();
        }
        return new FebsResponse().message("?????????????????????").success();
    }
    
    /**
     * 2021-03-06
     * ????????????????????????????????????
     * @param projectName
     * @param moduleTypeId
     * @throws ServiceException
     */
	@Override
	public void reladProjectCacheForModule(String projectName,String moduleTypeId) throws ServiceException 
    {
    	String names[]=projectName.split(StringPool.COMMA);
    	for (int i = 0; i < names.length; i++) 
    	{
    		try {
    			serverLoadConfigCacheForModule(names[i],moduleTypeId);
			} catch (Exception e) {
				log.error(String.format("?????????%s????????????%s???????????????",names[i],moduleTypeId));
			}
		}
    }
    
    /**
     * 2021-03-03
     * ????????????????????????
     * @param cacheType
     * @param moduleTypeId
     * @return
     * @throws ServiceException
     */
    @Override
    public String serverLoadConfigCacheForModule(String cacheType,String moduleTypeId) throws ServiceException {
        if(StringUtils.isBlank(cacheType)) {
            throw new ServiceException("????????????");
        }
        Map<String, String> paramMap = new TreeMap<String, String>();
        paramMap.put("serverType", cacheType);
        paramMap.put("moduleTypeId", moduleTypeId);
        paramMap.put("randonNum", RandomUtil.randomNum(5));
        String key = DatabaseCache.getCodeBySortCodeAndCode("System","cache_secret_key").getName();
        String signStr = JSONObject.toJSONString(paramMap) + key;
        String sign = MD5Util.MD5(signStr);
        paramMap.put("sign", sign);
        String content = JSONObject.toJSONString(paramMap);
        paramMap.clear();
        paramMap.put("data",content);

        String url = null;
        String serverIp = null;
        if(cacheType.equals("merch")) {
            serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","merch_server_ip").getName();
        }
        if(cacheType.equals("agent")) {
            serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","agent_server_ip").getName();
        }
        if(cacheType.equals("gateway")) {
            serverIp = DatabaseCache.getCodeBySortCodeAndCode("System","gateway_server_ip").getName();
        }
        String [] serverIps = serverIp.split(",");
        StringBuffer sb = new StringBuffer();
        for (String ip : serverIps) {
            url = "http://" + ip + "/api/loadConfigCache?data={data}";
            try {
                RestTemplate restTemplate = new RestTemplate();
                String result = restTemplate.getForObject(url,String.class,paramMap);
                if(!result.startsWith("{") && !result.endsWith("}")) {
                    sb.append("[").append(ip).append("]???????????????").append(result).append("<br>");
                    continue;
                }
                JSONObject resJson = JSONObject.parseObject(result);
                if(!resJson.getString("code").equals("200")) {
                    sb.append("[").append(ip).append("]???????????????").append(resJson.getString("message")).append("<br>");
                }
            }catch(Exception e) {
                sb.append("[").append(ip).append("]???????????????").append(e.getMessage()).append("<br>");
            }
        }
        if(StringUtils.isNotBlank(sb.toString())) {
            throw new ServiceException(sb.toString());
        }
        return null;
    }
    
    /**
     * ????????????????????????
     * 2021-04-09 
     */
    @Override
    public FebsResponse loadConfigCacheForOneModule(String moduleTypeIds) 
    {
        try {
            if(StringUtil.isNotBlank(moduleTypeIds))
            {
            	int moduleTypeId = 0;
            	try {
            		moduleTypeId = Integer.valueOf(moduleTypeIds).intValue();
				} catch (Exception e) {}
            	if(moduleTypeId==ModuleTypeEnums.SensitiveWord.getCode().intValue())
            	{
            		sensitiveWordService.init();//???????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.CodeList.getCode().intValue())
            	{
            		databaseCache.initCode();//??????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.AgentList.getCode().intValue())
            	{
            		databaseCache.initAgent();//??????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.OrganizationList.getCode().intValue())
            	{
            		databaseCache.initOrganization();//??????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.SmsChannelList.getCode().intValue())
            	{
            		databaseCache.initSmsChannel();//????????????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.PayChannelList.getCode().intValue())
            	{
            		databaseCache.initPayChannel();//????????????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.BlackIpConfig.getCode().intValue())
            	{
            		databaseCache.initBlackIpConfig();//??????IP?????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.MobileAreaList.getCode().intValue())
            	{
            		databaseCache.initMobileArea();//??????????????????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.MobileBlack.getCode().intValue())
            	{
            		databaseCache.initMobileBlack();//?????????????????????
            	}
            	else if(moduleTypeId==ModuleTypeEnums.GatewayType.getCode().intValue())
            	{
            		databaseCache.initGatewayType();//????????????????????????
            	}
            	else
            	{
            		databaseCache.init();
                    sensitiveWordService.init();
            	}
            }
            else
            {
            	databaseCache.init();
                sensitiveWordService.init();
            }
            /**
             * @end
             */
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            return new FebsResponse().message("???????????????").fail();
        }
        return new FebsResponse().message("???????????????").success();
    }
}
