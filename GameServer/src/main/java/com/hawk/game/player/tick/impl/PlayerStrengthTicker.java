package com.hawk.game.player.tick.impl;

import org.hawk.app.HawkApp;
import org.hawk.redis.HawkRedisSession;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.util.GsConst;

public class PlayerStrengthTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		// 跨服不重新计算战力
		if (player.isCsPlayer()) {
			return;
		}
		long lastCalcStrengthTime = player.getTickTimeLine().getLastCalcStrengthTime();
		long currentTime = HawkApp.getInstance().getCurrentTime();
		// 5min计算一次
		if (currentTime - lastCalcStrengthTime < GsConst.MINUTE_MILLI_SECONDS * 5) {
			return;
		}
		
		player.getTickTimeLine().setLastCalcStrengthTime(currentTime);
		
		// redis删除以后,获取战力的时候会重新计算
		HawkRedisSession redisSession = RedisProxy.getInstance().getRedisSession();
		String redisKey = "playerStrength" + player.getId();
		redisSession.del(redisKey);
		
		player.getStrength();
	}

}
