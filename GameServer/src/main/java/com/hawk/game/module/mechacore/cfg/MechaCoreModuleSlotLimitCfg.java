package com.hawk.game.module.mechacore.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 机甲核心的槽位表
 * @author lating
 */
@HawkConfigManager.XmlResource(file = "xml/mecha_core_module_slot_limit.xml")
public class MechaCoreModuleSlotLimitCfg extends HawkConfigBase {

	@Id
	protected final int id;
	/**
	 * 核心槽等类型
	 */
	protected final int slotType;
	/**
	 * 解锁槽位需要核心突破阶数
	 */
	protected final int unlockRankLimit;
	/**
	 * 解锁槽位需要的大本等级
	 */
	protected final int unlockBaseLevelLimit;
	
	private static Map<Integer, MechaCoreModuleSlotLimitCfg> typeConfigMap = new HashMap<>();
	
	public MechaCoreModuleSlotLimitCfg() {
		id = 0;
		slotType = 0;
		unlockRankLimit = 0;
		unlockBaseLevelLimit = 0;
	}
	
	@Override
	protected boolean assemble() {
		typeConfigMap.put(slotType, this);
		return true;
	}

	public int getId() {
		return id;
	}

	public int getSlotType() {
		return slotType;
	}

	public int getUnlockRankLimit() {
		return unlockRankLimit;
	}

	public int getUnlockBaseLevelLimit() {
		return unlockBaseLevelLimit;
	}
	
	public static MechaCoreModuleSlotLimitCfg getConfigByType(int type) {
		return typeConfigMap.get(type);
	}

}
