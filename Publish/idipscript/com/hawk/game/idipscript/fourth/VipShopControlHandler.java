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
 * vip商城商品控制
 *
 * localhost:8080/script/idip/4263?GoodsId=&Switch=
 *
 * @param GoodsId    商品ID
 * @param Switch       开关
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4263")
public class VipShopControlHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		long goodsId = request.getJSONObject("body").getLongValue("GoodsId");
		int switchVal = request.getJSONObject("body").getIntValue("Switch");
		IdipUtil.systemSwitchControl(switchVal, ControlerModule.VIP_SHOP, (int) goodsId);
		
		// 添加铭感日志
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
