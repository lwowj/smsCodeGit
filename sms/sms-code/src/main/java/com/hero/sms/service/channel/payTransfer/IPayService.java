package com.hero.sms.service.channel.payTransfer;

import org.springframework.transaction.annotation.Transactional;

import com.hero.sms.common.entity.FebsResponse;

/**
 * 交易
 */
public interface IPayService {


    /**
     * 商户后台充值交易
     * @return
     */
    @Transactional
    FebsResponse pay(String data);

    /**
     * 商户后台充值交易回调
     * @return
     */
    @Transactional
    FebsResponse payResult(String data) throws Exception;
}
