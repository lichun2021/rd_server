package com.hawk.game.idipscript.online;

import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.data.ZKMsgInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Player.ReportType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 中控提醒接口
 *
 * localhost:8080/script/idip/4335
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4335")
public class HealthControlRemindHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		String title = request.getJSONObject("body").getString("ZK_Title");
		title = IdipUtil.decode(title);
		String msg = request.getJSONObject("body").getString("ZK_Msg");
		msg = IdipUtil.decode(msg);
		String traceId = request.getJSONObject("body").getString("TraceId");
		// 玩家在线直接提示玩家，不在线先将信息存储下来
		if (player.isActiveOnline()) {
			player.sendHealthGameRemind(ReportType.DEFAULT_REMIND_VALUE, 0, title, msg, traceId, null);
			HawkLog.logPrintln("zk remind online player, playerId: {}, title: {}, msg: {}, traceId: {}", player.getId(), title, msg, traceId);
		} else {
			ZKMsgInfo msgInfo = new ZKMsgInfo(title, msg, traceId, "", 0, 0);
			RedisProxy.getInstance().updateHealthGameInfo(player.getOpenId(), ReportType.DEFAULT_REMIND_VALUE, JSONObject.toJSONString(msgInfo));
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}


