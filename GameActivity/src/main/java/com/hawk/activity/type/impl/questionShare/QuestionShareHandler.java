package com.hawk.activity.type.impl.questionShare;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.QuestionShareAnswerReq;
import com.hawk.game.protocol.HP;

public class QuestionShareHandler extends ActivityProtocolHandler {

	// 回答问题
	@ProtocolHandler(code = HP.code.QUESTION_SHARE_ANSWER_REQ_C_VALUE)
	public void onPlayerAnswer(HawkProtocol protocol, String playerId) {
		QuestionShareAnswerReq msg = protocol.parseProtocol(QuestionShareAnswerReq.getDefaultInstance());
		QuestionShareActivity activity = getActivity(ActivityType.QUESTION_SHARE);
		if (null == activity) {
			LoggerFactory.getLogger("Server").info("questionshare_log QuestionShare activity not open");
			return;
		}
		if (!activity.isOpening(playerId)) {
			LoggerFactory.getLogger("Server").info("questionshare_log QuestionShare activity not open for player:{}",
					playerId);
			return;
		}

		activity.answerQuestion(playerId, msg.getAnswerIndex());
		activity.syncActivityDataInfo(playerId);
	}

	// 回答问题
	@ProtocolHandler(code = HP.code.QUESTION_SHARE_DAILY_REWARD_C_VALUE)
	public void onPlayerDailyReward(HawkProtocol protocol, String playerId) {
		QuestionShareActivity activity = getActivity(ActivityType.QUESTION_SHARE);
		if (null == activity) {
			LoggerFactory.getLogger("Server").info("questionshare_log QuestionShare activity not open");
			return;
		}
		if (!activity.isOpening(playerId)) {
			LoggerFactory.getLogger("Server").info("questionshare_log QuestionShare activity not open for player:{}",
					playerId);
			return;
		}
		Result<?> result = activity.onProtocolDailyReward(playerId);
		if (result.isFail()) {
			sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
		}	
	}
}
