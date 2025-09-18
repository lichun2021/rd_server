package com.hawk.game.idipscript.third;

import java.util.Set;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.service.GuildService;
import com.hawk.idip.IdipResult;
import redis.clients.jedis.Tuple;
import com.hawk.idip.IdipScriptHandler;

/**
 * 基地等级榜单查询
 *
 * localhost:8080/script/idip/4189
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4189")
public class QueryCityLevelRankHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		Set<Tuple> rankSet = LocalRedis.getInstance().getRankList(RankType.PLAYER_CASTLE_KEY, IdipUtil.RANK_RECORD_COUNT);
		JSONArray array = new JSONArray();
		for (Tuple tuple : rankSet) {
			JSONObject dataJson = new JSONObject();
			String playerId = tuple.getElement();
			PlayerData playerData = GlobalData.getInstance().getPlayerData(playerId, true);
			if (playerData == null) {
				continue;
			}
			dataJson.put("RoleId", playerId);        // 角色ID
			dataJson.put("RoleName", IdipUtil.encode(playerData.getPlayerEntity().getName()));  // 角色名
			dataJson.put("LeagueId", GuildService.getInstance().getPlayerGuildId(playerId));      // 所属联盟
			dataJson.put("Lv", playerData.getConstructionFactoryLevel());                // 基地等级
			array.add(dataJson);
		}
		
		result.getBody().put("RankList_count", array.size());
		result.getBody().put("RankList", array);
		return result;
	}
	
}
