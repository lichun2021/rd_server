package com.hawk.game.item;

/**
 * 几率奖励
 *
 * @author lating
 *
 */
public class OddsItemInfo extends ItemInfo {
	/**
	 * 概率或权重
	 */
	protected int probability;

	public OddsItemInfo() {
		super();
	}

	public OddsItemInfo(int type, int itemId, long count, int probability) {
		super(type, itemId, count);
		this.probability = probability;
	}

	public OddsItemInfo(String info) {
		init(info);
	}

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	@Override
	public OddsItemInfo clone() {
		OddsItemInfo itemInfo = new OddsItemInfo(type, itemId, count, probability);
		return itemInfo;
	}

	public static OddsItemInfo valueOf(int type, int itemId, long count, int probability) {
		return new OddsItemInfo(type, itemId, count, probability);
	}

	public boolean init(String info) {
		if (info != null && info.length() > 0 && !info.equals("0") && !info.equals("none")) {
			String[] items = info.split("_");
			if (items.length < 4) {
				return false;
			}
			type = Integer.parseInt(items[0]);
			itemId = Integer.parseInt(items[1]);
			count = Long.parseLong(items[2]);
			probability = Integer.parseInt(items[3]);
			return true;
		}
		return false;
	}

	public static OddsItemInfo valueOf(String info) {
		OddsItemInfo itemInfo = new OddsItemInfo();
		if (itemInfo.init(info)) {
			return itemInfo;
		}
		return null;
	}

}
