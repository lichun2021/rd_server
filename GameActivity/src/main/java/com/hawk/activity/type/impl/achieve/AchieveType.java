package com.hawk.activity.type.impl.achieve;

import java.util.HashMap;
import java.util.Map;
import org.hawk.log.HawkLog;
import com.hawk.activity.type.impl.achieve.datatype.AchieveData;
import com.hawk.activity.type.impl.achieve.datatype.KeyValueData;
import com.hawk.activity.type.impl.achieve.datatype.ListValueData;
import com.hawk.activity.type.impl.achieve.datatype.ValueData;

/**
 * 成就条件类型
 * @author PhilChen
 *
 */
public enum AchieveType {

	/** 建筑升级  						配置格式：建筑类型_等级*/
	BUILD_LEVEL_UP(100, new ListValueData()),
	/** 建筑升级（新成长基金）    			配置格式：建筑类型_等级*/
	BUILD_LEVEL_UP_NEW(200, new ListValueData()),
	
	/** 建造某个建筑多少个  				配置格式：建筑类型_个数*/
	BUILD_NUM(1100, new KeyValueData()),
	/** 拥有兵多少个 						配置格式：兵种id（0表示任意兵种）_数量*/
	TRAIN_SOLDIER_HAVE_NUM(21100, new KeyValueData()),
	/** 某个兵种训练（开始训练）多少个  	配置格式：兵种id（0表示任意兵种）_数量*/
	TRAIN_SOLDIER_START_NUM(22100, new KeyValueData()),
	/** 某个兵种训练（训练完成）多少个  	配置格式：兵种id（0表示任意兵种）_训练数量*/
	TRAIN_SOLDIER_COMPLETE_NUM(20100, new KeyValueData()),
	/** 某类兵种训练（训练完成）多少个  	配置格式：兵种类型_数量*/
	TRAIN_SODIER_TYPE_NUMBER(23100, new KeyValueData()),
	
	/** 采集某种资源达到多少  				配置格式：采集资源类型_数量  1007:矿石 1008:石油 1009:钢铁 1010:合金*/
	RESOURCE_COLLECT(31100, new ListValueData()),
	/** 城内收集某种资源数量达到多少 		配置格式：采集资源类型_数量*/
	RESOURCE_COLLECT_IN_CITY(32100, new KeyValueData()),
	/** 某种资源产出率达到多少  			配置格式：资源类型_产出率*/
	RESOURCE_RATE(30100, new KeyValueData()),
	/** 培养英雄*/
	USE_HERO_EXPITEM(350100, new ListValueData()),
	/** 合成装备材料*/
	EQUIP_MATERIAL_MERGE(360100, new ListValueData()),
	/** 装备打造/升阶/升品*/
	EQUIP_UP(370100, new ListValueData()),
	/** 指挥官升级  						配置格式：等级*/
	PLAYER_LEVEL_UP(40100, new ValueData()),
	/** 战力数值  						配置格式：战力值*/
	BATTLE_POINT(41100, new ValueData()),
	/*********只积累增长值,不扣除降低值*************/
	/** 总战力提升  						配置格式：战力值*/
	POWER_UP_TOTAL(42000, new ValueData()),
	/** 建筑战力提升  					配置格式：战力值*/
	POWER_UP_BUILDING(42100, new ValueData()),
	/**科技战力提升  						配置格式：战力值*/
	POWER_UP_TECH(42200, new ValueData()),
	/** 部队战力提升  					配置格式：战力值*/
	POWER_UP_SOLDIER(42300, new ValueData()),
	/** 英雄战力提升  					配置格式：战力值*/
	POWER_UP_HERO(42400, new ValueData()),
	/*********只积累增长值,不扣除降低值*************/
	
	/** PVP-战斗胜利次数  				配置格式：次数*/
	PVP_BATTLE_WIN_NUM(60100, new ValueData()),
	/** PVP-进攻玩家次数  				配置格式：次数*/
	PVP_BATTLE_NUM(61100, new ValueData()),
	/** 英雄升级  						配置格式：英雄id(0表示任意英雄)_等级*/
	HERO_LEVEL_UP(100100, new KeyValueData()),
	/** 英雄升星 						配置格式：等级*/
	HERO_STAR_UP(100101, new ValueData()),
	/** 指定英雄达到x星 					配置格式：英雄id_星级*/
	HERO_STAR_UP_TWO(100102, new KeyValueData()),
	/** ：送花积分数量*/
	SONG_HUA(104000, new ValueData()),
	/** 联盟交易援助指定资源类型{0}		配置格式：资源ID_数量*/
	GUILD_ASSISTANCE_RES_NUM(110100, new KeyValueData()),
	/** 联盟帮助  						配置格式：次数*/
	GUILD_HELP(111100, new ValueData()),
	/** 联盟捐献 						配置格式：捐献数量*/
	GUILD_DONATE(112100, new ValueData()),
	/** 治疗伤兵(完成治疗)  				配置格式：治疗数量 */
	TREAT_ARMY(120100, new ValueData()),
	/** 抽取英雄  						配置格式：次数*/
	RANDOM_HERO(130100, new ValueData()),
	
