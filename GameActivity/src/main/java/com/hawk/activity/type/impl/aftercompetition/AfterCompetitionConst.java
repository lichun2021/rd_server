package com.hawk.activity.type.impl.aftercompetition;

public class AfterCompetitionConst {

	public static final int CHANNEL_WX = 1;
	public static final int CHANNEL_QQ = 2;
	
	/**
	 * 泰伯星海赛季
	 */
	public static final int RACE_TYPE_1 = 1;
	/**
	 * 霸主星海赛季
	 */
	public static final int RACE_TYPE_2 = 2;
	/**
	 * 星海联赛（冠军盟盟主有资格发放全服大奖）
	 */
	public static final int RACE_TYPE_3 = 3;
	
	/**
	 * 记录统计全服致敬次数
	 */
	public static final String REDIS_KEY_HOMAGE = "activity371_homage:";
	/**
	 * 记录玩家买礼物赠送给谁
	 */
	public static final String REDIS_KEY_SEND_TO = "activity371_send_to:";
	/**
	 * 记录玩家接受礼物次数
	 */
	public static final String REDIS_KEY_GIFT_REC = "activity371_gift_rec:";
	/**
	 * 记录每个礼包购买次数
	 */
	public static final String REDIS_KEY_GIFT_PAY = "activity371_gift_pay:";
	/**
	 * 记录礼包解锁时间
	 */
	public static final String REDIS_KEY_GIFT_UNLOCK = "activity371_gift_unlock:";
	
	/**
	 * 全服大奖
	 */
	public static final String REDIS_KEY_GLOBAL_GIFT = "activity371_global_gift:";
	/**
	 * 注水的锁
	 */
	public static final String REDIS_KEY_ZHU_LOCK = "activity371_zhu";
	public static final String REDIS_KEY_ZHU_TIME = "activity371_zhutime:";
	
	/**
	 * 送礼、收礼记录
	 */
	public static final String REDIS_KEY_SEND_RECORD = "activity371_send_record:";
	public static final String REDIS_KEY_REC_RECORD = "activity371_rec_record:";
	
	public static final String REDIS_KEY_GUILD_INFO = "activity371_guild_info:";
}
