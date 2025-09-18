package com.hawk.gamelib.rank;

import org.hawk.os.HawkTime;


/**
 * 排行榜时间序积分计算
 * @author PhilChen
 *
 */
public class RankScoreHelper {
	
	/**
	 * 特殊排行积分偏移量
	 */
	public static final long rankSpecialOffset = 1000000000;
	
	
	public static final long rankSpecialSeconds = 1501516800;
	/**
	 * 计算特殊排行榜积分
	 * 
	 * @param score
	 * @return
	 */
	public static long calcSpecialRankScore(long score) {
		long value = HawkTime.getSeconds() - rankSpecialSeconds;
		long rankScore = Long.valueOf(score + "" + (rankSpecialOffset - value));
		return rankScore;
	}

	/**
	 * 获取真实排行积分
	 * 
	 * @param score
	 * @return
	 */
	public static long getRealScore(long rankScore) {
		long scoreVal = rankScore / rankSpecialOffset;
		return scoreVal;
	}

}
