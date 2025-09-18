package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventMonsterAttack;

/**
 * 击杀{1}等级以上的野怪{2}次
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_KILL_MONSTER_TIMES)
public class KillLevelMonsterTimesMission implements IMission{

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventMonsterAttack event = (EventMonsterAttack)missionEvent;
		if (!event.isWin()) {
			return;
		}
		if (event.getLevel() < cfg.getIds().get(0)) {
			return;
		}
		entityItem.addValue(event.getAtkTimes());
		checkMissionFinish(entityItem, cfg);
	}

}
