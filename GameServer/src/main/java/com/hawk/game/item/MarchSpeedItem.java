package com.hawk.game.item;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MarchSpeedItem extends ItemInfo{
	protected String playerId;
	
	/**
	 * 构造
	 */
	public MarchSpeedItem() {
		super();
	}
	
	public MarchSpeedItem(ItemInfo item, String playerId) {
        super(item.getType(), item.getItemId(), item.getCount());
        this.playerId = playerId;
    }
	public MarchSpeedItem(int type, int itemId, long count, String playerId) {
		super(type, itemId, count);
		this.playerId = playerId;
	}
	
	public MarchSpeedItem(String speedInfo) {
		init(speedInfo);
	}
	
	
	@Override
	public boolean init(String speedInfo) {
		if (speedInfo != null && speedInfo.length() > 0 && !speedInfo.equals("0") && !speedInfo.equals("none")) {
			String[] speedItems = speedInfo.split("_");
			if (speedItems.length < 4) {
				return false;
			}

			type = Integer.parseInt(speedItems[0]);
			itemId = Integer.parseInt(speedItems[1]);
			count = Long.parseLong(speedItems[2]);
			playerId = speedItems[3];
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("%d_%d_%d_%s", type, itemId, count, playerId);
	}
	
	@Override
	public MarchSpeedItem clone() {
		MarchSpeedItem speedInfo = new MarchSpeedItem(type, itemId, count, playerId);
		return speedInfo;
	}
	
	/**
	 * 格式化行军加速字符串
	 * @param speedInfo
	 * @return
	 */
	public static MarchSpeedItem formatOf(String speedInfoString) {
		MarchSpeedItem speedInfo = new MarchSpeedItem();
		if (speedInfo.init(speedInfoString)) {
			return speedInfo;
		}
		return null;
	}
	
	/**
	 * 格式化行军加速数组
	 * @param missions
	 * @return
	 */
	public static List<MarchSpeedItem> formatListOf(String speedInfoString) {
		List<MarchSpeedItem> speedInfoList = new LinkedList<>();
		String[] speedArray = speedInfoString.split(",");
		for (String item : speedArray) {
			MarchSpeedItem speedInfo = MarchSpeedItem.formatOf(item);
			if (speedInfo != null) {
				speedInfoList.add(speedInfo);
			}
		}
		return speedInfoList;
	}

	/**
	 * 行军加速列表转String
	 * @return
	 */
	public static String speedListToStr(List<MarchSpeedItem> speedInfo) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < speedInfo.size(); i++) {
			if (i > 0) {
				result.append(",");
			}
			result.append(speedInfo.get(i).toString());
		}
		return result.toString();
	}
	
	/**
	 * 合并同类型行军加速
	 * @param speedInfos
	 */
	public static List<MarchSpeedItem> mergeSpeedList(List<MarchSpeedItem> speedInfos) {
		List<MarchSpeedItem> mergeList = new ArrayList<MarchSpeedItem>();
		for (MarchSpeedItem speedInfo : speedInfos) {
			if (mergeList.size() > 0) {
				for (MarchSpeedItem mergeInfo : mergeList) {
					if (isSameSpeedType(speedInfo, mergeInfo)) {
						mergeInfo.setCount(mergeInfo.getCount() + speedInfo.getCount());
					} else {
						mergeList.add(speedInfo);
						break;
					}
				}
			} else {
				mergeList.add(speedInfo);
			}
		}
		return mergeList;
	}
	
	/**
	 * 两个行军加速类型是否相等
	 * @param speedInfo1
	 * @param speedInfo2
	 * @return
	 */
	public static boolean isSameSpeedType(MarchSpeedItem speedInfo1, MarchSpeedItem speedInfo2) {
		return speedInfo1.getType() == speedInfo2.getType() && speedInfo1.getItemId() == speedInfo2.getItemId()
				&& speedInfo1.getPlayerId().equals(speedInfo2.getPlayerId());
	}

	/**
	 * 获取加速消耗物品
	 * @param speedInfo
	 * @return
	 */
	public static List<ItemInfo> getItemInfo(MarchSpeedItem speedInfo) {
		List<ItemInfo> itemInfos = new ArrayList<ItemInfo>();
		itemInfos.add(new ItemInfo(speedInfo.getType(), speedInfo.getItemId(), speedInfo.getCount()));
		return itemInfos;
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
}
