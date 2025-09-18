package com.hawk.log;

import java.util.Arrays;
import java.util.Optional;

/**
 * tlog日志相关常量
 * 
 * @author lating
 *
 */
public class LogConst {
	/**
	 * 日志类型
	 */
	public static class LogType {
		/**
		 * 腾讯日志
		 */
		public static final int TLOG = 1;
	}
	
	/**
	 * 日志打印级别
	 *
	 */
	public static class LogLevel {
		public static final int DEBUG = 0;
		public static final int INFO  = 1;
		public static final int ERROR = 2;
	}
	
	/**
	 * 登陆状态
	 */
	public static class LoginType {
		// 非首次登陆
		public static final int NO_FIRST_LOGIN = 0;
		// 首次登陆
		public static final int FIRST_LOGIN = 1;
	}

	/**
	 * 日志类型
	 */
	public enum LogInfoType {
		onlineInfo,		// 在线信息
		heart,          // 心跳日志、服务器状态
		userlogin,      // 玩家登录
		userlogout,     // 玩家登出
		createuser,     // 玩家注册
		account_register, // 一个账号首次注册
		money_add,      // 游戏币增加
		money_consume,  // 游戏币消耗
		goods_add,      // 物品增加
		goods_sub,      // 物品减少
		player_exp_flow,// 人物等级流水
		round_flow,     // 単局战斗结束数据流水
		sns_flow,       // 社交流水
		guide_flow,     // 新手引导
		vip_level_flow,   // vip等级流水
		task_flow,        // 任务流水
		guild_flow,       // 公会流水
		idip_flow,        // IDIP流水
		sec_talk_flow,       // 聊天安全日志 
		sec_operation_flow,  // 操作流水安全日志
		sec_battle_flow,      // 玩家战斗安全日志
		world_resource_refresh_flow,// 世界资源刷新
		world_resource_collect_flow,// 世界资源采集
		world_attack_monster_flow,  // 世界打怪
		guild_action,     // 联盟行为
		build_flow,       // 建筑等级流水
		gift_bag_flow,    // 礼包购买信息
		activity_click,   // 活动点击事件 
		resource_add,     // 资源增加
		resource_sub,     // 资源减少
		buy_month_card,   // 购买月卡
		hero_change,      // 英雄属性变化日志     
		equip_change,     // 装备刷星变化日志
		sec_reporting,    // 举报
		recharge_flow,        // 充值付费
		external_purchase,     // 游戏外购买
		wishing_cost_flow,     // 军需处购买
		travel_shop_gift_refresh, // 黑市黑金礼包刷新
		fund_active,              // 基金激活
		commander_score_refresh,  // 指挥官积分刷新
		strongest_guild_person_score,  // 王者联盟个人积分刷新
		
		build_lv_up_flow,   // 建筑升级操作
		train_flow,         // 训练兵种操作
		tech_research_flow, // 科技研究操作
		world_attack_foggy, // 迷雾要赛
		foggy_box_flow,     // 迷雾宝箱操作流水
		rts_flow,			// rts
		gacha_flow,			// 英雄抽奖
		gacha_item_flow,    // 英雄抽奖获取物品
		gacha_equip,        // 抽装备
		gacha_equip_cost,   // 抽装备消耗
		push_gift_refresh,  // 推送礼包刷新
		boundary_recharge_click, //城堡等级临界点充值弹窗点击事件
		
		city_wall_data_flow, // 城防数据变化
		lottery_draw_flow,   // 盟军宝藏抽奖信息
		grab_res_limit,      // 资源掠夺上限打点
		gift_refresh_flow,	//超值礼包刷新
		enter_gift, 		//点击进入超值礼包
		chapter_mission_flow, // 章节类型任务变化记录
		store_house_flow,     // 联盟宝藏操作记录
		guild_tech_flow,      // 联盟科技操作记录
		army_change,          // 兵种数量变化
		battle_flow,		  // 战斗流水日志
		power_flow,           // 战力变化
		tibetan_army_hole,	  // 藏兵洞
		detect_flow,          // 侦查信息
		hero_skill_change,    // 英雄技能操作（合成、分解、升级）
		hero_skill_equip,     // 英雄技能安装、卸载
		video_flow,           // 真人视频打点
		anchor_gift,          // 主播礼物
		pandora_lottery,	  // 潘多拉抽奖
		pandora_exchange,	  // 潘多拉兑换
		send_reward_mail,	  // 发放带奖励邮件
		luckystar_lotter,     // 幸运星抽奖
		eva_hotline,          // 伊娃热线   
		president_war,        // 总统争夺战
		office_appoint,       // 官职任命
		warrior_king,		  // 英雄试炼
		mecha_scenario,       // 机甲剧情
				
		mecha_change,		  // 机甲属性变更
		mecha_march,		  // 机甲属性变更
		daily_mission,        // 每日福袋分享
		war_college_team,     // 联合军演房间组队
		war_college_high,     // 联合军演high起来
		super_lab_item_op,    // 超能实验室材料操作
		super_lab_oper,       // 超能实验室操作
		cross_server,         // 跨服操作
		cross_player_score,   // 跨服个人积分
		cross_guild_score,    // 跨服联盟积分
		cross_server_score,   // 跨服全服积分
		
		come_back_reward,     //老玩家回归 回归大礼
		come_back_achieve,    //老玩家回归 成就
		come_back_exchange,   //老玩家回归 兑换
		come_back_buy,        //老玩家回归 低折回馈
		friend_recall,			//召回好友
		friend_back,			//好友回归.
		treasure_hunt_tool_use, //新年寻宝，使用藏宝图
		treasure_hunt_touch_reward, //新年寻宝，触发奖励
		treasure_hunt_touch_monster, //新年寻宝，触发生成野怪
		treasure_hunt_touch_resource, //新年寻宝，触发生成资源
		treasure_hunt_attack_monster, //新年寻宝，攻击野怪
		treasure_hunt_collect_resource,//新年寻宝，采集资源
		treasure_hunt_resource_battle, //新年寻宝，采集点战斗
		
		skill_cast, // 技能释放
		war_flag,   // 联盟插旗
		custom_gift, // 私人定制礼包
		auto_monster, // 自动打野
		college_create, // 创建军事学院
		college_dismiss, // 解散军事学院
		college_reward,  // 军事学院在线领取奖励
		
		voice_room,		//联盟语音
		click_event,    //点击事件
		invite_task,    //密友邀请任务
		order_exp_buy,	// 经验购买
		order_auth_buy, // 进阶权限购买
		order_exp_flow, // 战令经验流水
		order_flow,		// 战令任务完成流水
		
		proctect_soldier,  // 新兵救援
		bounty_hunter_hit, // 赏金猎人
		hero_talent_change, // 英雄天赋强化
		hero_talent_random, //英雄天赋随机
		hero_talent_select, // 选择
		plan_activity_lottery, //源计划抽奖打点
		daily_sign_sign, //月签签到
		
		receive_soldier, // 士兵领取（大R复仇或新兵救援）
		time_limit_store,  // 限时商店触发上架
		tiberium_cross,  //泰伯利亚的瓦加跨服行为.
		tiberium_sign_up, // 泰伯利亚报名
		tiberium_match, // 泰伯利亚匹配
		tiberium_match_guild, // 泰伯利亚参与匹配联盟信息
		tiberium_enter_room, // 泰伯利亚进入战场
		tiberium_quit_room, // 泰伯利亚退出战场
		tiberium_player_score, // 泰伯利亚个人结算积分
		tiberium_guild_score, // 泰伯利亚联盟结算积分
		tiberium_battle_score, // TBLY 战场结果
		tiberium_build_score,//TBLY 建筑得分, 控制时间
		tbly_player_honor, // 泰伯利亚每分钟积分
		tbly_guild_honor, // 泰伯利亚每分钟积分
		tiberium_nian_kill,// 击杀机甲
		tiberium_nuclear_shot,// 发射核弹
		tbly_player_kill_monster,  //泰伯利亚杀怪
		tbly_guild_use_order, //泰伯利亚使用号令
		
