package com.hawk.robot.action.item;

import java.util.Collections;
import java.util.List;
import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.HPBuyGiftReq;

/**
 * 
 * 购买礼包
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class PlayerBuyGiftAction extends HawkRobotAction {
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		long giftBuyDuration = GameRobotApp.getInstance().getConfig().getInt("giftBuyDuration") * 1000L;
		if (!ClientUtil.isExecuteAllowed(robot, PlayerBuyGiftAction.class.getSimpleName(), giftBuyDuration)) {
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.GIFT_SYNC_C));
			return;
		}
		
		robot.getCityData().getLastExecuteTime().put(PlayerBuyGiftAction.class.getSimpleName(), HawkTime.getMillisecond());
		if(HawkRand.randPercentRate(50)) {
			List<Integer> giftIdList = robot.getBasicData().getOnSellGifts();
			if (giftIdList.isEmpty()) {
				robot.sendProtocol(HawkProtocol.valueOf(HP.code.GIFT_SYNC_C));
				return;
			}
			Collections.shuffle(giftIdList);
			HPBuyGiftReq.Builder builder = HPBuyGiftReq.newBuilder();
			builder.setGiftId(giftIdList.get(0));
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.BUY_GIFT_C_VALUE, builder));
		} else {
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.GIFT_SYNC_C));
		}
	}
}
