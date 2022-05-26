package com.hero.sms.service.common;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.common.CodeSort;

/**
 * 代码分类 Service接口
 *
 * @author MrJac
 * @date 2020-03-04 16:53:27
 */
public interface ICodeSortService extends IService<CodeSort> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param codeSort codeSort
     * @return IPage<CodeSort>
     */
    IPage<CodeSort> findCodeSorts(QueryRequest request, CodeSort codeSort);

    /**
     * 查询（所有）
     *
     * @param codeSort codeSort
     * @return List<CodeSort>
     */
    List<CodeSort> findCodeSorts(CodeSort codeSort);

    /**
     * 新增
     *
     * @param codeSort codeSort
     */
    void createCodeSort(CodeSort codeSort);

    /**
     * 修改
     *
     * @param codeSort codeSort
     */
    void updateCodeSort(CodeSort codeSort);

    /**
     * 删除
     *
     * @param codeSortIds codeSortIds
     */
    void deleteCodeSorts(String[] codeSortIds);

    /**
     * 通过code查询
     * @param id
     * @return
     */
    CodeSort findById(String id);
}
