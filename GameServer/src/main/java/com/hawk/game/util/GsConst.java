package com.hawk.game.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.hawk.game.module.PlayerTimerModule;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.World.SearchType;

/**
 * 游戏常量定义
 *
 * @author hawk
 */
public class GsConst {
	/**
	 * 压缩标志
	 */
	public static byte COMPRESS_FLAG = 0x1;

	// 勋章道具id
	public static final int MEDAL_ITEM_ID = 1300017;
	// 星耀勋章id
	public static final int MEDAL_STAR_ID = 1300027;
	// 成功
	public static final int SUCCESS_OK = 0;
	// 概率最小基数
	public static final int RANDOM_BASE_VALUE = 1;
	// 万位改了基础
	public static final int RANDOM_MYRIABIT_BASE = 10000;
	// 一天的毫秒数
	public static final long DAY_MILLI_SECONDS = 86400000;
	// 一天的秒数
	public static final int DAY_SECONDS = 86400;
	// 一周的毫秒数
	public static final long WEEK_MILLI_SECONDS = 86400000L * 7;
	// 一个月的秒数
	public static final int MONTH_SECONDS = 86400 * 30;
	// 一个月的毫秒数
	public static final long MONTH_MILLI_SECONDS = 86400 * 30 * 1000L;
	// 一分钟的毫秒数
	public static final int MINUTE_MILLI_SECONDS = 60000;
	// 一小时的毫秒数
	public static final int HOUR_MILLI_SECONDS = 3600000;
	// 类型定义基数
	public static final int ITEM_TYPE_BASE = 10000;
	// 抽奖错误物品ID定义
	public static final int UNKONWN_ITEM_ID = 99999;
	// 地图边界距离
	public static final int WORLD_BOUNDARY_SIZE = 1;
	// 玩家城点半径
	public static final int PLAYER_POINT_RADIUS = 2;
	// 世界机甲半径
	public static final int GUNDAM_RADIUS = 2;
	// 世界年兽半径
	public static final int NIAN_RADIUS = 2;
	// 尤里点半径
	public static final int YURI_POINT_RADIUS = 2;
	// 最大建筑等级
	public static final int BUILDING_MAX_LEVEL = 50;
	// 客户端存储自定义数据最大长度限制
	public static final int CUSTOM_DATA_LIMIT = 256;
	// 玩家搜索最大数量
	public static final int SEARCH_PLAYER_MAX = 50;
	// 联盟搜索最大数量
	public static final int SEARCH_GUILD_MAX = 50;
	// 联盟推荐页显示数量
	public static final int RECOMMOND_GUILD_MAX = 20;
	// 联盟推荐每页的数量
	public static final int GUILD_RECOMMEND_PAGE_COUNG = 10;
	// 世界点随机次数
	public static final int POINT_SHUFFLE_TIMES = 5;
	// 作用号万分比
	public static final double EFF_PER = 0.0001;
	// 作用号万分比
	public static final double EFF_RATE = 10000;
	// 可重用个数
	public static final int REUSABLE_QUEUE_COUNT = 8;
	// 空字符串
	public static final String NULL_STRING = "null";
	// 新手资源创建失败
	public static final int NEWLY_POINT_CREATE_FAIL = 1;
	// 联盟宝藏刷新时间点
	public static final int STORE_HOUSE_REFRASH = 5;
	// 联盟科技推荐数量上限
	public static final int GUILD_SCIENCE_MAX_RECOMMEND = 2;
	// 领地BUFF类型
	public static final int MANOR_BUFF_TYPE = 1;
	// 领地DEBUFF类型
	public static final int MANOR_DEBUFF_TYPE = 2;
	// 城防值系数1000s
	public static final int CITY_DEF_RATE = 1000000;
	// 新手引导走完时存储的step值
	public static final int NEWBIE_COMPLETE_VALUE = -1;
	// 新手引导节点中第一个节点的前置节点ID
	public static final int NEWBIE_FIRST_PRE_STEP = 0;
	// 技能： 10103 救援
	public static final int SKILL_10103 = 10103;
	// 技能: 10104 决斗
	public static final int SKILL_10104 = 10104;
	// 雷达误差
	public static final double RADA_RANDOM_RANGE = 0.1D;
	// 充值使用基础值
	public static final float RECHARGE_BASE = 0.1f;
	// 异常修复间隔 60s
	public static final long EXCEPTION_CHECK_TICK_TIME = 60 * 1000;
	// 总统府箭塔数量
	public static final int PRESIDENT_TOWER_COUNT = 4;
	// 世界点点到地格转化率
	public static final int POINT_TO_GRUD = 2;
	// UIC限制门槛级别
	public static final int UIC_DOOR_LEVEL = 1;
	// UIC字串限制级别
	public static final int UIC_DIRTY_LEVEL = 2;
	// UIC敏感词替换
	public static final int UIC_SENSITIVE_REPLACE = 1;
	// UIC敏感词保持不替换
	public static final int UIC_KEEP_SENSITIVE = 0;
	public static final int INT_TRUE = 1;
	public static final int INT_FALSE = 0;

	// 周年庆蛋糕半径
	public static final int CAKE_SHARE_RADIUS = 3;
	/** 签名*/
	public static final String NO_SIGNATURE_SIGN = "@NoSignature";

	public static final String DRESS_GOD_ACTIVE = "DressGodActive";
	
	// 日志记录切换城外玩家数量tick周期
	public static final long COUNT_INT_WORLD_LOG_TICK = 60 * 5 * 1000L;
	/**
	 * 永久时间毫秒数。 定义为100年
	 */
	public static final long PERPETUAL_MILL_SECOND = 100L * 365L * 24L * 3600L * 1000L;
	/**
	 * 排队登录状态挂到session上的userObjectKey
	 */
	public static final String LOGIN_WAIT_KEY = "loginWaitState";
	/**
	 * 新手油井补偿
	 */
	public static final String TUTORIAL_OIL_KEY = "tutorialOil";
	/**
	 * 机甲解锁后发放道具奖励
	 */
	public static final String SUPER_SOLDIER_UNLOCK_REWARD_KEY = "superSoldierReward";
	/**
	 * 第二建造队列开启
	 */
	public static final String SECOND_BUILD_OPEN_KEY = "secondBuildUnlock";
	/**
	 * 队列额外数据
	 */
	public static final String QUEUE_CUSTOM_DATA_KEY = "queue_custon_data_key";
	/**
	 * 充值数据刷新
	 */
	public static final String RECHARGE_DATA_REF_KEY = "rechargeDataRefresh";

	/**
	 * 泰伯利亚分享记录
	 */
	public static final String SHARE_TBLY_FAME_HALL_KEY = "shareTblyFameHall";
	/**
	 * 推送信息
	 */
	public static final String PUSH_INFO_KEY = "pushInfo";
	
	/**
	 * 玩家名字槽数量
	 */
	public static final int NAME_SLOT_SIZE = 100;
	
	/**
	 * 资源援助
	 */
	public static final String RES_ASS = "assistanceRes";
	
	/**
	 * 被资源援助
	 */
	public static final String RES_BE_ASS= "resAssistance";
	
	/**
	 * vip经验值检测标识
	 */
	public static final String	VIP_EXP_FLAG_KEY = "vipExpFlag";
	/**
	 * 行军表情包
	 */
	public static final String MARCH_EMOTICON_BAG = "marchEmoticonBag"; 
	/**
	 * 聊天表情
	 */
	public static final String CHAT_IMAGE_UNLOCKED = "ChatImageUnlockId"; 

	/**
	 * 玩家设置允许接受装扮乞讨消息
	 */
	
	public static final String PLAYER_DRESS_ASKREFUSE = "option_dressAsk"; 
	
	/**
	 * 玩家主战兵种
	 */
	public static final String PLAYER_MAIN_FORCE = "mainForce";
	/**
	 * 打假怪的次数
	 */
	public static final String FAKE_MONSTER_KILL_TIMES = "fakeMonsterKillTimes";
	
	public static final String CHARGE_SHOW = "IdipNotShowDoubleCharge";
	
	/**
	 * 战旗地图参数
	 */
	public static final int FLAG_MAP_PARAM = 1000;
	
	// 本地ip地址
	public static String LOCAL_IP = null;
	
	public static final int DUEL_INDEX = 10;   // 决斗队列的index
	public static final int MAX_PRESET_SIZE = 15;  // 编队最大数量
	public static final long SUPER_ARMOUR_ENDTIME_TICK = 3000L;
	
	/**
	 * 要塞占领信息过期时间
	 */
	public static final int FORTRESS_OCCUPY_EXPIRE = 3 * 86400;
	
	// 自动打野时搜索野怪的顺序
	public static final List<SearchType> SEARCH_MONSTER_AUTO_ORDER = Arrays.asList(SearchType.SEARCH_NEW_ACT_MONSTER,SearchType.SEARCH_YURI_MONSTER, SearchType.SEARCH_MONSTER);

	/**
	 * 年兽幽灵怪
	 */
	public static final int WORLD_NIAN_GHODT = 2;
	/**
	 * 圣诞怪物的
	 */
	public static final int CHRISTMAS_BOSS_SIZE = 2;
	
	/**
	 * 兵种星级偏移量
	 */
	public static final long ARMY_STAR_OFFSET = 10000000;
	/**
	 * 每日必买卡维利宝箱开启次数
	 */
	public static final String 	DAILY_GIFT_BOX = "dailyGift_box_openTimes";
	/**
	 * 特权礼包半价倒计时提醒有效时间
	 */
	public static final String GOLD_PRIVILEGE_NOTITY_VALIDTIME = "goldPrivilege_notify_valid";
	
	/**
	 * 跨天联盟任务刷新,每轮tick刷新数量上限
	 */
	public static final int GUILD_TASK_REFRESH_CNT_PER = 20;
	/**
	 * 个保法开关
	 */
	public static final String PERSONAL_PROTECT_KEY = "personal_protect_val";
	/**
	 * 玩家连续登录信息
	 */
	public static final String CONTINUE_LOGIN_KEY = "player_continue_login";
	
	/**
	 * 平台信息授权变更事件
	 */
	public static final int EVENT_CANCEL_PFAUTH     = -1; // 解除全部授权。 取消渠道授权，一般会触发登录态失效，玩家需要重新登陆授权。所有信息（个人信息+平台好友关系链）全部清理。
	public static final int EVENT_CANCEL_PERSONINFO = -2; // 解除个人信息 。 需要清理游戏内昵称、头像等信息。
	public static final int EVENT_CANCEL_RELATION   = -3; // 解除平台好友关系链。  需要清理当前账号个人的游戏内平台好友关系链、及好友的平台关系链。
	public static final int EVENT_RECOVER_PFAUTH     = 1; // 全部授权。  重新登陆授权即可自动恢复授权，正常来讲用不上这个值。
	public static final int EVENT_RECOVER_PERSONINFO = 2; // 重新授权个人信息。  恢复游戏内昵称、头像等信息。
	public static final int EVENT_RECOVER_RELATION   = 3; // 重新授权平台好友关系链。  恢复当前账号在个人的平台好友关系链及其好友展示。
	
	/**
	 * 玩家可采集、掠夺的资源类型（注意不要改变顺序）
	 */
	public static final int[] RES_TYPE = new int[] {
			PlayerAttr.GOLDORE_UNSAFE_VALUE,
			PlayerAttr.OIL_UNSAFE_VALUE,
			PlayerAttr.TOMBARTHITE_UNSAFE_VALUE,
			PlayerAttr.STEEL_UNSAFE_VALUE
	};

	/**
	 * 个人排行榜（相关联盟类型的排行榜来说）
	 */
	public static final RankType[] PERSONAL_RANK_TYPE = new RankType[] {
			RankType.PLAYER_FIGHT_RANK,
			RankType.PLAYER_KILL_ENEMY_RANK,
			RankType.PLAYER_CASTLE_KEY,
			RankType.PLAYER_GRADE_KEY,
			RankType.PLAYER_NOARMY_POWER_RANK,
	};
	
