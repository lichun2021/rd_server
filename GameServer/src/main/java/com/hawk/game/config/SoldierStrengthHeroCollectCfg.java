package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 士兵战力英雄羁绊表
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/soldier_strength_hero_collect.xml")
public class SoldierStrengthHeroCollectCfg extends HawkConfigBase {

	@Id
	protected final int effectId;
	
	/**
	 * 战力属性计算加成
	 */
	protected final String atkAttr;
	
	protected final String hpAttr;
	
	public SoldierStrengthHeroCollectCfg() {
		effectId = 0;
		atkAttr = "";
		hpAttr = "";
	}

	public int getEffectId() {
		return effectId;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
}
