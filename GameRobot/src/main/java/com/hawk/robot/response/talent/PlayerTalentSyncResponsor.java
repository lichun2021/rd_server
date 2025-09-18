package com.hawk.robot.response.talent;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Talent.HPTalentInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.PLAYER_TALENT_SYNC_S_VALUE)
public class PlayerTalentSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPTalentInfoSync talentInfo = protocol.parseProtocol(HPTalentInfoSync.getDefaultInstance());
		robotEntity.getBasicData().refreshTalentData(talentInfo);
	}

}
