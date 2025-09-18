package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;

@HawkConfigManager.XmlResource(file = "xml/alliance_BigGift.xml")
public class AllianceBigGiftCfg extends HawkConfigBase {
	@Id
	protected final int id;// ="1"
	protected final int item;// ="1"
	protected final int bigGiftExp;// ="20000"

	@Override
	protected boolean checkValid() {
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item);
		HawkAssert.notNull(itemCfg, " itemcfg error cfgid = " + item);
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, itemCfg.getRewardId());
		HawkAssert.notNull(awardCfg, "  error itemCfg = " + item + " awardcfg = " + itemCfg.getRewardId());
		return super.checkValid();
	}

	public AllianceBigGiftCfg() {
		this.id = 0;
		this.item = 0;
		this.bigGiftExp = 0;
	}

	public int getId() {
		return id;
	}

	public int getItem() {
		return item;
	}

	public int getBigGiftExp() {
		return bigGiftExp;
	}

}
