package com.hawk.game.script;

import java.util.Map;
import java.util.Set;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.serialize.string.SerializeHelper;


/**
 * starWarsSendGift
 * @author jm
 *
 */
public class StarWarsSendGiftHandler extends HawkScript {

	@Override
	public String action(Map<String, String> param, HawkScriptHttpInfo script) {
		try {
			String playerIds = param.get("playerIds");
			Integer giftId = Integer.parseInt(param.get("giftId"));
			Integer part = Integer.parseInt(param.get("part"));
			Set<String> idSet = SerializeHelper.stringToSet(String.class, playerIds, SerializeHelper.ATTRIBUTE_SPLIT);
			StarWarsOfficerService.getInstance().onSendGift(idSet, part, giftId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "exception");
	}

}
