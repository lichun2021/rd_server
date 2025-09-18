package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 联盟标记配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/alliance_sign.xml")
public class AllianceSignCfg extends HawkConfigBase {
	@Id
	protected final int id;

	public AllianceSignCfg() {
		this.id = 0;
	}

	public int getId() {
		return id;
	}

}
