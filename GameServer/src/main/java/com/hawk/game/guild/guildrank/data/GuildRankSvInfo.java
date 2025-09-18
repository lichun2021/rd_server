package com.hawk.game.guild.guildrank.data;

import org.hawk.os.HawkTime;

/**
 * 保存到redis中的排行榜tick时间
 * 
 * @Desc
 * @author RickMei
 * @Date 2018年11月14日 下午6:54:04
 */

public class GuildRankSvInfo {
	private long lastUpdateTime;
	private boolean isLoadHistory = false;

	public GuildRankSvInfo() {
		lastUpdateTime = HawkTime.getMillisecond();
		isLoadHistory = false;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long updateTime) {
		lastUpdateTime = updateTime;
	}

	public boolean isLoadHistory() {
		return isLoadHistory;
	}

	public void setLoadHistory(boolean isLoadHistory) {
		this.isLoadHistory = isLoadHistory;
	}
}
