package com.hawk.game.gmscript;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.ServerInfo;
import com.hawk.game.global.RedisProxy;

/**
 * 获取所在大区服务器列表
 * 
 * @author hawk
 */
public class ServerListHandler extends HawkScript {
	/**
	 * 返回json格式
	 */
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String platform = params.get("platform");
			String channel = params.get("channel");
			
			// 根据条件查询可见服务器列表
			List<ServerInfo> serverList = queryServerList(platform, channel);
			if (serverList == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "server list not exist");
			}
			
			// 添加到服务器列表
			JSONArray svrArray = new JSONArray();
			for (ServerInfo serverInfo : serverList) {
				// 客户端的区服信息对象
				JSONObject json = genServerInfoJson(serverInfo);

				// 添加进区服列表
				svrArray.add(json);
			}

			// 返回服务器信息
			String svrInfo = svrArray.toJSONString();
			
			// 返回区服和额外信息
			return HawkScript.successResponse(svrInfo, null);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}

	/**
	 * 生成服务器列表同步json信息
	 * 
	 * @param serverInfo
	 * @return
	 */
	private JSONObject genServerInfoJson(ServerInfo serverInfo) {
		JSONObject json = new JSONObject();
		json.put("i", serverInfo.getId());
		json.put("n", serverInfo.getName());
		json.put("h", serverInfo.getHost());
		json.put("s", String.format("%d_%d_%d", serverInfo.getNewStatus(), serverInfo.getSuggestStatus(), serverInfo.getStatusLabel()));
		return json;
	}
	
	/**
	 * 选取合适的区服列表
	 * 
	 * @param platform
	 * @param channel
	 * @param country
	 * @param version
	 * @return
	 */
	private List<ServerInfo> queryServerList(String platform, String channel) {
		List<ServerInfo> servers = RedisProxy.getInstance().getServerList();
		if (servers == null || servers.size() <= 0) {
			return null;
		}
		
		// 过滤平台条件(platform)
		Iterator<ServerInfo> it = servers.iterator();
		while (it.hasNext()) {
			ServerInfo serverInfo = it.next();
			if (!serverInfo.matchingPlatform(platform)) {
				it.remove();
			}
		}

		// 过滤渠道条件(channel)
		it = servers.iterator();
		while (it.hasNext()) {
			ServerInfo serverInfo = it.next();
			if (!serverInfo.matchingChannel(channel)) {
				it.remove();
			}
		}

		return servers;
	}
}
