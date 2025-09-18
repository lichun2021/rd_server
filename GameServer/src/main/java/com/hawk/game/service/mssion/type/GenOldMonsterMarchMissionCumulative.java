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
import com.hawk.game.service.mssion.event.EventGenOldMonsterMarch;

/**
 * 向{1}等级野怪发起出征{2}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.GEN_OLD_MONSTER_MARCH_CUMULATIVE)
public class GenOldMonsterMarchMissionCumulative implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		Map<String, Integer> allMap = RedisProxy.getInstance().getCumulativeMissionCount(playerData.getPlayerId(), MissionType.GEN_OLD_MONSTER_MARCH_CUMULATIVE);
		int value = 0;
		for(Entry<String, Integer> ent: allMap.entrySet()){
			int level = NumberUtils.toInt(ent.getKey());
			if(level < cfg.getIds().get(0)){
				continue;
			}
			value += ent.getValue();
		}
		entityItem.addValue(value);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void cumulativeMission(String playerId, T missionEvent) {
		EventGenOldMonsterMarch event = (EventGenOldMonsterMarch) missionEvent;
		RedisProxy.getInstance().incCumulativeMissionCount(playerId, MissionType.GEN_OLD_MONSTER_MARCH_CUMULATIVE, event.getLevel() + "", 1);
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventGenOldMonsterMarch event = (EventGenOldMonsterMarch) missionEvent;
		if (event.getLevel() < cfg.getIds().get(0)) {
			return;
		}
		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}
}
