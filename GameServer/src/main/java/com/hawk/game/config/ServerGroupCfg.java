package com.hawk.game.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

@HawkConfigManager.XmlResource(file = "cfg/serverGroup.xml")
public class ServerGroupCfg extends HawkConfigBase {
	/**
	 * 分组id
	 */
	@Id
	private final String groupId;

	/**
	 * 服务器id
	 */
	private final String serverIds;
	
	/**
	 * 服务器所属的分组
	 */
	private static Map<String, String> serverGroupMap = new ConcurrentHashMap<String, String>();

	public ServerGroupCfg() {
		this.groupId = "";
		this.serverIds = "";
	}

	public String getGroupId() {
		return groupId;
	}

	public String getServerIds() {
		return serverIds;
	}

	@Override
	public boolean assemble() {
		if (!HawkOSOperator.isEmptyString(serverIds)) {
			String[] idArray = serverIds.split(",");
			for (String serverId : idArray) {
				serverGroupMap.put(serverId, groupId);
			}
		}
		return true;
	}
	
	public static String getServerGroupId(String serverId) {
		return serverGroupMap.get(serverId);
	}
}