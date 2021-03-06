package com.hero.sms.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.common.BlackIpConfig;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.MobileArea;
import com.hero.sms.entity.message.MobileBlack;
import com.hero.sms.entity.message.SensitiveWord;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.enums.blackIpConfig.IsAvailabilityEnums;
import com.hero.sms.enums.blackIpConfig.LImitProjectEnums;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.channel.IAreaCodeService;
import com.hero.sms.service.channel.IPayChannelService;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.common.IBlackIpConfigService;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.service.common.ICodeService;
import com.hero.sms.service.message.IMobileAreaService;
import com.hero.sms.service.message.IMobileBlackService;
import com.hero.sms.service.message.ISensitiveWordService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.utils.GetProjectConfigUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DatabaseCache {

    @Autowired
    private ICodeService codeService;
    @Autowired
    private IAgentService agentService;
    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private ISmsChannelService smsChannelService;
    @Autowired
    private IPayChannelService payChannelService;
    @Autowired
    private IBlackIpConfigService blackIpConfigService;
    @Value("${cacheServer:#{null}}")
    private String cacheServer;
    @Autowired
    private IBusinessManage businessManage;

    @Autowired
    private IMobileBlackService mobileBlackService;
    
    @Autowired
    private IMobileAreaService mobileAreaService;
    @Autowired
    private ISensitiveWordService sensitiveWordService;
    
    @Autowired
    private IAreaCodeService areaCodeService;
    
    private static List<Code> codeList;
    private static List<Organization> organizationList;
    private static List<Agent> agentList;
    private static List<SmsChannel> smsChannelList;
    private static List<PayChannel> payChannelList;
    private static List<BlackIpConfig> blackIpConfigList;
    private static List<MobileArea> mobileAreaList;
    private static List<MobileBlack> mobileBlackList;
    private static String gatewayType;
    private static List<SensitiveWord> sensitiveWordList;
    private static List<AreaCode> areaCodeList;
    /**
     *   key -> orgCode  ,value -> orgName;
     */
    private static Map<String,String> orgNameMap;

    /**
     *   key -> agentId  ,value -> agentName;
     */
    private static Map<Integer,String> agentNameMap;
    /**
     *   key -> channelId, value -> channelName
     */
    private static Map<Integer,String> smsChannelNameMap;

    /**
     * ?????????????????????????????????
     *
     * @return
     */
    public static Code getCodeBySortCodeAndCode(String sortCode, String code) {
        for (Code t : codeList) {
            if (t.getSortCode().equals(sortCode) && t.getCode().equals(code)) {
                return t;
            }
        }
        return null;
    }

    /**
     * ?????????????????????????????????
     *
     * @return
     */
    public static Code getCodeBySortCodeAndCodeAndUpCode(String sortCode, String code, String upCode) {
    	for (Code t : codeList) {
    		if (t.getSortCode().equals(sortCode) && t.getCode().equals(code) && t.getParentCode().equals(upCode)) {
    			return t;
    		}
    	}
    	return null;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param sortCode
     * @param name
     * @return Code
     * @exception @since
     *                1.0.0
     */
    public static Code getCodeBySortCodeAndContainsName(String sortCode, String name) {
        for (Code t : codeList) {
            if (t.getSortCode().equals(sortCode) && StringUtils.contains(t.getName(), name)) {
                return t;
            }
        }
        return null;
    }

    public static Code getCodeBySortCodeAndName(String sortCode, String name) {
        for (Code t : codeList) {
            if (t.getSortCode().equals(sortCode) && t.getName().equals(name)) {
                return t;
            }
        }
        return null;
    }

    public static Code getCodeBySortCodeAndUpCodeAndName(String sortCode,String upCode, String name) {
    	List<Code> list = codeList.stream().filter(code -> code.getSortCode().equals(sortCode)
    			&& code.getParentCode().equals(upCode) && code.getName().equals(name)).collect(Collectors.toList());
    	return list.size()>0?list.get(0):null;
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public static List<Code> getCodeListBySortCode(String sortCode) {
        List<Code> tmp = new ArrayList<Code>();
        for (Code code : codeList) {
            if (code.getSortCode().equals(sortCode)) {
                tmp.add(code);
            }
        }
        return tmp;
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public static List<Code> getCodeListByUpCode(String upCode) {
        List<Code> tmp = new ArrayList<Code>();
        for (Code code : codeList) {
            if (upCode.equals(StringUtils.isNotEmpty(code.getParentCode()) ? code.getParentCode() : "")) {
                tmp.add(code);
            }
        }
        return tmp;
    }

    /**
     * ???????????????????????????????????????
     * @param sortCode
     * @param upCode
     * @return
     */
    public static List<Code> getCodeListBySortCodeAndUpCode(String sortCode,String upCode){
        List<Code> tmp = getCodeListBySortCode(sortCode);
        if (StringUtils.isBlank(upCode)){
            tmp = tmp.stream().filter(code -> StringUtils.isBlank(code.getParentCode())).collect(Collectors.toList());
        }else {
            tmp = tmp.stream().filter(code -> upCode.equals(code.getParentCode())).collect(Collectors.toList());
        }
        return tmp;
    }

    public static List<Agent> getAgentList() {
        return agentList;
    }

    public static List<Organization> getOrganizationList() {
        return organizationList;
    }

    /**
     * ????????????ID????????????
     * @param id
     * @return
     */
    public static List<Organization> getOrganizationListByAgentId(Integer id) {
        List<Organization> list = new ArrayList<>();
        for (Organization o: organizationList) {
            if(o.getAgentId().equals(id)){
                list.add(o);
            }
        }
        return list;
    }

    public static List<SmsChannel> getSmsChannelList() {
        return smsChannelList;
    }

    public static List<PayChannel> getPayChannelList() {
        return payChannelList;
    }

    /**
     * ?????????????????????IP
     * @return
     */
    public static List<BlackIpConfig> getBlackIpConfigList() {
        return blackIpConfigList;
    }
    
    /**
     * ??????????????????????????????
     * 2021-01-15
     * @return
     */
    public static List<MobileArea> getMobileAreaList() {
        return mobileAreaList;
    }
    
    /**
     * ?????????????????????????????????
     * 2021-01-15
     * @return
     */
    public static List<MobileBlack> getMobileBlackList() {
        return mobileBlackList;
    }
    
    
    /**
     * ??????????????????????????????IP
     * @return
     */
    public static List<BlackIpConfig> getBlackIpConfigList(String limitProject) {
    	if(limitProject==null) {limitProject = "";}
    	List<BlackIpConfig> getBlackIpConfigList = new ArrayList<BlackIpConfig>();
    	if(blackIpConfigList!=null&&blackIpConfigList.size()>0)
    	{
    		for (int i = 0; i < blackIpConfigList.size(); i++) 
    		{
    			BlackIpConfig blackIpConfig = blackIpConfigList.get(i);
    			if(limitProject.equals(blackIpConfig.getLimitProject()))
    			{
    				getBlackIpConfigList.add(blackIpConfig);
    			}
			}
    	}
        return getBlackIpConfigList;
    }
    
    /**
     * ????????????????????????ALL???????????????ALL???????????????
     * @param limitProject
     * @return
     */
    public static List<BlackIpConfig> getBlackIpConfigAndAllList(String limitProject) {
    	if(limitProject==null) {limitProject = "";}
    	List<BlackIpConfig> getBlackIpConfigList = new ArrayList<BlackIpConfig>();
    	if(blackIpConfigList!=null&&blackIpConfigList.size()>0)
    	{
    		for (int i = 0; i < blackIpConfigList.size(); i++) 
    		{
    			BlackIpConfig blackIpConfig = blackIpConfigList.get(i);
    			if(limitProject.equals(blackIpConfig.getLimitProject()))
    			{
    				getBlackIpConfigList.add(blackIpConfig);
    			}
    			//????????????????????????ALL???????????????ALL???????????????
    			if(!limitProject.equals(LImitProjectEnums.All.getCode()))
    			{
    				if(LImitProjectEnums.All.getCode().equals(blackIpConfig.getLimitProject()))
        			{
        				getBlackIpConfigList.add(blackIpConfig);
        			}
    			}
			}
    	}
        return getBlackIpConfigList;
    }
    
    /**
     * ??????ip????????????????????????????????????????????????
     * @param limitProject
     * @param checkIp
     * @return
     */
    public static boolean checkBlackIpConfigFlag(String limitProject,String checkIp) 
    {
    	boolean checkFlag = false;
    	if(limitProject==null) {limitProject = "";}
    	if(StringUtils.isNotBlank(checkIp)) {checkFlag = false;}
    	List<BlackIpConfig> getBlackIpConfigList = getBlackIpConfigList(limitProject);
    	if(getBlackIpConfigList!=null&&getBlackIpConfigList.size()>0)
    	{
    		for (int i = 0; i < getBlackIpConfigList.size(); i++) 
    		{
    			BlackIpConfig blackIpConfig = getBlackIpConfigList.get(i);
    			if(checkIp.equals(blackIpConfig.getBlackIp()))
    			{
    				checkFlag = true;
    				break;
    			}
			}
    	}
        return checkFlag;
    }
    
    /**
     *  ??????ip?????????????????????????????????????????????
     * @param limitProject
     * @param checkIp
     * @return
     */
    public static boolean checkBlackIpConfigAndAllFlag(String limitProject,String checkIp) 
    {
    	boolean checkFlag = false;
    	if(limitProject==null) {limitProject = "";}
    	if(StringUtils.isNotBlank(checkIp)) {checkFlag = false;}
    	List<BlackIpConfig> getBlackIpConfigList = getBlackIpConfigAndAllList(limitProject);
    	if(getBlackIpConfigList!=null&&getBlackIpConfigList.size()>0)
    	{
    		for (int i = 0; i < getBlackIpConfigList.size(); i++) 
    		{
    			BlackIpConfig blackIpConfig = getBlackIpConfigList.get(i);
    			if(checkIp.equals(blackIpConfig.getBlackIp()))
    			{
    				checkFlag = true;
    				break;
    			}
			}
    	}
        return checkFlag;
    }
    
    /**
     * ????????????????????????????????????
     * @param orgCode
     * @return
     */
    public static String getOrgNameByOrgcode(String orgCode){
        if (orgNameMap == null || StringUtils.isBlank(orgCode)) return null;
        return orgNameMap.get(orgCode);
    }

    public static Organization getOrganizationByOrgcode(String orgCode){
    	if (organizationList == null || organizationList.size() == 0) return null;
    	Optional<Organization> orgOpt = organizationList.stream().filter(organization -> organization.getOrganizationCode().equals(orgCode)).findFirst();
    	if(orgOpt.isPresent()) {
    		return orgOpt.get();
    	}
    	return null;
    }

    public static String getSmsChannelNameById(Integer channelId){
        if (channelId == null) return null;
        return smsChannelNameMap.get(channelId);
    }

    /**
     * ????????????id??????????????????
     * @param agentId
     * @return
     */
    public static String getAgentNameByAgentId(Integer agentId){
        if (agentNameMap == null || agentId == null) return null;
        return agentNameMap.get(agentId);
    }

    public static String getGatewayType()
    {
    	return gatewayType;
    }
    
    public void init(){

        log.info("??????????????????");
        LambdaQueryWrapper<Code> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Code::getIsDelete,"0");
        codeList = (List<Code>) codeService.list(wrapper);
        if (codeList == null) {
            codeList = new ArrayList<Code>();
        }
        
        log.info("??????????????????");
        agentList = (List<Agent>) agentService.list();
        if (agentList == null) {
        	agentList = new ArrayList<Agent>();
        }
        if (agentList.size() > 0){
            //??????  ??????id????????????????????????
            agentNameMap = new HashMap<>();
            agentList.stream().forEach(agent -> {
                agent.setDataMd5(null);
                agent.setAgentPassword(null);
                agent.setPayPassword(null);
                agent.setGoogleKey(null);
                agentNameMap.put(agent.getId(),agent.getAgentName());
            });
        }
        log.info("??????????????????");
        organizationList = (List<Organization>) organizationService.list();
        if (organizationList == null) {
        	organizationList = new ArrayList<Organization>();
        }
        if (organizationList.size() > 0){
            orgNameMap = new HashMap<>();
            organizationList.stream().forEach(organization -> {
                organization.setDataMd5(null);
                organization.setBindIp(null);
                organization.setSmsSign(null);
                organization.setMd5Key(null);
                organization.setNotifyUrl(null);
                orgNameMap.put(organization.getOrganizationCode(),organization.getOrganizationName());
            });
        }
        
        log.info("????????????????????????");
        smsChannelList = (List<SmsChannel>) smsChannelService.list();
        if (smsChannelList == null) {
        	smsChannelList = new ArrayList<SmsChannel>();
        }
        if (smsChannelList.size() > 0){
            smsChannelNameMap = new HashMap<>();
            smsChannelList.stream().forEach(smsChannel -> {
                smsChannelNameMap.put(smsChannel.getId(),smsChannel.getName());
            });
        }
        
        log.info("????????????????????????");
        payChannelList = (List<PayChannel>) payChannelService.list();
        if (payChannelList == null) {
        	payChannelList = new ArrayList<PayChannel>();
        }
        /**
         * @begin 2020-12-23
         */
        log.info("??????IP???????????????");
        BlackIpConfig blackIpConfig = new BlackIpConfig();
        //??????????????????????????????
        blackIpConfig.setIsavailability(String.valueOf(IsAvailabilityEnums.Invoke.getCode()));
        blackIpConfigList = (List<BlackIpConfig>) blackIpConfigService.findBlackIpConfigs(blackIpConfig);
        if (blackIpConfigList == null) {
        	blackIpConfigList = new ArrayList<BlackIpConfig>();
        }
        /**
         * @end
         */
//        /**
//         * @begin 2021-01-15
//         */
//        log.info("??????????????????????????????");
//        mobileAreaList = (List<MobileArea>) mobileAreaService.findMobileAreas(null);
//        if (mobileAreaList == null) {
//        	mobileAreaList = new ArrayList<MobileArea>();
//        }
//        /**
//         * @end
//         */
        /**
         * @begin 2021-01-15
         */
        log.info("?????????????????????????????????");
        MobileBlack mobileBlack = new MobileBlack();
        mobileBlackList = (List<MobileBlack>) mobileBlackService.findMobileBlacks(mobileBlack);
        if (mobileBlackList == null) {
        	mobileBlackList = new ArrayList<MobileBlack>();
        }
        /**
         * @end
         */
        /**
         * @begin 2021-02-03
         */
        log.info("????????????????????????");
        gatewayType = GetProjectConfigUtil.getProjectConfig("gatewayType");
        if (gatewayType == null) {
        	gatewayType = "";
        }
        /**
         * @end
         */
        /**
         * @begin 2022-03-18
         */
        log.info("Begin-??????????????????");
        LambdaQueryWrapper<AreaCode> queryAreaCodeWrapper = new LambdaQueryWrapper<>();
        queryAreaCodeWrapper.orderByAsc(AreaCode::getOrderNum);
        areaCodeList = (List<AreaCode>) areaCodeService.list(queryAreaCodeWrapper);
        if (areaCodeList == null) {
        	areaCodeList = new ArrayList<AreaCode>();
        }
        log.info("End-????????????????????????");
        /**
         * @end
         */
//        /**
//         * @begin 2021-03-04
//         */
//        log.info("?????????????????????");
//        SensitiveWord sensitiveWord = new SensitiveWord();
//        sensitiveWordList = (List<SensitiveWord>) sensitiveWordService.findSensitiveWords(sensitiveWord);
//        if (sensitiveWordList == null) {
//        	sensitiveWordList = new ArrayList<SensitiveWord>();
//        }
//        /**
//         * @end
//        */
        
        String isCache = getCodeBySortCodeAndCode("System","cacheServer").getName();
        // ????????????????????????
        if(StringUtils.isNotEmpty(isCache) && "true".equals(isCache) && StringUtils.isNotEmpty(cacheServer)){
            new Thread(() -> {
                String [] serverNames = cacheServer.split(",");
                for (String name:serverNames) {
                    try {
                        businessManage.serverLoadConfigCache(name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
       
        log.info("??????????????????");

    }
    
    
    /**
     * 2021-03-04
     * ???????????????
     */
    public void initCode()
    {
        log.info("Begin-????????????");
        LambdaQueryWrapper<Code> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Code::getIsDelete,"0");
        codeList = (List<Code>) codeService.list(wrapper);
        if (codeList == null) {
            codeList = new ArrayList<Code>();
        }
        log.info("End-??????????????????");
    }
    
    /**
     * 2021-03-04
     * ???????????????
     */
    public void initAgent()
    {
        log.info("Begin-??????????????????");
        agentList = (List<Agent>) agentService.list();
        if (agentList == null) {
        	agentList = new ArrayList<Agent>();
        }
        if (agentList.size() > 0){
            //??????  ??????id????????????????????????
            agentNameMap = new HashMap<>();
            agentList.stream().forEach(agent -> {
                agent.setDataMd5(null);
                agent.setAgentPassword(null);
                agent.setPayPassword(null);
                agent.setGoogleKey(null);
                agentNameMap.put(agent.getId(),agent.getAgentName());
            });
        }
        log.info("End-????????????????????????");

    }
    
    /**
     * 2021-03-04
     * ???????????????
     */
    public void initOrganization()
    {
        log.info("Begin-??????????????????");
        organizationList = (List<Organization>) organizationService.list();
        if (organizationList == null) {
        	organizationList = new ArrayList<Organization>();
        }
        if (organizationList.size() > 0){
            orgNameMap = new HashMap<>();
            organizationList.stream().forEach(organization -> {
                organization.setDataMd5(null);
                organization.setBindIp(null);
                organization.setSmsSign(null);
                organization.setMd5Key(null);
                organization.setNotifyUrl(null);
                orgNameMap.put(organization.getOrganizationCode(),organization.getOrganizationName());
            });
        }
        log.info("End-????????????????????????");
    }
    
    /**
     * 2021-03-04
     * ?????????????????????
     */
    public void initSmsChannel()
    {
        log.info("Begin-????????????????????????");
        smsChannelList = (List<SmsChannel>) smsChannelService.list();
        if (smsChannelList == null) {
        	smsChannelList = new ArrayList<SmsChannel>();
        }
        if (smsChannelList.size() > 0){
            smsChannelNameMap = new HashMap<>();
            smsChannelList.stream().forEach(smsChannel -> {
                smsChannelNameMap.put(smsChannel.getId(),smsChannel.getName());
            });
        }
        log.info("End-??????????????????????????????");

    }
    
    /**
     * ??????ID ?????????????????????
     * @param inId
     * @return
     */
    
    public static SmsChannel getSmsChannelById(int inId)
    {
        if (smsChannelList != null && smsChannelList.size() > 0) 
        {
        	for (int i = 0; i < smsChannelList.size(); i++) 
        	{
        		SmsChannel smsChannel = smsChannelList.get(i);
        		int channelId = smsChannel.getId();
        		if(inId == channelId)
        		{
        			return smsChannel;
        		}
			}
        }
        return null;
    }
    
    /**
     * 2021-03-04
     * ?????????????????????
     */
    public void initPayChannel()
    {
        log.info("Begin-????????????????????????");
        payChannelList = (List<PayChannel>) payChannelService.list();
        if (payChannelList == null) {
        	payChannelList = new ArrayList<PayChannel>();
        }
        log.info("End-??????????????????????????????");
    }
    
    /**
     * 2021-03-04
     * ?????????IP???????????????
     */
    public void initBlackIpConfig()
    {
        log.info("Begin-??????IP???????????????");
        BlackIpConfig blackIpConfig = new BlackIpConfig();
        //??????????????????????????????
        blackIpConfig.setIsavailability(String.valueOf(IsAvailabilityEnums.Invoke.getCode()));
        blackIpConfigList = (List<BlackIpConfig>) blackIpConfigService.findBlackIpConfigs(blackIpConfig);
        if (blackIpConfigList == null) {
        	blackIpConfigList = new ArrayList<BlackIpConfig>();
        }
        log.info("End-IP?????????????????????");
    }
    
    /**
     * 2021-03-04
     * ?????????????????????????????????
     */
    public void initMobileArea()
    {
        log.info("Begin-??????????????????????????????");
        mobileAreaList = (List<MobileArea>) mobileAreaService.findMobileAreas(null);
        if (mobileAreaList == null) {
        	mobileAreaList = new ArrayList<MobileArea>();
        }
        log.info("End-???????????????????????????????????????");
    }
    
    /**
     * 2021-03-04
     * ????????????????????????????????????
     */
    public void initMobileBlack()
    {
        log.info("Begin-?????????????????????????????????");
        MobileBlack mobileBlack = new MobileBlack();
        mobileBlackList = (List<MobileBlack>) mobileBlackService.findMobileBlacks(mobileBlack);
        if (mobileBlackList == null) {
        	mobileBlackList = new ArrayList<MobileBlack>();
        }
        log.info("End-???????????????????????????????????????");
    }
    
    /**
     * 2021-03-04
     * ???????????????????????????
     */
    public void initGatewayType()
    {
        log.info("Begin-????????????????????????");
        gatewayType = GetProjectConfigUtil.getProjectConfig("gatewayType");
        if (gatewayType == null) {
        	gatewayType = "";
        }
        log.info("End-??????????????????????????????");
    }
    
    /**
     * 2021-03-04
     * ????????????????????????
     */
    public void initSensitiveWord()
    {
        log.info("Begin-?????????????????????");
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWordList = (List<SensitiveWord>)sensitiveWordService.findSensitiveWords(sensitiveWord);
        if (sensitiveWordList == null) {
        	sensitiveWordList = new ArrayList<SensitiveWord>();
        }
        log.info("End-???????????????????????????");
    }
    
    /**
     * 2021-03-04
     * ?????????????????????
     */
    public static List<SensitiveWord> getSensitiveWordList() 
    {
        return sensitiveWordList;
    }
    
    /**
     * 2022-03-18
     * ?????????????????????
     */
    public void initAreaCodeList()
    {
        log.info("Begin-??????????????????");
        LambdaQueryWrapper<AreaCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(AreaCode::getOrderNum);
        areaCodeList = (List<AreaCode>) areaCodeService.list(queryWrapper);
        if (areaCodeList == null) {
        	areaCodeList = new ArrayList<AreaCode>();
        }
        log.info("End-????????????????????????");
    }
    
    public static List<AreaCode> getAreaCodeList() 
    {
        return areaCodeList;
    }
    
    /**
     * ???????????????list??????areacode
     * @param costAreaList
     * @return
     */
    public static List<AreaCode> getAreaCodeListScreen(List<String> costAreaList) 
    {
    	List<AreaCode> outAreaCodeList = new ArrayList<AreaCode>();
    	if(areaCodeList!=null&&areaCodeList.size()>0&&costAreaList!=null&&costAreaList.size()>0)
    	{
    		for (int i = 0; i < costAreaList.size(); i++) 
    		{
    			String rtInArea = costAreaList.get(i);
    			for (int j = 0; j < areaCodeList.size(); j++) 
    			{
    				AreaCode areacode = areaCodeList.get(j);
    				String inArea = areacode.getInArea();
    				if(rtInArea.equals(inArea))
    				{
    					outAreaCodeList.add(areacode);
    					break;
    				}
				}
    		}
    	}
        return outAreaCodeList;
    }
    /**
     * ??????????????????
     * @param inArea
     * @return
     */
    public static String getAreaCodeOutArea(String inArea)
    {
    	String outArea = inArea;
    	if(!inArea.startsWith("+"))
    	{
    		inArea = "+" + inArea;
    	}
    	if (areaCodeList == null || areaCodeList.size() == 0)
    	{
    		if(!outArea.startsWith("+"))
        	{
        		outArea = "+" + outArea;
        	}
    		return outArea;
    	}
    	for (int i = 0; i < areaCodeList.size(); i++) 
    	{
    		AreaCode acode = areaCodeList.get(i);
    		if(acode.getInArea().equals(inArea))
    		{
    			outArea = acode.getOutArea();
    			break;
    		}
		}
    	if(!outArea.startsWith("+"))
    	{
    		outArea = "+" + outArea;
    	}
    	return outArea;
    }
    /**
     * ??????????????????
     * @param inArea
     * @return
     */
    public static String getAreaCodeName(String inArea)
    {
    	String areaName = "";
    	if (areaCodeList == null || areaCodeList.size() == 0) return areaName;
    	if(!inArea.startsWith("+"))
    	{
    		inArea = "+" + inArea;
    	}
    	for (int i = 0; i < areaCodeList.size(); i++) 
    	{
    		AreaCode acode = areaCodeList.get(i);
    		if(acode.getInArea().equals(inArea))
    		{
    			areaName = acode.getAreaName();
    			break;
    		}
		}
    	return areaName;
    }
    /**
     * ??????????????????
     * @param inArea
     * @return
     */
    public static String getAreaCodeCoding(String inArea)
    {
    	String areaCoding= "";
    	if (areaCodeList == null || areaCodeList.size() == 0) return areaCoding;
    	if(!inArea.startsWith("+"))
    	{
    		inArea = "+" + inArea;
    	}
    	for (int i = 0; i < areaCodeList.size(); i++) 
    	{
    		AreaCode acode = areaCodeList.get(i);
    		if(acode.getInArea().equals(inArea))
    		{
    			areaCoding = acode.getAreaCoding();
    			break;
    		}
		}
    	return areaCoding;
    }
}
