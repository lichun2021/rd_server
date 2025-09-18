package com.hawk.robot.action.pushgift;

import java.util.ArrayList;
import java.util.List;

import org.hawk.annotation.RobotAction;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.PushGiftBuyReq;
import com.hawk.game.protocol.PushGift.PushGiftMsg;
import com.hawk.game.protocol.PushGift.PushGiftNotifyDue;
import com.hawk.robot.GameRobotEntity;

@RobotAction(valid = true)
public class PushGiftAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity entity) {
		GameRobotEntity gameEntity = (GameRobotEntity)(entity);
		PushGiftTask task = EnumUtil.random(PushGiftTask.class);
		switch (task) {
		case BUY:
			doBuy(gameEntity);
			break;
		case DUE:
			doDue(gameEntity);
			break;
		}
	}
	
	private void doDue(GameRobotEntity entity) {
		boolean normal = HawkRand.randInt(0, 10000) > 8000;
		List<Integer> dueIdList = new ArrayList<Integer>();
		if (normal) {
			long now = HawkTime.getMillisecond();
			List<PushGiftMsg> pushGiftList = entity.getData().getBasicData().getPushGiftList();
			for (PushGiftMsg pushGiftMsg : pushGiftList) {
				if (pushGiftMsg.getEndTime() < now) {
					dueIdList.add(pushGiftMsg.getGiftId());
				}
			}
		} else {
			dueIdList.add(HawkRand.randInt());
		}
		
		if (!dueIdList.isEmpty()) {
			PushGiftNotifyDue.Builder dueBuilder = PushGiftNotifyDue.newBuilder();
			dueBuilder.addAllGiftIds(dueIdList);
			
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PUSH_GIFT_NOTIFY_DUE_VALUE, dueBuilder);
			entity.sendProtocol(protocol);
		}
	}

	private void doBuy(GameRobotEntity entity) {
		boolean normal = HawkRand.randInt(0, 10000) > 8000;
		int giftId = 0;
		if (normal) {
			List<PushGiftMsg> pushGiftList = entity.getData().getBasicData().getPushGiftList();
			long now = HawkTime.getMillisecond();
			for (PushGiftMsg builder : pushGiftList) {
				if (builder.getEndTime() > now) {
					giftId = builder.getGiftId();
				}
			}
		} else {
			boolean flag = HawkRand.randInt(0, 5000) > 5000;
			if (flag) {
				List<PushGiftMsg> pushGiftList = entity.getData().getBasicData().getPushGiftList();
				giftId = pushGiftList.get(HawkRand.randInt(0, pushGiftList.size() - 1)).getGiftId();
			} else {
				giftId = HawkRand.randInt(1, 10000);
			}
			
		}
		
		if (giftId != 0) {
			PushGiftBuyReq.Builder reqBuilder = PushGiftBuyReq.newBuilder();
			reqBuilder.setLevelId(giftId);
			
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PUSH_GIFT_BUY_REQ_VALUE, reqBuilder);
			entity.sendProtocol(protocol);
		}
		
	}

	static enum PushGiftTask {
		DUE,
		BUY;
	}   
}