		tiberium_leagua_match, // 泰伯利亚联赛匹配
		tiberium_leagua_join, // 泰伯利亚联赛入选正赛联盟
		tiberium_leagua_guild_war_info, // 泰伯利亚联赛出战联盟信息
		tiberium_leagua_guild_manage_end, // 泰伯利亚联赛出战双方锁定数据信息
		tiberium_leagua_player_war_info, // 泰伯利亚联赛出战玩家信息
		tiberium_leagua_guild_season_reward, // 泰伯利亚联赛联盟赛季积分奖励
		tiberium_leagua_player_season_reward, // 泰伯利亚联赛个人赛季积分奖励
		tiberium_leagua_quit_room, // 泰伯利亚联赛退出战场
		tiberium_leagua_enter_room, // 泰伯利亚联赛进入战场
		tiberium_leagua_player_score, // 泰伯利亚联赛个人积分信息
		tiberium_elo_score, // 泰伯利亚elo积分
		
		sw_guild_honor, // 星球大战每分钟积分
		sw_quit_room, // 星球大战退出战场
		sw_enter_room, // 星球大战进入战场
		sw_join_guild, // 星球大战参战联盟
		dress_send, //装扮赠送
		spread_log, //推广员活动玩家打点
		invest,     //投资理财
		
		lucky_discount_draw, //幸运折扣刷新奖池
		lucky_discount_buy, //幸运折扣刷购买
		cross_fortress_war, //远征航海要塞
		cross_fortress_occupy, //航海远征要塞占领时间.
		hero_trial_receive, // 英雄试炼接受任务
		hero_trial_refresh_mission, // 英雄试炼刷新任务
		hero_trial_complete, // 英雄试炼完成任务
		hero_trial_cost_refresh, // 英雄试炼金币刷新
		hero_trial_cost_complete, // 英雄试炼金币完成
		revenge_shop,    // 复仇商店触发记录
		hero_back_exchange,  // 英雄返场兑换
		hero_back_buy,       // 英雄返场购买
		black_tech_draw, 	//黑科技刷新奖池
		black_tech_buy, 	//黑科技购买	
		black_tech_active,  //黑科技激活
		city_shield,        //保护罩变动
		armour_add,        //铠甲产出
		armour_unlock_suit,	//铠甲套装解锁
		armour_resolve,	//铠甲分解
		armour_intensify,	//铠甲强化
		armour_inherit,   //铠甲传承
		armour_breakthrough,  //铠甲突破
		
		fully_armed_search, //全军动员探索
		fully_armed_buy, 	//全军动员购买	
		pioneer_gift_buy,   //先锋豪礼购买
		roulette_lottery,	//时空轮盘抽奖
		roulette_box,		//时空轮盘箱子领取
		championship_guild,  //锦标赛联盟信息
		championship_player, // 
		vit_change,          //体力变化
		daily_active_change, //每日活跃任务积分变化
		
		skin_plan,	  //皮肤计划扔骰子
		hero_skin_change, // 英雄皮肤解锁,升级
		daily_recharge,   // 今日累充活动
		daily_recharge_new, // 今日累充新版
		guard_relation,	//守护关系.
		guard_player_level, //各个档位的玩家数量.	
		machine_awake_person_damage,//机甲觉醒个人总伤害变化
		mid_autumn_gift,			//中秋庆典礼包购买
		gift_buy_voucher,    // 用券购买礼包信息
		ac_receive_mission, // 联盟总动员接受任务
		ac_finish_mission, // 联盟总动员完成任务
		ac_abandon_mission, // 联盟总动员放弃任务
		ac_buy_times, // 联盟总动员购买次数
		kill_gundam, //机甲击杀(最终一击或致命一击)
		guard_value,  //守护值.
		time_limit_drop, //限时掉落.
		hell_fire,      //火线征召
		
		ac_travel_shop_assist_achieve_finish,  //特惠商人助力庆典，任务完成
		ac_travel_shop_refresh_cost, //特惠商店刷新消耗
		ac_redkoi_award,  //锦鲤中奖纪录
		ac_redkoi_player_wish_cost, //玩家金币消耗和锦鲤点
		medal_treasure_lottery,    		  //勋章宝藏抽奖
		divide_gold_open_red_envelope,    //金币瓜分开红包
		player_share,      //分享打点

		player_share_fame_hall,      //名人堂分享打点
		pylon_march_reach,//能量塔到达开始采集
		pylon_march_battle, //能量塔战斗
		evolution_exchange, //英雄进化奖池兑换
		evolution_exp_change,   // 英雄进化积分变动
		evolution_task,         // 英雄进化任务完成情况
		flight_plan_exchange,   // 飞行计划商品兑换
		simulate_war_sign,		//攻防模拟战报名相关信息
		simulate_war_encourage,  //攻防模拟战的助威信息
		simulate_war_adjust,	   //攻防模拟战的调整信息.
		scenario_entrance_click,   // 场景入口点击事件
		
		command_academy_gift_buy, //指挥官学院礼包购买
		command_academy_rank,     //指挥官学院排名
		command_academy_buy_count, //指挥官学院团购人数
		command_academy_simplify_gift_buy, //指挥官学院礼包购买
		command_academy_simplify_rank,     //指挥官学院排名
		command_academy_simplify_buy_count, //指挥官学院团购人数
		
		christmas_task,			  //圣诞完成任务.
		christmas_task_receive,		  //圣诞任务领奖.
		christmas_box,					  //圣诞宝箱.
		equip_black_market_refine, //装备黑市精炼
		march_emotion,    // 使用行军表情
		hero_collect, //英雄羁绊数
		snowball_goal, // 雪球大战进球
		cyborg_build_control, // 赛伯建筑占领流水
		cyborg_nuclear_hit, // 赛伯核弹命中
		
		cyborg_sign_up, // 赛伯-报名
		cyborg_match_room, // 赛伯-匹配房间信息
		cyborg_match_team, // 赛伯-匹配战队信息
		cyborg_match_player, // 赛伯-出战玩家信息
		cyborg_enter_room, // 赛伯-进入战场信息
		cyborg_quit_room, // 赛伯-退出战场信息
		cyborg_player_score, // 赛伯-个人积分信息
		cyborg_team_score, // 赛伯-战队积分信息
		cyborg_nian_kill,
		resource_defense_build, // 资源保卫战建造
		resource_defense_steal, // 资源保卫战偷取
		resource_defense_exp, // 资源保卫战获取经验

		chrono_gift_task_finish,  //时空豪礼任务完成
		chrono_gift_achieve_free_award, //时空豪礼免费礼品领取
		chrono_gift_unlock, //时空豪礼解锁
		dress_show_change,  // 更换装扮外观
		dress_god_change,  // 神话外观激活
		recharge_fund_invest, // 充值基金投资
		recharge_fund_recharge, // 充值基金充值
		recharge_fund_reward, // 充值基金领奖
		guild_shop_add, // 联盟商店补货
		ss_armour_attr_change, // SS装备日志(废弃)
		armour_change,// S品质装备并且等级大于一级的装备日志
		
		back_gift_lottery, //回归有礼抽奖
		power_send_message_send,//体力赠送,信件发出数量
		cyborg_season_guild_score, // 赛博-赛季联盟积分信息
		cyborg_season_star, // 赛博-赛季段位流水
		cyborg_season_team_rank_award,//赛博-赛季段位排位奖励
		cyborg_season_team_star_award,//赛博-赛季段位奖励
		exchange_decorate_level,//免费兑换装扮等级经验信息
		ghost_secret_drew_result,	//幽灵秘宝翻牌结果
		ghost_secret_reset_info, 	//幽灵秘宝重置数据
		ghost_secret_reward_info, 	//幽灵秘宝奖励触发数据
		exchange_decorate_mission,//免费兑换装扮等级经验信息
		energies_self_score, // 能源滚滚个人积分
		energies_guild_score, // 能源滚滚联盟积分
		energies_rank, // 能源滚滚排行
		virtual_laboratory_open, // 虚拟实验室开牌
		equip_research_level_up, // 装备科技升级

