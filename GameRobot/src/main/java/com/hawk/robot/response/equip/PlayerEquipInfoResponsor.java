package com.hawk.robot.response.equip;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.Equip.EquipInfo;
import com.hawk.game.protocol.Equip.HPEquipInfo;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.EQUIP_INFO_SYNC_S_VALUE)
public class PlayerEquipInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPEquipInfo equipInfo = protocol.parseProtocol(HPEquipInfo.getDefaultInstance());
		List<EquipInfo> equipList = equipInfo.getEquipInfoList();
		robotEntity.getBasicData().updateEquipInfo(equipList);
	}
}


