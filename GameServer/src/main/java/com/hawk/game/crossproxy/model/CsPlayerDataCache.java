package com.hawk.game.crossproxy.model;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.hawk.game.GsConfig;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;

/**
 * 跨服玩家数据缓存对象
 * 
 * @author hawk
 *
 */
public class CsPlayerDataCache extends PlayerDataCache {
	
	private Set<PlayerDataKey> dataKeySet = new ConcurrentHashSet<>();
	/**
	 * 新建cache
	 * 
	 * @param playerId
	 * @return
	 */
	public static PlayerDataCache newCache(String playerId) {
		LoadingCache<PlayerDataKey, Object> cache = CacheBuilder.newBuilder().recordStats()
				.expireAfterAccess(GsConfig.getInstance().getCacheExpireTime(), TimeUnit.MILLISECONDS)
				.removalListener(new RemovalListener<PlayerDataKey, Object>() {
					@Override
					public void onRemoval(RemovalNotification<PlayerDataKey, Object> dataObj) {
						HawkLog.logPrintln("csplayerdata remove expire entity, playerId: {}, key: {}", playerId, dataObj.getKey());
					}
				}).build(new CsPlayerDataLoader(playerId));

		CsPlayerDataCache result = new CsPlayerDataCache(playerId);				
		result.dataCache = cache;
		return result;
	}
	
	/**
	 * 构造
	 * 
	 * @param playerId
	 */
	public CsPlayerDataCache(String playerId) {
		super(playerId);
	}

	/**
	 * {@link CsPlayerDataLoader#load(PlayerDataKey)}
	 */
	@SuppressWarnings("unchecked")
	public <T> T makesureDate(PlayerDataKey key) {
		try {
			Optional<T> resOp = tryMakeLockingData(key);
			if(resOp.isPresent()){
				return resOp.get();
			}
			
			T result = (T) dataCache.get(key);
			if (result != null) {
				return result;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 记录日志
		HawkLog.errPrintln("csplayerdata load failed, playerId: {}, dataKey: {}", this.getPlayerId(), key);

		// 加载异常
		throw new RuntimeException("csplayerdata load failed: " + key);
	}
		
	/**
	 * delete 也走update
	 * @param entity
	 */
	public void entityChange(HawkDBEntity entity) {
		PlayerDataKey key = PlayerDataSerializer.getEntityDataKey(entity);
		if (key == null) {
			return;
		}
		
		dataKeySet.add(key);
	} 
	
	
	/**
	 * 
	 * @param dataKey
	 * @return
	 */
	public boolean removeEntityFlag(PlayerDataKey dataKey) {
		return dataKeySet.remove(dataKey);
	} 
}
