package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

/**
 * 异步调用
 * 
 * @author hawk
 */
public class AsyncProtocolHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		return HawkScript.successResponse(null);
	}

	@Override
	public Object doSomething(int type, Object... args) {
		return null;
	}
}
