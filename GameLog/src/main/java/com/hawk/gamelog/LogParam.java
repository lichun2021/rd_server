package com.hawk.gamelog;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import java.util.Set;

/**
 * 日志参数封装
 * 
 * @author lating
 *
 */
public class LogParam {
	
	/**
	 * 日志参数字段-值Map
	 */
	private Map<String, Object> paramMap = new HashMap<String, Object>();
	
	public LogParam(boolean tlogEnable, String serverId) {
		paramMap.put("tlogEnable", tlogEnable);
		paramMap.put("serverId", serverId);
	}
	
	/**
	 * 添加日志参数
	 * @param key
	 * @param value
	 * @return
	 */
	public LogParam put(String key, Object value) {
		paramMap.put(key, value);
		return this;
	}
	
	/**
	 * 根据参数字段获取对应的值
	 * @param key
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(String key) throws Exception {
		Object val = paramMap.get(key);
		if(val == null) {
			throw new Exception("param key not exist: " + key);
		}
		
		return (T)val; 
	}
	
	/**
	 * 获取日志参数的字段列表
	 * @return
	 */
	public Set<String> keySet() {
		return paramMap.keySet();
	}
	
	/**
	 * 判断日志参数Map中是否包含指定字段
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key) {
		return paramMap.containsKey(key);
	}
	
	/**
	 * 
	 * @return
	 */
	public Set<Entry<String, Object>> entrySet() {
		return paramMap.entrySet();
	}
	
	/**
	 * 根据参数字段获取对应的值对象
	 * @param key
	 * @return
	 */
	public Object getObject(String key) {
		return paramMap.get(key);
	}
	
	/**
	 * 拼接TLOG信息
	 * @param time
	 * @return
	 * @throws Exception
	 */
	public String joinToString() {
		try {
			String logType = get("logType");
			LogTableCfg tableCfg = GameLog.getInstance().getLogTable(logType);
			if(tableCfg == null) {
				return null;
			}
			
			put("dtEventTime", HawkTime.formatNowTime());
			put("gameSvrId", get("serverId"));
			put("sequence", 0);
			StringBuilder sb = new StringBuilder();
			sb.append(tableCfg.getTableName());
			
			for(String key : tableCfg.getAttrList()) {
				sb.append("|" + get(key));
			}
			
			return sb.toString();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return null;
	}
	
}
