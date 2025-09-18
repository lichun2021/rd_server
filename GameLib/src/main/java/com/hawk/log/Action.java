package com.hawk.log;

import java.util.HashMap;
import java.util.Map;

import org.hawk.log.HawkLog;

import com.hawk.log.LogConst.PlayerExpResource;

/**
 * 日志行为定义（注意：已经定好的value值不要改动）
 * 
 * @author hawk,lating
 *
 */
public enum Action {
	NULL(0),    //无明显Action操作的行为
	PLAYER_BUILDING_UPGRADE(1, PlayerExpResource.BUILDING_UPGRADE),   //建筑升级：消耗钻石，资源，获得经验值
	PLAYER_BUILDING_CREATE(3, PlayerExpResource.BUILDING_CREATE),     //创建建筑
	PLAYER_BUILDING_REMOVE(4, PlayerExpResource.BUILDING_REMVOE),     //拆除建筑
	MISSION_BONUS(5, PlayerExpResource.MISSION_AWARD),                //普通任务奖励：获得钻石或道具
	STORY_MISSION_BONUS(6, PlayerExpResource.CHAPTER_MISSION_AWARD),  //剧情任务奖励：获得钻石或道具
	DAILY_MISSION_BONUS(7, PlayerExpResource.DAILY_MISSION_AWARD),    //日常任务奖励：获得钻石或道具
	TOOL_USE(8, PlayerExpResource.TOOL_USE),         //使用道具：消耗道具，获得钻石或道具
	FIGHT_MONSTER(9, PlayerExpResource.WORLD_MONSTER_AWARD),     // 普通击杀野怪奖励：获得道具
	STOREHOUSE_REWARD(10, PlayerExpResource.STORE_HOUSE_AWARD),  // 领取联盟宝藏奖励
	GM_AWARD(11, PlayerExpResource.GM_REVISE),                   // GM发放奖励：获得钻石或道具
	GM_EXPLOIT(12, PlayerExpResource.GM_REVISE),                 // GM扣除：扣除钻石或道具
	IDIP_CHANGE_PLAYER_ATTR(13, PlayerExpResource.GM_REVISE),    // IDIP修改玩家数据
	RECHARGE_BUY_DIAMONDS(14),    //充值购买：除了获得钻石，还有可能获得充值礼包
	TOOL_BUY(16),           //普通商城购买道具：消耗钻石，获得道具
	BUY_VIT(18),            //购买体力
    BUY_GIFT(19),           //购买礼包
	GUILD_BUY(20),          //联盟商店购买：获得道具
	GUILD_CREATE(21),       //创建联盟：消耗钻石，获得钻石或道具
	GUILD_CHANGENAME(22),   //联盟名称修改：只消耗钻石
	GUILD_CHANGETAG(23),    //联盟简称修改：只消耗钻石
	GUILD_CHANGEFLAG(24),   //联盟旗帜修改：值消耗钻石
	GUILD_SENDOPENRECRUIT(25),    //联盟公开招募：消耗钻石
	WORLD_MARCH_BUY_ITEMS(26),    //集结购买额外行军格子：只消耗钻石
	WORLD_QUARTERED_MARCH_BUY_ITEMS(27), //驻军购买额外行军格子：只消耗钻石
	RANDOM_MOVE_CITY(28),    //随机迁城：消耗钻石或道具
	MANTUAL_MOVE_CITY(29),   //定向迁城：消耗钻石或道具
	WORLD_MARCH_CALLBACK(30), //行军召回：消耗钻石或道具
	WORLD_MARCH_SPEEDUP(31),  //行军加速：消耗钻石或道具
	NEWLY_LEAVE(32),          //新手定点迁城道具清除：消耗道具
	TRAIN_SOLDIER(33),        //士兵训练：消耗钻石、资源
	SOLDIER_TREATMENT(34),    //伤兵治疗：消耗钻石、资源
	MAKE_TRAP(35),            //制造陷阱
	TECHNOLOGY_LEVEL_UP(36),  //科技升级
	PLAYER_CREATE(37),        //角色初始化赠送：钻石或道具或其他玩家属性
	PLAYER_LEVEL_UP_AWARD(38),  //角色升级奖励：获得钻石或道具
	PLAYER_CHANGE_NAME(39),    //指挥官名字修改：消耗钻石或道具
	PLAYER_BUY_ICON(40),      //指挥官购买头像：消耗钻石
	
	TRAVEL_SHOP_REFRESH(46),  //旅行商店商品刷新:消耗钻石
	TRAVEL_SHOP_BUY(47),      //旅行商店购买商品：消耗钻石，获得道具
	ACTIVITY_REWARD_BUILD_LEVEL(48, PlayerExpResource.ACTIVITY_AWARD), //建筑等级活动奖励
	ACTIVITY_REWARD_LOGIN_DAY(49, PlayerExpResource.ACTIVITY_AWARD),   //累计登录活动奖励
	ACTIVITY_REWARD_LOGIN_SIGN(50, PlayerExpResource.ACTIVITY_AWARD),  //登录签到活动奖励
	
	EXCHANGE_DIAMONDS_TO_GOLD(53),  //钻石兑换水晶
	PLAYER_QUEUE_SPEEDUP(54),       //时间队列加速：消耗钻石或道具
	WISHING(55),             //许愿
	VIP_LEVEL_UP(56),        //VIP升级
	OPEN_PAID_QUEUE(57),     //购买第二建筑队列
	GACHA(58),               //扭蛋(英雄招募)
	USE_CDK(59),             //cdk发送奖励：获得钻石或道具
	QUESTION_SUBMIT(60),     // (behavior log)提交调查问卷
	GUILD_SCIENCE_DONATE(61),     //联盟科技捐献
	GUILD_RESET_DONATE_TIMES(62), //联盟捐献次数重置
	STOREHOUSE_EXCAVATE(63),     //联盟宝藏挖掘
	STORE_HOUSE_REFRASH(64),     //刷新联盟宝藏: 只消耗钻石
	STOREHOUSE_HELP_SEPPDUP(65), //联盟宝藏加速
	HERO_UNLOCK(66),     //解锁英雄
	USE_HERO_SKILL(67),  //使用英雄技能
	HERO_STAR_UP(68),    //英雄升星
	USE_HERO_EXP(69),    //使用英雄经验道具
	TALENT_CHANGE(70),   // (behavior log)天赋路线免费切换
	USE_TALENT_CLEAR_ITEM(71),    // 天赋洗点：使用天赋点重置道具
	SYS_MAIL_AWARD(72),  //领取邮件附带奖励：获得钻石或道具
	PLAYER_OUT_FIRE(73), //城墙灭火
	WHARF(74),           //码头领取物品
	YURI_EXPLORE_RES(75, PlayerExpResource.WORLD_EXPLORE),//世界探索帝陵
	GEN_MONSTER(76),     //生成野怪
	ACTIVITY_EXCHANGE_RES_COLLECT(77, PlayerExpResource.ACTIVITY_AWARD),    //残卷兑换 资源收集
	ACTIVITY_EXCHANGE_WORLD_COLLECT(78, PlayerExpResource.ACTIVITY_AWARD),  //残卷兑换 世界采集
	ACTIVITY_EXCHANGE_BEAT_YURI(79, PlayerExpResource.ACTIVITY_AWARD),      //残卷兑换 击败尤里
	ACTIVITY_EXCHANGE_WISH(80, PlayerExpResource.ACTIVITY_AWARD),           //残卷兑换 许愿
	ACTIVITY_EXCHANGE_PACKAGE_BUY(81, PlayerExpResource.ACTIVITY_AWARD),    //残卷兑换 礼包购买
	ACTIVITY_BROKEN_EXCHANGE(82, PlayerExpResource.ACTIVITY_AWARD),         //残卷兑换
	BUIDING_AREA_UNLOCK(83),  //解锁区块
	GUILD_MOVE_CITY(84),      //联盟迁城：消耗道具
	WORLD_SPY(85),            //世界侦查: 消耗水晶
	PLAYER_BUILDING_UPGRADE_CANCEL(86), //取消建筑升级: 升级光棱塔等需要消耗物品的建筑，取消时返还物品
	CLEAR_MANOR_RESOURCE_POINT(87),     //清理领地内资源点
	ACTIVITY_ACHIEVE_REWARD(88, PlayerExpResource.ACTIVITY_AWARD),   //成就活动奖励
	ACTIVITY_GROW_FUND_CONSUME(89, PlayerExpResource.ACTIVITY_AWARD), //成长基金购买消耗
	
	PLOT_BATTLE_REWARD(90, PlayerExpResource.PLOT_BATTLE_AWARD),     // (behavior log)剧情战役通关奖励
	EQUIP_FORGE(91),            //装备打造
	EQUIP_RESOLVE(92),          //装备分解
	EQUIP_UPGRADE_QUALITY(93),  //装备品质提升
	EQUIP_MATERIAL_MERGE(94),   //装备材料合成
	EQUIP_MATERIAL_RESOLVE(95), //装备材料分解
	EQUIP_QUEUE_CANCEL(97),     //装备队列取消返回
	EQUIP_PUT_ON(98),          // (behavior log)装备穿戴
	EQUIP_TAKE_OFF(99),        // (behavior log)装备卸下
	HERO_INSTALL_SKILL(100),     // 安装英雄技能(英雄镶嵌芯片)
	HERO_SKILL_UNINSTALL(101),   // 卸载技能（英雄芯片卸载）
	USE_HERO_SKILL_EXP(102),     // 使用英雄技能经验道具（芯片升级）
	MERGE_HERO_SKILL(103),       // 合成英雄技能（芯片合成）
	HERO_SKILL_RESOLVE(104),     // 英雄技能分解（芯片分解）
	HERO_EXCHANGE_ITEM(105),     // 英雄碎片转换
	VIP_SHOP_BUY(107),           //vip商城购买 
	VIP_EXCLUSIVE_BOX_BUY(108),  //购买vip专属礼包
	TAKE_VIP_BENEFIT_BOX(109),   //领取vip福利礼包
	BATTLE_MISSION_BONUS(110, PlayerExpResource.BATTLE_MISSION_AWARD), //战地任务奖励
	
	LOGIN_GAME(111),           // (behavior log)登录游戏
	LOGOUT_GAME(112),          // (behavior log)登出游戏
	NEWBIE_GUIDE(113),         // 新手引导
	ADD_SHIELD_PLAYER(114),    // (behavior log)屏蔽玩家
	REMOVE_SHIELD_PLAYER(115), // (behavior log)解除屏蔽玩家
	VIT_AUTO_ADD(116),         // 体力自动恢复
	ITEM_BUF_EXPIRED(117),     // (behavior log)道具Buf到期
	TALENT_UPGRADE(118),       // (behavior log)天赋升级
	CREATE_MAIL(119),          // (behavior log)创建邮件
	GET_MAIL_REWARD(120),      // 领取邮件奖励
	USE_SKILL_10301(121),      //使用技能获得体力
	USE_SKILL_10304(122),      //使用技能10304
	BUILDING_COLLECT_RES(123),  //城内收取资源
	OIL_CONSUME_BY_ARMY(124),   //军队消耗石油
	BUBBLE_REWARD(125),         //领取城内冒泡奖励 
	HERO_ATTR_RESET(126),       //重置英雄属性 
	PLAYER_QUEUE_CANCEL(127),        //时间队列取消
	PLAYER_QUEUE_FINISH(128),        // (behavior log)时间队列结束
	PLAYER_BUILDING_CREATE_CANCEL(129),  //建筑建造队列取消
	TECHNOLOGY_CANCEL(130),          //取消科技升级
	TECHNOLOGY_LEVELUP_FINISH(131),  // (behavior log)科技研究结束
	MAKE_TRAP_CANCEL(132),           //制造陷阱队列取消
	TRAIN_SOLDIER_CANCEL(133),       //士兵训练取消 
	SOLDIER_TREATMENT_CANCEL(134),   //取消伤兵治疗
	COLLECT_TRAP(135),               // (behavior log)收取陷阱
	COLLECT_SOLDIER_TRAINED(136),    // (behavior log)收取训练完的士兵
	COLLECT_SOLDIER_TREATMENT(137),  // (behavior log)收取治疗完的士兵
	SOLDIER_ADVANCE(138),            // (behavior log)士兵进阶
	SOLDIER_REWARD(139),             //士兵奖励
	FIRE_SOLDIER(140),               // (behavior log)解雇士兵
	
	WORLD_START_QUARTERED(141),      // (behavior log)发起一个驻扎行军
	WORLD_START_FIGHT_MONSTER(142),  // (behavior log)发起一个打怪行军
	WORLD_START_ASSISTANT(143),      // (behavior log)发起一个部队援助行军
	WORLD_START_ASSISTANT_RES(144),  // (behavior log)发起一个援助资源行军
	WORLD_START_MASS(145),           // (behavior log)发起一个集结行军
	WORLD_START_MASS_JOIN(146),      // (behavior log)发起一个参与集结的行军
	WORLD_START_ATTACK_PLAYER(147),     // (behavior log)发起一个攻打玩家的行军
	WORLD_START_COLLECT_RESOURCE(148),  // (behavior log)发起一个采集行军
	WORLD_START_SPY(149),                    // (behavior log)发起一个侦查行军
	WORLD_MANOR_MARCH_START(150),            // (behavior log)发起一个联盟领地行军
	WORLD_START_YURI_EXPLORE(151),           // (behavior log)发起一个尤里探索行军
	WORLD_PRESIDENT_SINGLE_MARCH(152),       // (behavior log)发起一个总统府单人行军
	WORLD_PRESIDENT_TOWER_SINGLE_MARCH(153), // (behavior log)发起一个总统府箭塔单人行军
	WARE_HOUSE_STORE_MARCH(154),             // (behavior log)发起一个仓库存款行军
	WARE_HOUSE_TAKE_MARCH(155),              // (behavior log)发起一个取款行军
	WORLD_STRONGPOINT_MARCH(156),            // 据点行军
	
