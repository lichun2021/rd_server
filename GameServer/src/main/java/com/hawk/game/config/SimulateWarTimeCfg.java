package com.hawk.game.config;


import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 攻防模拟战
 * 
 * @author jm
 *
 */
@HawkConfigManager.XmlResource(file = "xml/simulate_war_time.xml")
public class SimulateWarTimeCfg extends HawkConfigBase {
	@Id
	private final int termId;
	/**
	 * 开始展示时间
	 */
	private final String showTime;
	/**
	 * 开始报名时间
	 */
	private final String signUpStartTime;
	/**
	 * 管理时间
	 */
	private final String manageStartTime;
	/**
	 * 服务器用于匹配的开始时间.
	 */
	private final String matchStartTime;
	/**
	 * 行军开始时间 概念便于理解.
	 */
	private final String marchStartTime;
	/**
	 * 服务器用于计算战斗的开始时间.
	 */
	private final String fightStartTime; 
	/**
	 * 战斗显示开始阶段
	 */
	private final String fightShowStartTime;
	/**
	 * 发奖时间.
	 */
	private final String rewardTime;
	/**
	 * 隐藏时间
	 */
	private final String hiddenTime;
	/** 开放服务器列表 */
	private final String limitServer;

	/** 关闭服务器列表 */
	private final String forbidServer;

	/**
	 * {@link #showTime}
	 */
	private long showTimeValue;
	/**
	 * {@link #signUpStartTime}
	 */
	private long signUpStartTimeValue;
	/**
	 * {@link #manageStartTime}
	 */
	private long manageStartTimeValue;
	/**
	 * {@link #marchStartTime}
	 */
	private long marchStartTimeValue;
	/**
	 * {@link #fightShowStartTime}
	 */
	private long fightShowStartTimeValue;
	/**
	 * {@link #rewardTime}
	 */
	private long rewardTimeValue;
	/**
	 * {@link #hiddenTime}
	 */
	private long hiddenTimeValue;
	/**
	 * 战斗计算开始时间.
	 */
	private long fightStartTimeValue;
	/**
	 * 匹配开始时间.
	 */
	private long matchStartTimeValue;
	
	/**
	 * {@link #limitServer}
	 */
	private List<String> limitServerList;
	/**
	 * {@link #forbidServer}
	 */
	private List<String> forbidServerList;

	public SimulateWarTimeCfg() {
		this.termId = 0;
		this.showTime = "";
		this.signUpStartTime = "";
		this.manageStartTime = "";
		this.marchStartTime = "";
		this.fightShowStartTime = "";
		this.hiddenTime = "";
		this.limitServer = "";
		this.forbidServer = "";
		this.rewardTime = "";
		this.fightStartTime = "";
		this.matchStartTime = "";
	}
	
	@Override
	public boolean assemble() {
		this.showTimeValue = HawkTime.parseTime(showTime);
		this.signUpStartTimeValue = HawkTime.parseTime(signUpStartTime);
		this.manageStartTimeValue = HawkTime.parseTime(manageStartTime);
		this.marchStartTimeValue = HawkTime.parseTime(marchStartTime);
		this.fightShowStartTimeValue = HawkTime.parseTime(fightShowStartTime);
		this.hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		this.rewardTimeValue = HawkTime.parseTime(this.rewardTime);
		this.fightStartTimeValue = HawkTime.parseTime(this.fightStartTime);
		this.matchStartTimeValue = HawkTime.parseTime(this.matchStartTime);
		
		List<String> localLimitServerList = new ArrayList<>();
		List<String> localForbidServerList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(limitServer)) {
			for (String serverId : limitServer.split(",")) {
				localLimitServerList.add(serverId);
			}
		}
		if (!HawkOSOperator.isEmptyString(forbidServer)) {
			for (String serverId : forbidServer.split(",")) {
				localForbidServerList.add(serverId);
			}
		}
		
		this.limitServerList = localLimitServerList;
		this.forbidServerList = localForbidServerList;
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		if (!(this.hiddenTimeValue > this.rewardTimeValue && this.rewardTimeValue > this.fightShowStartTimeValue
				&& this.fightShowStartTimeValue > this.fightStartTimeValue && this.fightStartTimeValue > this.marchStartTimeValue
				&& this.marchStartTimeValue > this.matchStartTimeValue && this.matchStartTimeValue > this.manageStartTimeValue
				&& this.manageStartTimeValue > this.signUpStartTimeValue && this.signUpStartTimeValue >= this.showTimeValue)) {
			throw new InvalidParameterException(String.format("simulate_war_time.xml id:%s 配置的时间先后顺序有问题.", this.termId));
		}
		
		return true;
	}
	public int getTermId() {
		return termId;
	}

	public long getShowTimeValue() {
		return showTimeValue;
	}

	public long getSignUpStartTimeValue() {
		return signUpStartTimeValue;
	}

	public long getManageStartTimeValue() {
		return manageStartTimeValue;
	}

	public long getMarchStartTimeValue() {
		return marchStartTimeValue;
	}

	public long getFightShowStartTimeValue() {
		return fightShowStartTimeValue;
	}

	public long getHiddenTimeValue() {
		return hiddenTimeValue;
	}

	public List<String> getLimitServerList() {
		return limitServerList;
	}

	public List<String> getForbidServerList() {
		return forbidServerList;
	}

	public long getRewardTimeValue() {
		return rewardTimeValue;
	}

	public long getFightStartTimeValue() {
		return fightStartTimeValue;
	}

	public long getMatchStartTimeValue() {
		return matchStartTimeValue;
	}
}
