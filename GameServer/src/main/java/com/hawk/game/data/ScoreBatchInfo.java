package com.hawk.game.data;

import com.hawk.game.util.GsConst.ScoreType;

/**
 * 成就积分上报信息
 * 
 * @author lating
 *
 */
public class ScoreBatchInfo {
	
	// 积分类型
	private ScoreType scoreType;

	// 成就值
	private Object value;

	// 与排行榜有关的数据bcover=0，其他bcover=1。游戏中心排行榜与游戏排行榜保持一致;
	private int bcover;
	
	// unix时间戳，单位s，表示哪个时间点数据过期，0时标识永不超时
	private String expires;
	
	public static ScoreBatchInfo valueOf(ScoreType scoreType, Object value, int bcover, String expires) {
		return new ScoreBatchInfo(scoreType, value, bcover, expires);
	}
	
	public static ScoreBatchInfo valueOf(ScoreType scoreType, Object value) {
		return new ScoreBatchInfo(scoreType, value, scoreType.bcoverVal(), scoreType.expiresVal());
	}
	
	public ScoreBatchInfo(ScoreType scoreType, Object value, int bcover, String expires) {
		this.scoreType = scoreType;
		this.value = value;
		this.bcover = bcover;
		this.expires = expires;
	}

	public ScoreType getScoreType() {
		return scoreType;
	}

	public Object getValue() {
		return value;
	}

	public int getBcover() {
		return bcover;
	}

	public String getExpires() {
		return expires;
	}
	
}
