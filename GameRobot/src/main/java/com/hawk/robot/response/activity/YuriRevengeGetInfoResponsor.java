package com.hawk.robot.response.activity;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.YuriRevenge.YuriRevengePageInfoResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.GET_YURI_REVENGE_PAGE_INFO_S_VALUE)
public class YuriRevengeGetInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		YuriRevengePageInfoResp resp = protocol.parseProtocol(YuriRevengePageInfoResp.getDefaultInstance());
		robotEntity.getActivityData().setActivityData(ActivityType.YURI_REVENGE, resp);
	}

}
