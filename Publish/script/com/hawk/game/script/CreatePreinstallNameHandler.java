package com.hawk.game.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.RedisProxy;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 添加预设名字
 * @author golden
 * localhost:8080/script/addPreinstallName?areaId=
 */
public class CreatePreinstallNameHandler extends HawkScript {

	static final String PREINSTALL_OPENID_NAME = "preinstall_openid_name";

	static final String PREINSTALL_NAME_OPENID = "preinstall_name_openid";
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		
		if (!params.containsKey("areaId")) {
			return HawkScript.successResponse(null);
		}
		
		// 目标大区Id
		int targetAreaId = Integer.parseInt(params.get("areaId"));
		
		int count = 0;
		
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
				Pipeline pip = jedis.pipelined()) {
			
			// 预设名字列表
			List<String> preinstallNames = new ArrayList<>();
			
			HawkOSOperator.readTextFileLines("cfg/preinstallName.txt", preinstallNames);
			
			for (String preinstallName : preinstallNames) {
				
				String[] split = preinstallName.split(",");
				
				// openId
				String openid = split[0];
				// 大区Id
				int areaId = Integer.parseInt(split[1]);
				// 服务器Id
				int serverId = Integer.parseInt(split[2]);
				// 名字
				String name = split[3];
				
				if (areaId != targetAreaId) {
					continue;
				}
				
				String key1 = String.format("%s:%s", PREINSTALL_OPENID_NAME, serverId);
				String key2 = PREINSTALL_NAME_OPENID;
				pip.hset(key1, openid, name);
				pip.hset(key2, name, openid);
				
				count++;
				
				if (count >= 100) {
					pip.sync();
					count = 0;
				}
			}
			
			if (count > 0) {
				pip.sync();
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkScript.successResponse("ok");
	}
}
