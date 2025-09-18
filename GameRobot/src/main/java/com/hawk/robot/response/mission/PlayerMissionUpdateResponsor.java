package com.hawk.robot.response.mission;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mission.MissionBonusRes;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = { HP.code.MISSION_BONUS_S_VALUE, HP.code.MISSION_UPDATE_SYNC_S_VALUE })
public class PlayerMissionUpdateResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		MissionBonusRes missionBonusRes = protocol.parseProtocol(MissionBonusRes.getDefaultInstance());
		// 领取完任务奖励后刷新奖励
		if(missionBonusRes != null) {
			robotEntity.getActivityData().deleteMission(missionBonusRes.getRemoveMissionId());
			robotEntity.getCityData().getSendMissions().remove(missionBonusRes.getRemoveMissionId());
			robotEntity.getActivityData().refreshMissionData(missionBonusRes.getAddMissionList());
		} else {
			RobotLog.cityPrintln("mission bonus error, playerId: {}", robotEntity.getPlayerId());
		}
	}

}
