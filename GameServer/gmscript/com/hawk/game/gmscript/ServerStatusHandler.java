package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;

/**
 * 服务器状态查询接口
 * 
 * localhost:8080/script/status
 * 
 * @author hawk
 */
public class ServerStatusHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		JSONObject status = GsApp.getInstance().getAppStatusInfo(false);
		return HawkScript.successResponse(status.toJSONString());
	}
}