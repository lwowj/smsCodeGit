package com.hero.sms.controller.export;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FileUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.common.ExportRecord;
import com.hero.sms.entity.common.ExportRecordQuery;
import com.hero.sms.enums.common.UserTypeEnums;
import com.hero.sms.service.common.IExportRecordService;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.system.entity.User;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 导出文件记录表 Controller
 *
 * @author Administrator
 * @date 2020-05-26 11:31:36
 */
@Slf4j
@Validated
@Controller
@RequestMapping("exportRecord")
public class ExportRecordController extends BaseController {

    @Autowired
    private IExportRecordService exportRecordService;


    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("exportRecord:list")
    public FebsResponse exportRecordList(QueryRequest request, ExportRecordQuery exportRecord) {
        User user = getCurrentUser();
        if (user == null || user.getId() == null){
            return new FebsResponse().fail().message("非法请求!");
        }
        //限制只能查询自己导出的文件记录
        exportRecord.setUserId(user.getId().intValue());
        exportRecord.setUserType(UserTypeEnums.Admin.getCode());
        Map<String, Object> dataTable = getDataTable(this.exportRecordService.findExportRecords(request, exportRecord));
        return new FebsResponse().success().data(dataTable);
    }

    @GetMapping("export/{id}")
    @ResponseBody
    @RequiresPermissions("exportRecord:export")
    public void export(@PathVariable Integer id, HttpServletResponse response) throws Exception {
        User user = getCurrentUser();
        if (user == null || user.getId() == null){
            log.warn("导出下载失败，非法请求！");
            return ;
        }

        ExportRecord exportRecord = this.exportRecordService.getById(id);
        if (exportRecord == null || user.getId().intValue() != exportRecord.getUserId().intValue()){
            log.info("导出下载失败，文件不存在！");
            return;
        }

        Code code = DatabaseCache.getCodeBySortCodeAndCode("System","adminExportPath");
        String filePath = code.getName() + exportRecord.getFilename();
        FileUtil.download(filePath, exportRecord.getFilename(),FebsConstant.VALID_FILE_TYPE, false, response);
    }

}
