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

/**
 * 拥有{1}等级以上英雄{2}个 (需要初始化)
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_HAVE_HERO_LVL_COUNT)
public class HaveHeroLvlCountMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int conditionLvl = cfg.getIds().get(0);
		
		int count = 0;
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		for (PlayerHero hero : heros) {
			if (hero.getLevel() < conditionLvl) {
				continue;
			}
			count++;
		}
		entityItem.setValue(count);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int conditionLvl = cfg.getIds().get(0);
		
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		for (PlayerHero hero : heros) {
			if (hero.getLevel() < conditionLvl) {
				continue;
			}
			entityItem.addValue(1);
		}
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
}
