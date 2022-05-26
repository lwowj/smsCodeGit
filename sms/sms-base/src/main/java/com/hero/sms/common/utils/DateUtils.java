package com.hero.sms.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * 
 * DateUtil
 * @createTime 2016年9月15日 下午7:17:37
 * @version 1.0.0
 * 
 */
public class DateUtils {
	public static final String Y_M_D_H_M_S_1 = "yyyy-MM-dd HH:mm:ss";
	public static final String Y_M_D_H_M_S_2 = "yyyyMMddHHmmss";
	public static final String Y_M_D_H_M_S_S_1 = "yyyy-MM-dd HH:mm:ss:SSS";
	public static final String Y_M_D_H_M_S_S_2 = "yyyyMMddHHmmssSSS";
	public static final String Y_M_D_H_M_S_S_2_1 = "yyyyMMddHHmmssSS";
	public static final String Y_M_D_H_M_S_S_3 = "yyMMddHHmmssSSS";
	public static final String Y_M_D_1 = "yyyy-MM-dd";
	public static final String Y_M_D_2 = "yyyyMMdd";
	public static final String H_M_S_1 = "HH:mm:ss";
	public static final String H_M_S_2 = "HHmmss";

	public static String getString(String pattern) {
		return new SimpleDateFormat(pattern).format(new Date());
	}

	public static String getString() {
		return getString(new Date(), Y_M_D_H_M_S_1);
	}
	public static String getString(Date date) {
		return getString(date, Y_M_D_H_M_S_1);
	}

	public static String getString(Date date, String pattern) {
		return new SimpleDateFormat(pattern).format(date);
	}

	public static Date getDate(String pattern, String str) {
		try {
			return new SimpleDateFormat(pattern).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Date getDate(String str) {
		return getDate(Y_M_D_H_M_S_1, str);
	}
	
	/**
	 * 判断天数 Date
	 * @param smdate
	 * @param bdate
	 * @return
	 * @throws ParseException
	 */
	 public static int daysBetween(Date smdate,Date bdate){   
		 try{
	        SimpleDateFormat sdf=new SimpleDateFormat(Y_M_D_H_M_S_1);  
	        smdate=sdf.parse(sdf.format(smdate));  
	        bdate=sdf.parse(sdf.format(bdate));  
	        Calendar cal = Calendar.getInstance();    
	        cal.setTime(smdate);    
	        long time1 = cal.getTimeInMillis();                 
	        cal.setTime(bdate);    
	        long time2 = cal.getTimeInMillis();         
	        long between_days=(time2-time1)/(1000*3600*24);       
	       return Integer.parseInt(String.valueOf(between_days));           
		 }catch(ParseException pe){
			 return 100;
		 }
	 } 
	 /**
	  * 判断天数 Sting
	  * @param smdate
	  * @param bdate
	  * @return
	  * @throws ParseException
	  */
	 public static int daysBetween(String smdate,String bdate){  
		 try{  
		 	SimpleDateFormat sdf=new SimpleDateFormat(Y_M_D_H_M_S_1);  
	        Calendar cal = Calendar.getInstance();    
	        cal.setTime(sdf.parse(smdate));    
	        long time1 = cal.getTimeInMillis();                 
	        cal.setTime(sdf.parse(bdate));    
	        long time2 = cal.getTimeInMillis();         
	        long between_days=(time2-time1)/(1000*3600*24);  
	        return Integer.parseInt(String.valueOf(between_days));
		 }catch(ParseException pe){
			 return 100;
		 }      
	    }  
	
	 /**
	  * 判断时间差几秒
	 * @Title: daysBetween 
	 * @Description: TODO(这里用一句话描述这个方法的作用) 
	 * @param @param smdate
	 * @param @param bdate
	 * @param @return    设定文件 
	 * @return int    返回类型 
	 * @throws
	  */
	 public static int secsBetween(String smdate,String bdate){  
		 try{  
		 	SimpleDateFormat sdf=new SimpleDateFormat(Y_M_D_H_M_S_1);  
	        Calendar cal = Calendar.getInstance();    
	        cal.setTime(sdf.parse(smdate));    
	        long time1 = cal.getTimeInMillis();                 
	        cal.setTime(sdf.parse(bdate));    
	        long time2 = cal.getTimeInMillis();         
	        long between_secs=(time2-time1)/(1000);  
	        return Integer.parseInt(String.valueOf(between_secs));
		 }catch(ParseException pe){
			 return -1;
		 }      
	    }
	 
   /**
     * 取得当前时间戳（精确到秒）
     *
     * @return nowTimeStamp
     */
    public static String getNowTimeStamp() {
        long time = System.currentTimeMillis();
        String nowTimeStamp = String.valueOf(time / 1000);
        return nowTimeStamp;
    }
    
    /**
     * 根据时间增加（减少）时间  Zhongzf  20180921
     * @return nowTimeStamp
     */
    public static Date getDate(Date date,int type,int count) {
    	Calendar startDate = Calendar.getInstance();
		startDate.add(type, count);
		return startDate.getTime();
    }
    /** 
	*@version: 
	* @Description: 获取昨天时间
	* @author: zhanggc
	* @date: 2019年5月31日 
	*/ 
	public static String getLastDay(String specifiedDay) {
		//SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		Calendar c = Calendar.getInstance();
		Date date = null;
		try {
			date = new SimpleDateFormat("yy-MM-dd").parse(specifiedDay);
		 
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.setTime(date);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day - 1);
 
		String dayBefore = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
		return dayBefore;
	}
	
	/**  
	 * @Title: getFormatDateTime  
	 * @Description: 获取两个时间的差值     lijq  20190922
	 * @param @param begin  开始时间
	 * @param @param end	结束时间
	 */
	public static String getFormatDateTime(Date begin, Date end){
		int seconds = secsBetween(getString(begin), getString(end));
		return secondsToTime(seconds);
	}
	
	/**  
	 * @Title: secondsToTime  
	 * @Description: 秒转为时分秒   lijq  20190923
	 * @param @param seconds
	 */
	public static String secondsToTime(int seconds){
		try {
			int hour = 0,minute = 0,second = 0;
			String period = null;
			if (seconds <= 0) {
				return "错误";
			}
			if (seconds >= 3600) {
				hour = seconds / 3600;
				minute = (seconds % 3600) / 60;
				second = (seconds % 3600) % 60;
				period = hour + "小时" + minute + "分" + second + "秒";
			} else if (seconds < 3600 && seconds >= 60) {
				minute = seconds / 60;
				second = seconds % 60;
				period = minute + "分" + second + "秒";
			} else if (seconds < 60 && seconds >= 0) {
				second = seconds;
				period = second + "秒";
			} else {
				period = "0秒";
			}
			return period;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
