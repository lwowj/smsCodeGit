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
import com.hero.sms.entity.organization.ext.OrganizationUserExt;
import com.hero.sms.enums.msg.MessageReadStatusEnums;
import com.hero.sms.mapper.msg.MessageMapper;
import com.hero.sms.service.msg.IMsgService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 站内信 Service实现
 *
 * @author Administrator
 * @date 2020-08-16 17:26:03
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class MsgServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMsgService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public IPage<Message> findMessages(QueryRequest request, Message message, boolean isAdmin) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotEmpty(message.getCreateUser())){
            queryWrapper.eq(Message::getCreateUser,message.getCreateUser());
        }
        if(StringUtils.isNotEmpty(message.getOrgCode())){
            queryWrapper.eq(Message::getOrgCode,message.getOrgCode());
        }
        if(StringUtils.isNotEmpty(message.getMessageType())){
            queryWrapper.eq(Message::getMessageType,message.getMessageType());
        }
        if(StringUtils.isNotEmpty(message.getMerchReadStatus())){
            queryWrapper.eq(Message::getMerchReadStatus,message.getMerchReadStatus());
        }
        if(StringUtils.isNotEmpty(message.getMessageContact())){
            queryWrapper.like(Message::getMessageContact,message.getMessageContact());
        }
        if(StringUtils.isNotEmpty(message.getAdminReadStatus())){
            queryWrapper.eq(Message::getAdminReadStatus,message.getAdminReadStatus());
        }
        // TODO 设置查询条件
        Page<Message> page = new Page<>(request.getPageNum(), request.getPageSize());
        if(isAdmin){
            queryWrapper.orderByDesc(Message::getAdminReadStatus);
        }else {
            queryWrapper.orderByDesc(Message::getMerchReadStatus);
        }
        queryWrapper.orderByDesc(Message::getId);
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Message> findMessages(Message message) {
	    LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 查询是否有新消息
     * @param userAccount
     * @return
     */
    @Override
    public boolean queryNewMsg(String userAccount){
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotEmpty(userAccount)){
            queryWrapper.eq(Message::getCreateUser,userAccount);
            queryWrapper.eq(Message::getMerchReadStatus,MessageReadStatusEnums.UNREAD.getCode());
        }else {
            queryWrapper.eq(Message::getAdminReadStatus,MessageReadStatusEnums.UNREAD.getCode());
        }
        List<Message> list = this.baseMapper.selectList(queryWrapper);
        if(list!=null && list.size() > 0){
            return true;
        }
        return false;
    }

    /**
     * 获取消息
     * @param id
     * @param userAccount
     * @return
     */
    @Override
    public Message findMessages(Integer id, String userAccount) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        if(StringUtils.isNotEmpty(userAccount)){
            queryWrapper.eq(Message::getCreateUser,userAccount);
        }
        queryWrapper.eq(Message::getId,id);
        return this.baseMapper.selectOne(queryWrapper);
    }
    @Override
    @Transactional
    public void createMessage(Message message, OrganizationUserExt userExt) {
        try {
            if(StringUtils.isEmpty(message.getMessageType()) || StringUtils.isEmpty(message.getMessageContact())){
                throw new ServiceException("信息不完整");
            }
            if(message.getPictureFile() != null){
                Code path = DatabaseCache.getCodeBySortCodeAndCode("System","logoFilePath");
                Code size = DatabaseCache.getCodeBySortCodeAndCode("System","msgPictureSize");
                StringBuffer imgPath = new StringBuffer();
                imgPath.append("/msg/").append(userExt.getUserAccount()).append("/").append(DateUtil.getString(new Date(),DateUtil.FULL_TIME_PATTERN)).append(".png");
                FileUtil.savePpictureFile(message.getPictureFile(),path.getName() + imgPath.toString(),Long.parseLong(size.getName()));
                message.setPictureUrl("/res" + imgPath.toString());
            }
            message.setOrgCode(userExt.getOrganizationCode());
            message.setAdminReadStatus(MessageReadStatusEnums.UNREAD.getCode());
            message.setMerchReadStatus(MessageReadStatusEnums.READ.getCode());
            message.setCreateUser(userExt.getUserAccount());
            message.setCreateDate(new Date());
            try {
            	this.save(message);
			} catch (Exception e) {
				throw new ServiceException("信息提交异常");
			} 
        }catch (Exception e){
            throw new FebsException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createMessage(Message message){
        try {
            if(StringUtils.isEmpty(message.getMessageType()) || StringUtils.isEmpty(message.getMessageContact())){
                throw new ServiceException("信息不完整");
            }
            if(message.getPictureFile() != null){
                Code path = DatabaseCache.getCodeBySortCodeAndCode("System","logoFilePath");
                Code size = DatabaseCache.getCodeBySortCodeAndCode("System","msgPictureSize");
                StringBuffer imgPath = new StringBuffer();
                String createUser = StringUtils.isNotBlank(message.getCreateUser())?message.getCreateUser():"system";
                imgPath.append("/msg/").append(createUser).append("/").append(DateUtil.getString(new Date(),DateUtil.FULL_TIME_PATTERN)).append(".png");
                FileUtil.savePpictureFile(message.getPictureFile(),path.getName() + imgPath.toString(),Long.parseLong(size.getName()));
                message.setPictureUrl("/res" + imgPath.toString());
            }
            message.setCreateDate(new Date());
            this.save(message);
        }catch (Exception e){
            throw new FebsException(e.getMessage());
        }
    }
    @Override
    @Transactional
    public void updateMessage(Message message) {
        this.saveOrUpdate(message);
    }

    @Override
    @Transactional
    public void deleteMessage(Message message) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteMessages(String[] messageIds) {
        List<String> list = Arrays.asList(messageIds);
        this.removeByIds(list);
    }
}
