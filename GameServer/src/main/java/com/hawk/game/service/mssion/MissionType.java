package com.hawk.game.service.mssion;

import java.util.EnumSet;

/**
 * 任务类型枚举类
 * 
 * @author golden
 *
 */
public enum MissionType {

	/** 升级{1}类型建筑到{2}级 */
	MISSION_BUILD_UPGRADE(0, 1),

	/** 建造{1}类型建筑{2}个 */
	MISSION_BUILD_CREATE(1, 2),

	/** 加入联盟{2}次 */
	MISSION_GUILD_JOIN(7, 3),

	/** 研究{1}id的科技{2}次*/
	MISSION_TECHNOLOGY_STUDY_TIMES(11, 4),

	/** 训练{1}id的士兵{2}个 */
	MISSION_SOLDIER_TRAIN(20, 5),
	
	/** 解锁{1}areaId的地块{2}个 */
	MISSION_UNLOCK_GROUND(21, 6),
	
	/** 拥有{1}id的士兵{2}个 */
	MISSION_HAVE_SOLIDER(22, 7),
	
	/** 联盟宝藏挖掘 */
	MISSION_GUILD_EXCAVTE(25, 8),
	
	/** 联盟个人积分 */
	MISSION_GUILD_SCORE(27, 9),
	
	/** 科技升级 */
	MISSION_TECHNOLOGY_UPGRADE(28, 10),
	
	/** 联盟捐献 */
	MISSION_GUILD_CONTRIBUTE(29, 11),
	
	/** 资源生产率 */
	MISSION_RESOURCE_PRODUCTION_RATE(30, 12),

	/** 采集资源数目 */
	MISSION_RESOURCE_COLLECT_COUNT(31, 13),
	
	/** 联盟交易(资源援助)*/
	MISSION_GUILD_DEAL(32, 14),
	
	/** 玩家升级 */
	MISSION_PLAYER_UPGRADE(40, 15),
	
	/** 增加战力至x点 */
	MISSION_POWER_CREATE(41, 16),

	/** 攻击野怪 */
	MISSION_MONSTER_ATTACK(51, 17),
	
	/** 开始采集资源 */
	MISSION_RESOURCE_COLLECT_BEGIN(53, 18),

	/** 战斗(攻打玩家大本) */
	MISSION_PVP(60, 19),
	
	/** 升级天赋 */
	MISSION_TALENT_UPGRADE(90, 20),

	/** 升级英雄 */
	MISSION_HERO_UPGRADE(100, 21),
	
	/** 升级x个英雄到n级 */
	MISSION_ANY_HERO_UPGRADE(102, 22),
	
	/** 联盟帮助*/
	MISSION_GUILD_HELP(111, 23),
	
	/** 每日登陆 */
	MISSION_LOGIN(140, 24),
	
	/** 训练士兵(开始训练) */
	MISSION_SOLDIER_TRAIN_START(150, 25),
	
	/** rts关卡任务*/
	MISSION_PLOT_BATTLE(160, 26),
	
	//---------  begin: mission type add 2018/01/10
	
	/** 拥有{1}类型{1}等级以上建筑{2}个  (需要初始化)*/
	MISSION_HAVE_BUILD_LEVEL_COUNT(200, 27),
	
	/** 拥有{1}星以上英雄{2}个 (需要初始化)*/
	MISSION_HAVE_HERO_STAR_COUNT(201, 28),
	
	/** 拥有{1}等级以上英雄{2}个 (需要初始化)*/
	MISSION_HAVE_HERO_LVL_COUNT(202, 29),
	
	/** 升级{1}英雄属性{2}次 */
	MISSION_HERO_ATTR_UP(203, 30),
	
	/** 探索等级{1}以上的尤里实验室{2}次*/
	MISSION_EXPLORE_YURI_TIMES(204, 31),
	
	/** 攻击{1}等级以上据点{2}次*/
	MISSION_ATTACK_STRONGPOINT_TIMES(212, 32),
	
	/** 研究{1}类型科技{2}次数(大类型 军事 发展 资源 城防)*/
	MISSION_TECHNOLOGY_TYPE_STUDY_TIMES(206, 33),
	
