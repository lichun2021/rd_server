package com.hawk.robot.action.item;

import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.TakeBenefitBoxReq;
import com.hawk.game.protocol.Item.VipExclusiveBoxBuyReq;
import com.hawk.game.protocol.Item.VipShopBuyReq;
import com.hawk.game.protocol.Item.VipShopItem;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.annotation.RobotAction;

/**
 * 
 * vip贵族商城操作
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class VipShopOperateAction extends HawkRobotAction {
	
	public static enum Type {
		VIP_SHOP_BUY,
		VIP_EXCLUSIVE_BOX_BUY,
		VIP_BENEFIT_BOX_TAKE
	}
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		Type reqType = EnumUtil.random(Type.class);
		switch (reqType) {
		case VIP_SHOP_BUY:
			buyVipShopGoods(robot);
			break;
		case VIP_EXCLUSIVE_BOX_BUY:
			buyExclusiveGift(robot);
			break;
		case VIP_BENEFIT_BOX_TAKE:
			receiveBenefitBoxAward(robot);
			break;
		}
	}
	
	/**
	 * 购买vip商城物品
	 * 
	 * @param robot
	 */
	private void buyVipShopGoods(GameRobotEntity robot) {
		Collection<VipShopItem> vipShopItems = robot.getBasicData().getVipShopItems();
		
		List<VipShopItem> list = new ArrayList<VipShopItem>();
		for (VipShopItem shopItem : vipShopItems) {
			if (shopItem.getVipLevel() > robot.getBasicData().getPlayerInfo().getVipLevel()) {
				continue;
			}
			
			if (shopItem.getRemainBuyTimes() <= 0) {
				continue;
			}
			
			list.add(shopItem);
		}
		
		if (list.isEmpty()) {
			return;
		}
		
		int index = HawkRand.randInt(0, list.size() - 1);
		VipShopItem shopItem = list.get(index);
		
		VipShopBuyReq.Builder builder = VipShopBuyReq.newBuilder();
		builder.setShopId(shopItem.getShopId());
		int count = HawkRand.randInt(1, shopItem.getRemainBuyTimes());
		builder.setCount(count);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_SHOP_BUY_C_VALUE, builder));
		RobotLog.cityPrintln("robot buy vipShopGoods, playerId: {}, shopId: {}, count: {}", robot.getPlayerId(), shopItem.getShopId(), count);
	}
	
	/**
	 * 购买vip专属礼包
	 * 
	 * @param robot
	 */
	private void buyExclusiveGift(GameRobotEntity robot) {
		Map<Integer, Boolean> map = robot.getBasicData().getVipExclusiveBoxStates();
		if (map.isEmpty()) {
			return;
		}
		
		for (Entry<Integer, Boolean> entry : map.entrySet()) {
			// 此vip等级专属礼包已购买过
			if (entry.getValue()) {
				continue;
			}
			
			if (entry.getKey() > robot.getBasicData().getPlayerInfo().getVipLevel()) {
				continue;
			}
			
			VipExclusiveBoxBuyReq.Builder builder = VipExclusiveBoxBuyReq.newBuilder();
			builder.setVipLevel(entry.getKey());
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_EXCLUSIVE_BOX_BUY_C_VALUE, builder));
			RobotLog.cityPrintln("robot buy vipExclusiveGift, playerId: {}, vipLevel: {}", robot.getPlayerId(), entry.getKey());
			break;
		}
	}
	
	/**
	 *  领取vip福利礼包
	 *  
	 * 	@param robot
	 */
	private void receiveBenefitBoxAward(GameRobotEntity robot) {
		// 当前vip等级福利礼包当天奖励还未领取过
		if (!robot.getBasicData().getCurLevelBenefitBoxState()) {
			TakeBenefitBoxReq.Builder builder = TakeBenefitBoxReq.newBuilder();
			builder.setVipLevel(robot.getBasicData().getPlayerInfo().getVipLevel());
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_BENEFIT_BOX_TAKE_C_VALUE, builder));
			return;
		}
		
		List<Integer> list = robot.getBasicData().getUnreceivedBenefitBoxes();
		if (list.isEmpty()) {
			return;
		}
		
		TakeBenefitBoxReq.Builder builder = TakeBenefitBoxReq.newBuilder();
		int level = list.get(list.size() / 2);
		builder.setVipLevel(level);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_BENEFIT_BOX_TAKE_C_VALUE, builder));
		RobotLog.cityPrintln("robot receive vipBenefit box award, playerId: {}, vipLevel: {}", robot.getPlayerId(), level);
		return;
	}
	
}
