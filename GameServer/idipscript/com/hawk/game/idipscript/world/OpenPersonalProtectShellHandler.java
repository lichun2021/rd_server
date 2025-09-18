package com.hawk.game.idipscript.world;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 开启个人保护罩 -- 10282133
 *
 * localhost:8081/idip/4409
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4409")
public class OpenPersonalProtectShellHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int minutes = request.getJSONObject("body").getIntValue("AddTime");
		long startTime = player.getData().getCityShieldTime();
		startTime = Math.max(startTime, HawkTime.getMillisecond());
		long endTime = startTime + minutes * HawkTime.MINUTE_MILLI_SECONDS;
		StatusDataEntity addStatusBuff = player.getData().addStatusBuff(EffType.CITY_SHIELD_VALUE, endTime);
		if (addStatusBuff != null) {
			WorldPlayerService.getInstance().updateWorldPointProtected(player.getId(), addStatusBuff.getEndTime());
			player.getPush().syncPlayerStatusInfo(false, addStatusBuff);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("EndTime", endTime/1000);
		return result;
	}
}


