package com.hawk.activity.helper;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.ActivityManager;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.gamelib.GameConst;

public class RewardHelper {

	/**
	 * 字符串转奖励对象列表
	 * 
	 * @param itemStr
	 * @deprecated 返回对象为LinkedList. 严禁做为cfg文件的类变量直接引用 
	 * replaced by <code>RewardHelper.toRewardItemImmutableList(String s)</code>.
	 * @return
	 */
	@Deprecated
	public static List<RewardItem.Builder> toRewardItemList(String itemStr) {
		List<RewardItem.Builder> itemList = new LinkedList<>();
		if (itemStr == null || itemStr.equals("")) {
			return itemList;
		}
		String[] itemArray = itemStr.split(",");
		for (String item : itemArray) {
			RewardItem.Builder builder = toRewardItem(item);
			if (builder != null) {
				itemList.add(builder);
			}
		}
		return itemList;
	}
	
	/**
	 * 字符串转奖励对象列表
	 * 
	 * @param itemStr
	 * @return
	 */
	public static ImmutableList<RewardItem.Builder> toRewardItemImmutableList(String itemStr) {
		return ImmutableList.copyOf(toRewardItemList(itemStr));
	}

	public static void multiCeilItemList(List<RewardItem.Builder> list, double multi){
		for (RewardItem.Builder item : list) {
			item.setItemCount((long)Math.ceil(item.getItemCount() * multi));
		}
	}
	
	/**
	 * 字符串转奖励对象
	 * 
	 * @param itemStr
	 * @return
	 */
	public static Builder toRewardItem(String itemStr) {
		if (itemStr != null && itemStr.length() > 0 && !itemStr.equals("0") && !itemStr.equals("none")) {
			String[] items = itemStr.split("_");
			if (items.length < 3) {
				return null;
			}
			int type = Integer.parseInt(items[0]);
			int itemId = Integer.parseInt(items[1]);
			int count = Integer.parseInt(items[2]);
			RewardItem.Builder builder = RewardItem.newBuilder();
			builder.setItemType(type);
			builder.setItemId(itemId);
			builder.setItemCount(count);		
			
			return builder;
		}
		return null;
	}
	
	public static void checkRewardItem(List<RewardItem.Builder> itemList) {
		for (RewardItem.Builder rewardBuilder : itemList) {
			checkRewardItem(rewardBuilder.getItemType(), rewardBuilder.getItemId(), rewardBuilder.getItemCount());
		} 
	}
	
	public static void checkRewardItem(int itemType, int itemId, long itemCount) {
		ItemType enumItemType = ItemType.valueOf(itemType / GameConst.ITEM_TYPE_BASE);
		if (enumItemType == null) {
			throw new InvalidParameterException(String.format("error award itemType:%s, itemId:%s, itemCount:%s", itemType, itemId, itemCount));
		}
		if (itemId <= 0 || itemCount <= 0 || (enumItemType == ItemType.TOOL && itemCount > 999)) {
			throw new InvalidParameterException(String.format("error award itemType:%s, itemId:%s, itemCount:%s", itemType, itemId, itemCount)); 
		}
		
		if (enumItemType == ItemType.TOOL) {
			if (!ActivityManager.getInstance().getDataGeter().isExistItemId(itemId)) {
				throw new InvalidParameterException(String.format("error award itemType:%s, itemId:%s, itemCount:%s", itemType, itemId, itemCount));
			}
		}		
	}

	public static RewardItem.Builder toRewardItem(int type, int id, long count) {
		RewardItem.Builder builder = RewardItem.newBuilder();
		builder.setItemType(type);
		builder.setItemId(id);
		builder.setItemCount(count);
		return builder;
	}

	public static RewardItem.Builder toRewardItem(int id, int count) {
		RewardItem.Builder builder = RewardItem.newBuilder();
		builder.setItemType(0);
		builder.setItemId(id);
		builder.setItemCount(count);
		return builder;
	}

	public static int getNum(List<RewardItem.Builder> itemList, ItemType itemType, int itemId) {
		if (itemList == null || itemList.isEmpty()) {
			return 0;
		}

		int num = 0;
		for (RewardItem.Builder builder : itemList) {
			if (itemType.getNumber() * 10000 == builder.getItemType() && builder.getItemId() == itemId) {
				num += builder.getItemCount();
			}
		}

		return num;
	}
	
	public static String toItemString(RewardItem item) {
		return item.getItemType() + "_" + item.getItemId() + "_" + item.getItemCount();
	}
	
	/**
	 * 合并奖励
	 * @param itemList
	 * @return
	 */
	public static List<RewardItem.Builder> mergeRewardItem(List<RewardItem.Builder> itemList) {
		List<RewardItem.Builder> retList = new ArrayList<>();
		
		for (RewardItem.Builder rewardBuilder : itemList) {
			
			// 之前有则堆叠
			boolean hasBefore = false;
			for (RewardItem.Builder ret : retList) {
				if (rewardBuilder.getItemType() == ret.getItemType() && rewardBuilder.getItemId() == ret.getItemId()) {
					ret.setItemCount(ret.getItemCount() + rewardBuilder.getItemCount());
					hasBefore = true;
				}
			}
			
			if (!hasBefore) {
				retList.add(toRewardItem(rewardBuilder.getItemType(), rewardBuilder.getItemId(), rewardBuilder.getItemCount()));
			}
		} 
		
		return retList;
	}
	
}
