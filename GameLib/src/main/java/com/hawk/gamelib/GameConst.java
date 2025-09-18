package com.hawk.gamelib;


public class GameConst {
	/**
	 * {@link GsConst.ITEM_TYPE_BASE} 为两个给活动用
	 */
	public static final int ITEM_TYPE_BASE = 10000;
	// 万位改了基础
	public static final int RANDOM_MYRIABIT_BASE = 10000;
	/**
	 * 消息定义
	 */
	public static class MsgId {
		// 创建公会
		public static final int GUILD_CREATE = 1;
		// 联盟领地通用
		public static final int GUILD_MANOR = 2;
		// 改变联盟名称
		public static final int CHANGE_GUILD_NAME = 3;
		// 改变联盟简称
		public static final int CHANGE_GUILD_TAG = 4;
		// 改变联盟旗帜
		public static final int CHANGE_GUILD_FLAG = 5;
		// 改变联盟语言
		public static final int CHANGE_GUILD_LAN = 6;
		// 改变联盟战略类型
		public static final int CHANGE_GUILD_TYPE = 7;
		// 改变联盟申请权限
		public static final int CHANGE_GUILD_AUTH = 8;
		// 发表联盟宣言
		public static final int GUILD_OPEN_ANNOUNCEMENT = 10;
		// 发表联盟通告
		public static final int GUILD_NOTICE = 11;
		// 资源建筑收取资源
		public static final int COLLECT_RESOURCE = 12;
		// 大本等级变化刷新大本等级排行榜
		public static final int CITY_LEVEL_RANK_REFRESH = 13;
		// 建筑升级触发调查文件系统检测
		public static final int BUILDING_LEVELUP_QUESTIONAIRE_CHECK = 14;
		// 指挥官升级刷新排行榜
		public static final int PLAYER_LEVELUP_RANK_REFRESH = 15;
		// 停服时刷新排行缓存数据
		public static final int UPDATE_RANK_SCORE_ON_SYSTEM_CLOSE = 16;
		// 退出联盟
		public static final int QUIT_GUILD = 17;
		// 玩家战力排行榜刷新
		public static final int PLAYER_FIGHT_RANK_REFRESH = 18;
		// 玩家击杀排行榜刷新
		public static final int PLAYER_KILL_RANK_REFRESH = 19;
		// 联盟盟主退出游戏
		public static final int GUILD_LEADER_LOGOUT = 20;
		// 购买道具触发调查文件系统检测
		public static final int ITEM_BUY_QUESTIONAIRE_CHECK = 21;
		// 玩家改名字通知联盟
		public static final int PLAYER_CHANGE_NAME = 22;
		// 禁止参与个人击杀排行榜
		public static final int BAN_PLAYER_KILL_RANK = 23;
		// 禁止参与联盟击杀排行榜
		public static final int BAN_GUILD_KILL_RANK = 24;
		// 解除禁止参与排行榜
		public static final int UNBAN_PLAYER_KILL_RANK = 25;
		// 增加联盟积分
		public static final int ADD_GUILD_SCORE = 26;
		// 玩家升级奖励推送消息
		public static final int PLAYER_LEVELUP_REWARD = 27;
		// 玩家vip升级奖励推送消息
		public static final int VIP_LEVELUP_REWARD = 28;
		// 刷新任务
		public static final int MISSION_REFRESH = 29;
		// 改变联盟称谓
		public static final int GUILD_LEVEL_NAME = 30;
		// 踢出联盟成员
		public static final int GUILD_KICK_MEMBER = 31;
		// 退出联盟
		public static final int GUILD_QUIT_GUILD = 32;
		// 让出联盟盟主
		public static final int GUILD_DEMISE_LEADER = 33;
		// 设置联盟外交官
		public static final int GUILD_GRANT_COLEADER = 34;
		// 取消联盟外交官
		public static final int GUILD_DISMISS_COLEADER = 35;
		// 解散联盟
		public static final int DISMISS_GUILD = 36;
		// 弹劾盟主
		public static final int GUILD_IMPEACHMENT_LEADER = 37;
		// 购买联盟商店道具
		public static final int GUILD_BUY_ITEM = 38;
		// 联盟商店补货
		public static final int GUILD_GUILDSHOP_ADD = 39;
		// 刷新联盟击杀排行
		public static final int GUILD_KILL_RANK_REFRESH = 40;
		// 刷新联盟战力排行
		public static final int GUILD_FIGHT_RANK_REFRESH = 41;
		// 刷新联盟成员战力
		public static final int GUILDMEMBER_POWER_REFRESH = 42;
		// 设置联盟推荐科技
		public static final int GUILD_SCIENCE_SET_RECOMMEND = 43;
		// 联盟科技研究
		public static final int GUILD_SCIENCE_RESEARCH = 44;
		// 获取联盟科技捐献消耗
		public static final int GET_GUILD_SCIENCE_DONATE_CONSUME = 45;
		// 联盟科技捐献奖励发放
		public static final int GUILD_SCIENCE_DONATE_REWARD = 46;
		// 修改联盟权限信息
		public static final int CHANGE_GUILD_AUTH_INFO = 47;
		// 添加联盟标记
		public static final int ADD_GUILD_SIGN = 48;
		// 资源援助行军到达
		public static final int ASSISTANCE_RES_MARCH_REACH = 49;
		// 联盟成员击杀数刷新
		public static final int GUILD_MEMBER_KILL_REFRESH = 50;
		// 移除联盟标记
		public static final int REMOVE_GUILD_SIGN = 51;
		// 发送玩家聊天信息
		public static final int SEND_CHAT = 52;
		// 存款行军到达
		public static final int WARE_HOUSE_STORE_MARCH_REACH = 53;
		// 取款行军到达
		public static final int WARE_HOUSE_TAKE_MARCH_REACH = 54;
		// 联盟仓库退款
		public static final int WAREHOUSE_SEND_BACK = 56;
		// 建筑改建触发调查文件系统检测
		public static final int BUILDING_REBUILD_QUESTIONAIRE_CHECK = 57;
		// 支付取消
		public static final int PAY_CANCLE = 58;
		// 联盟解散移除联盟领地
		public static final int REMOVE_MANOR_ON_GUILD_DISSMISE = 59;
		// 申请加入联盟
		public static final int APPLY_GUILD = 60;
		// 同意加入联盟
		public static final int ACCEPT_APPLY_GUILD = 61;
		// 同意邀请加入联盟
		public static final int ACCEPT_INVITE_APPLY_GUILD = 62;
		// 行军处理
		public static final int WORLD_MARCH = 63;
		// 大本升级触发任务
		public static final int CITY_LEVELUP_MISSION_REFRESH = 64;
		// 玩家升级触发任务
		public static final int PLAYER_LEVELUP_MISSION_REFRESH = 65;
		// 建筑升级触发任务
		public static final int BUILD_LEVELUP_MISSION_REFRESH = 66;
		// 击杀野怪奖励
		public static final int ATTACK_MONSTER_AWARD = 67;
		// 行军返回结算
		public static final int MARCH_RETURN = 68;
		// 行军删除前的结算
		public static final int MARCH_BEFORE_REMOVE = 69;
		// 返还体力
		public static final int RETURN_VIT = 70;
		// 世界使用道具生成野怪
		public static final int GEN_MONSTER = 71;
		// 迁城消耗道具
		public static final int MOVE_CITY = 72;
		// 仓库行军返回
		public static final int WARE_HOUSE_STORE_MARCH_BACK = 73;
		// 仓库取款行军返回
		public static final int WARE_HOUSE_TAKE_MARCH_BACK = 74;
		// 尤里复仇活动状态发生变化
		public static final int YUNI_REVENGE_STATE_CHANGE = 75;
		// 尤里复仇进攻结束
		public static final int YUNI_REVENGE_FINISH = 77;
		// 尤里复仇积分增长
		public static final int YUNI_REVENGE_ADD_SCORE = 78;
		// 尤里复仇开启进攻
		public static final int YUNI_REVENGE_OPEN_ATTACK = 79;
		/**请求添加好友*/
		public static final int FRIEND_ADD_REQ = 80;
		/**删除好友*/
		public static final int FRIEND_DELETE_REQ = 81;
		/**黑名单操作*/
		public static final int BLACKLIST_OPERATION_REQ = 82;
		/**处理好友请求*/
		public static final int FRIEND_APPLY_REQ = 83;
		/**上线加载玩家数据*/
		public static final int FRIEND_LOAD_DATA = 84;
		/**添加亲密度*/
		public static final int FRIEND_ADD_LOVE = 85;
		// 基地着火
		public static final int CITY_ON_FIRE = 86;
		// 城防恢复
		public static final int CITY_DEF_RECOVER = 87;
		// 城点移除
		public static final int CITY_REMOVE = 88;
		// idip接口修改玩家数据
		public static final int IDIP_CHANGE_PLAYER_INFO = 89; 
		// 封禁个人排行榜
		public static final int PERSONAL_RANK_BAN = 90;
		// 个人排行榜封禁解除
		public static final int PERSONAL_RANK_UNBAN = 91;
		// 保护罩破罩
		public static final int REMOVE_CITY_SHIELD = 92;
		// 世界奖励发放
		public static final int WORLD_AWARD_PUSH = 94;
		// 封禁所有的排行榜信息
		public static final int ALL_RANK_BAN = 95;
		// 禁止参与个人城堡等级排行榜
		public static final int BAN_PLAYER_CASTLE_RANK = 96;
		// 禁止参与个人战力排行榜
		public static final int BAN_PLAYER_FIGHT_RANK = 97;
		// 禁止参与领主等级排行榜
		public static final int BAN_PLAYER_GRADE_RANK = 98;
		// 禁止参与联盟战力排行榜
		public static final int BAN_GUILD_FIGHT_RANK = 99;
		// 屏蔽玩家联盟留言
		public static final int FORBID_POST_GUILD_MSG = 100;
		
