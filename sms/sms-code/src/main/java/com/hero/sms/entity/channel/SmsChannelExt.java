package com.hero.sms.entity.channel;

import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import lombok.Data;

/**
 * 短信通道 Entity
 *
 * @author Administrator
 * @date 2020-03-08 17:35:03
 */
@Data
public class SmsChannelExt extends SmsChannel{

    private List<SmsChannelProperty> propertys;

    private List<SmsChannelCost> costs;
    
    public SmsChannelProperty getProperty(String name) {
    	if(CollectionUtils.isNotEmpty(propertys)) {
    		Optional<SmsChannelProperty> first = propertys.stream().filter(property -> property.getName().equals(name)).findFirst();
    		if(first.isPresent()) {
    			return first.get();
    		}
    	}
    	return null;
    }
}
