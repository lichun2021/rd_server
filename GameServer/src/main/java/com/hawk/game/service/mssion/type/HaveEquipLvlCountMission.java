package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.EquipmentCfg;
import com.hawk.game.entity.EquipEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventEquipUpgrade;
import com.hawk.game.service.mssion.event.EventForgeEquip;

/**
 * 拥有{1}级装备{2}个 (需要初始化)
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_EQUIP_LVL_COUNT)
public class HaveEquipLvlCountMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int conditionLvl = cfg.getIds().get(0);
		
		if (missionEvent instanceof EventEquipUpgrade) {
			EventEquipUpgrade event = (EventEquipUpgrade)missionEvent;
			
			if (conditionLvl == 0 ||
					(event.getBeforeLvl() < conditionLvl && event.getAfterLvl() >= conditionLvl )) {
				entityItem.addValue(1);
				checkMissionFinish(entityItem, cfg);
			}
		}
		
		if (missionEvent instanceof EventForgeEquip) {
			EventForgeEquip event = (EventForgeEquip)missionEvent;
			if (event.getLevel() < conditionLvl) {
				return;
			}
			entityItem.addValue(1);
			checkMissionFinish(entityItem, cfg);
		}
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<EquipEntity> equipEntities = playerData.getEquipEntities();
		for (EquipEntity entity : equipEntities) {
			EquipmentCfg equipCfg = HawkConfigManager.getInstance().getConfigByKey(EquipmentCfg.class, entity.getCfgId());
			if (equipCfg.getLevel() < cfg.getIds().get(0)) {
				continue;
			}
			entityItem.addValue(1);
		}
		checkMissionFinish(entityItem, cfg);
	}
}
