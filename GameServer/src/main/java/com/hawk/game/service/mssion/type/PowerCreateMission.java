package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventPowerCreate;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 战力增加任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_POWER_CREATE)
public class PowerCreateMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventPowerCreate event = (EventPowerCreate) missionEvent;

		// 升级后等级
		entityItem.setValue(event.getAfterPower());

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		long value = playerData.getPlayerEntity().getBattlePoint();
		entityItem.setValue(value);

		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventPowerCreate event = (EventPowerCreate) missionEvent;
		int pointAdd = (int) (event.getAfterPower() - event.getBeforePower());
		if (pointAdd > 0) {
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_PLAYER_BATTLE_POINT, 0, pointAdd);
		}
	}
}
