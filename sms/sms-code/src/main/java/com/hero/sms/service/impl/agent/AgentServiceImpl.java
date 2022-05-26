package com.hero.sms.service.impl.agent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.AddressUtil;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.DateUtils;
import com.hero.sms.common.utils.GoogleAuthenticator;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.common.utils.RegexUtil;
import com.hero.sms.common.utils.URLUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentMenu;
import com.hero.sms.entity.agent.AgentSystemConfig;
import com.hero.sms.entity.agent.ext.AgentExt;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.enums.common.ModuleTypeEnums;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.mapper.agent.AgentMapper;
import com.hero.sms.service.agent.IAgentMenuLimitService;
import com.hero.sms.service.agent.IAgentMenuService;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.agent.IAgentSystemConfigService;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.organization.IOrganizationService;

/**
 * 商户代理 Service实现
 *
 * @author Administrator
 * @date 2020-03-06 10:05:11
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AgentServiceImpl extends ServiceImpl<AgentMapper, Agent> implements IAgentService {

    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private IAgentMenuService agentMenuService;
    @Autowired
    private IAgentMenuLimitService agentMenuLimitService;
    @Autowired
    private DatabaseCache databaseCache;
    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private IReportService reportService;
    @Autowired
    private IAgentSystemConfigService agentSystemConfigService;

    public static final String Md5Key = "sjiojsfjlcvjiwej";

    @Override
    public IPage<Agent> findAgents(QueryRequest request, Agent agent) {
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(agent.getId() != null){
            queryWrapper.eq(Agent::getId,agent.getId());
        }
        if(agent.getUpAgentId() != null){
            queryWrapper.eq(Agent::getUpAgentId,agent.getUpAgentId());
        }
        if(StringUtils.isNotEmpty(agent.getAgentAccount())){
            queryWrapper.eq(Agent::getAgentAccount,agent.getAgentAccount());
        }
        if(StringUtils.isNotEmpty(agent.getAgentName())){
            queryWrapper.eq(Agent::getAgentName,agent.getAgentName());
        }
        if(StringUtils.isNotEmpty(agent.getStateCode())){
            queryWrapper.eq(Agent::getStateCode,agent.getStateCode());
        }
        queryWrapper.orderByDesc(Agent::getId);
        Page<Agent> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Agent> findAgents(Agent agent) {
	    LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createAgent(Agent agent,String checkPassword) {
        if(agent == null || StringUtils.isEmpty(agent.getAgentAccount())){
            throw new FebsException("账号信息不完整");
        }
        //过滤登录账号前后空格 2020-12-11
        String agentAccount = agent.getAgentAccount().trim();
        if(StringUtils.isEmpty(agentAccount))
        {
            throw new FebsException("账号信息不完整");
        }
        //校验登录账号为英文与数字  2021-05-05
        if(!RegexUtil.isUserAccount(agent.getAgentAccount()))
        {
        	throw new FebsException("登录账号只能20位以内的英文、数字或英文数字组合");
        }
        agent.setAgentAccount(agentAccount);
        
        if(StringUtils.isEmpty(agent.getAgentPassword()) || !agent.getAgentPassword().equals(checkPassword)){
            throw new FebsException("设置密码错误");
        }
      //过滤前后空格 2020-12-11
        String agentPassword = agent.getAgentPassword().trim();
        if(StringUtils.isEmpty(agentPassword))
        {
            throw new FebsException("设置密码错误,密码不能为空格");
        }
        agent.setAgentPassword(agentPassword);
        
        if(StringUtils.isEmpty(agent.getPayPassword())){
            throw new FebsException("支付密码不能为空");
        }
      //过滤前后空格 2020-12-11
        String payPassword = agent.getPayPassword().trim();
        if(StringUtils.isEmpty(payPassword))
        {
            throw new FebsException("设置支付密码错误,支付密码不能为空格");
        }
        agent.setPayPassword(payPassword);
        
        Agent oldAgent = findByAccount(agent.getAgentAccount());
        if(oldAgent != null){
            throw new FebsException("该用户名存在");
        }
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getAgentName,agent.getAgentName());
        List<Agent> oldAgentList = this.list(wrapper);
        if(oldAgentList.size() !=0 ){
            throw new FebsException("该名称已存在");
        }
        if(agent.getUpAgentId() != null){
            Agent upAgent = this.baseMapper.selectById(agent.getUpAgentId());
            if(upAgent != null){
                Integer agentLevelCode = Integer.parseInt(upAgent.getLevelCode()) + 1;
                Integer levelCode = DatabaseCache.getCodeBySortCodeAndCode("System","levelCode") == null ? 2 :
                        Integer.parseInt(DatabaseCache.getCodeBySortCodeAndCode("System","levelCode").getName());
                if(agentLevelCode > levelCode){
                    throw new FebsException("不支持新增该级别代理！");
                }
                agent.setLevelCode(agentLevelCode + "");
            }else {
                throw new FebsException("上级代理不存在！");
            }
        }else {
            agent.setLevelCode("1");
        }
        agent.setAgentPassword(MD5Util.encrypt(agent.getAgentAccount(),agent.getAgentPassword()));
        agent.setAmount(0L);
        agent.setAvailableAmount(0L);
        agent.setCashAmount(0L);
        agent.setSendSmsTotal(0L);
        agent.setStateCode(OrgStatusEnums.Normal.getCode());
        agent.setQuotaAmount(0L);
        agent.setPayPassword(MD5Util.MD5(agent.getPayPassword()));
        agent.setCreateDate(new Date());
        //默认代理是否强制绑定Google口令
        int needBindGoogleKey = 0;
    	String agentDefaultNeedBindgoogleKey = "0";
		Code agentDefaultNeedBindgoogleKeyCode = DatabaseCache.getCodeBySortCodeAndCode("System","agentDefaultNeedBindgoogleKey");
	    if(agentDefaultNeedBindgoogleKeyCode!=null&&!"".equals(agentDefaultNeedBindgoogleKeyCode.getName()))
	    {
	    	agentDefaultNeedBindgoogleKey = agentDefaultNeedBindgoogleKeyCode.getName();
	    }
	    if("1".equals(agentDefaultNeedBindgoogleKey))
	    {
	    	needBindGoogleKey = 1;
	    }
        agent.setNeedBindGoogleKey(needBindGoogleKey);
      /*  agent.setGoogleKey(GoogleAuthenticator.generateSecretKey());*/
        agent.setDataMd5(getDataMd5(agent));
        this.save(agent);
        List<AgentMenu> menus = agentMenuService.findAgentMenus(new AgentMenu());
        StringBuffer ids = new StringBuffer();
        for (Iterator<AgentMenu> it = menus.iterator(); it.hasNext(); ) {
            AgentMenu agentMenu = it.next();
            if(Integer.parseInt(agent.getLevelCode())>1){
                if(Integer.parseInt(agentMenu.getLevelCode())>=Integer.parseInt(agent.getLevelCode())) continue;
            }
            ids.append(agentMenu.getMenuId());
            if (it.hasNext()){
                ids.append(",");
            }
        }
        this.agentMenuLimitService.updateAgentMenuLimits(ids.toString(),(long)agent.getId());
        // 加载下缓存
        databaseCache.init();
    }

    /**
     * 检查域名是否符合配置
     * @param domainName
     * @param agentAccount
     */
    @Override
    public void checkDomainName(String domainName,String agentAccount){
        if(StringUtils.isNotEmpty(domainName)){
            Agent agent = this.findByAccount(agentAccount);
            if(agent != null){
                LambdaQueryWrapper<AgentSystemConfig> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(AgentSystemConfig::getAgentId,agent.getId());
                queryWrapper.eq(AgentSystemConfig::getApproveStateCode, OrgApproveStateEnums.SUCCESS.getCode());
                AgentSystemConfig agentSystemConfig = agentSystemConfigService.getOne(queryWrapper);
                if (agentSystemConfig != null){
                    if(!agentSystemConfig.getSystemUrl().equals(domainName)){
                        throw new FebsException("账号不存在！");
                    }
                }else{
                    if(!URLUtil.isIp(domainName)) throw new FebsException("账号不存在！");
                }
            }
        }
    }

    /**
     * 检验谷歌验证码
     * @param username
     * @param verifyCode
     */
    @Override
    public void checkGoogleKey(String username, String verifyCode) {
        Agent agent = this.findByAccount(username);
        if(agent == null){
            throw new FebsException("用户名或密码错误！");
        }else{
            if(StringUtils.isBlank(agent.getGoogleKey())) return;
            try {
                boolean bl = GoogleAuthenticator.verifyGoogleKey(verifyCode, agent.getGoogleKey());
                if(!bl) throw new FebsException("谷歌码有误！");
            } catch (Exception e) {
                throw new FebsException("谷歌码有误！");
            }
        }
    }

    /**
     * 绑定谷歌
     * @param agent
     * @param goologoVerifyCode
     * @param googleKey
     */
    @Override
    public void bindGoogle(Agent agent, String goologoVerifyCode, String googleKey,String payPassword){
        if(!GoogleAuthenticator.verifyGoogleKey(goologoVerifyCode, googleKey)){
            throw new FebsException("谷歌码有误！");
        }
        if(!agent.getPayPassword().equals(MD5Util.MD5(payPassword))){
            throw new FebsException("支付密码有误！");
        }
        Agent newAgent = new Agent();
        newAgent.setId(agent.getId());
        newAgent.setGoogleKey(googleKey);
        this.updateById(newAgent);
    }

    /**
     * 清除代理谷歌Key
     * @param agent
     */
    @Override
    public void removeGoogleKey(Agent agent, String goologoVerifyCode,String payPassword){
        if(!GoogleAuthenticator.verifyGoogleKey(goologoVerifyCode, agent.getGoogleKey())){
            throw new FebsException("谷歌码有误！");
        }
        if(!agent.getPayPassword().equals(MD5Util.MD5(payPassword))){
            throw new FebsException("支付密码有误！");
        }
        Agent newAgent = new Agent();
        newAgent.setId(agent.getId());
        newAgent.setGoogleKey("");
        this.updateById(newAgent);
    }

    @Override
    public String getDataMd5(Agent agent){
        StringBuffer str = new StringBuffer();
        str.append(Md5Key);
        str.append(agent.getAgentAccount());
        str.append(agent.getAmount());
        str.append(agent.getAvailableAmount());
        str.append(agent.getCashAmount());
        str.append(agent.getSendSmsTotal());
        str.append(agent.getQuotaAmount());
        return MD5Util.MD5(str.toString());
    }
    @Override
    public boolean checkDataMd5(Agent agent){
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Agent::getAgentName,agent.getAgentName());
        Agent oldagent = this.baseMapper.selectOne(queryWrapper);
        if(oldagent != null && oldagent.getDataMd5().equals(getDataMd5(agent))){
            return true;
        }
        return false;
    }

    /**
     * 代理端修改代理
     * @param agent
     * @param upAgent
     */
    @Override
    @Transactional
    public void updateAgent(Agent agent,Agent upAgent) {
        Agent oldAgent = this.getById(agent.getId());
        if(!oldAgent.getUpAgentId().equals(upAgent.getId())){
            throw new FebsException("只能修改本代理的下级代理");
        }
        updateAgent(agent);
    }

    @Override
    @Transactional
    public void updateAgent(Agent agent) {
        if(agent == null){
            throw new FebsException("账号信息不完整");
        }
        if(DatabaseCache.getCodeBySortCodeAndCode("StateEnum",agent.getStateCode()) == null){
            throw new FebsException("状态值错误");
        }
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getAgentName,agent.getAgentName());
        wrapper.ne(Agent::getId,agent.getId());
        List<Agent> oldAgentList = this.list(wrapper);
        if(oldAgentList.size() !=0 ){
            throw new FebsException("该名称已存在");
        }
        Agent newAgent = new Agent();
        newAgent.setStateCode(agent.getStateCode());
        newAgent.setAgentName(agent.getAgentName());
        newAgent.setPhoneNumber(agent.getPhoneNumber());
        newAgent.setEmail(agent.getEmail());
        newAgent.setQq(agent.getQq());
        newAgent.setDayLimit(agent.getDayLimit());
        newAgent.setDescription(agent.getDescription());
        newAgent.setRemark(agent.getRemark());
        newAgent.setLoginFaildCount(0);
        newAgent.setId(agent.getId());
        //新增两个字段 2020-12-09
        newAgent.setNeedBindGoogleKey(agent.getNeedBindGoogleKey());
        newAgent.setLoginIp(agent.getLoginIp());
        this.baseMapper.updateById(newAgent);
    }

    @Override
    @Transactional
    public void deleteAgent(Agent agent) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
        // 加载下缓存
        databaseCache.init();
	}

    @Override
    @Transactional
    public void deleteAgents(String[] agentIds) {
        List<String> list = Arrays.asList(agentIds);
        this.removeByIds(list);
        // 加载下缓存
        databaseCache.init();
    }

    @Override
    public AgentExt queryAgentExtById(Integer id) {
        return this.baseMapper.queryAgentExtById(id);
    }

    @Override
    public Agent queryOrgByIdForUpdate(Integer agentId) {
        LambdaQueryWrapper<Agent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Agent::getId,agentId).last("FOR UPDATE");
        List<Agent> agents = list(queryWrapper);
        if (agents == null || agents.isEmpty() || agents.size() > 1) {
            return null;
        }
        return agents.get(0);
    }


    /**
     * 批量重置密码
     * @param agentAccounts
     */
    @Override
    @Transactional
    public void resetPasswordAgents(String[] agentAccounts,Agent upAgent) {
        Arrays.stream(agentAccounts).forEach(agentAccount -> {
            Agent agent = new Agent();
            String passWord =  DatabaseCache.getCodeBySortCodeAndCode("AgentPropertyConfig","defaultPassWord").getName();
            String defaultPassWord = DateUtils.getString(DateUtils.Y_M_D_2);
            if(passWord!=null&&!"".equals(passWord))
            {
            	defaultPassWord = passWord;
            }
            agent.setAgentPassword(MD5Util.encrypt(agentAccount, defaultPassWord));
            if(upAgent != null){
                this.baseMapper.update(agent, new LambdaQueryWrapper<Agent>().eq(Agent::getAgentAccount, agentAccount).eq(Agent::getUpAgentId,upAgent.getId()));
            }else {
                this.baseMapper.update(agent, new LambdaQueryWrapper<Agent>().eq(Agent::getAgentAccount, agentAccount));
            }
        });
    }

    /**
     * 更新登录时间
     * @param username
     */
    @Override
    @Transactional
    public void updateLoginTime(String username, String ip) {
        Agent agent = new Agent();
        agent.setLastLoginIp(AddressUtil.getCityInfo(ip));
//        agent.setRemark(ip);
        agent.setLoginIp(ip);
        agent.setLastLoginTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
        this.baseMapper.update(agent, new LambdaQueryWrapper<Agent>().eq(Agent::getAgentAccount,username));
    }

    /**
     * 根根据代理用户名查询
     * @param agentAccount
     * @return
     */
    @Override
    @Transactional
    public Agent findByAccount(String agentAccount) {
        LambdaQueryWrapper<Agent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Agent::getAgentAccount,agentAccount);
        return this.agentMapper.selectOne(wrapper);
    }

    /**
     * 修改密码
     * @param agentAccount
     * @param password
     */
    @Override
    @Transactional
    public void updatePassword(String agentAccount, String password) {
        Agent agent = new Agent();
        agent.setAgentPassword(MD5Util.encrypt(agentAccount, password));
        this.baseMapper.update(agent, new LambdaQueryWrapper<Agent>().eq(Agent::getAgentAccount, agentAccount));
    }

    /**
     * 修改支付密码
     * @param agentAccount
     * @param password
     */
    @Override
    @Transactional
    public void updatePayPassword(String agentAccount, String password) {
        Agent agent = new Agent();
        agent.setPayPassword(MD5Util.MD5(password));
        this.baseMapper.update(agent, new LambdaQueryWrapper<Agent>().eq(Agent::getAgentAccount, agentAccount));
    }

    /**
     *  代理统计
     * @param agent
     * @return
     */
    @Override
    @Transactional
    public String statisticalOnAgent(Agent agent){
        // 统计商户数量
        LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
        orgWrapper.eq(Organization::getAgentId,agent.getId());
        int orgCount = this.organizationService.count(orgWrapper);
        List<Map<String,Object>> list = this.reportService.sumSendRecordByAgent(agent);
        Agent newAgent = this.getById(agent.getId());
        JSONObject data = JSONObject.parseObject("{}");
        BigDecimal total = new BigDecimal(0);
        BigDecimal  successTotal = new BigDecimal(0);
        BigDecimal  successAmount = new BigDecimal(0);
        BigDecimal  consumeAmount = new BigDecimal(0);
        JSONArray dataArray = JSONObject.parseArray("[]");
        for (Map<String,Object> map: list) {
            total = total.add(new BigDecimal((Long) map.get("total")));
            successTotal = successTotal.add(new BigDecimal((Long) map.get("successTotal")));
            successAmount = successAmount.add((BigDecimal) map.get("successAmount"));
            consumeAmount = consumeAmount.add((BigDecimal) map.get("consumeAmount"));
            if(dataArray.size() < 15){
                JSONObject content = JSONObject.parseObject("{}");
                content.put("orgCode",map.get("org_code"));
                String orgName = DatabaseCache.getOrgNameByOrgcode(String.valueOf(map.get("org_code")));
                content.put("orgName",orgName);
                content.put("total",map.get("total"));
                dataArray.add(content);
            }
        }
        Integer levelCode = DatabaseCache.getCodeBySortCodeAndCode("System","levelCode") == null ? 2 :
                Integer.parseInt(DatabaseCache.getCodeBySortCodeAndCode("System","levelCode").getName());
        if(Integer.parseInt(agent.getLevelCode()) < levelCode){
            List<Map<String,Object>> lowerList = this.reportService.sumLowerSendRecordByAgent(agent);
            for (Map<String,Object> map: lowerList) {
                total = total.add(new BigDecimal((Long) map.get("total")));
                successTotal = successTotal.add(new BigDecimal((Long) map.get("successTotal")));
                successAmount = successAmount.add((BigDecimal) map.get("upAgentSuccessAmount"));
                consumeAmount = consumeAmount.add((BigDecimal) map.get("consumeAmount"));
            }
        }
        data.put("orgCount",orgCount);
        data.put("total",total);
        data.put("successTotal",successTotal);
        data.put("successAmount",successAmount);
        data.put("consumeAmount",consumeAmount);
        data.put("agent",newAgent);
        data.put("data",dataArray);
        return data.toJSONString();
    }

    /**
     * 获取代理缓存
     * @return
     */
    @Override
    public List<Map<Integer,String>> getAgents(){
        List<Map<Integer,String>> list = new ArrayList<>();
        for (Agent agent: DatabaseCache.getAgentList()) {
            Map<Integer,String> map = new HashMap<>();
            map.put(agent.getId(),agent.getAgentName());
            list.add(map);
        }
        return list;
    }

    /**
     * 获取上级代理缓存
     * @return
     */
    @Override
    public List<Map<Integer,String>> getUpAgents(){
        Integer levelCode = DatabaseCache.getCodeBySortCodeAndCode("System","levelCode") == null ? 2 :
                Integer.parseInt(DatabaseCache.getCodeBySortCodeAndCode("System","levelCode").getName());
        List<Map<Integer,String>> list = new ArrayList<>();
        for (Agent agent: DatabaseCache.getAgentList()) {
            if(Integer.parseInt(agent.getLevelCode()) >= levelCode){
                continue;
            }
            Map<Integer,String> map = new HashMap<>();
            map.put(agent.getId(),agent.getAgentName());
            list.add(map);
        }
        return list;
    }

    /**
     * 获取当前代理及下级代理缓存
     * @return
     */
    @Override
    public List<Map<Integer,String>> getCurrentAgents(Agent current){
        List<Map<Integer,String>> list = new ArrayList<>();
        for (Agent agent: DatabaseCache.getAgentList()) {
            if(agent.getId().equals(current.getId()) || (agent.getUpAgentId() != null && agent.getUpAgentId().equals(current.getId()))){
                Map<Integer,String> map = new HashMap<>();
                map.put(agent.getId(),agent.getAgentName());
                list.add(map);
            }
        }
        return list;
    }
}
