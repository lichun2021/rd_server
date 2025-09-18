package com.hawk.serialize.string;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import org.hawk.asm.HawkAsmConstructor;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSON;

/**
 * 字符串序列化、反序列化帮助类
 * @author PhilChen
 *
 */
public class SerializeHelper {

	/** 下划线分隔符*/
	public static final String ATTRIBUTE_SPLIT = "_";

	/** 逗号分隔符*/
	public static final String BETWEEN_ITEMS = ",";
	
	/** 冒号分隔符*/
	public static final String COLON_ITEMS = ":";
	
	/** 分号分隔符*/
	public static final String SEMICOLON_ITEMS = ";";

	/** 竖线"|"分隔，调用String.split时不能用ELEMENT_DELIMITER，请使用ELEMENT_SPLIT*/
	public static final String ELEMENT_DELIMITER = "|";

	/** 以竖线"|"拆分字符串*/
	public static final String ELEMENT_SPLIT = "\\|";
	
	static Map<Class<? extends SplitEntity>, HawkAsmConstructor<? extends SplitEntity>> METHOD_MAP = new HashMap<>();
	
	private static HawkAsmConstructor<? extends SplitEntity> getConstructor(Class<? extends SplitEntity> type) {
		HawkAsmConstructor<? extends SplitEntity> asmConstructor = METHOD_MAP.get(type);
		if (asmConstructor == null) {
			asmConstructor = HawkAsmConstructor.valueOf(type);
			METHOD_MAP.put(type, asmConstructor);
		}
		return asmConstructor;
	}
	
	public static boolean isBlank(String str) {
		if (str == null || str.equals("")) {
			return true;
		}
		return false;
	}

	public static String[] split(String str, String splitable) {
		if (isBlank(str) || isBlank(splitable)) {
			return new String[0];
		}
		String[] items = str.split(splitable);
		return items;
	}

	/**
	 * 填充制定数组到制定长度 
	 * @param src		源数组
	 * @param len		指定长度
	 * @param content	填充内容
	 * @return			生成的新数组，如果长度小于源数组长度，返回源数组
	 */
	public static String[] fillStringArray(String[] src, int len, String content) {
		if (src == null || src.length >= len) {
			return src;
		}
		if (content == null) {
			content = "";
		}
		String[] data = new String[len];
		for (int i = 0; i < len; i++) {
			if (i >= src.length) {
				data[i] = content;
			} else {
				if (src[i].isEmpty()) {
					src[i] = content;
				}
				data[i] = src[i];
			}
		}
		return data;
	}

	public static <T> Set<T> stringToSet(Class<T> tClass, String str, String splitable) {
		return stringToSet(tClass, str, splitable, null);
	}
	
	/**
	 * 字符串转Set
	 */
	public static <T> Set<T> stringToSet(Class<T> tClass, String str, String splitable, String splitableEntity) {
		return stringToSet(tClass, str, splitable, splitableEntity, null);
	}
	
	/**
	 * 字符串转Set
	 */
	public static <T> Set<T> stringToSet(Class<T> tClass, String str, String splitable, String splitableEntity, Set<T> initSet) {
		Set<T> set = null;
		
		if (initSet == null) {
			set = new HashSet<>();
		} else {
			set = initSet;
		}
		
		if (HawkOSOperator.isEmptyString(str)) {
			return set;
		}
		
		String[] split = str.split(splitable);
		for (String string : split) {
			T value = getValue(tClass, string, splitableEntity);
			set.add(value);
		}
		return set;
	}

	
	public static <T> List<T> stringToList(Class<T> tClass, String str) {
		return stringToList(tClass, str, null, null, null);
	}
	
	public static <T> List<T> stringToList(Class<T> tClass, String str, List<T> initList) {
		return stringToList(tClass, str, null, null, initList);
	}
	
	public static <T> List<T> stringToList(Class<T> tClass, String str, String splitable) {
		return stringToList(tClass, str, splitable, null, null);
	}
	
	public static <T> List<T> stringToList(Class<T> tClass, String str, String splitable, List<T> initList) {
		return stringToList(tClass, str, splitable, null, initList);
	}
	
