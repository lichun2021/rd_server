package com.hawk.robot.response.anchor;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Anchor.RoomInfo;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 *
 * @author zhenyu.shang
 * @since 2018年4月4日
 */
@RobotResponse(code = HP.code.ANCHOR_ROOM_INFO_S_VALUE)
public class AnchorRoomInfoResponse extends RobotResponsor{

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		RoomInfo info = protocol.parseProtocol(RoomInfo.getDefaultInstance());
		robotEntity.getAnchorData().setRoomInfo(info);
	}

}
