package com.hero.sms.service.impl.message;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.DateUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.commands.sortingSMS.SortingFailCommand;
import com.hero.sms.commands.utils.ChainUtil;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.entity.Regexp;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.AppUtil;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.sensi.SensitiveFilter;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.ApiSendSmsModel;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxExcelModel;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.entity.message.SimpleNote;
import com.hero.sms.entity.message.SmsTemplate;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationUser;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SendBoxSubTypeEnums;
import com.hero.sms.enums.message.SendBoxTypeEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsTypeEnums;
import com.hero.sms.enums.organization.OrgSmsApproveTypeEnums;
import com.hero.sms.enums.organization.OrgStatusEnums;
import com.hero.sms.mapper.message.SendBoxMapper;
import com.hero.sms.service.agent.IAgentService;
import com.hero.sms.service.channel.ISmsChannelCostService;
import com.hero.sms.service.channel.ISmsChannelService;
import com.hero.sms.service.message.IMobileAreaService;
import com.hero.sms.service.message.IMobileBlackService;
import com.hero.sms.service.message.ISendBoxRecordCheckinfoService;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.service.message.ISmsTemplateService;
import com.hero.sms.service.mq.MQService;
import com.hero.sms.service.organization.IOrganizationService;
import com.hero.sms.service.organization.IOrganizationUserService;
import com.hero.sms.utils.FileDownLoadUtil;
import com.hero.sms.utils.RandomUtil;
import com.hero.sms.utils.StringUtil;

