package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.starwars.StarWarsOfficerService;

/**
 * starWarsOperateOfficer
 * @author jm
 *
 */
public class StarWarsSendMailHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo script) {
		try {
			//新国王的ID。
			String newKingId = params.get("newKingPlayerId");
						
			//重新加载官职.
			StarWarsOfficerService.getInstance().loadOrReloadOfficer();
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "exception");
	}

}

