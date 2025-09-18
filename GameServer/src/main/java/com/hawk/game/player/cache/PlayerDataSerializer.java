package com.hawk.game.player.cache;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Id;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.octets.HawkOctetsStream;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.GameConstCfg;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.DBChecker;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class PlayerDataSerializer {
	/**
	 * entity对应key的反向隐射表
	 */
	private static Map<Class<?>, PlayerDataKey> entityDataKeyType = new ConcurrentHashMap<Class<?>, PlayerDataKey>();	
	/**
	 * 检测indexPro
	 * @return
	 */
	public static boolean checkIndexPro() {
		for (PlayerDataKey key : EnumSet.allOf(PlayerDataKey.class)) {
			Class<?> entityClass = key.entityType();
			if (entityClass == null) {
				HawkLog.warnPrintln("key entity type is null :{}", key.name());
				continue;
			}
			Field[] fields = entityClass.getDeclaredFields();
			for (Field field : fields) {
				Annotation columnAnnotation = field.getAnnotation(Column.class);
				Annotation indexPropAnnotation = field.getAnnotation(IndexProp.class);
				if (columnAnnotation != null && indexPropAnnotation == null) {
					HawkLog.errPrintln("className:{} filed:{} indexProp is null", entityClass.getName(), field.getName());
					
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 初始化检测
	 */
	public static boolean checkSerilizeData() {
		try {
			//校验entity的indexProp注解.
			boolean checkIndexProp = checkIndexPro();
			if (!checkIndexProp) {
				return false;
			}
			
			HawkDBManager.getInstance().setDBChecker(new DBChecker());
			
			for (PlayerDataKey key : EnumSet.allOf(PlayerDataKey.class)) {
				Class<?> entityClass = key.entityType();
				
				if (Objects.isNull(entityClass)) {
					continue;
				}
				
				boolean hasOwnerKey = false;
				BeanInfo beanInfo = Introspector.getBeanInfo(entityClass, entityClass.getSuperclass());
	            for (MethodDescriptor methodDesc : beanInfo.getMethodDescriptors()) {
	            	if (methodDesc.getName().equals("getOwnerKey")) {
	            		hasOwnerKey = true;
	            		break;
	            	}
	            }
	            
				if (!hasOwnerKey) {
					HawkLog.errPrintln("entity miss owner key, entity: {}", entityClass.getSimpleName());
					return false;
				}
				
				entityDataKeyType.put(entityClass, key);
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 获取entity的存储key
	 * 
	 * @param entity
	 * @return
	 */
	public static PlayerDataKey getEntityDataKey(HawkDBEntity entity) {
		return entityDataKeyType.get(entity.getClass());
	}
	
	/**
	 * 保存到redis
	 * 
	 * @param dataCache
	 * @param dataKey, 如果为null, 全量数据
	 * @return
	 */
	public static boolean flushToRedis(PlayerDataCache dataCache, PlayerDataKey dataKey, boolean resetState) {
		long startTime = System.nanoTime();
		int writeLength = 0;
		int pipelineNum = 0;
		String redisKey = "player_data:" + dataCache.getPlayerId();
		String statusKey = "entity_status:" + dataCache.getPlayerId();
		try {
			for (PlayerDataKey key : EnumSet.allOf(PlayerDataKey.class)) {
				if (dataKey != null && dataKey != key) {
					continue;
				}
				
				boolean flushFlag = dataCache.removeEntityFlag(key);
				if (!flushFlag || dataCache.isLockKey(key)) {
					continue;
				}
				
				//每次写一个key拿一次jedis pipeline 主要是怕一次异常导致全跪.
				try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); 
						Pipeline pipeline = jedis.pipelined();){
					//读取内存中的标志.					
					
					byte[] bytes = serializeData(key, dataCache.makesureDate(key));
					String fieldKey = key.name();
					if (bytes != null) {
						pipeline.hset(redisKey.getBytes(), fieldKey.getBytes(), bytes);
						writeLength += bytes.length; 
					} else {
						pipeline.hdel(redisKey, fieldKey);
					}
					
					// 数据变更计数 全量不记录状态.
					if (dataKey != null || resetState) {						
						pipeline.hincrBy(statusKey, key.name(), 1);
					}
					
					pipeline.sync();
					pipelineNum++;
				} catch (Exception e) {
					HawkException.catchException(e);
				}  				
			}
									
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("flush player data to redis failed, playerId: {}", dataCache.getPlayerId());
		} finally {
			//只有全量刷新的时候才设置该key的过期时间,避免频繁的操作
			if (dataKey == null) {
				try {
					int crossPlayerCacheExpireTime = GameConstCfg.getInstance().getCrossCacheExpireTime();
					RedisProxy.getInstance().getRedisSession().expire(redisKey, crossPlayerCacheExpireTime);
					RedisProxy.getInstance().getRedisSession().expire(statusKey, crossPlayerCacheExpireTime);
					HawkLog.logPrintln("flush player data to redis with expiretime, playerId: {}", dataCache.getPlayerId());
				} catch (Exception e) {
					HawkException.catchException(e);
				}				
			}
			long endTime = System.nanoTime();
			long costTime = (endTime - startTime) / 100_000;
			if (costTime > 50) {
				HawkLog.warnPrintln("dataCacheFlushToRedis costTime:{}, playerId:{}, writeLength:{}, pipelineNum:{}",
						costTime, dataCache.getPlayerId(), writeLength,pipelineNum);
			}			
		}		
		
		return false;
	}

	/**
	 * 从redis构建玩家数据
	 * 
	 * @return
	 */
	public static boolean buildFromRedis(PlayerDataCache dataCache, boolean persistable) {
		try {
			String redisKey = "player_data:" + dataCache.getPlayerId();
			for (PlayerDataKey key : EnumSet.allOf(PlayerDataKey.class)) {
				String fieldKey = key.name();
				byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes(redisKey, fieldKey);
				if (bytes != null) {
					Object objData = unserializeData(key, bytes, persistable);
					dataCache.update(key, objData);
				} else {
					dataCache.makesureDate(key);
				}
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("flush player data to redis failed, playerId: {}", dataCache.getPlayerId());
		}
		return false;
	}
	
	/**
	 * 把制定数据序列化到redis
	 * 
	 * @param key
	 * @param data
	 */
	public static byte[] serializeData(PlayerDataKey key, Object data) throws Exception {
		if (key.entityType() == null) {
			return null;
		}
		
		if (!key.listMode()) {
			HawkDBEntity entity = (HawkDBEntity) data;
			return entity.serialize();
		} else {
			List<?> entityList = (List<?>) data;
			
			HawkOctetsStream os = HawkOctetsStream.create();
			os.writeInt(entityList.size());
			
			for (Object entity : entityList) {
				if (!(entity instanceof HawkDBEntity)) {
					throw new RuntimeException("player data entity error, entityClass: " + entity.getClass().getSimpleName());
				}
				
				os.writeBytes(((HawkDBEntity) entity).serialize()); 
			}
			os.flip();
			
			byte[] bytes = new byte[os.limit()];
			System.arraycopy(os.getBuffer().array(), 0, bytes, 0, os.limit());
			return bytes;
		}
	}
	
	/**
	 * 反序列化
	 * 
	 * @param playerId
	 * @param key
	 * @param persistable
	 * @return
	 * @throws Exception
	 */
	public static  <T> T unserializeData(String playerId, PlayerDataKey key, boolean persistable) throws Exception {
		String redisKey = "player_data:" + playerId;
		byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes(redisKey, key.name());		
		return unserializeData(key, bytes, persistable);
	}
	
	/**
	 * 反序列化数据
	 * 
	 * @param key
	 * @param bytes
	 * @param persistable 是否可以持久化
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unserializeData(PlayerDataKey key, byte[] bytes, boolean persistable) throws Exception {
		if (bytes == null || key.entityType() == null) {
			return null;
		}
		
		if (!key.listMode()) {
			HawkDBEntity entity = key.entityType().newInstance();
			entity.setPersistable(persistable);
			
			if (key == PlayerDataKey.StatisticsEntity) {
				HawkOctetsStream os = HawkOctetsStream.create(bytes, 0, bytes.length, false);
				byte[] propBytes = os.readBytes();
				BitSet bitset = BitSet.valueOf(propBytes);
				
				if (bitset.get(26) == false) {
					TreeMap<Integer, Field> fieldIdProp = HawkDBEntity.getColumnIdProp(entity.getClass().getSimpleName());
					if (fieldIdProp == null || fieldIdProp.size() <= 0) {
						throw new RuntimeException("entity field idprop lack");
					}
					
					byte[] entityBytes = os.readBytes();
					HawkOctetsStream entityOs = HawkOctetsStream.create(entityBytes, 0, entityBytes.length, false);
					
					Method readColumnVal = HawkOSOperator.getClassMethod(entity, "readColumnVal", 
							HawkOctetsStream.class, HawkDBEntity.class, Field.class);
					
					// 逐个索引标记数据复原
					for (Entry<Integer, Field> entry : fieldIdProp.entrySet()) {
						try {												
							int idx = entry.getKey();
							
							if (idx == 23) {
								entry.getValue().set(entity, "");
							}
							
							if(idx >= 23){
								idx += 1;
							}
							
							if (bitset.get(idx)) {
								Field filed = fieldIdProp.get(idx);
								readColumnVal.invoke(entity, entityOs, entity, filed);
							}
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
					
					entity.afterRead();
					return (T) entity;
				}
			}
			
			if (!entity.parseFrom(bytes)) {
				throw new RuntimeException("entity unserialize failed, class: " + key.entityType().getSimpleName());
			}			
			entity.afterRead();
			return (T) entity;
		} 
			
		List<HawkDBEntity> entityList = new LinkedList<HawkDBEntity>();
		HawkOctetsStream os = HawkOctetsStream.create(bytes, 0, bytes.length, false);
		int count = os.readInt();
		for (int i = 0; i < count; i++) {
			HawkDBEntity entity = key.entityType().newInstance();
			if (!entity.parseFrom(os.readBytes())) {
				throw new RuntimeException("entity unserialize failed, class: " + key.entityType().getSimpleName());
			}
			entity.setPersistable(persistable);
			entity.afterRead();
			entityList.add(entity);
		}
		return (T) entityList;
	}
	
	/**
	 * 跨服通知实体数据变更
	 * 
	 * @param entity
	 */
	public static void csEntityChanged(HawkDBEntity entity, int opType) {
		PlayerDataKey dataKey = PlayerDataSerializer.getEntityDataKey(entity);
		if (dataKey == null) {
			HawkLog.errPrintln("entity data key miss, entity: {}", entity.getClass().getSimpleName());
			return;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(entity.getOwnerKey());
		if (player == null) {
			HawkLog.errPrintln("entity data player miss, entity: {}, owner: {}", 
					entity.getClass().getSimpleName(), entity.getOwnerKey());
			
			return;
		}
		
		//有一种极端情况，玩家进入泰伯利亚(通过跨服进入),触发了锁定数据的异步更新, 实际更新数据的时候,取到的是一份副本里面的临时数据,会导致覆盖真实数据
		if (dataKey != null && player.getData().getDataCache().isLockKey(dataKey)) {
			HawkLog.warnPrintln("entity already locked entity:{}, owner:{}", entity.getClass().getSimpleName(), entity.getOwnerKey());
			
			return;
		}
		
		// 更新到redis中
		flushToRedis(player.getData().getDataCache(), dataKey, true);
		@SuppressWarnings("deprecation")
		String realServerId = GlobalData.getInstance().getMainServerId(player.getServerId());
		
		// 添加需要同步的信息
		CrossService.getInstance().addWaitSyncPlayer(player.getId(), realServerId, dataKey);
	}
	
	/**
	 * 同步跨服数据回本服落地存储
	 * 
	 * @param playerId
	 * @param dataKey
	 */
	public static void csSyncPlayerData(String playerId) {
		csSyncPlayerData(playerId, false);
	}
	
	/**
	 * 同步跨服数据回本服落地存储
	 * 
	 * @param playerId
	 * @param dataKey
	 */
	public static void csSyncPlayerData(String playerId, boolean needRecord) {
		boolean crossEntityLog = GameConstCfg.getInstance().isCrossEntityLog();
		needRecord = crossEntityLog & needRecord;
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			HawkLog.errPrintln("cs sync entity player not found: {}", playerId);
			return;
		}
		
		if (!CrossService.getInstance().isEmigrationPlayer(playerId)) {
			HawkLog.errPrintln("cs sync player not a emigration player:{}", playerId);
			return;
		}
		
		long startTime = HawkTime.getMillisecond();
		Map<String, String> statusMap = null;
		synchronized (player.getSyncObj()) {
			String redisKey = "entity_status:" + player.getId();
			
			try {
				statusMap = RedisProxy.getInstance().getRedisSession().hGetAll(redisKey); 
			} catch (Exception e) {
				HawkException.catchException(e);
				return;
			}
			
			if (statusMap == null || statusMap.isEmpty()) {
				return;
			}
			
			//批量清理状态
			try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();Pipeline pipeline = jedis.pipelined()) {
				for (Entry<String, String> entry : statusMap.entrySet()) {					
					pipeline.hdel(redisKey, entry.getKey(), entry.getValue());
				}
				pipeline.sync();
			} catch (Exception e) {
				//这里如果状态错了还是要继续往下走.
				HawkException.catchException(e);
			}
									
			for (PlayerDataKey dataKey : EnumSet.allOf(PlayerDataKey.class)) {
				try {
					String value = statusMap.get(dataKey.name());
					if (HawkOSOperator.isEmptyString(value)) {
						continue;
					}
					
					// 变更状态
					int dataStatus = Integer.valueOf(value);
					if (dataStatus <= 0) {
						continue;
					}
					
					// 从redis读取数据
					byte[] bytes = RedisProxy.getInstance().getRedisSession().hGetBytes("player_data:" + playerId, dataKey.name());
						
					// 发序列化
					Object data = unserializeData(dataKey, bytes, false);
					if (data == null) {
						HawkLog.errPrintln("cs entity unserialize failed, playerId: {}, dataKey: {}", playerId, dataKey.name());
						continue;
					}
					
					// 数据合并处理
					boolean mergeSucc = mergePlayerData(player.getData().getDataCache(), dataKey, data, needRecord);
					if (mergeSucc) {
						// 记录日志
						HawkLog.logPrintln("cs sync entity success, playerId: {}, dataKey: {}, target server: {}", playerId, dataKey.name(), CrossService.getInstance().getEmigrationPlayerServerId(playerId));
					} else {
						// 记录日志
						HawkLog.errPrintln("cs sync entity failed, playerId: {}, dataKey: {}", playerId, dataKey.name());
					}
					
					//player身上挂了一个cityLevel.
					if (dataKey == PlayerDataKey.BuildingEntities) {
						player.setCityLevel(0);
					}
					
				} catch (Exception e) {
					HawkLog.errPrintln("cs sync entity exception, playerId: {}, dataKey: {}", playerId, dataKey.name());
					
					HawkException.catchException(e);				
				}
			}
		}
		
		// 超时时间
		long costTime = (HawkTime.getMillisecond() - startTime); 
		if (costTime > 50) {
			HawkLog.warnPrintln("csSyncPlayerData timeout, playerId:{}, costTime:{}", playerId, costTime);
		}
	}
	
	/**
	 * 合并玩家数据
	 * 
	 * @param dataCache
	 * @param key
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean mergePlayerData(PlayerDataCache dataCache, PlayerDataKey dataKey, Object data, boolean needRecord) {
		if (!dataKey.listMode()) {
			try {
				HawkDBEntity localEntity = dataCache.makesureDate(dataKey);
				HawkDBEntity mergeEntity = (HawkDBEntity) data;
				if (!localEntity.getPrimaryKey().equals(mergeEntity.getPrimaryKey())) {
					throw new RuntimeException("single entity mode need the same primary key, entity: " + localEntity.getClass().getSimpleName());
				}
				
				//更新前记录.
				if (needRecord) {
					recordUpdateEntityInfo(localEntity, true);
				}
				
				mergeEntity(localEntity, mergeEntity);
												
				// 准备更新数据
				localEntity.afterRead();
				localEntity.notifyUpdate();
				
				//必须是在afterRead后面调用。
				if (needRecord) {
					recordUpdateEntityInfo(localEntity, false);
				}
				
				return true;
			} catch (Exception e) {
				HawkException.catchException(e, data);
			}
			
		} else {
			List<HawkDBEntity> entityList = dataCache.makesureDate(dataKey);
			Map<String, HawkDBEntity> localEntityMap = HawkDBEntity.converEntityMap(entityList);			
			Map<String, HawkDBEntity> mergeEntityMap = HawkDBEntity.converEntityMap((List<HawkDBEntity>) data);
			
			// 先删除
			for (Entry<String, HawkDBEntity> entry : localEntityMap.entrySet()) {
				HawkDBEntity entity = entry.getValue();
				try {
					if (!mergeEntityMap.containsKey(entry.getKey())) {
						entityList.remove(entity);
						entity.delete();
						
						recordDeleteEntityInfo(entity);
					}
				} catch (Exception e) {
					HawkException.catchException(e, entity);
				}
			}
			
			// 添加创建或者合并更新
			for (Entry<String, HawkDBEntity> entry : mergeEntityMap.entrySet()) {
				HawkDBEntity entity = entry.getValue();
				try {
					// 新创建
					if (!localEntityMap.containsKey(entry.getKey())) {
						entity.setPersistable(true);
						
						// 添加到列表中来
						entityList.add(entity);
						localEntityMap.put(entry.getKey(), entity);											
						
						// 创建
						entity.afterRead();
						entity.create();
						
						if (needRecord) {
							recordAddEntityInfo(entity);
						}
					} else {
						HawkDBEntity localEntity = localEntityMap.get(entry.getKey());
						
						if (needRecord) {
							recordUpdateEntityInfo(entity, true);
						}
						
						mergeEntity(localEntity, entity);
						
						if (needRecord) {
							recordUpdateEntityInfo(entity, false);
						}
						
						// 准备更新数据
						localEntity.afterRead();
						localEntity.notifyUpdate();
					}
				} catch (Exception e) {
					HawkException.catchException(e, entity);
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * 合并entity
	 * 
	 * @param dstEntity
	 * @param srcEntity
	 * @return
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static boolean mergeEntity(HawkDBEntity dstEntity, HawkDBEntity srcEntity) throws Exception {
		if (dstEntity == null || srcEntity == null) {
			return false;
		}
		
		if (!dstEntity.getPrimaryKey().equals(srcEntity.getPrimaryKey())) {
			throw new RuntimeException("merge entity need the sam primary key, entity: " + dstEntity.getClass().getSimpleName());
		}
		
		// 清空内存数据
		HawkDBEntity.clearTransientValue(dstEntity);
		
		List<Field> fieldList = HawkDBEntity.getColumnFileds(dstEntity.getClass().getSimpleName());
		for (Field field : fieldList) {
			// 主键不可被写
			if (field.getAnnotation(Id.class) != null) {
				continue;
			}
			
			Object updateVal = field.get(srcEntity);
			field.set(dstEntity, updateVal);
		}
		
		return true;
	}
	
	/**
	 * 记录添加记录
	 * 
	 * @param dbEntity
	 */
	public static void recordAddEntityInfo(HawkDBEntity dbEntity) {
		if (dbEntity == null) {
			return;
		}
		
		try {
			HawkLog.logPrintln("add entity :{}", dbEntity.toString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录删除数据
	 * 
	 * @param dbEntity
	 */
	public static void recordDeleteEntityInfo(HawkDBEntity dbEntity) {
		if (dbEntity == null) {
			return;
		}
		
		try {
			HawkLog.logPrintln("delete entity :{}", dbEntity.toString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录更新数据
	 * 
	 * @param dbEntity
	 */
	public static void recordUpdateEntityInfo(HawkDBEntity dbEntity, boolean isBefore) {
		if (dbEntity == null) {
			return;
		}
		
		try {
			if (isBefore) {
				HawkLog.logPrintln("cs entity before:{}", dbEntity.toString());
			} else {
				HawkLog.logPrintln("cs entity after:{}", dbEntity.toString());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 删除本地db
	 * @param playerId
	 */
	@SuppressWarnings("unchecked")
	public static void deleteAllEntity(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		for (PlayerDataKey dataKey : EnumSet.allOf(PlayerDataKey.class)) {
			try {
				if (!dataKey.isSerializeable()) {
					continue;
				}
				
				Object data = player.getData().getDataCache().makesureDate(dataKey);
				if (dataKey.listMode()) {
					List<HawkDBEntity> entityList = (List<HawkDBEntity>) data;
					for (HawkDBEntity entity : entityList) {
						try {
							entity.setPersistable(true);
							entity.delete();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				} else {
					HawkDBEntity entity = (HawkDBEntity) data;
					entity.setPersistable(true);
					entity.delete();
				}
			} catch (Exception e) {
				HawkException.catchException(e);				
			}
		}		
	}
	
}