	/** 玩家拥有并已分享的英雄x个			配置格式: 数量  */
	HERO_SHARE_NUM(131100, new ValueData()),
	/** 总共拥有英雄{0}级{1}品质{2}星级的英雄{3}个  配置格式: 等级_品质_星级_数量  (等级/品质/星级配0标识任意条件)*/
	HERO_HAVE_NUM(131200, new ListValueData()),
	/** 玩家拥有并已分享指定英雄1次		配置格式: 英雄id_1  */
	HERO_SHARE(131300, new KeyValueData()),
	
	/** 累计登录(八日庆典)  						配置格式：登录天数*/
	LOGIN_DAYS_FESTIVAL(140100, new ValueData()),
	/** 累计登录(八日庆典2)  					配置格式：登录天数*/
	LOGIN_DAYS_FESTIVAL_TWO(140101, new ValueData()),
	/** 累计登录(登录基金)						配置格式：登录天数*/
	LOGIN_DAYS_FUND(140200, new ValueData()),
	/** 十连抽活动抽奖次数						配置格式：抽奖次数*/
	LOTTERY_DRAY_TIMES(140300, new ValueData()),
	/** 累计登录(战地福利)						配置格式：登录天数*/
	LOGIN_DAYS_WARZONE_WEAL(140400, new ValueData()),
	/** 累计登录(累计登陆活动,按注册时间开启)		配置格式：登录天数*/
	LOGIN_DAY(140500, new ValueData()),
	/** 累计登录(累计登陆活动,按日期配置开启)		配置格式：登录天数*/
	LOGIN_DAY_TWO(140600, new ValueData()),
	/** 豪礼派送累计登录天数 **/
	GIFT_SEND_LOGIN_DAYS(140700, new ValueData()),
	/** 累计登录(军魂承接)		配置格式：登录天数*/
	LOGIN_DAY_INHERIT(140800, new ValueData()),
	/** 累计登录(军事备战) -- 活动累计登录天数成就通用 20240730  	配置格式：登录天数*/
	LOGIN_DAYS_ACTIVITY(140900, new ValueData()),
	/** 累计登录(中秋庆典)  					配置格式：登录天数*/
	LOGIN_DAYS_MID_AUTUMN(141000, new ValueData()),
	/** 累计登录(勋章活动)  					配置格式：登录天数*/
	LOGIN_DAYS_MEDAL_ACTION(141001, new ValueData()),
	/** 累计登录(命运左轮)  					配置格式：登录天数*/
	LOGIN_DAYS_DESTINY_REVOLVER(141002, new ValueData()),
	/** 累计登录(时空豪礼)  					配置格式：登录天数*/
	LOGIN_DAYS_CHRONO_GIFT(141003, new ValueData()),
	/** 累计登录(欢乐购)  					配置格式：登录天数*/
	LOGIN_DAYS_JOYBUY(141004, new ValueData()),	
	/** 累计登录(空投补给登录)  					配置格式：登录天数*/
	LOGIN_DAYS_AIRDROP(141005,new ValueData()),
	/** 累计登录(回流拼图)  					配置格式：登录天数*/
	LOGIN_DAYS_RETURN_PUZZLE(141006,new ValueData()),
	/** 累计登录（预流失干预活动） */
	LOGIN_DAYS_PRESTRESS_LOSS(141007,new ValueData()),
	/** 预流失干预活动打怪 */
	PRESTRESS_LOSS_ATK_MONSTER(141008,new ValueData()),
	/** 金币觅宝累计登陆 */
	LOGIN_DAYS_GOLD_BABY(141009,new ValueData()),
	
	/** 购买礼包  						配置格式：礼包id_购买数量*/
	GIFT_PACK_BUG(150100, new KeyValueData()),
	/** 跨服团购次数累计  					配置格式：全服购买数量*/
	GROUP_PURCHASE_COUNT(150200, new ValueData()),
	/** 累积消耗指定资源(金币/金条){0} 					配置格式：资源id_数量*/
	CONSUME_MONEY(151100, new KeyValueData()),
	/** 加速时间累积  					配置格式：队列类型(0:任意队列  大于0详见{@see Const.QueueType})_时间分钟数*/
	SPEED_UP_TIME_TOTAL(160100, new KeyValueData()),
	/** 累积充值钻石{0} 					配置格式：充值钻石数量*/
	ACCUMULATE_DIAMOND_RECHARGE(160200, new ValueData()),
	/** 累积充值钻石积分{0} 					配置格式：充值钻石获得积分*/
	DIAMOND_RECHARGE_SCORE(160201, new ValueData()),
	/** 充值豪礼RMB                      配置格式：充值人民币 **/
	RECHARGE_GIFT(160300, new ValueData()),
	/** 体力消耗 						配置格式：消耗数量*/
	VITRECEIVE_CONSUME(170100, new ValueData()),
	/** 能量收集活跃积分  */
	ENERGY_GATHER_SCORE(170101, new ValueData()),
	
	/** 八日庆典累计积分  配置格式：积分数量*/
	FESTIVAL_SCORE(180100, new ValueData()),
	/** 酒馆累计积分  配置格式：积分数量*/
	TAVERN_SCORE(180101, new ValueData()),
	/** 八日庆典累计积分  配置格式：积分数量*/
	FESTIVAL_TWO_SCORE(180102, new ValueData()),
	/** 武者拼图累计积分  配置格式：积分数量*/
	FIGHTER_PUZZLE_SCORE(180103, new ValueData()),
	/** 军事战备累计积分  配置格式：积分数量*/
	MILITARY_PREPARE_SCORE(180104, new ValueData()),
	/** 武者拼图累计积分  配置格式：积分数量*/
	RETURN_PUZZLE_SCORE(180105, new ValueData()),
	
