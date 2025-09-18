package com.hawk.game.script;

import java.util.Map;

import org.hawk.net.HawkNetworkManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.Status;

/**
 * 踢出指定玩家, 或者关闭全部玩家会话
 * 
 * localhost:8080/script/kickout?playerName[playerId]=
 * 
 * @author hawk
 */
public class KickoutPlayerHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			if (!params.containsKey("playerId") && !params.containsKey("playerName") && !params.containsKey("openid")) {
				HawkNetworkManager.getInstance().closeAllSession();
				return HawkScript.successResponse(null);
			}
			
			Player player = null;
			String openid = params.get("openid");
			if (!HawkOSOperator.isEmptyString(openid)) {
				Map<String, String> roleMap = RedisProxy.getInstance().getAccountRole(openid);
				for (String value : roleMap.values()) {
					JSONObject jsonObject = JSON.parseObject(value);
					String playerId = jsonObject.getString("playerId");
					player = GlobalData.getInstance().getActivePlayer(playerId);
					if (player != null) {
						break;
					}
				}
				
			} else {
				player = GlobalData.getInstance().scriptMakesurePlayer(params);
				if (player == null) {
					return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
				}
			}
			
			// 踢下线
			if (player != null) {
				String serverId = params.get("serverId");
				player.kickout(Status.SysError.LOGIN_ON_OTHER_SERVER_VALUE, true, serverId);
			}

			return HawkScript.successResponse(null);
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
}
