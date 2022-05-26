package com.hero.sms.utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.hero.sms.common.utils.IPUtil;
import com.hero.sms.common.utils.MD5Util;

/**
 * 获取操作系统信息、浏览器信息工具类
 * @author payBoo
 *
 */
public class GetSystemBrowserInfo 
{
	public static Map<String, String> systemBrowserInfo(HttpServletRequest request) 
	{
		Map<String, String> infoMap = new HashMap<String, String>();
        try {
            StringBuilder userAgent = new StringBuilder("[");
            userAgent.append(request.getHeader("User-Agent"));
            userAgent.append("]");
            int indexOfMac = userAgent.indexOf("Mac OS X");
            int indexOfWindows = userAgent.indexOf("Windows NT");
            int indexOfIE = userAgent.indexOf("MSIE");
            int indexOfIE11 = userAgent.indexOf("rv:");
            int indexOfFF = userAgent.indexOf("Firefox");
            int indexOfSogou = userAgent.indexOf("MetaSr");
            int indexOfChrome = userAgent.indexOf("Chrome");
            int indexOfSafari = userAgent.indexOf("Safari");
            boolean isMac = indexOfMac > 0;
            boolean isWindows = indexOfWindows > 0;
            boolean isLinux = userAgent.indexOf("Linux") > 0;
            boolean containIE = indexOfIE > 0 || (isWindows && (indexOfIE11 > 0));
            boolean containFF = indexOfFF > 0;
            boolean containSogou = indexOfSogou > 0;
            boolean containChrome = indexOfChrome > 0;
            boolean containSafari = indexOfSafari > 0;
            String browser = "";
            if (containSogou) {
                if (containIE) {
                    browser = "搜狗" + userAgent.substring(indexOfIE, indexOfIE + "IE x.x".length());
                } else if (containChrome) {
                    browser = "搜狗" + userAgent.substring(indexOfChrome, indexOfChrome + "Chrome/xx".length());
                }
            } else if (containChrome) {
                browser = userAgent.substring(indexOfChrome, indexOfChrome + "Chrome/xx".length());
            } else if (containSafari) {
                int indexOfSafariVersion = userAgent.indexOf("Version");
                browser = "Safari "
                        + userAgent.substring(indexOfSafariVersion, indexOfSafariVersion + "Version/x.x.x.x".length());
            } else if (containFF) {
                browser = userAgent.substring(indexOfFF, indexOfFF + "Firefox/xx".length());
            } else if (containIE) {
                if (indexOfIE11 > 0) {
                    browser = "IE 11";
                } else {
                    browser = userAgent.substring(indexOfIE, indexOfIE + "IE x.x".length());
                }
            }
            String os = "";
            if (isMac) {
                os = userAgent.substring(indexOfMac, indexOfMac + "MacOS X xxxxxxxx".length());
            } else if (isLinux) {
                os = "Linux";
            } else if (isWindows) {
                os = "Windows ";
                String version = userAgent.substring(indexOfWindows + "Windows NT".length(), indexOfWindows
                        + "Windows NTx.x".length());
                switch (version.trim()) {
                    case "5.0":
                        os += "2000";
                        break;
                    case "5.1":
                        os += "XP";
                        break;
                    case "5.2":
                        os += "2003";
                        break;
                    case "6.0":
                        os += "Vista";
                        break;
                    case "6.1":
                        os += "7";
                        break;
                    case "6.2":
                        os += "8";
                        break;
                    case "6.3":
                        os += "8.1";
                        break;
                    case "10":
                        os += "10";
                        break;
                }
            }
            
            String thisIp = IPUtil.getIpAddr(request);
            infoMap.put("system",os);
            infoMap.put("browser",StringUtils.replace(browser, "/", " "));
            infoMap.put("thisIp",thisIp);
        } catch (Exception e) {
        	infoMap.put("system","");
            infoMap.put("browser","");
            infoMap.put("thisIp","");
        }
        return infoMap;
    }
	
	public static String getSystemBrowserInfoToken(HttpServletRequest request) 
	{
		String systemBrowserInfoMD5Token = "";
		Map<String, String> infoMap = systemBrowserInfo(request);
		if(!infoMap.isEmpty()&&infoMap!=null)
		{
			String system = infoMap.get("system");
			String browser = infoMap.get("browser");
			String thisIp = infoMap.get("thisIp");
			if(StringUtil.isNotBlank(system)&&StringUtil.isNotBlank(browser)&&StringUtil.isNotBlank(thisIp))
			{
				 StringBuffer checkStr = new StringBuffer();
				 checkStr.append(system).append("|").append(browser).append("|").append(thisIp).append("|").append(MD5Util.defaultKey);
				 systemBrowserInfoMD5Token = MD5Util.MD5(checkStr.toString());
			}
		}
		return systemBrowserInfoMD5Token;
	}
}
