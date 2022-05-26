package com.hero.sms.controller.statistic;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.StatisticalChannel;
import com.hero.sms.entity.message.StatisticalChannelExt;
import com.hero.sms.entity.message.StatisticalChannelQuery;
import com.hero.sms.entity.message.exportModel.StatisticalChannelExcel;
import com.hero.sms.service.message.IStatisticalChannelService;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 按照通道统计 Controller
 *
 * @author Administrator
 * @date 2020-05-28 15:59:07
 */
@Slf4j
@Validated
@Controller
@RequestMapping("statisticalChannel")
public class StatisticalChannelController extends BaseController {

    @Autowired
    private IStatisticalChannelService statisticalChannelService;


    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("statisticalChannel:list")
    public FebsResponse statisticalChannelList(QueryRequest request, StatisticalChannelQuery statisticalChannel) {

        IPage<StatisticalChannel> datas = this.statisticalChannelService.findStatisticalChannels(request, statisticalChannel);
        List<StatisticalChannelExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item -> {
            StatisticalChannelExt ext = new StatisticalChannelExt();
            BeanUtils.copyProperties(item,ext);
            String channelName = DatabaseCache.getSmsChannelNameById(item.getChannelId());
            if (StringUtils.isBlank(channelName)){
                channelName = "未知";
            }
            ext.setChannelName(channelName);
            ext.setRealReqSuccessCount(ext.getReqSuccessCount() + ext.getReceiptSuccessCount() + ext.getReceiptFailCount());
            list.add(ext);
        });


        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);

        return new FebsResponse().success().data(dataTable);
    }
    @GetMapping("pie")
    @ResponseBody
    @RequiresPermissions("statisticalChannel:list")
    public FebsResponse statisticalChannelPie(StatisticalChannelQuery statisticalChannel) {

        List<Map<String,Object>> result = this.statisticalChannelService.sumStatisticalChannels(statisticalChannel);
        List<String> channels = Lists.newArrayList();
        List<Map<String,Object>> totalCounts = Lists.newArrayList();
        List<Map<String,Object>> incomeAmounts = Lists.newArrayList();
        Map map = Maps.newHashMap();
        map.put("channelNameS",channels);
        map.put("totalCounts",totalCounts);
        map.put("incomeAmounts",incomeAmounts);
        result.stream().forEach(item -> {
            Integer channelId = (Integer) item.get("channelId");
            String channelName = DatabaseCache.getSmsChannelNameById(channelId);
            if (StringUtils.isBlank(channelName)){
                channelName = "未知";
            }
            channels.add(channelName);

            Map<String,Object> totalCount = Maps.newHashMap();
            totalCount.put("name",channelName);
            totalCount.put("value",item.get("totalCount"));

            totalCounts.add(totalCount);

            Map<String,Object> incomeAmount = Maps.newHashMap();
            incomeAmount.put("name",channelName);
            incomeAmount.put("value",item.get("incomeAmount"));
            incomeAmounts.add(incomeAmount);

        });
        return new FebsResponse().success().data(map);
    }

    @ControllerEndpoint(operation = "导出StatisticalChannel", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("statisticalChannel:export")
    public void export(StatisticalChannelQuery statisticalChannel, HttpServletResponse response) {
        List<StatisticalChannel> statisticalChannels = this.statisticalChannelService.findStatisticalChannels(statisticalChannel);
        List<StatisticalChannelExcel> lst = Lists.newArrayList();
        statisticalChannels.stream().forEach(item ->{
            StatisticalChannelExcel excel = new StatisticalChannelExcel();
            BeanUtils.copyProperties(item,excel);
            String channelName = DatabaseCache.getSmsChannelNameById(item.getChannelId());
            if (StringUtils.isBlank(channelName)){
                channelName = "未知";
            }
            excel.setChannelName(channelName);
            lst.add(excel);
        });
        ExcelKit.$Export(StatisticalChannelExcel.class, response).downXlsx(lst, false);
    }
}
