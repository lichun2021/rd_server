package com.hawk.game.idipscript.online;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.idipscript.util.IdipUtil.Switch;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 系统控制
 *
 * localhost:8080/script/idip/4327
 * 
 * <pre>
 * 参数说明：
 * @param ProtocalNo 协议号
 * @param Switch  开关：1开启，0关闭
 * </pre>
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4327")
public class SystemControlHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		int protocolNo = request.getJSONObject("body").getIntValue("ProtocalNo");
		int switchVal = request.getJSONObject("body").getIntValue("Switch");
		
		try {
			if (switchVal == Switch.OFF) {
				SystemControler.getInstance().closeProtocol(protocolNo);
			} else {
				SystemControler.getInstance().openProtocol(protocolNo);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
