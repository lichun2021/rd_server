package com.hawk.activity.type.impl.redblueticket.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 翻牌活动奖励道具
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/red_blue_ticket/red_blue_award_pool.xml")
public class RedBlueTicketActivityRewardCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int rewardId;
	
	private final String item;
	
	private final int Aweight;
	
	private final int Bweight;
	
	public RedBlueTicketActivityRewardCfg() {
		rewardId = 0;
		item = "";
		Aweight = 0;
		Bweight = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getRewardId() {
		return rewardId;
	}

	public String getItem() {
		return item;
	}

	public int getAweight() {
		return Aweight;
	}

	public int getBweight() {
		return Bweight;
	}

}