	/** 打造{1}装备{2}件*/
	MISSION_FORGE_EQUIP_COUNT(207, 34),
	
	/** 指挥官穿戴{1}等级以上的装备{2}次    (废弃)*/
	MISSION_PUT_ON_COMMANDER_EQUIP_TIMES(208, 35),
	
	/** 英雄穿戴{1}等级{1}品质的装备{2}件*/
	MISSION_PUT_ON_HERO_EQUIP_COUNT(209, 36),
	
	/** 联盟商店购买{1}道具{2}次*/
	MISSION_BUY_ITEM_IN_GUILD_SHOP_TIMES(210, 37),
	
	/** 击杀{1}等级以上的野怪{2}次*/
	MISSION_KILL_MONSTER_TIMES(211, 38),
	
	/** 攻击玩家据点{2}次*/
	MISSION_ATTACK_PLAYER_STRONGPOINT_TIMES(205, 39),
	
	/** 占领{1}等级以上的据点{2}时长*/
	MISSION_OCCUPY_STRONGPOINT_TIME(213, 40),
	
	/** 拥有{1}级装备{2}个 (需要初始化)*/
	MISSION_EQUIP_LVL_COUNT(214, 41),
	
	/** 拥有{1}阶装备{2}个  (需要初始化)*/
	MISSION_EQUIP_QUALITY_COUNT(215, 42),
	
	
	/** 抢夺{1}资源{2}点*/
	MISSION_GRAB_RESOURCE_COUNT(217, 43),
	
	/** 攻占{1}资源点{2}次*/
	MISSION_ATTACK_COLLECT_TIMES(218, 44),
	
	/** 消灭{1}类型敌军{2}个*/
	MISSION_KILL_SOLIDER_COUNT(219, 45),
	
	/** 科技{1}战力达到{2}*/
	MISSION_TECH_TYPE_POWER(220, 46),
	
	/** 指挥官穿戴{1}等级{1}品质的装备{2}件*/
	MISSION_COMMANDER_PUT_ON_EQUIP_COUNT(221, 47),
	
	/** 攻击玩家城点并胜利{2}次*/
	MISSION_ATTACK_PLAYER_CITY_TIMES(222, 48),
	
	/** 城内收集{1}资源{2}点 */
	MISSION_RESOURCE_PRODUCTION(223, 49),
	//---------  end: mission type add 2018/01/10
	
	/** 开始研究{1}id的科技{2}次*/
	MISSION_START_TECH__TIMES(224, 50),
	
	/** 攻击{1}等级的新版野怪{2}次*/
	MISSION_ATTACK_NEW_MONSTER(225, 51),
	/** 击杀{1}等级的新版野怪{2}次*/
	MISSION_ATTACK_NEW_MONSTER_WIN(226, 52),
	
	/** 升星{1}英雄到{2}星级 (需要初始化)*/
	MISSION_HERO_STAR_UP(227, 53),
	
	/** 攻击{1}等级以上的迷雾要塞胜利{2}次*/
	MISSION_ATK_FOGGY_WIN_TIMES(228, 54),
	
	/** {1}英雄驻防{2}次*/
	MISSION_HERO_GARRISON(229, 55),
	
	/** 向{1}等级野怪发起出征{2}次*/
	GEN_OLD_MONSTER_MARCH(230, 56),
	
	/** 攻击{1}等级以上据点胜利{2}次*/
	MISSION_ATTACK_STRONGPOINT_WIN_TIMES(231, 57),
	
	/** 抵御{1}类型幽灵行军{1}次*/
	DEFENCE_GHOST_STRIKE_WIN(232, 58),
	
	/** 治疗伤兵{1}个*/
	TREAT_ARMY(233, 59),
	
	/** 集结击杀士兵{1}个*/
	MASS_KILL_SOLIDER(234, 60),
	
	/** 援助击杀士兵{1}个*/
	ASSISTANCE_KILL_SOLIDER(235, 61),
	
	/** 陷阱击杀士兵{1}个*/
	TRAP_KILL_SOLIDER(236, 62),
	
	/** 累积登录*/
	ACCUMULATE_LOGIN(237, 63),
	
	/** 主动迁城*/
	INITIATIVE_MOVE_CITY(238, 64),
	