		dragon_boat_celebration_level_reward, //端午-联盟庆典升级发奖 
		dragon_boat_celebration_donate, //端午-联盟经验贡献 
		dragon_boat_exchange,//端午-兑换道具 
		dragon_boat_gift_achieve,//端午-龙船领奖
		dragon_boat_lucky_bag_open,//端午-福袋购买
		dragon_boat_recharge_days,//端午-充值天数
		medal_fund_reward_score, // 勋章基金奖励
		laboratory_op, // 超能核心操作
		armies_mass_open_sculpture,//沙场点兵，翻开雕像
		energy_invest_reward_score, //能量源投资
		supersoldier_invest_reward_score, //机甲投资
		overlord_blessing_info, //霸主膜拜
		
		new_order_exp_buy,	// 新服战令-经验购买
		new_order_auth_buy, // 新服战令-进阶权限购买
		new_order_exp_flow, // 新服战令-战令经验流水
		new_order_flow,		// 新服战令-战令任务完成流水

		season_order_auth_buy, // 新服战令-进阶权限购买
		season_order_exp_flow, // 新服战令-战令经验流水
		season_order_flow,		// 新服战令-战令任务完成流水
		tiberium_leagua_guild_info, // 泰伯联赛参赛联盟信息
		tiberium_leagua_war_result, // 泰伯联赛战场结果信息

		tiberium_leagua_new_signup_old,// 泰伯联赛报名巅峰赛区

		star_light_sign_award,//世界勋章奖励
		star_light_sign_score,//世界勋章奖励
		star_light_sign_choose,//世界勋章奖励

		double_gift_buy,   // 双享豪礼购买
		zk_remove_city,    // 中控飞堡
		group_buy,   		// 团购购买
		ghost_tower_attack,   //幽灵工厂行军
		ghost_tower_reward_email, //幽灵工厂奖励发放
		ordnance_fortress_open,   //军械要塞开奖
		ordnance_fortress_advance, //军械要塞进阶

		
		cake_share_reward_achieve,	//周年庆-蛋糕领奖
		
		equip_carftsman_attr,		// 装备工匠词条属性变动
		red_package_open,           //周年抢红包
		battle_field_dice,          // 战地寻宝活动骰子购买或使用
		battle_field_dice_reward,   // 战地寻宝活动投骰子奖励
		battle_field_buy_gift,      // 战地寻宝活动购买通行证
		activity_achieve,           // 活动成就任务

		celebration_food_make,		//周年庆 - 庆典美食,蛋糕制作
		fire_works_for_buff_active,  //周年庆烟花盛典激活buff
		armament_exchange_first,  //军备首次兑换

		order_equip_exp_buy,  // 装备战令经验购买
		order_equip_auth_buy, // 装备战令进阶权限购买
		order_equip_exp_flow, // 装备战令经验流水
		order_equip_flow,     // 装备战令任务完成流水
		greet_activity_item_index,	//周年祝福语随机出来的图片
		
		xzq_guild_participate,  //小站区参与联盟成员数量
		xzq_guild_signup,       //小站区报名
		xzq_npc_attacked,       //小战区攻破NPC
		xzq_control,            //小站区控制
		xzq_record_damage,      //小站区前三伤害
		xzq_record_fist_occupy, //小战区首次攻破
		xzq_record_occupy,      //小站区攻破
		xzq_player_participate, //小战区玩家参与
		xzq_guild_signup_cancel,//小站区取消报名
		xzq_control_time,       //小站区控制时间
		xzq_init_remove,        //小战区初始化移除点
		xzq_gift_send,          //小站区礼包发放
		
		super_discount_draw, //幸运折扣刷新奖池
		super_discount_buy, //幸运折扣刷购买
		global_sign,    //全服签到
		alliance_celebrate_score, // 双十一联盟捐献积分
		alliance_celebrate_reward, // 双十一联盟庆典玩家领取奖励
		return_puzzle_flow,     // 回流拼图完成流水
		plant_factory_flow, // 泰能工厂
		plant_tech_flow,
		resource_defense_skill_refresh, //资源保卫战 特工能力刷新/激活 记录
		resource_defense_skill_effect,//资源保卫战 特工能力 生效
		fire_reignite_receive_box,		//装扮投放系列活动三:重燃战火 领取宝箱每次一个

		plant_fortress_open,   //泰能宝库开奖
		plant_fortress_advance, //泰能宝库进阶
		player_usertag_change,  // 平台信息授权变更
		account_inherit_flow,   // 传承记录
		inherit_cond_result,    // 传承条件判断结果

		military_prepare_advanced_reward, //军事战备进阶奖励
		fire_reignite_receive_box_two,		//装圣诞节系列活动二:冬日装扮活动 每次领取宝箱
		
		peak_honour_score,		//巅峰荣耀获取积分
		time_limit_buy,		//限时抢购
		time_limit_buy_water,		//限时抢购注水
		christmas_recharge,		//圣诞节累计充值
		login_fund_two_buy,       // 登录基金购买

		coreplate_score_box,  //雄心壮志抽奖
		hong_fu_gift_unlock, //洪福礼包解锁
		hong_fu_gift_reward, //洪福礼包领奖
		asa_info,            //产品投放 Apple Search Ads上报信息
		server_statistic, // 服务器数据统计
		
		agency_levelup, // 情报中心升级
		agency_award, //情报中心奖励
		agency_refresh, //情报中心刷新
		agency_box,   //情报中心箱子奖励
		agency_complete,//情报中心箱子奖励
		redblue_ticket_flow, // 红蓝对决翻牌活动
		plant_soldier_flow,// 泰能兵强化
		
		dress_treasure_random, //精装夺宝随机
		dress_treasure_reset,//精装夺宝重置
		time_limit_client,// 限时抢购客户端打点记录
		login_power,// 战力记录
		lucky_box_random, //幸运转盘
		plant_secret, // 泰能机密
		obelisk_award,// 领取房间被奖励
		obelisk_mission, // 房间被任务状态改变 
		prestressing_loss, // 预流失活动激活
		
		dyzz_battle_result,   //达雅之战结束点打
		dyzz_match_time,    //达雅站站匹配时间
		dyzz_guild_use_order, //dyzz使用号令
		dyzz_battle_score,
		dyzz_season_score_change,//赛季记分变化
		
		nation_hospital_del, // 国家医院删除死兵
		nation_recover_queue, // 国家医院死兵恢复时间变化
		nation_accept_count, // 国家医院接收死兵具体数量
		nation_speed_recover, // 国家医院加速恢复
		nation_shop_exchange, // 国家商店兑换
		nation_warehouse_donate, // 国家仓库捐献
		nation_shippart_consume, // 飞船部件强化消耗 
		
		nation_status_change, // 国家状态改变
		nation_rebuild, // 国家重建捐献
		nation_building_upgrade_start, // 国家建筑升级开始
		nation_building_upgrade_end, // 国家建筑升级结束
		nation_build_support, // 国家金条资助
		
		nation_build_quest_start, // 国家建设任务开始
		nation_build_quest_cancel, // 国家建设任务取消
		nation_build_quest_over, // 国家建设任务结束
		nation_build_quest_refresh, // 国家任务刷新
		nation_build_item_use, // 国家建设道具使用

