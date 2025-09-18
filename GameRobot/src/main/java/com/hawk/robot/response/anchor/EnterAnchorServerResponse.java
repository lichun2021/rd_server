package com.hawk.robot.response.anchor;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.Anchor.AnchorServerInfoGS;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 *
 * @author zhenyu.shang
 * @since 2018年4月4日
 */
@RobotResponse(code = HP.code.ENTER_ANCHOR_SERVER_S_VALUE)
public class EnterAnchorServerResponse extends RobotResponsor{

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		AnchorServerInfoGS info = protocol.parseProtocol(AnchorServerInfoGS.getDefaultInstance());
		robotEntity.getAnchorData().setServerInfo(info);
	}

}