	/** 机甲觉醒累计伤害  配置格式：积分数量*/
	AWAKE_DAMAGE(181000, new ValueData()),
	/** 机甲觉醒2(年兽)累计伤害  配置格式：积分数量*/
	AWAKE_TWO_DAMAGE(181001, new ValueData()),
	
	/** 消灭怪物数量						配置格式：怪物id_数量（怪物id为0表示任意怪物） */
	MONSTER_KILL_NUM(200100, new ListValueData()),
	
	/** 签到次数							配置格式：次数*/
	SIGN_IN_TIMES(210100, new ValueData()),
	/** 码头卸载货物次数					配置格式：次数*/
	WHARF_UNLOAD_TIMES(220100, new ValueData()),           // TODO 待添加触发点
	/** 许愿池许愿次数					配置格式：次数*/
	WISHING_TIMES(230100, new ValueData()),
	/**  黑市购买次数					配置格式：次数*/
	TRAVEL_SHOP_PUCHASE_TIMES(240100, new ValueData()),    // TODO
	/**  黑市购用金币买次数					配置格式：次数*/
	TRAVEL_SHOP_PUCHASE_GOLD_TIMES(240200, new ValueData()),    // TODO
	/** 黑市购买黑金礼包次数*/
	TRAVEL_SHOP_BLACK_GOLD_PACKAGE_BUY_TIMES(240300, new ValueData()),
	/** 黑市商人特惠商店消耗数量（只是  特惠商店）*/
	TRAVEL_SHOP_COMMON_COST(240400,new ListValueData()),
	/** 黑市商人金币专区消耗*/
	TRAVEL_SHOP_GOLD_AREA_COST(240500,new ListValueData()),
	/** 黑市商人限时折扣专区消耗*/
	TRAVEL_SHOP_LIMITE_TIME_COST(240600,new ListValueData()),
	/** 黑市商人成就完成*/
	TRAVEL_SHOP_ASSIST_ASSIST_ACHIEVE_FINISH(240700,new ValueData()),
	/** 黑市商人特惠商店购买次数（只是  特惠商店）*/
	TRAVEL_SHOP_COMMON_BUY_TIMES(240800,new ValueData()),
	/** 端午-黑市商人购买成就完成*/
	DRAGON_BOAT_BENEFIT_ACHIEVE_FINISH(240900,new ValueData()),
	
	/** 使用资源道具次数					配置格式：次数*/
	USE_RESOURCE_TOOL_TIMES(250100, new ValueData()),
	/** [酒馆累积型]使用资源道具次数			配置格式：次数*/
	TAVERN_USE_RESOURCE_TOOL_TIMES(250200, new ValueData()),
	/** 合成装备材料次数					配置格式：次数*/
	MATERIAL_COMPOSITION_TIMES(260100, new ValueData()),   // TODO
	/** 拥有{0}品质{1}等级装备{2}个 						配置格式：品质_等级_数量  (品质/等级配0表示任意条件)*/
	HAVE_EQUIP_COUNT(270100, new ListValueData()),
	/** 打造{0}品质{1}等级装备{2}个 						配置格式：品质_等级_数量  (品质/等级配0表示任意条件)*/
	FORGE_EQUIP_COUNT(270200, new ListValueData()),
	/** 分解{0}品质{1}等级装备{2}个 						配置格式：品质_等级_数量  (品质/等级配0表示任意条件)*/
	RESOLVE_EQUIP_COUNT(270300, new ListValueData()),
	
	
	/** 士兵援助次数 						配置格式：最小部队数量_次数*/
	ASSISTANT_TIMES(280100, new KeyValueData()),              // TODO
	/** 建造陷阱数量 						配置格式：陷阱ID_数量（陷阱ID为0表示任意陷阱）*/
	MAKE_TRAP_COUNT(290100, new KeyValueData()),
	
	
	
	/** [酒馆累积型]建造陷阱数量 			配置格式：陷阱ID_数量（陷阱ID为0表示任意陷阱）*/
	TAVERN_MAKE_TRAP_COUNT(290200, new KeyValueData()),
	/** [酒馆累积型]指定列表中的兵种类型累计训练（训练完成）多少个  	配置格式：兵种类型1_兵种类型2_数量*/
	TAVERN_TRAIN_TANK_TYPE_NUMBER(330200, new ListValueData()),
	/** [酒馆累积型]采集某种资源达到多少  		配置格式：采集资源类型_数量  1007:矿石 1008:石油 1009:钢铁 1010:合金*/
	TAVERN_RESOURCE_COLLECT(31200, new KeyValueData()),
	
