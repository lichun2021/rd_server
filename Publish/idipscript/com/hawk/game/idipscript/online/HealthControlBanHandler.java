package com.hawk.game.idipscript.online;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.data.ZKMsgInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Player.ReportType;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 中控禁玩接口
 *
 * localhost:8080/script/idip/4337
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4337")
public class HealthControlBanHandler extends IdipScriptHandler {
	
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
		
		long beginTime = request.getJSONObject("body").getInteger("BeginTime");  // 单位秒
		long endTime = request.getJSONObject("body").getInteger("EndTime");  // 单位秒
		beginTime *= 1000L;
		endTime *= 1000L;
		if (endTime < HawkApp.getInstance().getCurrentTime() || beginTime >= endTime) {
			HawkLog.errPrintln("zk ban script param error, playerId: {}, startTime: {}, endTime: {}", player.getId(), beginTime, endTime);
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "time param error");
			return result;
		}
		
		RedisProxy.getInstance().removeHealthGameRemindInfo(player.getOpenId(), ReportType.BAN_GAME_VALUE);
		ZKMsgInfo msgInfo = new ZKMsgInfo(title, msg, traceId, "", beginTime, endTime);
		RedisProxy.getInstance().updateHealthGameInfo(player.getOpenId(), ReportType.DEFAULT_REMIND_VALUE, JSONObject.toJSONString(msgInfo));
		
		player.setZkBanMsgInfo(msgInfo);
		if (beginTime <= HawkApp.getInstance().getCurrentTime() && player.isActiveOnline()) {
			player.sendHealthGameRemind(ReportType.DEFAULT_REMIND_VALUE, endTime, title, msg, traceId, "");
		} 
		
		HawkLog.logPrintln("zk ban online player, playerId: {}, title: {}, msg: {}, traceId: {}, startTime: {}, endTime: {}", player.getId(), title, msg, traceId, beginTime, endTime);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}


