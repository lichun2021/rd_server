package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkRandObj;

import com.google.common.collect.ImmutableList;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/dyzz_rogue_pool.xml")
public class DYZZRoguePoolCfg extends HawkConfigBase implements HawkRandObj{
	@Id
	private final int id;// id="1"
	private final int quality;// ="2"
	private final String category;// ="1,2,3"
	private final int weight;// ="10"
	private final int maxRogue;// ="3"
	private final String exclusions;// ="2" />

	private List<Integer> exclusionList = ImmutableList.of();
	private List<Integer> categoryList = ImmutableList.of();

	public DYZZRoguePoolCfg() {
		id = 1;
		quality = 10000;
		category = "43_42";
		weight = 1;
		maxRogue = 0;
		exclusions = "";
	}

	@Override
	protected boolean checkValid() {
		HawkAssert.isTrue(weight > 0);
		return super.checkValid();
	}

	@Override
	protected boolean assemble() {
		if (StringUtils.isNotEmpty(exclusions)) {
			exclusionList = ImmutableList.copyOf(SerializeHelper.stringToList(Integer.class, exclusions, SerializeHelper.BETWEEN_ITEMS));
		}
		if (StringUtils.isNotEmpty(category)) {
			categoryList = ImmutableList.copyOf(SerializeHelper.stringToList(Integer.class, category, SerializeHelper.ATTRIBUTE_SPLIT));
		}
		return super.assemble();
	}

	public List<Integer> getExclusionList() {
		return exclusionList;
	}

	public void setExclusionList(List<Integer> exclusionList) {
		this.exclusionList = exclusionList;
	}

	public List<Integer> getCategoryList() {
		return categoryList;
	}

	public void setCategoryList(List<Integer> categoryList) {
		this.categoryList = categoryList;
	}

	public int getId() {
		return id;
	}

	public int getQuality() {
		return quality;
	}

	public String getCategory() {
		return category;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public int getMaxRogue() {
		return maxRogue;
	}

	public String getExclusions() {
		return exclusions;
	}

}