	WORLD_MARCH_REACH(157),                  // (behavior log)行军到达
	WORLD_MARCH_REACH_FIGHT_MONSTER(158),    // (behavior log)世界打怪行军到达目的地
	WORLD_MARCH_REACH_COLLECT(159),          // (behavior log)采集行军到达目的地
	WORLD_MARCH_REACH_ASSISTANT(160),        // (behavior log)部队援助行军到达目的地
	WORLD_MARCH_REACH_ASSISTANT_RES(161),    // (behavior log)资源援助行军到达目的地
	WORLD_MARCH_REACH_QUARTERED(162),        // (behavior log)驻扎行军到达目的地
	WORLD_MARCH_REACH_MASS_JOIN_REACH(163),  // (behavior log)参与集结行军到达目的地
	WORLD_MARCH_REACH_ATTACK_PLAYER(164),    // (behavior log)攻击玩家基地行军到达目的地
	
	ATTACKED_BY_PLAYER(165),                 //玩家城点被攻击
	WORLD_MANOR_MARCH_REACH(166),         // (behavior log)联盟领地行军到达目的地
	WORLD_YURI_EXPLORE_MARCH_REACH(167),  // (behavior log)尤里探索行军到达目的地
	WORLD_MANOR_COLLECT_MARCH_REACH(168), // (behavior log)联盟领地超级矿采集行军到达目的地
	WARE_HOUSE_STORE_MARCH_REACH(169),    // (behavior log)仓库存款行军到达目的地
	WARE_HOUSE_TAKE_MARCH_REACH(170),     // (behavior log)取款行军到达目的地
	
	WORLD_MARCH_RETURN(171),               // (behavior log)行军回程
	WORLD_MARCH_ATTACK_PLAYER_RETURN(172), //攻打玩家行军返回
	WORLD_MARCH_ASSISTANT_RES_RETURN(173), //资源援助行军返回
	WORLD_MARCH_MASS_RETURN(174),         // 集结行军返回
	WORLD_MARCH_COLLECT_RETURN(175),      // 世界行军采集返回
	WORLD_YURI_EXPLORE_MARCH_RETURN(176), // (behavior log)尤里探索返回
	WARE_HOUSE_STORE_MARCH_BACK(177),     // (behavior log)仓库存款行军返回
	WARE_HOUSE_TAKE_MARCH_BACK(178),      // 取款行军返回
	WORLD_COLLECT_RES(179),               // 世界资源采集
	ASSISTANT_RES(180),            // 资源援助
	
	UPDATE_MARCH(181),             // (behavior log)行军数据更新
	REMOVE_MARCH(182),             // (behavior log)行军数据删除
	WORLD_MARCH_DISSLOVE(183),     // (behavior log)队长解散一个行军
	WORLD_MASS_REPATRIATE(184),    // (behavior log)遣返一个行军
	PRESIDENT_REVENUE(185),        //总统征税
	PRESIDENT_REVENUED(186),       //被总统征税
	PRESIDENT_SEND_GIFT(187),      // (behavior log)国王颁发礼包
	
	WORLD_ADD_FAVORITE(188),             // (behavior log)世界收藏一个坐标
	WORLD_UPDATE_FAVORITE(189),          // (behavior log)世界更新一个收藏坐标
	WORLD_REMOVE_FAVORITE(190),          // (behavior log)世界移除一个坐标
	OPEN_YURI_REVENGE_FIGHT(191),        // (behavior log)开启尤里复仇战斗
	WORLD_MARCH_OCCUPY_WAR_POINT(192),   // (behavior log)占领堡垒
	REMOVE_MARCH_WHEN_MANTUAL_CITY(193), // (behavior log)迁城处理玩家的行军

	CREATE_MANOR_BASTION(194),  //(behavior log)放置联盟堡垒
	CREATE_MANOR_BUILDING(195), //(behavior log)放置联盟建筑
	CHANGE_MANOR_NAME(196),     // (behavior log)修改联盟领地名称
	REMOVE_MANOR_BUILDING(197), // (behavior log)移除联盟领地建筑
	WAREHOUSE_SEND_BACK(198),   //联盟仓库退款
	
	GUILD_CHANGEAPPLY(199),     // (behavior log)改变联盟申请
	GUILD_CANCELAPPLY(200),     // (behavior log)取消联盟申请
	GUILD_APPLY(201),           // (behavior log)联盟申请
	GUILD_ACCEPTAPPLY(202),     // (behavior log)同意联盟申请
	GUILD_JOIN(203),            // (behavior log)联盟加入
	GUILD_INVITE(204),          // (behavior log)联盟邀请
	GUILD_REFUSEAPPLY(205),       // (behavior log)拒绝联盟申请
	GUILD_ACCEPTINVITE(206),      // (behavior log)同意联盟邀请
	GUILD_REFUSEINVITE(207),      //(behavior log)拒绝联盟邀请
	GUILD_CANCELPOSTMESSAGE(208), // (behavior log)解除屏蔽留言 
	GUILD_FORBIDPOSTMESSAGE(209), // (behavior log)屏蔽留言
	GUILD_CHANGELEVELNAME(210),   // (behavior log)改变联盟阶级名字
	
	GUILD_KICK(211),              // (behavior log)踢出联盟成员
	GUILD_QUIT(212),              // (behavior log)退出联盟
	GUILD_DEMISELEADER(213),       // (behavior log)让出联盟盟主
	GUILD_DISMISSGUILD(214),       // (behavior log)解散联盟
	GUILD_IMPEACHLEADER(215),      // (behavior log)弹劾盟主解散联盟
	GUILD_SHOP_ITEM_ADD(216),      // (behavior log)联盟商店补货
	GUILD_SCIENCE_RESEARCH(217),   // (behavior log)研究联盟科技
	GUILD_SCIENCE_RECOMMEND(218),  // (behavior log)联盟科技推荐
	HERO_OFFICE_APPOINT(219),		// (behavior log)英雄委任
	
	GUILD_HOSPICE_GET(220),			// 领取联盟关怀
	ACTIVITY_LOGIN_FUND_CONSUME(221), //登录基金购买消耗
	ACTIVITY_MONTHCARD_REWARD(222), // 领取月卡活动每日奖励
	RECHARGE_BUY_MONTHCARD(223),    // 购买月卡
	RECHARGE_BUY_GIFT(224),         // 购买每日特惠礼包
	ACTIVITY_FIRST_RECHARGE(225),	// 首充奖励
	PUSH_GIFT_BUY(226),				// 推送礼包
	TRAVEL_GIFT_BUY(227),			// 黑市礼包
	SEND_GIFT_FOR_ANCHOR(228),		// 主播刷礼物
	INC_GUILDCOUNTER_BOUNTY(229),   // 提高悬赏金额
	WORLD_FOGGY_ATK_AWARD(231),     // 攻击世界迷雾森林奖励
	TAKE_MONTHCARD_BOX_AWARD(232),  // 领取月卡免费宝箱奖励
	
	NEW_MONSTER_VIT_RETURN(235), // 新版野怪体力返还
	ATK_NEW_MONSTER(236),        // (behavior log)攻打新版野怪
	ATK_FOGGY(237),              // (behavior log)攻打迷雾要塞
	DO_BATTLE(238),              // (behavior log)战斗
	RANK_UPDATE(239),            // (behavior log)排行更新
	
	STOREHOUSE_HELP_REWARD(240, PlayerExpResource.STORE_HOUSE_AWARD),  //领取联盟宝藏帮助奖励
	USE_CHAT_BROADCAST_ITEM(241),  // 使用广播道具发送广播消息
	BUY_CHAT_BROADCAST_ITEM(242),  // 购买广播道具发送广播消息
	USE_TALENT_CHANGE_ITEM(243),   // 使用天赋路线切换道具
	BUY_TALENT_CHANGE_ITEM(244),   // 购买天赋路线切换道具 
	BUY_TALENT_CLEAR_ITEM(245),    // 购买天赋点重置道具

	USE_GAIN_STATUS_ITEM(246),        // 使用基地增益道具获得增益效果
	USE_WISH_ADD_ITEM(247),           // 使用道具增加许愿次数
	USE_DRESS_ITEM(248),              // 使用装扮道具进行装扮
	USE_REWARD_BOX_ITEM(249),         // 开启道具箱子获得奖励
	USE_VIT_ITEM(250),                // 使用道具增加体力
	USE_EXP_ITEM(251),                // 使用道具增加指挥官经验
	USE_RESOURCE_ITEM(252),           // 使用道具获得资源
	USE_GOLD_ITEM(253),               // 使用道具获得水晶           
	
	PLAYER_BUILDING_QUEUE_SPEEDUP(254), // 建筑升级加速
	PLAYER_TECH_QUEUE_SPEEDUP(255),     // 科技研究加速
	PLAYER_TRAIN_QUEUE_SPEEDUP(256),    // 部队训练加速
	PLAYER_TRAP_QUEUE_SPEEDUP(257),     // 制造陷阱加速
	PLAYER_CURE_QUEUE_SPEEDUP(258),     // 治疗伤兵加速
	PLAYER_EQUIP_QUEUE_SPEEDUP(259),    // 锻造装备加速
	
	JOIN_GUILD_REWARD(260),              // 首次加入联盟奖励
	RANDOM_BOX_AWARD(261),               // 领取世界宝箱奖励
	STRONGEST_TARGET_AWARD(262),         // 最强指挥官活动积分奖励
	STRONGEST_STAGE_RANK_AWARD(263),     // 最强指挥官活动阶段排名奖励
	STRONGEST_TOTAL_RANK_AWARD(264),     // 最强指挥官活动总排名奖励
	YURI_REVENGE_SCORE_REWARD(265),      // 尤里复仇活动个人积分奖励
	YURI_REVENGE_SELF_RANK_REWARD(266),  // 尤里复仇活动个人排名奖励
	YURI_REVENGE_GUILD_RANK_REWARD(267), // 尤里复仇活动联盟排名奖励
	
	BATTLE_EXCEPTION_BACK_TOOL(268),  // 战斗异常返还道具
	
	USE_GOLD_BUY_STATUS(269),         // 使用水晶购买基地增益
	USE_GOLD_SPEED_RES_RATE(270),     // 使用水晶加速资源田产出速率
	PLAYER_SMAIL_GIFT_GET_REWARD(271), // 取得联盟礼物奖励
	
	HELL_FIRE_TARGET_AWARD(272),  // 地狱火活动目标奖励
	HELL_FIRE_RANK_AWARD(273),    // 地狱火活动排名奖励
	ACTIVITY_FESTIVAL_AWARD(274),       // 八日庆典活动奖励
	ACTIVITY_GROW_FUND_AWARD(275),      // 成长基金活动奖励
	ACTIVITY_YURI_ACHIEVE_AWARD(276),      // 使命战争活动奖励
	ACTIVITY_STRONG_POINT_AWARD(277),   // 据点活动奖励
	ACTIVITY_POWER_UP_AWARD(278),       // 王者归来活动奖励
	ACTIVITY_LOGIN_FUND_AWARD(279, PlayerExpResource.ACTIVITY_AWARD), // 登录基金活动奖励
	ACTIVITY_EQUIP_ACHIEVE_AWARD(280),  // 精工锻造活动奖励
	ACTIVITY_HERO_ACHIEVE_AWARD(281),   // 英雄军团活动奖励
	ACTIVITY_GROUP_PURCHASE_AWARD(282), // 基金团购活动奖励
	ACTIVITY_ACCUMULATE_RECHARGE_AWARD(283),// 累积充值活动奖励
	ACTIVITY_ACCUMULATE_CONSUME_AWARD(284), // 累积消耗活动奖励
	
	GET_MILITARY_RANK_REWARD(285),          // 军衔每日津贴
	MASS_MONSTER(286),                      // (behavior log)集结打野怪 
	
	FIRST_KILL_MOSNTER_AWARD(287),    // 首杀野怪奖励
	DAMAGE_MONSTER_AWARD(288),        // 野怪伤害奖励
	KILL_MONSTER_GROUP_AWARD(289),    // 野怪击杀组队成员奖励
	KILL_MONSTER_GUILD_AWARD(290),    // 野怪击杀全联盟奖励
	
	BUILDING_ITEM_BACK_GOLD(291),    // 使用第二建造队列开启道具返还黄金
	XINYUE_HELP_RECHARGE(292),       // 心悦大R代充获得钻石
	CHANGE_NAME_ITEM_COMPENSATION(293),    // 改名道具补偿
	TALENT_SWITCH_ITEM_COMPENSATION(294),  // 使用战略切换道具返还黄金
	RECHARGE_GIFT(295),                    // 充值豪礼活动奖励
	WAREHOUSE_STORE_RES(296),              // 联盟仓库存放资源
	
	ACCUMULATE_ONLINE(297),              // 累计在线奖励
	ACTIVITY_REWARD_LOTTERY_DRAW(298, PlayerExpResource.ACTIVITY_AWARD),  // 盟军宝藏抽奖
	HERO_ITEM_RESLOVE(299),	             // 英雄物品转换.(整卡转为碎片/英雄)
	RESOURCE_GIFT_BUY(300),              // 资源礼包购买
	ACTIVITY_WARZONE_WEAL_ACHIEVE_AWARD(301, PlayerExpResource.ACTIVITY_AWARD),   // 战地福利活动奖励
	ACTIVITY_REPLICATION_CENTER_ONE(302, PlayerExpResource.ACTIVITY_AWARD),  // 复制中心一次
	ACTIVITY_REPLICATION_CENTER_TEN(303, PlayerExpResource.ACTIVITY_AWARD),  // 复制中心十次
	ACTIVITY_BLOOD_CORPS_AWARD(304, PlayerExpResource.ACTIVITY_AWARD),  // 铁血军团活动奖励
	
	CONTINUOUS_RECHARGE(305),	// 连续充值
	GUILD_HOSPICE_SEND(306),			// 发放联盟关怀
	ACTIVITY_GRATITUDE_GIFT_GET(307, PlayerExpResource.ACTIVITY_AWARD),
	TREASURY_RECEIVED(308),   //藏宝阁奖励领取
	GET_YURISTRIKE_REWARD(309),// 领取尤里来袭净化奖励
	SOLDIER_FIRST_AID(310),    // 超时空急救站的死兵急救
	SUPER_GOLD_REWARD(311), //超级金矿抽奖
	PANDORA_BOX_REWARD(312), //潘朵拉宝盒抽奖
	
