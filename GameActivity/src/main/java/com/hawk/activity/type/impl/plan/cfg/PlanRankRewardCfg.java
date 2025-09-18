package com.hawk.activity.type.impl.plan.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 排名奖励配置
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/plan/plan_ranking.xml")
public class PlanRankRewardCfg extends HawkConfigBase {

	/** */
	@Id
	private final int id;
	/** 奖励列表*/
	private final String rewards;
	
	//排名最小排行
	private final long rankMinScore;
	
	private List<RewardItem.Builder> rewardList;

	public PlanRankRewardCfg() {
		id = 0;
		rewards = "";
		rankMinScore = 0;
	}
	
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		} catch (Exception e) {
			HawkLog.errPrintln("assemble error! file: {}, id: {}, reward: {}", this.getClass().getAnnotation(XmlResource.class).file(), this.id, this.rewards);
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("PlanRankRewardCfg reward error, id: %s , reward: %s", id, rewards));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public String getReward() {
		return rewards;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public long getRankMinScore() {
		return rankMinScore;
	}
}
