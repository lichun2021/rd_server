package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.RedisProxy;

/**
 * 添加openid白名单
 * @author golden
 * localhost:8080/script/addPuidControl?openid=golden
 */
public class CreatePuidControlHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!params.containsKey("openid")) {
			return "openid null";
		}
		RedisProxy.getInstance().addPuidControl(params.get("openid"));
		return HawkScript.successResponse("ok");
	}

}