	/** 联盟宝藏帮助挖掘 */
	MISSION_GUILD_EXCAVTE_HELP(239, 65),
	
	/** 联盟反击 */
	MISSION_COUNTER_ATTACK(240, 66),
	
	/** 集结攻击{1}等级以上的迷雾要塞胜利{2}次*/
	MISSION_MASS_ATK_FOGGY_WIN_TIMES(241, 67),

	/** 使用资源增产道具n次*/
	MISSION_UPGRADE_RESOURCE_PRODUCTOR(242, 68),
	
	/** n块资源田同时增产*/
	MISSION_UPGRADE_RESOURCE_FIELD_COUNT(243, 69),
	
	/** 拥有{1}品质{2}星级{3}等级以上英雄{4}个 (需要初始化)*/
	MISSION_HAVE_HERO_COUNT(244, 70),
	
	/** 英雄组合*/
	MISSION_HAVE_HERO_GROUP(245, 71),
	
	/** 所有英雄镶嵌{1}等级{2}品质芯片*/
	MISSION_ALL_HERO_INSTALL_SKILL(246, 72),
	
	/** 单个英雄镶嵌{1}等级{2}品质芯片*/
	MISSION_SINGEL_HERO_INSTALL_SKILL(247, 73),
	
	/** 许愿{1}次*/
	MISSION_WISHING(248, 74),
	
	/** 消耗金币{1}个*/
	CONSUM_MONEY(249, 75),
	
	/** 全服玩家第一个达到{1}等级{2}个*/
	MISSION_SOLE_PLAYER_LEVEL(250, 76),
	
	/** 全服第一次占领超级武器*/
	MISSION_SOLE_SUPER_WEAPON(251, 77),
	
	/** 全服第一次占领盟军司令部*/
	MISSION_SOLE_PRESIDENT(252, 78),
	
	/** 全服第一次击杀野怪*/
	MISSION_SOLE_KILL_MONSTER(253, 79),
	
	/** 全服第一次击杀{1}等级据点{2}次*/
	MISSION_SOLE_STRONGPOINT_WIN_TIMES(254, 80),
	
	/** 占领能量塔次数*/
	MISSION_OCCUPY_PYLON_TIMES(255, 81),
	
	/** 拥有{1}星级的士兵{2}个 */
	MISSION_HAVE_STAR_SOLIDER(256, 82),
	
	/** 消灭{1}等级士兵{2}个*/
	MISSION_KILL_LEVEL_SOLIDER_COUNT(257, 83),
	
	/**
	 * 在电塔中死兵
	 */
	MISSION_DEAD_IN_PRESIDENT_TOWER(258, 84),
	/**
	 * 在电塔中伤兵
	 */
	MISSION_HURT_IN_PRESIDENT_TOWER(259, 85),
	/**
	 * 在总统府死兵
	 */
	MISSION_DEAD_IN_PRESIDENT(260, 86),
	/**
	 * 在总统府伤兵
	 */
	MISSION_HURT_IN_PRESIDENT(261, 87),
	/**
	 * 在电塔中占领x分钟
	 */
	OCCUPY_PRESIDENT_TOWER_MINUTE(262, 88),
	
	/**
	 * 通过幽灵塔第{1}级{2}层
	 */
	GHOST_TOWER_PASS(263,89),
	/** 
	 * 完成情报任务{0}次
	 */
	AGENCY_MISSION_FINISH_INIT(264, 90), 
	/** 
	 * 完成{0}情报任务{1}次
	 */
	MISSION_AGENCY_TIMES(265, 91),
	/** 
	 * 领取每日任务宝箱{0}次
	 */
	DAILY_MISSION_BOX_INIT(266, 92),
	/** 
	 * 升级(正在升级){0}建筑{1}次
	 */
	BUILD_UPLEVEL_DOING(267, 93),
	/** 
	 * 抽{0}类型的卡{1}次
	 */
	GACHA(268, 94),
	/**
	 * 指定机甲{0}所有部件升至{1}级
	 */
	MECHA_PART_LEVELUP(269, 95),
	/**
	 * 获得指定道具{0}{1}个
	 */
	GAIN_ITEM(270, 96),
	/**
	 * 开始采集指定资源类型的矿点{0}{1}次
	 */
	WORLD_COLLECT_START(271, 97),
	
