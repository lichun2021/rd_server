package com.hawk.game.idipscript.reddot;

import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改界面红点（开启显示或关闭显示） -- 10282146
 *
 * localhost:8081/idip/4441
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4441")
public class UpdateRedDotHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int type = request.getJSONObject("body").getIntValue("Type");
		int light = request.getJSONObject("body").getIntValue("IsLight");
		HawkLog.logPrintln("idip/4441 update reddot, type: {}, light: {}", type, light);

		// 0关闭，1开启
		if (light == 0) {
			GlobalData.getInstance().delRedDotSwitch(type);
			RedisProxy.getInstance().delRedDotSwitch(type);
		} else {
			GlobalData.getInstance().addRedDotSwitch(type);
			RedisProxy.getInstance().addRedDotSwitch(type);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
