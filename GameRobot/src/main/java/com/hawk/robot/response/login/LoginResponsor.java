package com.hawk.robot.response.login;

import com.hawk.robot.annotation.RobotResponse;

import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Login.HPLoginRet;
import com.hawk.game.protocol.Status.WaitLoginCode;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.GameRobotEntity.RobotState;
import com.hawk.robot.response.RobotResponsor;

/**
 * 登录成功通知
 * 
 * @author
 *
 */
@RobotResponse(code = HP.code.LOGIN_S_VALUE)
public class LoginResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPLoginRet command = (HPLoginRet) protocol.parseProtocol(HPLoginRet.getDefaultInstance());
		int errCode = command.getErrCode();
		if(errCode == Status.SysError.SUCCESS_OK_VALUE) {
			robotEntity.setPlayerId(command.getPlayerId());
			robotEntity.getBasicData().getPlayerInfo().setPlayerId(command.getPlayerId());
			HawkLog.logPrintln("robot login success, puid: {}, playerId: {}", robotEntity.getPuid(), command.getPlayerId());
		} else if (errCode == WaitLoginCode.WAIT_LOGIN_UPPER_LIMIT_VALUE) {
			// TODO 排队人数已达上限，直接关闭session
			robotEntity.doLogout();
		} else if (errCode == WaitLoginCode.WAITING_LOGIN_VALUE) {
			robotEntity.setState(RobotState.WAIT_LOGIN);
			// TODO 5秒内如果没有收到允许登录的通知，则5秒后发起重连
			GameRobotApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
				@Override
				protected void doAction() {
					robotEntity.doLoginAgain();
				}
			});
		} else {
			robotEntity.doLogout();
			HawkLog.logPrintln("robot login failed, puid: {}, errCode: {}", robotEntity.getPuid(), command.getErrCode());
		}
	}

}
