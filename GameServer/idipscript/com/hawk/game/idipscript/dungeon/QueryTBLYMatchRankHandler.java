package com.hawk.game.idipscript.dungeon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.TiberiumZoneCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.service.tiberium.TLWActivityData;
import com.hawk.game.service.tiberium.TLWGuildJoinInfo;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.game.util.GameUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询泰伯利亚联赛赛事排行榜请求 -- 10282131
 *
 * localhost:8080/script/idip/4405
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4405")
public class QueryTBLYMatchRankHandler extends IdipScriptHandler {
	
	static final int MAX_MATCHRANKLIST_NUM = 50;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		int pageNo = request.getJSONObject("body").getIntValue("PageNo");
		int pageSize = request.getJSONObject("body").getIntValue("PageSize");
		if (pageSize <= 0) {
			pageSize = MAX_MATCHRANKLIST_NUM;
		}
		
		int indexStart = pageNo > 1 ? (pageNo - 1) * pageSize : 0;
		int indexEnd = indexStart + pageSize;
		indexStart += 1;
		
		List<JSONObject> matchInfoList = queryTLWGuildInfo();
		int count = 0, totalCount = matchInfoList.size();
		JSONArray array = new JSONArray();
		Iterator<JSONObject> it = matchInfoList.iterator();
		while (it.hasNext()) {
			JSONObject matchInfo = it.next();
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("AllianceId", matchInfo.getString("AllianceId"));        // 联盟ID
			jsonObj.put("AllianceName", GameUtil.getStrEncoded(matchInfo.getString("AllianceName")));    // 联盟名称
			jsonObj.put("Partition", matchInfo.getIntValue("Partition"));        // 联盟区服
			jsonObj.put("AllianceFlag", matchInfo.getIntValue("AllianceFlag"));  // 联盟旗帜
			jsonObj.put("ChampionName", GameUtil.getStrEncoded(matchInfo.getString("ChampionName")));    // 盟主名称
			jsonObj.put("MatchId", matchInfo.getIntValue("MatchId"));            // 赛区ID 
			jsonObj.put("MatchRank", matchInfo.getIntValue("MatchRank"));        // 赛区内排名
			jsonObj.put("WinNum", matchInfo.getIntValue("WinNum"));              // 胜利场数 
			jsonObj.put("DefeatNum", matchInfo.getIntValue("DefeatNum"));        // 失败场数
			jsonObj.put("Score", matchInfo.getLongValue("Score"));                // 联盟累计积分
			jsonObj.put("MatchStage", matchInfo.getIntValue("MatchStage"));      // 赛事阶段（小组赛、决赛）
			jsonObj.put("AllianceMemberNum", matchInfo.getIntValue("AllianceMemberNum")); // 联盟人数
			jsonObj.put("Fight", matchInfo.getLongValue("Fight"));              // 联盟总战力
			jsonObj.put("ChampionId", matchInfo.getString("ChampionId"));      // 盟主角色ID
			jsonObj.put("Zone", matchInfo.getIntValue("Zone"));                // 赛区分组
			jsonObj.put("Group", matchInfo.getIntValue("Group"));              // 赛区组别
			array.add(jsonObj);
		}
		
		result.getBody().put("TotalPageNo", (int)Math.ceil(totalCount * 1.0d / pageSize));
		result.getBody().put("MatchRankList_count", array.size());
		result.getBody().put("MatchRankList", array);
		
		return result;
	}
	
	public List<JSONObject> queryTLWGuildInfo() {
		ConfigIterator<TiberiumZoneCfg> zoneCfgs = HawkConfigManager.getInstance().getConfigIterator(TiberiumZoneCfg.class);
		String areaId = GsConfig.getInstance().getAreaId();
		TLWActivityData activityInfo = TiberiumLeagueWarService.getInstance().getActivityInfo();
		int season = activityInfo.getSeason();
		Map<String, TLWGuildJoinInfo> infoMap = RedisProxy.getInstance().getAllTLWJoinGuild(season);
		List<JSONObject> infoList = new ArrayList<JSONObject>();
//		for (TiberiumZoneCfg zoneCfg : zoneCfgs) {
//			if (!zoneCfg.getAreaId().equals(areaId)) {
//				continue;
//			}
//			int zoneId = zoneCfg.getZone();
//			/***************************************/
//			List<TLWGuildJoinInfo> guildList = infoMap.values().stream().filter(t -> t.getZone() == zoneId).collect(Collectors.toList());
//			Collections.sort(guildList, new Comparator<TLWGuildJoinInfo>() {
//				@Override
//				public int compare(TLWGuildJoinInfo arg0, TLWGuildJoinInfo arg1) {
//					if (arg0.getInitPower() != arg1.getInitPower()) {
//						return arg0.getInitPower() > arg1.getInitPower() ? -1 : 1;
//					}
//					return arg0.getId().compareTo(arg1.getId());
//				}
//			});
//			List<String> guildIds = new ArrayList<>();
//			for (TLWGuildJoinInfo info : guildList) {
//				guildIds.add(info.getId());
//			}
//			Map<String, TLWGuildData> dataMap = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
//			int rank = 1;
//
//			for (String guildId : guildIds) {
//				TLWGuildJoinInfo joinInfo = infoMap.get(guildId);
//				TLWGuildData guildData = dataMap.get(guildId);
//				TLWScoreData scoreInfo = RedisProxy.getInstance().getTLWGuildScoreInfo(season, guildId);
//
//				JSONObject json = new JSONObject();
//				json.put("AllianceId", HawkUUIDGenerator.strUUID2Long(guildId)); // 联盟ID
//				json.put("AllianceName", guildData.getName()); // 联盟名称
//				json.put("Partition", guildData.getServerId()); // 联盟区服
//				json.put("AllianceFlag", guildData.getFlag()); // 联盟旗帜
//				json.put("ChampionName", guildData.getLeaderName()); // 盟主名称
//				json.put("MatchId", zoneId); // 赛区ID
//				json.put("MatchRank", rank); // 赛区内排名
//				json.put("WinNum", joinInfo.getWinCnt()); // 胜利场数
//				json.put("DefeatNum", joinInfo.getLoseCnt()); // 失败场数
//				json.put("Score", scoreInfo.getScore()); // 联盟累计积分
//				json.put("MatchStage", guildData.getSeason()); // 赛事阶段（小组赛、决赛）
//				json.put("AllianceMemberNum", guildData.getJoinMemberCnt()); // 联盟出战人数
//				json.put("Fight", guildData.getPower()); // 联盟出战总战力
//				
//				json.put("ChampionId", RedisProxy.getInstance().getPlayerIdByName(guildData.getLeaderName())); // 盟主角色ID
//				json.put("Zone", zoneId); // 赛区分组
//				json.put("Group", joinInfo.getGroup().getNumber()); // 赛区组别
//				infoList.add(json);
//				rank ++;
//			}
//			/***************************************/
//		}
		
		return infoList;
	}	
}
