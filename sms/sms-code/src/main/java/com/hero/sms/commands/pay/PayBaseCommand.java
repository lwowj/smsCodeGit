package com.hero.sms.commands.pay;

import org.apache.commons.chain.Command;

/**
 * 商户充值
 */
public abstract class PayBaseCommand implements Command {

    /**
     * 错误信息
     */
    public static final String STR_ERROR_INFO = "STR_ERROR_INFO";

    /**
     * 商户充值命令链名称
     */
    public static final String PAY_COMMAND_NAME = "pay";

    /**
     * 商户充值回调命令链名称
     */
    public static final String PAY_RESULT_COMMAND_NAME = "payResult";

    /**
     *
     */
    public static final String PAY_DATA = "PAY_DATA";

    /**
     * 支付链接
     */
    public static final String PAY_URL = "payUrl";

    /**
     * 支付链接类型
     */
    public static final String PAY_URL_TYPE = "payUrlType";

    /**
     * 充值金额
     */
    public static final String AMOUNT = "amount";

    /**
     * 充值金额
     */
    public static final String NETWAY_CODE = "netwayCode";

    /**
     *
     */
    public static final String ORG_SERVICE = "ORG_SERVICE";

    /**
     *
     */
    public static final String PAY_CHANNEL_SERVICE = "PAY_CHANNEL_SERVICE";

    /**
     *
     */
    public static final String RECHARGE_ORDER_SERVICE = "RECHARGE_ORDER_SERVICE";

    /**
     *
     */
    public static final String ORG = "org";

    /**
     *
     */
    public static final String PAY_CHANNEL_LIST = "PAY_CHANNEL_LIST";

    /**
     *
     */
    public static final String PAY_CHANNEL = "PAY_CHANNEL";

    /**
     *
     */
    public static final String RECHARGE_ORDER = "RECHARGE_ORDER";



}
