package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.world.WorldMarchService;

/**
 * 返回玩家所有行军
 * @author golden
 *
 * curl 'localhost:8080/script/returnMarch?playerId='
 */
public class ReturnMarchHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			WorldMarchService.getInstance().mantualMoveCityProcessMarch(player);
			
			return HawkScript.successResponse("");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}
}
