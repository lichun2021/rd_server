package com.hawk.robot.response.activity;

import java.util.HashMap;
import java.util.Map;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.BuildLevelInfoSync;
import com.hawk.game.protocol.Activity.BuildLevelItemPB;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PUSH_BUILD_LEVEL_INFO_SYNC_S_VALUE)
public class BuildingLevelInfoSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		// 建筑等级活动
		BuildLevelInfoSync buildLevelInfo = protocol.parseProtocol(BuildLevelInfoSync.getDefaultInstance());
		Map<Integer, BuildLevelItemPB> map = new HashMap<>();
		for (BuildLevelItemPB itemPb : buildLevelInfo.getItemList()) {
			map.put(itemPb.getItemId(), itemPb);
		}
		robotEntity.getActivityData().setActivityData(ActivityType.BUILD_LEVEL, map);
	}

}