	/** 消灭怪物*次数(酒馆用)					配置格式：怪物id(0为任意)_次数*/
	MONSTER_KILL_COUNT(300100, new KeyValueData()),
	/** 升级*次建筑  						配置格式：建筑类型(0为任意)_次数*/
	BUILD_LEVEL_UP_COUNT(310100, new KeyValueData()),
	/** 提升资源产量*次(使用道具触发作用号)			配置格式：作用号id_次数*/
	UPGRADE_RESOURCE_PRODUCTOR(320100, new KeyValueData()),
	/** 指定列表中的兵种类型累计训练（训练完成）多少个  	配置格式：兵种类型1_兵种类型2_数量*/
	TRAIN_TANK_TYPE_NUMBER(330100, new ListValueData()),
	/** 升级/研究科技  					配置格式：科技id_次数*/
	TECHNOLOGY_LEVEL_UP(340100, new KeyValueData()),
	/** 升级/研究科技  					配置格式：科技类型（1 普通科技   2  泰能科技），科技ID,次数*/
	TECHNOLOGY_LEVEL_UP_TWO(340200, new KeyValueData()),
	/** 占领据点	多少次					配置格式: 据点等级(0表示任意等级)_次数*/
	OCCUPY_STRONGPOINT_NUM(380100, new ListValueData()),
	/** 占领据点	多长时间					配置格式: 据点等级(0表示任意等级)_时长*/
	OCCUPY_STRONGPOINT_TIME(380200, new ListValueData()),
	/** 攻打据点	多少次					配置格式: 据点等级(0表示任意等级)_次数*/
	ATTACK_STRONGPOINT_NUM(380300, new ListValueData()),
	
	
	/** 开启联盟宝藏n次*/
	GUILD_STORE(500003, new ValueData()),
	/** 使用道具加速n分钟*/
	USE_ITEM_SPEED_UP(500004, new ValueData()),
	/** 采集资源x次*/
	RES_COLLECT_TIMES(500005, new ValueData()),
	/** 某等级兵种训练（训练完成）多少个  	配置格式：等级_训练数量*/
	TRAIN_SOLDIER_LEVEL_COMPLETE_NUM(500006, new KeyValueData()),
	
	/** 攻击{1}等级的新版野怪{2}次			配置格式：怪物等级minLvl_maxLvl_次数*/
	ATTACK_NEW_MONSTER_LEVEL(500007, new ListValueData()),
	/** 攻击{1}等级的老版野怪{2}次			配置格式：怪物等级minLvl_maxLvl_次数*/
	ATTACK_OLD_MONSTER_LEVEL(500008, new ListValueData()),
	/** 攻击{1}等级的野怪{2}次				配置格式：怪物等级minLvl_maxLvl_次数*/
	ATTACK_MONSTER_LEVEL(500009, new ListValueData()),
	/** 击杀{1}等级的新版野怪{2}次			配置格式：怪物等级minLvl_maxLvl_次数*/
	KILL_NEW_MONSTER_LEVEL(500010, new ListValueData()),
	/** 击杀{1}等级的老版野怪{2}次			配置格式：怪物等级minLvl_maxLvl_次数*/
	KILL_OLD_MONSTER_LEVEL(500011, new ListValueData()),
	/** 击杀{1}等级的野怪{2}次				配置格式：怪物等级minLvl_maxLvl_次数*/
	KILL_MONSTER_LEVEL(500012, new ListValueData()),
	
	/** 扭蛋*/
	GACHA(500013, new ListValueData()),
	/** 使用技能*/
	CAST_SKILL(500014, new ListValueData()),
	
	/** 攻打迷雾要塞(尖塔)		配置格式：次数*/
	ATTACK_FOGGY(500019, new ValueData()),
	/** 战胜迷雾要塞(尖塔)*/
	ATTACK_FOGGY_WIN(500020, new ListValueData()),
	/**英雄出征 击杀野怪x次 不计算boss*/
	HERO_MARCH_KILL_MONSTER(500021, new ValueData()),
	/**英雄采集资源 行军到达就算*/
	HERO_MARCH_COLLECT_RESOURCE(500022, new ValueData()),
	/**集结攻击尖塔x次*/
	MASS_ATTACK_FOGGY(500023, new ValueData()),
	/**集结战胜尖塔 x次*/
	MASS_ATTACK_FOGGY_WIN(500024, new ValueData()),
	/**
	 * 限时掉落收集道具
	 */
	TIME_LIMIT_DROP_GET_ITEM(500101, new KeyValueData()),
	/**
	 *联盟反击收集道具
	 */
	ALLY_BEAT_BACK_GET_ITEM(500102, new KeyValueData()),
	/**
	 * 今日发出召唤好友
	 */
	RECALL_FRIEND(50103, new ValueData()),

	/**
	 * 今日发出召唤好友基地等级多少级
	 */
	RECALL_FRIEND_LEVEL(50105, new ListValueData()),

	//召回盟友数
	RECALL_GUILD_FRIEND(50201, new ValueData()),
	/**召回盟友 累积消耗指定资源(金币/金条){0} 					配置格式：资源id_数量*/
	RECALL_GUILD_CONSUME_MONEY(50202, new KeyValueData()),
	/**召回盟友 联盟捐献 						配置格式：捐献数量*/
	RECALL_GUILD_GUILD_DONATE(50203, new ValueData()),
	/**召回盟友 某个兵种训练（训练完成）多少个  	配置格式：兵种id（0表示任意兵种）_训练数量*/
	RECALL_GUILD_TRAIN_SOLDIER_COMPLETE_NUM(50204, new KeyValueData()),
	/**召回盟友 体力消耗 						配置格式：消耗数量*/
	RECALL_GUILD_VITRECEIVE_CONSUME(50205, new ValueData()),

