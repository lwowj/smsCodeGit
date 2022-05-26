package com.hero.sms.controller.message;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.commands.utils.ChainUtil;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.annotation.Limit;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.listener.SendboxExcelListener;
import com.hero.sms.common.utils.DateUtils;
import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.common.utils.sensi.SensitiveFilter;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxExcelModel;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SendRecordQuery;
import com.hero.sms.entity.message.SensitiveWord;
import com.hero.sms.entity.message.exportModel.SendBoxExcel;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SendBoxSubTypeEnums;
import com.hero.sms.enums.message.SendBoxTypeEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.service.organization.IOrganizationCostService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserService;
import com.hero.sms.utils.AjaxTokenProcessor;
import com.hero.sms.utils.FileDownLoadUtil;
import com.hero.sms.utils.RandomUtil;
import com.hero.sms.utils.StringUtil;
import com.wuwenze.poi.ExcelKit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("sendBox")
public class SendBoxController extends BaseController {

    @Autowired
    protected ISendBoxService sendBoxService;

    @Autowired
    protected IOrganizationService organizationService;

    @Autowired
    private IOrganizationUserService organizationUserService;
    
    @Autowired
    protected ISendRecordService sendRecordService;
    
    @Autowired
    private IOrganizationCostService organizationCostService;

    /**sun.rmi.runtime.Log
     * 列表页数据查询
     * @param request
     * @param sendBox
     * @return
     */
    @ControllerEndpoint( operation = "发件箱列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sendBox:list")
    @Limit(key = "sendBoxList", period = 1, count = 1, name = "发件箱列表", prefix = "limit")
    public FebsResponse sendBoxList(QueryRequest request, SendBoxQuery sendBox) {
        return sendBox(request,sendBox);
    }

    @ControllerEndpoint( operation = "发件箱列表")
    @GetMapping("IList")
    @ResponseBody
    @RequiresPermissions("sendBox:IList")
    @Limit(key = "sendBoxIList", period = 1, count = 1, name = "发件箱列表", prefix = "limit")
    public FebsResponse sendBoxIList(QueryRequest request, SendBoxQuery sendBox) {
        return sendBox(request,sendBox);
    }

