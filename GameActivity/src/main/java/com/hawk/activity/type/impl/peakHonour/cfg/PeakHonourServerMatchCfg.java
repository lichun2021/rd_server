package com.hawk.activity.type.impl.peakHonour.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/peak_honour/peak_honour_server_match.xml")
public class PeakHonourServerMatchCfg extends HawkConfigBase {
	/**
	 * 组
	 */
	@Id
	private final int groupId;
	
	/**
	 * 区ID
	 */
	private final String serverList;
	
	/**
	 * 构造
	 */
	public PeakHonourServerMatchCfg() {
		groupId = 0;
		serverList = "";
	}

	public int getGroupId() {
		return groupId;
	}

	public String getServerList() {
		return serverList;
	}

	public boolean containsServer(String serverId) {
		return serverList.contains(serverId);
	}
}
