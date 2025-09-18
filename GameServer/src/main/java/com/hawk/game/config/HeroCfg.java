package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkRandObj;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/hero.xml")
public class HeroCfg extends HawkConfigBase {
	@Id
	protected final int heroId;
	protected final int heroClass; // 英雄属性类型（1军事 2资源 3训练 4后勤）
	protected final int qualityColor;// ="5"
	protected final int iniStar;// ="2"
	protected final int iniLevel;// ="1"
	protected final String passiveSkill;// ="100901"
	protected final String maxSkillBlanks;// ="1_10|2_30|3_50|4_70"
	protected final String unlockPieces;// ="30000_1000011_100"
	protected final String unlockCard;// ="30000_1100011_1"
	protected final String attrValueCoeff;// "101_15_5|102_15_6|103_15_7|104_15_8"
	protected final String powerCoeff;// "150_100"
	protected final int talentList;// ="100101_50,100201_100,100301_300"
	protected final int passiveTalent;// ="100101"
	protected final String maxTalentBlanks;// ="0#10000_1000_0|1#10000_1000_100|2#10000_1000_200|3#10000_1000_1000"
	protected final String talentLevelupCost;//="30000_1001001_1"
	protected final int exchangePiecesStar;//="5"
	protected final int prohibitedHero;
	//配置即限定开启时间
	protected final String showTime;
	
	protected final int maxMilitary;
	protected final String marchSelfAtkAttr;
	protected final String marchSelfHpAttr;
	protected final int maxTalent;
	protected final String marchTalentAtkAttr;
	protected final String marchTalentHpAttr;
	protected final int maxLogistics;
	protected final String officeSelfAtkAttr;
	protected final int maxExclusiveTalent;
	protected final String officeTalentAtkAttr;
	protected final String officeSelfHpAttr;
	protected final String officeTalentHpAttr;
	protected final int staffOfficer;
	protected final int soulOpen;
	protected final String name;
	protected final String heroname;
	private long showTimeValue;
	
	private ItemInfo itemUnlockCard;

	private ImmutableList<HawkTuple3<Integer, Double, Double>> attrList;
	private HawkTuple2<Double, Double> powerCoe;
	private ImmutableList<HawkTuple3<Integer, Integer, Integer>> skillBlankList;
	private ImmutableList<HawkTuple2<Integer, String>> talentBlankList;
	private ImmutableList<Integer> passiveSkillList;

	public HeroCfg() {
		this.heroId = 0;
		this.heroClass = 0;
		this.unlockPieces = "";
		this.unlockCard = "";
		this.passiveSkill = "";
		this.qualityColor = 0;
		this.iniStar = 0;
		this.iniLevel = 0;
		this.maxSkillBlanks = "";
		this.attrValueCoeff = "";
		this.powerCoeff = "";
		this.talentList = 0;
		this.passiveTalent = 0;
		this.maxTalentBlanks = "";
		this.talentLevelupCost = "30000_1001001_1";
		this.exchangePiecesStar = 0;
		this.showTime = "";
		this.maxMilitary = 0;
		this.marchSelfAtkAttr = "";
		this.marchSelfHpAttr = "";
		this.maxTalent = 0;
		this.marchTalentAtkAttr = "";
		this.marchTalentHpAttr = "";
		this.maxLogistics = 0;
		this.officeSelfAtkAttr = "";
		this.maxExclusiveTalent = 0;
		this.officeTalentAtkAttr = "";
		this.officeSelfHpAttr = "";
		this.officeTalentHpAttr = "";
		this.prohibitedHero = 0;
		this.staffOfficer = 0;
		this.soulOpen = 0;
		this.name = "";
		this.heroname = "";
	}

	static class TWeight extends HawkTuple2<Integer, Integer> implements HawkRandObj {
		public TWeight(Integer first, Integer second) {
			super(first, second);
		}

		@Override
		public int getWeight() {
			return second;
		}
	}

