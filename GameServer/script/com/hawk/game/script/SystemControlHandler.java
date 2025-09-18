package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.protocol.Script.ScriptError;

/**
 * 系统控制：关闭或开启协议、消息、系统模块
 * 
 * localhost:8080/script/syscontrol?type=1&key=1000&close=true&all=false
 * <pre>
 * 参数说明：
 * @param type 类型：1表示协议控制、2表示消息id控制
 * @param key  控制对象对应的标识：type为1时key表示协议Id（如1000为登录协议）， type为2时key表示消息id（如1为创建联盟）
 *           
 * @param all  true表示关闭整个系统服务（此时type和key不起作用，可以不填这两个参数），false或其他值不起作用，不是对整个系统服务做开关控制时，此参数可不填
 * @param close true表示关闭一个协议、消息（all为true表示关闭整个系统服务），false表示开启一个已关闭的协议、消息（all为true表示取消关闭整个系统）
 * </pre>
 *
 * @author lating
 */
public class SystemControlHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		boolean controlAll = false;
		if (params.containsKey("all")) {
			controlAll = params.get("all").equals("true");
		}
		
		boolean close = false;
		if (params.containsKey("close")) {
			close = params.get("close").equals("true");
		}
		
		// 对整个系统服务进行控制, 关闭整个系统服务
		if (controlAll && close) {
			SystemControler.getInstance().closeAllSystem();
		} else if (controlAll) {
			SystemControler.getInstance().openSystem();
		} else {
			if (!params.containsKey("type") || !params.containsKey("key")) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "type or key lack");
			}
			
			try {
				String[] strs = params.get("key").trim().split(",");
				Integer[] ids = new Integer[strs.length];
				for (int i = 0; i < strs.length; i++) {
					ids[i] = Integer.parseInt(strs[i].trim());
				}
				
				if (Integer.valueOf(params.get("type")) == SystemControler.PROTOCOL) {
					if (close) {
						SystemControler.getInstance().closeProtocol(ids);
					} else {
						SystemControler.getInstance().openProtocol(ids);
					}
				} else {
					if (close) {
						SystemControler.getInstance().closeMsg(ids);
					} else {
						SystemControler.getInstance().openMsg(ids);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, e.getMessage());
			}
		}
		
		return HawkScript.successResponse(null);
	}
}
