package com.hawk.game.idipscript.control;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 充值开关控制 -- 10282057
 *
 * localhost:8080/script/idip/4255?GiftBagId=&Switch=
 *
 * @param GiftBagId    礼包ID
 * @param Switch       开关
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4255")
public class RechargeGiftControlHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		long giftId = request.getJSONObject("body").getLongValue("GiftBagId");
		int switchVal = request.getJSONObject("body").getIntValue("Switch");
		IdipUtil.systemSwitchControl(switchVal, ControlerModule.RECHARGE_PAY, (int) giftId);
		
		// 添加铭感日志
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
