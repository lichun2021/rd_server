package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 获取战斗日志
 * http://localhost:8080/script/battlelog?playerName=l0001
 *
 * @author hawk
 */
public class FetchBattleLogHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String playerId = "LLL";
		if (HawkOSOperator.isEmptyString(playerId)) {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			playerId = player.getId();
		}
		
		String result = LocalRedis.getInstance().getPlayerBattleLog(playerId);
		if (result != null) {
			result = result + ("<br>\n 当日已掠夺资源:" +LocalRedis.getInstance().grabResWeightDayCount(playerId));
			return result;
		}
		
		return "battle log not found: " + HawkTime.getMillisecond();
	}
}