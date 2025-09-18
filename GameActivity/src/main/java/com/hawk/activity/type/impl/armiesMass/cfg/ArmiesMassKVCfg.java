package com.hawk.activity.type.impl.armiesMass.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 端午庆典
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/armies_mass/armies_mass_cfg.xml")
public class ArmiesMassKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	private final int openSculptureTimes;
	
	private final int shareGetTimes;
	
	private final int times;
	
	private final int firstTime;
	
	private final int secondTime;
	
	private final int thirdTime;
	
	
	public ArmiesMassKVCfg() {
		serverDelay = 0;
		openSculptureTimes = 0;
		times = 0;
		shareGetTimes = 0;
		firstTime = 0;
		secondTime = 0;
		thirdTime = 0;
	}

	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}


	public int getOpenSculptureTimes() {
		return openSculptureTimes;
	}


	public int getFirstTime() {
		return firstTime  * 1000;
	}


	public int getSecondTime() {
		return secondTime  * 1000;
	}


	public int getThirdTime() {
		return thirdTime  * 1000;
	}


	public int getShareGetTimes() {
		return shareGetTimes;
	}


	public int getTimes() {
		return times;
	}


	
	

	

	
	


	
}