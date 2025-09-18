package com.hawk.game.idipscript.dungeon;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.tiberium.TLWActivityData;
import com.hawk.game.service.tiberium.TLWGuildJoinInfo;
import com.hawk.game.service.tiberium.TWGuildData;
import com.hawk.game.service.tiberium.TWRoomData;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询泰伯利亚单场比赛胜负情况 -- 10282125
 *
 * localhost:8080/script/idip/4393
 *
 * @param 
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4393")
public class QueryTBLYSingleMatchHandler extends IdipScriptHandler {

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
		long startTime = request.getJSONObject("body").getLongValue("BeginTime") * 1000;
		long endTime = request.getJSONObject("body").getLongValue("EndTime") * 1000;
		TLWActivityData activityInfo = TiberiumLeagueWarService.getInstance().getActivityInfo();
		int season = activityInfo.getSeason();

		ConfigIterator<TiberiumSeasonTimeCfg> cfgIts = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonTimeCfg.class);
		TiberiumSeasonTimeCfg timeCfg = null;
		for (TiberiumSeasonTimeCfg cfg : cfgIts) {
			long warStartTime = cfg.getWarStartTimeValue();
			if (warStartTime >= startTime && warStartTime <= endTime) {
				timeCfg = cfg;
				break;
			}
		}
		// 当前时间范围没有比赛
		if(timeCfg == null){
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "time range error");
			return result;
		}
		
		// 赛季数不对
		if(season > 0 && timeCfg.getSeason() !=season){
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "season error");
			return result;
		}
		
		if(timeCfg.getTermId() > activityInfo.getTermId()){
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "termId error");
			return result;
		}
		
		int mark = TiberiumLeagueWarService.getInstance().combineSeasonTerm(timeCfg.getSeason(), timeCfg.getTermId());
		List<TWRoomData> roomList = RedisProxy.getInstance().getAllTWRoomData(mark);
		TWRoomData roomData = null;
		for (TWRoomData room : roomList) {
			if (room.getGuildA().equals(unionId) || room.getGuildB().equals(unionId)) {
				roomData = room;
				break;
			}
		}
		// 没有对战信息
		if(roomData == null){
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "no match info");
			return result;
		}
		
		String guildAId = roomData.getGuildA();
		String guildBId = roomData.getGuildB();
		TLWGuildJoinInfo guildAInfo = RedisProxy.getInstance().getTLWJoinGuild(season, guildAId);
		TLWGuildJoinInfo guildBInfo = RedisProxy.getInstance().getTLWJoinGuild(season, guildBId);
		if(guildAInfo == null || guildBInfo == null){
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "no guildInfo");
			return result;
		}
		
		result.getBody().put("APartition", guildAInfo.getServerId());
		result.getBody().put("AUnionId", guildAInfo.getId());
		result.getBody().put("BPartition", guildBInfo.getServerId());
		result.getBody().put("BUnionId", guildBInfo.getId());
		result.getBody().put("MatchTime", timeCfg.getWarStartTimeValue()/1000);
		String winnerId = roomData.getWinnerId();
		if (HawkOSOperator.isEmptyString(winnerId)) {
			return result;
		}
		TWGuildData guildA = RedisProxy.getInstance().getTWGuildData(roomData.getGuildA(), mark);
		TWGuildData guildB = RedisProxy.getInstance().getTWGuildData(roomData.getGuildB(), mark);
		if(guildA == null || guildB == null){
			return result;
		}
		result.getBody().put("Outcome", winnerId.equals(roomData.getGuildA())?1 : 0);
		result.getBody().put("AScore", guildA.getScore());
		result.getBody().put("BScore", guildB.getScore());
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
				HawkLog.logPrintln("QueryTBLYSingleMatchHandler fetch guildInfo failed, catch exception, unionId: {}", unionId);
				HawkException.catchException(e);
			}
		}
		
		return guildInfo;
	}
	
}