/**
 * 发件箱 Service实现
 *
 * @author Administrator
 * @date 2020-03-07 15:56:53
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SendBoxServiceImpl extends ServiceImpl<SendBoxMapper, SendBox> implements ISendBoxService {

	@Autowired
	private SendBoxMapper sendBoxMapper;

	@Autowired
	private ISendRecordService sendRecordService;

	@Autowired
	private IOrganizationService organizationService;
	
    @Autowired
    private IOrganizationUserService organizationUserService;

	@Autowired
	private IAgentService agentService;

	@Autowired
	private ISmsChannelService smsChannelService;

	@Autowired
	private ISmsChannelCostService smsChannelcostService;

	@Autowired
	private ISmsTemplateService smsTemplateService;
	
	@Autowired
	private IMobileBlackService mobileBlackService;
	
	@Autowired
	private IMobileAreaService mobileAreaService;
	
	/**
	 * 这里实例本身类的作用是防止事务失效
	 * 代码中的一些方法，若本类直接调用本类方法会出现事务失效
	 */
	@Autowired
	@Lazy
	private ISendBoxService sendboxService;
	
    @Autowired
    private ISendBoxRecordCheckinfoService sendBoxRecordCheckinfoService;
	
	@Autowired
	@Lazy
	private MQService mqService;

	@Override
    public IPage<SendBox> findSendBoxs(QueryRequest request, SendBoxQuery sendBox) {
        LambdaQueryWrapper<SendBox> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(CollectionUtils.isNotEmpty(sendBox.getIds())) {
	    	queryWrapper.in(SendBox::getId, sendBox.getIds());
	    }
        if (sendBox.getIsSortingFlag() != null) {
			if (sendBox.getIsSortingFlag())
				queryWrapper.isNotNull(SendBox::getSortingTime);
			else
				queryWrapper.isNull(SendBox::getSortingTime);
		}
		if (sendBox.getSmsCount() != null) {
			queryWrapper.eq(SendBox::getSmsCount, sendBox.getSmsCount());
		}
		
        if(sendBox.getIsTiming() != null) {//是否定时
	    	if(sendBox.getIsTiming())queryWrapper.isNotNull(SendBox::getTimingTime);
	    	else queryWrapper.isNull(SendBox::getTimingTime);
	    }
        if(sendBox.getLeTimingTime() != null) {//定时时间
        	queryWrapper.le(SendBox::getTimingTime, sendBox.getLeTimingTime());
        }
        if (sendBox.getSortingStartTime() != null){//分拣开始时间
        	queryWrapper.ge(SendBox::getSortingTime,sendBox.getSortingStartTime());
		}
		if (sendBox.getSortingEndTime() != null) {// 分拣结束时间
			queryWrapper.le(SendBox::getSortingTime, sendBox.getSortingEndTime());
		}
		if (StringUtils.isNotBlank(sendBox.getSendCode())) {// 批次号
			queryWrapper.eq(BaseSend::getSendCode, sendBox.getSendCode());
		}
		if (StringUtils.isNotBlank(sendBox.getCreateUsername())) {// 提交人
			queryWrapper.eq(SendBox::getCreateUsername, sendBox.getCreateUsername());
		}
		if (sendBox.getAgentId() != null) {// 代理商id
			queryWrapper.eq(BaseSend::getAgentId, sendBox.getAgentId());
		}
		if (sendBox.getUpAgentId() != null){//上级代理商ID
			queryWrapper.eq(SendBox::getUpAgentId,sendBox.getUpAgentId());
		}
		if (StringUtils.isNotBlank(sendBox.getOrgCode())) {// 商户编码
			queryWrapper.eq(BaseSend::getOrgCode, sendBox.getOrgCode());
		}
		if (sendBox.getSmsType() != null) {// 短信类型
			queryWrapper.eq(BaseSend::getSmsType, sendBox.getSmsType());
		}
		if (sendBox.getAuditState() != null) {// 审核状态
			queryWrapper.eq(SendBox::getAuditState, sendBox.getAuditState());
		}
		if (sendBox.getSubType() != null) {// 提交方式
			queryWrapper.eq(SendBox::getSubType, sendBox.getSubType());
		}
        if (sendBox.getCreateStartTime() != null){//提交开始时间
        	queryWrapper.ge(SendBox::getCreateTime,sendBox.getCreateStartTime());
		}
		if (sendBox.getCreateEndTime() != null) {//提交结束时间
			queryWrapper.le(SendBox::getCreateTime, sendBox.getCreateEndTime());
		}
		if (StringUtils.isNotBlank(sendBox.getSmsNumbers())){
			queryWrapper.like(SendBox::getSmsNumbers,sendBox.getSmsNumbers());
		}
		if (StringUtils.isNotBlank(sendBox.getSmsContent())){
			queryWrapper.like(BaseSend::getSmsContent,sendBox.getSmsContent());
		}
		if (sendBox.getExcludeAuditState() != null){
			queryWrapper.ne(SendBox::getAuditState,sendBox.getExcludeAuditState());
		}
		/**
		 * 2021-01-28
		 */
        if(sendBox.getIsTimingFlag() != null) {//是否定时(用于查询)
	    	if(sendBox.getIsTimingFlag())queryWrapper.isNotNull(SendBox::getIsTimingTime);
	    	else queryWrapper.isNull(SendBox::getIsTimingTime);
	    }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendBox.getSmsNumberArea())) {// 手机号码归属编码
			queryWrapper.eq(BaseSend::getSmsNumberArea, sendBox.getSmsNumberArea());
		}
		/**
	     * 是否长短信
	     * 1 不是
	     * 2 是
	     */
		if (sendBox.getIsLongSms()!= null){//是否长短信
			int wordsOfPerMsgInt = 70;
			Code wordsOfPerMsgCode = DatabaseCache.getCodeBySortCodeAndCode("System","wordsOfPerMsg");
		    if(wordsOfPerMsgCode!=null&&!"".equals(wordsOfPerMsgCode.getName()))
		    {
		    	try {
		    		wordsOfPerMsgInt = Integer.parseInt(wordsOfPerMsgCode.getName());
				} catch (Exception e) {}
		    }
			if(sendBox.getIsLongSms()==1)
			{
				//非长短信，字数小于等于70
				queryWrapper.le(SendBox::getSmsWords,wordsOfPerMsgInt);
			}
			else
			{
				//大于70
				queryWrapper.gt(SendBox::getSmsWords,wordsOfPerMsgInt);
			}
		}
		//过滤掉手机号码字段
		if(!sendBox.getIsNeedSmsNumber()) {
			queryWrapper.select(SendBox.class,tableFieldInfo -> !tableFieldInfo.getColumn().equals("sms_numbers"));
		}
		queryWrapper.orderByDesc(SendBox::getCreateTime);
		Page<SendBox> page = new Page<>(request.getPageNum(), request.getPageSize());
		return this.page(page, queryWrapper);
	}

	@Override
	public String getSmsNumbersByID(Long id){
		LambdaQueryWrapper<SendBox> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.select(SendBox::getSmsNumbers,SendBox::getType)
				.eq(SendBox::getId,id);
		SendBox sendBox = this.getOne(queryWrapper);
		//无记录或excel提交的   返回null
		if (sendBox == null || sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()){
			return null;
		}
		return sendBox.getSmsNumbers();
	}


    @Override
    public List<SendBox> findSendBoxs(SendBoxQuery sendBox) {
	    LambdaQueryWrapper<SendBox> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
	    if(CollectionUtils.isNotEmpty(sendBox.getIds())) {
	    	queryWrapper.in(SendBox::getId, sendBox.getIds());
	    }
		if (sendBox.getIsTiming() != null) {
			if (sendBox.getIsTiming())
				queryWrapper.isNotNull(SendBox::getTimingTime);
			else
				queryWrapper.isNull(SendBox::getTimingTime);
		}
		if (sendBox.getIsSortingFlag() != null) {
			if (sendBox.getIsSortingFlag())
				queryWrapper.isNotNull(SendBox::getSortingTime);
			else
				queryWrapper.isNull(SendBox::getSortingTime);
		}
		if (sendBox.getSmsCount() != null) {
			queryWrapper.eq(SendBox::getSmsCount, sendBox.getSmsCount());
		}
		if (sendBox.getLeTimingTime() != null) {
			queryWrapper.le(SendBox::getTimingTime, sendBox.getLeTimingTime());
		}
		if (sendBox.getSortingStartTime() != null) {// 分拣开始时间
			queryWrapper.ge(SendBox::getSortingTime, sendBox.getSortingStartTime());
		}
		if (sendBox.getSortingEndTime() != null) {// 分拣结束时间
			queryWrapper.le(SendBox::getSortingTime, sendBox.getSortingEndTime());
		}
		if (StringUtils.isNotBlank(sendBox.getSendCode())) {// 批次号
			queryWrapper.eq(BaseSend::getSendCode, sendBox.getSendCode());
		}
		if (StringUtils.isNotBlank(sendBox.getCreateUsername())) {// 提交人
			queryWrapper.eq(SendBox::getCreateUsername, sendBox.getCreateUsername());
		}
		if (sendBox.getAgentId() != null) {// 代理商id
			queryWrapper.eq(BaseSend::getAgentId, sendBox.getAgentId());
		}
		if (sendBox.getUpAgentId() != null){//上级代理商ID
			queryWrapper.eq(SendBox::getUpAgentId,sendBox.getUpAgentId());
		}
		if (StringUtils.isNotBlank(sendBox.getOrgCode())) {// 商户编码
			queryWrapper.eq(BaseSend::getOrgCode, sendBox.getOrgCode());
		}
		if (sendBox.getSmsType() != null) {// 短信类型
			queryWrapper.eq(BaseSend::getSmsType, sendBox.getSmsType());
		}
		if (sendBox.getAuditState() != null) {// 审核状态
			queryWrapper.eq(SendBox::getAuditState, sendBox.getAuditState());
		}
		if (sendBox.getSubType() != null) {// 提交方式
			queryWrapper.eq(SendBox::getSubType, sendBox.getSubType());
		}
		if (sendBox.getCreateStartTime() != null){
			queryWrapper.ge(SendBox::getCreateTime,sendBox.getCreateStartTime());
		}
		if (sendBox.getCreateEndTime() != null){
			queryWrapper.le(SendBox::getCreateTime,sendBox.getCreateEndTime());
		}
		if (StringUtils.isNotBlank(sendBox.getSmsNumbers())){
			queryWrapper.like(SendBox::getSmsNumbers,sendBox.getSmsNumbers());
		}
		if (StringUtils.isNotBlank(sendBox.getSmsContent())){
			queryWrapper.like(BaseSend::getSmsContent,sendBox.getSmsContent());
		}
		/**
		 * 2021-01-28
		 */
        if(sendBox.getIsTimingFlag() != null) {//是否定时(用于查询)
	    	if(sendBox.getIsTimingFlag())queryWrapper.isNotNull(SendBox::getIsTimingTime);
	    	else queryWrapper.isNull(SendBox::getIsTimingTime);
	    }
        /**
		 * 2021-03-07
		 */
        if (StringUtils.isNotBlank(sendBox.getSmsNumberArea())) {// 手机号码归属编码
			queryWrapper.eq(BaseSend::getSmsNumberArea, sendBox.getSmsNumberArea());
		}
		/**
	     * 是否长短信
	     * 1 不是
	     * 2 是
	     */
		if (sendBox.getIsLongSms()!= null){//是否长短信
			int wordsOfPerMsgInt = 70;
			Code wordsOfPerMsgCode = DatabaseCache.getCodeBySortCodeAndCode("System","wordsOfPerMsg");
		    if(wordsOfPerMsgCode!=null&&!"".equals(wordsOfPerMsgCode.getName()))
		    {
		    	try {
		    		wordsOfPerMsgInt = Integer.parseInt(wordsOfPerMsgCode.getName());
				} catch (Exception e) {}
		    }
			if(sendBox.getIsLongSms()==1)
			{
				//非长短信，字数小于等于70
				queryWrapper.le(SendBox::getSmsWords,wordsOfPerMsgInt);
			}
			else
			{
				//大于70
				queryWrapper.gt(SendBox::getSmsWords,wordsOfPerMsgInt);
			}
		}
		queryWrapper.orderByDesc(SendBox::getCreateTime);
		return this.baseMapper.selectList(queryWrapper);
	}

	@Override
	@Transactional
	public void createSendBox(SendBox sendBox) throws Exception {
		if(StringUtils.isBlank(sendBox.getSmsNumberArea())) sendBox.setSmsNumberArea(SmsNumberAreaCodeEnums.China.getInArea());
		Integer agentId = sendBox.getAgentId();
		if(agentId != null) {
			Agent agent = agentService.getById(agentId);
			if(agent == null) {
				throw new ServiceException("代理不存在");
			}
			/*
			 * @begin 2021-09-28
			 * 			
			 */
			String checkAgentStateSwitch = "OFF";
			Code checkAgentStateSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","checkAgentStateSwitch");
		    if(checkAgentStateSwitchCode!=null&&!"".equals(checkAgentStateSwitchCode.getName()))
		    {
		    	checkAgentStateSwitch = checkAgentStateSwitchCode.getName();
		    }
		    if("ON".equals(checkAgentStateSwitch))
		    {
		    	String agentStateCode = agent.getStateCode();
		    	if(!OrgStatusEnums.Normal.getCode().equals(agentStateCode))
		    	{
		    		throw new ServiceException("所属代理状态不正确，无法正常使用");
		    	}
		    }
		    /*
		     * @end
		     */
			Integer upAgentId = agent.getUpAgentId();
			sendBox.setUpAgentId(upAgentId);
		}
		
		String content = sendBox.getSmsContent();
		SensitiveFilter sensitiveFilter = SensitiveFilter.DEFAULT;
		boolean isContainSens = sensitiveFilter.isContain(content);
		if(isContainSens) {
			throw new ServiceException(String.format("短信内容包含敏感词==>【%s】",sensitiveFilter.filter(content, '*')));
		}
		int smsWords = sendBox.getSmsContent().length();
		sendBox.setSmsWords(smsWords);
		String orgCode = sendBox.getOrgCode();
		Organization org = organizationService.getOrganizationByCode(orgCode);
		if (org == null)
			return;
		String smsApproveType = org.getSmsApproveType();
		boolean isNeedAudit = smsApproveType.equals(OrgSmsApproveTypeEnums.ManualAudit.getCode());
		if (smsApproveType.equals(OrgSmsApproveTypeEnums.TempNotAudit.getCode())) {
			isNeedAudit = true;
			SmsTemplate querySmsTemp = new SmsTemplate();
			querySmsTemp.setOrgCode(orgCode);
			querySmsTemp.setApproveStatus(AuditStateEnums.Pass.getCode());
			List<SmsTemplate> smsTemplates = smsTemplateService.findSmsTemplates(querySmsTemp);
			if (CollectionUtils.isNotEmpty(smsTemplates)) {
				String tmpContent = content;
				if(sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()) {
					tmpContent = content.replaceAll("##[B-I]{1}##", "#@#");
				}
				
				for (SmsTemplate smsTemplate : smsTemplates) {
					String tempContent = smsTemplate.getTemplateContent();
					tempContent = tempContent.replaceAll("\\{[0-9]+\\}", "#@#");
					if(sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()) {
						if(tmpContent.equals(tempContent)) {
							isNeedAudit = false;
							break;
						}
					}else {
						tempContent = AppUtil.escapeExprSpecialWord(tempContent);
						String regex = tempContent.replaceAll("#@#", ".*");
						boolean match = AppUtil.match(regex, content);
						if (match) {
							isNeedAudit = false;
							break;
						}
					}
				}
			}
		}
		if (isNeedAudit) {
			sendBox.setAuditState(AuditStateEnums.Wait.getCode());
		} else {
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
		    	 OrganizationUser organizationUser = organizationUserService.findByUserAccount(sendBox.getCreateUsername());
		    	 //判断谷歌口令是否绑定，未绑定则返回false
	             if(StringUtils.isBlank(organizationUser.getGoogleKey())) 
	             {
	            	 ispassflag = false;
	             }
		    }
			//判断未绑定谷歌口令的商户，默认转换为“待审核”状态
			if(ispassflag)
			{
				sendBox.setAuditState(AuditStateEnums.Pass.getCode());
			}
			else
			{
				sendBox.setAuditState(AuditStateEnums.Wait.getCode());
			}
			/**
	         * @end
	         */
		}
		sendBox.setSmsCount(0);
		sendBox.setConsumeAmount(0);
		sendBox.setChannelCostAmount(0);
		sendBox.setAgentIncomeAmount(0);
		sendBox.setUpAgentIncomeAmount(0);
		sendBox.setIncomeAmount(0);
		sendBox.setCreateTime(new Date());
		//2021-01-28 新增定时固定显示字段
		if(sendBox.getTimingTime()!=null) 
		{
			sendBox.setIsTimingTime(sendBox.getTimingTime());
		}
		String smsBatch =  DatabaseCache.getCodeBySortCodeAndCode("System","smsBatch").getName();
		String sendCode = RandomUtil.randomStringWithDate(5);
        if(smsBatch!=null&&!"".equals(smsBatch))
        {
        	sendCode = RandomUtil.randomStringWithDate(smsBatch,5);
        }
		sendBox.setSendCode(sendCode);
		this.save(sendBox);
	}

	@Override
	@Transactional
	public void updateSendBox(SendBox sendBox) {
		this.saveOrUpdate(sendBox);
	}

	@Override
	@Transactional
	public void deleteSendBox(SendBox sendBox) {
		LambdaQueryWrapper<SendBox> wrapper = new LambdaQueryWrapper<>();
		// TODO 设置删除条件
		this.remove(wrapper);
	}

	@Override
	@Transactional
	public void deleteSendBoxs(String[] sendBoxIds) {
		List<String> list = Arrays.asList(sendBoxIds);
		this.removeByIds(list);
	}
	
	@Override
	public void splitRecord(List<SendBox> sendBoxes) {
		if(CollectionUtils.isEmpty(sendBoxes)) return;
		for (SendBox sendBox : sendBoxes) {
			if(sendBox.getType().intValue() == SendBoxTypeEnums.formSubmit.getCode()) {
				List<String> smsNumberList = Lists.newArrayList(sendBox.getSmsNumbers().split(","));
				sendboxService.splitRecordForTxt(sendBox,smsNumberList);
			}
			if(sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()) {
				String fileUrl = sendBox.getSmsNumbers();
				
//	        	/**
//	        	 * 异步读取
//	        	 * EasyExcel.read(fileUrl, SendBoxExcelModel.class, new ExcelModelListener(smsContent)).sheet().doRead();
//	        	 */
//				/**
//		    	 * @begin
//		    	 * 2020-12-22
//		    	 * 替换商户号码execl文件读取地址，因无法直接访问商户服务器，故需转换地址读取
//		    	 */
//				String sendBoxExcelPathSwitch = "OFF";
//				Code sendBoxExcelPathSwitchCode =  DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","sendBoxExcelPathSwitch");
//		    	if(sendBoxExcelPathSwitchCode!=null&&sendBoxExcelPathSwitchCode.getName()!=null&&!"".equals(sendBoxExcelPathSwitchCode.getName()))
//		    	{
//		    		sendBoxExcelPathSwitch = sendBoxExcelPathSwitchCode.getName();
//		    	}
//		    	if("ON".equals(sendBoxExcelPathSwitch))
//		    	{
//		    		String tempFileUrl = sendBox.getSmsNumbers();
//					String fileFullName = sendBox.getSmsNumbers();
//		    		 //将文件保存  并把文件路径保存到记录中
//			        String savePath = DatabaseCache.getCodeBySortCodeAndCode("System", "sendBoxExcelPath").getName();
//			        //拼装商户服务器的IP地址
//			    	Code merch_img_ip_code =  DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","merch_img_ip");
//			    	String merch_img_ip = "";
//			    	if(merch_img_ip_code!=null&&merch_img_ip_code.getName()!=null&&!"".equals(merch_img_ip_code.getName()))
//			    	{
//			    		merch_img_ip = merch_img_ip_code.getName();
//			    	}
//			    	//拼装商户服务器的文件路径
//			    	if(merch_img_ip!=null&&!"".equals(merch_img_ip))
//			    	{
//			    		tempFileUrl = merch_img_ip+"/"+tempFileUrl.replace(savePath, "execlflie/");
//			    	}
//			    	//判断本服务器上是否已经有该文件存在,若已存在，则不做处理，直接读取；不存在则需先复制下载到本服务器上
//			    	if(!FileUtil.fileIsExists(fileUrl))
//			    	{
//			    		//截取文件名
//			    		fileFullName = fileFullName.replace(savePath, "");
//			    		try {
//			    			//将商户服务器上的文件，复制下载到本服务器相同路径下
//			    			FileUtil.downloadNet(tempFileUrl, savePath,fileFullName.trim());
//						} catch (Exception e) {
//							// TODO: handle exception
//							e.printStackTrace();
//						}
//			    	}
//		    	}
//		    	/**
//		    	 * @end 
//		    	 */
				
				/**
                 * @begin
                 * 2021-04-21
                 * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
                 * 【说明】：这里需要新增校验fileUrl 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
                 * 【步骤】：
                 * 1、使用文件工具判断文件地址的文件是否存在于本机上
                 * 2、获取字典中负载服务器的IP地址
                 * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
                 */
				int returnInt = FileDownLoadUtil.downLoadSendBoxExcelFile(fileUrl);
                /**
                 * @end
                 */
				if(returnInt == 1)
				{
					List<Object> excelModels = EasyExcel.read(fileUrl).head(SendBoxExcelModel.class).sheet().doReadSync();
		            if(CollectionUtils.isEmpty(excelModels)) {
			            continue;
		            }
		            sendboxService.splitRecordForExcel(sendBox,excelModels);
				}
		     }
		}
	}

	@Override
	@Async
	public void splitRecordForTxt(SendBox sendBox, List<String> smsNumberList) {
    	if(CollectionUtils.isEmpty(smsNumberList)) return;
    	if(sendBox == null) return;
    	int interval = 10;
    	Code code = DatabaseCache.getCodeBySortCodeAndCode("System", "numberSplitGroupEachNum");
    	if(code != null) {
    		try {
    			interval = Integer.parseInt(code.getName());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
    	}
    	int size = smsNumberList.size();
		int page = size%interval==0? size/interval : size/interval+1;
		for(int j = 1;j<=page;j++) {
			int end = j==page? size:j*interval;
			int start = (j-1)*interval;
			try {
				List<String> subList = smsNumberList.subList(start, end);
				SimpleNote simpleNote = new SimpleNote();
				simpleNote.setId(sendBox.getId());
				simpleNote.setData(subList);
				//6-1-1-1-1   使用发件箱生产者SendBoxP 推送消息MQ_Topic_SendBox到MQ
				mqService.sendBox(simpleNote);
			} catch (Exception e) {
				log.error(String.format("拆分记录【%s】【%s,%s】失败", sendBox.getSendCode(),start,end),e);
			}
		}
    }
	
	@Override
	@Async
	public void splitRecordForExcel(SendBox sendBox,List<Object> excelModels) {
    	if(CollectionUtils.isEmpty(excelModels)) return;
    	if(sendBox == null) return;
    	int interval = 10;
    	Code code = DatabaseCache.getCodeBySortCodeAndCode("System", "numberSplitGroupEachNum");
    	if(code != null) {
    		try {
    			interval = Integer.parseInt(code.getName());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
    	}
    	int size = excelModels.size();
		int page = size%interval==0? size/interval : size/interval+1;
		
		for(int j = 1;j<=page;j++) {
			int start = (j-1)*interval;
			int end = j==page? size:j*interval;
			try {
				List<Object> subList = excelModels.subList(start, end);
				SimpleNote simpleNote = new SimpleNote();
				simpleNote.setId(sendBox.getId());
				simpleNote.setData(subList);
				mqService.sendBox(simpleNote);
			} catch (Exception e) {
				log.error(String.format("拆分记录【%s】【%s,%s】失败", sendBox.getSendCode(),start,end),e);
			}
		}
    }

	@SuppressWarnings("unchecked")
	@Override
	public void sortingSendBox(SimpleNote simpleNote,String msgId) {
		SendBox sendBox = sendBoxMapper.selectById(simpleNote.getId());
		if (sendBox == null)
			return;
		/*if (sendBox.getSortingTime() != null)
			return;*/
		if(sendBox.getAuditState().intValue() != AuditStateEnums.Pass.getCode().intValue()) return;
		Object data = simpleNote.getData();
		if(data == null) return;
		log.warn(String.format("【%s】商户号：【%s】批次号：【%s】",msgId,sendBox.getOrgCode(),sendBox.getSendCode()));
		
		SendRecord baseSendRecord = new SendRecord();
		baseSendRecord.setSendCode(sendBox.getSendCode());
		baseSendRecord.setUpAgentId(sendBox.getUpAgentId());
		baseSendRecord.setAgentId(sendBox.getAgentId());
		baseSendRecord.setOrgCode(sendBox.getOrgCode());
		baseSendRecord.setSubType(sendBox.getSubType());
		baseSendRecord.setSmsType(sendBox.getSmsType());
		baseSendRecord.setSmsContent(sendBox.getSmsContent());
		baseSendRecord.setSmsNumberArea(sendBox.getSmsNumberArea());
		baseSendRecord.setSmsWords(sendBox.getSmsWords());
		baseSendRecord.setSmsCount(0);
		baseSendRecord.setUpAgentIncomeAmount(0);
		baseSendRecord.setAgentIncomeAmount(0);
		baseSendRecord.setConsumeAmount(0);
		baseSendRecord.setIncomeAmount(0);
		baseSendRecord.setChannelCostAmount(0);

		Command cmd = null;
		Context context = null;
		
		long start = System.currentTimeMillis();
		Date startDate = new Date(start);
    	String startDateStr = DateUtil.getString(startDate, DateUtil.Y_M_D_H_M_S_S_1);
		log.warn(String.format("【%s】批次号：【%s】执行【sortingSendBox1】开始时间：【%s】",msgId,sendBox.getSendCode(),startDateStr));
		
		try {
			//2-2-2 根据chain-cfg.xml中的配置sortingSendBox1，进行相关校验、业务逻辑处理
			//1、检索 商户状态是否正常 QueryOrgCommand
			//2、检索 代理状态是否正常 QueryAgentCommand
			//3、检索 上游短信通道是否正常 QueryUseableSmsChannelCommand
			//4、校验 20211123-ADD 商户与代理资费配置（针对指定运营商）CheckOrgAgentChargesCommand
			cmd = ChainUtil.getChain("sortingSendBox1");
			context = new ContextBase();
			context.put(BaseCommand.OBJ_ORG_SERVICE, organizationService);
			context.put(BaseCommand.OBJ_AGENT_SERVICE, agentService);
			context.put(BaseCommand.OBJ_CHANNEL_SERVICE, smsChannelService);
			context.put(BaseCommand.OBJ_SENDBOX_ENTITY, sendBox);
			context.put(BaseCommand.OBJ_BASESEND_ENTITY, baseSendRecord);
			if (cmd.execute(context)) {
				SendBox updateSendBox = new SendBox();
				updateSendBox.setId(sendBox.getId());
				LambdaUpdateWrapper<SendBox> updateWrapper = new LambdaUpdateWrapper<>();
				updateWrapper.set(SendBox::getAuditState, AuditStateEnums.NoPass.getCode());
				updateWrapper.set(SendBox::getRefuseCause, context.get(BaseCommand.STR_ERROR_INFO).toString());
				updateWrapper.set(SendBox::getSortingTime,new Date());
				updateWrapper.set(SendBox::getTimingTime,null);
				updateWrapper.eq(SendBox::getId, sendBox.getId());
				update(updateWrapper);
				return;
			}
		} catch (Exception e) {
			log.error(String.format("发件箱【%s】执行分拣命令失败[1]", sendBox.getSendCode()), e);
			return;
		}
		
		long end = System.currentTimeMillis();
        long getTime = end-start;
        Date endDate = new Date(end);
        String endDateStr = DateUtil.getString(endDate, DateUtil.Y_M_D_H_M_S_S_1);
		log.warn(String.format("【%s】批次号：【%s】执行【sortingSendBox1】结束时间：【%s】,耗时【%d】ms",msgId,sendBox.getSendCode(),endDateStr,getTime));
		
		List<SendRecord> sendRecords = null;
		
		log.warn(String.format("【%s】批次号：【%s】执行【sortingSendBox21-22】开始时间：【%s】",msgId,sendBox.getSendCode(),endDateStr));
		if(sendBox.getType().intValue() == SendBoxTypeEnums.formSubmit.getCode()) {
			//根据chain-cfg.xml中的配置sortingSendBox21，进行相关校验、业务逻辑处理
			//1、检索 代理日发送量限额 AgentDayLimitCommand
			//2、检索 手机号码是否通过（黑名单、区域限制、运营商限制、号段等） CheckNumberCommand
			//2-1、检索同批次同号码唯一分拣 CheckSendBoxRecordCommand
			//3、检索 上游短信通道是否通过 （通道状态、分组状态、区域、运营商等） SelectChannelCommand
			//4、检索 通道号码池  SelectNumberPoolCommand
			//5、检索 获取上游通道资费  QueryChannelCostCommand
			//6、检索 计算商户、代理、平台、上游等收益（成本） CalculationAmountCommand
			//7、增加 分拣成功，顶级代理 日发送量累加  IncreaseTopAgentSend
			sendRecords = assemblySendRecordsFormSubmit(sendBox, baseSendRecord,data, cmd, context);
		}
		if(sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode()) {
			//根据chain-cfg.xml中的配置sortingSendBox22，进行相关校验、业务逻辑处理
			//1、检索 短信内容长度，过长需拆分  CalculationSendRecordSmsContentCommand
			//2、检索 代理日发送量限额 AgentDayLimitCommand
			//3、检索 手机号码是否通过（黑名单、区域限制、运营商限制、号段等） CheckNumberCommand
			//3-1、检索同批次同号码唯一分拣 CheckSendBoxRecordCommand
			//4、检索 上游短信通道是否通过 （通道状态、分组状态、区域、运营商等） SelectChannelCommand
			//5、检索 通道号码池  SelectNumberPoolCommand
			//6、检索 获取上游通道资费  QueryChannelCostCommand
			//7、检索 计算商户、代理、平台、上游等收益（成本） CalculationAmountCommand
			//8、增加 分拣成功，顶级代理 日发送量累加  IncreaseTopAgentSend
			sendRecords = assemblySendRecordsExcleSubmit(sendBox,data, cmd, context);
		}
		
		long assemblySendRecordsEnd = System.currentTimeMillis();
        long assemblySendRecordsGetTime = assemblySendRecordsEnd-end;
        Date assemblySendRecordsEndDate = new Date(assemblySendRecordsEnd);
        String assemblySendRecordsEndDateStr = DateUtil.getString(assemblySendRecordsEndDate, DateUtil.Y_M_D_H_M_S_S_1);
		log.warn(String.format("【%s】批次号：【%s】执行【sortingSendBox21-22】结束时间：【%s】,耗时【%d】ms",msgId,sendBox.getSendCode(),assemblySendRecordsEndDateStr,assemblySendRecordsGetTime));
		
		if(CollectionUtils.isEmpty(sendRecords)) {
			log.error(String.format("发件箱【%s】没有有效的记录", sendBox.getSendCode()));
			LambdaUpdateWrapper<SendBox> updateWrapper = new LambdaUpdateWrapper<>();
			//updateWrapper.set(SendBox::getAuditState, AuditStateEnums.NoPass.getCode());
			updateWrapper.set(SendBox::getTimingTime,null);
			//updateWrapper.set(SendBox::getRefuseCause,"没有有效记录")
			updateWrapper.set(SendBox::getSortingTime, new Date());
			updateWrapper.eq(SendBox::getId, sendBox.getId());
			update(updateWrapper);
			return;
		}
		
		log.warn(String.format("【%s】批次号：【%s】执行【sortingSendBox3】开始时间：【%s】",msgId,sendBox.getSendCode(),assemblySendRecordsEndDateStr));
		try {
			//2-2-3 根据chain-cfg.xml中的配置sortingSendBox3，进行相关校验、业务逻辑处理
			//1、批量保存发送记录 BatchSaveSendRecordCommand
			//2、更新商户余额 UpdateOrgAmountCommand
			//3、更新代理分润 UpdateAgentAmountCommand
			//4、更新发件箱数据 UpdateSendBoxCommand
			sendboxService.sendBoxForSaveData(sendBox, sendRecords);
		} catch (ServiceException e) {
			LambdaUpdateWrapper<SendBox> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.set(SendBox::getAuditState, AuditStateEnums.NoPass.getCode());
			updateWrapper.set(SendBox::getRefuseCause, e.getMessage()).eq(SendBox::getId, sendBox.getId());
			updateWrapper.set(SendBox::getSortingTime, new Date());
			update(updateWrapper);
			return;
		} catch (Exception e) {
			log.error(String.format("发件箱【%s】执行分拣保存失败", sendBox.getSendCode()), e);
			return;
		}
		
		long sendBoxForSaveDataEnd = System.currentTimeMillis();
        long sendBoxForSaveDataGetTime = sendBoxForSaveDataEnd-assemblySendRecordsEnd;
        Date sendBoxForSaveDataEndDate = new Date(sendBoxForSaveDataEnd);
        String sendBoxForSaveDataEndDateStr = DateUtil.getString(sendBoxForSaveDataEndDate, DateUtil.Y_M_D_H_M_S_S_1);
		log.warn(String.format("【%s】批次号：【%s】执行【sortingSendBox3】结束时间：【%s】,耗时【%d】ms",msgId,sendBox.getSendCode(),sendBoxForSaveDataEndDateStr,sendBoxForSaveDataGetTime));
		
		//2-2-4 将分拣结果进行推送
		pushMsg((List<SmsChannel>)context.get(BaseCommand.LIST_SMS_CHANNEL),sendRecords);
	}

	private List<SendRecord> assemblySendRecordsFormSubmit(SendBox sendBox, SendRecord baseSendRecord, Object data, Command cmd,
			Context context) {
		List<String> numbers = (List<String>)data;
		int orgConsumeAmount = 0;
		int upAgentIncomeAmount = 0;
		int agentIncomeAmount = 0;
		int channelCostAmount = 0;
		int incomeAmount = 0;
		int numberCount = 0;
		int smsCount = 0;
		
/*		int words = 70;
		Code wordsCode = DatabaseCache.getCodeBySortCodeAndCode("System", "wordsOfPerMsg");
		if (wordsCode != null) {
			words = Integer.parseInt(wordsCode.getName());
		}*/
		int words = 70;
		int smsWords = sendBox.getSmsContent().length();
		if (smsWords > 70){
			words = 67;
		}
		int baseSmsCount = (int) Math.ceil(smsWords * 1d / words);
		baseSendRecord.setSmsCount(baseSmsCount);
		
		List<SendRecord> sendRecords = Lists.newArrayList();
		try {
			//1、检索 手机号码是否通过（黑名单、区域限制、运营商限制、号段等） CheckNumberCommand
			//1-1、检索同批次同号码唯一分拣 CheckSendBoxRecordCommand
			//2、检索 上游短信通道是否通过 （通道状态、分组状态、区域、运营商等） SelectChannelCommand
			//3、检索 通道号码池  SelectNumberPoolCommand
			//4、检索 获取上游通道资费  QueryChannelCostCommand
			//5、检索 计算商户、代理、平台、上游等收益（成本） CalculationAmountCommand
			cmd = ChainUtil.getChain("sortingSendBox21");
			context.put(BaseCommand.OBJ_CHANNEL_COST_SERVICE, smsChannelcostService);
			context.put(BaseCommand.OBJ_MOBILEBLACK_SERVICE, mobileBlackService);
			context.put(BaseCommand.OBJ_MOBILEAREA_SERVICE, mobileAreaService);
			context.put(BaseCommand.OBJ_SENDBOXRECORDCHECKINFO_SERVICE, sendBoxRecordCheckinfoService);
		} catch (Exception e) {
			log.error(String.format("发件箱【%s】获取分拣命令21失败", sendBox.getSendCode()), e);
		}
		for (String number : numbers) {
			try {
				if(StringUtils.isEmpty(number))continue;
				SendRecord newSendRecord = new SendRecord();
				BeanUtils.copyProperties(newSendRecord, baseSendRecord);
				 /*
	             * @begin 2020-06-09
	             * 过滤字符串中的换行符、回车符及空格 
	             */
				number = StringUtil.replaceBlank(number);
	            /*
	             * @end
	             */
				newSendRecord.setSmsNumber(number);
				newSendRecord.setState(SendRecordStateEnums.WaitReq.getCode());
//				newSendRecord.setCreateTime(new Date());
				sendRecords.add(newSendRecord);
		 	    context.put(BaseCommand.OBJ_SAVE_SENDRECORD_ENTITY, newSendRecord);
				if (cmd.execute(context)) {
					/*newSendRecord.setState(SendRecordStateEnums.SortingFail.getCode());
					newSendRecord.setStateDesc(context.get(BaseCommand.STR_ERROR_INFO).toString());*/
					new SortingFailCommand().execute(context);
					continue;
				}
				numberCount++;
				smsCount += newSendRecord.getSmsCount();
				orgConsumeAmount += newSendRecord.getConsumeAmount();
				upAgentIncomeAmount += newSendRecord.getUpAgentIncomeAmount();
				agentIncomeAmount += newSendRecord.getAgentIncomeAmount();
				channelCostAmount += newSendRecord.getChannelCostAmount();
				incomeAmount += newSendRecord.getIncomeAmount();
			} catch (Exception e) {
				log.error(String.format("发件箱【%s】号码【%s】执行分拣失败", sendBox.getSendCode(), number), e);
				try {
					new SortingFailCommand().execute(context);
				} catch (Exception e1) {
					log.error("分拣失败处理异常",e1);
				}
				continue;
			}
		}
		sendBox.setNumberCount(numberCount);
		sendBox.setSmsCount(smsCount);
		sendBox.setConsumeAmount(orgConsumeAmount);
		sendBox.setChannelCostAmount(channelCostAmount);
		sendBox.setAgentIncomeAmount(agentIncomeAmount);
		sendBox.setUpAgentIncomeAmount(upAgentIncomeAmount);
		sendBox.setIncomeAmount(incomeAmount);
		return sendRecords;
	}

	private List<SendRecord> assemblySendRecordsExcleSubmit(SendBox sendBox, Object data, Command cmd,
			Context context) {
		List<Object> datas = (List<Object>)data;
		String smsContent = sendBox.getSmsContent();
		
	/*	int words = 70;
		Code wordsCode = DatabaseCache.getCodeBySortCodeAndCode("System", "wordsOfPerMsg");
		if (wordsCode != null) {
			words = Integer.parseInt(wordsCode.getName());
		}*/
		int orgConsumeAmount = 0;
		int upAgentIncomeAmount = 0;
		int agentIncomeAmount = 0;
		int channelCostAmount = 0;
		int incomeAmount = 0;
		int numberCount = 0;
		int smsCount = 0;
		List<SendRecord> sendRecords = Lists.newArrayList();
		try {
			//1、检索 短信内容长度，过长需拆分  CalculationSendRecordSmsContentCommand
			//2、检索 手机号码是否通过（黑名单、区域限制、运营商限制、号段等） CheckNumberCommand
			//2-1、检索同批次同号码唯一分拣 CheckSendBoxRecordCommand
			//3、检索 上游短信通道是否通过 （通道状态、分组状态、区域、运营商等） SelectChannelCommand
			//4、检索 通道号码池  SelectNumberPoolCommand
			//5、检索 获取上游通道资费  QueryChannelCostCommand
			//6、检索 计算商户、代理、平台、上游等收益（成本） CalculationAmountCommand
			cmd = ChainUtil.getChain("sortingSendBox22");
			context.put(BaseCommand.OBJ_CHANNEL_COST_SERVICE, smsChannelcostService);
			context.put(BaseCommand.OBJ_MOBILEBLACK_SERVICE, mobileBlackService);
			context.put(BaseCommand.OBJ_MOBILEAREA_SERVICE, mobileAreaService);
			context.put(BaseCommand.OBJ_SENDBOXRECORDCHECKINFO_SERVICE, sendBoxRecordCheckinfoService);
		} catch (Exception e) {
			log.error(String.format("发件箱【%s】获取分拣命令22失败", sendBox.getSendCode()), e);
		}
		for (Object obj : datas) {
			SendRecord sendRecord = new SendRecord();
			try {
				JSONObject jsonObj = (JSONObject)obj;
				SendBoxExcelModel model = JSONObject.toJavaObject(jsonObj, SendBoxExcelModel.class);
    			String smsNumber = model.getColumn0();
    			if(StringUtils.isBlank(smsNumber))continue;
    			smsNumber = smsNumber.trim();
    			/*
	             * @begin 2020-06-09
	             * 过滤字符串中的换行符、回车符及空格 
	             */
    			smsNumber = StringUtil.replaceBlank(smsNumber);
	            /*
	             * @end
	             */
    			if(sendBox.getSmsNumberArea().equals(SmsNumberAreaCodeEnums.China.getInArea()) && !AppUtil.match(Regexp.MOBILE_REG, smsNumber)) {
    				continue;
    			}
    			sendRecord.setSmsNumber(smsNumber);
    			String newContent = smsContent.replaceAll("##B##", model.getColumn1()).replaceAll("##C##", model.getColumn2())
    			.replaceAll("##D##", model.getColumn3()).replaceAll("##E##", model.getColumn4())
    			.replaceAll("##F##", model.getColumn5()).replaceAll("##G##", model.getColumn6())
    			.replaceAll("##H##", model.getColumn7()).replaceAll("##I##", model.getColumn8());
    			
    			int smsWords = newContent.length();
    			//int baseSmsCount = (int) Math.ceil(smsWords * 1d / words);
    			//sendRecord.setSmsCount(baseSmsCount);
    			sendRecord.setSmsContent(newContent);
    			sendRecord.setSendCode(sendBox.getSendCode());
    			sendRecord.setUpAgentId(sendBox.getUpAgentId());
    			sendRecord.setAgentId(sendBox.getAgentId());
    			sendRecord.setOrgCode(sendBox.getOrgCode());
    			sendRecord.setSubType(sendBox.getSubType());
    			sendRecord.setSmsType(sendBox.getSmsType());
    			sendRecord.setSmsNumberArea(sendBox.getSmsNumberArea());
    			sendRecord.setUpAgentIncomeAmount(0);
    			sendRecord.setAgentIncomeAmount(0);
    			sendRecord.setConsumeAmount(0);
    			sendRecord.setIncomeAmount(0);
    			sendRecord.setChannelCostAmount(0);
    			sendRecord.setState(SendRecordStateEnums.WaitReq.getCode());
    			sendRecord.setCreateTime(new Date());
    			sendRecords.add(sendRecord);
				context.put(BaseCommand.OBJ_SAVE_SENDRECORD_ENTITY, sendRecord);
				if (cmd.execute(context)) {
/*					sendRecord.setState(SendRecordStateEnums.SortingFail.getCode());
					sendRecord.setStateDesc(context.get(BaseCommand.STR_ERROR_INFO).toString());*/
					new SortingFailCommand().execute(context);
					continue;
				}
				numberCount++;
				smsCount += sendRecord.getSmsCount();
				orgConsumeAmount += sendRecord.getConsumeAmount();
				upAgentIncomeAmount += sendRecord.getUpAgentIncomeAmount();
				agentIncomeAmount += sendRecord.getAgentIncomeAmount();
				channelCostAmount += sendRecord.getChannelCostAmount();
				incomeAmount += sendRecord.getIncomeAmount();
			} catch (Exception e) {
				log.error(String.format("发件箱【%s】执行分拣号码【%s】失败", sendBox.getSendCode(), sendRecord.getSmsNumber()), e);
			}
		}
		sendBox.setNumberCount(numberCount);
		sendBox.setSmsCount(smsCount);
		sendBox.setConsumeAmount(orgConsumeAmount);
		sendBox.setChannelCostAmount(channelCostAmount);
		sendBox.setUpAgentIncomeAmount(upAgentIncomeAmount);
		sendBox.setAgentIncomeAmount(agentIncomeAmount);
		sendBox.setIncomeAmount(incomeAmount);
		return sendRecords;
	}

	@Override
	public void audit(String sendBoxIds, Integer auditState,String refuseCause) {
		String[] ids = sendBoxIds.split(",");
		LambdaUpdateWrapper<SendBox> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.set(SendBox::getAuditState, auditState);
		if (auditState == AuditStateEnums.NoPass.getCode().intValue()){
			updateWrapper.set(SendBox::getSortingTime,new Date());
			updateWrapper.set(SendBox::getTimingTime,null);//2021-01-29 将定时时间字段清空
			refuseCause = StringUtils.isNotBlank(refuseCause)?refuseCause:"系统审核";
			updateWrapper.set(SendBox::getRefuseCause,refuseCause);
		}
		updateWrapper.in(BaseSend::getId, ids);
		update(updateWrapper);
	}

	/**
	 * 开启事务保存商户消费金额、代理利润金额、发送记录
	 * 
	 * @param sendBox
	 * @param sendRecords
	 * @param orgConsumeAmount
	 * @param agentIncomeAmount
	 * @throws Exception
	 */
	@Transactional(rollbackFor = { Throwable.class})
	@Override
	public void sendBoxForSaveData(SendBox sendBox, List<SendRecord> sendRecords) throws Exception {
		Command  cmd = ChainUtil.getChain("sortingSendBox3");
		ContextBase context = new ContextBase();
		context.put(BaseCommand.OBJ_ORG_SERVICE, organizationService);
		context.put(BaseCommand.OBJ_AGENT_SERVICE, agentService);
		context.put(BaseCommand.OBJ_SENDBOX_SERVICE, this);
		context.put(BaseCommand.OBJ_SENDRECORD_SERVICE, sendRecordService);
		context.put(BaseCommand.OBJ_SENDBOX_ENTITY, sendBox);
		context.put(BaseCommand.LIST_SEND_RECORD, sendRecords);
		cmd.execute(context);
}

	@Override
	public FebsResponse apiSendSMS(@Valid ApiSendSmsModel model,String clientIp) {
		try {
			
			/*
             * @begin 2020-06-09
             * 过滤字符串中的换行符、回车符及空格 
             */
			String smsNumber = StringUtil.replaceBlank(model.getPhones());
            /*
             * @end
             */
			smsNumber = StringUtils.removeEnd(StringUtils.removeStart(smsNumber, ","),",");
			int count = smsNumber.length() - smsNumber.replaceAll(",", "").length()+1;
			SendBox baseSendRecord = new SendBox();
			baseSendRecord.setOrgCode(model.getOrgCode());
			baseSendRecord.setSmsNumberArea(model.getPhoneArea());
			baseSendRecord.setSmsNumbers(smsNumber);
			baseSendRecord.setSmsContent(model.getMessage());
			if(StringUtils.isNotBlank(model.getTimingTime())) {
				baseSendRecord.setTimingTime(DateUtils.parseDate(model.getTimingTime(), "yyyyMMddHHmm"));
				//2021-01-28 新增定时固定显示字段
				baseSendRecord.setIsTimingTime(DateUtils.parseDate(model.getTimingTime(), "yyyyMMddHHmm"));
			}
			baseSendRecord.setClientIp(clientIp);
			baseSendRecord.setSubType(SendBoxSubTypeEnums.HttpSub.getCode());
			baseSendRecord.setCreateUsername(clientIp);
	        baseSendRecord.setType(SendBoxTypeEnums.formSubmit.getCode());
	        baseSendRecord.setNumberCount(count);
			baseSendRecord.setSmsType(SmsTypeEnums.TextMsg.getCode());
			
			
			Command  cmd = ChainUtil.getChain("apiSendBox");
			ContextBase context = new ContextBase();
			context.put(BaseCommand.OBJ_ORG_SERVICE, organizationService);
			context.put(BaseCommand.OBJ_TEMPLATE_SERVICE, smsTemplateService);
			context.put(BaseCommand.OBJ_SENDBOX_SERVICE, this);
			context.put(BaseCommand.OBJ_API_SENDBOX, model);
			context.put(BaseCommand.OBJ_BASESEND_ENTITY, baseSendRecord);
			if(cmd.execute(context)) {
				String errInfo = (String)context.get(BaseCommand.STR_ERROR_INFO);
				return new FebsResponse().fail().message(errInfo);
			}
			if(baseSendRecord.getAuditState().intValue() == AuditStateEnums.Pass.getCode().intValue()) {
	        	if(baseSendRecord.getTimingTime() == null) {
	        		sendboxService.splitRecordForTxt(baseSendRecord, Lists.newArrayList(smsNumber.split(",")));
	        	}
			}
			
			Map<String, Object> result = Maps.newHashMap();			
			result.put("sendCode", baseSendRecord.getSendCode());
			try {
				Long availableAmount = (Long) context.get(BaseCommand.OBJ_ORG_AVAILABLE_AMOUNT);
				result.put("availableAmount", new DecimalFormat("0.00").format(new BigDecimal(availableAmount).divide(new BigDecimal("100"))));
			} catch (Exception e) {
				log.error(String.format("商户【%s】api发件箱批次【%s】查询可用余额失败",model.getOrgCode(),baseSendRecord.getSendCode()));
			} 
			return new FebsResponse().success().message("提交成功").data(result);
		} catch (Exception e) {
			log.error(String.format("商户【%s】api发件箱失败",model.getOrgCode()),e);
			return new FebsResponse().fail().message("提交异常");
		}
	}

	@Override
	public void cancelTimings(String[] sendBoxIds, boolean isSendingNow) {
		if (sendBoxIds == null || sendBoxIds.length < 1 ) return;
		LambdaUpdateWrapper<SendBox> updateWrapper = new LambdaUpdateWrapper<>();

		if(isSendingNow){//取消定时，立即发送
			updateWrapper.set(SendBox::getTimingTime,null)
					.in(BaseSend::getId,sendBoxIds);
		}else {//取消定时，不在发送
			updateWrapper.set(SendBox::getTimingTime,null)
					.set(SendBox::getAuditState,AuditStateEnums.NoPass.getCode().intValue())
					.set(SendBox::getRefuseCause,"商户取消定时")
					.in(BaseSend::getId,sendBoxIds);
		}

		update(updateWrapper);
	}

	public int pushMsg(List<SmsChannel> smsChannels,List<SendRecord> sendRecords) {
		int successNum = 0;
		if(CollectionUtils.isEmpty(smsChannels)) return successNum;
		Map<Integer, String> protocolTypes = smsChannels.stream().collect(Collectors.toMap(SmsChannel::getId, SmsChannel::getProtocolType));
		if (CollectionUtils.isNotEmpty(sendRecords)) {
			for (SendRecord sendRecord : sendRecords) {
				if(sendRecord.getState().intValue() != SendRecordStateEnums.WaitReq.getCode() &&  sendRecord.getState().intValue() != SendRecordStateEnums.ReqFail.getCode()) continue;
				try {
					Integer channelId = sendRecord.getChannelId();
					//2-2-4-1  使用发件箱生产者SendBoxP  将分拣后的信息 推送消息MQ_Topic_SendRecord**到MQ
					mqService.sendRecord(protocolTypes.get(channelId),channelId.toString(),sendRecord.getId());
					successNum++;
				} catch (Exception e) {
					log.error("发送记录提交MQ失败:" + JSON.toJSONString(sendRecord), e);
				}
			}
		}
		return successNum;
	}

	@Override
	public int countByEntity(SendBoxQuery sendBox) {
		LambdaQueryWrapper<SendBox> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		if(CollectionUtils.isNotEmpty(sendBox.getIds())) {
			queryWrapper.in(SendBox::getId, sendBox.getIds());
		}
		if (sendBox.getIsTiming() != null) {
			if (sendBox.getIsTiming())
				queryWrapper.isNotNull(SendBox::getTimingTime);
			else
				queryWrapper.isNull(SendBox::getTimingTime);
		}
		if (sendBox.getLeTimingTime() != null) {
			queryWrapper.le(SendBox::getTimingTime, sendBox.getLeTimingTime());
		}
		if (sendBox.getSortingStartTime() != null) {// 分拣开始时间
			queryWrapper.ge(SendBox::getSortingTime, sendBox.getSortingStartTime());
		}
		if (sendBox.getSortingEndTime() != null) {// 分拣结束时间
			queryWrapper.le(SendBox::getSortingTime, sendBox.getSortingEndTime());
		}
		if (StringUtils.isNotBlank(sendBox.getSendCode())) {// 批次号
			queryWrapper.eq(BaseSend::getSendCode, sendBox.getSendCode());
		}
		if (StringUtils.isNotBlank(sendBox.getCreateUsername())) {// 提交人
			queryWrapper.eq(SendBox::getCreateUsername, sendBox.getCreateUsername());
		}
		if (sendBox.getAgentId() != null) {// 代理商id
			queryWrapper.eq(BaseSend::getAgentId, sendBox.getAgentId());
		}
		if (sendBox.getUpAgentId() != null){//上级代理商ID
			queryWrapper.eq(SendBox::getUpAgentId,sendBox.getUpAgentId());
		}
		if (StringUtils.isNotBlank(sendBox.getOrgCode())) {// 商户编码
			queryWrapper.eq(BaseSend::getOrgCode, sendBox.getOrgCode());
		}
		if (sendBox.getSmsType() != null) {// 短信类型
			queryWrapper.eq(BaseSend::getSmsType, sendBox.getSmsType());
		}
		if (sendBox.getAuditState() != null) {// 审核状态
			queryWrapper.eq(SendBox::getAuditState, sendBox.getAuditState());
		}
		if (sendBox.getSubType() != null) {// 提交方式
			queryWrapper.eq(SendBox::getSubType, sendBox.getSubType());
		}
		if (sendBox.getCreateStartTime() != null){
			queryWrapper.ge(SendBox::getCreateTime,sendBox.getCreateStartTime());
		}
		if (sendBox.getCreateEndTime() != null){
			queryWrapper.le(SendBox::getCreateTime,sendBox.getCreateEndTime());
		}
		if (StringUtils.isNotBlank(sendBox.getSmsNumbers())){
			queryWrapper.like(SendBox::getSmsNumbers,sendBox.getSmsNumbers());
		}
		if (StringUtils.isNotBlank(sendBox.getSmsContent())){
			queryWrapper.like(BaseSend::getSmsContent,sendBox.getSmsContent());
		}
		return count(queryWrapper);
	}
}
