package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SpreadBindCodeSuccEvent;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 推广码被绑定成功 
 *  A服玩家 AP 绑定 B服玩家BP
 *  AP 发送 给 BP绑定成功
 *  BP 处理 以下下逻辑 
 *
 * localhost:8080/script/spreadBindSuccNotify?playerId=7py-4uwfp-1&friendPlayerId=&friendOpenid=&friendCityLevel=&friendVipLevel&friendServerId=
 *
 * @param playerId
 * @param friendOpenid
 * @param serverId 
 * @author RickMei 
 */

public class SpreadBindSuccNotifyHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String playerId = params.get("playerId");
			String friendPlayerId = params.get("friendPlayerId");
			String friendOpenid = params.get("friendOpenid");
			String friendServerId = params.get("friendServerId");
			String friendCityLevel = params.get("friendCityLevel");
			String friendVipLevel = params.get("friendVipLevel");
			// 参数错误
			if (HawkOSOperator.isEmptyString(playerId) || HawkOSOperator.isEmptyString(friendPlayerId) 
					|| HawkOSOperator.isEmptyString(friendOpenid) || HawkOSOperator.isEmptyString(friendServerId) 
					|| HawkOSOperator.isEmptyString(friendCityLevel)|| HawkOSOperator.isEmptyString(friendVipLevel) ) {
				HawkLog.errPrintln(
						"spreadBindSuccNotify script failed, param error, "
								+ "playerId: {}, friendPlayerId: {}, friendOpenid: {}, friendServerId: {}, friendCityLevel: {}, friendVipLevel: {}",
						playerId, friendPlayerId, friendOpenid, friendServerId, friendCityLevel, friendVipLevel);
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				HawkLog.errPrintln(
						"spreadBindSuccNotify script failed, targetPlayer not exist, playerId: {}, friendOpenid: {} achieves: {}",
						params.get("playerId"), params.get("friendOpenid"), "");
				//这里直接回复结果失败
//				ActivityManager.getInstance().getDataGeter().playerSpreadOperatorRet(friendServerId, "spreadBindPlayerUpdate", false, friendPlayerId, playerId, "");
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}

			// 抛事件到活动
			ActivityManager.getInstance().postEvent(SpreadBindCodeSuccEvent.valueOf(player.getId(), friendOpenid,
					friendServerId, friendPlayerId, Integer.valueOf(friendCityLevel).intValue(), Integer.valueOf(friendVipLevel).intValue()));

			return HawkScript.successResponse("");
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}
}
