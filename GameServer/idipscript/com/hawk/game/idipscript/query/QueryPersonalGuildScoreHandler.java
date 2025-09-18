package com.hawk.game.idipscript.query;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询联盟个人积分 -- 10282040
 *
 * localhost:8080/script/idip/4225
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4225")
public class QueryPersonalGuildScoreHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		long guildScore = player.getGuildContribution();
		result.getBody().put("AllianceIntegral", guildScore);
		
		return result;
	}
	
}
