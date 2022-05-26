package com.hero.sms.controller.channel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import com.google.common.collect.Lists;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FileUtil;
import com.hero.sms.entity.channel.NumberPool;
import com.hero.sms.entity.channel.NumberPoolQuery;
import com.hero.sms.service.channel.INumberPoolService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 号码池 Controller
 *
 * @author Administrator
 * @date 2020-04-15 21:08:21
 */
@Slf4j
@Validated
@Controller
@RequestMapping("numberPool")
public class NumberPoolController extends BaseController {

    @Autowired
    private INumberPoolService numberPoolService;

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("numberPool:list")
    public FebsResponse numberPoolList(QueryRequest request, NumberPool numberPool) {
        Map<String, Object> dataTable = getDataTable(this.numberPoolService.findNumbers(request, numberPool));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增NumberPool", exceptionMessage = "新增NumberPool失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("numberPool:add")
    public FebsResponse addNumberPool(@Valid NumberPool numberPool) {
        try {
            this.numberPoolService.createNumberPool(numberPool);
        } catch (Exception e) {
            new FebsResponse().fail().message(e.getMessage());
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "导入NumberPool", exceptionMessage = "导入NumberPool失败")
    @PostMapping("submitByForm")
    @ResponseBody
    @RequiresPermissions("numberPool:import")
    public FebsResponse importNumberPool(HttpServletRequest request, @Valid NumberPoolQuery numberPool) {
        boolean isBlankNumberStart = StringUtils.isBlank(numberPool.getPoolNumberStart());
        boolean isBlankNumberEnd = StringUtils.isBlank(numberPool.getPoolNumberEnd());
        if (numberPool.getNumberFile() == null || numberPool.getNumberFile().isEmpty()){
            if (isBlankNumberStart && isBlankNumberEnd){
                return new FebsResponse().fail().message("导入号码数据为空");
            }
            if (isBlankNumberStart){
                return new FebsResponse().fail().message("号码区间起始数值不能为空");
            }
            if (isBlankNumberEnd){
                return new FebsResponse().fail().message("号码区间结尾数值不能为空");
            }
        }
        //解析 区间导入
        List<String> numberList = Lists.newArrayList();
        if (!isBlankNumberStart && !isBlankNumberEnd){
            try {
                Long numberStart = Long.valueOf(numberPool.getPoolNumberStart());
                Long numberEnd = Long.valueOf(numberPool.getPoolNumberEnd());
                if (numberStart > numberEnd){
                    return new FebsResponse().fail().message("号码区间填写有误【左边值必须小于右边】");
                }
                for (Long i = numberStart;i <= numberEnd;i++){
                    numberList.add(String.valueOf(i));
                }
            } catch (NumberFormatException e) {
                return new FebsResponse().fail().message("号码区间填写有误【非数字】");
            }
        }else if(isBlankNumberStart && !isBlankNumberEnd){
            return new FebsResponse().fail().message("号码区间起始数值不能为空");
        }else if (!isBlankNumberStart && isBlankNumberEnd){
            return new FebsResponse().fail().message("号码区间结尾数值不能为空");
        }
        //解析 txt导入
        if (numberPool.getNumberFile() != null && !numberPool.getNumberFile().isEmpty()){
            String filename = numberPool.getNumberFile().getOriginalFilename();
            if (!StringUtils.endsWith(filename, ".txt")) {
                return new FebsResponse().message("文件类型不支持").fail();
            }
            String textNumbers = FileUtil.getContentFromInputStream(numberPool.getNumberFile());
            //替换中文符号"，"
            if (textNumbers.contains("，")){
                textNumbers = textNumbers.replaceAll("，",",");
            }
            List<String> list = Arrays.asList(textNumbers.split(",")).stream()
                    .filter(s -> StringUtils.isNotBlank(s) && NumberUtils.isDigits(s))
                    .collect(Collectors.toList());
            if (list.size() > 0){
                numberList.addAll(list);
            }
        }
        this.numberPoolService.batchCreate2Redis(numberList,numberPool.getGroupId());

        return new FebsResponse().success().message(String.format("导入成功，本次导入%s个号码",numberList.size()));
    }

    @ControllerEndpoint(operation = "删除NumberPool", exceptionMessage = "删除NumberPool失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("numberPool:delete")
    public FebsResponse deleteNumberPool(NumberPool numberPool) {
        int result = this.numberPoolService.deleteNumberPool(numberPool);
        if (result < 1) return new FebsResponse().fail().message("删除失败");
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除NumberPool", exceptionMessage = "批量删除NumberPool失败")
    @GetMapping("delete/{numberPoolIds}")
    @ResponseBody
    @RequiresPermissions("numberPool:delete")
    public FebsResponse deleteNumberPool(@NotBlank(message = "{required}") @PathVariable String numberPoolIds) {
        String[] ids = numberPoolIds.split(StringPool.COMMA);
        this.numberPoolService.deleteNumberPools(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改NumberPool", exceptionMessage = "修改NumberPool失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("numberPool:update")
    public FebsResponse updateNumberPool(NumberPool numberPool) {
        this.numberPoolService.updateNumberPool(numberPool);
        return new FebsResponse().success();
    }
    @ControllerEndpoint(operation = "导出NumberPool", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("numberPool:export")
    public void export(QueryRequest queryRequest, NumberPool numberPool, HttpServletResponse response) {
        List<NumberPool> numberPools = this.numberPoolService.findNumberPools(queryRequest, numberPool).getRecords();
        ExcelKit.$Export(NumberPool.class, response).downXlsx(numberPools, false);
    }

    @GetMapping("test")
    @ResponseBody
    public String test( HttpServletResponse response) {
        return this.numberPoolService.randomNumberPoolBySmsChannelId(13);
    }


}
