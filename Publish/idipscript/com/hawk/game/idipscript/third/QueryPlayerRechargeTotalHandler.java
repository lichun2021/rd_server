package com.hawk.game.idipscript.third;

import java.util.List;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.data.RechargeInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst.RechargeType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询当前角色累计充值额度
 *
 * localhost:8080/script/idip/4207
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4207")
public class QueryPlayerRechargeTotalHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int begin = request.getJSONObject("body").getIntValue("BeginTime");
		int end = request.getJSONObject("body").getIntValue("EndTime");
		List<RechargeInfo> rechargerInfos = RedisProxy.getInstance().getAllRechargeInfoByOpenid(player.getOpenId());
		int diamonds = 0;
		for (RechargeInfo rechargeInfo : rechargerInfos) {
			if (rechargeInfo.getType() == RechargeType.GIFT) {
				continue;
			}
			
			int rechargeTime = rechargeInfo.getTime();
			if (rechargeTime < begin || rechargeTime > end) {
				continue;
			}

			diamonds += rechargeInfo.getCount();
		}
		
		result.getBody().put("Diamond", diamonds);
		return result;
	}
	
}
