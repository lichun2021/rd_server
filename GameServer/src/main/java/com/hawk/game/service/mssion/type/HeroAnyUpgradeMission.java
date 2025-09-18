package com.hawk.game.service.mssion.type;

import java.util.List;
import java.util.stream.Collectors;

import com.hawk.game.entity.HeroEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventHeroUpgrade;

/**
 * x个英雄升级到n级任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_ANY_HERO_UPGRADE)
public class HeroAnyUpgradeMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventHeroUpgrade event = (EventHeroUpgrade) missionEvent;

		int beforeLevel = event.getBeforeLevel();
		int afterLevel = event.getAfterLevel();

		int conditionLvl = cfg.getIds().get(0);
		if (beforeLevel < conditionLvl && afterLevel >= conditionLvl) {
			entityItem.addValue(1);
		}
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());

		for (PlayerHero hero : heros) {
			int level = hero.getLevel();
			int conditionLvl = cfg.getIds().get(0);
			if (level >= conditionLvl) {
				entityItem.addValue(1);
			}
		}
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		// TODO Auto-generated method stub
		
	}
}