		// 联盟成员领取奖励. 更新大礼包
		public static final int PLAYER_SMAIL_GIFT_GET_REWARD = 101;
		// 队列被帮助
		public static final int QUEUE_BE_HELPED = 102;
		// 机器人模式下大本升级时添加vip经验
		public static final int ADD_VIP_EXP = 103;
		
		// 一键加入联盟
		public static final int QUICK_JOIN_GUILD = 104;
		/**玩家迁城*/
		public static final int WORLD_MOVE_CITY = 105;
				
		// 申请联盟官员
		public static final int APPLY_GUILD_OFFICER= 106;
		// 任命联盟官员
		public static final int APPOINT_GUILD_OFFICER= 107;
		// 解除联盟官员
		public static final int DISMISS_GUILD_OFFICER= 108;
		// 联盟排行榜
		public static final int GUILD_GET_RANK= 109;
		// 更新账号角色信息
		public static final int UPDATE_QQVIP_LEVEL = 110;
		
		// 改变联盟语音聊天室模式
		public static final int CHANGE_VOICE_ROOM_MODEL = 111;
		
		// 创建战争学院
		public static final int COLLEGE_CREATE = 112;
		
		// 解散战争学院
		public static final int COLLEGE_DISMISS = 113;
		
		// 申请加入学院
		public static final int COLLEGE_APPLY = 114;
		
		// 退出学院
		public static final int COLLEGE_QUIT = 115;
		
		// 踢出学院
		public static final int COLLEGE_KICK = 116;
		
		// 同意学院成员申请
		public static final int COLLEGE_AGREE_APPLY = 117;
		
		// 拒绝学院成员申请
		public static final int COLLEGE_REFUSE_APPLY = 118;
		
		// 加入学院
		public static final int COLLEGE_JOIN = 119;
		
		// 退出学院-消息
		public static final int COLLEGE_QUIT_MSG = 120;
		
		/**
		 * 创建队伍
		 */
		public static final int WAR_COLLEGE_TEAM_CREATE = 121;
		/**
		 * 加入队伍
		 */
		public static final int WAR_COLLEGE_TEAM_JOIN = 122;
		/**
		 * 请求队伍
		 */
		public static final int WAR_COLLEGE_TEAM_REQ = 123;
		/**
		 * 退出队伍
		 */
		public static final int WAR_COLLEGE_TEAM_QUIT = 124;
		/**
		 * 解散队伍
		 */
		public static final int WAR_COLLEGE_TEAM_DISSOLVE = 125;
		/**
		 * 踢人
		 */
		public static final int WAR_COLLEGE_TEAM_KICK	= 126;
		/**
		 * 进入副本
		 */
		public static final int WAR_COLLEGE_ENTER_INSTANCE = 127;
		/**
		 * 快速加入队伍
		 */
		public static final int WAR_COLLEGE_QUICK_JOIN_TEAM = 128;
		
		// 世界使用道具生成资源宝库
		public static final int GEN_RES_TREASURE = 129;
		// 机甲修复任务
		public static final int SUPER_SOLDIER_TASK = 130;
		
		public static final int NATION_BUILD_QUEST_RETURN = 131;
		// 国家建筑资助发奖
		public static final int NATION_BUILD_SUPPORT_AWARD = 132;
		// 国家仓库捐献发奖
		public static final int NATION_WAREHOUSE_DONATE_AWARD = 133;
		// 世界行军加速
		public static final int WORLD_MARCH_SPEED = 134;
		// 添加至尊vip激活点数
		public static final int SUPER_VIP_ACTIVE_POINT = 135;
		// 账号重置
		public static final int ACCOUNT_RESET = 146;
		// 触发任务
		public static final int MISSION_TRIGGERED = 1001;
		// 战后进攻方玩家消息处理
		public static final int ATK_PLAYER_REFRESH = 1002;
		