		nation_ship_upgrade_start, // 国家飞船部件升级开始
		nation_ship_upgrade_cancel, // 国家飞船部件升级取消
		nation_ship_upgrade_over, // 国家飞船部件升级结束
		nation_ship_upgrade_assist, // 国家飞船助力
		
		nation_misison_receive, // 国家任务接受
		nation_misison_giveup, // 国家任务放弃
		nation_misison_failed, // 国家任务失败
		nation_misison_finish, // 国家任务完成
		nation_misison_delete, // 国家任务删除
		nation_misison_buy, // 国家任务购买道具
		nation_misison_disapper, // 国家任务消失
		
		alliance_wish_sign, //盟军祝福签到
		alliance_wish_help, //盟军祝福签到帮助

		heaven_blessing_pay, //天降洪福支付

		heaven_blessing_active, //天降洪福激活

		heaven_blessing_award, //天降洪福领奖

		heaven_blessing_random_award, //天降洪福随机奖励

		heaven_blessing_open, //天降洪福打开界面

		grateful_benefits_award,//感恩福利领奖

		honor_repay_buy_reward,		//荣耀返利 购买

		dyzz_occupy,//建筑占领
		dyzz_achieve_reach,//成就达成
		dyzz_achieve_take,//成就领取

		pdd_order_create,//发起拼单

		pdd_order_done,//拼单成功

		pdd_order_fail,//拼单失败

		pdd_buy_alone,//单独购买

		change_svr_apply,//报名

		change_svr_cancel,//报名

		change_svr_real,//转服

		luck_get_gold_num,//抽中数字

		luck_get_gold_win,//抽中金条

		back_to_new_fly_old,//老服触发活动
		back_to_new_fly,//新服触发活动

		star_explore_up,//星能探索升级
		star_explore_jump,//星能探索跃迁
		star_explore_up_log,//星能探索升级
		star_explore_jump_log,//星能探索跃迁
		supply_crate_score_self,//自己开箱获得积分
		supply_crate_score_guild,//联盟获得积分

		xhjz_join,//进入战场
		xhjz_quit,//退出战场
		xhjz_finish,//战场结束
		xhjz_team,//队伍管理
		xhjz_member,//人员管理
		xhjz_season_match,//联赛匹配结果
		xhjz_season_result,//联赛战场结果
		xhjz_season_qualifier,//联赛入围结果
		xhjz_season_final,//联赛最终排名
		guild_back_buff,//联盟回流 增益激活
		new_start_active,//破晓启程激活

		honor_repay_receive_reward,	//荣耀返利 领取返利奖励
		
		nation_tech_tool, // 国家科技使用道具
		nation_tech_research, // 国家科技研究
		nation_tech_giveup, // 国家科技取消研究
		nation_tech_finish, // 国家科技研究完成
		nation_tech_help, // 国家科技助力
		nation_tech_skill, // 国家科技释放技能
		
		super_vip_active,  // 至尊vip激活
		cross_tech_research_flow, // 远征科技
		lover_meet_ending, //七夕相遇结局
		plant_science_research_flow, //泰能科技树研究
		
		
		cross_activity_player_strength,  //跨服活动玩家新战斗力参数
		cross_activity_team_param,//跨服活动历史战绩参数
		cross_activity_match_power,//跨服活动匹配参数计算
		cross_activity_match, //跨服活动匹配结构
		cross_activity_start,//跨服活动开始
		
		cross_activity_score_add,//跨服活动-积分增加
		cross_activity_score_box,//跨服活动-领取积分宝箱
		cross_activity_self_score_rank,//跨服活动-个人积分排名
		cross_activity_mission,//跨服活动-领取成就任务奖励
		cross_activity_talent,//跨服活动-获取国家战略点
		cross_activity_pylon_march,//跨服活动-向能量塔发起行军
		cross_activity_self_talent_rank,//跨服活动-个人战略排名
		cross_activity_occupy_president,//跨服活动-攻占盟总建筑
		cross_activity_control_president,//跨服活动-控制盟总建筑
		cross_activity_send_gift,//跨服活动-分配礼包
		cross_activity_receive_box,//跨服活动-领取宝箱
		
		cross_activity_guild_score_rank,//跨服活动-联盟积分排名
		cross_activity_guild_talent_rank,//跨服活动-联盟战略排名
		cross_activity_server_score_rank,//跨服活动-国家积分排名
		cross_activity_gen_box,//跨服活动-爆仓
		cross_activity_pylon_refresh,//跨服活动-能量塔刷新
		
		march_speed_item_use, //  行军加速道具使用
		
		hero_wish_choose,  //英雄祈福选择祈福
		
		alliance_wish_gift_buy, //盟军祝福礼包购买
		alliance_wish_achieve, //盟军祝福收获
		
		guild_detail, // 联盟堡垒详细日志信息
		honour_hero_befell_lottery, //荣耀英雄降临抽奖
		
		client_tlog, //客户端日志
		strength, // 玩家个人实力打点
		share_glory_donate,	//荣耀同享玩家捐献
		share_glory_energy_levelup,	//荣耀同享能量柱升级
		cross_activity_fight_guild, // 跨服联盟出战
		newyear_lottery_paygift,  // 双旦活动礼包购买
		newyear_lottery_result,   // 双旦活动头彩抽奖
		newyear_lottery_achieve,  // 双旦活动领取联盟进度奖励
		
		yqzz_declare_war, //月球之战宣战
		yqzz_build_control_change, //月球之战建筑控制变化
		yqzz_build_guild_control_count,//月球之战联盟控制建筑数量
		yqzz_player_rank_reward, //月球之战玩家最终奖励
		yqzz_guild_rank_reward,//月球之战联盟最终奖励
		yqzz_country_rank_reward, //月球之战国家最终奖励
		yqzz_achieve_reward,//月球之战国家最终奖励
		yqzz_match, //匹配结果
		yqzz_match_power, //匹配战力
		yqzz_battle_start_power,//战斗开始
		yqzz_enter_room,// 进入副本
		yqzz_quit_room,//退出副本 
		yqzz_room_info,// 副本人数
		super_soldier_energy, // 机甲赋能
		tiberium_player_war_info, //泰伯玩家参战信息
		machine_lab_drop,  //机甲研究所 -攻坚芯片掉落
		machine_lab_contribute, //机甲研究所 -捐献 
		machine_lab_order_reward,//机甲研究所 -领取战令奖励
		machine_lab_exchange,   //机甲研究所 -领取战令奖励 
		
		space_mecha_point,      // 星甲召唤活动领取星币
		space_mecha_place,      // 星甲召唤放置舱体
		space_mecha_def_war,    // 星甲召唤舱体防守战斗结果
		space_mecha_atk_stronghold, // 星甲召唤进攻据点
		space_mecha_collect,    // 星甲召唤采集宝箱
		space_mecha_stage_end,  // 星甲召唤阶段结束
		space_mecha_def_change, // 星甲召唤玩家进入或离开舱体的记录
		guild_formation, // 联盟编队
		celebration_fund_gift_buy, //周年庆庆典基金礼包直购
		celebration_fund_score_buy, //周年庆庆典基金积分购买
		gold_baby_find_reward, //金币觅宝搜寻
		newbie_train,          //新兵作训
		plant_soldier_military_upgrade, //泰能军衔提升
		offensive_march,       //攻击性行军
		planet_explore_refresh,      //星能探索锅点刷新
		planet_explore_point_remove, //星能探索锅点（采集完）移除
		planet_explore_draw,         //星能探索抽奖
		planet_explore_collect,      //星能探索锅点采集
		
		grow_up_boost_achieve_score, //中部培养计划  任务完成获得积分
		grow_up_boost_item_score,  //中部培养计划  道具消耗获得积分
		grow_up_boost_score_achieve_reward_take, //中部培养计划  道具消耗获得积分 
		grow_up_boost_score_achieve_page_change, //中部培养计划  任务刷新记录
		grow_up_boost_exchange_group, //中部培养计划  兑换道具解锁层数
		grow_up_boost_buy_gift, //中部培养计划  兑换道具解锁层数
		grow_up_boost_item_recover,//中部培养计划  道具回收
		
