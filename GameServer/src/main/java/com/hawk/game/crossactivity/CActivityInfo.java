package com.hawk.game.crossactivity;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.CrossTimeCfg;
import com.hawk.game.protocol.CrossActivity.CrossActivityState;

public class CActivityInfo {
	public int termId = 0;

	public boolean rewarded = false;
	
	public boolean statistics = false;
	
	
	public CrossActivityState state = CrossActivityState.C_HIDDEN;

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
	public CrossTimeCfg getTimeCfg(){
		return HawkConfigManager.getInstance().getConfigByKey(CrossTimeCfg.class, termId);
	}

	public CrossActivityState getState() {
		return state;
	}

	public void setState(CrossActivityState state) {
		this.state = state;
	}

	public boolean isRewarded() {
		return rewarded;
	}

	public void setRewarded(boolean rewarded) {
		this.rewarded = rewarded;
	}
	
	public void setStatistics(boolean statistics) {
		this.statistics = statistics;
	}
	
	public boolean isStatistics() {
		return statistics;
	}
	
	/**
	 * 获取本期活动预览时间
	 * @return
	 */
	@JSONField(serialize = false)
	public long getShowTime() {
		CrossTimeCfg cfg = getTimeCfg();
		if(cfg == null){
			return Long.MAX_VALUE;
		}
		return cfg.getShowTimeValue();
	}
	
	/**
	 * 获取本期活动开启时间
	 * @return
	 */
	@JSONField(serialize = false)
	public long getOpenTime() {
		CrossTimeCfg cfg = getTimeCfg();
		if(cfg == null){
			return Long.MAX_VALUE;
		}
		return cfg.getStartTimeValue();
	}
	
	/**
	 * 获取本期活动结束预览时间
	 * @return
	 */
	@JSONField(serialize = false)
	public long getEndTime() {
		CrossTimeCfg cfg = getTimeCfg();
		if(cfg == null){
			return Long.MAX_VALUE;
		}
		return cfg.getEndTimeValue();
	}
	
	/**
	 * 获取本期活动发奖时间
	 * @return
	 */
	@JSONField(serialize = false)
	public long getAwardTime() {
		CrossTimeCfg cfg = getTimeCfg();
		if(cfg == null){
			return Long.MAX_VALUE;
		}
		return cfg.getAwardTimeValue();
	}
	
	/**
	 * 获取本期活动隐藏时间
	 * @return
	 */
	@JSONField(serialize = false)
	public long getHiddenTime() {
		CrossTimeCfg cfg = getTimeCfg();
		if(cfg == null){
			return Long.MAX_VALUE;
		}
		return cfg.getHiddenTimeValue();
	}

}