	/**
	 * 联盟相关的排行榜
	 */
	public static final RankType[] GUILD_RANK_TYPE = new RankType[] {
			RankType.ALLIANCE_KILL_ENEMY_KEY,
			RankType.ALLIANCE_FIGHT_KEY
	};

	/**
	 * 对象类型
	 *
	 * @author hawk
	 */
	public static class ObjType {
		// 玩家对象
		public static final int PLAYER = 1;
		// 应用程序
		public static final int MANAGER = 100;
		/**虎牢关副本*/
		public static final int LMJYAOGUAN_ROOM = 200; 
		/**锦标赛副本*/
		public static final int TBLYAOGUAN_ROOM = 300;
		public static final int SWAOGUAN_ROOM = 400;
		public static final int CYBORGAOGUAN_ROOM = 500;
		public static final int DYZZAOGUAN_ROOM = 600;
		public static final int YQZZAOGUAN_ROOM = 700;
		public static final int XHJZAOGUAN_ROOM = 800;
		public static final int FGYLAOGUAN_ROOM = 900;
		public static final int XQHXAOGUAN_ROOM = 1000;
	}

	/**
	 * 系统对象id
	 *
	 * @author hawk
	 */
	public static class ObjId {
		// 应用程序
		public static final int APP = 1;
		// 充值管理器
		public static final int RECHARGE = 2;
		// 聊天
		public static final int CHAT = 3;
		// 联盟
		public static final int GUILD = 4;
		// 世界管理器
		public static final int WORLDMARCH = 5;
		// 领地管理器
		public static final int MANOR = 6;
		// 行军管理器
		public static final int MARCH = 7;
		// 活动管理器
		public static final int ACTIVITY = 8;
		// 道具管理
		public static final int ITEM = 9;
		// 指挥官管理器
		public static final int COMMANDER = 10;
		// 国王战管理器
		public static final int PRESIDENT = 11;
		// 战斗管理器
		public static final int BATTLE = 13;
		// 技能管理器
		public static final int SKILL = 14;
		/**游戏服务中的Activity*/
		public static final int SERVER_ACTIVITY = 15;
		/**好友*/
		public static final int RELATION = 16;
		/**世界buff*/
		public static final int GLOBAL_BUFF = 17;
		/**邮件*/
		public static final int MAILMANAGER = 18; 
		// 推送服务
		public static final int PUSHER = 100;
		// 搜索服务
		public static final int SEARCHER = 101;
		// 排行
		public static final int RANK = 102;
		// 问卷系统
		public static final int QUESTIONNAIRE = 104;
		// 世界基础点
		public static final int WORLDPOINT = 105;
		// 世界玩家点
		public static final int WORLDPLAYER = 106;
		// 世界怪物点
		public static final int WORLDMONSTER = 107;
		// 世界资源点
		public static final int WORLDRESOURCE = 108;
		// 尤里兵工厂点
		public static final int WORLDYURI = 109;
		// 尤里复仇
		public static final int YURI_REVENGE = 110;
		// 据点
		public static final int WORLD_STRONGPOINT = 111;
		/**定时器服务*/
		public static final int TIMER = 112;
		// 据点
		public static final int WORLD_FOGGY = 113;
		// 超级武器
		public static final int SUPER_WEAPON = 114;
		/**虎牢关*/
		public static final int HLG_ROOMMANAGER = 115;
		// 世界机甲
		public static final int WORLDGUNDAM = 116;
		// 跨服处理器
		public static final int CROSS_SERVER = 117;
		/**战争学院*/
		public static final int WAR_COLLEGE = 118; 
			// 跨服处理器
		public static final int CROSS_ACTIVITY = 119;
		// 世界年兽
		public static final int WORLDNIAN = 120;
		// 世界寻宝
		public static final int TREASURE_HUNT = 121;
		// 战地之王
		public static final int WAR_FLAG = 122;
		// 军魂承接
		public static final int INHERIT = 123;
		// 资源宝库
		public static final int RES_TREASURE = 124;
		// 军事学院
		public static final int MILITARY_COLLEGE = 125;	
		// 要塞
		public static final int CROSS_FORTRESS = 126;
		// 泰伯利亚之战
		public static final int TIBERIUM_WAR = 127;
		/**泰博历亚*/
		public static final int TBLY_ROOMMANAGER = 128;
		/** 联盟锦标赛*/
		public static final int CHAMPIONSHIP = 129;
		// 泰伯利亚联赛
		public static final int TIBERIUM_LEAGUA_WAR = 130;
		/**star wars*/
		public static final int SW_ROOMMANAGER = 131;
		// 星球大战阶段管理
		public static final int STAR_WARS_ACTIVITY = 132;
		// 世界机器人
		public static final int WORLD_ROBOT = 133;
		// 世界能量塔
		public static final int WORLD_PYLON = 134;
		/**攻防模拟战*/
		public static final int SIMULATE_WAR = 135;
		/**圣诞大战*/
		public static final int CHRISMAS_WAR = 136;
		// 世界雪球
		public static final int WORLD_SNOWBALL = 137;
		public static final int CYBORG_ROOMMANAGER = 138;
		/** 赛博之战*/
		public static final int CYBORG_WAR = 139;
		/** 老玩家回流*/
		public static final int BACK_FLOW = 141;
		/** 军魂承接(新)*/
		public static final int INHERIT_NEW = 142;
		/** 赛博联赛*/
		public static final int CYBORG_LEAGUA = 143;
		/** 远征司令技能*/
		public static final int CROSS_SKILL = 144;

		/** 小战区*/
		public static final int XIAO_ZHAN_QU = 146;




		/** 方尖碑 */
		public static final int OBELISK = 145;
		/**dyzz*/
		public static final int DYZZ_ROOMMANAGER = 147;
		
		/** 达雅之战*/
		public static final int DYZZ_WAR = 148;
		public static final int DYZZ_MATCH = 149;
		
		/** 国家 */
		public static final int NATIONAL = 150;
		/** 达雅赛季*/
		public static final int DYZZ_SEASON = 151;
		public static final int YQZZ_ROOMMANAGER = 152;

		/** 月球之战匹配*/
		public static final int YQZZ_MATCH = 153;
		public static final int XHJZ_ROOMMANAGER = 154;
		public static final int XHJZ_WAR = 155;
		public static final int SHOP = 156;
		public static final int FGYL_ROOMMANAGER = 157;
		public static final int GUILD_TEAM = 158;
		public static final int TBLY_WAR = 159;
		public static final int TBLY_SEASON = 160;
		
		public static final int FGYL_WAR = 161;

		public static final int CMW = 162;
		public static final int XQHX_ROOMMANAGER = 163;
		public static final int XQHX_WAR = 164;
		
		/** 自动集结*/
		public static final int AUTO_MASS_JON = 165;
		public static final int METERIAL_TRAN = 166;
		/** 航海赛季*/
		public static final int CROSS_ACTIVITY_SEASON = 167;
		/** 家园*/
		public static final int HOME_LAND = 168;
	}

	/**
	 * 在线状态
	 *
	 * @author hawk
	 *
	 */
	public static class PlayerState {
		// 离线
		public static final int OFFLINE = 0;
		// 登录中
		public static final int LOGINING = 1;
		// 在线
		public static final int ONLINE = 2;
	}
	
	public static class PlayerCrossStatus {
		public static final int NOTHING = 0; //没有任何状态.
		public static final int PREPARE_CROSS = 10; //正在处理跨服
		public static final int CORSS = 20;//已经跨服
		public static final int PREPARE_EXIT_CROSS	= 30; //准备退出跨服
		public static final int EXIT_CROSS_MARCH_FINAL	= 31; //退出跨服行军处理完成
		public static final int EXIT_CROSS = 40; //退出跨服.
	}

	/**
	 * 鉴权等级
	 */
	public static class AuthCheckLevel {
		// 不鉴权
		public static final int IGNORE = 0;
		// 延迟鉴权
		public static final int RELAX = 1;
		// 严格鉴权
		public static final int STRICT = 2;
	}

	/**
	 * 保护状态作用号值
	 */
	public static class ProtectState {
		// 无保护
		public static final int NO_BUFF = 0;
		// 普通保护道具
		public static final int POP_TOOL = 1;
		// 新手出生保护
		public static final int NEW_PLAYER = 2;
	}

	/**
	 * 模块定义, 模块的先后顺序和数据初始化先后顺序相关联, 独立数据模块放在前面
	 *
	 * @author hawk
	 */
	public static class ModuleType {
		// 登陆模块
		public static final int LOGIN_MODULE = 1;
		// 登陆模块
		public static final int ITEM_MODULE = 2;
		// 建筑模块
		public static final int BUILDING_MODULE = 3;
		// 建筑模块
		public static final int QUEUE_MODULE = 4;
		// 学院模块
		public static final int TECHNOLOGY_MODULE = 5;
		// 军队模块
		public static final int ARMY_MODULE = 6;
		// 角色功能操作
		public static final int OPERATION_MODULE = 7;
		// 天赋模块
		public static final int TALENT_MODULE = 8;
		// 联盟模块
		public static final int GUILD_MODULE = 9;
		// 聊天模块
		public static final int CHAT_MODULE = 10;
		// 邮件模块
		public static final int MAIL_MODULE = 11;
		// 世界数据
		public static final int WORLD_MODULE = 12;
		// 排行模块
		public static final int RANK_MODULE = 13;
		// 任务模块
		public static final int MISSION_MODULE = 14;
		// 世界行军
		public static final int WORLD_MARCH_MODULE = 15;
		// 充值模块
		public static final int RECHARGE_MODULE = 16;
		// GM模块
		public static final int GM_MODULE = 17;
		// 装备系统模块
		public static final int EQUIP_MODULE = 20;
		// 指挥官模块
		public static final int COMMANDER_MODULE = 21;
		// 联盟领地
		public static final int GUILD_MANOR_MODULE = 22;
		// 每日登陆活动
		public static final int DAILY_LOGIN_ACTIVITY = 23;
		// 奖励模块
		public static final int REWARD_MODULE = 24;
		// 活动模块
		public static final int ACTIVITY_MODULE = 25;
		// 新手模块
		public static final int NEWLY_MODULE = 26;
		// 国王战模块
		public static final int PRESIDENT_MODULE = 27;
		// 英雄模块
		public static final int HERO = 29;
		// 剧情任务模块
		public static final int STORY_MISSSION = 30;
		// 腾讯安全SDK模块
		public static final int TESS_SDK = 31;
		// 问卷调查模块
		public static final int QUESTIONNAIRE = 32;
		// 配置检测模块
		public static final int CONFIG_CHECK = 33;
		// 联盟宝藏
		public static final int STORE_HOUSE = 34;
		// 联盟仓库
		public static final int WARE_HOUSE = 35;
		// 许愿池
		public static final int WISHING = 36;
		// 酒馆
		public static final int TAVERN = 37;
		/** 旅行商人 */
		public static final int TRAVEL_SHOP = 39;
		// 码头
		public static final int WHARF = 40;
		// 尤里复仇
		public static final int YURI_REVENGE = 41;
		/*** 好友系统 */
		public static final int RELATION = 42;
		/** 玩家定时系统 */
		public static final int TIMER = 43;
		/**
		 * 剧情战役
		 */
		public static final int PLOT_BATTLE = 44;
		/** 战地任务 **/
		public static final int BATTLE_MISSION = 45;

		/** 玩家装扮 **/
		public static final int PLAYER_DRESS = 46;

		/** 礼包模块 **/
		public static final int GIFT_MOUDLE = 47;
		/***
		 * 推送礼包
		 */
		public static final int PUSH_GIFT = 48;

		/** 联盟反击 */
		public static final int GUILD_COUNTER = 49;

		/** 联盟大礼包 */
		public static final int GUILD_BIG_GIFT = 50;

		/** 军衔 */
		public static final int MILITARY_RANK = 51;

