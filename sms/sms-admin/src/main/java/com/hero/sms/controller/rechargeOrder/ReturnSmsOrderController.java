package com.hero.sms.controller.rechargeOrder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.entity.ReturnSmsOrderImport;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrder;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrderExt;
import com.hero.sms.entity.rechargeOrder.ReturnSmsOrderQuery;
import com.hero.sms.service.rechargeOrder.IReturnSmsOrderService;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;
import com.wuwenze.poi.handler.ExcelReadHandler;
import com.wuwenze.poi.pojo.ExcelErrorField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 退还短信条数记录 Controller
 *
 * @author Administrator
 * @date 2020-04-20 00:13:55
 */
@Slf4j
@Validated
@Controller
@RequestMapping("returnSmsOrder")
public class ReturnSmsOrderController extends BaseController {

    @Autowired
    private IReturnSmsOrderService returnSmsOrderService;


    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("returnSmsOrder:list")
    public FebsResponse returnSmsOrderList(QueryRequest request, ReturnSmsOrderQuery returnSmsOrder) {

        IPage<ReturnSmsOrder> datas = this.returnSmsOrderService.findReturnSmsOrders(request, returnSmsOrder);
        List<ReturnSmsOrderExt> list = Lists.newArrayList();
        datas.getRecords().stream().forEach(item -> {
            ReturnSmsOrderExt  returnSmsOrderExt = new ReturnSmsOrderExt();
            BeanUtils.copyProperties(item,returnSmsOrderExt);

            if (item.getAgentId() != null){
                returnSmsOrderExt.setAgentName(DatabaseCache.getAgentNameByAgentId(item.getAgentId()));
            }
            if (item.getUpAgentId() != null){
                returnSmsOrderExt.setUpAgentName(DatabaseCache.getAgentNameByAgentId(item.getUpAgentId()));
            }
            if (StringUtils.isNotBlank(item.getOrderNo())){
                returnSmsOrderExt.setOrgName(DatabaseCache.getOrgNameByOrgcode(item.getOrgCode()));
            }

            list.add(returnSmsOrderExt);
        });
        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增ReturnSmsOrder", exceptionMessage = "新增ReturnSmsOrder失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("returnSmsOrder:add")
    public FebsResponse addReturnSmsOrder(@Valid ReturnSmsOrder returnSmsOrder) {
        try {
            User user = getCurrentUser();
            if (user == null){
                return new FebsResponse().message("用户信息错误！").fail();
            }
            this.returnSmsOrderService.createReturnSmsOrder(returnSmsOrder,user.getUsername());
        } catch (ServiceException e) {
            return new FebsResponse().fail().message(e.getMessage());
        }
        return new FebsResponse().success();
    }
    @ControllerEndpoint(operation = "导入ReturnSmsOrder", exceptionMessage = "导入ReturnSmsOrder失败")
    @PostMapping("import")
    @ResponseBody
    @RequiresPermissions("returnSmsOrder:import")
    public FebsResponse importReturnSmsOrder(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new FebsException("导入数据为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".xlsx")) {
            throw new FebsException("只支持.xlsx类型文件导入");
        }
        User user = getCurrentUser();
        if (user == null){
            return new FebsResponse().message("用户信息错误！").fail();
        }
        // 开始导入操作
        Stopwatch stopwatch = Stopwatch.createStarted();
        final List<ReturnSmsOrder> data = Lists.newArrayList();
        final List<Map<String, Object>> error = Lists.newArrayList();
        ExcelKit.$Import(ReturnSmsOrderImport.class).readXlsx(file.getInputStream(), new ExcelReadHandler<ReturnSmsOrderImport>() {

            @Override
            public void onSuccess(int i, int i1, ReturnSmsOrderImport returnSmsOrderImport) {
                // 数据校验成功时，加入集合
                returnSmsOrderImport.setCreateTime(new Date());
                ReturnSmsOrder returnSmsOrder = new ReturnSmsOrder();
                BeanUtils.copyProperties(returnSmsOrderImport,returnSmsOrder);
                data.add(returnSmsOrder);
            }

            @Override
            public void onError(int sheet, int row, List<ExcelErrorField> errorFields) {
                // 数据校验失败时，记录到 error集合
                error.add(ImmutableMap.of("row", row, "errorFields", errorFields));
            }
        });
        if (CollectionUtils.isNotEmpty(data)) {
            // 将合法的记录批量入库
            this.returnSmsOrderService.createTask(data,user.getUsername());
        }
        ImmutableMap<String, Object> result = ImmutableMap.of(
                "time", stopwatch.stop().toString(),
                "data", data,
                "error", error
        );
        return new FebsResponse().success().data(result);
    }

    /**
     * 生成 Excel导入模板
     */
    @GetMapping("template")
    @RequiresPermissions("returnSmsOrder:import")
    public void generateImportTemplate(HttpServletResponse response) {
        // 构建数据
        List<ReturnSmsOrderImport> list = new ArrayList<>();

        // 构建模板
        ExcelKit.$Export(ReturnSmsOrderImport.class, response).downXlsx(list, true);
    }


    @ControllerEndpoint(operation = "删除ReturnSmsOrder", exceptionMessage = "删除ReturnSmsOrder失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("returnSmsOrder:delete")
    public FebsResponse deleteReturnSmsOrder(ReturnSmsOrderQuery returnSmsOrder) {
        this.returnSmsOrderService.deleteReturnSmsOrder(returnSmsOrder);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除ReturnSmsOrder", exceptionMessage = "批量删除ReturnSmsOrder失败")
    @GetMapping("delete/{returnSmsOrderIds}")
    @ResponseBody
    @RequiresPermissions("returnSmsOrder:delete")
    public FebsResponse deleteReturnSmsOrder(@NotBlank(message = "{required}") @PathVariable String returnSmsOrderIds) {
        String[] ids = returnSmsOrderIds.split(StringPool.COMMA);
        this.returnSmsOrderService.deleteReturnSmsOrders(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改ReturnSmsOrder", exceptionMessage = "修改ReturnSmsOrder失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("returnSmsOrder:update")
    public FebsResponse updateReturnSmsOrder(ReturnSmsOrder returnSmsOrder) {
        this.returnSmsOrderService.updateReturnSmsOrder(returnSmsOrder);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "导出ReturnSmsOrder", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("returnSmsOrder:export")
    public void export(QueryRequest queryRequest, ReturnSmsOrderQuery returnSmsOrder, HttpServletResponse response) {
        List<ReturnSmsOrder> returnSmsOrders = this.returnSmsOrderService.findReturnSmsOrders(returnSmsOrder);
        try {
            ExcelKit.$Export(ReturnSmsOrder.class, response).downXlsx(returnSmsOrders, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
