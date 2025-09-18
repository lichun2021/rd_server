package com.hawk.activity.item;

/**
 * 随机奖励
 *
 * @author lating
 *
 */
public class OddsItemInfo {
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
	/**
	 * 概率或权重
	 */
	protected int probability;
	/**
	 * 是否稀有：1是0否
	 */
	protected int rare;

	public OddsItemInfo() {
		super();
	}

	public OddsItemInfo(int type, int itemId, int count, int probability) {
		this.type = type;
		this.itemId = itemId;
		this.count = count;
		this.probability = probability;
		this.rare = 0;
	}

	public OddsItemInfo(String info) {
		init(info);
	}

	@Override
	public OddsItemInfo clone() {
		OddsItemInfo itemInfo = new OddsItemInfo(type, itemId, count, probability);
		itemInfo.rare = this.rare;
		return itemInfo;
	}

	public static OddsItemInfo valueOf(int type, int itemId, int count, int probability) {
		return new OddsItemInfo(type, itemId, count, probability);
	}

	public boolean init(String info) {
		if (info != null && info.length() > 0 && !info.equals("0") && !info.equals("none")) {
			String[] items = info.split("_");
			if (items.length < 4) {
				return false;
			}
			this.type = Integer.parseInt(items[0]);
			this.itemId = Integer.parseInt(items[1]);
			this.count = Integer.parseInt(items[2]);
			this.probability = Integer.parseInt(items[3]);
			if (items.length > 4) {
				this.rare = Integer.parseInt(items[4]);
			}
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

	public int getType() {
		return type;
	}

	public int getItemId() {
		return itemId;
	}

	public int getCount() {
		return count;
	}

	public int getRare() {
		return rare;
	}
	
	public int getProbability() {
		return probability;
	}

}
