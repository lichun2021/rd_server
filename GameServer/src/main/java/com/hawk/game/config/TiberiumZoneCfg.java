package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 泰伯利亚联赛赛区配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tiberium_zone.xml")
public class TiberiumZoneCfg extends HawkConfigBase {
	@Id
	private final int id;

	private final String areaId;
	
	private final int zone;

	/** 奖励 */
	private final String serverId;
	
	private List<String> serverList;

	public TiberiumZoneCfg() {
		id = 0;
		areaId = "";
		zone = 0;
		serverId = "";
	}

	public int getId() {
		return id;
	}

	public String getAreaId() {
		return areaId;
	}

	public int getZone() {
		return zone;
	}

	public String getServerId() {
		return serverId;
	}
	
	public List<String> getServerList() {
		List<String> copy = new ArrayList<>();
		for (String server : serverList) {
			copy.add(server);
		}
		return copy;
	}

	protected boolean assemble() {
		List<String> _serverList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(serverId)) {
			String[] serverArr = serverId.split(",");
			for (String server : serverArr) {
				_serverList.add(server);
			}
		}
		serverList = _serverList;
		return true;
	}
}
