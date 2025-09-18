package com.hawk.game.idipscript.query;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.util.LoginUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询账号激活状态 -- 10282002
 *
 * localhost:8080/script/idip/4097?OpenId=
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4097")
public class SearchAccountActivateHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String openid = request.getJSONObject("body").getString("OpenId");
		
		if (LoginUtil.checkPuidCtrl(openid)) {
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
			return result;
		} 
		
		result.getBody().put("Result", -1);
		result.getBody().put("RetMsg", "account not activated");
		return result;
	}
}
