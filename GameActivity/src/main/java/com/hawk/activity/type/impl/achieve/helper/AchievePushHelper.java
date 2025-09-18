package com.hawk.activity.type.impl.achieve.helper;

import java.util.ArrayList;
import java.util.List;

import com.hawk.activity.constant.ObjType;
import com.hawk.activity.msg.PlayerAchieveUpdateMsg;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity.AchieveItemsInfoSync;
import com.hawk.game.protocol.HP;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

public class AchievePushHelper {

	public static void pushAchieveInfo(String playerId, List<AchieveItem> items) {
		AchieveItemsInfoSync.Builder builder = AchieveItemsInfoSync.newBuilder();
		for (AchieveItem achieveItem : items) {
			builder.addItem(achieveItem.createAchieveItemPB());
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_ACHIEVE_INFO_SYNC_S_VALUE, builder));
	}
	
	public static void pushAchieveUpdate(String playerId, AchieveItem item) {
		List<AchieveItem> items = new ArrayList<>();
		items.add(item);
		pushAchieveUpdate(playerId, items);
	}
	
	public static void pushAchieveUpdate(String playerId, List<AchieveItem> items) {
		if (items.size() <= 0) {
			return;
		}
		//System.out.println("WHC AchievePushHelper pushAchieveUpdate player:"+playerId+",items size:"+items.size());
		HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
		PlayerAchieveUpdateMsg msg = PlayerAchieveUpdateMsg.valueOf(items);
		HawkTaskManager.getInstance().postMsg(xid, msg);
//		AchieveItemsInfoSync.Builder builder = AchieveItemsInfoSync.newBuilder();
//		if (items.size() <= 0) {
//			return;
//		}
//		for (AchieveItem achieveItem : items) {
//			builder.addItem(achieveItem.createAchieveItemPB());
//		}
//		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_ACHIEVE_CHANGE_S_VALUE, builder));
	}
	
	public static void pushAchieveDelete(String playerId, AchieveItem item) {
		AchieveItemsInfoSync.Builder builder = AchieveItemsInfoSync.newBuilder();
		builder.addItem(item.createAchieveItemPB());
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_DELETE_ACHIEVE_ITEM_S_VALUE, builder));
	}
	
	public static void pushAchieveDelete(String playerId, List<AchieveItem> items) {
		if (items.size() <= 0) {
			return;
		}
		AchieveItemsInfoSync.Builder builder = AchieveItemsInfoSync.newBuilder();
		for (AchieveItem achieveItem : items) {
			builder.addItem(achieveItem.createAchieveItemPB());
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_DELETE_ACHIEVE_ITEM_S_VALUE, builder));
	}
	
	public static void pushAchieveAdd(String playerId, List<AchieveItem> items) {
		if (items.size() <= 0) {
			return;
		}
		AchieveItemsInfoSync.Builder builder = AchieveItemsInfoSync.newBuilder();
		for (AchieveItem achieveItem : items) {
			builder.addItem(achieveItem.createAchieveItemPB());
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_ADD_ACHIEVE_ITEM_S_VALUE, builder));
	}

	
	
}