		public static final int DEF_PLAYER_REFRESH = 1003;
		
		public static final int BUILDING_QUEUE_CANCEL = 1004;
		
		public static final int BUILDING_QUEUE_FINISH = 1005;
		
		public static final int TECH_QUEUE_CANCEL = 1006;
		
		public static final int TECH_QUEUE_FINISH = 1007;
		
		public static final int CURE_QUEUE_CANCEL = 1008;
		
		public static final int CURE_QUEUE_FINISH = 1009;
		
		public static final int TRAIN_QUEUE_CANCEL = 1010;
		
		public static final int TRAIN_QUEUE_FINISH = 1011;
		
		public static final int CALC_DEAD_ARMY = 1012;
		
		public static final int HERO_BUILDING_LEVELUP = 1013;
		
		public static final int HERO_ITEMCHANGED = 1014;
		
		public static final int PLAYER_ASSEMBLE = 1015;
		
		public static final int PLAYER_LOGIN = 1016;
		
		public static final int SESSION_CLOSE = 1017;
		
		public static final int REMOVE_OBJECT = 1018;
		
		public static final int RECHARGE_ITEM_GRANT = 1019;
		
		public static final int TAX_PLAYER_UPDATE = 1020;
		
		public static final int TRAVEL_SHOP_BUILDING_FINISH = 1021;
		
		public static final int TRAVEL_SHOP_REDUCE = 1022;
		/**集合行軍殺怪*/
		public static final int KILL_MASS_MONSTER = 1023;
		/**讨伐尤里排行榜发奖*/
		public static final int MONSTER4_RANK_REWARD = 1024;
		/**尤里复仇阶段变化*/
		public static final int YURIREVENGE_STATE_CHANGE = 1025;
		/**每日数据重置消息*/
		public static final int DAILY_DATA_CLEAR_MSG = 1026;
		/** 问卷调查提交*/
		public static final int QUESTIONNAIRE_SUBMIT = 1027;
		/** 装备材料挖掘队列完成*/
		public static final int MATERIAL_PRODUCT_FINISH = 1028;
		/** 据点占领奖励*/
		public static final int STRONGPOINT_OCCUPY = 1029;
		/**
		 * 迁出玩家
		 */
		public static final int MIGRATE_OUT = 1030;
		/**
		 * 迁入玩家
		 */
		public static final int IMMIGRATE = 1031;
		/**
		 * 聊天室聊天信息过滤
		 */
		public static final int CHATROOM_MSG_FILTER = 1032;
		/**
		 * 玩家聊天信息过滤（世界聊天，联盟聊天,世界广播）
		 */
		public static final int CHAT_MSG_FILTER = 1033;
		/**
		 * 创建聊天室消息过滤
		 */
		public static final int CREATE_CHATROOM_MSG_FILTER = 1034;
		/**
		 * 禁言中自发消息过滤
		 */
		public static final int SELF_CHAT_MSG_FILTER = 1035;
		/**
		 * 举报内容信息过滤
		 */
		public static final int REPORTING_INFO_FILTER = 1036;
		
		/**
		 * 主播登录
		 */
		public static final int ANCHOR_LOGIN = 1037;
		/**
		 * 聊天室名称过滤
		 */
		public static final int CHATROOM_NAME_FILTER = 1038;
		/**
		 * 超时空急救站冷却恢复结束
		 */
		public static final int BUILDING_RECOVER_FINISH = 1039;
		
		/**
		 * 活动阶段变更通知
		 */
		public static final int ACTIVITY_STATE_CHANGE = 1040;
		
		/***
		 * 红包祝福语过滤
		 */
		public static final int RED_PACKET_MSG_FILTER = 1041;
		
		/**
		 * 限时商店触发 
		 */
		public static final int TIMELIMIT_STORE_TRIGGER = 1042;

		/**
		 * 方尖碑任务触发
		 */
		public static final int OBELISK_MISSION_REFRESH = 1043;
		/**
		 * 安全sdk检测回调
		 */
		public static final int TSSSDK_UIC_INVOKE  = 1044;
		public static final int CALC_SW_DEAD_ARMY = 1050;

		public static final int ACTIVITY_CALL_SERVER = 2001;
		
		public static final int PLAYER_ACROSS_DAY_LOGIN = 2002;
		
		public static final int ASYNC_ACTIVITY_EVENT = 2004;
		
		public static final int PLAYER_PUSH = 2005;
		
		public static final int PLAYER_REWARD_FROM_ACTIVITY = 2006;
		
		public static final int PLAYER_REWARD_BY_ID_FROM_ACTIVITY = 2007;
		
		public static final int SEND_MAIL = 2008;
		
		public static final int ARMY_BACK = 2009;
		
		public static final int ACTIVITY_REWARD_CLICK = 2010;
		
		public static final int ACTIVITY_PLAYER_MIGRATE = 2011;
		/** GM关闭指定活动*/
		public static final int ACTIVITY_GM_CLOSE = 2012;
		/** GM开启指定活动*/
		public static final int ACTIVITY_GM_OPEN = 2013;
		/** 触发活动双倍充值*/
		public static final int ACTIVITY_DOUBLE_RECHARGE = 2014;
		/** 开启联盟补偿队列*/
		public static final int GUILD_HOSPICE = 2015;
		/** 联盟任务*/
		public static final int GUILD_TASK_MSG = 2016;
		/** 联盟任务领奖*/
		public static final int GUILD_TASK_REWARD = 2017;
		/** 跨服活动任务*/
		public static final int CROSS_TASK_MSG = 2100;
		/**守护*/
		public static final int GUARD_INVITE_PLAYER = 2111;
		/**赠送礼物*/
		public static final int GUARD_SEND_GIFT	= 2112;
		/***删除守护信息*/
		public static final int GUARD_DELETE = 2113;
		/**处理守护请求*/
		public static final int GUARD_HANDLE = 2114;
		/**加入排行榜*/
		public static final int GUARD_ADD_RANK = 2115;
		/**从排行榜删除*/
		public static final int GUARD_DELETE_RANK = 2116;
		/**修改基地特效*/
		public static final int GUARD_DRESS_UPDATE = 2117;
		
		/** 小站区门票获取*/
		public static final int XZQ_TICKET_ADD = 2118;
		public static final int XZQ_TICKET_COST = 2119;
		public static final int XZQ_TICKET_BACK = 2120;
		public static final int XZQ_TICKET_CLEAR = 2121;
		public static final int XZQ_SIGN_UP = 2122;
		public static final int XZQ_CANCEL_SIGN_UP = 2123;
		public static final int XZQ_FORCE_COLOR_SET = 2124;
		public static final int XZQ_BUILD_UPDATE = 2125;
		public static final int XZQ_BUILD_BROAD_CAST = 2126;
		public static final int XZQ_GICE_UP = 2127;
		
		
		
