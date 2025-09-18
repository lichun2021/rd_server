package com.hawk.game.idipscript.item;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询物品数量 -- 10282144
 *
 * localhost:8081/idip/4431
 *
 * @param OpenId     用户openId
 * @param ItemId     物品Id
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4431")
public class QueryItemHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int itemId = request.getJSONObject("body").getInteger("ItemId");
		int num = player.getData().getItemNumByItemId(itemId);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("Num", num);
		
		return result;
	}
}


