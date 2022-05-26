package com.hero.sms.service.impl.organization;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.utils.AddressUtil;
import com.hero.sms.common.utils.DateUtils;
import com.hero.sms.common.utils.GoogleAuthenticator;
import com.hero.sms.common.utils.HttpContextUtil;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.common.utils.MD5Util;
import com.hero.sms.common.utils.RegexUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.mapper.organization.OrganizationUserMapper;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserService;

/**
 * 商户用户 Service实现
 *
 * @author Administrator
 * @date 2020-03-07 21:36:18
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationUserServiceImpl extends ServiceImpl<OrganizationUserMapper, OrganizationUser> implements IOrganizationUserService {

    @Autowired
    private OrganizationUserMapper organizationUserMapper;
    @Autowired
    private IOrganizationService organizationService;

    @Override
    public IPage<OrganizationUser> findOrganizationUsers(QueryRequest request, OrganizationUser organizationUser) {
        LambdaQueryWrapper<OrganizationUser> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotBlank(organizationUser.getOrganizationCode())){
            queryWrapper.eq(OrganizationUser::getOrganizationCode,organizationUser.getOrganizationCode());
        }
        if(StringUtils.isNotBlank(organizationUser.getUserName())){
            queryWrapper.like(OrganizationUser::getUserName,organizationUser.getUserName());
        }
        if(StringUtils.isNotBlank(organizationUser.getUserAccount())){
            queryWrapper.eq(OrganizationUser::getUserAccount,organizationUser.getUserAccount());
        }
        if(StringUtils.isNotBlank(organizationUser.getStatus())){
            queryWrapper.eq(OrganizationUser::getStatus,organizationUser.getStatus());
        }
        queryWrapper.orderByDesc(OrganizationUser::getId);
        Page<OrganizationUser> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public IPage<OrganizationUser> selectLeftOrganizationPage(QueryRequest request, OrganizationUser organizationUser, Agent agent) {
        QueryWrapper queryWrapper = new QueryWrapper();
        // TODO 设置查询条件
        if(organizationUser.getId() != null){
            queryWrapper.eq("u.id",organizationUser.getId());
        }
        if(StringUtils.isNotBlank(organizationUser.getOrganizationCode())){
            queryWrapper.eq("u.Organization_Code",organizationUser.getOrganizationCode());
        }
        if(StringUtils.isNotBlank(organizationUser.getUserName())){
            queryWrapper.like("u.User_Name",organizationUser.getUserName());
        }
        if(StringUtils.isNotBlank(organizationUser.getUserAccount())){
            queryWrapper.eq("u.User_Account",organizationUser.getUserAccount());
        }
        if(StringUtils.isNotBlank(organizationUser.getStatus())){
            queryWrapper.eq("u.Status",organizationUser.getStatus());
        }
        queryWrapper.eq("o.Agent_Id",agent.getId());
        queryWrapper.orderByDesc("u.id");
        Page<OrganizationUser> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.organizationUserMapper.selectLeftOrganizationPage(page, queryWrapper);
    }

    @Override
    public List<OrganizationUser> findOrganizationUsers(OrganizationUser organizationUser) {
	    LambdaQueryWrapper<OrganizationUser> queryWrapper = new LambdaQueryWrapper<>();
	    if (organizationUser.getId()!=null){
	        queryWrapper.eq(OrganizationUser::getId,organizationUser.getId());
        }
        if(StringUtils.isNotBlank(organizationUser.getOrganizationCode())){
            queryWrapper.eq(OrganizationUser::getOrganizationCode,organizationUser.getOrganizationCode());
        }
        if (StringUtils.isNotBlank(organizationUser.getUserName())){
            queryWrapper.like(OrganizationUser::getUserName,organizationUser.getUserName());
        }
        queryWrapper.orderByDesc(OrganizationUser::getId);
		return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 代理端商户用户新增
     * @param organizationUser
     * @param agent
     */
    @Override
    @Transactional
    public void createOrganizationUser(OrganizationUser organizationUser, Agent agent) {
        if(organizationUser == null || StringUtils.isEmpty(organizationUser.getOrganizationCode())){
            throw new FebsException("商户用户信息错误");
        }
        Organization organization = organizationService.queryOrgByCode(organizationUser.getOrganizationCode());
        if(!organization.getAgentId().equals(agent.getId())){
            throw new FebsException("该商户不属于当前代理");
        }
        createOrganizationUser(organizationUser);
    }

    @Override
    @Transactional
    public void createOrganizationUser(OrganizationUser organizationUser) {
        if(organizationUser == null || StringUtils.isEmpty(organizationUser.getOrganizationCode()) || StringUtils.isEmpty(organizationUser.getUserAccount())
                || StringUtils.isEmpty(organizationUser.getUserPassword())|| StringUtils.isEmpty(organizationUser.getStatus())
                || StringUtils.isEmpty(organizationUser.getAccountType())){
            throw new FebsException("商户用户信息错误");
        }
        //过滤登录账号前后空格 2020-12-11
        String userAccount = organizationUser.getUserAccount().trim();
        if(StringUtils.isEmpty(userAccount))
        {
            throw new FebsException("设置用户名错误,用户名不能为空格");
        }
        //校验登录账号为英文与数字  2021-05-05
        if(!RegexUtil.isUserAccount(userAccount))
        {
        	throw new FebsException("登录账号只能20位以内的英文、数字或英文数字组合");
        }
        organizationUser.setUserAccount(userAccount);
        
      //过滤前后空格 2020-12-11
        String userPassword = organizationUser.getUserPassword().trim();
        if(StringUtils.isEmpty(userPassword))
        {
            throw new FebsException("设置密码错误,密码不能为空格");
        }
        organizationUser.setUserPassword(userPassword);
        
        Organization organization = organizationService.queryOrgByCode(organizationUser.getOrganizationCode());
        if(organization == null){
            throw new FebsException("商户信息错误");
        }
        OrganizationUser oldUser = this.findByUserAccount(organizationUser.getUserAccount());
        if(oldUser != null){
            throw new FebsException("登录账号已存在");
        }
        organizationUser.setUserPassword(MD5Util.encrypt(organizationUser.getUserAccount(),organizationUser.getUserPassword()));
        this.save(organizationUser);
    }

    /**
     * 代理端商户修改
     * @param organizationUser
     * @param agent
     */
    @Override
    @Transactional
    public void updateOrganizationUser(OrganizationUser organizationUser, Agent agent) {
        if(organizationUser == null || organizationUser.getId() == null){
            throw new FebsException("商户用户信息错误");
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("u.id",organizationUser.getId());
        queryWrapper.eq("o.Agent_Id",agent.getId());
        Page<OrganizationUser> page = new Page<>();
        IPage<OrganizationUser> users = this.organizationUserMapper.selectLeftOrganizationPage(page, queryWrapper);
        users.getTotal();
        if(users.getTotal() == 0){
            throw new FebsException("当前代理无权操作");
        }
        updateOrganizationUser(organizationUser);
    }

    @Override
    @Transactional
    public void updateOrganizationUser(OrganizationUser organizationUser) {
        if(organizationUser == null || organizationUser.getId() == null){
            throw new FebsException("商户用户信息错误");
        }
        OrganizationUser newOrganizationUser = new OrganizationUser();
        newOrganizationUser.setId(organizationUser.getId());
        //newOrganizationUser.setStatus(organizationUser.getStatus());
        newOrganizationUser.setUserName(organizationUser.getUserName());
        newOrganizationUser.setDescription(organizationUser.getDescription());
        newOrganizationUser.setLoginFaildCount(0);
        newOrganizationUser.setNeedBindGoogleKey(organizationUser.getNeedBindGoogleKey());
        this.updateById(newOrganizationUser);
    }

    /**
     * 商户端修改用户
     * @param organizationUser
     * @param userExt
     */
    @Override
    @Transactional
    public void updateOrganizationUser(OrganizationUser organizationUser, OrganizationUserExt userExt) {
        if(organizationUser == null || organizationUser.getId() == null){
            throw new FebsException("商户用户信息错误");
        }
        LambdaUpdateWrapper<OrganizationUser> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.set(OrganizationUser::getStatus,organizationUser.getStatus());
        updateWrapper.set(OrganizationUser::getUserName,organizationUser.getUserName());
        updateWrapper.set(OrganizationUser::getLoginFaildCount,0);
        updateWrapper.set(OrganizationUser::getDescription,organizationUser.getDescription());
        updateWrapper.eq(OrganizationUser::getOrganizationCode,userExt.getOrganizationCode());
        updateWrapper.eq(OrganizationUser::getId,organizationUser.getId());
        updateWrapper.ne(OrganizationUser::getAccountType, DatabaseCache.getCodeBySortCodeAndName("AccountType","主账户").getCode());
        this.update(updateWrapper);
    }

    @Override
    @Transactional
    public void deleteOrganizationUser(OrganizationUser organizationUser) {
        LambdaQueryWrapper<OrganizationUser> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizationUsers(String[] organizationUserIds) {
        List<String> list = Arrays.asList(organizationUserIds);
        this.removeByIds(list);
    }

    @Override
    public OrganizationUser findByUserAccount(String userAccount) {
        LambdaQueryWrapper<OrganizationUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrganizationUser::getUserAccount,userAccount);

        return this.baseMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public void updateUserStatus(String[] organizationUserIds, String status) {
        LambdaUpdateWrapper<OrganizationUser> wrapper = new LambdaUpdateWrapper<>();
        if(status.equals(OrgStatusEnums.Normal.getCode())){
            wrapper.set(OrganizationUser::getLoginFaildCount,0);
        }
        wrapper.set(OrganizationUser::getStatus,status)
                .in(OrganizationUser::getId,organizationUserIds);
        this.update(wrapper);
    }

    @Override
    @Transactional
    public void updateUserNeedGoogleStatus(String[] organizationUserIds, String status) {
        LambdaUpdateWrapper<OrganizationUser> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(OrganizationUser::getNeedBindGoogleKey,status)
                .in(OrganizationUser::getId,organizationUserIds);
        this.update(wrapper);
    }
    
    @Override
    public void updateUserStatus(String[] organizationUserIds, String status, Agent agent) {
        //过滤
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("o.Agent_Id",agent.getId());
        queryWrapper.in("u.id",organizationUserIds);
        List<Integer> ids = this.organizationUserMapper.selectIdsLeftOrganization(queryWrapper);

        LambdaUpdateWrapper<OrganizationUser> wrapper = new LambdaUpdateWrapper<>();
        if(status.equals(OrgStatusEnums.Normal.getCode())){
            wrapper.set(OrganizationUser::getLoginFaildCount,0);
        }
        wrapper.set(OrganizationUser::getStatus,status)
                .in(OrganizationUser::getId,ids);
        this.update(wrapper);
        
    }

    /**
     * 商户后台修改状态
     * @param organizationUserIds
     * @param status
     * @param organization
     */
    @Override
    @Transactional
    public void updateUserStatus(String[] organizationUserIds, String status, OrganizationUserExt organization) {
        LambdaUpdateWrapper<OrganizationUser> wrapper = new LambdaUpdateWrapper<>();
        if(status.equals(OrgStatusEnums.Normal.getCode())){
            wrapper.set(OrganizationUser::getLoginFaildCount,0);
        }
        wrapper.set(OrganizationUser::getStatus,status)
                .in(OrganizationUser::getId,organizationUserIds)
        .eq(OrganizationUser::getOrganizationCode,organization.getOrganizationCode())
                .ne(OrganizationUser::getAccountType, DatabaseCache.getCodeBySortCodeAndName("AccountType","主账户").getCode());
        this.update(wrapper);
    }

    @Override
    @Transactional
    public void resetPassword(String[] userAccounts) {
        Arrays.stream(userAccounts).forEach(UserAccount -> {
            OrganizationUser organizationUser = new OrganizationUser();
            String passWord =  DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","defaultPassWord").getName();
            String defaultPassWord = DateUtils.getString(DateUtils.Y_M_D_2);
            if(passWord!=null&&!"".equals(passWord))
            {
            	defaultPassWord = passWord;
            }
            organizationUser.setUserPassword(MD5Util.encrypt(UserAccount,defaultPassWord));
            this.baseMapper.update(organizationUser,new LambdaQueryWrapper<OrganizationUser>().eq(OrganizationUser::getUserAccount,UserAccount));
        });
    }

    @Override
    @Transactional
    public void resetGoogleKey(String[] userAccounts) {
        Arrays.stream(userAccounts).forEach(UserAccount -> {
            LambdaUpdateWrapper<OrganizationUser> updateWrapper = new LambdaUpdateWrapper();
            updateWrapper.set(OrganizationUser::getGoogleKey,null);
            updateWrapper.eq(OrganizationUser::getUserAccount,UserAccount);
            this.update(updateWrapper);
        });
    }
    
    /**
     * 商户端重置密码
     * @param userAccounts
     * @param userExt
     */
    @Override
    @Transactional
    public void resetPassword(String[] userAccounts, OrganizationUserExt userExt) {
        Arrays.stream(userAccounts).forEach(UserAccount -> {
            OrganizationUser organizationUser = new OrganizationUser();
            String passWord =  DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","defaultPassWord").getName();
    		String defaultPassWord = DateUtils.getString(DateUtils.Y_M_D_2);
            if(passWord!=null&&!"".equals(passWord))
            {
            	defaultPassWord = passWord;
            }
            organizationUser.setUserPassword(MD5Util.encrypt(UserAccount,defaultPassWord));
            this.baseMapper.update(organizationUser,new LambdaQueryWrapper<OrganizationUser>()
                    .eq(OrganizationUser::getUserAccount,UserAccount).eq(OrganizationUser::getOrganizationCode,userExt.getOrganizationCode())
                    .ne(OrganizationUser::getAccountType,DatabaseCache.getCodeBySortCodeAndName("AccountType","主账户").getCode()));
        });
    }

    @Override
    @Transactional
    public void updatePassword(String userAccount, String password) {
        OrganizationUser user = new OrganizationUser();
        user.setUserPassword(MD5Util.encrypt(userAccount, password));
        this.baseMapper.update(user, new LambdaQueryWrapper<OrganizationUser>().eq(OrganizationUser::getUserAccount, userAccount));
    }

    @Override
    @Transactional
    public void updateLoginTime(String userAccount) {
        HttpServletRequest request = HttpContextUtil.getHttpServletRequest();
        String ip = IPUtil.getIpAddr(request);
        OrganizationUser user = new OrganizationUser();
        user.setLastLoginTime(new Date());
        user.setLastLoginIp(AddressUtil.getCityInfo(ip));
//        user.setRemark(ip);
        user.setLoginIp(ip);
        this.baseMapper.update(user, new LambdaQueryWrapper<OrganizationUser>().eq(OrganizationUser::getUserAccount, userAccount));
    }

    /**
     * 检验谷歌验证码
     * @param useraccount
     * @param verifyCode
     */
    @Override
    public void checkGoogleKey(String useraccount, String verifyCode) {
        OrganizationUser organizationUser = this.findByUserAccount(useraccount);
        if(organizationUser == null){
            throw new FebsException("用户名或密码有误！");
        }else{
            if(StringUtils.isBlank(organizationUser.getGoogleKey())) return;
            try {
                boolean bl = GoogleAuthenticator.verifyGoogleKey(verifyCode, organizationUser.getGoogleKey());
                if(!bl) throw new FebsException("谷歌码有误！");
            } catch (Exception e) {
                throw new FebsException("谷歌码有误！");
            }
        }
    }

    /**
     * 绑定谷歌
     * @param organizationUser
     * @param goologoVerifyCode
     * @param googleKey
     */
    @Override
    public void bindGoogle(OrganizationUser organizationUser, String goologoVerifyCode, String googleKey,String password){
        if(!GoogleAuthenticator.verifyGoogleKey(goologoVerifyCode, googleKey)){
            throw new FebsException("谷歌码有误！");
        }
        if(!organizationUser.getUserPassword().equals(MD5Util.encrypt(organizationUser.getUserAccount(),password))){
            throw new FebsException("登录密码有误！");
        }
        OrganizationUser updateOrgUser = new OrganizationUser();
        updateOrgUser.setId(organizationUser.getId());
        updateOrgUser.setGoogleKey(googleKey);
        this.updateById(updateOrgUser);
    }

    /**
     * 清除商户谷歌Key
     * @param organizationUser
     */
    @Override
    public void removeGoogleKey(OrganizationUser organizationUser, String goologoVerifyCode,String password){
        if(!GoogleAuthenticator.verifyGoogleKey(goologoVerifyCode, organizationUser.getGoogleKey())){
            throw new FebsException("谷歌码有误！");
        }
        if(!organizationUser.getUserPassword().equals(MD5Util.encrypt(organizationUser.getUserAccount(),password))){
            throw new FebsException("登录密码有误！");
        }
        OrganizationUser updateOrgUser = new OrganizationUser();
        updateOrgUser.setId(organizationUser.getId());
        updateOrgUser.setGoogleKey("");
        this.updateById(updateOrgUser);
    }

}
