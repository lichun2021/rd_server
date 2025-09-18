package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.QuestionnaireService;

/**
 * 推送全服问卷
 *
 * <item id="pushSurvey"	className="com.hawk.game.script.PushSurveyHandler"/>
 * localhost:8080/script/pushSurvey?surveyId=?&&openid=?
 *
 * surveyId: 问卷id
 * openid: 推送目标openid(若无此参数,则为全服推送)
 *
 * @author Jesse
 *
 */
public class SurveyPushHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		int surveyId = Integer.parseInt(params.get("surveyId"));
		int result = Status.SysError.SUCCESS_OK_VALUE;
		// 全服推送
		if (!params.containsKey("openid")) {
			result = QuestionnaireService.getInstance().pushGlobalQuestionnaire(surveyId);
			if (result != Status.SysError.SUCCESS_OK_VALUE) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE,
						"push survey error, surveyId: " + surveyId + ", result: "+result);
			}
		} else {
			//指定openid推送
			String openid = params.get("openid");
			result = QuestionnaireService.getInstance().pushQuestionnaireByOpenid(surveyId, openid);
			if (result != Status.SysError.SUCCESS_OK_VALUE) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE,
						"push survey error, surveyId: " + surveyId + ", openid: " + openid + ", result: " + result);
			}

		}
		return HawkScript.successResponse("push survey success, surveyId: " + surveyId);
	}
}