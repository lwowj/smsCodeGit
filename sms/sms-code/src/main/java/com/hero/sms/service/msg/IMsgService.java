package com.hero.sms.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.msg.Message;
import com.hero.sms.entity.organization.ext.OrganizationUserExt;

import java.util.List;

/**
 * 站内信 Service接口
 *
 * @author Administrator
 * @date 2020-08-16 17:26:03
 */
public interface IMsgService extends IService<Message> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param message message
     * @return IPage<Message>
     */
    IPage<Message> findMessages(QueryRequest request, Message message, boolean isAdmin);

    /**
     * 查询（所有）
     *
     * @param message message
     * @return List<Message>
     */
    List<Message> findMessages(Message message);

    /**
     * 查询是否有新消息
     * @param userAccount
     * @return
     */
    boolean queryNewMsg(String userAccount);

    /**
     * 获取消息
     * @param id
     * @param userAccount
     * @return
     */
    Message findMessages(Integer id, String userAccount);

    /**
     * 新增
     *
     * @param message message
     */
    void createMessage(Message message, OrganizationUserExt userExt);

    /**
     * 新增
     *
     * @param message message
     */
    void createMessage(Message message);

    /**
     * 修改
     *
     * @param message message
     */
    void updateMessage(Message message);

    /**
     * 删除
     *
     * @param message message
     */
    void deleteMessage(Message message);

    /**
    * 删除
    *
    * @param messageIds messageIds
    */
    void deleteMessages(String[] messageIds);
}
