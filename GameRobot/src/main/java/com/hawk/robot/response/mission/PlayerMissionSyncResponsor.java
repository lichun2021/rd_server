package com.hawk.robot.response.mission;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mission.MissionListRes;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.MISSION_LIST_SYNC_S_VALUE)
public class PlayerMissionSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		MissionListRes missionInfo = protocol.parseProtocol(MissionListRes.getDefaultInstance());
		robotEntity.getActivityData().refreshMissionData(missionInfo.getListList());
	}

}
