package com.hawk.activity.type.impl.logingift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;


@HawkConfigManager.KVResource(file = "activity/login_gifts/%s/login_gifts_cfg.xml", autoLoad=false, loadParams="313")
public class LoginGiftActivityKVCfg extends HawkConfigBase {

	/** 服务器开服延时开启活动时间 单位:s */
	private final int serverDelay;

	/**
	 * 直购ID
	 */
	private final String androidPayId;
	private final String iosPayId;

	/**
	 * 关联活动
	 */
	private final int relationActivityId;
	
	private final int advancedTime;
	/**
	 * 此活动上线的时间
	 */
	private final String productTime;
	
	private long productTimeValue;
	private final long resetTime;
	public LoginGiftActivityKVCfg() {
		serverDelay = 0;
		androidPayId = "";
		iosPayId = "";
		relationActivityId = 0;
		advancedTime = 86400;
		productTime = "2022-11-24 06:05:00";
		resetTime = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public int getRelationActivityId() {
		return relationActivityId;
	}
	
	public int getAdvancedTime() {
		return advancedTime;
	}
	
	public long getProductTimeValue() {
		return productTimeValue;
	}
	
	@Override
	protected boolean assemble() {
		productTimeValue = HawkTime.parseTime(productTime);
		return true;
	}

	public long getResetTime() {
		return resetTime * 1000l;
	}
	
}
