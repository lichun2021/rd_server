package com.hawk.robot.action.gift;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.hawk.annotation.RobotAction;
import org.hawk.enums.EnumUtil;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.HPBuyGiftReq;
import com.hawk.robot.GameRobotEntity;

@RobotAction(valid = true)
public class GiftAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity entity) {
		GiftActionType giftActionType = EnumUtil.random(GiftActionType.class);
		GameRobotEntity gameRobotEntity = (GameRobotEntity)entity;
		switch(giftActionType) {
		case BUY:
			doBuy(gameRobotEntity);
			break;
		}
	}
	
	private void doBuy(GameRobotEntity gameRobotEntity) {
		List<Integer> id = new ArrayList<>();		
		//随机加一点ID进去
		for (int i = 0; i < 10; i++) {
			Random random = new Random();
			id.add(random.nextInt());
		}
		
		if (gameRobotEntity.getBasicData().getGiftBuilder() != null) {			
			id.addAll(gameRobotEntity.getBasicData().getGiftBuilder().getGiftsOnSellList());
		} else {
			HawkLog.errPrintln("robot giftDataBuilder is empty playerId:{}", gameRobotEntity.getPlayerId());
		}
		
		int buyId = HawkRand.randInt(0, id.size() - 1);
		HPBuyGiftReq.Builder buyBuilder = HPBuyGiftReq.newBuilder();
		buyBuilder.setGiftId(buyId);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.BUY_GIFT_C_VALUE, buyBuilder);
		gameRobotEntity.sendProtocol(protocol);
	}

	private enum GiftActionType {
		BUY
	} 
}


