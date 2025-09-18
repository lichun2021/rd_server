package com.hawk.robot.response.mission;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.StoryMission.StoryMissionPage;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.action.mission.PlayerMissionAction;
import com.hawk.robot.response.RobotResponsor;

/**
 * 章节任务同步
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.code.PUSH_STORY_MISSION_INFO_S_VALUE)
public class PlayerStoryMissionSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		StoryMissionPage storyMission = protocol.parseProtocol(StoryMissionPage.getDefaultInstance());
		robotEntity.getActivityData().saveStoryMission(storyMission);
		GameRobotApp.getInstance().executeTask(new Runnable() {
			@Override
			public void run() {
				PlayerMissionAction.doStoryMissionBonusAction(robotEntity, false);
			}
		});
	}

}
