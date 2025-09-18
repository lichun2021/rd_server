
package com.hawk.activity.type.impl.materialTransport.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 机甲破世 
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/material_transport/material_transport_res.xml")
public class MaterialTransportResCfg extends HawkConfigBase {
	// <data id="104" type="1" quality="5" rewardId="10,11,12,13,14,15,16,1,2,3" robNumber="3" />
	@Id
	private final int id;

	private final int type;
	private final int needTime;

	public MaterialTransportResCfg() {
		id = 0;
		type = 0;
		needTime = 0;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getNeedTime() {
		return needTime;
	}

}
