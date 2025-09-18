package com.hawk.game.idipscript.guild;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.service.GuildService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 通过联盟名称查询联盟信息 -- 10282038
 *
 * localhost:8080/script/idip/4221
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4221")
public class QueryGuildInfoByNameHandler extends IdipScriptHandler {
	
	static final int MAX_ALLIANCEMEMBERLIST_NUM = 50;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		String guildName = request.getJSONObject("body").getString("AllianceName");
		
		List<GuildInfoObject> list = new ArrayList<GuildInfoObject>();
		GuildService.getInstance().getGuildEntities(list);
		GuildInfoObject guildInfo = null;
		for (GuildInfoObject guild : list) {
			if (guild.getName().equals(guildName)) {
				guildInfo = guild;
				break;
			}
		} 
		
		if (guildInfo == null) {
			return result;
		}
		
		int pageNo = request.getJSONObject("body").getIntValue("PageNo");
		int indexStart = pageNo > 1 ? (pageNo - 1) * MAX_ALLIANCEMEMBERLIST_NUM : 0;
		int indexEnd = indexStart + MAX_ALLIANCEMEMBERLIST_NUM;
		indexStart += 1;
		
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildInfo.getId());
		int count = 0, totalCount = memberIds.size();
		JSONArray array = new JSONArray();
		Iterator<String> it = memberIds.iterator();
		while (it.hasNext()) {
			String playerId = it.next();
			count ++;
			if (count < indexStart) {
				continue;
			}
			
			if (count > indexEnd) {
				continue;
			}
			
			GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("AllianceId", guildInfo.getNumTypeId());
			jsonObj.put("AllianceName", IdipUtil.encode(guildInfo.getName()));
			jsonObj.put("AllianceMemberName", IdipUtil.encode(member.getPlayerName()));
			jsonObj.put("AllianceMemberId", member.getPlayerId());
			jsonObj.put("AllianceMemberPosition", member.getAuthority());
			jsonObj.put("AllianceMemberFight_count", member.getPower());
			Player playerSnapshot = GlobalData.getInstance().makesurePlayer(member.getPlayerId());
			jsonObj.put("AllianceMemberVipLevel", playerSnapshot.getVipLevel());
			String puid = playerSnapshot.getPuid();
			if (puid.contains("#")) {
				puid = puid.split("#")[0];
			}
			jsonObj.put("AllianceMemberopenid", puid);
			array.add(jsonObj);
		}
		
		result.getBody().put("TotalPageNo", (int)Math.ceil(totalCount * 1.0d /MAX_ALLIANCEMEMBERLIST_NUM));
		result.getBody().put("AllianceMemberList_count", array.size());
		result.getBody().put("AllianceMemberList", array);
		
		return result;
	}
	
}