	/**
	 * 字符串转Set
	 * @param tClass		仅支持String、Integer、Long，如有需要请自行添加
	 * @param str			源串
	 * @param splitable		分隔符
	 * @return
	 */
	public static <T> List<T> stringToList(Class<T> tClass, String str, String splitable, String splitableEntity, List<T> initList) {
		List<T> list;
		if (initList != null) {
			list = initList;
		} else {
			list = new ArrayList<>();
		}
		if (str == null || str.equals("")) {
			return list;
		}
		if (splitable == null) {
			splitable = ELEMENT_SPLIT;
		}
		if (splitableEntity == null) {
			splitableEntity = ATTRIBUTE_SPLIT;
		}
		String[] split = str.split(splitable);
		for (String string : split) {
			T value = getValue(tClass, string, splitableEntity);
			list.add(value);
		}
		return list;
	}
	
	public static Map<Integer, Integer> stringToMap(String str) {
		return stringToMap(str, Integer.class, Integer.class, COLON_ITEMS,  ELEMENT_SPLIT);
	}
	
	/**
	 * 字符串转map
	 * @param kClass			key 类型
	 * @param vClass			value类型
	 * @param str				字符串
	 * @param splitableKV		key和value的连接字符
	 * @param splitableEntry	键值对之间的连接字符
	 * @return
	 */
	public static <K, V> Map<K, V> stringToMap(String str, Class<K> kClass, Class<V> vClass, String splitableKV, String splitableEntry) {
		return stringToMap(str, kClass, vClass, splitableKV, splitableEntry, null, null);
	}
	
	public static <K, V> Map<K, V> stringToMap(String str, Class<K> kClass, Class<V> vClass) {
		return stringToMap(str, kClass, vClass, COLON_ITEMS, ELEMENT_SPLIT, BETWEEN_ITEMS, null);
	}
	
	public static <K, V> Map<K, V> stringToMap(String str, Class<K> kClass, Class<V> vClass, Map<K, V> initMap) {
		return stringToMap(str, kClass, vClass, COLON_ITEMS, ELEMENT_SPLIT, BETWEEN_ITEMS, initMap);
	}
	
