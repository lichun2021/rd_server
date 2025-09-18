package com.hawk.activity.type.impl.overlordBlessing.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/overlord_blessing/overlord_blessing_cfg.xml")
public class OverlordBlessingKVCfg extends HawkConfigBase {
	
	//# 服务器开服延时开启活动时间；单位:秒
	private final int serverDelay;
	//霸主奖励
	private final String overlordReward;
	//膜拜奖励
	private final String civilReward;
	//霸主分享奖励
	private final String overlordShareReward;
	//膜拜者分享奖励
	private final String civilShareReward;
	//WX到达指定值
	private final String waterLimitWX;
	//WX注水比例
	private final String waterScaleWX;
	//QQ到达指定值
	private final String waterLimitQQ;
	//QQ注水比例
	private final String waterScaleQQ;
	
	private final String allianceLimitTime; 
	
	private final int maxMarchTime;
	
	private List<RewardItem.Builder> overlordRewardList;
	private List<RewardItem.Builder> civilRewardList;
	private List<RewardItem.Builder> overlordShareRewardList;
	private List<RewardItem.Builder> civilShareRewardList;
	private long allianceLimitTimeValue; 
	
	public OverlordBlessingKVCfg(){
		serverDelay = 0;
		overlordReward = "";
		civilReward = "";
		overlordShareReward = "";
		civilShareReward = "";
		waterLimitWX = "";
		waterScaleWX = "";
		waterLimitQQ = "";
		waterScaleQQ = "";
		allianceLimitTime ="";
		maxMarchTime = 180;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	@Override
	protected boolean assemble() {
		overlordRewardList = RewardHelper.toRewardItemImmutableList(overlordReward);
		civilRewardList = RewardHelper.toRewardItemImmutableList(civilReward);
		overlordShareRewardList = RewardHelper.toRewardItemImmutableList(overlordShareReward);
		civilShareRewardList = RewardHelper.toRewardItemImmutableList(civilShareReward);
		
		allianceLimitTimeValue = HawkTime.parseTime(allianceLimitTime);
		return super.assemble();
	}
	
	public long getMaxMarchTime() {
		return maxMarchTime * 1000L;
	}

	public String getOverlordReward() {
		return overlordReward;
	}

	public String getCivilReward() {
		return civilReward;
	}

	public String getOverlordShareReward() {
		return overlordShareReward;
	}

	public String getCivilShareReward() {
		return civilShareReward;
	}

	public String getWaterLimitWX() {
		return waterLimitWX;
	}

	public String getWaterScaleWX() {
		return waterScaleWX;
	}

	public String getWaterLimitQQ() {
		return waterLimitQQ;
	}

	public String getWaterScaleQQ() {
		return waterScaleQQ;
	}

	public List<RewardItem.Builder> getOverlordRewardList() {
		return overlordRewardList;
	}

	public List<RewardItem.Builder> getCivilRewardList() {
		return civilRewardList;
	}

	public List<RewardItem.Builder> getOverlordShareRewardList() {
		return overlordShareRewardList;
	}

	public List<RewardItem.Builder> getCivilShareRewardList() {
		return civilShareRewardList;
	}

	public long getAllianceLimitTimeValue() {
		return allianceLimitTimeValue;
	}

	public void setAllianceLimitTimeValue(long allianceLimitTimeValue) {
		this.allianceLimitTimeValue = allianceLimitTimeValue;
	}

	
}
