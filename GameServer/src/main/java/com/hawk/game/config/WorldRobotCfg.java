package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 世界机器人配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_robot.xml")
public class WorldRobotCfg extends HawkConfigBase {

	@Id
	protected final int id;

	/**
	 * 排名上限
	 */
	protected final int rankUp;
	
	/**
	 * 排名下限
	 */
	protected final int rankDown;
	
	/**
	 * 数量
	 */
	protected final int count;

	public WorldRobotCfg() {
		id = 0;
		rankUp = 0;
		rankDown = 0;
		count = 0;
	}

	public int getId() {
		return id;
	}

	public int getRankUp() {
		return rankUp;
	}

	public int getRankDown() {
		return rankDown;
	}

	public int getCount() {
		return count;
	}
}