		public static final int DYZZ_CREATE_TEAM = 2128;
		public static final int DYZZ_TEAM_INVITE = 2129;
		public static final int DYZZ_JOIN_TEAM = 2130;
		public static final int DYZZ_KICK_OUT = 2131;
		public static final int DYZZ_MATCH_GAME = 2132;
		public static final int DYZZ_CANCEL_MATCH = 2133;
		
		
		public static final int DYZZ_EXIT_INSTANCE = 2134; //退出泰伯利亚副本.
		public static final int DYZZ_BACK_SERVER = 2135; //从跨服回到本服.
		public static final int DYZZ_PREPARE_EXIT_CROSS_INSTANCE = 2136; //退出泰伯利亚跨服副本.
		public static final int DYZZ_MOVE_BACK = 2137; //泰伯利亚签回玩家.
		public static final int DYZZ_PREPARE_MOVE_BACK = 2138;  //预签回
		
		public static final int DYZZ_EXIT_TEAM = 2139;

		public static final int DYZZ_SEASON_STATE_CHANGE = 2140;
		public static final int DYZZ_SEASON_SEND_SCORE_REWARD = 2141;

		public static final int PLAYER_ACHIEVE_UPDATE = 2142;
		
		
		public static final int YQZZ_EXIT_INSTANCE = 2143; //退出泰伯利亚副本.
		public static final int YQZZ_BACK_SERVER = 2144; //从跨服回到本服.
		public static final int YQZZ_PREPARE_EXIT_CROSS_INSTANCE = 2145; //退出泰伯利亚跨服副本.
		public static final int YQZZ_MOVE_BACK = 2146; //泰伯利亚签回玩家.
		public static final int YQZZ_PREPARE_MOVE_BACK = 2147;  //预签回
		
		
		
		// 改变联盟名称
		public static final int CHANGE_COLLEGE_NAME = 2200;
		// 申请加入学院
		public static final int COLLEGE_FAST_JOIN = 2201;
		
		public static final int COLLEGE_COACH_SET = 2202;
		
		public static final int COLLEGE_COACH_AUTO_SET = 2203;
		
		public static final int COLLEGE_AUTH_CHANGE = 2204;
		
		public static final int COLLEGE_JOIN_FREE_SET = 2205;
		
		public static final int COLLEGE_CONTRIBUTE_ADD = 2206;
		public static final int COLLEGE_VIT_SEND = 2207;
		
		public static final int FGYL_WAR_SIGN_UP = 2208;

		/******************活动callBack消息msgId 3000-4000********************/
		/** 同步活动数据*/
		public static final int SYNC_ACTIVITY_DATA_INFO = 3000;
		/** 同步活动状态数据*/
		public static final int SYNC_ACTIVITY_STATE_INFO = 3001;
		/** 根据玩家注册时间开启类活动,活动阶段检测*/   
		public static final int REGISTER_ACTIVITY_UPDATE_STATE = 3002;
		/** 月卡活动进入展示期时推送月卡信息*/
		public static final int SYNC_MONTHCARD_ACTIVITY_INFO = 3003;
		/** 地狱火1(火线征召)活动生效*/
		public static final int HELL_FIRE_ACTIVITY_EFFECT_START = 3004;
		/** 地狱火2(全军动员)活动生效*/
		public static final int HELL_FIRE_TWO_ACTIVITY_EFFECT_START = 3005;
		/** 地狱火3活动生效*/
		public static final int HELL_FIRE_THREE_ACTIVITY_EFFECT_START = 3006;
		/**活动开始的时候把互动重启*/
		public static final int BROKEN_ACTIVITY_RESET_DATA = 3007;
		/**限时掉落初始化成就*/
		public static final int TIME_LIMIT_DROP_INIT_ACHIEVE = 3008;
		/**盟军反击初始化成就*/
		public static final int ALLY_BEAT_BACK_INIT_ACHIEVE 	= 3009;
		
