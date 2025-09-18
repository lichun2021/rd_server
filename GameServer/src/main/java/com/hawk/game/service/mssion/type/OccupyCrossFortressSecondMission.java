package com.hawk.game.service.mssion.type;

import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventOccupyCrossFortressSecond;
import com.hawk.game.util.GsConst.MissionState;

/**
 * 占领远征要塞x秒
 * 
 * @author golden
 *
 */
@Mission(missionType = MissionType.OCCUPY_CROSS_FORTRESS_SECOND)
public class OccupyCrossFortressSecondMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		// 任务已经完成状态了,不累加进度了
		if (entityItem.getState() == MissionState.STATE_FINISH) {
			return;
		}
		
		EventOccupyCrossFortressSecond event = (EventOccupyCrossFortressSecond) missionEvent;
		entityItem.addValue((int)event.getMillSeconds());
		
		if (entityItem.getValue() >= cfg.getValue() * 1000 && entityItem.getState() == MissionState.STATE_NOT_FINISH) {
			entityItem.setState(MissionState.STATE_FINISH);
		}
	}

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
	}
}
