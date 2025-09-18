package com.hawk.gamelib.activity;

public abstract class ConfigChecker {
	static ConfigChecker defaultChecker;

	public static ConfigChecker getDefaultChecker() {
		return defaultChecker;
	}

	public static void setDefaultChecker(ConfigChecker defaultChecker) {
		ConfigChecker.defaultChecker = defaultChecker;
	}
	
	/**
	 * 检测奖励配置是否有效
	 * @param awardsStr
	 * @return
	 */
	public abstract boolean checkAwardsValid(String awardsStr);
	
	/****
	 * 检查直购礼包id是否合法
	 * @param giftId
	 * @return
	 */
	public abstract boolean checkPayGiftValid(String giftId, String channelType);
	
	/***
	 * 检测活动的awardId是否合法
	 * @param awardId
	 * @return
	 */
	public abstract boolean chectAwardIdValid(int awardId);
}
