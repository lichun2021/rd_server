package com.hawk.game.service.starwars;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.StarWarsTimeCfg;
import com.hawk.game.service.starwars.StarWarsConst.SWActivityState;

public class SWActivityData {
	private int termId = 0;

	private SWActivityState state = SWActivityState.NOT_OPEN;
	
	/**
	 * 第一场入围资格筛选是否准备完成
	 */
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
	public StarWarsTimeCfg getTimeCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(StarWarsTimeCfg.class, termId);
	}

	public SWActivityState getState() {
		return state;
	}

	public void setState(SWActivityState state) {
		this.state = state;
	}
}
