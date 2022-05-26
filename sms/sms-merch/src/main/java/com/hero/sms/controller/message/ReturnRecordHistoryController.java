package com.hero.sms.controller.message;

import java.text.SimpleDateFormat;
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
import com.hero.sms.entity.message.exportModel.ReturnRecordExcel;
import com.hero.sms.entity.message.history.ReturnRecordHistory;
import com.hero.sms.entity.message.history.ReturnRecordHistoryQuery;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.message.IReturnRecordHistoryService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * 历史回执记录 Controller
 *
 * @author Administrator
 * @date 2020-03-15 23:31:41
 */
@Slf4j
@Validated
@Controller
@RequestMapping("returnRecordHistory")
public class ReturnRecordHistoryController extends BaseController {

    @Autowired
    private IReturnRecordHistoryService returnRecordHistoryService;

    @ControllerEndpoint( operation = "历史回执列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("returnRecordHistory:list")
    @Limit(key = "returnRecordHistoryList", period = 1, count = 1, name = "历史回执列表", prefix = "limit")
    public FebsResponse returnRecordHistoryList(QueryRequest request, ReturnRecordHistoryQuery returnRecordHistory) {
        return returnRecordHistory(request,returnRecordHistory,false);
    }

    @ControllerEndpoint( operation = "历史回执列表")
    @GetMapping("IList")
    @ResponseBody
    @RequiresPermissions("returnRecordHistory:IList")
    @Limit(key = "returnRecordHistoryIList", period = 1, count = 1, name = "历史回执列表", prefix = "limit")
    public FebsResponse returnRecordHistoryIList(QueryRequest request, ReturnRecordHistoryQuery returnRecordHistory) {
        return returnRecordHistory(request,returnRecordHistory,true);
    }

    private FebsResponse returnRecordHistory(QueryRequest request, ReturnRecordHistoryQuery returnRecordHistory,boolean incompl) {
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode()) || user.getUserAccount() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己的发件箱
        returnRecordHistory.setOrgCode(user.getOrganizationCode());
        //sendBox.setCreateUsername(user.getUserAccount());
        returnRecordHistory.setAgentId(user.getOrganization().getAgentId());

        IPage<ReturnRecordHistory> datas = this.returnRecordHistoryService.findReturnRecordHistorys(request, returnRecordHistory);
        List<ReturnRecordHistory> list = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        //转成map  过滤掉敏感字段
        list.stream().forEach(item -> {
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            if(incompl){
                map.put("smsNumber",item.getSmsNumber().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));//手机号码
            }else {
                map.put("smsNumber",item.getSmsNumber());//手机号码
            }
            map.put("smsType",item.getSmsType());//消息类型
            map.put("sendCode",item.getSendCode());//批次号
            map.put("returnState",item.getReturnState());//接收状态
            map.put("returnTime", DateUtil.getDistanceTimes(item.getReqCreateTime(),item.getCreateTime()));
            map.put("smsWords",item.getSmsCount());//短信字数
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("smsNumberArea",item.getSmsNumberArea());//手机号码归属地
            map.put("smsCount",item.getSmsCount());//有效的短信数
            map.put("reqState",item.getReqState());//请求状态
            map.put("reqCreateTime",item.getReqCreateTime());//请求时间
            map.put("createTime",item.getCreateTime());

            String reqStateName = SendRecordStateEnums.getNameByCode(item.getReqState());
            map.put("reqStateName",StringUtils.isNotBlank(reqStateName)?reqStateName:"未知");
            String returnState = CommonStateEnums.getNameByCode(item.getReturnState());
            map.put("returnStateName",StringUtils.isNotBlank(returnState)?returnState:"未知");
            String smsTypeName = SmsTypeEnums.getNameByCode(item.getSmsType());
            map.put("smsType",StringUtils.isNotBlank(smsTypeName)?smsTypeName:"");
            dataList.add(map);
        });

        Map<String, Object> dataTable = getDataTableTransformMap(datas,dataList);
        return new FebsResponse().success().data(dataTable);
    }


    @ControllerEndpoint(operation = "导出ReturnRecordHistory", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("returnRecordHistory:export")
    @Limit(key = "returnRecordHistoryExport", period = 5, count = 1, name = "导出ReturnRecordHistory", prefix = "limit")
    public void export(QueryRequest queryRequest, ReturnRecordHistoryQuery returnRecordHistory, HttpServletResponse response) {
        OrganizationUserExt user = super.getCurrentUser();
        //限制只能查询自己的发件箱
        returnRecordHistory.setOrgCode(user.getOrganizationCode());
        //sendBox.setCreateUsername(user.getUserAccount());
        returnRecordHistory.setAgentId(user.getOrganization().getAgentId());


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
        Integer count = this.returnRecordHistoryService.countByQueryEntity(returnRecordHistory);
        if (count > exportLimit){
            response.setStatus(516);
            response.setHeader("exportLimit",String.valueOf(exportLimit));
            throw new FebsException(String.format("导出文件超过最大记录数【%d】限制，导出失败！",exportLimit));
        }

        //开始导出处理
        List<ReturnRecordHistory> returnRecords = this.returnRecordHistoryService.findReturnRecordHistorys(returnRecordHistory);
        List<ReturnRecordExcel> lst = new ArrayList<>();

        returnRecords.stream().forEach(item -> {
            ReturnRecordExcel excel = new ReturnRecordExcel();
            BeanUtils.copyProperties(item,excel);

            String reqStateName = SendRecordStateEnums.getNameByCode(item.getReqState());
            reqStateName = StringUtils.isNotBlank(reqStateName)?reqStateName:"未知";
            String reqTime = "未知";
            if (item.getReqCreateTime() != null){
                reqTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getReqCreateTime());
            }
            excel.setReqInfo(String.format("状态:%s 提交时间:%s",reqStateName,reqTime));

            excel.setSmsInfo(String.format("字数:%s 短信数:%s",item.getSmsWords(),item.getSmsCount()));
            lst.add(excel);
        });

        ExcelKit.$Export(ReturnRecordExcel.class, response).downXlsx(lst, false);
    }
}
