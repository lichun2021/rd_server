package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 兵种战力类型表
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/soldier_strength_type.xml")
public class SoldierStrengthTypeCfg extends HawkConfigBase {
	/**
	 * 类型
	 */
	@Id
	protected final int type;
	
	/**
	 * 攻击力加成最大值
	 */
	protected final int atkAttrMax;
	
	/**
	 * 血量加成最大值
	 */
	protected final int hpAttrMax;
	
	/**
	 * 参数
	 */
	protected final String param1;
	protected final String param2;
	protected final String param3;
	protected final String param4;
	
	public SoldierStrengthTypeCfg() {
		type = 0;
		atkAttrMax = 0;
		hpAttrMax = 0;
		param1 = "";
		param2 = "";
		param3 = "";
		param4 = "";
	}

	public int getType() {
		return type;
	}

	public int getAtkAttrMax() {
		return atkAttrMax;
	}

	public int getHpAttrMax() {
		return hpAttrMax;
	}

	public String getParam1() {
		return param1;
	}

	public String getParam2() {
		return param2;
	}

	public String getParam3() {
		return param3;
	}

	public String getParam4() {
		return param4;
	}
}
