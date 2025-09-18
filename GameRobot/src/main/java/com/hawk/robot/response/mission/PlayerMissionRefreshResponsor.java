package com.hawk.robot.response.mission;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mission.MissionRefreshRes;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.action.mission.PlayerMissionAction;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.MISSION_REFRESH_S_VALUE)
public class PlayerMissionRefreshResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		MissionRefreshRes missionRefresh = protocol.parseProtocol(MissionRefreshRes.getDefaultInstance());
		robotEntity.getActivityData().refreshMissionData(missionRefresh.getRefreshMissionList());
		// 刷新任务后查看是否有可领奖的任务
		GameRobotApp.getInstance().executeTask(new Runnable() {
			@Override
			public void run() {
				PlayerMissionAction.doGeneralMissionBonusAction(robotEntity, missionRefresh.getRefreshMissionList());
			}
		});
	}

}
