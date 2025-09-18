package com.hawk.game.crossactivity;

import com.hawk.game.protocol.CrossActivity.CrossRankType;

public enum CrossTargetType {

	/** 采集发展*/
	COLLECT_RESOURCE(1, ConfigType.MAP) {
		@Override
		public boolean provideScoreToRank(CrossRankType rankType) {
			if(rankType == CrossRankType.C_SELF_RANK || rankType == CrossRankType.C_GUILD_RANK){
				return true;
			}
			return false;
		}
	},
	/** 幽灵兵营*/
	OCCUPY_STRONGPOINT(2, ConfigType.LIST) {
		@Override
		public boolean provideScoreToRank(CrossRankType rankType) {
			if(rankType == CrossRankType.C_SELF_RANK || rankType == CrossRankType.C_GUILD_RANK){
				return true;
			}
			return false;
		}
	},
	/** 击杀野怪*/
	ATTACK_MONSTER(3, ConfigType.LIST) {
		@Override
		public boolean provideScoreToRank(CrossRankType rankType) {
			if(rankType == CrossRankType.C_SELF_RANK || rankType == CrossRankType.C_GUILD_RANK){
				return true;
			}
			return false;
		}
	},
	/** 击杀敌军*/
	BEAT_ARMY(4, ConfigType.OTHER) {
		@Override
		public boolean provideScoreToRank(CrossRankType rankType) {
			if(rankType == CrossRankType.C_SELF_RANK || rankType == CrossRankType.C_GUILD_RANK){
				return true;
			}
			return false;
		}
	},
	/** 占领能量塔*/
	OCCUPY_PYLON(5,ConfigType.LIST) {
		@Override
		public boolean provideScoreToRank(CrossRankType rankType) {
			if(rankType == CrossRankType.C_SERVER_RANK || rankType == CrossRankType.C_SELF_RANK || 
					rankType == CrossRankType.C_GUILD_RANK){
				return true;
			}
			return false;
		}
	}
	;
	
	CrossTargetType(int type, ConfigType configType) {
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
	
	
	public abstract boolean provideScoreToRank(CrossRankType rankType);

	public static CrossTargetType getType(int type) {
		for (CrossTargetType targetType : values()) {
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

