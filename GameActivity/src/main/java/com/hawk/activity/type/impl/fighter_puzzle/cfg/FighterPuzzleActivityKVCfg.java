package com.hawk.activity.type.impl.fighter_puzzle.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 武者拼图活动配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/fighter_puzzle/fighter_puzzle_cfg.xml")
public class FighterPuzzleActivityKVCfg extends HawkConfigBase {

	private final int receiveRewardLimitDay;
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;


	public FighterPuzzleActivityKVCfg() {
		serverDelay = 0;
		receiveRewardLimitDay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public int getReceiveRewardLimitDay() {
		return receiveRewardLimitDay;
	}
}
