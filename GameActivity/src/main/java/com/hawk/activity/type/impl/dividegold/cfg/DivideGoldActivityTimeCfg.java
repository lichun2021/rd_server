package com.hawk.activity.type.impl.dividegold.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import com.hawk.activity.config.IActivityTimeCfg;

/**瓜分金币 时间
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/divide_gold/dividegold_activity_time.xml")
public class DivideGoldActivityTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
	@Id
	private final int termId;

	private final String showTime;

	private final String startTime;

	private final String endTime;

	private final String hiddenTime;
	//集字开始时间
	private final String chestStartTime;
	//集字结束时间
	private final String chestEndTime;
	//开奖开始时间
	private final String rewardTime;
	//开奖结束时间
	private final String rewardEndTime;
	//client 用
	private final String waitRewardTime;

	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	
	private long chestStartTimeValue;
	private long chestEndTimeValue;
	private long rewardTimeValue;
	private long rewardEndTimeValue;

	public DivideGoldActivityTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
		chestStartTime = "";
		chestEndTime = "";
		rewardTime = "";
		rewardEndTime = "";
		waitRewardTime = "";
	}

	@Override
	public int getTermId() {
		return termId;
	}

	public String getShowTime() {
		return showTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getHiddenTime() {
		return hiddenTime;
	}

	@Override
	public long getShowTimeValue() {
		return showTimeValue;
	}

	@Override
	public long getStartTimeValue() {
		return startTimeValue;
	}

	@Override
	public long getEndTimeValue() {
		return endTimeValue;
	}

	@Override
	public long getHiddenTimeValue() {
		return hiddenTimeValue;
	}

	@Override
	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		startTimeValue = HawkTime.parseTime(startTime);
		endTimeValue = HawkTime.parseTime(endTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		
		chestStartTimeValue = HawkTime.parseTime(chestStartTime);
		chestEndTimeValue = HawkTime.parseTime(chestEndTime);
		rewardTimeValue = HawkTime.parseTime(rewardTime);
		rewardEndTimeValue = HawkTime.parseTime(rewardEndTime);
		//waitRewardTimeValue = HawkTime.parseTime(waitRewardTime);
		return true;
	}

	@Override
	protected boolean checkValid() {
		return checkTimeCfgValid(this.getClass());
	}

	public long getChestStartTimeValue() {
		return chestStartTimeValue;
	}

	public void setChestStartTimeValue(long chestStartTimeValue) {
		this.chestStartTimeValue = chestStartTimeValue;
	}

	public long getChestEndTimeValue() {
		return chestEndTimeValue;
	}

	public void setChestEndTimeValue(long chestEndTimeValue) {
		this.chestEndTimeValue = chestEndTimeValue;
	}

	public long getRewardTimeValue() {
		return rewardTimeValue;
	}

	public void setRewardTimeValue(long rewardTimeValue) {
		this.rewardTimeValue = rewardTimeValue;
	}

	public long getRewardEndTimeValue() {
		return rewardEndTimeValue;
	}

	public void setRewardEndTimeValue(long rewardEndTimeValue) {
		this.rewardEndTimeValue = rewardEndTimeValue;
	}

	public String getWaitRewardTime() {
		
		return waitRewardTime;
	}

}
