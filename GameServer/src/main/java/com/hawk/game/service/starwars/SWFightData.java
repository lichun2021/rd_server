package com.hawk.game.service.starwars;

import com.alibaba.fastjson.annotation.JSONField;
import com.hawk.game.service.starwars.StarWarsConst.SWFightState;

public class SWFightData {
	@JSONField(serialize = false)
	public boolean hasInit;

	public int termId = 0;

	/**
	 * 本赛区第一场是否已经结束
	 */
	public boolean firstWarFinish;

	/**
	 * 本赛区第一场是否完成发奖
	 */
	public boolean firstWarRewarded;

	/**
	 * 本赛区第二场是否已经结束
	 */
	public boolean secondWarFinish;

	/**
	 * 本赛区第二场是否完成发奖
	 */
	public boolean secondWarRewarded;

	/**
	 * 本赛区第三场是否已经结束
	 */
	public boolean thirdWarFinish;

	/**
	 * 本赛区第三场是否完成发奖
	 */
	public boolean thirdWarRewarded;

	/**
	 * 战场开启前通知
	 */
	public int noticeCnt;

	/**
	 * 上次通知轮训检测时间
	 */
	public long lastCheckTime;

	/**
	 * 本赛区第一场是否完成发奖
	 */
	public boolean firstRankRewarded;
	public boolean secondRankRewarded;
	public boolean thirdRankRewarded;

	public SWFightState state = SWFightState.NOT_OPEN;

	public SWFightData() {
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public SWFightState getState() {
		return state;
	}

	public void setState(SWFightState state) {
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

	public boolean isFirstWarFinish() {
		return firstWarFinish;
	}

	public void setFirstWarFinish(boolean firstWarFinish) {
		this.firstWarFinish = firstWarFinish;
	}

	public boolean isSecondWarFinish() {
		return secondWarFinish;
	}

	public void setSecondWarFinish(boolean secondWarFinish) {
		this.secondWarFinish = secondWarFinish;
	}

	public boolean isThirdWarFinish() {
		return thirdWarFinish;
	}

	public void setThirdWarFinish(boolean thirdWarFinish) {
		this.thirdWarFinish = thirdWarFinish;
	}

	public boolean isFirstWarRewarded() {
		return firstWarRewarded;
	}

	public void setFirstWarRewarded(boolean firstWarRewarded) {
		this.firstWarRewarded = firstWarRewarded;
	}

	public boolean isSecondWarRewarded() {
		return secondWarRewarded;
	}

	public void setSecondWarRewarded(boolean secondWarRewarded) {
		this.secondWarRewarded = secondWarRewarded;
	}

	public boolean isThirdWarRewarded() {
		return thirdWarRewarded;
	}

	public void setThirdWarRewarded(boolean thirdWarRewarded) {
		this.thirdWarRewarded = thirdWarRewarded;
	}

	public int getNoticeCnt() {
		return noticeCnt;
	}

	public void setNoticeCnt(int noticeCnt) {
		this.noticeCnt = noticeCnt;
	}

	public long getLastCheckTime() {
		return lastCheckTime;
	}

	public void setLastCheckTime(long lastCheckTime) {
		this.lastCheckTime = lastCheckTime;
	}

	public boolean isFirstRankRewarded() {
		return firstRankRewarded;
	}

	public void setFirstRankRewarded(boolean firstRankRewarded) {
		this.firstRankRewarded = firstRankRewarded;
	}

	public boolean isSecondRankRewarded() {
		return secondRankRewarded;
	}

	public void setSecondRankRewarded(boolean secondRankRewarded) {
		this.secondRankRewarded = secondRankRewarded;
	}

	public boolean isThirdRankRewarded() {
		return thirdRankRewarded;
	}

	public void setThirdRankRewarded(boolean thirdRankRewarded) {
		this.thirdRankRewarded = thirdRankRewarded;
	}

}
