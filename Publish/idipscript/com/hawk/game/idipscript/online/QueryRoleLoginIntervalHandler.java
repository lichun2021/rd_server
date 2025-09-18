package com.hawk.game.idipscript.online;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询玩家回流时长
 *
 * localhost:8080/script/idip/4363?openid
 *
 * @param openid
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4363")
public class QueryRoleLoginIntervalHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		long lastLoginTime = player.getEntity().getLastLoginTime();
		if (lastLoginTime == 0) {
			lastLoginTime = player.getLoginTime();
			HawkLog.logPrintln("queryRoleLoginIntervalDays, lastLoginTime is zero, playerId: {}", player.getId());
		}
		
		result.getBody().put("IntervalDay", HawkTime.getCrossDay(lastLoginTime, player.getLoginTime(), 0));
		return result;
	}
	
}