		/** 超级武器(名城) */
		public static final int SUPER_WEAPON = 52;

		/** 累积在线 */
		public static final int ACCUMULATE_ONLINE = 53;
		/** 线上紧急逻辑处理 */
		public static final int URGENCY_LOGIC = 54;
		public static final int YURI_STRIKE = 55; // 有力来犯
		public static final int PLAYER_IMAGE = 56; // 头像
		/** 关怀 */
		public static final int GUILD_HOSPICE = 57;
		/** 幽灵行军 */
		public static final int GHOST_STRIKE = 58;
		/** 玩家成就 **/
		public static final int PLAYER_ACHIEVE = 59;
		// 超级兵模块
		public static final int SUPER_SOLDIER = 60;
		/** 战争学院 */
		public static final int WAR_COLLEGE = 61;
		/** 我要变强 */
		public static final int STRENGTHEN_GUIDE = 62;
		/** 分享模块 **/
		public static final int SHARE_MODULE = 63;
		/** 联盟军演 */
		public static final int LMJY_MODULE = 64;
		public static final int SUPER_LAB = 65;
		/** 跨服 */
		public static final int CROSS_SERVER = 66;
		/** 跨服活动 */
		public static final int CROSS_ACTIVITY = 67;
		/** 红包模块 **/
		public static final int RED_ENVELOPE_MODULE = 68;
		/** 战地之王 **/
		public static final int WAR_FLAG = 69;
		/** 军事学院 **/
		public static final int MILITARY_COLLEGE = 70;
		/** 泰伯利亚之战 */
		public static final int TIBERIUM_WAR = 71;
		public static final int TBLY_MODULE = 75;
		// 铠甲系统模块
		public static final int ARMOUR_MODULE = 76;
		/** 联盟锦标赛之战 */
		public static final int CHAMPIONSHIP = 77;
		/**星球大战**/
		public static final int STAR_WARS		= 78;
		public static final int SW_MODULE = 79;
		/**活动周历*/
		public static final int ACTIVITY_WEEK_CALENDER = 80;
		/**攻防模拟战*/
		public static final int SIMULATE_WAR	= 81;
		public static final int CYBORG_MODULE	= 82;
		/** 赛博之战-玩家相关module */
		public static final int CYBORG_WAR = 83;
				
		/** 账号注销*/
		public static final int ACCOUNT_CANCELLATION = 86;
		public static final int LABRATORY = 87;
		// 远征科技模块
		public static final int CROSS_TECH_MODULE = 88;
		/** 幽灵工厂*/
		public static final int GHOST_TOWER = 89;

		public static final int PLANT_FACTORY = 91;
		public static final int PLANT_TECH = 92;
		/** 小战区*/
		public static final int XZQ_MODULE = 93;

		/** 方尖碑*/
		public static final int OBELISK_MODULE = 90;
	
		/** 情报中心*/
		public static final int AGENCY_MODULE = 94;

		/** 泰能强化*/
		public static final int PLANT_SOLDIER_SCHOOL = 95;
		public static final int PLANT_SOLDIER_ADVANCE = 96;
		/** 泰能科技树*/
		public static final int PLANT_SCIENCE = 97;
		public static final int DYZZ_MODULE = 98;
		
		/** 迁服*/
		public static final int IMMGRATION = 99;

		public static final int DYZZ_WAR = 100;
		
		/** 国家基础和建设处 */
		public static final int NATIONAL_CONSTRUCTION = 101;
		
		/** 国家任务中心 */
		public static final int NATIONAL_MISSION = 102;
		
		/** 国家医院 */
		public static final int NATIONAL_HOSPITAL = 103;
		
		/** 国家仓库 */
		public static final int NATIONAL_WAREHOUSE = 104;
		
		/** 国家科技 */
		public static final int NATIONAL_TECH = 105;

		/** 超时空特权卡 自动拉锅 */
		public static final int AUTO_GATHER = 106;
		
		/** 飞船制造厂 */
		public static final int NATIONAL_SHIP_FACTORY = 107;
		/**国家军功*/
		public static final int NATION_MILITARY = 108;
		/**国家军功*/
		public static final int CROSS_TALENT = 109;
		
		/** 国家医院(统帅之战死兵) */
		public static final int NATIONAL_HOSPITAL_TSZZ = 110;

		/** 5v5赛季*/
		public static final int DYZZ_SEASON = 111;
		public static final int YQZZ_MODULE = 112;

		/** 月球之战*/
		public static final int YQZZ_WAR_MODULE = 113; 
		/** 星甲召唤模块  */
		public static final int SPACE_MECHA_MODULE = 114;
		
		// 联盟编队
		public static final int GUILD_FORMATION = 115;
		// 兵种转换
		public static final int BINGZHONGZHUANHUAN = 116;
		public static final int STAFF_OFFICE = 117;
		
		// 终身卡
		public static final int LIFETIME_CARD = 118;
		// 偷菜
		public static final int MEDAL_FACTORY = 119;
		public static final int XHJZ = 120;
		public static final int XHJZ_WAR = 121;
		// 超武
		public static final int MANHATTAN = 122;
		//每日必买宝箱
		public static final int DAILY_GIFT_BUY_MOUDLE = 123;
		// 通用商店
		public static final int SHOP = 124;
		public static final int FGYL = 125;
		public static final int GUILD_TEAM = 126;
		
		
		public static final int FGYL_WAR_MOUDLE = 127;
		public static final int CMW_MOUDLE = 128;
		//机甲核心
		public static final int MECHA_CORE = 129;
		public static final int XQHX_WAR_MOUDLE = 130;
		public static final int XQHX = 131;
		public static final int MTTRUCK = 132;
		public static final int AUTO_MASS_JOIN = 133;
		public static final int SCHEDULE = 134;
        //家园
        public static final int HOME_LAND_MODULE = 135;

		// 账号模块
		public static final int ACCOUNT_MOUDLE = 199;
		
		// 空闲模块(保证在最后)
		public static final int IDLE_MODULE = 200;
		
	}

	public static class MarchAction {
		// 行军异常检测
		public static final int INIT_CHECK = 1;
	}

	/**
	 * 世界地图上对象类型定义
	 * 
	 * @author hawk
	 *
	 */
	public static class WorldObjType {
		// 怪
		public static final int MONSTER = 1;
		// 资源
		public static final int RESOURCE = 2;
		// 城点
		public static final int CITY = 3;
		// 驻扎部队
		public static final int ARMY = 4;
		// 行军
		public static final int MARCH = 5;
		// 玩家
		public static final int PLAYER = 6;
		// 联盟据点
		public static final int GUILD_GUARD = 7;
		// 联盟建筑
		public static final int GUILD_TERRITORY = 8;
		// 移动建筑
		public static final int MOVEABLE_BUILDING = 9;
		// 机器人类型
		public static final int ROBOT = 10;
		// 箱子
		public static final int BOX = 11;
		// 尤里兵工厂
		public static final int YURI = 12;
		// 总统府
		public static final int PRESIDENT = 13;
		// 总统府箭塔
		public static final int PRESIDENT_TOWER = 14;
		// 据点
		public static final int STRONGPOINT = 15;
		// 迷雾要塞
		public static final int FOGGYFORTESS = 16;
		// 超级武器
		public static final int SUPER_WEAPON = 17;
		// 尤里来犯
		public static final int YURI_STRIKE = 18;
		// 机甲
		public static final int GOUDA = 19;
		// 年兽
		public static final int NIAN = 20;
		// 寻宝野怪		
		public static final int TREASURE_MON = 21;
		// 寻宝资源
		public static final int TREASURE_RES = 22;
		// 战地旗帜
		public static final int WAR_FLAG = 23;
		/** 资源宝库, 开仓放粮,*/ 
		public static final int RESOURC_TRESURE = 24;
		// 跨服要塞
		public static final int CROSS_FORTRESS = 25;
		// 年兽宝箱
		public static final int NIAN_BOX = 26;
		// 能量塔
		public static final int PYLON = 27;
		/**圣诞boss*/
		public static final int CHRISTMAS_BOSS = 28;
		/**圣诞宝箱**/
		public static final int CHRISTMAS_BOX	 = 29;
		/**雪球**/
		public static final int SNOWBALL = 30;
		/** 端午龙船*/
		public static final int DRAGON_BOAT = 33;
		/** 幽灵工厂怪物*/
		public static final int GHOST_TOWER_MONSTER = 32;
		/**周年庆蛋糕*/
		public static final int CAKE_SHARE = 34;
		/** 小站区建筑*/
		public static final int XQZ_BUILD = 33;
		/** 国家建筑 */
		public static final int NATION_BUILD = 34;
		/** 资源狂欢宝箱*/
		public static final int RESOURCE_SPREE_BOX = 35;
		/** 星甲召唤主舱体 */
		public static final int SPACE_MECHA_MAIN = 36;
		/** 星甲召唤子舱体 */
		public static final int SPACE_MECHA_SLAVE = 37;
		/** 星甲召唤怪物点 */
		public static final int SPACE_MECHA_MONSTER = 38;
		/** 星甲召唤据点  */
		public static final int SPACE_MECHA_STRONG_HOLD = 39;
		/** 星甲召唤掉落的宝箱 */
		public static final int SPACE_MECHA_BOX = 40;
	}

	/**
	 * 领地内堡垒驻军；超级发射平台驻军
	 * 
	 * @author zhjx
	 *
	 */
	public static class QuarteredMarchType {
		public static final int MANOR = 1;
		public static final int SUPER_MACHINE = 2;
	}

	/**
	 * 价格类型
	 * 
	 * @author hawk
	 *
	 */
	public static enum PriceType {
		RMB(1),
		USD(2);
		
		int value;
		
		PriceType(int val) {
			this.value = val;
		}
		
		public int intVal() {
			return value;
		}
	}

	/**
	 * 警报类型
	 * 
	 * @author hawk
	 *
	 */
	public static class AlaramType {
		// 行军
		public static final int MARCH = 1;
		// 核弹
		public static final int NUCLEAR = 2;
		// 闪电风暴
		public static final int WEATHER_STORM = 3;
	}

	/**
	 * 超级武器状态
	 */
	public static class NuclearState {
		// 装填
		public static final int LOADING = 1;
		// 发射
		public static final int LAUCHING = 2;
		// 爆炸后
		public static final int DAMAGED = 3;
		// 消失
		public static final int DISAPPEAR = 4;
	}

	/**
	 * 奖励掉落类型
	 * 
	 * @author lating
	 *
	 */
	public static class AwardDropType {
		/**
		 *  概率掉落
		 */
		public static final int ODDS = 0;
		/**
		 *  权重掉落
		 */
		public static final int WEIGHT = 1;
	}


	public static class GuildConst {
		/**
		 * 联盟商店记录-购买
		 */
		public static final int SHOP_LOG_TYPE_BUY = 1;
		/**
		 * 联盟商店记录-补货
		 */
		public static final int SHOP_LOG_TYPE_ADD = 2;
	}

	/**
	 * 正则表达式类型
	 * 
	 * @author shadow
	 *
	 */
	public static class RegexType {
		/**
		 * 0-9
		 */
		public static final int NUM = 1;
		/**
		 * A-Z
		 */
		public static final int UPPERLETTER = 1 << 1;
		/**
		 * a-z
		 */
		public static final int LOWERLETTER = 1 << 2;
		/**
		 * a-z&A-Z
		 */
		public static final int ALLLETTER = UPPERLETTER + LOWERLETTER;
		/**
		 * 空格
		 */
		public static final int SPACE = 1 << 3;
		/**
		 * 汉字
		 */
		public static final int CHINESELETTER = 1 << 4;
	}


	/**
	 * 邮件常量
	 */
	public static class Mail {
		/**
		 * 聊天室消息最大条数
		 */
		public static final int CHATROOM_MSG_LEN = 50;
	}

