package com.hawk.activity.type.impl.cakeShare.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 蛋糕分享
 * hf
 */
@HawkConfigManager.KVResource(file = "activity/celebration_receive_cake/celebration_receive_cake_cfg.xml")
public class CakeShareKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;
	
	private final String loginAward;
	
	private final int boatAward;
	
	private final int giftCount;
	
	private final String refreshTimes;
	
	private final String award;
	
	private final String dragonPoint;
	
	private final int areaRadius;
	//持续时间秒
	private final long rewardDuration;
	
	private final String noticeRewards;
	
	private List<Integer> refreshTimeList = new ArrayList<>();
	
	private List<HawkTuple2<Integer, Integer>> pointList = new ArrayList<>();
	
	private List<RewardItem.Builder> noticeRewardList;
	
	private Map<Integer,HawkTuple2<Integer, Integer>> awardMap = new HashMap<>();
	private HawkTuple2<Integer, Integer> defaultAward;
	
	public CakeShareKVCfg() {
		serverDelay = 0;
		loginAward = "";
		boatAward = 0;
		refreshTimes = "";
		giftCount = 0;
		dragonPoint = "";
		areaRadius = 0;
		rewardDuration = 3600;  //默认
		award = "";
		noticeRewards = "";
		
	}

	
	@Override
	protected boolean assemble() {
		SerializeHelper.stringToList(Integer.class, this.refreshTimes, 
				SerializeHelper.ATTRIBUTE_SPLIT,refreshTimeList);
		String[] points = dragonPoint.split(SerializeHelper.BETWEEN_ITEMS);
		for (int i = 0; i < points.length; i++) {
			String[] arr = points[i].split(SerializeHelper.ATTRIBUTE_SPLIT);
			pointList.add(new HawkTuple2<Integer, Integer>(Integer.valueOf(arr[0]), Integer.valueOf(arr[0])));
		}
		
		String[] awardArr = this.award.split(SerializeHelper.BETWEEN_ITEMS);
		for (int i = 0; i < awardArr.length; i++) {
			String[] arr = awardArr[i].split(SerializeHelper.ATTRIBUTE_SPLIT);
			HawkTuple2<Integer, Integer> tuple2 = HawkTuples.
					tuple(Integer.valueOf(arr[0]), Integer.valueOf(arr[1]));
			awardMap.put(tuple2.first, tuple2);
			if(Objects.isNull(this.defaultAward) || this.defaultAward.first < tuple2.first){
				this.defaultAward = tuple2;
			}
		}
		noticeRewardList = RewardHelper.toRewardItemImmutableList(noticeRewards);
		return super.assemble();
	}
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public List<Integer> getRefreshTimeList() {
		return refreshTimeList;
	}


	public int getGiftCount() {
		return giftCount;
	}


	public String getLoginAward() {
		return loginAward;
	}


	public int getBoatAward() {
		return boatAward;
	}


	public int getAreaRadius() {
		return areaRadius;
	}
	
	public long getRewardDuration() {
		return rewardDuration * 1000;
	}


	public List<HawkTuple2<Integer, Integer>> getPointList() {
		return pointList;
	}


	public void setPointList(List<HawkTuple2<Integer, Integer>> pointList) {
		this.pointList = pointList;
	}


	public Map<Integer, HawkTuple2<Integer, Integer>> getAwardMap() {
		return awardMap;
	}


	public HawkTuple2<Integer, Integer> getDefaultAward() {
		return defaultAward;
	}


	public HawkTuple2<Integer, Integer> getTurnAward(int turn){
		if(this.awardMap.containsKey(turn)){
			return this.awardMap.get(turn);
		}
		return this.getDefaultAward();
	}
	
	
	public boolean noticeItem(int itemType,int itmeId,long itemNum){
		for(RewardItem.Builder builder : this.noticeRewardList){
			if(builder.getItemType() == itemType &&
					builder.getItemId() == itmeId &&
						builder.getItemCount() == itemNum){
				return true;
			}
		}
		return false;
	}
	
}
