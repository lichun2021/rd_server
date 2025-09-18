package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/war_college_instance.xml")
public class WarCollegeInstanceCfg extends HawkConfigBase {
	@Id
	private final int id;
	/**
	 * 难度
	 */
	private final int difficulty;
	/**
	 * 战斗配置id-->lmjy_battle.xml.id
	 */
	private final int battleId;
	/**
	 * 大本等级
	 */
	private final int cityLevel;
	/**
	 * 是否需要前置副本通关
	 */
	private final int needOpenInstanceId;
	/**
	 * 最大成员数目
	 */
	private final int maxMemberNum;
	
	/**
	 * 奖励
	 */
	private final String reward;
	
	// 每日联合军演副本可打最大次数
	private final int dailyTimesTotal;

	// 每日联合军演各难度可领奖次数
	private final int dailyRewardTimes;
	//队伍人数满这个数才可以开房间
	private final int openNum;
	//队伍创建后超过这个时间没有进房间就自动解散
	private final int cutTime;
	
	private final int moppingupDifficult;
	
	private final String firstReward;
	private final String teacherReward;
	
	
	
	public WarCollegeInstanceCfg() {
		this.id = 0;
		this.difficulty = 0;
		this.battleId = 0;
		this.cityLevel = 0; 
		this.maxMemberNum = 0;
		this.needOpenInstanceId = 0;
		this.reward="";
		this.dailyTimesTotal = 9999;
		this. dailyRewardTimes = 9999;
		this.openNum = 1;
		this.cutTime = 60;
		this.moppingupDifficult = 0;
		this.firstReward = "";
		this.teacherReward = "";
		
	}
	


	/**队伍人数满这个数才可以开房间*/
	public int getOpenNum() {
		return openNum;
	}
	/**队伍创建后超过这个时间没有进房间就自动解散*/
	public int getCutTime() {
		return cutTime;
	}

	public int getDailyTimesTotal() {
		return dailyTimesTotal;
	}

	public int getDailyRewardTimes() {
		return dailyRewardTimes;
	}

	public String getReward() {
		return reward;
	}

	public int getId() {
		return id;
	}
	public int getDifficulty() {
		return difficulty;
	}
	
	public int getBattleId() {
		return battleId;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public int getMaxMemberNum() {
		return maxMemberNum;
	}

	public int getNeedOpenInstanceId() {
		return needOpenInstanceId;
	}

	public int getMoppingupDifficult() {
		return moppingupDifficult;
	} 
	
	
	
	public String getFirstReward() {
		return firstReward;
	}
	
	public String getTeacherReward() {
		return teacherReward;
	}
	
	
	
}
