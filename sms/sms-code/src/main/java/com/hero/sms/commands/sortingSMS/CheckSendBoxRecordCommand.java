package com.hero.sms.commands.sortingSMS;

import org.apache.commons.chain.Context;

import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SendBoxRecordCheckinfo;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.service.message.ISendBoxRecordCheckinfoService;

import lombok.extern.slf4j.Slf4j;

/**
 * 发件箱分拣校验
 * 2021-01-13
 * 分拣入库中间校验表
 *
 */
@Slf4j
public class CheckSendBoxRecordCommand extends BaseCommand {
    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {
    	ISendBoxRecordCheckinfoService sendBoxRecordCheckinfoService = (ISendBoxRecordCheckinfoService) context.get(OBJ_SENDBOXRECORDCHECKINFO_SERVICE);
        SendRecord sendRecord = (SendRecord) context.get(OBJ_SAVE_SENDRECORD_ENTITY);
        /**
		 * @begin 2021-01-13
		 * 将同批次同号码入库分拣中间校验表，若入库失败则表示该批次该号码已分拣过，不可重复分拣
		 * 若需再次分拣，则需修改中间校验状态
		 * 
		 */
		String sendBoxRecordCheckinfoSwitch = "ON";
 		Code sendBoxRecordCheckinfoSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","sendBoxRecordCheckinfoSwitch");
 	    if(sendBoxRecordCheckinfoSwitchCode!=null&&!"".equals(sendBoxRecordCheckinfoSwitchCode.getName()))
 	    {
 	    	sendBoxRecordCheckinfoSwitch = sendBoxRecordCheckinfoSwitchCode.getName();
 	    }
 	    if(!"OFF".equals(sendBoxRecordCheckinfoSwitch))
 	    {
 	    	String showRunTimeLogSwitch = "OFF";
	 		Code showRunTimeLogSwitchCode = DatabaseCache.getCodeBySortCodeAndCode("System","showRunTimeLogSwitch");
	 	    if(showRunTimeLogSwitchCode!=null&&!"".equals(showRunTimeLogSwitchCode.getName()))
	 	    {
	 	    	showRunTimeLogSwitch = showRunTimeLogSwitchCode.getName();
	 	    }
	 	    
 	    	String checkSendRecordBeginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 	    	
 	    	try {
 	    		SendBoxRecordCheckinfo sendBoxRecordCheckinfo = new SendBoxRecordCheckinfo();
 	    		sendBoxRecordCheckinfo.setSendCode(sendRecord.getSendCode());
 	    		sendBoxRecordCheckinfo.setSmsNumber(sendRecord.getSmsNumber());
 	    		sendBoxRecordCheckinfo.setState(1);
 	    		sendBoxRecordCheckinfoService.createSendBoxRecordCheckinfo(sendBoxRecordCheckinfo);
 			} catch (Exception e) {
 				log.warn(String.format("【重复分拣】商户号：【%s】批次号：【%s】该号码【%s】已提交过分拣",sendRecord.getOrgCode(),sendRecord.getSendCode(),sendRecord.getSmsNumber()));
 				String msg = "本批次该号码重复分拣，默认分拣失败（不计费）";
				context.put(STR_ERROR_INFO, msg);
				context.put(STR_NOTIFY_MSG, "分拣入库中间校验表失败");
				return true;
 			}
 	    	
 	    	if("ON".equals(showRunTimeLogSwitch))
	 	    {
 	    		String checkSendRecordEndTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_2);
 	    		long checkSendRecordRuntime = DateUtil.getTime(checkSendRecordBeginTime, checkSendRecordEndTime,DateUtil.Y_M_D_H_M_S_S_2);
	 	    	log.error(String.format("【分拣入中间唯一校验表耗时】【%s】【%s】【%s】:开始时间【%s】返回时间【%s】耗时【%s毫秒】",sendRecord.getSendCode(),sendRecord.getSmsNumber(),sendRecord.getChannelId(),checkSendRecordBeginTime,checkSendRecordEndTime,String.valueOf(checkSendRecordRuntime)));
	 	    }
 	    }
		/**
		 * @end
		 */
        return false;
    }
}
