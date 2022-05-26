package com.hero.sms.service.message;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.message.SensitiveWord;

/**
 * 敏感词 Service接口
 *
 * @author Administrator
 * @date 2020-03-20 23:04:40
 */
public interface ISensitiveWordService extends IService<SensitiveWord> {
	
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param sensitiveWord sensitiveWord
     * @return IPage<SensitiveWord>
     */
    IPage<SensitiveWord> findSensitiveWords(QueryRequest request, SensitiveWord sensitiveWord);

    /**
     * 查询（所有）
     *
     * @param sensitiveWord sensitiveWord
     * @return List<SensitiveWord>
     */
    List<SensitiveWord> findSensitiveWords(SensitiveWord sensitiveWord);

    /**
     * 新增
     *
     * @param sensitiveWord sensitiveWord
     */
    void createSensitiveWord(SensitiveWord sensitiveWord);

    /**
     * 修改
     *
     * @param sensitiveWord sensitiveWord
     */
    void updateSensitiveWord(SensitiveWord sensitiveWord);

    /**
     * 删除
     *
     * @param sensitiveWord sensitiveWord
     */
    void deleteSensitiveWord(SensitiveWord sensitiveWord);

    /**
    * 删除
    *
    * @param sensitiveWordIds sensitiveWordIds
    */
    void deleteSensitiveWords(String[] sensitiveWordIds);

	void init();
}
