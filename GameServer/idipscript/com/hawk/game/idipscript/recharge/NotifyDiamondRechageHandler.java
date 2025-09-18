package com.hawk.game.idipscript.recharge;

import java.util.HashMap;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 通知游戏侧外部直充金条数 -- 10282190
 *
 * localhost:8081/idip/4529
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4529")
public class NotifyDiamondRechageHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result, false);
		if (player == null) {
			return result;
		}
		
		int diamond = request.getJSONObject("body").getIntValue("Diamond");
		if (diamond <= 0) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetErrMsg", "Diamond param error: " + diamond);
			return result;
		}
		String serialID = request.getJSONObject("body").getString("Serial");
		if (!RedisProxy.getInstance().saveIdipSerialID(serialID)) {
			HawkLog.errPrintln("NotifyDiamondRechage4529 request repeatd of serial: {}", serialID);
			result.getBody().put("Result", IdipConst.SysError.SERVER_BUSY);
			result.getBody().put("RetMsg", "request of Serial repeated");
			return result;
		}
		
		Map<String, Object> param = new HashMap<>();
		param.put("type", RechargeType.RECHARGE);
		param.put("diamonds",diamond);
		LogUtil.logActivityCommon(player, LogInfoType.external_purchase, param);
		LogUtil.logIdipSensitivity(player, request, 0, diamond);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}


