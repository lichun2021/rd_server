package com.hawk.robot.config.element;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 物品定义结构体
 *
 * @author hawk
 *
 */
public class ItemInfo {
	/**
	 * 种类
	 */
	protected int type;
	/**
	 * id
	 */
	protected int itemId;
	/**
	 * 数量
	 */
	protected int count;

	public ItemInfo() {
		super();
	}

	public ItemInfo(int type, int itemId, int count) {
		super();
		this.type = type;
		this.itemId = itemId;
		this.count = count;
	}

	public ItemInfo(String info) {
		init(info);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void addCount(int count) {
		this.count += count;
	}

	public boolean init(String info) {
		if (info != null && info.length() > 0 && !info.equals("0") && !info.equals("none")) {
			String[] items = info.split("_");
			if (items.length < 3) {
				return false;
			}

			type = Integer.parseInt(items[0]);
			itemId = Integer.parseInt(items[1]);
			count = Integer.parseInt(items[2]);
			return true;
		}
		return false;
	}

	/**
	 * 物品类型检测
	 * 
	 * @return
	 */
	public boolean checkItemInfo() {
		// TODO 根据类型进行管理判断
		return true;
	}

	@Override
	public ItemInfo clone() {
		ItemInfo itemInfo = new ItemInfo(type, itemId, count);
		return itemInfo;
	}

	public static ItemInfo valueOf(String info) {
		ItemInfo itemInfo = new ItemInfo();
		if (itemInfo.init(info)) {
			return itemInfo;
		}
		return null;
	}
	
	public static List<ItemInfo> valueListOf(String info, String splitor) {
		if(HawkOSOperator.isEmptyString(info)){
			return Collections.emptyList();
		}
		
		List<ItemInfo> itemList = new LinkedList<>();
		String[] itemArray = info.split(splitor);
		for (String item : itemArray) {
			ItemInfo itemInfo = ItemInfo.valueOf(item);
			if (itemInfo != null) {
				itemList.add(itemInfo);
			}
		}
		return itemList;
	}
	
	public static List<ItemInfo> valueListOf(String info) {
		return valueListOf(info, ",");
	}

	/**
	 * 转换为协议builder
	 * 
	 * @return
	 */
	public RewardItem.Builder toRewardItem() {
		return ItemInfo.toRewardItem(type, itemId, count);
	}

	/**
	 * 转换为协议builder
	 * 
	 * @param type
	 * @param itemId
	 * @param count
	 * @return
	 */
	public static RewardItem.Builder toRewardItem(int type, int itemId, int count) {
		RewardItem.Builder item = RewardItem.newBuilder();
		item.setItemId(itemId);
		item.setItemCount(count);
		item.setItemType(type);
		return item;
	}
	
}
