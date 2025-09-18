package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/*
 * 解锁英雄
 */
public class UnlockHeroMsg extends HawkMsg {
	/**
	 * 解锁的英雄ID
	 */
	private int heroId;
	/**
	 * 品质
	 */
	private int qualityColor;
	
	public UnlockHeroMsg(int heroId, int qualityColor) {
		this.heroId = heroId;
		this.qualityColor = qualityColor;
	}
	public int getHeroId() {
		return heroId;
	}

	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}
	public int getQualityColor() {
		return qualityColor;
	}
	public void setQualityColor(int qualityColor) {
		this.qualityColor = qualityColor;
	}
	
}
