package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.util.HawkNumberConvert;

import com.hawk.game.GsConfig;

/**
 * 脚本测试
 * 
 * localhost:8080/script/welcome
 *
 * @author hawk
 * 
 */
public class WelcomeHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String msg = String.format("gameserver welcome for you: %s", GsConfig.getInstance().getServerId());
		if (params.containsKey("sid")) {
			msg = String.format("gameserver welcome for you: %s", HawkNumberConvert.convertInt(params.get("sid")));
		}
		return HawkScript.successResponse(msg);
	}
}