		inherit_new_start, //新服触发军魂传承活动
		month_card_exchange, //特权兑换商店兑换记录
		month_card_custom, //定制特权卡选择物品
		buff_change,       //作用号变化记录
		
		manhattan_stage_up, //超武品阶提升
		manhattan_level_up, //超武部件升级
		manhattan_create,   //超武解锁
		manhattan_deploy,   //超武部署

		lottery_ticket_use,  //刮刮乐活动-刮奖
		lottery_ticket_assist_send, //刮刮乐活动-邀请代刮
		lottery_ticket_assist_back, //刮刮乐活动-邀请退回 
		lottery_ticket_item_recover, //刮刮乐活动-道具回收 
		
		back_immgration_start, //回流移民触发
		back_soldier_exchange_start, //回流转兵种触发

		
		plant_weapon_shop_buy, //泰能超武活动商店购买
		
		shooting_practice_game, //打靶活动游戏完成
		xhjz_fuel_distribute, // 星海分配燃油
		
		star_invest_recharge_reward, //星海投资-投资充值奖励领取
		star_invest_free_reward, //星海投资-投资免费奖励领取 
		star_invest_explore_start, //星海投资-探索开始
		star_invest_explore_speed, // 星海投资-探索加速
		star_invest_explore_reward, //星海投资-探索领奖
		
		merge_invite_oper,  //合服邀请操作
		merge_invite_vote,  //合服邀请投票
		merge_vote_result,  //合服邀请投票结果
		best_prize_draw,     //新春头奖专柜活动抽奖
		best_prize_pool_add, //新春头奖专柜活动动态添加奖池
		
		fgyl_sign_up,    //反攻 幽灵-报名
		fgyl_create_room, //反攻 幽灵-创建房间
		fgyl_join_room, //反攻 幽灵-进入房间
		fgyl_exit_room, //反攻 幽灵-退出房间
		fgyl_over_room, //反攻 幽灵-结束
		fgyl_fight_reward, //反攻 幽灵-战斗奖励
		fgyl_rank_reward, //反攻 幽灵-排行榜奖励
		
		quest_treasure_game_refersh, //秘境寻宝游戏刷新
		quest_treasure_game_point_choose, //秘境寻宝游戏路径选择
		quest_treasure_game_random_walk,//秘境寻宝游戏随机前进
		quest_treasure_game_random_item_buy, //秘境寻宝随机道具购买
		quest_treasure_shop_buy, //秘境寻宝商店购买
		
		daily_rank_log, //每日0点上报排行榜信息
		
	    submarine_war_game_start, // 潜艇大战游戏开始
	    submarine_war_game_pass,  // 潜艇大战游戏过关
	    submarine_war_game_over,// 潜艇大战游戏结束
	    submarine_war_shop_buy,// 潜艇大战道具购买
	    submarine_war_game_count_buy,// 潜艇大战游戏次数购买
	    submarine_war_rank_reward,// 潜艇大战游排行榜奖励
	    submarine_war_order_exp_add,//潜艇大战战令经验增加
	    submarine_war_order_reward,//潜艇大战奖励领取
	    
	    honor_mobilize_choose, //荣耀动员选定英雄
	    honor_mobilize_lottery, //荣耀动员抽奖
	    
	    mecha_core_tech_flow, //机甲核心科技等级、品阶提升
	    mecha_core_slot_flow, //机甲核心槽位解锁、升级
	    mecha_core_module_flow, //机甲核心模块添加、分解
	    mecha_core_module_oper, //模块装载、卸载
	    mecha_core_module_attr_replace, //模块属性传承
	    mecha_core_suit_flow,  //套装解锁、切换
	    
	    merge_compete_target_reward, //合服比拼活动目标奖励领取
	    merge_compete_gift_reward,   //合服比拼活动嘉奖奖励领取
	    merge_compete_rank_calc,     //合服比拼活动三大比拼项目排行结算
	    merge_compete_gift_score,    //合服比拼活动嘉奖排名结算
	    merge_compete_server_score,  //合服比拼活动区服积分结算
	    
	    core_explore_mining,   //核心勘探活动挖矿记录
	    core_explore_tech,     //核心勘探活动科技研究
	    core_explore_new_mine, //核心勘探活动刷出新的带奖励矿点
	    
	    guild_team_mamage_member, //先驱回响-管理小队成员
	    xqhx_join_battle,  //先驱回响-加入战斗
	    xqhx_exit_battle,   //先驱回响-退出战斗
	    xqhx_sign_up, //先驱回响-报名
	    xqhx_battle_over,//先驱回响-战斗结束
	    xqhx_send_award,//先驱回响-发奖
	    
	    after_comp_send_gift,     //赛后庆典送礼
	    after_comp_dist_bigaward, //赛后庆典分发礼包大奖
	    after_comp_rec_bigaward,  //赛后庆典领取礼包大奖
	    season_puzzle_send,       //赛季拼图赠送拼图碎片
	    season_puzzle_set,        //赛季拼图放入拼图碎片
	    
	    mass_battle_flow,		  //集结战斗流水日志
	    
	    guild_dragon_attack_appoint, // 巨龙来袭-预约
	    guild_dragon_attack_open,  //巨龙来袭-开启
	    guild_dragon_attack_end,  //巨龙来袭-结束 
	    guild_dragon_attack_fight, //巨龙来袭-攻击
	    guild_dragon_attack_reward, // 巨龙来袭-发奖
	    
	    auto_mass_jion_open,  //自动集结-开启
	    auto_mass_jion_start, // 自动集结-行军出发
	    auto_mass_jion_suc,  // 自动集结-行军成功加入
	    hot_blood_war_army_flow, //热血畅战-士兵处理
	    		
	    schedule_system, //系统待办事项
	    schedule_guild,  //联盟定制待办事项
	    material_transport,//押镖玩法

	    
	    
	    cross_season_score,         //航海赛季-积分计算
	    cross_season_battle_reward, //航海赛季-每期航海奖励
	    cross_season_final_rank,    //航海赛季-最终排名
	    cross_season_final_reward,  //航海赛季-最终发奖
	    		

		home_land_active_attr, //家园系统激活繁荣度
		home_land_upgrade, //家园系统升级建筑
		home_land_like, //家园系统点赞
		
		secondary_build_queue_free_use, //第二建筑队列免费试用
	}
	
	/////////////////////////////////////////////////////////////////////////
	//  TLOG参数
	/////////////////////////////////////////////////////////////////////////
	/**
	 * 分区分服的区ID基数
	 */
	public static class ZoneAreaBase {
		public static final int IOS_QQ      = 10000;
		public static final int ANDROID_QQ  = 20000;
		public static final int IOS_WX      = 30000;
		public static final int ANDROID_WX  = 40000;
		public static final int GUEST       = 50000;
	}

	public static class AddOrReduce {
		public static final int ADD    = 0;  // 加
		public static final int REDUCE = 1;  // 减
	}
	
	public static class IMoneyType {
		public static final int MT_GOLD    = 0;  // 水晶
		public static final int MT_DIAMOND = 1;  // 钻石
		public static final int MT_MONEY   = 2;  // 游戏币
	}
	
