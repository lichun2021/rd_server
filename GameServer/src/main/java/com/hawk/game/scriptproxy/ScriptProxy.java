package com.hawk.game.scriptproxy;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.hawk.cryption.HawkMd5;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptManager;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.GsConfig;
import com.hawk.game.gmproxy.GmProxyHelper;
import com.hawk.game.protocol.ScriptProxy.ScriptRequest;
import com.hawk.game.protocol.ScriptProxy.ScriptResponse;
import com.hawk.game.protocol.ScriptProxy.script;

public class ScriptProxy {
	public static boolean onProtocol(HawkSession session, HawkProtocol protocol) {
		ScriptRequest request = protocol.parseProtocol(ScriptRequest.getDefaultInstance());
		
		// 授权码计算
		String[] tokens = request.getToken().split("\\|");
		if (tokens == null || tokens.length != 2) {
			session.close();
			return false;
		}
		
		// 第一段授权码
		if (!"39ec785d60a1b23bfda9944b9138bbcf".equals(HawkMd5.makeMD5(tokens[0]))) {
			session.close();
			return false;
		}
		
		// 第二段授权码
		if (!tokens[1].equals(HawkMd5.makeMD5(request.getScript() + "/" + request.getQuery()))) {
			session.close();
			return false;
		}
		
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				ScriptResponse.Builder response = ScriptResponse.newBuilder();
				response.setCode(HttpServletResponse.SC_FORBIDDEN);
				
				try {
					// 获取对应脚本执行
					HawkScript script = HawkScriptManager.getInstance().getScript(request.getScript());
					if (script == null) {
						response.setCode(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
					
					// 分拆http的参数
					Map<String, String> paramsMap = new HashMap<String, String>();
					if (!HawkOSOperator.isEmptyString(request.getQuery())) {
						paramsMap = HawkOSOperator.parseHttpParam(request.getQuery());
					}
					
					// zonine监控查询
					if (!HawkOSOperator.isEmptyString(request.getTargetHost())) {
						if (request.getScript().equals("monitor")) {
							HawkTuple2<Integer, String> retInfo = GmProxyHelper.directHostCall(request.getTargetHost(), 
									request.getScript(), request.getQuery(), GsConfig.getInstance().getHttpUrlTimeout());
							
							if (retInfo.first == 0) {
								response.setCode(HttpServletResponse.SC_OK);
								response.setResponse(retInfo.second);
								
								HawkLog.logPrintln("script proxy success - 1, scriptId: {}, targetHost: {}, request: {}, response: {}", 
										request.getScript(), request.getTargetHost(), request.getQuery(), retInfo.second);
							}
						}
						return;
					}
					
					String user = paramsMap.get("user");
					if (HawkOSOperator.isEmptyString(user) || !paramsMap.get("user").equals(GsConfig.getInstance().getAdmin())) {
						if (!request.getScript().equals("monitor") && 
							!request.getScript().equals("serverlist") &&
							!request.getScript().equals("serverlist_web")) {
							response.setCode(HttpServletResponse.SC_BAD_REQUEST);
							HawkLog.errPrintln("script proxy unsupport id: {}", request.getScript());
							return;
						}						
					}
					
					// 执行脚本
					String result = script.action(paramsMap, null);
					response.setCode(HttpServletResponse.SC_OK);
					response.setResponse(result);
					
					HawkLog.logPrintln("script proxy success - 3, scriptId: {}, request: {}, response: {}", request.getScript(), request.getQuery(), result);
				} catch (Exception e) {
					HawkException.catchException(e);
				} finally {
					session.sendProtocol(HawkProtocol.valueOf(script.RESPONSE_VALUE, response));
				}
			}
		});
		
		thread.setDaemon(true);
		thread.setName("ScriptProxy");
		thread.start();
		return true;
	}
}
