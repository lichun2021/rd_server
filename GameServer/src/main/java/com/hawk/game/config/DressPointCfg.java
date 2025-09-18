package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 装扮点
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/skin_point_attribute.xml")
public class DressPointCfg extends HawkConfigBase {

	@Id
	protected final int id;

	/**
	 * 所需装扮点
	 */
	protected final int needPoint;

	protected final String atkAttr;
	protected final String hpAttr;
	
	/**
	 * 作用号
	 */
	protected final String effect;

	/**
	 * 作用号列表
	 */
	private List<EffectObject> effectList;

	public DressPointCfg() {
		id = 0;
		needPoint = 0;
		effect = "";
		atkAttr = "";
		hpAttr = "";
	}

	public int getId() {
		return id;
	}

	public int getNeedPoint() {
		return needPoint;
	}

	public String getEffect() {
		return effect;
	}

	public List<EffectObject> getEffectList() {
		return effectList;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	@Override
	protected boolean assemble() {
		effectList = GameUtil.assambleEffectObject(effect);
		return true;
	}
}
