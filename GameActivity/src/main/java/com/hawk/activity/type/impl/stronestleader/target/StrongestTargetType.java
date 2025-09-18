package com.hawk.activity.type.impl.stronestleader.target;

public enum StrongestTargetType {

	/** 采集发展*/
	COLLECT_RESOURCE(1, ConfigType.MAP),
	/** 基地发展*/
	BUILDING_PROGRESS(2, ConfigType.LIST),
	/** 打击尤里*/
	ATTACK_MONSTER(3, ConfigType.LIST),
	/** 战力提升*/
	FIGHT_POWER_UP(4, ConfigType.LIST),
	/** 发展战争*/
	BREAK_WAR(5, ConfigType.OTHER),
	/** 造兵晋升*/
	SOLIDER_PROGRESS(6, ConfigType.LIST),
	;
	
	StrongestTargetType(int type, ConfigType configType) {
		this.type = type;
		this.configType = configType;
	}
	
	private int type;
	
	private ConfigType configType;
	
	public ConfigType getConfigType() {
		return configType;
	}
	
	public int getType() {
		return type;
	}

	public static StrongestTargetType getType(int type) {
		for (StrongestTargetType targetType : values()) {
			if (targetType.type == type) {
				return targetType;
			}
		}
		return null;
	}
	
	public enum ConfigType {
		MAP,LIST,OTHER;
	}
	
}

