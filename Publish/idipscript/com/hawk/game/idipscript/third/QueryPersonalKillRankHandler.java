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
import com.hawk.idip.IdipResult;
import redis.clients.jedis.Tuple;
import com.hawk.idip.IdipScriptHandler;

/**
 * 个人击杀榜单查询
 *
 * localhost:8080/script/idip/4203
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4203")
public class QueryPersonalKillRankHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Set<Tuple> rankSet = LocalRedis.getInstance().getRankList(RankType.PLAYER_KILL_ENEMY_RANK, IdipUtil.RANK_RECORD_COUNT);
		JSONArray array = new JSONArray();
		for (Tuple tuple : rankSet) {
			String playerId = tuple.getElement();
			long score = (long) tuple.getScore();
			PlayerData playerData = GlobalData.getInstance().getPlayerData(playerId, true);
			if (playerData == null) {
				continue;
			}
			JSONObject dataJson = new JSONObject();
			dataJson.put("RoleId", playerId);        // 角色ID
			dataJson.put("RoleName", IdipUtil.encode(playerData.getPlayerEntity().getName()));  // 角色名
			dataJson.put("KillNum", score);                // 杀敌数量
			array.add(dataJson);
		}
		
		result.getBody().put("RankList_count", array.size());
		result.getBody().put("RankList", array);
		return result;
	}
	
}
