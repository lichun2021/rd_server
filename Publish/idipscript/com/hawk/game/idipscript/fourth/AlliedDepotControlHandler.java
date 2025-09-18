package com.hawk.game.idipscript.fourth;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 盟军补给控制(许愿池、军需处)
 *
 * localhost:8080/script/idip/4269?Switch=
 *
 * @param Switch       开关
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4269")
public class AlliedDepotControlHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int switchVal = request.getJSONObject("body").getIntValue("Switch");
		IdipUtil.systemSwitchControl(switchVal, ControlerModule.ALLIED_DEPOT);
		
		// 添加铭感日志
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
