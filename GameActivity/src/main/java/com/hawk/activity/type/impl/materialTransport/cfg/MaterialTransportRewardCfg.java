
package com.hawk.activity.type.impl.materialTransport.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 机甲破世 
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/material_transport/material_transport_reward.xml")
public class MaterialTransportRewardCfg extends HawkConfigBase {
	// <data id="12" item="30000_1782103_1000" robRate="0.01" />
	@Id
	private final int id;

	private final String item;

	private final double robRate;

	public MaterialTransportRewardCfg() {
		id = 0;
		item = "";
		robRate = 0;
	}

	public int getId() {
		return id;
	}

	public String getItem() {
		return item;
	}

	public double getRobRate() {
		return robRate;
	}

}
