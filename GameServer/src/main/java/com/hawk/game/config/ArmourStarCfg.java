package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 装备升星配置
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/armour_star_attr.xml")
public class ArmourStarCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	/**
	 * 装备id
	 */
	protected final int armourId;

	/**
	 * 星级
	 */
	protected final int starLevel;

	/**
	 * 星级属性
	 */
	protected final String starAttribute;
	
	/**
	 * 星级属性(作用号)
	 */
	private List<EffectObject> starEff;
	
	public ArmourStarCfg() {
		id = 0;
		armourId = 0;
		starLevel = 0;
		starAttribute = "";
	}

	@Override
	protected boolean assemble() {
		starEff = SerializeHelper.stringToList(EffectObject.class, starAttribute, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
		return true;
	}

	public int getId() {
		return id;
	}

	public int getArmourId() {
		return armourId;
	}

	public int getStarLevel() {
		return starLevel;
	}
	
	public List<EffectObject> getStarEff() {
		return new ArrayList<>(starEff);
	}
}
