package com.hero.sms.commands.sortingSMS;

import org.apache.commons.chain.Context;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.organization.Organization;
import com.hero.sms.enums.organization.OrgSettlementTypeEnums;
import com.hero.sms.service.impl.organization.OrganizationServiceImpl;
import com.hero.sms.service.organization.IOrganizationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateOrgAmountCommand extends BaseCommand {
    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {
        IOrganizationService organizationService = (IOrganizationService) context.get(OBJ_ORG_SERVICE);
        SendBox sendBox = (SendBox) context.get(OBJ_SENDBOX_ENTITY);
        Integer consumeAmount = sendBox.getConsumeAmount();
        if(consumeAmount == 0) return false;
        if(consumeAmount <0) {
        	String errMsg = String.format("商户【%s】消费金额【%f】异常",sendBox.getOrgCode(),consumeAmount/100.0);
        	context.put(STR_ERROR_INFO, errMsg);
            throw new ServiceException(errMsg);
        }
        Organization organization = organizationService.queryOrgByCodeForUpdate(sendBox.getOrgCode());
        if (organization == null){
        	String errMsg = String.format("商户【%s】不存在",sendBox.getOrgCode());
        	context.put(STR_ERROR_INFO, errMsg);
            throw new ServiceException(errMsg);
        }
        String md5Data = organizationService.getDataMd5(organization);
        if (!organization.getDataMd5().equals(md5Data)){
        	String errMsg = String.format("商户【%s】数据安全校验失败",sendBox.getOrgCode());
        	context.put(STR_ERROR_INFO, errMsg);
        	throw new ServiceException(errMsg);
        }
        if(OrgSettlementTypeEnums.Prepayment.getCode().equals(organization.getSettlementType())) {
        	if(consumeAmount.intValue() > organization.getAvailableAmount().intValue()) {
        		String errMsg = String.format("商户【%s】金额不足",sendBox.getOrgCode());
            	context.put(STR_ERROR_INFO, errMsg);
            	throw new ServiceException(errMsg);
        	}
        }

        /*organization.setCashAmount(organization.getCashAmount()+consumeAmount);
		organization.setAvailableAmount(organization.getAvailableAmount()-consumeAmount);
		organization.setSendSmsTotal(organization.getSendSmsTotal()+sendBox.getSmsCount());
		String dataMd5 = organizationService.getDataMd5(organization);*/
		LambdaUpdateWrapper<Organization> updateOrgWrapper = new LambdaUpdateWrapper<>();
		updateOrgWrapper.setSql("cash_amount=cash_amount+"+consumeAmount);
		updateOrgWrapper.setSql("available_amount=available_amount-"+consumeAmount);
		updateOrgWrapper.setSql("send_sms_total=send_sms_total+"+sendBox.getSmsCount());
		updateOrgWrapper.setSql("data_md5=UPPER(MD5(CONCAT('"+OrganizationServiceImpl.Md5Key+"',Organization_Code,amount,Available_Amount,Cash_Amount,Send_Sms_Total,Md5_Key,Sms_Sign,Bind_IP)))")
		/*updateOrgWrapper.set(Organization::getCashAmount, organization.getCashAmount())
		.set(Organization::getAvailableAmount, organization.getAvailableAmount())
		.set(Organization::getSendSmsTotal, organization.getSendSmsTotal())
		.set(Organization::getDataMd5, dataMd5)*/
		.eq(Organization::getId, organization.getId());
		organizationService.update(updateOrgWrapper);
        return false;
    }
}
