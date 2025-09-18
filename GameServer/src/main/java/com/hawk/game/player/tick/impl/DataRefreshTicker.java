package com.hawk.game.player.tick.impl;

import org.hawk.app.HawkApp;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;

public class DataRefreshTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		long dataRefreshTime = player.getTickTimeLine().getDataRefreshTime();
		if (currentTime > dataRefreshTime) {
			// 设置数据有效性
			GlobalData.getInstance().notifyPlayerDataAccess(player.getData());
			// 下次刷新时间
			player.getTickTimeLine().setDataRefreshTime(currentTime + 1800000L);
		}
	}

}