	/**
	 * 联盟聊天
	 */
	public static class Alliance {
		/**
		 * 联盟聊天消息最大条数
		 */
		public static final int PUSH_CHAT_COUNT = 500;// 聊天缓存条数
		public static final int GET_CHAT_MST_COUNT = 20; // 一次取20条记录
		public static final int GET_CHAT_WORLD_COUNT = 20; // 世界一次读取
		public static final int VOICE_CHAT_MAX_TIME = 16; // 语音最大长度
	}

	/**
	 * 军队兵力变化类型
	 * 
	 * @author Administrator
	 *
	 */
	public static class ArmyChangeType {
		// 出征
		public static final int MARCH = 0;
		// 受伤
		public static final int WOUNDED = 1;
		// 死亡
		public static final int DIE = 2;
		// 出征返回（包括伤兵和死亡兵的数量）
		public static final int MARCH_BACK = 3;
		// 训练
		public static final int TRAIN = 4;
		// 训练结束
		public static final int TRAIN_FINISH = 5;
		// 取消训练
		public static final int TRAIN_CANCEL = 6;
		// 收兵
		public static final int COLLECT = 7;
		// 治疗伤兵
		public static final int CURE = 8;
		// 治疗结束
		public static final int CURE_FINISH = 9;
		// 取消治疗
		public static final int CURE_CANCEL = 10;
		// 解雇
		public static final int FIRE = 11;
	}

	/**
	 * 雷达根据其等级不同，等级越高获取到敌方的行军信息越详细
	 * 
	 * @author lating
	 *
	 */
	public class RadarLevel {

		public static final int LV1 = 1;
		public static final int LV2 = 2;
		public static final int LV3 = 3;
		public static final int LV4 = 4;
		public static final int LV5 = 5;
		public static final int LV6 = 6;
		public static final int LV7 = 7;
		public static final int LV8 = 8;
		public static final int LV9 = 9;
		public static final int LV10 = 10;
		public static final int LV11 = 11;
		public static final int LV12 = 12;
		public static final int LV13 = 13;
		public static final int LV14 = 14;
		public static final int LV15 = 15;
		public static final int LV16 = 16;
		public static final int LV17 = 17;
		public static final int LV18 = 18;
		public static final int LV19 = 19;
		public static final int LV20 = 20;
		public static final int LV21 = 21;
		public static final int LV22 = 22;
		public static final int LV23 = 23;
		public static final int LV24 = 24;
		public static final int LV25 = 25;
		public static final int LV26 = 26;
		public static final int LV27 = 27;
		public static final int LV28 = 28;
		public static final int LV29 = 29;
		public static final int LV30 = 30;
		public static final int LV31 = 31;
		public static final int LV32 = 32;
		public static final int LV33 = 33;
		public static final int LV34 = 34;
		public static final int LV35 = 35;

	}

	/**
	 * 任务状态
	 */
	public static class MissionState {
		/**
		 * 任务未开启
		 */
		public static final int STATE_NOT_OPEN = -1;
		/**
		 * 任务已开启未完成
		 */
		public static final int STATE_NOT_FINISH = 0;
		/**
		 * 任务已完成未领取奖励
		 */
		public static final int STATE_FINISH = 1;
		/**
		 * 任务已领取奖励
		 */
		public static final int STATE_BONUS = 2;
		/**
		 * 章节任务全部完成
		 */
		public static final int STORY_MISSION_COMPLETE = 999;
	}

	/**
	 * 任务条件类型
	 */
	public static class MissionConditionType {
		/**
		 * 初始化任务标记无条件开启
		 */
		public static final int CONDITION_INIT = 0;
		/**
		 * 根据玩家条件开启
		 */
		public static final int CONDITION_PLAYER_LEVEL = 1;
		/**
		 * 根据建筑等级开启
		 */
		public static final int CONDITION_BUILD_LEVEL = 2;
		/**
		 * 根据任务条件开启
		 */
		public static final int CONDITION_IDS = 3;

	}

	/**
	 * 任务类型
	 * 
	 * @author lating
	 * 
	 */
	public enum MissionFunType implements IntConst {

		/** 城建-某个建筑升到多少级 */
		FUN_BUILD_LEVEL(0, 1),

		/** 城建-建筑个数 */
		FUN_BUILD_NUMBER(1, 2),

		/** 科技达到等级 */
		FUN_TECH_LEVEL(10, 4),

		/** 科技研究完成次数 */
		FUN_TECH_RESEARCH(11, 5, true),

		/** 士兵-某个兵种训练（训练完成）多少个 */
		FUN_TRAIN_SOLDIER_COMPLETE_NUMBER(20, 8, true),

		/** 士兵-拥有兵多少个 */
		FUN_TRAIN_SOLDIER_HAVE_NUMBER(21, 9),

		/** 士兵-某个兵种训练（开始训练）多少个 */
		FUN_TRAIN_SOLDIER_START_NUMBER(22, 10),

		/** 士兵-训练某种士兵多少个 */
		FUN_TRAIN_SODIER_TYPE_NUMBER(23, 11),

		/** 资源-产出率 */
		FUN_RESOURCE_RATE(30, 13),

		/** 世界采集某种资源达到多少 */
		FUN_RESOURCE_COLLECT_NUMBER(31, 14, true),

		/** 城内收取某种资源数量达到多少 */
		FUN_RESOURCE_TYPE_NUMBER(32, 15),

		/** 角色-等级 */
		FUN_PLAYER_LEVEL(40, 16),

		/** 角色-战力 */
		FUN_PLAYER_BATTLE_POINT(41, 17),

		/** 攻打野怪xx次 */
		FUN_ATTACK_MONSTER(50, 18, true),

		/** 攻打野怪胜利xx次 */
		FUN_ATTACK_MONSTER_WIN(51, 19, true),

		/** 攻击特殊野怪 */
		FUN_ATTACK_NEW_MONSTER(52, 37, true),
		
		/** 世界探索帝陵时长 */
		FUN_WORLD_EXPLORE_TIME(54, 35, true),

		/** 领取世界随机宝箱 */
		FUN_GET_WORLD_BOX(55, 36, true),

		/** PVP-战斗胜利 */
		FUN_PVP_WIN(60, 21),

		/** PVP-进攻玩家次数 */
		FUN_PVP_BATTLE(61, 22),

		/** PVE-击败尤里残部 (新手) */
		 FUN_NEWLY_BATTLE(62, 23),

		/** 防御战胜利次数 */
		FUN_CITYDEF_BATTLE_WIN(63, 24, true),

		/** 攻城胜利次数 */
		FUN_CITYATK_BATTLE_WIN(64, 25, true),

		/** 建造陷阱数量 */
		FUN_MAKE_TRAP(70, 26, true),

		/** 解锁区块数量 */
		FUN_UNLOCK_AREA(80, 27, true),

		/** 天赋加点 */
		 FUN_TALENT_UPLEVEL(90, 28),
		 
		/** 资源援助 */
		 FUN_LEAGUE_DEAL(110, 29),

		/** 联盟帮助 */
		 FUN_LEAGUE_HELP(111, 30),

		/** 治疗伤兵 */
		FUN_TREAT_ARMY(120, 31),

		/** 抽取英雄 */
		 FUN_RANDOM_HERO(130, 32),

		/** 登陆 */
		FUN_LOGIN(140, 33),

		/** 完成mission任务数量 */
		FUN_MISSION_ACCOMPLISH(150, 34),
		
		/** 攻击据点X次 */
		FUN_ATTACK_STRONGPOINT(160, 38, true),
		
		/** 攻占据点X次 */
		FUN_ATTACK_STRONGPOINT_WIN(161, 39, true),
		
		/** 驻守据点X秒 */
		FUN_OCCUPY_STRONGPOINT(162, 40, true)
		;

		/**
		 * 配置任务类型
		 */
		private int value;
		/**
		 * 日志记录任务类型
		 */
		private int logMissionType;
		/**
		 * 累计型标识：true表示累计型，false非累计型
		 */
		private boolean overlay = false;

		MissionFunType(int value, int logMissionType) {
			this.value = value;
			this.logMissionType = logMissionType;
		}
		
		MissionFunType(int value, int logMissionType, boolean overlay) {
			this.value = value;
			this.logMissionType = logMissionType;
			this.overlay = overlay;
		}
		
		@Override
		public int intValue() {
			return value;
		}

		/**
		 * 日志记录任务类型
		 * 
		 * @return
		 */
		public int intLogTypeVal() {
			return logMissionType;
		}

		/**
		 * 判断是否是累计型任务
		 * @param type
		 * @return
		 */
		public boolean isOverlay() {
			return overlay;
		}
		
		public static MissionFunType valueOf(int value) {
			return GsConst.valueOfEnum(MissionFunType.values(), value);
		}
	}

	/**
	 * 战争类型
	 * 
	 * @author lating
	 *
	 */
	public static class WarType {
		/**
		 * 攻击类型
		 */
		public static final int ATTACK = 0;
		/**
		 * 集结类型
		 */
		public static final int MASS = 1;
		/**
		 * 防御类型
		 */
		public static final int DEFENCE = 2;
	}

	/**
	 * 玩家权限标识
	 * 
	 * @author hawk
	 *
	 */
	public static class Privilege {
		public static final String BUILDING_FRONT = "building_front";
	}

	/**
	 * 世界同步的标记
	 * 
	 * @author hawk
	 *
	 */
	public static class WorldSyncFlag {
		// 世界点
		public static final int SYNC_WORLD = 0x0001;
		// 行军
		public static final int SYNC_MARCH = 0x0002;

		// 全部世界信息同步
		public static final int SYNC_ALL = SYNC_WORLD | SYNC_MARCH;
	}

	public static class ManorMarchShowType {
		/**
		 * 守军
		 */
		public static final int GARRISON = 1;

		/**
		 * 单人援助
		 */
		public static final int SINGLE = 2;

		/**
		 * 集结援助
		 */
		public static final int MASS = 3;

	}

	/** 禁言频道 */
	public static class SilentChannel {
		/**
		 * 全部
		 */
		public static final int ALL = 0;
		/**
		 * 世界（国家）
		 */
		public static final int WORLD = 1;
		/**
		 * 联盟
		 */
		public static final int GUILD = 2;
	}

	/** 周期活动奖励类型 */
	public static class RoundActivityRewardType {
		/**
		 * 积分目标奖励
		 */
		public static final int SCORE = 0;
		/**
		 * 阶段排名奖励
		 */
		public static final int STAGE_RANK = 1;
		/**
		 * 总排名奖励
		 */
		public static final int TOTAL_RANK = 2;
	}

	/** 世界行军通知类型 */
	public static enum MarchNotifyType {
		/**
		 * 行军出发
		 */
		MARCH_START,
		/**
		 * 行军到达
		 */
		MARCH_REACH,
		/**
		 * 迁城导致行军中断
		 */
		MARCH_BREAK,
		/**
		 * 行军到达时间变更
		 */
		MARCH_REACH_TIME_CHANGE,
		/**
		 * 被动接收
		 */
		MARCH_PASSIVE;
	}

	/**
	 * 短信监控的通知ID
	 *
	 */
	public static class SmsMsgId {
		public static final int LOGIN = 1;
		public static final int MARCH = 2;
		public static final int MOVE_CITY = 3;
	}

	public static class LanguageKeyPrefix {
		public static final String march = "march";
		public static final String pushTitle = "@PushTitle";
	}