	/**
	 * 召回好友
	 */
	RECALLED_FIREND(50104, new ValueData()),
	
	// 购买超值礼包次数
	SALE_GIFT_PURCHASE_TIMES(600000, new ValueData()),
	// 购买超值礼包消耗金条数量
	SALE_GIFT_PURCHASE_CONSUME(600001, new ValueData()),
	// 购买每日必买礼包消耗
	BUY_ITEM_CONSUME(600002, new ValueData()),
	// 购买价格XX金条的超值礼包次数
	PRICE_SALE_GIFT_PURCHASE_TIMES(600003, new KeyValueData()),
	// 购买特权补给消耗人民币
	MONTH_CARD_PURCHASE_CONSUME(600004, new ValueData()),
	//超能实验室收集道具成就
	POWER_LAB_COLLECT_ACHIEVE(600005, new KeyValueData()),
	//每日任务--分享
	DAILY_SHARE(700000, new ValueData()),
	//老玩家回归累计登录
	COMEBACK_LOGIN_DAYS(700001, new ValueData()),
	
	//分享箱子
	QUESTION_SHARE_TIMES(700002, new ValueData()),
	
	//机甲破世 买宝箱次数
	MACHINE_SELL_TIMES(700003, new ValueData()),
	
	//月签次数
	DAILY_SIGN_TIMES(700004, new ValueData()),
	
	//推广员 被推广一方建筑工厂等级
	SPREAD_CITY_LEVEL(700005,new ValueData()),
	//推广员 被推广一方贵族等级
	SPREAD_VIP_LEVEL(700006,new ValueData()),
	//推广员 新兵建筑工厂等级
	SPREAD_N_CITY_LEVEL(700007,new ValueData()),
	//推广员 新兵贵族等级
	SPREAD_N_VIP_LEVEL(700008,new ValueData()),
	
	//推广员 完成成就 
	SPREAD_FINISH_ACHIEVE(700009,new ValueData()),
	//推广员 新兵累计消耗体力
	SPREAD_VIT_COST(702301,new ValueData()),
	
	//幸运福利登录成就
	LUCKY_WELFARE_LOGIN_ACHIEVE(700010,new ValueData()),
	
	//全副武装登录成就
	FULLY_ARMED_LOGIN_ACHIEVE(700011,new ValueData()),
	//装备强化的成就
	ARMOUR_STRENGTHEN_TIMES(700012, new ValueData()),
	//先锋豪礼累计购买天数
	PIONEER_GIFT_BUY_DAYS(700013, new ValueData()),
	
	/** 指定类型分享x次			配置格式: 分享类型(0表示任意类型)_次数*/
	SHARE_TYPE(700014, new ListValueData()),
	
	//参与时空轮盘次数
	DO_ROULETTE_TIMES(700015, new ValueData()),
	
	// 购买月卡
	BUY_MONTH_CARD(700016, new ValueData()),
	
	//勋章宝藏抽奖次数
	MEDAL_TREASURE_LOTTERY_TIMES(700017, new ValueData()),
	
	//装备科技积分
	EQUIP_TECH_SCORE(700018, new ValueData()),
	//兵营场景分享(场景分享活动  特指4个兵营)
	BARRACKS_SHARE (700020,new ValueData()),
	//体力场景分享(场景分享活动)
	PHYSICAL_POWER_SHARE (700021,new ValueData()),
	// 威龙庆典分享
	WEILONG_QINGDIAN_SHARE(700025,new ValueData()),
	//黑武士.
	SAMURAI_BLACKENED_SCORE(700026, new ValueData()),
	// 欢乐限购
	RED_RECHARGE_SCORE(297101, new ValueData()),
	
	//科技战力值
	TECH_POWER(700029, new ValueData()),
	//能量源数量
	ENERGY_COUNT(700030, new ValueData()),
	//英雄战力值
	HERO_POWER(700031, new ValueData()),
	//机甲战力值
	ARMOR_POWER(700032, new ValueData()),
	//获取装备 品质_数量 
	ACHIVE_EQUIP_QUALIATY_COUNT(700034, new ListValueData()),
	//指挥官学院-助力礼包团购人数
	COMMAND_ACADEMY_GROUP_BUY(700035, new ListValueData()),
	/**
	 * 英雄委任分数.
	 */
	HERO_LOVE_SCORE(700036, new ValueData()),
	/**
	 * 英雄委任登录.
	 */
	HERO_LOVE_LOGIN(700037, new ValueData()),
	
	/**
	 * 赠送好友次数
	 */
	SEND_GFIT_TO_FRIENDS(700038, new ValueData()),
	
	/**
	 * 参与赛博次数
	 */
	SARBO_JOIN(700039, new ValueData()),
	
	/**
	 * 获得赛博积分
	 */
	SARBO_SCORE_GET(700040, new ValueData()),
	
	/**
	 * 加入联盟
	 */
	GUILD_JOIN(700041, new ValueData()),
	
	/**
	 * 再续前缘累计登陆
	 */
	LOGIN_DAYS_CHEMISTRY(700042,new ValueData()),
	
	/**
	 * 泰伯利亚进入战场次数
	 */
	TIBERIUM_JOIN_NUMBER(700043, new ValueData()),
	
