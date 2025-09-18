package com.hawk.game.module.obelisk.service.mission;

import java.util.EnumSet;

public enum ObeliskMissionType {
	/** 全服X名指挥官大本达到N级*/
	WORLD_PLAYER_BUILD_LEVEL(1, 1),
	/** 全服共击杀N支X级以上野外行军*/
	WORLD_KILL_LEVEL_MONSTER(2,2),

	/** 联盟共击杀N支X级以上野外行军*/
	GUIlD_KILL_LEVEL_MONSTER(3,3),

	/** 联盟共击杀N支X级以上野外行军*/
	GUIlD_KILL_LEVEL_MONSTER2(4,4),

	/** 全服有N个联盟达到X人 */
	GUILD_MEMBER(5,5),

	/** 全服N个玩家战力达到X */
	WORLD_PLAYER_POWER(6,6),

	/** 全服共有N个X级小战区被占领*/
	WORLD_XZQ_OCCUPIED_NUM(7,7),

	/** 联盟共有N个X级小战区被占领*/
	GUIlD_XZQ_OCCUPIED_NUM(8,8),

	/** 全服共击杀N支X级以上幽灵基地  */
	WORLD_KILL_FOGGY(9,9),

	/** 联盟共击杀N支X级以上幽灵基地*/
	GUIlD_KILL_FOGGY(10,10),

	/** 联盟共击杀N支X级以上幽灵基地 非累计型*/
	GUIlD_KILL_FOGGY2(11,11),

	/** 所在联盟 首占N个战区*/
	GUIlD_FIRST_OCCUPIED_SUPER_WEAPON(12,12),

	/** 所在联盟 占领N个战区*/
	GUIlD_OCCUPIED_SUPER_WEAPON(13,13),

	/** 所在联盟进入联盟战力排行榜前X 名*/
	GUIlD_POWER_RANK(14,14),

	/** 所在联盟占领盟总  EventControlPresident*/
	GUIlD_PRESIDENT(15,15),

	/** 个人进入个人战力排行榜前 N名*/
	PLAYER_POWER_RANK(16,16),

	/** 所在联盟去其他服占领盟总,战斗不在本服进行  */
	GUIlD_CROSS_PRESIDENT(17,17),

	/** 所在联盟参加赛博  */
	GUIlD_CYBORG_WAR(18,18),

	/** 所在联盟参加泰伯  */
	GUIlD_TIBERIUM_WAR(19,19),

	/** 全服共击杀N支X级以上野外行军, 非累计型*/
	WORLD_KILL_LEVEL_MONSTER_TWO(20,20),
	/** 国家进行到xx阶段*/
	WorldNationStatus(21,21)
	;

	/**
	 * 任务类型
	 */
	private int type;
	/**
	 * tLog日志记录任务类型
	 */
	private int logType;

	/**
	 *
	 * @param type
	 * @param logType
	 */
	private ObeliskMissionType(int type, int logType) {
		this.type = type;
		this.logType = logType;
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
	public static ObeliskMissionType valueOf(int type) {
		for (ObeliskMissionType obeliskMissionType : EnumSet.allOf(ObeliskMissionType.class)) {
			if (obeliskMissionType.intValue() == type) {
				return obeliskMissionType;
			}
		}
		return null;
	}
}
