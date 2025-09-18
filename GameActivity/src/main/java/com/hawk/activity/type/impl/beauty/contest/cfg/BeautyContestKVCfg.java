package com.hawk.activity.type.impl.beauty.contest.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 选美初赛
 * 
 * @author lating
 */
@HawkConfigManager.KVResource(file = "activity/beauty_contest/beauty_contest_cfg.xml")
public class  BeautyContestKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	/**
	 *  是否跨天重置(配1则每天零点重置成就)
	 */
	private final int dailyRefresh;
	/**
	 * 初赛道具
	 */
	private final String item;
	/**
	 * 购买初赛道具的单价
	 */
	private final String buyItem;
	
	public BeautyContestKVCfg(){
		serverDelay =0;
		dailyRefresh = 0;
		item = "";
		buyItem = "";
	}
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getDailyRefresh() {
		return dailyRefresh;
	}

	public String getItem() {
		return item;
	}

	public String getBuyItem() {
		return buyItem;
	}
	
}