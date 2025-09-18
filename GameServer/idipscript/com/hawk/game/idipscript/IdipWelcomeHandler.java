package com.hawk.game.idipscript;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 脚本测试
 * 
 * @author hawk
 * 
 */
@HawkScript.Declare(id = "idip/101")
public class IdipWelcomeHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		result.getBody().put("msg", "idip welcome for you: " + HawkTime.formatNowTime());
		return result;
	}
}