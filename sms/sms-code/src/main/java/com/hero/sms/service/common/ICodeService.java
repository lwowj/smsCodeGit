package com.hero.sms.service.common;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.entity.common.Code;

/**
 * 代码表 Service接口
 *
 * @author MrJac
 * @date 2020-03-04 21:15:50
 */
public interface ICodeService extends IService<Code> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param code code
     * @return IPage<Code>
     */
    IPage<Code> findCodes(QueryRequest request, Code code);

    /**
     * 查询（所有）
     *
     * @param code code
     * @return List<Code>
     */
    List<Code> findCodes(Code code);

    /**
     * 新增
     *
     * @param code code
     */
    void createCode(Code code) throws ServiceException;

    /**
     * 修改
     *
     * @param code code
     */
    void updateCode(Code code);

    /**
     * 删除
     *
     * @param codeIds codeIds
     */
    void deleteCodes(String[] codeIds);
}
