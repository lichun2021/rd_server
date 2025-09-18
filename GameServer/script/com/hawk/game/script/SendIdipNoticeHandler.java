package com.hawk.game.script;

import java.util.Map;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 
 * localhost:8080/script/sendIdipNotice?playerId=&msg=
 *
 * @author lating
 *
 */
public class SendIdipNoticeHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}

			player.sendIdipMsg(params.get("msg"));
			
			// 返回执行完成
			return HawkScript.successResponse(null);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 执行异常
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
