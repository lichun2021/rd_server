package com.hawk.robot.response.item;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.VipShopItemInfo;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.VIP_SHOP_ITEM_SYNC_S_VALUE)
public class VipShopItemInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		VipShopItemInfo vipShopItemInfo = protocol.parseProtocol(VipShopItemInfo.getDefaultInstance());
		robotEntity.getBasicData().refreshVipShopItemData(vipShopItemInfo.getVipShopItemsList());
	}

}
