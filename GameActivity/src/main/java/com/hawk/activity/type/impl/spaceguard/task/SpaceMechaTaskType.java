package com.hawk.activity.type.impl.spaceguard.task;

/**
 * 星甲召唤活动任务类型
 * 
 */
public enum SpaceMechaTaskType {
	/** 解锁英雄 */
	ACTIVE_HERO(1004),
	/** 攻打幽灵基地胜利 cnt */
	ATTACK_FOGGY_WIN(1014),
	/** 击杀野怪 lvlmin_lvlmax_cnt */
	KILL_OLD_MONSTER_LEVEL(1019),
	/** 训练士兵 id_id..._cnt */
	TRAIN_SOLDIER_COMPLETE_NUM(1020),
	/** 资源采集 type_num */
	RESOURCE_COLLECT(1028),
	/** 联盟捐献 num*/
	GUILD_DONATE(1035),
	/** 每日登录 */ 
	DAILY_LOGIN(1036),
	/** 每日完成活跃任务X档位 */
	DAILY_ACTIVE_SCORE(1037),
	;

	SpaceMechaTaskType(int type) {
		this.type = type;
	}

	private int type;

	public int getType() {
		return type;
	}

	public static SpaceMechaTaskType getType(int type) {
		for (SpaceMechaTaskType targetType : values()) {
			if (targetType.type == type) {
				return targetType;
			}
		}
		return null;
	}

}

