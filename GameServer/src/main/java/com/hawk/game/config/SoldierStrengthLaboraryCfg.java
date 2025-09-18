package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 兵种战力-超能实验室
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/soldier_strength_laborary_core.xml")
public class SoldierStrengthLaboraryCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	protected final int itemId;
	protected final int numMin;
	protected final int numMax;
	protected final String atkAttr;
	protected final String hpAttr;
	
	public SoldierStrengthLaboraryCfg() {
		id = 0;
		itemId = 0;
		numMin = 0;
		numMax = 0;
		atkAttr = "";
		hpAttr = "";
	}

	public int getId() {
		return id;
	}

	public int getItemId() {
		return itemId;
	}

	public int getNumMin() {
		return numMin;
	}

	public int getNumMax() {
		return numMax;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
}
