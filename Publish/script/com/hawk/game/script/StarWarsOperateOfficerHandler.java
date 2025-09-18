/*package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.GlobalData;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.starwars.StarWarsOfficerService;

*//**
 * starWarsOperateOfficer
 * @author jm
 *
 *//*
public class StarWarsOperateOfficerHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo script) {
		try {
			String oldOfficerPlayerId = params.get("oldOfficerPlayerId");
			String oldOfficerServerId = params.get("oldOfficerServerId");
			String newOfficerServerId = params.get("newOfficerServerId");
			String newOfficerPlayerId = params.get("newOfficerPlayerId");
			int playerOldOfficerId = Integer.parseInt(params.get("playerOldOfficerId"));
			int officerId = Integer.parseInt(params.get("officerId"));
			int part = Integer.parseInt(params.get("part"));
			boolean needReload = false;
			if (!HawkOSOperator.isEmptyString(oldOfficerServerId) && HawkOSOperator.isEmptyString(oldOfficerPlayerId) && 
					GlobalData.getInstance().isLocalServer(oldOfficerServerId)) {
				StarWarsOfficerService.getInstance().broadcastUnsetOfficer(oldOfficerPlayerId, part, officerId, false);
				needReload = true;
			} 
			
			if (!HawkOSOperator.isEmptyString(newOfficerServerId) && GlobalData.getInstance().isLocalServer(newOfficerServerId)) {
				//说明之前没有官职.
				if (playerOldOfficerId <= 0) {
					//StarWarsOfficerService.getInstance().broadcastSetOfficer(newOfficerPlayerId, part, officerId);
					needReload = true;
				} else {
					//StarWarsOfficerService.getInstance().broadcastChangeOfficer(newOfficerPlayerId, part, playerOldOfficerId, officerId);
					needReload = true;
				}
			}
			
			if (needReload) {
				StarWarsOfficerService.getInstance().loadOrReloadOfficer(part);
			}
			
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "exception");
	}

}

*/