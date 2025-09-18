package com.hawk.robot.action.rank;

import java.util.Random;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Rank.HPSendRank;
import com.hawk.game.protocol.Rank.RankType;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;

/**
 * 排行榜读取操作
 * @author Jesse
 */
 
@RobotAction(valid = false)
public class PlayerRankAction extends HawkRobotAction {
	private static Random random = new Random();

	/**
	 * 随机请求一个排行榜单
	 */
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		RankType[] rankTypes = RankType.values();
		RankType type = rankTypes[random.nextInt(rankTypes.length)];
		HPSendRank.Builder req = HPSendRank.newBuilder();
		req.setRankType(type);
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.RANK_INFO_C_VALUE, req));
	}
}