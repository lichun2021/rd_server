package com.hawk.activity.type.impl.order.activityNewOrder.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;


@HawkConfigManager.KVResource(file = "activity/order_two/%s/order_two_cfg.xml", autoLoad=false, loadParams="211")
public class NewOrderActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间 单位:s*/
	private final int serverDelay;
	
	private final String startDate;
	
	private long startDateValue;
	
	/**
	 * 活动屏蔽开始时间
	 */
	private final String closeTimeBegin;
	
	/**
	 * 活动屏蔽结束时间
	 */
	private final String closeTimeEnd;
	
	/**
	 * 活动屏蔽开始时间
	 */
	private long closeTimeBeginValue;
	
	/**
	 * 活动屏蔽结束时间
	 */
	private long closeTimeEndValue;
	
	public NewOrderActivityKVCfg() {
		serverDelay = 0;
		startDate = "";
		closeTimeBegin = "";
		closeTimeEnd = "";
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	@Override
	protected boolean assemble() {
		startDateValue = HawkTime.parseTime(startDate);
		
		if (!HawkOSOperator.isEmptyString(closeTimeBegin)) {
			closeTimeBeginValue = HawkTime.parseTime(closeTimeBegin);
		}
		
		if (!HawkOSOperator.isEmptyString(closeTimeEnd)) {
			closeTimeEndValue = HawkTime.parseTime(closeTimeEnd);
		}
		return true;
	}

	public long getStartDateValue() {
		return startDateValue;
	}

	public void setStartDateValue(long startDateValue) {
		this.startDateValue = startDateValue;
	}
	public String getCloseTimeBegin() {
		return closeTimeBegin;
	}

	public String getCloseTimeEnd() {
		return closeTimeEnd;
	}

	public long getCloseTimeBeginValue() {
		return closeTimeBeginValue;
	}

	public long getCloseTimeEndValue() {
		return closeTimeEndValue;
	}

}
