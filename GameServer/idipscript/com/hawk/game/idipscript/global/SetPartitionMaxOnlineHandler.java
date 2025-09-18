package com.hawk.game.idipscript.global;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.data.ServerSettingData;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 服务器最高在线人数上限设定 -- 10282082
 *
 * localhost:8081/idip/4289
 * 
 * @param AreaId     大区：微信（1），手Q（2）
 * @param Partition  小区id
 * @param PlatId     平台:ios(0)，安卓（1）
 * @param UpperLimitOnlineNum  最高在线人数
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4289")
public class SetPartitionMaxOnlineHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);

		int maxOnline = request.getJSONObject("body").getIntValue("UpperLimitOnlineNum");
		ServerSettingData serverSetting = GlobalData.getInstance().getServerSettingData();
		if (maxOnline > 0 && maxOnline != serverSetting.getMaxOnlineCount()) {
			serverSetting.setMaxOnlineCount(maxOnline);
			RedisProxy.getInstance().updateServerControlData(serverSetting);
		}
		
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
	
}


