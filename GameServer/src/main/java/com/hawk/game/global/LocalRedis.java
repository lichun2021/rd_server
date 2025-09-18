package com.hawk.game.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisPoolConfig;
import org.hawk.redis.HawkRedisSession;
import org.hawk.serializer.HawkSerializer;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;
import org.hawk.util.HawkZlib;
import org.hawk.util.JsonUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossfortress.FortressRecordItem;
import com.hawk.game.data.ActivityScoreParamsInfo;
import com.hawk.game.data.BubbleRewardInfo;
import com.hawk.game.entity.PlayerGuardInviteEntity;
import com.hawk.game.entity.PlayerRelationApplyEntity;
import com.hawk.game.guild.GuildHelpInfo;
import com.hawk.game.guild.GuildWarRecord;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.guild.guildrank.data.GuildRankSvInfo;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.MarchSpeedItem;
import com.hawk.game.module.college.cfg.CollegeConstCfg;
import com.hawk.game.nation.construction.model.NationalDonatModel;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentTower;
import com.hawk.game.protocol.Chat.HPPushChat;
import com.hawk.game.protocol.GuildManager.DonateRankType;
import com.hawk.game.protocol.GuildManager.GuildApplyInfo;
import com.hawk.game.protocol.GuildManager.GuildBBSMessage;
import com.hawk.game.protocol.GuildManager.GuildShopLog;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.GuildManager.HPGuildLog;
import com.hawk.game.protocol.GuildWar.GuildWarMarchType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.President.OfficerRecord;
import com.hawk.game.protocol.President.PresidentEvent;
import com.hawk.game.protocol.President.PresidentHistory;
import com.hawk.game.protocol.President.PresidentResourceInfoSyn;
import com.hawk.game.protocol.President.TaxGuildRecord;
import com.hawk.game.protocol.Questionnaire.PushSurverInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.RedisMail.ChatData;
import com.hawk.game.protocol.RedisMail.ChatMessage;
import com.hawk.game.protocol.RedisMail.ChatRoomData;
import com.hawk.game.protocol.RedisMail.PlayerChatRoom;
import com.hawk.game.protocol.Share.BattleReportSharedAwardStatus;
import com.hawk.game.protocol.Share.PlayerDailyShareSaveDataPB;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponEvent;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPresident;
import com.hawk.game.protocol.TravelShop.TravelShopInfoSync;
import com.hawk.game.protocol.World.WorldFavoritePB;
import com.hawk.game.protocol.YuriRevenge.YuriRankType;
import com.hawk.game.service.GlobalMail;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PlotBattleService;
import com.hawk.game.service.guildtask.GuildTaskItem;
import com.hawk.game.strengthenguide.entity.SGPlayerEntity;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.GlobalControlType;
import com.hawk.game.util.GsConst.GuildConst;
import com.hawk.game.util.GsConst.GuildOffice;
import com.hawk.game.util.MapUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

/** 本服的redis操作 key的前缀采用 serverName:type:key eg: s1:chat:1
 * 
 * @author hawk */
public class LocalRedis {
	/** redis会话对象 */
	HawkRedisSession redisSession;
	/** redis访问的次数 */
	private AtomicLong redisOpCount;

	/** 全局实例对象 */
	private static LocalRedis instance = null;
	/** 玩家数据缓存 */
	static final String PLAYER_DATA_KEY = getInstance().getLocalIdentify() + ":player_data";
	/** 联盟聊天缓存存储键值 */
	static final String GUILD_MSG_KEY = getInstance().getLocalIdentify() + ":guild_chat";
	/** 世界聊天缓存 */
	static final String WORLD_MSG_KEY = getInstance().getLocalIdentify() + ":world_chat";
	/** 屏蔽玩家信息键值 */
	static final String SHIELD_PLAYER_KEY = getInstance().getLocalIdentify() + ":shield_player";
	/** 全服邮件键值 */
	static final String GLOBAL_MAIL_KEY = getInstance().getLocalIdentify() + ":global_mail";
	/** 玩家战斗日志存储键值 */
	static final String BATTLE_LOG_KEY = getInstance().getLocalIdentify() + ":battle_log";
	/** 国王礼包个数记录 */
	static final String PRESIDENT_GIFT_NUMBER_KEY = getInstance().getLocalIdentify() + ":president_gift_number";
	/** 国王礼包发放记录 */
	static final String PRESIDENT_GIFT_SEND_KEY = getInstance().getLocalIdentify() + ":president_gift_send";
	/** 历史国王记录 */
	static final String PRESIDENT_HISTORY_KEY = getInstance().getLocalIdentify() + ":president_history";
	/** 国王战事件记录 */
	static final String PRESIDENT_EVENT_KEY = getInstance().getLocalIdentify() + ":president_event";
	/** 国王战箭塔事件记录 */
	static final String PRESIDEN_TOWER_EVENT_KEY = getInstance().getLocalIdentify() + ":president_tower_event";
	/** 征税数据 */
	static final String TAX_GUILD_INFO_KEY = getInstance().getLocalIdentify() + ":tax_guild";
	/** 征税记录 */
	static final String TAX_GUILD_RECORD_KEY = getInstance().getLocalIdentify() + ":tax_record";
	/** 官职记录 */
	static final String OFFICER_RECORD_KEY = getInstance().getLocalIdentify() + ":officer_record";
	/** 资源比例设置 */
	static final String PRESIDENT_RESOURCE = getInstance().getLocalIdentify() + ":president_resource";
	/** 国王战数据 */
	static final String PRESIDENT_DATA_KEY = getInstance().getLocalIdentify() + ":president_data";
	/** 国王战箭塔数据 */
	static final String PRESIDENT_TOWER_DATA_KEY = getInstance().getLocalIdentify() + ":president_tower_data";
	/**国王赐福*/
	static final String PRESIDENT_BUYY_KEY = getInstance().getLocalIdentify() + ":president_buff";
	/** 禁言数据 */
	static final String WORLD_CHAT_SILENT_KEY = getInstance().getLocalIdentify() + ":silent";
	/** 玩家交互数据 */
	static final String INTERACTIVE_PLAYERS = getInstance().getLocalIdentify() + ":interactive_players";
	/** 聊天信息存储 */
	static final String CHAT_MESSAGE = getInstance().getLocalIdentify() + ":chat_message_build";
	/** 玩家聊天室信息 */
	static final String PLAYER_CHAT_ROOM_MAP = getInstance().getLocalIdentify() + ":player_chat_rooms";

	/** 聊天室数据 */
	static final String CHAT_ROOM_DATA = getInstance().getLocalIdentify() + ":chat_room_data_build";

	/** 行军加速消耗 */
	static final String MARCH_SPEED_CONSUME = getInstance().getLocalIdentify() + ":march_speed_consume";

	/** 推送问卷记录 */
	static final String QUESTIONNAIRE_KEY = getInstance().getLocalIdentify() + ":questionnaire";
	/** 推送问卷记录 */
	static final String QUESTIONNAIRE_VERSION_KEY = getInstance().getLocalIdentify() + ":questionnaire_version";

	/** 商品购买计数 */
	static final String SHOP_BUY_COUNT = getInstance().getLocalIdentify() + ":shop_buy_count";
	/** 当日联盟仓库使用量 */
	static final String WARE_HOUSE_DAY = getInstance().getLocalIdentify() + ":ware_house_day_count";
	/** 当时被掠夺资源量 */
	static final String GRAB_RES_LOST_DAY = getInstance().getLocalIdentify() + ":grab_res_lost";
	/** 收藏夹数据key */
	static final String FAVORITE_KEY = getInstance().getLocalIdentify() + ":favorite";
	/** 旅行商人 */
	static final String TRAVEL_SHOP = getInstance().getLocalIdentify() + ":travel_shop";
	/** 城内冒泡奖励信息 */
	static final String BUBBLE_INFO_KEY = getInstance().getLocalIdentify() + ":bubble_reward_info";

	/** 最近一次获得随机宝箱 */
	static final String RANDOM_CHEST_KEY = getInstance().getLocalIdentify() + ":last_get_chest";
	/** 已完成本期尤里复仇的联盟信息 */
	static final String YURI_REVENGE_FINISH_KEY = getInstance().getLocalIdentify() + ":yr_finish";
	/** 已完发放本期尤里复仇积分奖励玩家列表 */
	static final String YURI_REVENGE_REWARDED_KEY = getInstance().getLocalIdentify() + ":yr_rewarded";
	/** 尤里复仇个人积分排行 */
	static final String YURI_REVENGE_SELF_RANK = getInstance().getLocalIdentify() + ":yrself_rank";
	/** 尤里复仇联盟积分排行 */
	static final String YURI_REVENGE_GUILD_RANK = getInstance().getLocalIdentify() + ":yrgguild_rank";
	/** 好友赠送礼物 */
	static final String FRIEND_PRESENT_GIFT = getInstance().getLocalIdentify() + ":friend_present_gift";
	/** 当日邮件发放数 */
	static final String DAY_MAIL_SEND_COUNT = getInstance().getLocalIdentify() + ":day_mail_count";
	/** 系统开关 */
	static final String SYSTEM_SWITCH = getInstance().getLocalIdentify() + ":system_switch";
	/** 黑市商人概率 */
	static final String TRAVEL_GIFT_PROB = getInstance().getLocalIdentify() + ":travel_gift_prob";
	/** 黑市商人VIP概率 */
	static final String VIP_TRAVEL_GIFT_PROB = getInstance().getLocalIdentify() + ":vip_travel_gift_prob";

	static final String ATK_NEW_MONSTER = getInstance().getLocalIdentify() + ":atk_new_monster";
	/** 当天购买体力的次数 */
	static final String BUY_VIT_TIMES = getInstance().getLocalIdentify() + ":buy_vit_times";
	/** 单日使用道具获得金币的数量 */
	static final String TOOL_BACK_GOLD = getInstance().getLocalIdentify() + ":tool_back_gold";
	/** 新版野怪刷新 */
	static final String NEW_MONSTER_REFRESH = getInstance().getLocalIdentify() + ":new_monster_refresh";
	/** 推送信息存储键值 */
	static final String PUSH_INFO_KEY = getInstance().getLocalIdentify() + ":push_info";
	/** 每日特惠礼包免费宝箱领取时间 */
	static final String FREE_BOX_KEY = getInstance().getLocalIdentify() + ":free_box";

	/** 超级武器总数据 */
	static final String SUPERWEAPON_DATA_KEY = getInstance().getLocalIdentify() + ":superweapon_data";

	/** 每个超级武器数据 */
	static final String ABSTRACT_SUPERWEAPON_DATA_KEY = getInstance().getLocalIdentify() + ":abstract_sw";

	/** 提醒盟主建群的时间 */
	static final String REMIND_GUILD_LEADER_TIME = getInstance().getLocalIdentify() + ":remind_guild_leader_time";
	/** 超级武器简要事件记录 */
	static final String SUPER_WEAPON_BRIEF_EVENT_KEY = getInstance().getLocalIdentify() + ":super_weapon_brief_event";
	/** 超级武器简要事件记录 */
	static final String SUPER_WEAPON_DETIAL_EVENT_KEY = getInstance().getLocalIdentify() + ":super_weapon_detial_event";
	/** 超级武器国王记录 */
	static final String SUPER_WEAPON_HISTORY_KEY = getInstance().getLocalIdentify() + ":super_weapon_history";
	/** 超级武器礼包个数记录 */
	static final String SUPER_WEAPON_GIFT_NUM_KEY = getInstance().getLocalIdentify() + ":swg_num";
	/** 超级武器礼包发放记录 */
	static final String SUPER_WEAPONT_GIFT_SEND_KEY = getInstance().getLocalIdentify() + ":swg_send";
	/** 超级武器礼包个人接收数量 */
	static final String SUPER_WEAPON_PLAYER_RECEIVE_COUNT = getInstance().getLocalIdentify() + ":swg_pcount";
	
	
	
	/** 推送开关数据 */
	static final String PUSH_SWITCH_DATA = getInstance().getLocalIdentify() + ":push_switch";

	/** IDIP接口系统消息 */
	static final String IDIP_MSG_ON_LOGIN = getInstance().getLocalIdentify() + ":idip_msg_on_login";
	/** 举报信息 */
	static final String REPORTING_INFO = getInstance().getLocalIdentify() + ":reporting_info";
	/** 手Q成就上报标志 */
	static final String SCORE_BATCH_FLAG = getInstance().getLocalIdentify() + ":score_batch_flag";
	/** 好友申请 */
	static final String RELATION_APPLY = getInstance().getLocalIdentify() + ":relation_apply";

	/** 商城内已点击的新上架商品类型存储 */
	static final String CLICKED_NEWLY_SHOP_ITEM = getInstance().getLocalIdentify() + ":clicked_newly_shopitem";

	/** 商城热销商品配置期数 */
	static final String SHOP_TERM_KEY = getInstance().getLocalIdentify() + ":shop_term";

	/** 建筑(选择性)奖励 */
	static final String BUILD_AWARD = getInstance().getLocalIdentify() + ":build_award";
	/** /* 联盟榜单存储时间数据 */
	static final String GUILDRANK_INFO = getInstance().getLocalIdentify() + ":guildrank_info";
	/** 唯一成就 */
	static final String SOLE_ACHIEVE = getInstance().getLocalIdentify() + ":sole_achieve";
	/** 快速答疑排行 */
	static final String OL_QUESTION = getInstance().getLocalIdentify() + ":online_question";
	/** 物品兑换 */
	static final String ITEM_EXCHANGE = getInstance().getLocalIdentify() + ":item_exchange";

	/** 今日赏金领取额度 */
	static final String PLAYER_DAILY_ACHIEVE_BOUNTY = getInstance().getLocalIdentify() + ":player_daily_achieve_bounty";

	/** 攻击机甲次数 */
	static final String PLAYER_ATK_GUNDAM_TIMES = getInstance().getLocalIdentify() + ":atk_gundam_times";

	/** 机甲刷新id */
	static final String GUNDAM_REFRESH_UUID = getInstance().getLocalIdentify() + ":gundam_refresh_uuid";

	/** 攻击年兽次数 */
	static final String PLAYER_ATK_NIAN_TIMES = getInstance().getLocalIdentify() + ":atk_nian_times";

	/** 年兽刷新id */
	static final String NIAN_REFRESH_UUID = getInstance().getLocalIdentify() + ":nian_refresh_uuid";
	
	/** 我要变强分布 */
	static final String STRENGTHEN_GUIDE = getInstance().getLocalIdentify() + ":strengthen_guide";

	/** 玩家我要变强 */
	static final String PLAYER_STRENGTHEN_GUIDE = getInstance().getLocalIdentify() + ":player_strengthen_guide";

	/** 玩家我要变强 */
	static final String LMJY_CREATE_ARMY = getInstance().getLocalIdentify() + ":lmjy_create_army";

	/**玩家战报分享*/
	static final String PLAYER_DAILY_SHARE = getInstance().getLocalIdentify() + ":player_daily_share";
	
	/**年兽上次的K值*/
	static final String NIAN_LAST_K = getInstance().getLocalIdentify() + ":nian_last_k";
	
	/** 战地旗帜资源 */
	static final String FLAG_RESOURCE = getInstance().getLocalIdentify() + ":flag_resource";

	/** 学院申请列表*/
	static final String COLLEGE_PLAYER_APPLY = getInstance().getLocalIdentify() + ":college_p_apply";
	
	/** 玩家已申请学院*/
	static final String COLLEGE_SELF_APPLY = getInstance().getLocalIdentify() + ":college_s_apply";
	
	/** 战区赛季积分*/
	static final String SW_SEASON_SCORE = getInstance().getLocalIdentify() + ":sw_season_score"; 
	
	/** 战区赛季轮次*/
	static final String SW_SEASON_TURN = getInstance().getLocalIdentify() + ":sw_season_turn";
	
	/** 战区赛季礼包*/
	static final String SW_SEASON_GIFT = getInstance().getLocalIdentify() + ":sw_season_gift";
	
	/** 要塞期数*/
	static final String FORTRESS_TURN =  getInstance().getLocalIdentify() + ":fortress_turn";
	
	/** 要塞记录*/
	static final String FORTRESS_RECORD =  getInstance().getLocalIdentify() + ":fortress_record";
	
	/** 要塞npc*/
	static final String FORTRESS_NPC =  getInstance().getLocalIdentify() + ":fortress_npc";
	
	/** 要塞占领时间*/
	static final String FORTRESS_OT =  getInstance().getLocalIdentify() + ":fortress_ot";
	static final String FORTRESS_OTM =  getInstance().getLocalIdentify() + ":fortress_otm";
	
	/**
	 * 贵族商城红点
	 */
	static final String VIP_SHOP_RED_POINT = getInstance().getLocalIdentify() + ":vipshop_red_point";
	
	/**
	 * 全服控制
	 */
	static final String GLOBAL_CONTROL_BAN = getInstance().getLocalIdentify() + ":global_control_ban";
	
	/**
	 * 合服活动发奖.
	 */
	static final String ACTIVITY_SEND_REWARD_BY_MERGE_SERVER = getInstance().getLocalIdentify() + ":activity_send_reward_by_merge_server";  
	
	/** 决斗值 */
	static final String DUEL_CONTROL_POWER = getInstance().getLocalIdentify() + ":duel_control_powerr";
	/**
	 * 当天的守护装扮索要记录.
	 */
	static final String GUARD_BLAG_HISOTRY = getInstance().getLocalIdentify() + ":guard_blag_hisotry";
	
	
	/** 国家数据 */
	static final String NATIONAL_DATA_KEY = getInstance().getLocalIdentify() + ":national_data";
	
	/** 国家捐献数据 */
	static final String NATIONAL_DONATE_DATA_KEY = getInstance().getLocalIdentify() + ":national_donate_data";
	
	/** 国家飞船和科技取消CD */
	static final String NATIONAL_CANCEL_CD_KEY = getInstance().getLocalIdentify() + ":national_cancel_cd_data";
	
	/** 机器人角色  */
	static final String ROBOT_ROLE_SET_KEY = getInstance().getLocalIdentify() + ":robot_role";
	/**
	 * 活动积分处理相关参数
	 */
	private static final String ACTIVITY_SCORE_PARAMS = getInstance().getLocalIdentify() + ":activity_score_params";

	
	/** 联盟相关键值
	 * 
	 * @author admin */

	public static class GuildKeys {
		/** 申请加入联盟键值 */
		static final String GUILD_PLAYERAPPLY_KEY = getInstance().getLocalIdentify() + ":guild_player_apply";
		/** 玩家申请加入联盟键值 */
		static final String PLAYER_GUILDAPPLY_KEY = getInstance().getLocalIdentify() + ":player_guild_apply";
		/** 联盟日志 */
		static final String GUILD_LOG_KEY = getInstance().getLocalIdentify() + ":guild_log";
		/** 联盟聊天缓存存储键值 */
		static final String GUILD_BBS_KEY = getInstance().getLocalIdentify() + ":guild_bbs";
		/** 联盟帮助键值 */
		static final String GUILD_HELP_KEY = getInstance().getLocalIdentify() + ":guild_helps";
		/** 联盟战争记录存储键值 */
		static final String GUILD_BATTLE_KEY = getInstance().getLocalIdentify() + ":guild_battle";
		/** 联盟计数 */
		static final String PLAYER_GUILDNUM_KEY = getInstance().getLocalIdentify() + ":guild_num";
		/** 联盟留言屏蔽玩家 */
		static final String GUILD_FORBIDMESSAGE_KEY = GsConfig.getInstance().getServerId() + ":guild_forbid_message";

		/** 联盟商店购买日志信息 */
		static final String GUILD_SHOP_BUY_LOG_KEY = getInstance().getLocalIdentify() + ":guild_shop_buy_log";
		/** 联盟商店补货日志信息 */
		static final String GUILD_SHOP_ADD_LOG_KEY = getInstance().getLocalIdentify() + ":guild_shop_add_log";
		/** 联盟贡献日排行键值 */
		static final String GUILD_DONATE_DAILY_KEY = getInstance().getLocalIdentify() + ":guild_donate_day";
		/** 联盟贡献周排行键值 */
		static final String GUILD_DONATE_WEEK_KEY = getInstance().getLocalIdentify() + ":guild_donate_week";
		/** 联盟贡献总排行键值 */
		static final String GUILD_DONATE_TOTAL_KEY = getInstance().getLocalIdentify() + ":guild_donate_total";

		/** 联盟礼包领取日志 */
		static final String GUILD_GIFT_LOG_KEY = getInstance().getLocalIdentify() + ":guild_gift_log";

		/** 联盟邀请冷却 */
		static final String GUILD_INVIT_CD_KEY = getInstance().getLocalIdentify() + ":guild_invite_cd";

		/** 联盟成员迁城邀请冷却 */
		static final String GUILD_CITY_MOVE_CD_KEY = getInstance().getLocalIdentify() + ":guild_citymove_cd";

		/** 联盟坐标指引 */
		static final String GUILD_POINT_GUIDE = getInstance().getLocalIdentify() + ":guild_point_guide";

		/** 联盟标记信息 */
		static final String GUILD_SIGN = getInstance().getLocalIdentify() + ":guild_mark";

		/** 联盟官职申请信息 */
		static final String GUILD_OFFICE_APPLY = getInstance().getLocalIdentify() + ":guild_office";

		/** 联盟任务信息 */
		static final String GUILD_TASK_KEY = getInstance().getLocalIdentify() + ":guild_task";

		/** 联盟登录玩家信息 */
		static final String GUILD_LOGIN_KEY = getInstance().getLocalIdentify() + ":guild_login";
		
		/** 联盟玩家分享信息 */
		static final String GUILD_SHARE_KEY = getInstance().getLocalIdentify() + ":guild_share";

