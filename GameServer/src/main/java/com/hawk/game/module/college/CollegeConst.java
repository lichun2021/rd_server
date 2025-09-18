package com.hawk.game.module.college;

/**
 * 军事学院相关常量
 */
public class CollegeConst {

	/**
	 * 兑换商店商品刷新类型
	 */
	public static class ShopRefreshType {
		public static final int REFRESH_DAILY  = 1; //每日刷新
		public static final int REFRESH_WEEKLY = 2; //每周刷新
		public static final int REFRESH_MONTH  = 3; //每月刷新
		public static final int REFRESH_NEVER  = 4; //不刷新
	}
	
	/**
	 * 直购商店商品刷新类型
	 */
	public static class GiftRefreshType {
		public static final int WEEKLY = 1; //每周刷新
		public static final int NEVER  = 2; //不刷新
	}
	
	/**
	 * 商店类型
	 */
	public static class ShopType {
		public static final int EXCHANGE_SHOP  = 0; //普通兑换商店
		public static final int GIFT_SHOP      = 1; //直购商店
	}
	
	/**
	 * 直购商店礼包类型
	 */
	public static class CollegeGiftType {
		public static final int FREE     = 0; //免费礼包
		public static final int RMB      = 1; //人民币直购礼包
		public static final int DIAMONDS = 2; //金条直购
	}
}
