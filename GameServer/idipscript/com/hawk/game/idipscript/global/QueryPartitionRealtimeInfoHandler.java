package com.hawk.game.idipscript.global;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 单服实时注册人数及实时在线查询 -- 10282085
 *
 * localhost:8081/idip/4295
 * 
 * @param AreaId     大区：微信（1），手Q（2）
 * @param Partition  小区id
 * @param PlatId     平台:ios(0)，安卓（1）
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4295")
public class QueryPartitionRealtimeInfoHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);

		String serverId = GsConfig.getInstance().getServerId();
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(serverId);
		result.getBody().put("SvrName", IdipUtil.encode(serverInfo.getName()));
		result.getBody().put("AreaId", Integer.valueOf(serverId));
		
		result.getBody().put("RegisterNum", GlobalData.getInstance().getRegisterCount());
		result.getBody().put("OnlineNum", GlobalData.getInstance().getOnlineUserCount());
		
		return result;
	}
	
}


