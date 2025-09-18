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

@Mission(missionType = MissionType.MECHA_PART_LEVELUP)
public class MechaPartLevelMission implements IMission {
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventMechaPartLvUp event = (EventMechaPartLvUp) missionEvent;
		
		List<Integer> conditions = cfg.getIds();
		if (conditions.get(0) != 0 && !conditions.contains(event.getSoldierId())) {
			return;
		}
		
		int minLevel = getMechaPartMinLevel(playerData, event.getSoldierId());
		if (minLevel < 0) {
			return;
		}
		entityItem.setValue(minLevel);
		checkMissionFinish(entityItem, cfg);
	}

	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int soldierId = cfg.getIds().get(0);
		int minLevel = 0;
		if (soldierId > 0) {
			minLevel = getMechaPartMinLevel(playerData, soldierId);
			if (minLevel < 0) {
				return;
			}
		} else {
			int maxLevel = 0;
			for (SuperSoldierEntity entity : playerData.getSuperSoldierEntityList()) {
				SuperSoldier mecha = entity.getSoldierObj();
				ImmutableList<SuperSoldierSkillSlot> skillSlots = mecha.getSkillSlots();
				int detailMinLevel = Integer.MAX_VALUE;
				// 从一个机甲中找出等级最低的那个槽位
				for (SuperSoldierSkillSlot slot : skillSlots) {
					if (slot.getSkill() != null) {
						detailMinLevel = Math.min(slot.getSkill().getLevel(), detailMinLevel);
					}
				}
				// 找出最低等级对比中值最大的那个机甲
				if (detailMinLevel != Integer.MAX_VALUE && detailMinLevel > maxLevel) {
					maxLevel = detailMinLevel;
				}
			}
			
			minLevel = maxLevel;
		}
		
		entityItem.setValue(minLevel);
		checkMissionFinish(entityItem, cfg);
	}
	
	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}
	
	private int getMechaPartMinLevel(PlayerData playerData, int soldierId) {
		Optional<SuperSoldier> soldierOp = playerData.getSuperSoldierEntityList().stream()
				.map(SuperSoldierEntity::getSoldierObj).filter(e -> e.getDBEntity().getSoldierId() == soldierId).findAny();
		if (!soldierOp.isPresent()) {
			return -1;
		}
		
		SuperSoldier mecha = soldierOp.get();
		ImmutableList<SuperSoldierSkillSlot> skillSlots = mecha.getSkillSlots();
		int minLevel = Integer.MAX_VALUE;
		for (SuperSoldierSkillSlot slot : skillSlots) {
			if (slot.getSkill() != null) {
				minLevel = Math.min(slot.getSkill().getLevel(), minLevel);
			}
		}
		
		if (minLevel == Integer.MAX_VALUE) {
			minLevel = -1;
		}
		
		return minLevel;
	}
}
