package com.hawk.game.module.lianmengyqzz.battleroom.extra;

import com.google.common.base.MoreObjects;
import com.google.common.collect.HashBiMap;

public class YQZZExtraParam {
	private String battleId = "";

	private boolean debug;
	/** 服务器所在地块 1-6*/
	private HashBiMap<String, YQZZNation> serverCamp = HashBiMap.create(6);

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("battleId", battleId)
				.add("serverCamp", serverCamp)
				.toString();
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

	public HashBiMap<String, YQZZNation> getServerCamp() {
		return serverCamp;
	}

	public void setServerCamp(HashBiMap<String, YQZZNation> serverCamp) {
		this.serverCamp = serverCamp;
	}

}
