package com.hawk.game.crossactivity;

public class CrossMatchInfo {
	/**
	 * 刷战力的次数
	 */
	private int flushBattleNumber;
	/**
	 * 记录是哪一个termId
	 */
	private int termId;
	
	/**
	 * 记录是否已经生成了匹配列表
	 */
	private boolean generated;
	
	public boolean needFlush() {
		return flushBattleNumber < 1;
	}
	
	public void incrFlushBattleNumber() {
		this.flushBattleNumber ++;
	}
	
	public void reset() {
		this.termId  = 0; 
		this.flushBattleNumber = 0;
		this.generated = false;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public boolean isGenerated() {
		return generated;
	}

	public void setGenerated(boolean generated) {
		this.generated = generated;
	}	
	
}
