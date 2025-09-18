package com.hawk.robot.response.login;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotApp;

import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.StatisticHelper;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.RobotAppConfig;
import com.hawk.robot.response.RobotResponsor;

/**
 * 登录游戏后服务器数据组装完成通知
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.code.ASSEMBLE_FINISH_S_VALUE)
public class AssembleFinishResponsor extends RobotResponsor {
	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		robotEntity.clearWaitLoginTimes();
		robotEntity.loginSuccess(robotEntity);
		StatisticHelper.removeLogin(robotEntity.getPuid());
		if (!StatisticHelper.isRegisterSucc(robotEntity.getPuid())) {
			StatisticHelper.removeRegisterFailed(robotEntity.getPuid());
			StatisticHelper.addRegisterSucc(robotEntity.getPuid());
		}
		
		// 注册满之前只做登录处理, 注册达上限后才可发送其它协议
		RobotAppConfig config = RobotAppConfig.getInstance();
		if (StatisticHelper.getRegisterSuccCnt() < config.getRobotRegisterCnt() &&
				StatisticHelper.getOnlineRobotCnt() > config.getRobotOnlineCnt() - config.getRobotOnlineCntGap()) {
			robotEntity.doLogout();
			HawkLog.logPrintln("robot assemble finish, do logout, puid: {}", robotEntity.getPuid());
			return;
		}
		
		int onlineDuration = HawkRobotApp.getInstance().getConfig().getInt("robot.onlineDuration");
		int offset = HawkRobotApp.getInstance().getConfig().getInt("robot.onlineOffset");
		onlineDuration += HawkRand.randInt(offset);
		robotEntity.setOfflineTime(HawkTime.getMillisecond() + onlineDuration * 1000);
		WorldDataManager.getInstance().addRobot(robotEntity.getPlayerId());
		HawkLog.logPrintln("robot assemble finish, puid: {}, online duration: {}", robotEntity.getPuid(), onlineDuration);
	}
}
