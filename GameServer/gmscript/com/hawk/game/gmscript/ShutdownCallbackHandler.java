package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.protocol.Script.ScriptError;

/**
 * 配置重新加载
 *
 * @author hawk
 */
public class ShutdownCallbackHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			HawkLog.logPrintln("script shutdown callback invoke");
			return HawkScript.successResponse(null);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}