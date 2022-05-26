package com.hero.sms.commands.utils;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.message.MobileArea;
import com.hero.sms.service.message.IMobileAreaService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MobileUtil 
{
	 /**
     * 百度API获取手机归属地信息
     * 2021-04-08
     * @param number
     * @return
     */
    public static Map<String,String> getFromInterfaceForBaiDu(String number) 
    {
		try 
		{
		    Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneMsgGetUrl", "BaiDu");
			//"http://mobsec-dianhua.baidu.com/dianhua_api/open/location?tel="
//		    {
//		        "response": {
//		            "15850781443": {
//		                "detail": {
//		                    "area": [{
//		                        "city": "南京"
//		                    }],
//		                    "province": "江苏",
//		                    "type": "domestic",
//		                    "operator": "移动"
//		                },
//		                "location": "江苏南京移动"
//		            }
//		        },
//		        "responseHeader": {
//		            "status": 200,
//		            "time": 1560740789379,
//		            "version": "1.1.0"
//		        }
//		    }
			String numberAreaUrl = code.getName();
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.getForObject(numberAreaUrl+number, String.class);
			if(StringUtils.isNotBlank(result))
	    	{
		    	JSONObject resultJson = JSON.parseObject(result);
				JSONObject responseJson = resultJson.getJSONObject("response").getJSONObject(number).getJSONObject("detail");
				String operatorName = responseJson.getString("operator");
				String provinceName = responseJson.getString("province");
				if(StringUtils.isNotBlank(operatorName)&&StringUtils.isNotBlank(provinceName))
				{
					Map<String, String> resultMap = Maps.newHashMap();
//					provinceName.lastIndexOf("she")startsWith
					resultMap.put("operatorName", operatorName);
					if(provinceName.endsWith("省"))
					{
						provinceName = StringUtils.removeEnd(provinceName, "省");
					}
					resultMap.put("provinceName", provinceName);
					return resultMap;
				}
	    	}
	    	else
	    	{
	    		log.error(String.format("BaiDu-接口获取手机号码【%s】详情失败",number));
	    	}
		} catch (Exception e) {
			log.error(String.format("BaiDu-接口获取手机号码【%s】详情失败",number),e);
		}
    	return null;
    }
    
    /**
     * 淘宝API获取手机归属地信息
     * 2021-04-08
     * @param number
     * @return
     */
    public static Map<String,String> getFromInterfaceForTaoBao(String number) 
    {
		try 
		{
		    Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneMsgGetUrl", "TaoBao");
		  //numberAreaUrl = "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=";
//		    __GetZoneResult_ = {
//		    	    mts: '1585078',
//		    	    province: '江苏',
//		    	    catName: '中国移动',
//		    	    telString: '15850781443',
//		    	    areaVid: '30511',
//		    	    ispVid: '3236139',
//		    	    carrier: '江苏移动'
//		    	}
			String numberAreaUrl = code.getName();
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.getForObject(numberAreaUrl+number, String.class);
			if(StringUtils.isNotBlank(result))
	    	{
				result = result.replace("__GetZoneResult_ = ", "");
		    	JSONObject  jasonObject = JSONObject.parseObject(result);
		        Map<String,Object> map = (Map<String,Object>)jasonObject;
		        String operatorName = String.valueOf(map.get("catName")).replace("中国", "");
		        String provinceName = String.valueOf(map.get("province"));
		        if(StringUtils.isNotBlank(operatorName)&&StringUtils.isNotBlank(provinceName))
				{
					Map<String, String> resultMap = Maps.newHashMap();
					resultMap.put("operatorName", operatorName);
					if(provinceName.endsWith("省"))
					{
						provinceName = StringUtils.removeEnd(provinceName, "省");
					}
					resultMap.put("provinceName", provinceName);
					return resultMap;
				}
	    	}
	    	else
	    	{
	    		log.error(String.format("TaoBao-接口获取手机号码【%s】详情失败",number));
	    	}
		} catch (Exception e) {
			log.error(String.format("TaoBao-接口获取手机号码【%s】详情失败",number),e);
		}
    	return null;
    }
    
    /**
     * 360API获取手机归属地信息
     * 2021-04-08
     * @param number
     * @return
     */
    public static Map<String,String> getFromInterfaceFor360(String number) 
    {
		try 
		{
		    Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneMsgGetUrl", "360");
		  //numberAreaUrl = "http://cx.shouji.360.cn/phonearea.php?number=";
//		    {
//		        "code": 0, 
//		        "data": {
//		            "province": "山东", 
//		            "city": "济南", 
//		            "sp": "联通"
//		        }
//		    }
			String numberAreaUrl = code.getName();
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.getForObject(numberAreaUrl+number, String.class);
			if(StringUtils.isNotBlank(result))
	    	{
		    	JSONObject resultJson = JSON.parseObject(result);
		    	String datajson = String.valueOf(resultJson.get("data"));
		    	JSONObject  datajsonObject = JSONObject.parseObject(datajson);
		    	String operatorName = String.valueOf(datajsonObject.get("sp")).replace("省", "");
		    	String provinceName = String.valueOf(datajsonObject.get("province"));
		        if(StringUtils.isNotBlank(operatorName)&&StringUtils.isNotBlank(provinceName))
				{
					Map<String, String> resultMap = Maps.newHashMap();
					resultMap.put("operatorName", operatorName);
					if(provinceName.endsWith("省"))
					{
						provinceName = StringUtils.removeEnd(provinceName, "省");
					}
					resultMap.put("provinceName", provinceName);
					return resultMap;
				}
	    	}
	    	else
	    	{
	    		log.error(String.format("360-接口获取手机号码【%s】详情失败",number));
	    	}
		} catch (Exception e) {
			log.error(String.format("360-接口获取手机号码【%s】详情失败",number),e);
		}
    	return null;
    }
    
    /**
     * 百付宝API获取手机归属地信息
     * 2021-04-08
     * @param number
     * @return
     */
    public static Map<String,String> getFromInterfaceForBaiFuBao(String number) 
    {
		try 
		{
		    Code code = DatabaseCache.getCodeBySortCodeAndCode("PhoneMsgGetUrl", "BaiFuBao");
			//"https://www.baifubao.com/callback?cmd=1059&callback=phone&phone="
//		    /*fgg_again*/phone({"meta":{"result":"0","result_info":"","jump_url":""},"data":{"operator":"联通","area":"吉林","area_operator":"吉林联通","support_price":{"10000":"9990","2000":"2110","20000":"19980","3000":"2997","30000":"29970","5000":"4995","50000":"49950"},"promotion_info":null}})
			String numberAreaUrl = code.getName();
			RestTemplate restTemplate=new RestTemplate();
			String result = restTemplate.getForObject(numberAreaUrl+number, String.class);
	    	if(StringUtils.isNotBlank(result))
	    	{
	    		result = result.replace("*fgg_again*/phone(", "");
		    	result = result.replaceFirst("/","");
		    	result = result.replace(")","");
		    	JSONObject  jasonObject = JSONObject.parseObject(result);
		    	String datajson = String.valueOf(jasonObject.get("data"));
		    	JSONObject  datajsonObject = JSONObject.parseObject(datajson);
		        String operatorName = String.valueOf(datajsonObject.get("operator"));
		        String provinceName = String.valueOf(datajsonObject.get("area"));
				if(StringUtils.isNotBlank(operatorName)&&StringUtils.isNotBlank(provinceName))
				{
					Map<String, String> resultMap = Maps.newHashMap();
					resultMap.put("operatorName", operatorName);
					if(provinceName.endsWith("省"))
					{
						provinceName = StringUtils.removeEnd(provinceName, "省");
					}
					resultMap.put("provinceName", provinceName);
					return resultMap;
				}
	    	}
	    	else
	    	{
	    		log.error(String.format("BaiFuBao-接口获取手机号码【%s】详情失败",number));
	    	}
		} catch (Exception e) {
			log.error(String.format("BaiFuBao-接口获取手机号码【%s】详情失败",number),e);
		}
    	return null;
    }
    
    public static Map<String,String> getFromDb(IMobileAreaService mobileAreaService,String number) {
    	try {
			String prNumber = number.substring(0, 7);
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
