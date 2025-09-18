package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


/**
 * 排名奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_reward_buff.xml")
public class CrossRewardBuffCfg extends HawkConfigBase {
//	<data id="1" name="采集加速" scoreType="1" effect="327_10000" time="8600" />
	@Id
	private final int id;
	/** buff名称*/
	private final String name;
	
	private final int scoreType;
	
	/** buff效果*/
	private final String effect;
	
	/** 持续时间*/
	private final int time;
	
	public CrossRewardBuffCfg() {
		id = 0;
		name = "";
		scoreType = 0;
		effect = "";
		time = 0;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getScoreType() {
		return scoreType;
	}

	public String getEffect() {
		return effect;
	}

	public long getTime() {
		return time * 1000l;
	}
	
}
