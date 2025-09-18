package com.hawk.activity.type.impl.loverMeet.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.XmlResource(file = "activity/lover_meet/lover_meet_ending.xml")
public class LoverMeetEndingCfg extends HawkConfigBase{
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	private final String reward;
	private final int favorRange1;
	private final int favorRange2;
	 
	public LoverMeetEndingCfg() {
		id = 0;
		reward = "";
		favorRange1 = 0;
		favorRange2 = 0;
		
	}

	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}
	

	public int getId() {
		return id;
	}
	
	public int getFavorRange1() {
		return favorRange1;
	}
	
	
	public int getFavorRange2() {
		return favorRange2;
	}
	
	
	
	
	public List<RewardItem.Builder> getRewardItemList() {
		return RewardHelper.toRewardItemImmutableList(this.reward);
	}
	
	
	
	
	
}
