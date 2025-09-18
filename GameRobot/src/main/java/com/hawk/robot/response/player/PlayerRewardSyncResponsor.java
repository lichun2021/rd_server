package com.hawk.robot.response.player;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.HPPlayerReward;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

/**
 * 接收玩家奖励信息
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.code.PLAYER_AWARD_S_VALUE)
public class PlayerRewardSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPPlayerReward rewardInfo = protocol.parseProtocol(HPPlayerReward.getDefaultInstance());
		robotEntity.getBasicData().updatePlayerInfo(rewardInfo);
	}

}