	/**
	 * 联合军演获取奖励次数
	 */
	LMJY_REWARD_NUMBER(700044, new ValueData()),
	
	/**
	 * 战区占领
	 */
	OCCUPY_SUPER_WEAPON(700045, new ValueData()),
	
	/**
	 * 能源滚滚活动积分  配置格式：积分数量
	 */
	ENERGIES_SELF_SCORE(700046, new ValueData()),
	
	/**
	 * 能源滚滚活动积分  配置格式：积分数量
	 */
	ENERGIES_GUILD_SCORE(700047, new ValueData()),
	
	/** 端午，联盟庆典积分*/
	DRAGON_BOAT_CELEBRATION_GUILD_SCORE(192001,new ValueData()),
	/** 开启端午礼包次数*/
	DRAGON_BOAT_LUCKY_BAG_OPEN_COUNT(196001,new ValueData()),
	/** 金条充值天数*/
	DIAMOND_RECHARGE_DAYS(197101,new ValueData()),
	
	/** 端午，完成某个任务多少次*/
	DRAGON_BOAT_RECHARGE_ACHIVE_FINISH_COUNT(197102, new ValueData()),

	//泰伯利亚单场积分
	TIBERIUM_SINGLE_SCORE(700055, new ValueData()),
	
	//装备研究功能升级次数
	ARMOUR_TECH_LEVEL_UP(700056, new ValueData()),
	
	//虚拟实验室登录成就
	VIRTUAL_LABOTATORY_LOGIN_ACHIEVE(700057,new ValueData()),
	
	//充值福利抽奖次数
	RECHARGE_WELFARE_LOTTERY_TIMES(700058, new ValueData()),

	//个人发送祝福语
	PERSON_SEND_GREETINGS(700059, new ValueData()),

	//全服发送祝福语
	GLOBAL_SEND_GREETINGS(700060, new ValueData()),
	
	//双享豪礼购买天数事件
	DOUBLE_GIFT_BUY_DAYS(700061, new ValueData()),
	//周年历程签到
	CELEBRATION_COURSE_SIGN(700064, new ValueData()),	
	//购团购买积分
	GROUP_BUY_SCORE(700062, new ValueData()),

	//荣耀返利都买次数
	HONOR_REPAY_BUY(700065, new ValueData()),

	//全服签到
	GLOBAL_SIGN_COUNT(700070,new ValueData()),

	/** 累计登录(祝福语活动)  配置格式：登录天数*/
	LOGIN_DAYS_GREETINGS(700066, new ValueData()),

	/** 拼图连线(双十一拼图活动)  配置格式：四点连线次数*/
	JIGSAW_CONNECT_TIMES(700067, new ValueData()),
	/** 累计登录(双十一拼图活动)  				配置格式：登录天数*/
	LOGIN_DAYS_JIGSAW_CONNECT(700068, new ValueData()),
	// 回流拼图分享
	RETURN_PUZZLE_SHARE(700069,new ValueData()),
	/** 累计登录(装扮投放系列活动二:能量聚集)  	配置格式：登录天数*/
	LOGIN_DAYS_ENERGY_GATHER(700071, new ValueData()),

	/**冠军试炼次数*/
	DO_CR_TIMES(700072, new ValueData()),
	/**购买每日必买礼包次数*/
	BUY_DAILY_GIFT(700073, new ValueData()),

	/**累计登录(冰雪计划)*/
	LOGIN_DAYS_ENERGY_GATHER_TWO(700074, new ValueData()),

	/** 累计登录(登录基金2)配置格式：登录天数*/
	LOGIN_DAYS_FUND_TWO(700075, new ValueData()),

	// 体力消耗:新加成就任务,防刷。行军到达目标点,成功攻击以后才计算体力消耗。
	VI_COST(700076, new ValueData()),
	/** 七夕相遇结局数量*/
	LOVER_MEET_ENDING_COUNT(700077,new ValueData()),

	/** 天降鸿福*/
	HEAVEN_BLESSING_FREE(700078,new ValueData()),//活动激活就完成
	HEAVEN_BLESSING_PAY(700079,new ValueData()),//通过购买直购礼包完成
	
	//勋章宝藏抽奖次数
	HIDDEN_TEASURE_BOX_TIMES(1700017, new ValueData()),
	/** 英雄祈福-登陆天数*/
	HERO_WISH_LOGIN_DAYS(700080, new ValueData()),
	/** 独家记忆分享天数*/
	EXCLUSIVE_MEMORY_SHARE_COUNT(700081, new ValueData()),
	
	/** 荣耀英雄降临幸运值*/
	HONOUR_HERO_BEFELL_LUCKY_COUNT(700082, new ValueData()),

	/** 荣耀英雄回归每日登陆*/
	HONOUR_HERO_RETURN_LOGIN(700083, new ValueData()),

	/** 荣耀英雄回归每日积分*/
	HONOUR_HERO_RETURN_DAILY_SCORE(700084, new ValueData()),

