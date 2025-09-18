package com.hawk.game.idipscript.first;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.GameUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 玩家账号信息查询
 *
 * localhost:8080/script/idip/4111?OpenId=
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4111")
public class SearchAccountDataHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		JSONObject dataJson = GameUtil.gmGetAccountInfo(player);
		JSONArray accountArray = new JSONArray();
		accountArray.add(dataJson);
		result.getBody().put("AccountList_count", accountArray.size());
		result.getBody().put("AccountList", accountArray);
		
		return result;
	}
}
