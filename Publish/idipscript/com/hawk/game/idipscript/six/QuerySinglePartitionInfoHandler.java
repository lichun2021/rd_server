package com.hawk.game.idipscript.six;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.data.ServerSettingData;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 单服务器（最高在线、注册上限、排队上限）设定查询
 *
 * localhost:8081/idip/4287
 * 
 * @param AreaId     大区：微信（1），手Q（2）
 * @param Partition  小区id
 * @param PlatId     平台:ios(0)，安卓（1）
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4287")
public class QuerySinglePartitionInfoHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);

		String serverId = GsConfig.getInstance().getServerId();
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(serverId);
		result.getBody().put("SvrName", IdipUtil.encode(serverInfo.getName()));
		result.getBody().put("AreaId", Integer.valueOf(serverId));
		
		ServerSettingData serverSetting = GlobalData.getInstance().getServerSettingData();
		result.getBody().put("UpperLimitOnlineNum", serverSetting.getMaxOnlineCount());
		result.getBody().put("UpperLimitRegisterNum", serverSetting.getMaxRegisterCount());
		result.getBody().put("UpperLimitQueueNum", serverSetting.getMaxWaitCount());
		
		return result;
	}
	
}


