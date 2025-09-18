package com.hawk.activity.type.impl.questTreasure.cfg;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/quest_treasure/quest_treasure_cfg.xml")
public class QuestTreasureKVCfg extends HawkConfigBase {
	
	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	
	private final String gameMapRange;
	
	private final String home;

	private final int chooseCount;

	private final String recover;

	private final int randomItem;
	
	private final String dicePrice;
	
	private final String randomWeight;
	
	private final int gameRefreshCount;
	
	private final int scoreItem;
	
	public QuestTreasureKVCfg(){
		serverDelay = 0;
		gameMapRange = "";
		home = "";
		chooseCount = 0;
		recover = "";
		dicePrice = "";
		randomItem = 0;
		randomWeight = "";
		gameRefreshCount = 0;
		scoreItem = 0;
	}
	

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}
	
	
	public Map<Integer, Integer> getRandomWeightMap() {
		return  SerializeHelper.stringToMap(this.randomWeight, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.ELEMENT_SPLIT);
	}
	
	
	public List<RewardItem.Builder> getDicePriceItem(int milt) {
		List<RewardItem.Builder> list = RewardHelper.toRewardItemImmutableList(this.dicePrice);
		for(RewardItem.Builder rb : list){
			long count = rb.getItemCount() * milt;
			rb.setItemCount(count);
		}
		return list;
	}
	
	
	public RewardItem.Builder getRandomItem(int milt) {
		RewardItem.Builder costBuilder = RewardItem.newBuilder();
		//类型为道具
		costBuilder.setItemType(ItemType.TOOL_VALUE);
		//待扣除物品ID
		costBuilder.setItemId(this.randomItem);
		//待扣除的物品数量
		costBuilder.setItemCount(milt);
		
		return costBuilder;
	}
	
	
	public int getChooseCount() {
		return chooseCount;
	}
	
	
	
	public HawkTuple2<Integer, Integer> getHomePos() {
		String[] arr = this.home.trim().split(",");
		return HawkTuples.tuple(Integer.parseInt(arr[0]) , Integer.parseInt(arr[1]));
	}
	
	
	public HawkTuple2<Integer, Integer> getRange() {
		String[] arr = this.gameMapRange.trim().split(",");
		return HawkTuples.tuple(Integer.parseInt(arr[0]) , Integer.parseInt(arr[1]));
	}
	
	public int getGameRefreshCount() {
		return gameRefreshCount;
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
	
	public int getScoreItem() {
		return scoreItem;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = true;
		String[] arr = this.recover.trim().split("|");
		for(String str : arr){
			valid = ConfigChecker.getDefaultChecker().checkAwardsValid(str);
			if (!valid) {
				throw new InvalidParameterException(String.format("QuestTreasureKVCfg recover error, recover: %s", recover));
			}
		}
		return super.checkValid();
	}
}
