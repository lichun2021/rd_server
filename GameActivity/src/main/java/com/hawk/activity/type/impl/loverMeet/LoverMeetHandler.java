package com.hawk.activity.type.impl.loverMeet;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.PBLoverQuestionAnswerReq;

/**
 * 端午兑换
 * 
 * @author che
 *
 */
public class LoverMeetHandler extends ActivityProtocolHandler {
	
	
	@ProtocolHandler(code = HP.code2.LOVER_MEET_START_REQ_VALUE)
	public void questionStart(HawkProtocol hawkProtocol, String playerId){
		LoverMeetActivity activity = this.getActivity(ActivityType.LOVER_MEET_ACTIVITY);
		if(activity == null){
			return;
		}
		activity.questionStart(playerId);
	}
	
	
	@ProtocolHandler(code = HP.code2.LOVER_MEET_ANSWER_REQ_VALUE)
	public void questionAnswer(HawkProtocol hawkProtocol, String playerId){
		LoverMeetActivity activity = this.getActivity(ActivityType.LOVER_MEET_ACTIVITY);
		if(activity == null){
			return;
		}
		PBLoverQuestionAnswerReq req = hawkProtocol.parseProtocol(PBLoverQuestionAnswerReq.getDefaultInstance());
		activity.questionAnswer(playerId, req.getQuestionId(), req.getAnswer());
	}
	
	
	
}