	@Override
	protected boolean assemble() {
		itemUnlockCard = ItemInfo.valueOf(unlockCard);
		{

			List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(attrValueCoeff);
			List<HawkTuple3<Integer, Double, Double>> list = new ArrayList<>();
			for (String str : attrs) {
				String[] arr = Splitter.on("_").omitEmptyStrings().splitToList(str).toArray(new String[3]);
				list.add(HawkTuples.tuple(NumberUtils.toInt(arr[0]), NumberUtils.toDouble(arr[1]), NumberUtils.toDouble(arr[2])));
			}
			this.attrList = ImmutableList.copyOf(list);
		}

		{
			List<String> powParam = Splitter.on("_").splitToList(powerCoeff);
			this.powerCoe = HawkTuples.tuple(NumberUtils.toDouble(powParam.get(0)), NumberUtils.toDouble(powParam.get(1)));
		}
		{
			List<HawkTuple3<Integer, Integer, Integer>> list = new ArrayList<>();
			Splitter.on("|").omitEmptyStrings().trimResults().split(maxSkillBlanks).forEach(str -> {
				String[] arr = str.split("_");
				HawkTuple3<Integer, Integer, Integer> slot = null;
				if(arr.length == 2){
					slot = HawkTuples.tuple(NumberUtils.toInt(arr[0]), NumberUtils.toInt(arr[1]),0);
				}else if(arr.length == 3){
					slot = HawkTuples.tuple(NumberUtils.toInt(arr[0]), NumberUtils.toInt(arr[1]),NumberUtils.toInt(arr[2]));
				}
				list.add(slot);
			});
			this.skillBlankList = ImmutableList.copyOf(list);
		}
		{// "0#10000_1000_0|1#10000_1000_100|2#10000_1000_200|3#10000_1000_1000"
			List<HawkTuple2<Integer, String>> list = new ArrayList<>();
			Splitter.on("|").omitEmptyStrings().trimResults().split(maxTalentBlanks).forEach(str -> {
				String[] arr = str.split("#");
				HawkTuple2<Integer, String> slot = HawkTuples.tuple(NumberUtils.toInt(arr[0]), arr[1]);
				list.add(slot);
			});
			this.talentBlankList = ImmutableList.copyOf(list);
		}
		this.showTimeValue = StringUtils.isEmpty(showTime) ? 0L : HawkTime.parseTime(showTime);
		{
			List<Integer> list = new ArrayList<>();
			Splitter.on(",").omitEmptyStrings().trimResults().split(passiveSkill).forEach(str -> {
				list.add(NumberUtils.toInt(str));
			});
			this.passiveSkillList = ImmutableList.copyOf(list);
		}
		return super.assemble();
	}

	public ItemInfo getTalentUnlockPrice(int index) {
		for (HawkTuple2<Integer, String> tt : talentBlankList) {
			if (tt.first == index) {
				return ItemInfo.valueOf(tt.second);
			}
		}
		throw new RuntimeException("unknow talent index  = " + index);
	}

