package com.hawk.game.service.mssion.type;


import org.hawk.config.HawkConfigManager;

import com.google.common.collect.ImmutableList;
import com.hawk.game.config.EquipmentCfg;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.equip.CommanderObject;
import com.hawk.game.player.equip.EquipSlot;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 指挥官穿戴{1}等级{1}品质的装备{2}件
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_COMMANDER_PUT_ON_EQUIP_COUNT)
public class CommanderPutOnEquipCountMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int conditionLvl = cfg.getIds().get(0);
		int conditionQuality = cfg.getIds().get(1);
		
		int wareCount = 0;
		CommanderObject commander = playerData.getCommanderObject();
		ImmutableList<EquipSlot> equipSlots = commander.getEquipSlots();
		for (EquipSlot slot : equipSlots) {
			EquipEntity equip = playerData.getEquipEntity(slot.getEquipId());
			if (equip == null) {
				continue;
			}
			
			EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, equip.getCfgId());
			if (equipCfg.getLevel() < conditionLvl || equipCfg.getQuality() < conditionQuality) {
				continue;
			}
			wareCount++;
		}
		
		entityItem.setValue(wareCount);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int conditionLvl = cfg.getIds().get(0);
		int conditionQuality = cfg.getIds().get(1);
		
		CommanderObject commander = playerData.getCommanderObject();
		ImmutableList<EquipSlot> equipSlots = commander.getEquipSlots();
		for (EquipSlot slot : equipSlots) {
			EquipEntity equip = playerData.getEquipEntity(slot.getEquipId());
			if (equip == null) {
				continue;
			}
			EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, equip.getCfgId());
			if (equipCfg.getLevel() < conditionLvl || equipCfg.getQuality() < conditionQuality) {
				continue;
			}
			entityItem.addValue(1);
		}
		
		checkMissionFinish(entityItem, cfg);
	}
}
