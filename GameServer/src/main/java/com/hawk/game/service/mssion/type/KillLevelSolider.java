package com.hawk.game.service.mssion.type;

import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventPvpBattle;

/**
 * 击杀x等级的士兵x个
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_KILL_LEVEL_SOLIDER_COUNT)
public class KillLevelSolider implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventPvpBattle event = (EventPvpBattle)missionEvent;
		
		int killCount = 0;
		Map<Integer, Integer> armyKillMap = event.getArmyKillMap();
		for (Entry<Integer, Integer> entry : armyKillMap.entrySet()) {
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entry.getKey());
			if (armyCfg.getLevel() < cfg.getIds().get(0)) {
				continue;
			}
			killCount += entry.getValue();
		}
		//添加日志方便定位
		if (!armyKillMap.isEmpty()) {
			HawkLog.logPrintln("KillLevelSolider mission touch, playerId: {}, killCount: {}, armyKillMap: {}", playerData.getPlayerId(), killCount, armyKillMap);
		}
		entityItem.addValue(killCount);
		checkMissionFinish(entityItem, cfg);
	}
}
