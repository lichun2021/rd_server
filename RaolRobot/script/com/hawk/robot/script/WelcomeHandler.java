package com.hawk.robot.script;

import java.util.Map;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

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
		String msg = String.format("robot welcome for you: %s", HawkTime.getMillisecond());
		return HawkScript.successResponse(msg);
	}
}
