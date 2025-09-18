package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/push_gift_level.xml")
public class PushGiftLevelCfg extends HawkConfigBase implements Comparable<PushGiftLevelCfg> {
	/**
	 * 唯一ID
	 */
	@Id
	protected final int id;
	/**
	 * 组ID
	 */
	protected final int groupId;
	/**
	 * 优先级
	 */
	protected final int level;
	/**
	 * 触发阈值参数
	 */
	protected final String param;
	/**
	 * 价格
	 */
	protected final String price;
	/**
	 * 特殊奖励
	 */
	protected final String specialReward;
	/**
	 * 普通奖励
	 */
	protected final String ordinaryReward;
	/**
	 * 水晶返还
	 */
	protected final String crystalReward;
	/** 联盟礼物 */
	protected final int allianceGift;
	/**
	 * iosPayId
	 */
	private final String iosPayId;
	/**
	 * 安卓的payId
	 */
	private final String androidPayId;

	private List<ItemInfo> priceList;
	private List<ItemInfo> specialRewardList;
	private List<ItemInfo> ordinaryRewardList;
	private List<ItemInfo> crystalRewardList;
	private List<Integer> paramList;

	public PushGiftLevelCfg() {
		id = 0;
		groupId = 0;
		level = 0;
		param = "";
		price = "";
		specialReward = "";
		ordinaryReward = "";
		crystalReward = "";
		allianceGift = 0;
		this.iosPayId = "";
		this.androidPayId = "";
	}

	public int getId() {
		return id;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getLevel() {
		return level;
	}

	public String getParam() {
		return param;
	}

	public String getPrice() {
		return price;
	}

	public String getSpecialReward() {
		return specialReward;
	}

	public String getOrdinaryReward() {
		return ordinaryReward;
	}

	public String getCrystalReward() {
		return crystalReward;
	}

	public List<ItemInfo> getPriceList() {
		return priceList;
	}

	public void setPriceList(List<ItemInfo> priceList) {
		this.priceList = priceList;
	}

	public List<ItemInfo> getSpecialRewardList() {
		return specialRewardList;
	}

	public void setSpecialRewardList(List<ItemInfo> specialRewardList) {
		this.specialRewardList = specialRewardList;
	}

	public List<ItemInfo> getOrdinaryRewardList() {
		return ordinaryRewardList;
	}

	public void setOrdinaryRewardList(List<ItemInfo> ordinaryRewardList) {
		this.ordinaryRewardList = ordinaryRewardList;
	}

	public List<ItemInfo> getCrystalRewardList() {
		return crystalRewardList;
	}

	public void setCrystalRewardList(List<ItemInfo> crystalRewardList) {
		this.crystalRewardList = crystalRewardList;
	}

	@Override
	public boolean assemble() {
		this.ordinaryRewardList = ItemInfo.valueListOf(ordinaryReward);
		this.specialRewardList = ItemInfo.valueListOf(specialReward);
		this.crystalRewardList = ItemInfo.valueListOf(crystalReward);
		this.priceList = ItemInfo.valueListOf(price);
		this.paramList = SerializeHelper.stringToList(Integer.class, param, SerializeHelper.ATTRIBUTE_SPLIT);
		return true;
	}

	@Override
	public boolean checkValid() {
		ItemInfo.checkValid(specialRewardList);
		ItemInfo.checkValid(crystalRewardList);
		ItemInfo.checkValid(ordinaryRewardList);
		if (allianceGift > 0) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, allianceGift);
			HawkAssert.notNull(itemCfg, " itemcfg error cfgid = " + allianceGift);
			AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, itemCfg.getRewardId());
			HawkAssert.notNull(awardCfg, " awardcfg error cfgid = " + itemCfg.getRewardId());
		}
		return true;
	}

	@Override
	public int compareTo(PushGiftLevelCfg o) {
		return this.level - o.level;
	}

	public List<Integer> getParamList() {
		return paramList;
	}

	public void setParamList(List<Integer> paramList) {
		this.paramList = paramList;
	}

	public int getAllianceGift() {
		return allianceGift;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}
}
