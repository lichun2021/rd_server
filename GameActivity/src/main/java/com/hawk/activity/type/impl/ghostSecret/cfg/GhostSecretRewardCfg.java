package com.hawk.activity.type.impl.ghostSecret.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
																	
@HawkConfigManager.XmlResource(file = "activity/equip_treasure/equip_treasure_reward.xml")
public class GhostSecretRewardCfg extends HawkConfigBase {
	@Id
	private final int id;
	//是否特等奖
	private final int isSpec;
	//等级
	private final int level;
	//奖励
	private final String rewards;
	//卡1
	private final int card1;
	//卡2
	private final int card2;
	//卡3
	private final int card3;

	//3个卡的集合
	private List<Integer> cardInfo;
	//奖励
	private List<RewardItem.Builder> rewardList;
	
	public GhostSecretRewardCfg(){
		id = 0;
		isSpec = 0;
		level = 0;
		rewards = "";
		card1 = 0;
		card2 = 0;
		card3 = 0;
		cardInfo = new ArrayList<>();
		
	}

	@Override
	protected boolean assemble() {
		cardInfo.add(card1);
		cardInfo.add(card2);
		cardInfo.add(card3);
		Collections.sort(cardInfo);
		rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		return super.assemble();
	}

	public List<Integer> getCardInfo() {
		return cardInfo;
	}

	public void setCardInfo(List<Integer> cardInfo) {
		this.cardInfo = cardInfo;
	}


	public int getId() {
		return id;
	}

	public int getIsSpec() {
		return isSpec;
	}
	
	public int getLevel() {
		return level;
	}

	public String getRewards() {
		return rewards;
	}

	public int getCard1() {
		return card1;
	}

	public int getCard2() {
		return card2;
	}

	public int getCard3() {
		return card3;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public void setRewardList(List<RewardItem.Builder> rewardList) {
		this.rewardList = rewardList;
	}
	
	//是否是特等奖
	public boolean IsSpec() {
		return isSpec == 1;
	}

}
