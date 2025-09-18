package com.hawk.game.guild.championship;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.ChampionshipTimeCfg;
import com.hawk.game.protocol.GuildChampionship.GCState;

public class GCActivityData {
	public int termId = 0;

	public GCState state = GCState.GC_HIDDEN;
	
	public boolean prepareFinish;
	
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public boolean isPrepareFinish() {
		return prepareFinish;
	}

	public void setPrepareFinish(boolean prepareFinish) {
		this.prepareFinish = prepareFinish;
	}

	/**
	 * 获取时间配置
	 * @return
	 */
	@JSONField(serialize = false)
	public ChampionshipTimeCfg getTimeCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(ChampionshipTimeCfg.class, termId);
	}

	public GCState getState() {
		return state;
	}

	public void setState(GCState state) {
		this.state = state;
	}

}
