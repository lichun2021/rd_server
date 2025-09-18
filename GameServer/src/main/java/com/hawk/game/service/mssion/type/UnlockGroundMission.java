package com.hawk.game.service.mssion.type;

import java.util.List;
import java.util.Set;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.MissionService;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventUnlockGround;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.MissionFunType;

/**
 * 解锁地块任务
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_UNLOCK_GROUND)
public class UnlockGroundMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventUnlockGround event = (EventUnlockGround) missionEvent;

		// 科技id
		int areaId = event.getGroundId();

		List<Integer> conditions = cfg.getIds();
		// conditions为空 或 为0 代表任意的
		if (!conditions.isEmpty() && !conditions.contains(0) && !conditions.contains(areaId)) {
			return;
		}

		entityItem.addValue(1);
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		List<Integer> conditions = cfg.getIds();
		Set<Integer> unlockedAreas = playerData.getPlayerBaseEntity().getUnlockedAreaSet();
		
		for (int unlockedArea : unlockedAreas) {
			if (conditions.contains(unlockedArea)) {
				entityItem.addValue(1);
			}
		}
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		EventUnlockGround event = (EventUnlockGround) missionEvent;
		if (GameUtil.isCityOutsideAreaBlock(event.getGroundId())) {
			MissionService.getInstance().missionRefreshAsync(player, MissionFunType.FUN_UNLOCK_AREA, 0, 1);
		}
	}
}
