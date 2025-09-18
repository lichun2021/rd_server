package com.hawk.robot.response.friend;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Friend.FriendInfoResp;
import com.hawk.game.protocol.Friend.FriendMsg;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 * 好友列表返回协议
 * @author golden
 *
 */
@RobotResponse(code = HP.code.FRIEND_INFO_RESP_VALUE)
public class FriendInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		FriendInfoResp resp = protocol.parseProtocol(FriendInfoResp.getDefaultInstance());
		List<FriendMsg> FriendMsg = resp.getFriendsList();
		robotEntity.getBasicData().setFriendMsg(FriendMsg);
	}
}

