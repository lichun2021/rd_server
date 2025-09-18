package com.hawk.activity.type.impl.monthcard.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 定制类特权卡定制奖励
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/monthCard_madeReward.xml")
public class MonthCardCustomRewardCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	// 对应周卡（月卡）ID
	private final int cardId;
	// 奖励内容
	private final String reward;

	public MonthCardCustomRewardCfg() {
		id = 0;
		cardId = 0;
		reward = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public int getCardId() {
		return cardId;
	}

	public String getReward() {
		return reward;
	}

}
