package com.hawk.game.battle;
/**
 * 战报分析信息
 * @author admin
 *
 */
public class BattleAnalysisInfo {
	/** 进攻方战斗分析 */
	private int atkAnalysis;

	/** 防御方战斗分析 */
	private int defAnalysis;

	/** 进攻方战斗提示 */
	private int atkPrompt;

	/** 防御方战斗提示 */
	private int defPrompt;

	/** 是否大胜或者惨败1-是，0-不是 */
	private int battleGreat;

	public int getAtkAnalysis() {
		return atkAnalysis;
	}

	public void setAtkAnalysis(int atkAnalysis) {
		this.atkAnalysis = atkAnalysis;
	}

	public int getDefAnalysis() {
		return defAnalysis;
	}

	public void setDefAnalysis(int defAnalysis) {
		this.defAnalysis = defAnalysis;
	}

	public int getAtkPrompt() {
		return atkPrompt;
	}

	public void setAtkPrompt(int atkPrompt) {
		this.atkPrompt = atkPrompt;
	}

	public int getDefPrompt() {
		return defPrompt;
	}

	public void setDefPrompt(int defPrompt) {
		this.defPrompt = defPrompt;
	}

	public int getBattleGreat() {
		return battleGreat;
	}

	public void setBattleGreat(int battleGreat) {
		this.battleGreat = battleGreat;
	}
}
