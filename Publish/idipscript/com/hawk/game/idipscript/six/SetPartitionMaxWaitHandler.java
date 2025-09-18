package com.hawk.game.idipscript.six;

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
 * 服务器排队人数上限设定
 *
 * localhost:8081/idip/4293
 * 
 * @param AreaId     大区：微信（1），手Q（2）
 * @param Partition  小区id
 * @param PlatId     平台:ios(0)，安卓（1）
 * @param UpperLimitQueueNum  排队人数上限
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4293")
public class SetPartitionMaxWaitHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);

		int maxWait = request.getJSONObject("body").getIntValue("UpperLimitQueueNum");
		ServerSettingData serverSetting = GlobalData.getInstance().getServerSettingData();
		if (maxWait >= 0 && maxWait != serverSetting.getMaxWaitCount()) {
			serverSetting.setMaxWaitCount(maxWait);
			RedisProxy.getInstance().updateServerControlData(serverSetting);
		}
		
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
	
}


