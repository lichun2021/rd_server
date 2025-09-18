package com.hawk.game.service.mssion.type;

import java.util.List;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventAttackCollectTimes;

/**
 * 攻打{1}资源点{2}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_ATTACK_COLLECT_TIMES)
public class AttackCollectTimesMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventAttackCollectTimes event = (EventAttackCollectTimes)missionEvent;
		if (!event.isWin()) {
			return;
		}
		List<Integer> conditions = cfg.getIds();
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(event.getResourceType())) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}
