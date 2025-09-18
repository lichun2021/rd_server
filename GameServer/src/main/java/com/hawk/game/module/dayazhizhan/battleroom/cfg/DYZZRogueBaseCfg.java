package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/dyzz_rogue_base.xml")
public class DYZZRogueBaseCfg extends HawkConfigBase {
	@Id
	private final int id;// ="1"
	private final int quality;// ="1"
	private final int category;// ="1"
	private final String armsusability;// ="1,2,3,4"
	private final String rogueEffect;// ="9004_20000"
	private final int heroData;// ="5012001"
	private final int heroOffice;// ="0"
	private final String heroTalent;// ="5101001,5101002,5101003"
	private final String soldier;// ="100307_20000,100207_200000"
	private final int collect;//
	private ImmutableMap<EffType, int[]> collectBuffMap;
	private List<Integer> armsusabilityList = ImmutableList.of();
	private List<Integer> talentList = ImmutableList.of();
	private ImmutableMap<Integer, Integer> soldierMap;

	public DYZZRogueBaseCfg() {
		id = 0;
		quality = 0;
		category = 0;
		armsusability = "1,2,3,4";
		rogueEffect = "";
		heroData = 0;
		heroOffice = 0;
		heroTalent = "";
		soldier = "";
		collect = 0;
	}

	@Override
	protected boolean assemble() {
		if (StringUtils.isNotEmpty(armsusability)) {
			armsusabilityList = ImmutableList.copyOf(SerializeHelper.stringToList(Integer.class, armsusability, SerializeHelper.BETWEEN_ITEMS));
		}
		if (StringUtils.isNotEmpty(heroTalent)) {
			talentList = ImmutableList.copyOf(SerializeHelper.stringToList(Integer.class, heroTalent, SerializeHelper.BETWEEN_ITEMS));
		}
		{
			Map<EffType, int[]> lsit = new HashMap<>();
			for (String xy : Splitter.on(",").omitEmptyStrings().splitToList(rogueEffect)) {
				int[] arr = Splitter.on("_").omitEmptyStrings().splitToList(xy).stream().mapToInt(Integer::valueOf).toArray();
				int[] pos = new int[10];
				for (int i = 0; i < arr.length; i++) {
					pos[i] = arr[i];
				}
				lsit.put(EffType.valueOf(pos[0]), pos);
			}
			collectBuffMap = ImmutableMap.copyOf(lsit);
		}
		{
			Map<Integer, Integer> lsit = new HashMap<>();
			for (String xy : Splitter.on(",").omitEmptyStrings().splitToList(soldier)) {
				String[] x_y = xy.split("_");
				int[] pos = new int[2];
				pos[0] = NumberUtils.toInt(x_y[0]);
				pos[1] = NumberUtils.toInt(x_y[1]);
				lsit.put(pos[0], pos[1]);
			}
			soldierMap = ImmutableMap.copyOf(lsit);
		}
		return super.assemble();
	}

	public ImmutableMap<EffType, int[]> getCollectBuffMap() {
		return collectBuffMap;
	}

	public void setCollectBuffMap(ImmutableMap<EffType, int[]> collectBuffMap) {
		this.collectBuffMap = collectBuffMap;
	}

	public int getId() {
		return id;
	}

	public int getQuality() {
		return quality;
	}

	public int getCategory() {
		return category;
	}

	public String getArmsusability() {
		return armsusability;
	}

	public String getRogueEffect() {
		return rogueEffect;
	}

	public int getHeroData() {
		return heroData;
	}

	public int getHeroOffice() {
		return heroOffice;
	}

	public String getHeroTalent() {
		return heroTalent;
	}

	public List<Integer> getArmsusabilityList() {
		return armsusabilityList;
	}

	public void setArmsusabilityList(List<Integer> armsusabilityList) {
		this.armsusabilityList = armsusabilityList;
	}

	public ImmutableMap<Integer, Integer> getSoldierMap() {
		return soldierMap;
	}

	public void setSoldierMap(ImmutableMap<Integer, Integer> soldierMap) {
		this.soldierMap = soldierMap;
	}

	public String getSoldier() {
		return soldier;
	}

	public List<Integer> getTalentList() {
		return talentList;
	}

	public void setTalentList(List<Integer> talentList) {
		this.talentList = talentList;
	}

	public int getCollect() {
		return collect;
	}

}
