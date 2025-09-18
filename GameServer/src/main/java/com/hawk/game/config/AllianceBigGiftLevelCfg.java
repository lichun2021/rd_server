package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.util.RandomUtil;
import com.hawk.game.util.WeightAble;

@HawkConfigManager.XmlResource(file = "xml/alliance_GiftLevel.xml")
public class AllianceBigGiftLevelCfg extends HawkConfigBase {
	@Id
	protected final int level;// ="1"
	protected final int giftLevelExp;// ="100"
	protected final String bigGiftWeight;// ="1_100,2_100"

	private ImmutableList<GiftItem> gitfItemList;

	class GiftItem implements WeightAble {
		private int giftId;
		private int weight;

		@Override
		public int getWeight() {
			return weight;
		}
	}

	public AllianceBigGiftLevelCfg() {
		this.level = 0;
		this.giftLevelExp = 0;
		this.bigGiftWeight = "";
	}

	@Override
	protected boolean assemble() {
		Iterable<String> gitWeight = Splitter.on(",").omitEmptyStrings().split(bigGiftWeight);
		List<GiftItem> list = new ArrayList<>();
		for (String str : gitWeight) {
			String[] arr = str.split("_");
			GiftItem item = new GiftItem();
			item.giftId = NumberUtils.toInt(arr[0]);
			item.weight = NumberUtils.toInt(arr[1]);
			list.add(item);
		}
		this.gitfItemList = ImmutableList.copyOf(list);

		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		if (gitfItemList.isEmpty()) {
			throw new RuntimeException("alliance_GiftLevel error id=" + level);
		}
		return super.checkValid();
	}

	public int rundomBigGift() {
		return RandomUtil.random(gitfItemList).giftId;
	}

	public int getLevel() {
		return level;
	}

	public int getGiftLevelExp() {
		return giftLevelExp;
	}

	public String getBigGiftWeight() {
		return bigGiftWeight;
	}

}