	GUILD_POINT_GUIDE_REWARD(313), //联盟坐标指引奖励
	YURISTRIKE_CLEAN(314), // 净化尤里部队
	RES_BUILDING_OUTPUT_INC(315),  // 资源田增产
	HELL_FIRE_ONE(316),  //火线征召
	HELL_FIRE_TWO(317),  //全军动员
	HELL_FIRE_THREE(318),  //全日警戒
	STORY_MISSION_VIT_COST(319), //剧情任务体力消耗
	STORY_MISSION_KILL_MONSTER(320), //剧情任务杀怪
	INVITE_QQ_FRIEND_AWARD(321),  // QQ密友邀请任务奖励
	HERO_SKIN_USE(322),// 英雄皮肤
	ACTIVITY_REWARD_LOGIN_DAY_TWO(323, PlayerExpResource.ACTIVITY_AWARD),   //累计登录2活动奖励
	ACTIVITY_YURI_ACHIEVE_TWO_AWARD(324, PlayerExpResource.ACTIVITY_AWARD), //使命战争2活动奖励
	
	PLAYER_ACHIEVE_REWARD(325), //成就奖励
	REWARD_ORDER_REWARD(326),   //盟军悬赏令奖励
	
	BUILD_SELECTABLE_REWARD(327),  // 大本升级礼包奖励
	LUCKY_STAR_LOT(328),           // 幸运星抽奖
	GREAT_GIFT(329),               // 超值好礼
	ACTIVITY_FESTIVAL_TWO_AWARD(330),    // 八日庆典2活动奖励	
	TIME_LIMIT_DROP_ACHIEVE_REWARD(331), // 限时掉落奖励
	TIME_LIMIT_DROP_TASK_REWARD(332),    // 限时掉落完成任务奖励
	ALLY_BEAT_BACK_TASK_REWARD(333),     // 盟军反击任务
	ALLY_BEAT_BACK_ACHIEVE_REWARD(334),  // 盟军反击成就
	ALLY_BEAT_BACK_EXCHANGE_REWARD(335), // 盟军反击兑换
	ALLY_BEAT_BACK_EXTRA_REWARD(336),	 // 盟军反击额外奖励
	ACTIVITY_PRESENT_REBATE_AWARD(337, PlayerExpResource.ACTIVITY_AWARD),   // 礼包返利活动奖励
	SUPER_SOLDIER_STAR_UP(338),          // 超级兵升星
	USE_SUPER_SOLDIER_EXP(339),          // 使用超级兵经验道具
	USE_SUPER_SOLDIER_SKILL_EXP(340),    // 使用超级兵技能经验道具（芯片升级）
	USE_SUPER_OFFICE_APPOINT(341),		 // 超级兵委任
	SUPER_SOLDIER_OFFICE_APPOINT(342),	 // 超级兵委任
	SUPER_SOLDIER_SKIN_USE(343),         // 超级兵皮肤
	
	SUPER_SOLDIER_UNLOCK(344),     //解锁超级兵
	DOME_EXCHANGE(345),  //穹顶兑换
	GUILD_SIGN(346),		//联盟签到
	GUILD_TASK_AWARD(347),	//联盟任务领奖
	ALLY_BEAT_BACK_EXCHANGE_COST(348), //盟军扣除物品
	ACTIVITY_HERO_THEME_AWARD(349),   // 英雄主题(最强步兵)活动奖励
	CR_MISSION_REWARD(350), // CR英雄试练领奖
	ITEM_EXCHANGE(351),     // 物品兑换
	LUCKY_STAR_FERR_BAG(352), //幸运星每天免费礼包
	ACTIVITY_BROKEN_EXCHANGE_THREE(353, PlayerExpResource.ACTIVITY_AWARD),         //残卷兑换3
	ACTIVITY_EXCHANGE_THREE_RES_COLLECT(354, PlayerExpResource.ACTIVITY_AWARD),    //残卷兑换 资源收集
	ACTIVITY_EXCHANGE_THREE_WORLD_COLLECT(355, PlayerExpResource.ACTIVITY_AWARD),  //残卷兑换 世界采集
	ACTIVITY_EXCHANGE_THREE_BEAT_YURI(356, PlayerExpResource.ACTIVITY_AWARD),      //残卷兑换 击败尤里
	ACTIVITY_EXCHANGE_THREE_WISH(357, PlayerExpResource.ACTIVITY_AWARD),           //残卷兑换 许愿
	SUPER_SOLDIER_UNLOCK_AWARD(358),     // 机甲解锁发放道具
	GUNDAM_MARCH(359),                   // 高达行军
	MACHINE_AWAKE_ACHIEVE_AWARD(360),    // 机甲觉醒活动成就奖励
	ONE_RMB_PURCHASE(361),               // 一元购活动
	SUPPLY_STATION_COST(362), //补给站消耗
	SUPPLY_STATION_REWARD(363), //补给站获得道具
	BUY_MILITARY_ITEM(364),        // 军演商店购买物品
	STRONGEST_GUILD_REWARD(365),  //王者联盟个人奖励
	HERO_BACK_COST(366), //英雄返场活动消耗
	HERO_BACK_REWARD(367), //英雄返场活动获得
	HERO_BACK_EXCHANGE(368),  //英雄返场 英雄回归
	WAR_COLLEGE_WIN(369),  //聯盟副本勝利獎勵
	ACTIVITY_GIFT_SEND_ACHIEVE_AWARD(370, PlayerExpResource.ACTIVITY_AWARD),   //豪礼派送【战地福利拷贝活动】
	SUPER_LAB_Compose(371), // 超能实验室 合成
	SUPER_LAB_DeCompose(372), // 超能实验室 分解
	SUPER_LAB_JiHuo(373), // 超能实验室 激活
	SUPER_LAB_NoJiHuo(374), // 超能实验室 取消激活
	CROSS_SERVER_COST(375), 					//跨服消耗
	CROSS_SERVER_ACHIEVE_REWARD(376),	// 跨服成就奖励
	POWER_COLLECT_ACHIEVE_REWARD(377),
	LMJY_JOIN_ROOM(378), // 联盟军演进入游戏
	LMJY_QUIT_ROOM(379), // 联盟军演离开游戏
	SHARE_BTREPORT(380),//每日分享战报
	RED_ENVELOPE_SYSTEM(381), //系统红包
	RED_ENVELIPE_PLAYER(382), //个人红包
	HAPPY_GIFT_REWARD(383), //欢购豪礼
	DOME_EXCHANGE_TWO(384), //复制穹顶杜欢，玫瑰之约.
	SUPPLY_STATION_TWO_COST(385), //复制盟军补给站, 又称时空恋人.
	SUPPLY_STATION_TWO_REWARD(386), //复制盟军补给站, 又称时空恋人.
	SEND_FLOWER_HUA_REWARD(387), // 送花奖励
	SEND_FLOWER_SONG(388), // 送花
	SEND_FLOWER_MAI(389),  // 买
	MACHINE_AWAKE_TWO_ACHIEVE_AWARD(390), // 机甲觉醒2(年兽)活动成就奖励
	NIAN_MARCH(391),                      // 年兽行军
	
	NEWYEAR_TREASURE_ACHIEVE_AWARD(392),   // 新年寻宝活动成就奖励
	RECALL_FRIEND_ACHIEVE_AWARD(393),      //召回好友成就奖励.
	TREASURE_HUNT_RES_VIT_COST(394), 		  //新年寻宝资源点行军体力消耗
	TREASURE_HUNT_RES_VIT_RETURN(395), 	  //新年寻宝资源点行军体力返还
	TREASURE_HUNT_MONSTER_VIT_RETURN(396),//新年寻宝野怪点行军体力返还
	COME_BACK_PLAYER_GREAT_AWARD(397), //老玩家回归 回归大礼奖励
	COME_BACK_PLAYER_EXCHANGE(398), //老玩家回归 兑换
	COME_BACK_PLAYER_BUY(399), //老玩家回归 低折回馈
	COME_BACK_ACHIEVE_REWARD(400), //老玩家回归成就奖励
	
	COMMON_EXCHANGE_EXCHANGE(401), //通用兑换 兑换
	COMMON_EXCHANGE_BUY(402), //通用兑换 购买
	USE_CHOOSE_AWARD_ITEM(403),  // 使用自选奖励宝箱道具
	TREASURE_CALVALRY_REFRESH(404), //夺宝将池 
	TREASURE_CALVALRY_OPEN(405), //夺宝将池 
	QUESTION_SHARE_AWARD(406),   // 在线答题  情报分享 
	INHERIT_ACHIEVE_AWARD(407),   //军魂承接累登奖励
	BANNER_KILL_TARGET_REWARD(408), // 战神降临活动目标积分奖励
	GEN_RES_TREASURE(409),	 // 生成资源宝库(开仓放粮)
	COLLECT_WAR_FLAG_RESOURCE(410),// 收取世界战旗资源
	MACHINE_SELL_LOTTERY_REWARD(411),// 机甲破世 抽奖所得
	MACHINE_SELL_BOX_REWARD(412),// 机甲破世宝箱所得
	MACHINE_SELL_LOTTERY_ONCE_COST(413),// 机甲破世 抽奖抽一次
	MACHINE_SELL_LOTTERY_FIVE_COST(414),// 机甲破世 抽奖抽5次
	CUSTOM_GIFT_REWARD(415),  // 定制礼包
	GIFT_ZERO_CONSUME(416),    // 0元礼包购买消耗
	GIFT_ZERO_REWARD(417),    // 0元礼包奖励
	
	COLLEGE_ONLINE_REWARD(418),  // 学院在线时长奖励
	CREATE_COLLEGE(419),   // 创建学院
	DISSMISS_COLLEGE(420),	// 解散学院
	JOIN_COLLEGE(421),	// 加入学院
	QUIT_COLLEGE(422), // 退出学院
	
	HERO_TALENT_UNLOCK(423), // 英雄天赋解锁
	HERO_TALENT_STRENG(424), // 英雄天赋强化
	HERO_TALENT_ROLL_TALE(425), // 英雄天赋roll
	HERO_TALENT_CHOSE_TALE(426), // 英雄天赋选定
	
	ORDER_EXP_BUY_CONSUME(427), //红警战令经验购买消耗
	BOUNTY_HUNTER_HIT(428), // 推金币攻击boss
	REVENGE_SHOP_BUY(429),  // 购买大R复仇折扣商品
	
	PLAN_ACTIVITY_LOTTERY_REWARD(430), //源计划抽奖所得
	PLAN_ACTIVITY_LOTTERY_COST(431), //源计划抽奖消耗
	LIMITTIME_SHOP_BUY(432), // 限时商店购买
	DAILY_SIGN_AWARD(433), //月签每日签到
	DAILY_SIGN_RESIGN_AWARD(434), //月签补签
	DAILY_SIGN_RESIGN_COST(435), //月签补签花费
	DAILY_SIGN_ACHIEVE_AWARD(436), //月签成就奖励
	TBLY_JOIN_ROOM(437), // 联盟锦标赛进入游戏
	TBLY_QUIT_ROOM(438), // 联盟锦标赛离开游戏
	TBLY_MOVE_CITY(439),   //定向迁城：消耗钻石或道具
	
	PLAYER_DRESS_SEND_COST(440), // 赠送装扮消耗
	
	SPREAD_ACTIVITY_ACHIEVE(441), // 推广员新兵成就领奖
	SPREAD_ACTIVITY_DAILY(442), // 推广员每日领奖
	SPREAD_ACTIVITY_SHOP_COST(443), // 推广员商店兑换消耗
	SPREAD_ACTIVITY_SHOP_GAIN(444), // 推广员商店兑换获取
	SPREAD_ACTIVITY_OLD_ACHIEVE(445), // 推广员老兵成就奖励
	
	INVEST_PROFIT(446), // 投资理财
	INVEST_CANCEL(447), // 投资取消返还
	
	COMMON_EXCHANGE_TWO_EXCHANGE(448), //通用兑换2 兑换
	COMMON_EXCHANGE_TWO_BUY(449), //通用兑换2 购买
	LUCKY_DISCOUNT_DRAW_COST(450),	//幸运折扣刷新奖池消耗 
	LUCKY_DISCOUNT_BUY_COST(451),	//幸运折扣购买消耗
	LUCKY_DISCOUNT_BUY_GAIN(452),	//幸运折扣刷新 购买获取
	
	HERO_TRIAL_REFRESH(453), // 英雄试炼刷新任务
	HERO_TRIAL_SPEED_UP(454), // 英雄试炼加速
	HERO_TRIAL_TAKE_AWARD(455), // 英雄试炼获取奖励

	URL_MODEL_EIGHT_DAILY(456), // 腾讯URL活动8 分享活动8
	
	ACTIVITY_LUCKY_WELFARE_ACHIEVE_AWARD(457, PlayerExpResource.ACTIVITY_AWARD),   // 幸运福利
	
	ARMOUR_INTENSIFY(458), // 铠甲强化
	ARMOUR_BREAKTHROUGH(459), // 铠甲突破
	ARMOUR_INHERIT(460), // 铠甲传承
	ARMOUR_RESOLVE(461), // 铠甲分解
	ARMOUR_SUIT_UNLOCK(462), // 铠甲套装解锁
	BLACK_TECH_DRAW_COST(463),		//黑科技扣刷新奖池消耗 
	BLACK_TECH_BUY_COST(464),		//黑科技购买消耗
	BLACK_TECH_BUY_GAIN(465),		//黑科技刷新 购买获取
	
	FULLY_ARMED_BUY_COST(466),		//全副武装购买消耗
	FULLY_ARMED_BUY_GAIN(467),		//全副武装购买获取
	FULLY_ARMED_SEARCH_GAIN(468),	//全副武装探索获取
	FULLY_ARMED_ACHIEVE_GAIN(469),	//全副武装成就获取
	
	QUESTION_SHARE_DAILY_AWARD(470),   // 在线答题  情报分享  每日奖励
	PIONEER_GIFT_FREE(471),    // 先锋豪礼免费礼包领取
	PIONEER_GIFT_BUY(472),     // 先锋豪礼购买发货
	PIONEER_ACC_GIFT_TAKE(473), // 先锋豪礼累计购买奖励
	
