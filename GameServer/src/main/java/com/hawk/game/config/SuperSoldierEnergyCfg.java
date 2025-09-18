package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/supersoldier_enabling.xml")
public class SuperSoldierEnergyCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	/**
	 * 机甲id
	 */
	private final int supersoldierId;
	
	/**
	 * 部位
	 */
	private final int enablingPosition;
	
	/**
	 * 物理赋能/能量赋能
	 */
	private final int enablingType;
	
	/**
	 * 最高等级
	 */
	private final int enablingLevel;
	
	/**
	 * 作用号
	 */
	private final String effectList;
	
	/**
	 * 战力成长
	 */
	private final int powerCoeff;
	
	/**
	 * 赋能到这一级需要的消耗
	 */
	private final String enablingCost;
	
	
	
	
	/**
     * 强度配置
     */
    protected final String atkAttr;
    protected final String hpAttr;
	
	
	public SuperSoldierEnergyCfg() {
		id = 0;
		supersoldierId = 0;
		enablingPosition = 0;
		enablingType = 0;
		enablingLevel = 0;
		effectList = "";
		powerCoeff = 0;
		enablingCost = "";
		
		atkAttr = "";
		hpAttr = "";
	}

	public int getId() {
		return id;
	}

	public int getSupersoldierId() {
		return supersoldierId;
	}

	public int getEnablingPosition() {
		return enablingPosition;
	}

	public int getEnablingType() {
		return enablingType;
	}

	public int getEnablingLevel() {
		return enablingLevel;
	}

	public String getEffectList() {
		return effectList;
	}

	public int getPowerCoeff() {
		return powerCoeff;
	}

	public String getEnablingCost() {
		return enablingCost;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

    public int getHpAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
    }
    
}
