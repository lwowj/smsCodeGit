package com.hero.sms.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间工具类
 *
 * @author Administrator
 */
public class DateUtil {

    public static final String FULL_TIME_PATTERN = "yyyyMMddHHmmss";

    public static final String FULL_TIME_SPLIT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String CST_TIME_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";
    
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
	
	//根据时间格式类型获取当前时间
	public static String getString(String pattern) 
	{
		return new SimpleDateFormat(pattern).format(new Date());
	}

	//根据时间格式类型获取当前时间(默认格式yyyy-MM-dd HH:mm:ss)
	public static String getString() 
	{
		return getString(new Date(), Y_M_D_H_M_S_1);
	}
	
	//根据当前时间date类型转换为字符串格式（默认格式yyyy-MM-dd HH:mm:ss)
	public static String getString(Date date) 
	{
		return getString(date, Y_M_D_H_M_S_1);
	}

	/**
	 * 判断时间差几秒（精确到秒级）
	* @Title: daysBetween 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param @param smdate
	* @param @param bdate
	* @param @return    设定文件 
	* @return int    返回类型 
	* @throws
	*/
	public static int secsBetween(String smdate,String bdate)
	{  
		try
		{  
			SimpleDateFormat sdf=new SimpleDateFormat(Y_M_D_H_M_S_S_1);  
			Calendar cal = Calendar.getInstance();    
			cal.setTime(sdf.parse(smdate));    
			long time1 = cal.getTimeInMillis();                 
			cal.setTime(sdf.parse(bdate));    
			long time2 = cal.getTimeInMillis();         
			long between_secs=(time2-time1)/(1000);  
			return Integer.parseInt(String.valueOf(between_secs));
		}
		catch(ParseException pe)
		{
			return -1;
		}      
	}
	
	/**
	 * 转换为时间（天,时:分:秒.毫秒）
	 * 
	 * @param timeMillis
	 * @return
	 */
	public static String formatDateTime(long timeMillis) 
	{
		long day = timeMillis / (24 * 60 * 60 * 1000);
		long hour = (timeMillis / (60 * 60 * 1000) - day * 24);
		long min = ((timeMillis / (60 * 1000)) - day * 24 * 60 - hour * 60);
		long s = (timeMillis / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
		long sss = (timeMillis - day * 24 * 60 * 60 * 1000 - hour * 60 * 60 * 1000 - min * 60 * 1000 - s * 1000);
		return (day > 0 ? day + "," : "") + hour + ":" + min + ":" + s + "." + sss;
	}
	
	// 获取两个时间相差分钟数(毫秒级)
	public static long getTime(String beginTime,String endTime)
	{
		return getTime(beginTime, endTime,Y_M_D_H_M_S_S_1); 
	}
		
	// 获取两个时间相差分钟数（可根据需要传入时间格式）
	public static long getTime(String beginTime,String endTime,String pattern)
	{
		try 
	   	{
	   		SimpleDateFormat df = new SimpleDateFormat(pattern);
	        long NTime =df.parse(endTime).getTime();
	        //从对象中拿到时间
	        long OTime = df.parse(beginTime).getTime();
	        long diff=(NTime-OTime);
	        return diff;
		}
	   	catch (Exception e) 
	   	{
			// TODO: handle exception
			return -1;
		} 
	}
	    
    public static String formatFullTime(LocalDateTime localDateTime) {
        return formatFullTime(localDateTime, FULL_TIME_PATTERN);
    }

    public static String formatFullTime(LocalDateTime localDateTime, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(dateTimeFormatter);
    }

    public static String getDateFormat(Date date, String dateFormatType) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatType, Locale.CHINA);
        return simpleDateFormat.format(date);
    }

    public static String formatCSTTime(String date, String format) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CST_TIME_PATTERN, Locale.US);
        Date usDate = simpleDateFormat.parse(date);
        return DateUtil.getDateFormat(usDate, format);
    }

    public static String formatInstant(Instant instant, String format) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }
    public static Date addDay(Date date, int days) {
        return add(date, Calendar.DATE, days);
    }
    public static Date add(Date date, int type, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(type, days);
        date = calendar.getTime();
        return date;
    }
    public static String getDistanceTimes(Date start,Date end){
        if (start == null || end == null) return null;
        long st=start.getTime();
        long en=end.getTime();
        //1秒：1000 1分钟：60000;1小时:3600000；1天为86400000
        //计算天数
        int day=(int) ((en-st)/86400000);
        //计算小时
        int h=(int) (((en-st)%86400000)/3600000);
        //计算分钟
        int m=(int)(((en-st)%86400000)%3600000)/60000;
        //计算秒
        int s=(int)((((en-st)%86400000)%3600000)%60000)/1000;
        return String.format("%s天%s时%s分%s秒",day,h,m,s);
    }
    public static String getString(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public static void main(String[] args) 
    {
    	long start = System.currentTimeMillis();
    	Date startDate = new Date(start);
    	String startDateStr = DateUtil.getString(startDate, DateUtil.Y_M_D_H_M_S_S_1);
    	System.out.println(String.format("Begin分拣==========【%s】开始时间：【%s】,时间戳：【%d】,数量【%d】", 1,startDateStr,start,1));
       	
//    	String beginTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_1);
//    	long start = System.currentTimeMillis();
//    	Date startDate = new Date(start);
//    	String startDateStr = DateUtil.getString(startDate, Y_M_D_H_M_S_S_1);
//    	 System.out.println(String.format("Begin分拣==========【%s】开始时间：【%s】,数量【%s】", 1,beginTime,1));
//    	 System.out.println(String.format("Begin分拣==========【%s】开始时间：【%s】,数量【%s】", 2,startDateStr,2));
    	for (int i = 0; i < 9999999; i++) 
    	{
			if(i==0)
			{
				System.out.println("begin");
			}
			else if(i==(9999999-1))
			{
				System.out.println("end");
			}
				
		}
    	 long end = System.currentTimeMillis();
         try {
         	Date endDate = new Date(end);
             String endDateStr = DateUtil.getString(endDate, DateUtil.Y_M_D_H_M_S_S_1);
             long getTime = end-start;
             String getTimeStr = DateUtil.formatDateTime(47);
             System.out.println(String.format("End分拣============【%s】结束时间：【%s】,时间戳：【%d】,分拣耗时【%s】",1,endDateStr,end,getTimeStr));
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println(String.format("End分拣============【%s】,分拣耗时【%d】",1,(end-start)));
			}
         
//    	 String endTime = DateUtil.getString(DateUtil.Y_M_D_H_M_S_S_1);
//    	 long end = System.currentTimeMillis();
//    	 Date endDate = new Date(end);
//     	 String endDateStr = DateUtil.getString(endDate, Y_M_D_H_M_S_S_1);
//    	 long se = end-start;
//    	 long gettime = getTime(beginTime, endTime);
//    	 System.out.println(String.format("end分拣==========【%s】结束时间：【%s】,数量【%s】", 1,endTime,1));
//    	 System.out.println(String.format("end分拣==========【%s】结束时间：【%s】,数量【%s】", 2,endDateStr,2));
//    	 System.out.println("start===="+start+"<<<<<<end==="+end+"<<<<<se====="+se);
//    	 System.out.println(String.format("end分拣==========【%s】结束时间：【%s】,数量【%s】", 1,DateUtil.formatDateTime(se),1)); 
//    	 System.out.println(String.format("end分拣==========【%s】结束时间：【%s】,数量【%s】", 2,DateUtil.formatDateTime(gettime),2));
	}
    
}
