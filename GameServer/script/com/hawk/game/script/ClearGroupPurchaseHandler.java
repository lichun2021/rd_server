package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.RedisProxy;

/**
 * 获取玩家数据
 *
 * localhost:8080/script/clearScore?termId=xx&&score=xx
 *
 * playerId: 玩家Id
 * playerName: 玩家名字
 *
 * @author Jesse
 */
public class ClearGroupPurchaseHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			int termId = Integer.parseInt(params.get("termId"));
			int score = Integer.parseInt(params.get("score"));
			String key = "activity:group_purchase_score" + termId;
			RedisProxy.getInstance().getRedisSession().setString(key, String.valueOf(score));	
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
}
