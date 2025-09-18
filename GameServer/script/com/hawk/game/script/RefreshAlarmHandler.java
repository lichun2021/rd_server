package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.timer.HawkTimerManager;

import com.hawk.game.protocol.Script.ScriptError;

/**
 * 刷新Alarm时间
 * localhost:8080/script/refreshAlarm
 * 
 * @author Jesse
 *
 */
public class RefreshAlarmHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			HawkTimerManager.getInstance().refreshAlarm();
			return successResponse("刷新时间成功");

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}
