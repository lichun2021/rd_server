package com.hawk.game.service.cyborgWar;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.CyborgWarTimeCfg;
import com.hawk.game.service.cyborgWar.CWConst.CWActivityState;

public class CWActivityData {
	public int termId = 0;

	public CWActivityState state = CWActivityState.NOT_OPEN;
	
	public boolean prepareFinish;
	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	/**
	 * 获取时间配置
	 * @return
	 */
	@JSONField(serialize = false)
	public CyborgWarTimeCfg getTimeCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(CyborgWarTimeCfg.class, termId);
	}

	public CWActivityState getState() {
		return state;
	}

	public void setState(CWActivityState state) {
		this.state = state;
	}

	public boolean isPrepareFinish() {
		return prepareFinish;
	}

	public void setPrepareFinish(boolean prepareFinish) {
		this.prepareFinish = prepareFinish;
	}

}
