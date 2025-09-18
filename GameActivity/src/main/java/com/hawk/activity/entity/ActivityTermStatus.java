package com.hawk.activity.entity;

import com.hawk.activity.type.ActivityState;

/***
 * 玩家活动配置
 * @author yang.rao
 */
public class ActivityTermStatus {
	
	//当前活动配置期数
	private int termId;
	
	//当前活动状态
	private ActivityState curState;

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public ActivityState getCurState() {
		return curState;
	}

	public void setCurState(ActivityState curState) {
		this.curState = curState;
	}
	
	public void clear(){
		termId = 0;
		curState = null;
	}
}
