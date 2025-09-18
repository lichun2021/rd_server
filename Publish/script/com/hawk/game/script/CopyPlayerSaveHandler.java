package com.hawk.game.script;

import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataCache;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.util.GameUtil;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.EnumSet;
import java.util.Map;

/**
 * 复制账号
 * @author golden
 *
 */
public class CopyPlayerSaveHandler extends HawkScript {
	static final String csplayerId = "234j234klj234jlk";

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {

		if (!GsConfig.getInstance().isDebug()) {
			return "not debug mode !";
		}
		// 源玩家
		Player from = getPlayer(params.get("fromId"), params.get("fromName"));
		flushToRedis(from.getData().getDataCache(), false);
		
		return "ok";
	}

	/**
	 * 获取玩家
	 */
	public Player getPlayer(String playerId, String playerName) {
		if (HawkOSOperator.isEmptyString(playerId) && !HawkOSOperator.isEmptyString(playerName)) {
			playerId = GameUtil.getPlayerIdByName(playerName);
		}
		return GlobalData.getInstance().scriptMakesurePlayer(playerId);
	}

	/**
	 * 保存到redis
	 * 
	 * @param dataCache
	 * @param dataKey, 如果为null, 全量数据
	 * @return
	 */
	public static boolean flushToRedis(PlayerDataCache dataCache, boolean resetState) {
		String info = RedisProxy.getInstance().getPlayerPresetWorldMarch(dataCache.getPlayerId());
		if (info != null){
			RedisProxy.getInstance().getRedisSession().hSet("world_preset_march", csplayerId, info);
		}

		String redisKey = "gm_player_data:" + csplayerId;
		try {
			for (PlayerDataKey key : EnumSet.allOf(PlayerDataKey.class)) {

				boolean flushFlag = dataCache.removeEntityFlag(key);
				if (!flushFlag) {
					continue;
				}

				// 每次写一个key拿一次jedis pipeline 主要是怕一次异常导致全跪.
				try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
						Pipeline pipeline = jedis.pipelined();) {
					// 读取内存中的标志.

					byte[] bytes = PlayerDataSerializer.serializeData(key, dataCache.makesureDate(key));
					String fieldKey = key.name();
					if (bytes != null) {
						pipeline.hset(redisKey.getBytes(), fieldKey.getBytes(), bytes);
					} else {
						pipeline.hdel(redisKey, fieldKey);
					}

					pipeline.sync();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}

			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.errPrintln("flush player data to redis failed, playerId: {}", dataCache.getPlayerId());
		}

		return false;
	}
}