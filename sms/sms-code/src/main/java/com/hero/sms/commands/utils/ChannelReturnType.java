package com.hero.sms.commands.utils;



/**
 * 通道返回类型
 * @author Lenovo
 *
 */
public interface ChannelReturnType {
	public static final int SUCCESS=0;//成功
	public static final int SIGN_ERROR=1;//签名错误
	public static final int NET_ERROR=2;//网络错误
	public static final int FAILD=3;//失败
	public static final int PROCCESSING=4;//处理中
	public static final int OTHER=5;//其他
	public static final int ORDER_EXIST=6;//订单已经存在
	public static final int NO_MONEY=7;//余额不足
	public static final int ACCOUNT_ERR=8;//账户状态异常
	public static final int REPEAT=9;//重复打款
	public static final int LIMIT=10;//超过限额
	public static final int FINISH=11;//代付上游接口是同步方式的，请求完成，不需要再去查询代付结果了
	public static final int INIT=12;//订单初始状态
}
