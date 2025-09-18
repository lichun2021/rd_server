package com.hawk.activity.type.impl.loverMeet.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.XmlResource(file = "activity/lover_meet/lover_meet_answer.xml")
public class LoverMeetAnswerCfg extends HawkConfigBase{
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	
	private final String cost;
	
	private final String reward;
	
	private final int score;
	
	private final int favor;

	private final int favorLimit;
	
	private final int nextQuestion1;
	
	private final int nextQuestion2;
	
	private final String ending;
	
	private List<Integer> endingList;

	public LoverMeetAnswerCfg() {
		id = 0;
		cost = "";
		reward = "";
		score = 0;
		favor = 0;
		favorLimit = 0;
		nextQuestion1 = 0;
		nextQuestion2 = 0;
		ending = "";
		
	}

	
	@Override
	protected boolean assemble() {
		List<Integer> endingListTemp = new ArrayList<>();
		if(!HawkOSOperator.isEmptyString(this.ending)){
			String[] arr = this.ending.split(",");
			for(String str : arr){
				int id = Integer.parseInt(str);
				endingListTemp.add(id);
			}
		}
		this.endingList = ImmutableList.copyOf(endingListTemp);
		return super.assemble();
	}
	

	public int getId() {
		return id;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getFavor() {
		return favor;
	}
	
	
	public int getFavorLimit() {
		return favorLimit;
	}
	
	public int getNextQuestion1() {
		return nextQuestion1;
	}
	
	
	public int getNextQuestion2() {
		return nextQuestion2;
	}
	
	public List<Integer> getEndingList() {
		return endingList;
	}
	
	
	public List<RewardItem.Builder> getCostItemList() {
		return RewardHelper.toRewardItemImmutableList(this.cost);
	}
	
	public List<RewardItem.Builder> getRewardItemList() {
		return RewardHelper.toRewardItemImmutableList(this.reward);
	}
	
	
	
	
}