	/**
	 * 可重用队列标识
	 */
	public enum QueueReusage {
		/**
		 * 不可重用队列
		 */
		NONE(-1),
		/**
		 * 空闲的可重用队列
		 */
		FREE(0),
		/**
		 * 建筑升级或改建
		 */
		BUILDING_OPERATION(1),
		/**
		 * 防御建筑升级或维修或改建
		 */
		DEFENCE_OPERATION(2),
		/**
		 * 科技升级
		 */
		TECH_UPGRADE(3),
		/**
		 * 训练士兵
		 */
		ARMY_TRAIN(4),
		/**
		 * 伤兵治疗
		 */
		SOLDIER_CURE(5),
		/**
		 * 英雄训练
		 */
		HERO_TRAIN(6),
		/**
		 * 英雄治疗
		 */
		HERO_CURE(7),
		/**
		 * 制造陷阱
		 */
		MAKE_TRAP(8),
		/**
		 * 装备队列
		 */
		EQUIP_QUEUE(9),
		/**
		 * 联盟关怀
		 */
		GUILD_HOSPICE_QUEUE(10),
		/**
		 * 开启迷雾要塞宝箱
		 */
		OPEN_FOGGY_BOX(11),
		/**尤里来犯净化*/
		YURISTRIKE_CLEAN(12),
		/**
		 * 超时空急救站冷却恢复
		 */
		FIRST_AIT_STAION_RECOVER(13),
		/**
		 * 远征科技研究
		 */
		CROSS_TECH_UPGRADE(14),
		/**
		 * 装备科技
		 */
		EQUIP_RESEARCH(15),
		/**
		 * 泰能进化
		 */
		PLANT_SOLDIER_ADVANCE(16),
		/**
		 * 泰能科技研究
		 */
		PLANT_SCIENCE_UPGRADE(17),
		
		/**
		 * 泰能兵治愈
		 */
		PLANT_SOLDIER_CURE(18),
		;
		

		int value;

		private QueueReusage(int value) {
			this.value = value;
		}

		public int intValue() {
			return value;
		}

	}

	private interface IntConst {
		int intValue();
	}

	// TODO 持续优化中
	private static <T extends IntConst> T valueOfEnum(T[] values, int value) {
		Optional<T> op = Arrays.stream(values)
				.filter(o -> o.intValue() == value).findAny();
		if (op.isPresent()) {
			return op.get();
		}
		throw new RuntimeException("Incorrect value : " + value);
	}

	/**
	 * 由于数据跟客户端混用，所以定义常亮key时记得带上GS前缀
	 * 
	 * @author zhjx
	 *
	 */
	public static class CustomerDataGSKey {
		/**
		 * 新手野怪
		 */
		public static final String NEWLY_POINT_CREATE = "NEWLY_POINT";
	}

	/**
	 * 调查问卷相关常量
	 * 
	 * @author admin
	 */
	public static class QuestionnaireConst {
		/**
		 * 条件类型-建筑等级
		 */
		public static final int CONDITION_BUILDING_LEVEL = 1;
		/**
		 * 条件类型-充值额度
		 */
		public static final int CONDITION_BUILDING_RECHARGE = 2;
		/**
		 * 条件类型-购买道具
		 */
		public static final int CONDITION_BUILDING_BUY_ITEM = 3;

		/**
		 * 推送类型-触发推送
		 */
		public static final int PUSHTYPE_TRIGGER = 1;
		/**
		 * 推送类型-后台推送
		 */
		public static final int PUSHTYPE_GM = 2;

		/**
		 * 推送位置-邮件
		 */
		public static final int PUSH_SHOW_MAIL = 1;
		/**
		 * 推送位置-主页
		 */
		public static final int PUSH_SHOW_PAGE = 2;
	}

	/**
	 * puid类型，是否区分平台
	 * 
	 * @author admin
	 *
	 */
	public static class AccountPuidType {
		/**
		 * 不区分平台
		 */
		public static final int PLATFORM_NONE = 0;
		/**
		 * 区分平台
		 */
		public static final int PLATFORM_DISTINCT = 1;

	}
	
	/**
	 * vip相关的时间类型
	 * @author lating
	 *
	 */
	public static enum VipRelatedDateType {
		/**
		 * vip商城刷新时间
		 */
		VIP_SHOP_REFRESH,
		/**
		 * vip福利礼包领取时间
		 */
		VIP_BENEFIT_TAKEN
	}

	/**
	 * 服务器日统计信息
	 *
	 */
	public static class DailyInfoField {
		/**
		 * 服务器日新增注册角色数
		 */
		public static final String DAY_REGISTER = "addReg";
		/**
		 * 服务器日活跃登陆数
		 */
		public static final String DAY_LOGIN = "active";
		/**
		 * 服务器日充值付费钻只数
		 */
		public static final String DAY_RECHARGE = "payDiamonds";
		/**
		 * 服务器日月卡付费钻石数
		 */
		public static final String DAY_MONTHCARD = "payMonthCard";
		/**
		 * 服务器日直购付费钻石数
		 */
		public static final String DAY_PAYITEM = "payItems";
		/**
		 * 服务器日充值人数
		 */
		public static final String DAY_RECHARGE_PLAYER = "payRoles";
	}

	/**
	 * idip封禁类型常量
	 */
	public enum IDIPBanType {
		BAN_ACCOUNT,   // 封禁帐号
		BAN_SEND_MSG,  // 禁言
		BAN_ZERO_EARNING,  // 零收益
		AREA_BAN_ACCOUNT,  // 全区全服封号
		AREA_BAN_SEND_MSG, // 全区全服禁言
		CARE_BAN_ACCOUNT,  // 全区全服封号（成长守护平台家长封禁）
		BAN_CREATE_ROLE,   // 禁止创建角色
		
		// 排行榜封禁常量在Rank.proto中已定义
		
		BAN_ADD_FRIEND,     // 添加好友
		BAN_ASK_DRESS,      // 索要装扮
		BAN_SEND_DRESS,     // 赠送装扮
		BAN_SEND_MAIL,      // 发送邮件
		BAN_GUILD_INVITE,   // 联盟邀请
		
		BAN_PLAYER_NAME,         // 个人昵称
		BAN_SIGNATURE,           // 个性签名
		BAN_GUILD_NAME,          // 联盟名字
		BAN_GUILD_NOTICE,        // 联盟公告
		BAN_GUILD_MANOR,         // 联盟堡垒名字
		BAN_GUILD_ANNOUNCE,      // 联盟宣言
		BAN_GUILD_SIGN,          // 联盟标记
		BAN_GUILD_LEVELNAME,     // 联盟阶级
		BAN_GUILD_TAG,           // 联盟简称
		BAN_KING_NOTICE,         // 国王公告
		BAN_CYBOR_TEAM_NAME,     // 赛博队伍名
		BAN_TIBERIUM_TEAM_NAME,  // 泰伯队伍名
		BAN_CHAT_ROOM_NAME,      // 群聊名
		BAN_FRIEND_TXT,          // 好友备注
		BAN_ENERGY_MATRIX,       // 能量矩阵
		BAN_MARCH_PRSET_NAME,    // 部队编队
		BAN_EQUIP_MARSHALLING,   // 装备编组
		BAN_WORLD_FAVORITE,      // 世界收藏
		BAN_CHANGE_IMAGE,        // 禁止修改玩家头像
		;
	}

	/**
	 * 天赋技能可使用状态
	 */
	public static class TalentSkill {
		public static final int CAN_USE = 0;
		public static final int IN_CD = 1;
	}

	public interface TravelShopConstant {
		/**一组刷新多少数量*/
		int GROUP_ITEM_NUM = 1;
		/** 攻击yuri */
		int TRAVEL_SHOP_REDUCE_TYPE_YURI = 1;
		/** 建造 */
		int TRAVEL_SHOP_REDUCE_TYPE_BUILD = 2;
		/** 训练士兵 */
		int TRAVEL_SHOP_REDUCE_TYPE_ARMY = 3;
		/** 这里用了100 */
		int RATE_BASE = 100;
		/**初始化的时候*/
		int STATE_UNKNOW = 0;
		/**回来*/
		int STATE_OPEN = 1;
		/**离开*/
		int STATE_LEAVE = 2;
		/**普通池*/
		int POOL_TYPE_NORMAL = 0;
		/**vip池*/
		int POOL_TYPE_VIP = 1; 
	}

	/**
	 * 新手初始化数据类型
	 * 
	 * @author golden
	 *
	 */
	public static class NewlyType {
		/** 新手建筑 */
		public static final int NEWLY_BUILDING = 1;
		/** 新手奖励 */
		public static final int NEWLY_REWARD = 2;
		/** 新手士兵 */
		public static final int NEWLY_SODLIER = 3;
		/** 新手英雄 */
		public static final int NEWLY_HERO = 4;
		/** 新手消耗 */
		public static final int NEWLY_CONSUME = 5;
	}

	/**
	 * 世界任务类型
	 */
	public static class WorldTaskType {
		/** 玩家登陆初始化城点 */
		public static final int MOVE_CITY = 1;
		/** 同步城防信息 */
		public static final int SYNC_CITY_DEF = 2;
		/** 玩家主动迁城 */
		public static final int PLAYER_MOVE_CITY = 3;
		/** 放置联盟堡垒 */
		public static final int CREATE_GUILD_MANOR = 4;
		/** 放置联盟建筑 */
		public static final int CREATE_GUILD_BUILD = 5;
		/** 移除联盟建筑 */
		public static final int REMOVE_GUILD_BUILD = 6;
		/** 使用技能 */
		public static final int USE_SKILL = 7;
		/**活动开启*/
		public static final int ACTIVITY_OPEN = 8;
		/**活动结束*/
		public static final int ACTIVITY_CLOSE = 9;
		/** 重置账号时移除世界点 */
		public static final int ACCOUNT_RESET_REMOVE_CITY = 10;
		/** 清理城点 */
		public static final int CLEAN_CITY = 11;
		/** 清除领地内资源点 */
		public static final int CLEAR_RESOURCE = 12;
		/** 重置清除领地内资源点次数 */
		public static final int RESET_CLEAR_RESOURCE_NUM = 13;
		/** 外界强制行军回城 */
		public static final int MARCH_BACK_FORCED = 14;
		/** 迁出玩家  */
		public static final int MIGRATE_OUT_PLAYER = 15;
		/** 生成怪物点   */
		public static final int WORLD_MONSTER_POINT_GENERATE = 16;
		/** 世界行军加速  */
		public static final int WORLD_MARCH_SPEED = 17;
		/** 世界行军召回 */
		public static final int WORLD_MARCH_CALLBACK = 18;
		/** 世界行军召回-针对点的 */
		public static final int WORLD_MARCH_POINT_CALLBACK = 19;
		/** 新版野怪刷新*/
		public static final int WORLD_NEW_MONSTER_REFRESH = 20;
		/** 活动据点刷新 */
		public static final int WORLD_STRONGPOINT_REFRESH = 21;
		/** 老版野怪刷新*/
		public static final int WORLD_Old_MONSTER_REFRESH = 22;
		/** 新版野怪删除*/
		public static final int WORLD_NEW_MONSTER_REMOVE = 23;
		/** 开启尤里复仇活动 */
		public static final int OPEN_YURI_REVENGE = 24;
		/** 任命队长 */
		public static final int CHANGE_QUARTER_LEADER = 25;
		/** 区域更新 */
		public static final int AREA_UPDATE = 26;
		/** 原地高迁  */
		public static final int MOVE_CITY_IN_PLACE = 27;
		/** 野怪刷新 */
		public static final int REFRESH_MONSTER = 28;
		/** 资源点刷新*/
		public static final int RESOURCE_REFRESH = 29;
		/** 据点刷新*/
		public static final int STRONGPOINT_REFRESH = 30;
		/** 遣返行军*/
		public static final int REPATRIATE_MARCH = 31;
		/** 世界机甲刷新*/
		public static final int GUNDAM_REFRESH = 32;
		/** 部队修复*/
		public static final int ARMY_FIX = 33;
		/** 跨服*/
		public static final int CROSS_SERVER = 34;
		/**跨服结束清除城点*/
		public static final int CROSS_END_REMOVE_CITY = 35;
		/** 年兽刷新*/
		public static final int NIAN_REFRESH = 36;
		/** 寻宝野怪   */
		public static final int TREASURE_HUNT_MONSTER = 37;
		/** 寻宝资源  */
		public static final int TREASURE_HUNT_RESOURECE = 38;
		/** 放置战地旗帜   */
		public static final int PLACE_WAR_FLAG = 39;
		/** 检测战地旗帜数量   */
		public static final int CHECK_WAR_FLAG_COUNT = 40;
		/**关服为了合服,删除行军.*/
		public static final int CLOSE_FOR_MERGE_SERVER = 41;
		/** 解散联盟移除战旗   */
		public static final int REMOVE_GUILD_WAR_FLAG = 42;
		/** 保存战旗资源   */
		public static final int SAVE_FLAG_RESOURCE = 44;
		/** 收取战旗资源   */
		public static final int COLLECT_FLAG_RESOURCE = 45;
		/** 资源宝库清理   */
		public static final int RES_TREASURE_DEL = 46;
		/** 刷新机器人   */
		public static final int REFRESH_ROBOT = 47;
		/** 刷新能量塔   */
		public static final int RESOURCE_PYLON = 48;
		/**
		 * 圣诞大战刷新.
		 */
		public static final int CHRISTMAS_REFRESH = 49;
		/** 刷新雪球  */
		public static final int REFRESH_SNOWBALL = 50;
		
