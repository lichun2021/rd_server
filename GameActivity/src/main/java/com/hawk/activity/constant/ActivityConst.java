package com.hawk.activity.constant;

public class ActivityConst {
	
	// 成就解析器包路径
	public static  final String ACHIEVE_PARSER_PACKAGE = "com.hawk.activity.type.impl.achieve.parser";
	public static  final String STRONGEST_TARGET_PACKAGE = "com.hawk.activity.type.impl.stronestleader.target.impl";
	public static final String STRONGEST_GUILD_PACKAGE = "com.hawk.activity.type.impl.strongestGuild.target.impl";
	
	/** 红警战力任务解析器*/
	public static  final String ORDER_PACKAGE = "com.hawk.activity.type.impl.order.task.impl";
	/** 英雄进化之路任务解析器 */
	public static  final String EVOLUTION_PACKAGE = "com.hawk.activity.type.impl.evolution.task.impl";
	/** 机甲舱体代币获取任务解析器*/
	public static  final String SPACE_POINT_PACKAGE = "com.hawk.activity.type.impl.spaceguard.task.impl";

	/** 基金团购积分自动增长时间控制类型1-根据开服时间控制*/
	public static final int GROUP_PURCHASE_SCORE_TIME_TYPE1 = 1;
	
	/** 基金团购积分自动增长时间控制类型2- 根据配置具体时间控制*/
	public static final int GROUP_PURCHASE_SCORE_TIME_TYPE2 = 2;
	
	/** 成就奖励配置放入redis的过期时间，默认一个月(s) **/
	public static final int ACHIEVE_CONFIG_EXPIRE_TIME = 30 * 24 * 3600;
	
	/** 排行更新周期 ms*/
	public static final long ACTIVITY_RANK_UPDATE_PERIOD = 60000l; 
	
	/** 万分比基数*/
	public static final int MYRIABIT_BASE = 10000;
	
	/** 周一时间基准*/
	public static final long BASE_MONDAY_TIME = 1554048000000l;
	
	/** 一周的毫秒数*/
	public static final long WEEK_MILLI_SECONDS = 86400000l * 7;
	
	/** 荣耀勋章id*/
	public static final int HONOR_ITEMID = 1300017;
	
	/** 活动相关msgId偏移*/
	public static final int MSGID_OFFSET = 10000;
	
	/** 活动事件超时*/
	public static final long EVENT_TIMEOUT = 100l; 
	
	/** 快速tick时间间隔*/
	public static final long QUICK_TICK_PEROID = 200L; 
	
	/**
	 * 一小时的毫秒数
	 */
	public static final long HOUR_MILL_SECOND = 60 * 60 * 1000L;
	
	/**
	 * 一天的毫秒数
	 */
	public static final long DAY_MILL_SECOND = 24 * 60 * 60 * 1000L;
}
