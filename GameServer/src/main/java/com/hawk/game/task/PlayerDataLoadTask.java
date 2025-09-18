package com.hawk.game.task;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.thread.HawkTask;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.PlayerData;
import com.hawk.game.queryentity.AccountInfo;

public class PlayerDataLoadTask extends HawkTask {
	/**
	 * 账号信息
	 */
	private AccountInfo accountInfo;
	
	public PlayerDataLoadTask(AccountInfo accountInfo) {
		this.accountInfo = accountInfo;
	}
	
	@Override
	public Object run() {
		try {
			PlayerData playerData = GlobalData.getInstance().getPlayerData(accountInfo.getPlayerId(), false);
			if (playerData != null) {
				playerData.setFromCache(true);

				// 日志记录
				HawkLog.logPrintln("async load player data from cache, accountInfo: {}", accountInfo.toString());
			} else {
				playerData = GlobalData.getInstance().getPlayerData(accountInfo.getPlayerId(), true);

				// 日志记录
				HawkLog.logPrintln("async load player data from db, accountInfo: {}", accountInfo.toString());
			}

			// 是否为新玩家
			boolean isNewly = false;
			if (accountInfo != null) {
				isNewly = accountInfo.isNewly();
			}

			// 在异步线程中登录的时候加载全部数据.
			playerData.loadAll(isNewly);

			return playerData;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
}
