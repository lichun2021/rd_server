package com.hawk.game.idipscript.guild;

import java.util.Collection;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.Player.PlayerSnapshotPB;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.GuildService;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询联盟成员信息 -- 10282025
 *
 * localhost:8080/script/idip/4173
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4173")
public class QueryGuildMemberHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String numId = request.getJSONObject("body").getString("GuildID");
		String guildId = HawkUUIDGenerator.longUUID2Str(Long.parseLong(numId));
		
		if (!GuildService.getInstance().isGuildExist(guildId)) {
			result.getHead().put("Result", IdipConst.SysError.ALLIANCE_NOT_FOUND);
			result.getHead().put("RetErrMsg", "guild not found, guildId: " + guildId);
			return result;
		}

		try {
			JSONArray jsonArray = new JSONArray();
			Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
			for (String playerId : memberIds) {
				GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
				JSONObject json = new JSONObject();
				json.put("RoleId", playerId);
				String openid = "";
				AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
				if (accountInfo != null) {
					openid = accountInfo.getPuid().split("#")[0];
				} else {
					PlayerSnapshotPB snapshot = RedisProxy.getInstance().getPlayerSnapshot(playerId);
					openid = snapshot.getPuid().split("#")[0];
				}
				json.put("OpenId", openid);
				json.put("Position", member.getAuthority());
				json.put("RegTime", member.getJoinGuildTime());
				jsonArray.add(json);
			}
			
			result.getBody().put("UnionMemberList_count", jsonArray.size());
			result.getBody().put("UnionMemberList", jsonArray);
		} catch (Exception e) {
			HawkException.catchException(e);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "query guild member info failed");
		}
		
		return result;
	}
}