		/** 试炼殿堂玩家每日最高试炼分数 (:日期:联盟ID) **/
		static final String PLAYER_PLOT_BATTLE_RANK_SCORE = getInstance().getLocalIdentify() + ":player_plot_battle_rank_score:%s:%s";

		/** 勇士殿堂发送排行邮件真实时间 **/
		static final String PLOT_BATTLE_SEND_RANK_MAIL_REAL_TIME = getInstance().getLocalIdentify() + ":plot_battle_send_rank_mail_real_time";	
		
	}

	/** 排行keys */
	static final String[] RANK_KEYS = new String[] {
			getInstance().getLocalIdentify() + ":pfight_rank",
			getInstance().getLocalIdentify() + ":pkill_rank",
			getInstance().getLocalIdentify() + ":pcity_rank",
			getInstance().getLocalIdentify() + ":plvl_rank",
			getInstance().getLocalIdentify() + ":gfight_rank",
			getInstance().getLocalIdentify() + ":gkill_rank",
			getInstance().getLocalIdentify() + ":pnoarmy_rank"
	};
	
	/**
	 * 存储所有的
	 */
	static String GUARD_INVITE = getInstance().getLocalIdentify() + ":" + "guard_invite";
	
	/**圣诞大战uuid*/
	static String CHRISTMAS_WAR_REFRESH_UUID = getInstance().getLocalIdentify() + ":christmas_war_refresh_uuid"; 

	/** 获取实例对象
	 *
	 * @return */
	public static LocalRedis getInstance() {
		if (instance == null) {
			instance = new LocalRedis();
		}

		instance.incRedisOp();
		return instance;
	}

	/** 构造 */
	private LocalRedis() {
		instance = this;
		redisOpCount = new AtomicLong();
	}

	/** 初始化本地redis
	 * 
	 * @return */
	public boolean init() {
		HawkRedisPoolConfig config = new HawkRedisPoolConfig();
		config.setMaxTotal(GsConfig.getInstance().getRedisMaxActive());
		config.setMaxIdle(GsConfig.getInstance().getRedisMaxIdle());
		config.setMaxWaitMillis(GsConfig.getInstance().getRedisMaxWait());

		String localRedis = GsConfig.getInstance().getLocalRedis();
		if (!HawkOSOperator.isEmptyString(localRedis)) {
			String[] infos = localRedis.split(":");
			String redisHost = infos[0];
			int redisPort = Integer.valueOf(infos[1]);

			// redis连接配置
			int timeout = GsConfig.getInstance().getRedisTimeout();
			String redisAuth = GsConfig.getInstance().getLocalRedisAuth();

			// 初始化redis会话
			redisSession = new HawkRedisSession();
			if (!redisSession.init(redisHost, redisPort, timeout, redisAuth, config)) {
				HawkLog.errPrintln("init local redis failed, ip: {}, port: {}", redisHost, redisPort);
				return false;
			}
			HawkLog.logPrintln("init local redis success, ip: {}, port: {}", redisHost, redisPort);
		} else {
			redisSession = RedisProxy.getInstance().getRedisSession();
			HawkLog.logPrintln("init local redis as global success");
		}

		return true;
	}

	/** 获取本地redis标识
	 * 
	 * @return */
	public String getLocalIdentify() {
		String serverIdentify = GsApp.getInstance().getServerIdentify();
		String serverId = GsConfig.getInstance().getServerId();
		return serverId + ":" + serverIdentify;
	}

	/** 获取会话对象, 便于脚本调用
	 * 
	 * @return */
	public HawkRedisSession getRedisSession() {
		return redisSession;
	}

	/** 增加redis操作
	 * 
	 * @return */
	public long incRedisOp() {
		return redisOpCount.incrementAndGet();
	}

	/** 获取redis操作数
	 * 
	 * @return */
	public long getRedisOpCount() {
		return redisOpCount.get();
	}

	/** 重置redis操作数 */
	public void resetRedisOpCount() {
		redisOpCount.set(0);
	}

	/** 更新玩家数据到缓存
	 * 
	 * @param playerId
	 * @param dataJson
	 * @return */
	public boolean updatePlayerData(String playerId, JSONObject dataJson) {
		if (HawkOSOperator.isEmptyString(playerId) || dataJson == null) {
			return false;
		}
		StatisManager.getInstance().incRedisKey(PLAYER_DATA_KEY);

		String key = PLAYER_DATA_KEY + ":" + playerId;
		try {
			String value = dataJson.toJSONString();
			if (GsConfig.getInstance().isGzPlayerData()) {
				byte[] dateBytes = HawkZlib.zlibDeflate(value.getBytes());
				redisSession.setBytes(key, dateBytes, GsConfig.getInstance().getPlayerRedisExpire());
				HawkLog.logPrintln("player local data cache success, playerId: {}, size: {}", playerId, value.length());
			} else {
				redisSession.setString(key, value, GsConfig.getInstance().getPlayerRedisExpire());
				HawkLog.logPrintln("player local data cache success, playerId: {}, size: {}", playerId, value.length());
			}

			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("player local data cache failed, playerId: {}", playerId);
		}

		return false;
	}

	/** 从本服缓存中获取玩家数据
	 * 
	 * @param playerId
	 * @return */
	public JSONObject getPlayerData(String playerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_DATA_KEY);

		String key = PLAYER_DATA_KEY + ":" + playerId;
		try {
			byte[] dataBytes = redisSession.getBytes(key.getBytes());
			if (dataBytes[0] == '{') {
				return JSON.parseObject(new String(dataBytes, "UTF-8"));
			} else {
				byte[] jsonData = HawkZlib.zlibInflate(dataBytes);
				return JSON.parseObject(new String(jsonData, "UTF-8"));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/** 获取聊天缓存
	 * 
	 * @param guildId
	 *            如果为null,则获取世界聊天信息
	 * @return */
	public HPPushChat getChatMsg(String guildId) {
		String key = WORLD_MSG_KEY;
		if (!HawkOSOperator.isEmptyString(guildId)) {
			key = GUILD_MSG_KEY + ":" + guildId;
			StatisManager.getInstance().incRedisKey(GUILD_MSG_KEY);
		} else {
			StatisManager.getInstance().incRedisKey(WORLD_MSG_KEY);
		}

		try {
			byte[] dataBytes = redisSession.getBytes(key.getBytes());
			if (dataBytes != null && dataBytes.length > 0) {
				return HPPushChat.parseFrom(dataBytes);
			}
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/** 更新聊天缓存
	 * 
	 * @param msgs
	 * @return */
	public boolean updateChatMsgCache(HPPushChat msg, String guildId) {

		String key = WORLD_MSG_KEY;
		// 世界聊天
		if (!HawkOSOperator.isEmptyString(guildId)) {
			key = GUILD_MSG_KEY + ":" + guildId;
			StatisManager.getInstance().incRedisKey(GUILD_MSG_KEY);
		} else {
			StatisManager.getInstance().incRedisKey(WORLD_MSG_KEY);
		}

		return redisSession.setBytes(key, msg.toByteArray(), (int) TimeUnit.DAYS.toSeconds(7));
	}

	/** 添加联盟留言缓存 */
	public boolean addGuildBBS(String guildId, GuildBBSMessage.Builder chatMsgInfo) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_BBS_KEY);

		byte[] key = (GuildKeys.GUILD_BBS_KEY + ":" + guildId).getBytes();
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		redisSession.zAdd(key, chatMsgInfo.getTime(), chatMsgInfo.build().toByteArray(), expireSecond);
		if (redisSession.zCount(key, 0, Long.MAX_VALUE) >= GuildConstProperty.getInstance().getAllianceLeaveMsgSave()) {
			redisSession.zRemrangeByRank(key, 0, 0);
		}
		return true;
	}

	/** 删除联盟成员
	 * 
	 * @param guildId
	 * @param member */
	public void delGuildBBS(String guildId, byte[]... member) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_BBS_KEY);

		byte[] key = (GuildKeys.GUILD_BBS_KEY + ":" + guildId).getBytes();
		redisSession.zRem(key, member);
	}

	/** 获取联盟留言缓存 (一次取20条) */
	public Set<byte[]> getGuildBBS(String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_BBS_KEY);