	/**
	 * 社交类型
	 */
	public static class SnsType {
		public static final int SHOWOFF      = 0;  // 炫耀
		public static final int INVITE       = 1;  // 邀请
		public static final int SENDHEART    = 2;  // 送心
		public static final int RECEIVEHEART = 3;  // 收取心
		public static final int SENDMAIL     = 4;  // 发邮件
		public static final int RECEIVEMAIL  = 5;  // 收邮件
		public static final int SHARE        = 6;  // 分享
		public static final int OTHER        = 7;  // 其他原因
		public static final int WORLD_CHAT            = 8;  // 世界聊天
		public static final int GUILD_CHAT            = 9;  // 联盟聊天
		public static final int ROOM_CHAT             = 10;  // 聊天室聊天
		public static final int WORLD_BROADCAST       = 11;  // 世界广播
		public static final int GUILD_ALL_MEMBER_MSG  = 12;  // 联盟全体消息
		public static final int GUILD_ALL_MEMBER_MAIL = 13;  // 联盟全体邮件
		public static final int CHANGE_CHARROOM_NAME  = 14;  // 更改聊天室名字
	}
	
	/**
	 * 信息类型
	 */
	public static class LogMsgType {
		public static final int OTHER                        = 0;  // 其它
		public static final int MAIL                         = 1;  // 邮件
		public static final int WORLD                        = 2;  // 世界聊天
		public static final int GUILD                        = 3;  // 联盟聊天
		public static final int PERSONAL                     = 4;  // 个人
		public static final int FRIEND                       = 5;  // 好友
		
		public static final int GUILD_ALL_MEMBER_MSG         = 6;  // 联盟全体消息
		public static final int GUILD_ALL_MEMBER_MAIL        = 7;  // 联盟全体邮件
		public static final int GUILD_NOTICE_CHANGE          = 8;  // 联盟修改公告
		public static final int GUILD_ANNOUNCEMENT_CHANGE    = 9;  // 联盟修改宣言
		public static final int GUILD_NAME_CHANGE            = 10;  // 联盟修改名称
		public static final int GUILD_TAG_CHANGE             = 11;  // 联盟修改简称
		public static final int GUILD_LEVEL_NAME_CHANGE      = 12;  // 修改联盟等级称谓
		
		public static final int CHAT_ROOM                    = 13;  // 聊天室信息
		public static final int WORLD_BROADCAST              = 14;  // 世界广播
		public static final int CHANGE_CHARROOM_NAME         = 15;  // 更改聊天室名字
		
		public static final int GUILD_SIGN                   = 16;  // 添加联盟标记
		public static final int SIGNATURE                    = 17;  // 更新签名
		public static final int CHANGE_MANOR_NAME            = 18;  // 联盟领地名称修改
		public static final int CREATE_GUILD_NAME            = 19;  // 创建联盟设置联盟名称
		public static final int CREATE_GUILD_ANNOUNCE        = 20;  // 创建联盟设置宣言内容
		public static final int SEARCH_PLAYER                = 21;  // 搜索玩家
		public static final int SEARCH_GUILD                 = 22;  // 搜索联盟
		public static final int CHANGE_NAME                  = 23;  // 玩家名字修改
		public static final int COUNTRY_NAME_CHANGE          = 24;  // 国王名称修改
		public static final int FRIEND_REMARK_CHANGE         = 25;  // 好友备注修改
		public static final int SEARCH_STRANGER              = 26;  // 搜索陌生人
		public static final int WORLD_FAVORITE_ADD           = 27;  // 添加世界收藏夹内容
		public static final int WORLD_MARCH_PRESET           = 28;  // 行军预设信息
		public static final int WORLD_FAVORITE_UPDATE        = 29;  // 世界收藏夹内容更新
		public static final int SLIENCE_WORD                 = 30;  // 禁言状态下发言（发言内容仅对自己可见）
		public static final int REPORT_CONTENT               = 31;  // 举报内容
		public static final int ANCHOR                       = 32;  // 主播聊天
		public static final int CYBOR_TEAM_NAME              = 33;  // 修改赛博战队名字
		public static final int TIBER_TEAM_NAME              = 34;  // 修改泰伯战队名字
		public static final int SCHEDULE_TITLE               = 35;  // 待办事项标题
	}
	
	/**
	 * 战斗类型 
	 */
	public static class BattleType {
		public static final int BATTLE_PVP          = 1; // PVP战斗
		public static final int BATTLE_PVE          = 2; // PVE战斗
	}
	
	/**
	 * 战斗子类型
	 */
	public static class BattleSubType {
		public static final int OTHER                    = 0; // 其它类型战斗
		public static final int QUARTERED_BATTLE         = 1; // 攻打驻扎点
		public static final int ATTACK_MONSTER           = 2; // 攻打普通野怪
		public static final int ATTACK_PLAYER_CITY       = 3; // 攻打玩家城点
		public static final int RES_FIELD_BATTLE         = 4; // 攻打资源田
		
		public static final int MASS_MONSTER             = 5; // 集结攻打普通野怪
		public static final int MASS_PLAYER_CITY         = 6; // 集结攻打玩家城点
		public static final int ATTACK_NEW_MONSTER       = 7; // 攻打新版野怪
		public static final int STRONG_POINT_BATTLE      = 8; // 攻打据点
		public static final int GUILD_MANOR_BATTLE       = 9; // 攻打联盟堡垒
		public static final int ATTACK_PRESIDENT_FIELD   = 10; // 攻打总统府
		public static final int ATTACK_SUPER_WEAPON      = 11; // 攻打超级武器
	}
	
	/**
	 * 公会操作类型
	 */
	public static class GuildOperType {
		public static final int GUILD_CREATE    = 1;  // 创建
		public static final int GUILD_DISSOLVE  = 2;  // 解散
		public static final int GUILD_LEVELUP   = 3;  // 升级
		public static final int GUILD_JOIN      = 4;  // 加入
		public static final int GUILD_QUIT      = 5;  // 主从退出
		public static final int GUILD_KICKOUT   = 6;  // 被踢出
		public static final int GUILD_DEMISE      = 7;  // 转让盟主
		public static final int GUILD_IMPEACHMENT = 8;  // 取代盟主
		public static final int GUILD_CHANGENAME  = 9;  // 修改联盟名称
	}
	
	/**
	 * 防守方玩家身份
	 */
	public static class DefenderIdentity {
		public static final int CITY          = 1;  // 玩家基地
		public static final int RES_FIELD     = 2;  // 资源点
		public static final int QUARTER_FIELD = 3;  // 驻扎点
		public static final int GUILD_MANOR   = 4;  // 联盟领地
		public static final int CAPITAL       = 5;   // 首都
		public static final int SUPER_WEAPON = 6;  // 超级武器
		public static final int CROSS_FORTRESS = 7;  // 航海要塞
		public static final int WAR_FLAG = 8;  // 战旗
		public static final int XZQ = 9; //小站区
	}
	
	/**
	 * 移动终端运营商类型
	 */
	public static enum TelecomOper implements IntConst {
		NULL(0),
		China_Unicom(1),
		China_Mobile(2),
		China_Telecom(3);
		
		int value;
		
		TelecomOper(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
		
		public String strVal() {
			String name = name();
			return name.replace("_", " ");
		}
		
		public static TelecomOper valueOf(int value) {
			return LogConst.valueOfEnum(TelecomOper.values(), value);
		}
	}
	
	/**
	 * 移动终端网络类型
	 */
	public static enum NetWork implements IntConst {
		_NULL(0),
		_WIFI(1),
		_2G(2),
		_3G(3),
		_4G(4);
		
		int value;
		
		NetWork(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
		
		public String strVal() {
			String name = name();
			return name.substring(1);
		}
		
		public static NetWork valueOf(int value) {
			return LogConst.valueOfEnum(NetWork.values(), value);
		}
	}
	
	/**
	 * 移动终端平台
	 */
	public static enum Platform implements IntConst {
		IOS(0),
		ANDROID(1);
		
		int value;
		
		Platform(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
		
		public String strVal() {
			return name();
		}
		
		public String strLowerCase() {
			return name().toLowerCase();
		}
		
		public static Platform valueOf(int value) {
			return LogConst.valueOfEnum(Platform.values(), value);
		}
	}
	
