package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 铠甲池配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/armour_pool.xml")
public class ArmourPoolCfg extends HawkConfigBase {

	@Id
	protected final int id;

	/**
	 * 铠甲id
	 */
	protected final int armourId;
	
	/**
	 * 品质
	 */
	protected final int quality;
	
	public ArmourPoolCfg() {
		id = 0;
		armourId = 0;
		quality = 0;
	}

	public int getId() {
		return id;
	}

	public int getArmourId() {
		return armourId;
	}

	public int getQuality() {
		return quality;
	}
}
