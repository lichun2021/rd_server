package com.hawk.activity.type.impl.lotteryTicket.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
@HawkConfigManager.XmlResource(file = "activity/lottery_ticket/lottery_ticket_reward.xml")
public class LotteryTicketRewardCfg extends HawkConfigBase{
	@Id
	private final int id;
	// 组
	private final int pool;
	private final int weight1;
	private final int weight2;
	// 获取道具
	private final String reward;
	private final int multiplyValue;
	private final int showValue;
	private final int broadcast;
	private final String assistReward;
	
	
	
	private static Map<Integer,Map<Integer,Integer>> weightMap1 = new HashMap<>();
	private static Map<Integer,Map<Integer,Integer>> weightMap2 = new HashMap<>();
	private static Set<Integer> showValSet = new HashSet<>();
	
	
	
	public LotteryTicketRewardCfg() {
		this.id = 0;
		this.pool = 0;
		this.weight1 = 0;
		this.weight2 = 0;
		this.reward = "";
		multiplyValue = 0;
		showValue = 0;
		broadcast = 0;
		assistReward = "";
	}
	
	public int getId() {
		return id;
	}

	public int getPool() {
		return pool;
	}

	public int getWeight1() {
		return weight1;
	}
	
	public int getWeight2() {
		return weight2;
	}
	
	public List<RewardItem.Builder> getRewardItemList() {
		if(!HawkOSOperator.isEmptyString(this.reward)){
			List<RewardItem.Builder> rlist = RewardHelper.toRewardItemImmutableList(this.reward);
			for(RewardItem.Builder builder : rlist){
				builder.setItemCount(builder.getItemCount() * this.multiplyValue);
			}
			return rlist;
    	}
		return new ArrayList<>();
	}
	
	
	public String getAssistReward() {
		return assistReward;
	}
	
	public List<RewardItem.Builder>  getAssistRewardList() {
		if(!HawkOSOperator.isEmptyString(this.assistReward)){
			List<RewardItem.Builder> rlist = RewardHelper.toRewardItemImmutableList(this.assistReward);
			return rlist;
    	}
		return new ArrayList<>();
	}
	
	public int getBroadcast() {
		return broadcast;
	}
	
	public int getMultiplyValue() {
		return multiplyValue;
	}
	
	public int getShowValue() {
		return showValue;
	}

	
	public static Map<Integer, Integer> getWeightMap1(int pool) {
		return weightMap1.get(pool);
	}
	
	public static Map<Integer, Integer> getWeightMap2(int pool) {
		return weightMap2.get(pool);
	}
	
	public static Set<Integer> getShowValSet() {
		return showValSet;
	}
	
	public boolean assemble() {
		Map<Integer,Integer> map1 = weightMap1.get(pool);
		if(map1 == null){
			map1 = new HashMap<>();
			weightMap1.put(pool, map1);
		}
		map1.put(this.id, this.weight1);
		
		Map<Integer,Integer> map2 = weightMap2.get(pool);
		if(map2 == null){
			map2 = new HashMap<>();
			weightMap2.put(pool, map2);
		}
		map2.put(this.id, this.weight2);
		
		showValSet.add(this.showValue);
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("LotteryTicketRewardCfg reward error, id: %s , reward: %s", id, reward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(assistReward);
		if (!valid) {
			throw new InvalidParameterException(String.format("LotteryTicketRewardCfg assistReward error, id: %s , assistReward: %s", id, assistReward));
		}
		return super.checkValid();
		
				
	}
	
	
}
