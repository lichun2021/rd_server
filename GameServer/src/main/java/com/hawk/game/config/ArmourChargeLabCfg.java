package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.util.WeightAble;
import com.hawk.serialize.string.SerializeHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * 装备充能库
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/armour_charge_lab.xml")
public class ArmourChargeLabCfg extends HawkConfigBase implements WeightAble {

	/**
	 * 
	 */
	@Id
	protected final int id;
	
	/**
	 * 充能标识类别
	 */
	protected final int chargingLabel;
	
	/**
	 * 初始进度
	 */
	protected final int defaultProgress;
	
	/**
	 * 属性值
	 */
	protected final String attributeValue;
	
	/**
	 * 属性折合战斗力
	 */
	protected final int attributePower;
	
	/**
	 * 随机权重
	 */
	protected final int randomWeight;
	
	protected final String atkAttr;
	protected final String hpAttr;

	protected final String soldierType;
	protected final int effectGroup;
	
	public ArmourChargeLabCfg() {
		id = 0;
		chargingLabel = 0;
		defaultProgress = 0;
		attributeValue = "";
		attributePower = 0;
		randomWeight = 0;
		atkAttr = "";
		hpAttr = "";
		soldierType = "";
		effectGroup = 0;
	}

	public int getId() {
		return id;
	}

	public int getChargingLabel() {
		return chargingLabel;
	}

	public int getDefaultProgress() {
		return defaultProgress;
	}

	public EffectObject getAttributeValue() {
		String[] split = attributeValue.split("_");
		return new EffectObject(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
	}

	public int getAttributePower() {
		return attributePower;
	}

	public int getRandomWeight() {
		return randomWeight;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}

	@Override
	public int getWeight() {
		return Math.max(0, randomWeight);
	}

	public int getEffectGroup() {
		return effectGroup;
	}

	public Set<Integer> getSoldierType() {
		Set<Integer> soldierTypeSet = new HashSet<>();
		String[] tmp = soldierType.split("_");
		for (String typeStr : tmp) {
			soldierTypeSet.add(Integer.parseInt(typeStr));
		}
		return soldierTypeSet;
	}
}
