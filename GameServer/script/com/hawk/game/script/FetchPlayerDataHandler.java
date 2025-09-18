package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.PlayerEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerSerializer;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.util.GameUtil;

/**
 * 获取玩家数据
 *
 * localhost:8080/script/fetchplayer?playerName[playerId]=
 *
 * playerId:   玩家Id
 * playerName: 玩家名字
 *
 * @author hawk
 */
public class FetchPlayerDataHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		JSONObject dataObject = null;
		try {
			String playerId = null;
			if (!params.containsKey("playerId")) {
				String playerName = params.get("playerName");
				playerId = GameUtil.getPlayerIdByName(playerName);
			} else {
				playerId = params.get("playerId");
			}

			if (HawkOSOperator.isEmptyString(playerId)) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, "player not found playerId: " + playerId);
			}
			
			Player player = GlobalData.getInstance().scriptMakesurePlayer(playerId);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, "player not found playerId: " + playerId);
			}
			//获取玩家基础数据
			dataObject = GameUtil.gmGetAccountInfo(player);
			if (dataObject != null) {
				return HawkScript.successResponse(dataObject.toJSONString());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, dataObject.toJSONString());
	}
}
