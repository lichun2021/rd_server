package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventCommanderPutOnEquip;

/**
 * 指挥官穿戴{1}装备{2}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_PUT_ON_COMMANDER_EQUIP_TIMES)
public class CommanderPutOnEquipMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventCommanderPutOnEquip event = (EventCommanderPutOnEquip)missionEvent;
		if (event.getLevel() < cfg.getIds().get(0)) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}
