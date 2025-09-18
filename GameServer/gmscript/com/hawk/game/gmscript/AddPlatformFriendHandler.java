package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.global.RedisProxy;

public class AddPlatformFriendHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo arg1) {
		String myOpenId = params.get("myOpenId");
		String targetOpenId = params.get("targetOpenId");
		String nickName = HawkUUIDGenerator.genUUID();
		RedisProxy.getInstance().addWin32Friend(myOpenId, targetOpenId, nickName);
		
		return HawkScript.successResponse("nickName:"+nickName);
	}

}
