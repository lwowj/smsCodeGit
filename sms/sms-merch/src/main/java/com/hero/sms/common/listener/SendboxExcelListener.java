package com.hero.sms.common.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.SendBoxExcelModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class SendboxExcelListener extends AnalysisEventListener<SendBoxExcelModel> {

    private String smsContent;
    private Integer numberLimit = 20;
    private List<Integer> errorParmRow;
    private Set<String> paramColumn;
    private List<String> errorPhoneNum;

    public SendboxExcelListener(String smsContent) {
        this.smsContent = smsContent;
        errorParmRow = Lists.newArrayList();
        errorPhoneNum = Lists.newArrayList();
        matchParamColumn();
        //字典限制优先级 > 默认值
        Code code = DatabaseCache.getCodeBySortCodeAndCode("System","numberLengthLimit");
        if (code != null){
            try {
                numberLimit = Integer.valueOf(code.getName());
            } catch (NumberFormatException e) {
                log.error("发送短信 -> 号码最大长度限制字典值未生效，使用默认值20",e.getMessage());
            }
        }
    }

    public List<Integer> getErrorParmRow() {
        return errorParmRow;
    }

    public List<String> getErrorPhoneNum() {
        return errorPhoneNum;
    }

    public Integer getNumberLimit() {
        return numberLimit;
    }

    public Set<String> getParamColumn() {
        return paramColumn;
    }

    @Override
    public void invoke(SendBoxExcelModel sendBoxExcelModel, AnalysisContext analysisContext) {
        if (StringUtils.isBlank(smsContent)) return;
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();

        if (!checkPhoneNum(sendBoxExcelModel)){
            errorPhoneNum.add(sendBoxExcelModel.getColumn0());
        }
        if (!checkParam(sendBoxExcelModel)){
            //rowIndex  从0开始
            errorParmRow.add(rowIndex+1);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
    //检查号码
    private boolean checkPhoneNum(SendBoxExcelModel model){
        String phoneNum = model.getColumn0();
        if (phoneNum.length() > numberLimit){
            return false;
        }
        return true;
    }
    //检查参数
    private boolean checkParam(SendBoxExcelModel model){
        try {
            smsContent.replaceAll("##B##", model.getColumn1()).replaceAll("##C##", model.getColumn2())
                    .replaceAll("##D##", model.getColumn3()).replaceAll("##E##", model.getColumn4())
                    .replaceAll("##F##", model.getColumn5()).replaceAll("##G##", model.getColumn6())
                    .replaceAll("##H##", model.getColumn7()).replaceAll("##I##", model.getColumn8());
        } catch (Exception e) {
           return false;
        }
        return true;
    }

    private void matchParamColumn(){
        Map map = Maps.newHashMap();
        String regex = "\\#\\#(.*?)\\#\\#";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(this.smsContent);
        while (matcher.find()) {
            String matchStr = matcher.group(1);
            map.put(matchStr,matcher.group());
        }
        paramColumn = map.keySet();
    }


}
