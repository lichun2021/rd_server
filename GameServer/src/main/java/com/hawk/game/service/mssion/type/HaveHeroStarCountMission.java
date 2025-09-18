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
 * 拥有{1}星以上英雄{2}个 (需要初始化)
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_HAVE_HERO_STAR_COUNT)
public class HaveHeroStarCountMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		
		List<Integer> conditions = cfg.getIds();
		int conditionStar = conditions.get(0);
		
		int count = 0;
		for (PlayerHero hero : heros) {
			if (hero.getStar() < conditionStar) {
				continue;
			}
			count++;
		}
		entityItem.setValue(count);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		
		List<Integer> conditions = cfg.getIds();
		int conditionStar = conditions.get(0);
		
		for (PlayerHero hero : heros) {
			if (hero.getStar() < conditionStar) {
				continue;
			}
			entityItem.addValue(1);
		}
		checkMissionFinish(entityItem, cfg);
	}
}
