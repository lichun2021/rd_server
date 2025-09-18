package com.hawk.robot.response.president;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.President.PresidentInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 * 国王战同步消息处理
 * @author zhenyu.shang
 * @since 2018年1月1日
 */
@RobotResponse(code = HP.code.PRESIDENT_INFO_SYNC_VALUE)
public class PresidentSyncInfoResponse extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		doPresidentInfoSync(robotEntity, protocol);
	}
	
	/**
	 * 王城同步消息
	 * @param robotEntity
	 * @param protocol
	 */
	private void doPresidentInfoSync(GameRobotEntity robotEntity, HawkProtocol protocol){
		PresidentInfoSync info = protocol.parseProtocol(PresidentInfoSync.getDefaultInstance());
		WorldDataManager.getInstance().setPresidentInfo(info.getInfo());
	}
}
