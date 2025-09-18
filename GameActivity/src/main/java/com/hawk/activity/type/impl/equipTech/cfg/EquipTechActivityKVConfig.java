package com.hawk.activity.type.impl.equipTech.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/equipment_tech/equipment_tech_cfg.xml")
public class EquipTechActivityKVConfig extends HawkConfigBase {
	
	private final int serverDelay;
	
	private final int getScore;
	
	private final String noticeEquipment;
	
	
	private List<RewardItem.Builder> noticeRewards;
	
	public EquipTechActivityKVConfig(){
		serverDelay = 0;
		getScore = 0;
		noticeEquipment = "";
	}
	

	@Override
	protected boolean assemble() {
		noticeRewards = RewardHelper.toRewardItemImmutableList(noticeEquipment);
		return true;
	}


	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	public int getGetScore() {
		return getScore;
	}

	public String getNoticeEquipment() {
		return noticeEquipment;
	}


	public List<RewardItem.Builder> getNoticeRewards() {
		return noticeRewards;
	}
	
}
