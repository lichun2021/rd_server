package com.hawk.game.service.mssion.type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * 英雄组合
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_HAVE_HERO_GROUP)
public class HeroGroupMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		
		Set<Integer> groupSet = new HashSet<>();
		for (Integer condition : cfg.getIds()) {
			groupSet.add(condition);
		}
		
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		for (PlayerHero hero : heros) {
			groupSet.remove(hero.getCfgId());
		}
		
		if (groupSet.size() == 0) {
			entityItem.setValue(1);
		} else {
			entityItem.setValue(0);
		}
		
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		
		Set<Integer> groupSet = new HashSet<>();
		for (Integer condition : cfg.getIds()) {
			groupSet.add(condition);
		}
		
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		for (PlayerHero hero : heros) {
			groupSet.remove(hero.getCfgId());
		}
		
		if (groupSet.size() == 0) {
			entityItem.setValue(1);
		} else {
			entityItem.setValue(0);
		}
		
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		// TODO Auto-generated method stub
		
	}
}