		/** 初始化据点活动成就数据*/
		public static final int ACHIEVE_INIT_STRONGPOINT = 3101;
		/** 初始化累积消耗活动成就数据*/
		public static final int ACHIEVE_INIT_ACCUMULATE_CONSUME = 3102;
		/** 初始化累积充值活动成就数据*/
		public static final int ACHIEVE_INIT_ACCUMULATE_RECHARGE = 3103;
		/** 初始化基金团购活动成就数据*/
		public static final int ACHIEVE_INIT_GROUP_PURCHASE = 3104;
		/** 初始化建筑升级活动成就数据*/
		public static final int ACHIEVE_INIT_BUILD_LEVEL = 3105;
		/** 初始化装备成就活动成就数据*/
		public static final int ACHIEVE_INIT_EQUIP_ACHIEVE = 3106;
		/** 初始化十连抽活动成就数据*/
		public static final int ACHIEVE_INIT_LOTTERY_DRAW = 3107;
		/** 初始化充值豪礼活动成就数据*/
		public static final int ACHIEVE_INIT_RECHARGE_GIFT = 3108;
		/** 初始化英雄成就活动成就数据*/
		public static final int ACHIEVE_INIT_HERO_ACHIEVE = 3109;
		/** 初始化战力提升活动成就数据*/
		public static final int ACHIEVE_INIT_POWER_UP = 3110;
		/** 初始化战地福利活动成就数据*/
		public static final int ACHIEVE_INIT_WARZONE_WEAL = 3111;
		/** 初始化战地福利活动成就数据*/
		public static final int ACHIEVE_INIT_BLOOD_CORPS = 3112;
		/** 初始化剿灭叛军活动成就数据*/
		public static final int ACHIEVE_INIT_YURI_ACHIEVE = 3113;
		/** 初始化剿灭叛军活动成就数据*/
		public static final int ACHIEVE_INIT_YURI_ACHIEVE_TWO = 3114;
		/** 初始化累计登陆2活动成就数据*/
		public static final int ACHIEVE_INIT_LOGIN_DAY_TWO = 3115;
		/** 礼包返利活动成就数据*/
		public static final int ACHIEVE_INIT_PRESENT_REBATE = 3116;
		/** 初始化英雄主题(最强步兵)活动成就数据*/
		public static final int ACHIEVE_INIT_HERO_THEME = 3117;
		/** 初始化超级金矿活动数据 **/
		public static final int ON_SUPER_GOLD_ACTIVITY_OPEN = 3120;
		/**潘多拉活动开启*/
		public static final int PANDORA_OPEN = 3131;
		/** 幸运星活动开启 **/
		public static final int ON_LUCKY_STAR_ACTIVITY_OPEN = 3132;
		/** 穹顶兑换活动开启 **/
		public static final int ON_DOME_EXCHANGE_ACTIVITY_OPEN = 3133;
		/** 悬赏令活动系统刷新 **/
		public static final int REWARD_ORDER_SYSTEM_REFRESH = 3134;
		/** 初始化机甲觉醒活动成就数据*/
		public static final int ACHIEVE_INIT_MACHINE_AWAKE = 3135;
		/** 一元购数据初始化 */
		public static final int ONE_RMB_PURCHASE_INIT = 3136;
		/** 初始化补给站数据 **/
		public static final int SUPPLY_STATION_INIT = 3137;
		/** 初始化英雄返场 最强步兵活动 */
		public static final int HERO_BACK_INIT = 3138;
		/** 初始化英雄返场 英雄回归活动 */
		public static final int HERO_BACK_EXCHANGE_INIT = 3139;
		/** 英雄返场 英雄活动活动开放 */
		public static final int ON_HERO_BACK_EXCHANGE_ACTIVITY_OPEN = 3140;
		/** 豪礼派送【战地福利拷贝活动】 **/
		public static final int ACHIEVE_INIT_GIFT_SEND = 3141;
		/** 能量收集初始化 **/
		public static final int INIT_POWER_COLLECT_ACHIEVE = 3142;		
		/** 红包活动同步信息 **/
		public static final int RED_ENVELOPE_SYNC_INFO = 3143;
		/** 欢购豪礼 **/
		public static final int HAPPY_GIFT_INIT = 3144;
		/**复制盟军补给站，时空恋人.*/
		public static final int SUPPLY_STATION_TWO_INIT = 3145;
		/**送花*/
		public static final int SEND_FLOWER_INIT = 3146;
		/**在线答题 情报分享**/
		public static final int QUESTION_SHARE_INIT = 3147;
		/** 初始化新年寻宝活动成就数据*/
		public static final int ACHIEVE_INIT_NEWYEAR_TREASURE_ACHIEVE = 3147;
		/**好友召回成就初始化*/
		public static final int ACHIEVE_INIT_RECALL_FRIEDN = 3151;
		/** 通用兑换初始化 **/
		public static final int COMMON_EXCHANGE_INIT = 3152;
		/** 定制礼包活动数据初始化 **/
		public static final int CUSTOM_GIFT_ACTIVITY_INIT = 3153;
		/**月签功能初始化**/
		public static final int DAILY_SIGN_ACTIVITY_INIT = 3154;
		/** 源计划活动初始化**/
		public static final int PLAN_ACTIVITY_INIT = 3155;
		/** 推广员活动初始化*/
		public static final int SPREAD_ACTIVITY_INIT = 3156;
		/** 通用兑换2初始化 **/
		public static final int COMMON_EXCHANGE_TWO_INIT = 3157;
		/** 幸运福利活动初始化*/
		public static final int LUCKY_WELFARE_ACTIVITY_INIT = 3158;
		/** 全副武装活动初始化*/
		public static final int FULLY_ARMED_ACTIVITY_INIT = 3159;
		/** 先锋豪礼数据初始化 */
		public static final int PIONEER_GIFT_INIT = 3160;
		/** 初始化武者拼图活动成就数据*/
		public static final int ACHIEVE_INIT_FIGHTER_PUZZLE = 3161;
		/** 时空轮盘 */
		public static final int ROULETTE_INIT = 3162;
		/**皮肤计划活动开启*/
		public static final int SKIN_PLAN = 3163;
		/** 初始化累积充值2活动成就数据*/
		public static final int ACHIEVE_INIT_ACCUMULATE_RECHARGE_TWO = 3164;
		/** 今日累计充值活动成就数据初始化  */
		public static final int ACHIEVE_INIT_DAILY_RECHARGE = 3165;
		/** 军事备战活动成就数据初始化  */
		public static final int ACHIEVE_INIT_MILITARY_PREPARE = 3166;
		/** 中秋庆典活动成就数据初始化  */
		public static final int ACHIEVE_INIT_MID_AUTUMN = 3167;
		
		/** 限时登录活动  */
		public static final int LIMIT_TIME_LOGIN_SYNC = 3168;
		/** 勋章宝藏活动成就数据初始化  */
		public static final int ACHIEVE_INIT_MEDAL_TREASURE = 3169;
		
		/** 红警锦鲤活动数据初始化*/
		public static final int ACTIVITY_REDKOI_INIT = 3170;
		
		/** 特惠商人助力庆典数据初始化*/
		public static final int ACTIVITY_TRAVEL_SHOP_ASSIST_INIT = 3171;
		
		/** 特惠商人助力庆典结束*/
		public static final int ACTIVITY_TRAVEL_SHOP_ASSIST_CLOSE = 3172;
		
		/** 飞行计划活动成就数据*/
		public static final int ACHIEVE_INIT_FLIGHT_PLAN = 3173;
		
		/** 瓜分金币活动刷新Acheive*/
		public static final int ACTIVITY_DIVIDE_GOLD_REFRESH_ACHIEVE = 3174;
		public static final int ACTIVITY_DIVIDE_GOLD_SYNC = 3010;
		
		/** 场景分享活动初始化*/
		public static final int ACTIVITY_SCENE_SHARE_INIT = 3175;

		/** 英雄进化之路活动初始化 */
		public static final int ACTIVITY_EVOLUTION_INIT = 3176;
		
		/** 装备科技活动成就数据*/
		public static final int ACHIEVE_INIT_EQUIP_TECH = 3177;
		
		/** 今日累计充值(改版)活动成就数据初始化  */
		public static final int ACHIEVE_INIT_DAILY_RECHARGE_NEW = 3178;
		/**黑武士成就初始化*/
		public static final int ACHIEVE_INIT_SAMURAI_BLACKENED = 3179;
		/** 指挥官学院初始化*/
		public static final int COMMAND_ACADEMY_INIT = 3180;
		/** 基地飞升建筑升级*/
		public static final int BASE_BUILD_UP = 3181;
		/** 指挥官学院能源任务推动*/
		public static final int COMMAND_ACADEMY_ACHIVE_PROCESS = 3182;
		/** 装备黑市数据初始化*/
		public static final int EQUIP_BLACK_MARKET_INIT = 3183;
		/**
		 * 圣诞大战任务完成.
		 */
		public static final int CHRISTMAS_WAR_TASK_FINISH = 3184;
		/**
		 * 英雄委任成就初始化.
		 */
		public static final int ACHIEVE_INIT_HERO_LOVE = 3185;
		/** 时空豪礼数据初始化*/
		public static final int CHRONO_GIFT_INIT = 3188;
		/** 充值基金活动初始化*/
		public static final int RECHARGE_FUND_INIT = 3186;
		/** 充值基金活动阶段切换*/
		public static final int RECHARGE_FUND_STAGE_CHANGE= 3187;
		
