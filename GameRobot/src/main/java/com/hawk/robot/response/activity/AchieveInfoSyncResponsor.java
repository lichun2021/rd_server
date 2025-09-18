package com.hawk.robot.response.activity;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.AchieveItemsInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PUSH_ACHIEVE_INFO_SYNC_S_VALUE)
public class AchieveInfoSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		AchieveItemsInfoSync achieveInfo = protocol.parseProtocol(AchieveItemsInfoSync.getDefaultInstance());
		robotEntity.getActivityData().addAchiveData(achieveInfo.getItemList());
	}

}
