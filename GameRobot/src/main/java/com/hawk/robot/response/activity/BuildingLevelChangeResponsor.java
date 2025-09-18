package com.hawk.robot.response.activity;

import java.util.Map;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.BuildLevelInfoSync;
import com.hawk.game.protocol.Activity.BuildLevelItemPB;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PUSH_BUILD_LEVEL_CHANGE_S_VALUE)
public class BuildingLevelChangeResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		BuildLevelInfoSync buildLevelInfo = protocol.parseProtocol(BuildLevelInfoSync.getDefaultInstance());
		Map<Integer, BuildLevelItemPB> map = robotEntity.getActivityData().getActivityData(ActivityType.BUILD_LEVEL);
		if(map == null) {
			RobotLog.activityErrPrintln("building level change response, playerId: {}, BuildLevelItemPB map: {}", robotEntity.getPlayerId(), map);
			return;
		}
		
		for (BuildLevelItemPB pb : buildLevelInfo.getItemList()) {
			map.put(pb.getItemId(), pb);
		}
	}

}
