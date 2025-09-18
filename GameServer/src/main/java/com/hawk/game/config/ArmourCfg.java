package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 铠甲配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/armour.xml")
public class ArmourCfg extends HawkConfigBase {
	/**主武器*/
	public static final int POS1 = 1;
	/**手枪*/
	public static final int POS2 = 2;
	/**近战*/
	public static final int POS3 = 3;
	/**投掷*/
	public static final int POS4 = 4;
	/**头盔*/
	public static final int POS5 = 5;
	/**护甲*/
	public static final int POS6 = 6;
	/**鞋子*/
	public static final int POS7 = 7;
	/**
	 * 铠甲id
	 */
	@Id
	protected final int armourId;
	
	/**
	 * 穿戴位置
	 */
	protected final int pos;

	/**
	 * 基础属性
	 */
	protected final String baseAttr;

	/**
	 * 套装id
	 */
	protected final int armourSuitId;

	/**
	 * 是否是神器
	 */
	protected final boolean isSuper;
	
	/**
	 * 神器额外属性
	 */
	protected final String superExtraAttr;
	
	/**
	 * 特技属性
	 */
	protected final String skillAttr;
	
	/**
	 * 神器有效期
	 */
	protected final int lifeTime;
	
	/**
	 * 战力属性计算加成
	 */
	protected final String atkAttr;
	protected final String hpAttr;
	
	/**
	 * 基础属性
	 */
	private List<EffectObject> baseAttrList;
	
	/**
	 * 神器额外属性
	 */
	private List<Integer> superExtraAttrIds;

	/**
	 * 特技属性
	 */
	private List<Integer> skillAttrList;
	
	public ArmourCfg() {
		armourId = 0;
		pos = 0;
		baseAttr = "";
		armourSuitId = 1;
		isSuper = false;
		superExtraAttr = "";
		skillAttr = "";
		lifeTime = 0;
		atkAttr = "";
		hpAttr = "";
	}
	
	public int getArmourId() {
		return armourId;
	}

	public int getPos() {
		return pos;
	}

	public String getBaseAttr() {
		return baseAttr;
	}

	public int getArmourSuitId() {
		return armourSuitId;
	}
	
	public boolean isSuper() {
		return isSuper;
	}

	public List<EffectObject> getBaseAttrList() {
		return baseAttrList;
	}

	public List<Integer> getSuperExtraAttrIds() {
		return superExtraAttrIds;
	}

	public List<Integer> getSkillAttrList() {
		return new ArrayList<>(skillAttrList);
	}

	public long getLifeTime() {
		return lifeTime * 1000L;
	}

	@Override
	protected boolean assemble() {
		baseAttrList = SerializeHelper.stringToList(EffectObject.class, baseAttr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
		
		List<Integer> superExtraAttrIds = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(superExtraAttr)) {
			String[] split = superExtraAttr.split(",");
			for(int i = 0; i < split.length; i++) {
				superExtraAttrIds.add(Integer.valueOf(split[i]));
			}
		}
		this.superExtraAttrIds = superExtraAttrIds;
		
		List<Integer> skillAttrList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(skillAttr)) {
			String[] split = skillAttr.split(",");
			for(int i = 0; i < split.length; i++) {
				skillAttrList.add(Integer.valueOf(split[i]));
			}
		}
		this.skillAttrList = skillAttrList;
		
		return true;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
}
