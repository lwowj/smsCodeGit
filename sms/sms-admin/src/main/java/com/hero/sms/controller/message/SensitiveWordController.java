package com.hero.sms.controller.message;

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
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.message.SensitiveWord;
import com.hero.sms.enums.common.ModuleTypeEnums;
import com.hero.sms.service.common.IBusinessManage;
import com.hero.sms.service.message.ISensitiveWordService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 敏感词 Controller
 *
 * @author Administrator
 * @date 2020-03-20 23:04:40
 */
@Slf4j
@Validated
@Controller
@RequestMapping("sensitiveWord")
public class SensitiveWordController extends BaseController {

    @Autowired
    private ISensitiveWordService sensitiveWordService;
    @Autowired
    private IBusinessManage businessManage;

    @GetMapping(FebsConstant.VIEW_PREFIX + "sensitiveWord")
    public String sensitiveWordIndex(){
        return FebsUtil.view("sensitiveWord/sensitiveWord");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("sensitiveWord:list")
    public FebsResponse getAllSensitiveWords(SensitiveWord sensitiveWord) {
        return new FebsResponse().success().data(sensitiveWordService.findSensitiveWords(sensitiveWord));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sensitiveWord:list")
    public FebsResponse sensitiveWordList(QueryRequest request, SensitiveWord sensitiveWord) {
        Map<String, Object> dataTable = getDataTable(this.sensitiveWordService.findSensitiveWords(request, sensitiveWord));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增敏感词", exceptionMessage = "新增敏感词失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("sensitiveWord:add")
    public FebsResponse addSensitiveWord(@Valid SensitiveWord sensitiveWord) {
        this.sensitiveWordService.createSensitiveWord(sensitiveWord);
        try {
			reladSensitiveWordsCache("merch,gateway");
		} catch (ServiceException e) {
			return new FebsResponse().message("重载缓存失败，请手动清除缓存！");
		}
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除敏感词", exceptionMessage = "删除敏感词失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("sensitiveWord:delete")
    public FebsResponse deleteSensitiveWord(SensitiveWord sensitiveWord) {
        this.sensitiveWordService.deleteSensitiveWord(sensitiveWord);
        try {
			reladSensitiveWordsCache("merch,gateway");
		} catch (ServiceException e) {
			return new FebsResponse().message("重载缓存失败，请手动清除缓存！");
		}
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除敏感词", exceptionMessage = "批量删除敏感词失败")
    @GetMapping("delete/{sensitiveWordIds}")
    @ResponseBody
    @RequiresPermissions("sensitiveWord:delete")
    public FebsResponse deleteSensitiveWord(@NotBlank(message = "{required}") @PathVariable String sensitiveWordIds) {
        String[] ids = sensitiveWordIds.split(StringPool.COMMA);
        this.sensitiveWordService.deleteSensitiveWords(ids);
        try {
			reladSensitiveWordsCache("merch,gateway");
		} catch (ServiceException e) {
			return new FebsResponse().message("重载缓存失败，请手动清除缓存！");
		}
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改敏感词", exceptionMessage = "修改敏感词失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("sensitiveWord:update")
    public FebsResponse updateSensitiveWord(SensitiveWord sensitiveWord) {
        this.sensitiveWordService.updateSensitiveWord(sensitiveWord);
        try {
			reladSensitiveWordsCache("merch,gateway");
		} catch (ServiceException e) {
			return new FebsResponse().message("重载缓存失败，请手动清除缓存！");
		}
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改敏感词", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("sensitiveWord:export")
    public void export(QueryRequest queryRequest, SensitiveWord sensitiveWord, HttpServletResponse response) {
        List<SensitiveWord> sensitiveWords = this.sensitiveWordService.findSensitiveWords(queryRequest, sensitiveWord).getRecords();
        ExcelKit.$Export(SensitiveWord.class, response).downXlsx(sensitiveWords, false);
    }
    
    /**
     * 清理指定项目中的敏感词缓存
     * @param projectName
     * @throws ServiceException
     */
    private void reladSensitiveWordsCache(String projectName) throws ServiceException {
    	businessManage.reladProjectCacheForModule(projectName,String.valueOf(ModuleTypeEnums.SensitiveWord.getCode().intValue()));
    }
}
