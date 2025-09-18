package com.hawk.robot.response.protocol;

import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SysProtocol.HPOperateSuccess;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.StatisticHelper;
import com.hawk.robot.action.mission.PlayerMissionAction;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 * 协议处理成功响应
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.sys.OPERATE_SUCCESS_VALUE)
public class ProtocolResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPOperateSuccess resp = protocol.parseProtocol(HPOperateSuccess.getDefaultInstance());
		StatisticHelper.incSuccessProtocolCnt(protocol.getType());
		
		switch (resp.getHpCode()) {
		case HP.code.TALENT_CLEAR_C_VALUE:
			robotEntity.getBasicData().clearTalentData();
			break;
		case HP.code.MISSION_BONUS_C_VALUE:
			PlayerMissionAction.doGeneralMissionBonusAction(robotEntity, robotEntity.getMissionObjects());
			break;
		default:
			break;
		}
	}

}
