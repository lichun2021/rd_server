package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 兵种战力基础属性表
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/soldier_strength_base.xml")
@HawkConfigBase.CombineId(fields = { "soldierLevel", "soldierStar" })
public class SoldierStrengthBaseCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	/**
	 * 兵种等级
	 */
	protected final int soldierLevel;
	
	/**
	 * 兵种星级
	 */
	protected final int soldierStar;
	
	/**
	 * 基础属性
	 */
	protected final int baseValue;
	
	public SoldierStrengthBaseCfg() {
		id = 0;
		soldierLevel = 0;
		soldierStar = 0;
		baseValue = 0;
	}

	public int getId() {
		return id;
	}

	public int getSoldierLevel() {
		return soldierLevel;
	}

	public int getSoldierStar() {
		return soldierStar;
	}

	public int getBaseValue() {
		return baseValue;
	}
}
