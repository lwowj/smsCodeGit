package com.hero.sms.job.mapper;


import com.hero.sms.job.entity.Job;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author Administrator
 */
public interface JobMapper extends BaseMapper<Job> {
	
	List<Job> queryList();
}