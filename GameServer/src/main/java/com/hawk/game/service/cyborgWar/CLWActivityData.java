package com.hawk.game.service.cyborgWar;

import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.config.CyborgSeasonTimeCfg;
import com.hawk.game.service.cyborgWar.CWConst.CLWActivityState;

public class CLWActivityData {
	public int season = 0;

	public CLWActivityState state = CLWActivityState.NOT_OPEN;

	public boolean rankRewardFinish;

	public boolean divisionRewardFinish;

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	/**
	 * 获取时间配置
	 * @return
	 */
	@JSONField(serialize = false)
	public CyborgSeasonTimeCfg getTimeCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(CyborgSeasonTimeCfg.class, season);
	}

	public CLWActivityState getState() {
		return state;
	}

	public void setState(CLWActivityState state) {
		this.state = state;
	}

	public boolean isRankRewardFinish() {
		return rankRewardFinish;
	}

	public void setRankRewardFinish(boolean rankRewardFinish) {
		this.rankRewardFinish = rankRewardFinish;
	}

	public boolean isDivisionRewardFinish() {
		return divisionRewardFinish;
	}

	public void setDivisionRewardFinish(boolean divisionRewardFinish) {
		this.divisionRewardFinish = divisionRewardFinish;
	}

}