		/** 母旗操作*/
		public static final int CENTER_FLAG_ACTION = 51;
		/** 幽灵工厂怪点生成*/
		public static final int GHOST_TOWER_MONSTER_POINT_GENERATE = 52;
		/** 幽灵工厂怪点删除*/
		public static final int GHOST_TOWER_MONSTER_DEL = 53;
		/** 小站区刷新*/
		public static final int XZQ_POINT_UPDATE = 54;

		/** 国家建筑升级 */
		public static final int NATIONAL_BUILDING_UPGRADE = 55;
		/** 国家重建捐献 */
		public static final int NATIONAL_REBUILD_DONATE = 56;

		/** 国家仓库捐献 */
		public static final int NATIONAL_STOREHOUSE_DONATE = 57;
		
		/** 清除当日建设值上限 */
		public static final int NATIONAL_BUILDING_RESET = 58;
		
		/** 国家飞船制造厂部件升级 */
		public static final int NATIONAL_SHIP_UPGRADE = 59;
		
		/** 国家飞船制造厂部件取消升级 */
		public static final int NATIONAL_SHIP_CANCEL_UPGRADE = 60;
		
		/** 使用国家建设值道具 */
		public static final int NATIONAL_BUILD_USE_ITEM = 61;
		
		/** 生成资源狂欢宝箱*/
		public static final int RESOURCE_SPREE_BOX_GEN = 62;
		/** 删除资源狂欢宝箱*/
		public static final int RESOURCE_SPREE_BOX_DELETE = 63;
		
		/** 航海远征生成资源宝箱*/
		public static final int CROSS_ACTIVITY_BOX = 64;
		
		/** 放置联盟机甲舱体  */
		public static final int PLACE_GUILD_SPACE = 65;
		
		/**选择舱体等级  */
		public static final int SPACE_MECHA_LV_SELECT = 66;
		
		/** 星甲召唤期间退出联盟  */
		public static final int SPACE_MECHA_LEAVE_GUILD = 67;
		/** 任命队长 */
		public static final int CHANGE_SPACE_MECHA_LEADER = 68;
		/** 联盟编队 */
		public static final int GUILD_FORMATION = 69;
	}
	
	/**
	 * 关系类型
	 * 
	 *
	 */
	public static class RelationType {
		public static final int FRIEND = 1 ;//好友
		public static final int BLACKLIST = 2; //黑名单 
	}
	
	public static class GuardType {
		public static final int INVITE = 1;//申请
		public static final int CREATE = 2; //建立
		public static final int DELETE = 3; //删除守护关系.
	}
	
	/**
	 * 好友 文字介绍的最大长度
	 */
	public static final int RELATION_MAX_REQEUST_CONTENT = 28;
	
	
	public static class FightMailAnalysis {
		
		public static final int MASS_ATK_WIN_ATK = 1 ;//集结进攻胜利-进攻方
		public static final int MASS_ATK_WIN_DEF = 102; //集结进攻胜利-防守方
		
		public static final int MASS_ATK_FAIL_ATK = 101 ;//集结进攻失败-进攻方
		public static final int MASS_ATK_FAIL_DEF = 2; //集结进攻失败-防守方
		
		public static final int AISS_ATK_WIN_ATK = 3 ;//援助时进攻胜利-进攻方
		public static final int AISS_ATK_WIN_DEF = 104; //援助时进攻胜利-防守方
		public static final int AISS_ATK_FAIL_ATK = 103 ;//援助时进攻失败-进攻方
		public static final int AISS_ATK_FAIL_DEF = 4; //援助时进攻失败-防守方
		
		public static final int GREAT_WIN = 5 ; //大胜
		public static final int GREAT_FAIL = 105 ; //大败
		
		public static final int LITTLE_WIN = 6 ; //险胜
		public static final int LITTLE_FAIL = 106 ; //险败
		
		public static final int SOLDIER_LESS_WIN = 7 ; //兵种单一胜利
		public static final int SOLDIER_LESS_FAIL = 107 ; //兵种单一失败
		
		public static final int TANK_SOLDIER_LESS_WIN = 8 ; //装甲坦克少一胜利
		public static final int TANK_SOLDIER_LESS_FAIL = 108 ; //装甲坦克少一失败
		
		public static final int KILL_COUNT_SOLDIER_WIN = 9 ; //士兵消灭最多一胜利
		public static final int KILL_COUNT_TANK_WIN = 10 ; //坦克消灭最多一胜利
		public static final int KILL_COUNT_FLY_WIN = 11 ; //飞机消灭最多一胜利
		public static final int KILL_COUNT_CANOON_WIN = 12 ; //重武器消灭最多一胜利
		
		public static final int KILL_COUNT_SOLDIER_FAIL = 109 ; //士兵消灭最多一失败
		public static final int KILL_COUNT_TANK_FAIL = 110 ; //坦克消灭最多一失败
		public static final int KILL_COUNT_FLY_FAIL = 111 ; //飞机消灭最多一失败
		public static final int KILL_COUNT_CANOON_FAIL = 112 ; //重武器消灭最多一失败
	}
	
	/**
	 * 模块控制
	 * @author golden
	 *
	 */
	public static enum ControlerModule {
		MAIL_SEND(1),        // 邮件发送
		RECHARGE_PAY(2),     // 支付（充值礼包）
		RECHARGE_SHOP(3),    // 道具直购（每日特惠礼包）
		PREMIUM_GIFT(4),     // 超值礼包
		VIP_SHOP(5),         // vip商城
		VIP_GIFT(6),         // vip礼包
		SALES_GOODS(7),      // 热销商品
		MAIL_REWARD_RECV(8), // 邮件奖励获取
		WORLD_CHAT(9),       // 世界聊天
		GUILD_CHAT(10),      // 联盟聊天
		INDEPENDENT_ARMS(11), // 独立军火商（黑市商人）
		ALLIED_DEPOT(12),    // 盟军补给站（军需处）
		DAILY_TASK(13),      // 每日任务（酒馆）
		ITEM_USE(14),        // 道具使用
		ITEM_GET(15),        // 道具获得
		DIAMOND_USE(16),     // 使用钻石
		GOLD_USE(17),        // 使用水晶
		SKILL_USE(18),       // 使用技能
		ACTIVITY(19),        // 活动
		GRABRESENABLE(20),   // 掠夺资源
		GATHERRESINCITY(21), // 城内收取资源
		PUSH_GIFT(22),		   // 推送礼包
		GUARD(23),			  //守护模块。
		;
		
		int value;
		
		ControlerModule(int val) {
			this.value = val;
		}
		
		public int value() {
			return this.value;
		}
	}
	
	/**
	 * 行军处理标记
	 * @author zhenyu.shang
	 * @since 2017年11月23日
	 */
	public static class MarchProcMask{
		
		/** 行军回程处理中 */
		public static final int RETURN_PROC = 0x01;
		
		/** 部队回家处理中 */
		public static final int ARMY_PROC = 0x02;
		
		/** 奖励发放处理中 */
		public static final int AWARD_PROC = 0x04;
		
		/** 行军是否到达目标点标识位 */
		public static final int IS_MARCHREACH = 0x08;
	}
	
	/**
	 * 定时事件
	 * @author jm
	 * clock 执行时间, 暂时只支持小时
	 * 玩家事件
	 * {@link PlayerTimerModule  }
	 * 
	 * 全服事件
	 * 
	 *
	 */
	public static enum TimerEventEnum {
		/**
		 * 0点触发
		 */
		ZERO_CLOCK(0, true),
		/**
		 * 3点钟
		 */
		THREEE_CLOCK(3, false),
		/**
		 * 4点
		 */
		FOUR_CLOCK(4, false),
		/**
		 * 五点钟事件
		 */
		FIVE_CLOCK(5, true);		
		/**
		 * 时间
		 */
		private int clock;
		/**
		 * 是否需要通知玩家, 默认会给GlobalTimeService 发送事件通知, 为true则会往GlobalTimeService发送事件通知和往玩家身上发通知
		 */
		private boolean noticePlayer;
		
		public int getClock() {
			return clock;
		}
		
		private TimerEventEnum(int clock, boolean noticePlayer){
			this.clock = clock;
			this.noticePlayer = noticePlayer;
		}

		public boolean isNoticePlayer() {
			return noticePlayer;
		}
	}
	
	/**
	 * 国王箭塔坐标
	 * @author golden
	 *
	 */
	public static enum PresidentTowerPointId  implements IntConst {
		ONE(GameUtil.combineXAndY(380,751)),
		TWO(GameUtil.combineXAndY(389,760)),
		THREE(GameUtil.combineXAndY(380, 769)),
		FOUR(GameUtil.combineXAndY(371,760));
		
		private int pointId;
		
		public int getPointId() {
			return pointId;
		}
		
		private PresidentTowerPointId(int pointId){
			this.pointId = pointId;
		}
			
		public static PresidentTowerPointId valueOf(int pointId) {
			PresidentTowerPointId point = null;
			
			try {
				point = GsConst.valueOfEnum(PresidentTowerPointId.values(), pointId);
			} catch (RuntimeException e) {
				return null;
			}
			
			return point;
		}

		@Override
		public int intValue() {
			return pointId;
		}
	}
	
	public static class PushGiftConst {
		public static int TRIGGER_TYPE_SINGLE = 1;//单个
		public static int TRIGGER_TYPE_MULTI = 2; //多个.
		/**
		 * 数据是有效的.
		 */
		public static int SALE = 1;
	}
	
	/**
	 * 超级武器等级
	 * @author zhenyu.shang
	 * @since 2018年4月23日
	 */
	public static class SuperWeaponLevel{
		public static final int SENIOR = 0;  // 高级
		public static final int MIDDLE  = 1;  // 中级
	}
	
	/**
	 * uic敏感词检测结果标识
	 */
	public static class UicMsgResultFlag {
		public static final int LEGAL     = 0;  // 合法
		public static final int ILLEGAL   = 1;  // 不合法，不能显示（恶意文本，其他人不可见）
		public static final int SENSITIVE = 2;  // 合法，但包含敏感词（其他人只能看见***）
	}
	
	/**
	 * uic消息内容类别
	 */
	public static class UicMsgCatagory {
		public static final int MAIL   = 1;  // 邮件
		public static final int CHAT   = 2;  // 聊天
	}
	
	public static class GiftConst {
		/**
		 * 普通
		 */
		public static final int POOL_TYPE_NORMAL = 1;
		/**
		 * 特殊
		 */
		public static final int POOL_TYPE_SPECIAL = 2;
		/**
		 * 荣耀同享
		 */
		public static final int POOL_SHARE_GLORY_GIFT = 3;
		
