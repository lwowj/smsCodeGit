package com.hero.sms.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.ExpiredSessionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.authentication.ShiroHelper;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.utils.AppUtil;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.utils.GoogleAuthenticator;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationProperty;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.OrganizationUserLimit;
import com.hero.sms.service.organization.IOrganizationCostService;
import com.hero.sms.service.organization.IOrganizationPropertyService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserLimitService;
import com.hero.sms.service.organization.IOrganizationUserService;
import com.hero.sms.utils.AjaxTokenProcessor;
import com.hero.sms.utils.GetSystemBrowserInfo;

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
    private ShiroHelper shiroHelper;
    @Autowired
    IOrganizationPropertyService organizationPropertyService;


    @GetMapping("login")
    @ResponseBody
    public Object login(HttpServletRequest request) {
        if (AppUtil.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        } else {
            ModelAndView mav = new ModelAndView();
            /**
             * @begin 2020-12-12
             * 新增初始token 防止重复提交
             */
            String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.LOGIN_TRANSACTION_TOKEN_KEY);
            mav.addObject("sessionToken", sessionToken);
            /**
             * @end
             */
            mav.setViewName(FebsUtil.view("loginB"));
            return mav;
        }
    }

    @GetMapping("unauthorized")
    public String unauthorized() {
        return FebsUtil.view("error/403");
    }

    @GetMapping("/")
    public String redirectIndex() {
        return "redirect:/index";
    }

    @GetMapping("index")
    public String index(Model model,HttpServletRequest request) {
        AuthorizationInfo authorizationInfo = shiroHelper.getCurrentuserAuthorizationInfo();
        OrganizationUser user = super.getCurrentUser();
        OrganizationUser currentUserDetail = organizationUserService.findByUserAccount(user.getUserAccount());
        Organization organization = this.organizationService.getOrganizationByCode(user.getOrganizationCode());
        organization.setDataMd5(null);
        organization.setRemark(null);
        organization.setCreateUser(null);

        Code code = DatabaseCache.getCodeBySortCodeAndCode("System", "orgCashlessNotifyNum");
        String value = "50";
        if (code != null) value = code.getName();
        BigDecimal orgCashlessNotifyNum = new BigDecimal(value);
        currentUserDetail.setUserPassword("It's a secret");
        model.addAttribute("user", currentUserDetail);
        model.addAttribute("org", organization);
        model.addAttribute("permissions", authorizationInfo.getStringPermissions());
		model.addAttribute("orgCashlessNotifyNum",orgCashlessNotifyNum);
		Code welcomeMessageCode = DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig", "welcomeMessage");
		String welcomeMessage = "您好，感谢使用！";
		if(welcomeMessageCode!=null&&welcomeMessageCode.getName()!=null)
		{
			welcomeMessage = welcomeMessageCode.getName();
		}
		model.addAttribute("welcomeMessage",welcomeMessage);
        try {
        	HttpSession session = request.getSession();
            String thisLoginIp = String.valueOf(session.getAttribute("thisLoginIp"));
            String thisLoginTime = String.valueOf(session.getAttribute("thisLoginTime"));
            String pwStrong = String.valueOf(session.getAttribute("pwStrong"));
            model.addAttribute("thisLoginIp", thisLoginIp);
            model.addAttribute("thisLoginTime", thisLoginTime);
            String lastLoginIp = currentUserDetail.getLastLoginIp();
            if(session.getAttribute("lastLoginIp")!=null&&!"".contentEquals(String.valueOf(session.getAttribute("lastLoginIp"))))
            {
            	lastLoginIp = String.valueOf(session.getAttribute("lastLoginIp"));
            }
            else 
            {
            	session.setAttribute("lastLoginIp",lastLoginIp);
			}
            
            String lastLoginTime =DateUtil.getString(currentUserDetail.getLastLoginTime(), DateUtil.FULL_TIME_SPLIT_PATTERN);
            if(session.getAttribute("lastLoginTime")!=null&&!"".contentEquals(String.valueOf(session.getAttribute("lastLoginTime"))))
            {
            	lastLoginTime = String.valueOf(session.getAttribute("lastLoginTime"));
            }
            else 
            {
            	session.setAttribute("lastLoginTime",lastLoginTime);
			}
            model.addAttribute("pwStrong", pwStrong);
            model.addAttribute("lastLoginIp", lastLoginIp);
            model.addAttribute("lastLoginTime", lastLoginTime);
		} catch (Exception e) {
			// TODO: handle exception
		}
        return "index";
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "layout")
    public String layout() {
        return FebsUtil.view("layout");
    }

    @RequestMapping(FebsConstant.VIEW_PREFIX + "index")
    public String pageIndex() {
        return FebsUtil.view("index");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser/organizationInfo")
    @RequiresPermissions("organizationUser:view")
    public String organizationInfo(Model model) 
    {
    	OrganizationUser user = super.getCurrentUser();
    	String accountType = "1";
    	String interfaceType = "0";
    	String systemId = "";
    	String password = "";
    	String maxChannels = "";
    	String readLimit = "";
    	String interfaceDocUri = "";
//    	String HttpIP = "";
//    	String HttpPort = "";
//    	String SmppIP = "";
//    	String SmppPort = "";
    	List<OrganizationProperty> list = new ArrayList<OrganizationProperty>();
    	if(user!=null)
    	{
    		accountType = user.getAccountType();
    		if("0".equals(user.getAccountType()))
    		{
    			Organization organization = this.organizationService.getOrganizationByCode(user.getOrganizationCode());
    			interfaceType = String.valueOf(organization.getInterfaceType());
    			if("2".equals(interfaceType)||"3".equals(interfaceType))
    			{
    				LambdaQueryWrapper<OrganizationProperty> queryWrapper = new LambdaQueryWrapper<>();
        	        queryWrapper.eq(OrganizationProperty::getOrganizationCode,user.getOrganizationCode());
        	        list = this.organizationPropertyService.list(queryWrapper);
        	        if(list!=null&&list.size()>0)
        	        {
        	        	for (int i = 0; i < list.size(); i++) 
        	        	{
        	        		OrganizationProperty op = list.get(i);
        	            	if("systemId".equals(op.getName()))
        	            	{
        	            		systemId = op.getValue();
        	            	}
        	            	else if("password".equals(op.getName()))
        	            	{
        	            		password = op.getValue();
        	            	}
        	            	else if("maxChannels".equals(op.getName()))
        	            	{
        	            		maxChannels = op.getValue();
        	            	}
        	            	else if("readLimit".equals(op.getName()))
        	            	{
        	            		readLimit = op.getValue();
        	            	}
						}
        	        }
//        	        Code smppHostCode = DatabaseCache.getCodeBySortCodeAndCode("SmppServerConfig", "host");
//        	        if(smppHostCode!=null)
//        	    	{
//        	        	SmppIP = smppHostCode.getName();
//        	    	}
//        	        Code smppPortCode = DatabaseCache.getCodeBySortCodeAndCode("SmppServerConfig", "port");
//        	        if(smppPortCode!=null)
//        	    	{
//        	        	SmppPort = smppPortCode.getName();
//        	    	}
    			}
    			if("1".equals(interfaceType)||"3".equals(interfaceType))
    			{
//    				Code httpHostCode = DatabaseCache.getCodeBySortCodeAndCode("HttpServerConfig", "host");
//         	    	if(httpHostCode!=null)
//         	    	{
//         	    		HttpIP = httpHostCode.getName();
//         	    	}
//         	        Code httpPortCode = DatabaseCache.getCodeBySortCodeAndCode("HttpServerConfig", "port");
//         	        if(httpPortCode!=null)
//         	    	{
//         	        	HttpPort = httpPortCode.getName();
//         	    	}
         	        Code interfaceDocUriCode = DatabaseCache.getCodeBySortCodeAndCode("HttpServerConfig", "interfaceDocUri");
         	        if(interfaceDocUriCode!=null)
         	    	{
         	        	interfaceDocUri = interfaceDocUriCode.getName();
         	    	}
    			}
    		}
    	}
    	model.addAttribute("interfaceDocUri",interfaceDocUri);
//    	model.addAttribute("HttpIP",HttpIP);
//    	model.addAttribute("HttpPort",HttpPort);
//    	model.addAttribute("SmppIP",SmppIP);
//    	model.addAttribute("SmppPort",SmppPort);
    	model.addAttribute("systemId",systemId);
    	model.addAttribute("password",password);
    	model.addAttribute("maxChannels",maxChannels);
    	model.addAttribute("readLimit",readLimit);
    	model.addAttribute("interfaceType",interfaceType);
    	model.addAttribute("accountType",accountType);
        return FebsUtil.view("organization/organizationInfo");
    }
    
    @GetMapping(FebsConstant.VIEW_PREFIX + "password/update")
    public String passwordUpdate() {
        return FebsUtil.view("organizationUser/passwordUpdate");
    }

    //商户用户管理
    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUser")
    @RequiresPermissions("organizationUser:view")
    public String organizationUser() {
        return FebsUtil.view("organization/organizationUser");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUser/add")
    @RequiresPermissions("organizationUser:add")
    public String organizationUserAdd(Model model) {
        OrganizationUser user = super.getCurrentUser();
        model.addAttribute("organizationName", DatabaseCache.getOrgNameByOrgcode(user.getOrganizationCode()));
        model.addAttribute("organizationCode", user.getOrganizationCode());

        return FebsUtil.view("organization/organizationUserAdd");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "organization/organizationUser/update/{id}")
    @RequiresPermissions("organizationUser:update")
    public String organizationUserUpdate(@PathVariable String id, Model model) {
        OrganizationUser user = super.getCurrentUser();
        resolveOrganizationUserModel(user, id, model);
        model.addAttribute("organizationName", DatabaseCache.getOrgNameByOrgcode(user.getOrganizationCode()));
        return FebsUtil.view("organization/organizationUserUpdate");
    }
    // 跳转商户用户权限
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser/updateMenuLimit/{id}")
    @RequiresPermissions("organizationUserLimit:update")
    public String updateOrganizationUserLimit(@PathVariable Long id, Model model) {
        resolveOrganizationUserLimitModel(id, model);
        return FebsUtil.view("organization/organizationUserLimitUpdate");
    }
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser/updataPassWord")
    @RequiresPermissions("organizationUser:password:update")
    public String updateOrganizationUserPassWord(Model model) {
        return FebsUtil.view("organization/updateOrgUserPassWord");
    }

    //绑定谷歌
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser/bindGoogle")
    public String bindGoogle(Model model){
        OrganizationUser user = super.getCurrentUser();
        if(StringUtils.isEmpty(user.getGoogleKey())){
            String googleKey = GoogleAuthenticator.generateSecretKey();
            String googleQrcode = GoogleAuthenticator.getQRBarcode(getCurrentUser().getUserAccount(),googleKey);
            model.addAttribute("googleQrcode", googleQrcode);
            model.addAttribute("googleKey", googleKey);
        }
        return FebsUtil.view("organization/orgUserBindGoogle");
    }

    //绑定谷歌
    @GetMapping(FebsConstant.VIEW_PREFIX + "organizationUser/compelBindGoogle")
    public String compelBindGoogle(Model model){
        OrganizationUser user = super.getCurrentUser();
        if(user==null)
        {
        	return "redirect:/logout";
        }
        if(StringUtils.isEmpty(user.getGoogleKey())){
            String googleKey = GoogleAuthenticator.generateSecretKey();
            String googleQrcode = GoogleAuthenticator.getQRBarcode(getCurrentUser().getUserAccount(),googleKey);
            model.addAttribute("googleQrcode", googleQrcode);
            model.addAttribute("googleKey", googleKey);
        }
        String userAccount = user.getUserAccount();
        model.addAttribute("userAccount", userAccount);
        return FebsUtil.view("organization/orgUserCompelBindGoogle");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "404")
    public String error404() {
        return FebsUtil.view("error/404");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "403")
    public String error403() {
        return FebsUtil.view("error/403");
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "500")
    public String error500() {
        return FebsUtil.view("error/500");
    }

    /**
     * 获取商户用户权限
     * @param id
     * @param model
     */
    private void resolveOrganizationUserLimitModel(Long id, Model model) {
        OrganizationUser user = super.getCurrentUser();
        OrganizationUser queryOrgUser = new OrganizationUser();
        queryOrgUser.setOrganizationCode(user.getOrganizationCode());
        queryOrgUser.setId(new Long(id).intValue());
        List<OrganizationUser> orgUserList = this.organizationUserService.findOrganizationUsers(queryOrgUser);
        List<OrganizationUserLimit> limits = null;
        if(orgUserList.size() > 0){
            OrganizationUserLimit limit = new OrganizationUserLimit();
            limit.setUserId(id);
            limits = this.organizationUserLimitService.findOrganizationUserLimits(limit);
        }
        model.addAttribute("limits",limits);
        model.addAttribute("id",id);
    }

    /**
     * 获取商户用户信息
     * @param id
     * @param model
     */
    private void resolveOrganizationUserModel(OrganizationUser user, String id, Model model) {
        OrganizationUser queryOrgUser = new OrganizationUser();
        queryOrgUser.setOrganizationCode(user.getOrganizationCode());
        queryOrgUser.setId(Integer.parseInt(id));
        List<OrganizationUser> orgUserList = this.organizationUserService.findOrganizationUsers(queryOrgUser);
        OrganizationUser updateOrgUser = orgUserList.get(0);
        updateOrgUser.setUserPassword(null);
        updateOrgUser.setGoogleKey(null);
        updateOrgUser.setCreateUser(null);
        updateOrgUser.setRemark(null);
        updateOrgUser.setLoginIp(null);
        model.addAttribute("organizationUser",updateOrgUser);
    }
}
