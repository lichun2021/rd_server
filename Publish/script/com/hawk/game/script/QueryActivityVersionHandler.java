package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.RedisProxy;

/**
 * 查询活动版本号
 * http://127.0.0.1:8080/script/queryActivityVersion?serverId=
 * @author golden
 *
 */
public class QueryActivityVersionHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String key = "activity_version:" + params.get("serverId");
		Map<String, String> versionMap = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		if (versionMap == null) {
			return "data null";
		}
		return versionMap.toString();
	}
}