	/** 拥有{0}类型{1}等级建筑{2}个 */
	MISSION_BUILD_COUNT_LEVEL(272, 98),
	
	/** 向{1}等级野怪发起出征{2}次*/
	GEN_OLD_MONSTER_MARCH_CUMULATIVE(10230, 99),
	/** 攻击{1}等级以上的迷雾要塞胜利{2}次*/
	MISSION_ATK_FOGGY_WIN_TIMES_CUMULATIVE(10228, 100),
	/** 完成情报任务{0}次 */
	AGENCY_MISSION_FINISH_INIT_CUMULATIVE(10264, 101), 
	/** 开始采集指定资源类型的矿点{0}{1}次  */
	WORLD_COLLECT_START_CUMULATIVE(10271, 102),
	/** 训练士兵(开始训练) */
	MISSION_SOLDIER_TRAIN_START_CUMULATIVE(10150, 103),
	/** 联盟帮助*/
	MISSION_GUILD_HELP_CUMULATIVE(10111, 104),
	/** 联盟捐献 */
	MISSION_GUILD_CONTRIBUTE_CUMULATIVE(10029, 105),
	/** 联盟宝藏挖掘 */
	MISSION_GUILD_EXCAVTE_CUMULATIVE(10025, 106),
	
	/** 修复机甲某个部件（参数是部件ID）*/
	MECHA_PART_REPAIR(273, 107),

	/** 解锁机甲，参数=机甲id */
	MECHA_UNLOCK(274, 108),
	/** A机甲进行B次  A=机甲id（任意用0），B=进阶次数 */
	MECHA_ADVANCE(275,109),
	/**
	 * 指定机甲{0}至少有一个部件升至{1}级
	 */
	MECHA_PART_LEVELUP_MAX(276, 110),
	/** 认识泰能*/
	PLANT_SOLDIER_SEE(277,111),
	/**泰能研究台升满*/
	PLANT_INSTRUMENT_MAX(278,112),
	/**解锁任一太能战士*/
	PLANT_SOLDIER_CRACK_MAX(279,113),
	/** 进行一次泰能战士进化*/
	PLANT_SOLDIER_ADVANCE(280,114),
	/**泰能晶体满升满*/
	PLANT_CRYSTAL_MAX(281,115),
	/** 泰能战士一阶*/
	PLANT_SOLDIER_STEP_ONE(282,116),
	/** 泰能战士5阶*/
	PLANT_SOLDIER_STEP_MAX(283,117),
	/** 使用加速道具x分钟*/
	ITEM_SPEED(284, 118),
	/** 训练x类型士兵x个*/
	SOLDIER_TRAIN_TYPE(285, 119),
	/**
	 * 在要塞中占领x秒(电塔每5s计算一次)
	 */
	OCCUPY_CROSS_FORTRESS_SECOND(286, 120),
	/** 泰能研究所的XX生产线达到XX级： type = 290，val1 = 泰能生产线类型（1-4） ，val2 = 泰能生产线等级  */
	PLANT_FACTOFY_LEVEL(290, 121),
	/** 军事委任XX个英雄： type = 291 ，val1 = 空 ，val2 = 委任数量  */
	HERO_APPOINT_COUNT(291, 122),
	;

	/**
	 * 任务类型
	 */
	private int missionType;
	/**
	 * tlog日志记录任务类型
	 */
	private int logMissionType;

	/**
	 * 构造
	 * 
	 * @param missionType
	 */
	private MissionType(int missionType, int logMissionType) {
		this.missionType = missionType;
		this.logMissionType = logMissionType;
	}

	/**
	 * 任务类型  int
	 * 
	 * @return
	 */
	public int intValue() {
		return missionType;
	}
	
	/**
	 * 日志记录任务类型
	 * 
	 * @return
	 */
	public int logMissionTypeVal() {
		return logMissionType;
	}

	/**
	 * 任务类型  MissionType
	 * 
	 * @param type
	 * @return
	 */
	public static MissionType valueOf(int type) {
		for (MissionType missionType : EnumSet.allOf(MissionType.class)) {
			if (missionType.intValue() == type) {
				return missionType;
			}
		}
		return null;
	}
}
