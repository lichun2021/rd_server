package com.hawk.activity.type.impl.evolution.task;

public enum EvolutionTaskType {

	// 分享多少次
	SHARE_NUM(2101),
	// 打怪多少次
	KILL_OLD_MONSTER_LEVEL(2102),
	// 采集资源多少点
	RES_COLLECT(2103),
	// 集结战胜幽灵基地多少次  （幽灵基地=箭塔，幽灵兵营=据点）
	MASS_ATTACK_FOGGY_WIN(2104),
	// 帮助盟友多少次
	GUILD_HELP_NUM(2105),
	// 训练部队多少个
	TRAIN_SOLDIER_COMPLETE_NUM(2106),
	// 累计充值多少元
	RECHARGE_COUNT(2107),
	/**  偷取X次（开始偷就算）					配置格式：消耗数量*/
	MEDALF_TOU(2108),
	;

	EvolutionTaskType(int type) {
		this.type = type;
	}

	private int type;

	public int getType() {
		return type;
	}

	public static EvolutionTaskType getType(int type) {
		for (EvolutionTaskType targetType : values()) {
			if (targetType.type == type) {
				return targetType;
			}
		}
		return null;
	}

}