	/**
	 * 移动终端渠道类型
	 */
	public static enum Channel implements IntConst {
		WX(1),
		QQ(2),
		GUEST(3),
		HAWK(4),
		APPLE(5);
		
		int value;
		
		Channel(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
		
		public String strVal() {
			return name();
		}
		
		public String strLowerCase() {
			return name().toLowerCase();
		}
		
		public static Channel valueOf(int value) {
			return LogConst.valueOfEnum(Channel.values(), value);
		}
	}
	
	private interface IntConst {
		int intVal();
	}

	private static <T extends IntConst> T valueOfEnum(T[] values, int value) {
		Optional<T> op = Arrays.stream(values).filter(o -> o.intVal() == value).findAny();
		if (op.isPresent()) {
			return op.get();
		}
		throw new RuntimeException("incorrect enum value : " + value);
	}
	
	/**
	 * 玩家经验值来源
	 * 
	 * @author admin
	 *
	 */
	public static class PlayerExpResource {
		public static final int BUILDING_UPGRADE       = 1;  // 建筑升级
		public static final int BUILDING_REBUILD       = 2;  // 建筑改建
		public static final int BUILDING_CREATE        = 3;  // 新建建筑
		public static final int BUILDING_REMVOE        = 4;  // 建筑移除
		public static final int MISSION_AWARD          = 5;  // 普通任务奖励
		public static final int CHAPTER_MISSION_AWARD  = 6;  // 剧情任务奖励
		public static final int DAILY_MISSION_AWARD    = 7;  // 每日活跃任务奖励
		public static final int TOOL_USE               = 8;  // 使用经验道具
		public static final int WORLD_MONSTER_AWARD    = 9;  // 世界打怪掉落奖励
		public static final int STORE_HOUSE_AWARD      = 10;  // 联盟宝藏奖励
		public static final int GM_REVISE              = 11;  // GM修改玩家数据
		public static final int WORLD_EXPLORE          = 12;  // 世界探索帝陵奖励
		public static final int ACTIVITY_AWARD         = 13;  // 活动奖励
		public static final int PLOT_BATTLE_AWARD 	   = 14;  // 剧情战役通关奖励
		public static final int BATTLE_MISSION_AWARD   = 15;  // 战斗任务奖励
	}

	
	/**
	 * 联盟行为
	 * @author golden
	 *
	 */
	public static enum GuildAction {
		GUILD_SEARCH(1),						//联盟搜索
		GUILD_APPLAY(2),						//联盟申请
		GUILD_DO_APPLAY(3),					//通过申请
		GUILD_INVIT(4),						//联盟邀请
		GUILD_CREATE(5),						//联盟创建
		GUILD_DISSOLVE(6),					//联盟解散
		GUILD_INVIT_MOVE_CITY(7),			//邀请迁城
		GUILD_SHARE_ATKREPORT(8),			//分享战报
		GUILD_MEMBER_JOIN(9),					//加入联盟
		GUILD_MEMBER_QUIT(10),					//退出联盟
		
		GUILD_MANOR_CREATE(11),				//创建领地
		GUILD_MANOR_COMPLETE(12),				//领地建造完成
		GUILD_MANOR_REMOVE(13),				//移除联盟领地
		GUILD_BUILD_TOWER_COMPLETE(14),		//建造联盟箭塔完成
		GUILD_BUILD_WAREHOUSE_COMPLETE(15),	//建造联盟仓库完成
		GUILD_BUILD_SUPERMINE_COMPLETE(16),	//建造联盟超级矿完成
		GUILD_TOWER_REMOVE(17),				//移除联盟箭塔
		GUILD_WAREHOUSE_REMOVE(18),			//移除联盟仓库
		GUILD_SUPERMINE_REMOVE(19),			//移除联盟超级矿
		
		GUILD_ATK_MANOR(20),					//进攻联盟领地
		GUILD_ATK_BUILDING_MANOR_COM(21),	//打爆建设中联盟领地
		GUILD_ATK_COMPLETE_MANOR_COM(22),	//打爆已完成联盟领地
		
		GUILD_ACTION_23(23), // 放置联盟建筑
		GUILD_ACTION_24(24), // 主动移除联盟建筑
		GUILD_ACTION_25(25), // 修改联盟名称
		
		;
		int value;
		
