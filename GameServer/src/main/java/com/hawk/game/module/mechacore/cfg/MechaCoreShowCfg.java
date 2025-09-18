package com.hawk.game.module.mechacore.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 外显配置
 */
@HawkConfigManager.XmlResource(file = "xml/mecha_core_show.xml")
public class MechaCoreShowCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	protected final int showLevel;

	protected final int showType;
	/**
	 * 解锁条件
	 */
	protected final int rankLevelLimit;   //机甲核心阶位等级（rankLevelLimit="3"）
	protected final String moduleValueLimit; //装配核心的数量_需求核心的品质（moduleValueLimit="3_4"）
	protected final String moduleAdditionalValueLimit; //装配核心上词条的数量_装配核心上词条的品质 （moduleAdditionalValueLimit="30_6"）
	
	private int moduleCount, moduleQuality, attrCount, attrQuality;
	
	/**
	 * 最低品质
	 */
	private static int lowModuleQuality = 100, lowAttrQuality = 100;
	
	public MechaCoreShowCfg() {
		id = 0;
		showLevel = 0;
		showType = 0;
		rankLevelLimit = 0;
		moduleValueLimit = "";
		moduleAdditionalValueLimit = "";
	}
	
	@Override
	protected boolean assemble() {
		int count = rankLevelLimit > 0 ? 1 : 0;
		count += HawkOSOperator.isEmptyString(moduleValueLimit) ? 0 : 1;
		count += HawkOSOperator.isEmptyString(moduleAdditionalValueLimit) ? 0 : 1;
		if (count != 1) {
			return false;
		}
		
		if (!HawkOSOperator.isEmptyString(moduleValueLimit)) {
			String[] arr = moduleValueLimit.split("_");
			moduleCount = Integer.parseInt(arr[0]);
			moduleQuality = Integer.parseInt(arr[1]);
			lowModuleQuality = Math.min(lowModuleQuality, moduleQuality);
		}
		
		if (!HawkOSOperator.isEmptyString(moduleAdditionalValueLimit)) {
			String[] arr = moduleAdditionalValueLimit.split("_");
			attrCount = Integer.parseInt(arr[0]);
			attrQuality = Integer.parseInt(arr[1]);
			lowAttrQuality = Math.min(lowAttrQuality, attrQuality);
		}
		
		return true;
	}

	public int getId() {
		return id;
	}

	public int getShowLevel() {
		return showLevel;
	}

	public int getShowType() {
		return showType;
	}

	public int getRankLevelLimit() {
		return rankLevelLimit;
	}

	public String getModuleValueLimit() {
		return moduleValueLimit;
	}

	public String getModuleAdditionalValueLimit() {
		return moduleAdditionalValueLimit;
	}

	public int getModuleCount() {
		return moduleCount;
	}

	public int getModuleQuality() {
		return moduleQuality;
	}

	public int getAttrCount() {
		return attrCount;
	}

	public int getAttrQuality() {
		return attrQuality;
	}

	public static int getLowModuleQuality() {
		return lowModuleQuality;
	}

	public static int getLowAttrQuality() {
		return lowAttrQuality;
	}

}