		/**
		 * 系常规
		 */
		public static final int POOL_ACTIVATE_TYPE_NORMAL = 0;
		/**
		 * 条件开启
		 */
		public static final int POOL_ACTIVATE_TYPE_CONDITION = 1;
		/**
		 * 后置礼包
		 */
		public static final int GROUP_TYPE_POST = 0;
		/**
		 * 服务器开服时间
		 */
		public static final int UNLOCK_SERVER_OPEN = 1;
		/**
		 * 玩家注册时间
		 */
		public static final int UNLOCK_PLAYER_REGISTER = 2;
		/**
		 * 指挥官等级
		 */
		public static final int UNLOCK_PLAYER_COMMANDER_LEVEL = 3;
		/**
		 * 解锁建筑等级
		 */
		public static final int UNLOCK_PLAYER_BUILDING_LEVEL = 4;
		/**
		 * 自然周时间
		 */
		public static final int UNLOCK_WEEK = 5;
		/**
		 * 国王战
		 */
		public static final int UNLOCK_PRESIDENT = 6;
		/**
		 * 尤里活动
		 */
		public static final int UNLOCK_YURI = 7;
		/**
		 * 最强指挥官阶段
		 */
		public static final int UNLOCK_STRONGEST_STAGE = 8;
		/**
		 * 自然时间段
		 */
		public static final int UNLOCK_NATURE_TIME = 9;
		/**
		 * 平台 安卓，ios
		 */
		public static final int UNLOCK_PLATFORM = 10;
		/**
		 * 大区
		 */
		public static final int UNLOCK_AREA = 11;
		/**
		 * 跨服活动.
		 */
		public static final int UNLOCK_CROSS_ACTIVITY = 12;
		/**
		 * 王者联盟
		 */
		public static final int UNLOCK_KING_GUILD = 13;
		/**
		 * 通用活动开启.
		 */
		public static final int UNLOCK_COMMON_ACTIVITY = 14;
		/**
		 * 月球之战礼包
		 */
		public static final int YQZZ = 15;
		/**
		 * 荣耀同享
		 */
		public static final int SHARE_GLORY_GIFT = 16;
		/**
		 * 不限制
		 */
		public static final int LIMIT_NONE = 0;
		/**
		 * 每日限制类型
		 */
		public static final int LIMIT_DAILY = 1;
		/**
		 * 永久
		 */
		public static final int LIMIT_PERPETUAL = 2;
		/**
		 * 每月限制类型
		 */
		public static final int LIMIT_MONTH = 3;
		/**
		 * 每周限制类型
		 */
		public static final int LIMIT_WEEK = 4;
		/**
		 * 逻辑或
		 */
		public static final int LOGIC_OR = 1;
		/**
		 * 逻辑与
		 */
		public static final int LOGIC_AND = 2;
	}
	
	/**
	 * 功能模块控制id
	 * @author golden
	 *
	 */
	public static class SysFunctionModuleId {
		// 任务
		public static final int MISSION = 101;
		// 每日任务
		public static final int DAILYMISSION = 102;
		// 军衔任务
		public static final int BATTLEMISSION = 103;
		// 切世界
		public static final int WORLD = 104;
		// 总览
		public static final int ALL = 105;
		// 指挥官天赋
		public static final int TALENT = 106;
		// 防空洞
		public static final int HOLD = 107;
		// 救援物资
		public static final int HELPAWARD = 108;
		// 天赋切换
		public static final int TANLENTCHANGE = 109;
		// 英雄特训
		public static final int HEROSPICALTRAIN = 110;
		// 装备切换
		public static final int EQUIPCHANGE = 111;
		// 野怪多倍攻击
		public static final int MONSTERATKMORE = 112;
		// 联盟礼物一键领取
		public static final int GUILDAWARD = 113;
	}
	
	/**
	 * 功能模块控制条件类型
	 * @author golden
	 *
	 */
	public static class SysFunctionContionType {
		public static final int BUILDLVL = 1;
		public static final int PLAYERLVL = 2;
		public static final int STORYMISSIONLVL = 3;
		public static final int MILITARYRANK = 4;
	}
	
	/**
	 * 新版野怪刷新状态
	 * @author golden
	 *
	 */
	public static enum NewMonsterRefreshState {
		DEFAULT(0),
		/**
		 * 进行中
		 */
		PROGRESS(1),
		/**
		 * 已完成
		 */
		FINISH(2)
		;
		
		int value;

		private NewMonsterRefreshState(int value) {
			this.value = value;
		}

		public int intValue() {
			return value;
		}
	}
	
	/**
	 * 超级武器奖励类型
	 * @author golden
	 * 
	 */
	public static enum SuperWeaponAwardType implements IntConst {
		/**
		 * 攻占奖励-攻占者奖励
		 */
		ATTACK_MEMBER_AWARD(1),
		/**
		 * 攻占奖励-联盟全员奖励
		 */
		ATTACK_GUILD_MEMBER_AWARD(2),
		/**
		 * 攻占奖励-盟主分配奖励
		 */
		ATTACK_LEADER_SEND_AWARD(3),
		
		/**
		 * 驻守奖励-联盟全员奖励
		 */
		OCCUPY_GUILD_MEMEBER_AWARD(6),
		/**
		 * 驻守奖励-盟主分配奖励
		 */
		OCCUPY_LEADER_SEND_AWARD(7),
		
		/**
		 * 控制奖励-联盟全员奖励
		 */
		CONTROL_GUILD_MEMEBER_AWARD(4),
		/**
		 * 控制奖励-盟主分配奖励
		 */
		CONTROL_LEADER_SEND_AWARD(5),
		;
		
		int type;
		
		private SuperWeaponAwardType(int type) {
			this.type = type;
		}
		
		public static SuperWeaponAwardType valueOf(int type) {
			SuperWeaponAwardType weaponType = null;
			try {
				weaponType = GsConst.valueOfEnum(SuperWeaponAwardType.values(), type);
			} catch (RuntimeException e) {
				return null;
			}
			return weaponType;
		}

		@Override
		public int intValue() {
			return type;
		}
	}
	
	
	/**
	 * 小战区奖励类型
	 * @author golden
	 * 
	 */
	public static enum XZQAwardType implements IntConst {
		/**
		 * 攻占奖励-攻占者奖励
		 */
		ATTACK_MEMBER_AWARD(1),
		/**
		 * 攻占奖励-联盟全员奖励
		 */
		ATTACK_GUILD_MEMBER_AWARD(2),
		/**
		 * 攻占奖励-盟主分配奖励
		 */
		ATTACK_LEADER_SEND_AWARD(3),
		
		/**
		 * 驻守奖励-联盟全员奖励
		 */
		OCCUPY_GUILD_MEMEBER_AWARD(6),
		/**
		 * 驻守奖励-盟主分配奖励
		 */
		OCCUPY_LEADER_SEND_AWARD(7),
		
		/**
		 * 控制奖励-联盟全员奖励
		 */
		CONTROL_GUILD_MEMEBER_AWARD(4),
		/**
		 * 控制奖励-盟主分配奖励
		 */
		CONTROL_LEADER_SEND_AWARD(5),
		;
		
		int type;
		
		private XZQAwardType(int type) {
			this.type = type;
		}
		
		public static XZQAwardType valueOf(int type) {
			XZQAwardType weaponType = null;
			try {
				weaponType = GsConst.valueOfEnum(XZQAwardType.values(), type);
			} catch (RuntimeException e) {
				return null;
			}
			return weaponType;
		}

		@Override
		public int intValue() {
			return type;
		}
	}
	
	
	
	/**
	 * 大雅之战奖励类型
	 * @author golden
	 * 
	 */
	public static enum DYZZAwardType implements IntConst {
		/**
		 * 没有奖励
		 */
		AWARD_LIMIT(100),
		/**
		 * 胜利者奖励
		 */
		WIN_AWARD(1),
		
		/**
		 * MVP奖励
		 */
		MVP_AWARD(2),
		
		/**
		 * 失败者奖励
		 */
		LOSS_AWARD(3),
		
		/**
		 * SMVP奖励
		 */
		SMVP_AWARD(4),
	
		/**
		 * 消极战斗奖励
		 */
		NEGATIVE_AWARD(5),
		
		;
		
		int type;
		
		private DYZZAwardType(int type) {
			this.type = type;
		}
		
		public static DYZZAwardType valueOf(int type) {
			DYZZAwardType weaponType = null;
			try {
				weaponType = GsConst.valueOfEnum(DYZZAwardType.values(), type);
			} catch (RuntimeException e) {
				return null;
			}
			return weaponType;
		}

		@Override
		public int intValue() {
			return type;
		}
	}
	
	
	/**
	 * 积分上报基础公共信息
	 */
	public static final ScoreType[] COMMON_SCORE_TYPE = {ScoreType.PLAT, ScoreType.AREA, ScoreType.SERVER, ScoreType.PLAYER_ID, ScoreType.PLAYER_NAME};
	
	/**
	 * 积分上报类型 
	 */
	public static enum ScoreType implements IntConst {
		PLAYER_LEVEL(1),        // 等级
		GOLD(2),                // 金币（黄金）
		CITY_LEVEL(3),          // 王城等级
		LATEST_LOGIN_TIME(8),   // 最近登录时间
		PLAT(12),               // 平台类型
		POWER(17),              // 战力
		REGISTER_TIME(25),      // 注册时间
		AREA(26),               // 大区
		SERVER(27),             // 服务器
		PLAYER_ID(28),          // 角色id
		PLAYER_NAME(29),        // 角色名称
		TOTAL_RECHARGE_AMOUNT(43),  // 累计充值金额
		RECHARGE_AMOUNT(44),    // 单次充值金额
		VIP_LEVEL(45),          // vip等级
		RECHARGE_TIME(46),      // 充值时间
		NICK_NAME(47),          // 游戏昵称
		CHANNEL(201),           // 渠道号
		REG_CHANNEL(202),       // 注册渠道号
		
		GUILD_ID(30),           // 公会ID
		GUILD_NAME(301),        // 公会名称
		GUILD_CREATE(306),        // 公会创建时间
		GUILD_DISSOLVE(307),      // 公会解散时间
		GUILD_MEMBER_COUNT(308),  // 公会成员人数
		GUILD_MEMBER_CHANGE(309), // 公会成员变动（1-加入，2-退出）
		GUILD_MEMBER_LEVEL_CHANGE(311),  // 公会成员身份变化
		
		KILL_ENEMY(1001),        // 消灭敌军数量
		PLAYER_POWER_RANK(1002), // 个人战力排名
		GUILD_KILL_RANK(1003),   // 联盟击杀排名
		GUILD_POWER_RANK(1004),  // 联盟战力排名
		DEFNDER_POWER_LOSE(1005, 3000), // 被攻击战力损失大于等于30%
		
		DAIY_GAME_TIME(6000)     // 当天累计游戏时长
		;

		int type;
		int param;
		int bcover;   // 与排行榜有关的数据bcover=0，其他bcover=1。游戏中心排行榜与游戏排行榜保持一致;
		String expires;  // unix时间戳，单位s，表示哪个时间点数据过期，0时标识永不超时
		
		private ScoreType(int type, int value) {
			this.type = type;
			this.bcover = 1;   
			this.expires = "0";
			this.param = value;
		}
		
		private ScoreType(int type, int bcover, String expires) {
			this.type = type;
			this.bcover = bcover;
			this.expires = expires;
			this.param = 0;
		}
		
		private ScoreType(int type) {
			this.type = type;
			this.bcover = 1;   
			this.expires = "0";
			this.param = 0;
		}
		
		@Override
		public int intValue() {
			return type;
		}
		
		public int paramVal() {
			return param;
		}
		
		public int bcoverVal() {
			return this.bcover;
		}
		
		public String expiresVal() {
			return this.expires;
		}
		
		public static ScoreType valueOf(int value) {
			return GsConst.valueOfEnum(ScoreType.values(), value);
		}
	}
	
	/**
	 * 等待登录状态
	 */
	public static enum WaitLoginState {
		NONE,         // 无状态
		WAIT_LOGIN,   // 排队等待中
		ALLOW_LOGIN;  // 准备登录
	}
	
	/***
	 * 作用号类型
	 * @author yang.rao
	 *
	 */
	public static enum EffectType{
		IMAGE_ITEM(100),
		HERO_SKIN(200);
		
