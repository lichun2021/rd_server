package com.hawk.game.idipscript.third;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 君主战力查询
 *
 * localhost:8080/script/idip/4209
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4209")
public class QueryPlayerPowerHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		result.getBody().put("RoleName", player.getNameEncoded());
		result.getBody().put("Fight", player.getPower());
		
		return result;
	}
	
}
