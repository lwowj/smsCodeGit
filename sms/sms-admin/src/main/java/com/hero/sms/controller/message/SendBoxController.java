package com.hero.sms.controller.message;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.annotation.ControllerEndpoint;
import com.hero.sms.common.controller.BaseController;
import com.hero.sms.common.entity.FebsConstant;
import com.hero.sms.common.entity.FebsResponse;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.FebsUtil;
import com.hero.sms.entity.message.SendBox;
import com.hero.sms.entity.message.SendBoxExt;
import com.hero.sms.entity.message.SendBoxQuery;
import com.hero.sms.enums.common.AuditStateEnums;
import com.hero.sms.enums.message.SendBoxTypeEnums;
import com.hero.sms.service.message.IReportService;
import com.hero.sms.service.message.ISendBoxService;
import com.hero.sms.utils.FileDownLoadUtil;
import com.wuwenze.poi.ExcelKit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发件箱 Controller
 *
 * @author Administrator
 * @date 2020-03-12 00:20:42
 */
@Slf4j
@Validated
@Controller
@RequestMapping("sendBox")
public class SendBoxController extends BaseController {

    @Autowired
    private ISendBoxService sendBoxService;
    @Autowired
    private IReportService reportService;


    @ControllerEndpoint(operation = "统计数据", exceptionMessage = "统计数据失败")
    @GetMapping("statistic")
    @ResponseBody
    @RequiresPermissions("sendBox:statistic")
    public FebsResponse statistic(SendBoxQuery sendBox) {
        return new FebsResponse().success().data(reportService.statisticSendBoxInfo(sendBox));
    }

    @GetMapping(FebsConstant.VIEW_PREFIX + "sendBox")
    public String sendBoxIndex(){
        return FebsUtil.view("sendBox/sendBox");
    }

    @GetMapping
    @ResponseBody
    @RequiresPermissions("sendBox:list")
    public FebsResponse getAllSendBoxs(SendBoxQuery sendBox) {
        return new FebsResponse().success().data(sendBoxService.findSendBoxs(sendBox));
    }

    @GetMapping("list")
    @ResponseBody
    @RequiresPermissions("sendBox:list")
    public FebsResponse sendBoxList(QueryRequest request, SendBoxQuery sendBox) {

        IPage<SendBox> datas = this.sendBoxService.findSendBoxs(request, sendBox);
        List<SendBoxExt> list = new ArrayList<>();
        datas.getRecords().stream().forEach(item -> {
            SendBoxExt sendBoxExt = new SendBoxExt();
            BeanUtils.copyProperties(item,sendBoxExt);

            String orgName = DatabaseCache.getOrgNameByOrgcode(item.getOrgCode());
            sendBoxExt.setOrgName(orgName);
            //短信详情
            String smsInfo = String.format("字数:%s 有效短信数:%s 号码数:%s",item.getSmsWords(),item.getSmsCount(),item.getNumberCount());
            sendBoxExt.setSmsInfo(smsInfo);

            //短信金额详情
            BigDecimal divideNum = new BigDecimal(100);
            BigDecimal consumeAmout = new BigDecimal(item.getConsumeAmount()!=null?item.getConsumeAmount():0).divide(divideNum);
            BigDecimal channelCostAmout = new BigDecimal(item.getChannelCostAmount()!=null?item.getChannelCostAmount():0).divide(divideNum );
            BigDecimal agentIncomeAmout = new BigDecimal(item.getAgentIncomeAmount()!=null?item.getAgentIncomeAmount():0).divide(divideNum);
            BigDecimal incomeAmout = new BigDecimal(item.getIncomeAmount()!=null?item.getIncomeAmount():0).divide(divideNum);
            BigDecimal upAgentIncomeAmout = new BigDecimal(item.getUpAgentIncomeAmount() != null?item.getUpAgentIncomeAmount():0).divide(divideNum);
            String amountInfo = String.format("消费金额:%s元 通道成本:%s元 代理收益:%s元 上级代理收益:%s元 平台收益:%s元"
                        ,consumeAmout,channelCostAmout,agentIncomeAmout,upAgentIncomeAmout ,incomeAmout);
            sendBoxExt.setAmountInfo(amountInfo);

            list.add(sendBoxExt);
        });

        Map<String, Object> dataTable = getDataTable(datas.getTotal(),list);
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
        return new FebsResponse().success().data(this.sendBoxService.getSmsNumbersByID(sendBoxId));
    }

