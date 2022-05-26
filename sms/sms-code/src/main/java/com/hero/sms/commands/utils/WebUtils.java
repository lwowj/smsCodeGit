package com.hero.sms.commands.utils;


import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * 
 * @Title:WebUtils
 * @Description:web交互常用工具单元
 * @author chenwz
 * @date 2017年8月3日 上午9:54:17
 */
public class WebUtils {
	
	/**
	 * 
	 * @Title: sort 
	 * @Description: 按照ASCII从小到大排序
	 * @param 
	 * @return Map<String,String>
	 * @throws
	 */
	public static Map<String, String> sort(Map<String, String> params) {
		Map<String, String> map = new TreeMap<String, String>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (!StringUtils.isEmpty(entry.getValue()))
				map.put(entry.getKey(), entry.getValue());
		}
		
		return map;
	}
	
	/**
	 * 
	 * @Title: concatParams 
	 * @Description: 拼接web请求参数，例：key1=value1&key2=value2 
	 * @param 
	 * @return String
	 * @throws
	 */
	public static String concatParams(Map<String, String> params) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (sb.length() > 0) 
				sb.append("&");
			
			sb.append(entry.getKey()).append("=").append(entry.getValue());			
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @Title: sortAndConcatParams 
	 * @Description: 按照ASCII排序并拼接 
	 * @param 
	 * @return String
	 * @throws
	 */
	public static String sortAndConcatParams(Map<String, String> params) {
		Map<String, String> sortMap = sort(params);
		return concatParams(sortMap);
	}
	
	/**
	 * 
	 * @Title: map2Json 
	 * @Description: Map转json ,注：此处jsonObject必须用net.jsonobject方可保证转字符串顺序正常
	 * @param 
	 * @return String
	 * @throws
	 */
	public static JSONObject map2Json(Map<String, String> params) {
		JSONObject jsonObj = new JSONObject();
		for (Map.Entry<String, String> param : params.entrySet()) {
			String value = param.getValue();
			if (!StringUtils.isEmpty(value)) {
				jsonObj.put(param.getKey(), param.getValue());	
			}
		}
		
		return jsonObj;
	}
	
	/**
	 * 
	 * @Title: map2JsonString 
	 * @Description: Map转json字符串 
	 * @param 
	 * @return String
	 * @throws
	 */
	public static String map2JsonString(Map<String, String> params) {
		return map2Json(params).toString();
	}
	
	/**
	 * 
	 * @Title: json2Map 
	 * @Description: 目前只做一级解析 
	 * @param 
	 * @return Map<String,String>
	 * @throws
	 */
	public static Map<String, String> json2Map(JSONObject jsonObj) 
	{
		Map<String, String> result = new HashMap<>();
		if (jsonObj != null) {			
			Iterator it = jsonObj.entrySet().iterator();
			while (it.hasNext()) 
			{
				Map.Entry entry = (Map.Entry) it.next();
				String key = String.valueOf(entry.getKey());
				String value = String.valueOf(entry.getValue());
				result.put(key, value);
			}
		}
		return result;
	}

	public static Map<String, String> json2Map(String jsonStr) {
		JSONObject jsonObj = JSONObject.parseObject(jsonStr);
		return json2Map(jsonObj);
	}
	
	public static void json2Map(JSONObject jsonObj, Map<String, String> map) {
		if (jsonObj != null) {
			Iterator it = jsonObj.entrySet().iterator();
			while (it.hasNext()) 
			{
				Map.Entry entry = (Map.Entry) it.next();
				String key = String.valueOf(entry.getKey());
				String value = String.valueOf(entry.getValue());
				map.put(key, value);
			}
		}
	}
	
	/**
	 * 
	 * @Title: xml2Map 
	 * @Description: XML转map，注：这种方式只把第一级的明细转成map
	 * @param 
	 * @return Map<String,String>
	 * @throws
	 */
	public static Map<String, String> xml2Map(Element e) {
		Map<String, String> rest = new HashMap<String, String>();
		
        List<Element> els = e.elements();
        for(Element el : els){
            rest.put(el.getName().toLowerCase(), el.getTextTrim());
        }
        return rest;
	}
	
	/**
	 * 
	 * @Title: xml2Map 
	 * @Description: XML字符串转map
	 * @param 
	 * @return Map<String,String>
	 * @throws IOException
	 */
	public static Map<String, String> xml2Map(String xmlString) throws IOException {
		Map<String, String> result = null;
		try {
			Document doc = DocumentHelper.parseText(xmlString);
			Element root = doc.getRootElement();
			result = xml2Map(root);
		} catch (DocumentException e) {
			throw new IOException("解析XML字符串失败！");
		}
		  
        return result;
	}
	
	/**
	 * xml转map 不带属性
	 * @param xmlStr
	 * @param needRootKey 是否需要在返回的map里加根节点键
	 * @return
	 * @throws DocumentException
	 */
	public static Map xmlToMap(String xmlStr, boolean needRootKey) throws DocumentException {
		Document doc = DocumentHelper.parseText(xmlStr);
		Element root = doc.getRootElement();
		Map<String, Object> map = (Map<String, Object>) xmlToMap(root);
		if(root.elements().size()==0 && root.attributes().size()==0){
			return map;
		}
		if(needRootKey){
			//在返回的map里加根节点键（如果需要）
			Map<String, Object> rootMap = new HashMap<String, Object>();
			rootMap.put(root.getName(), map);
			return rootMap;
		}
		return map;
	}

	/**
	 * xml转map 不带属性
	 * @param element
	 * @return
	 */
	private static Object xmlToMap(Element element) {
		Map<String, Object> map = new TreeMap<String, Object>();
		List<Element> elements = element.elements();
		if (elements.size() == 0) {
			map.put(element.getName(), element.getText());
			if (!element.isRootElement()) {
				return element.getText();
			}
		} else if (elements.size() == 1) {
			map.put(elements.get(0).getName(), xmlToMap(elements.get(0)));
		} else if (elements.size() > 1) {
			// 多个子节点的话就得考虑list的情况了，比如多个子节点有节点名称相同的
			// 构造一个map用来去重
			Map<String, Element> tempMap = new LinkedHashMap<String, Element>();
			for (Element ele : elements) {
				tempMap.put(ele.getName(), ele);
			}
			Set<String> keySet = tempMap.keySet();
			for (String string : keySet) {
				Namespace namespace = tempMap.get(string).getNamespace();
				List<Element> elements2 = element.elements(new QName(string,
						namespace));
				// 如果同名的数目大于1则表示要构建list
				if (elements2.size() > 1) {
					List<Object> list = new ArrayList<Object>();
					for (Element ele : elements2) {
						if(StringUtils.isEmpty(ele.getText())) continue;
						list.add(xmlToMap(ele));
					}
					map.put(string, list);
				} else {
					// 同名的数量不大于1则直接递归去
					map.put(string, xmlToMap(elements2.get(0)));
				}
			}
		}
		return map;
	}
	
	/**
	 * 
	 * @Title: map2XML 
	 * @Description: map转XML字符串
	 * @param 
	 * @return String
	 * @throws
	 */
	public static String map2XML(Map<String, String> params) {
		StringBuilder buf = new StringBuilder();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        buf.append("<xml>");
        for(String key : keys){
            buf.append("<").append(key).append(">");
            buf.append("<![CDATA[").append(params.get(key)).append("]]>");
            buf.append("</").append(key).append(">\n");
        }
        buf.append("</xml>");
        return buf.toString();
	}
	
	/**
	 * 
	 * @Title: bean2Map 
	 * @Description: Bean转换成map（目前只做一级转换） 
	 * @param 
	 * @return Map<String,String>
	 * @throws IOException
	 */
	public static Map<String, String> bean2Map(Object bean) throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		
		try {
			BeanInfo info = Introspector.getBeanInfo(bean.getClass());
			PropertyDescriptor[] props = info.getPropertyDescriptors();
			for (PropertyDescriptor prop : props) {
				String key = prop.getName();
				if (prop.getReadMethod() != null && prop.getWriteMethod() != null) {
					Method getter = prop.getReadMethod();
					Object value = getter.invoke(bean);
					if (value != null) {
						result.put(key, value.toString());
					}
				}
			}
		} catch (Exception e) {
			throw new IOException("BeanClass[" +bean.getClass().getName() + "]解析失败:" + e.getMessage());
		}
		
		return result;
	}
	
	/**
	 *   
	 * 
	 * @Title: map2Bean 
	 * @Description:  map内的值根据key赋值给bean对应的属性(目前只做一级转换)
	 * @param 
	 * @return void
	 * @throws IOException
	 */
	public static void map2Bean(Map<String, String> map, Object bean) throws IOException {
		if (map == null || map.size() == 0 || bean == null) 
			return;
		try {
			BeanInfo info = Introspector.getBeanInfo(bean.getClass());
			PropertyDescriptor[] props = info.getPropertyDescriptors();
			for (PropertyDescriptor prop : props) {
				String key = prop.getName();
				String value = map.get(key);
				if (StringUtils.isEmpty(value)) continue;				
				
				if (prop.getReadMethod() != null && prop.getWriteMethod() != null) {
					Method setter = prop.getWriteMethod();
					Class<?> propType = prop.getPropertyType();
					if (Integer.class.isAssignableFrom(propType)) {
						setter.invoke(bean, new Object[]{ Integer.parseInt(value.toString())});
						
					} else if (String.class.isAssignableFrom(propType)) {
						setter.invoke(bean, new Object[] { value.toString() });
						
					} else if (Boolean.class.isAssignableFrom(propType)) {
						setter.invoke(bean, new Object[] { Boolean.valueOf(value) });
						
					} else if (Date.class.isAssignableFrom(propType)) {
						setter.invoke(bean, new Object[] { Date.valueOf(value) });
						
					} else if (Float.class.isAssignableFrom(propType)) {
						setter.invoke(bean, new Object[] { Float.valueOf(value) });
					} else {
						setter.invoke(bean, new Object[] { value.toString() });
					}
				}
			}
		} catch (Exception e) {
			throw new IOException("BeanClass[" +bean.getClass().getName() + "]解析失败:" + e.getMessage());
		}
	}
	
	public static int getCheckResult(String resultJsonStr, String type) 
	{
		//校验数据是否为空
		if (StringUtils.isEmpty(resultJsonStr)) {
			return ChannelReturnType.NET_ERROR;
		}
		//判断数据是否json，是json则进行转换判断
		if ("JSON".equals(type)) {
			JSONObject retjson = null;
			try {
				retjson = JSONObject.parseObject(resultJsonStr);
			} catch (Exception e) {
				return ChannelReturnType.FAILD;
			}
		}
		//判断数据是否xml格式，是xml则进行转换判断
		if ("XML".equals(type)) {
			Map<String, String> resultXmlMap = null;
			try {
				 resultXmlMap = WebUtils.xml2Map(resultJsonStr);	
			} catch (Exception exception) {
				return ChannelReturnType.FAILD;
			}
		}
		return 0;
	}
}