	ROULETTE_BOX_REWARD(474), //时空轮盘箱子奖励
	ROULETTE_LOTTERY_COST(475), //时空轮盘抽奖消耗
	ROULETTE_LOTTERY_REWARD(476), //时空轮盘抽奖奖励
	FIGHTER_PUZZLE_ACHIEVE_AWARD(477, PlayerExpResource.ACTIVITY_AWARD),   // 武者拼图活动奖励
	PRESIDENT_OPEN_BUFF(478),  //国王开启buff
	PRESIDENT_UPDATE_MANIFESTO(479), //修改宣言.
	SKIN_PLAN_ROLL_DICE(480), //皮肤计划扔骰子抽奖.
	SKIN_PLAN_RECHARGE(481), //皮肤计划活动期间充值给额外物品.
	HERO_SKIN_UNLOCK(482), // 解锁皮肤
	HERO_SKIN_STARUP(483), // 解锁皮肤
	ACTIVITY_ACCUMULATE_RECHARGE_TWO_AWARD(484),// 累积充值活动2奖励
	ACTIVITY_POWER_FUND_AWARD(485),      // 战力基金活动奖励
	DAILY_RECHARGE_AWARD(486),           // 今日累计充值奖励
	DAILY_RECHARGE_BUY_CONSUME(487),     // 今日累充活动购买宝箱消耗
	DAILY_RECHARGE_BUY_REWARD(488),     // 今日累充活动购买宝箱奖励
	GUARD_OUT_FIRE(489),   //守护灭火.
	GUARD_SEND_GIFT(490), //守护赠送礼物.
	GUARD_OPEN_COVER(491), //开启保护罩.
	GUARD_INVITE(492),		//守护申请.
	ACTIVITY_MILITARY_PREPARE_AWARD(493),    // 军事备战活动奖励
	MID_AUTUMN_REWARD(494),     // 中秋庆典任务
	MID_AUTUMN_EXCHANGE_COST(495),       // 中秋庆典兑换消耗
	MID_AUTUMN_EXCHANGE_REWARD(496),     // 中秋庆典兑换奖励
	MID_AUTUMN_BUY_GIFT_COST(497),       // 中秋庆典购买礼包消耗
	MID_AUTUMN_BUY_GIFT_REWARD(498),     // 中秋庆典购买礼包奖励
	MID_AUTUMN_TASK_REWARD(499),         // 中秋庆典任务掉落奖励
	STAR_WARS_KING_UPDATE_MANIFESTO(500), //修改宣言.
	ALLIANCE_CARNIVAL_AWARD(501),     // 联盟总动员活动领奖
	ALLIANCE_CARNIVAL_BUY_TIMES(502), // 联盟总动员购买次数
	
	ACTIVITY_REDKOI_WISH_COST(503),    // 红警锦鲤许愿消耗
	ACTIVITY_REDKOI_WISH_REWARD(504),  // 红警锦鲤许愿获得
	ACTIVITY_REDKOI_WISH_BUY(505),     // 红警锦鲤许愿购买
	ACTIVITY_TRAVEL_SHOP_ASSIST_AWARD(506), //特惠庆典奖励
	SW_JOIN_ROOM(507), // 联盟锦标赛进入游戏
	SW_QUIT_ROOM(508), // 联盟锦标赛离开游戏
	SW_MOVE_CITY(509), // 定向迁城：消耗钻石或道具
	TIME_LIMIT_LOGIN_REWARD(510),     // 限时登录活动奖励
	MEDAL_ACTION_TASK_REWARD(511),	  // 勋章宝藏活动奖励
	MEDAL_ACTION_LOTTERY(512),        // 勋章宝藏抽奖 
	MEDAL_ACTION_LOTTERY_COST(513),   // 勋章宝藏抽奖消耗 
	
	DIVIDE_GOLD_TASK_REWARD(514),			 //瓜分金币活动奖励
	DIVIDE_GOLD_GIVE_FUZI(515),		  	 	 //瓜分金币给福字消耗
	DIVIDE_GOLD_OPEN_RED_ENVELOPE(516), 	 //瓜分金币开红包获得
	DIVIDE_GOLD_MAKE_RED_ENVELOPE_COST(517), //合成红包消耗
	DIVIDE_GOLD_OPEN_BOX_COST(518),			 //开宝箱消耗.
	DIVIDE_GOLD_OPEN_BOX_REWARD(519),		 //开宝箱奖励
	DIVIDE_GOLD_MAKE_RED_ENVELOPE_REWARD(520), //合成红包奖励
	FLIGHT_PLAN_ACHIEVE_REWARD(521),  // 飞行计划任务奖励
	FLIGHT_PLAN_CONSUME(522),         // 飞行计划消耗 
	FLIGHT_PLAN_REWARD(523),          // 飞行计划奖励
	EQUIP_TECH_TASK_REWARD(524),	  //装备科技活动奖励
	HERO_SKIN_REFRESH(525),           // 英雄皮肤将池 
	HERO_SKIN_OPEN(526),              // 英雄皮肤将池 
	FLIGHT_PLAN_EXCHANGE_CONSUME(527), // 飞行计划商店兑换消耗
	FLIGHT_PLAN_EXCHANGE_AWARD(528),   // 飞行计划商店兑换获得
	SCENE_SHARE_AWARD(529),            // 场景分享奖励
	HERO_SKIN_RECHARGE(530),           // 英雄皮肤投放活动期间充值给额外物品.
	EVOLUTION_LEVEL_AWARD(531),        // 英雄进化之路活动奖池等级奖励
	EVOLUTION_EXCHANGE_AWARD(532),     // 英雄进化之路活动奖池兑换奖励
	SIMULATE_WAR_ENCOURAGE(533),       // 攻防模拟战助威.
	USE_MARCH_EMOTICON(534),       // 使用行军表情
	MARCH_EMOTICON_BUY(535),       // 购买行军表情包奖励
	DAILY_RECHARGE_NEW_AWARD(536), // 今日累计充值(改版)奖励
	
	WORLD_PYLON_MARCH_VIT_COST(537), // 能量塔行军体力消耗
	WORLD_PYLON_MARCH_REWARD(538),   // 能量塔行军奖励
	CROSS_CHARGE_COST(539),          // 跨服充能消耗
	CROSS_MISSION_AWARD(540),        // 跨服任务奖励
	CROSS_HOSPITAL_COST(541),        // 跨服恢复死兵消耗
	CROSS_ITEM_CHECK_REMIVE(542),    // 跨服道具检测移除
	TBLY_GUESS_VOTE_COST(543),       // 泰伯利亚竞猜活动竞猜消耗.
	COMMAND_ACADEMY_BUY_COST(544),  //指挥官学院礼包购买消耗
	COMMAND_ACADEMY_BUY_REWARD(545), //指挥官学院礼包购买获得
	COMMAND_ACADEMY_ACHIVE_REWARD(546),  //指挥官学院任务奖励
	CHRISTMAS_MARCH(547), 					//圣诞行军
	CHRISTMAS_AWARD(548),					//圣诞任务领奖.
	EQUIP_BLACK_MARKET_REFINE_COST(549),    //装备黑市炼化消耗
	EQUIP_BLACK_MARKET_REFINE_ACHIEVE(550),  //装备黑市炼化获得
	WORLD_START_COUNTRY_QUEST(551),           // (behavior log)发起一个国家任务行军
	WORLD_SONWBALL_ATTACK(552),          // 雪球攻击
	WORLD_ESPIONAGE(553),                // 间谍行军
	SNOWBALL_BOX_AWARD(554),             // 雪球大战宝箱奖励
	WORLD_ESPIONAGE_TOOL_RETURN(555),    // 间谍道具返还
	SAMURAI_BALCKENED_ACHIEVE_AWARD(556, PlayerExpResource.ACTIVITY_AWARD),   // 黑武士.
	DESTINY_REVOLVER_ACHIEVE_AWARD(557),    // 命运左轮成就奖励
	DESTINY_REVOLVER_FIVE_CONSUNE(558), // 命运左轮五个格子消耗
	DESTINY_REVOLVER_FIVE_AWARD(559), // 命运左轮五个格子奖励
	DESTINY_REVOLVER_NINE_CONSUNE(560), // 命运左轮九个格子消耗
	DESTINY_REVOLVER_NINE_AWARD(561), // 命运左轮九个格子奖励
	GUARD_DRESS_EXCHANGE(562),        //守护特效兑换
	GUARD_DRESS_SEND(563),        //守护特效赠送.
	SNOWBALL_GOAL_AWARD(564), // 雪球进球奖励
	WORLD_PULL_TREASURE(565),   // 世界拉取超时空补给站资源
	CYBORG_JOIN_ROOM(566), // 联盟锦标赛进入游戏
	CYBORG_QUIT_ROOM(567), // 联盟锦标赛离开游戏
	CYBORG_MOVE_CITY(568),   //定向迁城：消耗钻石或道具
	CYBORG_CREATE_TEAM(569),	//赛博之战创建战队
	CYBORG_EDIT_NAME(570),   	//赛博之战战队改名
	CYBORG_BUY_ITEM(571),		//赛博之战商店购买物品
	
	HERO_ACHIEVE_AWARD(572),
	CHRONO_GIFT_FREE_AWARD_ACHIEVE(573), //时空豪礼免费奖励获取
	CHRONO_GIFT_BUY_AWARD_ACHIEVE(574), //时空豪礼直购礼包奖励获取
	CHRONO_GIFT_TASK_REWARD(575), //时空豪礼成就任务奖励
	RECHARGE_FUND_INVEST(576),	// 充值基金投资
	RECHARGE_FUND_REWARD(577), // 充值基金领奖
	CHRONO_GIFT_KEY_BUY(578), //时空豪礼钥匙购买
	CHRONO_GIFT_UNLOCK_COST(579), //时空豪礼解锁消耗	
	RESOURCE_DEFENSE_CHARGE(580),       //资源保卫战，收取资源
	RESOURCE_DEFENSE_LEVEL_REWARD(581), //资源保卫战，等级奖励
	RESOURCE_DEFENSE_STEAL(582),        //资源保卫战，偷取
	RESOURCE_DEFENSE_BUYEXP(583),       //资源保卫战，购买经验s
	CROSS_ATK_MONSTER_DROP(584),        //跨服打野掉落
	HERO_LOVE_ACHIEVE(585),             //英雄委任.
	HERO_LOVE_GIFT_ITEM(586),           //英雄委托赠送道具.
	CHEMISTRY_ACHIEVE_REWARD(587), //回流活动,在续前缘奖励成就
	DEVELOP_SPURT_ACHIEVE_REWARD(588), //回流活动,在续前缘奖励成就
	BACK_GIFT_LOTTERY_REWARD(589),//回流活动,抽奖获取
	DEVELOP_SPURT_SIGN_REWARD(590), //回流活动，新发展冲刺签到获得
	NEWLY_EXPERIENCE_ACHEIVE_REWARD(591), //回流活动，新发展冲刺签到获得
	POWER_SEND_ACHIEVE_REWARD(592),//回流活动，赠送体力奖品获取
	POWER_SEND_BACK_REWARD(593),//回流活动，赠送体力回礼获取
	JOY_BUY_REWARD(594), //欢乐购消耗物品换取奖励
	JOY_BUY_CONSUME(595), //欢乐购消耗物品
	JOY_BUY_REFRESHCOST_CONSUME(596), //欢乐购消耗物品
	JOY_BUY_ACHIEVE_REWARD(597), //欢乐购成就领取
	LABORATORY_REMAKE_BLOCK(598), // 改造模仿
	LABORATORY_EXCHANGE_ITEM(599), // 兑换能量
	ACTIVITY_RADIATION_WAR_AWARD(600, PlayerExpResource.ACTIVITY_AWARD), //新版辐射战争活动奖励
	ACTIVITY_RADIATION_WAR_TWO_AWARD(601, PlayerExpResource.ACTIVITY_AWARD), //新版辐射战争2活动奖励
	EXCHANGE_DECORATE_REWARD(602), //兑换装扮
	EXCHANGE_DECORATE_LEVEL_EXP_ADD(603), //获取任务经验
	EXCHANGE_DECORATE_LEVEL_REWARD(604), //获取等级奖励
	EXCHANGE_DECORATE_LEVEL_OPEN_COST(605), //限时特惠消耗
	EXCHANGE_DECORATE_LEVEL_OPEN_REWARD(606), //限时特惠奖励
	EXCHANGE_DECORATE_LEVEL_ONLOCK_COST(607), //等级解锁
	EXCHANGE_DECORATE_COST(608), //装扮兑换
	PLAYER_CROSS_TECH_QUEUE_SPEEDUP(609),     // 远征科技研究加速
	CROSS_TECH_LEVEL_UP(610),  //远征科技升级
	ENERGIES_AWARD(611),    // 能源滚滚活动成就奖励
	GHOST_SECRET_DREW_CONSUME(612),        	//幽灵秘宝消耗
	GHOST_SECRET_RESET_CONSUME(613),        //幽灵秘宝重置次数消耗
	GHOST_SECRET_DREW_REWARD(614),         	//幽灵秘宝奖励
	GHOST_SECRET_DREW_RANDOM_REWARD(615),   //幽灵秘宝随机奖励
	VIRTUAL_LABORATORY_REWARD(616),   	//虚拟实验室成就奖励
	VIRTUAL_LABORATORY_CONSUME(617),   	//虚拟实验室消耗
	EQUIP_RESEARCH_LEVEL_UP(618), // 装备科技升级
	EQUIP_RESEARCH_BOX(619), // 装备科技宝箱奖励
	PLAY_OVERVIEW_MISSION_BONUS(620),		//玩法总览

