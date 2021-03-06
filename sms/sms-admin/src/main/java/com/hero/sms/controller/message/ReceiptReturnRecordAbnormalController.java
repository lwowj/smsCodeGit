package com.hero.sms.controller.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormal;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormalExt;
import com.hero.sms.entity.message.ReceiptReturnRecordAbnormalQuery;
import com.hero.sms.entity.message.ReturnRecord;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.common.CommonStateEnums;
import com.hero.sms.enums.common.ProcessingStateEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.message.IReceiptReturnRecordAbnormalService;
import com.hero.sms.service.message.IReturnRecordService;
import com.hero.sms.service.message.ISendRecordService;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

/**
 * ??????????????????????????? Controller
 *
 * @author Administrator
 * @date 2020-11-21 17:50:39
 */
@Slf4j
@Validated
@Controller
@RequestMapping("receiptReturnRecordAbnormal")
public class ReceiptReturnRecordAbnormalController extends BaseController {

    @Autowired
    private IReceiptReturnRecordAbnormalService receiptReturnRecordAbnormalService;
    
    @Autowired
    @Lazy
    private ISendRecordService sendRecordService;
    
    @Autowired
    private IReturnRecordService returnRecordService;

    @GetMapping(FebsConstant.VIEW_PREFIX + "receiptReturnRecordAbnormal")
    public String receiptReturnRecordAbnormalIndex(){
        return FebsUtil.view("receiptReturnRecordAbnormal/receiptReturnRecordAbnormal");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("receiptReturnRecordAbnormal:list")
    public FebsResponse getAllReceiptReturnRecordAbnormals(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
        return new FebsResponse().success().data(receiptReturnRecordAbnormalService.findReceiptReturnRecordAbnormals(receiptReturnRecordAbnormal));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("receiptReturnRecordAbnormal:list")
    public FebsResponse receiptReturnRecordAbnormalList(QueryRequest request, ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
    	 IPage<ReceiptReturnRecordAbnormal> datas = this.receiptReturnRecordAbnormalService.findReceiptReturnRecordAbnormals(request, receiptReturnRecordAbnormal);
    	 List<ReceiptReturnRecordAbnormalExt> list = new ArrayList<>();
         datas.getRecords().stream().forEach(item -> {
        	 ReceiptReturnRecordAbnormalExt receiptReturnRecordAbnormalExt = new ReceiptReturnRecordAbnormalExt();
             BeanUtils.copyProperties(item,receiptReturnRecordAbnormalExt);

             //????????????
             String returnStateName = CommonStateEnums.getNameByCode(item.getReturnState());
             returnStateName = StringUtils.isNotBlank(returnStateName)?returnStateName:"??????";
             receiptReturnRecordAbnormalExt.setReturnInfo(returnStateName);
             
             //????????????
             String processingStateInfo = item.getProcessingState()==0?"?????????":"?????????";
             processingStateInfo = StringUtils.isNotBlank(processingStateInfo)?processingStateInfo:"??????";
             receiptReturnRecordAbnormalExt.setProcessingStateInfo(processingStateInfo);
             
             receiptReturnRecordAbnormalExt.setSmsChannelName(DatabaseCache.getSmsChannelNameById(item.getChannelId()));
             list.add(receiptReturnRecordAbnormalExt);
         });
        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "??????ReceiptReturnRecordAbnormal", exceptionMessage = "??????ReceiptReturnRecordAbnormal??????")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("receiptReturnRecordAbnormal:add")
    public FebsResponse addReceiptReturnRecordAbnormal(@Valid ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
        this.receiptReturnRecordAbnormalService.createReceiptReturnRecordAbnormal(receiptReturnRecordAbnormal);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "??????ReceiptReturnRecordAbnormal", exceptionMessage = "??????ReceiptReturnRecordAbnormal??????")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("receiptReturnRecordAbnormal:delete")
    public FebsResponse deleteReceiptReturnRecordAbnormal(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
        this.receiptReturnRecordAbnormalService.deleteReceiptReturnRecordAbnormal(receiptReturnRecordAbnormal);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "????????????ReceiptReturnRecordAbnormal", exceptionMessage = "????????????ReceiptReturnRecordAbnormal??????")
    @GetMapping("delete/{receiptReturnRecordAbnormalIds}")
    @ResponseBody
    @RequiresPermissions("receiptReturnRecordAbnormal:delete")
    public FebsResponse deleteReceiptReturnRecordAbnormal(@NotBlank(message = "{required}") @PathVariable String receiptReturnRecordAbnormalIds) {
        String[] ids = receiptReturnRecordAbnormalIds.split(StringPool.COMMA);
        this.receiptReturnRecordAbnormalService.deleteReceiptReturnRecordAbnormals(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "??????ReceiptReturnRecordAbnormal", exceptionMessage = "??????ReceiptReturnRecordAbnormal??????")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("receiptReturnRecordAbnormal:update")
    public FebsResponse updateReceiptReturnRecordAbnormal(ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal) {
        this.receiptReturnRecordAbnormalService.updateReceiptReturnRecordAbnormal(receiptReturnRecordAbnormal);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "??????ReceiptReturnRecordAbnormal", exceptionMessage = "??????Excel??????")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("receiptReturnRecordAbnormal:export")
    public void export(QueryRequest queryRequest, ReceiptReturnRecordAbnormalQuery receiptReturnRecordAbnormal, HttpServletResponse response) {
        List<ReceiptReturnRecordAbnormal> receiptReturnRecordAbnormals = this.receiptReturnRecordAbnormalService.findReceiptReturnRecordAbnormals(queryRequest, receiptReturnRecordAbnormal).getRecords();
        ExcelKit.$Export(ReceiptReturnRecordAbnormal.class, response).downXlsx(receiptReturnRecordAbnormals, false);
    }
    
    /**
     * @begin 2020-11-23
     * ???????????????????????????????????????????????????????????????????????????
     * @param receiptAbnormalIds  ????????????ID??????????????????????????????json?????????????????????????????????ID
     * @return
     */
    @ControllerEndpoint(operation = "?????????????????????????????????????????????", exceptionMessage = "????????????????????????")
    @GetMapping("toReceive/{receiptAbnormalIds}")
    @ResponseBody
    @RequiresPermissions("receiptReturnRecordAbnormal:toReceive")
    public FebsResponse toReceive(@PathVariable("receiptAbnormalIds") String receiptAbnormalIds) 
    {
        if (StringUtils.isBlank(receiptAbnormalIds))
        {
            return new FebsResponse().fail().message("?????????????????????????????????");
        }

    	List<Long> ids = Arrays.stream(receiptAbnormalIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
    	ReceiptReturnRecordAbnormalQuery  query = new ReceiptReturnRecordAbnormalQuery();
        query.setIds(ids);
        query.setProcessingState(ProcessingStateEnums.UNFINISHED.getCode().intValue());
    	List<ReceiptReturnRecordAbnormal> receiptReturnRecordAbnormalList = receiptReturnRecordAbnormalService.findReceiptReturnRecordAbnormals(query);
    	if(receiptReturnRecordAbnormalList!=null&&receiptReturnRecordAbnormalList.size()>0)
    	{
    		List<ReceiptReturnRecordAbnormal> updateAbnormalList = new ArrayList<>();
    		for (int i = 0; i < receiptReturnRecordAbnormalList.size(); i++) 
        	{
    			ReceiptReturnRecordAbnormal thisAbnormal = receiptReturnRecordAbnormalList.get(i);
    			ReceiptReturnRecordAbnormal updateAbnormal = new ReceiptReturnRecordAbnormal();
        		updateAbnormal.setId(thisAbnormal.getId());
        		
        		SendRecord sendRecord = sendRecordService.getByAreaAndNumberAndMsgId(thisAbnormal.getSmsNumberArea(),thisAbnormal.getSmsNumber(),thisAbnormal.getResMsgid());
        		if(sendRecord!=null)
        		{
        			if(sendRecord.getState()==SendRecordStateEnums.ReqSuccess.getCode())
        			{
        				String data  = thisAbnormal.getReturnDataparam();
        				returnRecordService.receiptReturnRecord(JSON.parseObject(data, ReturnRecord.class),"RESMSGID="+thisAbnormal.getResMsgid());
        			}
        			updateAbnormal.setProcessingState(ProcessingStateEnums.FINISHED.getCode().intValue());
        		}
        		else
        		{
        			updateAbnormal.setProcessingState(ProcessingStateEnums.INVALID.getCode().intValue());
        		}
        		updateAbnormal.setAgainTime(new Date());
        		updateAbnormalList.add(updateAbnormal);
			}
    		receiptReturnRecordAbnormalService.updateBatchById(updateAbnormalList);
    	}
    	else
    	{
    		 return new FebsResponse().fail().message("?????????????????????????????????");
    	}
        return new FebsResponse().success();
    }
    /**
     * @end
     */
}
