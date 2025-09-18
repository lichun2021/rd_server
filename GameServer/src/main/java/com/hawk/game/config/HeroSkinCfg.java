package com.hawk.game.config;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@HawkConfigManager.XmlResource(file = "xml/hero_skin.xml")
@HawkConfigBase.CombineId(fields = { "skinId", "step" })
public class HeroSkinCfg extends HawkConfigBase {
	private final int skinId;// ="40001" 对应buff表 effectId.
	private final int step;
	private final int type; // 1 活动皮肤, 2 专属
	private final int heroId;// ="1006"
	private final String attrSkin;// ="101_100|102_100|103_100"
	private final double powerSkin;// ="2000"
	private final String unlockItem;// ="10000_1001_100"
	private final String stepUpItem;// ="10000_1001_100"
	private final double luckRate;// ="1.5"
	private ImmutableMap<Integer, Integer> attrMap;
	
	//配置即限定开启时间
	protected final String showTime;
	private long showTimeValue;

	public HeroSkinCfg() {
		this.skinId = 0;
		this.heroId = 0;
		this.attrSkin = "10000_1001_100";
		unlockItem = "10000_1001_100";
		stepUpItem = "10000_1001_100";
		luckRate = 1;
		step = 1;
		type = 1;
		powerSkin = 0;
		showTime = "";
	}

	@Override
	protected boolean assemble() {
		List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(attrSkin);
		Map<Integer, Integer> map = Maps.newHashMapWithExpectedSize(3);
		for (String str : attrs) {
			String[] arr = Splitter.on("_").omitEmptyStrings().splitToList(str).toArray(new String[2]);
			map.put(NumberUtils.toInt(arr[0]), NumberUtils.toInt(arr[1]));
		}
		this.attrMap = ImmutableMap.copyOf(map);
		this.showTimeValue = StringUtils.isEmpty(showTime) ? 0L : HawkTime.parseTime(showTime);
		return super.assemble();
	}

	public ImmutableMap<Integer, Integer> getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(ImmutableMap<Integer, Integer> attrMap) {
		throw new UnsupportedOperationException();
	}

	public int getSkinId() {
		return skinId;
	}

	public int getHeroId() {
		return heroId;
	}

	public String getAttrSkin() {
		return attrSkin;
	}

	public double getPowerSkin() {
		return powerSkin;
	}

	public int getStep() {
		return step;
	}

	public int getType() {
		return type;
	}

	public String getUnlockItem() {
		return unlockItem;
	}

	public String getStepUpItem() {
		return stepUpItem;
	}

	public double getLuckRate() {
		return luckRate;
	}

	public long getShowTimeValue() {
		return showTimeValue;
	}

	public void setShowTimeValue(long showTimeValue) {
		this.showTimeValue = showTimeValue;
	}

	public String getShowTime() {
		return showTime;
	}

}
