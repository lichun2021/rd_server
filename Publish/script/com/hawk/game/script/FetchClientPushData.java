package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 获取客户端本地推送数据
 * 
 * 127.0.0.1:8080/script/fetchPushData?date="2018-01-05"
 * 
 */
public class FetchClientPushData extends HawkScript {
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			if (!params.containsKey("date")) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "has no params: date");
			}
			
			// 推送数据
			String pushData = RedisProxy.getInstance().getPushData(params.get("date"));
			return HawkScript.successResponse(pushData);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
}