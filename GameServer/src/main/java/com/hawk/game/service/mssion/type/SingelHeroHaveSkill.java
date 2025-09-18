package com.hawk.game.service.mssion.type;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.hawk.game.config.HeroSkillCfg;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.SkillSlot;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 所有英雄镶嵌{1}等级{2}品质芯片
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_SINGEL_HERO_INSTALL_SKILL)
public class SingelHeroHaveSkill implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		
		List<Integer> conditions = cfg.getIds();
		int levelCondition = conditions.get(0);
		int qualityCondition = conditions.get(1);

		int count = 0;
		
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		for (PlayerHero hero : heros) {
			int thisCount = 0;
			ImmutableList<SkillSlot> skillSlots = hero.getSkillSlots();
			for (SkillSlot slot : skillSlots) {
				IHeroSkill skill = slot.getSkill();
				if (skill == null) {
					continue;
				}
				
				HeroSkillCfg heroCfg = skill.getCfg();
				if (heroCfg == null) {
					continue;
				}
				
				int level = skill.getLevel();
				
				if (level >= levelCondition && heroCfg.getSkillQuality() >= qualityCondition) {
					thisCount++;
				}
			}
			
			if (thisCount > count) {
				count = thisCount;
			}
		}
		
		entityItem.setValue(count);
		checkMissionFinish(entityItem, cfg);
	}
	
	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {

		List<Integer> conditions = cfg.getIds();
		int levelCondition = conditions.get(0);
		int qualityCondition = conditions.get(1);

		int count = 0;
		
		List<PlayerHero> heros = playerData.getHeroEntityList().stream().map(HeroEntity::getHeroObj).collect(Collectors.toList());
		for (PlayerHero hero : heros) {
			int thisCount = 0;
			ImmutableList<SkillSlot> skillSlots = hero.getSkillSlots();
			for (SkillSlot slot : skillSlots) {
				IHeroSkill skill = slot.getSkill();
				if (skill == null) {
					continue;
				}
				
				HeroSkillCfg heroCfg = skill.getCfg();
				if (heroCfg == null) {
					continue;
				}
				
				int level = skill.getLevel();
				
				if (level >= levelCondition && heroCfg.getSkillQuality() >= qualityCondition) {
					thisCount++;
				}
			}
			
			if (thisCount > count) {
				count = thisCount;
			}
		}
		
		entityItem.setValue(count);
		checkMissionFinish(entityItem, cfg);
	}
}