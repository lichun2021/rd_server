package com.hawk.activity.type.impl.starInvest.cfg;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


@HawkConfigManager.XmlResource(file = "activity/star_invest/star_invest_explore_box.xml")
public class StarInvestExploreBox extends HawkConfigBase {
	// 礼包ID
	@Id 
	private final int id;
	
	private final  int quality;
	
	private final int normalWeight;
	
	private final int upWeight;
	
	private final String awardWeight;
	private final String awards1;
	private final String awards2;
	private final String awards3;
	
	private Map<Integer,Integer> awardWeightMap;
	
	public StarInvestExploreBox() {
		id = 0;
		quality = 0;
		normalWeight= 0;
		upWeight = 0;
		awards1= "";
		awards2= "";
		awards3= "";
		awardWeight = "";
	}
	
	@Override
	protected boolean assemble() {
		Map<Integer,Integer> awardWeightMapTemp = new HashMap<>();
		String[] awArr = awardWeight.split("_");
		if(awArr.length != 3){
			return false;
		}
		for(int i=0;i<awArr.length;i++){
			awardWeightMapTemp.put(i+1, Integer.parseInt(awArr[i]));
		}
		this.awardWeightMap = awardWeightMapTemp;
		return true;
	}


	public int getId() {
		return id;
	}

	
	public int getQuality() {
		return quality;
	}
	
	public int getNormalWeight() {
		return normalWeight;
	}
	
	public int getUpWeight() {
		return upWeight;
	}
	
	public Map<Integer, Integer> getAwardWeightMap() {
		return awardWeightMap;
	}
	
	
	
	public HawkTuple2<Integer,List<RewardItem.Builder>> getRewardList() {
		int index = HawkRand.randomWeightObject(this.awardWeightMap);
		if(index == 1){
			return HawkTuples.tuple(1,RewardHelper.toRewardItemImmutableList(this.awards1));
		}
		if(index == 2){
			return HawkTuples.tuple(2,RewardHelper.toRewardItemImmutableList(this.awards2));
		}
		if(index == 3){
			return HawkTuples.tuple(3,RewardHelper.toRewardItemImmutableList(this.awards3));
		}
		return null;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.awards1);
		if (!valid) {
			throw new InvalidParameterException(String.format("StarInvestExploreBox reward error, id: %s , reward: %s", id, awards1));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.awards2);
		if (!valid) {
			throw new InvalidParameterException(String.format("StarInvestExploreBox reward error, id: %s , reward: %s", id, awards2));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.awards2);
		if (!valid) {
			throw new InvalidParameterException(String.format("StarInvestExploreBox reward error, id: %s , reward: %s", id, awards3));
		}
		return super.checkValid();
	}
	
}
