package com.hero.sms.service.impl.organization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.AppUtil;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.common.utils.RandomStringUtil;
import com.hero.sms.common.utils.RegexUtil;
import com.hero.sms.common.utils.URLUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentSystemConfig;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.MqPushHttpBody;
import com.hero.sms.entity.message.NotifyMsgStateModel;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.entity.organization.OrganizationGroup;
import com.hero.sms.entity.organization.OrganizationQuery;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.OrganizationUserMenu;
import com.hero.sms.entity.organization.ext.OrganizationExt;
import com.hero.sms.entity.organization.ext.OrganizationExtGroup;
import com.hero.sms.entity.rechargeOrder.AgentRechargeOrder;
import com.hero.sms.enums.message.SendBoxSubTypeEnums;
import com.hero.sms.enums.organization.OrgAmountHandleEnums;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.enums.organization.OrgChargesTypeEnums;
import com.hero.sms.enums.organization.OrgCostStateEnums;
import com.hero.sms.enums.organization.OrgInterfaceTypeEnums;
import com.hero.sms.enums.organization.OrgSettlementTypeEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.mapper.organization.OrganizationMapper;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.agent.IAgentSystemConfigService;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.service.mq.MQService;
import com.hero.sms.service.organization.IOrganizationGroupService;
import com.hero.sms.service.organization.IOrganizationPropertyService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserLimitService;
import com.hero.sms.service.organization.IOrganizationUserMenuService;
import com.hero.sms.service.organization.IOrganizationUserService;
import com.hero.sms.service.rechargeOrder.IAgentRechargeOrderService;
/**
 * 商户信息 Service实现
 *
 * @author Administrator
 * @date 2020-03-07 17:24:55
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements IOrganizationService {

    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private IOrganizationUserService organizationUserService;
    @Autowired
    private IOrganizationUserLimitService organizationUserLimitService;
    @Autowired
    private IOrganizationUserMenuService organizationUserMenuService;
    @Autowired
    private IAgentService agentService;
    @Autowired
    private IAgentRechargeOrderService agentRechargeOrderService;
    @Autowired
    private DatabaseCache databaseCache;
    @Autowired
    private MQService mqService;
    @Autowired
    private IAgentSystemConfigService agentSystemConfigService;
    @Autowired
    private IOrganizationPropertyService organizationPropertyService;
    @Autowired
    private IBusinessManage businessManage;
    @Autowired
    private IOrganizationGroupService organizationGroupService;
    public static final String Md5Key = "dgfjokijorewdfsjfixcvjewr";

    @Override
    public IPage<Organization> findOrganizations(QueryRequest request, Organization organization) {
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(organization.getBusinessUserId() != null){
            queryWrapper.eq(Organization::getBusinessUserId,organization.getBusinessUserId());
        }
        if(organization.getAgentId() != null){
            queryWrapper.eq(Organization::getAgentId,organization.getAgentId());
        }
        if(StringUtils.isNotBlank(organization.getOrganizationName())){
            queryWrapper.like(Organization::getOrganizationName,organization.getOrganizationName());
        }
        if(StringUtils.isNotBlank(organization.getOrganizationCode())){
            queryWrapper.eq(Organization::getOrganizationCode,organization.getOrganizationCode());
        }
        if(StringUtils.isNotBlank(organization.getStatus())){
            queryWrapper.eq(Organization::getStatus,organization.getStatus());
        }
        if(StringUtils.isNotBlank(organization.getApproveStateCode())){
            queryWrapper.eq(Organization::getApproveStateCode,organization.getApproveStateCode());
        }
        queryWrapper.orderByDesc(Organization::getId);
        Page<Organization> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public IPage<OrganizationExtGroup> findOrganizationExtGroups(QueryRequest request, OrganizationQuery organization) {
        Page<OrganizationExtGroup> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.findListContainsGroup(page,organization);
    }

    @Override
    public List<Organization> findOrganizations(Organization organization) {
	    LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public Organization getOrganizationByCode(String orgCode) {
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Organization::getOrganizationCode,orgCode);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public void createOrganization(Organization organization,String userAccount,String userPassword) 
    {
    	//默认“非强制”绑定google
    	int needBindGoogleKey = 0;
    	String orgDefaultNeedBindgoogleKey = "0";
		Code orgDefaultNeedBindgoogleKeyCode = DatabaseCache.getCodeBySortCodeAndCode("System","orgDefaultNeedBindgoogleKey");
	    if(orgDefaultNeedBindgoogleKeyCode!=null&&!"".equals(orgDefaultNeedBindgoogleKeyCode.getName()))
	    {
	    	orgDefaultNeedBindgoogleKey = orgDefaultNeedBindgoogleKeyCode.getName();
	    }
	    if("1".equals(orgDefaultNeedBindgoogleKey))
	    {
	    	needBindGoogleKey = 1;
	    }
    	createOrganization(organization, userAccount, userPassword, needBindGoogleKey);
    }
    
    @Override
    @Transactional
    public void createOrganization(Organization organization,String userAccount,String userPassword,int needBindGoogleKey) {
        if(organization == null || organization.getAgentId() == null || StringUtils.isEmpty(userAccount) || StringUtils.isEmpty(userPassword)){
            throw new FebsException("账号信息不完整");
        }
        //过滤登录账号前后空格 2020-12-11
        userAccount = userAccount.trim();
        if(StringUtils.isEmpty(userAccount))
        {
            throw new FebsException("设置用户名错误,用户名不能为空格");
        }
        //校验登录账号为英文与数字  2021-05-05
        if(!RegexUtil.isUserAccount(userAccount))
        {
        	throw new FebsException("登录账号只能20位以内的英文、数字或英文数字组合");
        }
      //过滤前后空格 2020-12-11
        userPassword = userPassword.trim();
        if(StringUtils.isEmpty(userPassword))
        {
            throw new FebsException("设置密码错误,密码不能为空格");
        }
        
        //2020/7/7  去掉电话号码必填限制
        if(!StringUtils.isEmpty(organization.getContactMobile()) && !AppUtil.match("^[1][3,4,5,7,8][0-9]{9}$",organization.getContactMobile())){
            throw new FebsException("手机号不正确");
        }
        LambdaQueryWrapper<Organization> orgQueryWrapper = new LambdaQueryWrapper<>();
        orgQueryWrapper.eq(Organization::getOrganizationName,organization.getOrganizationName());
        List<Organization> orgList = this.baseMapper.selectList(orgQueryWrapper);
        if(orgList.size() != 0){
            throw new FebsException("商户名称已存在");
        }
        organization.setSettlementType(OrgSettlementTypeEnums.Prepayment.getCode());
        organization.setChargesType(OrgChargesTypeEnums.SUBMIT.getCode());
        organization.setAmount(0L);
        organization.setAvailableAmount(0L);
        organization.setCashAmount(0L);
        organization.setSendSmsTotal(0L);
        organization.setStatus(OrgStatusEnums.Normal.getCode());
        organization.setApproveStateCode(OrgApproveStateEnums.SUCCESS.getCode());
        String orgPrefix = DatabaseCache.getCodeBySortCodeAndCode("System","orgPrefix").getName();
        String orgcode = DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(4,RandomStringUtil.BIG);
        if(orgPrefix!=null&&!"".equals(orgPrefix))
        {
        	orgcode = orgPrefix +  DateUtil.getDateFormat(new Date(),"yyMMddHHmmss") + RandomStringUtil.randomStr(4,RandomStringUtil.BIG);
        }
        organization.setOrganizationCode(orgcode);
        organization.setMd5Key(RandomStringUtil.randomStr(32,RandomStringUtil.BIG));
        organization.setDataMd5(getDataMd5(organization));
        organization.setCreateDate(new Date());
        String groupIds = organization.getRemark();
        organization.setRemark("");
        this.save(organization);

        //是否开通SMPP接口，是则创建默认SMPP属性
        Integer interfaceType= organization.getInterfaceType();
        Integer smppTypeValue = OrgInterfaceTypeEnums.Smpp.getCode();
        String data = null;
        if (interfaceType != null && ((interfaceType & smppTypeValue) == smppTypeValue)){
            organizationPropertyService.createDefaultSmppProperty(organization.getOrganizationCode(),"创建商户生成默认属性");
            data = JSON.toJSONString(organizationMapper.findContainPropertyById(organization.getId()));
        }else {
        	data = JSON.toJSONString(organization);
        }
        FebsResponse loadGateServerRes = businessManage.reqConstrolSmsGateServer(data);
        if(((Integer)loadGateServerRes.get("code")).intValue() != HttpStatus.OK.value()) {
			throw new FebsException("网关服务端控制失败！");
		}
        //新增商户用户
        OrganizationUser user = new OrganizationUser();
        user.setCreateDate(new Date());
        user.setCreateUser(organization.getCreateUser());
        user.setOrganizationCode(organization.getOrganizationCode());
        user.setUserAccount(userAccount);
        user.setUserName(organization.getOrganizationName());
        user.setUserPassword(userPassword);
        user.setNeedBindGoogleKey(needBindGoogleKey);
        user.setStatus(OrgStatusEnums.Normal.getCode());
        user.setAccountType(DatabaseCache.getCodeBySortCodeAndName("AccountType","主账户").getCode());
        this.organizationUserService.createOrganizationUser(user);
        OrganizationUserMenu queryUserMenu = new OrganizationUserMenu();
        queryUserMenu.setAuth("0");//筛选有权限赋予的菜单
        List<OrganizationUserMenu> menus = this.organizationUserMenuService.findOrganizationUserMenus(queryUserMenu);
        //新增商户用户权限
        StringBuffer ids = new StringBuffer();
        for (Iterator<OrganizationUserMenu> it = menus.iterator(); it.hasNext(); ) {
            OrganizationUserMenu menu = it.next();
            ids.append(menu.getMenuId());
            if (it.hasNext()){
                ids.append(",");
            }
        }
        this.organizationUserLimitService.updateeOrganizationUserLimits(ids.toString(),(long) user.getId());
        
        /**
         * @begin 2021-03-05
         * 新建商户时，设置分组
         */
        if(StringUtils.isNotBlank(groupIds))
    	{
    		try 
    		{
    			organizationGroupService.incrementSaveOrganizationGroup(organization.getOrganizationCode(),groupIds);
			} 
    		catch (Exception e) {}
    	}
        else
        {
        	  //商户添加默认分组
            OrganizationGroup organizationGroup = new OrganizationGroup();
            organizationGroup.setGroupId("1");//默认组id固定值为1
            organizationGroup.setOrgCode(organization.getOrganizationCode());
            organizationGroup.setCreateTime(new Date());
            organizationGroupService.createOrganizationGroup(organizationGroup);
        }
        /**
         * @end
         */
        //重新加载缓存
        databaseCache.init();
    }

    @Override
    public String getDataMd5(Organization organization){
        StringBuffer str = new StringBuffer();
        str.append(Md5Key);
        str.append(organization.getOrganizationCode());
        str.append(organization.getAmount());
        str.append(organization.getAvailableAmount());
        str.append(organization.getCashAmount());
        str.append(organization.getSendSmsTotal());
        str.append(organization.getMd5Key());
        str.append(organization.getSmsSign());
        str.append(organization.getBindIp());
        return MD5Util.MD5(str.toString());
    }

    @Override
    public boolean checkDataMd5(Organization organization){
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Organization::getOrganizationCode,organization.getOrganizationCode());
        Organization oldOrganization = this.baseMapper.selectOne(queryWrapper);
        if(oldOrganization != null && oldOrganization.getDataMd5().equals(getDataMd5(organization))){
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void updateOrganization(Organization organization) {
        if(organization == null){
            throw new FebsException("账号信息不完整");
        }
        boolean exist = true;
        for (Agent agent:DatabaseCache.getAgentList()){
            if (agent.getId().equals(organization.getAgentId())){
                exist = false;
            }
        }
        if (exist){
            throw new FebsException("请选择代理");
        }
        Organization oldOrganization = this.baseMapper.selectById(organization.getId());
        if(oldOrganization ==null || !oldOrganization.getOrganizationCode().equals(organization.getOrganizationCode())){
            throw new FebsException("商户信息错误");
        }
        if(OrgStatusEnums.Cancel.getCode().equals(oldOrganization.getStatus())){
            throw new FebsException("该商户已经作废，无法操作！");
        }
        LambdaQueryWrapper<Organization> orgQueryWrapper = new LambdaQueryWrapper<>();
        orgQueryWrapper.eq(Organization::getOrganizationName,organization.getOrganizationName());
        orgQueryWrapper.ne(Organization::getId,organization.getId());
        List<Organization> orgList = this.baseMapper.selectList(orgQueryWrapper);
        if(orgList.size() != 0){
            throw new FebsException("商户名称已存在");
        }
        Organization newOrganization = new Organization();
        newOrganization.setOrganizationName(organization.getOrganizationName());
        //newOrganization.setStatus(organization.getStatus());
        if(organization.getBusinessUserId() == null){
            newOrganization.setBusinessUserId(0);
        }else {
            newOrganization.setBusinessUserId(organization.getBusinessUserId());
        }
        newOrganization.setAddress(organization.getAddress());
        newOrganization.setContact(organization.getContact());
        newOrganization.setContactMobile(organization.getContactMobile());
        newOrganization.setEmail(organization.getEmail());
        newOrganization.setWebUrl(organization.getWebUrl());
        newOrganization.setSmsSign(organization.getSmsSign());
        newOrganization.setInterfaceType(organization.getInterfaceType());
/*        newOrganization.setChargesType(organization.getChargesType());
        newOrganization.setSettlementType(organization.getSettlementType());*/
        newOrganization.setSmsApproveType(organization.getSmsApproveType());
        newOrganization.setBindIp(organization.getBindIp());
        newOrganization.setNotifyUrl(organization.getNotifyUrl());
        newOrganization.setDescription(organization.getDescription());
        oldOrganization.setBindIp(organization.getBindIp());
        newOrganization.setDataMd5(getDataMd5(oldOrganization));
        newOrganization.setId(organization.getId());
        this.baseMapper.updateById(newOrganization);

        //是否开通SMPP接口，是则创建默认SMPP属性
        Integer interfaceType= organization.getInterfaceType();
        Integer smppTypeValue = OrgInterfaceTypeEnums.Smpp.getCode();
        String data = null;
        if (interfaceType != null && ((interfaceType & smppTypeValue) == smppTypeValue)){
            this.organizationPropertyService.createDefaultSmppProperty(oldOrganization.getOrganizationCode(),"修改商户生成默认属性");
            data = JSON.toJSONString(organizationMapper.findContainPropertyById(organization.getId()));
        }else {
        	data = JSON.toJSONString(organization);
        }
        FebsResponse loadGateServerRes = businessManage.reqConstrolSmsGateServer(data);
        if(((Integer)loadGateServerRes.get("code")).intValue() != HttpStatus.OK.value()) {
			throw new FebsException("网关服务端控制失败！");
		}
    }

    /**
     * 更新商户判断代理
     * @param organization
     * @param agentId
     */
    @Override
    @Transactional
    public void updateOrganization(Organization organization,int agentId) {
        if(organization == null){
            throw new FebsException("账号信息不完整");
        }
        Organization oldOrganization = this.baseMapper.selectById(organization.getId());
        if(oldOrganization ==null || !oldOrganization.getOrganizationCode().equals(organization.getOrganizationCode()) ||
            !oldOrganization.getAgentId().equals(agentId)){
            throw new FebsException("商户信息错误");
        }
        if(OrgStatusEnums.Cancel.getCode().equals(oldOrganization.getStatus())){
            throw new FebsException("该商户已经作废，无法操作！");
        }
        LambdaQueryWrapper<Organization> orgQueryWrapper = new LambdaQueryWrapper<>();
        orgQueryWrapper.eq(Organization::getOrganizationName,organization.getOrganizationName());
        orgQueryWrapper.ne(Organization::getId,organization.getId());
        List<Organization> orgList = this.baseMapper.selectList(orgQueryWrapper);
        if(orgList.size() != 0){
            throw new FebsException("商户名称已存在");
        }
        Organization newOrganization = new Organization();
        newOrganization.setOrganizationName(organization.getOrganizationName());
        //newOrganization.setStatus(organization.getStatus());
        newOrganization.setAddress(organization.getAddress());
        newOrganization.setContact(organization.getContact());
        newOrganization.setContactMobile(organization.getContactMobile());
        newOrganization.setEmail(organization.getEmail());
        newOrganization.setWebUrl(organization.getWebUrl());
        newOrganization.setSmsSign(organization.getSmsSign());
     /*   newOrganization.setChargesType(organization.getChargesType());
        newOrganization.setSettlementType(organization.getSettlementType());*/
        newOrganization.setSmsApproveType(organization.getSmsApproveType());
        newOrganization.setBindIp(organization.getBindIp());
        newOrganization.setDescription(organization.getDescription());
        oldOrganization.setBindIp(organization.getBindIp());
        newOrganization.setDataMd5(getDataMd5(oldOrganization));
        newOrganization.setId(organization.getId());
        this.baseMapper.updateById(newOrganization);
    }

    @Override
    @Transactional
    public void deleteOrganization(Organization organization) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizations(String[] organizationIds) {
        List<String> list = Arrays.asList(organizationIds);
        QueryWrapper<Organization> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("o.id",list);
        this.organizationMapper.deleteOrganizationUserByOrganization(queryWrapper);
        this.removeByIds(list);
        // 加载下缓存
        databaseCache.init();
    }

    /**
     * 批量认证
     * @param organizationIds
     */
    @Override
    @Transactional
    public void approveOrgs(String[] organizationIds,String approveState) {
        List<String> list = Arrays.asList(organizationIds);
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Organization::getId,list);
        Organization organization = new Organization();
        organization.setApproveStateCode(approveState);
        this.update(organization,queryWrapper);
    }

    @Override
    public Organization queryOrgByCodeForUpdate(String code) {
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Organization::getOrganizationCode,code).last("FOR UPDATE");
        List<Organization> orgs = list(queryWrapper);
        if (orgs == null || orgs.isEmpty() || orgs.size() > 1) {
            return null;
        }
        return orgs.get(0);
    }
    @Override
    public Organization queryOrgByCode(String code) {
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Organization::getOrganizationCode,code);
        List<Organization> orgs = list(queryWrapper);
        if (orgs == null || orgs.isEmpty() || orgs.size() > 1) {
            return null;
        }
        return orgs.get(0);
    }

    @Override
    @Transactional
    public void cancelOrganization(String organizationCode,String orgAmountHandle,String currentUserName) {
        if(StringUtils.isEmpty(organizationCode)  || StringUtils.isEmpty(orgAmountHandle)){
            throw new FebsException("填写信息不完整");
        }
        Organization queryOrgForUpdate = this.queryOrgByCodeForUpdate(organizationCode);
        if(queryOrgForUpdate == null || !queryOrgForUpdate.getDataMd5().equals(this.getDataMd5(queryOrgForUpdate))){
            throw new FebsException("商户信息错误");
        }
        if(OrgStatusEnums.Cancel.getCode().equals(queryOrgForUpdate.getStatus())){
            throw new FebsException("该商户已经作废，无法操作！");
        }
        Organization updateOrg = new Organization();
        updateOrg.setId(queryOrgForUpdate.getId());
        updateOrg.setStatus(OrgStatusEnums.Cancel.getCode());
        if(queryOrgForUpdate.getAvailableAmount()>0 && OrgAmountHandleEnums.ReturnAgent.getCode().equals(orgAmountHandle)){//余额转入代理
            Agent queryAgentForUpdate = this.agentService.queryOrgByIdForUpdate(queryOrgForUpdate.getAgentId());
            if(queryAgentForUpdate == null){
                throw new FebsException("该代理不存在！");
            }
            if(!queryAgentForUpdate.getDataMd5().equals(agentService.getDataMd5(queryAgentForUpdate))){
                throw new FebsException("该代理信息异常！");
            }
            queryAgentForUpdate.setAmount(queryAgentForUpdate.getAmount()+queryOrgForUpdate.getAvailableAmount());
            queryAgentForUpdate.setQuotaAmount(queryAgentForUpdate.getQuotaAmount()+queryOrgForUpdate.getAvailableAmount());
            Agent updateAgent = new Agent();
            updateAgent.setId(queryAgentForUpdate.getId());
            updateAgent.setAmount(queryAgentForUpdate.getAmount());
            updateAgent.setQuotaAmount(queryAgentForUpdate.getQuotaAmount());
            updateAgent.setDataMd5(agentService.getDataMd5(queryAgentForUpdate));
            this.agentService.updateById(updateAgent);
            //写入代理充值记录
            AgentRechargeOrder agentRechargeOrder = new AgentRechargeOrder();
            agentRechargeOrder.setAgentId(queryAgentForUpdate.getId().toString());
            agentRechargeOrder.setOrderNo(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_PATTERN) + RandomStringUtil.randomStr(6));
            agentRechargeOrder.setPayChannelId("");
            agentRechargeOrder.setPayMerchNo(queryOrgForUpdate.getOrganizationCode());
            agentRechargeOrder.setRemark(queryOrgForUpdate.getOrganizationName());
            agentRechargeOrder.setRechargeType(DatabaseCache.getCodeBySortCodeAndName("RechargeType","平台充值").getCode());
            agentRechargeOrder.setNetwayCode("MERCH");//商户作废转入
            agentRechargeOrder.setRechargeAmount(queryOrgForUpdate.getAvailableAmount().intValue());
            agentRechargeOrder.setAvailableAmount(updateAgent.getQuotaAmount().intValue());
            agentRechargeOrder.setReqStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
            agentRechargeOrder.setReqTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
            agentRechargeOrder.setRechargeStateCode(DatabaseCache.getCodeBySortCodeAndName("PayStateType","成功").getCode());
            agentRechargeOrder.setRechargeTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
            agentRechargeOrder.setRechargeCompleteTime(DateUtil.getDateFormat(new Date(),DateUtil.FULL_TIME_SPLIT_PATTERN));
            agentRechargeOrder.setCeateUser(currentUserName);
            agentRechargeOrder.setCreateTime(new Date());
            this.agentRechargeOrderService.save(agentRechargeOrder);

            queryOrgForUpdate.setAvailableAmount(0L);
            updateOrg.setAvailableAmount(0L);
            updateOrg.setDataMd5(this.getDataMd5(queryOrgForUpdate));
        }
        this.updateById(updateOrg);
    }

    @Override
    public OrganizationExt queryOrganizationExtByOrgCode(String orgCode) {
        return this.baseMapper.queryOrganizationExtByOrgCode(orgCode);
    }


    /**
     * 获取商户缓存
     * @return
     */
    @Override
    public List<Map<String,String>> getOrgs(){
        List<Map<String,String>> list = new ArrayList<>();
        for (Organization org: DatabaseCache.getOrganizationList()) {
            Map<String,String> map = new HashMap<>();
            map.put(org.getOrganizationCode(),org.getOrganizationName());
            list.add(map);
        }
        return list;
    }
    /**
     * 根据代理ID获取商户缓存
     * @return
     */
    @Override
    public List<Map<String,String>> getOrgsByAgentId(Integer id){
        List<Map<String,String>> list = new ArrayList<>();
        for (Organization org: DatabaseCache.getOrganizationListByAgentId(id)) {
            Map<String,String> map = new HashMap<>();
            map.put(org.getOrganizationCode(),org.getOrganizationName());
            list.add(map);
        }
        return list;
    }

	@Override
	public void notifyMsgState(Organization org,NotifyMsgStateModel model) throws Exception {
		if(org == null)return;
		if(model.getSubType().intValue() == SendBoxSubTypeEnums.HttpSub.getCode().intValue()) {
			if(StringUtils.isBlank(org.getNotifyUrl()))return;
			StringBuilder checkParam = new StringBuilder();
			/**
			 * @bigen
			 * 2020-09-16
			 * 调整通知返回的参数定义，与原系统做个简单区别
			 */
			String state = model.getState();
			String msg = model.getMsg();
			if("16".equals(state))
			{
				model.setState("0");
				model.setMsg("发送成功");
			}
			else
			{
				model.setState("1");
				model.setMsg("发送失败（"+msg+"）");
			}
			checkParam.append(model.getOrgCode()).append("|").append(model.getSendCode()).append("|").append(model.getState()).append("|").append(model.getMobile()).append("|").append(org.getMd5Key());
			/**
			 * @end
			 */
			String sign = MD5Util.MD5(checkParam.toString());
			Map<String,Object> paramMap =new HashMap<String, Object>();
			paramMap.put("orgCode", model.getOrgCode());
			paramMap.put("sendCode", model.getSendCode());
			paramMap.put("sendState", model.getState());
			paramMap.put("phone", model.getMobile());
			paramMap.put("phoneArea", model.getMobileArea());
			paramMap.put("message", model.getMsg());
			paramMap.put("sign", sign);
			MqPushHttpBody body = new MqPushHttpBody();
			body.setNotifyUrl(org.getNotifyUrl());
			body.setParams(paramMap);
			body.setRand(RandomStringUtil.randomStr(6));
			//推送HTTP协议的通知MQ
			//4-1-1-1-1-1 将结果按我方协议进行封装，以HTTP方式推送到MQ
			mqService.httpNotify(body);
		}
		if(model.getSubType().intValue() == SendBoxSubTypeEnums.SmppSub.getCode().intValue()) {
			//推送SMPP协议的通知MQ
			//4-1-1-1-1-2 将结果按我方协议进行封装，以SMPP方式推送到MQ
			mqService.gateNotify(OrgInterfaceTypeEnums.Smpp.getCode(), model);
		}
	}

    /**
     * 检查域名是否符合配置
     * @param domainName
     * @param userAccount
     */
    @Override
    public void checkDomainName(String domainName, String userAccount){
        if(StringUtils.isNotEmpty(domainName)){
            Organization org = this.organizationMapper.queryOrganizationByUserAccount(userAccount);
            if(org != null){
                LambdaQueryWrapper<AgentSystemConfig> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(AgentSystemConfig::getAgentId,org.getAgentId());
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

	@Override
	public List<OrganizationExt> findListContainProperty(Organization organization) {
		return organizationMapper.findListContainProperty(organization);
	}

	@Override
	public boolean checkOrgAmountPrePay(String orgCode, Integer smsType, String costName, int smsCount) throws ServiceException {
        OrganizationExt org = this.queryOrganizationExtByOrgCode(orgCode);

        if (org == null){
            throw new ServiceException("商户不存在！");
        }
        if (!OrgStatusEnums.Normal.getCode().equals(org.getStatus())){
            throw new ServiceException("商户状态被锁！");
        }
        if (!OrgApproveStateEnums.SUCCESS.getCode().equals(org.getApproveStateCode())){
            throw new ServiceException("商户未审核！");
        }

        //后付费用户  直接返回true
        if(OrgSettlementTypeEnums.UsedPayment.getCode().equals(org.getSettlementType())) {
            return true;
        }

        OrganizationCost organizationCost = org.getOrganizationCost(smsType, costName);
        if(organizationCost == null) {
            throw new ServiceException("商户资费未配置");
        }
        String state = organizationCost.getState();
        if(!state.equals(OrgCostStateEnums.Normal.getCode())) {
            throw new ServiceException("商户资费未开启");
        }
        if(StringUtils.isBlank(organizationCost.getCostValue()) || Integer.parseInt(organizationCost.getCostValue()) <= 0 ) {
            throw new ServiceException("商户资费异常");
        }

        int orgCost = Integer.parseInt(organizationCost.getCostValue());
        //消费金额
        Integer consumeAmount = smsCount * orgCost;
        if(consumeAmount.intValue() > org.getAvailableAmount().intValue()) {
            //商户余额不足
            return false;
        }
        return true;
    }

    /**
     * 2021-11-12
     * 统计商户信息数据
     * @param organization
     * @return
     *
     */
	@Override
	public Map<String, Object> statisticOrganizationInfo(OrganizationQuery organization) {
        LambdaQueryWrapper<Organization> queryWrapper = new LambdaQueryWrapper<>();
        if (organization.getAgentId()!=null){//代理
            queryWrapper.eq(Organization::getAgentId,organization.getAgentId());
        }
        if(StringUtils.isNotBlank(organization.getOrganizationName())){
            queryWrapper.like(Organization::getOrganizationName,organization.getOrganizationName());
        }
        if(StringUtils.isNotBlank(organization.getOrganizationCode())){
            queryWrapper.eq(Organization::getOrganizationCode,organization.getOrganizationCode());
        }
        if(StringUtils.isNotBlank(organization.getStatus())){
            queryWrapper.eq(Organization::getStatus,organization.getStatus());
        }
        if(StringUtils.isNotBlank(organization.getApproveStateCode())){
            queryWrapper.eq(Organization::getApproveStateCode,organization.getApproveStateCode());
        }
        if(organization.getBusinessUserId() != null){
            queryWrapper.eq(Organization::getBusinessUserId,organization.getBusinessUserId());
        }
        if (StringUtils.isNotBlank(organization.getGroupId())){
            queryWrapper.inSql(Organization::getOrganizationCode
                    ,String.format("SELECT org_code FROM t_organization_group WHERE group_id = %s",organization.getGroupId()));
        }
        return this.baseMapper.statisticOrganizationInfo(queryWrapper);
    }
}
