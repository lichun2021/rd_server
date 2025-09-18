package com.hawk.activity.type.impl.luckyStar.cfg;

import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/lucky_star/lucky_star_cfg.xml")
public class LuckyStarKVCfg extends HawkConfigBase {
	
	private final int serverDelay;

	/** 每抽奖多少次 **/
	private final int lotteryCnt;
	
	/** 必得奖励ID **/
	private final int rewardId;
	
	/** 抽奖消耗 **/
	private final String cost;
	
	private List<RewardItem.Builder> costList = null;
	
	private static LuckyStarKVCfg instance = null;
	
	public static LuckyStarKVCfg getInstance(){
		return instance;
	}
	
	@Override
	protected boolean assemble() {
		if(empty(cost) || rewardId == 0 || lotteryCnt == 0){
			throw new RuntimeException("lucky_star_cfg.xml error!");
		}
		costList = RewardHelper.toRewardItemImmutableList(cost);
		return super.assemble();
	}
	
	
	private boolean empty(String src){
		if(src == null || src.trim().equals("")){
			return true;
		}
		return false;
	}
	public LuckyStarKVCfg() {
		serverDelay = 0;
		lotteryCnt = 0;
		rewardId = 0;
		cost = "";
		instance = this;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
	public int getLotteryCnt() {
		return lotteryCnt;
	}

	public int getRewardId() {
		return rewardId;
	}

	public String getCost() {
		return cost;
	}

	public List<RewardItem.Builder> getCostList() {
		List<RewardItem.Builder> list = new ArrayList<>();
		for(RewardItem.Builder builder : costList){
			RewardItem.Builder clone = RewardItem.newBuilder();
			clone.setItemId(builder.getItemId());
			clone.setItemType(builder.getItemType());
			clone.setItemCount(builder.getItemCount());
			list.add(clone);
		}
		return list;
	}
}
