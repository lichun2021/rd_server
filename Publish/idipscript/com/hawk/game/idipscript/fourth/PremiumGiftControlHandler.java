package com.hawk.game.idipscript.fourth;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 超值礼包销售开关控制
 *
 * localhost:8080/script/idip/4259?GiftBagId=&Switch=
 *
 * @param GiftBagId    礼包ID
 * @param Switch       开关
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4259")
public class PremiumGiftControlHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		long giftId = request.getJSONObject("body").getLongValue("GiftBagId");
		int switchVal = request.getJSONObject("body").getIntValue("Switch");
		IdipUtil.systemSwitchControl(switchVal, ControlerModule.PREMIUM_GIFT, (int) giftId);
		
		// 添加铭感日志
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
