package com.hawk.activity.type.impl.plan.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 抽奖奖励配置
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/plan/plan_reward.xml")
public class PlanRewardCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 类型*/
	private final int type;
	/** 权重*/
	private final int weight;
	/** 奖励列表*/
	private final String rewards;
	
	private List<RewardItem.Builder> rewardList;
	
	static private List<PlanRewardCfg> nomalLotteryCfgAll = new ArrayList<PlanRewardCfg>() ;
	
	static public List<PlanRewardCfg> hightLotteryCfgAll = new ArrayList<PlanRewardCfg>();
	
	
	public PlanRewardCfg() {
		id = 0;
		type = 0;
		weight = 0;
		rewards = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
			if( 1 == this.type){
				nomalLotteryCfgAll.add(this);
			}else{
				hightLotteryCfgAll.add(this);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("PlanRewardCfg reward error, id: %s , reward: %s", id, rewards));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getWeight() {
		return weight;
	}

	public void setRewardList(List<RewardItem.Builder> rewardList) {
		this.rewardList = rewardList;
	}

	public String getReward() {
		return rewards;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
	public static int getTotalWeightByType(int type){
		int ret = 0;
		List<PlanRewardCfg> cfgs = PlanRewardCfg.getLotteryCfgAllByType(type);
		if(null != cfgs){
			for(PlanRewardCfg cfg : cfgs){
				ret += cfg.getWeight();
			}
		}
		return ret;
	}
	
	public static List<PlanRewardCfg> getLotteryCfgAllByType(int type){
		if(1 == type){
			return PlanRewardCfg.nomalLotteryCfgAll;
		}else{
			return PlanRewardCfg.hightLotteryCfgAll;
		}
	}
}
