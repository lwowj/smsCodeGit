package com.hero.sms.service.impl.message;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.message.SmsTemplate;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.mapper.message.SmsTemplateMapper;
import com.hero.sms.service.message.ISmsTemplateService;

/**
 * 短信模板表 Service实现
 *
 * @author Administrator
 * @date 2020-03-11 20:08:30
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SmsTemplateServiceImpl extends ServiceImpl<SmsTemplateMapper, SmsTemplate> implements ISmsTemplateService {

    @Autowired
    private SmsTemplateMapper smsTemplateMapper;

    @Override
    public IPage<SmsTemplate> findSmsTemplates(QueryRequest request, SmsTemplate smsTemplate) {
        LambdaQueryWrapper<SmsTemplate> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(smsTemplate.getOrgCode())){//商户编号
            queryWrapper.eq(SmsTemplate::getOrgCode,smsTemplate.getOrgCode());
        }
        if (smsTemplate.getSubmitterId() != null){//提交人id
            queryWrapper.eq(SmsTemplate::getSubmitterId,smsTemplate.getSubmitterId());
        }
        if (StringUtils.isNotBlank(smsTemplate.getTemplateName())){//模板名称
            queryWrapper.like(SmsTemplate::getTemplateName,smsTemplate.getTemplateName());
        }
        if (smsTemplate.getApproveStatus() != null){//审核状态
            queryWrapper.eq(SmsTemplate::getApproveStatus,smsTemplate.getApproveStatus());
        }
        queryWrapper.orderByDesc(SmsTemplate::getCreateDate);
        // TODO 设置查询条件
        Page<SmsTemplate> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SmsTemplate> findSmsTemplates(SmsTemplate smsTemplate) {
	    LambdaQueryWrapper<SmsTemplate> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
	    if(StringUtils.isNotBlank(smsTemplate.getOrgCode())) {
	    	queryWrapper.eq(SmsTemplate::getOrgCode, smsTemplate.getOrgCode());
	    }
	    if(smsTemplate.getApproveStatus() != null) {
	    	queryWrapper.eq(SmsTemplate::getApproveStatus, smsTemplate.getApproveStatus());
	    }
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSmsTemplate(SmsTemplate smsTemplate) throws ServiceException {
        //必填项检验
        if (StringUtils.isBlank(smsTemplate.getTemplateName())){
            throw new ServiceException("模板名称不能为空！");
        }
        if (StringUtils.isBlank(smsTemplate.getTemplateContent())){
            throw new ServiceException("模板内容不能为空！");
        }
        if (smsTemplate.getApproveStatus() == null){
            //审核状态没填默认待审核
            smsTemplate.setApproveStatus(AuditStateEnums.Wait.getCode());
        }
        if (smsTemplate.getSubmitterId() == null){
            throw new ServiceException("模板提交人不能为空！");
        }
        if (StringUtils.isBlank(smsTemplate.getOrgCode())){
            throw new ServiceException("商户编码不能为空！");
        }

        smsTemplate.setCreateDate(new Date());
        this.save(smsTemplate);
    }

    @Override
    @Transactional
    public void updateSmsTemplate(SmsTemplate smsTemplate) {
        this.saveOrUpdate(smsTemplate);
    }

    @Override
    @Transactional
    public void deleteSmsTemplate(SmsTemplate smsTemplate) {
        LambdaQueryWrapper<SmsTemplate> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSmsTemplates(String[] smsTemplateIds) {
        List<String> list = Arrays.asList(smsTemplateIds);
        this.removeByIds(list);
    }

    @Override
    public void deleteSmsTemplates(String[] ids, SmsTemplate smsTemplate) {

        LambdaQueryWrapper<SmsTemplate> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(smsTemplate.getOrgCode())){
            wrapper.eq(SmsTemplate::getOrgCode,smsTemplate.getOrgCode());
        }
        if (smsTemplate.getSubmitterId() != null){
            wrapper.eq(SmsTemplate::getSubmitterId,smsTemplate.getSubmitterId());
        }
        wrapper.in(SmsTemplate::getId,ids);
        this.remove(wrapper);
    }
}
