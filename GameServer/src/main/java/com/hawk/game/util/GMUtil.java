package com.hawk.game.util;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * GM工具类
 * 
 * @author Nannan.Gao
 * @date 2016-9-22 15:52:19
 */
public class GMUtil {
	/**
	 * 解析gm请求参数
	 * 
	 * @param params
	 * @return
	 */
	public static JSONObject parseGmParams(String params) {
		try {
			params = HawkOSOperator.urlDecode(params);
			JSONObject json = JSON.parseObject(params);
			return json;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 请求处理失败返回数据
	 * 
	 * @param clase
	 * @param message
	 * @return
	 */
	public static String gmResultFailed(Class<?> clase, String message) {
		JSONObject result = new JSONObject();
		result.put("status", "1");
		result.put("message", clase.getSimpleName() + " : " + message);
		result.put("data", null);
		return result.toJSONString();
	}

	/**
	 * 请求处理成功返回数据
	 * 
	 * @param jsonData
	 * @return
	 */
	public static<T extends JSON> String gmResultSuccess(T jsonData) {
		JSONObject result = new JSONObject();
		result.put("status", "0");
		result.put("message", "success");
		result.put("data", jsonData);
		return result.toJSONString();
	}
	
	
}
