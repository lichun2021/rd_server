package com.hawk.activity.type.impl.bestprize;

public class BestPrizeRedisKey {
	/**
	 * 服务器组
	 */
	protected static final String SERVER_GROUP_KEY = "bestPrizeGroup";
	/**
	 * 奖池抽奖的锁
	 */
	protected static final String POOL_DRAW_LOCK_KEY = "bestPrizeDrawLock";
	/**
	 * 奖池信息
	 */
	protected static final String POOL_INFO_KEY = "bestPrizePoolInfo";
	/**
	 * 大奖池内的小奖池数量
	 */
	protected static final String SMALL_POOL_COUNT_KEY = "bestPrizePoolNum";
	/**
	 * 大奖池内的小奖池增加（每天）数量
	 */
	protected static final String SMALL_POOL_DAILY_KEY = "bestPrizePoolDailyAdd";
	/**
	 * 抽奖记录
	 */
	protected static final String POOL_DRAW_RECORD_KEY = "bestPrizeDrawRecord";
	/**
	 * 添加池子的锁
	 */
	protected static final String POOL_ADD_LOCK_KEY = "bestPrizeAddPoolLock";
	
	/**
	 * 抽奖人次
	 */
	protected static final String DRAW_TIMES_KEY = "bestPrizedDrawTimes";
	
	/**
	 * 补箱子的时间
	 */
	protected static final String ADD_POOL_TIME_KEY = "bestPrizedAddPoolTime";
	
	/**
	 * 负责分组的区服
	 */
	protected static final String MASTER_SERVER_KEY = "bestPrizeMasterServer";
}
