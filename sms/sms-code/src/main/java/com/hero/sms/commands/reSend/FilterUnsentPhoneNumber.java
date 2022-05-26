package com.hero.sms.commands.reSend;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.entity.message.BaseSend;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxExcelModel;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.message.SendBoxTypeEnums;
import com.hero.sms.enums.message.SendRecordStateEnums;
import com.hero.sms.service.message.ISendRecordService;
import com.hero.sms.utils.RandomUtil;
import org.apache.commons.chain.Context;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FilterUnsentPhoneNumber extends BaseCommand {
    @Override
    public boolean execute(Context context) throws Exception {
        ISendRecordService sendRecordService = (ISendRecordService) context.get(OBJ_SENDRECORD_SERVICE);
        SendBox oldSendBox = (SendBox) context.get(OBJ_SENDBOX_ENTITY);
        SendBox saveSendBox = (SendBox) context.get(OBJ_SAVE_SENDBOX_ENTITY);
        LambdaQueryWrapper<SendRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SendRecord::getSmsNumber).eq(BaseSend::getSendCode,oldSendBox.getSendCode())
                .ne(SendRecord::getState, SendRecordStateEnums.SortingFail.getCode());
        List<SendRecord> list = sendRecordService.list(queryWrapper);
        List<String> excludeList = list.stream().map(item -> item.getSmsNumber()).collect(Collectors.toList());
        Integer type = oldSendBox.getType();
        //过滤掉除分拣失败之外的那部分，剩下的就是要重新发送的手机号码集合。
        //即待发送，已发送，发送失败，接收成功，接收失败的，不重发
        String smsNumbers = oldSendBox.getSmsNumbers();
        if (type.intValue() == SendBoxTypeEnums.formSubmit.getCode()){
            List<String> smsNumberList = new ArrayList<>();
            Collections.addAll(smsNumberList,smsNumbers.split(","));
            smsNumberList.removeAll(excludeList);
            context.put(LIST_SAVE_MODEL,smsNumberList);
            saveSendBox.setSmsNumbers(Joiner.on(",").join(smsNumberList));
            saveSendBox.setNumberCount(smsNumberList.size());
        }else if (type.intValue() == SendBoxTypeEnums.excleSubmit.getCode()){
            File file = new File(smsNumbers);
            InputStream inputStream = new FileInputStream(file);
            List<Object> excelModels = Lists.newArrayList();
            //ArrayList 转 HashSet提高效率
            Set<String> excludeSet = new HashSet<>(excludeList);
            EasyExcel.read(inputStream, new AnalysisEventListener<SendBoxExcelModel>() {
                @Override
                public void invoke(SendBoxExcelModel model, AnalysisContext analysisContext) {
                    String smsNumber = model.getColumn0();
                    if (!excludeSet.contains(smsNumber)){
                        excelModels.add(model);
                    }
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                    //EXCEL全部解析完成后执行
                    context.put(LIST_SAVE_MODEL,excelModels);
                    saveSendBox.setNumberCount(excelModels.size());
                    //生成号码文件
                    String filePath = rename(smsNumbers);
                    EasyExcel.write(filePath,SendBoxExcelModel.class).sheet("重发记录").doWrite(excelModels);
                    saveSendBox.setSmsNumbers(filePath);
                }
            }).head(SendBoxExcelModel.class).sheet().doReadSync();

        }else {
            context.put(STR_ERROR_INFO,"发件箱类型错误！");
            return true;
        }

        if (saveSendBox.getNumberCount() < 1){
            context.put(STR_ERROR_INFO,"该发件箱无符合重发条件的记录");
            return true;
        }
        return false;
    }


    private String rename(String filePath){
        int i = filePath.lastIndexOf(".");
        String str = filePath.substring(i);
        return filePath.replace(str,"_sub"+str);

    }
}
