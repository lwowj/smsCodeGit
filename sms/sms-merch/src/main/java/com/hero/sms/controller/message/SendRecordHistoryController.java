package com.hero.sms.controller.message;

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
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.exportModel.SendRecordExcel;
import com.hero.sms.entity.message.history.SendRecordHistory;
import com.hero.sms.entity.message.history.SendRecordHistoryQuery;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.service.message.ISendRecordHistoryService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 历史发送记录 Controller
 *
 * @author Administrator
 * @date 2020-03-15 23:31:38
 */
@Slf4j
@Validated
@Controller
@RequestMapping("sendRecordHistory")
public class SendRecordHistoryController extends BaseController {

    @Autowired
    private ISendRecordHistoryService sendRecordHistoryService;

    @ControllerEndpoint( operation = "历史发送记录")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sendRecordHistory:list")
    @Limit(key = "sendRecordHistoryList", period = 1, count = 1, name = "历史发送记录", prefix = "limit")
    public FebsResponse sendRecordHistoryList(QueryRequest request, SendRecordHistoryQuery sendRecordHistory) {
        return sendRecordHistory(request,sendRecordHistory,false);
    }

    @ControllerEndpoint( operation = "历史发送记录")
    @GetMapping("IList")
    @ResponseBody
    @RequiresPermissions("sendRecordHistory:IList")
    @Limit(key = "sendRecordHistoryIList", period = 1, count = 1, name = "历史发送记录", prefix = "limit")
    public FebsResponse sendRecordHistoryIList(QueryRequest request, SendRecordHistoryQuery sendRecordHistory) {
        return sendRecordHistory(request,sendRecordHistory,true);
    }

    private FebsResponse sendRecordHistory(QueryRequest request, SendRecordHistoryQuery sendRecordHistory,boolean incompl) {
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode()) || user.getUserAccount() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己的发件箱
        sendRecordHistory.setOrgCode(user.getOrganizationCode());
        sendRecordHistory.setAgentId(user.getOrganization().getAgentId());

        IPage<SendRecordHistory> datas = this.sendRecordHistoryService.findSendRecordHistorys(request, sendRecordHistory);

        List<SendRecordHistory> list = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        list.stream().forEach(item -> {// 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("sendCode",item.getSendCode());//批次号
            if(incompl){
                map.put("smsNumber",item.getSmsNumber().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));//手机号码
            }else {
                map.put("smsNumber",item.getSmsNumber());//手机号码
            }
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("state",item.getState());//状态
            map.put("stateDesc",item.getStateDesc());//状态说明
            map.put("smsNumberOperator",item.getSmsNumberOperator());//运营商
            map.put("smsType",item.getSmsType());//消息类型
            map.put("smsNumberArea",item.getSmsNumberArea());//手机号码归属地区
            map.put("smsNumberProvince",item.getSmsNumberProvince());//手机号码鬼实地（省份）
            map.put("smsCount",item.getSmsCount());//有效短信数
            map.put("smsWords",item.getSmsWords());//短信字数
            map.put("returnTime", DateUtil.getDistanceTimes(item.getCreateTime(),item.getReceiptTime()));
            map.put("receiptTime",item.getReceiptTime());
            map.put("createTime",item.getCreateTime());
            map.put("orgCode",item.getOrgCode());

            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("运营商:%s 地域:%s %s",
                    code != null? code.getName():"未知",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
            map.put("operatorInfo",operatorInfo);

            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "导出SendRecordHistory", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("sendRecordHistory:export")
    @Limit(key = "sendRecordHistoryExport", period = 5, count = 1, name = "导出SendRecordHistory", prefix = "limit")
    public void export(QueryRequest queryRequest, SendRecordHistoryQuery sendRecordHistory, HttpServletResponse response) {
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode()) || user.getUserAccount() == null) {
            log.error("获取不到用户信息，导出SendRecord失败");
        }
        //限制只能查询自己的发件箱
        sendRecordHistory.setOrgCode(user.getOrganizationCode());
        sendRecordHistory.setAgentId(user.getOrganization().getAgentId());

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
        Integer count = this.sendRecordHistoryService.countByQueryEntity(sendRecordHistory);
        if (count > exportLimit){
            response.setStatus(516);
            response.setHeader("exportLimit",String.valueOf(exportLimit));
            throw new FebsException(String.format("导出文件超过最大记录数【%d】限制，导出失败！",exportLimit));
        }


        List<SendRecordHistory> sendRecords = this.sendRecordHistoryService.findSendRecordHistorys(sendRecordHistory);
        List<SendRecordExcel> lst = new ArrayList<>();
        sendRecords.stream().forEach(item -> {
            SendRecordExcel excel = new SendRecordExcel();
            BeanUtils.copyProperties(item,excel);
            //运营商详情
            Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneOperator",item.getSmsNumberOperator());
            String smsNumberAreaName = SmsNumberAreaCodeEnums.getNameByCode(item.getSmsNumberArea());
            String smsNumberAreaProvinceName = SmsNumberAreaProvinceEnums.getNameByCode(item.getSmsNumberProvince());
            String operatorInfo = String.format("运营商:%s 地域:%s %s",
                    code != null? code.getName():"未知",
                    StringUtils.isNotBlank(smsNumberAreaName)?smsNumberAreaName:"未知" ,
                    StringUtils.isNotBlank(smsNumberAreaProvinceName)?smsNumberAreaProvinceName:"未知");
            excel.setOperatorInfo(operatorInfo);
            excel.setReturnTime(DateUtil.getDistanceTimes(item.getCreateTime(),item.getReceiptTime()));
            excel.setReceiptTime(item.getReceiptTime());
            //短信详情
            excel.setSmsInfo(String.format("字数:%s 短信数:%s",item.getSmsWords(),item.getSmsCount()));
            lst.add(excel);
        });
        ExcelKit.$Export(SendRecordExcel.class, response).downXlsx(lst, false);
    }
}
