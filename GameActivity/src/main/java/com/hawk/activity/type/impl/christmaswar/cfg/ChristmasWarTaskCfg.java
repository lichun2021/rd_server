package com.hawk.activity.type.impl.christmaswar.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/christmas_war/christmas_war_task.xml")
public class ChristmasWarTaskCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 完成的任务数量
	 */
	private final int taskNumber;
	/**
	 * 可召唤的boss数量.
	 */
	private final int bossNumber;
	/**
	 * 奖励
	 */
	private final String award;
	/**
	 * 奖励
	 */
	private List<RewardItem.Builder> awardList;
	
	public ChristmasWarTaskCfg() {
		this.id = 0;
		this.taskNumber = 0;
		this.bossNumber = 0;
		this.award = "";
	}
	
	public int getId() {
		return id;
	}
	public int getTaskNumber() {
		return taskNumber;
	}
	public int getBossNumber() {
		return bossNumber;
	}
	public String getAward() {
		return award;
	}
	public List<RewardItem.Builder> getAwardList() {
		return awardList;
	}
	
	@Override
	public boolean assemble() {
		if (!HawkOSOperator.isEmptyString(award)) {
			 awardList = Collections.synchronizedList(RewardHelper.toRewardItemImmutableList(award));
		} else {
			awardList = Collections.synchronizedList(new ArrayList<>());
		}
						
		return true;
	}
	
	@Override
	public boolean checkValid() {
		return ConfigChecker.getDefaultChecker().checkAwardsValid(award);		
	}
}
