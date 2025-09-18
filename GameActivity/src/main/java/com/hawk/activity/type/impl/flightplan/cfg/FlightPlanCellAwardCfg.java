package com.hawk.activity.type.impl.flightplan.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 威龙庆典-飞行计划活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/j20_celebration/j20_celebration_cell_cfg.xml")
public class FlightPlanCellAwardCfg extends HawkConfigBase {
	@Id
	private final int id;
	/** 事件类型*/
	private final int event;
	/** 奖励列表*/
	private final String rewards;
	
	private List<RewardItem.Builder> rewardList;

	public FlightPlanCellAwardCfg() {
		id = 0;
		event = 0;
		rewards = "";
	}
	
	public int getId() {
		return id;
	}

	public int getEvent() {
		return event;
	}

	public String getRewards() {
		return rewards;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		return true;
	}

}
