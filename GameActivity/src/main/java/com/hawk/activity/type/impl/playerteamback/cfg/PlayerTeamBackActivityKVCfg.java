package com.hawk.activity.type.impl.playerteamback.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 玩家回流H5活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/team_return/team_return_cfg.xml")
public class PlayerTeamBackActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public PlayerTeamBackActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
