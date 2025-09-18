package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.google.common.collect.ImmutableMap;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 装备科技
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/equip_research.xml")
public class EquipResearchCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	/**
	 * 解锁条件
	 */
	protected final String unlock;
	/**
	 * 二期标识
	 */
	protected final int phaseTwo;
	
	ImmutableMap<Integer, Integer> unlockEquipMap;
	
	/**
	 * 构造
	 */
	public EquipResearchCfg() {
		id = 0;
		unlock = "";
		phaseTwo = 0;
	}

	public int getId() {
		return id;
	}

	public int getPhaseTwo() {
		return phaseTwo;
	}

	@Override
	protected boolean assemble() {
		unlockEquipMap = ImmutableMap.copyOf(SerializeHelper.stringToMap(unlock, Integer.class, Integer.class, "_", ","));
		return true;
	}
	
    public ImmutableMap<Integer, Integer> getUnlockEquipMap() {
    	return unlockEquipMap;
    }
	
}
