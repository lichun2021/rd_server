package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

@HawkConfigManager.KVResource(file = "xml/obelisk_const.xml")
public class ObeliskConstCfg extends HawkConfigBase {
	/**
	 * 方尖碑上线时间点.
	 */
	private final String onlinetime;
	private final String newTime;
	private final int newServerTermId;
	
	
	private long onlineTimeValue;
	private long newTimeValue;

	public ObeliskConstCfg() {
		instance = this;
		onlinetime = "";
		newTime = "";
		newServerTermId = 0;
	}
	
	private static ObeliskConstCfg instance;

	public static ObeliskConstCfg getInstance() {
		return instance;
	}

	@Override
	public boolean assemble() {
		this.onlineTimeValue = HawkTime.parseTime(onlinetime);
		if(!HawkOSOperator.isEmptyString(this.newTime)){
			this.newTimeValue = HawkTime.parseTime(newTime);
		}
		return true;
	}

	public long getOnlineTimeValue() {
		return onlineTimeValue;
	}

	public void setOnlineTimeValue(long onlineTimeValue) {
		this.onlineTimeValue = onlineTimeValue;
	}

	public String getOnlineTime() {
		return onlinetime;
	}

	public int getNewServerTermId() {
		return newServerTermId;
	}
	
	public long getNewTimeValue() {
		return newTimeValue;
	}
}
