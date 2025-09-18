package com.hawk.activity.type.impl.goldBaby.cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkRandObj;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;

import sun.awt.util.IdentityArrayList;

@HawkConfigManager.XmlResource(file = "activity/gold_baby/gold_baby_reward.xml")
public class GoldBabyRewardCfg extends HawkConfigBase implements HawkRandObj{

	@Id
	private final int id;
	
	private final int poolId;
	
	private final int level;
	
	private final String rewards;
	
	//倍数_权重，锁定奖品后在两个倍数之间随机
	private final String magnification1;
	
	//倍数_权重
	private final String magnification2;
	
	private final int weight;
	
	private List<RewardItem.Builder> rewardList;
	
	public GoldBabyRewardCfg() {
		id=0;
		poolId=0;
		level=0;
		rewards="";
		magnification1="";
		magnification2="";
		weight=0;
	}

	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public List<RewardItem.Builder> getRewardList() {
		return RewardHelper.toRewardItemImmutableList(rewards);
	}

	public void setRewardList(List<RewardItem.Builder> rewardList) {
		this.rewardList = rewardList;
	}

	public int getId() {
		return id;
	}

	public int getPoolId() {
		return poolId;
	}

	public int getLevel() {
		return level;
	}

	public String getRewards() {
		return rewards;
	}
	
	public int getWeight() {
		return weight;
	}

	public String getMagnification1() {
		return magnification1;
	}

	public String getMagnification2() {
		return magnification2;
	}
	
}	
