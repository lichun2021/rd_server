package com.hawk.game.idipscript.dungeon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.service.tiberium.TLWActivityData;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.game.util.GameUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询泰伯利亚请求 -- 10282132
 *
 * localhost:8080/script/idip/4407
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4407")
public class QueryTBLYInfoHandler extends IdipScriptHandler {
	
	static final int MAX_TIMERIUMLIST_NUM = 50;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		int pageNo = request.getJSONObject("body").getIntValue("PageNo");
		int pageSize = request.getJSONObject("body").getIntValue("PageSize");
		if (pageSize <= 0) {
			pageSize = MAX_TIMERIUMLIST_NUM;
		}
		
		int indexStart = pageNo > 1 ? (pageNo - 1) * pageSize : 0;
		int indexEnd = indexStart + pageSize;
		indexStart += 1;
		int termId = request.getJSONObject("body").getIntValue("TermId");
		List<JSONObject> warInfoList = queryTLWWarInfo(termId);
		int count = 0, totalCount = warInfoList.size();
		JSONArray array = new JSONArray();
		Iterator<JSONObject> it = warInfoList.iterator();
		while (it.hasNext()) {
			JSONObject warInfo = it.next();
			count++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("MatchTime", warInfo.getLongValue("MatchTime")/1000);       // 对战时间
			jsonObj.put("AAllianceName", GameUtil.getStrEncoded(warInfo.getString("AAllianceName")));  // A联盟名称
			jsonObj.put("BAllianceName", GameUtil.getStrEncoded(warInfo.getString("BAllianceName")));  // B联盟名称
			jsonObj.put("AFight", warInfo.getLongValue("AFight"));              // A总战力
			jsonObj.put("BFight", warInfo.getLongValue("BFight"));              // B总战力
			jsonObj.put("AAllianceId", warInfo.getString("AAllianceId"));      // A联盟ID 
			jsonObj.put("BAllianceId", warInfo.getString("BAllianceId"));      // B联盟ID
			jsonObj.put("AScore", warInfo.getLongValue("AScore"));              // A积分  
			jsonObj.put("BScore", warInfo.getLongValue("BScore"));              // B积分 
			jsonObj.put("AConclusion", warInfo.getIntValue("AConclusion"));    // A胜负结论
			jsonObj.put("BConclusion", warInfo.getIntValue("BConclusion"));    // B胜负结论
			jsonObj.put("MatchId", warInfo.getString("MatchId"));              // 战斗编号
			
			array.add(jsonObj);
		}
		
		result.getBody().put("TotalPageNo", (int)Math.ceil(totalCount * 1.0d / pageSize));
		result.getBody().put("TimeriumList_count", array.size());
		result.getBody().put("TimeriumList", array);
		
		return result;
	}
	
	public List<JSONObject> queryTLWWarInfo( int termId) {
		TLWActivityData activityInfo = TiberiumLeagueWarService.getInstance().getActivityInfo();
		int season = activityInfo.getSeason();
		int currTermId = activityInfo.getTermId();

		List<JSONObject> finalList = new ArrayList<JSONObject>();
//		if(termId > currTermId){
//			return finalList;
//		}
//		// 若是请求当前期数的匹配阶段的对战数据,屏蔽不显示
//		if (termId == activityInfo.getTermId() && activityInfo.getState() == TLWActivityState.TLW_MATCH) {
//			return finalList;
//		}
//		int mark = TiberiumLeagueWarService.getInstance().combineSeasonTerm(season, termId);
//		List<TWRoomData> roomList = RedisProxy.getInstance().getAllTWRoomData(mark);
//		TiberiumSeasonTimeCfg timeCfg = TiberiumLeagueWarService.getInstance().getTimeCfgBySeasonAndTermId(season, termId);
//		long battleTime = timeCfg.getWarStartTimeValue();
//		List<String> guildIds = new ArrayList<>();
//		List<TWRoomData> zoneRoomList = new ArrayList<>();
//		if (termId < 12) {
//			// 小组赛
//			for (int j = 1; j <= 8; j++) {
//				int zoneId = j;
//				for (TWRoomData roomData : roomList) {
//					String roomServer = roomData.getRoomServerId();
//					int roomZone = AssembleDataManager.getInstance().getTiberiumZone(roomServer);
//					if (roomZone == zoneId) {
//						zoneRoomList.add(roomData);
//						guildIds.add(roomData.getGuildA());
//						guildIds.add(roomData.getGuildB());
//					}
//				}
//			}
//		} else {
//			// 决赛列表
//			for (TWRoomData roomData : roomList) {
//				zoneRoomList.add(roomData);
//				guildIds.add(roomData.getGuildA());
//				guildIds.add(roomData.getGuildB());
//			}
//		}
//		
//		Map<String, TLWGuildData> dataMap = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
//		for (TWRoomData roomData : zoneRoomList) {
//			TLWGuildData guildA = dataMap.get(roomData.getGuildA());
//			TLWGuildData guildB = dataMap.get(roomData.getGuildB());
//			String winnerId = roomData.getWinnerId();
//			long guildAIdNum = HawkUUIDGenerator.strUUID2Long(guildA.getId());
//			long guildBIdNum = HawkUUIDGenerator.strUUID2Long(guildB.getId());
//			long winnerIdNum = 0;
//			if (!HawkOSOperator.isEmptyString(winnerId)) {
//				winnerIdNum = HawkUUIDGenerator.strUUID2Long(winnerId);
//			}
//			
//			JSONObject json = new JSONObject();
//			json.put("MatchTime", battleTime); // 对战时间
//			json.put("AAllianceName", guildA.getName()); // A联盟名称
//			json.put("BAllianceName", guildB.getName()); // B联盟名称
//			json.put("AFight", guildA.getPower()); // A总战力
//			json.put("BFight", guildB.getPower()); // B总战力
//			json.put("AAllianceId", guildAIdNum); // A联盟ID
//			json.put("BAllianceId", guildBIdNum); // B联盟ID
//			json.put("AScore", roomData.getScoreA()); // A积分
//			json.put("BScore", roomData.getScoreB()); // B积分 
//			int winFlagA = winnerIdNum == guildAIdNum ? 1 : (winnerIdNum == 0 ? 0 : 2);
//			int winFlagB = winnerIdNum == guildBIdNum ? 1 : (winnerIdNum == 0 ? 0 : 2);
//			json.put("AConclusion", winFlagA); // A胜负结论
//			json.put("BConclusion", winFlagB); // B胜负结论
//			json.put("MatchId", roomData.getId());// 战斗编号
//			finalList.add(json);
//		}

		return finalList;
	}	
}
