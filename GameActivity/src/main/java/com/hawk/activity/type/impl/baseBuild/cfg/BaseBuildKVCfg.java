package com.hawk.activity.type.impl.baseBuild.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/base_build/%s/base_build_activity_cfg.xml", autoLoad=false, loadParams="161")
public class BaseBuildKVCfg extends HawkConfigBase {

	/** 可见活动大本等级 */
	private final int build;
	
	/** 可见活动初始等级 */
	private final int buildMin;

	/** 活动开始日期 */
	private final String activityTime;

	/** 额外奖励 */
	private final String awardItems;
	
	/** 额外高级奖励*/
	private final String superAwardItems;

	/** 安卓支付ID */
	private final int androidPayId;

	/** 苹果支付ID */
	private final int iosPayId;

	private final String endDate;
	
	private final String restartDate;
	
	
	private List<RewardItem.Builder> awardItemsList;

	private long activityTimeValue;
	
	private long endDateTimeValue;
	private long restartDateTimeValue;

	public BaseBuildKVCfg() {
		build = 0;
		activityTime = "";
		awardItems = "";
		superAwardItems = "";
		androidPayId = 0;
		iosPayId = 0;
		endDate = "";
		restartDate = "";
		buildMin = 0;
		
	}

	public List<RewardItem.Builder> getAwardItemsList() {
		return awardItemsList;
	}

	public void setAwardItemsList(List<RewardItem.Builder> awardItemsList) {
		this.awardItemsList = awardItemsList;
	}

	public long getActivityTimeValue() {
		return activityTimeValue;
	}

	public void setActivityTimeValue(long activityTimeValue) {
		this.activityTimeValue = activityTimeValue;
	}

	public int getBuild() {
		return build;
	}

	public String getActivityTime() {
		return activityTime;
	}

	public String getAwardItems() {
		return awardItems;
	}

	public int getAndroidPayId() {
		return androidPayId;
	}

	public int getIosPayId() {
		return iosPayId;
	}

	public String getSuperAwardItems() {
		return superAwardItems;
	}

	
	
	public long getEndDateTimeValue() {
		return endDateTimeValue;
	}

	
	public long getRestartDateTimeValue() {
		return restartDateTimeValue;
	}


	

	public int getBuildMin() {
		return buildMin;
	}

	@Override
	protected boolean assemble() {
		try {
			if(this.buildMin > this.build){
				return false;
			}
			awardItemsList = new ArrayList<>();
			List<RewardItem.Builder> superReward = RewardHelper.toRewardItemImmutableList(superAwardItems);
			if (!superReward.isEmpty()) {
				awardItemsList.addAll(superReward);
			}
			List<RewardItem.Builder> normalReward = RewardHelper.toRewardItemImmutableList(awardItems);
			if (!normalReward.isEmpty()) {
				awardItemsList.addAll(normalReward);
			}
			activityTimeValue = HawkTime.parseTime(activityTime);
			endDateTimeValue = HawkTime.parseTime(endDate);
			restartDateTimeValue = HawkTime.parseTime(restartDate);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

}
