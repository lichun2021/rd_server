package com.hawk.game.idipscript.online.pack0604;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改联盟名称、联盟堡垒名称、联盟宣言CD时间
 *
 * localhost:8080/script/idip/4373
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4373")
public class ChangeGuildNameCDTimeHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String allianceId = request.getJSONObject("body").getString("AllianceId");
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(allianceId);
		
		if (guildInfo == null) {
			try {
				String guildId = HawkUUIDGenerator.longUUID2Str(Long.parseLong(allianceId));
				guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
			} catch (Exception e) {
				HawkLog.logPrintln("ChangeGuildNameCDTime failed, catch exception, allianceId: {}", allianceId);
				HawkException.catchException(e);
			} finally {
				if (guildInfo == null) {
					result.getBody().put("Result", IdipConst.SysError.ALLIANCE_NOT_FOUND);
					result.getBody().put("RetMsg", "guild not exist");
					return result;
				}
			}
		}
		
		final String guildId = guildInfo.getId();
		
		int type = request.getJSONObject("body").getIntValue("Type"); // 修改类型：1-联盟名称，2-联盟堡垒名称，3-联盟宣言
		int cdSeconds = request.getJSONObject("body").getIntValue("CDTime");
		if (type == 1) {
			RedisProxy.getInstance().updateChangeContentCDTime(guildId, ChangeContentType.CHANGE_GUILD_NAME, cdSeconds);
		} else if (type == 2) {
			RedisProxy.getInstance().updateChangeContentCDTime(guildId, ChangeContentType.CHANGE_GUILD_MANOR_NAME, cdSeconds);
		} else {
			RedisProxy.getInstance().updateChangeContentCDTime(guildId, ChangeContentType.CHANGE_GUILD_ANNOUNCE, cdSeconds);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}


