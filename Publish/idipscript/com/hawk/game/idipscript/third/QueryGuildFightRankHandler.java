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
 * 单区联盟战力排名榜单查询
 *
 * localhost:8080/script/idip/4219
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4219")
public class QueryGuildFightRankHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		Set<Tuple> rankSet = LocalRedis.getInstance().getRankList(RankType.ALLIANCE_FIGHT_KEY, IdipUtil.RANK_RECORD_COUNT);
		JSONArray array = new JSONArray();
		for (Tuple tuple : rankSet) {
			String guildId = tuple.getElement();
			long score = (long) tuple.getScore();
			GuildInfoObject object = GuildService.getInstance().getGuildInfoObject(guildId);
			if (object == null) {
				continue;
			}
			
			JSONObject dataJson = new JSONObject();
			dataJson.put("AllianceName", IdipUtil.encode(object.getName()));        // 联盟名称
			dataJson.put("ChampionName", IdipUtil.encode(object.getLeaderName()));  // 盟主名称
			dataJson.put("ChampionId", object.getLeaderId());      // 盟主ID
			dataJson.put("Fight", score);                          // 联盟总战力
			array.add(dataJson);
		}
		
		result.getBody().put("RankList1_count", array.size());
		result.getBody().put("RankList1", array);
		return result;
	}
	
}
