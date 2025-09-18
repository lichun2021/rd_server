package com.hawk.game.service.mssion.type;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.SuperSoldierSkillSlot;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventMechaPartLvUp;

@Mission(missionType = MissionType.MECHA_PART_LEVELUP_MAX)
public class MechaPartLevelMaxMission implements IMission {
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventMechaPartLvUp event = (EventMechaPartLvUp) missionEvent;
		
		List<Integer> conditions = cfg.getIds();
		if (conditions.get(0) != 0 && !conditions.contains(event.getSoldierId())) {
			return;
		}
		
		int maxLevel = getMechaPartMaxLevel(playerData, event.getSoldierId());
		entityItem.setValue(maxLevel);
		checkMissionFinish(entityItem, cfg);
	}

	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int soldierId = cfg.getIds().get(0);
		int maxLevel = 0;
		if (soldierId > 0) {
			maxLevel = getMechaPartMaxLevel(playerData, soldierId);
		} else {
			for (SuperSoldierEntity entity : playerData.getSuperSoldierEntityList()) {
				SuperSoldier mecha = entity.getSoldierObj();
				ImmutableList<SuperSoldierSkillSlot> skillSlots = mecha.getSkillSlots();
				for (SuperSoldierSkillSlot slot : skillSlots) {
					if (slot.getSkill() != null) {
						maxLevel = Math.max(slot.getSkill().getLevel(), maxLevel);
					}
				}
			}
		}
		
		entityItem.setValue(maxLevel);
		checkMissionFinish(entityItem, cfg);
	}
	
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
	private int getMechaPartMaxLevel(PlayerData playerData, int soldierId) {
		Optional<SuperSoldier> soldierOp = playerData.getSuperSoldierEntityList().stream()
				.map(SuperSoldierEntity::getSoldierObj).filter(e -> e.getDBEntity().getSoldierId() == soldierId).findAny();
		if (!soldierOp.isPresent()) {
			return 0;
		}
		
		SuperSoldier mecha = soldierOp.get();
		ImmutableList<SuperSoldierSkillSlot> skillSlots = mecha.getSkillSlots();
		int maxLevel = 0;
		for (SuperSoldierSkillSlot slot : skillSlots) {
			if (slot.getSkill() != null) {
				maxLevel = Math.max(slot.getSkill().getLevel(), maxLevel);
			}
		}
		
		return maxLevel;
	}
}
