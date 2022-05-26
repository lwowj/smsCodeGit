package com.hero.sms.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class RandomUtil {

    private static final char[] RANDOM_META_DATA = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0',
            '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    private static final char[] RANDOM_META_NUM_DATA = new char[] {'1', '2', '3', '4', '5', '6', '7', '8', '9' };

    /**
     * 随机字母大小写+数字
     * @param num
     * @return
     */
    public static String randomStr(int num) {
        return random(num, RANDOM_META_DATA);
    }

    /**
     * 随机数字
     * @param num
     * @return
     */
    public static String randomNum(int num) {
        return random(num, RANDOM_META_NUM_DATA);
    }

    /**
     * 随机数
     * @param num
     * @param randomData
     * @return
     */
    public static String random(int num, char[] randomData) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer(num);
        for (int i = 0; i < num; i++) {
            sb.append(randomData[random.nextInt(randomData.length - 1)]);
        }
        return sb.toString();
    }

    /**
     * 生成日期（yyyyMMddHHmmss） + ramdomStrSize位随机数的字符串
     * @param ramdomStrSize
     * @return
     */
    public static String randomStringWithDate(int ramdomStrSize){
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");// 设置日期格式
        String randomStr = df.format(new Date()) + RandomUtil.randomStr(ramdomStrSize).toUpperCase();
        return randomStr;
    }

    public static String randomStringWithDate(String batchStr,int ramdomStrSize){
        SimpleDateFormat df = new SimpleDateFormat("yMMddHHmmss");// 设置日期格式
        String randomStr = batchStr + df.format(new Date()) + RandomUtil.randomStr(ramdomStrSize).toUpperCase();
        return randomStr;
    }
    public static void main(String[] args) {
        System.out.println(randomStringWithDate(5));
    }
}
