package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 安全协议id
 *
 * @author hawk
 *
 */
@HawkConfigManager.XmlResource(file = "cfg/secProto.xml")
public class SecProtoCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final int pps;

	public SecProtoCfg() {
		id = 0;
		pps = 0;
	}

	/**
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	public int getPps() {
		return pps;
	}
}
