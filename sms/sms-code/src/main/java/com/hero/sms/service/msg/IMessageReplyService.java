package com.hero.sms.service.msg;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.msg.MessageReply;

import java.util.List;

/**
 * 站内信回复 Service接口
 *
 * @author Administrator
 * @date 2020-08-16 17:26:11
 */
public interface IMessageReplyService extends IService<MessageReply> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param messageReply messageReply
     * @return IPage<MessageReply>
     */
    IPage<MessageReply> findMessageReplys(QueryRequest request, MessageReply messageReply);

    /**
     * 查询（所有）
     *
     * @param messageReply messageReply
     * @return List<MessageReply>
     */
    List<MessageReply> findMessageReplys(MessageReply messageReply);

    /**
     *查询
     * @param messageReply
     * @param userAccount
     * @param isAdmin
     * @return
     */
    List<MessageReply> findMessageReplys(MessageReply messageReply, String userAccount,boolean isAdmin);

    /**
     * 新增
     *
     * @param messageReply messageReply
     */
    void createMessageReply(MessageReply messageReply, String userAccount, boolean isAdmin);

    /**
     * 修改
     *
     * @param messageReply messageReply
     */
    void updateMessageReply(MessageReply messageReply);

    /**
     * 删除
     *
     * @param messageReply messageReply
     */
    void deleteMessageReply(MessageReply messageReply);

    /**
    * 删除
    *
    * @param messageReplyIds messageReplyIds
    */
    void deleteMessageReplys(String[] messageReplyIds);
}
