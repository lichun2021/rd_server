package com.hawk.game.idipscript.control;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.util.GsConst.GlobalControlType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 禁止全服玩家修改名字 -- 10282109
 *
 * localhost:8081/idip/4361
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4361")
public class ChangeNameControlHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int switchVal = request.getJSONObject("body").getIntValue("Switch");
		// 1是禁止，0是解除禁止
		if (switchVal == 0) {
			GlobalData.getInstance().cancelGlobalBan(GlobalControlType.CHANGE_NAME);
			LocalRedis.getInstance().cancelGlobalControlBan(GlobalControlType.CHANGE_NAME);
		} else {
			GlobalData.getInstance().addGlobalBanType(GlobalControlType.CHANGE_NAME, "");
			LocalRedis.getInstance().addGlobalControlBan(GlobalControlType.CHANGE_NAME, "");
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}