		byte[] key = (GuildKeys.GUILD_BBS_KEY + ":" + guildId).getBytes();
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		return redisSession.zRangeByScore(key, 0, Long.MAX_VALUE, expireSecond);
	}

	/** 添加联盟申请 */
	public void addGuildPlayerApply(String guildId, GuildApplyInfo.Builder info) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_PLAYERAPPLY_KEY);

		String guildApplykey = GuildKeys.GUILD_PLAYERAPPLY_KEY + ":" + guildId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		redisSession.hSet(guildApplykey, info.getPlayerId(), JsonFormat.printToString(info.build()), expireSecond);
	}

	/** 添加玩家联盟申请 */
	public void addPlayerGuildApply(String playerId, String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_GUILDAPPLY_KEY);

		String playerApplykey = GuildKeys.PLAYER_GUILDAPPLY_KEY + ":" + playerId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		redisSession.hSet(playerApplykey, guildId, String.valueOf(HawkTime.getMillisecond()), expireSecond);
	}

	/** 获取联盟申请 */
	public List<String> getGuildPlayerApply(String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_PLAYERAPPLY_KEY);

		String key = GuildKeys.GUILD_PLAYERAPPLY_KEY + ":" + guildId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		return redisSession.hVals(key, expireSecond);
	}

	/** 获取联盟申请 */
	public long getGuildPlayerApplyNum(String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_PLAYERAPPLY_KEY);

		String key = GuildKeys.GUILD_PLAYERAPPLY_KEY + ":" + guildId;
		return redisSession.hLen(key, false);
	}

	/** 获取联盟申请 */
	public String getGuildPlayerApply(String guildId, String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_PLAYERAPPLY_KEY);

		String key = GuildKeys.GUILD_PLAYERAPPLY_KEY + ":" + guildId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		return redisSession.hGet(key, playerId, expireSecond);
	}

	/** 获取玩家联盟申请 */
	public Set<String> getPlayerGuildApply(String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_GUILDAPPLY_KEY);

		String key = GuildKeys.PLAYER_GUILDAPPLY_KEY + ":" + playerId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		return redisSession.hKeys(key, expireSecond);
	}

	/** 包括联盟申请
	 * 
	 * @param playerId
	 * @param guildId
	 * @return */
	public boolean containPlayerGuildApply(String playerId, String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_GUILDAPPLY_KEY);

		String key = GuildKeys.PLAYER_GUILDAPPLY_KEY + ":" + playerId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		return redisSession.hExists(key, guildId, expireSecond);
	}

	/** 移除联盟申请 */
	public boolean removeGuildPlayerApply(String guildId, String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_GUILDAPPLY_KEY);

		String key = GuildKeys.GUILD_PLAYERAPPLY_KEY + ":" + guildId;
		return redisSession.hDel(key, playerId) > 0;
	}

	/** 移除玩家联盟申请 */
	public boolean removePlayerGuildApply(String playerId, String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_GUILDAPPLY_KEY);

		String key = GuildKeys.PLAYER_GUILDAPPLY_KEY + ":" + playerId;
		return redisSession.hDel(key, guildId) > 0;
	}

	/** 移除所有联盟申请 */
	public boolean removeAllGuildPlayerApply(String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_GUILDAPPLY_KEY);

		String key = GuildKeys.GUILD_PLAYERAPPLY_KEY + ":" + guildId;
		return redisSession.delete(key, false);
	}

	/** 移除所有玩家联盟申请 */
	public boolean removeAllPlayerGuildApply(String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_GUILDAPPLY_KEY);

		String key = GuildKeys.PLAYER_GUILDAPPLY_KEY + ":" + playerId;
		return redisSession.delete(key, false);
	}

	/** 获取排行榜列表
	 * 
	 * @param rankType
	 * @return */
	public Set<Tuple> getRankList(RankType rankType, int maxCount) {
		StatisManager.getInstance().incRedisKey(RANK_KEYS[rankType.getNumber()]);
		return redisSession.zRevrangeWithScores(RANK_KEYS[rankType.getNumber()], 0, maxCount - 1, 0);
	}

	/** 获取具体积分
	 * 
	 * @param uuid
	 * @param rankType
	 * @return */
	public long getPlayerRankScore(String id, RankType rankType) {
		if (HawkOSOperator.isEmptyString(id)) {
			return 0;
		}
		StatisManager.getInstance().incRedisKey(RANK_KEYS[rankType.getNumber()]);

		Double zScore = redisSession.zScore(RANK_KEYS[rankType.getNumber()], id, 0);
		return Objects.nonNull(zScore) ? zScore.longValue() : 0;
	}

	/** 添加单个排行对象
	 * 
	 * @param key
	 * @param score
	 * @param member */
	public void updateRankScore(RankType rankType, long score, String member) {
		StatisManager.getInstance().incRedisKey(RANK_KEYS[rankType.getNumber()]);

		String key = RANK_KEYS[rankType.getNumber()];
		redisSession.zAdd(key, score, member);
	}

	/** 批量添加排行对象
	 * 
	 * @param key
	 * @param member */
	public void updateRankScore(RankType rankType, Map<String, Double> members) {
		StatisManager.getInstance().incRedisKey(RANK_KEYS[rankType.getNumber()]);

		String key = RANK_KEYS[rankType.getNumber()];
		redisSession.zAdd(key, members);
	}

	/** 从排行中移除指定元素
	 * 
	 * @param rankType
	 * @param member */
	public void removeFromRank(RankType rankType, String member) {
		StatisManager.getInstance().incRedisKey(RANK_KEYS[rankType.getNumber()]);

		String key = RANK_KEYS[rankType.getNumber()];
		redisSession.zRem(key, 0, member);
	}

	/** 联盟解散删除排行对象
	 * 
	 * @param key
	 * @param score
	 * @param id */
	public void deleteGuildRank(String id) {
		StatisManager.getInstance().incRedisKey(RANK_KEYS[RankType.ALLIANCE_FIGHT_KEY_VALUE]);
		StatisManager.getInstance().incRedisKey(RANK_KEYS[RankType.ALLIANCE_KILL_ENEMY_KEY_VALUE]);

		redisSession.zRem(RANK_KEYS[RankType.ALLIANCE_FIGHT_KEY_VALUE], 0, id);
		redisSession.zRem(RANK_KEYS[RankType.ALLIANCE_KILL_ENEMY_KEY_VALUE], 0, id);
	}

	/** 删除聊天记录缓存
	 * 
	 * @param groupId */
	public void deleteGuildMsgCache(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		StatisManager.getInstance().incRedisKey(GUILD_MSG_KEY);

		String key = GUILD_MSG_KEY + ":" + guildId;
		redisSession.delete(key, true);
	}

	/** 删除多个排行对象
	 * 
	 * @param playerId */
	public void deletePlayerRanks(String playerId) {
		for (String key : RANK_KEYS) {
			StatisManager.getInstance().incRedisKey(key);

			redisSession.zRem(key, 0, playerId);
		}
	}

	/** 删除指定排行榜数据
	 * 
	 * @param rankType */
	public void deleteRank(RankType rankType) {
		StatisManager.getInstance().incRedisKey(RANK_KEYS[rankType.getNumber()]);

		redisSession.zRemrangeByRank(RANK_KEYS[rankType.getNumber()], 0, -1);
	}

	/** 设置联盟相关数量
	 * 
	 * @param playerId
	 * @param numKey
	 * @param count
	 * @return */
	public boolean setGuildNum(String playerId, String numKey, int count) {
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_GUILDNUM_KEY);

		String key = GuildKeys.PLAYER_GUILDNUM_KEY + ":" + playerId + ":" + numKey;
		return redisSession.setString(key, String.valueOf(count));
	}

	/** 获得联盟相关数量
	 * 
	 * @param playerId
	 * @param numKey
	 * @return */
	public int getGuildNum(String playerId, String numKey) {
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_GUILDNUM_KEY);

		String key = GuildKeys.PLAYER_GUILDNUM_KEY + ":" + playerId + ":" + numKey;
		if (redisSession.exists(key)) {
			Integer.parseInt(redisSession.getString(key));
		}

		return 0;
	}

	/** 获取某联盟的所有帮助信息
	 * 
	 * @param guildId
	 *            : 联盟ID */
	public Map<String, GuildHelpInfo> loadAllGuildHelpInfo(String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_HELP_KEY);

		String key = GuildKeys.GUILD_HELP_KEY + ":" + guildId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		Map<String, String> map = redisSession.hGetAll(key, expireSecond);
		Map<String, GuildHelpInfo> result = new HashMap<>();
		for (Entry<String, String> ent : map.entrySet()) {
			try {
				result.put(ent.getKey(), GuildHelpInfo.valueOf(ent.getValue()));
			} catch (Exception e) {
				HawkLog.logPrintln("LocalRedis loadAllGuildHelpInfo exception, guildId: {}, key: {}, value: {}", guildId, ent.getKey(), ent.getValue());
				HawkException.catchException(e, ent.getKey());
			}
		}
		redisSession.delete(key, false);
		return result;
	}

	public void saveAllGuildHelpInfo(String guildId, Map<String, GuildHelpInfo> helps) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_HELP_KEY);
		String key = GuildKeys.GUILD_HELP_KEY + ":" + guildId;

		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		Map<String, String> map = new HashMap<>();
		helps.forEach((qid, info) -> map.put(qid, info.serializ()));
		redisSession.hmSet(key, map, expireSecond);
	}

	/** 添加联盟礼包领取记录
	 * 
	 * @param guildId
	 * @param log */
	public void addGuildGiftLog(String playerId, List<byte[]> logs) {
		if (logs.isEmpty()) {
			return;
		}
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_GIFT_LOG_KEY);

		byte[][] logBytes = logs.toArray(new byte[logs.size()][]);
		int logMaxNum = GuildConstProperty.getInstance().getAllianceGiftRecordUpLimit();
		byte[] key = (GuildKeys.GUILD_GIFT_LOG_KEY + ":" + playerId).getBytes();
		redisSession.lPush(key, expireSecond, logBytes);
		if (redisSession.lLen(key, expireSecond) > logMaxNum) {
			redisSession.lTrim(key, 0, logMaxNum - 1, expireSecond);
		}
	}

	/** 获得联盟礼包领取日志
	 * 
	 * @param guildId
	 * @param count
	 *            日志条数 */
	public List<byte[]> getGuildGiftLog(String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_GIFT_LOG_KEY);

		byte[] key = (GuildKeys.GUILD_GIFT_LOG_KEY + ":" + playerId).getBytes();
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		return redisSession.lRange(key, 0, -1, expireSecond);
	}

	/** 移除联盟礼包领取日志
	 * 
	 * @param guildId */
	public void removeGuildGiftLog(String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_GIFT_LOG_KEY);

		byte[] key = (GuildKeys.GUILD_GIFT_LOG_KEY + ":" + playerId).getBytes();
		redisSession.del(key);
	}

	/** 添加联盟邀请邮件cd
	 * 
	 * @param inviterId
	 *            邀请玩家id
	 * @param inviteeId
	 *            被邀请玩家id */
	public void addGuildInviteMailCd(String inviterId, String inviteeId) {
		String key = GuildKeys.GUILD_INVIT_CD_KEY + inviterId + inviteeId;
		int cd = GuildConstProperty.getInstance().getInviteMailCd();
		if (cd > 0) {
			redisSession.setString(key, String.valueOf(HawkTime.getMillisecond()), cd);
		}
	}

	/** 获取联盟邀请邮件cd开始事件
	 * 
	 * @return */
	public long getGuildInviteMailCd(String inviterId, String inviteeId) {
		String key = GuildKeys.GUILD_INVIT_CD_KEY + inviterId + inviteeId;
		String result = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(result)) {
			return Long.valueOf(result);
		} else {
			return 0;
		}
	}

	/** 添加联盟迁城邀请邮件cd
	 * 
	 * @param inviterId
	 *            邀请玩家id
	 * @param inviteeId
	 *            被邀请玩家id */
	public void addGuildCitymoveMailCd(String inviterId, String inviteeId) {
		String key = GuildKeys.GUILD_CITY_MOVE_CD_KEY + inviterId + inviteeId;
		int cd = GuildConstProperty.getInstance().getInviteMailCd();
		if (cd > 0) {
			redisSession.setString(key, String.valueOf(HawkTime.getMillisecond()), cd);
		}
	}

	/** 获取联盟邀迁城请邮件cd开始时间
	 * 
	 * @return */
	public long getGuildCitymoveMailCd(String inviterId, String inviteeId) {
		String key = GuildKeys.GUILD_CITY_MOVE_CD_KEY + inviterId + inviteeId;
		String result = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(result)) {
			return Long.valueOf(result);
		} else {
			return 0;
		}
	}

	/** 联盟战争信息保存
	 * 
	 * @param atkPlayer
	 * @param defPlayer
	 * @param isAtkWin
	 * @param warType */
	public void saveGuildBattleInfo(List<Player> atkPlayers, List<Player> defPlayers, boolean isAtkWin, GuildWarMarchType atkMarchType, GuildWarMarchType defMarchType) {
		if (atkPlayers.isEmpty() || defPlayers.isEmpty()) {
			return;
		}

		Player atkPlayer = atkPlayers.get(0);
		Player defPlayer = defPlayers.get(0);

		if (!atkPlayer.hasGuild() && !defPlayer.hasGuild()) {
			return;
		}

		GuildWarRecord guildWar = new GuildWarRecord();

		guildWar.setAttPlayerId(atkPlayer.getId());
		guildWar.setAttPlayerName(atkPlayer.getName());
		guildWar.setAttGuildId(atkPlayer.getGuildId() == null ? "" : atkPlayer.getGuildId());
		String guildTag = GuildService.getInstance().getGuildTag(atkPlayer.getGuildId());
		guildWar.setAttGuildName(guildTag == null ? "" : guildTag);
		guildWar.setAtkMarchType(atkMarchType);

		List<String> atkPlayerIds = new ArrayList<>();
		for (Player player : atkPlayers) {
			atkPlayerIds.add(player.getId());
		}
		guildWar.setAtkPlayerIds(atkPlayerIds);

		guildWar.setDefPlayerId(defPlayer.getId());
		guildWar.setDefPlayerName(defPlayer.getName());
		guildWar.setDefGuildId(defPlayer.getGuildId() == null ? "" : defPlayer.getGuildId());
		String defGuildTag = GuildService.getInstance().getGuildTag(defPlayer.getGuildId());
		guildWar.setDefGuildName(defGuildTag == null ? "" : defGuildTag);
		guildWar.setDefMarchType(defMarchType);

		List<String> defPlayerIds = new ArrayList<>();
		for (Player player : defPlayers) {
			defPlayerIds.add(player.getId());
		}
		guildWar.setDefPlayerIds(defPlayerIds);

		guildWar.setWinTimes(isAtkWin ? 1 : -1);
		guildWar.setWarTime(HawkTime.getMillisecond());

		addGuildWarRecord(guildWar);
	}

	/** 添加联盟战争记录 */
	private boolean addGuildWarRecord(GuildWarRecord warRecord) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_BATTLE_KEY);

		try {
			// 判断是否已加入联盟
			int mailExpireSecond = GameConstCfg.getInstance().getMailExpireSecond();
			if (!HawkOSOperator.isEmptyString(warRecord.getAttGuildId())) {
				String key = GuildKeys.GUILD_BATTLE_KEY + ":" + warRecord.getAttGuildId();
				String warInfo = redisSession.lIndex(key, 0); // 获得最近插入的一条记录
				if (warInfo != null) {
					GuildWarRecord guildWar = JSON.parseObject(warInfo, GuildWarRecord.class);
					// 判断是否是连续进攻
					if (warRecord.isContinuous(guildWar)) {
						guildWar.setWarTime(warRecord.getWarTime());
						guildWar.addWinTimes(1); // 连续进攻胜利或失败次数加1
						redisSession.lPop(key, false);
						redisSession.lPush(key, mailExpireSecond, JSON.toJSONString(guildWar));
					} else {
						redisSession.lPush(key, mailExpireSecond, JSON.toJSONString(warRecord));
						int recordCount = GameConstCfg.getInstance().getRecordCount();
						redisSession.lTrim(key, 0, recordCount);
					}
				} else {
					redisSession.lPush(key, mailExpireSecond, JSON.toJSONString(warRecord));
				}
			}

			if (!HawkOSOperator.isEmptyString(warRecord.getDefGuildId())) {
				String key = GuildKeys.GUILD_BATTLE_KEY + ":" + warRecord.getDefGuildId();
				GuildWarRecord defSide = warRecord.clone();
				defSide.setWinTimes(-1 * warRecord.getWinTimes());
				String warInfo = redisSession.lIndex(key, 0); // 获得最近插入的一条记录
				if (warInfo != null) {
					GuildWarRecord guildWar = JSON.parseObject(warInfo, GuildWarRecord.class);
					// 判断是否是连续进攻
					if (defSide.isContinuous(guildWar)) {
						guildWar.setWarTime(defSide.getWarTime());
						guildWar.addWinTimes(1); // 连续进攻胜利或失败次数加1
						redisSession.lPop(key, false);
						redisSession.lPush(key, mailExpireSecond, JSON.toJSONString(guildWar));
					} else {
						redisSession.lPush(key, mailExpireSecond, JSON.toJSONString(defSide));
						int recordCount = GameConstCfg.getInstance().getRecordCount();
						redisSession.lTrim(key, 0, recordCount);
					}
				} else {
					redisSession.lPush(key, mailExpireSecond, JSON.toJSONString(defSide));
				}
			}

			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return false;
	}

	/** 获取联盟战争记录
	 * 
	 * @param battleInfo
	 * @return */
	public List<String> getGuildWarRecord(String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_BATTLE_KEY);

		String key = GuildKeys.GUILD_BATTLE_KEY + ":" + guildId;
		int recordCount = GameConstCfg.getInstance().getRecordCount();
		return redisSession.lRange(key, 0, recordCount, 0);
	}

	/** 增加联盟留言屏蔽玩家
	 * 
	 * @param info */
	public void addForbidPostPlayer(String guildId, String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_FORBIDMESSAGE_KEY);

		String key = GuildKeys.GUILD_FORBIDMESSAGE_KEY + ":" + guildId;
		redisSession.sAdd(key, 0, playerId);
	}

	/** 获得联盟留言被屏蔽的玩家
	 * 
	 * @param guildId */
	public Set<String> GetForbidPostPlayer(String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_FORBIDMESSAGE_KEY);

		String key = GuildKeys.GUILD_FORBIDMESSAGE_KEY + ":" + guildId;
		return redisSession.sMembers(key);
	}

	/** 判断指定玩家是否被屏蔽留言
	 * 
	 * @param guildId
	 * @param playerId
	 * @return */
	public boolean isBeForbid(String guildId, String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_FORBIDMESSAGE_KEY);

		String key = GuildKeys.GUILD_FORBIDMESSAGE_KEY + ":" + guildId;
		return redisSession.sIsmember(key, playerId);

	}

	/** 移除联盟留言被屏蔽的玩家 */
	public Long removeForbidPostPlayer(String guildId, String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_FORBIDMESSAGE_KEY);

		String key = GuildKeys.GUILD_FORBIDMESSAGE_KEY + ":" + guildId;
		return redisSession.sRem(key, playerId);
	}

	/** 添加聊天屏蔽玩家
	 * 
	 * @param playerId
	 * @param shieldedPlayerId */
	public void addShieldPlayer(String playerId, String shieldedPlayerId) {
		StatisManager.getInstance().incRedisKey(SHIELD_PLAYER_KEY);

		String key = SHIELD_PLAYER_KEY + ":" + playerId;
		redisSession.sAdd(key, GsConfig.getInstance().getPlayerRedisExpire(), shieldedPlayerId);
	}

	/** 获得聊天屏蔽的玩家
	 * 
	 * @param info */
	public Set<String> getShieldPlayer(String playerId) {
		StatisManager.getInstance().incRedisKey(SHIELD_PLAYER_KEY);

		String key = SHIELD_PLAYER_KEY + ":" + playerId;
		return redisSession.sMembers(key);
	}

	/** 移除聊天屏蔽的玩家 */
	public long removeShieldPlayer(String playerId, String shieldedPlayerId) {
		StatisManager.getInstance().incRedisKey(SHIELD_PLAYER_KEY);

		String key = SHIELD_PLAYER_KEY + ":" + playerId;
		return redisSession.sRem(key, shieldedPlayerId);
	}

	/** 添加联盟日志
	 * 
	 * @param guildId
	 * @param log */
	public void addGuildLog(String guildId, HPGuildLog.Builder log) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_LOG_KEY);

		int logMaxNum = GuildConstProperty.getInstance().getAllianceDiarySave();
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		String jsonObject = JsonFormat.printToString(log.build());
		String key = GuildKeys.GUILD_LOG_KEY + ":" + guildId;
		String member = jsonObject;
		redisSession.zAdd(key, log.getTime(), member, expireSecond);
		if (redisSession.zCount(key, 0, Long.MAX_VALUE) > logMaxNum) {
			redisSession.zRemrangeByRank(key, 0, 0);
		}
	}

	/** 获得日志
	 * 
	 * @param guildId
	 * @param count
	 *            日志条数 */
	public Set<String> getGuildLog(String guildId, int count) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_LOG_KEY);

		String key = GuildKeys.GUILD_LOG_KEY + ":" + guildId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		return redisSession.zRange(key, 0, count - 1, expireSecond);
	}

	/** 移除联盟日志
	 * 
	 * @param guildId */
	public void removeGuildLog(String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_LOG_KEY);

		redisSession.delete(GuildKeys.GUILD_LOG_KEY + ":" + guildId, false);
	}

	/** 增加全服邮件 */
	public long addGlobalMail(GlobalMail mail) {
		StatisManager.getInstance().incRedisKey(GLOBAL_MAIL_KEY);

		return redisSession.lPush(GLOBAL_MAIL_KEY, 0, mail.toJson().toString());
	}

	/** 获取所有全服邮件 */
	public List<GlobalMail> getAllGlobalMails() {
		StatisManager.getInstance().incRedisKey(GLOBAL_MAIL_KEY);

		List<GlobalMail> mails = new CopyOnWriteArrayList<GlobalMail>();
		try {
			List<String> jsonData = redisSession.lRange(GLOBAL_MAIL_KEY, 0, -1, 0);
			if (jsonData != null && jsonData.size() > 0) {
				for (String value : jsonData) {
					GlobalMail globalMail = GlobalMail.create(JSONObject.parseObject(value));
					if (globalMail != null) {
						mails.add(globalMail);
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return mails;
	}

	/** 删除全服邮件
	 * 
	 * @param index
	 *            : 删除index及之后的所有邮件 */
	public void delGlobalMail(int index) {
		StatisManager.getInstance().incRedisKey(GLOBAL_MAIL_KEY);

		if (index == 0) {
			// 清空
			redisSession.lTrim(GLOBAL_MAIL_KEY, Integer.MAX_VALUE, Integer.MAX_VALUE);
		} else {
			redisSession.lTrim(GLOBAL_MAIL_KEY, 0, index - 1);
		}
	}
	
	/**
	 *删除list中指定下标的全服邮件
	 *
	 * @param index
	 */
	public void removeGlobalMail(int index) {
		redisSession.lSet(GLOBAL_MAIL_KEY, index, "del");
		redisSession.lRem(GLOBAL_MAIL_KEY, 0, "del");
	}

	/** 添加联盟商店购买/补货记录
	 * 
	 * @param guildId
	 * @param log */
	public void addGuildShopLog(String guildId, GuildShopLog.Builder log, int logType) {
		String jsonObject = JsonFormat.printToString(log.build());
		String key = getGuildShopLogKey(logType) + ":" + guildId;
		String member = jsonObject;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		redisSession.zAdd(key, log.getTime(), member, expireSecond);
		if (redisSession.zCount(key, 0, Long.MAX_VALUE) > GuildConstProperty.getInstance().getAllianceStorePurchaseConfigLimit()) {
			redisSession.zRemrangeByRank(key, 0, 0);
		}
	}

	/** 获得联盟商店购买记录
	 * 
	 * @param guildId
	 * @return */
	public Set<String> getGuildShopLog(String guildId, int logType) {
		String key = getGuildShopLogKey(logType);
		if (!HawkOSOperator.isEmptyString(key)) {
			key = key + ":" + guildId;
			int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
			return redisSession.zRangeByScore(key, 0, Long.MAX_VALUE, expireSecond);
		}

		return Collections.emptySet();
	}

	/** 根据联盟商店log类型获取对应key值
	 * 
	 * @param logType
	 * @return */
	public String getGuildShopLogKey(int logType) {
		String key = null;
		switch (logType) {
		case GuildConst.SHOP_LOG_TYPE_BUY:
			key = GuildKeys.GUILD_SHOP_BUY_LOG_KEY;
			break;
		case GuildConst.SHOP_LOG_TYPE_ADD:
			key = GuildKeys.GUILD_SHOP_ADD_LOG_KEY;
			break;
		}
		StatisManager.getInstance().incRedisKey(key);
		return key;
	}

	/** 联盟解散删除该联盟所有联盟商店记录
	 * 
	 * @param guildId */
	public void removeGuildShopLog(String guildId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_SHOP_BUY_LOG_KEY);
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_SHOP_ADD_LOG_KEY);

		String key = GuildKeys.GUILD_SHOP_BUY_LOG_KEY + ":" + guildId;
		redisSession.delete(key, false);
		key = GuildKeys.GUILD_SHOP_ADD_LOG_KEY + ":" + guildId;
		redisSession.delete(key, false);
	}

	/** 根据捐献排行类型,获取对应键值
	 * 
	 * @param type
	 * @return */
	private String getGuildDonateRankKey(DonateRankType type) {
		switch (type) {
		case DAILY_RANK:
			StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_DONATE_DAILY_KEY);
			return GuildKeys.GUILD_DONATE_DAILY_KEY;
		case WEEK_RANK:
			StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_DONATE_WEEK_KEY);
			return GuildKeys.GUILD_DONATE_WEEK_KEY;
		case TOTAL_RANK:
			StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_DONATE_TOTAL_KEY);
			return GuildKeys.GUILD_DONATE_TOTAL_KEY;
		}
		return null;
	}

	/** 联盟解散清除联盟贡献信息
	 * 
	 * @param playerId
	 * @param type
	 * @return */
	public boolean removeGuildDonateInfo(String guildId, DonateRankType type) {
		String key = getGuildDonateRankKey(type) + ":" + guildId;
		return redisSession.delete(key, false);
	}

	/** 清除玩家联盟捐献信息
	 * 
	 * @param playerId
	 * @param type
	 * @return */
	public boolean removePlayerGuildDonate(String guildId, String playerId) {
		String key = getGuildDonateRankKey(DonateRankType.DAILY_RANK) + ":" + guildId;
		redisSession.hDel(key, playerId);
		key = getGuildDonateRankKey(DonateRankType.WEEK_RANK) + ":" + guildId;
		redisSession.hDel(key, playerId);
		key = getGuildDonateRankKey(DonateRankType.TOTAL_RANK) + ":" + guildId;
		redisSession.hDel(key, playerId);
		return true;

	}

	/** 更新玩家联盟贡献信息
	 * 
	 * @param playerId
	 * @param type
	 * @param count
	 * @return */
	public boolean updatePlayerGuildDonate(String guildId, String playerId, String dailyVal, String weekVal, String totalVal) {
		String key = getGuildDonateRankKey(DonateRankType.DAILY_RANK) + ":" + guildId;
		redisSession.hSet(key, playerId, dailyVal);
		key = getGuildDonateRankKey(DonateRankType.WEEK_RANK) + ":" + guildId;
		redisSession.hSet(key, playerId, weekVal);
		key = getGuildDonateRankKey(DonateRankType.TOTAL_RANK) + ":" + guildId;
		redisSession.hSet(key, playerId, totalVal);

		return true;
	}

	/** 获取联盟贡献信息 */
	public Map<String, String> getGuildDonateInfo(String guild, DonateRankType type) {
		String key = getGuildDonateRankKey(type) + ":" + guild;
		return redisSession.hGetAll(key);
	}

	/** 获取国王礼包个数 */
	public int getGiftNumber(int giftId) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_GIFT_NUMBER_KEY);

		String value = redisSession.hGet(PRESIDENT_GIFT_NUMBER_KEY, String.valueOf(giftId));
		if (!HawkOSOperator.isEmptyString(value)) {
			return Integer.parseInt(value);
		}

		return 0;
	}

	/** 国王礼包个数更新
	 * 
	 * @param giftId
	 * @param number
	 * @return */
	public void updateGiftNumber(int giftId, int number) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_GIFT_NUMBER_KEY);

		redisSession.hSet(PRESIDENT_GIFT_NUMBER_KEY, String.valueOf(giftId), String.valueOf(number));
	}

	/** 删除国王礼包个数 */
	public void deleteAllGiftNumber() {
		StatisManager.getInstance().incRedisKey(PRESIDENT_GIFT_NUMBER_KEY);

		redisSession.delete(PRESIDENT_GIFT_NUMBER_KEY, false);
	}

	/** 获取国王礼包发送记录数据
	 * 
	 * @param playerId
	 * @return */
	public String getGiftSend(String playerId) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_GIFT_SEND_KEY);

		return redisSession.hGet(PRESIDENT_GIFT_SEND_KEY, playerId);
	}

	/** 获取国王礼包发送记录数据 */
	public Map<String, String> getAllGiftSend() {
		StatisManager.getInstance().incRedisKey(PRESIDENT_GIFT_SEND_KEY);

		return redisSession.hGetAll(PRESIDENT_GIFT_SEND_KEY);
	}

	/** 国王礼包发送记录数据更新
	 * 
	 * @param playerId
	 * @param dataJson
	 * @return */
	public void updateGiftSend(String playerId, String dataJson) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_GIFT_SEND_KEY);

		redisSession.hSet(PRESIDENT_GIFT_SEND_KEY, playerId, dataJson);
	}

	/** 删除国王礼包发送记录数据 */
	public void deleteAllGiftSend() {
		StatisManager.getInstance().incRedisKey(PRESIDENT_GIFT_SEND_KEY);

		redisSession.delete(PRESIDENT_GIFT_SEND_KEY, false);
	}

	/** 添加当选国王记录
	 * 
	 * @param info */
	public void addElectedPresident(PresidentHistory.Builder builder) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_HISTORY_KEY);

		redisSession.lPush(PRESIDENT_HISTORY_KEY.getBytes(), 0, builder.build().toByteArray());
	}

	/** 获取历届国王记录
	 * 
	 * @param maxCount
	 * @return */
	public List<PresidentHistory.Builder> getElectedPresident(int maxCount) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_HISTORY_KEY);

		List<PresidentHistory.Builder> builderList = new LinkedList<PresidentHistory.Builder>();
		try {
			List<byte[]> infoList = redisSession.lRange(PRESIDENT_HISTORY_KEY.getBytes(), 0, maxCount - 1, 0);
			if (infoList != null) {
				for (byte[] info : infoList) {
					PresidentHistory.Builder builder = PresidentHistory.newBuilder();
					builder.mergeFrom(info);
					builderList.add(builder);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return builderList;
	}

	/** 添加国王战事件
	 * 
	 * @param info */
	public void addPresidentEvent(PresidentEvent.Builder builder) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_EVENT_KEY);

		redisSession.lPush(PRESIDENT_EVENT_KEY.getBytes(), 0, builder.build().toByteArray());
	}

	/** 获取国王战事件列表
	 * 
	 * @param maxCount
	 * @return */
	public List<PresidentEvent.Builder> getPresidentEvent(int maxCount) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_EVENT_KEY);

		List<PresidentEvent.Builder> builderList = new LinkedList<PresidentEvent.Builder>();
		try {
			List<byte[]> infoList = redisSession.lRange(PRESIDENT_EVENT_KEY.getBytes(), 0, maxCount - 1, 0);
			if (infoList != null) {
				for (byte[] info : infoList) {
					PresidentEvent.Builder builder = PresidentEvent.newBuilder();
					builder.mergeFrom(info);
					builderList.add(builder);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return builderList;
	}

	/** 清空国王战事件 */
	public void clearPresidentEvent() {
		StatisManager.getInstance().incRedisKey(PRESIDENT_EVENT_KEY);

		redisSession.lTrim(PRESIDENT_EVENT_KEY, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/** 添加国王战箭塔事件
	 * 
	 * @param info */
	public void addPresidentTowerEvent(PresidentEvent.Builder builder, int towerIndex) {
		StatisManager.getInstance().incRedisKey(PRESIDEN_TOWER_EVENT_KEY);

		String info = JsonFormat.printToString(builder.build());
		redisSession.lPush(PRESIDEN_TOWER_EVENT_KEY + ":" + towerIndex, 0, info);
	}

	/** 获取国王战箭塔事件列表
	 * 
	 * @param maxCount
	 * @return */
	public List<PresidentEvent.Builder> getPresidentTowerEvent(int maxCount, int towerIndex) {
		StatisManager.getInstance().incRedisKey(PRESIDEN_TOWER_EVENT_KEY);

		List<PresidentEvent.Builder> builderList = new LinkedList<PresidentEvent.Builder>();
		try {
			List<String> infoList = redisSession.lRange(PRESIDEN_TOWER_EVENT_KEY + ":" + towerIndex, 0, maxCount - 1, 0);
			if (infoList != null) {
				for (String info : infoList) {
					PresidentEvent.Builder builder = PresidentEvent.newBuilder();
					JsonFormat.merge(info, builder);
					builderList.add(builder);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return builderList;
	}

	/** 清空国王战箭塔事件 */
	public void clearPresidentTowerEvent(int towerIndex) {
		StatisManager.getInstance().incRedisKey(PRESIDEN_TOWER_EVENT_KEY);

		redisSession.lTrim(PRESIDEN_TOWER_EVENT_KEY + ":" + towerIndex, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/** 添加官职记录信息 */
	public void addOfficerRecord(int period, OfficerRecord.Builder builder) {
		StatisManager.getInstance().incRedisKey(OFFICER_RECORD_KEY);

		String key = OFFICER_RECORD_KEY + ":" + period;
		String idx = String.valueOf(redisSession.hLen(key, false));
		redisSession.hSet(key, idx, JsonFormat.printToString(builder.build()));
	}

	/** 获取官职记录信息 */
	public List<String> getOfficerRecord(int period) {
		StatisManager.getInstance().incRedisKey(OFFICER_RECORD_KEY);

		String key = OFFICER_RECORD_KEY + ":" + period;
		return redisSession.hVals(key);
	}

	public void addOrUpdatePresidentResource(PresidentResourceInfoSyn.Builder sbuilder) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_RESOURCE);

		redisSession.setBytes(PRESIDENT_RESOURCE, sbuilder.build().toByteArray());
	}

	public PresidentResourceInfoSyn.Builder getPresidentResource() {
		StatisManager.getInstance().incRedisKey(PRESIDENT_RESOURCE);
		try {
			byte[] result = redisSession.getBytes(PRESIDENT_RESOURCE.getBytes());
			if (result != null) {
				return PresidentResourceInfoSyn.newBuilder().mergeFrom(result);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/** 添加征税记录信息 */
	public void addTaxGuildRecord(int period, String guildId, TaxGuildRecord.Builder builder) {
		StatisManager.getInstance().incRedisKey(TAX_GUILD_INFO_KEY);

		String key = TAX_GUILD_RECORD_KEY + ":" + period;
		redisSession.hSet(key, guildId, JsonFormat.printToString(builder.build()));
	}

	/** 获取征税记录信息 */
	public List<String> getTaxGuildRecord(int period) {
		StatisManager.getInstance().incRedisKey(TAX_GUILD_INFO_KEY);

		String key = TAX_GUILD_RECORD_KEY + ":" + period;
		return redisSession.hVals(key);
	}

	/** 清除所有征税信息 */
	public void clearTaxGuildInfo() {
		StatisManager.getInstance().incRedisKey(TAX_GUILD_INFO_KEY);

		redisSession.delete(TAX_GUILD_INFO_KEY, false);
	}

	/** 更新征税信息 */
	public void addTaxGuildInfo(String guildId, String timestamp) {
		StatisManager.getInstance().incRedisKey(TAX_GUILD_INFO_KEY);

		redisSession.hSet(TAX_GUILD_INFO_KEY, guildId, timestamp);
	}

	/** 获取征税信息 */
	public String getTaxGuildInfo(String guildId) {
		StatisManager.getInstance().incRedisKey(TAX_GUILD_INFO_KEY);

		String key = TAX_GUILD_INFO_KEY;
		return redisSession.hGet(key, guildId);
	}

	/** 根据key, 获取单个超级武器数据
	 * 
	 * @param key
	 * @return */
	public <T> T getAbstractSuperWeaponDataByKey(int pointId, String key, Class<T> type) {
		StatisManager.getInstance().incRedisKey(ABSTRACT_SUPERWEAPON_DATA_KEY);
		byte[] dataBytes = redisSession.hGetBytes(ABSTRACT_SUPERWEAPON_DATA_KEY + ":" + pointId, key);
		if (dataBytes != null && dataBytes.length > 0) {
			return HawkSerializer.deserialize(dataBytes, type);
		}
		return null;
	}

	/** 更新单个超级武器
	 * 
	 * @return */
	public void updateAbstractSuperWeaponDataByKey(int pointId, String key, Object val) {
		StatisManager.getInstance().incRedisKey(ABSTRACT_SUPERWEAPON_DATA_KEY);

		String hKey = ABSTRACT_SUPERWEAPON_DATA_KEY + ":" + pointId;
		byte[] data = val == null ? new byte[0] : HawkSerializer.serialize(val);
		redisSession.hSetBytes(hKey, key, data);
	}

	/** 清除单个超级武器信息
	 * 
	 * @param pointId */
	public void clearAbstractSWData(int pointId) {
		StatisManager.getInstance().incRedisKey(ABSTRACT_SUPERWEAPON_DATA_KEY);

		String key = ABSTRACT_SUPERWEAPON_DATA_KEY + ":" + pointId;
		redisSession.delete(key, true);
	}

	/** 根据key, 获取超级武器数据
	 * 
	 * @param key
	 * @return */
	public <T> T getSuperWeaponDataByKey(String key, Class<T> type) {
		StatisManager.getInstance().incRedisKey(SUPERWEAPON_DATA_KEY);

		byte[] dataBytes = redisSession.hGetBytes(SUPERWEAPON_DATA_KEY, key);
		if (dataBytes != null && dataBytes.length > 0) {
			return HawkSerializer.deserialize(dataBytes, type);
		}
		return null;
	}

	/** 更新国王战数据
	 * 
	 * @return */
	public void updateSuperWeaponDataByKey(String key, Object val) {
		StatisManager.getInstance().incRedisKey(SUPERWEAPON_DATA_KEY);

		byte[] data = null;
		if (val == null) {
			data = new byte[0];
		} else {
			data = HawkSerializer.serialize(val);
		}

		// 序列化写入
		redisSession.hSetBytes(SUPERWEAPON_DATA_KEY, key, data);
	}

	/** 根据key, 获取国王战数据
	 * 
	 * @param key
	 * @return */
	public <T> T getPresidentDataByKey(String key, Class<T> type) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_DATA_KEY);

		byte[] dataBytes = redisSession.hGetBytes(PRESIDENT_DATA_KEY, key);
		if (dataBytes != null && dataBytes.length > 0) {
			return HawkSerializer.deserialize(dataBytes, type);
		}

		return null;
	}

	/** 更新国王战数据
	 * 
	 * @return */
	public void updatePresidentDataByKey(String key, Object val) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_DATA_KEY);

		byte[] data = null;
		if (val == null) {
			data = new byte[0];
		} else {
			data = HawkSerializer.serialize(val);
		}

		// 序列化写入
		redisSession.hSetBytes(PRESIDENT_DATA_KEY, key, data);
	}

	/** 获取国王战箭塔数据 */
	public PresidentTower getPresidentTower(int index) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_TOWER_DATA_KEY);

		String key = PRESIDENT_TOWER_DATA_KEY + ":" + index;
		byte[] dataBytes = redisSession.getBytes(key.getBytes());
		if (dataBytes != null && dataBytes.length > 0) {
			if (GsConfig.getInstance().isGzPlayerData()) {
				dataBytes = HawkZlib.zlibInflate(dataBytes);
			}
			PresidentTower tower = HawkSerializer.deserialize(dataBytes, PresidentTower.class);
			return tower;
		}

		return null;
	}

	/** 更新国王战箭塔数据
	 * 
	 * @return */
	public boolean updatePresidentTower(PresidentTower city, int index) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_TOWER_DATA_KEY);

		String key = PRESIDENT_TOWER_DATA_KEY + ":" + index;
		// 序列化写入
		byte[] data = HawkSerializer.serialize(city);
		if (GsConfig.getInstance().isGzPlayerData()) {
			data = HawkZlib.zlibDeflate(data);
		}

		return redisSession.setBytes(key, data);
	}

	/** 添加公共频道禁言玩家（存在则覆盖）
	 * 
	 * @param playerName
	 *            : 玩家名称
	 * @param startTime
	 *            : 开始时间
	 * @param endTime
	 *            : 结束时间 */
	public boolean addSilentPlayer(String playerId, long startTime, long endTime) {
		StatisManager.getInstance().incRedisKey(WORLD_CHAT_SILENT_KEY);

		String key = WORLD_CHAT_SILENT_KEY + ":" + playerId;
		JSONObject json = new JSONObject();
		json.put("startTime", startTime);
		json.put("endTime", endTime);
		redisSession.setString(key, json.toString(), GsConfig.getInstance().getPlayerRedisExpire());
		return true;
	}

	/** 获得公共频道禁言玩家数据
	 * 
	 * @param info */
	public JSONObject getSilentPlayer(String playerId) {
		StatisManager.getInstance().incRedisKey(WORLD_CHAT_SILENT_KEY);

		String key = WORLD_CHAT_SILENT_KEY + ":" + playerId;
		String data = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(data)) {
			return JSONObject.parseObject(data);
		}

		return null;
	}

	/** 更新禁言时间 */
	public boolean updateSilentTime(String playerId, long endTime) {
		StatisManager.getInstance().incRedisKey(WORLD_CHAT_SILENT_KEY);

		JSONObject json = getSilentPlayer(playerId);
		if (json != null) {
			json.put("endTime", endTime);
			String key = WORLD_CHAT_SILENT_KEY + ":" + playerId;
			redisSession.setString(key, json.toString(), GsConfig.getInstance().getPlayerRedisExpire());
			return true;
		}

		return false;
	}

	/** 添加交互玩家 */
	public long addInteractivePlayer(String playerId, String interPlayerId, long time) {
		if (interPlayerId.startsWith(BattleService.NPC_ID)) {
			return 0;
		}
		StatisManager.getInstance().incRedisKey(INTERACTIVE_PLAYERS);

		String key = INTERACTIVE_PLAYERS + ":" + playerId;
		long ret = redisSession.zAdd(key, -time, interPlayerId, GsConfig.getInstance().getPlayerRedisExpire());
		if (ret > 0 && redisSession.zCount(key, Long.MIN_VALUE, 0) > 50) {
			redisSession.zRemrangeByRank(key, 50, -1);
		}

		return ret;
	}

	/** 获取交互玩家 */
	public Set<String> getInteractivePlayers(String playerId) {
		StatisManager.getInstance().incRedisKey(INTERACTIVE_PLAYERS);

		String key = INTERACTIVE_PLAYERS + ":" + playerId;
		return redisSession.zRange(key, 0, -1, 0);
	}

	/** 添加聊天室的聊天数据
	 *
	 * @param roomId
	 * @param builder
	 * @return */
	public boolean addChatMessage(String roomId, ChatData.Builder chatData) {
		StatisManager.getInstance().incRedisKey(CHAT_MESSAGE);

		try {
			String key = CHAT_MESSAGE + ":" + roomId;
			int expireSeconds = ConstProperty.getInstance().getMailEffectTime();
			long count = redisSession.rPushBytes(key, chatData.build().toByteArray());// 插入到列表的尾部
			redisSession.expire(key, expireSeconds);
			if (count > ConstProperty.getInstance().getChatMessage()) {
				redisSession.lPop(key, true); // 删除头部元素
			}
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return false;
	}

	/** 获取聊天室的聊天数据
	 * 
	 * @param roomId
	 * @return */
	public ChatMessage.Builder getChatMessage(String roomId) {
		StatisManager.getInstance().incRedisKey(CHAT_MESSAGE);

		try {
			String key = CHAT_MESSAGE + ":" + roomId;
			List<byte[]> list = redisSession.lRange(key.getBytes(), 0, -1, 0);
			ChatMessage.Builder builder = ChatMessage.newBuilder();
			for (byte[] str : list) {
				ChatData.Builder chatData = ChatData.newBuilder();
				chatData.mergeFrom(str);
				builder.addChat(chatData);
			}
			return builder;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/** 新增或更新玩家聊天室
	 *
	 * @param playerId
	 * @param builder
	 * @return */
	public void saveOrUpdatePlayerChatRoom(String playerId, PlayerChatRoom.Builder builder) {
		String key = keyChatRoom(playerId);
		int expireSeconds = ConstProperty.getInstance().getMailEffectTime();
		redisSession.hSetBytes(key, builder.getRoomId(), builder.build().toByteArray(), expireSeconds);
	}

	private String keyChatRoom(String playerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_CHAT_ROOM_MAP);

		return PLAYER_CHAT_ROOM_MAP + ":" + playerId;
	}

	/** 增加shop购买计数(天) */
	public void incrementShopBuyCount(final String playerId, final int shopId, final int count) {
		StatisManager.getInstance().incRedisKey(SHOP_BUY_COUNT);

		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			String key = SHOP_BUY_COUNT + ":" + playerId + ":" + HawkTime.getYearDay();
			pip.hincrBy(key, String.valueOf(shopId), count);
			pip.expire(key, (int) TimeUnit.DAYS.toSeconds(1));

			String totalBuyKey = SHOP_BUY_COUNT + ":" + playerId;
			pip.hincrBy(totalBuyKey, String.valueOf(shopId), count);

			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 取得商品购买数量 */
	public ImmutableMap<Integer, Integer> shopBuyCountTotal(final String playerId, int... fields) {
		StatisManager.getInstance().incRedisKey(SHOP_BUY_COUNT);

		final String key = SHOP_BUY_COUNT + ":" + playerId;
		return shopBuyCount(key, fields);
	}

	/** 取得商品购买数量(天) */
	public ImmutableMap<Integer, Integer> shopBuyCountDay(final String playerId, int... fields) {
		StatisManager.getInstance().incRedisKey(SHOP_BUY_COUNT);

		final int dayOfYear = HawkTime.getYearDay();
		final String key = SHOP_BUY_COUNT + ":" + playerId + ":" + dayOfYear;
		return shopBuyCount(key, fields);
	}

	private ImmutableMap<Integer, Integer> shopBuyCount(final String key, int... fields) {
		try {
			String[] shopIds = Arrays.stream(fields).mapToObj(Integer::toString).toArray(String[]::new);
			List<String> buyCount = redisSession.hmGet(key, shopIds);
			int[] count = buyCount.stream().mapToInt(s -> s == null ? 0 : Integer.parseInt(s)).toArray();
			Map<Integer, Integer> result = new HashMap<>();
			for (int i = 0; i < fields.length; i++) {
				result.put(fields[i], count[i]);
			}
			return ImmutableMap.copyOf(result);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return ImmutableMap.of();
	}

	/** 获取玩家聊天室,并按创建时间排序
	 * 
	 * @param playerId
	 * @return */
	public List<PlayerChatRoom.Builder> getPlayerChatRooms(String playerId) {
		try {
			String key = keyChatRoom(playerId);
			List<byte[]> values = redisSession.hValsBytes(key);
			List<PlayerChatRoom.Builder> result = new ArrayList<>(values.size());
			for (byte[] val : values) {
				PlayerChatRoom.Builder roomBuilder = PlayerChatRoom.newBuilder();
				roomBuilder.mergeFrom(val);
				result.add(roomBuilder);
			}
			result.sort(Comparator.comparingLong(PlayerChatRoom.Builder::getLastMsg).reversed());
			return result;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return Collections.emptyList();
	}

	/** 获取玩家聊天室数量
	 * 
	 * @param playerId
	 * @return */
	public long countPlayerChatRooms(String playerId) {
		return redisSession.hLen(keyChatRoom(playerId), true);
	}

	/** 获取玩家聊天室 */
	public Optional<PlayerChatRoom.Builder> getPlayerChatRoom(String playerId, String roomId) {
		try {
			String key = keyChatRoom(playerId);
			byte[] value = redisSession.hGetBytes(key, roomId);
			if (Objects.isNull(value)) {
				return Optional.empty();
			}

			PlayerChatRoom.Builder roomBuilder = PlayerChatRoom.newBuilder();
			roomBuilder.mergeFrom(value);
			return Optional.of(roomBuilder);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return Optional.empty();
	}

	/** 删除玩家最早聊天室
	 * 
	 * @param playerId
	 * @return */
	public Optional<PlayerChatRoom.Builder> delPlayerChatRoom(String playerId) {
		List<PlayerChatRoom.Builder> chatRooms = this.getPlayerChatRooms(playerId);
		if (chatRooms.isEmpty()) {
			return Optional.empty();
		}

		String key = keyChatRoom(playerId);
		PlayerChatRoom.Builder toDel = chatRooms.get(0);
		return redisSession.hDelBytes(key, toDel.getRoomId().getBytes()) == 1 ? Optional.of(toDel) : Optional.empty();
	}

	/** 指定删除玩家聊天室
	 * 
	 * @param playerId
	 * @param chatRoom
	 * @return */
	public long delPlayerChatRoom(String playerId, String... roomId) {
		if (roomId.length == 0) {
			return 0;
		}

		String key = keyChatRoom(playerId);
		byte[][] toDel = Arrays.asList(roomId).stream().map(String::getBytes).collect(Collectors.toList()).toArray(new byte[0][0]);
		return redisSession.hDelBytes(key, toDel);
	}

	/** 添加聊天室信息
	 * 
	 * @param roomId
	 * @param builder
	 * @return */
	public boolean addChatRoomData(String roomId, ChatRoomData.Builder builder) {
		StatisManager.getInstance().incRedisKey(CHAT_ROOM_DATA);

		int expireSeconds = ConstProperty.getInstance().getMailEffectTime();
		String key = CHAT_ROOM_DATA + ":" + roomId;
		return redisSession.setBytes(key, builder.build().toByteArray(), expireSeconds);
	}

	/** 获取聊天室信息
	 * 
	 * @param roomId
	 * @return */
	public ChatRoomData.Builder getChatRoomData(String roomId) {
		StatisManager.getInstance().incRedisKey(CHAT_ROOM_DATA);

		try {
			String key = CHAT_ROOM_DATA + ":" + roomId;
			byte[] value = redisSession.getBytes(key.getBytes());
			if (Objects.nonNull(value)) {
				ChatRoomData.Builder builder = ChatRoomData.newBuilder();
				builder.mergeFrom(value);
				return builder;
			}
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/** 删除聊天信息和聊天室数据
	 * 
	 * @param roomId */
	public void delChatRoom(String roomId) {
		StatisManager.getInstance().incRedisKey(CHAT_MESSAGE);
		StatisManager.getInstance().incRedisKey(CHAT_ROOM_DATA);

		// 清理聊天信息数据
		redisSession.delete(CHAT_MESSAGE + ":" + roomId, true);
		// 清理聊天室数据
		redisSession.delete(CHAT_ROOM_DATA + ":" + roomId, true);
	}

	/** 获取行军加速消耗
	 * 
	 * @param marchId
	 * @return */
	public List<MarchSpeedItem> getMarchSpeedConsume(String marchId) {
		StatisManager.getInstance().incRedisKey(MARCH_SPEED_CONSUME);

		String key = MARCH_SPEED_CONSUME + ":" + marchId;
		String consumeInfo = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(consumeInfo)) {
			return MarchSpeedItem.formatListOf(consumeInfo);
		}

		return null;
	}

	/** 更新行军加速消耗
	 * 
	 * @param marchId
	 * @param consumeInfo
	 *            物品消耗信息
	 * @param liveTime
	 *            数据存活时间 */
	public void updateMarchSpeedConsume(String marchId, String consumeInfo, long liveTime) {
		StatisManager.getInstance().incRedisKey(MARCH_SPEED_CONSUME);

		String key = MARCH_SPEED_CONSUME + ":" + marchId;
		redisSession.setEx(key, (int) (liveTime * 3 / 1000), consumeInfo);
	}

	/** 删除行军消耗
	 * 
	 * @param marchId */
	public void removeMarchSpeedConsume(String marchId) {
		StatisManager.getInstance().incRedisKey(MARCH_SPEED_CONSUME);
		String key = MARCH_SPEED_CONSUME + ":" + marchId;
		redisSession.delete(key, false);
	}

	/** 更新全服调查问卷信息
	 * 
	 * @param questionnaires
	 * @return */
	public boolean updateGlobalQuestionnaire(PushSurverInfo questionnaires) {
		StatisManager.getInstance().incRedisKey(QUESTIONNAIRE_VERSION_KEY);

		String key = QUESTIONNAIRE_KEY;
		redisSession.setBytes(key, questionnaires.toByteArray());
		// 刷新推送问卷版本
		String version = HawkOSOperator.randomUUID();
		redisSession.setString(QUESTIONNAIRE_VERSION_KEY, version);
		return true;
	}

	/** 获取全服调查问卷
	 * 
	 * @return */
	public PushSurverInfo getAllGlobalQuestionnaires() {
		StatisManager.getInstance().incRedisKey(QUESTIONNAIRE_KEY);
		try {
			byte[] dataBytes = redisSession.getBytes(QUESTIONNAIRE_KEY.getBytes());
			if (dataBytes != null && dataBytes.length > 0) {
				return PushSurverInfo.parseFrom(dataBytes);
			}
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/** 获取推送问卷版本
	 * 
	 * @return */
	public String getQuestionnaireVersion() {
		StatisManager.getInstance().incRedisKey(QUESTIONNAIRE_VERSION_KEY);
		return redisSession.getString(QUESTIONNAIRE_VERSION_KEY);
	}

	/** 更新玩家战斗数据到缓存 */
	public boolean updatePlayerBattleLog(String playerId, String battleLog) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		StatisManager.getInstance().incRedisKey(BATTLE_LOG_KEY);

		String key = BATTLE_LOG_KEY + ":" + playerId;
		if (redisSession.setString(key, battleLog, GsConst.HOUR_MILLI_SECONDS / 1000)) {
			HawkLog.logPrintln("player battle log success, playerId: {}, size: {}", playerId, battleLog.length());
		}

		return false;
	}

	/** 获取玩家战斗日志缓存数据 */
	public String getPlayerBattleLog(String playerId) {
		StatisManager.getInstance().incRedisKey(BATTLE_LOG_KEY);

		String key = BATTLE_LOG_KEY + ":" + playerId;
		return redisSession.getString(key);
	}

	/** 记录今日联盟仓库存量 */
	public void incrementWarehouseStoreCount(String playerId, int totalWeight) {
		StatisManager.getInstance().incRedisKey(WARE_HOUSE_DAY);

		String key = WARE_HOUSE_DAY + ":" + playerId;
		redisSession.hIncrBy(key, String.valueOf(HawkTime.getYearDay()), totalWeight, (int) TimeUnit.DAYS.toSeconds(1));

	}

	/** 取得当天仓库使用量 */
	public int warehouseStoreCount(final String playerId) {
		StatisManager.getInstance().incRedisKey(WARE_HOUSE_DAY);

		String key = WARE_HOUSE_DAY + ":" + playerId;
		String str = redisSession.hGet(key, String.valueOf(HawkTime.getYearDay()));
		if (!HawkOSOperator.isEmptyString(str)) {
			return Integer.parseInt(str);
		}

		return 0;
	}

	/** 记录今日掠夺资源量 */
	public void incrementGrabResWeightDayCount(String playerId, int resId, int totalWeight) {
		StatisManager.getInstance().incRedisKey(GRAB_RES_LOST_DAY);

		String key = GRAB_RES_LOST_DAY + ":" + playerId + ":" + HawkTime.getYearDay();
		redisSession.hIncrBy(key, String.valueOf(resId), totalWeight, (int) TimeUnit.DAYS.toSeconds(1));

		GuildRankMgr.getInstance().onRobRes(playerId, totalWeight);
	}

	/** 今日掠夺资源总量 */
	public ImmutableMap<Integer, Integer> grabResWeightDayCount(final String playerId) {
		StatisManager.getInstance().incRedisKey(GRAB_RES_LOST_DAY);

		String key = GRAB_RES_LOST_DAY + ":" + playerId + ":" + HawkTime.getYearDay();
		Map<String, String> str = redisSession.hGetAll(key);
		Map<Integer, Integer> result = new HashMap<>(str.size());
		for (Entry<String, String> ent : str.entrySet()) {
			Integer resId = Integer.valueOf(ent.getKey());
			Integer val = Integer.valueOf(ent.getValue());
			result.put(resId, val);
		}
		return ImmutableMap.copyOf(result);
	}

	/** 领取随机宝箱 */
	public void openRandomChest(String playerId) {
		StatisManager.getInstance().incRedisKey(RANDOM_CHEST_KEY);

		redisSession.hSet(RANDOM_CHEST_KEY, playerId, String.valueOf(HawkTime.getMillisecond()));
	}

	/** 上次领取随机宝箱 */
	public long lastOpenRandomChest(final String playerId) {
		StatisManager.getInstance().incRedisKey(RANDOM_CHEST_KEY);

		String str = redisSession.hGet(RANDOM_CHEST_KEY, playerId);
		if (!HawkOSOperator.isEmptyString(str)) {
			return NumberUtils.toLong(str);
		}

		return 0;
	}

	/** 获取玩家收藏夹的数量
	 * 
	 * @param playerId
	 * @return */
	public long getFavoriteCount(String playerId) {
		StatisManager.getInstance().incRedisKey(FAVORITE_KEY);

		String key = FAVORITE_KEY + ":" + playerId;
		return redisSession.hLen(key, false);
	}

	/** 获取玩家所有收藏夹数据并反序列号为对象
	 * 
	 * @param playerId
	 * @return */
	public List<WorldFavoritePB.Builder> getWorldFavorite(String playerId) {
		StatisManager.getInstance().incRedisKey(FAVORITE_KEY);

		List<WorldFavoritePB.Builder> favoriteList = new LinkedList<WorldFavoritePB.Builder>();
		String key = FAVORITE_KEY + ":" + playerId;
		Map<String, String> favoriteMap = redisSession.hGetAll(key);
		for (String favoriteInfo : favoriteMap.values()) {
			try {
				WorldFavoritePB.Builder builder = WorldFavoritePB.newBuilder();
				JsonFormat.merge(favoriteInfo, builder);
				favoriteList.add(builder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		return favoriteList;
	}

	/** 添加收藏夹信息
	 * 
	 * @param playerId
	 * @param favorite
	 */
	public void addWorldFavorite(String playerId, WorldFavoritePB.Builder favorite, int expire) {
		StatisManager.getInstance().incRedisKey(FAVORITE_KEY);

		String key = FAVORITE_KEY + ":" + playerId;
		String value = JsonFormat.printToString(favorite.build());
		redisSession.hSet(key, favorite.getFavoriteId(), value, expire);
	}

	/** 删除收藏夹数据
	 * 
	 * @param playerId
	 * @param favoriteId */
	public void deleteWorldFavorite(String playerId, String... favoriteId) {
		StatisManager.getInstance().incRedisKey(FAVORITE_KEY);

		if (favoriteId == null || favoriteId.length == 0) {
			return;
		}

		String key = FAVORITE_KEY + ":" + playerId;
		redisSession.hDel(key, favoriteId);
	}

	public boolean addOrUpdateTravelShop(String playerId, TravelShopInfoSync.Builder infoBuilder) {
		StatisManager.getInstance().incRedisKey(TRAVEL_SHOP);

		String key = TRAVEL_SHOP + ":" + playerId;
		return redisSession.setBytes(key, infoBuilder.build().toByteArray(), GsConfig.getInstance().getPlayerRedisExpire());
	}

	public TravelShopInfoSync.Builder getTravelShopInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(TRAVEL_SHOP);

		try {
			String key = TRAVEL_SHOP + ":" + playerId;
			byte[] data = redisSession.getBytes(key.getBytes(), GsConfig.getInstance().getPlayerRedisExpire());
			if (data != null) {
				TravelShopInfoSync.Builder builder = TravelShopInfoSync.newBuilder().mergeFrom(data);

				return builder;
			}
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/** 添加已完成本期尤里复仇活动的联盟id
	 * 
	 * @param stage
	 * @param guildId
	 * @return */
	public long addYuriRevengeFinishGuild(int stage, String guildId) {
		StatisManager.getInstance().incRedisKey(YURI_REVENGE_FINISH_KEY);

		String key = YURI_REVENGE_FINISH_KEY + stage;
		return redisSession.lPush(key, GameConstCfg.getInstance().getYuriRevengeCachePeriod(), guildId);
	}

	/** 获取已完成本期尤里复仇活动的联盟列表
	 * 
	 * @param stage
	 * @param guildId
	 * @return */
	public List<String> getYuriRevengeFinishGuilds(int stage) {
		StatisManager.getInstance().incRedisKey(YURI_REVENGE_FINISH_KEY);

		String key = YURI_REVENGE_FINISH_KEY + stage;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		return redisSession.lRange(key, 0, -1, expireSecond);
	}

	/** 添加已发放本期尤里复仇积分奖励的玩家id
	 * 
	 * @param stage
	 * @param playerId
	 * @return */
	public long addYuriRevengeRewardedPlayer(int stage, String playerId) {
		StatisManager.getInstance().incRedisKey(YURI_REVENGE_REWARDED_KEY);

		String key = YURI_REVENGE_REWARDED_KEY + stage;
		int expireSecond = GameConstCfg.getInstance().getYuriRevengeCachePeriod();
		return redisSession.lPush(key, expireSecond, playerId);
	}

	/** 获取已发放本期尤里复仇积分奖励的玩家列表
	 * 
	 * @param stage
	 * @param playerId
	 * @return */
	public List<String> getYuriRevengeRewardedPlayers(int stage) {
		StatisManager.getInstance().incRedisKey(YURI_REVENGE_REWARDED_KEY);

		String key = YURI_REVENGE_REWARDED_KEY + stage;
		int expireSecond = GameConstCfg.getInstance().getYuriRevengeCachePeriod();
		return redisSession.lRange(key, 0, -1, expireSecond);
	}

	/** 获取尤里复仇活动积分排行key值
	 * 
	 * @param stage
	 * @param rankType
	 * @return */
	private String getYuriRevengeRankKey(int stage, YuriRankType rankType) {
		String key = "";
		switch (rankType) {
		case GUILD_RANK:
			key = YURI_REVENGE_GUILD_RANK;
			StatisManager.getInstance().incRedisKey(YURI_REVENGE_GUILD_RANK);
			break;
		case SELF_RANK:
			key = YURI_REVENGE_SELF_RANK;
			StatisManager.getInstance().incRedisKey(YURI_REVENGE_SELF_RANK);
			break;
		}
		return key + stage;
	}

	/** 更新尤里复仇活动积分
	 * 
	 * @param stage
	 * @param rankType
	 * @param id
	 * @return */
	public long updateYuriRevengeRankScore(int stage, YuriRankType rankType, String id, long score) {
		String key = getYuriRevengeRankKey(stage, rankType);
		redisSession.zAdd(key, score, id, GameConstCfg.getInstance().getYuriRevengeCachePeriod());
		return 0;
	}

	/** 移除排行积分
	 * 
	 * @param stage
	 * @param rankType
	 * @param id */
	public void removeYuriRevengeRankScore(int stage, YuriRankType rankType, String id) {
		String key = getYuriRevengeRankKey(stage, rankType);
		int expireSecond = GameConstCfg.getInstance().getYuriRevengeCachePeriod();
		redisSession.zRem(key, expireSecond, id);
	}

	/** 获取尤里复仇活动排行和积分
	 * 
	 * @param stage
	 * @param rankType
	 * @param id
	 * @return */
	public long getYuriRevengeRankScore(int stage, YuriRankType rankType, String id) {
		String key = getYuriRevengeRankKey(stage, rankType);
		int expireSecond = GameConstCfg.getInstance().getYuriRevengeCachePeriod();
		Double zScore = redisSession.zScore(key, id, expireSecond);
		return Objects.nonNull(zScore) ? zScore.longValue() : 0;
	}

	/** 获取尤里复仇活动排行名次(index + 1)
	 * 
	 * @param stage
	 * @param rankType
	 * @param id
	 * @return */
	public long getYuriRevengeRank(int stage, YuriRankType rankType, String id) {
		String key = getYuriRevengeRankKey(stage, rankType);
		int expireSecond = GameConstCfg.getInstance().getYuriRevengeCachePeriod();
		Long zrevrank = redisSession.zrevrank(key, id, expireSecond);
		return Objects.nonNull(zrevrank) ? zrevrank.longValue() + 1 : -1;
	}

	/** 获取尤里复仇积分排行
	 * 
	 * @param stage
	 * @param rankType
	 * @return */
	public Set<Tuple> getYuriScoreRankScore(int stage, YuriRankType rankType, int rankCount) {
		String key = getYuriRevengeRankKey(stage, rankType);
		int expireSecond = GameConstCfg.getInstance().getYuriRevengeCachePeriod();
		return redisSession.zRevrangeWithScores(key, 0, rankCount, expireSecond);
	}

	/** 更新城内冒泡奖励信息
	 * 
	 * @return */
	public void updateBubbleRewardInfo(String playerId, BubbleRewardInfo bubbleInfo) {
		StatisManager.getInstance().incRedisKey(BUBBLE_INFO_KEY);

		String key = BUBBLE_INFO_KEY + ":" + playerId;
		redisSession.hSet(key, String.valueOf(bubbleInfo.getType()), JSONObject.toJSONString(bubbleInfo), GsConfig.getInstance().getPlayerRedisExpire());
	}

	/** 获取城内冒泡奖励信息
	 * 
	 * @return */
	public BubbleRewardInfo getBubbleRewardInfo(String playerId, int type) {
		StatisManager.getInstance().incRedisKey(BUBBLE_INFO_KEY);

		String key = BUBBLE_INFO_KEY + ":" + playerId;
		String value = redisSession.hGet(key, String.valueOf(type));
		if (!HawkOSOperator.isEmptyString(value)) {
			return JSONObject.parseObject(value, BubbleRewardInfo.class);
		}

		return null;
	}

	/** 获取赠送礼物列表
	 * 
	 * @return */
	public List<String> getFriendPresentGift(String playerId) {
		StatisManager.getInstance().incRedisKey(FRIEND_PRESENT_GIFT);

		String key = FRIEND_PRESENT_GIFT + ":" + playerId;
		return redisSession.lRange(key, 0, -1, 0);
	}

	/** 添加赠送礼物
	 * 
	 * @param fromPlayerId
	 * @param toPlayerIdList */
	public void addFriendPresentGift(String fromPlayerId, Collection<String> toPlayerIdList) {
		StatisManager.getInstance().incRedisKey(FRIEND_PRESENT_GIFT);
		String key = FRIEND_PRESENT_GIFT + ":" + fromPlayerId;

		try (Jedis redis = redisSession.getJedis(); Pipeline pip = redis.pipelined()) {
			for (String toPlayerId : toPlayerIdList) {
				pip.lpush(key, toPlayerId);
			}

			pip.sync();

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 清空赠送礼物列表
	 * 
	 * @param fromPlayerId */
	public void clearFriendPresentGift(String fromPlayerId) {
		StatisManager.getInstance().incRedisKey(FRIEND_PRESENT_GIFT);

		String key = FRIEND_PRESENT_GIFT + ":" + fromPlayerId;
		redisSession.lTrim(key, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/** 当日邮件发放数 */
	public void dayMailSendInc(String playerId, MailId mailId) {
		StatisManager.getInstance().incRedisKey(DAY_MAIL_SEND_COUNT);

		final String key = DAY_MAIL_SEND_COUNT + ":" + playerId + ":" + HawkTime.getYearDay();
		redisSession.hIncrBy(key, String.valueOf(mailId.getNumber()), 1, (int) TimeUnit.DAYS.toSeconds(1));
	}

	/** 当天发放补偿邮件数
	 * 
	 * @return */
	public int dayMailSendCount(String playerId, MailId mailId) {
		StatisManager.getInstance().incRedisKey(DAY_MAIL_SEND_COUNT);

		final int dayOfYear = HawkTime.getYearDay();
		final String key = DAY_MAIL_SEND_COUNT + ":" + playerId + ":" + dayOfYear;
		String buyCount = redisSession.hGet(key, String.valueOf(mailId.getNumber()));
		if (!HawkOSOperator.isEmptyString(buyCount)) {
			return NumberUtils.toInt(buyCount);
		}

		return 0;
	}

	/** 添加系统关闭条目
	 * 
	 * @param systemType
	 * @param closeItem */
	public void pushClosedSystemItem(int systemType, String closeItem) {
		StatisManager.getInstance().incRedisKey(SYSTEM_SWITCH);

		String key = SYSTEM_SWITCH + ":" + systemType;
		redisSession.sAdd(key, 0, closeItem);
	}

	/** 获取已关闭的系统条目
	 * 
	 * @param systemType
	 * @param args
	 * @return */
	public Set<String> getClosedSystemItems(int systemType) {
		StatisManager.getInstance().incRedisKey(SYSTEM_SWITCH);

		String key = SYSTEM_SWITCH + ":" + systemType;
		return redisSession.sMembers(key);
	}

	/** 删除已关闭的某个系统条目
	 * 
	 * @param systemType
	 * @param closeItem
	 * @return */
	public void removeClosedSystemItem(int systemType, String closeItem) {
		StatisManager.getInstance().incRedisKey(SYSTEM_SWITCH);

		String key = SYSTEM_SWITCH + ":" + systemType;
		redisSession.sRem(key, closeItem);
	}

	public int getVipTravelGiftProb(String playerId) {
		StatisManager.getInstance().incRedisKey(TRAVEL_GIFT_PROB);

		String key = VIP_TRAVEL_GIFT_PROB + ":" + playerId;
		String value = redisSession.getString(key, GsConfig.getInstance().getPlayerRedisExpire());

		return value == null ? 0 : Integer.parseInt(value);
	}

	public void addOrUpdateVipTravelGiftProb(String playerId, int value) {
		StatisManager.getInstance().incRedisKey(TRAVEL_GIFT_PROB);

		String key = VIP_TRAVEL_GIFT_PROB + ":" + playerId;
		redisSession.setString(key, value + "", GsConfig.getInstance().getPlayerRedisExpire());
	}

	public int getTravelGiftProb(String playerId) {
		StatisManager.getInstance().incRedisKey(TRAVEL_GIFT_PROB);

		String key = TRAVEL_GIFT_PROB + ":" + playerId;
		String value = redisSession.getString(key, GsConfig.getInstance().getPlayerRedisExpire());

		return value == null ? 0 : Integer.parseInt(value);
	}

	public void addOrUpdateTravelGiftProb(String playerId, int value) {
		StatisManager.getInstance().incRedisKey(TRAVEL_GIFT_PROB);

		String key = TRAVEL_GIFT_PROB + ":" + playerId;
		redisSession.setString(key, value + "", GsConfig.getInstance().getPlayerRedisExpire());
	}

	/** 获取攻击新版野怪信息
	 * 
	 * @param playerId
	 * @param pointId */
	public String getAtkNewMonsterInfo(String playerId, int pointId) {
		StatisManager.getInstance().incRedisKey(ATK_NEW_MONSTER);

		String key = ATK_NEW_MONSTER + ":" + playerId + ":" + pointId;
		return redisSession.getString(key);
	}

	/** 更新攻击新版野怪信息
	 * 
	 * @param playerId
	 * @param pointId
	 * @param times */
	public void updateAtkNewMonsterInfo(String playerId, int pointId, int times) {
		String key = ATK_NEW_MONSTER + ":" + playerId + ":" + pointId;
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("times", times);
		jsonObj.put("last", HawkTime.getMillisecond());
		int expireSeconds = WorldMapConstProperty.getInstance().getWorldNewMonsterAtkBuffContiTime();
		redisSession.setString(key, jsonObj.toString(), expireSeconds);

		StatisManager.getInstance().incRedisKey(ATK_NEW_MONSTER);
	}

	/** 获取攻击新版野怪次数
	 * 
	 * @param playerId
	 * @param pointId
	 * @return */
	public int getAtkNewMonsterTimes(String playerId, int pointId) {
		String atkNewMonsterInfo = getAtkNewMonsterInfo(playerId, pointId);
		if (HawkOSOperator.isEmptyString(atkNewMonsterInfo)) {
			return 0;
		}
		JSONObject parseObject = JSON.parseObject(atkNewMonsterInfo);
		return parseObject.getIntValue("times");
	}

	/** 获取上次攻击新版野怪时间
	 * 
	 * @param playerId
	 * @param pointId
	 * @return */
	public long getLastAtkNewMonsterTime(String playerId, int pointId) {
		String atkNewMonsterInfo = getAtkNewMonsterInfo(playerId, pointId);
		if (HawkOSOperator.isEmptyString(atkNewMonsterInfo)) {
			return 0;
		}
		JSONObject parseObject = JSON.parseObject(atkNewMonsterInfo);
		return parseObject.getLongValue("last");
	}

	/** 更新当日购买体力次数
	 * 
	 * @param playerId
	 * @param buyTimes */
	public void updateBuyVitTimes(String playerId, int buyTimes) {
		String key = BUY_VIT_TIMES + ":" + playerId;
		redisSession.setString(key, HawkApp.getInstance().getCurrentTime() + ":" + buyTimes, GsConfig.getInstance().getPlayerRedisExpire());
		StatisManager.getInstance().incRedisKey(BUY_VIT_TIMES);
	}

	/** 获取当日体力购买次数
	 * 
	 * @param playerId
	 * @return */
	public String getBuyVitTimes(String playerId) {
		String key = BUY_VIT_TIMES + ":" + playerId;
		StatisManager.getInstance().incRedisKey(BUY_VIT_TIMES);
		return redisSession.getString(key);
	}

	/** 更新当日使用道具获得的金币数量
	 * 
	 * @param playerId
	 * @param gold */
	public void updateToolBackGold(String playerId, int gold) {
		String key = TOOL_BACK_GOLD + ":" + playerId;
		redisSession.setString(key, HawkApp.getInstance().getCurrentTime() + ":" + gold, GsConfig.getInstance().getPlayerRedisExpire());
		StatisManager.getInstance().incRedisKey(TOOL_BACK_GOLD);
	}

	/** 获取当日使用道具获得的金币数量信息
	 * 
	 * @param playerId
	 * @return */
	public String getToolBackGold(String playerId) {
		String key = TOOL_BACK_GOLD + ":" + playerId;
		StatisManager.getInstance().incRedisKey(TOOL_BACK_GOLD);
		return redisSession.getString(key);
	}

	/** 更新新版野怪刷新信息
	 * 
	 * @param playerId
	 * @param buyTimes */
	public void updateNewMonsterRefreshInfo(int week, int state) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("week", week);
		jsonObj.put("state", state);
		redisSession.setString(NEW_MONSTER_REFRESH, jsonObj.toJSONString());
		StatisManager.getInstance().incRedisKey(NEW_MONSTER_REFRESH);
	}

	/** 获取新版野怪刷新信息
	 * 
	 * @param playerId
	 * @return */
	public String getNewMonsterRefreshInfo() {
		String info = redisSession.getString(NEW_MONSTER_REFRESH);
		if (HawkOSOperator.isEmptyString(info)) {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("week", -1);
			jsonObj.put("state", 0);
			return jsonObj.toJSONString();
		}
		StatisManager.getInstance().incRedisKey(NEW_MONSTER_REFRESH);
		return info;
	}

	/** 获取新版野怪刷新星期
	 * 
	 * @return */
	public int getNewMonsterRefreshWeek() {
		JSONObject parseObject = JSON.parseObject(getNewMonsterRefreshInfo());
		return parseObject.getIntValue("week");
	}

	/** 获取新版野怪刷新状态
	 * 
	 * @return */
	public int getNewMonsterRefreshState() {
		JSONObject parseObject = JSON.parseObject(getNewMonsterRefreshInfo());
		return parseObject.getIntValue("state");
	}

	/** 更新推送信息 */
	public boolean updatePushInfo(String playerId, String pushInfo) {
		if (HawkOSOperator.isEmptyString(playerId) || HawkOSOperator.isEmptyString(pushInfo)) {
			return false;
		}
		StatisManager.getInstance().incRedisKey(PUSH_INFO_KEY);

		String key = PUSH_INFO_KEY + ":" + playerId;
		return redisSession.setString(key, pushInfo, GsConfig.getInstance().getPlayerRedisExpire());
	}

	/** 获取推送信息 */
	public String getPushInfo(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}
		StatisManager.getInstance().incRedisKey(PUSH_INFO_KEY);

		String key = PUSH_INFO_KEY + ":" + playerId;
		return redisSession.getString(key);
	}

	/** 更新免费宝箱领取时间
	 * 
	 * @param playerId
	 * @param takenTime
	 * @return */
	public boolean updateFreeBoxTakenTime(String playerId, long takenTime) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		StatisManager.getInstance().incRedisKey(FREE_BOX_KEY);

		String key = FREE_BOX_KEY + ":" + playerId;
		return redisSession.setString(key, String.valueOf(takenTime), GsConst.DAY_SECONDS);
	}

	/** 获取免费宝箱领取时间
	 * 
	 * @param playerId
	 * @return */
	public long getFreeBoxTakenTime(String playerId) {
		StatisManager.getInstance().incRedisKey(FREE_BOX_KEY);

		String key = FREE_BOX_KEY + ":" + playerId;
		String value = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(value)) {
			return Long.valueOf(value);
		}

		return 0;
	}

	/** 更新提醒盟主建群的提醒时间 */
	public boolean updateRemindTime(String playerId, long time, int expireTime) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		StatisManager.getInstance().incRedisKey(REMIND_GUILD_LEADER_TIME);

		String key = REMIND_GUILD_LEADER_TIME + ":" + playerId;
		return redisSession.setString(key, String.valueOf(time), expireTime);
	}

	/** 获取提醒盟主建群的提醒时间 */
	public long getRemindTime(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		StatisManager.getInstance().incRedisKey(REMIND_GUILD_LEADER_TIME);

		String key = REMIND_GUILD_LEADER_TIME + ":" + playerId;
		String time = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(time)) {
			Long.valueOf(time);
		}

		return 0;
	}

	/** 添加超级武器事件
	 * 
	 * @param info */
	public void addSuperWeaponDetialEvent(int pointId, SuperWeaponEvent.Builder builder) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_DETIAL_EVENT_KEY);

		redisSession.lPush((SUPER_WEAPON_DETIAL_EVENT_KEY + ":" + pointId).getBytes(), 0, builder.build().toByteArray());
	}

	public void updateSuperWeaponBriefEvent(int pointId, SuperWeaponEvent.Builder builder) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_BRIEF_EVENT_KEY);

		redisSession.hSetBytes(SUPER_WEAPON_BRIEF_EVENT_KEY, String.valueOf(pointId), builder.build().toByteArray());
	}

	/** 获取超级武器事件列表
	 * 
	 * @param maxCount
	 * @return */
	public List<SuperWeaponEvent.Builder> getSuperWeaponDetailEvent(int pointId, int maxCount) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_DETIAL_EVENT_KEY);

		List<SuperWeaponEvent.Builder> builderList = new LinkedList<SuperWeaponEvent.Builder>();
		try {
			List<byte[]> infoList = redisSession.lRange((SUPER_WEAPON_DETIAL_EVENT_KEY + ":" + pointId).getBytes(), 0, maxCount - 1, 0);
			if (infoList != null) {
				for (byte[] info : infoList) {
					SuperWeaponEvent.Builder builder = SuperWeaponEvent.newBuilder();
					builder.mergeFrom(info);
					builderList.add(builder);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return builderList;
	}

	public List<SuperWeaponEvent.Builder> getSuperWeaponBriefEvent() {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_BRIEF_EVENT_KEY);

		List<SuperWeaponEvent.Builder> builderList = new LinkedList<SuperWeaponEvent.Builder>();
		try {
			Map<byte[], byte[]> infoMap = redisSession.hGetAllBytes(SUPER_WEAPON_BRIEF_EVENT_KEY.getBytes());
			if (infoMap != null) {
				for (byte[] info : infoMap.values()) {
					SuperWeaponEvent.Builder builder = SuperWeaponEvent.newBuilder();
					builder.mergeFrom(info);
					builderList.add(builder);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return builderList;
	}

	/** 清空超级武器事件 */
	public void clearSuperWeaponDetialEvent(int pointId) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_DETIAL_EVENT_KEY);

		redisSession.lTrim(SUPER_WEAPON_DETIAL_EVENT_KEY + ":" + pointId, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	/** 清空超级武器事件 */
	public void clearSuperWeaponBriefEvent(int pointId) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_BRIEF_EVENT_KEY);

		redisSession.hDel(SUPER_WEAPON_BRIEF_EVENT_KEY, String.valueOf(pointId));
	}

	/** 添加当选国王记录
	 * 
	 * @param info */
	public void addElectedSuperWeapon(SuperWeaponPresident.Builder builder, int pointId) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_HISTORY_KEY);

		redisSession.lPush((SUPER_WEAPON_HISTORY_KEY + ":" + pointId).getBytes(), 0, builder.build().toByteArray());
	}

	/** 获取历届国王记录
	 * 
	 * @param maxCount
	 * @return */
	public List<SuperWeaponPresident.Builder> getElectedSuperWeapon(int maxCount, int pointId) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_HISTORY_KEY);
		List<SuperWeaponPresident.Builder> builderList = new LinkedList<SuperWeaponPresident.Builder>();
		try {
			List<byte[]> infoList = redisSession.lRange((SUPER_WEAPON_HISTORY_KEY + ":" + pointId).getBytes(), 0, maxCount - 1, 0);
			if (infoList != null) {
				for (byte[] info : infoList) {
					SuperWeaponPresident.Builder builder = SuperWeaponPresident.newBuilder();
					builder.mergeFrom(info);
					builderList.add(builder);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return builderList;
	}

	/** 获取超级武器礼包信息 */
	public Map<String, String> getSuperWeaponGiftInfo(int turnCount, int pointId, String guildId) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_GIFT_NUM_KEY);
		String key1 = SUPER_WEAPON_GIFT_NUM_KEY + ":" + turnCount + ":" + pointId;
		return redisSession.hGetAll(key1);
	}

	/** 获取超级武器礼包信息 */
	public String getSuperWeaponGiftInfo(int turnCount, int pointId, String guildId, int giftId) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_GIFT_NUM_KEY);
		String key1 = SUPER_WEAPON_GIFT_NUM_KEY + ":" + turnCount + ":" + pointId;
		String key2 = guildId + ":" + String.valueOf(giftId);
		return redisSession.hGet(key1, key2);
	}

	/** 超级武器礼包个数更新
	 * 
	 * @param giftId
	 * @param number
	 * @return */
	public void updateSuperWeaponGiftInfo(int turnCount, int pointId, String guildId, int giftId, int sendNum, int totalNum) {
		String number = String.valueOf(sendNum) + "_" + String.valueOf(totalNum);
		updateSuperWeaponGiftInfo(turnCount, pointId, guildId, giftId, number);
	}

	/** 超级武器礼包个数更新
	 * 
	 * @param giftId
	 * @param number
	 * @return */
	public void updateSuperWeaponGiftInfo(int turnCount, int pointId, String guildId, int giftId, String info) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_GIFT_NUM_KEY);
		String key1 = SUPER_WEAPON_GIFT_NUM_KEY + ":" + turnCount + ":" + pointId;
		String key2 = guildId + ":" + String.valueOf(giftId);
		redisSession.hSet(key1, key2, info, GameConstCfg.getInstance().getSuperWeaponExpireSecond());
	}

	/** 获取超级武器礼包发送记录数据 */
	public List<String> getAllSuperWeaponGiftSend(int turnCount, int pointId, String guildId) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPONT_GIFT_SEND_KEY);
		String key = SUPER_WEAPONT_GIFT_SEND_KEY + ":" + turnCount + ":" + pointId + ":" + guildId;
		return redisSession.lRange(key, 0, -1, 0);
	}

	/** 超级武器礼包发送记录数据更新
	 * 
	 * @param playerId
	 * @param dataJson
	 * @return */
	public void addSuperWeaponGiftSend(int turnCount, int pointId, String guildId, String... dataJson) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPONT_GIFT_SEND_KEY);
		String key = SUPER_WEAPONT_GIFT_SEND_KEY + ":" + turnCount + ":" + pointId + ":" + guildId;
		redisSession.lPush(key, GameConstCfg.getInstance().getSuperWeaponExpireSecond(), dataJson);
	}

	/** 更新推送开关数据
	 * 
	 * @param playerId
	 * @param group
	 * @param val
	 * @return */
	public void updatePushSwitch(String playerId, String group, String val) {
		StatisManager.getInstance().incRedisKey(PUSH_SWITCH_DATA);
		String key = PUSH_SWITCH_DATA + ":" + playerId;
		redisSession.hSet(key, group.trim(), val.trim(), GsConfig.getInstance().getPlayerRedisExpire());
	}

	/** 获取推送开关数据
	 * 
	 * @param playerId
	 * @param group
	 * @return */
	public String getPushSwitch(String playerId, String group) {
		StatisManager.getInstance().incRedisKey(PUSH_SWITCH_DATA);
		String key = PUSH_SWITCH_DATA + ":" + playerId;
		return redisSession.hGet(key, group.trim());
	}

	/** 添加IDIP登录消息
	 * 
	 * @param playerId
	 * @param msg */
	public void addIDIPMsg(String playerId, String msg) {
		StatisManager.getInstance().incRedisKey(IDIP_MSG_ON_LOGIN);
		String key = IDIP_MSG_ON_LOGIN + ":" + playerId;
		redisSession.setString(key, msg, GsConfig.getInstance().getPlayerRedisExpire());
	}

	/** 获取IDIP推送消息
	 * 
	 * @param playerId
	 * @return */
	public String getIDIPMsg(String playerId) {
		StatisManager.getInstance().incRedisKey(IDIP_MSG_ON_LOGIN);
		String key = IDIP_MSG_ON_LOGIN + ":" + playerId;
		return redisSession.getString(key);
	}

	/** 删除IDIP推送消息
	 * 
	 * @param playerId
	 * @return */
	public void delIDIPMsg(String playerId) {
		StatisManager.getInstance().incRedisKey(IDIP_MSG_ON_LOGIN);
		String key = IDIP_MSG_ON_LOGIN + ":" + playerId;
		redisSession.delete(key, false);
	}

	/** 添加举报信息
	 * 
	 * @param playerId
	 *            举报玩家id
	 * @param targetId
	 *            被举报玩家id
	 * @param reportInfo
	 *            举报信息 */
	public void addReportInfo(String playerId, String targetId, String reportInfo) {
		StatisManager.getInstance().incRedisKey(REPORTING_INFO);
		String key = String.format("%s:%s", REPORTING_INFO, playerId);
		redisSession.hSet(key, targetId, reportInfo, GsConfig.getInstance().getPlayerRedisExpire());
	}

	/** 获取举报信息
	 * 
	 * @param playerId
	 *            举报玩家id
	 * @param targetId
	 *            被举报玩家id
	 * @return */
	public String getReportInfo(String playerId, String tagerId) {
		StatisManager.getInstance().incRedisKey(REPORTING_INFO);
		String key = String.format("%s:%s", REPORTING_INFO, playerId);
		return redisSession.hGet(key, tagerId);
	}

	/** 添加手Q成就登录时上报标志
	 * 
	 * @param playerId
	 * @param scoreType */
	public void addScoreBatchFlag(String playerId, int scoreType, String scoreVal) {
		String key = SCORE_BATCH_FLAG + ":" + playerId;
		redisSession.hSet(key, String.valueOf(scoreType), scoreVal, GsConfig.getInstance().getPlayerRedisExpire());
		StatisManager.getInstance().incRedisKey(SCORE_BATCH_FLAG);
	}

	/** 批量增加
	 * 
	 * @param playerIds
	 * @param scoreTypes
	 * @param scoreVals */
	public void addScoreBatchFlag(String[] playerIds, int[] scoreTypes, String[] scoreVals) {
		if (playerIds.length == 0) {
			HawkLog.debugPrintln("add score batch flag failed, playerId param empty");
			return;
		}

		int size = Math.max(playerIds.length, scoreTypes.length);
		if (playerIds.length < size) {
			String key = SCORE_BATCH_FLAG + ":" + playerIds[0];
			try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
				for (int i = 0; i < size; i++) {
					pip.hset(key, String.valueOf(scoreTypes[i]), scoreVals[i]);
					StatisManager.getInstance().incRedisKey(SCORE_BATCH_FLAG);
				}

				pip.expire(key, GsConfig.getInstance().getPlayerRedisExpire());
				pip.sync();
			} catch (Exception e) {
				HawkException.catchException(e);
			}

		} else {
			String innerKey = String.valueOf(scoreTypes[0]);
			try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
				for (int i = 0; i < size; i++) {
					String key = SCORE_BATCH_FLAG + ":" + playerIds[i];
					pip.hset(key, innerKey, scoreVals[0]);
					pip.expire(key, GsConfig.getInstance().getPlayerRedisExpire());
					StatisManager.getInstance().incRedisKey(SCORE_BATCH_FLAG);
				}

				pip.sync();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

	}

	/** 获取手Q成就登录时上报标志
	 * 
	 * @param playerId
	 * @return */
	public Map<String, String> getAllScoreBatchFlag(String playerId) {
		String key = SCORE_BATCH_FLAG + ":" + playerId;
		StatisManager.getInstance().incRedisKey(SCORE_BATCH_FLAG);
		return redisSession.hGetAll(key);
	}

	/** 上报完成后移除标识
	 * 
	 * @param playerId */
	public void removeScoreBatchFlag(String playerId) {
		String key = SCORE_BATCH_FLAG + ":" + playerId;
		StatisManager.getInstance().incRedisKey(SCORE_BATCH_FLAG);
		redisSession.del(key);
	}

	/** 获取所有的请求
	 * 
	 * @param playerId
	 * @return */
	public Map<String, PlayerRelationApplyEntity> getAllRelationApplies(String playerId) {
		String key = RELATION_APPLY + ":" + playerId;
		StatisManager.getInstance().incRedisKey(RELATION_APPLY);
		Map<String, String> map = redisSession.hGetAll(key);
		Map<String, PlayerRelationApplyEntity> rltMap = new HashMap<>();
		if (map != null) {
			for (Entry<String, String> entry : map.entrySet()) {
				PlayerRelationApplyEntity prae = JSON.parseObject(entry.getValue(), PlayerRelationApplyEntity.class);
				prae.setPlayerId(entry.getKey());
				prae.setTargetPlayerId(playerId);
				rltMap.put(entry.getKey(), prae);
			}
		}

		return rltMap;
	}

	/** 创建好友申请
	 * 
	 * @param playerId
	 * @param pares */
	public void createRelationApply(PlayerRelationApplyEntity pare) {
		String key = RELATION_APPLY + ":" + pare.getTargetPlayerId();

		redisSession.hSet(key, pare.getPlayerId(), JSON.toJSONString(pare), GameConstCfg.getInstance().getRelaitonApplyExpireTime());
		StatisManager.getInstance().incRedisKey(RELATION_APPLY);

	}

	/** 删除好友的
	 * 
	 * @param playerId
	 * @param ids */
	public void deleteRelationApplies(String playerId, List<String> ids) {
		String key = RELATION_APPLY + ":" + playerId;
		redisSession.hDel(key, ids.toArray(new String[ids.size()]));
		StatisManager.getInstance().incRedisKey(RELATION_APPLY);
	}

	/** 联盟坐标指引
	 * 
	 * @param playerId */
	public String getGuildPointGuideInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_POINT_GUIDE);
		return redisSession.hGet(GuildKeys.GUILD_POINT_GUIDE, playerId);
	}

	/** 联盟坐标指引
	 * 
	 * @param playerId */
	public void updateGuildPointGuideInfo(String playerId) {
		redisSession.hSet(GuildKeys.GUILD_POINT_GUIDE, playerId, "1");
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_POINT_GUIDE);
	}

	/** 冒泡奖励相关信息批量更新
	 * 
	 * @param player
	 * @param bubbleTypes */
	public Map<Integer, BubbleRewardInfo> batchUpdateBubbleRewardInfo(String playerId, Set<Integer> bubbleTypes) {
		Map<Integer, BubbleRewardInfo> resultMap = new HashMap<>();
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			for (Integer type : bubbleTypes) {
				ItemInfo item = ConstProperty.getInstance().randomBubbleAwardItem(type);
				BubbleRewardInfo bubbleInfo = new BubbleRewardInfo(type, item);
				String key = BUBBLE_INFO_KEY + ":" + playerId;
				pip.hset(key, String.valueOf(bubbleInfo.getType()), JSONObject.toJSONString(bubbleInfo));
				pip.expire(key, GsConfig.getInstance().getPlayerRedisExpire());
				resultMap.put(type, bubbleInfo);
			}

			pip.sync();

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		StatisManager.getInstance().incRedisKey(BUBBLE_INFO_KEY);

		return resultMap;
	}

	/** 添加联盟标记信息
	 * 
	 * @param playerId
	 * @param dataJson
	 * @return */
	public void addGuildSign(String guildId, GuildSign guildSign) {
		String key = GuildKeys.GUILD_SIGN + ":" + guildId;
		redisSession.hSetBytes(key, String.valueOf(guildSign.getId()), guildSign.toByteArray());
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_SIGN);
	}

	/** 获取联盟标记信息
	 * 
	 * @param guildId
	 * @return */
	public Map<Integer, GuildSign> getGuildSigns(String guildId) {
		Map<Integer, GuildSign> signMap = new HashMap<>();
		String key = GuildKeys.GUILD_SIGN + ":" + guildId;
		Map<byte[], byte[]> resultMap = redisSession.hGetAllBytes(key.getBytes());
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_SIGN);
		try {
			for (Entry<byte[], byte[]> entry : resultMap.entrySet()) {
				int signId = Integer.valueOf(new String(entry.getKey()));
				GuildSign.Builder builder = GuildSign.newBuilder();
				builder.mergeFrom(entry.getValue());
				signMap.put(signId, builder.build());
			}
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}
		return signMap;
	}

	/** 移除联盟标记信息
	 * 
	 * @param playerId
	 * @param dataJson
	 * @return */
	public void removeGuildSign(String guildId, int signId) {
		String key = GuildKeys.GUILD_SIGN + ":" + guildId;
		redisSession.hDelBytes(key, String.valueOf(signId).getBytes());
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_SIGN);
	}

	/** 联盟解散时移除所有联盟标记
	 * 
	 * @param guildId */
	public void removeAllGuildSign(String guildId) {
		String key = GuildKeys.GUILD_SIGN + ":" + guildId;
		redisSession.del(key.getBytes());
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_SIGN);

	}

	/** 更新超级武器礼包接收次数
	 * 
	 * @param pointId
	 * @param playerId
	 * @param count */
	public void updateSpPlayerReceiveCount(int turnCount, int pointId, String playerId, String giftId, int count) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_PLAYER_RECEIVE_COUNT);
		String key1 = SUPER_WEAPON_PLAYER_RECEIVE_COUNT + ":" + turnCount + ":" + pointId;
		String key2 = playerId + ":" + giftId;
		redisSession.hSet(key1, key2, String.valueOf(count));
	}
	
	/**
	 * 按增量添加
	 * @param turnCount
	 * @param pointId
	 * @param playerId
	 * @param giftId
	 * @param count
	 */
	public void incSpPlayerReceiveCount(int turnCount, int pointId, String playerId, int giftId, int count) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_PLAYER_RECEIVE_COUNT);
		String key1 = SUPER_WEAPON_PLAYER_RECEIVE_COUNT + ":" + turnCount + ":" + pointId;
		String key2 = playerId + ":" + giftId;
		redisSession.hIncrBy(key1, key2, count);
	}

	/** 获取超级武器礼包接收次数
	 * 
	 * @param pointId
	 * @param playerId
	 * @param count */
	public int getSpPlayerReceiveCount(int turnCount, int pointId, String playerId, String giftId) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_PLAYER_RECEIVE_COUNT);
		String key1 = SUPER_WEAPON_PLAYER_RECEIVE_COUNT + ":" + turnCount + ":" + pointId;
		String key2 = playerId + ":" + giftId;
		String count = redisSession.hGet(key1, key2);
		if (HawkOSOperator.isEmptyString(count)) {
			return 0;
		}
		return Integer.parseInt(count);
	}

	/**
	 * 获取所有礼包
	 * @param turnCount
	 * @param pointId
	 * @return
	 */
	public Map<String, String> getAllSpPlayerReceiveCount(int turnCount, int pointId) {
		StatisManager.getInstance().incRedisKey(SUPER_WEAPON_PLAYER_RECEIVE_COUNT);
		String key1 = SUPER_WEAPON_PLAYER_RECEIVE_COUNT + ":" + turnCount + ":" + pointId;
		Map<String, String> hGetAll = redisSession.hGetAll(key1);
		if (hGetAll == null) {
			return null;
		}
		return hGetAll;
	}
	
	/** 添加已点击过的新上架商品类型
	 * 
	 * @param playerId
	 * @param shopItemGroups */
	public void addClickedNewlyShopItem(String playerId, String... shopItemGroups) {
		String key = CLICKED_NEWLY_SHOP_ITEM + ":" + playerId;
		redisSession.sAdd(key, GsConfig.getInstance().getPlayerRedisExpire(), shopItemGroups);
		StatisManager.getInstance().incRedisKey(CLICKED_NEWLY_SHOP_ITEM);
	}

	/** 删除已点击过的新上架商品类型
	 * 
	 * @param playerId
	 * @param shopItemGroups */
	public void removeClickedNewlyShopItem(String playerId, String... shopItemGroups) {
		String key = CLICKED_NEWLY_SHOP_ITEM + ":" + playerId;
		if (shopItemGroups.length > 0) {
			redisSession.sRem(key, shopItemGroups);
		} else {
			redisSession.del(key);
		}
		StatisManager.getInstance().incRedisKey(CLICKED_NEWLY_SHOP_ITEM);
	}

	/** 获取已点击过的新上架商品类型
	 * 
	 * @param playerId */
	public Set<String> getClickedNewlyShopItem(String playerId) {
		String key = CLICKED_NEWLY_SHOP_ITEM + ":" + playerId;
		StatisManager.getInstance().incRedisKey(CLICKED_NEWLY_SHOP_ITEM);
		return redisSession.sMembers(key);
	}

	/** 更新热销商品配置期数
	 * 
	 * @param newTerm */
	public void updateShopTerm(String playerId, int newTerm) {
		String key = SHOP_TERM_KEY + ":" + playerId;
		redisSession.setString(key, String.valueOf(newTerm), GsConfig.getInstance().getPlayerRedisExpire());
		StatisManager.getInstance().incRedisKey(SHOP_TERM_KEY);
	}

	/** 获取热销商品配置期数
	 * 
	 * @return */
	public int getShopTerm(String playerId) {
		String key = SHOP_TERM_KEY + ":" + playerId;
		String shopTerm = redisSession.getString(key);
		StatisManager.getInstance().incRedisKey(SHOP_TERM_KEY);
		if (!HawkOSOperator.isEmptyString(shopTerm)) {
			return Integer.parseInt(shopTerm);
		}

		return 0;
	}

	/** 添加联盟官员申请信息
	 * 
	 * @param playerId
	 * @param dataJson
	 * @return */
	public void addGuildOfficeApply(String guildId, int officeId, String playerId) {
		String key = GuildKeys.GUILD_OFFICE_APPLY + ":" + guildId + ":" + officeId;
		redisSession.hSet(key, playerId, String.valueOf(HawkTime.getMillisecond()));
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_OFFICE_APPLY);
	}

	/** 获取联盟官员申请信息
	 * 
	 * @param guildId
	 * @return */
	public Map<String, String> getGuildOfficeApply(String guildId, int officeId) {
		String key = GuildKeys.GUILD_OFFICE_APPLY + ":" + guildId + ":" + officeId;
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_OFFICE_APPLY);
		return redisSession.hGetAll(key);
	}

	/** 移除指定玩家官员申请信息
	 * 
	 * @param playerId
	 * @param dataJson
	 * @return */
	public void removeGuildOfficeApply(String guildId, String playerId) {
		for (GuildOffice office : GuildOffice.values()) {
			String key = GuildKeys.GUILD_OFFICE_APPLY + ":" + guildId + ":" + office.value();
			redisSession.hDel(key, playerId);
		}
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_OFFICE_APPLY);
	}

	/** 移除联盟指定官职的申请信息
	 * 
	 * @param guildId
	 * @param officeId */
	public void removeGuildOfficeApply(String guildId, int officeId) {
		String key = GuildKeys.GUILD_OFFICE_APPLY + ":" + guildId + ":" + officeId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_OFFICE_APPLY);
	}

	/** 移除指定联盟的官员申请信息
	 * 
	 * @param guildId */
	public void removeGuildOfficeApply(String guildId) {
		for (GuildOffice office : GuildOffice.values()) {
			removeGuildOfficeApply(guildId, office.value());
		}
	}

	/** 获取联盟任务列表
	 * 
	 * @param guildId
	 * @return */
	public List<GuildTaskItem> getGuildTaskList(String guildId) {
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		String key = GuildKeys.GUILD_TASK_KEY + ":" + guildId;
		List<String> infoList = redisSession.lRange(key, 0, -1, expireSecond);
		List<GuildTaskItem> taskList = new ArrayList<>();
		for (String info : infoList) {
			GuildTaskItem taskItem = GuildTaskItem.valueOf(info);
			if (taskItem != null) {
				taskList.add(taskItem);
			}
		}
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_TASK_KEY);
		return taskList;
	}

	/** 更新联盟任务信息
	 * 
	 * @param guildId
	 * @return */
	public void updateGuildTask(String guildId, List<GuildTaskItem> taskList) {
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		String key = GuildKeys.GUILD_TASK_KEY + ":" + guildId;
		redisSession.del(key);
		if (taskList.isEmpty()) {
			return;
		}
		String[] infoArr = new String[taskList.size()];
		for (int i = 0; i < taskList.size(); i++) {
			infoArr[i] = taskList.get(i).toString();
		}
		redisSession.lPush(key, expireSecond, infoArr);
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_TASK_KEY);
	}

	/** 删除联盟任务
	 * 
	 * @param guildId */
	public void removeGuildTask(String guildId) {
		String key = GuildKeys.GUILD_TASK_KEY + ":" + guildId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_TASK_KEY);
	}

	/** 添加在线玩家信息
	 * 
	 * @param guildId
	 * @param playerId */
	public void addGuildLoginMember(String guildId, String... playerId) {
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		String key = GuildKeys.GUILD_LOGIN_KEY + ":" + guildId;
		redisSession.sAdd(key, expireSecond, playerId);
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_LOGIN_KEY);
	}
	
	/**
	 * 拉取今日登录成员列表
	 * @param guildId
	 * @return
	 */
	public Set<String> getGuildLoginMembers(String guildId){
		String key = GuildKeys.GUILD_LOGIN_KEY + ":" + guildId;
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_LOGIN_KEY);
		return redisSession.sMembers(key);
	}

	/** 获取今日联盟登录人数
	 * 
	 * @param guildId
	 * @return */
	public int getGuildLoginCnt(String guildId) {
		String key = GuildKeys.GUILD_LOGIN_KEY + ":" + guildId;
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_LOGIN_KEY);
		return (int) redisSession.sCard(key);
	}

	/** 移除联盟成员登录信息
	 * 
	 * @param guildId */
	public void removeGuildLogin(String guildId) {
		String key = GuildKeys.GUILD_LOGIN_KEY + ":" + guildId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_LOGIN_KEY);
	}
	
	/** 添加联盟成员分享玩家信息
	 * 
	 * @param guildId
	 * @param playerId */
	public void addGuildShareMember(String guildId, String playerId) {
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		String key = GuildKeys.GUILD_SHARE_KEY + ":" + guildId;
		redisSession.sAdd(key, expireSecond, playerId);
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_SHARE_KEY);
	}
	
	/** 获取今日联盟分享人数
	 * 
	 * @param guildId
	 * @return */
	public int getGuildShareCnt(String guildId) {
		String key = GuildKeys.GUILD_SHARE_KEY + ":" + guildId;
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_SHARE_KEY);
		return (int) redisSession.sCard(key);
	}
	
	/** 移除联盟成员分享信息
	 * 
	 * @param guildId */
	public void removeGuildShare(String guildId) {
		String key = GuildKeys.GUILD_SHARE_KEY + ":" + guildId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(GuildKeys.GUILD_SHARE_KEY);
	}

	public void updateBuildRewardInfo(String playerId, int baseUpgradeCfgId, int pos) {
		StatisManager.getInstance().incRedisKey(BUILD_AWARD);
		String key = BUILD_AWARD + ":" + playerId;
		redisSession.hSet(key, String.valueOf(baseUpgradeCfgId), String.valueOf(pos));
	}

	public Map<String, String> getBuildRewardInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(BUILD_AWARD);
		String key = BUILD_AWARD + ":" + playerId;
		return redisSession.hGetAll(key);
	}

	/** 获取全服所有唯一成就
	 * 
	 * @return */
	public Map<String, String> getAllSoleAchieve() {
		StatisManager.getInstance().incRedisKey(SOLE_ACHIEVE);
		return redisSession.hGetAll(SOLE_ACHIEVE);
	}

	/** 更新唯一成就
	 * 
	 * @param achieveId
	 * @param playerId */
	public void updateSoleAchieve(String achieveId, String playerId) {
		StatisManager.getInstance().incRedisKey(SOLE_ACHIEVE);
		redisSession.hSet(SOLE_ACHIEVE, achieveId, playerId);
		StatisManager.getInstance().incRedisKey(OL_QUESTION);
	}

	/*
	 * 更新自助答疑系统
	 */
	public void updateOnlineQuestion(int keyId) {
		String key = String.valueOf(keyId);
		getRedisSession().zIncrby(OL_QUESTION, key, 1, 0);
		StatisManager.getInstance().incRedisKey(OL_QUESTION);
	}

	/*
	 * 获取在线答疑排行
	 */
	public Set<String> getOnlineQuestionRank(int from, int to) {
		Set<String> ret = getRedisSession().zRevRange(OL_QUESTION, from, to, 0);
		StatisManager.getInstance().incRedisKey(OL_QUESTION);
		return ret;
	}

	/*
	 * 获取联盟排行榜存储的 时间等。
	 */

	public GuildRankSvInfo getGuildRankSvInfo() {
		String jsonStr = redisSession.getString(GUILDRANK_INFO);
		StatisManager.getInstance().incRedisKey(GUILDRANK_INFO);
		if (null != jsonStr && !jsonStr.isEmpty()) {
			GuildRankSvInfo rkInfo = JsonUtils.String2Object(jsonStr, GuildRankSvInfo.class);
			return rkInfo;
		}
		return null;
	}

	/*
	 * 更新联盟排行榜 存储的时间.
	 */

	public void updateGuildRankSvInfo(GuildRankSvInfo svInfo) {
		String jsonStr = JsonUtils.Object2Json(svInfo);
		redisSession.setString(GUILDRANK_INFO, jsonStr);
		StatisManager.getInstance().incRedisKey(GUILDRANK_INFO);
	}

	/** 增加物品兑换次数
	 * 
	 * @param playerId
	 * @param exchangeId
	 * @param count */
	public void addItemExchangeTimes(String playerId, String exchangeId, int count) {
		String key = ITEM_EXCHANGE + ":" + playerId;
		redisSession.hIncrBy(key, exchangeId, count);
		StatisManager.getInstance().incRedisKey(ITEM_EXCHANGE);
	}

	/** 获取全部的物品兑换次数
	 * 
	 * @param playerId
	 * @return */
	public Map<String, String> getItemExchangeTimesAll(String playerId) {
		String key = ITEM_EXCHANGE + ":" + playerId;
		Map<String, String> result = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(ITEM_EXCHANGE);
		return result;
	}

	/** 增加当日赏金领取总额 */
	public void dayGuildBountyAwdInc(String playerId, int addAmount) {
		try {
			final String key = PLAYER_DAILY_ACHIEVE_BOUNTY + ":" + playerId + ":" + HawkTime.getYearDay();
			redisSession.increaseBy(key, addAmount, GsConst.DAY_SECONDS);
			StatisManager.getInstance().incRedisKey(PLAYER_DAILY_ACHIEVE_BOUNTY);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 当日赏金领取总额获取 */
	public int getDayGuildBountyAwd(String playerId) {
		final String key = PLAYER_DAILY_ACHIEVE_BOUNTY + ":" + playerId + ":" + HawkTime.getYearDay();
		String retStr = redisSession.getString(key);
		StatisManager.getInstance().incRedisKey(PLAYER_DAILY_ACHIEVE_BOUNTY);
		try {
			if (null != retStr && !retStr.isEmpty()) {
				return Integer.valueOf(retStr);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/** 获取攻击机甲次数
	 * 
	 * @param gundamUUID
	 * @param playerId */
	public int getAtkGundamTimes(String gundamUuid, String playerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_ATK_GUNDAM_TIMES);

		String key = PLAYER_ATK_GUNDAM_TIMES + ":" + playerId;

		String str = redisSession.hGet(key, gundamUuid);
		StatisManager.getInstance().incRedisKey(PLAYER_ATK_GUNDAM_TIMES);
		
		if (!HawkOSOperator.isEmptyString(str)) {
			return Integer.parseInt(str);
		}

		return 0;
	}

	/** 获取攻击所有机甲次数
	 * 
	 * @param gundamUUID
	 * @param playerId */
	public Map<String, String> getAllAtkGundamTimes(String playerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_ATK_GUNDAM_TIMES);

		String key = PLAYER_ATK_GUNDAM_TIMES + ":" + playerId;

		Map<String, String> map = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(PLAYER_ATK_GUNDAM_TIMES);
		if (map != null) {
			return map;
		}

		return new HashMap<>();
	}

	/** 增加攻击机甲次数
	 * 
	 * @param gundamUuid
	 * @param playerId */
	public void incrementAtkGundamTimes(String gundamUuid, String playerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_ATK_GUNDAM_TIMES);

		String key = PLAYER_ATK_GUNDAM_TIMES + ":" + playerId;
		redisSession.hIncrBy(key, gundamUuid, 1, (int) TimeUnit.DAYS.toSeconds(1));
		StatisManager.getInstance().incRedisKey(PLAYER_ATK_GUNDAM_TIMES);
	}

	/** 设置机甲刷新id
	 * 
	 * @param uuid */
	public void setGundamRefreshUuid(String uuid) {
		redisSession.setString(GUNDAM_REFRESH_UUID, uuid);
		StatisManager.getInstance().incRedisKey(GUNDAM_REFRESH_UUID);
	}

	/** 获取机甲刷新id
	 * 
	 * @param uuid */
	public String getGundamRefreshUuid() {
		StatisManager.getInstance().incRedisKey(GUNDAM_REFRESH_UUID);
		return redisSession.getString(GUNDAM_REFRESH_UUID);
	}

	/**
	 * 设置年兽刷新id
	 */
	public void setNianRefreshUuid(String uuid) {
		redisSession.setString(NIAN_REFRESH_UUID, uuid);
		StatisManager.getInstance().incRedisKey(NIAN_REFRESH_UUID);
	}

	/**
	 * 获取年兽刷新id
	 */
	public String getNianRefreshUuid() {
		StatisManager.getInstance().incRedisKey(NIAN_REFRESH_UUID);
		return redisSession.getString(NIAN_REFRESH_UUID);
	}
	
	/**
	 * 获取年兽上次刷新K值 -- 改为存全局redis-202407171400
	 */
	public int getNianLastK() {
		StatisManager.getInstance().incRedisKey(NIAN_LAST_K);
		String stringK = redisSession.getString(NIAN_LAST_K);
		int k = 0;
		if (!HawkOSOperator.isEmptyString(stringK)) {
			k = Integer.parseInt(stringK);
		}
		return k;
	}
	
	/**
	 * 设置年兽上次刷新K值 -- 改为存全局redis-202407171400
	 */
	public void setNianLastK(long k) {
		StatisManager.getInstance().incRedisKey(NIAN_LAST_K);
		redisSession.setString(NIAN_LAST_K, String.valueOf(k));
	}

	
	/*** 更新玩家试炼殿堂分数
	 * 
	 * @param guildId
	 *            玩家联盟id
	 * @param playerId
	 *            玩家id
	 * @param score
	 *            本日最高分数
	 * @param expire
	 *            过期时间 */
	public void updatePlayerPlotBattleScore(String guildId, String playerId, double score, int expire) {
		if (playerId == null || score <= 0) {
			return;
		}
		String key = String.format(GuildKeys.PLAYER_PLOT_BATTLE_RANK_SCORE, PlotBattleService.getInstance().getTodayStr(), guildId);
		redisSession.zAdd(key, score, playerId, expire);
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_PLOT_BATTLE_RANK_SCORE);
	}

	/*** 通过guildId获取玩家在本盟的最高积分
	 * 
	 * @param guildId
	 * @param playerId
	 * @return */
	public double getPlayerPlotBattleMaxScoreWithGuildId(String guildId, String playerId, int expire) {
		if (guildId == null || playerId == null) {
			return 0;
		}
		String key = String.format(GuildKeys.PLAYER_PLOT_BATTLE_RANK_SCORE, PlotBattleService.getInstance().getTodayStr(), guildId);
		Double score = redisSession.zScore(key, playerId, expire);
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_PLOT_BATTLE_RANK_SCORE);
		if (score == null) {
			return 0;
		}
		return score;
	}

	/*** 退出联盟删掉排行榜数据
	 * 
	 * @param guildId
	 * @param playerId */
	public void deletePlayerPlotBattleScoreLeaveGuild(String guildId, String playerId, int expireTime) {
		if (guildId == null || playerId == null) {
			return;
		}
		String key = String.format(GuildKeys.PLAYER_PLOT_BATTLE_RANK_SCORE, PlotBattleService.getInstance().getTodayStr(), guildId);
		// 记录行为日志
		redisSession.zRem(key, expireTime, playerId);
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_PLOT_BATTLE_RANK_SCORE);
	}

	/*** 获取试炼殿堂分数zset集合
	 * 
	 * @param guildId
	 * @param date
	 *            日期字符串
	 * @return */
	public Set<Tuple> getCrRankInfo(String guildId, int max, String date) {
		if (guildId == null) {
			return null;
		}
		String key = String.format(GuildKeys.PLAYER_PLOT_BATTLE_RANK_SCORE, date, guildId);
		// return redisSession.zRangeWithScores(key, 0, -1, 0);
		StatisManager.getInstance().incRedisKey(GuildKeys.PLAYER_PLOT_BATTLE_RANK_SCORE);
		return redisSession.zRevrangeWithScores(key, 0, max, PlotBattleService.getInstance().getCrRankExpireTime());
	}

	public void setPlotBattleSendRankMailRealTime(long realTime) {
		redisSession.setString(GuildKeys.PLOT_BATTLE_SEND_RANK_MAIL_REAL_TIME, String.valueOf(realTime));
		StatisManager.getInstance().incRedisKey(GuildKeys.PLOT_BATTLE_SEND_RANK_MAIL_REAL_TIME);
	}

	public long getPlotBattleSendRankMailRealTime() {
		String value = redisSession.getString(GuildKeys.PLOT_BATTLE_SEND_RANK_MAIL_REAL_TIME);
		StatisManager.getInstance().incRedisKey(GuildKeys.PLOT_BATTLE_SEND_RANK_MAIL_REAL_TIME);
		if (value == null) {
			return 0;
		}
		return Long.valueOf(value);
	}

	/** 更新玩家我要变强各模块积分
	 * 
	 * @param entity */
	public void updatePlayerStrengthenGuideScore(SGPlayerEntity entity) {
		try {
			if (null != entity) {
				String svStr = JsonUtils.Object2Json(entity);
				if (svStr != null && !svStr.isEmpty()) {
					String key = String.format("%s:%s", PLAYER_STRENGTHEN_GUIDE, entity.getPlayerId());
					redisSession.setString(key, svStr);
					StatisManager.getInstance().incRedisKey(PLAYER_STRENGTHEN_GUIDE);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 读取redis存储的我要变强各模块积分
	 * 
	 * @param playerId
	 * @return */
	public SGPlayerEntity loadPlayerStrengthenGuideScore(String playerId) {
		try {
			if (playerId != null && !playerId.isEmpty()) {
				String key = String.format("%s:%s", PLAYER_STRENGTHEN_GUIDE, playerId);
				String svStr = redisSession.getString(key);
				StatisManager.getInstance().incRedisKey(PLAYER_STRENGTHEN_GUIDE);
				if (null != svStr && !svStr.isEmpty()) {
					SGPlayerEntity entity = JsonUtils.String2Object(svStr, SGPlayerEntity.class);
					return entity;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}


	/** 更新本服务器我要变强分布
	 * 
	 * @param keyFix
	 * @param field1
	 * @param value1
	 * @param field2
	 * @param value2 */
	public void updateServerStrengthenGuideDistribution(String keyFix, int field1, int value1, int field2, int value2) {
		try {
			String key = String.format("%s:%s", STRENGTHEN_GUIDE, keyFix);
			redisSession.hSet(key, String.valueOf(field1), String.valueOf(value1));
			StatisManager.getInstance().incRedisKey(STRENGTHEN_GUIDE);
			redisSession.hSet(key, String.valueOf(field2), String.valueOf(value2));
			StatisManager.getInstance().incRedisKey(STRENGTHEN_GUIDE);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	/** 更新本服务器我要变强分布
	 * 
	 * @param keyFix
	 * @param field1
	 * @param value1 */
	public void updateServerStrengthenGuideDistribution(String keyFix, int field1, int value1) {
		try {
			String key = String.format("%s:%s", STRENGTHEN_GUIDE, keyFix);
			redisSession.hSet(key, String.valueOf(field1), String.valueOf(value1));
			StatisManager.getInstance().incRedisKey(STRENGTHEN_GUIDE);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 从redis 加载服务器各个类型分段人数
	 * 
	 * @param keyFix
	 * @return */
	public Map<String, String> loadServerStrengthenGuideDistribution(String keyFix) {
		String key = String.format("%s:%s", STRENGTHEN_GUIDE, keyFix);
		StatisManager.getInstance().incRedisKey(STRENGTHEN_GUIDE);
		return redisSession.hGetAll(key);
	}

	/**
	 * 记录联盟军演中秒的兵
	 */
	public void lmjyIncreaseCreateSoldier(String playerId, int armyId, int count) {
		String key = String.format("%s:%s", LMJY_CREATE_ARMY, playerId);
		redisSession.hIncrBy(key, armyId + "", count);
		StatisManager.getInstance().incRedisKey(LMJY_CREATE_ARMY);
	}
	
	/***
	 * 所有在军演中造的兵
	 */
	public Map<String, String> lmjyTakeOutAllCreateSoldier(String playerId){
		String key = String.format("%s:%s", LMJY_CREATE_ARMY, playerId);
		Map<String,String> result = redisSession.hGetAll(key);
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(LMJY_CREATE_ARMY);
		return result;
	}
		
	/**读取redis存储的分享信息*/
	public PlayerDailyShareSaveDataPB.Builder loadPlayerDailyShareSaveData(String playerId){
		PlayerDailyShareSaveDataPB.Builder builder = PlayerDailyShareSaveDataPB.newBuilder();
		try {
			if (playerId != null && !playerId.isEmpty()) {
				String dateStr = HawkTime.formatTime(HawkTime.getMillisecond() , HawkTime.FORMAT_YMD);
				String key = String.format("%s:%s:%s", PLAYER_DAILY_SHARE, playerId, dateStr) ;
				String value = redisSession.getString(key);
				if (null != value && !value.isEmpty()) {
					JsonFormat.merge(value, builder);
				}
			}
			
			if(builder.getBrStatus().getNumber() < BattleReportSharedAwardStatus.SHARE_ENABLE.getNumber()){
				builder.setBrStatus(BattleReportSharedAwardStatus.SHARE_ENABLE);
			}else if(builder.getBrStatus().getNumber() > BattleReportSharedAwardStatus.AWARDED.getNumber()){
				builder.setBrStatus(BattleReportSharedAwardStatus.AWARDED);
			}
			StatisManager.getInstance().incRedisKey(PLAYER_DAILY_SHARE);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return builder;
	}
	
	/**保存redis存储的分享信息*/
	public void savePlayerDailyShareSaveData(String playerId, PlayerDailyShareSaveDataPB dt){
		try {
			if(null != dt ){
				String value = JsonFormat.printToString(dt);
				if(null != value && !value.isEmpty()){
					String dateStr = HawkTime.formatTime(HawkTime.getMillisecond() , HawkTime.FORMAT_YMD);
					String key = String.format("%s:%s:%s", PLAYER_DAILY_SHARE, playerId, dateStr) ;
					redisSession.setString(key, value, (int) TimeUnit.DAYS.toSeconds(1));
					StatisManager.getInstance().incRedisKey(PLAYER_DAILY_SHARE);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取战地旗帜资源
	 */
	public List<ItemInfo> getFlagResource(String playerId) {
		StatisManager.getInstance().incRedisKey(FLAG_RESOURCE);
		String resourceStr = redisSession.hGet(FLAG_RESOURCE, playerId);
		if (HawkOSOperator.isEmptyString(resourceStr)) {
			resourceStr = "";
		}
		return ItemInfo.valueListOf(resourceStr);
	}
	
	/**
	 * 更新战地旗帜资源
	 */
	public void updateFlagResource(String playerId, String resource) {
		StatisManager.getInstance().incRedisKey(FLAG_RESOURCE);
		redisSession.hSet(FLAG_RESOURCE, playerId, resource);
	}
	
	/**
	 * 添加学院申请信息
	 * @param collegeId
	 * @param playerId
	 */
	public void addCollegeApply(String collegeId, String playerId) {
		long now = HawkTime.getMillisecond();
		String collegeKey = COLLEGE_PLAYER_APPLY + ":" + collegeId;
		redisSession.hSet(collegeKey, playerId, String.valueOf(now), (int) (CollegeConstCfg.getInstance().getApplyEffectTime() / 1000));
		String playerKey = COLLEGE_SELF_APPLY + ":" + playerId;
		redisSession.hSet(playerKey, collegeId, String.valueOf(now), (int) (CollegeConstCfg.getInstance().getApplyEffectTime() / 1000));
		StatisManager.getInstance().incRedisKey(COLLEGE_PLAYER_APPLY);
		StatisManager.getInstance().incRedisKey(COLLEGE_SELF_APPLY);
	}
	
	/**
	 * 获取学院的学员申请列表
	 * @param collegeId
	 * @return
	 */
	public Map<String, String> getCollegeApplys(String collegeId) {
		String key = COLLEGE_PLAYER_APPLY + ":" + collegeId;
		Map<String, String> map = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(COLLEGE_PLAYER_APPLY);
		return map;
	}
	
	/**
	 * 批量删学员申请信息
	 * @param collegeId
	 * @param members
	 */
	public void delCollegeApplys(String collegeId, String... memberId) {
		String key = COLLEGE_PLAYER_APPLY + ":" + collegeId;
		redisSession.hDel(key, memberId);
		StatisManager.getInstance().incRedisKey(COLLEGE_PLAYER_APPLY);
	}
	
	/**
	 * 移除玩家申请的学院列表
	 * @param playerId
	 */
	public void removeCollegeApplys(String collegeId){
		String key = COLLEGE_PLAYER_APPLY + ":" + collegeId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(COLLEGE_PLAYER_APPLY);
	}
	
	/**
	 * 获取申请的学院的列表
	 * @param playerId
	 * @return
	 */
	public Map<String, String> getApplyedColleges(String playerId){
		String key = COLLEGE_SELF_APPLY + ":" + playerId;
		Map<String, String> map = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(COLLEGE_SELF_APPLY);
		return map;
	}
	
	/**
	 * 删除申请的学院的列表
	 * @param playerId
	 * @param collegeId
	 */
	public void delApplyCollege(String playerId, String collegeId) {
		String key = COLLEGE_SELF_APPLY + ":" + playerId;
		redisSession.hDel(key, collegeId);
		StatisManager.getInstance().incRedisKey(COLLEGE_SELF_APPLY);
	}
	
	/**
	 * 移除玩家申请的学院列表
	 * @param playerId
	 */
	public void removeApplyedColleges(String playerId){
		String key = COLLEGE_SELF_APPLY + ":" + playerId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(COLLEGE_SELF_APPLY);
	}
	
	public Map<String, String> getActivitySendRewardRecord() {
		return redisSession.hGetAll(ACTIVITY_SEND_REWARD_BY_MERGE_SERVER);
	}
	
	/**
	 * 
	 * @param map
	 */
	public void putActivitySendRewardRecord(Map<String, String> map) {
		//2两天
		redisSession.hmSet(ACTIVITY_SEND_REWARD_BY_MERGE_SERVER, map, 86400 * 2);
	}
	
	/**
	 * 获取战区赛季积分
	 */
	public Map<String, Integer> getSWSeasonScore() {
		StatisManager.getInstance().incRedisKey(SW_SEASON_SCORE);
		
		Map<String, Integer> ret = new ConcurrentHashMap<String, Integer>();
		
		Map<String, String> scores = redisSession.hGetAll(SW_SEASON_SCORE);
		if (scores == null) {
			return ret;
		}
		for (Entry<String, String> entry : scores.entrySet()) {
			ret.put(entry.getKey(), Integer.valueOf(entry.getValue()));
		}
		return ret;
	}
	
	/**
	 * 添加战区赛季积分
	 */
	public void setSWSeasonScore(String guildId, String score) {
		StatisManager.getInstance().incRedisKey(SW_SEASON_SCORE);
		redisSession.hSet(SW_SEASON_SCORE, guildId, score);
	}
	
	/**
	 * 清空战区赛季积分
	 */
	public void clearSWSeasonScore() {
		StatisManager.getInstance().incRedisKey(SW_SEASON_SCORE);
		redisSession.del(SW_SEASON_SCORE);
	}
	
	/**
	 * 获取赛季轮次
	 */
	public int getSWSeasonTurn() {
		StatisManager.getInstance().incRedisKey(SW_SEASON_TURN);
		String turn = redisSession.getString(SW_SEASON_TURN);
		if (HawkOSOperator.isEmptyString(turn)) {
			return 1;
		}
		return Integer.parseInt(turn);
	}
	
	/**
	 * 设置赛季轮次
	 */
	public void setSWSeasonTurn(int turn) {
		StatisManager.getInstance().incRedisKey(SW_SEASON_TURN);
		redisSession.setString(SW_SEASON_TURN, String.valueOf(turn));
	}
	
	/**
	 * 获取要塞期数
	 */
	public int getFortressTurn() {
		StatisManager.getInstance().incRedisKey(FORTRESS_TURN);
		
		String turn = redisSession.getString(FORTRESS_TURN);
		if (HawkOSOperator.isEmptyString(turn)) {
			return 1;
		}
		return Integer.parseInt(turn);
	}
	
	/**
	 * 设置要塞期数
	 */
	public void setFortressTurn(int turn) {
		StatisManager.getInstance().incRedisKey(FORTRESS_TURN);
		
		redisSession.setString(FORTRESS_TURN, String.valueOf(turn));
	}
	
	/**
	 * 获取要塞记录
	 */
	public Map<Integer, FortressRecordItem>  getAllFortressRecord() {
		StatisManager.getInstance().incRedisKey(FORTRESS_RECORD);
		
		Map<Integer, FortressRecordItem> retMap = new ConcurrentHashMap<>();
		
		Map<String, String> recordMap = redisSession.hGetAll(FORTRESS_RECORD);
		if (recordMap == null || recordMap.isEmpty()) {
			return retMap;
		}
		
		for (Entry<String, String> record : recordMap.entrySet()) {
			retMap.put(Integer.valueOf(record.getKey()), JSONObject.parseObject(record.getValue(), FortressRecordItem.class));
		}
		return retMap;
	}
	
	/**
	 * 添加要塞记录
	 */
	public void addFortressRecord(FortressRecordItem record) {
		StatisManager.getInstance().incRedisKey(FORTRESS_RECORD);
		redisSession.hSet(FORTRESS_RECORD, String.valueOf(record.getTurn()), JSONObject.toJSONString(record));
	}


	public void clearFortressRecord() {
		redisSession.del(FORTRESS_RECORD);
	}
	/**
	 * 获取npc
	 */
	public int getFortressNpc(int pointId) {
		StatisManager.getInstance().incRedisKey(FORTRESS_NPC);
		
		String key = FORTRESS_NPC + ":" + pointId;
		String hasNpc = redisSession.getString(key);
		if (HawkOSOperator.isEmptyString(hasNpc)) {
			return 1;
		}
		return Integer.parseInt(hasNpc);
	}
	
	/**
	 * 设置npc
	 */
	public void setFortressNpc(int pointId, int hasNpc) {
		StatisManager.getInstance().incRedisKey(FORTRESS_NPC);
		String key = FORTRESS_NPC + ":" + pointId;
		redisSession.setString(key, String.valueOf(hasNpc));
	}
	
	/**
	 * 获取要塞占领时间
	 */
	public long getFortressOccupyTime(int pointId) {
		StatisManager.getInstance().incRedisKey(FORTRESS_OT);
		
		String key = FORTRESS_OT + ":" + pointId;
		String occupyTime = redisSession.getString(key);
		if (HawkOSOperator.isEmptyString(occupyTime)) {
			return HawkTime.getMillisecond();
		}
		return Long.parseLong(occupyTime);
	}
	
	/**
	 * 设置npc
	 */
	public void setFortressOccupyTime(int pointId, long occupyTime) {
		StatisManager.getInstance().incRedisKey(FORTRESS_OT);
		String key = FORTRESS_OT + ":" + pointId;
		redisSession.setString(key, String.valueOf(occupyTime));
	}
	
	/**
	 * 添加贵族商城红点标识
	 * 
	 * @param playerId
	 */
	public void addVipShopRedPoint(String playerId) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_RED_POINT);
		String key = VIP_SHOP_RED_POINT + ":" + playerId;
		redisSession.setString(key, "1", (int) (GsConst.WEEK_MILLI_SECONDS / 1000));
	}
	
	/**
	 * 移除贵族商城红点标识
	 * 
	 * @param playerId
	 */
	public void removeVipShopRedPoint(String playerId) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_RED_POINT);
		String key = VIP_SHOP_RED_POINT + ":" + playerId;
		redisSession.del(key);
	}
	
	/**
	 * 获取贵族商城红点标识
	 * 
	 * @param playerId
	 * @return
	 */
	public int getVipShopRedPoint(String playerId) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_RED_POINT);
		String key = VIP_SHOP_RED_POINT + ":" + playerId;
		String result = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(result)) {
			return Integer.valueOf(result);
		}
		
		return 0;
	}
	
	/**
	 * 添加全服控制
	 * 
	 * @param type
	 */
	public void addGlobalControlBan(GlobalControlType type, String reason) {
		StatisManager.getInstance().incRedisKey(GLOBAL_CONTROL_BAN);
		redisSession.hSet(GLOBAL_CONTROL_BAN, String.valueOf(type.intVal()), reason);
	}
	
	public void cancelGlobalControlBan(GlobalControlType type) {
		StatisManager.getInstance().incRedisKey(GLOBAL_CONTROL_BAN);
		redisSession.hDel(GLOBAL_CONTROL_BAN, String.valueOf(type.intVal()));
	}
	
	/**
	 * 获取所有的全局控制参数
	 * 
	 * @return
	 */
	public Map<Integer, String> getAllGlobalControlBanType() {
		StatisManager.getInstance().incRedisKey(GLOBAL_CONTROL_BAN);
		Map<String, String> result = redisSession.hGetAll(GLOBAL_CONTROL_BAN);
		Map<Integer, String> map = new HashMap<Integer, String>();
		for (Entry<String, String> entry : result.entrySet()) {
			map.put(Integer.valueOf(entry.getKey()), entry.getValue());
		}
		
		return map;
	}
	
	
	/**
	 * 添加buff信息
	 * @param builder
	 */
	public void addPresidentBuff(Map<Integer, Long> map) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_BUYY_KEY);
		Map<String, String> redisMap = MapUtil.map2Map(map, MapUtil.INTEGER2STRING, MapUtil.LONG2STRING);
		redisSession.hmSet(PRESIDENT_BUYY_KEY, redisMap, GsConfig.getInstance().getPlayerRedisExpire());
	}
	
	/**
	 * 读取buff信息
	 * @return
	 */
	public Map<Integer, Long> getPresidentBuff() {
		StatisManager.getInstance().incRedisKey(PRESIDENT_BUYY_KEY);		
		Map<String, String> map = redisSession.hGetAll(PRESIDENT_BUYY_KEY);
		return MapUtil.map2Map(map, MapUtil.STRING2INTEGER, MapUtil.STRING2LONG);
	}
	
	public void deletePresidentBuff() {
		StatisManager.getInstance().incRedisKey(PRESIDENT_BUYY_KEY);
		redisSession.del(PRESIDENT_BUYY_KEY);
	}  
	
	
	/**
	 * 加载所有的守护请求消息
	 * @return
	 */
	public Map<String, PlayerGuardInviteEntity> loadAllGuardInvite() {
		StatisManager.getInstance().incRedisKey(GUARD_INVITE);
		
		Map<String, String> redisMap = redisSession.hGetAll(GUARD_INVITE);
		if (redisMap != null && !redisMap.isEmpty()) {
			return MapUtil.map2Map(redisMap, MapUtil.genSelfTransform(String.class), (String str)->{
				return JSON.parseObject(str, PlayerGuardInviteEntity.class);
			});
		} else {
			return new HashMap<>();
		} 
	} 
	
	/**
	 * 删除所有的守护邀请信息
	 */
	public void deleteAllGuardInvite() {
		StatisManager.getInstance().incRedisKey(GUARD_INVITE);
		redisSession.del(GUARD_INVITE);
	}
	/**
	 * 删除守护信息
	 * @param playerId
	 */
	public void deleteGuardInvite(String playerId) {
		StatisManager.getInstance().incRedisKey(GUARD_INVITE);
		redisSession.hDel(GUARD_INVITE, playerId);
	}
	
	/**
	 * 
	 * @param entity
	 */
	public void addGuardInvite(PlayerGuardInviteEntity entity) {
		StatisManager.getInstance().incRedisKey(GUARD_INVITE);
		redisSession.hSet(GUARD_INVITE, entity.getPlayerId(), JSON.toJSONString(entity));
	}
	
	public void saveDuelPower(HawkTuple2<Integer, Integer> duel) {
		redisSession.setString(DUEL_CONTROL_POWER, duel.first + "_" + duel.second);
	}

	public HawkTuple2<Integer, Integer> getDuelPower() {
		String result = redisSession.getString(DUEL_CONTROL_POWER);
		int index = 0;
		int power = 0;
		if (StringUtils.isNotEmpty(result)) {
			String[] arr = result.split("_");
			if (arr.length == 2) {
				index = NumberUtils.toInt(arr[0]);
				power = NumberUtils.toInt(arr[1]);
			}
		}
		return HawkTuples.tuple(index, power);
	}	
	
	/**
	 * 圣诞boss的刷新ID
	 * @return
	 */
	public String getChristmasWarRefreshUuid() {
		StatisManager.getInstance().incRedisKey(CHRISTMAS_WAR_REFRESH_UUID);
		
		return redisSession.getString(CHRISTMAS_WAR_REFRESH_UUID);
	}
	
	/**
	 * 圣诞boss的uuid
	 * @param uuid
	 */
	public void setChristmasWarRefreshUuid(String uuid) {
		StatisManager.getInstance().incRedisKey(CHRISTMAS_WAR_REFRESH_UUID);
		redisSession.setString(CHRISTMAS_WAR_REFRESH_UUID, uuid);
	}
	
	/**
	 * f
	 * @param playerId
	 * @param toPlayerId
	 * @return
	 */
	public boolean checkGuardDressHasBlagLog(String playerId, String toPlayerId) {
		String day = HawkTime.formatTime(HawkTime.getAM0Date(new Date()));
		StringJoiner sj = new StringJoiner(":").add(GUARD_BLAG_HISOTRY).add(day).add(playerId);
		StatisManager.getInstance().incRedisKey(GUARD_BLAG_HISOTRY);
		String result = redisSession.hGet(sj.toString(), toPlayerId);
		return !HawkOSOperator.isEmptyString(result);
	}
	
	public void  addGuardDressBlagLog(String playerId, String toPlayerId) {
		String day = HawkTime.formatTime(HawkTime.getAM0Date(new Date()));
		StringJoiner sj = new StringJoiner(":").add(GUARD_BLAG_HISOTRY).add(day).add(playerId);
		StatisManager.getInstance().incRedisKey(GUARD_BLAG_HISOTRY);
		redisSession.hSet(sj.toString(), toPlayerId, "1", 86400);
	}
	
	
	/** 
	 * 根据key, 获取国家数据
	 * @param key
	 * @return 
	 */
	public <T> T getNationalDataByKey(String key, Class<T> type) {
		StatisManager.getInstance().incRedisKey(NATIONAL_DATA_KEY);
		byte[] dataBytes = redisSession.hGetBytes(NATIONAL_DATA_KEY, key);
		if (dataBytes != null && dataBytes.length > 0) {
			return HawkSerializer.deserialize(dataBytes, type);
		}
		return null;
	}

	/** 
	 * 更新国家数据
	 * @return 
	 */
	public void updateNationalDataByKey(String key, Object val) {
		StatisManager.getInstance().incRedisKey(NATIONAL_DATA_KEY);
		byte[] data = null;
		if (val == null) {
			data = new byte[0];
		} else {
			data = HawkSerializer.serialize(val);
		}
		// 序列化写入
		redisSession.hSetBytes(NATIONAL_DATA_KEY, key, data);
	}
	
	/**
	 * 更新个人捐献信息
	 */
	public void updateNationalDonateInfo(String playerId, NationalDonatModel model){
		StatisManager.getInstance().incRedisKey(NATIONAL_DONATE_DATA_KEY);
		byte[] data = null;
		if (model == null) {
			data = new byte[0];
		} else {
			data = HawkSerializer.serialize(model);
		}
		redisSession.hSetBytes(NATIONAL_DONATE_DATA_KEY, playerId, data);
	}
	
	/**
	 * 获取所有捐献信息
	 */
	public Map<String, NationalDonatModel> getAllNationalDonateInfo() {
		Map<String, NationalDonatModel> all = new ConcurrentHashMap<String, NationalDonatModel>();
		
		StatisManager.getInstance().incRedisKey(NATIONAL_DONATE_DATA_KEY);
		Map<byte[], byte[]> allMaps = redisSession.hGetAllBytes(NATIONAL_DONATE_DATA_KEY.getBytes());
		if(allMaps != null && !allMaps.isEmpty()) {
			for (Entry<byte[], byte[]> bbs : allMaps.entrySet()) {
				String playerId = new String(bbs.getKey());
				NationalDonatModel model = HawkSerializer.deserialize(bbs.getValue(), NationalDonatModel.class);
				all.put(playerId, model);
			}
		}
		return all;
	}
	
	/**
	 * 获取CD
	 * @param type
	 * @return
	 */
	public long getNationCancelCd(String type) {
		StatisManager.getInstance().incRedisKey(NATIONAL_CANCEL_CD_KEY);
		String cd = redisSession.hGet(NATIONAL_CANCEL_CD_KEY, type);
		if(cd == null){
			return 0;
		}
		return Long.parseLong(cd);
	}
	
	/**
	 * 设置CD
	 * @param type
	 * @param endTime
	 */
	public void setNationCancelCd(String type, long endTime) {
		StatisManager.getInstance().incRedisKey(NATIONAL_CANCEL_CD_KEY);
		redisSession.hSet(NATIONAL_CANCEL_CD_KEY, type, String.valueOf(endTime));
	}
	
	/**
	 * 添加机器人角色id
	 * @param playerId
	 */
	public void addRobotRole(String playerId) {
		redisSession.sAdd(ROBOT_ROLE_SET_KEY, 0, playerId);
	}
	
	public boolean isRobotRole(String playerId) {
		return redisSession.sIsmember(ROBOT_ROLE_SET_KEY, playerId);
	}
	
	public Set<String> getAllRobotRole() {
		return redisSession.sMembers(ROBOT_ROLE_SET_KEY);
	}
	
	/**
	 * 一个活动的积分处理相关参数信息
	 * @param paramsInfo
	 */
	public void addActivityScoreParams(int activityId, ActivityScoreParamsInfo paramsInfo) {
		StatisManager.getInstance().incRedisKey(ACTIVITY_SCORE_PARAMS);
		String key = ACTIVITY_SCORE_PARAMS + ":" + activityId;
		if (!HawkOSOperator.isEmptyString(paramsInfo.getPlayerId())) {
			redisSession.hSet(key, paramsInfo.getPlayerId(), JSONObject.toJSONString(paramsInfo), GsConst.MONTH_SECONDS);
		} else if (!HawkOSOperator.isEmptyString(paramsInfo.getServerId())) {
			redisSession.hSet(key, paramsInfo.getServerId(), JSONObject.toJSONString(paramsInfo), GsConst.MONTH_SECONDS);
		}
	}
	
	/**
	 * 删除一个活动的积分处理相关参数信息
	 * @param paramsInfo
	 */
	public void delActivityScoreParams(int activityId, ActivityScoreParamsInfo paramsInfo) {
		StatisManager.getInstance().incRedisKey(ACTIVITY_SCORE_PARAMS);
		String key = ACTIVITY_SCORE_PARAMS + ":" + activityId;
		if (!HawkOSOperator.isEmptyString(paramsInfo.getPlayerId())) {
			redisSession.hDel(key, paramsInfo.getPlayerId());
		} else if (!HawkOSOperator.isEmptyString(paramsInfo.getServerId())) {
			redisSession.hDel(key, paramsInfo.getServerId());
		}
	}
	
	/**
	 * 获取一个活动的积分处理相关参数信息
	 * @param activityId
	 * @return
	 */
	public Map<String, String> getActivityScoreParams(int activityId) {
		StatisManager.getInstance().incRedisKey(ACTIVITY_SCORE_PARAMS);
		String key = ACTIVITY_SCORE_PARAMS + ":" + activityId;
		return redisSession.hGetAll(key);
	}
}

