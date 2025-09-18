package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.util.GameUtil;

/**
 * 删除玩家
 * 
 * @author lating
 *
 */
public class PlayerDeleteHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
		}
		
		GameUtil.resetAccount(player);
		
		return HawkScript.successResponse("player delete success, playerId: " + player.getId() + ", playerName: " + player.getName());
	}
}