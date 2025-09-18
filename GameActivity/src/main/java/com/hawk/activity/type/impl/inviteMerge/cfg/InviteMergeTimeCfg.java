package com.hawk.activity.type.impl.inviteMerge.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

import com.hawk.activity.config.IActivityTimeCfg;

/**
 * 合服邀请活动时间表
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/merge_invite/merge_invite_time.xml")
public class InviteMergeTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
	/** 活动期数*/
	@Id
	private final int termId;
	
	/** 预览时间*/
	private final String showTime; 
	
	/** 开启时间*/
	private final String startTime;
	
	private final String inviteEndTime1;
	private final String voteEntTime1;
	private final String showEndTime1;
	private final String inviteEndTime2;
	private final String voteEntTime2;
	private final String showEndTime2;
	
	/** 结束时间*/
	private final String endTime;
	
	/** 消失时间*/
	private final String hiddenTime;
	
	private long showTimeValue;
	private long startTimeValue;
	
	private long inviteEndTime1Value;
	private long voteEntTime1Value;
	private long showEndTime1Value;
	private long inviteEndTime2Value;
	private long voteEntTime2Value;
	private long showEndTime2Value;
	
	private long endTimeValue;
	private long hiddenTimeValue;
	
	public InviteMergeTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
		inviteEndTime1 = "";
		voteEntTime1 = "";
		showEndTime1 = "";
		inviteEndTime2 = "";
		voteEntTime2 = "";
		showEndTime2 = "";
	}
	
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

	public long getInviteEndTime1Value() {
		return inviteEndTime1Value;
	}

	public long getVoteEntTime1Value() {
		return voteEntTime1Value;
	}

	public long getShowEndTime1Value() {
		return showEndTime1Value;
	}

	public long getInviteEndTime2Value() {
		return inviteEndTime2Value;
	}

	public long getVoteEntTime2Value() {
		return voteEntTime2Value;
	}

	public long getShowEndTime2Value() {
		return showEndTime2Value;
	}

	@Override
	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		startTimeValue = HawkTime.parseTime(startTime);
		if (showTimeValue >= startTimeValue) {
			HawkLog.errPrintln("invite merge time config error, termId: {}", termId);
			return false;
		}
		endTimeValue = HawkTime.parseTime(endTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		
		inviteEndTime1Value = HawkTime.parseTime(inviteEndTime1);
		voteEntTime1Value = HawkTime.parseTime(voteEntTime1);
		showEndTime1Value = HawkTime.parseTime(showEndTime1);
		inviteEndTime2Value = HawkTime.parseTime(inviteEndTime2);
		voteEntTime2Value = HawkTime.parseTime(voteEntTime2);
		showEndTime2Value = HawkTime.parseTime(showEndTime2);
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		return checkTimeCfgValid(this.getClass());
	}
}