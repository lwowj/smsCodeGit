package com.hero.sms.controller.organization;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationQuery;
import com.hero.sms.enums.common.ModuleTypeEnums;
import com.hero.sms.enums.organization.OrgApproveStateEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.monitor.service.ISessionService;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.service.common.ICodeService;
import com.hero.sms.service.organization.IOrganizationCostService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * ???????????? Controller
 *
 * @author Administrator
 * @date 2020-03-07 17:24:55
 */
@Slf4j
@Validated
@Controller
@RequestMapping("organization")
public class OrganizationController extends BaseController {

    @Autowired
    private IOrganizationService organizationService;
    
    @Autowired
    private IOrganizationCostService organizationCostService;

    @Autowired
    private ISessionService sessionService;
    
    @Autowired
    private ICodeService codeService;
    
    @Autowired
    private IBusinessManage businessManage;
    
    @Autowired
    private DatabaseCache databaseCache;

    /**
     * 2021-11-12
     * ????????????????????????
     * @param organization
     * @return
     */
    @ControllerEndpoint(operation = "??????????????????", exceptionMessage = "????????????????????????")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("organization:statistic")
    public FebsResponse statistic(OrganizationQuery organization) {
        Map<String, Object> result = organizationService.statisticOrganizationInfo(organization);
        return new FebsResponse().success().data(result);
    }
    
    @GetMapping(FebsConstant.VIEW_PREFIX + "organization")
    public String organizationIndex(Model model){
        return FebsUtil.view("organization/organization");
    }

    @ControllerEndpoint(operation = "????????????")
    @GetMapping
    @ResponseBody
    @RequiresPermissions("organization:view")
    public FebsResponse getAllOrganizations(Organization organization) {
        return new FebsResponse().success().data(organizationService.findOrganizations(organization));
    }

