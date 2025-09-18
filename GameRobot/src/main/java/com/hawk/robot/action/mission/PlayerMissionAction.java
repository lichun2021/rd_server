package com.hawk.robot.action.mission;

import java.util.List;
import java.util.Optional;
import org.hawk.annotation.RobotAction;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotAppConfig;
import com.hawk.robot.RobotLog;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mission.MissionBonusReq;
import com.hawk.game.protocol.Mission.MissionPB;
import com.hawk.game.protocol.StoryMission.MissionData;
import com.hawk.game.protocol.StoryMission.StoryMissionPage;
import com.hawk.game.protocol.StoryMission.StoryMissionRewReq;

/**
 * 
 * 领取任务奖励
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class PlayerMissionAction extends HawkRobotAction {
	// 已完成未领取状态
	public static final int STATE_FINISH = 1;
	
	/**
	 * 任务类型
	 */
	private static enum MissionType {
		GENERAL_MISSION,  // 主线任务
		STORY_MISSION,    // 剧情任务
		BATTLE_MISSION,   // 军衔任务
	}

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		
		MissionType type = EnumUtil.random(MissionType.class);
		switch (type) {
		case GENERAL_MISSION:
			doGeneralMissionBonusAction(robot, robot.getMissionObjects());
			break;
		case STORY_MISSION:
			doStoryMissionBonusAction(robot, true);
			break;
		case BATTLE_MISSION:
			doBattleMissionBonusAction(robot);
		default:
			break;
		}
	}
	
	/**
	 * 主线任务奖励领取
	 * 
	 * @param robot
	 * @param missionObjs
	 */
	public static synchronized void doGeneralMissionBonusAction(GameRobotEntity robot, List<MissionPB> missionObjs) {
		if (!ClientUtil.isExecuteAllowed(robot, PlayerMissionAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		if(!robot.isOnline()) {
			return;
		}
		
		if (robot.getMilitaryLevel() < RobotAppConfig.getInstance().getMilitaryLevelBoundary()) {
			return;
		}
		
		if(missionObjs.size() <= 0) {
			return;
		}
		
		List<String> sendMissions = robot.getCityData().getSendMissions();
		for(MissionPB missionObj : missionObjs) {
			if (missionObj.getState() != STATE_FINISH || sendMissions.contains(missionObj.getMissionId())) {
				continue;
			}
			
			sendMissions.add(missionObj.getMissionId());
			MissionBonusReq.Builder builder = MissionBonusReq.newBuilder();
			builder.setMissionId(missionObj.getMissionId());
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.MISSION_BONUS_C_VALUE, builder));
			robot.getCityData().getLastExecuteTime().put(PlayerMissionAction.class.getSimpleName(), HawkTime.getMillisecond());
			RobotLog.cityPrintln("mission bonus action, playerId: {}, missionId: {}", 
					robot.getPlayerId(), missionObj.getMissionId());
			break;
		}
	}
	
	/**
	 * 领取剧情任务奖励
	 * 
	 * @param robot
	 * @param inner
	 */
	public static void doStoryMissionBonusAction(GameRobotEntity robot, boolean inner) {
		if(!robot.isOnline()) {
			return;
		}
		
		StoryMissionPage storyMission = robot.getActivityData().getStoryMission();
		if(storyMission == null) {
			RobotLog.cityErrPrintln("story mission bonus req failed, playerId: {}, storyMission: {}", robot.getPlayerId(), storyMission);
			return;
		}
		StoryMissionRewReq.Builder builder = StoryMissionRewReq.newBuilder();
		if(storyMission.getChapterState() == STATE_FINISH) {
			builder.setIsChapterAward(true);
			builder.setMissionId(0);
		} else {
			List<MissionData> missionDataList = storyMission.getDataList();
			if(missionDataList == null || missionDataList.size() <= 0) {
				return;
			}
			Optional<MissionData> op = missionDataList.stream().filter(e -> e.getState() == STATE_FINISH).findAny();
			if(!op.isPresent()) {
				return;
			}
			builder.setIsChapterAward(false);
			builder.setMissionId(op.get().getMissionId());
		}
		
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.STORY_MISSION_REWARD_C_VALUE, builder));
		RobotLog.cityPrintln("story mission bonus, playerId: {}, missionId: {}", robot.getPlayerId(), builder.getMissionId());
	}
	
	/**
	 * 领取军衔任务奖励
	 * 
	 * @param robot
	 * @param inner
	 */
	public static void doBattleMissionBonusAction(GameRobotEntity robot) {
//		if(!robot.isOnline()) {
//			return;
//		}
//		
//		if (robot.getMilitaryLevel() < RobotAppConfig.getInstance().getMilitaryLevelBoundary()) {
//			return;
//		}
//		
//		BattleMissionPage battleMission = robot.getActivityData().getBattleMission();
//		if(battleMission == null) {
//			RobotLog.cityErrPrintln("battle mission bonus req failed, playerId: {}, battleMission: {}", robot.getPlayerId(), battleMission);
//			return;
//		}
//		
//		if(battleMission.getState() != BattleMissionState.COMPLETE) {
//			return;
//		}
//		
//		robot.sendProtocol(HawkProtocol.valueOf(HP.code.BATTLE_MISSION_CHATPTER_REWARD_C_VALUE));
//		RobotLog.cityPrintln("battle mission bonus, playerId: {}", robot.getPlayerId());
	}
	
}
