package com.hawk.game.player.tick.impl;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.config.GameConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.ScoreType;

public class QQScoreBatchTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		long scoreBatchTime = player.getTickTimeLine().getScoreBatchTime();
		// 手Q成就上报
		if (GameUtil.isScoreBatchEnable(player) && currentTime - scoreBatchTime >= 300000) {
			try {
				int onlineTimeCurDay = player.getEntity().getOnlineTimeCurDay();
				int day = (0xfffe0000 & onlineTimeCurDay) >> 17;
				int todayOfYear = HawkTime.getYearDay();
				int onlineTime = 0x0001ffff & onlineTimeCurDay;
				// 不是同一天
				if (todayOfYear != day) {
					onlineTime = Math.max(0, (int) (currentTime - HawkTime.getAM0Date().getTime()) / 1000);
				} else {
					onlineTime += (int) (currentTime - player.getEntity().getLoginTime()) / 1000;
				}

				GameUtil.scoreBatch(player, ScoreType.DAIY_GAME_TIME, onlineTime);

				if (player.getPower() != player.getLastPowerScore() && player.getCityLevel() > GameConstCfg.getInstance().getPowerScoreBatchLv()) {
					GameUtil.scoreBatch(player, ScoreType.POWER, player.getPower());
					player.setLastPowerScore(player.getPower());
				}

				player.getTickTimeLine().setScoreBatchTime(currentTime);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

}
