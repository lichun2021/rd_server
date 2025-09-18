package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ArmourAttrTemplate;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.util.WeightAble;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 铠甲属性配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/armour_additional.xml")
public class ArmourAdditionalCfg extends HawkConfigBase implements WeightAble {
	
	@Id
	protected final int id;

	/**
	 * 属性类型
	 */
	protected final int type;
	
	/**
	 * 品质
	 */
	protected final int quality;
	
	/**
	 * 额外属性
	 */
	protected final String attr;
	
	/**
	 * 传承消耗
	 */
	protected final String inheritConsume;
	
	/**
	 * 权重
	 */
	protected final int weight;
	
	/**
	 * 战力
	 */
	protected final int armourCombat;

	protected final String soldierType;
	protected final int effectGroup;
	
	/**
	 * 额外属性
	 */
	private List<ArmourAttrTemplate> attrList;
	
	/**
	 * 强化消耗
	 */
	protected List<ItemInfo> inheritConsumeItem;
	
	
	public ArmourAdditionalCfg() {
		id = 0;
		type = 0;
		quality = 0;
		attr = "";
		inheritConsume = "";
		weight = 0;
		armourCombat = 0;
		soldierType = "";
		effectGroup = 0;
	}

	public List<ArmourAttrTemplate> getAttrList() {
		return attrList;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getQuality() {
		return quality;
	}

	public String getAttr() {
		return attr;
	}
	
	public String getInheritConsume() {
		return inheritConsume;
	}

	public int getArmourCombat() {
		return armourCombat;
	}

	@Override
	public int getWeight() {
		return Math.max(1, weight);
	}
	
	public List<ItemInfo> getInheritConsumeItem() {
		return inheritConsumeItem;
	}

	@Override
	protected boolean assemble() {
		attrList = SerializeHelper.stringToList(ArmourAttrTemplate.class, attr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
		inheritConsumeItem = ItemInfo.valueListOf(inheritConsume);
		return true;
	}

	public int getEffectGroup() {
		return effectGroup;
	}

	public Set<Integer> getSoldierType() {
		Set<Integer> soldierTypeSet = new HashSet<>();
		String [] tmp = soldierType.split("_");
		for(String typeStr : tmp){
			soldierTypeSet.add(Integer.parseInt(typeStr));
		}
		return soldierTypeSet;
	}
}
