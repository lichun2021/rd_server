package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *
 * @author zhenyu.shang
 * @since 2017年7月4日
 */
@HawkConfigManager.XmlResource(file = "xml/guild_science_main.xml")
public class GuildScienceMainCfg extends HawkConfigBase {
	/** 科技Id */
	@Id
	protected final int id; 
	
	/** 所属层级 */
	protected final int floor;
	
	/** 科技基础值 */
	protected final int scienceBase;
	
	/** 贡献基础值 */
	protected final int contributionBase;
	
	/** 积分基础值 */
	protected final int scoreBase;

	public GuildScienceMainCfg() {
		this.id = 0;
		this.floor = 0;
		this.scienceBase = 0;
		this.contributionBase = 0;
		this.scoreBase = 0;
	}

	public int getId() {
		return id;
	}

	public int getFloor() {
		return floor;
	}

	public int getScienceBase() {
		return scienceBase;
	}

	public int getContributionBase() {
		return contributionBase;
	}

	public int getScoreBase() {
		return scoreBase;
	}
	
}
