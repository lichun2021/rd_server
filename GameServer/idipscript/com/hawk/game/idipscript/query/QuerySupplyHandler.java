package com.hawk.game.idipscript.query;

import java.util.Map;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.WishingWellEntity;
import com.hawk.game.entity.item.WishingCountItem;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.util.TimeUtil;


/**
 * 查询玩家补给次数 -- 10282119
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
		int todayWishCount = 0;
		if (!TimeUtil.isNeedReset(0, HawkTime.getMillisecond(), wishingEntity.getLastWishTime())) {
			Map<Integer, WishingCountItem> map = wishingEntity.getTodayWishCountMap();
			for (WishingCountItem item : map.values()) {
				int count = item.getFreeCount() + item.getCostCount() + item.getExtraCount();
				todayWishCount += count;
			}
		}
		
		result.getBody().put("SupplyNum", todayWishCount);
		return result;
	}
}


