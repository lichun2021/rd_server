package com.hawk.robot.response.activity;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.AchieveItemsInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotAppHelper;
import com.hawk.robot.action.activity.PlayerActivityAction;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PUSH_ADD_ACHIEVE_ITEM_S_VALUE)
public class AchieveItemAddResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		AchieveItemsInfoSync achieveInfo = protocol.parseProtocol(AchieveItemsInfoSync.getDefaultInstance());
		robotEntity.getActivityData().addAchiveData(achieveInfo.getItemList());
		// 投递一个延时任务，延时时长为30s到10分钟的一个随机值
		RobotAppHelper.getInstance().executeDelayTask(new Runnable() {
			@Override
			public void run() {
				PlayerActivityAction.achieveReward(robotEntity);
			}
		}, HawkRand.randInt(30000, 600000));
		
	}

}
