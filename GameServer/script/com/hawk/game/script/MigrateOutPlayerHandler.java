
package com.hawk.game.script;

import java.util.Map;

import org.eclipse.jetty.client.api.ContentResponse;
import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.tuple.HawkTuple3;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.ServerInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.MigrateService;
import com.hawk.game.util.GameUtil;

public class MigrateOutPlayerHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		HawkLog.logPrintln("migrate out player param:{}", params);
		String playerId = params.get("playerId");
		String serverId = params.get("targetServerId");
		MigrateService migrateService = MigrateService.getInstance(); 
		
		JSONObject jsonObject = new JSONObject();
		HawkTuple3<Integer, Player, ServerInfo> tuple3 = migrateService.isCanMigrate(playerId, serverId);
		if (tuple3.first.intValue() != Status.SysError.SUCCESS_OK_VALUE) {
			jsonObject.put("code", tuple3.first);
			jsonObject.put("msg", "can not migrate");
		} else {
			ServerInfo serverInfo = tuple3.third;
			int rlt = Status.Error.MIGRATE_SYSTEM_ERROR_VALUE;
			try {
				rlt = migrateService.migrateOutPlayer(tuple3.second, serverId);
				if (rlt == Status.SysError.SUCCESS_OK_VALUE) {					
					String url = GameUtil.getImmgratePlayerURL(serverInfo, playerId, serverId);
					ContentResponse response = HawkHttpUrlService.getInstance().doGet(url, 3000);
					if (response == null) {
						HawkLog.errPrintln("call remote immigrate player error url:{}", url);
						
						jsonObject.put("code", Status.Error.MIGRATE_SYSTEM_ERROR_VALUE);
						jsonObject.put("msg", "call remote immigrate error retrurn null all empty");
					} else {
						String str = response.getContentAsString();
						HawkLog.logPrintln("call remote immigrate player receive str:{}", str);
						
						// 远程的和本机通用
						jsonObject = JSON.parseObject(str);
						if (jsonObject.getInteger("code") != Status.SysError.SUCCESS_OK_VALUE) {
							HawkLog.errPrintln("call remote server immigrate error json:{}", jsonObject);
						}
					}					
				}
			} catch (Exception e) {
				HawkException.catchException(e, "player migrate out playerId:" + playerId + ",serverId" + serverId);
				
				jsonObject.put("code", Status.Error.MIGRATE_SYSTEM_ERROR_VALUE);
				jsonObject.put("msg", "migrate exception");
			} finally {
				if (jsonObject.getInteger("code") == Status.SysError.SUCCESS_OK_VALUE) {
					migrateService.migrateOutFinish(tuple3.second);
				} else {
					// 保证在中间出错之后还能让当前角色可用
					migrateService.migrateOutError(tuple3.second, jsonObject.getIntValue("code"));
				}
			}
		}

		return jsonObject.toString();
	}

}
