package com.hawk.game.lianmengstarwars;

import com.google.common.base.MoreObjects;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;

public class SWExtraParam {
	private SWWarType warType;
	private String battleId = "";

	private boolean debug;

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("SWWarType", warType)
				.toString();
	}

	public SWWarType getWarType() {
		return warType;
	}

	public void setWarType(SWWarType warType) {
		this.warType = warType;
	}

	public String getBattleId() {
		return battleId;
	}

	public void setBattleId(String battleId) {
		this.battleId = battleId;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}
