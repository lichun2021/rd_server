package com.hawk.robot.response.travelshop;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TravelShop.TravelGiftBuyS;
import com.hawk.game.protocol.TravelShop.TravelShopBuyResp;
import com.hawk.game.protocol.TravelShop.TravelShopInfoSync;
import com.hawk.game.protocol.TravelShop.TravelShopItemMsg;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = {HP.code.TRAVEL_SHOP_INFO_SYNC_S_VALUE, 
		HP.code.TRAVEL_SHOP_BUY_S_VALUE, HP.code.TRAVEL_GIFT_BUY_S_VALUE})
public class PlayerTravelShopResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		switch(protocol.getType()) {
		case HP.code.TRAVEL_SHOP_INFO_SYNC_S_VALUE:
			doInfoResp(robotEntity, protocol);
			break;
		case HP.code.TRAVEL_GIFT_BUY_S_VALUE:
			doGiftBuyResp(robotEntity, protocol);
			break;
		case HP.code.TRAVEL_SHOP_BUY_C_VALUE:
			doBuyResp(robotEntity, protocol);
			break;
		}

	}

	private void doBuyResp(GameRobotEntity robotEntity, HawkProtocol protocol) {
		TravelShopBuyResp buyResp = protocol.parseProtocol(TravelShopBuyResp.getDefaultInstance());
		TravelShopInfoSync.Builder builder = robotEntity.getBasicData().getTravelShop(); 
		if (buyResp.getRlt() == Status.SysError.SUCCESS_OK_VALUE) {
			int index = 0;
			TravelShopItemMsg.Builder item = null;
			for (; index < builder.getItemsBuilderList().size(); index++) {
				item = builder.getItemsBuilderList().get(index);
				if (item.getCfgId() == buyResp.getTravelShopItemMsg().getCfgId()) {
					item.setBought(1);
					if (buyResp.getTravelShopItemMsg().hasGiftMsg()) {
						item.setGiftMsg(buyResp.getTravelShopItemMsg().getGiftMsg());
					}					
					break;
				}
			}
		}
		
	}

	private void doGiftBuyResp(GameRobotEntity robotEntity, HawkProtocol protocol) {
		TravelGiftBuyS giftBuyResp = protocol.parseProtocol(TravelGiftBuyS.getDefaultInstance());
		robotEntity.getBasicData().getTravelShop().getItemsBuilderList().stream().forEach(item->{
			if (item.hasGiftMsg()) {
				if (item.getGiftMsg().getTravelShopGiftId() == giftBuyResp.getGiftItemMsg().getTravelShopGiftId()) {
					item.setGiftMsg(giftBuyResp.getGiftItemMsg());
				}
			}
		});
		
	}

	private void doInfoResp(GameRobotEntity robotEntity, HawkProtocol protocol) {
		TravelShopInfoSync info = protocol.parseProtocol(TravelShopInfoSync.getDefaultInstance());
		if (info != null) {
			robotEntity.getBasicData().setTravelShop(info.toBuilder());
		}
		
	}

}
