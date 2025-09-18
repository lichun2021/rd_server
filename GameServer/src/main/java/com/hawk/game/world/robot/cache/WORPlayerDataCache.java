package com.hawk.game.world.robot.cache;

import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.hawk.game.GsConfig;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.cache.PlayerDataKey;

public class WORPlayerDataCache extends PlayerDataCache {
	private WORPlayerDataLoader dataLoader;

	public WORPlayerDataCache(String playerId) {
		super(playerId);
	}

	/**
	 * 缓存对象
	 */
	private LoadingCache<PlayerDataKey, Object> dataCache;

	/**
	 * 新疆缓存
	 * 
	 * @param playerId
	 * @param newly
	 * @return
	 */
	public static WORPlayerDataCache newCache(String playerId, String sourcePlayerId) {

		WORPlayerDataCache result = new WORPlayerDataCache(playerId);
		result.dataLoader = new WORPlayerDataLoader(playerId, sourcePlayerId);
		LoadingCache<PlayerDataKey, Object> cache = CacheBuilder.newBuilder().recordStats()
				.expireAfterAccess(GsConfig.getInstance().getCacheExpireTime(), TimeUnit.MILLISECONDS)
				.build(result.dataLoader);
		result.dataCache = cache;
		return result;
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
			// LoadingCache类是线程安全的
			T result = (T) dataCache.get(key);

			return result;

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 加载异常
		throw new RuntimeException("player data load failed: " + key);
	}

	public WORPlayerDataLoader getDataLoader() {
		return dataLoader;
	}

	public void setDataLoader(WORPlayerDataLoader dataLoader) {
		this.dataLoader = dataLoader;
	}

	public LoadingCache<PlayerDataKey, Object> getDataCache() {
		return dataCache;
	}

	public void setDataCache(LoadingCache<PlayerDataKey, Object> dataCache) {
		this.dataCache = dataCache;
	}

}