	DRAGON_BOAT_CELEBRATION_MAKE_GIFT(621),  //端午-联盟庆典兑换消耗
	DRAGON_BOAT_EXCHANGE_COST(622), //端午-节日兑换消耗
	DRAGON_BOAT_EXCHANGE_ACHIEVE(623), //端午-节日兑换获得
	DRAGON_BOAT_BENEFIT_ACHIEVE_AWARD(624), //端午-特惠庆典成就奖励
	DRAGON_BOAT_LCUK_BAG_OPEN_COST(625), //端午-开福袋消耗
	DRAGON_BOAT_LCUK_BAG_OPEN_AWARD(626),//端午-开福袋获得
	DRAGON_BOAT_LCUK_BAG_ACHIEVE_AWARD(627),//端午-开福袋成就获得
	DRAGON_BOAT_RECHARGE_ACHIEVE_AWARD(628),//端午-充值天数成就获得
	ACTIVITY_EXCHANGE_GUILD_DONATE(629, PlayerExpResource.ACTIVITY_AWARD),    //残卷兑换 联盟捐献
	DRAGON_BOAT_GIFT_LOGIN_AWARD(630),  //端午-龙船送礼登录奖励
	MEDAL_FUND_BUY_AWARD(631),  //勋章基金活动购买奖励
	MEDAL_FUND_DDLIVERY_AWARD(632),  //勋章基金活动交割期奖励
	RECHARGE_WELFARE_TASK_REWARD(633),	  // 充值福利活动任务奖励
	RECHARGE_WELFARE_LOTTERY_REWARD(634), //充值福利活动抽奖奖励
	RECHARGE_WELFARE_LOTTERY_CONSUME(635), //充值福利活动抽奖消耗
	RECHARGE_WELFARE_RECEIVE_COUPON(636), //充值福利活动领取点券奖励
	RECHARGE_WELFARE_RECEIVE_DAILY_COUPON(637), //充值福利活动领取免费积分点券奖励
	ENERGY_INVEST_BUY_AWARD(638),  //能量源投资活动购买奖励
	ENERGY_INVEST_DDLIVERY_AWARD(639),  //能量源投资活动交割期奖励
	SUPERSOLDIER_INVEST_BUY_AWARD(640),  //能量源投资活动购买奖励
	SUPERSOLDIER_INVEST_DDLIVERY_AWARD(641),  //能量源投资活动交割期奖励
	
	OVERLORD_BLESSING_AWARD(642),  		//霸主膜拜膜拜奖励
	OVERLORD_BLESSING_SHARE_AWARD(643), //霸主膜拜分享奖励

	ARMIES_MASS_BUY_GIFT_ACHIVE(644), //沙场点兵礼包购买
	ARMIES_MASS_OPEN_SCULPTURE_REWARD(645),//沙场点兵翻开雕像奖励
	ARMIES_MASS_ACHIEVE_AWARD(646), //沙场点兵成就奖励
	ARMIES_MASS_BUY_OPEN_TIMES_COST(647), //沙场点兵购买翻开次数
	ARMIES_MASS_BUY_GIFT_COST(648),//沙场点兵礼包购买消耗
	SUPER_GOLD_TWO_REWARD(649), //超级金矿抽奖
	AIRDROP_SUPPLY_REWARD(650),	  // 空投补给活动任务奖励
	NEW_ORDER_EXP_BUY_CONSUME(651), //新服战令经验购买消耗
	NEW_ORDER_LEVEL_REWARD(652),  //新服战令等级奖励
	WAR_FLAG_TWO_EXCHANGE_TWO(653), //鹊桥会
	RECHARGE_QIXI_REWARD(654),	  //七夕充值任务奖励	
	DOUBLE_GIFT_BUY(655),     // 双享豪礼购买发货
	DOUBLE_GIFT_FREE(656),    // 双享豪礼免费礼包领取
	DOUBLE_ACC_GIFT_TAKE(657),//双享豪礼累计购买奖励
	
	ALLIANCE_CARNIVAL_EXCHANGE_TIMES(658), // 联盟总动员兑换
	GROUP_BUG_TASK_REWARD(659),	  // 团购任务奖励
	GROUP_BUG_COST(660),	  // 团购消耗
	GROUP_BUG_REWARD(661), 		//团购购买奖励
	GHOST_TOWER_COLLECT_RES(662),
	ORDNANCE_FORTRESS_OPEN_COST(663),  //军械要塞抽奖消耗
	ORDNANCE_FORTRESS_OPEN_ACHIVE(664),//军械要塞抽奖获得
	ORDER_TWO_SHOP_BUY(665), // 新版战令商店购买商品
	ORDER_TWO_LVL_BUY(666), // 新版战令解锁等级
	EQUIP_CARFTSMAN_GACHA(667), // 装备工匠抽奖
	EQUIP_CARFTSMAN_GACHA_EXTRA(668, PlayerExpResource.ACTIVITY_AWARD),  //装备工匠抽奖额外奖励
	EQUIP_CARFTSMAN_GACHA_DELETE(669, PlayerExpResource.ACTIVITY_AWARD), //装备工匠放弃词条奖励
	ARMOUR_ATTR_INHERIT(670),           //铠甲词条传承
	BATTLE_FIELD_ACHIEVE_AWARD(671),    //战场寻宝活动任务奖励
	BATTLE_FIELD_ROLLDICE_AWARD(672),   //战场寻宝活动投掷骰子奖励
	BATTLE_FIELD_ROLLDICE_CONSUME(673), //战场寻宝活动投掷骰子消耗
	BATTLE_FIELD_LOGINDAY_AWARD(674),   //战场寻宝活动每日登录奖励 
	RED_PACKAGE_RECIEVE_REWARD(675),    //红包活动，抢红包获取
	SUPER_SOLDIER_ANYWHERE(676), // 机甲无处不在
	SUPER_SOLDIER_CSKIN(677), // 机甲换形象
	BATTLE_FIELD_BUYDICE_CONSUME(678), //战场寻宝活动购买骰子消耗
	BATTLE_FIELD_BUYDICE_AWARD(679),   //战场寻宝活动购买获得骰子
	ORDER_EQUIP_LVL_BUY(680), // 装备战令解锁等级
	ARMAMENT_EXCHANGE_REWARD(681), //军备商城奖励 
	ARMAMENT_EXCHANGE_COST(682), //军备消耗
	ARMAMENT_EXCHANGE_COMMON(683), //军备商城
	ARMAMENT_EXCHANGE_ALLOPEN(684), //军备开启全部奖励
	MONTH_CARD_PRICE_CUT(685),      // 特权礼包半价
	ACTIVITY_POWER_FUND_CONSUME(686),      // 战力基金活动购买基金消耗
	GIFT_ZERO_FREE_REWARD(687),  // 0元礼包免费奖励领取

	CELEBRATION_COURSE_ACHIEVE_REWARD(688), //周年历程
	CELEBRATION_COURSE_ACHIEVE_SHARE_REWARD(689),//周年分享奖励
	CELEBRATION_SHOP_REWARD(690), //周年商城
	CELEBRATION_SHOP_EXCHANGE_REWARD(691), //周年商城兑换奖励
	CELEBRATION_SHOP_EXCHANGE_COST(692), //周年商城兑换消耗
	GREETINGS_REWARD(693),	  // 祝福语任务奖励
	FIRE_WORKS_FREE_REWARD(694),  	//周年庆烟花盛典免费领取的奖励
	FIRE_WORKS_LIGHT_COST(695),  	//周年庆烟花盛典点燃烟花消耗
	FIRE_WORKS_LIGHT_REWARD(696),  	//周年庆烟花盛典点燃烟花奖励
	CELEBRATION_FOOD_REWARD(697),  	//周年庆 庆典美食奖励
	CELEBRATION_FOOD_COST(698),  	//周年庆庆典美食 消耗
	CELEBRATION_FOOD_WORLD_COLLECT(699, PlayerExpResource.ACTIVITY_AWARD),  //庆典美食 世界采集
	CELEBRATION_FOOD_BEAT_YURI(700, PlayerExpResource.ACTIVITY_AWARD),
	//庆典美食 击败尤里
	CELEBRATION_FOOD_WISH(701, PlayerExpResource.ACTIVITY_AWARD),   		//庆典美食 军需补给
	ACTIVITY_JIGSAW_CONNECT_AWARD(702),    // 双十一拼图活动奖励
	GLOBAL_SIGN_TASK_REWARD(703),   //全服签到成就任务奖励
	SUPER_DISCOUNT_DRAW_COST(704),	//幸运折扣刷新奖池消耗 
	SUPER_DISCOUNT_BUY_COST(705),	//幸运折扣购买消耗
	SUPER_DISCOUNT_BUY_GAIN(706),	//幸运折扣刷新 购买获取
	ALLIANCE_CELEBRATE_DONATION_COST(707),  	//双十一联盟欢庆活动捐献消耗
	ALLIANCE_CELEBRATE_DONATION_REWARD(708),  //双十一联盟欢庆活动奖励
	RETURN_PUZZLE_ACHIEVE_AWARD(709),   // 回流拼图活动奖励

	ACTIVITY_DRAWING_SEARCH_RES_COLLECT(710, PlayerExpResource.ACTIVITY_AWARD),    //图纸搜索 资源收集
	ACTIVITY_DRAWING_SEARCH_WORLD_COLLECT(711, PlayerExpResource.ACTIVITY_AWARD),  //图纸搜索 世界采集
	ACTIVITY_DRAWING_SEARCH_BEAT_YURI(712, PlayerExpResource.ACTIVITY_AWARD),      //图纸搜索 击败尤里
	ACTIVITY_DRAWING_SEARCH_WISH(713, PlayerExpResource.ACTIVITY_AWARD),           //图纸搜索 许愿
	ENERGY_GATHER_AWARD(714),   	// 装扮投放系列活动二:能量聚集
	FIRE_REIGNITE_BUY_COST(715), //装扮投放系列活动三:重燃战火购买消耗
	FIRE_REIGNITE_BUY_REWARD(716), //装扮投放系列活动三:重燃战火购买获得
	FIRE_REIGNITE_DELIVER_REWARD(717), //装扮投放系列活动三:重燃战火交付获得
	FIRE_REIGNITE_BOX_REWARD(718), //装扮投放系列活动三:重燃战火宝箱获得
	RESOURCE_DEFENSE_REFRESH_SKILL(719),       //资源保卫战，刷新技能消耗
	PLANT_FACTORY_UNKOCK(720),
	PLANT_FACTORY_COLLECT(721),
	PLANT_FACTORY_UPGRADE(722),
	PLANT_TECH_UNKOCK(723),
	PLANT_TECH_UPGRADE(724),
	PLANT_TECH_CHIP_UPGRADE(725),
	GUNPOWDER_RISE_REWARD(726), 	//装扮投放系列活动四:浴火重生兑换获得
	GUNPOWDER_RISE_COST(727), 	//装扮投放系列活动四:浴火重生兑换消耗
	PLANT_FORTRESS_OPEN_COST(728),  //泰能宝库抽奖消耗
	PLANT_FORTRESS_OPEN_ACHIVE(729),//泰能宝库抽奖获得
	PLANT_FORTRESS_BUY_KEY_COST(730), //泰宝库购买钥匙
	PLANT_FORTRESS_BUY_KEY_ACHIEVE(731), //泰能宝库
	TIME_LIMIT_BUY_ACTION(732), // 限时抢购	
	PEAK_HONOUR_PLAYER_SCORE_AWARD(733), // 巅峰荣耀玩家积分奖励
	PEAK_HONOUR_GUILD_SCORE_AWARD(734), // 巅峰荣耀联盟积分奖励

	ACTIVITY_MILITARY_PREPARE_ADVANCED_AWARD(735),  //军事备战进阶固定奖励
	ENERGY_GATHER_TWO_AWARD(736),   	// 圣诞节系列活动一:冰雪计划活动
	FIRE_REIGNITE_TWO_BUY_COST(737), //圣诞节系列活动二:冬日装扮活动购买消耗
	FIRE_REIGNITE_TWO_BUY_REWARD(738), //圣诞节系列活动二:冬日装扮活动购买获得
	FIRE_REIGNITE_TWO_DELIVER_REWARD(739), //圣诞节系列活动二:冬日装扮活动交付获得
	FIRE_REIGNITE_TWO_BOX_REWARD(740), //圣诞节系列活动二:冬日装扮活动宝箱获得
	GUNPOWDER_RISE_TWO_REWARD(741), 	//圣诞节系列活动三:冰雪商城活动兑换获得
	GUNPOWDER_RISE_TWO_COST(742), 	//装扮投放系列活动三:冰雪商城兑换消耗
	CHRISTMAS_RECHARGE_REWARD(743), 	//圣诞节系列活动:累计充值
	PLAYER_TEAM_BACK_AWARD_TAKE(744), // 玩家回流H5活动奖励 领取
	GLOBAL_SIGN(745),   //全服签到
	BACK_PRIVILEGE_REWARD(746),  //回归特权礼包奖励
	ACTIVITY_LOGIN_FUND_TWO_AWARD(747, PlayerExpResource.ACTIVITY_AWARD), // 登录基金2活动奖励
	FIGHTER_PUZZLE_SERVEROPEN_ACHIEVE_AWARD(748, PlayerExpResource.ACTIVITY_AWARD),   // 武者拼图活动奖励 开服活动

	COMMAND_ACADEMY_SIMPLIFY_ACHIVE_REWARD(749),  //指挥官学院任务奖励
	COMMAND_ACADEMY_SIMPLIFY_BUY_COST(750),  //指挥官学院礼包购买消耗
	COMMAND_ACADEMY_SIMPLIFY_BUY_REWARD(751), //指挥官学院礼包购买获得
	COREPLATE_TASK_REWARD(752),	  //雄芯壮志活动奖励
	COREPLATE_BOX_REWARD(753),	  //雄芯壮志活动奖励
	HONG_FU_REC_GIFT_REWARD(754),       //洪福礼包奖励
	HONG_FU_UNLOCK_GIFT_REWARD(755),       //洪福礼包解锁奖励
	ARMOUR_STAR_ATTR_CHARGE(756), //装备星级属性充能一次
	ARMOUR_STAR_ATTR_CHARGE_TEN(757), //装备星级属性充能十次
	ARMOUR_STAR_ATTR_REFRESH(758), //装备词条属性刷新
	ARMOUR_STAR_UP(759), //装备升星
	
	RED_BLUE_OPEN_TICKET_CONSUME(760), // 红蓝对决翻牌消耗
	RED_BLUE_OPEN_TICKET_REWARD(761),  // 红蓝对决翻牌获取奖励
	RED_BLUE_REFRESH_CONSUME(762),     // 红蓝对决洗牌消耗
	
