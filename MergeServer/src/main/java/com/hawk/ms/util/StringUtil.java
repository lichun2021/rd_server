package com.hawk.ms.util;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hawk.os.HawkOSOperator;


public class StringUtil {
	@SuppressWarnings("unchecked")
	public static <T> List<T> str2List(String sourceString, String splitSymbol, Class<T> clazz) {
		List<T> list = new ArrayList<>();
		if (StringUtils.isEmpty(sourceString)) {
			return list;
		}
		
		if (clazz != Integer.class && clazz != String.class && clazz != Long.class) {
			throw new RuntimeException("unsupport class type : " +  clazz.getSimpleName());
		}
		
		String strs[] = sourceString.split(splitSymbol);
		for (String tmpStr : strs) {
			if (clazz == Integer.class) {
				list.add((T)Integer.valueOf(tmpStr));
			} else if (clazz == Long.class) {
				list.add((T)Long.valueOf(tmpStr));
			} else {
				list.add((T)tmpStr);
			}
		}
		
		return list;
	}
	
	public static <K, V> Map<K, V> str2Map(String sourceStr, String rowSpliter, String columnSpliter, Class<K> kclass, Class<V> vclass) {
		Map<K, V> map = new HashMap<>();
		if (HawkOSOperator.isEmptyString(sourceStr)) {
			return map;
		}
		
		if (!isSupportType(kclass)) {
			throw new InvalidParameterException("unsupport type");
		}
		
		if (!isSupportType(vclass)) {
			throw new InvalidParameterException("unsupport type");
		}
		
		String[] rows = sourceStr.split(rowSpliter);
		for (String row : rows) {
			String[] columns = row.split(columnSpliter);
			K k = transform(columns[0], kclass);
			V v = transform(columns[1], vclass);
			map.put(k, v);
		}
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T transform(String str, Class<T> clazz) {
		if (HawkOSOperator.isEmptyString(str)) {
			return null;
		}
		
		if (clazz == Integer.class) {
			return (T) Integer.valueOf(str);
		} else if (clazz == Long.class) {
			return (T) Long.valueOf(str);
		} else if (clazz == String.class) {
			return (T) str;
		} else {
			return null;
		}
	}
	private static <T> boolean isSupportType(Class<T> clazz) {
		if (clazz == Integer.class || clazz == Long.class || clazz == String.class) {
			return true;
		} else {
			return false;
		}
	}
}
