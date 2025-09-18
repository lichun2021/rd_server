package com.hawk.game.idipscript.online;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改角色名称、基地签名CD时间 -- 10282114
 *
 * localhost:8080/script/idip/4371
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4371")
public class ChangeNameCDTimeHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int type = request.getJSONObject("body").getIntValue("Type"); // 修改类型：1-角色名称，2-基地签名
		int cdSeconds = request.getJSONObject("body").getIntValue("CDTime");
		if (type == 1) {
			RedisProxy.getInstance().updateChangeContentCDTime(player.getId(), ChangeContentType.CHANGE_ROLE_NAME, cdSeconds);
		} else {
			RedisProxy.getInstance().updateChangeContentCDTime(player.getId(), ChangeContentType.CHANGE_SIGNATURE, cdSeconds);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}


