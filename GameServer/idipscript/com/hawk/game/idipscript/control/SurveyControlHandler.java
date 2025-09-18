package com.hawk.game.idipscript.control;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil.Switch;
import com.hawk.game.service.QuestionnaireService;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 问卷控制（推送/下架） -- 10282089
 *
 * localhost:8080/script/idip/4303
 *
 * @param QuestionaryId   问卷ID
 * @param State           1推送/0下架
 * 
 * @author jesse
 */
@HawkScript.Declare(id = "idip/4303")
public class SurveyControlHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int surveyId = request.getJSONObject("body").getIntValue("QuestionaryId");
		int switchVal = Switch.OFF;
		if (request.getJSONObject("body").containsKey("State")) {
			switchVal = request.getJSONObject("body").getIntValue("State");
		}
		
		int status = IdipConst.SysError.SUCCESS;
		switch (switchVal) {
		case Switch.ON:
			status = QuestionnaireService.getInstance().pushGlobalQuestionnaire(surveyId);
			break;
		case Switch.OFF:
			QuestionnaireService.getInstance().removeGlobalQuestionaire(surveyId);
			break;

		default:
			break;
		}
		
		// 添加敏感日志
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		if (status != IdipConst.SysError.SUCCESS) {
			status = IdipConst.SysError.API_EXCEPTION;
		}
		
		result.getBody().put("Result", status);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
