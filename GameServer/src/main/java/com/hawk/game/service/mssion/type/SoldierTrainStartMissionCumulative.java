package com.hawk.game.service.mssion.type;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.math.NumberUtils;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventSoldierTrainStart;

@Mission(missionType = MissionType.MISSION_SOLDIER_TRAIN_START_CUMULATIVE)
public class SoldierTrainStartMissionCumulative implements IMission {
	
	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		Map<String, Integer> allMap = RedisProxy.getInstance().getCumulativeMissionCount(playerData.getPlayerId(), MissionType.MISSION_SOLDIER_TRAIN_START_CUMULATIVE);
		int value = 0;
		for(Entry<String, Integer> ent: allMap.entrySet()){
			int soldierId = NumberUtils.toInt(ent.getKey());
			List<Integer> conditions = cfg.getIds();
			// conditions为空 或 为0 代表任意的
			if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(soldierId)) {
				continue;
			}
			value += ent.getValue();
		}
		entityItem.addValue(value);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void cumulativeMission(String playerId, T missionEvent) {
		EventSoldierTrainStart event = (EventSoldierTrainStart) missionEvent;
		int trainCount = event.getCount();
		RedisProxy.getInstance().incCumulativeMissionCount(playerId, MissionType.MISSION_SOLDIER_TRAIN_START_CUMULATIVE, event.getSoldierId() + "", trainCount);
	}
	
	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventSoldierTrainStart event = (EventSoldierTrainStart) missionEvent;

		// 兵种id
		int soldierId = event.getSoldierId();
		List<Integer> conditions = cfg.getIds();

		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(soldierId)) {
			return;
		}

		int trainCount = event.getCount();
		entityItem.addValue(trainCount);

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
	}
}
