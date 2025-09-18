package com.hawk.activity.type.impl.battlefield.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 战地寻宝活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/battlefield_treasure/battlefield_award_pool.xml")
public class BattleFieldCellAwardCfg extends HawkConfigBase {
	@Id
	private final int id;
	/** 奖励类型 */
	private final int type;
	/** 奖励列表*/
	private final String item;
	
	private List<RewardItem.Builder> rewardList;

	public BattleFieldCellAwardCfg() {
		id = 0;
		type = 0;
		item = "";
	}
	
	public int getId() {
		return id;
	}

	public int getAwardType() {
		return type;
	}

	public String getRewards() {
		return item;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(item);
		return true;
	}

}
