package com.hero.sms.common.properties;

import com.hero.sms.common.entity.ImageType;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class ValidateCodeProperties {

    /**
     * 验证码有效时间，单位秒
     */
    private Long time = 120L;
    /**
     * 验证码类型，可选值 png和 gif
     */
    private String type = ImageType.GIF;
    /**
     * 图片宽度，px
     */
    private Integer width = 180;
    /**
     * 图片高度，px
     */
    private Integer height = 50;
    /**
     * 验证码位数
     */
    private Integer length = 5;
    /**
     * 验证码值的类型
     * 1. 数字加字母
     * 2. 纯数字
     * 3. 纯字母
     * 4. 中文
     */
    private Integer charType = 1;
}
