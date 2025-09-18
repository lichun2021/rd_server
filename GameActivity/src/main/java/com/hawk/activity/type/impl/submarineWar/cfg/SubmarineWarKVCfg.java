package com.hawk.activity.type.impl.submarineWar.cfg;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/submarine_war/submarine_war_cfg.xml")
public class SubmarineWarKVCfg extends HawkConfigBase {
	
	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	private final int hp;
	private final int gameCountLimit;
	private final String buyGameCountCost;
	private final int buyGameTimes;
	private final int serverNumber;
	private final String scoreItem;
	private final String recover;
	private final int rankSize;
	private final int barrageSize;
	private final String dailyRankRewarSortTime;
	private final String dailyRankRewarSendTime;
	
	private final String goldPrice;
	private final int goldTimeLimit;
	private final float baseTransRate;
	private final float highTransRate;
	private final int sweepLevel;
	private final int baseLimit;
	
	public SubmarineWarKVCfg(){
		serverDelay = 0;
		hp = 0;
		gameCountLimit = 0;
		buyGameCountCost = "";
		buyGameTimes = 0;
		serverNumber = 0;
		rankSize = 0;
		barrageSize = 0;
		dailyRankRewarSortTime = "";
		dailyRankRewarSendTime = "";
		scoreItem = "";
		recover = "";
		goldPrice = "";
		goldTimeLimit = 0;
		baseTransRate = 0f;
		highTransRate = 0f;
		sweepLevel = 1;
		baseLimit = 0;
	}
	

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}
	
	
	public int getHp() {
		return hp;
	}
	
	public int getGameCountLimit() {
		return gameCountLimit;
	}
	
	public int getServerNumber() {
		return serverNumber;
	}
	
	public int getBuyGameTimes() {
		return buyGameTimes;
	}
	
	public int getRankSize() {
		return rankSize;
	}
	
	public int getBarrageSize() {
		return barrageSize;
	}
	
	public int getGoldTimeLimit() {
		return goldTimeLimit;
	}
	
	public String getGoldPrice() {
		return goldPrice;
	}
	
	public float getBaseTransRate() {
		return baseTransRate;
	}
	
	public float getHighTransRate() {
		return highTransRate;
	}
	
	public int getSweepLevel() {
		return sweepLevel;
	}
	
	public int getBaseLimit() {
		return baseLimit;
	}
	
	
	public HawkTuple3<Integer, Integer, Integer> getDailyRankRewarSortHour() {
		String[] arr = this.dailyRankRewarSortTime.split(SerializeHelper.COLON_ITEMS);
		return HawkTuples.tuple(Integer.parseInt(arr[0]),
				Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
	}
	
	public HawkTuple3<Integer, Integer, Integer> getDailyRankRewarSendHour() {
		String[] arr = this.dailyRankRewarSendTime.split(SerializeHelper.COLON_ITEMS);
		return HawkTuples.tuple(Integer.parseInt(arr[0]),
				Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
	}
	
	
	public List<RewardItem.Builder> getBuyGameCountCost(int buyTimes) {
		String[] arr = this.buyGameCountCost.split(SerializeHelper.SEMICOLON_ITEMS);
		if(buyTimes <= arr.length){
			return RewardHelper.toRewardItemImmutableList(arr[buyTimes -1]);
		}
		return RewardHelper.toRewardItemImmutableList(arr[arr.length -1]);
	}
	
	
	
	public Map<String,String> getRecoverMap(){
		Map<String,String> rmap = new HashMap<>();
		String[] arr = this.recover.trim().split(SerializeHelper.ELEMENT_SPLIT);
		for(String str : arr){
			String[] param = str.split(SerializeHelper.BETWEEN_ITEMS);
			if(param.length == 1){
				rmap.put(param[0], "");
			}
			if(param.length == 2){
				rmap.put(param[0], param[1]);
			}
		}
		return rmap;
	}
	
	
	public RewardItem.Builder getScoreItem(int count){
		RewardItem.Builder builder = RewardHelper.toRewardItem(this.scoreItem);
		builder.setItemCount(count);
		return builder;
	}
	
	
	@Override
	protected final boolean checkValid() {
		boolean valid = true;
		String[] arr = this.buyGameCountCost.split(SerializeHelper.SEMICOLON_ITEMS);
		for(String str : arr){
			valid = ConfigChecker.getDefaultChecker().checkAwardsValid(str);
			if (!valid) {
				throw new InvalidParameterException(String.format("SubmarineWarKVCfg buyGameCountCost error : %s", buyGameCountCost));
			}
		}
		
		return super.checkValid();
	}
}
