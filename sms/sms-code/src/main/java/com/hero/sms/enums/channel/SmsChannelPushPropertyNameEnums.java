package com.hero.sms.enums.channel;

import com.hero.sms.enums.BaseEnum;

/**
 * 短信通道推送属性名称
 * @author Lenovo
 *
 */
public enum SmsChannelPushPropertyNameEnums implements BaseEnum {

	ReqUrl("reqUrl","请求地址"),
	QueryUrl("queryUrl","查询地址"),
	OrgNo("orgNo","商户编号"),
	Account("account","账号"),
	SignKey("signKey","密钥"),
	Charset("charset","字符编码"),
	ConnectTimeout("connectTimeout","连接超时时间"),
	ReadTimeout("readTimeout","读取超时时间"),
	RequestMethod("requestMethod","请求方式"),
	Host("host","ip地址"),
	Port("port","端口"),
	SystemId("systemId","接口号"),
	Password("password","密码"),
	SystemType("systemType","接口类型"),
	AddrTon("addrTon","编码类型"),
	AddrNpi("addrNpi","编码方案"),
	MaxChannels("maxChannels","最大连接数"),
	Version("version","版本号"),
	Speed("speed","流速"),
	WriteLimit("writeLimit","写入速率"),
	ReadLimit("readLimit","读取速率"),
	OpenNumberPool("openNumberPool","是否开启号码池"),
	NumberPoolGroup("numberPoolGroup","号码池组");
	private String code;
	private String name;
	private SmsChannelPushPropertyNameEnums(String code, String name) {
		this.code = code;
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
}