    private FebsResponse sendBox(QueryRequest request, SendBoxQuery sendBox) {
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode()) || user.getUserAccount() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }
        //限制只能查询自己的发件箱
        sendBox.setOrgCode(user.getOrganizationCode());
        //sendBox.setCreateUsername(user.getUserAccount());
        sendBox.setAgentId(user.getOrganization().getAgentId());
        IPage<SendBox> datas = this.sendBoxService.findSendBoxs(request, sendBox);
        List<SendBox> sendBoxList = datas.getRecords();
        List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
        sendBoxList.stream().forEach(item -> { // 转成map  过滤掉敏感字段
            Map<String,Object> map = new HashMap<>();
            map.put("id",item.getId());
            map.put("sendCode",item.getSendCode());//批次号
            map.put("smsType",item.getSmsType());//消息类型
            map.put("auditState",item.getAuditState());//审核状态
            map.put("smsContent",item.getSmsContent());//消息内容
            map.put("type",item.getType());
            //map.put("smsNumbers",item.getSmsNumbers());//手机号码
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
    @RequiresPermissions("sendBox:list")
    public FebsResponse getSmsNumbers(@PathVariable("sendBoxId") Long sendBoxId){
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode())){
            return new FebsResponse().fail().data("账号异常，请求失败！");
        }
        SendBox sendBox= this.sendBoxService.getById(sendBoxId);
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

    @ControllerEndpoint(operation = "导出SendBox", exceptionMessage = "导出Excel失败")
    @GetMapping("excel")
    @ResponseBody
    @RequiresPermissions("sendBox:export")
    @Limit(key = "sendBoxExport", period = 5, count = 1, name = "导出SendBox", prefix = "limit")
    public void export(QueryRequest queryRequest, SendBoxQuery sendBox, HttpServletResponse response) {
        OrganizationUserExt user = super.getCurrentUser();
        //限制只能查询自己的发件箱
        sendBox.setOrgCode(user.getOrganizationCode());
        //sendBox.setCreateUsername(user.getUserAccount());
        sendBox.setAgentId(user.getOrganization().getAgentId());


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

        int count = this.sendBoxService.countByEntity(sendBox);
        if (count > exportLimit){
            response.setStatus(516);
            response.setHeader("exportLimit",String.valueOf(exportLimit));
            throw new FebsException(String.format("导出文件超过最大记录数【%d】限制，导出失败！",exportLimit));
        }

        //开始导出处理
        List<SendBox> sendBoxs = this.sendBoxService.findSendBoxs(sendBox);

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


    /**
     * 下载号码文件
     * @param response
     * @param id
     * @return
     * @throws FileNotFoundException
     */
    @ControllerEndpoint(operation = "下载手机号码文件", exceptionMessage = "下载手机号码文件失败")
    @GetMapping("download/{id}")
    @ResponseBody
    @RequiresPermissions("sendBox:list")
    @Limit(key = "sendBoxDownload", period = 5, count = 1, name = "下载手机号码文件", prefix = "limit")
    public void downLoadSmsNumbersExcel(HttpServletResponse response,@PathVariable Integer id) throws FileNotFoundException {
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getOrganizationCode()) || user.getUserAccount() == null) {
            log.error("异常请求，手机号码文件下载失败");
            return;
        }
        SendBox sendBox = this.sendBoxService.getById(id);

        if (!sendBox.getOrgCode().equals(user.getOrganizationCode())){
            log.error(String.format("用户[%s]非法请求，手机号码文件下载失败",user.getUserAccount()));
            return;
        }
        if (!(sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode())){
            log.error("发件箱类型非excel模板提交，下载失败！");
            return;
        }
        String smsNumbers = sendBox.getSmsNumbers();
        String prefixPath = DatabaseCache.getCodeBySortCodeAndCode("System", "sendBoxExcelPath").getName();
        if (!smsNumbers.contains(prefixPath)){
            log.error("文件不存在，下载失败!");
            return;
        }
        
        /**
         * @begin
         * 2021-04-21
        * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
        * 【说明】：这里需要新增校验smsNumbers 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
        * 【步骤】：
        * 1、使用文件工具判断文件地址的文件是否存在于本机上
        * 2、获取字典中负载服务器的IP地址
        * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
        */
        FileDownLoadUtil.downLoadSendBoxExcelFile(smsNumbers);
        /**
         * @end
         */
        
        download(response,smsNumbers);

    }

    @ControllerEndpoint(operation = "取消定时发件箱", exceptionMessage = "取消定时失败")
    @PostMapping("cancelTimings")
    @ResponseBody
    @RequiresPermissions("sendBox:cancelTimings")
    public FebsResponse cancelTimings(@RequestParam(value = "sendBoxIds",required = true) String sendBoxIds,
                                      @RequestParam(value = "isSendingNow",required = true) boolean isSendingNow){
        if (StringUtils.isBlank(sendBoxIds)){
            return new FebsResponse().fail().message("参数异常，此次操作不成功！");
        }
        String[] ids = sendBoxIds.split(",");
        if (ids == null || ids.length < 0){
            return new FebsResponse().message("参数异常，此次操作不成功！").fail();
        }
        /**
         * @begin
         * 
         */
        SendBoxQuery querySendBox = new SendBoxQuery();
        //分拣时间为空
        querySendBox.setIsSortingFlag(false);
        //定时时间不为空
        querySendBox.setIsTiming(true);
        //审核状态通过
        querySendBox.setAuditState(AuditStateEnums.Pass.getCode().intValue());
        //有效短信数为0
        querySendBox.setSmsCount(0);
        List<Long> idss = Arrays.stream(sendBoxIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        querySendBox.setIds(idss);
        //查询以上条件的发件箱记录（已审核通过、未分拣的记录、是定时记录）
    	List<SendBox> sendBoxList = this.sendBoxService.findSendBoxs(querySendBox);
    	if(sendBoxList!=null&&sendBoxList.size()>0)
		{
    		//待分拣记录
			List<SendBox> sortingSendBoxList = new ArrayList<SendBox>();
			//待分拣记录ID
			List<Long> sortingSendBoxIdList = new ArrayList<Long>();
			//取消分拣记录ID（已分拣无需再分拣）
			List<Long> cancelSortingSendBoxIdList = new ArrayList<Long>();
    		for (int i = 0; i < sendBoxList.size(); i++) 
        	{
        		SendBox thisSendBox = sendBoxList.get(i);
        		//以批次号与商户号为条件进行查询
        		SendRecordQuery sendRecord = new SendRecordQuery();
        		sendRecord.setSendCode(thisSendBox.getSendCode());
        		sendRecord.setOrgCode(thisSendBox.getOrgCode());
        		List<SendRecord> sendRecordList =  this.sendRecordService.findSendRecords(sendRecord);
        		//判断发送记录表中没有分拣后的数据记录
        		if(sendRecordList!=null&&sendRecordList.size()>0)
        		{
        			cancelSortingSendBoxIdList.add(thisSendBox.getId());
        			continue;
        		}
        		sortingSendBoxIdList.add(thisSendBox.getId());
        		sortingSendBoxList.add(thisSendBox);
			}
    		
    		if (isSendingNow)
    		{
    			//将定时时间清掉
		        this.sendBoxService.cancelTimings(ids,true);
    			//将没有分拣过的记录。推送分拣
    			if(sortingSendBoxList!=null&&sortingSendBoxList.size()>0)
        		{
        			sendBoxService.splitRecord(sortingSendBoxList);
        		}
    		}
    		else
    		{
    			//存在取消分拣的记录（因某原因，已经分拣，无需再次分拣，仅清除定时时间）
    			if(cancelSortingSendBoxIdList!=null&&cancelSortingSendBoxIdList.size()>0)
        		{
    				String[] cancelIds = new String[cancelSortingSendBoxIdList.size()];
    				for (int i = 0; i < cancelSortingSendBoxIdList.size(); i++) 
    				{
    					cancelIds[i] = String.valueOf(cancelSortingSendBoxIdList.get(i));
					}
    				//将定时时间清掉
    		        this.sendBoxService.cancelTimings(cancelIds,true);
        		}
    			if(sortingSendBoxIdList!=null&&sortingSendBoxIdList.size()>0)
        		{
    				String[] sortingIds = new String[sortingSendBoxIdList.size()];
    				for (int i = 0; i < sortingSendBoxIdList.size(); i++) 
    				{
    					sortingIds[i] = String.valueOf(sortingSendBoxIdList.get(i));
					}
    				//将定时时间清掉
    		        this.sendBoxService.cancelTimings(sortingIds,false);
        		}
    		}
		}
    	else
    	{
    		//可取消状态不为<审核通过>的定时，但不允许发送
    		if (!isSendingNow)
    		{
    			//将定时时间清掉
		        this.sendBoxService.cancelTimings(ids,false);
    		}
    	}
        /**
         * @end
         */
//        //将定时时间清掉
//        this.sendBoxService.cancelTimings(ids,isSendingNow);
//        if (isSendingNow){
//            LambdaQueryWrapper<SendBox> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(SendBox::getAuditState,AuditStateEnums.Pass.getCode().intValue())
//                        .in(SendBox::getId,ids);
//            List<SendBox> sendBoxes = this.sendBoxService.list(queryWrapper);
//            /*if (sendBoxes != null && sendBoxes.size()>0){
//                sendBoxes.stream().forEach(sendBox -> {
//                    try {//推送到MQ
//                        mqService.sendBox(sendBox.getId());
//                    } catch (Exception e) {
//                        log.error(String.format("取消定时立即发送发件箱【%s】推送MQ失败",sendBox.getSendCode()),e);
//                    }
//                });
//            }*/
//            sendBoxService.splitRecord(sendBoxes);
//        }
        return new FebsResponse().success();
    }

    @Limit(key = "sensitiveFilter", period = 60, count = 20,name = "敏感词查询接口", prefix = "limit")
    @ControllerEndpoint(operation = "敏感词查询", exceptionMessage = "敏感词查询失败")
    @PostMapping("sensitiveFilter")
    @ResponseBody
    @RequiresPermissions("sendBox:sensitiveFilter")
    public FebsResponse sensitiveFilter(HttpServletRequest request,@RequestParam(value = "smsContent",required = true) String smsContent){
        SensitiveFilter sensitiveFilter = SensitiveFilter.DEFAULT;
        boolean isContainSens = sensitiveFilter.isContain(smsContent);
        if(isContainSens) {
            return new FebsResponse().success().data(false)
                    .message(String.format("短信内容包含敏感词==>【%s】",sensitiveFilter.filter(smsContent, '*')));
        }
        return new FebsResponse().success().data(true);
    }

    @Limit(key = "replaceSensitiveWord", period = 60, count = 20,name = "敏感词替换接口", prefix = "limit")
    @ControllerEndpoint(operation = "敏感词替换", exceptionMessage = "敏感词替换失败")
    @PostMapping("replaceSensitiveWord")
    @ResponseBody
    @RequiresPermissions("sendBox:replaceSensitiveWord")
    public FebsResponse replaceSensitiveWord(HttpServletRequest request,@RequestParam(value = "smsContent",required = true) String smsContent){
        SensitiveFilter sensitiveFilter = SensitiveFilter.DEFAULT;
        boolean isContainSens = sensitiveFilter.isContain(smsContent);
        if(isContainSens) 
        {
        	String newSmsContent = replaceWordUtil(smsContent); 
        	return new FebsResponse().success().data(false).message(newSmsContent);
        }
        return new FebsResponse().success().data(true);
    }
    
    /**
     * 2021-03-05
     * 敏感词替换工具
     * @param smsContent
     * @return
     */
    public String replaceWordUtil(String smsContent)
    {
    	if(StringUtil.isNotBlank(smsContent))
    	{
    		List<SensitiveWord> sensitiveWordList = DatabaseCache.getSensitiveWordList();
    		if(sensitiveWordList!=null&&sensitiveWordList.size()>0)
    		{
    			for (int i = 0; i < sensitiveWordList.size(); i++)
    			{
    				SensitiveWord sensitiveWord = sensitiveWordList.get(i);
    				String word = sensitiveWord.getWord();
    				String newWord = sensitiveWord.getNewWord();
    				if(StringUtil.isNotBlank(word)&&StringUtil.isNotBlank(newWord))
    				{
    					String[] wordArr = word.split(StringPool.COMMA);
    					for (int j = 0; j < wordArr.length; j++) 
    					{
    						if(smsContent.contains(wordArr[j]))
    						{
    							smsContent = smsContent.replaceAll(wordArr[j], newWord);
    						}
						}
    				}
				}
    		}
    	}
    	return smsContent;
    }
    
    /**
     * Excel提交表单
     *
     * @param sendBox
     * @return
     */
    @ControllerEndpoint(operation = "新增SendBox", exceptionMessage = "新增SendBox失败")
    @PostMapping("submitWithExcel")
    @ResponseBody
    @RequiresPermissions("sendBox:add")
    @Limit(key = "addSendBoxWithExcel", period = 2, count = 1, name = "新增SendBoxExcel提交表单", prefix = "limit")
    public FebsResponse addSendBoxWithExcel(HttpServletRequest request,@Valid SendBoxQuery sendBox) {
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || user.getOrganizationCode() == null || user.getUserAccount() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }

        if (sendBox.getMoblieFile() == null || sendBox.getMoblieFile().isEmpty()) {
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("导入数据为空");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
            return febsResponse.fail();
        }

        String filename = sendBox.getMoblieFile().getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".xlsx") && !StringUtils.endsWith(filename, ".xls")) {
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("文件类型不支持");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
            return febsResponse.fail();
        }
        
        /**
         * @begin 2020-12-12
                * 新增校验token，防止重复提交
         */
        if(!checkToken(request, AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY, sendBox.getSessionToken()))
        {
        	return new FebsResponse().message("当前请求无效,请关闭当前菜单后重新打开！").fail();
        }
        /**
         * @end
         */
	    
        /**
         * @begin 2020-10-09
         * 新增支持国际号码
         */
        if(StringUtils.isBlank(sendBox.getSmsNumberArea())) sendBox.setSmsNumberArea(SmsNumberAreaCodeEnums.China.getInArea());
        boolean checkflag = checkHaveAreaCode(user.getOrganizationCode(), sendBox.getSmsNumberArea());
        if(!checkflag)
        {
        	 return new FebsResponse().message("所选号码区域不正确").fail();
        }
        /**
         * @end
         */
        /**
         * @begin 2020-09-01
         * 短信内容逻辑判断及去除前后空格
         * 
         */
        String smsContent = sendBox.getSmsContent();
    	if(smsContent!=null&&!"".equals(smsContent))
    	{
    		smsContent = smsContent.trim();
    		if(smsContent!=null&&!"".equals(smsContent))
    		{
    			sendBox.setSmsContent(smsContent);
    		}
    		else
    		{
    			return new FebsResponse().message("短信内容不能为空").fail();
    		}
    	}
    	else
		{
			return new FebsResponse().message("短信内容不能为空").fail();
		}
    	/**
    	 * @end
    	 */
        int count = 0;
        List<Object> excelModels = null;
        try {
            SendboxExcelListener listener = new SendboxExcelListener(sendBox.getSmsContent());
        	excelModels = EasyExcel.read(sendBox.getMoblieFile().getInputStream(),listener).head(SendBoxExcelModel.class).sheet().doReadSync();
        	count = excelModels.size();
        	if (count < 1){
        		FebsResponse febsResponse = new FebsResponse();
    	        febsResponse.message("没有检索到手机号码，此次提交无效");
    	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
    	        febsResponse.put("sessionToken", sessionToken);
                return febsResponse.fail();
            }
            if (count > 50000){
            	FebsResponse febsResponse = new FebsResponse();
    	        febsResponse.message("单次提交不能超过5万个号码");
    	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
    	        febsResponse.put("sessionToken", sessionToken);
                return febsResponse.fail();
            }
            //排查是否存在号码异常的情况
            List<String> errNumbers = listener.getErrorPhoneNum();
            if (errNumbers.size() > 0){
                String errMsg = String.format("本次提交号码存在%d个错误号码[%s]，号码长度不能超过%d,请修改后重试"
                        ,errNumbers.size()
                        ,StringUtils.join(errNumbers,",")
                        ,listener.getNumberLimit());
                FebsResponse febsResponse = new FebsResponse();
    	        febsResponse.message(errMsg);
    	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
    	        febsResponse.put("sessionToken", sessionToken);
                return febsResponse.fail();
            }

            //排查参数是否缺失
            List<Integer> errorParmRow = listener.getErrorParmRow();
            if (errorParmRow.size() > 0){
                String errMsg = String.format("参数列[%s]不能为空,本次提交共存在%d行参数缺失,请修改后重试"
                        ,StringUtils.join(listener.getParamColumn(),",")
                        ,errorParmRow.size()
                        );
                FebsResponse febsResponse = new FebsResponse();
    	        febsResponse.message(errMsg);
    	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
    	        febsResponse.put("sessionToken", sessionToken);
                return febsResponse.fail();
            }
        } catch (Exception e) {
            log.error("表格提交读取文件失败",e);
            return new FebsResponse().message("文件类型不支持！").fail();
        }
        //商户资费及余额预检查
        boolean result = false;
        try {
       	 /**
             * @begin 2020-10-10
             * @modify
             * 新增支持国际号码
             */
//             result = organizationService.checkOrgAmountPrePay(user.getOrganizationCode()
//            , SmsTypeEnums.TextMsg.getCode(), SmsNumberAreaCodeEnums.China.getInArea(), count);
        	String smsNumberArea = sendBox.getSmsNumberArea();
        	 result = organizationService.checkOrgAmountPrePay(user.getOrganizationCode(), SmsTypeEnums.TextMsg.getCode(), smsNumberArea, count);
            /**
             * @end
             */
            if (!result){
                return new FebsResponse().fail().message("商户余额不足");
            }
        } catch (ServiceException e) {
            return new FebsResponse().fail().message(e.getMessage());
        }
        //商户等相关信息初始化
        sendBox.setClientIp(IPUtil.getIpAddr(request));
        sendBox.setSubType(SendBoxSubTypeEnums.OrgWebSub.getCode());
        sendBox.setOrgCode(user.getOrganizationCode());
        sendBox.setCreateUsername(user.getUserAccount());
        sendBox.setAgentId(user.getOrganization().getAgentId());
        sendBox.setSmsType(SmsTypeEnums.TextMsg.getCode());
        sendBox.setType(SendBoxTypeEnums.excleSubmit.getCode());
        sendBox.setNumberCount(count);

        //将文件保存  并把文件路径保存到记录中
        String savePath = DatabaseCache.getCodeBySortCodeAndCode("System", "sendBoxExcelPath").getName();
        savePath = savePath + rename(filename);
        sendBox.setSmsNumbers(savePath);
        File desFile = new File(savePath);
        if (!desFile.getParentFile().exists())
            desFile.mkdirs();
        try {
            sendBox.getMoblieFile().transferTo(desFile);
        } catch (IOException e) {
            return new FebsResponse().message("文件上传失败").fail();
        }

        try {
			this.sendBoxService.createSendBox(sendBox);
		} catch (ServiceException e) {
			FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message(e.getMessage());
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
            return febsResponse.fail();
		}catch (Exception e1) {
			log.error("保存发件箱失败",e1);
			FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("提交失败");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
		}

        if(sendBox.getAuditState().intValue() == AuditStateEnums.Pass.getCode().intValue()) {
        	/*try {//推送到MQ
        		mqService.sendBox(sendBox.getId());
        	} catch (Exception e) {
        		log.error(String.format("excel提交发件箱【%s】推送MQ失败",sendBox.getSendCode()),e);
        	}*/
        	if(sendBox.getTimingTime() == null) 
        	{
        		/**
        		 * @begin 2020-12-06
        		 *  校验商户谷歌口令开关开启;
        		 *  开启则判断商户是否绑定谷歌：绑定则继续推送，未绑定则不推送
        		 *  未开启则保持原本流程推送
        		 * 
        		 */
        		boolean ispassflag = true;
        		String checkGooleKeySwitch = "OFF";
        		Code checkGooleKeySwitchSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkGooleKeySwitch");
        	    if(checkGooleKeySwitchSwitchCode!=null&&!"".equals(checkGooleKeySwitchSwitchCode.getName()))
        	    {
        	    	checkGooleKeySwitch = checkGooleKeySwitchSwitchCode.getName();
        	    }
        	    if("ON".equals(checkGooleKeySwitch))
        	    {
        	    	 OrganizationUser organizationUser = organizationUserService.findByUserAccount(user.getUserAccount());
        	    	 //判断谷歌口令是否绑定，未绑定则返回false
                     if(StringUtils.isBlank(organizationUser.getGoogleKey())) 
                     {
                    	 ispassflag = false;
                     }
        	    }
        	    //判断未绑定谷歌口令的商户，默认转换为“待审核”状态
    			if(ispassflag)
    			{
    				sendBoxService.splitRecordForExcel(sendBox, excelModels);
    			}
                /**
                 * @end
                 */
        	}
        }
        FebsResponse febsResponse = new FebsResponse();
        febsResponse.message(String.format("提交成功,此次提交共检索到%s个手机号码",count));
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY);
        febsResponse.put("sessionToken", sessionToken);
        return febsResponse.success();
    }

    /**
     * txt-提交表单
     *
     * @param sendBox
     * @return
     */
    @ControllerEndpoint(operation = "新增SendBox", exceptionMessage = "新增SendBox失败")
    @PostMapping("submitWithTxt")
    @ResponseBody
    @RequiresPermissions("sendBox:add")
    @Limit(key = "addSendBoxWithTxt", period = 2, count = 1, name = "新增SendBoxTxt提交表单", prefix = "limit")
    public FebsResponse addSendBoxWithTxt(HttpServletRequest request,@Valid SendBoxQuery sendBox) {
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || user.getOrganizationCode() == null || user.getUserAccount() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }

        if ((sendBox.getMoblieFile() == null || sendBox.getMoblieFile().isEmpty()) && StringUtils.isBlank(sendBox.getSmsNumbers()) ) {
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("导入数据为空");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
        }
        
        /**
         * @begin 2020-12-12
                * 新增校验token，防止重复提交
         */
        if(!checkToken(request, AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY, sendBox.getSessionToken()))
        {
        	return new FebsResponse().message("当前请求无效,请关闭当前菜单后重新打开！").fail();
        }
        /**
         * @end
         */
	    
        /**
         * @begin 2020-10-09
         * 新增支持国际号码
         */
        if(StringUtils.isBlank(sendBox.getSmsNumberArea())) sendBox.setSmsNumberArea(SmsNumberAreaCodeEnums.China.getInArea());
        boolean checkflag = checkHaveAreaCode(user.getOrganizationCode(), sendBox.getSmsNumberArea());
        if(!checkflag)
        {
        	 return new FebsResponse().message("所选号码区域不正确").fail();
        }
        /**
         * @end
         */
        /**
         * @begin 2020-09-01
         * @add
         * 短信内容逻辑判断及去除前后空格
         * 
         */
        String smsContent = sendBox.getSmsContent();
    	if(smsContent!=null&&!"".equals(smsContent))
    	{
    		smsContent = smsContent.trim();
    		if(smsContent!=null&&!"".equals(smsContent))
    		{
    			sendBox.setSmsContent(smsContent);
    		}
    		else
    		{
    			return new FebsResponse().message("短信内容不能为空").fail();
    		}
    	}
    	else
		{
			return new FebsResponse().message("短信内容不能为空").fail();
		}
    	/**
    	 * @end
    	 */
        //解析（合并） 手机号码集合字符串
        String textNumbers = "";
        if (sendBox.getMoblieFile() != null && !sendBox.getMoblieFile().isEmpty()){
            String filename = sendBox.getMoblieFile().getOriginalFilename();
            if (!StringUtils.endsWith(filename, ".txt")) {
            	FebsResponse febsResponse = new FebsResponse();
    	        febsResponse.message("文件类型不支持");
    	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
    	        febsResponse.put("sessionToken", sessionToken);
    	        return febsResponse.fail();
            }
            textNumbers = getContentFromInputStream(sendBox.getMoblieFile());
        }

        //商户等相关信息初始化
        sendBox.setClientIp(IPUtil.getIpAddr(request));
        sendBox.setSubType(SendBoxSubTypeEnums.OrgWebSub.getCode());
        sendBox.setOrgCode(user.getOrganizationCode());
        sendBox.setCreateUsername(user.getUserAccount());
        sendBox.setAgentId(user.getOrganization().getAgentId());
        sendBox.setSmsType(SmsTypeEnums.TextMsg.getCode());
        sendBox.setType(SendBoxTypeEnums.formSubmit.getCode());
        if (StringUtils.isNotBlank(sendBox.getSmsNumbers())) {
            String smsNumbers = sendBox.getSmsNumbers();
            String[] numbers = smsNumbers.split("\n");
            String newNumbers = StringUtils.join(numbers, ",");
            textNumbers = textNumbers + "," + newNumbers;
        }

        //替换中文符号"，"
        if (textNumbers.contains("，")){
            textNumbers = textNumbers.replaceAll("，",",");
        }
        //过滤掉其中可能存在‘,,’的情况
        List<String> smsNumberList = Arrays.asList(textNumbers.split(",")).stream()
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.toList());
        if (smsNumberList.size() < 1){
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("未检测到任何手机号码,此次提交无效");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
        }

        //排查是否存在号码异常的情况
        int numberLimitDefault = 20;

        Code code = DatabaseCache.getCodeBySortCodeAndCode("System","numberLengthLimit");
        if (code != null){
            try {
                numberLimitDefault = Integer.valueOf(code.getName());
            } catch (NumberFormatException e) {
                log.error("发送短信 -> 号码最大长度限制字典值未生效，使用默认值20",e.getMessage());
            }
        }
        final int numberLimit = numberLimitDefault;
        List<String> errNumbers = Arrays.asList(textNumbers.split(",")).stream()
                .filter(s -> s.length() > numberLimit)
                .collect(Collectors.toList());
        if (errNumbers.size() > 0){
            String errMsg = String.format("本次提交号码存在%d个错误号码[%s]，号码长度不能超过%d,请修改后重试"
                    ,errNumbers.size()
                    ,StringUtils.join(errNumbers,",")
                    ,numberLimit);
            FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message(errMsg);
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
        }


        String smsNumbers = Joiner.on(",").join(smsNumberList);

        if (smsNumberList.size() > 50000){
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("单次提交不能超过5万个号码");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
        }
        sendBox.setSmsNumbers(smsNumbers);
        sendBox.setNumberCount(smsNumberList.size());

        //商户资费及余额预检查
        boolean result = false;
        try {
        	 /**
             * @begin 2020-10-10
             * @modify
             * 新增支持国际号码
             */
//            result = organizationService.checkOrgAmountPrePay(user.getOrganizationCode()
//            , SmsTypeEnums.TextMsg.getCode(), SmsNumberAreaCodeEnums.China.getInArea(), smsNumberList.size());
        	String smsNumberArea = sendBox.getSmsNumberArea();
        	result = organizationService.checkOrgAmountPrePay(user.getOrganizationCode(), SmsTypeEnums.TextMsg.getCode(), smsNumberArea, smsNumberList.size());
            /**
             * @end
             */
            if (!result){
                return new FebsResponse().fail().message("商户余额不足");
            }
        } catch (ServiceException e) {
            return new FebsResponse().fail().message(e.getMessage());
        }

        //保存
        try {
			this.sendBoxService.createSendBox(sendBox);
        } catch (ServiceException e) {
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message(e.getMessage());
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
		}catch (Exception e1) {
			log.error("保存发件箱失败",e1);
			FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("提交失败");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
		}

        if(sendBox.getAuditState().intValue() == AuditStateEnums.Pass.getCode().intValue()) {
        	/*try {//推送到MQ
        		mqService.sendBox(sendBox.getId());
        	} catch (Exception e) {
        		log.error(String.format("表单提交发件箱【%s】推送MQ失败",sendBox.getSendCode()),e);
        	}*/
        	if(sendBox.getTimingTime() == null) {
        		/**
        		 * @begin 2020-12-06
        		 *  校验商户谷歌口令开关开启;
        		 *  开启则判断商户是否绑定谷歌：绑定则继续推送，未绑定则不推送
        		 *  未开启则保持原本流程推送
        		 * 
        		 */
        		boolean ispassflag = true;
        		String checkGooleKeySwitch = "OFF";
        		Code checkGooleKeySwitchSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkGooleKeySwitch");
        	    if(checkGooleKeySwitchSwitchCode!=null&&!"".equals(checkGooleKeySwitchSwitchCode.getName()))
        	    {
        	    	checkGooleKeySwitch = checkGooleKeySwitchSwitchCode.getName();
        	    }
        	    if("ON".equals(checkGooleKeySwitch))
        	    {
        	    	 OrganizationUser organizationUser = organizationUserService.findByUserAccount(user.getUserAccount());
        	    	 //判断谷歌口令是否绑定，未绑定则返回false
                     if(StringUtils.isBlank(organizationUser.getGoogleKey())) 
                     {
                    	 ispassflag = false;
                     }
        	    }
        	    //判断未绑定谷歌口令的商户，默认转换为“待审核”状态
    			if(ispassflag)
    			{
    				sendBoxService.splitRecordForTxt(sendBox, smsNumberList);
    			}
                /**
                 * @end
                 */
        	}
        }
        FebsResponse febsResponse = new FebsResponse();
        febsResponse.message(String.format("提交成功%s个号码", smsNumberList.size()));
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXT_TRANSACTION_TOKEN_KEY);
        febsResponse.put("sessionToken", sessionToken);
        return febsResponse.success();
    }
    
    /**
     * txt文件-提交表单
     *
     * @param sendBox
     * @return
     */
    @ControllerEndpoint(operation = "新增SendBox", exceptionMessage = "新增SendBox失败")
    @PostMapping("submitWithTxtFile")
    @ResponseBody
    @RequiresPermissions("sendBox:add")
    @Limit(key = "addSendBoxWithTxtFile", period = 2, count = 1, name = "新增SendBoxTxt文件提交表单", prefix = "limit")
    public FebsResponse addSendBoxWithTxtFile(HttpServletRequest request,@Valid SendBoxQuery sendBox) {
        OrganizationUserExt user = super.getCurrentUser();
        if (user == null || user.getOrganizationCode() == null || user.getUserAccount() == null) {
            return new FebsResponse().message("账户异常,数据请求失败").fail();
        }

        if ((sendBox.getMoblieFile() == null || sendBox.getMoblieFile().isEmpty()) && StringUtils.isBlank(sendBox.getSmsNumbers()) ) {
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("导入数据为空");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
        }
        
        /**
         * @begin 2020-12-12
                * 新增校验token，防止重复提交
         */
        if(!checkToken(request, AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY, sendBox.getSessionToken()))
        {
        	return new FebsResponse().message("当前请求无效,请关闭当前菜单后重新打开！").fail();
        }
        /**
         * @end
         */
	    
        /**
         * @begin 2020-10-09
         * 新增支持国际号码
         */
        if(StringUtils.isBlank(sendBox.getSmsNumberArea())) sendBox.setSmsNumberArea(SmsNumberAreaCodeEnums.China.getInArea());
        boolean checkflag = checkHaveAreaCode(user.getOrganizationCode(), sendBox.getSmsNumberArea());
        if(!checkflag)
        {
        	 return new FebsResponse().message("所选号码区域不正确").fail();
        }
        /**
         * @end
         */
        /**
         * @begin 2020-09-01
         * 短信内容逻辑判断及去除前后空格
         * 
         */
        String smsContent = sendBox.getSmsContent();
    	if(smsContent!=null&&!"".equals(smsContent))
    	{
    		smsContent = smsContent.trim();
    		if(smsContent!=null&&!"".equals(smsContent))
    		{
    			sendBox.setSmsContent(smsContent);
    		}
    		else
    		{
    			return new FebsResponse().message("短信内容不能为空").fail();
    		}
    	}
    	else
		{
			return new FebsResponse().message("短信内容不能为空").fail();
		}
    	/**
    	 * @end
    	 */
        //解析（合并） 手机号码集合字符串
        String textNumbers = "";
        if (sendBox.getMoblieFile() != null && !sendBox.getMoblieFile().isEmpty()){
            String filename = sendBox.getMoblieFile().getOriginalFilename();
            if (!StringUtils.endsWith(filename, ".txt")) {
            	FebsResponse febsResponse = new FebsResponse();
    	        febsResponse.message("文件类型不支持");
    	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
    	        febsResponse.put("sessionToken", sessionToken);
    	        return febsResponse.fail();
            }
            textNumbers = getContentFromInputStream(sendBox.getMoblieFile());
        }
        String timingTime = String.valueOf(request.getParameter("timingTimeTxt"));
        if(timingTime != null&&!"".equals(timingTime)) 
        {
        	sendBox.setTimingTime(DateUtils.getDate(DateUtils.Y_M_D_H_M_S_1, timingTime));
        }
        
        //商户等相关信息初始化
        sendBox.setClientIp(IPUtil.getIpAddr(request));
        sendBox.setSubType(SendBoxSubTypeEnums.OrgWebSub.getCode());
        sendBox.setOrgCode(user.getOrganizationCode());
        sendBox.setCreateUsername(user.getUserAccount());
        sendBox.setAgentId(user.getOrganization().getAgentId());
        sendBox.setSmsType(SmsTypeEnums.TextMsg.getCode());
        sendBox.setType(SendBoxTypeEnums.formSubmit.getCode());
        if (StringUtils.isNotBlank(sendBox.getSmsNumbers())) {
            String smsNumbers = sendBox.getSmsNumbers();
            String[] numbers = smsNumbers.split("\n");
            String newNumbers = StringUtils.join(numbers, ",");
            textNumbers = textNumbers + "," + newNumbers;
        }

        //替换中文符号"，"
        if (textNumbers.contains("，")){
            textNumbers = textNumbers.replaceAll("，",",");
        }
        //过滤掉其中可能存在‘,,’的情况
        List<String> smsNumberList = Arrays.asList(textNumbers.split(",")).stream()
                .filter(s -> StringUtils.isNotBlank(s))
                .collect(Collectors.toList());
        if (smsNumberList.size() < 1){
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("未检测到任何手机号码,此次提交无效");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
        }

        //排查是否存在号码异常的情况
        int numberLimitDefault = 20;

        Code code = DatabaseCache.getCodeBySortCodeAndCode("System","numberLengthLimit");
        if (code != null){
            try {
                numberLimitDefault = Integer.valueOf(code.getName());
            } catch (NumberFormatException e) {
                log.error("发送短信 -> 号码最大长度限制字典值未生效，使用默认值20",e.getMessage());
            }
        }
        final int numberLimit = numberLimitDefault;
        List<String> errNumbers = Arrays.asList(textNumbers.split(",")).stream()
                .filter(s -> s.length() > numberLimit)
                .collect(Collectors.toList());
        if (errNumbers.size() > 0){
            String errMsg = String.format("本次提交号码存在%d个错误号码[%s]，号码长度不能超过%d,请修改后重试"
                    ,errNumbers.size()
                    ,StringUtils.join(errNumbers,",")
                    ,numberLimit);
            FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message(errMsg);
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
        }


        String smsNumbers = Joiner.on(",").join(smsNumberList);

        if (smsNumberList.size() > 50000){
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("单次提交不能超过5万个号码");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
        }
        sendBox.setSmsNumbers(smsNumbers);
        sendBox.setNumberCount(smsNumberList.size());

        //商户资费及余额预检查
        boolean result = false;
        try {
       	 	/**
             * @begin 2020-10-10
             * @modify
             * 新增支持国际号码
             */
//            result = organizationService.checkOrgAmountPrePay(user.getOrganizationCode()
//            , SmsTypeEnums.TextMsg.getCode(), SmsNumberAreaCodeEnums.China.getInArea(), smsNumberList.size());
        	String smsNumberArea = sendBox.getSmsNumberArea();
        	result = organizationService.checkOrgAmountPrePay(user.getOrganizationCode(), SmsTypeEnums.TextMsg.getCode(), smsNumberArea, smsNumberList.size());
            /**
             * @end
             */
            if (!result){
                return new FebsResponse().fail().message("商户余额不足");
            }
        } catch (ServiceException e) {
            return new FebsResponse().fail().message(e.getMessage());
        }

        //保存
        try {
			this.sendBoxService.createSendBox(sendBox);
        } catch (ServiceException e) {
        	FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message(e.getMessage());
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
		}catch (Exception e1) {
			log.error("保存发件箱失败",e1);
			FebsResponse febsResponse = new FebsResponse();
	        febsResponse.message("提交失败");
	        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
	        febsResponse.put("sessionToken", sessionToken);
	        return febsResponse.fail();
		}

        if(sendBox.getAuditState().intValue() == AuditStateEnums.Pass.getCode().intValue()) {
        	/*try {//推送到MQ
        		mqService.sendBox(sendBox.getId());
        	} catch (Exception e) {
        		log.error(String.format("表单提交发件箱【%s】推送MQ失败",sendBox.getSendCode()),e);
        	}*/
        	if(sendBox.getTimingTime() == null) {
        		/**
        		 * @begin 2020-12-06
        		 *  校验商户谷歌口令开关开启;
        		 *  开启则判断商户是否绑定谷歌：绑定则继续推送，未绑定则不推送
        		 *  未开启则保持原本流程推送
        		 * 
        		 */
        		boolean ispassflag = true;
        		String checkGooleKeySwitch = "OFF";
        		Code checkGooleKeySwitchSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkGooleKeySwitch");
        	    if(checkGooleKeySwitchSwitchCode!=null&&!"".equals(checkGooleKeySwitchSwitchCode.getName()))
        	    {
        	    	checkGooleKeySwitch = checkGooleKeySwitchSwitchCode.getName();
        	    }
        	    if("ON".equals(checkGooleKeySwitch))
        	    {
        	    	 OrganizationUser organizationUser = organizationUserService.findByUserAccount(user.getUserAccount());
        	    	 //判断谷歌口令是否绑定，未绑定则返回false
                     if(StringUtils.isBlank(organizationUser.getGoogleKey())) 
                     {
                    	 ispassflag = false;
                     }
        	    }
        	    //判断未绑定谷歌口令的商户，默认转换为“待审核”状态
    			if(ispassflag)
    			{
    				sendBoxService.splitRecordForTxt(sendBox, smsNumberList);
    			}
                /**
                 * @end
                 */
        	}
        }
        FebsResponse febsResponse = new FebsResponse();
        febsResponse.message(String.format("提交成功%s个号码", smsNumberList.size()));
        String sessionToken = getTokenInputStr(request,AjaxTokenProcessor.SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY);
        febsResponse.put("sessionToken", sessionToken);
        return febsResponse.success();
    }
    
    /**
     * 根据流（.txt）获取内容
     *
     * @param file
     * @return
     */
    private String getContentFromInputStream(MultipartFile file) {
        StringBuffer result = new StringBuffer();
        InputStreamReader inputStream = null;
        try {
            String code = getFilecharset(file.getInputStream());
            inputStream = new InputStreamReader(file.getInputStream(),code);
            BufferedReader reader = new BufferedReader(inputStream);
            String line = null;
            boolean firstFlag = true;
            while ((line = reader.readLine()) != null) {
                if (!firstFlag) {
                    result.append(",");
                } else {
                    firstFlag = false;
                }
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }

    private String rename(String fileName){
        int i = fileName.lastIndexOf(".");
        String str = fileName.substring(i);
        return RandomUtil.randomStringWithDate(18)+str;

    }

    public static FebsResponse download(HttpServletResponse response, String path) {
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        File file = null;
        try {
            file = new File(path);
            response.setHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(file.getName(), "UTF-8"));
        } catch (UnsupportedEncodingException e2) {
            return new FebsResponse().message("下载失败").fail();
        }
        byte[] buff = new byte[1024];
        BufferedInputStream bis = null;
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            bis = new BufferedInputStream(new FileInputStream(file));
            response.setHeader("Content-Length",String.valueOf(bis.available()));
            int i = bis.read(buff);
            while (i != -1) {
                os.write(buff, 0, buff.length);
                os.flush();
                i = bis.read(buff);
            }
        } catch (FileNotFoundException e1) {
            //e1.getMessage()+"系统找不到指定的文件";
            return new FebsResponse().message("系统找不到指定的文件").fail();
        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new FebsResponse().success();
    }

    private static  String getFilecharset(InputStream inputStream) {
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                return charset; //文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF
                    && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE"; //文件编码为 Unicode
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE"; //文件编码为 Unicode big endian
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8"; //文件编码为 UTF-8
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80
                            // - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return charset;
    }

    /**
         * 将原有发件箱中的记录进行重发
         * （仅筛选发送记录中<分拣失败>号码进行生成新单重发；若无符合条件的<分拣失败>号码，则重发失败）
     * @param request
     * @param sendCodes
     * @return
     */
    @ControllerEndpoint(operation = "发件箱批量重发", exceptionMessage = "发件箱批量重发失败")
    @GetMapping("resend/{sendCodes}")
    @ResponseBody
    @RequiresPermissions("sendBox:resend")
    @Limit(key = "resend", period = 2, count = 1, name = "发件箱批量重发", prefix = "limit")
    public FebsResponse resend(HttpServletRequest request,@PathVariable("sendCodes") String sendCodes){
        List<String> sendCodeList = Arrays.asList(sendCodes.split(","));
        if (sendCodeList == null ||sendCodeList.isEmpty()){
            return new FebsResponse().fail().message("重发失败");
        }

        final List<Map<String, Object>> data = Lists.newArrayList();
        final List<Map<String, Object>> error = Lists.newArrayList();
        sendCodeList.stream().forEach(sendCode ->{
            SendBox saveSendbox = new SendBox();
            saveSendbox.setCreateUsername(getCurrentUser().getUserName());
            saveSendbox.setClientIp(IPUtil.getIpAddr(request));
            try {
    			//根据chain-cfg.xml中的配置sendboxResent，进行相关校验、业务逻辑处理
    			//1、检索 将发件箱记录是否正常存在，并复制相关属性 queryOldSendBox
    			//2、检索 发件记录筛选号码<分拣失败>的号码进行重发 filterUnsentPhoneNumber
    			//3、商户资费及余额预检查 checkOrgAmout
            	//4、将符合条件的<分拣失败>号码生成新的发件箱记录入库，并推送分拣（若无符合条件的分拣失败号码，则不生成新发件箱记录）saveSendBox
                Command cmd = ChainUtil.getChain("sendboxResent");
                ContextBase context = new ContextBase();
                context.put(BaseCommand.OBJ_ORG_SERVICE,organizationService);
                context.put(BaseCommand.OBJ_SENDBOX_SERVICE,sendBoxService);
                context.put(BaseCommand.OBJ_SENDRECORD_SERVICE,sendRecordService);
                context.put(BaseCommand.STR_SENDCODE,sendCode);
                context.put(BaseCommand.OBJ_SAVE_SENDBOX_ENTITY,saveSendbox);
                if (cmd.execute(context)){
                    String errMsg = (String) context.get(BaseCommand.STR_ERROR_INFO);
                    error.add(
                            ImmutableMap.of(
                                    "oldSendCode",sendCode,
                                    "smsContent",saveSendbox.getSmsContent(),
                                    "errMsg",errMsg
                            )
                    );
                }else {
                    SendBox oldSendBox = (SendBox) context.get(BaseCommand.OBJ_SENDBOX_ENTITY);
                    data.add(
                            ImmutableMap.of(
                                    "oldSendCode",oldSendBox.getSendCode(),
                                    "newSendCode",saveSendbox.getSendCode(),
                                    "smsContent",saveSendbox.getSmsContent(),
                                    "resendNum",saveSendbox.getNumberCount()
                            )
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                error.add(
                        ImmutableMap.of(
                                "oldSendCode",sendCode,
                                "smsContent",saveSendbox.getSmsContent(),
                                "errMsg","系统内部异常:" + e.getMessage()
                        )
                );
            }
        });
        ImmutableMap<String, Object> result = ImmutableMap.of(
                "total", sendCodeList.size(),
                "data", data,
                "error", error
        );
        return new FebsResponse().success().data(result);
    }

    /**
     * 2021-03-02
     * 	校验token是否有效-公共方法
     * @param request
     * @param token_key
     * @param sessionToken
     * @param code
     */
    public boolean checkToken(HttpServletRequest request,String token_key,String sessionToken)
    {
    	boolean flag = true;
        String checkSessionTokenSwitch = "ON";
	    Code checkSessionTokenSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkSessionTokenSwitch");
	    if (checkSessionTokenSwitchCode != null && !"".equals(checkSessionTokenSwitchCode.getName())) 
	    {
	      checkSessionTokenSwitch = checkSessionTokenSwitchCode.getName();
	    }
	    if (!"OFF".equals(checkSessionTokenSwitch)) 
	    {
	      AjaxTokenProcessor token = AjaxTokenProcessor.getInstance();
	      if (!token.isTokenValid(request, true,token_key,sessionToken)) 
	      {
	      	flag = false ;
	      }
	    }
	    return flag;
      }
    
    /**
     * 校验传入的地域编码是否存在商户配置中
     * @param orgCode
     * @param smsNumberArea
     * @return 存在 true  不存在 false
     */
    public boolean checkHaveAreaCode(String orgCode,String smsNumberArea)
    {
    	boolean flag = false;
    	List<AreaCode> smsNumberAreaCode = organizationCostService.getOrgAreaCodeList(orgCode);
    	if(smsNumberAreaCode!=null && smsNumberAreaCode.size()>0)
    	{
    		for (int i = 0; i < smsNumberAreaCode.size(); i++) 
    		{
				String inArea = smsNumberAreaCode.get(i).getInArea();
				if(inArea.equals(smsNumberArea))
				{
					flag = true;
					break;
				}
			}
    	}
	    return flag;
      }
    
}