	/**
	 * 
	 * 字符串转map
	 * @param kClass			key 类型
	 * @param vClass			value类型
	 * @param str				字符串
	 * @param splitableKV		key和value的连接字符
	 * @param splitableEntry	键值对之间的连接字符
	 * @param initMap			自定义map对象
	 * @return
	 */
	public static <K, V> Map<K, V> stringToMap(String str, Class<K> kClass, Class<V> vClass, String splitableKV, String splitableEntry, String splitableEntity, Map<K, V> initMap) {
		Map<K, V> map;
		if (initMap == null) {
			map = new HashMap<K, V>();
		} else {
			map = initMap;
		}
		if (str == null || "".equals(str)) {
			return map;
		}
		String[] entry = str.split(splitableEntry);
		for (String kv : entry) {
			String[] keyValue = kv.split(splitableKV);
			if (keyValue.length != 2) {
				throw new RuntimeException("string to map error");
			}
			
			K key = getValue(kClass, keyValue[0], null);
			V value = getValue(vClass, keyValue[1], splitableEntity);
			map.put(key, value);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getValue(Class<T> tClass, String string, String splitableEntry) {
		if (HawkOSOperator.isEmptyString(string)) {
			return null;
		}
		
		if (tClass == String.class) {
			return (T) string;
		} else if (tClass == Integer.class) {
			if (string.equals("")) {
				return (T) Integer.valueOf(0);
			} else {
				return (T) Integer.valueOf(string);
			}
		} else if (tClass == Long.class) {
			return (T) Long.valueOf(string);
		} else {
			Type[] genericInterfaces = tClass.getGenericInterfaces();
			for (Type type : genericInterfaces) {
				if (type == SplitEntity.class) {
					String[] split = string.split(splitableEntry);
					Class<SplitEntity> entityClass = (Class<SplitEntity>) tClass;
					HawkAsmConstructor<? extends SplitEntity> constructor = getConstructor(entityClass);
					SplitEntity entity = constructor.newInstance();
					entity.fullData(new DataArray(split));
					return (T) entity;
				}
			}
		}
		throw new RuntimeException("[toObject] not support serialize Class type! Class=" + tClass + " value=" + string);
	}
	
	public static <T> String toSerializeString(T value, String splitableEntity) {
		if (value instanceof Number) {
			return value.toString();
		} else if (value instanceof String) {
			return value.toString();
		} else if (value instanceof SplitEntity) {
			if (splitableEntity == null) {
				throw new RuntimeException("SplitEntity separator is null!");
			}
			SplitEntity entity = (SplitEntity) value;
			List<Object> dataList = new ArrayList<>();
			entity.serializeData(dataList);
			String str = collectionToString(dataList, splitableEntity);
			return str;
		}
		throw new RuntimeException("[toString] not support serialize Object type! type=" + value.getClass() + " value=" + value.toString());
	}
	
	/**
	 * 集合转换成字符串
	 * @param collection	集合
	 * @param splitable		分隔符
	 * @return
	 */
	public static String collectionToString(Collection<? extends Object> collection, String... separators) {
		StringBuilder builder = new StringBuilder();
		if (collection == null || collection.isEmpty()) {
			return "";
		}
		String separator1 = ELEMENT_DELIMITER, separator2 = ATTRIBUTE_SPLIT;
		if (separators.length > 0) {
			separator1 = separators[0];
			if (separators.length > 1) {
				separator2 = separators[1];
			}
		}
		for (Object obj : collection) {
			if (obj == null) {
				continue;
			}
			if (obj instanceof SplitEntity) {
				SplitEntity entity = (SplitEntity) obj;
				List<Object> dataList = new ArrayList<>();
				entity.serializeData(dataList);
				String str = collectionToString(dataList, separator2);
				builder.append(str).append(separator1);
			} else {
				builder.append(obj.toString()).append(separator1);
			}
		}
		if (builder.length() > 0) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}
	
	public static <K, V> String mapToString (Map<K, V> map) {
		return mapToString(map, COLON_ITEMS, ELEMENT_DELIMITER, BETWEEN_ITEMS);
	}
	
	public static <K, V> String mapToString(Map<K, V> map, String separatorKV, String separatorEntry) {
		return mapToString(map, separatorKV, separatorEntry, BETWEEN_ITEMS);
	}
	/**
	 * map转字符串
	 * value可以是SplitEntity形式
	 * 序列化默认格式为1:2,3,4,5|2:2,3,4,5
	 * @param map
	 * @param separatorKV
	 * @param separatorEntry
	 * @return
	 */
	public static <K, V> String mapToString(Map<K, V> map, String separatorKV, String separatorEntry, String separatorEntity) {
		if (map == null || map.isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (Entry<K, V> entry : map.entrySet()) {
			String keyStr = toSerializeString(entry.getKey(), separatorEntity);
			String valueStr = toSerializeString(entry.getValue(), separatorEntity);
			builder.append(keyStr).append(separatorKV).append(valueStr).append(separatorEntry);
		}
		builder.deleteCharAt(builder.length() - 1);
		return builder.toString();
	}

	/**
	 * 获取数组指定位置的int型数值
	 * @param array
	 * @param index		数组下标序号
	 * @return
	 */
	public static int getInt(String[] array, int index) {
		try {
			return Integer.valueOf(array[index]);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 获取数组指定位置的long型数值
	 * @param array
	 * @param index		数组下标序号
	 * @return
	 */
	public static long getLong(String[] array, int index) {
		try {
			return Long.valueOf(array[index]);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 获取数组指定位置的string型数值
	 * @param array
	 * @param index		数组下标序号
	 * @return
	 */
	public static String getString(String[] array, int index) {
		return array[index];
	}

	/**
	 * 对象转json串
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		return JSON.toJSONString(obj);
	}

	/**
	 * json串转对象
	 * @param jsonStr
	 * @param toClass
	 * @return
	 */
	public static <T> T parseJsonStr(String jsonStr, Class<T> toClass) {
		T parseObject = JSON.parseObject(jsonStr, toClass);
		return parseObject;
	}

	/**
	 * json串转对象列表
	 * @param jsonStr
	 * @param toClass
	 * @return
	 */
	public static <T> List<T> parseJsonArray(String jsonStr, Class<T> toClass) {
		List<T> parseArray = JSON.parseArray(jsonStr, toClass);
		return parseArray;
	}
	
	public static List<int[]> str2intList(String str) {
		List<int[]> list = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(str)) {
			return list;
		}
		
		String[] columns = str.split(BETWEEN_ITEMS);
		for (String column : columns) {
			list.add(SerializeHelper.string2IntArray(column));
		}
		
		return list;
	}
	
	public static int[] string2IntArray(String str) {
		return string2IntArray(str, ATTRIBUTE_SPLIT);
	}
	
	public static int[][] string2IntIntArray(String str) {
		return string2IntIntArray(str, BETWEEN_ITEMS, ATTRIBUTE_SPLIT);
	}
	
	public static int[][] string2IntIntArray(String str, String itemSplit,  String attributeSplit) {
		String[] itemArray = str.split(itemSplit);
		int[][] rltArray = null;
		
		for (int i = 0; i < itemArray.length; i++) {
			String item = itemArray[i];
			String[] propArray = item.split(attributeSplit);
			if (rltArray == null) {
				rltArray = new int[itemArray.length][];
			}
			rltArray[i] = new int[propArray.length];
			
			for(int j = 0; j < propArray.length; j++) {
				rltArray[i][j] = Integer.parseInt(propArray[j]);
			}
		}
		
		return rltArray;
	}

	public static int[] string2IntArray(String str, String splitStr) {
		if (HawkOSOperator.isEmptyString(str)) {
			return new int[0];
		}
		
		String[] strArray = str.split(splitStr);
		int[] rltArray = new int[strArray.length];
		for (int i = 0; i < strArray.length; i++) {
			rltArray[i] = Integer.parseInt(strArray[i]);
		}
		
		return rltArray;
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static List<Integer> cfgStr2List(String str) {
		return cfgStr2List(str, ATTRIBUTE_SPLIT);
	}
	
	public static List<Integer> cfgStr2List(String str, String splitStr) {
		List<Integer> rltList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(str)) {
			return rltList;
		}
		
		String[] strArray = str.split(splitStr);
		for (String item : strArray) {
			rltList.add(Integer.parseInt(item));
		}
		
		return rltList;
	}
	
	public static Map<Integer, Integer> cfgStr2Map(String str) {
		return cfgStr2Map(str, BETWEEN_ITEMS, ATTRIBUTE_SPLIT);
	}
	
	public static Map<Integer, Integer> cfgStr2Map(String str, String listSplitStr, String itemSplitStr) {
		Map<Integer, Integer> rltMap = new HashMap<>();
		if (HawkOSOperator.isEmptyString(str)) {
			return rltMap;
		}
		
		String[] listArray = str.split(listSplitStr);
		for (String listItem : listArray) {
			String[] listItemArray = listItem.split(itemSplitStr);
			rltMap.put(Integer.parseInt(listItemArray[0]), Integer.parseInt(listItemArray[1]));
		}
		
		return rltMap;
	}

	public static RangeMap<Integer, Integer> str2RangeMap(String str){
		RangeMap<Integer, Integer> rangeMap = TreeRangeMap.create();
		for(String rangeStr : str.split(";")){
			String arr[] = rangeStr.split(",");
			if(arr.length != 2){
				throw new RuntimeException("string to RangeMap error" + rangeStr);
			}
			String rangeArr[] = arr[0].split("_");
			if(rangeArr.length != 2){
				throw new RuntimeException("string to RangeMap error" + arr[0]);
			}
			rangeMap.put(Range.closed(Integer.parseInt(rangeArr[0]), Integer.parseInt(rangeArr[1])), Integer.parseInt(arr[1]));
		}
		return rangeMap;
	}
}
