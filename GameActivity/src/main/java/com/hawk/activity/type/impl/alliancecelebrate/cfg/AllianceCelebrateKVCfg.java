package com.hawk.activity.type.impl.alliancecelebrate.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 双十一联盟欢庆 配置
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/ssy_alliance_celebrate/ssy_alliance_celebrate_cfg.xml")
public class AllianceCelebrateKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;
	//捐献道具
	private final String donateItem;
	//捐献积分
	private final int donateExp;


	public AllianceCelebrateKVCfg() {
		serverDelay = 0;
		donateItem = "";
		donateExp = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getDonateItem() {
		return donateItem;
	}

	public int getDonateExp() {
		return donateExp;
	}

}
