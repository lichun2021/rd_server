package com.hawk.game.idipscript.third;

import java.util.Set;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.service.GuildService;
import com.hawk.idip.IdipResult;
import redis.clients.jedis.Tuple;
import com.hawk.idip.IdipScriptHandler;

/**
 * 单区联盟击杀排名榜单查询
 *
 * localhost:8080/script/idip/4229
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4229")
public class QueryGuildKillRankHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		Set<Tuple> rankSet = LocalRedis.getInstance().getRankList(RankType.ALLIANCE_KILL_ENEMY_KEY, IdipUtil.RANK_RECORD_COUNT);
		JSONArray array = new JSONArray();
		for (Tuple tuple : rankSet) {
			JSONObject dataJson = new JSONObject();
			String guildId = tuple.getElement();
			long score = (long) tuple.getScore();
			GuildInfoObject object = GuildService.getInstance().getGuildInfoObject(guildId);
			if (object == null) {
				continue;
			}
			dataJson.put("AllianceName", IdipUtil.encode(object.getName()));        // 联盟名称
			dataJson.put("ChampionName", IdipUtil.encode(object.getLeaderName()));  // 盟主名称
			dataJson.put("ChampionId", object.getLeaderId());      // 盟主ID
			dataJson.put("AllianceKillNum", score);                // 联盟总击杀
			array.add(dataJson);
		}
		
		result.getBody().put("RankList2_count", array.size());
		result.getBody().put("RankList2", array);
		return result;
	}
	
}
