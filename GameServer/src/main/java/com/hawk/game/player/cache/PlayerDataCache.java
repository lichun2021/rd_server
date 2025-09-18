package com.hawk.game.player.cache;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;

public class PlayerDataCache {
	/**
	 * 玩家id
	 */
	private String playerId;
	/**
	 * 缓存对象
	 */
	protected LoadingCache<PlayerDataKey, Object> dataCache;

	/** 
	 * 锁定数据
	 */
	private EnumMap<PlayerDataKey, Object> lockingKey = new EnumMap<>(PlayerDataKey.class);
	
	/**
	 * 私有构造
	 * 
	 */
	public PlayerDataCache(String playerId) {
		this.playerId = playerId;
	}
	
	/**
	 * 获取对应的玩家id
	 * 
	 * @return
	 */
	public String getPlayerId() {
		return playerId;
	}
	
	/**
	 * 新建缓存
	 * 
	 * @param playerId
	 * @param newly
	 * @return
	 */
	public static PlayerDataCache newCache(String playerId) {
		int cacheExpireTime = GsConfig.getInstance().getCacheExpireTime();
		
		// 流失玩家缓存过期时间
		int playerLossDays = GlobalData.getInstance().getPlayerLossDays(playerId);
		if (playerLossDays >= GsConfig.getInstance().getLossCacheDays()) {
			cacheExpireTime = GsConfig.getInstance().getLossCacheTime();
		}
		
		LoadingCache<PlayerDataKey, Object> cache = CacheBuilder.newBuilder().recordStats()
				.expireAfterAccess(cacheExpireTime, TimeUnit.MILLISECONDS)
				.removalListener(new RemovalListener<PlayerDataKey, Object>() {
					@Override
					public void onRemoval(RemovalNotification<PlayerDataKey, Object> dataObj) {
						HawkLog.logPrintln("player data remove expire entity, playerId: {}, key: {}", playerId, dataObj.getKey());
					}
				}).build(new PlayerDataLoader(playerId));

		PlayerDataCache result = new PlayerDataCache(playerId);
		result.dataCache = cache;
		return result;
	}

	/**
	 * 更新缓存的过期时间
	 * 
	 * @param expireTime
	 */
	public boolean updateCacheExpire(int expireTime) {
		try {
			Field localCacheField = HawkOSOperator.getClassField(dataCache, "localCache");

			Field expireField = HawkOSOperator.getClassField(localCacheField.get(dataCache), "expireAfterAccessNanos");
			expireField.setAccessible(true);
			final Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(expireField, expireField.getModifiers() & ~Modifier.FINAL);

			long expireAfterAccessNanos = TimeUnit.MILLISECONDS.toNanos(expireTime);
			expireField.set(localCacheField.get(dataCache), expireAfterAccessNanos);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 锁定数据不可访问
	 */
	public void lockKey(PlayerDataKey key, Object value) {
		lockingKey.put(key, value);
	}
	
	/**
	 * 解锁数据访问
	 * 
	 * @param key
	 */
	public void unLockKey(PlayerDataKey key) {
		lockingKey.remove(key);
	}

	public boolean isLockKey(PlayerDataKey key){
		return lockingKey.containsKey(key);
	}
	
	public void clearLockKey(){
		lockingKey.clear();
	}
	
	@SuppressWarnings("unchecked")
	protected <T> Optional<T> tryMakeLockingData(PlayerDataKey key) {
		if (lockingKey.isEmpty()) {
			return Optional.empty();
		}
		T result = null;
		if (lockingKey.containsKey(key)) {
			Object value = lockingKey.get(key);
			try {
				result = (T) value; 
			} catch (Exception e) {
				unLockKey(key);
				HawkException.catchException(e);
			}
		}
		return Optional.ofNullable(result);
	}
	
	/**
	 * 加载对应数据对象
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T makesureDate(PlayerDataKey key) {
		try {
			Optional<T> resOp = tryMakeLockingData(key);
			if (resOp.isPresent()) {
				return resOp.get();
			}
		
			// LoadingCache类是线程安全的
			T result = (T) dataCache.get(key);

			// 数据有效性判断
			if (result == null) {
				HawkLog.errPrintln("player data load null entity, playerId: {}, dataKey: {}", playerId, key);
			}

			return result;

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 记录日志
		HawkLog.errPrintln("player data load failed, playerId: {}, dataKey: {}", playerId, key);

		// 加载异常
		throw new RuntimeException("player data load failed: " + key);
	}

	/**
	 * 获取已存在数据, 不加载
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getData(PlayerDataKey key) {
		try {
			T result = (T) dataCache.getIfPresent(key);
			return result;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 更新玩家数据
	 * 
	 * @param key
	 * @param objData
	 */
	public void update(PlayerDataKey key, Object objData) {
		dataCache.put(key, objData);
	}
		
	/**
	 * delete 也走update
	 * @param entity
	 */
	public void entityChange(HawkDBEntity entity) {
		
	}

	public boolean removeEntityFlag(PlayerDataKey dataKey) {
		return true;
	} 
}
