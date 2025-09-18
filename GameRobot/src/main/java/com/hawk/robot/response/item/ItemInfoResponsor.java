package com.hawk.robot.response.item;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.HPItemInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.ITEM_INFO_SYNC_S_VALUE)
public class ItemInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPItemInfoSync itemInfo = protocol.parseProtocol(HPItemInfoSync.getDefaultInstance());
		robotEntity.getBasicData().refreshItemData(itemInfo.getItemInfosList());
	}

}
