package com.hawk.game.lianmengjunyan.player.npc.cache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.hawk.game.GsConfig;
import com.hawk.game.lianmengjunyan.player.npc.LMJYNPCPlayer;

public class LMJYNPCPlayerDataCache {
	/**
	 * 玩家id
	 */
	private String playerId;
	/**
	 * 缓存对象
	 */
	private LoadingCache<LMJYNPCPlayerDataKey, Object> dataCache;

	/**
	 * 私有构造
	 * 
	 */
	private LMJYNPCPlayerDataCache() {

	}

	/**
	 * 新疆缓存
	 * 
	 * @param playerId
	 * @param newly
	 * @return
	 */
	public static LMJYNPCPlayerDataCache newCache(LMJYNPCPlayer player) {
		LoadingCache<LMJYNPCPlayerDataKey, Object> cache = CacheBuilder.newBuilder().recordStats()
				.expireAfterAccess(GsConfig.getInstance().getCacheExpireTime(), TimeUnit.MILLISECONDS)
				.build(new LMJYNPCPlayerDataLoader(player.getId(), player.getNpcCfg()));

		LMJYNPCPlayerDataCache result = new LMJYNPCPlayerDataCache();
		result.dataCache = cache;
		result.playerId = player.getId();
		return result;
	}

	/**
	 * 加载对应数据对象
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T makesureDate(LMJYNPCPlayerDataKey key) {
		// 这些数据被锁定了, 在军演期间不可访问. 将返回数据副本
		try {
			// LoadingCache类是线程安全的
			T result = (T) dataCache.get(key);

			// 数据有效性判断
			if (result == null) {
				HawkLog.errPrintln("player data load null entity, playerId: {}, dataKey: {}", playerId, key);
			}

			if (result instanceof HawkDBEntity) {
				((HawkDBEntity) result).setPersistable(false);
			} else if (result instanceof List) {
				List<T> it = (List<T>) result;
				if (!it.isEmpty()) {
					boolean isDbentityList = it.get(0) instanceof HawkDBEntity;
					if (isDbentityList) {
						for (T entity : it) {
							((HawkDBEntity) entity).setPersistable(false);
						}
					}
				}
			}
			return result;

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 加载异常
		throw new RuntimeException("player data load failed: " + key);
	}

}