	@Override
	protected boolean checkValid() {
		ItemInfo card = ItemInfo.valueOf(unlockCard);
		ItemInfo piec = ItemInfo.valueOf(unlockPieces);
		ItemCfg cardItem = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, card.getItemId());
		ItemCfg cardPiec = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, piec.getItemId());
		if (Objects.isNull(cardItem)) {
			throw new RuntimeException("unlockCard error " + unlockCard);
		}
		if (Objects.isNull(cardPiec)) {
			throw new RuntimeException("unlockPieces error " + unlockPieces);
		}

		HeroColorQualityCfg colorQualityCfg = HawkConfigManager.getInstance().getConfigByKey(HeroColorQualityCfg.class, qualityColor);
		if (Objects.isNull(colorQualityCfg)) {
			throw new RuntimeException("HeroColorQualityCfg not found qualityColor = " + qualityColor);
		}

		if (skillBlankList.isEmpty()) {
			throw new RuntimeException("maxSkillBlanks err heroId = " + heroId + " maxSkillBlanks = " + maxSkillBlanks);
		}

		HeroSkillCfg skillcfg = HawkConfigManager.getInstance().getConfigByKey(HeroSkillCfg.class, passiveSkillList.get(0));
		HawkAssert.notNull(skillcfg, "HeroSkillCfg is null for id : " + passiveSkill);
		return super.checkValid();
	}

	public int getHeroId() {
		return heroId;
	}

	public int getHeroClass() {
		return heroClass;
	}

	public String getUnlockPieces() {
		return unlockPieces;
	}

	public String getUnlockCard() {
		return unlockCard;
	}

	public String getPassiveSkill() {
		return passiveSkill;
	}

	public ItemInfo getItemUnlockCard() {
		return itemUnlockCard.clone();
	}

	public void setItemUnlockCard(ItemInfo itemUnlockCard) {
		throw new UnsupportedOperationException();
	}

	public int getQualityColor() {
		return qualityColor;
	}

	public int getIniStar() {
		return iniStar;
	}

	public int getIniLevel() {
		return iniLevel;
	}

	public String getMaxSkillBlanks() {
		return maxSkillBlanks;
	}

	public ImmutableList<HawkTuple3<Integer, Double, Double>> getAttrList() {
		return attrList;
	}

	public void setAttrList(ImmutableList<HawkTuple3<Integer, Double, Double>> attrList) {
		throw new UnsupportedOperationException();
	}

	public HawkTuple2<Double, Double> getPowerCoe() {
		return powerCoe;
	}

	public String getAttrValueCoeff() {
		return attrValueCoeff;
	}

	public String getPowerCoeff() {
		return powerCoeff;
	}

	public ImmutableList<HawkTuple3<Integer, Integer, Integer>> getSkillBlankList() {
		return skillBlankList;
	}

	public void setSkillBlankList(ImmutableList<HawkTuple3<Integer, Integer, Integer>> skillBlankList) {
		this.skillBlankList = skillBlankList;
	}

	public ImmutableList<HawkTuple2<Integer, String>> getTalentBlankList() {
		return talentBlankList;
	}

	public void setPowerCoe(HawkTuple2<Double, Double> powerCoe) {
		throw new UnsupportedOperationException();
	}

	public int getPassiveTalent() {
		return passiveTalent;
	}

	public String getMaxTalentBlanks() {
		return maxTalentBlanks;
	}

	public int getTalentList() {
		return talentList;
	}

	public String getTalentLevelupCost() {
		return talentLevelupCost;
	}

	public int getExchangePiecesStar() {
		return exchangePiecesStar;
	}

	public String getShowTime() {
		return showTime;
	}

	public long getShowTimeValue() {
		return showTimeValue;
	}

	public void setShowTimeValue(long showTimeValue) {
		this.showTimeValue = showTimeValue;
	}

	public int getMaxMilitary() {
		return maxMilitary;
	}

	public int getMarchSelfAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(marchSelfAtkAttr).getOrDefault(soldierType, 0);
	}

	public int getMarchSelfHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(marchSelfHpAttr).getOrDefault(soldierType, 0);
	}

	public int getMaxTalent() {
		return maxTalent;
	}

	public int getMarchTalentAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(marchTalentAtkAttr).getOrDefault(soldierType, 0);
	}

	public int getMarchTalentHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(marchTalentHpAttr).getOrDefault(soldierType, 0);
	}

	public int getMaxLogistics() {
		return maxLogistics;
	}

	public int getOfficeSelfAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(officeSelfAtkAttr).getOrDefault(soldierType, 0);
	}

	public int getMaxExclusiveTalent() {
		return maxExclusiveTalent;
	}

	public int getOfficeTalentAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(officeTalentAtkAttr).getOrDefault(soldierType, 0);
	}

	public int getOfficeSelfHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(officeSelfHpAttr).getOrDefault(soldierType, 0);
	}

	public int getOfficeTalentHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(officeTalentHpAttr).getOrDefault(soldierType, 0);
	}

	public ImmutableList<Integer> getPassiveSkillList() {
		return passiveSkillList;
	}

	public int getProhibitedHero() {
		return prohibitedHero;
	}

	public int getStaffOfficer() {
		return staffOfficer;
	}

	public int getSoulOpen() {
		return soulOpen;
	}

	public String getName() {
		return name;
	}

	public String getHeroname() {
		return heroname;
	}
	
}
