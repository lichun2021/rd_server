package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuple5;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

@HawkConfigManager.XmlResource(file = "xml/hero_skill.xml")
public class HeroSkillCfg extends HawkConfigBase {
	@Id
	protected final int skillId;// ="10001"
	protected final int isTalentSkill;// ="1"
	protected final int skillQuality;// ="2"
	protected final String unlockItem;// ="30000_13220001_10"
	protected final String resolveItem;// ="30000_13200001_3"
	protected final int attrId;// ="102"
	protected final int skillType;//
	protected final String effectList;// ="403_0_0.6_10501_10502|404_0_0.6_10601_10602"
										// 做用号_初值_系数
	protected final String officeId;// ="10501|10502|10601|10602|10701|10702|10801|10802"
	protected final String powerCoeff;// ="50_50"
	protected final int marchUsed;

	protected final String proficiencyEffect;
	protected final int proficiencyCdWorld;// ="28800"
	protected final int proficiencyCdRaid;//
	protected final int staffOfficer;
	protected final int proficiencyAuto;
	private ImmutableList<HawkTuple5<Integer, Double, Double, Integer, Integer>> buff;
	private HawkTuple2<Double, Double> powerCoe;
	private ImmutableList<Integer> officeIdList;

	public HeroSkillCfg() {
		skillId = 0;
		isTalentSkill = 0;
		skillQuality = 0;
		unlockItem = "";
		resolveItem = "";
		skillType = 0;
		effectList = "";
		powerCoeff = "";
		attrId = 0;
		officeId = "";
		marchUsed = 0;
		proficiencyEffect = "";
		proficiencyCdWorld = 0;
		proficiencyCdRaid = 0;
		staffOfficer = 0;
		proficiencyAuto =0;
	}

	@Override
	protected boolean assemble() {
		{
			List<HawkTuple5<Integer, Double, Double, Integer, Integer>> result = new ArrayList<>();

			List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(effectList);
			Set<Integer> check = new HashSet<>();
			for (String str : attrs) {
				String[] arr = Splitter.on("_").omitEmptyStrings().splitToList(str).toArray(new String[5]);
				int offId1 = 0, offId2 = 0;
				if (arr.length >= 4) {
					offId1 = NumberUtils.toInt(arr[3]);
				}
				if (arr.length >= 5) {
					offId2 = NumberUtils.toInt(arr[4]);
				}
				int effectId = NumberUtils.toInt(arr[0]);
				if (check.contains(effectId)) {
					throw new RuntimeException("hero_skill.xml 配置错误 . 重复的effectList id = " + effectId);
				}
				result.add(HawkTuples.tuple(effectId, NumberUtils.toDouble(arr[1]), NumberUtils.toDouble(arr[2]), offId1, offId2));
				check.add(effectId);
			}
			buff = ImmutableList.copyOf(result);
		}

		{
			List<String> list = Splitter.on("_").splitToList(powerCoeff);
			this.powerCoe = HawkTuples.tuple(NumberUtils.toDouble(list.get(0)), NumberUtils.toDouble(list.get(1)));
		}
		{
			List<Integer> list = Splitter.on("|").omitEmptyStrings().splitToList(officeId).stream()
					.mapToInt(Integer::valueOf)
					.mapToObj(Integer::valueOf)
					.collect(Collectors.toList());
			this.officeIdList = ImmutableList.copyOf(list);
		}
		return super.assemble();
	}

	public int getSkillId() {
		return skillId;
	}

	public int getIsTalentSkill() {
		return isTalentSkill;
	}

	public int getSkillQuality() {
		return skillQuality;
	}

	public ImmutableList<HawkTuple5<Integer, Double, Double, Integer, Integer>> getBuff() {
		return buff;
	}

	public void setBuff(ImmutableList<HawkTuple3<Integer, Double, Double>> buff) {
		throw new UnsupportedOperationException();
	}

	public int getAttrId() {
		return attrId;
	}

	public String getEffectList() {
		return effectList;
	}

	public HawkTuple2<Double, Double> getPowerCoe() {
		return powerCoe;
	}

	public void setPowerCoe(HawkTuple2<Integer, Integer> powerCoe) {
		throw new UnsupportedOperationException();
	}

	public int getMarchUsed() {
		return marchUsed;
	}

	public String getUnlockItem() {
		return unlockItem;
	}

	public String getResolveItem() {
		return resolveItem;
	}

	public String getPowerCoeff() {
		return powerCoeff;
	}

	public int getSkillType() {
		return skillType;
	}

	public ImmutableList<Integer> getOfficeIdList() {
		return officeIdList;
	}

	public void setOfficeIdList(ImmutableList<Integer> officeIdList) {
		throw new UnsupportedOperationException();
	}

	public int getProficiencyCdWorld() {
		return proficiencyCdWorld;
	}

	public int getProficiencyCdRaid() {
		return proficiencyCdRaid;
	}

	public String getProficiencyEffect() {
		return proficiencyEffect;
	}

	public int getStaffOfficer() {
		return staffOfficer;
	}

	public int getProficiencyAuto() {
		return proficiencyAuto;
	}

}
