package com.hero.sms.service.impl.message;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.sensi.SensitiveFilter;
import com.hero.sms.entity.message.SensitiveWord;
import com.hero.sms.mapper.message.SensitiveWordMapper;
import com.hero.sms.service.message.ISensitiveWordService;

/**
 * 敏感词 Service实现
 *
 * @author Administrator
 * @date 2020-03-20 23:04:40
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements ISensitiveWordService {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    
    @Autowired
    private DatabaseCache databaseCache;

    @Override
    public IPage<SensitiveWord> findSensitiveWords(QueryRequest request, SensitiveWord sensitiveWord) {
        LambdaQueryWrapper<SensitiveWord> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotBlank(sensitiveWord.getWord())) {
        	queryWrapper.like(SensitiveWord::getWord, sensitiveWord.getWord());
        }
        queryWrapper.orderByDesc(SensitiveWord::getId);
        Page<SensitiveWord> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SensitiveWord> findSensitiveWords(SensitiveWord sensitiveWord) {
	    LambdaQueryWrapper<SensitiveWord> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
	    if(StringUtils.isNotBlank(sensitiveWord.getWord())) {
        	queryWrapper.like(SensitiveWord::getWord, sensitiveWord.getWord());
        }
        queryWrapper.orderByDesc(SensitiveWord::getId);
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createSensitiveWord(SensitiveWord sensitiveWord) {
        this.save(sensitiveWord);
    }

    @Override
    @Transactional
    public void updateSensitiveWord(SensitiveWord sensitiveWord) {
        this.saveOrUpdate(sensitiveWord);
    }

    @Override
    @Transactional
    public void deleteSensitiveWord(SensitiveWord sensitiveWord) {
        LambdaQueryWrapper<SensitiveWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensitiveWord::getId, sensitiveWord.getId());
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteSensitiveWords(String[] sensitiveWordIds) {
        List<String> list = Arrays.asList(sensitiveWordIds);
        this.removeByIds(list);
    }

	@Override
	public void init() {
		databaseCache.initSensitiveWord();//重载敏感词
		SensitiveFilter filter = SensitiveFilter.init();
//		List<SensitiveWord> words = findSensitiveWords(new SensitiveWord());
		List<SensitiveWord> words = databaseCache.getSensitiveWordList();
		if(CollectionUtils.isNotEmpty(words)) {
			for (SensitiveWord sensitiveWord : words) {
				filter.put(sensitiveWord.getWord());
			}
		}
	}
}
