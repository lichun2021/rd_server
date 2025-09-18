package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 区服描述相关信息配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/server_info.xml")
public class ServerInfoCfg extends HawkConfigBase {

	/**
	 * 区服id
	 */
	@Id
	private final int serverId;
	/**
	 * 平台
	 */
	private final String platformName;
	/**
	 * 区服编号
	 */
	private final int areaId;
	/**
	 * 是否是手Q服：1是0否
	 */
	private final int isQQ;
	
	
	public ServerInfoCfg() {
		serverId = 0;
		platformName = "";
		areaId = 0;
		isQQ = 0;
	}

	public int getServerId() {
		return serverId;
	}

	public String getPlatformName() {
		return platformName;
	}

	public int getAreaId() {
		return areaId;
	}

	public int getIsQQ() {
		return isQQ;
	}

}
