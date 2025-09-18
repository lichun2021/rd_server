package com.hawk.game.module.spacemecha.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 星甲召唤箱子配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/space_machine_box.xml")
public class SpaceMechaBoxCfg extends HawkConfigBase {
	@Id
	protected final int id;
	/**
	 * 等级
	 */
	protected final int level;
	/**
	 * 奖励
	 */
	protected final String reward;
	/**
	 * 采集时间:s
	 */
	protected final int gatherTime;
	
	protected final int gridCnt;

	public SpaceMechaBoxCfg() {
		id = 0;
		level = 0;
		reward = "";
		gatherTime  = 0;
		gridCnt = 1;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public String getReward() {
		return reward;
	}

	public int getGatherTime() {
		return gatherTime;
	}
	
	public int getGridCnt() {
		return gridCnt;
	}

}
