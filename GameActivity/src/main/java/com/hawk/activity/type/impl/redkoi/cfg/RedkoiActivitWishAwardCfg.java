package com.hawk.activity.type.impl.redkoi.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 许愿奖励配置
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/redkoi/koi_fish_cell_cfg.xml")
public class RedkoiActivitWishAwardCfg extends HawkConfigBase {

	@Id
	private final int id;
	private final String rewards;
	private final int singleWeight;
	private final int tenWeight;

	public RedkoiActivitWishAwardCfg() {
		this.id = 0;
		this.rewards = "";
		this.singleWeight = 0;
		this.tenWeight = 0;
	}

	public int getId() {
		return id;
	}

	public String getRewards() {
		return rewards;
	}

	public int getSingleWeight() {
		return singleWeight;
	}

	public int getTenWeight() {
		return tenWeight;
	}

	

}
