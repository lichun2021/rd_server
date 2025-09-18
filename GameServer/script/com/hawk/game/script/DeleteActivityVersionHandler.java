package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.RedisProxy;

/**
 * 查询活动版本号
 * localhost:8080/script/deleteActivityVersion&serverId=
 * @author golden
 *
 */
public class DeleteActivityVersionHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String key = "activity_version:" + params.get("serverId");
		RedisProxy.getInstance().getRedisSession().del(key);
		return "success";
	}
}
