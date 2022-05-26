package com.hero.sms.entity.message;


import javax.validation.constraints.Size;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 敏感词 Entity
 *
 * @author Administrator
 * @date 2020-03-20 23:04:40
 */
@Data
@TableName("t_sensitive_word")
public class SensitiveWord {

    /**
     * 
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 敏感词
     */
    @TableField("word")
    @Size(min = 1,max = 10,message = "敏感词字数必须是1-10个字之间")
    private String word;
    
    /**
     * 替换词
     */
    @TableField("new_word")
    private String newWord;

}
