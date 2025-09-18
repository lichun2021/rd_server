package com.hawk.game.idipscript.online.pack0627;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询玩家治疗士兵数量
 *
 * localhost:8080/script/idip/4379
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4379")
public class QueryCureSoldierHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}

		long soldierCureCount = player.getData().getStatisticsEntity().getArmyCureCnt();
		result.getBody().put("TreatSoldierNum", soldierCureCount);
		return result;
	}
}


