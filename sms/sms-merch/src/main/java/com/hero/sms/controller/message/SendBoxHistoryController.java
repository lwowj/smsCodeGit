package com.hero.sms.controller.message;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.exportModel.SendBoxExcel;
import com.hero.sms.entity.message.history.SendBoxHistory;
import com.hero.sms.entity.message.history.SendBoxHistoryQuery;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.message.SendBoxTypeEnums;
import com.hero.sms.service.message.ISendBoxHistoryService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 历史发件箱 Controller
 *
 * @author Administrator
 * @date 2020-03-15 23:31:27
 */
@Slf4j
@Validated
@Controller
@RequestMapping("sendBoxHistory")
public class SendBoxHistoryController extends BaseController {

    @Autowired
    private ISendBoxHistoryService sendBoxHistoryService;

    @ControllerEndpoint( operation = "发件箱列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:list")
    @Limit(key = "sendBoxHistoryList", period = 1, count = 1, name = "历史发件箱列表", prefix = "limit")
    public FebsResponse sendBoxHistoryList(QueryRequest request, SendBoxHistoryQuery sendBoxHistory) {
        return  sendBoxHistory(request,sendBoxHistory);
    }

    @ControllerEndpoint( operation = "发件箱列表")
    @GetMapping("IList")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:IList")
    @Limit(key = "sendBoxHistoryIList", period = 1, count = 1, name = "历史发件箱列表", prefix = "limit")
    public FebsResponse sendBoxHistoryIList(QueryRequest request, SendBoxHistoryQuery sendBoxHistory) {
        return  sendBoxHistory(request,sendBoxHistory);
    }

    private FebsResponse sendBoxHistory(QueryRequest request, SendBoxHistoryQuery sendBoxHistory) {

        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode()) || user.getUserAccount() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己的发件箱
        sendBoxHistory.setOrgCode(user.getOrganizationCode());
        //sendBoxHistory.setCreateUsername(user.getUserAccount());
        sendBoxHistory.setAgentId(user.getOrganization().getAgentId());
        IPage<SendBoxHistory> datas = this.sendBoxHistoryService.findSendBoxHistorys(request, sendBoxHistory);
        List<SendBoxHistory> sendBoxList = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        sendBoxList.stream().forEach(item -> { // 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("sendCode",item.getSendCode());//批次号
            map.put("smsType",item.getSmsType());//消息类型
            map.put("auditState",item.getAuditState());//审核状态
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("type",item.getType());
           // map.put("smsNumbers",item.getSmsNumbers());//手机号码
            map.put("smsNumberArea",item.getSmsNumberArea());//所属区域
            map.put("refuseCause",item.getRefuseCause());//拒绝原因
            map.put("subType",item.getSubType());//提交类型
            map.put("timingTime",item.getTimingTime());//定时时间
            map.put("isTimingTime",item.getIsTimingTime());//定时时间
            map.put("createTime",item.getCreateTime());//提交时间
            map.put("smsWords",item.getSmsWords());//短信字数
            map.put("smsCount",item.getSmsCount());//有效的短信数
            map.put("numberCount",item.getNumberCount());//号码数
            BigDecimal divideNum = new BigDecimal(100);
            BigDecimal consumeAmout = new BigDecimal(item.getConsumeAmount()!=null?item.getConsumeAmount():0).divide(divideNum);
            map.put("consumeAmount",consumeAmout);//消费金额
            map.put("createUsername",item.getCreateUsername());//提交人
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);

        return new FebsResponse().success().data(dataTable);
    }

    /**
     * 获取手机码号
     * @param sendBoxId
     * @return
     */
    @GetMapping("smsNumbers/{sendBoxId}")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:list")
    public FebsResponse getSmsNumbers(@PathVariable("sendBoxId") Long sendBoxId){
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode())){
            return new FebsResponse().fail().data("账号异常，请求失败！");
        }
        SendBoxHistory sendBox = this.sendBoxHistoryService.getById(sendBoxId);
        //防止接口入侵
        if(sendBox == null || !user.getOrganizationCode().equals(sendBox.getOrgCode())){
            return new FebsResponse().fail().data("异常请求！");
        }
        //excel方式提交的  不支持查看手机号码，只能下载文件
        if (sendBox.getType() != null && sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()){
            return new FebsResponse().fail().data("异常请求！");
        }
        return new FebsResponse().success().data(sendBox.getSmsNumbers());
    }



    @ControllerEndpoint(operation = "导出SendBoxHistory", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("sendBoxHistory:export")
    @Limit(key = "sendBoxHistoryExport", period = 5, count = 1, name = "导出SendBoxHistory", prefix = "limit")
    public void export(QueryRequest queryRequest, SendBoxHistoryQuery sendBoxHistory, HttpServletResponse response) {
        OrganizationUserExt user = super.getCurrentUser();
        //限制只能查询自己的发件箱
        sendBoxHistory.setOrgCode(user.getOrganizationCode());
        //sendBoxHistory.setCreateUsername(user.getUserAccount());
        sendBoxHistory.setAgentId(user.getOrganization().getAgentId());

        //限制导出最大数量,默认10W
        int exportLimit = 100000;
        Code exportLimitCode = DatabaseCache.getCodeBySortCodeAndCode("System","exportLimit");
        if (exportLimitCode != null && exportLimitCode.getName() != null){
            try {
                exportLimit = Integer.valueOf(exportLimitCode.getName());
            }catch (Exception e){
                log.warn("字典代码【exportLimit】值转换失败！设置未生效，使用默认值");
            }
        }

        int count = this.sendBoxHistoryService.countByEntity(sendBoxHistory);
        if (count > exportLimit){
            response.setStatus(516);
            response.setHeader("exportLimit",String.valueOf(exportLimit));
            throw new FebsException(String.format("导出文件超过最大记录数【%d】限制，导出失败！",exportLimit));
        }

        //开始导出处理
        List<SendBoxHistory> sendBoxs = this.sendBoxHistoryService.findSendBoxHistorys(sendBoxHistory);
        List<SendBoxExcel> lst = new ArrayList<>();
        sendBoxs.stream().forEach(item -> {
            SendBoxExcel temp = new SendBoxExcel();
            BeanUtils.copyProperties(item,temp);
            String smsInfo = String.format("字数:%s 号码数:%s",item.getSmsWords(),item.getNumberCount());
            temp.setSmsInfo(smsInfo);
            lst.add(temp);
        });

        ExcelKit.$Export(SendBoxExcel.class, response).downXlsx(lst, false);
    }
}
