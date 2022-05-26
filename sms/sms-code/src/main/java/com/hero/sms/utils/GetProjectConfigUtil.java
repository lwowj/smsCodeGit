package com.hero.sms.utils;

import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.logging.log4j.util.PropertiesUtil;

/**
 * 获取配置文件信息
 */
public class GetProjectConfigUtil 
{
    public static String getProjectConfig(String key) 
    {
        Properties pros = new Properties();
        String value = "";
        try 
        {
            pros.load(new InputStreamReader(PropertiesUtil.class.getResourceAsStream("/application.yml"), "UTF-8"));
            value = pros.getProperty(key);
        }
        catch (Exception e) 
        {
        	return null;
        }
        return value;
    }
}