		/** 初始化新版辐射战争活动成就数据*/
		public static final int ACHIEVE_INIT_RADIATION_WAR = 3189;
		/** 初始新版辐射战争2活动成就数据*/
		public static final int ACHIEVE_INIT_RADIATION_WAR_TWO = 3190;
		/** 端午节-道具兑换初始化*/
		public static final int DRAGON_BOAT_EXCHANGE_INIT = 3191;
		/** 端午节-特惠初始化*/
		public static final int DRAGON_BOAT_BEBEFIT_INIT = 3192;
		/** 端午节-特惠初始化*/
		public static final int DRAGON_BOAT_BEBEFIT_CLOSE = 3193;
		/** 端午节-福袋*/
		public static final int DRAGON_BOAT_LUCKY_BAG_INIT = 3194;
		/** 端午节-充值*/
		public static final int DRAGON_BOAT_RECHARGE_INIT = 3195;
		/** 端午节-福船送礼*/
		public static final int DRAGON_BOAT_GIFT_INIT = 3196;
		/** 端午节-联盟庆典*/
		public static final int DRAGON_BOAT_CELEBRATION_INIT = 3197;
		/** 端午节-福船送礼,信息同步*/
		public static final int DRAGON_BOAT_INFO_SYNC = 3198;
		/** 充值福利 */		
		public static final int RECHARGE_WELFARE_INIT = 3199;
		/** 沙场点兵-初始化*/
		public static final int ARMIES_MASS_INIT = 3120;
		/** 沙场点兵-重置*/
		public static final int ARMIES_MASS_RESET = 3121;
		
		/**空投补给 */		
		public static final int AIRDROP_SUPPLY_INIT = 3122;
		
		/** 初始化超级金矿活动2数据 **/
		public static final int ON_SUPER_GOLD_TWO_ACTIVITY_OPEN = 3123;

		/** 新服战令初始化*/
		public static final int NEW_ORDER_INIT = 3124;
		
		/**七夕充值 */		
		public static final int RECHARGE_QIXI_INIT = 3126;
		/**团购活动 初始化*/		
		public static final int GROUP_BUY_INIT = 3128;
		/** 双享豪礼数据初始化 */
		public static final int DOUBLE_GIFT_INIT = 3129;
		/**团购活动结束发奖*/		
		public static final int GROUP_BUY_END_REWARD = 3130;
		/**周年庆蛋糕分享初始化*/
		public static final int CAKE_SHARE_INIT = 3201;

		/** 军械要塞初始化*/
		public static final int ORDNANCE_FORTRESS_INIT = 3202;
		/**周年庆美食庆典初始化*/
		public static final int CELEBRATION_FOOD_INIT = 3203;

		
		/**荣耀返利*/		
		public static final int HONOR_REPAY_INIT = 3204;

		/** 红包活动同步*/
		public static final int RED_PACKAGE_INFO_SYNC = 3205;
		/** 战地寻宝活动成就数据 */
		public static final int ACHIEVE_INIT_BATTLE_FIELD = 3206;

		/**祝福语活动*/
		public static final int GREETINGS_INIT = 3207;

		/** 装备战令活动初始化*/
		public static final int ORDER_EQUIP_INIT = 3208;

		/** 双十一拼图活动成就数据初始化  */
		public static final int ACHIEVE_INIT_JIGSAW_CONNECT = 3209;

		/** 全服签到活动初始话*/
		public static final int GLOBAL_SIGN_INIT = 3210;
		
		
		/** 回流武者拼图活动成就数据*/
		public static final int ACHIEVE_INIT_RETURN_PUZZLE = 3211;

		/** 双十一联盟欢庆活动成就数据初始化  */
		public static final int ACHIEVE_INIT_ALLIANCE_CELEBRATE = 3212;

		/**装扮投放系列活动二:能量聚集*/
		public static final int ACHIEVE_INIT_ENERGY_GATHER = 3213;

		/**装扮投放系列活动三:重燃战火*/
		public static final int ACHIEVE_INIT_FIRE_REIGNITE = 3214;
		/**装扮投放系列活动四:浴火重生*/
		public static final int ACHIEVE_INIT_GUNPOWDER_RISE = 3215;
		/**装扮投放系列活动一:搜寻图纸*/
		public static final int ACHIEVE_INIT_DRAWING_SEARCH = 3216;

		/** 泰能宝库初始化*/
		public static final int PLANT_FORTRESS_INIT = 3217;

		/**圣诞节系列活动一:冰雪计划活动*/
		public static final int ACHIEVE_INIT_ENERGY_GATHER_TWO = 3218;

		/**圣诞节系列活动二:冬日装扮活动*/
		public static final int ACHIEVE_INIT_FIRE_REIGNITE_TWO = 3219;
		/**圣诞节系列活动三:冰雪商城活动*/
		public static final int ACHIEVE_INIT_GUNPOWDER_RISE_TWO = 3220;
		/** 圣诞节系列活动累积重置活动成就数据*/
		public static final int ACHIEVE_INIT_CHRISTMAS_RECHARGE = 3221;
		/** 玩家回流H5活动初始化   */
		public static final int PLAYER_TEAM_BACK_INIT = 3222;
		/** 初始化武者拼图活动成就数据*/
		public static final int ACHIEVE_INIT_FIGHTER_PUZZLE_SERVEROPEN = 3223;
		/** 雄芯壮志活动初始化*/
		public static final int ACHIEVE_INIT_COREPLATE = 3224;

		/** 洪福礼包活动初始化*/
		public static final int ACHIEVE_INIT_HONG_FU_GIFT= 3225;
		 /** 红蓝对决翻牌活动  */
		public static final int RED_BLUE_TICKET = 3226;
		/** 精装夺宝活动初始化*/
		public static final int DRESS_TREASURE_INIT = 3227;
		/** 泰能机密 */
		public static final int PLANT_SECRET_INIT = 3228;

		/** 幸运转盘*/
		public static final int LUCKY_BOX_INIT = 3229;
		/** 欢乐限购297 */
		public static final int RED_RECHARGE_INIT = 3230;
		
		/** 盟军祝福初始化*/
		public static final int ALLIANCE_WISH_INIT = 3231;
		/** 盟军祝福帮助盟友*/
		public static final int ALLIANCE_WISH_HELP_GUILD_MEMBER = 3232;
		/** 七夕相遇*/
		public static final int LOVER_MEET_INIT = 3233;
		/** 盟军祝福状态同步*/
		public static final int ALLIANCE_WISH_SYNC = 3234;
		/** 英雄祈福数据初始化*/
		public static final int HERO_WISH_INIT = 3235;
		/** 感恩福利帮助*/
		public static final int GRATEFUL_BENEFITS_HELP = 3236;
		/** 感恩福利开始*/
		public static final int GRATEFUL_BENEFITS_OPEN = 3237;
		/** 感恩福利签到结束*/
		public static final int GRATEFUL_BENEFITS_END = 3238;
		/** 独家记忆初始化*/
		public static final int EXCLUSIVE_MEMORY_INIT = 3239;
		/** 荣耀英雄降临初始化*/
		public static final int HONOUR_HERO_BEFELL_INIT = 3240;
		/** 荣耀英雄回归初始化*/
		public static final int HONOUR_HERO_RETURN_INIT = 3241;
		/** 陨晶战场成就初始化*/
		public static final int DYZZ_ACHIEVE_INIT = 3242;
		/** 双旦活动初始化  */
		public static final int NEWYEAR_LOTTERTY_INIT  = 3243;

