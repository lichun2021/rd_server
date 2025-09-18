package com.hawk.activity.type.impl.hellfiretwo;
/**
 * 
 * @author jm
 *
 */
public class HellFireTwoConst {
	/**
	 * 未知
	 */
	public static final int STAGE_UNKNOW = 0;
	/**
	 * 生效
	 */
	public static final int STAGE_EFFECT = 1;
	/**
	 * 结算
	 */
	public static final int STAGE_ACCOUNT = 2;
	
	/**
	 * 建筑建造.
	 */
	public static final int SCORE_TYPE_BUILDING = 1;
	/**
	 * 科技研究
	 */
	public static final int SCORE_TYPE_TECH = 2;
	/**
	 * 击败尤里
	 */
	public static final int SCORE_TYPE_HIT_MONSTER = 3;
	/**
	 * 训练
	 */
	public static final int SCORE_TYPE_DRAIN = 4;
	/**
	 * 资源采集
	 */
	public static final int SCORE_TYPE_RESOURCE = 5;
	/**
	 * 击杀老版野怪
	 */
	public static final int SCORE_TYPE_HIT_OLD_MONSTER = 6;
	/**
	 * 分数的最大值
	 */
	public static final int MAX_SCORE = 900000000;
	/**
	 * 未达成
	 */
	public static final int STATUS_NOT_FINISH = 0;
	/**
	 * 完成目标
	 */
	public static final int STATUS_FINISH = 1;
	/*
	 * 已经领取奖励
	 */
	public static final int STATUS_RECEIVED = 2;
	/**没有排行榜*/
	public static final int RANK_TYPE_NONE = 0;
	/**按周期开排行榜*/
	public static final int RANK_TYPE_STAGE = 1;
	/**按活动排行*/
	public static final int RANK_TYPE_ACTIVITY = 2;
}
