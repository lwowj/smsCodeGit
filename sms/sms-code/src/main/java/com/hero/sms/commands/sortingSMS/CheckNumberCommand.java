package com.hero.sms.commands.sortingSMS;

import java.util.Map;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.hero.sms.commands.BaseCommand;
import com.hero.sms.commands.utils.MobileUtil;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.Regexp;
import com.hero.sms.common.utils.AppUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.MobileArea;
import com.hero.sms.entity.message.MobileBlack;
import com.hero.sms.entity.message.SendRecord;
import com.hero.sms.enums.message.SmsNumberAreaCodeEnums;
import com.hero.sms.enums.message.SmsNumberAreaProvinceEnums;
import com.hero.sms.service.message.IMobileAreaService;
import com.hero.sms.service.message.IMobileBlackService;

import lombok.extern.slf4j.Slf4j;

/**
 * 手机号码校验
 * @author Lenovo
 *
 */
@Slf4j
public class CheckNumberCommand extends BaseCommand {
    @SuppressWarnings("unchecked")
	@Override
    public boolean execute(Context context) throws Exception {
    	IMobileBlackService mobileBlackService = (IMobileBlackService) context.get(OBJ_MOBILEBLACK_SERVICE);
    	IMobileAreaService mobileAreaService = (IMobileAreaService) context.get(OBJ_MOBILEAREA_SERVICE);
        SendRecord sendRecord = (SendRecord) context.get(OBJ_SAVE_SENDRECORD_ENTITY);
        String areaCode = SmsNumberAreaCodeEnums.China.getInArea();
        if(sendRecord.getSmsNumberArea().equals(areaCode)) {
        	String number = sendRecord.getSmsNumber().trim();
        	number = StringUtils.removeStart(number, areaCode);
        	sendRecord.setSmsNumber(number);
        	
        	/**
        	 * @begin
        	 * 校验手机号码格式 
        	 * 2021-06-27
        	 */
        	if(StringUtils.isNotBlank(number))
        	{
        		//86手机号码基本格式1开头，11位号码
        		String regex = Regexp.MOBILE_REG;
        		if(!AppUtil.match(regex, number))
        		{
        			String msg = "获取手机详细信息失败E001";
    				context.put(STR_ERROR_INFO, msg);
    				context.put(STR_NOTIFY_MSG, msg);
    				return true;
        		}
        	}
        	/**
        	 * @end
        	 */
        	Map<String,String> detailMap = null;
        	detailMap = getFromDb(mobileAreaService,number);
        	if(detailMap == null) {
        		detailMap = getFromInterface(number);
        	}
        	if(detailMap == null) {
        		String msg = "获取手机详细信息失败";
				context.put(STR_ERROR_INFO, msg);
				context.put(STR_NOTIFY_MSG, msg);
				return true;
        	}
        	
        	String operatorName = detailMap.get("operatorName");
        	String provinceName = detailMap.get("provinceName");
        	try {
				
				if(StringUtils.isNotBlank(operatorName)) {
					String operatorCode = DatabaseCache.getCodeBySortCodeAndUpCodeAndName("PhoneOperator", sendRecord.getSmsNumberArea() , operatorName).getCode();
					sendRecord.setSmsNumberOperator(operatorCode);
				}
				
				if(StringUtils.isNotEmpty(provinceName)) {
					sendRecord.setSmsNumberProvince(SmsNumberAreaProvinceEnums.getCodeByAreaCodeAndName(sendRecord.getSmsNumberArea(), provinceName));
				}
				
				Code areaRuleCode = DatabaseCache.getCodeBySortCodeAndCodeAndUpCode("SmsNumberLimitRule", "areaRule",areaCode);
				if(areaRuleCode != null && StringUtils.isNotBlank(areaRuleCode.getName())) {
					boolean match = AppUtil.match(areaRuleCode.getName(), provinceName);
					if(!match) {
						String msg = String.format("不支持地区【%s】",provinceName);
						context.put(STR_ERROR_INFO, msg);
						context.put(STR_NOTIFY_MSG, msg);
						return true;
					}
				}
				Code operatorRuleCode = DatabaseCache.getCodeBySortCodeAndCodeAndUpCode("SmsNumberLimitRule", "operatorRule",areaCode);
				if(operatorRuleCode != null && StringUtils.isNotBlank(operatorRuleCode.getName())) {
					boolean match = AppUtil.match(operatorRuleCode.getName(), operatorName);
					if(!match) {
						String msg = String.format("不支持运营商【%s】",operatorName);
						context.put(STR_ERROR_INFO, msg);
						context.put(STR_NOTIFY_MSG, msg);
						return true;
					}
				}
				Code areaOperatorRuleCode = DatabaseCache.getCodeBySortCodeAndCodeAndUpCode("SmsNumberLimitRule", "areaAndOperatorRule",areaCode);
				if(areaOperatorRuleCode != null && StringUtils.isNotBlank(areaOperatorRuleCode.getName())) {
					String rules = areaOperatorRuleCode.getName();
					boolean match = AppUtil.match(rules, provinceName+operatorName);
					if(!match) {
						String msg = String.format("不支持地区【%s】运营商【%s】",provinceName,operatorName);
						context.put(STR_ERROR_INFO, msg);
						context.put(STR_NOTIFY_MSG, msg);
						return true;
					}
				}
				
			} catch (Exception e) {
				log.error(String.format("发件箱【%s】手机号码【%s】获取手机详细信息失败",sendRecord.getSendCode(),number),e);
				context.put(STR_ERROR_INFO, "获取手机详细信息失败");
				context.put(STR_NOTIFY_MSG, "手机号码异常");
				return true;
			}
        }
        /**
         * @begin 2021-01-15
         * 查询手机号码黑名单方式切换成查缓存列表
         */
        MobileBlack mobileBlack = new MobileBlack();
        mobileBlack.setArea(sendRecord.getSmsNumberArea());
        mobileBlack.setNumber(sendRecord.getSmsNumber());
        if(mobileBlackService.getCacheOne(mobileBlack) != null) 
        {
        	context.put(STR_ERROR_INFO, "黑名单手机号码");
        	context.put(STR_NOTIFY_MSG, "手机号码限制");
        	return true;
        }
        /**
         * @end
         */
//        LambdaQueryWrapper<MobileBlack> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(MobileBlack::getArea, sendRecord.getSmsNumberArea());
//        queryWrapper.eq(MobileBlack::getNumber, sendRecord.getSmsNumber());
//        if(mobileBlackService.getOne(queryWrapper) != null) {
//        	context.put(STR_ERROR_INFO, "黑名单手机号码");
//        	context.put(STR_NOTIFY_MSG, "手机号码限制");
//        	return true;
//        }
        return false;
    }
    
//    public static void main(String[] args) 
//    {
////    	String number = "18884389054";
////    	String regex = Regexp.MOBILE_REG;
////		if(!AppUtil.match(regex, number))
////		{
////			System.out.println("11111111");
////		}
////		else
////		{
////			System.out.println("2222222");
////		}
//		try {
//			String phoneMsgGetType = "360";
//			String number = "18884389054";
//			String numberAreaUrl = "http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel=";
////			numberAreaUrl = "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=";
////			numberAreaUrl = "https://www.baifubao.com/callback?cmd=1059&callback=phone&phone=";
//			numberAreaUrl = "http://cx.shouji.360.cn/phonearea.php?number=";
//			RestTemplate restTemplate=new RestTemplate();
//			String result = restTemplate.getForObject(numberAreaUrl+number, String.class);
//			System.out.println(result);
//			String operatorName = "";
//			String provinceName = "";
//		    if("TAOBAO".equals(phoneMsgGetType))
//		    {
//		    	result = result.replace("__GetZoneResult_ = ", "");
//		    	JSONObject  jasonObject = JSONObject.parseObject(result);
//		        Map<String,Object> map = (Map<String,Object>)jasonObject;
//		        operatorName = String.valueOf(map.get("catName")).replace("中国", "");
//				provinceName = String.valueOf(map.get("province"));
//		    }
//		    else if("BAIDU".equals(phoneMsgGetType))
//		    {
//		    	JSONObject resultJson = JSON.parseObject(result);
//				JSONObject responseJson = resultJson.getJSONObject("response").getJSONObject(number).getJSONObject("detail");
//				operatorName = responseJson.getString("operator");
//				provinceName = responseJson.getString("province");
//		    }
//		    else if("BAIFUBAO".equals(phoneMsgGetType))
//		    {
//		    	result = result.replace("*fgg_again*/phone(", "");
//		    	System.out.println(result);
//		    	result = result.replaceFirst("/","");
//		    	System.out.println(result);
//		    	result = result.replace(")","");
//		    	JSONObject  jasonObject = JSONObject.parseObject(result);
//		    	String datajson = String.valueOf(jasonObject.get("data"));
//		    	JSONObject  datajsonObject = JSONObject.parseObject(datajson);
//		        operatorName = String.valueOf(datajsonObject.get("operator"));
//				provinceName = String.valueOf(datajsonObject.get("area"));
//		    }
//		    else if("360".equals(phoneMsgGetType))
//		    {
//		    	JSONObject resultJson = JSON.parseObject(result);
//		    	String datajson = String.valueOf(resultJson.get("data"));
//		    	JSONObject  datajsonObject = JSONObject.parseObject(datajson);
//		        operatorName = String.valueOf(datajsonObject.get("sp")).replace("省", "");
//				provinceName = String.valueOf(datajsonObject.get("province"));
//		    }
//			Map<String, String> resultMap = Maps.newHashMap();
//			resultMap.put("operatorName", operatorName);
//			resultMap.put("provinceName", provinceName);
//			System.out.println(resultMap);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}
    
