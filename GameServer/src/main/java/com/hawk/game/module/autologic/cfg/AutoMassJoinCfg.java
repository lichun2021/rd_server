package com.hawk.game.module.autologic.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "xml/auto_mass_join.xml")
public class AutoMassJoinCfg extends HawkConfigBase {
	//生效时间 秒
	private final int workTime;
	//距离限制
	private final int distanceLimit;
	//集结时间限制
	private final int massTimeLimit;
	//行军时间限制
	private final int marchTimeLimit;
	//随机英雄品质
	private final int heroQualityColor;
	//错误邮件间隔
	private final int  missMailTime;

	private final int qaLog;
	
	public AutoMassJoinCfg() {
		workTime = 0;
		distanceLimit = 0;
		massTimeLimit = 0;
		marchTimeLimit = 0;
		heroQualityColor = 0;
		missMailTime = 0;
		qaLog = 0;
		
	}

	@Override 
	protected boolean assemble() {
		return true;
	}

	public double getDistanceLimit() {
		return distanceLimit;
	}
	
	public int getMarchTimeLimit() {
		return marchTimeLimit;
	}
	
	public int getMassTimeLimit() {
		return massTimeLimit;
	}
	public int getWorkTime() {
		return workTime;
	}
	
	public int getHeroQualityColor() {
		return heroQualityColor;
	}
	
	
	public int getMissMailTime() {
		return missMailTime;
	}
	
	public boolean needQAlog(){
		return this.qaLog > 0;
	}
}
