package com.hawk.activity.type.impl.strongestGuild.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
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
 * 阶段积分奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "activity/strongest_alliance/activity_person_target_reward.xml")
public class StrongestGuildTargetRewardCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 目标id*/
	private final int targetId;
	/** 最小等级*/
	private final int lvMin;
	/** 最大等级*/
	private final int lvMax;
	/** 积分值*/
	private final int score;
	/** 奖励列表*/
	private final String reward;
	
	private List<RewardItem.Builder> rewardList;
	
	public StrongestGuildTargetRewardCfg() {
		id = 0;
		targetId = 0;
		lvMin = 0;
		lvMax = 0;
		score = 0;
		reward = "";
	}
	
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(reward);
		} catch (Exception e) {
			HawkLog.errPrintln("assemble error! file: {}, id: {}, reward: {}", this.getClass().getAnnotation(XmlResource.class).file(), this.id, this.reward);
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("StrongestGuildTargetRewardCfg reward error, id: %s , reward: %s", id, reward));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getTargetId() {
		return targetId;
	}

	public int getLvMin() {
		return lvMin;
	}

	public int getLvMax() {
		return lvMax;
	}

	public int getScore() {
		return score;
	}

	public String getReward() {
		return reward;
	}

	public List<RewardItem.Builder> getRewardList() {
		List<RewardItem.Builder> list = new ArrayList<>();
		for(RewardItem.Builder builder : rewardList){
			RewardItem.Builder clone = RewardItem.newBuilder();
			clone.setItemId(builder.getItemId());
			clone.setItemType(builder.getItemType());
			clone.setItemCount(builder.getItemCount());
			list.add(clone);
		}
		return list;
	}
}
