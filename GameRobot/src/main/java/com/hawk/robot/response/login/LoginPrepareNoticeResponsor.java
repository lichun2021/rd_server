package com.hawk.robot.response.login;

import com.hawk.robot.annotation.RobotResponse;

import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status.WaitLoginCode;
import com.hawk.game.protocol.Login.HPWaitLogin;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotAppConfig;
import com.hawk.robot.GameRobotEntity.RobotState;
import com.hawk.robot.RobotAppHelper;
import com.hawk.robot.response.RobotResponsor;

/**
 * 排队等待登录通知通知
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.code.LOGIN_PREPARE_SYNC_S_VALUE)
public class LoginPrepareNoticeResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPWaitLogin command = (HPWaitLogin) protocol.parseProtocol(HPWaitLogin.getDefaultInstance());
		int code = command.getWaitLoginCode();
		if(code == WaitLoginCode.ALLOW_LOGIN_VALUE) {
			RobotAppHelper.getInstance().userLogin(robotEntity);
			HawkLog.logPrintln("login allowed notice, puid: {}", robotEntity.getPuid());
		} else if (code == WaitLoginCode.WAITING_LOGIN_VALUE) {
			if (robotEntity.getWaitLoginTimes() > RobotAppConfig.getInstance().getWaitReloginTimes()) {
				robotEntity.doLogout();
				HawkLog.logPrintln("robot wait login times to uplimit, puid: {}", robotEntity.getPuid());
				return;
			}
			
			robotEntity.setState(RobotState.WAIT_LOGIN);
			// TODO 5秒内如果没有收到允许登录的通知，则5秒后发起重连
			GameRobotApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					robotEntity.doLoginAgain();
				}
			});
			
			HawkLog.logPrintln("wait login notice, puid: {}, waitNum: {}, waitIndex: {}", 
					robotEntity.getPuid(), command.getWaitingNum(), command.getWaitingIndex());
		}
	}

}
