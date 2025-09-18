package com.hawk.game.script;

import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.RecalledFriendLoginEvent;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

public class RecalledFriendBackHandler extends HawkScript {

	@Override
	public String action(Map<String, String> param, HawkScriptHttpInfo info) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(param);
		String msg = "";
		if (player == null) {
			msg = "can not find player";
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, msg);
		} else {
			String openId = param.get("openId");
			int facLv = NumberUtils.toInt(param.get("facLv"));
			RecalledFriendLoginEvent event = new RecalledFriendLoginEvent(player.getId(), openId, facLv);
			ActivityManager.getInstance().postEvent(event);
			return HawkScript.successResponse("ok");
		}		
	}

}
