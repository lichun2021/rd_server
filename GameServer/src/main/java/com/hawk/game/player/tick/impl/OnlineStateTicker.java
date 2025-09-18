package com.hawk.game.player.tick.impl;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;

import com.hawk.game.config.GameConstCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;

public class OnlineStateTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		long onlineTickTime = player.getTickTimeLine().getOnlineTickTime();
		// 在线状态tick
		int onlineFlagTickPeriod = GameConstCfg.getInstance().getOnlineFlagTickPeriod();
		if (onlineFlagTickPeriod > 0 && currentTime - onlineTickTime > onlineFlagTickPeriod) {
			try {
				if (onlineTickTime > 0) {
					RedisProxy.getInstance().updateOnlineInfo(player.getOpenId(), player.getPlatform());
				}

				player.getTickTimeLine().setOnlineTickTime(currentTime);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

}
