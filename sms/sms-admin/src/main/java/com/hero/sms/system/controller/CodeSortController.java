package com.hero.sms.system.controller;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.common.CodeSort;
import com.hero.sms.service.common.ICodeSortService;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 代码分类 Controller
 *
 * @author MrJac
 * @date 2020-03-04 16:53:27
 */
@Slf4j
@Validated
@RestController
@RequestMapping("codeSort")
public class CodeSortController extends BaseController {

    @Autowired
    private ICodeSortService codeSortService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "codeSort")
    public String codeSortIndex(){
        return FebsUtil.view("codeSort");
    }


    @GetMapping
    @ResponseBody
    @RequiresPermissions("codeSort:view")
    public FebsResponse getAllCodeSorts(CodeSort codeSort) {
        return new FebsResponse().success().data(codeSortService.findCodeSorts(codeSort));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("codeSort:view")
    public FebsResponse codeSortList(QueryRequest request, CodeSort codeSort) {
        Map<String, Object> dataTable = getDataTable(this.codeSortService.findCodeSorts(request, codeSort));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增CodeSort", exceptionMessage = "新增CodeSort失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("codeSort:add")
    public FebsResponse addCodeSort(@Valid CodeSort codeSort) {
        this.codeSortService.createCodeSort(codeSort);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除CodeSort", exceptionMessage = "删除CodeSort失败")
    @GetMapping("delete/{codeSortIds}")
    @ResponseBody
    @RequiresPermissions("codeSort:delete")
    public FebsResponse deleteCodeSort(@NotBlank(message = "{required}") @PathVariable String codeSortIds) {
        String[] ids = codeSortIds.split(StringPool.COMMA);
        this.codeSortService.deleteCodeSorts(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改CodeSort", exceptionMessage = "修改CodeSort失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("codeSort:update")
    public FebsResponse updateCodeSort(CodeSort codeSort) {
        this.codeSortService.updateCodeSort(codeSort);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改CodeSort", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("codeSort:export")
    public void export(QueryRequest queryRequest, CodeSort codeSort, HttpServletResponse response) {
        List<CodeSort> codeSorts = this.codeSortService.findCodeSorts(queryRequest, codeSort).getRecords();
        ExcelKit.$Export(CodeSort.class, response).downXlsx(codeSorts, false);
    }
}
