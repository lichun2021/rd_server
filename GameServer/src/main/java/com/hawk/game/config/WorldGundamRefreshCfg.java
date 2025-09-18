package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 机甲刷新配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_gundam_refresh.xml")
public class WorldGundamRefreshCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	protected final int serverOpenTime;
	
	protected final int gundamId;
	
	public WorldGundamRefreshCfg() {
		id = 0;
		serverOpenTime = 0;
		gundamId = 0;
	}

	public int getId() {
		return id;
	}

	public long getServerOpenTime() {
		return serverOpenTime * 1000L;
	}

	public int getGundamId() {
		return gundamId;
	}
}
