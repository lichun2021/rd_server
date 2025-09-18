package com.hawk.game.gmscript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.redis.HawkRedisSession;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.world.WorldMarchService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 扫描卡兵玩家
 * @author golden
 *
 */
public class ScanArmyHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		
		// 是否扫描全部
		boolean scanall = !HawkOSOperator.isEmptyString(params.get("sacnall"));
		// 是否修复
		boolean fix = !HawkOSOperator.isEmptyString(params.get("fix"));
		
		if (scanall) {
			return scanAll(fix);
		} else {
			return scan(fix, params);
		}
	}

	/**
	 * 扫描单人
	 * @param fix
	 * @param params
	 * @return
	 */
	private String scan(boolean fix, Map<String, String> params) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
		}
		
		WorldMarchService.getInstance().checkAndFixArmy(player, fix);
		
		return HawkScript.successResponse("");
	}
	
	/**
	 * 扫描全部
	 * @param fix 是否修复
	 */
	private String scanAll(boolean fix) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// 需要修复的玩家列表
					List<Player> fixList = new ArrayList<>();
					
					List<AccountInfo> accountList = new ArrayList<AccountInfo>();
					GlobalData.getInstance().getAccountList(accountList);
					for (AccountInfo account : accountList) {
						Player player = GlobalData.getInstance().scriptMakesurePlayer(account.getPlayerId());
						
						// 检测修复
						boolean result = WorldMarchService.getInstance().checkAndFixArmy(player, fix);
						
						if (result) {
							fixList.add(player);
						}
					}
					
					// 写入redis
					if (fixList.size() > 0) {
						writeToRedis(fixList);
					} else {
						HawkLog.logPrintln("scan army count:{}", fixList.size());
					}
					
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		});
		
		thread.setDaemon(true);
		thread.start();
		
		return HawkScript.successResponse("");
	}
	
	/**
	 * 写入redis
	 * @param fixList
	 */
	private void writeToRedis(List<Player> fixList) {
		// 数量，用于分批写入
		int count = 0;
		String serverId = GsConfig.getInstance().getServerId();
		
		// 写redis
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			for (Player player : fixList) {
				
				// key
				String openId = player.getOpenId();
				String platform = player.getPlatform();
				String key = openId + "#" + platform + "#" + serverId;
				
				jedis.hset("army_process", key, "1");
				
				count++;
				
				if (count >= 100) {
					pip.sync();
					count = 0;
				}
			}
			
			if (count > 0) {
				pip.sync();
			}
			
			//设置过期
			jedis.expire("army_process", 7200);
			
			HawkLog.logPrintln("scan army count:{}", fixList.size());
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}