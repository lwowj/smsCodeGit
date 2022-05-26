package ${basePackage}.${controllerPackage};

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import ${basePackage}.${entityPackage}.${className};
import ${basePackage}.${servicePackage}.I${className}Service;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * ${tableComment} Controller
 *
 * @author ${author}
 * @date ${date}
 */
@Slf4j
@Validated
@Controller
@RequestMapping("${className?uncap_first}")
public class ${className}Controller extends BaseController {

    @Autowired
    private I${className}Service ${className?uncap_first}Service;

    @GetMapping(FebsConstant.VIEW_PREFIX + "${className?uncap_first}")
    public String ${className?uncap_first}Index(){
        return FebsUtil.view("${className?uncap_first}/${className?uncap_first}");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("${className?uncap_first}:list")
    public FebsResponse getAll${className}s(${className} ${className?uncap_first}) {
        return new FebsResponse().success().data(${className?uncap_first}Service.find${className}s(${className?uncap_first}));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("${className?uncap_first}:list")
    public FebsResponse ${className?uncap_first}List(QueryRequest request, ${className} ${className?uncap_first}) {
        Map<String, Object> dataTable = getDataTable(this.${className?uncap_first}Service.find${className}s(request, ${className?uncap_first}));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增${className}", exceptionMessage = "新增${className}失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("${className?uncap_first}:add")
    public FebsResponse add${className}(@Valid ${className} ${className?uncap_first}) {
        this.${className?uncap_first}Service.create${className}(${className?uncap_first});
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除${className}", exceptionMessage = "删除${className}失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("${className?uncap_first}:delete")
    public FebsResponse delete${className}(${className} ${className?uncap_first}) {
        this.${className?uncap_first}Service.delete${className}(${className?uncap_first});
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除${className}", exceptionMessage = "批量删除${className}失败")
    @GetMapping("delete/{${className?uncap_first}Ids}")
    @ResponseBody
    @RequiresPermissions("${className?uncap_first}:delete")
    public FebsResponse delete${className}(@NotBlank(message = "{required}") @PathVariable String ${className?uncap_first}Ids) {
        String[] ids = ${className?uncap_first}Ids.split(StringPool.COMMA);
        this.${className?uncap_first}Service.delete${className}s(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改${className}", exceptionMessage = "修改${className}失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("${className?uncap_first}:update")
    public FebsResponse update${className}(${className} ${className?uncap_first}) {
        this.${className?uncap_first}Service.update${className}(${className?uncap_first});
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改${className}", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("${className?uncap_first}:export")
    public void export(QueryRequest queryRequest, ${className} ${className?uncap_first}, HttpServletResponse response) {
        List<${className}> ${className?uncap_first}s = this.${className?uncap_first}Service.find${className}s(queryRequest, ${className?uncap_first}).getRecords();
        ExcelKit.$Export(${className}.class, response).downXlsx(${className?uncap_first}s, false);
    }
}
