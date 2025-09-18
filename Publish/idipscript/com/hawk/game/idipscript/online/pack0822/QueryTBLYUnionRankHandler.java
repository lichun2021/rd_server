package com.hawk.game.idipscript.online.pack0822;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.tiberium.TLWActivityData;
import com.hawk.game.service.tiberium.TLWGuildJoinInfo;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.game.service.tiberium.TibernumConst.TLWActivityState;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询泰伯利亚联盟排行信息
 *
 * localhost:8080/script/idip/4395
 *
 * @param 
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4395")
public class QueryTBLYUnionRankHandler extends IdipScriptHandler {

	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		// 联盟ID
		String unionId = request.getJSONObject("body").getString("UnionID");
		GuildInfoObject guildInfo = getGuildInfo(unionId);
		if (guildInfo== null) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "unionId error, guild not exist");
			return result;
		}
		
		unionId = guildInfo.getId();
		
		// 赛区ID：1-8
		int zoneId = request.getJSONObject("body").getIntValue("MatchId");
		TLWActivityData activityInfo = TiberiumLeagueWarService.getInstance().getActivityInfo();
		int season = activityInfo.getSeason();
		Map<String, TLWGuildJoinInfo> infoMap = RedisProxy.getInstance().getAllTLWJoinGuild(season);
		if (infoMap.isEmpty()) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "no guildJoin info");
			return result;
		}
		
		if (activityInfo.state == TLWActivityState.TLW_CLOSE || activityInfo.state == TLWActivityState.TLW_NOT_OPEN) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "activity not opened");
			return result;
		}
		
		List<TLWGuildJoinInfo> guildList = infoMap.values().stream().filter(t -> t.getZone() == zoneId).collect(Collectors.toList());
		Collections.sort(guildList, new Comparator<TLWGuildJoinInfo>() {
			@Override
			public int compare(TLWGuildJoinInfo arg0, TLWGuildJoinInfo arg1) {
				if (arg0.getInitPower() != arg1.getInitPower()) {
					return arg0.getInitPower() > arg1.getInitPower() ? -1 : 1;
				}
				return arg0.getId().compareTo(arg1.getId());
			}
		});
		int rank = 0;
		TLWGuildJoinInfo selfGuild = null;
		for (TLWGuildJoinInfo info : guildList) {
			rank++;
			if (info.getId().equals(unionId)) {
				selfGuild = info;
				break;
			}
		}
		
		if (selfGuild == null) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "guildJoin info not found");
			return result;
		}

		result.getBody().put("UnionRank", rank);
		result.getBody().put("Partition", selfGuild.getServerId());
		result.getBody().put("UnionId", selfGuild.getId());
		result.getBody().put("Score", selfGuild.getScore());
		result.getBody().put("WinNum", selfGuild.getWinCnt());
		result.getBody().put("DefeatNum", selfGuild.getLoseCnt());
		result.getBody().put("UnionLevel", selfGuild.getGroup().getNumber());
		return result;
	}
	
	/**
	 * 获取联盟信息
	 * 
	 * @param unionId
	 * @return
	 */
	private GuildInfoObject getGuildInfo(String unionId) {
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(unionId);
		
		if (guildInfo == null) {
			try {
				String guildId = HawkUUIDGenerator.longUUID2Str(Long.parseLong(unionId));
				guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
			} catch (Exception e) {
				HawkLog.logPrintln("QueryTBLYUnionRankHandler fetch guildInfo failed, catch exception, unionId: {}", unionId);
				HawkException.catchException(e);
			}
		}
		
		return guildInfo;
	}
}