		GuildAction(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 礼包类型
	 * 
	 * @author lating
	 *
	 */
	public static enum GiftType {
		OVERBALANCED_GIFT(1),  // 超值礼包
		PREFERENTIAL_GIFT(2),  // 每日特惠礼包
		VIP_EXCLUSIVE_GIFT(3), // 贵族专属礼包
		RECHARGE_GIFT(4),      // 充值买钻礼包
		SHOPPING_MALL_ITEM(5), // 游戏商城道具
		VIP_STORE_ITEM(6),     // 贵族商城道具
		TRAVEL_SHOP_ITEM(7),   // 黑市道具
		TRAVEL_SHOP_GIFT(8),   // 黑市黑金礼包
		PUSH_GIFT(9),           // 推送礼包 
		REVENGE_STORE_ITEM(10), // 大R复仇折扣商品 
		TIMELIMIT_STORE_ITEM(11), // 限时商店商品
		;
		
		int value;
		
		GiftType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 活动点击类型
	 * 
	 * @author lating
	 *
	 */
	public static enum ActivityClickType {
		REWARD_CLICK(1),        // 点击领取奖励
		PARTICIPATION_CLICK(2), // 点击参与活动
		;
		
		int value;
		
		ActivityClickType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 任务类型
	 */
	public static enum TaskType {
		GENERAL_MISSION(1),  // 主线任务
		STORY_MISSION(2),    // 剧情任务
		BATTLE_MISSION(3)    // 军衔（战地）任务
		;
		
		int value;
		
		TaskType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 章节类型任务操作类型
	 */
	public static enum ChapterMissionOperType {
		MISSION_REFRESH(0),
		COMPLETE_AWARD_TAKEN(1);
		
		int value;
		
		ChapterMissionOperType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 城墙状态变化类型
	 */
	public static enum CityWallChangeType {
		NEW_BUILDING(1),      // 新建城墙建筑
		CITY_ONFIRE(2),       // 城墙着火
		CITY_ONFIRE_EDN(3),   // 城墙着火状态自然结束
		CITY_MOVE_RECOVER(4), // 迁城恢复
		CITY_DEF_INC(5),      // 建筑升级或作用加成城防值增加
		CITY_DEF_CONSUME(6),  // 着火状态下城防值消耗      
		CITY_OUTFIRE(7),      // 灭火
		CITY_REMOVE(8),       // 世界移除城点
		CITY_WALL_REPAIR(9);  // 修复城墙
		
		int value;
		
		CityWallChangeType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 盟军宝藏抽奖类型
	 */
	public static enum LotteryDrawType {
		LOTTERY_DRAW_TYPE_FREE(1),      // 免费单抽
		LOTTERY_DRAW_TYPE_SINGLE(2),    // 单抽
		LOTTERY_DRAW_TYPE_TEN(3)        // 十连抽
		;
		
		int value;
		
		LotteryDrawType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 联盟宝藏相关操作类型
	 */
	public static enum StoreHouseOperType {
		STOREHOUSE_EXCAVATE(1),    // 挖掘宝藏
		STOREHOUSE_HELP(2),        // 帮助挖掘
		STOREHOUSE_SPEED(3),        // 宝藏加速
		STOREHOUSE_HELP_REWARD(4),  // 领取帮助奖励
		STOREHOUSE_REWARD(5),       // 领取挖掘奖励
		STOREHOUSE_QUERY_HELP(6),   // 请求帮助
		STOREHOUSE_REFRASH(7)       // 手动刷新宝藏
		;
		
		int value;
		
		StoreHouseOperType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 联盟科技操作
	 */
	public static enum GuildTechOperType {
		DONATE(1),              // 联盟科技捐献
		RESET_DONATE_TIMES(2),  // 重置联盟科技捐献次数
		RESEARCH(3)             // 开启联盟科技研究
		;
		
		int value;
		
		GuildTechOperType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 兵种数量变化类型 
	 */
	public static enum ArmyChangeReason {
		MARCH(1),  // 出征
		MARCH_BACK(2),
		MARCH_DIE(3),
		DEFENCE(4),
		
		TRAIN(5),
		TRAIN_FINISH(6),
		TRAIN_COLLECT(7),
		TRAIN_CANCEL(8),
		
		ADVANCE(9),
		ADVANCE_FINISH(10),
		ADVANCE_COLLECT(11),
		ADVANCE_CANCEL(12),
		
		CURE(13),
		CURE_FINISH(14),
		CURE_COLLECT(15),
		CURE_CANCEL(16),
		
		TARALABS_COLLECT(17),
		
		AWARD(18),
		FIRE(19),
		REMOVE_BUILDING(20),
		MARCH_FIX(21),
		GMSET(22),
		RECEIVE_PROTECT_SOLDIER(23),  // 新兵救援领取
		
		PLANT_ADVANCE(24), // 泰能进化开始
		PLANT_ADVANCE_COST(25), // 泰能进化消耗
		PLANT_ADVANCE_COLLECT(26), // 泰能进化收
		NATION_HOSPITAL_COLLECT(27),  // 从国家医院收兵
		PLANT_ADVANCE_CANCEL(28), // 泰能进化取消
		BING_ZHOPNG_ZH(29), // 兵种转换
		
		MARCH_FIX_SELF(30), //玩家主动修复数据
		;
		int value;
		
		ArmyChangeReason(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 士兵数量构成成分 
	 */
	public static enum ArmySection {
		FREE(1),
		MARCH(2),
		TRAIN(3),
		TRAIN_FINISH(4),
		ADVANCE(5),
		ADVANCE_FINISH(6),
		WOUNDED(7),
		CURE(8),
		CURE_FINISH(9),
		TARALABS(10);
		
		int value;
		
		ArmySection(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 战力类型
	 */
	public static enum PowerType {
		OTHER(0),
		ARMY(1),
		BUILDING(2),
		TECHNOLOGY(3),
		HERO(4),
		COMMANDER(5),
		TRAP(6),
		MONTHCARD(7),
		PLANT_SCIENCE(8),
		SUPER_SOLDIER(9),
		ARMOUR(10),
		CROSS_TECH(11),
		PLANT_TECH(12),
		PLANT_SCHOOL(13),
		STAR_EXPLORE(14),
		MANHATTAN_BASE(15),
		MANHATTAN_SW(16),
		MECHA_CORE_TECH(17),
		MECHA_CORE_MODULE(18),
		HOME_LAND_MODULE(19);

		int value;
		
		PowerType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 战力变化原因 
	 */
	public static enum PowerChangeReason {
		OTHER(0),         // 其他
		WARFARE_ATK(1),   // 战斗进攻
		WARFARE_DEF(2),   // 战斗防守
		INIT_SOLDIER(3),  // 初始化给兵
		INIT_PLAYER(4),   // 玩家初始化
		COMMANDER_LVUP(5),  // 指挥官升级
		BUILD_LVUP(6),    // 建筑升级
		BUILD_REMOVE(7),  // 建筑拆除
		CITY_REPAIR(8),   // 修复城防
		TECH_LVUP(9),     // 科技升级
		AWARD_TRAP(10),    // 陷阱发放
		AWARD_SOLDIER(11), // 士兵发放
		TRAIN_SOLDIER(12), // 训练士兵
		CURE_SOLDIER(13),  // 治疗伤兵
		MAKE_TRAP(14),     // 制造陷阱
		CANCEL_TRAIN(15),  // 取消训练
		CANCEL_CURE(16),   // 取消治疗
		CANCEL_TRAP(17),   // 取消陷阱制造
		SOLDIER_DIE(18),   // 士兵死亡
		SOLDIER_FIRST_AID(19), // 士兵急救
		FIRE_SOLDIER(20),  // 解雇士兵
		ARMY_BACK(21),     // 部队回城
		HERO_ATTR_CHANGE(22),   // 英雄属性变化
		BUY_MONTHCARD(23),      // 购买月卡
		MONTHCARD_CHANGE(24),   // 月卡信息变更
		SUPER_SOLDIER_ATTR_CHANGE(25), // 超级兵
		LMJY_INIT(26),// 联盟军演
		RECEIVE_PROTECT_SOLDIER(27), // 新兵救援领取
		TBLY_INIT(28),// 锦标赛军演
		ARMOUR_CHANGE(29),
		SW_INIT(30),
		CYBORG_INIT(31),
		CROSS_TECH_LVUP(32),     // 远征科技升级
		EQUIP_RESRERCH_LEVEL_UP(33), // 装备科技升级
		PLANTTECH_CHANGE(34),   // 泰能属性变化
		PLANT_SCIENCE_LEVEL_UP(35), //泰能科技树升级
		DYZZ_INIT(36),
		NATION_HOSPITAL_COLLECT(37), // 从国家医院收兵
		YQZZ_INIT(38),
		STAR_EXPLORE(39),
		MANHATTAN_BASE(40),  //超武底座
		MANHATTAN_SW(41),    //超级武器
		MECHA_CORE_TECH(42),   //机甲核心科技
		MECHA_CORE_MODULE(43), //机甲核心模块
		XQHX_INIT(44),//先驱回响
		HOME_LAND_MODULE(45),//家园
		;
		
		int value;
		
		PowerChangeReason(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 英雄技能变化类型
	 */
	public static enum HeroSkillOperType {
		LEVELUP(0),  // 技能升级
		MERGE(1),    // 技能合成
		RESOLVE(2);  // 技能分解
		
		int value;
		
		HeroSkillOperType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	public static enum CrossStateType {
		CROSS_START(0),  //跨服
		CROSS_FINISH(1),  //跨服成功
		CROSS_EXIT(2),   //跨服退出
		CROSS_FAIL(3);	  //跨服失败.
		int value;
		
		CrossStateType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	public static enum WarFlagOwnChangeType {
		PLACE(1),   //放置
		OCCUPY(2),  //抢夺
		LOSE(3),    //被抢夺
		DESTROY(4);	//拆除
		int value;
		
		WarFlagOwnChangeType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 点击事件类型
	 */
	public static enum ClickEventType {
		FRIEND_ENTRANCE_CLICK(1),  // 点击密友入口
		FRIEND_INVITE_CLICK(2);    // 点击邀请好友
		int value;
		
		ClickEventType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 新兵救援事件类型
	 */
	public static enum SoldierProtectEventType {
		TRIGGER_PROTECT(1),      // 触发新兵保护
		RECEIVE_UPLIMIT_DAY(2),  // 每日领取达到上限
		RECEIVE_UPLIMIT_TOTAL(3);// 总领取数量达到上限
		
		int value;
		
		SoldierProtectEventType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 机甲击杀类型
	 */
	public static enum GundamKillType {
		ONCE_KILL(1),  // 致命一击
		KILL(2);       // 最终一击
		
		int value;
		
		GundamKillType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
	/**
	 * 传承判断条件类型 
	 */
	public static enum InheritCondType {
		COND_MATCH_SUCC(0), // 条件检测成功
		VALID_ON(1),      // 最近一期正在进行中
		LOGOUT_TIME_LIMIT(2), // 账号离线时长不达标
		COND_MATCH_NONE(3),   // 不存在满足基础条件的角色（或所有的角色都已经被传承过了）
		NEW_SERVER_ROLE_EXIST(4), // 已经有新角色触发传承了
		;
		
		int value;
		
		InheritCondType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return this.value;
		}
	}
	
}
