package com.hawk.game.service.tiberium;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.TiberiumTimeCfg;
import com.hawk.game.service.tiberium.TiberiumConst.TWActivityState;

public class TWActivityData {
	public int termId = 0;

	public TWActivityState state = TWActivityState.NOT_OPEN;
	
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
	public TiberiumTimeCfg getTimeCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(TiberiumTimeCfg.class, termId);
	}

	public TWActivityState getState() {
		return state;
	}

	public void setState(TWActivityState state) {
		this.state = state;
	}

	public boolean isPrepareFinish() {
		return prepareFinish;
	}

	public void setPrepareFinish(boolean prepareFinish) {
		this.prepareFinish = prepareFinish;
	}

}
