package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 联盟捐献排行奖励配置
 * 
 * @author shadow
 *
 */
@HawkConfigManager.XmlResource(file = "xml/guild_donate_rank_award.xml")
public class GuildDonateRankAwardCfg extends HawkConfigBase {
	// 联盟等级
	@Id
	protected final int id;
	
	// 奖励
	protected final int award;

	public GuildDonateRankAwardCfg() {
		id = 0;
		award = 0;
	}

	public int getId() {
		return id;
	}

	public int getAward() {
		return award;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

}
