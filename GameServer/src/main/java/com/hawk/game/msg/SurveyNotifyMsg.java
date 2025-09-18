package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.gamelib.GameConst.MsgId;

/**
 * 问卷调查回调消息
 * 
 * @author Jesse
 *
 */
public class SurveyNotifyMsg extends HawkMsg {
	/**
	 * 问卷id
	 */
	int surveyId;
	
	public int getSurveyId() {
		return surveyId;
	}

	public void setSurveyId(int surveyId) {
		this.surveyId = surveyId;
	}

	public SurveyNotifyMsg() {
		super(MsgId.CURE_QUEUE_FINISH);
	}

	/**
	 * 构造消息对象
	 * 
	 * @return
	 */
	public static SurveyNotifyMsg valueOf(int surveyId) {
		SurveyNotifyMsg msg = new SurveyNotifyMsg();
		msg.surveyId = surveyId;
		return msg;
	}
}
