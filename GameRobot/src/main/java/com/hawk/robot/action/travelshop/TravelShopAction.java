package com.hawk.robot.action.travelshop;

import java.util.Optional;

import org.hawk.annotation.RobotAction;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.TravelShop.TravelGiftBuyC;
import com.hawk.game.protocol.TravelShop.TravelGiftItemMsg;
import com.hawk.game.protocol.TravelShop.TravelShopBuyReq;
import com.hawk.game.protocol.TravelShop.TravelShopInfoSync;
import com.hawk.game.protocol.TravelShop.TravelShopItemMsg;
import com.hawk.robot.GameRobotEntity;

@RobotAction(valid=true)
public class TravelShopAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity entity) {
		TravelShopTask travelShopTask = EnumUtil.random(TravelShopTask.class);
		GameRobotEntity robotEntity = (GameRobotEntity)(entity);
		//为空说明没有开启黑市商人.
		TravelShopInfoSync.Builder shopInfoBuilder = robotEntity.getBasicData().getTravelShop(); 
		if (shopInfoBuilder == null) {
			return;
		}
		
		switch(travelShopTask) {
		case BUY:
			doBuy(entity, shopInfoBuilder);
			break;
		case REFRESH:
			doRefresh(entity, shopInfoBuilder);
			break;
		case BUY_GIFT:
			doBuyGift(entity, shopInfoBuilder);
			break;
		default:
			break;
		}
	}

	private void doBuyGift(HawkRobotEntity entity, TravelShopInfoSync.Builder shopinfoBuilder) {
		TravelGiftItemMsg giftMsg = null;
		for (TravelShopItemMsg.Builder shopItemMsg : shopinfoBuilder.getItemsBuilderList()) {
			if (shopItemMsg.hasGiftMsg()) {
				giftMsg = shopItemMsg.getGiftMsg();
				break;
			}
		}
		
		if (giftMsg == null) {
			return;
		}
		
		int boughtNum = giftMsg.getBuyNum();
		boolean needBuy = true;
		if (boughtNum > 0) {
			needBuy = HawkRand.randInt(0, 10000) > 9500;
		}
		
		if (!needBuy) {
			return;
		}
		
		int buyId = giftMsg.getTravelShopGiftId();
		int tmpId = HawkRand.randInt(0, 1000);
		if (tmpId > 500) {
			buyId = tmpId;
		}
		
		TravelGiftBuyC.Builder builder = TravelGiftBuyC.newBuilder();
		builder.setTravelShopGiftId(buyId);
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.TRAVEL_GIFT_BUY_C_VALUE, builder);
		entity.sendProtocol(protocol);
	}

	private void doRefresh(HawkRobotEntity entity, TravelShopInfoSync.Builder shopinfoBuilder) {
		boolean needFresh;
		boolean allBought = true;
		for (TravelShopItemMsg.Builder shopItemMsg : shopinfoBuilder.getItemsBuilderList()) {
			if (shopItemMsg.getBought() <= 0) {
				allBought = false;
			}
		}
		
		if (allBought) {
			needFresh = true;
		} else {
			needFresh = HawkRand.randInt(0, 10000) > 9500;
		}
		
		if (!needFresh) {
			return;
		}
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.TRAVEL_SHOP_REFRESH_C_VALUE);
		entity.sendProtocol(protocol);		
	}

	private void doBuy(HawkRobotEntity entity, TravelShopInfoSync.Builder shopinfoBuilder) {
		boolean normal = false;
		normal = HawkRand.randInt(0, 10000) > 5000;
		int id = 0;
		if (normal) {
			Optional<TravelShopItemMsg> optional = shopinfoBuilder.getItemsList().stream().filter(item->{
				return item.getBought() <= 0;
			}).findAny();
			if (optional.isPresent()) {
				id = optional.get().getCfgId();
			}
		}else {
			int index = HawkRand.randInt(0, shopinfoBuilder.getItemsCount() - 1);
			id = shopinfoBuilder.getItemsList().get(index).getCfgId();
			
		}
		
		if (id != 0) {
			TravelShopBuyReq.Builder reqBuilder = TravelShopBuyReq.newBuilder();
			reqBuilder.setCfgId(id);
			
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.TRAVEL_SHOP_BUY_C_VALUE, reqBuilder);
			
			entity.sendProtocol(protocol);
			
			((GameRobotEntity)entity).getBasicData().setLastTravelShopBuyId(id);
		}
	}

	private static enum TravelShopTask {
		BUY,   //购买礼包
		REFRESH, //刷新礼包
		BUY_GIFT, //购买礼包.
		REQ_INFO, //请求礼包信息.
		;
	}
}
