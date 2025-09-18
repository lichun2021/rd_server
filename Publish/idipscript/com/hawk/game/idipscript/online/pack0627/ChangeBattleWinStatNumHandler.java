package com.hawk.game.idipscript.online.pack0627;

import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 修改玩家战斗胜利次数
 *
 * localhost:8080/script/idip/4391
 *
 * @param OpenId  用户openId
 * @param Num
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4391")
public class ChangeBattleWinStatNumHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int num = request.getJSONObject("body").getInteger("Num");
		StatisticsEntity entity =player.getData().getStatisticsEntity();		
		if (num < 0 && num + entity.getAtkWinCnt() < 0) {
			num = 0 - entity.getAtkWinCnt();
		}
		
		entity.addAtkWinCnt(num);
		entity.setWarWinCnt(entity.getAtkWinCnt() + entity.getDefWinCnt());
		
		HawkLog.logPrintln("idip change battleWinCount, playerId: {}, count: {}", player.getId(), num);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}