    @ControllerEndpoint(operation = "????????????")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("organization:view")
    public FebsResponse organizationList(QueryRequest request, OrganizationQuery organization) {
        Map<String, Object> dataTable = getDataTable(this.organizationService.findOrganizationExtGroups(request, organization));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "????????????")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("organization:add")
    public FebsResponse addOrganization(@Valid Organization organization,
                @NotBlank(message = "{required}") String userAccount,
                @NotBlank(message = "{required}") String userPassword) {
        try {
            User user = super.getCurrentUser();
            organization.setCreateUser(user.getUsername());
            this.organizationService.createOrganization(organization,userAccount,userPassword);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("??????????????????",e);
            return new FebsResponse().message("??????????????????").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "????????????")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("organization:delete")
    public FebsResponse deleteOrganization(Organization organization) {
        this.organizationService.deleteOrganization(organization);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "????????????")
    @GetMapping("delete/{organizationIds}")
    @ResponseBody
    @RequiresPermissions("organization:delete")
    public FebsResponse deleteOrganization(@NotBlank(message = "{required}") @PathVariable String organizationIds) {
        String[] ids = organizationIds.split(StringPool.COMMA);
        this.organizationService.deleteOrganizations(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "????????????")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("organization:update")
    public FebsResponse updateOrganization(Organization organization) {
        try {
            this.organizationService.updateOrganization(organization);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("??????????????????",e);
            return new FebsResponse().message("??????????????????").fail();
        }
        return new FebsResponse().success();
    }

    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("organization:export")
    public void export(QueryRequest queryRequest, Organization organization, HttpServletResponse response) {
        List<Organization> organizations = this.organizationService.findOrganizations(queryRequest, organization).getRecords();
        ExcelKit.$Export(Organization.class, response).downXlsx(organizations, false);
    }

    @ControllerEndpoint(operation = "??????????????????")
    @GetMapping("approveSuccessOrgs/{organizationIds}")
    @ResponseBody
    @RequiresPermissions("organization:approve")
    public FebsResponse approveSuccessOrgs(@NotBlank(message = "{required}") @PathVariable String organizationIds) {
        String[] ids = organizationIds.split(StringPool.COMMA);
        this.organizationService.approveOrgs(ids, OrgApproveStateEnums.SUCCESS.getCode());
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "??????????????????")
    @GetMapping("approveFailOrgs/{organizationIds}")
    @ResponseBody
    @RequiresPermissions("organization:approve")
    public FebsResponse approveFailOrgs(@NotBlank(message = "{required}") @PathVariable String organizationIds) {
        String[] ids = organizationIds.split(StringPool.COMMA);
        this.organizationService.approveOrgs(ids,OrgApproveStateEnums.FAIL.getCode());
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "????????????")
    @PostMapping("cancel")
    @ResponseBody
    @RequiresPermissions("organization:cancel")
    public FebsResponse cancelOrganization(@NotBlank(message = "{required}") String organizationCode,
                                        @NotBlank(message = "{required}") String orgAmountHandle) {
        try {
            this.organizationService.cancelOrganization(organizationCode,orgAmountHandle,this.getCurrentUser().getUsername());
            this.sessionService.forceLogoutOrgByOrgCodes(organizationCode);
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("??????????????????",e);
            return new FebsResponse().message("??????????????????").fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "????????????")
    @GetMapping("lock/{organizationIds}")
    @ResponseBody
    @RequiresPermissions("organization:lock")
    public FebsResponse lockOrganization(@NotBlank(message = "{required}") @PathVariable String organizationIds) {
        String[] orgIds = organizationIds.split(StringPool.COMMA);
        LambdaUpdateWrapper<Organization> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Organization::getStatus, OrgStatusEnums.Lock.getCode())
                .eq(Organization::getStatus,OrgStatusEnums.Normal.getCode())
                .in(Organization::getId,orgIds);
        this.organizationService.update(wrapper);
        try {
            sessionService.forceLogoutOrg(organizationIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "????????????")
    @GetMapping("unlock/{organizationIds}")
    @ResponseBody
    @RequiresPermissions("organization:unlock")
    public FebsResponse unlockOrganization(@NotBlank(message = "{required}") @PathVariable String organizationIds) {
        String[] orgIds = organizationIds.split(StringPool.COMMA);
        LambdaUpdateWrapper<Organization> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(Organization::getStatus, OrgStatusEnums.Normal.getCode())
                .eq(Organization::getStatus,OrgStatusEnums.Lock.getCode())
                .in(Organization::getId,orgIds);
        this.organizationService.update(wrapper);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "??????????????????")
    @PostMapping("assignChannel")
    @ResponseBody
    @RequiresPermissions("organization:assignChannel")
    public FebsResponse assignChannel(@NotBlank(message = "{required}") String organizationCodes,
    		@NotBlank(message = "{required}") String costName,
    		@NotBlank(message = "{required}") String smsType,
    		@NotBlank(message = "{required}") String state,
    		String channelId
    		) {
    	try {
    		organizationCostService.orgAssignChannel(organizationCodes,costName,smsType,state,channelId);
    	} catch (FebsException e) {
    		return new FebsResponse().message(e.getMessage()).fail();
    	} catch (Exception e) {
    		log.error("????????????????????????",e);
    		return new FebsResponse().message("????????????????????????").fail();
    	}
    	return new FebsResponse().success();
    }

    @GetMapping("getOrgs")
    @ResponseBody
    public FebsResponse getOrgs(){
        List<Map<String,String>> list = this.organizationService.getOrgs();
        return new FebsResponse().success().data(list);
    }

    @ControllerEndpoint(operation = "????????????????????????")
    @PostMapping("updateCosts")
    @ResponseBody
    @RequiresPermissions("organization:updateCosts")
    public FebsResponse updateCosts(@NotBlank(message = "{required}") String organizationCodes,
                                      @NotBlank(message = "{required}") String costName,
                                      @NotBlank(message = "{required}") String smsType,
                                      String operator,
                                      @NotBlank(message = "{required}") String costValue
    ) {
        try {
        	User user = super.getCurrentUser();
            organizationCostService.updateCosts(organizationCodes,costName,smsType,costValue,operator,user.getUsername());
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("??????????????????????????????",e);
            return new FebsResponse().message("??????????????????????????????").fail();
        }
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "????????????????????????")
    @PostMapping("updateUpCosts")
    @ResponseBody
    @RequiresPermissions("organization:updateUpCosts")
    public FebsResponse updateUpCosts(@NotBlank(message = "{required}") String organizationCodes,
                                      @NotBlank(message = "{required}") String costName,
                                      @NotBlank(message = "{required}") String smsType,
                                      String operator,
                                      @NotBlank(message = "{required}") String costValue
    ) {
        try {
        	User user = super.getCurrentUser();
            organizationCostService.updateFloatCosts(organizationCodes,costName,smsType,costValue,operator,user.getUsername(),"1");
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("??????????????????????????????",e);
            return new FebsResponse().message("??????????????????????????????").fail();
        }
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "????????????????????????")
    @PostMapping("updateCutCosts")
    @ResponseBody
    @RequiresPermissions("organization:updateCutCosts")
    public FebsResponse updateCutCosts(@NotBlank(message = "{required}") String organizationCodes,
                                      @NotBlank(message = "{required}") String costName,
                                      @NotBlank(message = "{required}") String smsType,
                                      String operator,
                                      @NotBlank(message = "{required}") String costValue
    ) {
        try {
        	User user = super.getCurrentUser();
            organizationCostService.updateFloatCosts(organizationCodes,costName,smsType,costValue,operator,user.getUsername(),"0");
        } catch (FebsException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        } catch (Exception e) {
            log.error("??????????????????????????????",e);
            return new FebsResponse().message("??????????????????????????????").fail();
        }
        return new FebsResponse().success();
    }
    
    /**
     * 2021-03-06
     * ??????????????????
     */
    @GetMapping("orgGrouplist")
    @ResponseBody
    @RequiresPermissions("organization:orgGroupView")
    public FebsResponse orgGrouplist(QueryRequest request, Code code) 
    {
    	if(code == null)
    	{
    		code = new Code();
    	}
    	code.setSortCode("OrgGroup");
        Map<String, Object> dataTable = getDataTable(codeService.findCodes(request, code));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "??????????????????", exceptionMessage = "????????????????????????")
    @PostMapping("orgGroupAdd")
    @ResponseBody
    @RequiresPermissions("organization:orgGroupAdd")
    public FebsResponse orgGroupAdd(@Valid Code code) 
    {
        try {
        	code.setSortCode("OrgGroup");
           codeService.createCode(code);
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }
    
    @ControllerEndpoint(operation = "??????????????????", exceptionMessage = "????????????????????????")
    @GetMapping("orgGroupDelete/{orgGroupIds}")
    @ResponseBody
    @RequiresPermissions("organization:orgGroupDelete")
    public FebsResponse orgGroupDelete(@NotBlank(message = "{required}") @PathVariable String orgGroupIds) 
    {
        String[] ids = orgGroupIds.split(StringPool.COMMA);
        codeService.deleteCodes(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "??????????????????", exceptionMessage = "????????????????????????")
    @PostMapping("orgGroupUpdate")
    @ResponseBody
    @RequiresPermissions("organization:orgGroupUpdate")
    public FebsResponse orgGroupUpdate(@Valid Code code) 
    {
    	code.setSortCode("OrgGroup");
        codeService.updateCode(code);
        return new FebsResponse().success();
    }
    
    
    @ControllerEndpoint(operation = "????????????????????????", exceptionMessage = "??????????????????????????????")
    @GetMapping("loadOrgGroupCache")
    @ResponseBody
    @RequiresPermissions("organization:loadOrgGroupCache")
    public FebsResponse loadOrgGroupCache(){
        try {
        	databaseCache.initCode();
        	businessManage.reladProjectCacheForModule("merch,agent,gateway",String.valueOf(ModuleTypeEnums.CodeList.getCode().intValue()));
		} catch (ServiceException e) {
			return new FebsResponse().message("?????????????????????????????????????????????");
		}
        return new FebsResponse().success();
    }
}
