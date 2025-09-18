package com.hawk.game.idipscript.msg;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 发消息接口 -- 10282801
 *
 * localhost:8080/script/idip/4131
 *
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4131")
public class SendMsgHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String msg = request.getJSONObject("body").getString("Msg");
		msg = IdipUtil.decode(msg);
		if (player.isActiveOnline() && !player.isBackground()) {
			player.sendIdipMsg(msg);
		} else {
			LocalRedis.getInstance().addIDIPMsg(player.getId(), msg);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
