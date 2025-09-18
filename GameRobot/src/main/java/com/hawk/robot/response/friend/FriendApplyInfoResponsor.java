package com.hawk.robot.response.friend;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.Friend.FriendApplyInfoResp;
import com.hawk.game.protocol.Friend.FriendApplyMsg;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 * 好友申请列表返回协议
 * @author golden
 *
 */
@RobotResponse(code = {HP.code.FRIEND_APPLY_INFO_RESP_VALUE, HP.code.SYN_FRIEND_APPLY_UPDATE_VALUE})
public class FriendApplyInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		FriendApplyInfoResp resp = protocol.parseProtocol(FriendApplyInfoResp.getDefaultInstance());
		List<FriendApplyMsg> applyMsg = resp.getApplyMsgsList();
		robotEntity.getBasicData().setFriendApplyMsg(applyMsg);;
	}
}