    private Map<String,String> getFromInterface(String number) {
		try 
		{
			String phoneMsgGetType = "TAOBAO";
			Code phoneMsgGetTypeCode = DatabaseCache.getCodeBySortCodeAndCode("System", "PhoneMsgGetType");
		    if(phoneMsgGetTypeCode!=null&&!"".equals(phoneMsgGetTypeCode.getName()))
		    {
		    	phoneMsgGetType = phoneMsgGetTypeCode.getName();
		    }
		    if("TAOBAO".equals(phoneMsgGetType))
		    {
		    	return MobileUtil.getFromInterfaceForTaoBao(number);
		    }
		    else if("BAIDU".equals(phoneMsgGetType))
		    {
		    	return MobileUtil.getFromInterfaceForBaiDu(number);
		    }
		    else if("360".equals(phoneMsgGetType))
		    {
		    	return MobileUtil.getFromInterfaceFor360(number);
		    }
		    else if("BAIFUBAO".equals(phoneMsgGetType))
		    {
		    	return MobileUtil.getFromInterfaceForBaiFuBao(number);
		    }
		    else
		    {
		    	return MobileUtil.getFromInterfaceForTaoBao(number);
		    }
		} catch (Exception e) {
			log.error(String.format("接口获取手机号码【%s】详情失败",number),e);
		}
    	return null;
    }
    
