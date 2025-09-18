package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.protocol.Script.ScriptError;

/**
 * op预留加公告.
 * starWarsBless
 * @author jm
 *
 */
public class StarWarsBlessHandler extends HawkScript {

	@Override
	public String action(Map<String, String> param, HawkScriptHttpInfo script) {
		String op = param.get("op");
		try {				
			return  HawkScript.successResponse("load worldXStruct success ");
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "exception");
	}

}
