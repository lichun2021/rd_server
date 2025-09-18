package com.hawk.activity.type.impl.groupPurchase.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 跨服团购活动全局K-V配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/group_purchase/%s/group_purchase_activity_cfg.xml", autoLoad=false, loadParams="30")
public class GroupPurchaseActivityKVCfg extends HawkConfigBase {

	/** 积分自动增长时间间隔 */
	private final int addScoreTimeGap;

	/** 单次自动增长积分数量 */
	private final int addScore;

	/** 积分自动增长时间控制类型(0-不增长, 1-根据开服时间控制, 2-根据具体配置时间点控制) */
	private final int addScoreType;

	/** 积分自动增长开始时间(根据开服时间) */
	private final long addStartTime1;

	/** 积分自动增长开始时间(根据开服时间) */
	private final long addEndTime1;

	/** 积分自动增长开始时间 */
	private final String addStartTime2;

	/** 积分自动增长结束时间 */
	private final String addEndTime2;
	/**每日登陆礼包*/
	private final String dailyReward;

	/** 积分自动增长开始时间戳 */
	private long addStartTimeValue2;

	/** 积分自动增长结束时间戳 */
	private long addEndTimeValue2;



	public GroupPurchaseActivityKVCfg() {
		addScoreTimeGap = 0;
		addScore = 0;
		addScoreType = 0;
		addStartTime1 = 0;
		addEndTime1 = 0;
		addStartTime2 = "";
		addEndTime2 = "";
		dailyReward = "";
	}
	public long getAddScoreTimeGap() {
		return addScoreTimeGap * 1000l;
	}

	public int getAddScore() {
		return addScore;
	}

	public int getAddScoreType() {
		return addScoreType;
	}

	public long getAddStartTimeValue1() {
		return addStartTime1;
	}

	public long getAddEndTimeValue1() {
		return addEndTime1;
	}

	public long getAddStartTimeValue2() {
		return addStartTimeValue2;
	}

	public long getAddEndTimeValue2() {
		return addEndTimeValue2;
	}

	@Override
	protected boolean assemble() {
		addStartTimeValue2 = 0;
		addEndTimeValue2 = 0;
		if (!HawkOSOperator.isEmptyString(addStartTime2)) {
			addStartTimeValue2 = HawkTime.parseTime(addStartTime2);
		}
		if (!HawkOSOperator.isEmptyString(addEndTime2)) {
			addEndTimeValue2 = HawkTime.parseTime(addEndTime2);
		}
		if (addEndTime1 < addStartTime1) {
			HawkLog.errPrintln(" GroupPurchaseActivityKVCfg check valid failed, addStartTime1: {}, addEndTime1: {}", addStartTime1, addEndTime1);
			return false;
		}
		if (addEndTimeValue2 < addStartTimeValue2) {
			HawkLog.errPrintln(" GroupPurchaseActivityKVCfg check valid failed, addStartTime2: {}, addEndTime2: {}", addStartTime2, addEndTime2);
			return false;
		}
		return true;
	}

	public String getDailyReward() {
		return dailyReward;
	}
}