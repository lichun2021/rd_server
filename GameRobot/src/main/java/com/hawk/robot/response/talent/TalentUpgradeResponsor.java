package com.hawk.robot.response.talent;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Talent.HPTalentUpgradeResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.TALENT_UPGRADE_S_VALUE)
public class TalentUpgradeResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPTalentUpgradeResp resp = protocol.parseProtocol(HPTalentUpgradeResp.getDefaultInstance());
		robotEntity.getBasicData().updateTalentData(resp);
	}

}
