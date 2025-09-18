package com.hawk.game.gmscript;

import java.util.List;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.GsConfig;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.GuildManorService;

/**
 * 开启联盟超级矿
 * @author golden
 *
 */
public class OpenGuildMineHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {

		if (HawkOSOperator.isEmptyString(params.get("serverId"))) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "have no param: serverId");
		}
		 
		if (HawkOSOperator.isEmptyString(params.get("guildId"))) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "have no param: guildId");
		}
		
		String serverId = params.get("serverId");
		if (!serverId.equals(GsConfig.getInstance().getServerId())) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "serverId error!");
		}
		
		String guildId = params.get("guildId");
		
		List<IGuildBuilding> buildings = GuildManorService.getInstance().getGuildBuildings(guildId);
		//解锁未解锁的建筑
		for (IGuildBuilding iGuildBuilding : buildings) {
			if(iGuildBuilding.getBuildType() == TerritoryType.GUILD_MINE){
				if(iGuildBuilding.getBuildStat() == GuildBuildingStat.LOCKED){
					iGuildBuilding.tryChangeBuildStat(GuildBuildingStat.OPENED.getIndex());
					HawkLog.logPrintln("open guild mine guildId:{}, buildingId:{}, afterState", guildId, iGuildBuilding.getEntity().getBuildingId(), iGuildBuilding.getBuildStat());
				}
			}
		}
	
		return HawkScript.successResponse(null);
	}
}