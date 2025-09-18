package com.hawk.game.idipscript.roleexchange;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询服务器是否停服（心悦角色交易）请求 -- 10282186
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4521")
public class QueryServerStateHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		// 此接口在GMServer实现
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("Data", "");
		return result;
	}
}
