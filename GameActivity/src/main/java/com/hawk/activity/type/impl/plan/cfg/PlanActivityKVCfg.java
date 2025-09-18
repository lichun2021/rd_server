package com.hawk.activity.type.impl.plan.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/plan/plan_activity_cfg.xml")
public class PlanActivityKVCfg extends HawkConfigBase {
	
	@Override
	protected boolean assemble() {
		
		try {
			planSenioItemList = RewardHelper.toRewardItemImmutableList(planSenioItem);
			planBlastItemList = RewardHelper.toRewardItemImmutableList(planBlastItem);
			senioItemPriceList = RewardHelper.toRewardItemImmutableList(senioItemPrice);
			bastItemPriceList = RewardHelper.toRewardItemImmutableList(bastItemPrice);
			extRewardSenioList = RewardHelper.toRewardItemImmutableList(extRewardSenio);
			extRewardBlastList = RewardHelper.toRewardItemImmutableList(extRewardBlast);
		} catch (Exception e) {
			HawkLog.errPrintln("assemble error! file: {}", this.getClass().getAnnotation(XmlResource.class).file());
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}

	private final int serverDelay;
	
	// 高级充能消耗
	private final String planSenioItem;
	private List<RewardItem.Builder> planSenioItemList;
	
	//磁暴充能消耗
	private final String planBlastItem;
	private List<RewardItem.Builder> planBlastItemList;
	
	//高级充能道具价格
	private final String senioItemPrice;
	private List<RewardItem.Builder> senioItemPriceList;
	
	//磁暴充能道具价格
	private final String bastItemPrice;
	private List<RewardItem.Builder> bastItemPriceList;
	
	//高级充能获得固定奖励
	private final String extRewardSenio;
	private List<RewardItem.Builder> extRewardSenioList;
	
	//磁暴充能得固定奖励
	private final String extRewardBlast;
	private List<RewardItem.Builder> extRewardBlastList;
	
	//进榜最低奖励
	private final int rankMinScore;
	
	//榜单最大人数
	private final int rankerMax;
	
	public PlanActivityKVCfg(){
		serverDelay = 0;
		// 高级充能消耗
		planSenioItem = "";
		//磁暴充能消耗
		planBlastItem = "";
		//高级充能道具价格
		senioItemPrice = "";
		//磁暴充能道具价格
		bastItemPrice = "";
		//高级充能获得固定奖励
		extRewardSenio = "";
		//磁暴充能得固定奖励
		extRewardBlast = "";
		
		rankMinScore = 0;
		
		rankerMax = 0;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}
	
	public String getPlanSenioItem() {
		return planSenioItem;
	}

	public String getPlanBlastItem() {
		return planBlastItem;
	}

	public String getSenioItemPrice() {
		return senioItemPrice;
	}

	public String getBastItemPrice() {
		return bastItemPrice;
	}

	public String getExtRewardSenio() {
		return extRewardSenio;
	}

	public String getExtRewardBlast() {
		return extRewardBlast;
	}

	public List<RewardItem.Builder> getPlanSenioItemList() {
		return planSenioItemList;
	}

	public List<RewardItem.Builder> getPlanBlastItemList() {
		return planBlastItemList;
	}

	public List<RewardItem.Builder> getSenioItemPriceList() {
		return senioItemPriceList;
	}

	public List<RewardItem.Builder> getBastItemPriceList() {
		return bastItemPriceList;
	}

	public List<RewardItem.Builder> getExtRewardSenioList() {
		return extRewardSenioList;
	}

	public List<RewardItem.Builder> getExtRewardBlastList() {
		return extRewardBlastList;
	}
	
	public List<RewardItem.Builder> getPlanLotteryCost(int type){
		if(1 == type){
			return getPlanSenioItemList();
		}
		return getPlanBlastItemList();
	}
	
	public List<RewardItem.Builder> getPlanLotteryPrice(int type){
		if(1 == type){
			return getSenioItemPriceList();
		}
		return getBastItemPriceList();
	}

	public List<RewardItem.Builder> getPlanLotteryReward(int type){
		if(1 == type){
			return getExtRewardSenioList();
		}
		return getExtRewardBlastList();
	}

	public int getRankMinScore() {
		return rankMinScore;
	}

	public int getRankerMax() {
		return rankerMax;
	}
	
}