	AGENCY_COASTER_MARCH_REWARD(763), //情报中心搜索奖励
	AGENCY_REWARD(764), // 情报中心领取奖励
	AGENCY_CLUE_REWARD(765), // 情报中心领取线索奖励
	AGENCY_MARCH_VIT(766), // 情报中心行军体力消耗
	AGENCY_MARCH_VIT_RETURN(767), // 情报中心行军体力消耗返回
	AGENCY_BOX_REWARD(768), // 情报中心宝箱奖励
	MEDAL_FUND_TWO_BUY_AWARD(769),     //勋章基金活动购买奖励
	MEDAL_FUND_TWO_DDLIVERY_AWARD(770),  //勋章基金活动交割期奖励
	TRAVEL_SHOP_FRIENDLY_AWARD(771), // 黑市商店好友度奖励
    OBELISK_MISSION_REWARD(772),       //方尖碑任务奖励
    
    BEAUTY_BUY_FLOWER_CONSUME(773),  // 选美初赛购买道具消耗
    BEAUTY_BUY_FLOWER_AWARD(774),  // 选美初赛购买道具获得
	PLANT_INSTRUMENT_CHIP_UPGRADE(775),//破译仪器升级
	PLANT_SOLDIER_CRACK_UPGRADE(776), //战士主体破译
	PLANT_SOLDIER_CRACK_UPGRADE_CHIP(777), // 战士组件破译
	PLANT_CRYSTALANALYSIS_CHIP_UPGRADE(778), // 晶体分析阶段
	PLANT_SOLDIERSTRENGTHEN_TECH_UPGRADE(779), //强化阶段
	PLANT_SOLDIER_ADVANCE(780), //  泰能兵进化
	PLANT_SCIENCE_LEVEL_UP(781),  //泰能科技升级
	PRESTRESSING_LOSS_ACTIVITY_REWARD(782), // 预流失活动奖励

	DRESS_TREASURE_RANDOM_COST(783), //精装夺宝随机消耗
	DRESS_TREASURE_RANDOM_AWARD(784), //精装夺宝随机奖励
	DRESS_TREASURE_RESET_COST(785), //精装夺宝重置消耗
	DRESS_TREASURE_EXCAHNGE_GAIN(786),//精装夺宝兑换活动
	DRESS_TREASURE_EXCAHNGE_COST(787),//精装夺宝兑换消耗
	BEAUTY_CONTEST_REWARD(788),     // 选美初赛活动奖励
	BEAUTY_FINALS_REWARD(789),     // 选美决赛活动奖励

	LUCKY_BOX_BUY_NEED_ITEM_COST(790),  //幸运转盘购买道具
	LUCKY_BOX_BUY_NEED_ITEM_GAIN(791),  //幸运转盘购买道具
	LUCKY_BOX_RANDOM_COST(792),  //抽奖消耗
	LUCKY_BOX_RANDOM_GAIN(793), //抽奖获取
	LUCKY_BOX_RECOVER_COST(794), //幸运转盘回收消耗
	LUCKY_BOX_RECOVER_GAIN(795), //幸运转盘回收获得
	IMMGRATION(796), //迁服
	DYZZ_JOIN_ROOM(797), // 联盟锦标赛进入游戏
	DYZZ_QUIT_ROOM(798), // 联盟锦标赛离开游戏
	DYZZ_MOVE_CITY(799),   //定向迁城：消耗钻石或道具
	DYZZ_SHOP_BUY(800), //达雅商店购买
	PLANT_SECRET_TICKET_CONSUME(801), // 泰能机密翻牌消耗
	PLANT_SECRET_BOX_REWARD(802),     // 泰能机密开箱奖励
	PLANT_SECRET_BUY_CONSUME(803),    // 购买翻牌道具消耗
	PLANT_SECRET_BUY_AWARD(804),    // 购买翻牌道具获得
    RED_RECHARGE_BOX_AWARD(805), // 欢乐限购红包活动积分成就奖励
	RED_RECHARGE_AWARD(806), // 欢乐限购红包活动付费奖励
	
	NATIONAL_CONSTRUCTION_BUILDING_COST(807), // 国家建设处金条捐献
	NATIONAL_WAREHOUSE_DODATE_CONSUMNE(808),  // 国家仓库捐献消耗
	NATIONAL_WAREHOUSE_DODATE_AWARD(809),     // 国家仓库捐献奖励
	NATIONAL_WAREHOUSE_SHOP_COMSUME(810),     // 国家商店购买消耗
	NATIONAL_WAREHOUSE_SHOP_AWARD(811),       // 国家商店购买所得
	NATIONAL_MISSION_AWARD(812), // 国家任务奖励
	NATIONAL_MISSION_BUY_TIMES(813), // 国家任务购买次数
	NATIONAL_HOSPITAL_SPEED_CONSUME(814), // 国家医院死兵恢复加速消耗
	NATIONAL_BUILD_SUPPORT_CONSUME(815),  // 国家建筑资助消耗
	NATIONAL_BUILD_SUPPORT_AWARD(816),  // 国家建筑资助奖励
	NATIONAL_SHIP_ASSIST_AWARD(817), // 国家飞船助力奖励
	NATIONAL_TECH_ASSIST_AWARD(818), // 国家科技助力奖励
	NATIONAL_DONATE_COST(819), // 国家建设处重建捐献
	NATIONAL_CONSTRUCTION_REFRESH_COST(820), // 国家建设处任务刷新
	NATIONAL_DONATE_AWARD(821), // 国家重建捐献奖励
	ALLIANCE_WISH_ACHIEVE(822), //盟军祝福收获获取
	ALLIANCE_WISH_EXCAHNGE_COST(823),//盟军祝福兑换消耗
	ALLIANCE_WISH_EXCAHNGE_GAIN(824),//盟军祝福兑换获得
	ALLIANCE_WISH_EXCAHNGE_RESET_COST(825),//盟军祝福兑换重置消耗
	IMMGRATION_DIAMONDS(826),
	SEA_TREASURE_COMMON(827),//秘海珍寻普通奖励
	SEA_TREASURE_ADV(828),//秘海珍寻高级奖励
	SEA_TREASURE_BOX(829),//秘海珍寻开箱子
	SEA_TREASURE_BUY_ITEM(830),//秘海珍寻购买道具
	HIDDEN_TREASURE_REWARD(831),	  // 土拨鼠成就
	HIDDEN_TREASURE_LOTTERY(832),        // 土拨鼠开箱子 
	HIDDEN_TREASURE_REFRESH(833),        // 土拨鼠开箱子 
	HIDDEN_TREASURE_BUY(834),        // 土拨鼠开箱子 道具兑换
	HIDDEN_TREASURE_EXCAHNGE(835),  // 土拨鼠道具兑换
	SUPER_VIP_LOGIN_AWARD(836),     // 至尊vip每日登录奖励
	SUPER_VIP_ACTIVE_AWARD(837),    // 至尊vip每日活跃奖励
	SUPER_VIP_MONTH_GIFT_AWARD(838), // 至尊vip月度礼包奖励
	SUPER_VIP_DAILY_GIFT_AWARD(839),  // 至尊vip每日礼包奖励
	UPER_VIP_LEVEL_UP_AWARD(840),  // 至尊vip升级奖励
	LOVER_MEET_ANSWER_COST(841),  //七夕相遇答题消耗
	LOVER_MEET_ANSWER_AWARD(842),//七夕相遇答题获得
	LOVER_MEET_ENDING_AWARD(843),//七夕相结局获得
	LOVER_MEET_TASK_REWARD(844),//七夕相成就任务获得
	CROSS_TALENT_CLEAR(845),    // 天赋洗点：使用天赋点重置道具
	CROSS_TALENT_UPGRADE(846), // 跨服天赋加点
	NATION_MILITARY_REWARD(847), // 国家军功每日奖励 
	BATTLE_NATION_MILITARY(848), // 战斗获得军功
	RESOURCE_SPREE_MARCH(849),   //资源狂欢礼包行军消耗
	MONTHCARD_ACTIVE_COST_ITEM(850), // 激活月卡消耗道具

	HEAVEN_BLESSING_REWARD(851),   //天降洪福
	DYZZ_SEASON_ORDER_ACHIVE(852), //达雅赛季战令奖励
	SUPER_VIP_ACTIVE(853),         //至尊vip激活
	GRATEFUL_BENEFITS_REWARD(854),//感恩福利发奖
	HERO_WISH_TASK_REWARD(855),//英雄祈福成就任务获得
	HERO_WISH_COST(856),//英雄祈福许愿消耗
	HERO_WISH_REWARD(857),//英雄祈福许愿收获奖励
	
	HEAL_EXCAHNGE(858),  // sw道具兑换
	EXCLUSIVE_MEMORY_TASK_REWARD(859),//独家记忆成就任务获得
	SHARE_GLORY_DONATE_COST_A(860),  //荣耀同享捐献A道具扣除
	SHARE_GLORY_DONATE_COST_B(861),  //荣耀同享捐献A道具扣除
	SHARE_GLORY_DONATE_FIX_REWARD(862),  //荣耀同享捐献A道具扣除
	
	HIDDEN_TREASURE_LOTTERY_COST(863),        // 土拨鼠开箱子 消耗(863)	

	HONOUR_HERO_BEFELL_LOTTERY_REWARD(864),  //荣耀英雄降临抽奖获得
	HONOUR_HERO_BEFELL_LOTTERY_COST(865),//荣耀英雄降临抽奖消耗
	HONOUR_HERO_BEFELL_ACHIVE_REWARD(866), //荣耀英雄降临成就任务获得
	HONOUR_HERO_BEFELL_EXCAHNGE_COST(867),//荣耀英雄降临兑换消耗
	HONOUR_HERO_BEFELL_EXCAHNGE_GAIN(868),//荣耀英雄降临兑换获得	

	HONOR_REPAY_BUY_COST(869), 		//荣耀返利购买消耗
	HONOR_REPAY_BUY_REWARD(870), 	//荣耀返利购买奖励
	HONOR_REPAY_TASK_REWARD(871), 	//荣耀返利任务奖励
	HONOR_REPAY_REBATE_REWARD(872), //荣耀返利 投资返利奖励

	HONOUR_HERO_RETURN_LOTTERY_REWARD(873),  	//荣耀英雄回归单抽随机奖获得
	HONOUR_HERO_RETURN_LOTTERY_COST(874),		//荣耀英雄回归抽奖消耗
	HONOUR_HERO_RETURN_ACHIVE_REWARD(875), 	//荣耀英雄回归成就任务获得
	HONOUR_HERO_RETURN_EXCAHNGE_COST(876),	//荣耀英雄回归兑换消耗
	HONOUR_HERO_RETURN_EXCAHNGE_GAIN(877),	//荣耀英雄回归兑换获得
	
	HERO_SKILL_CAST(878),		// (behavior log)英雄技能释放
	HONOUR_HERO_RETURN_LOTTERY_REWARD_FIX(879),  	//荣耀英雄回归单抽固定奖获得
	HONOUR_HERO_RETURN_LOTTERY_REWARD_TEN(880),  	//荣耀英雄回归10抽随机奖获得
	HONOUR_HERO_RETURN_LOTTERY_REWARD_TEN_FIX(881),  	//荣耀英雄回归10抽固定奖获得
	NEW_FIRST_RECHARGE_REWARD(882),  	//新首充
	SHARE_TBLY_FAME_HALL_AWARD(883), // 泰伯利亚名人堂分享
	
	LOGIN_GIFTS_COMM_REWARD(884),   // 新版新手登录普通奖励
	LOGIN_GIFTS_ADV_REWARD(885),   // 新版新手登录进阶奖励

	LOGIN_GIFTS_ALL_REWARD(886),   // 登录豪礼活动一键领取所有奖励
	ITEM_RECYCLE_EXCHANGE_COST(887), //道具回收积分兑换道具消耗
	ITEM_RECYCLE_EXCHANGE_GAIN(888), //道具回收积分兑换道具获得
	ITEM_RECYCLE_RECYCLE_COST(889), //道具回收,回收道具的扣除
	ITEM_RECYCLE_RECYCLE_GAIN(890), //道具回收,回收道具的获得
	ITEM_RECYCLE_REDEMPTION_COST(891), //道具回收,回收道具的扣除
	ITEM_RECYCLE_REDEMPTION_GAIN(892), //道具回收,回收道具的获得
	ITEM_RECYCLE_RECOVERY_ORDINARY_COST(893), //道具回收,普通道具精炼的扣除
	ITEM_RECYCLE_RECOVERY_ORDINARY_GAIN(894), //道具回收,普通道具精炼的获得
	ITEM_RECYCLE_RECOVERY_EXTRAORDINARY_COST(895), //道具回收,高级道具精炼的扣除
	ITEM_RECYCLE_RECOVERY_EXTRAORDINARY_GAIN(896), //道具回收,高级道具精炼的获得
	ACTIVITY_GROUP_PURCHASE_DAILY_AWARD(897), // 基金团购活动每日登陆礼包
	
