package com.hero.sms.controller.channel;

import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.channel.NumberPoolGroup;
import com.hero.sms.service.channel.INumberPoolGroupService;
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
 * 号码池组表 Controller
 *
 * @author Administrator
 * @date 2020-04-15 21:09:40
 */
@Slf4j
@Validated
@Controller
@RequestMapping("numberPoolGroup")
public class NumberPoolGroupController extends BaseController {

    @Autowired
    private INumberPoolGroupService numberPoolGroupService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "numberPoolGroup")
    public String numberPoolGroupIndex(){
        return FebsUtil.view("numberPoolGroup/numberPoolGroup");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("numberPoolGroup:list")
    public FebsResponse getAllNumberPoolGroups(NumberPoolGroup numberPoolGroup) {
        return new FebsResponse().success().data(numberPoolGroupService.findNumberPoolGroups(numberPoolGroup));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("numberPoolGroup:list")
    public FebsResponse numberPoolGroupList(QueryRequest request, NumberPoolGroup numberPoolGroup) {
        Map<String, Object> dataTable = getDataTable(this.numberPoolGroupService.findNumberPoolGroups(request, numberPoolGroup));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增NumberPoolGroup", exceptionMessage = "新增NumberPoolGroup失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("numberPoolGroup:add")
    public FebsResponse addNumberPoolGroup(@Valid NumberPoolGroup numberPoolGroup) {
        this.numberPoolGroupService.createNumberPoolGroup(numberPoolGroup);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除NumberPoolGroup", exceptionMessage = "删除NumberPoolGroup失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("numberPoolGroup:delete")
    public FebsResponse deleteNumberPoolGroup(NumberPoolGroup numberPoolGroup) {
        try {
            this.numberPoolGroupService.deleteNumberPoolGroup(numberPoolGroup);
        } catch (ServiceException e) {
            return new FebsResponse().fail().message(e.getMessage());
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除NumberPoolGroup", exceptionMessage = "批量删除NumberPoolGroup失败")
    @GetMapping("delete/{numberPoolGroupIds}")
    @ResponseBody
    @RequiresPermissions("numberPoolGroup:delete")
    public FebsResponse deleteNumberPoolGroup(@NotBlank(message = "{required}") @PathVariable String numberPoolGroupIds) {
        String[] ids = numberPoolGroupIds.split(StringPool.COMMA);
        this.numberPoolGroupService.deleteNumberPoolGroups(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改NumberPoolGroup", exceptionMessage = "修改NumberPoolGroup失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("numberPoolGroup:update")
    public FebsResponse updateNumberPoolGroup(NumberPoolGroup numberPoolGroup) {
        this.numberPoolGroupService.updateNumberPoolGroup(numberPoolGroup);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改NumberPoolGroup", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("numberPoolGroup:export")
    public void export(QueryRequest queryRequest, NumberPoolGroup numberPoolGroup, HttpServletResponse response) {
        List<NumberPoolGroup> numberPoolGroups = this.numberPoolGroupService.findNumberPoolGroups(queryRequest, numberPoolGroup).getRecords();
        ExcelKit.$Export(NumberPoolGroup.class, response).downXlsx(numberPoolGroups, false);
    }
}
