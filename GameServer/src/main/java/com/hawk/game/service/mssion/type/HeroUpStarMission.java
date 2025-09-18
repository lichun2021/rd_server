package com.hawk.game.service.mssion.type;

import java.util.List;
import java.util.Optional;
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
 * 升星{1}英雄到{2}星级 (需要初始化)
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_HERO_STAR_UP)
public class HeroUpStarMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		List<Integer> conditions = cfg.getIds();
		
		if (conditions.isEmpty() || conditions.get(0) == 0) {
			for (PlayerHero hero : heros) {
				int heroStar = hero.getStar();
				if (heroStar > entityItem.getValue()) {
					entityItem.setValue(heroStar);
				}
			}
			
		} else {
			for (int heroId : cfg.getIds()) {
				Optional<PlayerHero> hero = heros.stream().filter(e -> e.getCfgId() == heroId).findAny();
				if (hero.isPresent()) {
					int heroStar = hero.get().getStar();
					if (heroStar > entityItem.getValue()) {
						entityItem.setValue(heroStar);
					}
				}
			}
		}
		
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		List<Integer> conditions = cfg.getIds();
		
		if (conditions.isEmpty() || conditions.get(0) == 0) {
			for (PlayerHero hero : heros) {
				int heroStar = hero.getStar();
				if (heroStar > entityItem.getValue()) {
					entityItem.setValue(heroStar);
				}
			}
			
		} else {
			for (int heroId : cfg.getIds()) {
				Optional<PlayerHero> hero = heros.stream().filter(e -> e.getCfgId() == heroId).findAny();
				if (hero.isPresent()) {
					int heroStar = hero.get().getStar();
					if (heroStar > entityItem.getValue()) {
						entityItem.setValue(heroStar);
					}
				}
			}
		}
		
		checkMissionFinish(entityItem, cfg);
	}
}
