package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.util.GameUtil;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 装扮套装配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_dress_group.xml")
public class DressGroupCfg extends HawkConfigBase {

	@Id
	protected final int groupId;
	
	protected final String dressIds;
	
	protected final String effects;
	
	protected final int attrValue;
	protected final String atkAttr;
	protected final String hpAttr;
	
	/**
	 * 装扮列表
	 */
	private List<Integer> dressIdList;
	
	/**
	 * 套装作用号属性
	 */
	private List<EffectObject> effectList;
	
	public DressGroupCfg() {
		groupId = 0;
		dressIds = "";
		effects = "";
		attrValue = 0;
		atkAttr = "";
		hpAttr = "";
	}

	public int getAttrValue() {
		return attrValue;
	}

	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	public int getGroupId() {
		return groupId;
	}

	public String getDressIds() {
		return dressIds;
	}

	public String getEffects() {
		return effects;
	}
	
	public List<Integer> getDressIdList() {
		return dressIdList;
	}

	public List<EffectObject> getEffectList() {
		return effectList;
	}
	
	@Override
	protected boolean assemble() {
		
		List<Integer> dressIdList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(dressIds)) {
			String[] dressIdSplit = dressIds.split(",");
			for (String dressId : dressIdSplit) {
				dressIdList.add(Integer.valueOf(dressId));
			}
		}
		this.dressIdList = dressIdList;
		
		this.effectList = GameUtil.assambleEffectObject(effects);
		
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		for (int dressId : dressIdList) {
			DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressId);
			if (dressCfg != null) {
				continue;
			}
			throw new InvalidParameterException(String.format("dressId=%s not exist in world_dress_model.xml", dressId));
		}
		return true;
	}
}
