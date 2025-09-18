package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 装扮配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_dress_model.xml")
public class DressCfg extends HawkConfigBase {
	@Id
	protected final int dressId;
	
	/**
	 * 装扮类型 -> 1:名牌 2:皮肤 3:聊天框
	 */
	protected final int dressType;
	
	/**
	 * 具体类型
	 */
	protected final int modelType;
	
	/**
	 * 拥有装扮触发作用号
	 */
	protected final String gainAttribute;
	
	/**
	 * 使用装扮触发作用号
	 */
	protected final String useAttribute;
	
	/**
	 * 装扮点 
	 */
	protected final int skinPoint;
	
	/**
	 * 是否可以更换外观，1可以，0不可以
	 */
	protected final int canChange;
	
	protected final int attrUseValue;
	
	protected final String atkAttrGain;
	protected final String atkAttrUse;
	protected final String hpAttrGain;
	protected final String hpAttrUse;

	protected final int isShowMyth;
	
	/**
	 * 拥有装扮触发作用号列表
	 */
	private List<EffectObject> gainEffectList;
	
	/**
	 * 使用装扮触发作用号列表
	 */
	private List<EffectObject> useEffectList;
	
	
	public DressCfg() {
		dressId = 0;
		dressType = 0;
		modelType = 0;
		gainAttribute = "";
		useAttribute = "";
		skinPoint = 0;
		canChange = 0;
		
		attrUseValue = 0;
		atkAttrGain = "";
		atkAttrUse = "";
		hpAttrGain = "";
		hpAttrUse = "";
		isShowMyth = 0;
	}

	public int getAtkAttrGain(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttrGain).getOrDefault(soldierType, 0);
	}
	
	public int getAttrUseValue() {
		return attrUseValue;
	}
	
	public int getAtkAttrUse(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttrUse).getOrDefault(soldierType, 0);
	}
	
	public int getHpAttrGain(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttrGain).getOrDefault(soldierType, 0);
	}
	
	public int getHpAttrUse(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttrUse).getOrDefault(soldierType, 0);
	}

	public int getDressId() {
		return dressId;
	}

	public int getDressType() {
		return dressType;
	}

	public int getModelType() {
		return modelType;
	}
	
	
	public List<EffectObject> getGainEffectList() {
		return gainEffectList;
	}

	public List<EffectObject> getUseEffectList() {
		return useEffectList;
	}

	public int getSkinPoint() {
		return skinPoint;
	}
	
	public int getCanChange() {
		return canChange;
	}

	public int getIsShowMyth() {
		return isShowMyth;
	}

	@Override
	protected boolean assemble() {
		gainEffectList = GameUtil.assambleEffectObject(gainAttribute);
		useEffectList = GameUtil.assambleEffectObject(useAttribute);
		return true;
	}
}