	/** 陨晶 丝血反杀（获胜时基地血量小于10点）*/
	DYZZ_WIN_WITH_BASE_LESS_TEN(700085, new ValueData()),
	/** 陨晶 绝地翻盘（获胜时基地血量仅为1点）*/
	DYZZ_WIN_WITH_BASE_EQUAL_ONE(700086, new ValueData()),
	/** 陨晶 获胜*/
	DYZZ_WIN(700087, new ValueData()),
	/** 陨晶 超神（评价达到18）*/
	DYZZ_HOLY_SHIT(700088, new ValueData()),
	/** 陨晶 尽力（失败方，队内评价最高）*/
	DYZZ_LOST_BEST(700089, new ValueData()),
	/** 陨晶 超神（获胜方，队内评价最高）*/
	DYZZ_WIN_BEST(700090, new ValueData()),
	/** 玫瑰赠礼个人数量 */
	ROSE_GIFT_SELF_NUM(700091, new ValueData()),
	/** 玫瑰赠礼本服数量 */
	ROSE_GIFT_SERVER_NUM(700092, new ValueData()),
	/** 充值X元任意充值含直购 */
	RECHARGE_ALL_RMB(700093, new ValueData()),
	/** 世界勋章登录任务 */
	STAR_LIGHT_SIGN_LOGIN_DAYS(700094, new ValueData()),
	/** 拼多多登录天数 */
	PDD_LOGIN(700095, new ValueData()),
	/** 拼多多消耗金条 */
	PDD_GOLD_COST(700096, new ValueData()),
	/** 拼多多完成次数 */
	PDD_ORDER_DONE(700097, new ValueData()),
	/** 拼多多分享次数 */
	PDD_ORDER_SHARE(700098, new ValueData()),
	/**
	 * 霸主膜拜累积数量
	 */
	OVERLORD_BLESSING_NUM(700100, new ValueData()),
	/**
	 * 收集周年装扮
	 */
	DRESS_COLLECT(700102, new ValueData()),

	/** 机甲舱体代币  */
	SPACE_MACHINE_POINT(316100, new ValueData()),

	LUCK_GET_GOLD_DRAW(700101, new ValueData()),
	/** 兵转跳1  				配置格式：次数*/
	SOLDIER_EXCHANGER_P1(60101, new ValueData()),
	/** 兵转跳2  				配置格式：次数*/
	SOLDIER_EXCHANGER_P2(60102, new ValueData()),
	/** 兵转到达界面1*/
	SODIDER_EXCHANGE_PAGE_1(32801, new ValueData()),
	/** 兵转到达界面2*/
	SODIDER_EXCHANGE_PAGE_2(32802, new ValueData()),
	Appoint_Get331001(331001, new ValueData()),
	Appoint_Get331002(331002, new ValueData()),
	Appoint_Get331003(331003, new ValueData()),
	Appoint_Get331004(331004, new ValueData()),
	
	/** 金币觅宝每日累计在线分钟 */
	GOLD_BABY_DAILY_ONLINE(700103,new ValueData()),
	/** 金币觅宝累计抽奖次数 */
	GOLD_BABY_CUMULATIVE_FIND(700104,new ValueData()),
	/** 金币觅宝每日活跃积分 */
	GOLD_BABY_DAILY_SCORE(700105,new ValueData()),
	/** 新兵作训次数 */
	NOVICE_HERO_TRAINING_TIMES(700106,new ValueData()),
	NOVICE_EQUIP_TRAINING_TIMES(700107,new ValueData()),

	/** 在线分钟数 */
	ONLINE_MINUTES(700108,new ValueData()),

	/** 加速道具使用 */
	USE_ITEM_SPEED_UP_QUEUE(700109, new ValueData()),

	/** 情报中心完成 */
	AGENCY_FINISH(700110, new ValueData()),

	/** 实力飞升登录 */
	DEVELOP_FAST_LOGIN(700111, new ValueData()),

	/** 新春试炼登录 */
	CNY_EXAM_LOGIN(700112, new ValueData()),

	/**   1. 生产X次*/
	MEDALF_SHENGCHAN(170201, new ValueData()),
	/**  2. 收取X次				配置格式：消耗数量*/
	MEDALF_SHOU(170202, new ValueData()),
	/**  偷取X次（开始偷就算）					配置格式：消耗数量*/
	MEDALF_TOU(170203, new ValueData()),

	COMMON_ACTIVITY_LOGIN(170204, new ListValueData()),
	
	/** 星能探索活动个人积分任务 */
	PLANET_EXPLORE_PERSONAL_SCORE(170205, new ValueData()),
	/** 完成【清除危险】情报任务X次（情报中心打怪类型的任务）*/
	AGENCY_MONSTER_ATK_WIN(170206, new ValueData()),
	/** 完成【星能探索】X次（指的是养成线的探索。，注意，养成线的一键探索，需要计算多次）*/
	PLANET_EXPLORE_LEVEL_UP(170207, new ValueData()),
	/** 幸运星抽奖进度  */
	LUCKY_STAR_LOTTERY_TIMES(170208, new ValueData()),
	
	/** 中部培养计划积分达到*/
	GROW_UP_BOOST_SCORE(170209,new ValueData()),

	/** 联盟x人消耗体力*/
	SUPPLY_CRATE_GUILD_VIT_COST(170210, new ValueData()),
	/** 联盟x人充值*/
	SUPPLY_CRATE_GUILD_RECHARGE(170211, new ValueData()),
	/** 联盟获得徽章*/
	SUPPLY_CRATE_GUILD_ITEM_GET(170212, new ValueData()),
	
	/** 联盟击杀怪物BOSS数量*/
	RADIATIO_NWAR_TWO_BOSS_KILL_COUNT(170213,new ListValueData()),
	
