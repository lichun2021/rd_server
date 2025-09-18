package com.hawk.game.module.lianmengyqzz.battleroom.secondmap;

import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.hawk.game.GsConfig;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.protocol.YQZZ.PBYQZZSecondMapResp;

public class YQZZSecondMap {
	private final YQZZBattleRoom parent;
	private LoadingCache<String, PBYQZZSecondMapResp.Builder> dataCache;

	private YQZZSecondMap(YQZZBattleRoom parent) {
		this.parent = parent;
	}

	public static YQZZSecondMap create(YQZZBattleRoom parent) {
		LoadingCache<String, PBYQZZSecondMapResp.Builder> cache = CacheBuilder.newBuilder().recordStats()
				.expireAfterWrite(3, TimeUnit.SECONDS)
				.build(new YQZZSecondMapDataLoader(parent));

		YQZZSecondMap result = new YQZZSecondMap(parent);
		result.dataCache = cache;

		return result;
	}

	public PBYQZZSecondMapResp.Builder getSecondMap(String guildId) {
		try {
			return dataCache.get(guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
}
