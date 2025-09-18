package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 排行榜配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.XmlResource(file = "xml/rank.xml")
public class RankCfg extends HawkConfigBase {
	@Id
	protected final int rankId;
	
	protected final String key;
	// 榜单显示数量
	protected final int rankCount;
	// 最大排行数量
	protected final int maxCount;
	// 更新周期
	protected final int period;

	public RankCfg() {
		rankId = 0;
		key = "";
		rankCount = 0;
		maxCount = 0;
		period = 60000;
	}

	public int getRankId() {
		return rankId;
	}
	
	public String getKey() {
		return key;
	}

	public int getRankCount() {
		return rankCount;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public int getPeriod() {
		return period;
	}
}
