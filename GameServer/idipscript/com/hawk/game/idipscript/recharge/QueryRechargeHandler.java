package com.hawk.game.idipscript.recharge;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询玩家单服充值金额 -- 10282120
 *
 * localhost:8080/script/idip/4383
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4383")
public class QueryRechargeHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result, false);
		if (player == null) {
			return result;
		}
		
		long begin = request.getJSONObject("body").getInteger("BeginTime") * 1000L;
		long end = request.getJSONObject("body").getInteger("EndTime") * 1000L;
		// 这里不能取diamonds字段，月卡的diamonds没有记在此处。payMoney的单位是人民币：角，正好对应钻石数
		int rechargeTotal = player.getData().getPlayerRechargeEntities().parallelStream().filter(e -> e.getTime() >= begin && e.getTime() <= end).mapToInt(e -> e.getPayMoney()).sum();
		result.getBody().put("Recharge", rechargeTotal);
		
		return result;
	}
}