		public static final int ON_ROSE_GIFT_ACTIVITY_OPEN = 3244;

		public static final int ON_ROSE_GIFT_SERVER_NUM = 3245;
		
		
		/** 机甲研究所初始化*/
		public static final int MACHINE_LIB_INIT = 3246;
		public static final int MACHINE_LIB_SYNC = 3247;
		public static final int MACHINE_LIB_CLEAR = 3248;

		public static final int ON_ROSE_GIFT_ACTIVITY_END = 3249;
		
		public static final int SPACE_MACHA_ACTIVITY_OPEN  = 3250;
		public static final int ON_PDD_ACTIVITY_OPEN = 3251;

		public static final int CHANGE_SVR_ACTIVITY_INFO_SYN = 3252;
		// 霸主膜拜数量同步
		public static final int OVERLORD_BLESSING_NUM_SYNC = 3253;

		public static final int ON_LUCK_GET_GOLD_SYNC = 3254;
		
		// 新兵作训活动开启
		public static final int NEWBIE_TRAIN_ACTIVITY_OPEN  = 3255;

		public static final int CNY_EXAM_ACTIVITY_OPEN  = 3256;
		// 星能探索活动
		public static final int INIT_PLANET_EXPLORE_ACHIEVE = 3257;

		public static final int ON_SUPPLY_CRATE_INIT = 3258;

		public static final int ON_JIJIA_SKIN_INIT = 3259;
		
		//泰能超武投放活动
		public static final int INIT_PLANT_WEAPON_INIT = 3260;

		public static final int ON_GUILD_BACK_SYNC = 3261;
		public static final int ON_GUILD_BACK_DROP = 3262;
		
		//泰能超武返场活动
		public static final int PLANT_WEAPON_BACK_INIT = 3263;
		//新春头奖专柜活动
		public static final int BEST_PRIZE_INIT = 3264;
		//合服比拼活动
		public static final int MERGE_COMPETE_INIT = 3265;
		//（机甲）核心勘探
		public static final int CORE_EXPLORE_INIT = 3266;
		//赛后庆典活动371
		public static final int AFTER_COMPETITION_INIT = 3267;
		//赛季拼图活动373
		public static final int SEASON_PUZZLE_INIT = 3268;
		
		/******************活动回调消息msgId 3000-3500********************/
		
		/******************跨服相关4001-4100********************/
		public static final int CROSS_EIXT = 4001;//退出跨服
		public static final int CROSS_LOCAL_LOGIN = 4002; //跨服的时候本地一些数据需要初始化.
		public static final int CORSS_PREPARE_EXIT = 4003; //准备退出跨服，给活动开一个接口.
		public static final int CROSS_FORCE_MOVE_BACK = 4004; //强制迁回.
		public static final int CROSS_PREPARE_FORCE_MOVE_BACK = 4005; //发起强制回迁.
		public static final int CROSS_BACK_SERVER = 4006; //跨服玩家回到原服
		/******************跨服相关3501-4000********************/
		/************************合服相关 4101-4200 ************/
		public static final int MERGE_SERVER_BACK_GUILD_STORAGE = 4101; //返还联盟仓库
		/************************合服相关 4101-4200 ************/
		/************泰伯利亚4201-4220----------*/
		public static final int TIBERIUM_EXIT_INSTANCE = 4201; //退出泰伯利亚副本.
		public static final int TIBERIUM_BACK_SERVER = 4202; //从跨服回到本服.
		public static final int TIBERIUM_PREPARE_EXIT_CROSS_INSTANCE = 4203; //退出泰伯利亚跨服副本.
		public static final int TIBERIUM_MOVE_BACK = 4204; //泰伯利亚签回玩家.
		public static final int TIBERIUM_PREPARE_MOVE_BACK = 4205;  //预签回
		
		public static final int TIBERIUM_EDIT_TEAM_NAME = 4210; // 修改小组名称
		public static final int TIBERIUM_EDIT_TEAM_TARGET = 4211; // 修改小组目标
		public static final int TIBERIUM_EDIT_MEMBER_TARGET = 4212; // 修改小组成员策略
		public static final int TIBERIUM_MEMBER_MANAGE = 4213; // 管理小组成员
		public static final int TIBERIUM_MEMBER_RESET = 4214; // 小组成员重置
		public static final int TIBERIUM_MEMBER_QUIT = 4215; // 小组成员退盟处理
		/************泰伯利亚4201-4220----------*/
		
		public static final int REMOVE_ARMOUR = 4216; // 删除铠甲
		
		/***---------------------星球大战-------------------------------*/
		public static final int STAR_WARS_EXIT_INSTANCE = 4231; //退出星球大战
		public static final int STAR_WARS_BACK_SERVER = 4232; //从跨服回到本服.
		public static final int STAR_WARS_PREPARE_EXIT_CROSS_INSTANCE = 4233; //退出星球大战
		public static final int STAR_WARS_MOVE_BACK = 4234; //泰伯利亚签回玩家.
		public static final int STAR_WARS_PREPARE_MOVE_BACK = 4235;  //预签回
		public static final int STAR_WARS_ENTER_INSTANCE = 4236;  //进入星球大战
		/***---------------------星球大战-------------------------------*/
		
		// 删除道具
		public static final int REMOVE_ITEM = 4236;		
		
		/************赛博之战4250-4280----------*/
		public static final int CYBOGR_CREATE_TEAM		= 4250; // 赛博-创建战队
		public static final int CYBOGR_EDIT_TEAM_NAME	= 4251; // 赛博-修改战队名称
		public static final int CYBOGR_DISMISS_TEAM		= 4252; // 解散战队
		public static final int CYBOGR_MEMBER_EDIT		= 4253; // 修改战队成员
		
		public static final int CYBORG_EXIT_INSTANCE = 4201; //退出赛博副本.
		public static final int CYBORG_BACK_SERVER = 4202; //从跨服回到本服.
		public static final int CYBORG_PREPARE_EXIT_CROSS_INSTANCE = 4203; //退出赛博跨服副本.
		public static final int CYBORG_MOVE_BACK = 4204; //赛博签回玩家.
		public static final int CYBORG_PREPARE_MOVE_BACK = 4205;  //预签回
		/************赛博之战4250-4280----------*/

