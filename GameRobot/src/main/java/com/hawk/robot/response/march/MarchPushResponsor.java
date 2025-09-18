package com.hawk.robot.response.march;

import java.util.List;

import com.hawk.robot.annotation.RobotResponse;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

/**
 * 登录时接收行军消息处理
 * 
 * @author lating
 *
 */
@RobotResponse(code = {HP.code.WORLD_MARCHS_PUSH_VALUE})
public class MarchPushResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		WorldMarchLoginPush worldMarchs = protocol.parseProtocol(WorldMarchLoginPush.getDefaultInstance());
		if(worldMarchs == null || worldMarchs.getMarchsList() == null || worldMarchs.getMarchsList().size() <= 0) {
			HawkLog.logMonitor("push world march failed, action: login push");
			return;
		}
		
		List<WorldMarchPB> marchList = worldMarchs.getMarchsList();
		robotEntity.getWorldData().refreshWorldMarch(robotEntity, marchList.toArray(new WorldMarchPB[0]));
	}

}
