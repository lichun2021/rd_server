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
import com.hawk.game.service.mssion.event.EventHeroUpgrade;

/**
 * 英雄升级任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_HERO_UPGRADE)
public class HeroUpgradeMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventHeroUpgrade event = (EventHeroUpgrade) missionEvent;
		int heroId = event.getHeroId();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(heroId)) {
			return;
		}

		// 升级后等级
		int afterLevel = event.getAfterLevel();
		if (afterLevel > entityItem.getValue()) {
			entityItem.setValue(afterLevel);
		}

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		List<Integer> conditions = cfg.getIds();
		
		if (conditions.isEmpty() || conditions.get(0) == 0) {
			for (PlayerHero hero : heros) {
				int heroLevel = hero.getLevel();
				if (heroLevel > entityItem.getValue()) {
					entityItem.setValue(heroLevel);
				}
			}
			
		} else {
			for (int heroId : cfg.getIds()) {
				Optional<PlayerHero> hero = heros.stream().filter(e -> e.getCfgId() == heroId).findAny();
				if (hero.isPresent()) {
					int heroLevel = hero.get().getLevel();
					if (heroLevel > entityItem.getValue()) {
						entityItem.setValue(heroLevel);
					}
				}
			}
		}
		
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		// TODO Auto-generated method stub
		
	}
}
