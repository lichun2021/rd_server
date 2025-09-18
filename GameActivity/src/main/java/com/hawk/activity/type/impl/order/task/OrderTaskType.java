package com.hawk.activity.type.impl.order.task;

public enum OrderTaskType {

	/** 跨服消灭敌军 cnt */
	BEAT_BATTLE_CROSS(1001),
	/** 最终攻占总统府 cnt */
	OCCUPY_PRESIDENT_FINALLY(1002),
	/** 开启潘多拉宝箱次数 cnt */
	OPEN_PANDORA_BOX(1003),
	/** 激活永久皮肤 */
	ACTIVE_FOREVER_SKIN(1004),
	/** 激活英雄 quality_quality..._cnt */
	ACTIVE_HERO(1005),
	/** 购买月卡 cnt */
	BUY_MONTH_CARD(1006),
	/** 进入总统府 cnt */
	ENTER_PRESIDENT(1007),
	/** 进攻跨服玩家基地 cnt */
	ATK_CITY_CROSS(1008),
	/** 使用技能 id_cnt */
	USE_SKILL(1009),
	/** 最终占领战区 cnt */
	OCCUPY_SUPER_WEAPON_FINALLY(1010),
	/** 联盟军演获得积分奖励 cnt */
	LMJY_GET_AWARD(1011),
	/** 冠军试炼分数达成 score_cnt */
	CR_SCORE(1012),
	/** 能量源收集 cnt */
	SUPER_LAB_COLLECT(1013),
	/** 战胜迷雾要塞 cnt */
	ATTACK_FOGGY_WIN(1014),
	/** 荣耀勋章收集 cnt */
	HONOR_COLLECT(1015),
	/** 战胜幽灵兵营 lvl_lvl..._cnt */
	ATTACK_STRONGPOINT_WIN(1016),
	/** 抽取英雄/芯片 type_cnt */
	GACHA(1017),
	/** 使用道具加速 cnt */
	USE_ITEM_SPEED_UP(1018),
	/** 击杀野怪 lvlmin_lvlmax_cnt */
	KILL_OLD_MONSTER_LEVEL(1019),
	/** 训练士兵 id_id..._cnt */
	TRAIN_SOLDIER_COMPLETE_NUM(1020),
	/** 进入战区 cnt */
	ENTER_SUPER_WEAPON(1021),
	/** 尤里复仇奖励达到x id */
	YURI_REVENGE_REWARD(1022),
	/** 科技升级 cnt */
	TECH_LVL_UP(1023),
	/** 科技升级战力 num */
	TECH_POWER_UP(1024),
	/** 建筑升级 cnt */
	BUILD_LVL_UP(1025),
	/** 建筑升级战力 num */
	BUILD_POWER_UP(1026),
	/** 军需补给 cnt */
	WISHING_TIMES(1027),
	/** 资源采集 type_num */
	RESOURCE_COLLECT(1028),
	/** 货币消耗(金条/金币) 货币类型_num */
	GOLD_CONSUME(1029),
	/** 转时空轮盘次数 cnt */
	DO_ROULETTE(1030),
	/** 泰伯利亚个人积分 num*/
	TW_SCORE(1031),
	/** 赛博之战个人积分 num*/
	CW_SCORE(1032),
	/** 机甲觉醒集结攻击 num*/
	MACHINE_AWAKE_TWO_MASS(1033),
	/** 机甲觉醒致命一击 num*/
	MACHINE_AWAKE_TWO_KILL(1034),
	/** 联盟捐献 num*/
	GUILD_DONATE(1035),
	/** 挖掘联盟宝藏 num*/
	GUILD_STORE(1036),
	/** 领取联盟礼物 */
	GUILD_GIFT(1037),
	/** 工会帮助*/
	GUILD_HELP(1038),
	/** 特惠商店购买*/
	TRAVEL_SHOP_BUY(1039),
	/** 每日必买礼包 购买次数*/
	DAILY_CONSUME_GIFT_BUY_TIMES(1040),
	/** 泰伯利亚参与次数*/
	TLW_JOIN(1041),
	/** 赛博参与次数*/
	CLW_JOIN(1042),
	/** 陨晶参与次数*/
	DYZZS_JOIN(1043),
	/** 统帅参与次数*/
	SLW_JOIN(1044),
	/** 月球参与次数*/
	YQZZ_JOIN(1045),
	/** 陨晶单次分数*/
	DYZZS_SCORE(1046),
	/** 统帅消灭战力*/
	SLW_KILL(1047),
	/** 统帅阵亡战力*/
	SLW_DEAD(1048),
	/** 月球单次个人军功*/
	YQZZ_MILITARY(1049),
	/** 赛博段位*/
	CW_GRADE(1050),
	/** 陨晶段位*/
	DYZZ_GRADE(1051),
	/** 泰伯利亚联赛个人积分*/
	TLW_SCORE(1052),
	/** 赛博联赛个人积分*/
	CLW_SCORE(1053),
	/** 星海激战联赛积分*/
	XLW_SCORE(1054),
	/** 星海激战参与次数*/
	XLW_JOIN(1055),
	/** 星海激战积分*/
	XW_SCORE(1056),
	/** 航海-跨出*/
	CS_OUT(1057),
	/** 航海-玩家积分*/
	CS_SELF_SCORE(1058),
	/** 航海-联盟积分*/
	CS_GUILD_SCORE(1059),
	/** 参与先驱*/
	XQLW_JOIN(1060),
	/** 先驱积分*/
	XQLW_SCORE(1061),
	;

	OrderTaskType(int type) {
		this.type = type;
	}

	private int type;

	public int getType() {
		return type;
	}

	public static OrderTaskType getType(int type) {
		for (OrderTaskType targetType : values()) {
			if (targetType.type == type) {
				return targetType;
			}
		}
		return null;
	}

}

