package com.hawk.game.service.guildtask;

/**
 * 联盟任务类型枚举类
 * 
 * @author Jesse
 *
 */
public enum GuildTaskType {
	
	/** 今日{1}个联盟成员登录 */
	member_login(100),
	/** 今日个联盟成员帮助次数{1} */
	guild_help(101),
	/** 今日完成集结战斗次数{1} */
	guild_mass_atk(102),
	/** 今日进行部队援助次数{1} */
	guild_assist(103),
	/** 今日进行联盟科技捐献次数{1} */
	guild_donate(104),
	/** 今日采集{1}类型资源数量{2}  例:0_100(采集所有资源100)1007_1008_100(采集100的矿石或石油);*/  
	resource_collect(105),
	/** 今日与非本盟成员战斗时消灭的总战力{1} */
	kill_enemy(106),
	/** 今日消灭野怪总数量{1} */
	kill_monster(107),
	/** 今日联盟成员提升总战力{1}*/
	battle_point_increase(108),
	/** 今日领取联盟宝藏数量{1} (挖掘&帮助)*/
	guild_storehouse(109),
	/** 今日联盟成员提升建筑等级{1}*/
	building_up(110),
	/** 今日{1}个联盟成员分享*/
	guild_share(111),
	;

	private int taskType;

	private GuildTaskType(int taskType) {
		this.taskType = taskType;
	}

	/**
	 * 任务类型
	 * 
	 * @return
	 */
	public int intValue() {
		return taskType;
	}

	/**
	 * 任务类型 TaskType
	 * 
	 * @param type
	 * @return
	 */
	public static GuildTaskType valueOf(int type) {
		for (GuildTaskType taskType : values()) {
			if (taskType.intValue() == type) {
				return taskType;
			}
		}
		return null;
	}
}
