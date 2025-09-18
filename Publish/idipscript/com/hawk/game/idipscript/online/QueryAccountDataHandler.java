package com.hawk.game.idipscript.online;

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
 * 玩家账号信息查询(时间用时间戳)
 *
 * localhost:8080/script/idip/4347?OpenId=
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4347")
public class QueryAccountDataHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		JSONObject dataJson = GameUtil.gmGetAccountInfo(player);
		// 最近登录时间
		dataJson.put("LastLoginTime", String.valueOf(player.getLoginTime()/1000));
		// 最后登出时间
		dataJson.put("LastLogoutTime", player.getLogoutTime() > 0 ? String.valueOf(player.getLogoutTime()/1000) : "");
				
		JSONArray accountArray = new JSONArray();
		accountArray.add(dataJson);
		result.getBody().put("AccountList_count", accountArray.size());
		result.getBody().put("AccountList", accountArray);
		
		return result;
	}
}
