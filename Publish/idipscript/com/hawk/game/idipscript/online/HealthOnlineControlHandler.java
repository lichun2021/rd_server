package com.hawk.game.idipscript.online;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 关闭全服防沉迷系统
 *
 * localhost:8080/script/idip/4365
 *
 * @param State 操作状态：1上线（开启），0下线（关闭）
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4365")
public class HealthOnlineControlHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int state = request.getJSONObject("body").getIntValue("State");
		if (state == 0) {  // 下线
			GlobalData.getInstance().setHealthGameEnable(false);
		} else {  // 上线
			GlobalData.getInstance().setHealthGameEnable(true);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
}
