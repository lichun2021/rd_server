package com.hawk.activity.type.impl.skinPlan.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
/**
 * 金字塔活动积分奖励配置
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/hero_skin/hero_skin_reward.xml")
public class SkinPlanRewardCfg extends HawkConfigBase{
	private final int id;
	//条件类型
	private final String needGold;
	//条件值
	private final String needItem;
	//档位的分数
	private final String point;
	//奖励
	private final String rewardID;
	
	public SkinPlanRewardCfg(){
		id = 0;
		needGold = "";
		needItem = "";
		point = "";
		rewardID = "";
	}

	public int getId() {
		return id;
	}

	public String getNeedGold() {
		return needGold;
	}

	public String getNeedItem() {
		return needItem;
	}

	public String getPoint() {
		return point;
	}

	public String getRewardID() {
		return rewardID;
	}

	public List<RewardItem.Builder> buildPrize(int buyCnt){
		List<RewardItem.Builder> list = new ArrayList<RewardItem.Builder>();
		if(buyCnt <= 0){
			return list;
		}
		for(int i = 0 ; i < buyCnt ; i ++){
			list.addAll(RewardHelper.toRewardItemList(this.needGold));
			list.addAll(RewardHelper.toRewardItemList(this.needItem));
		}
		return list;
	}
	
	
}