		/************星海激战4901-4930----------*/
		public static final int XHJZ_EXIT_INSTANCE = 4901; //退出赛博副本.
		public static final int XHJZ_BACK_SERVER = 4902; //从跨服回到本服.
		public static final int XHJZ_PREPARE_EXIT_CROSS_INSTANCE = 4903; //退出赛博跨服副本.
		public static final int XHJZ_MOVE_BACK = 4904; //赛博签回玩家.
		public static final int XHJZ_PREPARE_MOVE_BACK = 4905;  //预签回
		public static final int XHJZ_POWER_REFRESH = 4906; //战力刷新
		public static final int XHJZ_TEAM_MANAGER = 4907; //小队管理
		public static final int XHJZ_MEMBER_MANAGER = 4908; //队员管理
		public static final int XHJZ_NAME_REFRESH = 4909; //名字刷新
		public static final int XHJZ_QUIT_GUILD = 4910; //退出联盟
		public static final int XHJZ_DISMISS_GUILD = 4911; //解散联盟
		/************星海激战4901-4930----------*/

		/************星海激战4901-4930----------*/
		public static final int GUILD_TEAM_POWER_REFRESH = 4921; //战力刷新
		public static final int GUILD_TEAM_TEAM_MANAGER = 4922; //小队管理
		public static final int GUILD_TEAM_MEMBER_MANAGER = 4923; //队员管理
		public static final int GUILD_TEAM_NAME_REFRESH = 4924; //名字刷新
		public static final int GUILD_TEAM_QUIT_GUILD = 4925; //退出联盟
		public static final int GUILD_TEAM_DISMISS_GUILD = 4926; //解散联盟
		/************星海激战4901-4930----------*/

		/************先驱回响4931-4940----------*/
		public static final int XQHX_EXIT_INSTANCE = 4931; //退出赛博副本.
		public static final int XQHX_BACK_SERVER = 4932; //从跨服回到本服.
		public static final int XQHX_PREPARE_EXIT_CROSS_INSTANCE = 4933; //退出赛博跨服副本.
		public static final int XQHX_MOVE_BACK = 4934; //赛博签回玩家.
		public static final int XQHX_PREPARE_MOVE_BACK = 4935;  //预签回
		/************先驱回响4931-4940----------*/
		
		/** 初始化英雄皮肤成就活动成就数据*/
		public static final int ACHIEVE_INIT_HERO_SKIN_ACHIEVE = 4206;
		
		// 账号注销
		public static final int ACCOUNT_CANCELLATION = 4207;

		
		//幽灵工厂生成怪
		public static final int GHOST_TOWER_GEN_MONSTER = 4208;
		//幽灵工厂怪被击杀
		public static final int GHOST_TOWER_MONSTER_KILLED = 4209;

		// 装备科技队列
		public static final int EQUIP_RESEARCH_QUEUE_FINISH = 4210;
		
		// 鹊桥会活动开启
		public static final int WAR_FLAG_EXCHANGE_ACTIVITY_OPEN = 4211;

		/** 登录基金2活动数据*/
		public static final int LOGIN_FUND_DATA_INIT = 4212;
		/************************ 推送礼包触发消息  ************************/
		
		public static final int QUEUE_SPEED       = 4501;  // 造兵、治疗加速
		public static final int ITEM_CONSUME      = 4503;  // 消耗指定道具
		public static final int SUPER_LAB_LEVELUP = 4504;  // 超能实验室部件升级
		public static final int EQUIP_RESEARCH    = 4505;  // 装备研究升级
		public static final int EQUIP_ENHANCE     = 4506;  // 装备强化
		public static final int COMMANDER_EXP_ADD = 4507;  // 指挥官经验增加
		
		public static final int PLANT_ADVANCE_SPEEDUP = 4508; // 加速泰能兵进化
		public static final int AUTO_PUT_MODULE = 4509; //自动拉锅
		public static final int YQZZ_GAMEING_TICK = 4511;
		
		public static final int SEA_TREASURE_RECEIVE = 4512; // 觅海巡诊

		public static final int SEASON_ACTIVITY_SYNC = 4513; // 赛季同步
		public static final int STAR_LIGHT_SIGN_INIT = 4514; // 世界勋章活动初始化
		public static final int STAR_LIGHT_SIGN_END = 4515; // 世界勋章活动结束
		public static final int ON_SEASON_ACTIVITY_END = 4516;
		
		public static final int CELEBRATION_FUND_GIFT = 4517; // 周年庆庆典基金礼包
		public static final int DRESS_COLLECTION      = 4518; // 周年庆称号活动
		public static final int CELEBRATION_FUND_REWARD = 4519; // 周年庆庆典基金礼包

		public static final int GOLD_BABY_INIT = 4520; //金币觅宝初始化
		
		public static final int GOLD_BABY_NEW_INIT = 4521; //金币觅宝初始化
		
		
		public static final int GROW_UP_BOOST_INIT = 4523; //中部培养计划数据初始化
		public static final int GROW_UP_RECOVER_ITEM = 4524; //中部培养计划数据回收道具
		
		
		public static final int DAIY_BUY_GIFT_INIT = 4526; //每日必买数据初始化
		
		public static final int ANNIVERSARY_GIFT_INIT = 4527;// 周年庆-6重好礼数据初始化

		public static final int LOTTERY_TICKET_INIT = 4528; //刮刮乐数据初始化
		public static final int LOTTERY_TICKET_RECOVERITEM = 4529; //刮刮乐道具回收
		
		public static final int SHOOTING_PRACTICE_INIT = 4530; //打靶活动数据初始化
		
		public static final int STAR_INVEST = 4531; //星海投资初始化
		
		public static final int QUEST_TREASURE_INIT = 4532; //秘境寻宝初始化
		public static final int QUEST_TREASURE_BOX_SCORE = 4533;//秘境寻宝宝箱积分
		
		
		public static final int SUBMARINE_WAR_INIT = 4534; //潜艇大战初始化
		
		public static final int HONOUR_MOBILIZE_INIT = 4535; //荣耀动员初始化
	
		public static final int DRESS_COLLECTION_TWO      = 4536; // 周年庆称号活动2
		public static final int DEEP_TREASURE = 4537; // 深海秘藏
		
		public static final int HOT_BLOOD_WAR_INIT = 4538; //热血畅战初始化
		public static final int HOT_BLOOD_WAR_END = 4539; //热血畅战初始化
		public static final int HOT_BLOOD_WAR_TICK = 4540; //热血畅战初始化

		//家园
		public static final int HOME_LAND_ROUND_INIT = 4541; //命运轮盘机

	}
	
	public static final int PANDORA_NUM_ONE = 1;
	public static final int PANDORA_NUM_TEN = 10;
	
	/**
	 * 罩子的buffId
	 */
	public static final int CITY_SHIELD_BUFF_ID = 23001;
	/**
	 * 三分钟
	 */
	public static final int TBLY_PROTECTE_TIME = 180;
	/**
	 * 
	 * @author jm
	 *
	 */
	public static class HELL_FIRE_TYPE {
		public static final int HELL_FIRE = 1; //火线征召
		public static final int HELL_FIRE_TOW = 2; //全军动员
	}
}
