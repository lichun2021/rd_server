package com.hawk.game.service.mssion.type;

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
import com.hawk.game.service.mssion.event.EventAttackFoggy;

/**
 * 攻击迷雾要塞胜利{1}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_ATK_FOGGY_WIN_TIMES_CUMULATIVE)
public class AttackFoggyWinTimesCumulative implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {

	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		Map<String, Integer> allMap = RedisProxy.getInstance().getCumulativeMissionCount(playerData.getPlayerId(), MissionType.MISSION_ATK_FOGGY_WIN_TIMES_CUMULATIVE);
		int value = 0;
		for (Entry<String, Integer> ent : allMap.entrySet()) {
			int level = NumberUtils.toInt(ent.getKey());
			if (level < cfg.getIds().get(0)) {
				continue;
			}
			value += ent.getValue();
		}
		entityItem.addValue(value);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void cumulativeMission(String playerId, T missionEvent) {
		EventAttackFoggy event = (EventAttackFoggy) missionEvent;
		RedisProxy.getInstance().incCumulativeMissionCount(playerId, MissionType.MISSION_ATK_FOGGY_WIN_TIMES_CUMULATIVE, event.getFoggyLvl() + "", 1);
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventAttackFoggy event = (EventAttackFoggy) missionEvent;
		if (event.getFoggyLvl() < cfg.getIds().get(0) || !event.isWin()) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}
