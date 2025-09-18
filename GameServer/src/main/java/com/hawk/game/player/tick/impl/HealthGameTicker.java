package com.hawk.game.player.tick.impl;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkException;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.protocol.Player.ReportType;
import com.hawk.sdk.config.HealthCfg;

public class HealthGameTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		long tickTime = player.getTickTimeLine().getHealthGameTickTime();
		// 健康游戏检测
		if (GlobalData.getInstance().isHealthGameEnable() && currentTime >= tickTime) {
			try {
				if (!HealthCfg.getInstance().isHealthGameRemind()) {
					return;
				}

				// 单次在线强制下线休息
				int onlineTime = (int) ((currentTime - player.getLoginTime()) / 1000);
				int exitTimeLong = GlobalData.getInstance().getHealthGameConf().getOnceGameForceExitTime(player.isAdult());
				if (onlineTime >= exitTimeLong) {
					int restTimeLong = GlobalData.getInstance().getHealthGameConf().getOnceGameForceRestTime(player.isAdult());
					player.sendHealthGameRemind(ReportType.ONECE_GAME_FORCE_EIXT_VALUE, exitTimeLong, restTimeLong, 0);
					return;
				}

				// 单次在线时长休息提醒
				if (currentTime >= player.getNextRemindTime()) {
					int peroidTime = (int) ((player.getNextRemindTime() - player.getLoginTime()) / 1000);
					player.sendHealthGameRemind(ReportType.ONCE_GAME_TIME_LONG_VALUE, peroidTime, 0, 0);
					player.updateRemindTime();
				}
				
				player.getTickTimeLine().setHealthGameTickTime(currentTime + HealthCfg.getInstance().getHealthGameTickPeriod() * 1000L);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

}
