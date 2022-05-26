package com.hero.sms.system.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.utils.RegexUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.enums.common.ModuleTypeEnums;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.service.common.ICodeService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 代码表 Controller
 *
 * @author MrJac
 * @date 2020-03-04 21:15:50
 */
@Slf4j
@Validated
@RestController
@RequestMapping("code")
public class CodeController extends BaseController {

    @Autowired
    private ICodeService codeService;

    @Autowired
    private IBusinessManage businessManage;

    @GetMapping(FebsConstant.VIEW_PREFIX + "code")
    public String codeIndex(){
        return FebsUtil.view("code");
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("code:view")
    public FebsResponse codeList(QueryRequest request, Code code) {
        Map<String, Object> dataTable = getDataTable(this.codeService.findCodes(request, code));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增Code", exceptionMessage = "新增Code失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("code:add")
    public FebsResponse addCode(@Valid Code code) {
        try {
            this.codeService.createCode(code);
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除Code", exceptionMessage = "删除Code失败")
    @GetMapping("delete/{codeSortIds}")
    @ResponseBody
    @RequiresPermissions("code:delete")
    public FebsResponse deleteCode(@NotBlank(message = "{required}") @PathVariable String codeSortIds) {
        String[] ids = codeSortIds.split(StringPool.COMMA);
        this.codeService.deleteCodes(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改Code", exceptionMessage = "修改Code失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("code:update")
    public FebsResponse updateCode(Code code) {
    	/**
         * @begin 2021-01-23
         * 新增限制的格式校验
         */
        if("SmsNumberLimitRule".equals(code.getSortCode()))
        {
        	if (StringUtils.isNotBlank(code.getName()))
            {
            	if(!RegexUtil.isNumberLimit(code.getName()))
            	{
            		return new FebsResponse().message("限制表达式格式不正确！").fail();
            	}
            }
        }
        /**
         * @end
         */
        this.codeService.updateCode(code);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "刷新admin缓存", exceptionMessage = "刷新admin缓存失败")
    @GetMapping("loadReapplication")
    @ResponseBody
    @RequiresPermissions("code:loadReapplication")
    public FebsResponse loadReapplication(){
        this.businessManage.loadReapplication();
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "刷新服务器缓存", exceptionMessage = "刷新服务器缓存失败")
    @GetMapping("serverLoadConfigCache")
    @ResponseBody
    @RequiresPermissions("code:serverLoadConfigCache")
    public FebsResponse serverLoadConfigCache(String cacheType) {
        try {
            businessManage.serverLoadConfigCache(cacheType);
        }catch(ServiceException se) {
            return new FebsResponse().fail().message(se.getMessage());
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "导出Excel", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("code:export")
    public void export(QueryRequest queryRequest, Code code, HttpServletResponse response) {
        List<Code> codes = this.codeService.findCodes(queryRequest, code).getRecords();
        ExcelKit.$Export(Code.class, response).downXlsx(codes, false);
    }
    
    @ControllerEndpoint(operation = "刷新字典模块缓存", exceptionMessage = "刷新字典模块缓存失败")
    @GetMapping("loadReladCodeCache")
    @ResponseBody
    @RequiresPermissions("code:loadReapplication")
    public FebsResponse loadReladCodeCache(){
        try {
        	this.businessManage.loadConfigCacheForOneModule(String.valueOf(ModuleTypeEnums.CodeList.getCode().intValue()));
        	this.businessManage.reladProjectCacheForModule("merch,agent,gateway",String.valueOf(ModuleTypeEnums.CodeList.getCode().intValue()));
		} catch (ServiceException e) {
			return new FebsResponse().message("重载缓存失败，请手动清除缓存！");
		}
        return new FebsResponse().success();
    }
}
