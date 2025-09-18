package com.hawk.game.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.util.HawkNumberConvert;

public class MapUtil {
	
	/**
	 * 对value 进行累加 当map中的value为空的时候当0处理.
	 * @param map
	 * @param k
	 * @param value
	 * @return
	 */
	public static <K> Integer appendIntValue(Map<K, Integer> map, K k, Integer value) {
		Integer oldValue = getIntValue(map, k);
		Integer newValue = value + oldValue;
		map.put(k, newValue);
		
		return newValue;
	}
	
	/**
	 * 为空的时候默认返回 0
	 * @param map
	 * @param k
	 * @return
	 */
	public static <K> Integer getIntValue(Map<K, Integer> map, K k) {
		return get(map, k, Integer.valueOf(0));
	}
	
	public static <K,V> V get(Map<K, V> map, K k, V v) {
		if (map == null || map.isEmpty()) {
			return v;
		} else {
			V tmp = map.get(k);
			return tmp == null ? v : tmp; 
		}
	}
	
	/**
	 * 最后还是选择了这种写法.
	 * 合并map
	 * @param mainMap
	 * @param slaveMap
	 * @return
	 */
	public static <K> void mergeMap(Map<K, Integer> mainMap, Map<K, Integer> slaveMap) {
		for (Entry<K, Integer> entry : slaveMap.entrySet()) {
			Integer mainValue = mainMap.get(entry.getKey());
			if (mainValue != null) {
				entry.setValue(entry.getValue() + mainValue);
			} else {
				mainMap.put(entry.getKey(), entry.getValue());
			}
		}  
	}
	
	/**
	 * 
	 * @param sourceMap
	 * @return
	 */
	public static Map<String, String> toStringString(Map<Integer, Integer> sourceMap) {
		Map<String,String> toMap = new HashMap<>();
		if (sourceMap == null || sourceMap.isEmpty()) {
			return toMap;
		}
		
		for (Entry<Integer, Integer> entry : sourceMap.entrySet()) {
			toMap.put(entry.getKey().toString(), entry.getValue().toString());
		}
		
		return toMap;
	}
	
	public static Map<Integer, Integer> toIntegerInteger(Map<String, String> sourceMap) {
		Map<Integer, Integer> toMap = new HashMap<>();
		if (sourceMap == null || sourceMap.isEmpty()) {
			return toMap;
		}
		
		for (Entry<String, String> entry : sourceMap.entrySet()) {
			toMap.put(Integer.parseInt(entry.getKey()), Integer.parseInt(entry.getValue()));
		}
				
		return toMap;
	}
	
	public static <FK, FV, TK, TV>  Map<TK, TV> map2Map(Map<FK, FV> sourceMap, Transform<FK, TK> tKey, Transform<FV, TV> tValue) {
		Map<TK, TV> toMap = new HashMap<>();
		sourceMap.forEach((k, v)->{
			toMap.put(tKey.transform(k), tValue.transform(v));
		});
		
		return toMap;
	}
	
	/**
	 * 自己转成自己.
	 * @param clazz
	 * @return
	 */
	public static <F> Transform<F, F> genSelfTransform(Class<F> clazz)  {
		return (F _f) -> {
			return _f;
		};
	}
	
	@FunctionalInterface
	public static interface Transform<F, T> {
		T transform(F f);
	}
	
	public static Transform<Long, String> LONG2STRING = (Long value) -> {
		return value.toString(); 
	};
	
	public static Transform<String, Long> STRING2LONG = (String value) -> {
		return Long.valueOf(value);
	};
	
	public static Transform<String, Integer> STRING2INTEGER = (String value) -> {
		return Integer.valueOf(value);
	};
	
	public static Transform<Integer, String> INTEGER2STRING = (Integer value) -> {
		return value.toString();
	};	
	
	public static Transform<String, String> STRING2STRING = (String value) ->{
		return value;
	};
}
