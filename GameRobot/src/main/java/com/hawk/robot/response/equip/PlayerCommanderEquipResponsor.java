package com.hawk.robot.response.equip;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.Equip.CommanderEquipSync;
import com.hawk.game.protocol.Equip.PBEquipSlot;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.COMMANDER_EQUIP_SYNC_S_VALUE)
public class PlayerCommanderEquipResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		CommanderEquipSync equipInfo = protocol.parseProtocol(CommanderEquipSync.getDefaultInstance());
		List<PBEquipSlot> slotInfoList = equipInfo.getEquipSlotList();
		robotEntity.getBasicData().updateCommanderEquipInfo(slotInfoList);
	}
}


