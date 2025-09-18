package com.hawk.game.script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.redis.HawkRedisSession;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.util.HawkZlib;

import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;

import redis.clients.jedis.Jedis;

/**
 * 拉取玩家反馈日志
 * @author golden
 *
 */
public class ExportLogHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
			
			// 获取本服所有玩家id
			List<String> playerIds = getPlayerIds();

			for (String playerId : playerIds) {
				
				// 日志压缩模式
				byte[] logData = getPlayerLog(redisSession, playerId);
				if (logData == null || logData.length <= 0) {
					continue;
				}
				
				// 解压	
				logData = HawkZlib.zlibInflate(logData);

				String filePath = System.getProperty("user.dir") + "/playerLog/" + playerId;
				
				File file = new File(filePath);
				if (!file.exists()) {
					File dir = new File(file.getParent());
					dir.mkdir();
					file.createNewFile();
				}
				
				OutputStream out = new FileOutputStream(file, true);
				out.write(logData);
				out.close();
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.successResponse(null);
	}
	
	/**
	 * 获取本服所有玩家id
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	@SuppressWarnings({ "unchecked" })
	private List<String> getPlayerIds() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		List<String> playerIds = new ArrayList<>();
		Field field = GlobalData.class.getDeclaredField("playerNames");
		field.setAccessible(true);
		Map<String, String> playerNames = (ConcurrentHashMap<String, String>) field.get(GlobalData.getInstance());
		for (String playerId : playerNames.values()) {
			playerIds.add(playerId);
		}
		return playerIds;
	}
	
	/**
	 * 获取玩家日志
	 * 
	 * @param playerId
	 * @return
	 */
	private byte[] getPlayerLog(HawkRedisSession redisSession, String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}

		String key = "player_log" + ":" + playerId;
		Jedis jedis = redisSession.getJedis();
		if (jedis == null) {
			return null;
		}

		try {
			return jedis.get(key.getBytes());
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			jedis.close();
		}

		return null;
	}
}