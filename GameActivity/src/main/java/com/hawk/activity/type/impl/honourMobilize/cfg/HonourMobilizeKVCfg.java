package com.hawk.activity.type.impl.honourMobilize.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 
 * @author che
 */
@HawkConfigManager.KVResource(file = "activity/honour_mobilize/%s/honour_mobilize_cfg.xml", autoLoad=false, loadParams="366")
public class  HonourMobilizeKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间；单位:秒*/
	private final int serverDelay;
	/** VIP等级*/
	private final int VIPLimit;
	/** 大本等级*/
	private final int baseLimit;
	/** 单抽消耗*/
	private final String cost1;
	/** 10抽消耗*/
	private final String cost2;
	/** 免费次数*/
	private final int freeCount;
	/** 每日上限*/
	private final int dailyLimit;
	/** 额外奖励*/
	private final String extReward;
	
	
	
	
	public HonourMobilizeKVCfg(){
		serverDelay = 0;
		VIPLimit = 0;
		baseLimit= 0;
		cost1 = "";
		cost2 = "";
		freeCount = 0;
		dailyLimit= 0;
		extReward = "";
	}
	
	
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}
	
	public int getBaseLimit() {
		return baseLimit;
	}
	
	public int getVIPLimit() {
		return VIPLimit;
	}
	
	public int getDailyLimit() {
		return dailyLimit;
	}
	
	public int getFreeCount() {
		return freeCount;
	}
	
	public List<RewardItem.Builder> getOneCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.cost1);
	}
	
	public List<RewardItem.Builder> getTenCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.cost2);
	}
	
	public List<RewardItem.Builder> getExtRewards(int mult) {
		List<RewardItem.Builder> list = RewardHelper.toRewardItemImmutableList(this.extReward);
		for(RewardItem.Builder reward : list){
			reward.setItemCount(reward.getItemCount() * mult);
		}
		return list;
	}
	
	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}
	
	
	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}


	
	
		
}