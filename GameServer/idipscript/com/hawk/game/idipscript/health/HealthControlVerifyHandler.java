package com.hawk.game.idipscript.health;

import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Player.ReportType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 中控强制 验证接口 -- 10282102
 *
 * localhost:8080/script/idip/4339
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4339")
public class HealthControlVerifyHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		String traceId = request.getJSONObject("body").getString("TraceId");
		String jsonStr = request.getJSONObject("body").getString("JsonStr");
		jsonStr = IdipUtil.urlCodecDecode(jsonStr);
		
		// 玩家在线直接提示玩家，不在线先将信息存储下来
		if (player.isActiveOnline()) {
			player.sendHealthGameRemind(ReportType.DEFAULT_REMIND_VALUE, 0, null, null, traceId, jsonStr);
			HawkLog.logPrintln("zk verify online player, playerId: {}, traceId: {}, jsonStr: {}", player.getId(), traceId, jsonStr);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}


