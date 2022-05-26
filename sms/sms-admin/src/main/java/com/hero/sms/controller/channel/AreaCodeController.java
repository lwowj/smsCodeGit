package com.hero.sms.controller.channel;


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
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.channel.AreaCodeExt;
import com.hero.sms.enums.common.ModuleTypeEnums;
import com.hero.sms.service.channel.IAreaCodeService;
import com.hero.sms.service.common.IBusinessManage;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 国家地区表 Controller
 *
 * @author Administrator
 * @date 2022-03-18 15:41:19
 */
@Slf4j
@Validated
@Controller
@RequestMapping("areaCode")
public class AreaCodeController extends BaseController {

    @Autowired
    private IAreaCodeService areaCodeService;
    @Autowired
    private IBusinessManage businessManage;
    
    @Autowired
    private DatabaseCache databaseCache;
    
    @GetMapping(FebsConstant.VIEW_PREFIX + "areaCode")
    public String areaCodeIndex(){
        return FebsUtil.view("areaCode/areaCode");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("areaCode:list")
    public FebsResponse getAllAreaCodes(AreaCodeExt areaCode) {
        return new FebsResponse().success().data(areaCodeService.findAreaCodes(areaCode));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("areaCode:list")
    public FebsResponse areaCodeList(QueryRequest request, AreaCodeExt areaCode) {
        Map<String, Object> dataTable = getDataTable(this.areaCodeService.findAreaCodes(request, areaCode));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增AreaCode", exceptionMessage = "新增AreaCode失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("areaCode:add")
    public FebsResponse addAreaCode(@Valid AreaCode areaCode) {
        try {
        	this.areaCodeService.createAreaCode(areaCode);
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除AreaCode", exceptionMessage = "删除AreaCode失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("areaCode:delete")
    public FebsResponse deleteAreaCode(AreaCode areaCode) {
        this.areaCodeService.deleteAreaCode(areaCode);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除AreaCode", exceptionMessage = "批量删除AreaCode失败")
    @GetMapping("delete/{areaCodeIds}")
    @ResponseBody
    @RequiresPermissions("areaCode:delete")
    public FebsResponse deleteAreaCode(@NotBlank(message = "{required}") @PathVariable String areaCodeIds) {
        String[] ids = areaCodeIds.split(StringPool.COMMA);
        this.areaCodeService.deleteAreaCodes(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改AreaCode", exceptionMessage = "修改AreaCode失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("areaCode:update")
    public FebsResponse updateAreaCode(AreaCode areaCode) {
    	try {
    		this.areaCodeService.updateAreaCode(areaCode);
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "导出AreaCode", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("areaCode:export")
    public void export(QueryRequest queryRequest, AreaCodeExt areaCode, HttpServletResponse response) {
        List<AreaCode> areaCodes = this.areaCodeService.findAreaCodes(queryRequest, areaCode).getRecords();
        ExcelKit.$Export(AreaCode.class, response).downXlsx(areaCodes, false);
    }
    
    @ControllerEndpoint(operation = "刷新地域配置缓存", exceptionMessage = "刷新地域配置缓存失败")
    @GetMapping("loadAreaCodeCache")
    @ResponseBody
    @RequiresPermissions("areaCode:loadAreaCodeCache")
    public FebsResponse loadAreaCodeCache(){
        try {
        	databaseCache.initAreaCodeList();
        	businessManage.reladProjectCacheForModule("merch,agent,gateway",String.valueOf(ModuleTypeEnums.AreaCodeList.getCode().intValue()));
		} catch (ServiceException e) {
			return new FebsResponse().message("重载缓存失败，请手动清除缓存！");
		}
        return new FebsResponse().success();
    }
}
