package com.hawk.activity.type.impl.honourHeroBefell.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 
 * @author che
 */
@HawkConfigManager.KVResource(file = "activity/honour_hero_befell/honour_hero_befell_cfg.xml")
public class  HonourHeroBefellKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间；单位:秒
	 */
	private final int serverDelay;
	
	/**
	 * 免费次数
	 */
	private final int freeTimes;
	
	
	private final String oneCost;
	
	private final String tenCost;
	
	private final int luckyPer;
	
	private final int exchangeHeroLimit;
	
	private final int limitTimes;
	
	private final String fixReward;
	
	
	public HonourHeroBefellKVCfg(){
		serverDelay =0;
		freeTimes = 0;
		oneCost= "";
		tenCost = "";
		luckyPer = 0;
		exchangeHeroLimit= 0;
		limitTimes= 0;
		fixReward = "";
	}
	
	public long getServerDelay() {
		return serverDelay * 1000L;
	}
	
	public int getFreeTimes() {
		return freeTimes;
	}
		
	public int getLuckyPer() {
		return luckyPer;
	}
	
	public int getExchangeHeroLimit() {
		return exchangeHeroLimit;
	}
	
	public int getLimitTimes() {
		return limitTimes;
	}
	
	public List<RewardItem.Builder> getOneCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.oneCost);
	}
	
	public List<RewardItem.Builder> getTenCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.tenCost);
	}
	
	public List<RewardItem.Builder> getFixItemList(int milt) {
		List<RewardItem.Builder> list =  RewardHelper.toRewardItemImmutableList(this.fixReward);
		for(RewardItem.Builder builder : list){
			builder.setItemCount(builder.getItemCount() * milt);
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