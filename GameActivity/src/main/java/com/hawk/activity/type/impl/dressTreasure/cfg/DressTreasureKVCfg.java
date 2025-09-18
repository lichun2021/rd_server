package com.hawk.activity.type.impl.dressTreasure.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 装扮投放系列活动四:硝烟再起
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/dress_treasure/dress_treasure_cfg.xml")
public class  DressTreasureKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	
	private final int awardPoolRandomStart;
	
	private final int awardPoolRandomEnd;
	
	private final int randomStart;
	
	private final int randomEnd;
	
	private final int extendCount;
	
	private final String resetCost;
	
	private final String scoreAdd;
	
	private final int resetCountLimit;
	
	
	public DressTreasureKVCfg(){
		serverDelay =0;
		awardPoolRandomStart = 0;
		awardPoolRandomEnd = 0;
		randomStart = 0;
		randomEnd = 0;
		extendCount = 0;
		resetCost = "";
		scoreAdd= "";
		resetCountLimit= 0;
	}
	
	
	
	
	@Override
	protected boolean checkValid() {
		if(awardPoolRandomStart > awardPoolRandomEnd){
			return false;
		}
		if(randomStart > randomEnd){
			return false;
		}
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(resetCost);
		if (!valid) {
			return false;
		}
		return super.checkValid();
	}


	public long getServerDelay() {
		return serverDelay * 1000L;
	}


	public int getAwardPoolRandomStart() {
		return awardPoolRandomStart;
	}

	public int getAwardPoolRandomEnd() {
		return awardPoolRandomEnd;
	}


	public int getRandomStart() {
		return randomStart;
	}


	public int getRandomEnd() {
		return randomEnd;
	}

	public int getExtendCount() {
		return extendCount;
	}

	
	
	public int getResetCountLimit() {
		return resetCountLimit;
	}




	public List<RewardItem.Builder> getResetCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.resetCost);
	}

	
	public List<RewardItem.Builder> getScoreAddItemList() {
		return RewardHelper.toRewardItemImmutableList(this.scoreAdd);
	}

	
}