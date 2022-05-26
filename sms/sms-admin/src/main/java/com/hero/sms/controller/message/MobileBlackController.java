package com.hero.sms.controller.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.commands.utils.MobileUtil;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.SpringContextUtil;
import com.hero.sms.entity.message.MobileBlack;
import com.hero.sms.service.message.IMobileAreaService;
import com.hero.sms.service.message.IMobileBlackService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 手机号码黑名单 Controller
 *
 * @author Administrator
 * @date 2020-03-17 01:17:22
 */
@Slf4j
@Validated
@Controller
@RequestMapping("mobileBlack")
public class MobileBlackController extends BaseController {

    @Autowired
    private IMobileBlackService mobileBlackService;

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("mobileBlack:list")
    public FebsResponse mobileBlackList(QueryRequest request, MobileBlack mobileBlack) {
        Map<String, Object> dataTable = getDataTable(this.mobileBlackService.findMobileBlacks(request, mobileBlack));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增MobileBlack", exceptionMessage = "新增MobileBlack失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("mobileBlack:add")
    public FebsResponse addMobileBlack(@Valid MobileBlack mobileBlack) {
        try {
            this.mobileBlackService.createMobileBlack(mobileBlack);
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除MobileBlack", exceptionMessage = "删除MobileBlack失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("mobileBlack:delete")
    public FebsResponse deleteMobileBlack(MobileBlack mobileBlack) {
        try {
            this.mobileBlackService.deleteMobileBlack(mobileBlack);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除MobileBlack", exceptionMessage = "批量删除MobileBlack失败")
    @GetMapping("delete/{mobileBlackIds}")
    @ResponseBody
    @RequiresPermissions("mobileBlack:delete")
    public FebsResponse deleteMobileBlack(@NotBlank(message = "{required}") @PathVariable String mobileBlackIds) {
        String[] ids = mobileBlackIds.split(StringPool.COMMA);
        this.mobileBlackService.deleteMobileBlacks(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改MobileBlack", exceptionMessage = "修改MobileBlack失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("mobileBlack:update")
    public FebsResponse updateMobileBlack(MobileBlack mobileBlack) {
        this.mobileBlackService.updateMobileBlack(mobileBlack);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改MobileBlack", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("mobileBlack:export")
    public void export(QueryRequest queryRequest, MobileBlack mobileBlack, HttpServletResponse response) {
        List<MobileBlack> mobileBlacks = this.mobileBlackService.findMobileBlacks(queryRequest, mobileBlack).getRecords();
        ExcelKit.$Export(MobileBlack.class, response).downXlsx(mobileBlacks, false);
    }
    
    @ControllerEndpoint(operation = "查询号码归属地", exceptionMessage = "查询号码归属地失败")
    @PostMapping("queryNumAddr")
    @ResponseBody
    @RequiresPermissions("mobile:queryNumAddr")
    public FebsResponse queryNumAddr(MobileBlack mobileBlack) 
    {
    	String number = mobileBlack.getNumber();
    	String area =  mobileBlack.getArea();
    	Map<String,String> mobileMap = new HashMap<String, String>(); 
	    if("TaoBao".equals(area))
	    {
	    	mobileMap = MobileUtil.getFromInterfaceForTaoBao(number);
	    }
	    else if("BaiDu".equals(area))
	    {
	    	mobileMap = MobileUtil.getFromInterfaceForBaiDu(number);
	    }
	    else if("360".equals(area))
	    {
	    	mobileMap = MobileUtil.getFromInterfaceFor360(number);
	    }
	    else if("BaiFuBao".equals(area))
	    {
	    	mobileMap = MobileUtil.getFromInterfaceForBaiFuBao(number);
	    }
	    else
	    {
	    	IMobileAreaService mobileAreaService = SpringContextUtil.getBean(IMobileAreaService.class);
	    	mobileMap = MobileUtil.getFromDb(mobileAreaService,number);
	    }
	    String message = "查询失败，返回空";
	    if(mobileMap!=null)
	    {
	    	message = mobileMap.toString();
	    }
        return new FebsResponse().success().message(message);
    }
}
