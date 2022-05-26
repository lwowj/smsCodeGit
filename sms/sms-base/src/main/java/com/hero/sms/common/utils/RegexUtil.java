package com.hero.sms.common.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


/**
 * 校验器：利用正则表达式校验邮箱、手机号等
 * 
 * @author liujiduo
 * 
 */
public class RegexUtil {
	/**
	 * 号码限制的正则表达式：头部格式
	 * 2021-01-24
	 */
	public static final String REGEX_NUMBER_LIMIT_TOP = "^((?!.*(";

	/**
	 * 号码限制的正则表达式：尾部部格式
	 * 2021-01-24
	 */
	public static final String REGEX_NUMBER_LIMIT_END = ")).)*$";

	/**
	 * 正则表达式：验证用户名
	 */
	public static final String REGEX_USERNAME = "^[a-zA-Z]\\w{5,17}$";

	/**
	 * 正则表达式：验证密码
	 */
	public static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,16}$";

	/**
	 * 正则表达式：验证手机号
	 */
	public static final String REGEX_MOBILE = "^1\\d{10}$";

	/**
	 * 正则表达式：验证邮箱
	 */
	public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

	/**
	 * 正则表达式：验证汉字（单字符）
	 */
	public static final String REGEX_CHINESE = "^[\u4e00-\u9fa5],{0,}$";
	
	/**
	 * 正则表达式：验证汉字（全字符）
	 */
	public static final String REGEX_CHINESES = "[\u4e00-\u9fa5]+";

	/**
	 * 正则表达式：验证身份证
	 */
	public static final String REGEX_ID_CARD = "(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])";

	/**
	 * 正则表达式：验证URL
	 */
	public static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";

	/**
	 * 正则表达式：验证IP地址
	 */
	public static final String REGEX_IP_ADDR = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
	/**
	 * 正则表达式：验证密码，8~16位，至少包含数字、字母和特殊字符，author：chenwp
	 */
	public static final String REGEX_COMPLEX_PASSWORD1 = "^(?![a-zA-Z0-9]+$)(?![^a-zA-Z/D]+$)(?![^0-9/D]+$).{8,16}$";
	
	
	/**
	 * 正则表达式：验证IP格式
	 */
	public static final String REGEX_IP = "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$";
	
	/**
	 * 正则表达式：验证DNS格式
	 */
	public static final String REGEX_DNS = "^([a-zA-Z\\d][a-zA-Z\\d-_]+\\.)+[a-zA-Z\\d-_][^ ]*$";
	
	/**
	 * 正则表达式：验证登录账号
	 */
	public static final String REGEX_USERACCOUNT = "^[a-zA-Z0-9]{1,20}$";
	
	/**
	 * 校验登录账号
	 * 
	 * @param userAccount
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isUserAccount(String userAccount) {
		return Pattern.matches(REGEX_USERACCOUNT, userAccount);
	}
	
	/**
	 * 校验用户名
	 * 
	 * @param username
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isUsername(String username) {
		return Pattern.matches(REGEX_USERNAME, username);
	}

	/**
	 * 校验密码
	 * 
	 * @param password
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isPassword(String password) {
		return Pattern.matches(REGEX_PASSWORD, password);
	}

	/**
	 * 校验手机号
	 * 
	 * @param mobile
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isMobile(String mobile) {
		if (StringUtils.isEmpty(mobile)) {
			return false;
		}
		return Pattern.matches(REGEX_MOBILE, mobile);
	}

	/**
	 * 校验邮箱
	 * 
	 * @param email
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isEmail(String email) {
		return Pattern.matches(REGEX_EMAIL, email);
	}

	/**
	 * 校验汉字(单字符)
	 * 
	 * @param chinese
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isChinese(String chinese) {
		return Pattern.matches(REGEX_CHINESE, chinese);
	}

	/**
	 * 校验汉字（全字符串）
	 * 
	 * @param chinese
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isChineses(String chinese) {
		return Pattern.matches(REGEX_CHINESES, chinese);
	}

	
	/**
	 * 校验身份证
	 * 
	 * @param idCard
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isIDCard(String idCard) {
		return Pattern.matches(REGEX_ID_CARD, idCard);
	}

	/**
	 * 校验URL
	 * 
	 * @param url
	 * @return 校验通过返回true，否则返回false
	 */
	public static boolean isUrl(String url) {
		return Pattern.matches(REGEX_URL, url);
	}
	/** 
	* @Title: regExpTest 
	* @Description: 验证正则表达式
	* @param regExp
	* @param str
	* @return
	* @return: boolean
	* @throws 
	* @author: chenwp
	*/
	public static boolean regexVerify(String regExp, String str){
		return Pattern.matches(regExp, str);
	}

	/**
	 * 校验IP地址
	 * 
	 * @param ipAddr
	 * @return
	 */
	public static boolean isIPAddr(String ipAddr) {
		return Pattern.matches(REGEX_IP_ADDR, ipAddr);
	}
	
	/**
	 * 校验IP格式
	 */
	public static boolean isIP(String ip) {
		return Pattern.matches(REGEX_IP, ip);
	}
	
	/**
	 * 校验DNS格式
	 */
	public static boolean isDNS(String dns) {
		return Pattern.matches(REGEX_DNS, dns);
	}
	
	/**
	 * 校验金额
	 * @param args
	 */
	public static boolean isNumber(String str) {
		Pattern pattern = Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$"); // 判断小数点后2位的数字的正则表达式
		Matcher match = pattern.matcher(str);
		if (match.matches() == false) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 校验号码限制的格式是否正确
	 * 2021-01-24
	 * @param numberLimit
	 * @return
	 */
	public static boolean isNumberLimit(String numberLimit) 
	{
		boolean flag = true;
		if(numberLimit.indexOf(REGEX_NUMBER_LIMIT_TOP)!=0)
		{
			flag = false;
			return flag;
		}
		if(numberLimit.lastIndexOf(REGEX_NUMBER_LIMIT_END)!=(numberLimit.length()-REGEX_NUMBER_LIMIT_END.length()))
		{
			flag = false;
			return flag;
		}
		String numberLimitStr = numberLimit.substring(REGEX_NUMBER_LIMIT_TOP.length(), numberLimit.length());
		numberLimitStr = numberLimitStr.substring(0, numberLimitStr.length()-REGEX_NUMBER_LIMIT_END.length());
		if(StringUtils.isBlank(numberLimitStr))
		{
			flag = false;
			return flag;
		}
		String[] numberLimitArray = numberLimitStr.split("\\|");
		if(numberLimitArray==null||numberLimitArray.length==0)
		{
			flag = false;
			return flag;
		}
		for (int i = 0; i < numberLimitArray.length; i++) 
		{
			String numberLimitArrayStr = numberLimitArray[i];
			if(StringUtils.isNotBlank(numberLimitArrayStr))
			{
				if(!isChineses(numberLimitArrayStr))
				{
					flag = false;
					break;
				}
			}
			else
			{
				flag = false;
				break;
			}
		}
		return flag;
	}
//	public static void main(String[] args) {
//		String numberLimit = "^((?!.*(北京)).)*$";
//		System.out.println(isNumberLimit(numberLimit));
//		String smsNumberProvinceName = "北1京";
//		boolean match = AppUtil.match(numberLimit, smsNumberProvinceName);
//		System.out.println(match);
//	}
}
