package com.hawk.game.idipscript.third;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 通过角色ID查询角色名
 *
 * localhost:8080/script/idip/4211
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4211")
public class QueryPlayerNameHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		result.getBody().put("RoleName", player.getNameEncoded());
		return result;
	}
	
}
