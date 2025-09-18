package com.hawk.activity.type.impl.return_puzzle.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 武者拼图活动配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/return_puzzle/return_puzzle_cfg.xml")
public class ReturnPuzzleActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;


	public ReturnPuzzleActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
}