	HERO_ARCHIVE_UNLOCK(898), // 英雄档案解锁
	HERO_ARCHIVE_UPLEVEL(899), // 英雄档案解密
	HERO_ARCHIVES_EXCHANGE(900), // 英雄档案碎片兑换
	HERO_ARCHIVES_FULL_EXCHANGE(901), // 英雄档案碎片反向兑换
	DYZZ_ACHIEVE_REWARD(902), // 陨晶战场成就奖励
	HERO_ARCHIVES_OPEN_AWARD(903), // 英雄档案开启奖励
	NEWYEAR_LOTTERY_ITEM_REMOVE_CROSSDAY(904),  // 跨天清除
	NEWYEAR_LOTTERY_ACHIEVE(905), // 双旦活动成就奖励
	NEWYEAR_LOTTERY_GIFT_REWARD(906), // 双旦活动礼包购买奖励
	NEWYEAR_LOTTERY_REWARD(907), // 双旦活动抽奖获得奖励
	NEWYEAR_LOTTERY_COST(908), // 双旦活动抽奖消耗
	YQZZ_ACHIEVE_REWARD(909), //月球之战成就奖励 
	YQZZ_NATION_MILITARY(910), // 月球获得军功
	SUPER_SOLDIER_ENERGY_UNLOCK(911), // 机甲赋能解锁
	SUPER_SOLDIER_ENERGY_LEVEL_UP(912), // 机甲赋能升级
	ROSE_GIFT_ACHIEVE_REWARD(913), // 玫瑰赠礼成就奖励
	ROSE_GIFT_EXCHANGE(914), // 玫瑰赠礼兑换
	ROSE_GIFT_EXCHANGE_REWARD(915),//玫瑰赠礼兑换奖励
	ROSE_GIFT_DRAW(916), // 玫瑰赠礼兑换
	ROSE_GIFT_DRAW_REWARD(917),//玫瑰赠礼兑换奖励
	ROSE_GIFT_DRAW_RECOVER(918),//玫瑰赠礼回收消耗
	MACHINE_LAB_CLEAR_ITEM(919), //机甲研究所清除数据
	MACHINE_LAB_CONTRIBUTE_COST(920), //机甲研究所捐献消耗
	MACHINE_LAB_CONTRIBUTE_ACHIEVE(921), //机甲研究所捐选获得
	MACHINE_LAB_ORDER_ACHIEVE(922), //机甲研究所战令领奖
	MACHINE_LAB_EXCAHNGE_COST(923),//机甲研究所兑换消耗
	MACHINE_LAB_EXCAHNGE_GAIN(924),//机甲研究所兑换获得
	MACHINE_LAB_FINISH_EVENT_ACHIEVE(925),//机甲研究所事件获得
	SPACE_GUARD_ACHIEVE(926),  // 机甲舱体活动成就奖励 
	SPACE_MECHA_ATK_STRONG_AWARD(927), // 星甲召唤攻击据点奖励
	SPACE_MECHA_COLLECT_BOX_MARCH(928),    //星甲召唤采集宝箱行军出发
	SPACE_MECHA_ATK_STRONGHOLD_MARCH(929), //星甲召唤进攻据点单人行军出发
	SPACE_MECHA_MAIN_SPACE_MARCH(930),     //星甲召唤主舱单人行军出发
	SPACE_MECHA_SLAVE_SPACE_MARCH(931),    //星甲召唤子舱单人行军出发
	PROP_EXCHANGE(932),   // 使用道具返还物品

	SEASON_SHOP_COST(933),  // 赛季商店消耗

	SEASON_SHOP_GET(934),   // 赛季商店获得

	SEASON_ORDER_GET(935),   // 赛季战令
	SEASON_ORDER_BOX_GET(936),   // 赛季战令
	SEASON_ORDER_AD_GET(937),   // 赛季战令
	SEASON_ORDER_BOX_AD_GET(938),   // 赛季战令

	STAR_LIGHT_SIGN_ACHIEVE_REWARD(939),   // 世界勋章活动
	STAR_LIGHT_SIGN_REWARD(940),   // 世界勋章活动

	STAR_LIGHT_SIGN_REDEEM_COST(941),   // 世界勋章活动补签消耗

	STAR_LIGHT_SIGN_MUTLI_REWARD(942),   // 世界勋章活动

	STAR_LIGHT_SIGN_SCORE_COST(943),   // 世界勋章活动补签消耗
	
	BATTLE_FIELD_YIJIANPAOTU_CONSUME(944), //战场寻宝活动投掷骰子消耗
	NATION_MILI_RESET(945),// 军功重置
	DIFF_INFO_SAVE_REWARD(946),// 情报储蓄
	DIFF_NEW_SERVER_TECH_REWARD(947),// 全军出击
	PDD_ACHIEVE_REWARD(948), // 拼多多成就奖励
	PDD_COST(949), // 拼多多成就消耗
	PDD_GAIN(950), // 拼多多成就获得
	PDD_COST_CANCEL(951), // 拼多多成就消耗
	ARMOUR_QUANTUM_UP(952),// 量子升级消耗
	SPREAD_ACTIVITY_NEW_BAND(953), // 推广员新兵绑定奖励
	CHANGE_SVR(954), // 跨服消耗
	ACTIVITY_JXJIGSAW_CONNECT_AWARD(955),    // 双十一拼图活动奖励
	OVERLORD_BLESSING_ACHIEVE(956),  // 霸主膜拜成就奖励
	OVERLORD_BLESSING_START(957),    // 发起霸主膜拜行军
	DRESS_BUY(958), // 装扮购买

	BINZHONGZHUANHUAN(959),          // 使用超级兵经验道具
	LUCK_GET_GOLD_ACHIEVE_REWARD(960), // 鸿运夺金成就奖励
	LUCK_GET_GOLD_DRAW_COST(961), // 鸿运夺金抽奖消耗
	LUCK_GET_GOLD_DRAW_REWARD(962), // 鸿运夺金抽奖奖励
	LUCK_GET_GOLD_DRAW_GOLD_REWARD(963), // 鸿运夺金抽奖奖励
	SOLDIER_EXCHANGE_ACHIEVE(964), // 兵种转换
	DRESS_COLLECTION_ACHIEVE(965), // 周年庆称号活动成就奖励
	
	CELEBRATION_FUND_REWARD(966), //周年庆庆典基金礼包
	CELEBRATION_FUND_BUYSCORE(967), //周年庆庆典基金购买积分
	APPOINT_GET(968), // sss为人英雄331
	PLANTSOLDIER_MILITARY_UPGRADE(969), // 泰能兵军衔升级
	LIFETIME_WEEK_AWARD(970), // 终身卡周奖励
	LIFETIME_MONTH_AWARD(971), // 终身卡月奖励
	LIFETIME_UNLOCK_AWARD(972), // 终身卡周奖励
	LIFETIME_SPEED_BUY(973), // 终身卡加速道具
	
	GOLD_BABY_FIND_REWARD(974), //金币觅宝搜寻奖励
	GOLD_BABY_FIND_COST(975), //金币觅宝搜寻消耗
	GOLD_BABY_BUY_COST(976), //金币觅宝购买道具
	GOLD_BABY_ACHIEVE_REWARD(977), //金币觅宝成就奖励
	GOLD_BABY_BUY_ITEM(978), //金币觅宝购买获得道具
	
	GOLD_BABY_NEW_FIND_REWARD(979), //金币觅宝搜寻奖励 新服
	GOLD_BABY_NEW_FIND_COST(980), //金币觅宝搜寻消耗 新服
	GOLD_BABY_NEW_BUY_COST(981), //金币觅宝购买道具 新服
	GOLD_BABY_NEW_ACHIEVE_REWARD(982), //金币觅宝成就奖励 新服
	GOLD_BABY_NEW_BUY_ITEM(983), //金币觅宝购买获得道具 新服
	NEWBIE_TRAIN_ACHIEVE(984), // 新兵作训成就奖励
	NEWBIE_TRAIN_REWARD(985),  // 新兵作训作训奖励
	NEWBIE_TRAIN_GIFT(986),  // 新兵作训作训奖励

	DEVELOP_FAST_REWARD(987), //金币觅宝成就奖励

	DIRECT_GIFT_REWARD(988), //新直购礼包活动 礼包奖励

	CNY_EXAM_ACHIEVE_REWARD(989), //金币觅宝成就奖励 新服
	
	MEDAL_COLLECT(990), // 偷菜
	MEDAL_SHOU(991), // 偷菜
	MEDAL_DAILY(992), // 偷菜每日
	BACK_TO_NEW_FLY_ACHIEVE_REWARD(993),
	BACK_TO_NEW_FLY_SHOP_COST(994),
	BACK_TO_NEW_FLY_SHOP_GET(995),
	POINT_SPRINT_PLAYER_SCORE_AWARD(996),
	POINT_SPRINT_EXCHANGE(997),

	HERO_SOUL_LEVEL_UP(998),
	HERO_SOUL_STAGE_UP(999),
	RETURN_UPGRADE_ACHIEVE_REWARD(1000),
	RETURN_UPGRADE_SHOP_COST(1001),
	RETURN_UPGRADE_SHOP_GET(1002),
	
	ARMOUR_STAR_EXPLORE_UP_ONCE(1003), // 星能探索升级一次
	ARMOUR_STAR_EXPLORE_UP_ALL(1004), // 星能探索升级到满级
	ARMOUR_STAR_EXPLORE_JUMP(1005), // 星能探索升级一次
	PLANET_EXPLORE_ACHIEVE_REWARD(1006), //星能探索成就任务奖励
	PLANET_EXPLORE(1007), //星能探索抽奖
	PLANET_EXPLORE_BUY_ITEM(1008), //星能探索购买道具
	URL_REWARD(1009), //URL奖励
	LUCKY_STAR_ACHIEVE_REWARD(1010), //幸运星抽奖成就任务奖励
	HERO_SOUL_RESET(1011),

	GROW_UP_BOOST_RECOVER_COST(1012), //中部培养计划道具回收
	GROW_UP_BOOST_ACHIEVE_REWARD(1013), //中部培养计划任务奖励
	GROW_UP_BOOST_EXCHANGE_COST(1014), //中部培养计划兑换消耗
	GROW_UP_BOOST_EXCHANGE_GET(1015), //中部培养计划兑换获得
	GROW_UP_BOOST_BUY_COST(1016), //中部培养计划购买消耗
	GROW_UP_BOOST_BUY_GET(1017), //中部培养计划购买获得

	SUPPLY_CRATE_OPEN_COST(1018),//幸运补给箱开箱消耗
	SUPPLY_CRATE_OPEN_GET(1019),//幸运补给箱开箱获得
	SUPPLY_CRATE_BUY_COST(1020),//幸运补给箱购买消耗
	SUPPLY_CRATE_BUY_GET(1021),//幸运补给箱购买获得
	SUPPLY_CRATE_BOX_GET(1022),//幸运补给箱联盟宝箱获得
	SUPPLY_CRATE_ACHIEVE_REWARD(1023),//幸运补给箱成就奖励

	JIJIA_SKIN_ACHIEVE_AWARD(1024),//机甲皮肤活动成就奖励
	JIJIA_SKIN_RECHARGE(1025),//机甲皮肤活动充值获得
	JIJIA_SKIN_OPEN(1026),//机甲皮肤活动翻牌
	JIJIA_SKIN_REFRESH(1027),//机甲皮肤活动刷新
	
	ORDER_TWO_REWARD_NORMAL(1028), // 新版战令领取普通奖励
	ORDER_TWO_REWARD_ADVANCE(1029), //新版战令领取进阶奖励
	ORDER_TWO_REWARD_ADVANCE_DOUBLE(1030), //新版战令进阶暴击奖励
	MONTH_CARD_EXCHANGE(1031), //特权兑换商店兑换
	MONTH_CARD_RENEW(1032), //特权卡续费消耗续费卡
	MONTH_CARD_FREE_REWARD(1033), //免费特权卡奖励领取
	
	DAIY_BUY_GIFT_ACHIEVE_REWARD(1034),//每日必买成就任务奖励
	MANHATTAN_BASE_STAGE_UP(1035), //超武聚能底座升阶
	MANHATTAN_BASE_LEVEL_UP(1036), //超武聚能底座部件升级
	MANHATTAN_SW_UNLOCK(1037), //超级武器解锁
	MANHATTAN_SW_STAGE_UP(1038), //超级武器升阶
	MANHATTAN_SW_LEVEL_UP(1039), //超级武器部件升级

	ANNIVERSARY_GIFT_ACHIVE_REWARD(1040), //6重好礼成就任务奖励
	XHJZ_SHOP_COST(1041),//星海激战兑换消耗
	XHJZ_SHOP_GET(1042),//星海激战兑换获得
	XHJZ_CREATE_TEAM(1043),//星海激战创建战队

	LOTTERY_TICKET_USE_COST(1044), //呱呱乐抽奖消耗
	LOTTERY_TICKET_USE_ACHIEVE(1045), //呱呱乐玩家自抽获得
	LOTTERY_TICKET_ASSIST_APPLY_COST(1046), //呱呱乐玩家申请代刮消耗
	LOTTERY_TICKET_ASSIST_APPLY_FAIL_BACK(1047), //呱呱乐玩家申请代刮失败返回
	LOTTERY_TICKET_ASSIST_APPLY_REFUSE_BACK(1048), //呱呱乐玩家申请代刮被拒绝返回
	LOTTERY_TICKET_ASSIST_APPLY_TIME_OUT_BACK(1049), //呱呱乐玩家申请代刮超时返回
	LOTTERY_TICKET_RECOVER_COST(1050), //呱呱乐抽奖消耗

	PLANT_WEAPON_ACHIEVE_REWARD(1051), //泰能超武活动成就任务奖励
	PLANT_WEAPON_RESEARCH(1052), //泰能超武活动研究
	PLANT_WEAPON_SHOP_BUY(1053), //泰能超武活动商店购买
	PLANT_WEAPON_FREE_AWARD(1054), //泰能超武活动每日免费奖励领取
	PLANT_WEAPON_SPECIAL_CONSUME(1055), //泰能超武活动特殊道具消耗 
	
	SHOOTING_PRACTICE_ACHIEVE_REWARD(1056),  //打靶活动成就任务奖励
	SHOOTING_PRACTICE_EXCHANGE_COST(1057), //打靶活动兑换消耗
	SHOOTING_PRACTICE_EXCHANGE_GET(1058), //打靶活动兑换获得
	SHOOTING_PRACTICE_BUY_COUNT_COST(1059), //打靶活动购买次数消耗
	SHOOTING_PRACTICE_ITEM_RECOVER(1060), //打靶活动道具回收
	SHOOTING_PRACTICE_GAME_REWARD(1061),  //打靶活动游戏结束获得
	GROUP_BUY_TOP_DISCOUNT_REWARD(1062),  //万人团购最高折扣奖励领取
	

