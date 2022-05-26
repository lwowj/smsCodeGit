package com.hero.sms.enums.msg;

import com.hero.sms.enums.BaseEnum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  消息类型
 */
public enum MessageTypeEnums implements BaseEnum {
    COMPLAINTS(1,"投诉",false),
    ADVICE(2,"建议",false),
    OTHER(3,"其它",false),
    SYSTEM(4,"系统消息",true);

    private int code;
    private String name;
    private boolean isSystem;

    MessageTypeEnums(int code, String name,boolean isSystem) {
        this.code = code;
        this.name = name;
        this.isSystem = isSystem;
    }
    public int getCode() {
        return code;
    }
    public String getName() {
        return name;
    }

    public boolean isSystem() {
        return isSystem;
    }
    //获取 非系统 的其他类型
    public static MessageTypeEnums[] filterSystemType(){
        return filter(false);
    }

    /**
     * 获取属于 系统 的类型
     * @return
     */
    public static MessageTypeEnums[] getSystemType(){
        return filter(true);
    }

    public static MessageTypeEnums[] filter(boolean isSystem){
        List<MessageTypeEnums> enums = Arrays.stream(values())
                .filter(item -> item.isSystem()==isSystem).collect(Collectors.toList());
        MessageTypeEnums[] result = new MessageTypeEnums[enums.size()];
        return enums.toArray(result);
    }
    public static String getNameByCode(Integer code) {
        if (code == null) return null;
        for (MessageTypeEnums msgTypeEnums : values()) {
            if(msgTypeEnums.getCode() == code) {
                return msgTypeEnums.getName();
            }
        }
        return null;
    }

}
