package com.hawk.game.service.college;

import java.util.EnumSet;

public enum CollegeMissionType {
	LOGIN_MEMBERS(1), // 35901 1. 学院当日中累计上线成员人数
	VIT_COST(2), // 35902 2. 消耗xx体力
	ONLINEGIFT_TAKE(3);// 35903 3. 领取玩家在线奖励xx次

	/**
	 * 任务类型
	 */
	private int type;

	/**
	 * tLog日志记录任务类型
	 */

	/**
	 *
	 * @param type
	 * @param logType
	 */
	private CollegeMissionType(int type) {
		this.type = type;
	}

	/**
	 * 任务类型  int
	 *
	 * @return
	 */
	public int intValue() {
		return type;
	}

	/**
	 * 任务类型  MissionType
	 *
	 * @param type
	 * @return
	 */
	public static CollegeMissionType valueOf(int type) {
		for (CollegeMissionType obeliskMissionType : EnumSet.allOf(CollegeMissionType.class)) {
			if (obeliskMissionType.intValue() == type) {
				return obeliskMissionType;
			}
		}
		return null;
	}
}
