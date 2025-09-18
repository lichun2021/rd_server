package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *
 * @author zhenyu.shang
 * @since 2017年7月4日
 */
@HawkConfigManager.XmlResource(file = "xml/guild_science_floor.xml")
public class GuildScienceFloorCfg extends HawkConfigBase {
	
	//层级
	@Id
	protected final int floor;
	
	//解锁科技等级
	protected final int unlockLevel;
	
	public GuildScienceFloorCfg() {
		floor = 0;
		unlockLevel = 0;
	}

	public int getFloor() {
		return floor;
	}

	public int getUnlockLevel() {
		return unlockLevel;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}
	
}
