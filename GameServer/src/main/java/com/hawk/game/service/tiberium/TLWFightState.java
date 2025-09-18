package com.hawk.game.service.tiberium;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.service.tiberium.TiberiumConst.FightState;

public class TLWFightState {
	@JSONField(serialize = false)
	public boolean hasInit;
	
	public int termId = 0;
	
	public FightState state = FightState.NOT_OPEN;

	
	
	public TLWFightState() {
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}


	public FightState getState() {
		return state;
	}

	public void setState(FightState state) {
		this.state = state;
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
