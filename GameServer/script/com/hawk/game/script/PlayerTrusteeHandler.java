package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;

public class PlayerTrusteeHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		return HawkScript.successResponse(null);
	}

	/**
	 * 执行托管策略
	 * 
	 */
	@Override
	public Object doSomething(int type, Object... args) {
		try {
			String playerId = (String) args[0];
			JSONObject trusteeArgs = (JSONObject) args[1];
			
			Player player = GlobalData.getInstance().scriptMakesurePlayer(playerId);
			if (player != null && trusteeArgs != null) {
				onTrusteeStrategy(player, trusteeArgs);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.doSomething(type, args);
	}
	
	/**
	 * 开启策略
	 * 
	 * @param player
	 * @param trusteeArgs
	 */
	private void onTrusteeStrategy(Player player, JSONObject trusteeArgs) {
		
	}
}
