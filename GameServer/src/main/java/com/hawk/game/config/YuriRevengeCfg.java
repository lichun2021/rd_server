package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 尤里复仇出兵表
 * @author zhenyu.shang
 * @since 2017年9月21日
 */
@HawkConfigManager.XmlResource(file = "xml/yuriRevenge.xml")
public class YuriRevengeCfg extends HawkConfigBase{
	
	@Id
	protected final int id;
	// 个人积分
	protected final int personIntegral;
	// 联盟积分
	protected final int allianceIntegral;
	// 下一波出怪时间间隔
	protected final int monsterInterval;
	// 怪物模型等级
	protected final int modelLevel;
	
	public YuriRevengeCfg() {
		this.id = 0;
		this.personIntegral = 0;
		this.allianceIntegral = 0;
		this.monsterInterval = 0;
		this.modelLevel = 0;
	}

	public int getId() {
		return id;
	}

	public int getPersonIntegral() {
		return personIntegral;
	}

	public int getAllianceIntegral() {
		return allianceIntegral;
	}

	public int getMonsterInterval() {
		return monsterInterval;
	}

	public int getModelLevel() {
		return modelLevel;
	}
}
