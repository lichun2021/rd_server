package com.hawk.game.idipscript.account;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.player.platchange.PlatChangeService;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 安卓iOS账号内角色互转（同区服）请求 -- 10282172
 *
 * @param AreaId     服务器大区ID：微信（1），手Q（2）
 * @param Partition  服务器区服ID
 * @param OpenId     
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4493")
public class PlatTransfer4493Handler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String openId = request.getJSONObject("body").getString("OpenId");
		String serverId = request.getJSONObject("body").getString("Partition");
		// 调用转平台接口
		try {
			PlatChangeService.getInstance().changePlatform(openId, serverId);
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
			LogUtil.logIdipSensitivity(null, request, 0, 0); // 添加敏感日志
		} catch (Exception e) {
			HawkException.catchException(e);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "server exception");
		}
		
		return result;
	}
	
}
