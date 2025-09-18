package com.hawk.game.guild.championship;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.guild.championship.GCConst.GCBattleStage;
import com.hawk.game.guild.championship.GCConst.GCBattleState;

public class GCWarState {
	@JSONField(serialize = false)
	public boolean hasInit;

	public GCBattleState state = GCBattleState.WAIT;

	public GCBattleStage stage = GCBattleStage.TO_8;
	
	/** 当前正在计算的小组角标*/
	public int calcIndex = 0;
	
	public int termId = 0;

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public GCBattleState getState() {
		return state;
	}

	public void setState(GCBattleState state) {
		this.state = state;
	}

	public GCBattleStage getStage() {
		return stage;
	}

	public void setStage(GCBattleStage stage) {
		this.stage = stage;
	}

	public int getCalcIndex() {
		return calcIndex;
	}

	public void setCalcIndex(int calcIndex) {
		this.calcIndex = calcIndex;
	}

	@JSONField(serialize = false)
	public boolean isHasInit() {
		return hasInit;
	}

	@JSONField(serialize = false)
	public void setHasInit(boolean hasInit) {
		this.hasInit = hasInit;
	}

}
