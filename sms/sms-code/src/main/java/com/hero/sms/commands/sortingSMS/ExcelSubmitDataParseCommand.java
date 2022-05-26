package com.hero.sms.commands.sortingSMS;

import java.util.List;

import org.apache.commons.chain.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.google.common.collect.Lists;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.common.entity.Regexp;
import com.hero.sms.common.utils.AppUtil;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxExcelModel;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.message.SendBoxTypeEnums;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 废弃
 * @author Lenovo
 *
 */
@Slf4j
public class ExcelSubmitDataParseCommand extends BaseCommand {

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute(Context context) throws Exception {
		SendBox sendBox = (SendBox)context.get(OBJ_SENDBOX_ENTITY);
		String smsNumberArea = sendBox.getSmsNumberArea();
		Integer type = sendBox.getType();
		if(type.intValue() == SendBoxTypeEnums.excleSubmit.getCode()) {
			String smsContent = sendBox.getSmsContent();
			String fileUrl = sendBox.getSmsNumbers();
	        try {
	        	/**
	        	 * 异步读取
	        	 * EasyExcel.read(fileUrl, SendBoxExcelModel.class, new ExcelModelListener(smsContent)).sheet().doRead();
	        	 */
	        	
	        	// 这里 取出来的是 ExcelModel实体 的集合
            	List<Object> excelModels = EasyExcel.read(fileUrl).head(SendBoxExcelModel.class).sheet().doReadSync();
	            if(CollectionUtils.isEmpty(excelModels)) {
	            	String errInfo = String.format("发件箱【%s】文件数据为空",sendBox.getSendCode());
		            context.put(STR_ERROR_INFO, errInfo);
		            return true;
	            }
	            int size = excelModels.size();
            	List<SendRecord> sendRecords = Lists.newArrayList();
            	for (int i = 0; i < (size>20000?20000:size);i++) {
            		SendBoxExcelModel model = (SendBoxExcelModel)excelModels.get(i);
            		SendRecord sendRecord = new SendRecord();
        			String smsNumber = model.getColumn0();
        			if(StringUtils.isBlank(smsNumber))continue;
        			smsNumber = smsNumber.trim();
        			if(smsNumberArea.equals(SmsNumberAreaCodeEnums.China.getInArea()) && !AppUtil.match(Regexp.MOBILE_REG, smsNumber)) {
        				continue;
        			}
        			sendRecord.setSmsNumber(smsNumber);
        			String newContent = smsContent.replaceAll("##B##", model.getColumn1()).replaceAll("##C##", model.getColumn2())
        			.replaceAll("##D##", model.getColumn3()).replaceAll("##E##", model.getColumn4())
        			.replaceAll("##F##", model.getColumn5()).replaceAll("##G##", model.getColumn6())
        			.replaceAll("##H##", model.getColumn7()).replaceAll("##I##", model.getColumn8());
        			sendRecord.setSmsContent(newContent);
        			sendRecords.add(sendRecord);
				}
            	context.put(LIST_SEND_RECORD, sendRecords);
	        } catch (Exception e) {
	        	String errInfo = String.format("解析发件箱【%s】文件数据失败",sendBox.getSendCode());
	        	log.error(errInfo,e);
	            context.put(STR_ERROR_INFO, errInfo);
	            return true;
	        }
		}
		return false;
	}
	
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < 20000; i++) {
			sb.append("13086794012").append(",");
		}
		System.out.println(sb);
	}

	@Data
	class ExcelModelListener extends AnalysisEventListener<SendBoxExcelModel>{
		
		private List<SendRecord> sendRecords = Lists.newArrayList();
		private String content;
		
		public ExcelModelListener(String content) {
			super();
			this.content = content;
		}

		@Override
		public void invoke(SendBoxExcelModel model, AnalysisContext arg1) {
			SendRecord sendRecord = new SendRecord();
			String smsNumber = model.getColumn0();
			sendRecord.setSmsNumber(smsNumber);
			String newContent = content.replaceAll("##B##", model.getColumn1()).replaceAll("##C##", model.getColumn2())
			.replaceAll("##D##", model.getColumn3()).replaceAll("##E##", model.getColumn4())
			.replaceAll("##F##", model.getColumn5()).replaceAll("##G##", model.getColumn6())
			.replaceAll("##H##", model.getColumn7()).replaceAll("##I##", model.getColumn8());
			sendRecord.setSmsContent(newContent);
			sendRecords.add(sendRecord);
		}

		@Override
		public void doAfterAllAnalysed(AnalysisContext arg0) {
			
		}
	}
}
