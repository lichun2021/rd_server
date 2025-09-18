package com.hawk.game.service.tiberium;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.service.tiberium.TiberiumConst.TLWActivityState;

/**
 * 泰伯利亚联赛活动状态
 * @author admin
 *
 */
public class TLWActivityData {
	/** 赛季*/
	public int season = 0;
	
	/** 阶段*/
	public int termId = 0;

	public TLWActivityState state = TLWActivityState.TLW_NOT_OPEN;
	
	public boolean prepareFinish;
	
	/** 是否推送入围邮件*/
	public boolean sendPickedMail;
	
	
	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public TLWActivityState getState() {
		return state;
	}

	public void setState(TLWActivityState state) {
		this.state = state;
	}

	public boolean isPrepareFinish() {
		return prepareFinish;
	}

	public void setPrepareFinish(boolean prepareFinish) {
		this.prepareFinish = prepareFinish;
	}
	
	public boolean isSendPickedMail() {
		return sendPickedMail;
	}

	public void setSendPickedMail(boolean sendPickedMail) {
		this.sendPickedMail = sendPickedMail;
	}

	@JSONField(serialize = false)
	public int getMark() {
		return TiberiumLeagueWarService.getInstance().combineSeasonTerm(season, termId);
	}

}
