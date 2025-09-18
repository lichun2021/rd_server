package com.hawk.game.player.tick.impl;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;

import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;

public class ZeroEarningTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		// 零收益状态判断
		if (player.zeroEarningState && player.getZeroEarningTime() <= currentTime) {
			try {
				player.sendIDIPZeroEarningMsg();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

}