		int value;
		private EffectType(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
	
	public static class GuildFavourite {
		/**
		 * 联盟领地
		 */
		public static final int TYPE_GUILD_MANOR = 1;
		/**
		 * 联盟成员
		 */
		public static final int TYPE_GUILD_MEMBER = 2;
		/**
		 * 联盟标记
		 */
		public static final int TYPE_GUILD_SIGN = 3;
		/**
		 * 联盟建筑
		 */
		public static final int TYPE_GUILD_BUILDING = 4;
		/**
		 *工会收藏
		 */
		public static final int TAG_GUILD = 4;
	}
	
	public static enum GuildOffice{
		/**
		 * 无官职
		 */
		NONE(0),
		/**
		 * 盟主
		 */
		LEADER(1),
		/**
		 * 官员A
		 */
		OFFICE_A(2),
		/**
		 * 官员B
		 */
		OFFICE_B(3),
		/**
		 * 官员C
		 */
		OFFICE_C(4),
		/**
		 * 官员D
		 */
		OFFICE_D(5),
		;
		
		int value;
		
		GuildOffice(int val) {
			this.value = val;
		}
		
		public int value() {
			return this.value;
		}
	}
	
	
	public enum GuildActiveType {
		NONE, //非死盟
		DEADA, //A类死盟
		DEADB, //B类死盟
		DEADAB; //又是A又是B
	}
	
	/**
	 * 自动搜索野怪结果 
	 */
	public static class AutoSearchMonsterResultCode {
		public static final int KEEPTRYING    = -1;// 继续搜索野怪
		public static final int SUCCESS       = 0; // 成功发起行军
		public static final int SEARCH_BREAK  = 1; // 搜索失败中断搜索，关闭自动打野
		public static final int VIT_BREAK     = 2; // 体力不足中断搜索，关闭自动打野
		public static final int ARMY_BREAK    = 3; // 编队没有空闲兵中断搜索，关闭自动打野
		public static final int PVE_BREAK     = 4; // 打野失败中断搜索，关闭自动打野
		public static final int BUFF_END_BREAK = 5; // 自动打野作用号结束中断搜索，关闭自动打野
	}
	
	/**
	 * 限时商店触发类型
	 * 
	 */
	public static class TimeLimitStoreTriggerType {
		public static final int SOLDIER_TRAIN          = 1;  //指定时间内造兵超过多少
		public static final int BUILDING_SPEED         = 2; //指定时间内使用建造加速多少
		public static final int RESOURCE_CHANGE        = 3; //指定时间内资源变化多少
		public static final int ATTACK_MONSTER         = 4; //指定时间内打野次数
		public static final int TRVEL_SHOP_BUY         = 5; //特惠商店指定时间内商品购买次数
		public static final int MILITARY_SUPPLIES      = 6; //军需补给次数
		public static final int CAPTURE_GHOST_BARRACKS = 7; //幽灵兵营占领次数
		public static final int ATTACK_GHOST_BASE      = 8; //幽灵基地攻打次数
	}
	
	/**
	 * 全服控制参数
	 */
	public static enum GlobalControlType {
		CHANGE_NAME(1),       // 修改玩家名字
		CHANGE_ICON(2),       // 修改玩家头像
		CHANGE_GUILD_NAME(3),  // 修改联盟名称
		CHANGE_GUILD_MANOR_NAME(4), // 修改联盟堡垒名称
		CHANGE_GUILD_ANNOUNCE(5);   // 修改联盟宣言
		
		int value;
		
		private GlobalControlType(int value) {
			this.value = value;
		}
		
		public int intVal() {
			return value;
		}
	}
	
	/**
	 * 
	 * 内容修改类型
	 *
	 */
	public static enum ChangeContentType {
		CHANGE_ROLE_NAME(1),       // 修改角色名称
		CHANGE_SIGNATURE(2),       // 修改基地签名
		CHANGE_GUILD_NAME(3),  // 修改联盟名称
		CHANGE_GUILD_MANOR_NAME(4), // 修改联盟堡垒名称
		CHANGE_GUILD_ANNOUNCE(5);   // 修改联盟宣言
		
		int value;
		
		private ChangeContentType(int value) {
			this.value = value;
		}
		
		public int intVal() {
			return value;
		}
	}
	
	/**
	 * 星球大战.
	 * @author jm
	 *
	 */
	public static class StarWarsConst {
		/**
		 * 世界分区
		 */
		public static final int WORLD_PART = 0;
		/**
		 * 特殊的分组.
		 */
		public static final int TEAM_NONE = 0;
		/**
		 * 16
		 */
		public static final int FIRST_LEVEL = 1;
		/**
		 * 4
		 */
		public static final int SECOND_LEVEL = 2;
		/**
		 * 世界霸主
		 */
		public static final int THIRD_LEVEL = 3;
		/**
		 * 官员所在区服合服
		 */
		public static final int UNSET_OFFICER_COMMON = 1;
		/**
		 * 赛区统治者合服
		 */
		public static final int UNSET_OFFICER_PART_KING = 8;
		/**
		 * 世界统治者合服.
		 */
		public static final int UNSET_OFFICER_WORLD_KING = 32;
		
		/**
		 * 已经处理过了
		 */
		public static final int UNSET_OFFICER_ALREADY_HANDLED = 1024;
		/**
		 * 是否已经修改了国王了
		 */		 
		public static final int UPDATED = 1;
	}
	
	/**
	 * 攻防模拟战.
	 * @author jm
	 *
	 */
	public static class SimulateWarConst {
		public static final int SIGN_UP = 1;//报名
		public static final int DISSOLVE = 2;//解散
		
	}
	
	public static class ChristmasConst {
		public static final int GET_BOX_NORMAL = 1; //直接获得
		public static final int GET_BOX_OCCUY = 2; //占领获得
		public static final int GET_BOX_WAR = 3; //战斗获得。
		public static final int BOX_WAR = 4; //宝箱上发生的战斗.
	}
	
	/**
	 * 每日限定使用次数，跨天重置的场景类型
	 * 
	 */
	public static enum DailyResetUseTimesType {
		MARCH_EMOTION,  // 每日免费使用行军表情（限定次数由玩家的vip等级决定）
	}
	
	/**
	 * 装备属性改变原因
	 */
	public static class ArmourChangeReason {
		public static final int ARMOUR_CHANGE_1 = 1; // 突破
		public static final int ARMOUR_CHANGE_2 = 2; // 强化
		public static final int ARMOUR_CHANGE_3 = 3; // 传承
		public static final int ARMOUR_CHANGE_4 = 4; // 穿戴
		public static final int ARMOUR_CHANGE_5 = 5; // 卸下
		public static final int ARMOUR_CHANGE_6 = 6; // 删除
		public static final int ARMOUR_CHANGE_7 = 7; // 装备工匠传承
		public static final int ARMOUR_CHANGE_8 = 8; // 升星
		public static final int ARMOUR_CHANGE_9 = 9; // 星级属性激活
		public static final int ARMOUR_CHANGE_10 = 10; // 星级属性充能
		public static final int ARMOUR_CHANGE_11 = 11; // 星级属性充能十次
		public static final int ARMOUR_CHANGE_12 = 12; // 星级属性随机
		public static final int ARMOUR_CHANGE_13 = 13; // 星级属性替换
		public static final int ARMOUR_CHANGE_14 = 14; // 星级属性突破
		public static final int ARMOUR_CHANGE_15 = 15; // 量子槽位升级
		public static final int ARMOUR_CHANGE_16 = 16; // 兵种转换

		public static final int ARMOUR_CHANGE_17 = 17; // 装备补发

	}
	
	/**
	 * 金条赠送原因
	 */
	public static class DiamondPresentReason {
		public static final String ACTIVE          = "customer_active";                 // 用户活跃行为赠送
		public static final String RECHATGE        = "recharge_activity";               // 充值赠送
		public static final String PRIVILEGE       = "vip_privilege";                   // vip返利
		public static final String PACKAGE         = "package";                         // 购买礼包赠送
		public static final String COOPERATION     = "cooperation_external_platforms";  // 外部平台联合
		public static final String EXPERIENCE      = "customer_experience";             // 客户体验
		public static final String GAMEPLAY        = "gameplay";                        // 游戏玩法
		public static final String COMPENSATION    = "customer_compensation";           // 用户补偿
		public static final String GAME_EXPERIENCE = "game_experience";                 // 新游戏体验服赠送
		public static final String OTHERS          = "others";                          // 其它
	}
	
	/**
	 * 统计数据类型
	 */
	public static enum StatisticDataType {
		COLLECT_RES_TODAY(1),     // 当天采集 资源数量统计
		GROUP_TOTAL_TODAY(2),     // 当天集结次数统计: 包括自己发起的集结和参与别人发起的集结
		GROUP_PVP_TOTAL_TODAY(3), // 当天集结PVP次数： 包括自己发起的集结和参与别人发起的集结
		ATK_GHOST_TOTAL_TODAY(4), // 当天累计战胜幽灵基地: 包括集结和单打
		PVE_TOTAL_TODAY(5);       // 当天累计打野次数
		
		int value;
		
		private StatisticDataType(int value) {
			this.value = value;
		}
		
		public int intVal() {
			return value;
		}
	}
	
	/**
	 * 禁止玩家操作类型
	 */
	public static enum BanPlayerOperType {
		BAN_CHANGE_IMAGE(1),   // 禁止修改头像
		BAN_CHANGE_NAME(2),    // 禁止修改昵称
		BAN_CHANGE_SIG(3),     // 禁止修改签名
		;
		
		int value;
		
		private BanPlayerOperType(int value) {
			this.value = value;
		}
		
		public int intVal() {
			return value;
		}
	}
	
	/**
	 * 建筑升级条件类型
	 */
	public static class BuildingConditionType {
		public static final int DRAMA_CHAPTER_PASS = 1;  // 剧情任务通过此章节
		public static final int DRAMA_TASK_MISSION = 2;  // 剧情任务完成状态
		public static final int GENERAL_MISSION = 3;     // 主线任务完成状态
		public static final int TECH_RESEARCH = 4;       // 科技研究完成状态
	}
	
	/**
	 * 计算战力的兵种类型
	 */
	public static int[] calcStrengthSoldierType = new int[]{1,2,3,4,5,6,7,8};
	
	/**
	 * 航海远征要塞状态
	 * @author Golden
	 *
	 */
	public static class CrossFortressState {
		public static final int NOTOPEN = 0; // 未开启
		public static final int OPEN = 1; // 已开启
		public static final int END = 2; // 已结束
	}
	
	/**
	 * 跨服阵营
	 * @author Golden
	 *
	 */
	public static class CrossCamp {
		public static final int ATK = 0; // 攻击方
		public static final int DEF = 1; // 防守方
	}
	
	/**
	 * 跨服出战联盟操作类型
	 * @author Golden
	 *
	 */
	public static class CrossFightActionType {
		public static final int INVITE_FIGHT = 1; // 邀请出战
		public static final int INVITE_FIGHT_APPROVE = 2; // 同意邀请出战
		public static final int APPLY_FIGHT = 3; // 申请出战
		public static final int APPLY_APPROVE_FIGHT = 4; // 同意申请出战
	}
	
	/**
	 *	idip每日统计量 
	 */
	public static enum IDIPDailyStatisType {
		DIAMOND_CONSUME("diamondConsume"),
		GOLD_CONSUME("goldConsume"),
		GIFT_RECHARGE("rechargeGift"),
		DIAMOND_RECHARGE("rechargeDiamond"),
		TOTAL_RECHARGE("rechargeTotal"),
		ONLINE_TIME("onlineTime"),
		TRAIN_ARMY("trainArmy"),
		ITEM_SPEED_TIME("itemSpeedTime");
		
		String typeValue;
		IDIPDailyStatisType(String value) {
			this.typeValue = value;
		}
		
		public String getTypeValue() {
			return typeValue;
		}
	}
	
}



