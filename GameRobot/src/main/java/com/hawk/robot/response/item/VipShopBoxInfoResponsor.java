package com.hawk.robot.response.item;

import com.hawk.robot.annotation.RobotResponse;

import java.util.List;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.VipBoxInfoPB;
import com.hawk.game.protocol.Item.VipExclusiveBox;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.VIP_BOX_SYNC_S_VALUE)
public class VipShopBoxInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		VipBoxInfoPB vipBoxInfo = protocol.parseProtocol(VipBoxInfoPB.getDefaultInstance());
		List<VipExclusiveBox> list = vipBoxInfo.getExclusiveBoxList();
		if (!list.isEmpty()) {
			robotEntity.getBasicData().refreshVipExclusiveBox(list);
		}
		
		List<Integer> list1 = vipBoxInfo.getUnreceivedBenefitBoxList();
		robotEntity.getBasicData().refreshVipBenefitBox(list1, vipBoxInfo.hasBenefitBoxTaken() ? (vipBoxInfo.getBenefitBoxTaken() ? 1 : 0) : -1);
	}

}