    @ControllerEndpoint(operation = "批量审核发件箱", exceptionMessage = "批量审核失败")
    @GetMapping("audit/{stateTag}/{sendBoxIds}")
    @ResponseBody
    @RequiresPermissions("sendBox:audit")
    public FebsResponse auditSendBox(@PathVariable("stateTag") String stateTag
            ,@PathVariable("sendBoxIds") String sendBoxIds
            ,@RequestParam("refuseCause") String refuseCause) {
        if (StringUtils.isBlank(stateTag) || StringUtils.isBlank(sendBoxIds)){
            return new FebsResponse().fail().message("缺少必要参数，操作失败");
        }
        if ("pass".equals(stateTag)){
            this.sendBoxService.audit(sendBoxIds,AuditStateEnums.Pass.getCode().intValue(),null);
            
            List<Long> ids = Arrays.stream(sendBoxIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
            SendBoxQuery query = new SendBoxQuery();
            query.setIds(ids);
            query.setIsTiming(false);
            List<SendBox> sendBoxs = sendBoxService.findSendBoxs(query);
            sendBoxService.splitRecord(sendBoxs);
        }else if ("refuse".equals(stateTag)){
            this.sendBoxService.audit(sendBoxIds,AuditStateEnums.NoPass.getCode().intValue(),refuseCause);
        }else {
            return new FebsResponse().fail();
        }
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "删除SendBox", exceptionMessage = "删除SendBox失败")
    @GetMapping("delete")
    @ResponseBody
    @RequiresPermissions("sendBox:delete")
    public FebsResponse deleteSendBox(SendBox sendBox) {
        this.sendBoxService.deleteSendBox(sendBox);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "批量删除SendBox", exceptionMessage = "批量删除SendBox失败")
    @GetMapping("delete/{sendBoxIds}")
    @ResponseBody
    @RequiresPermissions("sendBox:delete")
    public FebsResponse deleteSendBox(@NotBlank(message = "{required}") @PathVariable String sendBoxIds) {
        String[] ids = sendBoxIds.split(StringPool.COMMA);
        this.sendBoxService.deleteSendBoxs(ids);
        return new FebsResponse().success();
    }

    @ControllerEndpoint(operation = "修改SendBox", exceptionMessage = "修改SendBox失败")
    @PostMapping("update")
    @ResponseBody
    @RequiresPermissions("sendBox:update")
    public FebsResponse updateSendBox(SendBox sendBox) {
        this.sendBoxService.updateSendBox(sendBox);
        return new FebsResponse().success();
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
    public void downLoadSmsNumbersExcel(HttpServletResponse response,@PathVariable Integer id) throws FileNotFoundException {

        SendBox sendBox = this.sendBoxService.getById(id);
        if (!(sendBox.getType().intValue() == SendBoxTypeEnums.excleSubmit.getCode())){
            log.error("发件箱类型非excel模板提交，下载失败！");
            return;
        }
        String smsNumbers = sendBox.getSmsNumbers();
        String prefixPath = DatabaseCache.getCodeBySortCodeAndCode("System", "sendBoxExcelPath").getName();
        if (!smsNumbers.contains(prefixPath)){
            log.error("文件不存在，下载失败!");
        }
        download(response,smsNumbers);
    }


    @ControllerEndpoint(operation = "修改SendBox", exceptionMessage = "导出Excel失败")
    @PostMapping("excel")
    @ResponseBody
    @RequiresPermissions("sendBox:export")
    public void export(QueryRequest queryRequest, SendBoxQuery sendBox, HttpServletResponse response) {
        List<SendBox> sendBoxs = this.sendBoxService.findSendBoxs(queryRequest, sendBox).getRecords();
        ExcelKit.$Export(SendBox.class, response).downXlsx(sendBoxs, false);
    }

    private FebsResponse download(HttpServletResponse response, String path) {
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        File file = null;
        try {
//        	/**
//	    	 * @begin
//	    	 * 2020-12-22
//	    	 * 替换商户号码execl文件读取地址，因无法直接访问商户服务器，故需转换地址读取
//	    	 */
//			String sendBoxExcelPathSwitch = "OFF";
//			Code sendBoxExcelPathSwitchCode =  DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","sendBoxExcelPathSwitch");
//	    	if(sendBoxExcelPathSwitchCode!=null&&sendBoxExcelPathSwitchCode.getName()!=null&&!"".equals(sendBoxExcelPathSwitchCode.getName()))
//	    	{
//	    		sendBoxExcelPathSwitch = sendBoxExcelPathSwitchCode.getName();
//	    	}
//	    	if("ON".equals(sendBoxExcelPathSwitch))
//	    	{
//	    		String tempFileUrl = path;
//				String fileFullName = path;
//	    		 //将文件保存  并把文件路径保存到记录中
//		        String savePath = DatabaseCache.getCodeBySortCodeAndCode("System", "sendBoxExcelPath").getName();
//		      //拼装商户服务器的IP地址
//		    	Code merch_img_ip_code =  DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","merch_img_ip");
//		    	String merch_img_ip = "";
//		    	if(merch_img_ip_code!=null&&merch_img_ip_code.getName()!=null&&!"".equals(merch_img_ip_code.getName()))
//		    	{
//		    		merch_img_ip = merch_img_ip_code.getName();
//		    	}
//		    	//拼装商户服务器的文件路径
//		    	if(merch_img_ip!=null&&!"".equals(merch_img_ip))
//		    	{
//		    		tempFileUrl = merch_img_ip+"/"+tempFileUrl.replace(savePath, "execlflie/");
//		    	}
//		    	//判断本服务器上是否已经有该文件存在,若已存在，则不做处理，直接读取；不存在则需先复制下载到本服务器上
//		    	if(!FileUtil.fileIsExists(path))
//		    	{
//		    		//截取文件名
//		    		fileFullName = fileFullName.replace(savePath, "");
//		    		try {
//		    			//将商户服务器上的文件，复制下载到本服务器相同路径下
//		    			FileUtil.downloadNet(tempFileUrl, savePath,fileFullName.trim());
//					} catch (Exception e) {
//						// TODO: handle exception
//						e.printStackTrace();
//					}
//		    	}
//	    	}
//	    	/**
//	    	 * @end 
//	    	 */
	    	
        	/**
             * @begin
             * 2021-04-21
             * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
             * 【说明】：这里需要新增校验path 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
             * 【步骤】：
             * 1、使用文件工具判断文件地址的文件是否存在于本机上
             * 2、获取字典中负载服务器的IP地址
             * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
             */
        	FileDownLoadUtil.downLoadSendBoxExcelFile(path);
            /**
             * @end
             */
        	
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
}
