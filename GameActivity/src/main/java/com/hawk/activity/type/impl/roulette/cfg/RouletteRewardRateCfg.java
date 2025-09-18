package com.hawk.activity.type.impl.roulette.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;
@XmlResource(file="activity/roulette/roulette_reward.xml")
public class RouletteRewardRateCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int weight;
	
	private final boolean canSelected;
	private final String reward;
	private List<RewardItem.Builder> rewardList;

	private List<String> rewardStrList;
	public int getId() {
		return id;
	}

	public int getWeight() {
		return weight;
	}

	public String getReward() {
		return reward;
	}
	
	public RouletteRewardRateCfg() {
		canSelected = false;
		this.id = 0;
		this.weight = 0;
		this.reward = "";
	}
	
	@Override
	protected boolean assemble() {
		try{
			rewardList = RewardHelper.toRewardItemImmutableList(reward);
			rewardStrList = SerializeHelper.stringToList(String.class, this.reward ,",");		
		}catch(Exception e){
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(getReward());
		if(!valid){
			throw new InvalidParameterException(String.format("reward error, id: %d, Class name: %s ", getId(), getClass().getName())); 
		}
		if(weight < 0){
			throw new InvalidParameterException(String.format("rate error, rate: %d, Class name: %s ", getWeight(), getClass().getName()));
		}
		return super.checkValid();
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public boolean isCanSelected() {
		return canSelected;
	}
	
	public static int getTotalWeight(){
		int totalWeight = 0;
		ConfigIterator<RouletteRewardRateCfg> iter = HawkConfigManager.getInstance().getConfigIterator(RouletteRewardRateCfg.class);
		while(iter.hasNext()){
			totalWeight += ((RouletteRewardRateCfg )iter.next()).getWeight();
		}
		return totalWeight;
	}
	
	public static RouletteRewardRateCfg getRandomCfg(){
		RouletteRewardRateCfg ret = null;
		int totalWeight = getTotalWeight();
		int curWeight = HawkRand.randInt(1, totalWeight);
		ConfigIterator<RouletteRewardRateCfg> iter = HawkConfigManager.getInstance().getConfigIterator(RouletteRewardRateCfg.class);
		while(iter.hasNext()){
			RouletteRewardRateCfg iterCfg = iter.next();
			if (iterCfg.getWeight() < curWeight) {
				curWeight -= iterCfg.getWeight();
				continue;
			}
			ret = iterCfg;
			break;
		}
		return ret;
	}
	
	public static int getCanSetMaxCount(){
		int ret = 0;
		ConfigIterator<RouletteRewardRateCfg> iter = HawkConfigManager.getInstance().getConfigIterator(RouletteRewardRateCfg.class);
		while(iter.hasNext()){
			if(iter.next().isCanSelected()){
				ret++;
			}
		}	
		return ret;
	}
	
	public boolean isInRewardList(String itemStr){
		return this.reward.indexOf(itemStr) != -1;
	}
	
	public RewardItem.Builder getRewardByStr(String itemStr){
		try{
			for(int i = 0; i < rewardStrList.size(); i++){
				if(rewardStrList.get(i).equals(itemStr)){
					return rewardList.get(i);
				}
			}			
		}catch(Exception e){
			HawkException.catchException(e);
		}
		return null;
	}
}
