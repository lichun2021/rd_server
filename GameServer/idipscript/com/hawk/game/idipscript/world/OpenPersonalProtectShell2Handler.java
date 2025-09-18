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
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 开启个人保护罩（如果玩家已经有保护罩了，在新加保护罩和现有保护罩中选时间更长的那个，而不是在现有保护罩上延长时间） -- 10282140
 *
 * localhost:8081/idip/4423
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4423")
public class OpenPersonalProtectShell2Handler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int minutes = request.getJSONObject("body").getIntValue("EffectiveDate");
		// 生效期（0-720小时）（单位：分钟）
		if (minutes <= 0 || minutes > 720 * 60) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "EffectiveDate param invalid");
			result.getBody().put("EndTime", 0);
			return result;
		}
		
		long newShieldTime = HawkTime.getMillisecond() + minutes * HawkTime.MINUTE_MILLI_SECONDS;
		long oldShieldTime = player.getData().getCityShieldTime();
		if (newShieldTime > oldShieldTime) {
			StatusDataEntity addStatusBuff = player.getData().addStatusBuff(EffType.CITY_SHIELD_VALUE, newShieldTime);
			if (addStatusBuff != null) {
				WorldPlayerService.getInstance().updateWorldPointProtected(player.getId(), addStatusBuff.getEndTime());
				player.getPush().syncPlayerStatusInfo(false, addStatusBuff);
			}
		} else {
			newShieldTime = oldShieldTime;
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("EndTime", newShieldTime/1000);
		return result;
	}
	
}


