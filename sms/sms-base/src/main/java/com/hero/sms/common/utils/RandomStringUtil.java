package com.hero.sms.common.utils;

import java.util.Random;

/**
 * 随机字符
 */
public class RandomStringUtil {

    public static final int SMALL = 1;
    public static final int BIG = 2;


    public static String randomStr(int length){
        return randomStr(length,0);
    }


    public static String randomStr(int length,int type){
        if(length<=0){
            length = 10;
        }else if(length >50){
            length = 50;
        }
        StringBuilder s = new StringBuilder(50);
        Random random = new Random();
        for( int i = 0; i < length; i ++) {
            int choice;
            switch (type){
                case 1: choice = 97;break;
                case 2: choice = 65;break;
                default: choice = random.nextInt(2) % 2 == 0 ? 65 : 97;;
            }
            s.append((char)(choice + random.nextInt(26)));
        }
        return s.toString();
    }

}
