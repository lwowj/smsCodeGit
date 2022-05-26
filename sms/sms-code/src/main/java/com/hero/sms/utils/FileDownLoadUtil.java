package com.hero.sms.utils;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.utils.FileUtil;
import com.hero.sms.entity.common.Code;

/**
 * 文件下载工具
 * @author payBoo
 *
 */
public class FileDownLoadUtil 
{
	public static void main(String[] args) {
		try {
			String targetFileUrl = "http://192.168.1.1:8181/res/agent/guangsu1.png";
			String savePath = "C:\\work\\febs\\agent"; 
			String fileFullName = "guangsu1.png";
//			System.out.println(FileUtil.fileIsExists(logoPath));
			try {
    			//将代理服务器上的文件，复制下载到本服务器相同路径下
    			int i = FileUtil.downloadNet(targetFileUrl, savePath, fileFullName.trim());
    			System.out.println(i);
			}
			catch (Exception e) {
    			// TODO: handle exception
    		}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * 2021-04-21
	 * 代理logo图片下载方法
	 * 
	 * @param SystemLogoUrl		保存在库中的logo地址
	 * @return 
	 * 0：文件不存在
	 * 1；文件存在
	 * -1：文件下载失败
	 */
	public static int downLoadAgentLogo(String SystemLogoUrl)
	{
		int fileIsExists = 1;
		String imgPathSwitch = "OFF";
		Code imgPathSwitchCode =  DatabaseCache.getCodeBySortCodeAndCode("AgentPropertyConfig","imgPathSwitch");
    	if(imgPathSwitchCode!=null&&imgPathSwitchCode.getName()!=null&&!"".equals(imgPathSwitchCode.getName()))
    	{
    		imgPathSwitch = imgPathSwitchCode.getName();
    	}
    	if("ON".equals(imgPathSwitch))
    	{
    		if(StringUtil.isNotBlank(SystemLogoUrl))
    		{
    			/*
    			 * logo库存地址
    			 * 如：/res/agent/guangsu.png
    			 */
    			String logoPath = SystemLogoUrl;
    			/*
    			 * logo图片文件服务器存储目录地址
    			  * 如：/home/sms/logo
    			 * 该路径下/agent目录需要自行补全
    			 */
		        String savePath = DatabaseCache.getCodeBySortCodeAndCode("System", "logoFilePath").getName();
		        /*
		         * 将logo库存地址转换成服务器绝对目录地址
		     * /res路径项目识别转换路径串，系统自动转换为 /home/sms/logo
		         * 转换后：如：/home/sms/logo/agent/guangsu.png
		         */
				logoPath = logoPath.replace("/res",savePath);
				/*
				 * 判断本服务器上是否已经有该文件存在,若已存在，则不做处理，直接读取；不存在则需先复制下载到本服务器上
				 * 使用logo文件服务器绝对目录地址，校验本机是否存在该文件
				 */
		    	if(!FileUtil.fileIsExists(logoPath))
		    	{
		    		/*
	    			 * logo库存地址
	    			 * 如：/res/agent/guangsu.png
	    			 */
		    		String targetFileUrl = SystemLogoUrl;
		    		String fileFullName = SystemLogoUrl;
		    		
		    		/*
		    		 * 获取代理服务器的所有IP地址 ： 如： http://192.168.1.1:8181,http://192.168.1.2:8181
		    		 */
			    	Code agent_img_ip_code =  DatabaseCache.getCodeBySortCodeAndCode("AgentPropertyConfig","agent_img_ip");
			    	String agent_img_ip = "";
			    	if(agent_img_ip_code!=null&&agent_img_ip_code.getName()!=null&&!"".equals(agent_img_ip_code.getName()))
			    	{
			    		//  http://192.168.1.1:8181,http://192.168.1.2:8181
			    		agent_img_ip = agent_img_ip_code.getName();
			    	}
			    	if(agent_img_ip!=null&&!"".equals(agent_img_ip))
			    	{
			    		/*
			    		 * 拼装代理服务器的logo文件路径
			    		 * 补全agent目录
			    		 * 	/home/sms/logo ====>>>> /home/sms/logo/agent
			    		 */
			    		savePath = savePath + "/agent";
			    		/*
			    		 * 截取文件名
			    		 * 	/res/agent/guangsu.png ====>>>> guangsu.png
			    		 */
			    		fileFullName = fileFullName.replace("/res/agent/","");
			    		String[] agentImgIps = agent_img_ip.split(StringPool.COMMA);
			    		for (int i = 0; i < agentImgIps.length; i++) 
			    		{
			    			/*
			    			 * 拼装logo文件可访问路径地址
			    			 * http://192.168.1.1:8181/res/agent/guangsu.png
			    			 */
				    		targetFileUrl = agentImgIps[i] + targetFileUrl;
				    		try 
				    		{
				    			/*
				    			 * 将logo访问地址的文件，复制下载到本机服务器相同目录路径下
								 * @param targetFileUrl		可访问文件地址：如：http://192.168.1.1:8181/res/agent/guangsu.png
								 * @param savePath			服务器文件存放目录：如：/home/sms/logo/agent/
								 * @param fileFullName		文件名：guangsu.png 
								 * 
								 * @return
								 * 返回0 ，表示targetFileUrl 的文件不存在
								 * 返回1，表示复制下载成功
								 * 返回-1，表示复制下载失败
				    			 */
				    			int isExists = FileUtil.downloadNet(targetFileUrl, savePath, fileFullName.trim());
				    			if(isExists==1)
				    			{
				    				fileIsExists = 1;
				    				break;
				    			}
				    			fileIsExists = isExists;
				    		}
				    		catch (Exception e) 
				    		{
				    			// TODO: handle exception
				    			e.printStackTrace();
				    		}
						}
			    	}
		    	}
    		}
    	}
    	return fileIsExists;
	}
	
	
	/**
	 * 2021-04-21
	 * 发件箱execl格式号码文件下载方法
	 * 
	 * @param sendBoxExcelFileUrl		保存在库中的execl号码文件地址
	 * @return 
	 * 0：文件不存在
	 * 1；文件存在
	 * -1：文件下载失败
	 * 
	 */
	public static int downLoadSendBoxExcelFile(String sendBoxExcelFileUrl)
	{
		int fileIsExists = 1;
		String sendBoxExcelPathSwitch = "OFF";
		Code sendBoxExcelPathSwitchCode =  DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","sendBoxExcelPathSwitch");
    	if(sendBoxExcelPathSwitchCode!=null&&sendBoxExcelPathSwitchCode.getName()!=null&&!"".equals(sendBoxExcelPathSwitchCode.getName()))
    	{
    		sendBoxExcelPathSwitch = sendBoxExcelPathSwitchCode.getName();
    	}
    	if("ON".equals(sendBoxExcelPathSwitch))
    	{
    		if(StringUtil.isNotBlank(sendBoxExcelFileUrl))
    		{
    			/*
    			  * 判断本服务器上是否已经有该文件存在,若已存在，则不做处理，直接读取；不存在则需先复制下载到本服务器上
				  * 使用execl号码文件服务器绝对目录地址，校验本机是否存在该文件
				  * sendBoxExcelFileUrl格式： /home/sms/upload/20210306182648BE1FHFF7VOYQEHNCCK.xlsx
    			 */
		    	if(!FileUtil.fileIsExists(sendBoxExcelFileUrl))
		    	{
		    		String targetFileUrl = sendBoxExcelFileUrl;
					String fileFullName = sendBoxExcelFileUrl;
					/*
	    			 * execl号码文件服务器存储目录地址
	    			  * 如：/home/sms/upload/
	    			 */
			        String savePath = DatabaseCache.getCodeBySortCodeAndCode("System", "sendBoxExcelPath").getName();
			        /*
			         * 获取商户服务器的所有IP地址 ： 如： http://192.168.1.1:9090,http://192.168.1.2:9090
			         */
			    	Code merch_img_ip_code =  DatabaseCache.getCodeBySortCodeAndCode("OrgPropertyConfig","merch_img_ip");
			    	String merch_img_ip = "";
			    	if(merch_img_ip_code!=null&&merch_img_ip_code.getName()!=null&&!"".equals(merch_img_ip_code.getName()))
			    	{
			    		merch_img_ip = merch_img_ip_code.getName();
			    	}
			    	if(merch_img_ip!=null&&!"".equals(merch_img_ip))
			    	{
			    		/*
			    		 * 截取文件名
			    		 * 	/home/sms/upload/20210306182648BE1FHFF7VOYQEHNCCK.xlsx ====>>>>  20210306182648BE1FHFF7VOYQEHNCCK.xlsx
			    		 */
			    		fileFullName = fileFullName.replace(savePath, "");
			    		String[] merchImgIps = merch_img_ip.split(StringPool.COMMA);
			    		for (int i = 0; i < merchImgIps.length; i++) 
			    		{
			    			/*
			    			 * 拼装execl号码文件可访问路径地址
			    			 * /execlflie/路径项目识别转换路径串，系统自动转换为 /home/sms/
			    			 * /home/sms/upload/20210306182648BE1FHFF7VOYQEHNCCK.xlsx ====>>>> http://192.168.1.1:9090/execlflie/20210306182648BE1FHFF7VOYQEHNCCK.xlsx
			    			  *  转换后：如： http://192.168.1.1:9090/execlflie/20210306182648BE1FHFF7VOYQEHNCCK.xlsx
			    			 */
			    			targetFileUrl = merchImgIps[i]+targetFileUrl.replace(savePath, "/execlflie/");
				    		try 
				    		{
				    			/*
				    			 * 将execl文件访问地址的文件，复制下载到本机服务器相同目录路径下
								 * @param excelFilePath		execl号码文件服务器地址：如：/home/sms/upload/20210306182648BE1FHFF7VOYQEHNCCK.xlsx
								 * @param targetFileUrl		可访问execl号码文件地址：如：http://192.168.1.1:9090/execlflie/20210306182648BE1FHFF7VOYQEHNCCK.xlsx
								 * @param savePath			服务器文件存放目录：如：/home/sms/upload/
								 * @param fileFullName		文件名：20210306182648BE1FHFF7VOYQEHNCCK.xlsx
								 * 
								 * @return
								 * 返回0 ，表示targetFileUrl 的文件不存在
								 * 返回1，表示复制下载成功
								 * 返回-1，表示复制下载失败
				    			 */
				    			int isExists = FileUtil.downloadNet(targetFileUrl, savePath, fileFullName.trim());
				    			if(isExists==1)
				    			{
				    				fileIsExists = 1;
				    				break;
				    			}
				    			fileIsExists = isExists;
				    		}
				    		catch (Exception e) 
				    		{
				    			// TODO: handle exception
				    			e.printStackTrace();
				    		}
						}
			    	}
		    	}
    		}
    	}
    	return fileIsExists;
	}
	
	/**
	 * 2021-04-21
	 * 代理提现凭证图片下载方法
	 * 
	 * @param remitPictureUrl		保存在库中的现凭证图片地址
	 * @return 
	 * 0：文件不存在
	 * 1；文件存在
	 * -1：文件下载失败
	 */
	public static int downLoadAgentRemitPicture(String remitPictureUrl)
	{
		int fileIsExists = 1;
		String imgPathSwitch = "OFF";
		Code imgPathSwitchCode =  DatabaseCache.getCodeBySortCodeAndCode("AgentPropertyConfig","imgPathSwitch");
    	if(imgPathSwitchCode!=null&&imgPathSwitchCode.getName()!=null&&!"".equals(imgPathSwitchCode.getName()))
    	{
    		imgPathSwitch = imgPathSwitchCode.getName();
    	}
    	if("ON".equals(imgPathSwitch))
    	{
    		if(StringUtil.isNotBlank(remitPictureUrl))
    		{
    			/*
    			 * 凭证图片库存地址
    			 * 如：/res/agentRemitOrder/20200404230141UC6hl.png
    			 */
    			String remitPicturePath = remitPictureUrl;
    			/*
    			 * 凭证图片文件服务器存储目录地址
    			  * 如：/home/sms/logo
    			 * 该路径下/agentRemitOrder目录需要自行补全
    			 */
		        String savePath = DatabaseCache.getCodeBySortCodeAndCode("System", "logoFilePath").getName();
		        /*
		         * 将凭证图片库存地址转换成服务器绝对目录地址
		     * /res路径项目识别转换路径串，系统自动转换为 /home/sms/logo
		         * 转换后：如：/home/sms/logo/agentRemitOrder/20200404230141UC6hl.png
		         */
				remitPicturePath = remitPicturePath.replace("/res",savePath);
				/*
				 * 判断本服务器上是否已经有该文件存在,若已存在，则不做处理，直接读取；不存在则需先复制下载到本服务器上
				 * 使用凭证图片文件服务器绝对目录地址，校验本机是否存在该文件
				 */
		    	if(!FileUtil.fileIsExists(remitPicturePath))
		    	{
		    		/*
	    			 * 凭证图片库存地址
	    			 * 如：/res/agent/guangsu.png
	    			 */
		    		String targetFileUrl = remitPictureUrl;
		    		String fileFullName = remitPictureUrl;
		    		
		    		/*
		    		 * 获取管理后台服务器的所有IP地址 ： 如： 192.168.1.1:8808,192.168.1.2:8808
		    		 */
			    	Code admin_img_ip_code =  DatabaseCache.getCodeBySortCodeAndCode("AgentPropertyConfig","admin_img_ip");
			    	String admin_img_ip = "";
			    	if(admin_img_ip_code!=null&&admin_img_ip_code.getName()!=null&&!"".equals(admin_img_ip_code.getName()))
			    	{
			    	//  http://192.168.1.1:8181,http://192.168.1.2:8181
			    		admin_img_ip = admin_img_ip_code.getName();
			    	}
			    	if(admin_img_ip!=null&&!"".equals(admin_img_ip))
			    	{
			    		/*
			    		 * 拼装管理服务器的凭证图片文件路径
			    		 * 补全agentRemitOrder目录
			    		 * 	/home/sms/logo ====>>>> /home/sms/logo/agentRemitOrder
			    		 */
			    		savePath = savePath + "/agentRemitOrder";
			    		/*
			    		 * 截取文件名
			    		 * 	/res/agentRemitOrder/20200404230141UC6hl.png ====>>>> 20200404230141UC6hl.png
			    		 */
			    		fileFullName = fileFullName.replace("/res/agentRemitOrder/","");
			    		String[] adminImgIps = admin_img_ip.split(StringPool.COMMA);
			    		for (int i = 0; i < adminImgIps.length; i++) 
			    		{
			    			/*
			    			 * 拼装现凭证图片文件可访问路径地址
			    			 * http://192.168.1.1:8808/res/agentRemitOrder/20200404230141UC6hl.png
			    			 */
				    		targetFileUrl = adminImgIps[i] + targetFileUrl;
				    		try 
				    		{
				    			/*
				    			 * 将logo访问地址的文件，复制下载到本机服务器相同目录路径下
								 * @param targetFileUrl		可访问文件地址：如：http://192.168.1.1:8808/res/agentRemitOrder/20200404230141UC6hl.png
								 * @param savePath			服务器文件存放目录：如：/home/sms/logo/agentRemitOrder/
								 * @param fileFullName		文件名：20200404230141UC6hl.png
								 * 
								 * @return
								 * 返回0 ，表示targetFileUrl 的文件不存在
								 * 返回1，表示复制下载成功
								 * 返回-1，表示复制下载失败
				    			 */
				    			int isExists = FileUtil.downloadNet(targetFileUrl, savePath, fileFullName.trim());
				    			if(isExists==1)
				    			{
				    				fileIsExists = 1;
				    				break;
				    			}
				    			fileIsExists = isExists;
				    		}
				    		catch (Exception e) 
				    		{
				    			// TODO: handle exception
				    			e.printStackTrace();
				    		}
						}
			    	}
		    	}
    		}
    	}
    	return fileIsExists;
	}
}
