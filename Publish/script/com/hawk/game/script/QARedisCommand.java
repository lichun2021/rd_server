package com.hawk.game.script;

import java.util.Map;

import org.hawk.redis.HawkRedisSession;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;

public class QARedisCommand extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		boolean debug = GsConfig.getInstance().isDebug();
		if (!debug) {
			return HawkScript.failedResponse(-1, "not debug");
		}
		
		String oper = params.get("oper");
		String key = params.get("key");
		String field = params.get("field");
		
		HawkRedisSession session = RedisProxy.getInstance().getRedisSession();
		if (oper.equals("del")) {
			session.del(key);
		} else if(oper.equals("hdel")) {
			session.hDel(key, field);
		} else {
			return HawkScript.failedResponse(-1, "oper error");
		}
		
		DungeonRedisLog.log("QARedisCommand", "serverId:{}, oper:{}, key:{}, field:{}",
				GsConfig.getInstance().getServerId(), oper, key, field);
		
		return HawkScript.successResponse(null);
	}
}
