package com.hawk.game.idipscript.query;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询角色创建时间 -- 10282094
 *
 * localhost:8080/script/idip/4315
 *
 * @param OpenId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4315")
public class QueryUserRegisterHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		result.getBody().put("RegisterTime", player.getCreateTime() / 1000);
		
		return result;
	}
}
