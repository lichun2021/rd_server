package com.hawk.game.service.mssion.type;

import java.util.Collection;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.PlayerAchieveEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.PlayerAchieveItem;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PlayerAchieveService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

@Mission(missionType = MissionType.MISSION_SOLE_PRESIDENT)
public class ControlPresidentMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		String guildId = getGuildId(playerData.getPlayerId());
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		boolean soleAchieveConclude = PlayerAchieveService.getInstance().soleAchieveConclude(guildId, MissionType.MISSION_SOLE_PRESIDENT.intValue());
		if (!soleAchieveConclude) {
			return;
		}
		
		Collection<String> guildMembers = GuildService.getInstance().getGuildMembers(guildId);
		for (String playerId : guildMembers) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			PlayerAchieveEntity entity = player.getData().getPlayerAchieveEntity();
			PlayerAchieveItem memberEntityItem = entity.getAchieveItem(entityItem.getCfgId());
			if (memberEntityItem == null) {
				continue;
			}
			
			memberEntityItem.setValue(1);
			checkMissionFinish(memberEntityItem, cfg);
			if (player.isActiveOnline()) {
				PlayerAchieveService.getInstance().syncAchieveUpdate(player, memberEntityItem);
			}
		}
		
	}
	
	/** 联盟Id
	 */
	public String getGuildId(String playerId) {
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		return guild == null ? null : guild.getId();
	}
}
