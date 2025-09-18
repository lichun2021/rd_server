package com.hawk.robot.action.item;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.config.ConstProperty;
import com.hawk.robot.config.ItemCfg;
import com.hawk.robot.config.ShopCfg;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Item.HPItemBuyAndUseReq;
import com.hawk.game.protocol.Item.HPItemBuyReq;

/**
 * 
 * 购买道具
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class PlayerItemBuyAction extends HawkRobotAction {
	
	public static enum Type {
		ITEM_BUY_C,
		BUY_AND_USE,
		SYNC_ITEM_BUY
	}
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		Type reqType = EnumUtil.random(Type.class);
		switch (reqType) {
		case ITEM_BUY_C:
			buyItemByShopId(robot);
			break;
		case BUY_AND_USE:
			buyVit(robot);
			break;
		case SYNC_ITEM_BUY:
			syncItemBuyReq(robot);
			break;
		}
	}
	
	/**
	 * 通过shopId购买道具
	 * 
	 * @param robot
	 */
	private void buyItemByShopId(GameRobotEntity robot) {
		HPItemBuyReq.Builder builder = HPItemBuyReq.newBuilder();
		List<Integer> shopIdList = ShopCfg.getShopIdList();
		Collections.shuffle(shopIdList);
		Optional<Integer> op = shopIdList.stream().filter(e -> shopBuyCheck(robot, e)).findAny();
		if (!op.isPresent()) {
			return;
		}
		
		ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, op.get());
		builder.setShopId(op.get());
		int itemBuyRandomRange = GameRobotApp.getInstance().getConfig().getInt("itemBuyRandomRange");
		itemBuyRandomRange = HawkRand.randInt(1, itemBuyRandomRange - 1);
		builder.setItemCount(itemBuyRandomRange > shopCfg.getMaxBuyTimes() ? shopCfg.getMaxBuyTimes() : itemBuyRandomRange);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_BUY_C_VALUE, builder));
		RobotLog.cityPrintln("buy item action, playerId: {}, shopId: {}, count: {}", robot.getPlayerId(), shopIdList.get(0), itemBuyRandomRange);
	}
	
	/**
	 * 购买体力
	 * 
	 * @param robot
	 */
	public static synchronized void buyVit(GameRobotEntity robot) {
		int vitCountOnce = ConstProperty.getInstance().getBuyEnergyAdd();
		int actualCount = (ConstProperty.getInstance().getActualVitLimit() - robot.getVit()) / vitCountOnce;
		if (actualCount == 0 && ConstProperty.getInstance().getActualVitLimit() - robot.getVit() > 0) {
			actualCount = 1;
		}
		
		if (actualCount <= 0) {
			return;
		}
		
		HPItemBuyAndUseReq.Builder builder = HPItemBuyAndUseReq.newBuilder();
		builder.setItemId(PlayerAttr.VIT_VALUE);
		builder.setItemCount(actualCount);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.BUY_AND_USE_C_VALUE, builder));
		RobotLog.cityPrintln("buy vit action, playerId: {},  buyCount: {}, player vit: {}", robot.getPlayerId(), actualCount, robot.getVit());
	}
	
	/**
	 *  商品购买记数
	 *  
	 * 	@param robot
	 */
	private void syncItemBuyReq(GameRobotEntity robot) {
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.SYNC_ITEM_BUY_C_VALUE));
		RobotLog.cityPrintln("sync buy item info action, playerId: {}", robot.getPlayerId());
	}
	
	/**
	 * 购买资源类道具
	 * @param resType
	 */
	public static synchronized void buyResItem(GameRobotEntity robot, int resType) {
		if (!ClientUtil.isExecuteAllowed(robot, PlayerItemBuyAction.class.getSimpleName(), 60000)) {
			return;
		}
		
		List<Integer> itemIds = ItemCfg.getItemCfgIdByResType(resType);
		ShopCfg shopCfg = ShopCfg.getShopCfgByItemList(itemIds);
		if(shopCfg == null) {
			return;
		}
		
		HPItemBuyReq.Builder builder = HPItemBuyReq.newBuilder();
		int itemBuyRandomRange = GameRobotApp.getInstance().getConfig().getInt("itemBuyRandomRange");
		itemBuyRandomRange = HawkRand.randInt(itemBuyRandomRange);
		builder.setShopId(shopCfg.getId());
		builder.setItemCount(itemBuyRandomRange);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_BUY_C_VALUE, builder));
		robot.getCityData().getLastExecuteTime().put(PlayerItemBuyAction.class.getSimpleName(), HawkTime.getMillisecond());
		RobotLog.cityPrintln("buy resItem action, playerId: {}, shopId: {}, count: {}", robot.getPlayerId(), shopCfg.getId(), itemBuyRandomRange);
	}
	
	/**
	 * 购买一般道具
	 * @param robot
	 * @param itemId
	 */
	public static synchronized void buyItem(GameRobotEntity robot, int itemId, int count) {
		ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(itemId);
		if(shopCfg == null) {
			return;
		}
		
		HPItemBuyReq.Builder builder = HPItemBuyReq.newBuilder();
		builder.setShopId(shopCfg.getId());
		builder.setItemCount(count);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_BUY_C_VALUE, builder));
		RobotLog.cityPrintln("buy item action, playerId: {}, shopId: {}, count: {}", robot.getPlayerId(), shopCfg.getId(), count);
	}
	
	/**
	 * 判断商品是否可出售
	 * @param robot
	 * @param shopId
	 * @return
	 */
	private boolean shopBuyCheck(GameRobotEntity robot, int shopId) {
		ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, shopId);
		// 商品配置不存在
		if (shopCfg == null) {
			return false;
		}

		// 商品当前不可出售
		if (shopCfg.getIsUse() == 0) {
			return false;
		}

		// 玩家大本等级不够无法购买此商品
		if (robot.getCityLevel() < shopCfg.getBuyLV()) {
			return false;
		}
		
		return true;
	}
}
