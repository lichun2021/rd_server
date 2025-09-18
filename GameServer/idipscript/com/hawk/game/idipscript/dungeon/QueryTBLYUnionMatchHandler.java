package com.hawk.game.idipscript.dungeon;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.service.GuildService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询泰伯利亚联盟赛区信息 -- 10282127
 *
 * localhost:8080/script/idip/4397
 *
 * @param OpenId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4397")
public class QueryTBLYUnionMatchHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
//		// 联盟ID
//		String unionId = request.getJSONObject("body").getString("UnionID");
//		GuildInfoObject guildInfo = getGuildInfo(unionId);
//		if (guildInfo== null) {
//			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
//			result.getBody().put("RetMsg", "unionId error, guild not exist");
//			return result;
//		}
//				
//		String serverId = GsConfig.getInstance().getServerId();
//		result.getBody().put("MatchId", AssembleDataManager.getInstance().getTiberiumZone(serverId));
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
				HawkLog.logPrintln("QueryTBLYUnionMatchHandler fetch guildInfo failed, catch exception, unionId: {}", unionId);
				HawkException.catchException(e);
			}
		}
		
		return guildInfo;
	}
}


