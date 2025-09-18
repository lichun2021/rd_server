package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 系统基础配置
 *
 * @author hawk
 *
 */
@HawkConfigManager.KVResource(file = "xml/super_barrack_const.xml")
public class SuperWeaponConstCfg extends HawkConfigBase {
	/**
	 * 初始和平时间(s)
	 */
	protected final int initPeaceTime;
	/**
	 * 初始开始的星期数，只能配1-7
	 */
	protected final int initday;
	/**
	 * 初始开始的时间点，只能配0-23
	 */
	protected final int initTime;
	/**
	 * 初始开始的分钟
	 */
	protected final int initMinute;
	/**
	 * 占领武器到成为拥有这过度时间(s)
	 */
	protected final int occupationTime;
	/**
	 * 战争时期周期(s)
	 */
	protected final int warfareTime;
	/**
	 * tick周期(ms)
	 */
	protected final long tickPeriod;
	/**
	 * 开启前邮件时间
	 */
	protected final String mailBeforeTime;
	/**
	 * 结束前邮件时间
	 */
	protected final String mailAfterTime;
	/**
	 * 报名周期
	 */
	protected final int signUpTime;
	/**
	 * 超级武器活动预热时间
	 */
	protected final int prepareTime;
	/**
	 * 控制周期时间
	 */
	protected final int controlTime;
	/**
	 * 超级武器占地格半径
	 */
	protected final int radius;
	/**
	 * 是否关闭
	 */
	protected final boolean isClosed;
	/**
	 * 最大可报名数量
	 */
	protected final int maxSighUp;
	/**
	 * 一个赛季轮次
	 */
	protected final int seasonPeriodNum;
	
	protected int[] mailBeforeTimeArray;
	protected int[] mailAfterTimeArray;
	protected int[] signUpOverTimeArray;
	
	/**
	 * 全局静态对象
	 */
	private static SuperWeaponConstCfg instance = null;

	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static SuperWeaponConstCfg getInstance() {
		return instance;
	}

	public SuperWeaponConstCfg() {
		instance = this;
		initPeaceTime = 0;
		initday = 0;
		initTime = 0;
		occupationTime = 0;
		warfareTime = 0;
		tickPeriod = 0;
		mailBeforeTime = "";
		mailAfterTime = "";
		signUpTime = 0;
		prepareTime = 0;
		controlTime = 0;
		radius = 6;
		initMinute = 0;
		isClosed = false;
		maxSighUp = 2;
		seasonPeriodNum = 6;
	}

	@Override
	protected boolean assemble() {
		if(initday < 1 || initday > 7){
			return false;
		}
		
		if(initTime < 0 || initTime > 23){
			return false;
		}
		
		if (!HawkOSOperator.isEmptyString(mailBeforeTime)) {
			String[] arr = mailBeforeTime.split("_");
			mailBeforeTimeArray = new int[arr.length];
			for (int i = 0; i < arr.length; i++) {
				mailBeforeTimeArray[i] = Integer.valueOf(arr[i]);
			}
		}
		if (!HawkOSOperator.isEmptyString(mailAfterTime)) {
			String[] arr = mailAfterTime.split("_");
			mailAfterTimeArray = new int[arr.length];
			for (int i = 0; i < arr.length; i++) {
				mailAfterTimeArray[i] = Integer.valueOf(arr[i]);
			}
		}
		
		return true;
	}

	public long getTickPeriod() {
		return tickPeriod;
	}

	public int getOccupationTime() {
		return occupationTime;
	}

	public int getInitPeaceTime() {
		return initPeaceTime;
	}

	public int getWarfareTime() {
		return warfareTime;
	}

	public int getInitday() {
		return initday;
	}

	public int getInitTime() {
		return initTime;
	}

	public long getSignUpTime() {
		return signUpTime * 1000L;
	}

	public String getMailBeforeTime() {
		return mailBeforeTime;
	}

	public int[] getMailBeforeTimeArray() {
		return mailBeforeTimeArray;
	}

	public String getMailAfterTime() {
		return mailAfterTime;
	}

	public int[] getMailAfterTimeArray() {
		return mailAfterTimeArray;
	}

	public int getRadius() {
		return radius;
	}

	public int[] getSignUpOverTimeArray() {
		return signUpOverTimeArray;
	}

	public int getPrepareTime() {
		return prepareTime;
	}

	public int getControlTime() {
		return controlTime;
	}

	public int getInitMinute() {
		return initMinute;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public int getMaxSighUp() {
		return maxSighUp;
	}
	
	public int getSeasonPeriodNum() {
		return seasonPeriodNum;
	}
}
