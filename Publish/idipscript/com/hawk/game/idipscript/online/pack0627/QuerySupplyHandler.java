package com.hawk.game.idipscript.online.pack0627;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.WishingWellEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询玩家补给次数
 *
 * localhost:8080/script/idip/4381
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4381")
public class QuerySupplyHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		WishingWellEntity wishingEntity = player.getData().getWishingEntity();
		int todayWishCount = wishingEntity.getTodayTotalWishCount();
		result.getBody().put("SupplyNum", todayWishCount);
		return result;
	}
}


