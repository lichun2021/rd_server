package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventHeroAttrUp;

/**
 * 升级{1}英雄属性{2}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_HERO_ATTR_UP)
public class HeroAttrUpMission implements IMission{

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventHeroAttrUp event = (EventHeroAttrUp) missionEvent;
		entityItem.addValue(event.getCount());
		checkMissionFinish(entityItem, cfg);
	}
}
