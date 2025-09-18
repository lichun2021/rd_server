package com.hawk.activity.type.impl.beauty.finals.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 选美决赛
 * 
 * @author lating
 */
@HawkConfigManager.KVResource(file = "activity/beauty_contest_finals/beauty_contest_finals_cfg.xml")
public class  BeautyContestFinalKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	/**
	 *  是否跨天重置(配1则每天零点重置成就)
	 */
	private final int dailyRefresh;
	
	public BeautyContestFinalKVCfg(){
		serverDelay =0;
		dailyRefresh = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getDailyRefresh() {
		return dailyRefresh;
	}

}