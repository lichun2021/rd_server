package com.hawk.robot.response.mission;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.BattleMission.BattleMissionPage;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.action.mission.PlayerMissionAction;
import com.hawk.robot.response.RobotResponsor;

/**
 * 军衔任务同步
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.code.BATTLE_MISSION_PAGE_S_VALUE)
public class PlayerBattleMissionSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		BattleMissionPage battleMission = protocol.parseProtocol(BattleMissionPage.getDefaultInstance());
		robotEntity.getActivityData().saveBattleMission(battleMission);
		GameRobotApp.getInstance().executeTask(new Runnable() {
			@Override
			public void run() {
				PlayerMissionAction.doBattleMissionBonusAction(robotEntity);
			}
		});
	}

}
