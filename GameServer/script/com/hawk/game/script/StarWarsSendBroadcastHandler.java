package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSON;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;

/**
 * starWarsSendBroadcast
 * @author jm
 *
 */
public class StarWarsSendBroadcastHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo script) {
		try {
			String chatMsg = params.get("chatMsg");			
			ChatParames chatParames = JSON.parseObject(chatMsg, ChatParames.class);
			ChatService.getInstance().addWorldBroadcastMsg(chatParames);
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "exception");
	}

}

 