	/** 每日必买指定道具获取个数*/
	DAILY_BUY_GIFT_ITEM_COUNT(170214,new ValueData()),
	
	/** 直购礼包花费X元(不包括充值金条花费)*/
	RECHARGE_GIFT_PAY_COUNT(170215,new ValueData()),

	/** 联盟回流回归玩家累计消耗金条*/
	GUILD_BACK_GOLD_COST(170216,new ValueData()),
	/** 联盟回流回归玩家组队*/
	GUILD_BACK_TEAM_BATTLE(170217,new ValueData()),
	/** 联盟回流回归玩家累计消耗体力*/
	GUILD_BACK_VIT_COST(170218,new ValueData()),
	/** 指定道具消耗,需要在const.xml givenItemCostEvent配置，逗号分割*/
	GIVEN_ITEM_COST_21070054(170219,new ValueData()),
	PLANT_SOLDIER_FACTORY_DRAW(170220,new ValueData()),
	
	/** 泰能超武投放活动中获得一个泰能超武  */
	PLANT_WEAPON_GET(35501,new ValueData()),
	
	/** 泰能超武投放活动中解锁后，分享一次泰能超武  */
	PLANT_WEAPON_SHARE(35502,new ValueData()),
	
	/** 泰能超武投放活动中抽奖x次 */
	PLANT_WEAPON_DRAW(35503,new ValueData()),
	
	/** 打靶活动购买游戏次数*/
	SHOOT_PRACTICE_BUY_GAME_COUNT(35101,new ValueData()),
	
	/** 打靶活动积分*/
	SHOOT_PRACTICE_SCORE(35102,new ValueData()),
	
	/** 超武返场活动抽奖次数  */
	PLANT_WEAPON_BACK_DRAW(36001,new ValueData()),
	/** 新春头奖专柜活动抽奖次数 */
	BEST_PRIZE_DRAW_CONSUME(36101,new ValueData()),
	
	/** 秘境寻宝宝箱积分*/
	QUEST_TREASURE_BOX_SCORE(36102,new ValueData()),
	
	/** 潜艇大战参与次数*/
	SUBMARINE_WAR_PLAY_COUNT(36104,new ValueData()),
	/** 潜艇大战积分*/
	SUBMARINE_WAR_SCORE(36103,new ValueData()),
	/** 潜艇大战通关*/
	SUBMARINE_WAR_STAGE_PASS(36105,new ValueData()),
	/** 潜艇击杀*/
	SUBMARINE_WAR_KILL(36106,new ListValueData()),
	
	/** 荣耀动员抽奖次数*/
	HONOUR_MOBILIZE_LOTTERY_COUNT(36107,new ValueData()),
	
	/** 合服比拼-玩家去兵战力目标 */
	MERGE_COMPETE_NOARMY_POWER(36801, new ValueData()),
	/** 合服比拼-联盟去兵战力目标 */
	MERGE_COMPETE_GUILD_NOARMY_POWER(36802, new ValueData()),
	/** 合服比拼-玩家体力消耗目标 */
	MERGE_COMPETE_VIT_COST(36803, new ValueData()),
	
	/** 赛后庆典全服致敬次数目标  */
	AFTER_COMPETTITION_HOMAGE_VALUE(37101, new ValueData()),
	
	/** 赛季拼图活动完成拼图数量 */
	SEASON_PUZZLE_COMPLETE_VALUE(37301, new ValueData()),
	
    /** 巨龙来袭总伤害*/
	GUILD_DRAGON_ATTACK_SCORE(37401, new ValueData()),

	/** 秘藏抽奖次数*/
	DEEP_TREASURE_BOX_TIMES(37701, new ValueData()),

	
	/** 热血畅战杀敌积分*/
	HOT_BLOOD_WAR_ENEMY_KILL_SCORE(37801, new ValueData()),
	
	/** 热血畅战损兵积分*/
	HOT_BLOOD_WAR_SOLDIER_LOSS_SCORE(37802, new ValueData()),
	
	/** 热血畅战损兵杀敌总积分*/
	HOT_BLOOD_WAR_TOTAL_SCORE(37803, new ValueData()),
	/** 命运轮抽道具消耗*/
	HOME_ROUND_ITEM_COST(37804, new KeyValueData()),

	NONE(0, new ListValueData()),
	;

	AchieveType(int value, AchieveData achieveData) {
		this.value = value;
		this.achieveData = achieveData;
	}

	private int value;

	private AchieveData achieveData;

	public int getValue() {
		return value;
	}

	public AchieveData getAchieveData() {
		return achieveData;
	}

	public static AchieveType getType(int type) {
		for (AchieveType achieveType : values()) {
			if (achieveType.getValue() == type) {
				return achieveType;
			}
		}
		return null;
	}
	
	/**
	 * 重复性检测
	 * 
	 * @return
	 */
	public static boolean checkRepeated() {
		Map<Integer, String> achieveIds = new HashMap<Integer, String>();
		for (AchieveType type : values()) {
			String value = achieveIds.putIfAbsent(type.getValue(), "");
			if (value != null) {
				HawkLog.errPrintln("activity achieveType Id repeated, id: {}", type.getValue());
				return false;
			}
		}
		
		return true;
	}
	
}