	STAR_INVEST_EXPLORE_REWARD(1063), //星海投资探索领奖
	STAR_INVEST_EXPLORE_SPEED_COST(1064), //星海投资探索加速消耗
	STAR_INVEST_EXPLORE_SPEED_BUY_COST(1065), //星海投资购买加速道具消耗
	STAR_INVEST_EXPLORE_SPEED_BUY_ACHIEVE(1066),//星海投资购买加速道具获得
	STAR_INVEST_FREE_REWARD_ACHIEVE(1067), //星海投资每日免费奖励
	STAR_INVEST_RECHARGE_REWARD_ACHIEVE(1068),//星海投资收割期奖励
	STAR_INVEST_RECHARGE_GIFT(1069), //星海投资充值礼包
	STAR_INVEST_ACHIEVE_REWARD(1070), //星海投资成就任务奖励
	COLLEGE_MISSION_REWARD_TAKE(1071), //学院任务领奖
	GUILD_BACK_ACHIEVE_REWARD(1072), //联盟回流成就奖励
	GUILD_BACK_VIT_POOL_COST(1073), //瓜分体力消耗
	GUILD_BACK_VIT_POOL_GET(1074), //瓜分体力获得
	GUILD_BACK_GOLD_POOL_COST(1075), //瓜分金币消耗
	GUILD_BACK_GOLD_POOL_GET(1076), //瓜分金币获得
	GUILD_BACK_SHOP_COST(1077), //瓜分金币消耗
	GUILD_BACK_SHOP_GET(1078), //瓜分金币获得
	GUILD_BACK_BOX_REWARD(1079), //联盟回流宝箱奖励
	COLLEGE_EXCHANGE(1080), //军事学院兑换商店兑换
	COLLEGE_GIFT_BUY(1081), //军事学院直购商店购买
	COLLEGE_RENAME_COST(1082), //军事学院改名消耗
	COLLEGE_QUIT(1083), //军事学院退出消耗
	PLANT_WEAPON_BACK_ACHIEVE(1084), //超武返场活动成就任务奖励
	PLANT_WEAPON_BACK_SHOPBUY(1085), //超武返场活动商店购买
	PLANT_WEAPON_BACK_DRAW(1086),    //超武返场抽奖
	BEST_PRIZE_ACHIEVE(1087), //新春头奖专柜活动成就任务奖励
	BEST_PRIZE_EXCHANGE(1088), //新春头奖专柜活动积分兑换
	BEST_PRIZE_SHOP_BUY(1089), //新春头奖专柜活动商店购买
	BEST_PRIZE_POOL_DRAW(1090), //新春头奖专柜活动抽奖
	BEST_PRIZE_END_CONSUME(1091), //新春头奖专柜活动结束后活动积分自动清除
	FGYL_SHOP_COST(1092),//反攻幽灵兑换消耗
	FGYL_SHOP_GET(1093),//反攻幽灵兑换获得

	PLANT_SOLDIER_FACTORY_ACHIEVE_REWARD(1094),
	PLANT_SOLDIER_FACTORY_DRAW(1095),
	PLANT_SOLDIER_FACTORY_AWARD(1096),
	PLANT_SOLDIER_FACTORY_SHOP_BUY(1097),
	
	QUEST_TREASURE_ACHIEVE_REWARD(1098), //秘境寻宝任务奖励
	QUEST_TREASURE_RANDOM_WALK_REWARD(1099), //秘境寻宝随机前进奖励
	QUEST_TREASURE_RANDOM_WALK_COST(1100), //秘境寻宝随机前进消耗
	QUEST_TREASURE_RANDOM_ITEM_BUY_COST(1101), //秘境寻宝随机道具购买消耗
	QUEST_TREASURE_RANDOM_ITEM_BUY_GET(1102), //秘境寻宝随机道具购买获得
	QUEST_TREASURE_SHOP_BUY_COST(1103), //秘境寻宝商店道具购买消耗
	QUEST_TREASURE_SHOP_BUY_GET(1104), //秘境寻宝商店道具购买获得
	QUEST_TREASURE_RECOVER_COST(1105),  //秘境寻宝回收消耗
	QUEST_TREASURE_RECOVER_GET(1106),  //秘境寻宝回收获得
	LABORATORY_UNLOCK_PAGE(1107), // 超能解锁页
	
	MECHA_CORE_BREAKTHROUGH(1108),   //机甲核心突破升级
	MECHA_CORE_TECH_LEVEL(1109),     //机甲核心科技升级
	MECHA_CORE_SLOT_LEVEL(1110),     //机甲核心槽位解锁
	MECHA_CORE_MODULE_RESOLVE(1111), //机甲核心模块分解
	MECHA_CORE_SUIT_UNLOCK(1112),    //机甲核心套装解锁
	
	FIRST_RECHARGE_THREE_REWARD(1113), //首冲活动3领奖
	
	SUBMARINE_WAR_ACHIEVE_REWARD(1114), //潜艇大战任务奖励
	SUBMARINE_WAR_SKILL_ITEM_BUY_COST(1115),//潜艇大战技能道具购买消耗
	SUBMARINE_WAR_SKILL_ITEM_BUY_GET(1116),//潜艇大战技能道具购买获得
	SUBMARINE_WAR_SHOP_ITEM_BUY_COST(1117),//潜艇大战商店道具购买消耗
	SUBMARINE_WAR_SHOP_ITEM_BUY_GET(1118),//潜艇大战商店道具购买获得
	SUBMARINE_WAR_GAME_COUNT_BUY_COST(1119),//潜艇大战游戏次数购买消耗
	SUBMARINE_WAR_GAME_OVER_REWARD(1120), //潜艇大战游戏结束奖励
	SUBMARINE_WAR_GAME_SKILL_COST(1121), //潜艇大战技能道具消耗
	SUBMARINE_WAR_RECOVER_COST(1122), //潜艇大战道具回收
	
	
	HONOUR_MOBILIZE_ACHIEVE_REWARD(1123), //荣耀动员任务奖励
	HONOUR_MOBILIZE_LOTTERY_TEN_COST(1124),//荣耀动员10抽消耗
	HONOUR_MOBILIZE_LOTTERY_TEN_REWARD(1125),//荣耀动员10抽奖励
	HONOUR_MOBILIZE_LOTTERY_ONE_COST(1126),//荣耀动员1抽消耗
	HONOUR_MOBILIZE_LOTTERY_ONE_REWARD(1127),//荣耀动员1抽奖励

	NEW_START_AWARD(1128),//破晓启程奖励
	MERGE_COMPETE_ACHIEVE(1129), //合服比拼活动成就奖励
	MERGE_COMPETE_GIFT_AWARD(1130), //合服比拼活动领取嘉奖礼包
	MECHA_CORE_REPLACE_ATTR(1131),  //机甲核心模块属性传承
	
	SUBMARINE_WAR_ORDER_ACHIEVE(1132), //潜艇大战战令获得
	SUBMARINE_WAR_ORDER_LVL_BUY(1133),//潜艇大战战令等级够买

	XQHX_SEASON_CLEAR(1134),//先驱回响赛季清空
	CORE_EXPLORE_REMOVE_OBS(1135),    //核心勘探活动清除障碍
	CORE_EXPLORE_SHOP_EXCHANGE(1136), //核心勘探活动商店兑换
	CORE_EXPLORE_TECH_AWARD(1137),    //核心勘探活动科技奖励
	CORE_EXPLORE_TECH_CONSUME(1138),  //核心勘探活动科技提升消耗
	CORE_EXPLORE_BUY_PICK(1139),      //核心勘探活动购买矿镐消耗
	CORE_EXPLORE_BOX_AWARD(1140),     //核心勘探活动领取宝箱奖励
	CORE_EXPLORE_SEND_PICK(1141),     //核心勘探活动赠送矿镐
	CORE_EXPLORE_SEND_OPEN(1142),     //核心勘探活动开启赠送道具

	DRESS_COLLECTION_ACHIEVE_TWO(1143), //周年庆称号活动成就奖励
	CORE_EXPLORE_END_CLEAR(1144),       //核心勘探活动结束回收道具
	AFTER_COMPETITION_ACHIEVE(1145),    //赛后庆典活动成就奖励
	AFTER_COMPETITION_HOMAGE_REWARD(1146), //赛后庆典活动致敬奖励
	AFTER_COMPETITION_BUY_GIFT(1147),      //购买礼物
	AFTER_COMPETITION_BIG_REWARD(1148),    //赛后庆典活动致敬奖励
	SEASON_PUZZLE_SYS_CONSUME(1149),       //赛季拼图373活动系统扣除
	SEASON_PUZZLE_SEND_ITEM(1150),         //赛季拼图373活动赠送拼图碎片
	SEASON_PUZZLE_REWARD(1151),            //赛季拼图373活动拼图奖励
	SEASON_PUZZLE_CONSUME(1152),           //赛季拼图373活动拼图消耗
	SEASON_PUZZLE_ACHIEVE(1153),           //赛季拼图373活动成就奖励
	SEASON_PUZZLE_REC_ITEM(1154),          //赛季拼图373活动接收他人赠送的拼图碎片
	
	CORE_EXPLORE_SPE_ITEM_AWARD(1155),     //核心勘探活动领取沙土附带的特殊道具
	
	GUILD_DRAGON_ATTACK_ACHIEVE(1156),    //巨龙来袭成就任务
	
	MT_TRAIN_ROB(1157), // 押镖集结联盟车
	MT_TRUCK_REACH(1158), // 押镖车辆到达
	MT_TRUCK_REFRESH(1159), // 刷新个人车奖励
	MT_TRAIN_REFRESH(1160), // 联盟列车刷新消耗（三段式）
	MT_SPECIAL_TRAIN(1161), //召唤豪华列车消耗

	WORLD_START_MASS_JOIN_AUTO(1162),  //自动加入集结

    DEEP_TREASURE_REWARD(1163),      // 秘藏成就
    DEEP_TREASURE_LOTTERY(1164),        // 秘藏开箱子
    DEEP_TREASURE_REFRESH(1165),        // 秘藏开箱子
    DEEP_TREASURE_BUY(1166),        // 秘藏开箱子 道具兑换
    DEEP_TREASURE_EXCAHNGE(1167),  // 秘藏道具兑换
    DEEP_TREASURE_LOTTERY_COST(1168),
	HOT_BLOOD_WAR_ACHIEVE_REWARD(1169), //热血畅战成就奖励
	HOT_BLOOD_WAR_ACHIEVE_ARMY(1170), //热血畅战收货士兵
	HOT_BLOOD_WAR_CURE_SPEED_COST(1171), //热血畅加速道具消耗

	HOME_LAND_SHOP_DRAW(1172), //家园商店消耗
	HOME_LAND_COLLECT_RES(1173), //家园建筑产出
	HOME_LAND_UPGRADE(1174), //家园升级建筑
	HOME_LAND_RECYCLE(1175),//家园拆解建筑
	HOME_LAND_GET_BUILD(1176),//家园商店抽奖
	
	
	CROSS_SHOP_COST(1177),//航海兑换消耗
	CROSS_SHOP_GET(1178),//航海兑换获得

	HOME_PUZZLE_BUY_COST(1179),		//心愿庄园购买
	HOME_PUZZLE_BUY_AWARD(1180),		//心愿庄园购买奖励
	HOME_PUZZLE_EXCHANGE_COST(1181),		//心愿庄园兑换商店消耗
	HOME_PUZZLE_EXCHANGE_AWARD(1182),		//心愿庄园兑换商店消耗
	HOME_PUZZLE_SCRATCH_COST(1183),	//心愿庄园刮卡消耗
	HOME_PUZZLE_SCRATCH_AWARD(1184),	//心愿庄园刮卡获取
	HOME_LAND_INIT(1185),//家园解锁
	HOME_LAND_ACTIVE_ATTR(1186),//家园激活属性获得
	HOME_PUZZLE_END_CONSUME(1187),	//心愿庄园活动结束清除积分
	
	ANNY_PARTY_ACHIEVE_REWARD(1188), //周年聚会384活动成就奖励
	ANNY_PARTY_CREATE_ROOM(1189),    //周年聚会384活动创建房间
	ANNY_PARTY_AWARD_REC(1190),      //周年聚会384活动领取聚会奖励
	ANNY_PARTY_SHOP_BUY(1191),       //周年聚会384活动商店购买
	ANNY_PARTY_BACK_ITEM(1192),      //停服重启后对过去房间的房主补发创建房间的消耗道具

	HOME_ROUND_ACHIEVE_REWARD(1193),  //命运轮盘机383活动成就奖励
	HOME_ROUND_DRAW_COST(1194),    	//命运轮盘机383活动抽奖消耗
	HOME_ROUND_DRAW_AWARD(1195),   	//命运轮盘机383活动抽奖获得
	HOME_ROUND_FLOOR_AWARD(1196),  	//命运轮盘机383层数领奖
	HOME_ROUND_EXCHANGE_COST(1197),	//命运轮盘机383商店消耗
	HOME_ROUND_EXCHANGE_AWARD(1198),	//命运轮盘机兑换商店消耗
	HOME_ROUND_BUY_COST(1199),	//命运轮盘机购买消耗
	HOME_ROUND_BUY_AWARD(1200),	//命运轮盘机购买消耗
	;
	
	/**
	 * 货币、道具、资源流动原因id
	 */
	int itemVal = -1;
	/**
	 * 指挥官经验流动原因id
	 */
	int expVal = -1;

	public String strValue() {
		return name();
	}

	public int intItemVal() {
		return itemVal;
	}
	
	public int intExpVal() {
		return expVal;
	}

	/**
	 * 构造函数
	 */
	private Action(int value) {
		this.itemVal = value;
	}
	
	private Action(int itemVal, int expVal) {
		this.itemVal = itemVal;
		this.expVal = expVal;
	}

	public static Action getAction(int type) {
		for (Action action : values()) {
			if (action.itemVal == type) {
				return action;
			}
		}
		return Action.NULL;
	}
	
	/**
	 * 重复性检测
	 * 
	 * @return
	 */
	public static boolean checkRepeated() {
		// action.itemVal的最大值，action.itemVal大于900的action个数
		int maxVal = -1, count = 0;
		Map<Integer, String> itemKeys = new HashMap<Integer, String>();
		for (Action action : values()) {
			String value = itemKeys.putIfAbsent(action.itemVal, "");
			if (value != null) {
				HawkLog.errPrintln("action repeated, id: {}", action.itemVal);
				return false;
			}
			
			if (action.itemVal <= maxVal) {
				HawkLog.errPrintln("action id error, currentVal: {}, lastVal: {}", action.itemVal, maxVal);
				return false;
			}
			
			maxVal = action.itemVal;
			if (action.itemVal > 900) {
				count++;
			}
		}
		
		if (maxVal - 900 != count) {
			HawkLog.errPrintln("action id error, maxVal: {}, count: {}", maxVal, count);
			return false;
		}
		
		return true;
	}
}
