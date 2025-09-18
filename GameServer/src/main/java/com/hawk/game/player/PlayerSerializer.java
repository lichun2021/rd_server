package com.hawk.game.player;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.hawk.annotation.SerializeField;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.serializer.HawkEntitySerializer;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class PlayerSerializer {
	/**
	 * 玩家数据序列化
	 * 
	 * @param playerData
	 * @return
	 */
	public static JSONObject serializePlayerData(PlayerData playerData, Collection<HawkDBEntity> serializeEntities) {
		JSONObject dataJson = new JSONObject();		
		try {
			Class<?> dataClass = playerData.getClass();
			Method[] methods = dataClass.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getAnnotation(SerializeField.class) != null) {
					if (!serializeField(playerData, method, dataJson, serializeEntities)) {
						HawkLog.errPrintln("serialize player method failed, method: {}", method.getName());
						return null;
					}
				}
			}
			return dataJson;
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("serialize player data failed, playerId: {}", playerData.getPlayerEntity().getId());
		}
		return null;
	}
	
	
	
	/**
	 * 玩家数据反序列化
	 * 不序列化成playerData了，毫无意义
	 * @param value
	 * @return
	 */
	public static void unserializeHawkDBEntityList(JSONObject dataJson, Collection<HawkDBEntity> unserializeEntities) {
		try {
			//暂时只处理list 和普通的信息,map暂时不管
			for (Entry<String, Object> obj : dataJson.entrySet()) {				
				if (obj.getValue() instanceof JSONArray) {
					List<HawkDBEntity> dbList = unserializeHawkDBEntityList((JSONArray)obj.getValue());
					unserializeEntities.addAll(dbList);
				} else if (obj.getValue() instanceof JSONObject){
					unserializeEntities.add(unserializeHawkDBEntity((JSONObject)obj.getValue()));
				} else {
					throw new RuntimeException("only handle JSONObject JSONArray Object");
				} 
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("unserialize player data failed, json: {}", dataJson);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private static boolean serializeField(PlayerData playerData, Method method, JSONObject dataJson, Collection<HawkDBEntity> serializeEntities) throws Exception {
		Class<?> rltClazz = method.getReturnType();
		if (HawkDBEntity.class.isAssignableFrom(rltClazz)) {
			HawkDBEntity entity = (HawkDBEntity) method.invoke(playerData);
			if (entity != null) {
				dataJson.put(entity.getClass().getName(), HawkEntitySerializer.serialize((entity)));
				if (serializeEntities != null) {
					serializeEntities.add(entity);
				}
			}
			return true;
		} 
		
		if (List.class.isAssignableFrom(rltClazz)) {
			Class<?>[] genericClazz = HawkOSOperator.getMethodGenericType(method);
			if (genericClazz != null && genericClazz.length > 0 && HawkDBEntity.class.isAssignableFrom(genericClazz[0])) {
				List<HawkDBEntity> entities = (List<HawkDBEntity>) method.invoke(playerData);
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
					dataJson.put("list#"+genericClazz[0].getName(), entityArray);
				}
				return true;
			}
		}
		
		throw new RuntimeException(String.format("unsupport serialize field class type, methodName: %s", method.getName()));
	}
	@SuppressWarnings("unchecked")
	public static HawkDBEntity unserializeHawkDBEntity(JSONObject jsonObject) throws Exception {
		String className = jsonObject.getString("class");
		if (className == null) {
			return null;
		} else {
			Class<? extends HawkDBEntity> clazz = (Class<? extends HawkDBEntity>) Class.forName(className);
			return HawkEntitySerializer.unserialize(clazz, jsonObject);
		}
		
	}
	public static List<HawkDBEntity> unserializeHawkDBEntityList(JSONArray jsonArray) throws Exception {
		List<HawkDBEntity> dbList = new LinkedList<>();
		for (Object obj : jsonArray) {
			if (obj instanceof JSONObject) {
				JSONObject jsonObject = (JSONObject)obj;
				String className = jsonObject.getString("class");
				@SuppressWarnings("unchecked")
				Class<? extends HawkDBEntity> clazz = (Class<? extends HawkDBEntity>) Class.forName(className);
				dbList.add(HawkEntitySerializer.unserialize(clazz, jsonObject));
			}			
		}
		
		return dbList;
	}

}
