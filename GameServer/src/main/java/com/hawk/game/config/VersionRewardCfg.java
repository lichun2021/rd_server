package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.XmlResource(file = "xml/version_reward.xml")
public class VersionRewardCfg extends HawkConfigBase {
	@Id
	private final int id;// ="1"
	private final String version;
	private final String reward;
	private final String starttime;
	private final String endtime;
	
	private final String subTitile;
	
	private final String mainTitle;
	
	private final String content;
	
	private long startTimeValue;
	private long endTimeValue;

	private List<RewardItem.Builder> rewardList;
	
	public VersionRewardCfg () {
		id = 1;
		version = "";
		reward = "";
		starttime = "";
		endtime = "";
		startTimeValue = 0;
		endTimeValue = 0;
		subTitile = "";
		mainTitle = "";
		content = "";
	}

	@Override
	protected boolean assemble() {
		startTimeValue = HawkTime.parseTime(starttime);
		endTimeValue = HawkTime.parseTime(endtime);
		try {
			this.rewardList = RewardHelper.toRewardItemImmutableList(this.reward);
			
			
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1, new Object[0]);
			return false;
		}
	}
	
	public int getId() {
		return id;
	}


	public String getVersion() {
		return version;
	}


	public String getReward() {
		return reward;
	}


	public String getStarttime() {
		return starttime;
	}


	public String getEndtime() {
		return endtime;
	}

	public long getStartTimeValue() {
		return startTimeValue;
	}

	public long getEndTimeValue() {
		return endTimeValue;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public String getContent() {
		return content;
	}

	public String getMainTitle() {
		return mainTitle;
	}

	public String getSubTitile() {
		return subTitile;
	}
}
