package com.hero.sms.controller.organization;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationCost;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.OrganizationUserLimit;
import com.hero.sms.enums.common.OperatorEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.enums.organization.OrgSmsApproveTypeEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.service.agent.IAgentCostService;
import com.hero.sms.service.organization.IOrganizationCostService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserLimitService;
import com.hero.sms.service.organization.IOrganizationUserService;

/**
 * @author Administrator
 */
@Controller("organizationView")
public class ViewController extends BaseController {

    @Autowired
    private IOrganizationService organizationService;
    @Autowired
    private IOrganizationUserService organizationUserService;
    @Autowired
    private IOrganizationCostService organizationCostService;
    @Autowired
    private IOrganizationUserLimitService organizationUserLimitService;
    @Autowired
    private IAgentCostService agentCostService;
    //商户管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organization")
    @RequiresPermissions("organization:view")
    public String organization(Model model) {
        model.addAttribute("orgSmsApproveTypeEnums", OrgSmsApproveTypeEnums.values());
        model.addAttribute("orgApproveStateEnums", OrgApproveStateEnums.values());
        model.addAttribute("orgStatusEnums", OrgStatusEnums.values());
        return FebsUtil.view("organization/organization");
    }

    //商户用户管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUser")
    @RequiresPermissions("organizationUser:view")
    public String organizationUser(Model model) {
        model.addAttribute("orgs", DatabaseCache.getOrganizationListByAgentId(getCurrentAgent().getId()));
        model.addAttribute("orgStatusEnums", OrgStatusEnums.values());
        return FebsUtil.view("organization/organizationUser");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/add")
    @RequiresPermissions("organization:add")
    public String organizationAdd() {
        return FebsUtil.view("organization/organizationAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/update/{id}")
    @RequiresPermissions("organization:update")
    public String organizationUpdate(@PathVariable String id, Model model) {
        resolveOrganizationModel(id, model);
        model.addAttribute("orgStatusEnums", OrgStatusEnums.values());
        return FebsUtil.view("organization/organizationUpdate");
    }

    private void resolveOrganizationModel(String id, Model model) {
        LambdaQueryWrapper<Organization> queryWrapper  = new LambdaQueryWrapper();
        queryWrapper.eq(Organization::getId,id);
        queryWrapper.eq(Organization::getAgentId,getCurrentAgent().getId());
        Organization organization = this.organizationService.getOne(queryWrapper);
        organization.setMd5Key(null);
        organization.setDataMd5(null);
        organization.setCreateUser(null);
        organization.setRemark(null);
        model.addAttribute("organization",organization);
    }
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationCost/updates/{id}")
    @RequiresPermissions("organizationCost:updates")
    public String organizationCostUpdates(@PathVariable String id, Model model) {
        resolveOrganizationCostModel(id, model);
        model.addAttribute("operatorEnums", OperatorEnums.values());
        return FebsUtil.view("organization/organizationCostUpdate");
    }

    /**
     * 获取商户费率
     * @param id
     * @param model
     */
    private void resolveOrganizationCostModel(String id, Model model) {
        LambdaQueryWrapper<Organization> queryWrapper  = new LambdaQueryWrapper();
        queryWrapper.eq(Organization::getId,id);
        queryWrapper.eq(Organization::getAgentId,getCurrentAgent().getId());
        Organization organization = this.organizationService.getOne(queryWrapper);
        organization.setMd5Key(null);
        organization.setDataMd5(null);
        organization.setCreateUser(null);
        organization.setRemark(null);
        OrganizationCost cost = new OrganizationCost();
        cost.setOrganizationCode(organization.getOrganizationCode());
        List<OrganizationCost> costs = this.organizationCostService.findOrganizationCosts(cost);
        model.addAttribute("smsTypeEnums", SmsTypeEnums.values());
//        List<AreaCode> smsNumberAreaCode = organizationCostService.getOrgAreaCodeList(organization.getOrganizationCode());
        List<AreaCode> smsNumberAreaCode = agentCostService.getAgentAreaCodeList(getCurrentAgent().getId());
        model.addAttribute("smsNumberAreaCodeEnums", smsNumberAreaCode);
        model.addAttribute("costs",costs);
        model.addAttribute("organization",organization);
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUser/add")
    @RequiresPermissions("organizationUser:add")
    public String organizationUserAdd(Model model) {
        model.addAttribute("orgs", DatabaseCache.getOrganizationListByAgentId(getCurrentAgent().getId()));
        return FebsUtil.view("organization/organizationUserAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUser/update/{id}")
    @RequiresPermissions("organizationUser:update")
    public String organizationUserUpdate(@PathVariable String id, Model model) {
        resolveOrganizationUserModel(id, model);
        return FebsUtil.view("organization/organizationUserUpdate");
    }
    private void resolveOrganizationUserModel(String id, Model model) {
        OrganizationUser user = new OrganizationUser();
        user.setId(Integer.parseInt(id));
        OrganizationUser organizationUser = this.organizationUserService.selectLeftOrganizationPage(new QueryRequest(),user,getCurrentAgent()).getRecords().get(0);
        organizationUser.setGoogleKey(null);
        organizationUser.setUserPassword(null);
        organizationUser.setCreateUser(null);
        organizationUser.setRemark(null);
        organizationUser.setLoginIp(null);
        model.addAttribute("organizationName",DatabaseCache.getOrgNameByOrgcode(organizationUser.getOrganizationCode()));
        model.addAttribute("organizationUser",organizationUser);
    }
    // 商户用户权限
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser/updateMenuLimit/{id}")
    @RequiresPermissions("organizationUserLimit:update")
    public String updateOrganizationUserLimit(@PathVariable Long id, Model model) {
        resolveOrganizationUserLimitModel(id, model);
        return FebsUtil.view("organization/organizationUserLimitUpdate");
    }
    private void resolveOrganizationUserLimitModel(Long id, Model model) {
        OrganizationUser queryOrgUser = new OrganizationUser();
        queryOrgUser.setId(new Long(id).intValue());
        List<OrganizationUser> orgUserList = this.organizationUserService.selectLeftOrganizationPage(new QueryRequest(),queryOrgUser,getCurrentAgent()).getRecords();
        List<OrganizationUserLimit> limits = null;
        if(orgUserList.size() > 0){
            OrganizationUserLimit limit = new OrganizationUserLimit();
            limit.setUserId(id);
            limits = this.organizationUserLimitService.findOrganizationUserLimits(limit);
        }
        model.addAttribute("limits",limits);
        model.addAttribute("id",id);
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationRechargeOrder/add/{id}")
    @RequiresPermissions("organizationRechargeOrder:add")
    public String organizationRechargeOrderAdd(@PathVariable String id, Model model) {
        resolveOrganizationModel(id, model);
        model.addAttribute("type", StringUtils.isEmpty(getCurrentAgent().getGoogleKey())?"pay":"google");
        return FebsUtil.view("rechargeOrder/organizationRechargeOrderAdd");
    }

    //商户充值管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "orgRechargeOrder/orgRechargeOrder")
    public String organizationRechargeOrderIndex(){
        return FebsUtil.view("rechargeOrder/organizationRechargeOrder");
    }



}
