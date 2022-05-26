package com.hero.sms.enums.message;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.Regexp;
import com.hero.sms.common.utils.AppUtil;
import com.hero.sms.entity.channel.AreaCode;
import com.hero.sms.enums.BaseEnum;
import com.hero.sms.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 手机号码区域
 * @author Lenovo
 *
 */
public enum SmsNumberAreaCodeEnums implements BaseEnum {

	China("+86","+86","国内","CN");
	
	private String inArea;
	private String outArea;
	private String areaName;
	private String areaCoding;
	
	private SmsNumberAreaCodeEnums(String inArea,String outArea, String areaName, String areaCoding) {
		this.inArea = inArea;
		this.outArea = outArea;
		this.areaName = areaName;
		this.areaCoding = areaCoding;
	}

	public String getInArea() {
		return inArea;
	}
	public String getOutArea() {
		return outArea;
	}

	public String getAreaName() {
		return areaName;
	}
	public String getAreaCoding() {
		return areaCoding;
	}
	
	/*
	 * 2021-09-16 固定枚举的方法进行调整
	 * 弃用
	 */
	public static String getNameByCodeNew(String inArea) {
		if (StringUtils.isBlank(inArea)) return null;
		for (SmsNumberAreaCodeEnums smsNumberAreaCodeEnums : values()) {
			if(smsNumberAreaCodeEnums.getInArea().equals(inArea)) {
				return smsNumberAreaCodeEnums.getAreaName();
			}
		}
		return null;
	}
	
	/*
	 * 2021-09-16 先获取动态配置，如无配置则取枚举
	 */
	public static String getNameByCode(String inArea) {
		if (StringUtils.isBlank(inArea)) return null;
		List<AreaCode> smsNumberAreaCodeList = DatabaseCache.getAreaCodeList();
		if(smsNumberAreaCodeList!=null&&smsNumberAreaCodeList.size()>0)
		{
			for (int i = 0; i < smsNumberAreaCodeList.size(); i++) 
			{
				AreaCode smsNumberAreaCode = smsNumberAreaCodeList.get(i);
				if(inArea.equals(smsNumberAreaCode.getInArea())) {
					return smsNumberAreaCode.getAreaName();
				}
			}
		}
		else
		{
			for (SmsNumberAreaCodeEnums smsNumberAreaCodeEnums : values()) {
				if(smsNumberAreaCodeEnums.getInArea().equals(inArea)) {
					return smsNumberAreaCodeEnums.getAreaName();
				}
			}
		}
		return null;
	}
	
	/*
	 * 2021-09-16 固定枚举的方法进行调整
	 * 弃用
	 */
	public static String[] splitAreaAndNumberNew(String numbers) {
		if(!numbers.startsWith("+"))numbers = "+"+numbers;
		for (SmsNumberAreaCodeEnums areaEnum : values()) {
			if(numbers.startsWith(areaEnum.getInArea())) {
				String number = StringUtils.removeStart(numbers, areaEnum.getInArea());
				return new String[] {areaEnum.getInArea(),number};
			}
		}
		return null;
	}

	/*
	 * 2021-09-16 先获取动态配置，如无配置则取枚举
	 */
	public static String[] splitAreaAndNumber(String numbers) {
		if(StringUtil.isNotBlank(numbers))
		{
			numbers = numbers.trim();
			if(!numbers.startsWith("+")) { numbers = "+"+numbers; }
			
			boolean checkChinaFlag = false;
			
			if(numbers.startsWith(SmsNumberAreaCodeEnums.China.getOutArea())) 
			{
				String checkNumber = StringUtils.removeStart(numbers, SmsNumberAreaCodeEnums.China.getOutArea());
				//86手机号码基本格式1开头，11位号码
				String regex = Regexp.MOBILE_REG;
				if(AppUtil.match(regex, checkNumber))
				{
					checkChinaFlag = true;
				}
			}
			
			if(checkChinaFlag)
			{
				for (SmsNumberAreaCodeEnums areaEnum : values()) {
					if(numbers.startsWith(areaEnum.getOutArea())) {
						String number = StringUtils.removeStart(numbers, areaEnum.getOutArea());
						return new String[] {areaEnum.getOutArea(),number};
					}
				}
			}
			else
			{
				List<AreaCode> smsNumberAreaCodeList = DatabaseCache.getAreaCodeList();
				if(smsNumberAreaCodeList!=null&&smsNumberAreaCodeList.size()>0)
				{
					for (int i = 0; i < smsNumberAreaCodeList.size(); i++) 
					{
						AreaCode smsNumberAreaCode = smsNumberAreaCodeList.get(i);
						if(smsNumberAreaCode.getOutArea().equals(SmsNumberAreaCodeEnums.China.getOutArea()))
						{
							continue;
						}
						if(numbers.startsWith(smsNumberAreaCode.getOutArea())) {
							String number = StringUtils.removeStart(numbers, smsNumberAreaCode.getOutArea());
							return new String[] {smsNumberAreaCode.getOutArea(),number};
						}
					}
				}
//				else
//				{
//					for (SmsNumberAreaCodeEnums areaEnum : values()) 
//					{
//						if(numbers.startsWith(areaEnum.getInArea())) {
//							String number = StringUtils.removeStart(numbers, areaEnum.getInArea());
//							return new String[] {areaEnum.getInArea(),number};
//						}
//					}
//				}
			}
		}
		return null;
	}
}
