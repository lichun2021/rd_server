package com.hawk.game.gmproxy;

import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.os.HawkException;
import org.hawk.tuple.HawkTuple2;

import com.hawk.common.ServerInfo;
import com.hawk.game.global.RedisProxy;

public class GmProxyHelper {
	
	public static String doHttpRequest(String url,int timeout){
		try {
			// 由游戏服务器进行处理
			ContentResponse httpResponse = HawkHttpUrlService.getInstance().doGet(url, timeout);
			if (httpResponse != null) {
				String content = httpResponse.getContentAsString();
				if (content != null) {
					return content;
				}
			}
			HawkLog.errPrintln("proxy doHttpRequest request failed, url: {}", url);
		} catch (Exception e) {
			// 打印异常信息
			HawkException.catchException(e);
			HawkLog.errPrintln("proxy doHttpRequest request timeout, url: {}", url);
		}
		return null;
	}
	
	public static HawkTuple2<Integer, String> proxyCall(String serverId, String script, Map<String, String> paramMap, int timeout) {
		StringJoiner sj = new StringJoiner("&");
		for (Entry<String, String> entry : paramMap.entrySet()) {
			sj.add(entry.getKey()+"="+entry.getValue());
		}
		
		return proxyCall(serverId, script, sj.toString(), timeout);
	}
	
	public static HawkTuple2<Integer, String> proxyCall(String serverId, String script, String formatArgs, int timeout) {
		// 获取服务器信息
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(String.valueOf(serverId));
		if (serverInfo == null) {
			return new HawkTuple2<Integer, String>(-1, null);
		} 

		return directHostCall(serverInfo.getWebHost(), script, formatArgs, timeout);
	}
	
	
	public static HawkTuple2<Integer, String> directHostCall(String webHost, String script, String formatArgs, int timeout) {
		try {
			if (!webHost.startsWith("http://") && !webHost.startsWith("HTTP://")) {
				webHost = "http://" + webHost;
			}
			if (!webHost.endsWith("/")) {
				webHost += "/";
			}
			
			// 构建脚本url
			String scriptUrl = webHost + "script";
			if (!script.startsWith("/")) {
				scriptUrl = scriptUrl + "/";
			}
			
			scriptUrl += script;
			scriptUrl += "?";
			scriptUrl += formatArgs;
			
			try {
				// 由游戏服务器进行处理
				ContentResponse httpResponse = HawkHttpUrlService.getInstance().doGet(scriptUrl, timeout);
				if (httpResponse != null) {
					String content = httpResponse.getContentAsString();
					if (content != null) {
						return new HawkTuple2<Integer, String>(0, content);
					}
				}
				
				HawkLog.errPrintln("proxy dispatch request failed, url: {}", scriptUrl);
			} catch (TimeoutException e) {
				// 打印异常信息
				HawkException.catchException(e);
				
				// 分发超时异常
				HawkLog.errPrintln("proxy dispatch request timeout, url: {}", scriptUrl);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return new HawkTuple2<Integer, String>(-1, null);
	}
}
