package com.hero.sms.controller.message;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.google.common.collect.Lists;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.message.SmsTemplate;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.service.message.ISmsTemplateService;

import lombok.extern.slf4j.Slf4j;

/**
 * 短信模板表 Controller
 *
 * @author Administrator
 * @date 2020-03-11 20:08:30
 */
@Slf4j
@Validated
@Controller
@RequestMapping("smsTemplate")
public class SmsTemplateController extends BaseController {

    @Autowired
    private ISmsTemplateService smsTemplateService;

    @ControllerEndpoint( operation = "短信模板列表")
    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("smsTemplate:list")
    public FebsResponse smsTemplateList(QueryRequest request, SmsTemplate smsTemplate) {
        OrganizationUserExt user = super.getCurrentUser();
        //目前只限制  可以看商户的所有记录
        smsTemplate.setOrgCode(user.getOrganizationCode());
        //smsTemplate.setSubmitterId(user.getId());

        Map<String, Object> dataTable = getDataTable(this.smsTemplateService.findSmsTemplates(request, smsTemplate));
        return new FebsResponse().success().data(dataTable);
    }

    @ControllerEndpoint(operation = "新增SmsTemplate", exceptionMessage = "新增SmsTemplate失败")
    @PostMapping
    @ResponseBody
    @RequiresPermissions("smsTemplate:add")
    public FebsResponse addSmsTemplate(@Valid SmsTemplate smsTemplate) {

        //前端填写的信息
        SmsTemplate saveTemplate = new SmsTemplate();
        saveTemplate.setTemplateName(smsTemplate.getTemplateName());
        saveTemplate.setTemplateContent(smsTemplate.getTemplateContent());
        saveTemplate.setDescription(smsTemplate.getDescription());

        OrganizationUserExt user = super.getCurrentUser();
        //初始化商户用户等信息
        saveTemplate.setSubmitterId(user.getId());
        saveTemplate.setSubmitterName(user.getUserName());
        saveTemplate.setOrgCode(user.getOrganizationCode());
        saveTemplate.setOrgName(user.getOrganization().getOrganizationName());
        saveTemplate.setApproveStatus(AuditStateEnums.Wait.getCode());

        try {
            this.smsTemplateService.createSmsTemplate(saveTemplate);
        } catch (ServiceException e) {
            return new FebsResponse().message(e.getMessage()).fail();
        }
        return new FebsResponse().success();
    }


    /**
     * 批量删除
     * @param smsTemplateIds
     * @return
     */
    @ControllerEndpoint(operation = "批量删除SmsTemplate", exceptionMessage = "批量删除SmsTemplate失败")
    @GetMapping("delete/{smsTemplateIds}")
    @ResponseBody
    @RequiresPermissions("smsTemplate:delete")
    public FebsResponse deleteSmsTemplate(@NotBlank(message = "{required}") @PathVariable String smsTemplateIds) {

        //限制只能删除自己商户的模板
        OrganizationUserExt user = super.getCurrentUser();
        SmsTemplate deleteParams = new SmsTemplate();
        deleteParams.setOrgCode(user.getOrganizationCode());
        //deleteParams.setSubmitterId(user.getId());

        String[] ids = smsTemplateIds.split(StringPool.COMMA);
        this.smsTemplateService.deleteSmsTemplates(ids,deleteParams);
        return new FebsResponse().success();
    }

    /**
     * 修改模板
     * @param smsTemplate
     * @return
     */
    @ControllerEndpoint(operation = "修改SmsTemplate", exceptionMessage = "修改SmsTemplate失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("smsTemplate:update")
    public FebsResponse updateSmsTemplate(SmsTemplate smsTemplate) {

        if (smsTemplate.getId() == null){
            return new FebsResponse().message("必要参数缺失,更新失败！").fail();
        }
        OrganizationUserExt user = super.getCurrentUser();

        //先查出模板
        SmsTemplate existTemplate = this.smsTemplateService.getById(smsTemplate.getId());

        //不是自己商户的模板  不能修改
        if (!existTemplate.getOrgCode().equals(user.getOrganizationCode())){
            return new FebsResponse().message("您无权修改此模板！").fail();
        }

        //部分字段更新
        existTemplate.setTemplateName(smsTemplate.getTemplateName());
        existTemplate.setTemplateContent(smsTemplate.getTemplateContent());
        existTemplate.setDescription(smsTemplate.getDescription());

        //模板内容被更改  审核状态重置成待审核
        if (existTemplate.getTemplateContent().equals(smsTemplate.getTemplateContent())){
            existTemplate.setApproveStatus(AuditStateEnums.Wait.getCode());
        }


        this.smsTemplateService.updateSmsTemplate(existTemplate);
        return new FebsResponse().success();
    }


    @PostMapping("import")
    @RequiresPermissions("smsTemplate:import")
    @ControllerEndpoint(exceptionMessage = "导入模板失败",operation = "导入模板")
    @ResponseBody
    public FebsResponse importExcels(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new FebsException("导入数据为空");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.endsWith(filename, ".txt")) {
            throw new FebsException("只支持.txt类型文件导入");
        }
        final List<SmsTemplate> data = Lists.newArrayList();
        final List<Map<String, Object>> error = Lists.newArrayList();
        OrganizationUserExt user = super.getCurrentUser();
        StringBuffer result = new StringBuffer();
        InputStreamReader inputStream = null;
        try {
            String code = getFilecharset(file.getInputStream());
            inputStream = new InputStreamReader(file.getInputStream(),code);
            BufferedReader reader = new BufferedReader(inputStream);
            String line = null;
            while ((line = reader.readLine()) != null) {

                SmsTemplate smsTemplate ;
               //读行
                if (line.contains("#")){
                    smsTemplate = new SmsTemplate();
                    String[] items = line.split("#");
                    smsTemplate.setTemplateName(items[0]);
                    smsTemplate.setTemplateContent(items[1]);

                    //初始化商户用户等信息
                    smsTemplate.setSubmitterId(user.getId());
                    smsTemplate.setSubmitterName(user.getUserName());
                    smsTemplate.setOrgCode(user.getOrganizationCode());
                    smsTemplate.setOrgName(user.getOrganization().getOrganizationName());
                    smsTemplate.setApproveStatus(AuditStateEnums.Wait.getCode());
                    smsTemplate.setCreateDate(new Date());
                    data.add(smsTemplate);
                }else {

                }
            }
        }catch (Exception e){
            return new FebsResponse().message("文件导入失败").fail();
        } finally{
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (data.size()>0){
            this.smsTemplateService.saveBatch(data);
        }
        return new FebsResponse().success().message("成功导入" + data.size() + "条模板");
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



 /*   public static String resolveCode(InputStream inputStream) throws Exception {
//      String filePath = "D:/article.txt"; //[-76, -85, -71]  ANSI
//      String filePath = "D:/article111.txt";  //[-2, -1, 79] unicode big endian
//      String filePath = "D:/article222.txt";  //[-1, -2, 32]  unicode
//      String filePath = "D:/article333.txt";  //[-17, -69, -65] UTF-8
        byte[] head = new byte[3];
        inputStream.read(head);
        String code = "gb2312";  //或GBK
        if (head[0] == -1 && head[1] == -2 )
            code = "UTF-16";
        else if (head[0] == -2 && head[1] == -1 )
            code = "Unicode";
        else if(head[0]==-17 && head[1]==-69 && head[2] ==-65)
            code = "UTF-8";

        inputStream.close();

        System.out.println(code);
        return code;
    }*/

}
