package com.hawk.game.script;

import java.util.Map;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 查询玩家信息
 * 
 * localhost:8080/script/queryPlayer?playerName=l0001
 * 
 * @author lating
 *
 */
public class QueryPlayerInfoHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}

			StringBuilder sb = new StringBuilder();
			sb.append("<br>").append("openid: ").append(player.getOpenId())
			  .append("<br>").append("roleid: ").append(player.getId())
			  .append("<br>").append("rolename: ").append(player.getName())
			  .append("<br>");

			return HawkScript.successResponse(sb.toString());

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
