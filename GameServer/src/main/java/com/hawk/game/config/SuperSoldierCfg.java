package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierEffect;

@HawkConfigManager.XmlResource(file = "xml/supersoldier.xml")
public class SuperSoldierCfg extends HawkConfigBase {
	@Id
	protected final int supersoldierId;
	protected final int supersoldierClass; // 英雄属性类型（1军事 2资源 3训练 4后勤）
	protected final int qualityColor;// ="5"
	protected final int iniStar;// ="2"
	protected final int iniLevel;// ="1"
	protected final int passiveSkill;// ="100901"
	protected final String maxSkillBlanks;// ="1_10|2_30|3_50|4_70"
	protected final String unlockPieces;// ="30000_1000011_100"
	protected final String unlockCard;// ="30000_1100011_1"
	protected final String attrValueCoeff;// "101_15_5|102_15_6|103_15_7|104_15_8"
	protected final String powerCoeff;// "150_100"
	protected final int unlockLevel;// ="2"
	protected final String unlockCost;// ="10000_1004_1000"
	private final int preSupersoldierId;
	private final String unlockAnyWhereCost;// ="10000_1004_1000"
	private final String unlockAnyWhereEffect;// = 1004_500|1012_500|1020_500
	private final int unlockAnyWhereGetSkin;// 无处不在送皮肤
	private final int unlockAnyWherePower;
	private final String unlockEnabling; // 赋能解锁消耗
	private final String enablingEffect; // 赋能解锁获得作用号
	private final String soldierType;//="3_4";
	private ItemInfo itemUnlockCard;
	private ImmutableSet<SoldierType> soldierTypeSet;
	private ImmutableList<HawkTuple3<Integer, Double, Double>> attrList;
	private HawkTuple2<Double, Double> powerCoe;
	private ImmutableList<HawkTuple3<Integer, Integer, Integer>> skillBlankList;
	private ImmutableList<PBSuperSoldierEffect> unlockAnyWhereEffectList;

	public SuperSoldierCfg() {
		this.supersoldierId = 0;
		this.supersoldierClass = 0;
		this.unlockPieces = "";
		this.unlockCard = "";
		this.passiveSkill = 0;
		this.qualityColor = 0;
		this.iniStar = 0;
		this.iniLevel = 0;
		this.maxSkillBlanks = "";
		this.attrValueCoeff = "";
		this.powerCoeff = "";
		preSupersoldierId = 0;
		unlockLevel = 0;
		unlockCost = "10000_1004_1000";
		unlockAnyWhereCost = "";
		unlockAnyWhereEffect = "";
		unlockAnyWhereGetSkin = 0;
		unlockAnyWherePower = 0;
		unlockEnabling = "";
		enablingEffect = "";
		soldierType = "";
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
				HawkTuple3<Integer, Integer, Integer> slot = HawkTuples.tuple(NumberUtils.toInt(arr[0]), NumberUtils.toInt(arr[1]), NumberUtils.toInt(arr[2]));
				list.add(slot);
			});
			this.skillBlankList = ImmutableList.copyOf(list);
		}

		{
			List<String> attrs = Splitter.on("|").omitEmptyStrings().splitToList(unlockAnyWhereEffect);
			List<PBSuperSoldierEffect> templist = new ArrayList<>();
			for (String str : attrs) {
				String[] arr = str.split("_");
				PBSuperSoldierEffect ef = PBSuperSoldierEffect.newBuilder().setEffectId(NumberUtils.toInt(arr[0])).setValue(NumberUtils.toInt(arr[1])).build();
				templist.add(ef);
			}
			unlockAnyWhereEffectList = ImmutableList.copyOf(templist);
		}
		
		{
			List<String> attrs = Splitter.on("_").omitEmptyStrings().splitToList(soldierType);
			List<SoldierType> templist = new ArrayList<>();
			for (String str : attrs) {
				SoldierType type = SoldierType.valueOf(Integer.valueOf(str));
				templist.add(type);
			}
			soldierTypeSet = ImmutableSet.copyOf(templist);
		}
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		// ItemInfo card = ItemInfo.valueOf(unlockCard);
		// ItemInfo piec = ItemInfo.valueOf(unlockPieces);
		// ItemCfg cardItem = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, card.getItemId());
		// ItemCfg cardPiec = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, piec.getItemId());
		// if (Objects.isNull(cardItem)) {
		// throw new RuntimeException("unlockCard error " + unlockCard);
		// }
		// if (Objects.isNull(cardPiec)) {
		// throw new RuntimeException("unlockPieces error " + unlockPieces);
		// }

		SuperSoldierColorQualityCfg colorQualityCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierColorQualityCfg.class, qualityColor);
		if (Objects.isNull(colorQualityCfg)) {
			throw new RuntimeException("HeroColorQualityCfg not found qualityColor = " + qualityColor);
		}

		if (skillBlankList.isEmpty()) {
			throw new RuntimeException("maxSkillBlanks err supersoldierId = " + supersoldierId + " maxSkillBlanks = " + maxSkillBlanks);
		}

		// SuperSoldierSkillCfg skillcfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierSkillCfg.class, passiveSkill);
		// HawkAssert.notNull(skillcfg, "HeroSkillCfg is null for id : " + passiveSkill);
		return super.checkValid();
	}

	public String getUnlockPieces() {
		return unlockPieces;
	}

	public String getUnlockCard() {
		return unlockCard;
	}

	public int getPassiveSkill() {
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

	public void setSkillBlankList(ImmutableList<HawkTuple2<Integer, Integer>> skillBlankList) {
		throw new UnsupportedOperationException();
	}

	public void setPowerCoe(HawkTuple2<Double, Double> powerCoe) {
		throw new UnsupportedOperationException();
	}

	public int getSupersoldierId() {
		return supersoldierId;
	}

	public int getSupersoldierClass() {
		return supersoldierClass;
	}

	public int getUnlockLevel() {
		return unlockLevel;
	}

	public String getUnlockCost() {
		return unlockCost;
	}

	public int getPreSupersoldierId() {
		return preSupersoldierId;
	}

	public String getUnlockAnyWhereCost() {
		return unlockAnyWhereCost;
	}

	public String getUnlockAnyWhereEffect() {
		return unlockAnyWhereEffect;
	}

	public ImmutableList<PBSuperSoldierEffect> getUnlockAnyWhereEffectList() {
		return unlockAnyWhereEffectList;
	}

	public int getUnlockAnyWhereGetSkin() {
		return unlockAnyWhereGetSkin;
	}

	public int getUnlockAnyWherePower() {
		return unlockAnyWherePower;
	}

	public String getUnlockEnabling() {
		return unlockEnabling;
	}

	public String getEnablingEffect() {
		return enablingEffect;
	}

	public ImmutableSet<SoldierType> getSoldierTypeSet() {
		return soldierTypeSet;
	}

	@SuppressWarnings("unused")
	private void setSoldierTypeSet(ImmutableSet<SoldierType> soldierTypeSet) {
		this.soldierTypeSet = soldierTypeSet;
	}
	
}
