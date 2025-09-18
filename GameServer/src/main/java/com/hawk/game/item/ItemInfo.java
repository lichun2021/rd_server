package com.hawk.game.item;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.google.common.base.Joiner;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.gamelib.GameConst;

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
	protected long count;

	public ItemInfo() {
		super();
	}

	public ItemInfo(int type, int itemId, long count) {
		super();
		this.type = type;
		this.itemId = itemId;
		this.count = count;
		checkItemCount(count);
	}
	
	public static String toString(List<ItemInfo> items){
		return Joiner.on(",").join(items);
	}
	
	public ItemType getItemType() {
		ItemType iType = ItemType.valueOf(GameUtil.convertToStandardItemType(type) / GsConst.ITEM_TYPE_BASE);
		return iType;
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

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		checkItemCount(count);
		this.count = count;
	}

	public void addCount(long count) {
		checkItemCount(count);
//		long countAfter = this.count + count;
//		if (countAfter >= Integer.MAX_VALUE) {
//			throw new RuntimeException("item count too large, " + countAfter);
//		}
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
			count = Long.parseLong(items[2]);
			checkItemCount(count);
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
	public String toString() {
		return String.format("%d_%d_%d", GameUtil.convertToStandardItemType(type), itemId, count);
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
			return new LinkedList<>();
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

	public static List<ItemInfo> valueListOf(String info, int multi) {
		List<ItemInfo> list = valueListOf(info, ",");
		for(ItemInfo itemInfo : list){
			itemInfo.setCount(itemInfo.count * multi);
		}
		return list;
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
	public static RewardItem.Builder toRewardItem(int type, int itemId, long count) {
		RewardItem.Builder item = RewardItem.newBuilder();
		item.setItemId(itemId);
		item.setItemCount(count);
		item.setItemType(type);
		return item;
	}
	
	/**
	 * 负重
	 * @return
	 */
	public long weight() {
		return count * WorldMarchConstProperty.getInstance().getResWeightByType(itemId);
	}
	
	public static void checkValid(List<ItemInfo> itemInfoList) {
		for (ItemInfo itemInfo : itemInfoList) {
			checkItemInfo(itemInfo.getType(), itemInfo.getItemId(), itemInfo.getCount());
		}		
	}
	
	public static void checkItemInfo(int itemType, int itemId, long itemCount) {
		ItemType enumItemType = ItemType.valueOf(itemType / GameConst.ITEM_TYPE_BASE);
		if (enumItemType == null) {
			throw new InvalidParameterException(String.format("error award itemType:%s, itemId:%s, itemCount:%s", itemType, itemId, itemCount));
		}
		if (itemId <= 0 || itemCount <= 0 || (enumItemType == ItemType.TOOL && itemCount > ConstProperty.getInstance().getMaxAddItemNum())) {
			throw new InvalidParameterException(String.format("error award itemType:%s, itemId:%s, itemCount:%s", itemType, itemId, itemCount)); 
		}
		
		if (enumItemType == ItemType.TOOL) {
			if (!ItemCfg.isExistItemId(itemId)) {
				throw new InvalidParameterException(String.format("error award itemType:%s, itemId:%s, itemCount:%s", itemType, itemId, itemCount));
			}
		}		
	}
	
	/**
	 * ItemInfo 数量校验
	 * @param count
	 */
	private void checkItemCount(long count) {
		if (count < 0) {
			throw new RuntimeException("itemInfo count error!");
		}
		
	}
}
