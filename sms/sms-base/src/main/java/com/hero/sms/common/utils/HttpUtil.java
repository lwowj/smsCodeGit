package com.hero.sms.common.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtil {

	private static int connectTimeout = 5000;
	private static int readTimeout = 5000;
	
    /**
     * post请求
     * @param url       目的url
     * @param params    发送的参数
     */
    public static String sendPostRequest(String url, Object params){
    	SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();  
    	requestFactory.setConnectTimeout(connectTimeout);// 设置超时  
    	requestFactory.setReadTimeout(readTimeout);
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    	return sendPostRequest(url,requestFactory,headers,params);
    }
    /**
     * post请求
     * @param url       目的url
     * @param params    发送的参数
     */
    public static String sendPostRequest(String url,ClientHttpRequestFactory requestFactory,HttpHeaders headers, Object params){
    	RestTemplate client = new RestTemplate(requestFactory);
    	HttpMethod method = HttpMethod.POST;
    	//将请求头部和参数合成一个请求
    	HttpEntity<Object> requestEntity = new HttpEntity<>(params, headers);
    	//执行HTTP请求，将返回的结构使用ResultVO类格式化
    	ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);
    	return response.getBody();
    }

	public static String getResponseBodyAsString(InputStream in, String charset) {
		try {
			// 解决php BOM头问题
			BOMInputStream bomIn = new BOMInputStream(in, false, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE,
					ByteOrderMark.UTF_16BE);
			// 若检测到bom，则使用bom对应的编码
			if (bomIn.hasBOM()) {
				charset = bomIn.getBOMCharsetName();
			}
			BufferedInputStream buf = new BufferedInputStream(bomIn);
			byte[] buffer = new byte[1024];
			StringBuffer data = new StringBuffer();
			int readDataLen;
			while ((readDataLen = buf.read(buffer)) != -1) {
				data.append(new String(buffer, 0, readDataLen, charset));
			}
			try {
				buf.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return data.toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 
	 * @param mapParam
	 *            参数map
	 * @param kvMark
	 *            key和value连接标识
	 * @param domainMark
	 *            域连接标识
	 * @param isJoinKey
	 *            是否连接key
	 * @param isJoinEmptyValue
	 *            是否连接空值
	 * @return
	 */
	public static String mapToParam(Map<String, String> mapParam, String kvMark, String domainMark, boolean isJoinKey,
			boolean isJoinEmptyValue) {
		StringBuffer params = new StringBuffer();
		for (Map.Entry<String, String> p : mapParam.entrySet()) {
			String value = (String) p.getValue();
			boolean isEmptyValue = StringUtils.isBlank(value);
			boolean isJoinValue = isJoinEmptyValue ? true : !isEmptyValue;
			if (!isJoinValue) {
				continue;
			}
			if (isJoinKey) {
				params.append(p.getKey());
				params.append(kvMark);
			}
			if (isJoinValue) {
				params.append(p.getValue());
				params.append(domainMark);
			}
		}
		if (StringUtils.isNotBlank(domainMark) && params.length() > 0) {
			return params.substring(0, params.lastIndexOf(domainMark));
		}
		return params.toString();
	}
	
	public static String mapToParam(Map<String, String> mapParam) {
		StringBuffer params = new StringBuffer();
		for (Map.Entry<String, String> p : mapParam.entrySet()) {
			params.append(p.getKey());
			params.append("=");
			params.append(p.getValue());
			params.append("&");
		}
		if (params.length() > 0) {
			params.deleteCharAt(params.length() - 1);
		}
		return params.toString();
	}
	
	public static String mapToJson(Map<String, String> map) {
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		StringBuffer json = new StringBuffer();
		json.append("{");
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			json.append("\"").append(key).append("\"");
			json.append(":");
			json.append("\"").append(value).append("\"");
			if (it.hasNext()) {
				json.append(",");
			}
		}
		json.append("}");
		return json.toString();
	}
	
	public static class DefaultTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	}
	
	public static boolean isHttps(String url) {
		return url.startsWith("https") || url.startsWith("HTTPS");
	}
	
	public static String request(ConnectionParams cp) {
		if (cp.getRequestMethod().equals("POST")) {
			return resultPost(cp).getResOrErr();
		}
		if (cp.getRequestMethod().equals("GET")) {
			return resultGet(cp).getResOrErr();
		}
		return "请求方法出错";
	}
	
	public static RequestResult resultPost(ConnectionParams connParams) {
		HttpURLConnection conn = null;
		try {
			URL urlObj = new URL(connParams.getRequestUrl());
			conn = (HttpURLConnection) urlObj.openConnection();
			if (isHttps(connParams.getRequestUrl())) {
				SSLContext ctx = SSLContext.getInstance("SSL", "SunJSSE");
				ctx.init(null, new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
				((HttpsURLConnection) conn).setSSLSocketFactory(ctx.getSocketFactory());
			}
			log.info("请求报文:" + connParams.getContent());
			conn.setRequestMethod(connParams.getRequestMethod());
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(connParams.getConnectTimeout());
			conn.setReadTimeout(connParams.getReadTimeout());

			Properties requestProperties = connParams.getRequestProperty();
			for (Map.Entry<Object, Object> entry : requestProperties.entrySet()) {
				conn.setRequestProperty(entry.getKey().toString(), entry.getValue().toString());
			}
			conn.setRequestProperty("Content-Length", String.valueOf(connParams.getContent().length()));
			OutputStream outStream = conn.getOutputStream();
			outStream.write(connParams.getContent().getBytes(connParams.getCharset()));
			outStream.flush();
			outStream.close();
			String resStr = null;
			// 302:重定向
			if (conn.getResponseCode() == 200 || conn.getResponseCode() == 302) {
				resStr = getResponseBodyAsString(conn.getInputStream(),connParams.getCharset());
				return new RequestResult(conn, resStr, null);
			} else {
				return new RequestResult(conn, null, "请求异常:" + String.valueOf(conn.getResponseCode()));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new RequestResult(conn, null, e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	public static RequestResult resultGet(ConnectionParams connParams) {
		HttpURLConnection conn = null;
		try {
			// GET请求直接在链接后面拼上请求参数
			URL urlObj = new URL(connParams.getRequestUrl() + "?" + connParams.getContent());
			conn = (HttpURLConnection) urlObj.openConnection();
			conn.setRequestMethod("GET");
			// Get请求不需要DoOutPut
			conn.setDoOutput(false);
			conn.setDoInput(true);
			// 设置连接超时时间和读取超时时间
			conn.setConnectTimeout(connParams.getConnectTimeout());
			conn.setReadTimeout(connParams.getReadTimeout());
			Properties requestProperties = connParams.getRequestProperty();
			for (Map.Entry<Object, Object> entry : requestProperties.entrySet()) {
				conn.setRequestProperty(entry.getKey().toString(), entry.getValue().toString());
			}
			// 连接服务器
			conn.connect();
			// 取得输入流，并使用Reader读取
			String resStr = null;
			if (conn.getResponseCode() == 200 || conn.getResponseCode() == 302) {
				resStr = getResponseBodyAsString(conn.getInputStream());
				return new RequestResult(conn, resStr, null);
			} else {
				return new RequestResult(conn, null, "请求异常:" + String.valueOf(conn.getResponseCode()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new RequestResult(conn, null, e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	public static String getResponseBodyAsString(InputStream in) {
		try {
			// 解决php BOM头问题
			BOMInputStream bomIn = new BOMInputStream(in, false,
					ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE,
					ByteOrderMark.UTF_16BE);
			String charset = "UTF-8";
			// 若检测到bom，则使用bom对应的编码
			if (bomIn.hasBOM()) {
				charset = bomIn.getBOMCharsetName();
			}
			BufferedInputStream buf = new BufferedInputStream(bomIn);
			byte[] buffer = new byte[1024];
			StringBuffer data = new StringBuffer();
			int readDataLen;
			while ((readDataLen = buf.read(buffer)) != -1) {
				data.append(new String(buffer, 0, readDataLen, charset));
			}
			if (log.isDebugEnabled()) {
				log.info(data.toString());
			}
			try {
				buf.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return data.toString();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
//	public static String requestJson(String url, String params) 
//	{
//		return requestPostJson(url, params,60000).getResStr();
//	} 
//	
//	public static String requestJson(String url, String params,int timeout) 
//	{
//		return requestPostJson(url, params,timeout).getResStr();
//	} 
//	
//	public static String requestJson(String url, String params,int timeout,String charset) 
//	{
//		return requestPostJson(url, params,timeout,charset).getResStr();
//	} 
//	
//	public static String requestJson(String url, String params,int connectTimeout,int readTimeout) 
//	{
//		return requestPostJson(url, params,connectTimeout,readTimeout,"UTF-8").getResStr();
//	} 
//	
//	public static String requestJson(String url, String params,int connectTimeout,int readTimeout,String charset) 
//	{
//		return requestPostJson(url, params,connectTimeout,readTimeout,charset).getResStr();
//	}
//	
//	public static RequestResult requestPostJson(String qUrl, String jsonStr)
//	{
//		return requestPostJson(qUrl, jsonStr, 60000);
//	}
//	
//	public static RequestResult requestPostJson(String qUrl, String jsonStr,int timeout)
//	{
//		return requestPostJson(qUrl, jsonStr, timeout,"UTF-8");
//	}
//	
//	public static RequestResult requestPostJson(String qUrl, String jsonStr,int timeout,String charset)
//	{
//		return requestPostJson(qUrl, jsonStr, timeout,timeout,charset);
//	}
//	
//	public static RequestResult requestPostJson(String qUrl, String jsonStr,int connectTimeout,int readTimeout,String charset)
//    { 
//		HttpURLConnection conn = null; 
//        try {
//        	URL urlObj = new URL(qUrl);
//			conn = (HttpURLConnection) urlObj.openConnection();
//			conn.setRequestMethod("POST");
//			conn.setDoOutput(true);
//			conn.setDoInput(true);
//			conn.setUseCaches(false);
//			conn.setConnectTimeout(connectTimeout);
//			conn.setReadTimeout(readTimeout);
//            // 设置文件类型:
//            conn.setRequestProperty("Content-Type", "application/json"); 
//			conn.setRequestProperty("Content-Length",String.valueOf(jsonStr.length()));
//			OutputStream outStream = conn.getOutputStream();
//			outStream.write(jsonStr.toString().getBytes(charset));
//			outStream.flush();
//			outStream.close();
//			String resStr = null;
//			if (conn.getResponseCode() == 200) {
//				resStr = getResponseBodyAsString(conn.getInputStream(),charset);
//				return new RequestResult(conn, resStr, null);
//			} else {
//				return new RequestResult(conn, null, "请求异常:"
//						+ String.valueOf(conn.getResponseCode()));
//			} 	
//		 } catch (Exception e) {
//				return new RequestResult(conn, null, e.getMessage());
//		}
//    } 
}
