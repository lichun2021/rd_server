package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventPvpBattle;

/**
 * 在电塔中死兵
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_DEAD_IN_PRESIDENT_TOWER)
public class PresidentTowerDeadMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventPvpBattle event = (EventPvpBattle)missionEvent;
		
		// 判断是电塔行军类型
		int marchType = event.getMarchType().getNumber();
		if (marchType != WorldMarchType.PRESIDENT_TOWER_SINGLE_VALUE
				&& marchType != WorldMarchType.PRESIDENT_TOWER_MASS_VALUE
				&& marchType != WorldMarchType.PRESIDENT_TOWER_MASS_JOIN_VALUE) {
			return;
		}
		
		int count = 0;
		List<ArmyInfo> armyList = event.getSelfArmy();
		for (ArmyInfo army : armyList) {
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if (armyCfg.getLevel() >= cfg.getIds().get(1)) {
				count += army.getDeadCount();
			}
		}
		entityItem.addValue(count);
		
		checkMissionFinish(entityItem, cfg);
	}
}