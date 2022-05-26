package com.hero.sms.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.core.toolkit.StringPool;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppUtil {

	/**
	 * 驼峰转下划线
	 *
	 * @param value
	 *            待转换值
	 * @return 结果
	 */
	public static String camelToUnderscore(String value) {
		if (StringUtils.isBlank(value))
			return value;
		String[] arr = StringUtils.splitByCharacterTypeCamelCase(value);
		if (arr.length == 0)
			return value;
		StringBuilder result = new StringBuilder();
		IntStream.range(0, arr.length).forEach(i -> {
			if (i != arr.length - 1)
				result.append(arr[i]).append(StringPool.UNDERSCORE);
			else
				result.append(arr[i]);
		});
		return StringUtils.lowerCase(result.toString());
	}

	/**
	 * 下划线转驼峰
	 *
	 * @param value
	 *            待转换值
	 * @return 结果
	 */
	public static String underscoreToCamel(String value) {
		StringBuilder result = new StringBuilder();
		String[] arr = value.split("_");
		for (String s : arr) {
			result.append((String.valueOf(s.charAt(0))).toUpperCase()).append(s.substring(1));
		}
		return result.toString();
	}

	/**
	 * 判断是否为 ajax请求
	 *
	 * @param request
	 *            HttpServletRequest
	 * @return boolean
	 */
	public static boolean isAjaxRequest(HttpServletRequest request) {
		return (request.getHeader("X-Requested-With") != null
				&& "XMLHttpRequest".equals(request.getHeader("X-Requested-With")));
	}

	/**
	 * 正则校验
	 *
	 * @param regex
	 *            正则表达式字符串
	 * @param value
	 *            要匹配的字符串
	 * @return 正则校验结果
	 */
	public static boolean match(String regex, String value) {
		if(value == null)return false;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(value);
		return matcher.matches();
	}
	
	/**
	 * 转义正则特殊字符 （$()*+.[]?\^{},|）
	 * 
	 * @param keyword
	 * @return
	 */
	public static String escapeExprSpecialWord(String keyword) {
		if (StringUtils.isNotBlank(keyword)) {
			String[] fbsArr = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
			for (String key : fbsArr) {
				if (keyword.contains(key)) {
					keyword = keyword.replace(key, "\\" + key);
				}
			}
		}
		return keyword;
	}

	/**
	 * 判断是否包含中文
	 *
	 * @param value
	 *            内容
	 * @return 结果
	 */
	public static boolean containChinese(String value) {
		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(value);
		return m.find();
	}
}
