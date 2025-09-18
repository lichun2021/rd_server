package com.hawk.robot.action.item;

import java.util.Collections;
import java.util.List;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.config.ItemCfg;
import com.hawk.robot.util.ClientUtil;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.LimitType;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.Item.HPItemUseByItemIdReq;
import com.hawk.game.protocol.Item.HPItemUseReq;
import com.hawk.game.protocol.Item.ItemInfo;

/**
 * 
 * 使用道具
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class PlayerItemUseAction extends HawkRobotAction {
	

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		List<ItemInfo> itemList = robot.getItemObjects();
		if(itemList.size() <= 0) {
			return;
		}
		
		Collections.shuffle(itemList);
		for (ItemInfo itemInfo : itemList) {
			if (itemInfo.getCount() <= 0) {
				continue;
			}
			
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
			if (itemCfg == null || itemCfg.getUse() == 0 || robot.getCityLevel() < itemCfg.getCityLevel()) {
				continue;
			}
			
			// 免战道具在有出征队列时不能使用（采集除外）
			if (itemCfg.getItemType() == Const.ToolType.STATUS_VALUE && itemCfg.getEffect() == Const.EffType.CITY_SHIELD_VALUE && ClientUtil.hasOffensiveMarch(robot)) {
				continue;
			}
			
			String targetId = "";
			if (itemCfg.getItemType() == Const.ToolType.STATUS_VALUE && ClientUtil.isResProduceUpEffect(itemCfg.getEffect())) {
				List<BuildingPB> resBuildings = robot.getBuildingListByLimitType(LimitType.LIMIT_TYPE_BUIDING_STEEL, LimitType.LIMIT_TYPE_BUIDING_ORE_REFINING, 
						LimitType.LIMIT_TYPE_BUIDING_OIL_WELL, LimitType.LIMIT_TYPE_BUIDING_RARE_EARTH);
				if (resBuildings.size() > 0) {
					BuildingPB resBuild = resBuildings.get(HawkRand.randInt(resBuildings.size() - 1));
					targetId = resBuild.getId();
				}
			}
			
			if(HawkRand.randPercentRate(50)) {
				useItemByItemId(robot, itemInfo, itemCfg, targetId);
			} else {
				useItemByUUID(robot, itemInfo, itemCfg, targetId);
			}
			
			break;
		}
	}
	
	/**
	 * 通过itemId使用道具
	 * @param robot
	 * @param item
	 * @param itemCfg
	 * @param targetId
	 */
	private void useItemByItemId(GameRobotEntity robot, ItemInfo item, ItemCfg itemCfg, String targetId) {
		HPItemUseByItemIdReq.Builder builder = HPItemUseByItemIdReq.newBuilder();
		builder.setItemId(item.getItemId());
		builder.setTargetId(targetId);
		int count = itemCfg.getUseAll() == 0 ? 1 : HawkRand.randInt(item.getCount() - 1) + 1;
		builder.setItemCount(count);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_USE_BY_ITEMID_C_VALUE, builder));
		RobotLog.cityPrintln("use item by itemId, playerId: {}, itemId: {}, itemCount: {}", robot.getPlayerId(), item.getItemId(), count);
	}
	
	/**
	 * 通过entity uuid使用道具
	 * @param robot
	 * @param item
	 * @param itemCfg
	 * @param targetId
	 */
	private void useItemByUUID(GameRobotEntity robot, ItemInfo item, ItemCfg itemCfg, String targetId) {
		HPItemUseReq.Builder builder = HPItemUseReq.newBuilder();
		builder.setUuid(item.getUuid());
		builder.setTargetId(targetId);
		int count = itemCfg.getUseAll() == 0 ? 1 : HawkRand.randInt(item.getCount() - 1) + 1;
		builder.setItemCount(count);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_USE_C_VALUE, builder));
		RobotLog.cityPrintln("use item by uuid, playerId: {}, uuid: {}, itemCount: {}", robot.getPlayerId(), item.getUuid(), count);
	}
	
	/**
	 * 使用资源类道具
	 * @param robotEntity
	 * @param resType
	 */
	public static boolean useItem(GameRobotEntity robotEntity, int resType) {
		if (!ClientUtil.isExecuteAllowed(robotEntity, PlayerItemUseAction.class.getSimpleName(), 60000)) {
			return false;
		}
		
		List<ItemInfo> itemObjs = robotEntity.getItemObjects();
		boolean useItemResult = false;
		for(ItemInfo itemInfo : itemObjs) {
			if (itemInfo.getCount() <= 0) {
				continue;
			}
			
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemInfo.getItemId());
			if(itemCfg ==  null || itemCfg.getItemType() != ToolType.ADD_ATTR_VALUE || itemCfg.getAttrType() != resType || itemCfg.getUse() == 0) {
				continue;
			}
			
			if (robotEntity.getCityLevel() < itemCfg.getCityLevel()) {
				continue;
			}
			
			useItemResult = true;
			HPItemUseByItemIdReq.Builder builder = HPItemUseByItemIdReq.newBuilder();
			builder.setItemId(itemInfo.getItemId());
			builder.setItemCount(itemInfo.getCount());
			robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.ITEM_USE_BY_ITEMID_C_VALUE, builder));
			robotEntity.getCityData().getLastExecuteTime().put(PlayerItemUseAction.class.getSimpleName(), HawkTime.getMillisecond());
			RobotLog.cityPrintln("use resItem, playerId: {}, itemId: {}, itemCount: {}", robotEntity.getPlayerId(), itemInfo.getItemId(), itemInfo.getCount());
		}
		
		return useItemResult;
	}
}