    private Map<String,String> getFromDb(IMobileAreaService mobileAreaService,String number) {
    	try {
			String prNumber = number.substring(0, 7);
//			/**
//	         * @begin 2021-01-15
//	         * 查询手机号段方式切换成查缓存列表
//	         */
//			MobileArea paramMobileArea = new MobileArea();
//			paramMobileArea.setMobilenumber(prNumber);
//			MobileArea mobileArea = mobileAreaService.getCacheOne(paramMobileArea);
//			if(mobileArea != null) 
//			{
//				Map<String, String> resultMap = Maps.newHashMap();
//				String[] provinceCity = mobileArea.getMobilearea().split("\\s+");
//				resultMap.put("operatorName", mobileArea.getMobiletype());
//				resultMap.put("provinceName", provinceCity[0]);
//				return resultMap;
//			}
//	        /**
//	         * @end
//	         */
			LambdaQueryWrapper<MobileArea> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(MobileArea::getMobilenumber, prNumber);
			MobileArea mobileArea = mobileAreaService.getOne(queryWrapper);
			if(mobileArea != null) {
				Map<String, String> resultMap = Maps.newHashMap();
				String[] provinceCity = mobileArea.getMobilearea().split("\\s+");
				resultMap.put("operatorName", mobileArea.getMobiletype());
				resultMap.put("provinceName", provinceCity[0]);
				return resultMap;
			}
		} catch (Exception e) {
			log.error(String.format("数据库获取手机号码【%s】详情失败",number),e);
		}
    	return null;
    }
}
