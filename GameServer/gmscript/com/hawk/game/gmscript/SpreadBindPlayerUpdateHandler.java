package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SpreadBindPlayerAchieveFinishEvent;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 推广码被绑定成功 
 *
 * localhost:8080/script/spreadBindPlayerUpdate?playerId=7py-4uwfp-1&friendPlayerId=&friendOpenid=&friendCityLevel=&friendVipLevel&friendServerId=&friendAchieves=
 *
 * @param playerId
 * @param friendOpenid
 * @param serverId 
 * @author RickMei 
 */

public class SpreadBindPlayerUpdateHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {

			String playerId = params.get("playerId");
			String friendPlayerId = params.get("friendPlayerId");
			String friendOpenid = params.get("friendOpenid");
			String friendServerId = params.get("friendServerId");
			String friendCityLevel = params.get("friendCityLevel");
			String friendVipLevel = params.get("friendVipLevel");
			String friendAchieves = params.get("friendAchieves");
			if(null == friendAchieves){
				friendAchieves = "";
			}
			
			// 参数错误
			if (HawkOSOperator.isEmptyString(friendPlayerId) || HawkOSOperator.isEmptyString(friendOpenid)
					|| HawkOSOperator.isEmptyString(friendServerId) || HawkOSOperator.isEmptyString(friendCityLevel)
					|| HawkOSOperator.isEmptyString(friendVipLevel)) {
				HawkLog.errPrintln(
						"spreadBindPlayerUpdate script failed, param error, "
								+ "playerId: {}, friendPlayerId: {}, friendOpenid: {}, friendServerId: {}, friendCityLevel: {}, friendVipLevel: {} achieves:{}",
						playerId, friendPlayerId, friendOpenid, friendServerId, friendCityLevel, friendVipLevel,friendAchieves);
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			
			//玩家如果在副本在跨服，活动完成不了
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				HawkLog.errPrintln(
						"spreadBindPlayerUpdate script failed, targetPlayer not exist, playerId: {}, friendOpenid: {} achieves: {}",
						params.get("playerId"), params.get("friendOpenid"), friendAchieves);
				//这里直接回复结果失败
				//2019-10-17不知道为什么这里要直接返回.
				//ActivityManager.getInstance().getDataGeter().playerSpreadOperatorRet(friendServerId, "spreadBindPlayerUpdate", false, friendPlayerId, playerId, friendAchieves);
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			//不在副本里面,并且.
			if (HawkOSOperator.isEmptyString(player.getDungeonMap()) && !player.isCsPlayer()) {
				//完成隐藏成就 给老兵奖励累加次数
				SpreadBindPlayerAchieveFinishEvent event = SpreadBindPlayerAchieveFinishEvent.valueOf(player.getId(), friendOpenid, friendServerId,
					friendPlayerId, Integer.valueOf(friendCityLevel).intValue(), Integer.valueOf(friendVipLevel).intValue(), friendAchieves);
				ActivityManager.getInstance().postEvent(event);
				
				return HawkScript.successResponse("");
			}										
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}
}
