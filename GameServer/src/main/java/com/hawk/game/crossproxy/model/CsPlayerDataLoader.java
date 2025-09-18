package com.hawk.game.crossproxy.model;

import java.util.HashSet;

import org.hawk.log.HawkLog;

import com.google.common.cache.CacheLoader;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;

public class CsPlayerDataLoader extends CacheLoader<PlayerDataKey, Object> {
	/**
	 * 玩家id
	 */
	private String playerId;

	/**
	 * 构造跨服玩家数据加载器
	 * 
	 * @param playerId
	 */
	public CsPlayerDataLoader(String playerId) {
		this.playerId = playerId;
	}
	
	/**
	 * 因为要和原来的dataKey共用名字
	 * 所以在这里处理,后续如果playerDataKey加了新的东西,
	 * 使用数据库无需特殊处理,如果使用的是redis之类的操作,视情况而定.
	 */
	@Override
	public Object load(PlayerDataKey key) throws Exception {		
		switch (key) {
		case ShieldPlayers:
			return new HashSet<>();
			
		case BanRankInfos:
		case SettingDatas:
		case PlayerDressAskEntities:
		case PlayerDressSendEntities:
			return key.load(playerId, false);
			
		default:			
			Object obj = PlayerDataSerializer.unserializeData(playerId, key, false);
			if (obj == null) {
				String redisKey = "player_data:" + playerId;
				if (!RedisProxy.getInstance().getRedisSession().hExists(redisKey, key.name(), 0)) {
					HawkLog.logPrintln("csplayerdata unserialize failed while load, redis key not exist, playerId: {}, key: {}", playerId, key.name());
					return key.load(playerId, false);
				}
				
				HawkLog.logPrintln("csplayerdata unserialize failed while load, playerId: {}, key: {}", playerId, key.name());
			}
			
			return obj; 
		}
	}
}
