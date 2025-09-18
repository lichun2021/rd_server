package com.hawk.activity.helper;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.annotation.SerializeField;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.serializer.HawkEntitySerializer;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class PlayerActivityDataSerialize {
	public static JSONObject serializePlayerActivityData(PlayerActivityData activityData, Collection<HawkDBEntity> serializeEntities) {
		JSONObject dataJson = new JSONObject();		
		try {
			Class<?> dataClass = activityData.getClass();
			Field[] fields = dataClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.getAnnotation(SerializeField.class) != null) {
					if (!serializeField(activityData, field, dataJson, serializeEntities)) {
						HawkLog.errPrintln("serialize activity data field failed, field: {}", field.getName());
						return null;
					}
				}
			}
			return dataJson;
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("serialize activity data  failed, playerId: {}", activityData.getPlayerId());
		}
		return null;
	}
	
	/**
	 * 玩家数据反序列化
	 * 
	 * @param value
	 * @return
	 */
	public static PlayerActivityData unserializePlayerActivityData(JSONObject dataJson, Collection<HawkDBEntity> unserializeEntities) {
		PlayerActivityData activityData = new PlayerActivityData();
		try {
			Class<?> dataClass = activityData.getClass();
			Field[] fields = dataClass.getDeclaredFields();
			for (Field field : fields) {
				if (field.getAnnotation(SerializeField.class) != null) {
					if (!unserializeField(activityData, field, dataJson, unserializeEntities)) {
						HawkLog.errPrintln("unserialize player field failed, field: {}", field.getName());
						return null;
					}
				}
			}
			return activityData;
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("unserialize player data failed, json: {}", dataJson);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static boolean serializeField(PlayerActivityData activityData, Field field, JSONObject dataJson, Collection<HawkDBEntity> serializeEntities) throws Exception {
		Class<?> fieldClazz = field.getType();
		if (HawkDBEntity.class.isAssignableFrom(fieldClazz)) {
			HawkDBEntity entity = (HawkDBEntity) field.get(activityData);
			if (entity != null) {
				dataJson.put(field.getName(), HawkEntitySerializer.serialize((entity)));
				if (serializeEntities != null) {
					serializeEntities.add(entity);
				}
			}
			return true;
		} 
		
		if (List.class.isAssignableFrom(fieldClazz)) {
			Class<?>[] genericClazz = HawkOSOperator.getFieldGenericType(field);
			if (genericClazz != null && genericClazz.length > 0 && HawkDBEntity.class.isAssignableFrom(genericClazz[0])) {
				List<HawkDBEntity> entities = (List<HawkDBEntity>) field.get(activityData);
				if (entities != null) {
					JSONArray entityArray = new JSONArray();
					for (HawkDBEntity entity : entities) {
						if (entity != null) {
							entityArray.add(HawkEntitySerializer.serialize((entity)));
							if (serializeEntities != null) {
								serializeEntities.add(entity);
							}
						}
					}
					dataJson.put(field.getName(), entityArray);
				}
				return true;
			}
		}
		
		if (Map.class.isAssignableFrom(fieldClazz)) {
			Class<?>[] genericClazz = HawkOSOperator.getFieldGenericType(field);
			if (genericClazz != null && genericClazz.length > 1 && HawkDBEntity.class.isAssignableFrom(genericClazz[1])) {
				Map<Object, HawkDBEntity> entityMap = (Map<Object, HawkDBEntity>) field.get(activityData);
				if (entityMap != null) {
					JSONObject entityMapInfo = new JSONObject();
					for (Entry<?, HawkDBEntity> entry : entityMap.entrySet()) {
						HawkDBEntity entity = entry.getValue();
						if (entity != null) {
							entityMapInfo.put(entry.getKey().toString(), HawkEntitySerializer.serialize((entity)));
							if (serializeEntities != null) {
								serializeEntities.add(entity);
							}
						}
					}
					dataJson.put(field.getName(), entityMapInfo);
				}
				return true;
			}
		}
		
		throw new RuntimeException(String.format("unsupport serialize field class type, className: %s, field: %s", fieldClazz.getName(), field.getName()));
	}
	
	/**
	 * 反序列化对象的制定域
	 * 
	 * @param playerData
	 * @param field
	 * @param dataJson
	 * @throws IllegalAccessException 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private static boolean unserializeField(PlayerActivityData activityData, Field field, JSONObject dataJson, Collection<HawkDBEntity> unserializeEntities) throws Exception {
		Class<?> fieldClazz = field.getType();
		if (HawkDBEntity.class.isAssignableFrom(fieldClazz)) {
			JSONObject jsonInfo = dataJson.getJSONObject(field.getName());
			
			// 获取真实的类型
			Class<?> clazz = fieldClazz;
			if (clazz.equals(HawkDBEntity.class) && jsonInfo.containsKey("class")) {
				clazz = Class.forName(jsonInfo.getString("class"));
			}
			
			HawkDBEntity entity = HawkEntitySerializer.unserialize((Class<? extends HawkDBEntity>)clazz, jsonInfo);
			if (entity != null) {
				field.set(activityData, entity);
				if (unserializeEntities != null) {
					unserializeEntities.add(entity);
				}
			}
			return true;
		} 
		
		if (List.class.isAssignableFrom(fieldClazz)) {
			Class<?>[] genericClazz = HawkOSOperator.getFieldGenericType(field);
			if (genericClazz != null && genericClazz.length > 0 && HawkDBEntity.class.isAssignableFrom(genericClazz[0])) {
				JSONArray jsonArray = dataJson.getJSONArray(field.getName());
				if (jsonArray != null) {
					List<HawkDBEntity> entities = new LinkedList<HawkDBEntity>();
					for (int i = 0; i < jsonArray.size(); i++) {
						JSONObject jsonInfo = jsonArray.getJSONObject(i);
						
						// 获取真实的类型
						Class<?> clazz = genericClazz[0];
						if (clazz.equals(HawkDBEntity.class) && jsonInfo.containsKey("class")) {
							clazz = Class.forName(jsonInfo.getString("class"));
						}
						
						HawkDBEntity entity = HawkEntitySerializer.unserialize((Class<? extends HawkDBEntity>)clazz, jsonInfo);
						if (entity != null) {
							entities.add(entity);
							if (unserializeEntities != null) {
								unserializeEntities.add(entity);
							}
						}
					}
					field.set(activityData, entities);
				}
				return true;
			}
		}

		if (Map.class.isAssignableFrom(fieldClazz)) {
			Class<?>[] genericClazz = HawkOSOperator.getFieldGenericType(field);
			if (genericClazz != null && genericClazz.length > 1 && HawkDBEntity.class.isAssignableFrom(genericClazz[1])) {
				JSONObject entityMapInfo = dataJson.getJSONObject(field.getName());
				if (entityMapInfo != null) {
					Object fieldValue = field.get(activityData);
					Map<Object, HawkDBEntity> entityMap = null;
					//尽量沿用已有的结构
					if (fieldValue != null) {
						entityMap = (Map<Object, HawkDBEntity>) fieldValue;
					} else {
						entityMap = new HashMap<Object, HawkDBEntity>();
					}
					
					for (Entry<String, Object> entry : entityMapInfo.entrySet()) {
						JSONObject jsonInfo = (JSONObject) entry.getValue();
						
						// 获取真实的类型
						Class<?> clazz = genericClazz[1];
						if (clazz.equals(HawkDBEntity.class) && jsonInfo.containsKey("class")) {
							clazz = Class.forName(jsonInfo.getString("class"));
						}
						
						HawkDBEntity entity = HawkEntitySerializer.unserialize((Class<? extends HawkDBEntity>)clazz, jsonInfo);
						if (entity != null) {
							entityMap.put(entry.getKey(), entity);
							if (unserializeEntities != null) {
								unserializeEntities.add(entity);
							}
						}
					}
					
					field.set(activityData, entityMap);
				}
				return true;
			}
		}
		
		throw new RuntimeException(String.format("unsupport unserialize field class type, className: %s, field: %s", fieldClazz.getName(), field.getName()));
	}
}
