package com.hero.sms.service.impl.msg;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.DatabaseCache;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.exception.FebsException;
import com.hero.sms.common.exception.ServiceException;
import com.hero.sms.common.utils.DateUtil;
import com.hero.sms.common.utils.FileUtil;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.msg.Message;
import com.hero.sms.entity.msg.MessageReply;
import com.hero.sms.enums.msg.MessageReadStatusEnums;
import com.hero.sms.mapper.msg.MessageMapper;
import com.hero.sms.mapper.msg.MessageReplyMapper;
import com.hero.sms.service.msg.IMessageReplyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 站内信回复 Service实现
 *
 * @author Administrator
 * @date 2020-08-16 17:26:11
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class MessageReplyServiceImpl extends ServiceImpl<MessageReplyMapper, MessageReply> implements IMessageReplyService {

    @Autowired
    private MessageReplyMapper messageReplyMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public IPage<MessageReply> findMessageReplys(QueryRequest request, MessageReply messageReply) {
        LambdaQueryWrapper<MessageReply> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<MessageReply> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<MessageReply> findMessageReplys(MessageReply messageReply) {
	    LambdaQueryWrapper<MessageReply> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 商户端查询
     * @param messageReply
     * @param userAccount
     * @return
     */
    @Override
    public List<MessageReply> findMessageReplys(MessageReply messageReply, String userAccount, boolean isAdmin) {
        if(!isAdmin){
            checkUser(messageReply,userAccount);
        }
        Message newMsg = new Message();
        newMsg.setId(messageReply.getMessageId());
        if(!isAdmin){
            newMsg.setMerchReadStatus(MessageReadStatusEnums.READ.getCode());
        }else {
            newMsg.setAdminReadStatus(MessageReadStatusEnums.READ.getCode());
        }
        this.messageMapper.updateById(newMsg);
        LambdaQueryWrapper<MessageReply> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MessageReply::getMessageId,messageReply.getMessageId());
        queryWrapper.orderByDesc(MessageReply::getId);
        List<MessageReply> list = this.baseMapper.selectList(queryWrapper);
        list.stream().forEach(item -> {
        	
            /**
             * @begin
             * 2021-04-21
             * 【内容】：【本机校验文件是否存在，若不存在，则需要到同负载的其他服务器下载到本机】
             * 【说明】：这里需要新增校验agentSystemConfig.getSystemLogoUrl() 文件是否在本台服务器上，若不存在，则需要到另外的负载服务器上下载到本机
             * 【步骤】：
             * 1、使用文件工具判断文件地址的文件是否存在于本机上
             * 2、获取字典中负载服务器的IP地址
             * 3、遍历ip+文件地址，是否在目标服务器上，若存在，则下载到本机对应目录中，其后都直接读取本机目录文件；若不存在，则遍历下一个IP地址
             */
            
            /**
             * @end
             */
        	
            if(item.getCreateUser().equals(userAccount)){
                item.setCreateUser("你");
            }else {
                if(!isAdmin){
                    item.setCreateUser("管理员");
                }
            }
            if(!isAdmin){
                item.setRemark(null);
            }
        });

        // TODO 设置查询条件
        return list;
    }

    private void checkUser(MessageReply messageReply, String userAccount){
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getCreateUser,userAccount);
        queryWrapper.eq(Message::getId,messageReply.getMessageId());
        Message msg = this.messageMapper.selectOne(queryWrapper);
        if(msg == null){
            throw new FebsException("未找到该消息！");
        }
    }

    @Override
    @Transactional
    public void createMessageReply(MessageReply messageReply, String userAccount, boolean isAdmin) {
        try {
            if(StringUtils.isEmpty(messageReply.getReplyContact())){
                throw new ServiceException("信息不完整");
            }
            if(!isAdmin){
                checkUser(messageReply,userAccount);
            }
            if(messageReply.getPictureFile() != null){
                Code path = DatabaseCache.getCodeBySortCodeAndCode("System","logoFilePath");
                Code size = DatabaseCache.getCodeBySortCodeAndCode("System","msgPictureSize");
                StringBuffer imgPath = new StringBuffer();
                imgPath.append("/msg/").append(userAccount).append("/").append(DateUtil.getString(new Date(),DateUtil.FULL_TIME_PATTERN)).append(".png");
                FileUtil.savePpictureFile(messageReply.getPictureFile(),path.getName() + imgPath.toString(),Long.parseLong(size.getName()));
                messageReply.setPictureUrl("/res" + imgPath.toString());
            }
            messageReply.setCreateUser(userAccount);
            messageReply.setCreateDate(new Date());
            Message newMsg = new Message();
            newMsg.setId(messageReply.getMessageId());
            if(!isAdmin){
                newMsg.setAdminReadStatus(MessageReadStatusEnums.UNREAD.getCode());
            }else {
                newMsg.setMerchReadStatus(MessageReadStatusEnums.UNREAD.getCode());
            }
            try {
            	this.messageMapper.updateById(newMsg);
                this.save(messageReply);
			} catch (Exception e) {
				throw new ServiceException("信息回复异常");
			}
        }catch (Exception e){
            throw new FebsException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateMessageReply(MessageReply messageReply) {
        this.saveOrUpdate(messageReply);
    }

    @Override
    @Transactional
    public void deleteMessageReply(MessageReply messageReply) {
        LambdaQueryWrapper<MessageReply> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteMessageReplys(String[] messageReplyIds) {
        List<String> list = Arrays.asList(messageReplyIds);
        this.removeByIds(list);
    }
}
