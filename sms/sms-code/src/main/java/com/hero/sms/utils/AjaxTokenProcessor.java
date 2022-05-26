package com.hero.sms.utils;



import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author lxq
 * 日期时间: 2018-08-05 16:18:53 
 * 说明备注: 专门用来ajax 限制重复提交.
 *
 */
public class AjaxTokenProcessor {
	private static final Log LOG = LogFactory.getLog(AjaxTokenProcessor.class);
	private static final String inputName= "sessionToken";
	/**
	 * 用来做为session键值
	 */
	private static final String TRANSACTION_TOKEN_KEY= "TRANSACTION_TOKEN_KEY_AJAX";
	
	/**
	 * 登录使用
	 */
	public static final String LOGIN_TRANSACTION_TOKEN_KEY= "LOGIN_TRANSACTION_TOKEN_KEY";
	
	/**
	 * 登录行为校验使用
	 */
	public static final String LOGIN_BEHAVIORCHECK_TOKEN_KEY= "LOGIN_BEHAVIORCHECK_TOKEN_KEY";
	
	/**
	 * 手动输入号码发送短信使用
	 */
	public static final String SENDBOX_TXT_TRANSACTION_TOKEN_KEY= "SENDBOX_TXT_TRANSACTION_TOKEN_KEY";
	
	/**
	 * 文本发送短信使用
	 */
	public static final String SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY= "SENDBOX_TXTFILE_TRANSACTION_TOKEN_KEY";
	
	/**
	 * 表格发送短信使用
	 */
	public static final String SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY= "SENDBOX_EXECLFILE_TRANSACTION_TOKEN_KEY";
	
	/**
	 * 充值使用
	 */
	public static final String RECHARGE_TRANSACTION_TOKEN_KEY= "RECHARGE_TRANSACTION_TOKEN_KEY";
	
    /**
     * 创建一个实列.
     */
    private static AjaxTokenProcessor instance = new AjaxTokenProcessor();

    /**
     * 返回一个实列.
     */
    public static AjaxTokenProcessor getInstance() {
        return instance;
    }

    protected AjaxTokenProcessor() {
        super();
    }
    
    /**
     * 保存最近一次操作的值.
     */
    private long previous;

    /**
     * 取出做为表单的名称。
     * @return
     */
    public String getTokenName(){
    	return inputName;
    }
    
    /**
     * 调用判断一个令牌是否失效paytax。
     * 默认不清除原本的session缓存
     * 默认key为TRANSACTION_TOKEN_KEY
     * @param request
     * @return
     */
    public synchronized boolean isTokenValid(HttpServletRequest request,String value) 
    {
        return this.isTokenValid(request,false,TRANSACTION_TOKEN_KEY,value);
    }

    /**
     * 调用判断一个令牌是否失效paytax。
          * 默认不清除原本的session缓存
     * @param request
     * @param token_key 需传入key
     * @param value 需校验的值
     * @return
     */
    public synchronized boolean isTokenValid(HttpServletRequest request,String token_key,String value) 
    {
        return this.isTokenValid(request,false,token_key,value);
    }
    
    /**
     * 调用判断一个令牌是否失效paytax。
          * 默认不清除原本的session缓存
          * 默认key为TRANSACTION_TOKEN_KEY
     * @param request
     * @param reset 需传入是否清除原缓存
     * @param value 需校验的值
     * @return
     */
    public synchronized boolean isTokenValid(HttpServletRequest request,boolean reset,String value) 
    {
    	return this.isTokenValid(request,reset,TRANSACTION_TOKEN_KEY,value);
    }
    
    /**
     * 调用判断一个令牌是否失效
     * @param request
     * @param reset  是否清除原缓存标识
     * @param token_key	缓存key
     * @param value	需校验的值
     * @return
     */
    public synchronized boolean isTokenValid(HttpServletRequest request,boolean reset,String token_key,String value) 
    {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        //如果取session里的值为空或者说""，那么返回
        String saved = (String) session.getAttribute(token_key);
        if (saved == null || "".equals(saved)) {
            return false;
        }
        
        
        //当传进来为true时，说明返回一个新的session，否则返回空
        if (reset) {
            this.resetToken(request);
        }

        // 接收传过来的value的值。
        String token = value;//request.getParameter(Constants.TOKEN_KEY);
        if (token == null || "".equals(token)) {
            return false;
        }
        
        return saved.equals(token);
    }


    /**
     * 清除令牌
     * 默认key为TRANSACTION_TOKEN_KEY
     * @param request
     */
    public synchronized void resetToken(HttpServletRequest request) 
    {
    	resetToken(request, TRANSACTION_TOKEN_KEY);
    }

    public synchronized void resetToken(HttpServletRequest request,String token_key) 
    {
        HttpSession session = request.getSession(false);
        if (session == null) {
        	session = request.getSession();
        }
        session.removeAttribute(token_key);
    }
    
    /**
     * 保存令牌  返回生成字符值
     * 默认key为TRANSACTION_TOKEN_KEY
     * @param request
     */
    public synchronized String saveToken(HttpServletRequest request) {
        return saveToken(request, TRANSACTION_TOKEN_KEY);
    }

    public synchronized String saveToken(HttpServletRequest request,String token_key) 
    {
        HttpSession session = request.getSession();
        String token = generateToken(request);
        if (token != null) {
        	//再这里多加一个sessionID与前台比较,因为是ajax，在jsp页里也是直接用session.getID取到
        	//值加上在这里返回的token做为比对。
        	//注意：总的字符串是：token
        	String sessId_token = token;
            session.setAttribute(token_key, sessId_token);
        }
        return token;
    }
    
    /**
     * 生成字符
     * @param request The request we are processing
     */
    public synchronized String generateToken(HttpServletRequest request) {

        HttpSession session = request.getSession();
        try {
            byte id[] = session.getId().getBytes();
            long current = System.currentTimeMillis();
            if (current == previous) {
                current++;
            }
            previous = current;
            byte now[] = new Long(current).toString().getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(id);
            md.update(now);
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
        	LOG.error(e);
            return null;
        }

    }
    
    /**
     * 加密字符
     * @param buffer
     * @return
     */
    private String toHex(byte buffer[]) {
        StringBuffer sb = new StringBuffer(buffer.length * 2);
        for (int i = 0; i < buffer.length; i++) {
            sb.append(Character.forDigit((buffer[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(buffer[i] & 0x0f, 16));
        }
        return sb.toString();
    }
}
