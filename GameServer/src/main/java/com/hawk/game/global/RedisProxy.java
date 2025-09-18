package com.hawk.game.global;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.app.HawkApp;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisPoolConfig;
import org.hawk.redis.HawkRedisSession;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;
import org.hawk.util.HawkZlib;
import org.hawk.util.JsonUtils;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.activity.entity.ActivityAccountRoleInfo;
import com.hawk.activity.type.impl.inherit.BackPlayerInfo;
import com.hawk.activity.type.impl.inheritNew.BackNewPlayerInfo;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.IDIPBanInfo;
import com.hawk.common.ServerInfo;
import com.hawk.common.ServerStatus;
import com.hawk.common.VersionInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.SimulateWarConstCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.crossactivity.CActivityInfo;
import com.hawk.game.crossactivity.CRankBuff;
import com.hawk.game.crossactivity.CrossActivityRecord;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.CrossServerInfo;
import com.hawk.game.crossfortress.FortressOccupyItem;
import com.hawk.game.data.FriendInviteInfo.TaskAttrInfo;
import com.hawk.game.data.PlatTransferInfo;
import com.hawk.game.data.PlayerAddBountyInfo;
import com.hawk.game.data.ProtectSoldierInfo;
import com.hawk.game.data.RechargeInfo;
import com.hawk.game.data.RevengeInfo;
import com.hawk.game.data.RevengeSoldierInfo;
import com.hawk.game.data.ServerSettingData;
import com.hawk.game.data.TimeLimitStoreConditionInfo;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.guild.championship.GCActivityData;
import com.hawk.game.guild.championship.GCConst.GCGuildGrade;
import com.hawk.game.guild.championship.GCGroupData;
import com.hawk.game.guild.championship.GCGuildData;
import com.hawk.game.guild.championship.GCWarState;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.travelshop.TravelShopFriendly;
import com.hawk.game.nation.mission.NationMissionTemp;
import com.hawk.game.nation.tech.NationTechResearchTemp;
import com.hawk.game.player.Player;
import com.hawk.game.player.vipsuper.PlayerSuperVipInfo;
import com.hawk.game.president.model.PresidentCrossAccumulateInfo;
import com.hawk.game.president.model.PresidentCrossRateInfo;
import com.hawk.game.protocol.Activity.PBEmptyModel10Info;
import com.hawk.game.protocol.Activity.PBEmptyModel8Info;
import com.hawk.game.protocol.Activity.PBEmptyModel9Info;
import com.hawk.game.protocol.Activity.PBSpreadBindRoleInfo;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.CrossActivity.CGuildInfo;
import com.hawk.game.protocol.CrossActivity.CPlayerInfo;
import com.hawk.game.protocol.CrossActivity.CrossGiftRecord;
import com.hawk.game.protocol.CrossActivity.CrossRankType;
import com.hawk.game.protocol.CrossActivity.CrossTaxRecord;
import com.hawk.game.protocol.CrossActivity.CrossTaxSendRecord;
import com.hawk.game.protocol.Dress.PlayerDressPlayerInfo;
import com.hawk.game.protocol.Friend.GuardSendAndBlagHistoryInfo;
import com.hawk.game.protocol.GuildChampionship.GCGuildBattle;
import com.hawk.game.protocol.GuildChampionship.GCPlayerInfo;
import com.hawk.game.protocol.GuildChampionship.PBChampionPlayer;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.Player.PlayerSnapshotPB;
import com.hawk.game.protocol.President.PresidentInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.SimulateWar.PBSimulateWarPlayer;
import com.hawk.game.protocol.SimulateWar.SimulateWarBattleList;
import com.hawk.game.protocol.SimulateWar.SimulateWarGuildBattle;
import com.hawk.game.protocol.SimulateWar.WayType;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsGiftRecordStruct;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsKingRecordStruct;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerStruct;
import com.hawk.game.protocol.TiberiumWar.TWBattleLog;
import com.hawk.game.protocol.TiberiumWar.TWGuildTeamInfo;
import com.hawk.game.protocol.TravelShop.TravelShopInfoSync;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.simulatewar.data.SimulateWarActivityData;
import com.hawk.game.service.simulatewar.data.SimulateWarGuildData;
import com.hawk.game.service.simulatewar.data.SimulateWarMatchInfo;
import com.hawk.game.service.simulatewar.data.SimulateWarPlayerExt;
import com.hawk.game.service.starwars.SWActivityData;
import com.hawk.game.service.starwars.SWFightData;
import com.hawk.game.service.starwars.SWGuildData;
import com.hawk.game.service.starwars.SWGuildJoinInfo;
import com.hawk.game.service.starwars.SWPlayerData;
import com.hawk.game.service.starwars.SWRoomData;
import com.hawk.game.service.starwars.StarWarsConst;
import com.hawk.game.service.tiberium.TLWActivityData;
import com.hawk.game.service.tiberium.TLWEliminationGroup;
import com.hawk.game.service.tiberium.TLWFightState;
import com.hawk.game.service.tiberium.TLWFinalGroup;
import com.hawk.game.service.tiberium.TLWGuildData;
import com.hawk.game.service.tiberium.TLWGuildJoinInfo;
import com.hawk.game.service.tiberium.TLWScoreData;
import com.hawk.game.service.tiberium.TWActivityData;
import com.hawk.game.service.tiberium.TWFightState;
import com.hawk.game.service.tiberium.TWGuildData;
import com.hawk.game.service.tiberium.TWGuildEloData;
import com.hawk.game.service.tiberium.TWGuildTeamData;
import com.hawk.game.service.tiberium.TWPlayerData;
import com.hawk.game.service.tiberium.TWRoomData;
import com.hawk.game.service.tiberium.TiberiumConst;
import com.hawk.game.service.tiberium.TiberiumConst.TLWGroupType;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.BanPlayerOperType;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.util.GsConst.DailyInfoField;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.util.GsConst.IDIPDailyStatisType;
import com.hawk.game.util.GsConst.VipRelatedDateType;
import com.hawk.game.util.MapUtil;
import com.hawk.serialize.string.SerializeHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

public class RedisProxy {
	/**
	 * 服务器列表信息存储键值
	 */
	static final String SERVER_LIST_KEY = "server_list";
	/**
	 * 服务器条件配置数据
	 */
	static final String SERVER_COND_KEY = "server_condtion";
	/**
	 * 服务器状态信息
	 */
	static final String SERVER_STATUS_KEY = "server_status";
	/**
	 * 最近登陆服务器
	 */
	static final String RECENT_SERVER_KEY = "recent_server";

	/**
	 * 服务器专服标志
	 */
	static final String PROPRIETARY_SERVER = "PROPRIETARY_SERVER";
	/**
	 * 服务器标识
	 */
	static final String SERVER_IDENTIFY_KEY = "server_identify";
	/**
	 * 版本信息
	 */
	static final String VERSION_INFO_KEY = "version_info";
	/**
	 * 授权token
	 */
	static final String AUTH_TOKEN_KEY = "auth_token";
	/**
	 * puid对应个人信息
	 */
	static final String PUID_PROFILE_KEY = "puid_profile";
	/**
	 * openid-角色信息
	 */
	static final String ACCOUNT_ROLE_KEY = "account_role";

	/**
	 * puid对应头像信息
	 */
	static final String PUID_PFICON_KEY = "puid_pfIcon";
	/**
	 * 已激活的设备
	 */
	static final String ACTIVE_DEVICE_KEY = "active_device";
	/**
	 * 激活码信息
	 */
	static final String ACTIVE_TOKEN_KEY = "active_token";
	/**
	 * 玩家设备信息
	 */
	static final String PHONE_INFO_KEY = "phone_info";
	/**
	 * 生成订单记录
	 */
	static final String GENERATE_ORDER_KEY = "generate_order";
	/**
	 * 回调订单存储
	 */
	static final String RECHARGE_CB_KEY = "recharge_cb";
	/**
	 * 玩家快照数据key
	 */
	static final String SNAPSHOT_KEY = "snapshot";
	/**
	 * 每个服的国王信息
	 */
	static final String PRESIDENT_INFO_KEY = "president_info";
	/**
	 * 航海远征王战胜利区服
	 */
	static final String CROSS_WIN_SERVER = "cross_win_server";
	/**
	 * 累计型任务累计数量存储键值
	 */
	static final String OVERLAY_MISSION_KEY = "overlay_mission";
	/**
	 * 最后一次充值时间
	 */
	static final String RECHARGE_LATEST_TIME_KEY = "recharge_latest_time";
	/**
	 * 发起订单直购的token
	 */
	static final String RECHARGE_TOKEN = "recharge_token";
	/**
	 * 服务器每日统计信息,冒号一个serverId
	 */
	static final String SERVER_DAILY_KEY = "server_daily";
	/**
	 * 服务器总的统计信息
	 */
	static final String GLOBAL_STAT_KEY = "global_stat";
	/**
	 * 设置实体数据
	 */
	static final String SETTING_DATA = "setting_data";
	/**
	 * 建筑拆除数据
	 */
	static final String BUILDING_REMOVE_EXP = "building_remove_exp";  // 设置了过期
	/**
	 * vip专属/福利礼包状态数据
	 */
	static final String VIP_BOX_STATUS = "vip_box_status";
	/**
	 * 未领取的vip福利礼包（不包括当前vip等级）
	 */
	static final String UNRECEIVED_BENEFIT_BOX = "unreceived_vip_box";
	/**
	 * vip商城物品购买次数
	 */
	static final String VIP_SHOP_BOUGHT_TIMES = "vip_shop_bought_times";  // 设置过期
	/**
	 * vip商城物品刷新时间
	 */
	static final String VIP_SHOP_REFRESH_TIME = "vip_shop_refresh_time";
	/**
	 * vip商城物品
	 */
	static final String VIP_SHOP_IDS = "vip_shop_ids";  // 设置过期
	/**
	 * 军演商店刷新时间
	 */
	static final String MILITARY_SHOP_REFRESH = "military_shop_refresh";
	/**
	 * 军演商店商品购买次数
	 */
	static final String MILITARY_SHOP_BUY = "military_item_buy";

	static final String SUPERSOLDIER_EXP_ITEN_EXCHANGE = "supersoldier_skillextp_itemexchange";
	/**
	 * 用于迁服
	 */
	static final String PLAYER_DATA_KEY = "player_data_key";
	/**
	 * 迁服
	 */
	private static final String IMMIGRATE_RECORD_KEY = "immigrate_record_key";
	/**
	 * 客户端推送数据
	 */
	static final String CLIENT_PUSH_DATA = "push";
	/**
	 * cdkey使用
	 */
	static final String CDKEY_USED = "cdkey_used";
	/**
	 * 调查问卷回调统计
	 */
	static final String SURVEY_COUNT = "survey_count";
	/**
	 * 封禁信息
	 */
	static final String BAN_INFO_KEY = "ban_info";
	/**
	 * 排行榜封禁信息
	 */
	static final String BAN_RANK_KEY = "ban_rank";
	/**
	 * 账号在线状态
	 */
	static final String ACCOUNT_ONLINE_KEY = "account_online";

	/**
	 * 货币充值订单ID
	 */
	static final String RECHARGE_ORDER_ID = "recharge_order_id";
	/**
	 * 购买流程未结束的直购礼包
	 */
	static final String UNFINISHED_RECHARGE_GOODS = "unfinished_recharge_goods";  // 设置过期

	/**
	 * 主播服务器前缀
	 */
	static final String ANCHOR_SERVER_PREFIX = "anchor";

	/**
	 * 主播服务器状态表
	 */
	static final String ANCHOR_SERVER_MAP = ANCHOR_SERVER_PREFIX + ":infomap";

	/**
	 * 主播服务器预告表
	 */
	static final String ANCHOR_SERVER_PREVIEW = ANCHOR_SERVER_PREFIX + ":preview";

	/**
	 * 主播礼物信息
	 */
	static final String ANCHOR_SERVER_GIFT_INFO = ANCHOR_SERVER_PREFIX + ":giftinfo";

	/**
	 * 服务器房间信息
	 */
	static final String ANCHOR_ROOM_INFO = ANCHOR_SERVER_PREFIX + ":roominfo";

	/**
	 * 主播信息
	 */
	static final String ANCHOR_SERVER_ANCHOR_INFO = ANCHOR_SERVER_PREFIX + ":anchors";

	/**
	 * 主播信息
	 */
	static final String ANCHOR_FOLLOW_INFO = ANCHOR_SERVER_PREFIX + ":userfollows";

	/**
	 * 预设名字
	 */
	static final String PREINSTALL_OPENID_NAME = "preinstall_openid_name";

	static final String PREINSTALL_NAME_OPENID = "preinstall_name_openid";
	/**
	 * 邀请QQ或微信密友
	 */
	static final String INVITE_FRIEND_KEY = "invite_friend";
	/**
	 * 被邀请
	 */
	static final String BE_INVITED_KEY = "be_invited";
	/**
	 * 邀请成功的密友
	 */
	static final String INVITE_SUCC_KEY = "invite_succ";
	/**
	 * 已领取过奖励的密友邀请任务ID
	 */
	static final String INVITE_TASK_KEY = "invite_reward_task";
	/**
	 * 已完成的邀請任務
	 */
	static final String FINISHED_TASK_KEY = "finished_invite_task";
	
	/** 今日加好友数 */
	static final String DAY_ADD_FRIEND_COUNT = "day_add_fri_cnt";
	/**
	 * 服务器控制参数（在线人数、注册人数，排队人数）
	 */
	static final String SERVER_CONTROL_KEY = "server_control";

	/**
	 * 白名单控制
	 */
	static final String PUID_CONTROL = "puid_control";
	/**
	 * idip注册白名单控制
	 */
	static final String IDIP_PUID_CONTROL = "idip_puid_control";
	/**
	 * 二级密码
	 */
	static final String SEC_PASSWD = "sec_passwd";

	/**
	 * 个人信用分
	 */
	static final String CREDIT_KEY = "self_credit";
	/**
	 * 手Q vip信息
	 */
	static final String QQ_VIP_KEY = "qq_vip";

	/**
	 * 玩家名字
	 */
	static final String PLAYER_NAME_SLOT = "name_slot";

	/**
	 * 联盟商店存量信息
	 */
	static final String GUILD_SHOP_INFO_KEY = "guild_shop";

	/**
	 * 全服同时在线人数
	 */
	static final String ONLINE_USER_COUNT = "global_online";
	
	/**
	 * 屏蔽平台头像账号
	 */
	static final String BAN_PORTRAIT = "ban_portrait";
	/**
	 * 屏蔽平台头像账号+时戳
	 */
	static final String BAN_PORTRAIT_TIMESTAMP = "ban_portrait_timestamp";
	
	/**
	 * 触发推送礼包次数
	 */
	static final String TOUCH_PUSH_GIFT_TIMES = "touch_push_gift_times";
	
	/**
	 * 泰能装备外显
	 */
	static final String EQUIP_STAR_SHOW = "equip_star_show";

	/**
	 * 星能探索外显
	 */
	static final String STAR_EXPLORE_SHOW = "star_explore_show";
	
	/**
	 * 玩家预存行军信息
	 */
	static final String PLAYER_PRESET_MARCH = "world_preset_march";
	/**
	 * 签名
	 */
	static final String SIGNATURE = "signature";
	/**
	 * 装扮信息
	 */
	static final String DRESS_SHOW = "dress_show";
	/**
	 * 玩家通用数据
	 */
	static final String PLAYER_LOCAL_VAR = "palyer_local_var";

	/**
	 * 伊娃红点提示信息
	 */
	static final String YIWA_RED_POINT = "yiwa_red_point";
	/**
	 * 玩家历史所在的联盟保存前三
	 */
	static final String HISTORY_GUILDS = "history_guilds:%s";

	/**
	 * 玩家追加悬赏和被追加悬赏的总值
	 */
	static final String PLAYER_BOUNTY_ADD_INFO = "player_bounty_add_info:%s";
	/**
	 * 从别的区服跨过来的
	 */
	static final String IMMIGRATION_PLAYER = "immigration_player";
	/**
	 * 本服跨到别的服
	 */
	public final String EMIGRATION_PLAYER = "emigration_player";
	/**
	 * 保存玩家的跨服状态.
	 */
	public final String CROSS_STATUS = "cross_status";

	/** 跨服个人积分 */
	public final String CACTIVITY_SELF_RANK = "cactivity_self";

	/** 跨服联盟积分 */
	public final String CACTIVITY_GUILD_RANK = "cactivity_guild";

	/** 跨服全服积分 */
	public final String CACTIVITY_SERVER_RANK = "cactivity_svr";
	
	/**
	 * 跨服能量点排行
	 */
	public final String CACTIVITY_TALENT_POINT_RANK = "cactivity_talent_point";
	
	/**
	 * 跨服能量点联盟排行
	 */
	public final String CACTIVITY_TALENT_POINT_GUILD_RANK = "cactivity_talent_point_guild";
	
	/** 跨服充能全服积分 */
	public static final String CACTIVITY_CHARGE_SERVER_RANK = "cactivity_ch_svr";
	
	/** 跨服充能个人积分 */
	public static final String CACTIVITY_CHARGE_SELF_RANK = "cactivity_ch_self";
	
	/** 跨服充能联盟积分 */
	public static final String CACTIVITY_CHARGE_GUILD_RANK = "cactivity_ch_guild";
	
	/** 跨服充能状态 */
	public static final String CACTIVITY_CHARGE_STATE = "cactivity_charge_state";
	
	/** 跨服王战开启服务器 */
	public static final String CACTIVITY_PRE_OPEN = "cactivity_pre_open";
	
	/** 跨服投票状态 */
	public static final String CACTIVITY_VOTE = "cactivity_vote";
	
	/** 跨服简要联盟信息 */
	public static final String CROSS_GUILD_LEADER_INFO = "cs_guild_leader_info";
	
	/** 战时司令简要信息 */
	public static final String FIGHT_PRESIDENT_LEADER_INFO = "fight_president_leader_info";

	/** 跨服税收 */
	public static final String CROSS_TAX = "cs_tax";
	
	/** 跨服税收记录 */
	public static final String CROSS_TAX_RECORD = "cs_tax_record";
	
	/** 跨服税收分配记录 */
	public static final String CROSS_TAX_SEND_RECORD = "cs_tax_send_record";
	
	/** 跨服税收奖励收取次数 */
	public static final String CROSS_TAX_RECEIVE_TIMES = "cs_tax_rec_times";
	
	/** 跨服任务 */
	public static final String CROSS_MISSION = "cs_mission";
	
	/** 跨服医院 */
	public static final String CROSS_HOSPITAL = "cs_hospital";

	/** 跨服道具检测 */
	public static final String CROSS_ITEM_CHECK = "cs_item_check";
	
	/** 跨服王战战斗结束 */
	public static final String CROSS_PRESIDENT_FIGHT_OVER = "cs_pf_over";
	
	/** 跨服发充能奖励 */
	public static final String CROSS_SEND_CHARGE_REWARD = "cs_send_ch_reward";
	
	/** 跨服个人奖励领取记录 */
	public final String CACTIVITY_SELF_REWARDED_IDS = "cactivity_slfreward";

	/** 跨服总部首次占领 */
	public final String CACTIVITY_FIRST_OCCUPY = "cactivity_first_occupy";
	
	/** 跨服区组玩家信息 */
	public final String CACTIVITY_PLAYER_INFO = "cactivity_pinfo";

	/** 跨服区组联盟信息 */
	public final String CACTIVITY_GUILD_INFO = "cactivity_ginfo";

	/** 跨服活动区服数据 */
	public final String CACTIVITY_SERVER_INFO = "cactivity_svr_info";

	/** 跨服联盟id信息 */
	public final String CCROSS_GUILDIDS = "c_guild_ids";

	/** 跨服玩家id信息 */
	public final String CCROSS_PLAYERS = "c_player_ids";

	/** 跨服排行buff奖励 */
	public final String CACTIVITY_RANK_BUFF = "c_rank_buff";
	/** win32 平台好友 */
	public final String WIN32_FRIEND = "win32_friend";

	/** 传承标识(老玩家回归到一个老服，需要设置传承标识，新服注册的帐号根据传承标识开启老玩家回归活动) **/
	public final String INHERIT_IDENTIFY = "inherit_identify";

	public final String PLAYER_UPDATE_VERSION_REWARD_HISTORY = "player_v_reward_history";

	/** 军魂承接回归标识 */
	public final String PLAYER_BACK_INFO = "player_back";
	/** 军魂承接回归标识-新 */
	public final String PLAYER_BACK_INFO_NEW = "player_back_new";

	/** 被承接角色列表 */
	public final String INHERITED_INFOS = "inherited_list";
	public final String HERO_TALENT_ROLL_DAY_COUNT = "hero_talent_day_roll";
	
	/** 玩家我要变强 */
	static final String JBS_CREATE_ARMY = "jbs_create_army";
	
	/**
	 * efftct通过Redis传递给跨服.
	 */
	public static final String PLAYER_EFFECT_CROSS = "player_effect_cross";
	/**
	 * 跨服联盟科技
	 */
	public static final String CS_GUILD_TECH = "cs_guild_tech"; 
	/**
	 * 新兵救援信息
	 */
	static final String PROTECT_SOLDIER = "protect_soldier";
	
	/**
	 * 大R复仇信息
	 */
	public static final String REVENGE_INFO_KEY = "revenge_info";
	/**
	 * 大R复仇死兵信息
	 */
	public static final String REVENGE_DEAD_SOLDIER_KEY = "revenge_dead_soldier";
	/**
	 * 大R复仇折扣商店购买信息
	 */
	public static final String REVENGE_SHOP_BUY = "revenge_shop_buy";
	
	/**
	 * 限时商店条件达成信息
	 */
	public static final String TIMELIMIT_STORE_CONDITION = "timelimit_store_condition";
	/**
	 * 限时商店购买信息
	 */
	public static final String TIMELIMIT_STORE_BOUGHT = "timelimit_store_bought";
	
	/** 泰伯利亚之战区服活动阶段数据 */
	public final String TWACTIVITY_SERVER_INFO = "tw_activity_info";
	
	/** 泰伯利亚之战报名联盟 期数-报名分组*/
	public final String TWACTIVITY_SIGN_GUILD = "tw_sign_guild";
	
	/** 泰伯利亚之战参战人员列表*/
	public final String TWACTIVITY_JOIN_PLAYER = "tw_join_player";
	
	/** 泰伯利亚之战房间信息*/
	public final String TWACTIVITY_ROOM_INFO = "tw_room_info";
	
	/** 泰伯利亚之战联盟信息*/
	public final String TWACTIVITY_GUILD_INFO = "tw_guild_info";
	
	/** 泰伯利亚之战匹配状态*/
	public final String TWACTIVITY_MATCH_STATE = "tw_match_state";

	/** 泰伯利亚之战匹配权限锁*/
	public final String TWACTIVITY_MATCH_LOCK = "tw_match_lock";
	
	/** 泰伯利亚之战战斗开启阶段状态*/
	public final String TWACTIVITY_FIGHT_STATE = "tw_fight_state";
	
	/** 泰伯利亚之战玩家信息*/
	public final String TWACTIVITY_PLAYER_INFO = "tw_player_info";
	
	/** 泰伯利亚之战历史联盟战报*/
	public final String TWACTIVITY_BATTLE_HISTORY = "tw_battle_log";
	
	/** 泰伯利亚之战联盟小组信息*/
	public final String TWACTIVITY_GUILD_TEAM = "tw_guild_team";
	
	/** 泰伯利亚之战联盟小组构建信息*/
	public final String TWACTIVITY_GUILD_TEAM_BUILD = "tw_guild_team_build";
	/**************************************************/
	/** 泰伯利亚联赛区服活动阶段数据 */
	public final String TLWACTIVITY_INFO = "tlw_activity_info";
	
	/** 泰伯利亚之战战斗开启阶段状态*/
	public final String TLWACTIVITY_FIGHT_STATE = "tlw_fight_state";
	
	/** 泰伯利亚联赛本服前30联盟信息*/
	public final String TLWACTIVITY_GUILD_INFO = "tlw_guild_info";
	
	/** 泰伯利亚联赛联盟战力排行*/
	public final String TLWACTIVITY_GUILD_POWER_RANK = "tlw_guild_power_rank";
	
	/** 泰伯利亚联赛参赛联盟*/
	public final String TLWACTIVITY_JOIN_GUILD = "tlw_join_guild";
	
	/** 泰伯利亚联赛匹配状态*/
	public final String TLWACTIVITY_MATCH_STATE = "tlw_match_state";

	/** 泰伯利亚联赛匹配权限锁*/
	public final String TLWACTIVITY_MATCH_LOCK = "tlw_match_lock";
	
	/** 泰伯利亚联赛决赛赛程信息*/
	public final String TLWACTIVITY_FINAL_GROUP = "tlw_final_group";
	
	/** 泰伯利亚联赛玩家积分*/
	public final String TLWACTIVITY_PLAYER_SCORE_INFO = "tlw_player_score";
	
	/** 泰伯利亚联赛联盟积分*/
	public final String TLWACTIVITY_GUILD_SCORE_INFO = "tlw_guild_score";
	
	/** 泰伯利亚联赛正赛参赛联盟成员列表-决赛战斗结束后成员快照*/
	public final String TLWACTIVITY_MEMBERS = "tlw_members";
	
	/** 泰伯利亚联赛正赛联盟总排行*/
	public final String TLWACTIVITY_GUILD_TOTAL_RANK = "tlw_guild_total_rank";

	public final String TLW_NEW_SIGNUP_OLD = "TLW_NEW_SIGNUP_OLD";

	public final String TLW_NEW_SIGNUP_OLD_SERVER = "TLW_NEW_SIGNUP_OLD_SERVER";
	/***********************泰伯利亚elo机制****************************/
	/** 泰伯利亚之战联盟信息*/
	public final String TWACTIVITY_GUILD_ELO_DATA = "tw_guild_elo";
	/**按日重置类做用号记数*/
	static final String EFFECT_DAY_USECNT = "effect_dayyy_cnt";
	
	/***********************泰伯利亚elo机制****************************/
	/************************星球大战**************************/
	/** 星球大战区服活动阶段数据 */
	public final String SWACTIVITY_INFO = "sw_activity_info";
	
	/** 星球大战战斗开启阶段状态*/
	public final String SWACTIVITY_FIGHT_STATE = "sw_fight_state";
	
	/** 星球大战入围联盟信息*/
	public final String SWACTIVITY_GUILD_INFO = "sw_guild_info";
	
	/** 星球大战各区入围联盟*/
	public final String SWACTIVITY_SERVER_GUILD = "sw_server_guild"; 
	
	/** 星球大战联盟战力排行*/
	public final String SWACTIVITY_GUILD_POWER_RANK = "sw_guild_power_rank";
	
	/** 星球大战参赛联盟*/
	public final String SWACTIVITY_JOIN_GUILD = "sw_join_guild";
	
	/** 星球大战匹配状态*/
	public final String SWACTIVITY_MATCH_STATE = "sw_match_state";

	/** 星球大战匹配权限锁*/
	public final String SWACTIVITY_MATCH_LOCK = "sw_match_lock";
	
	/** 星球大战房间信息*/
	public final String SWACTIVITY_ROOM_INFO = "sw_room_info";
	
	/** 星球大战玩家信息*/
	public final String SWACTIVITY_PLAYER_INFO = "sw_player_info";
	
	/************************星球大战**************************/
	
	
	/**
	 * 要塞占领信息
	 */
	public static final String FORTRESS_OCCUPY_INFO = "fortress_occ";
	public static final String CROSS_REFRESH_LOCK= "cross_refresh_lock";
	public static final String CROSS_REFRESH_FINISH= "cross_refresh_finish";
	public static final String CROSS_REFRESH_REWARD= "cross_refresh_reward";
	public static final String CROSS_REFRESH_TERM = "cross_refresh_term";
	/**
	 * 航海之星
	 */
	public static final String FORTRESS_STAR = "fortress_star";
	public static final String FORTRESS_STAR_BAK = "fortress_star_bak";
	/**
	 * 跨服的类型.
	 */
	public static final String CROSS_TYPE = "cross_type";
	
	/**
	 * 城防燃烧速度
	 */
	public static final String WALL_FIRE_SPEED = "wall_fire_speed";
	
	/**
	 * 玩家装扮赠送记录 
	 */
	public static final String PLAYER_DRESS_SEND_LOG = "player_dress_send_log";
	
	/**
	 * 玩家装扮请求赠送记录
	 */
	public static final String PLAYER_DRESS_ASK_LOG = "player_dress_ask_log";
	
	/**
	 * 设置玩家的playerId:openId
	 */
	public static final String PLAYER_UID_OPENID_PREFIX = "player_uid_openid_prefix";
	
	/**
	 * openid 有没有被绑定过推广码
	 */
	public static final String SPREAD_OPENID_BIND_FLAG = "spread_openid_bind_flag";
	
	/**
	 * 腾讯url活动 活动模板8 今日是否领取 
	 */
	public static final String ACTIVITY_MODEL_8_PREFIX = "activity_model_8_prefix";
	
	/**
	 * 腾讯url活动 活动模板9 今日是否领取 
	 */
	public static final String ACTIVITY_MODEL_9_PREFIX = "activity_model_9_prefix";
	
	/**
	 * 腾讯url活动 活动模板10 今日是否领取 
	 */
	public static final String ACTIVITY_MODEL_10_PREFIX = "activity_model_10_prefix";

	/**
	 * 铠甲套装名字
	 */
	public static final String ARMOUR_SUIT_NAME = "armour_suit_name";

	/**
	 * 福袋分享时间存储
	 */
	public static final String BLESS_BAG_SHARE = "bless_bag_share";
	
	/**
	 * 每日重置类-当日已使用次数
	 */
	public static final String DAILY_RESET_USE_TIMES = "daily_reset_use_times";
	
	
	/** 锦标赛区服活动阶段数据 */
	public final String GCACTIVITY_SERVER_INFO = "gc_activity_info";
	
	/** 锦标赛匹配状态*/
	public final String GCACTIVITY_MATCH_STATE = "gc_match_state";
	
	/** 锦标赛匹配权限锁*/
	public final String GCACTIVITY_MATCH_LOCK = "gc_match_lock";
	
	/** 锦标赛战斗阶段状态*/
	public final String GCACTIVITY_FIGHT_STATE = "gc_fight_state";
	
	/** 锦标赛报名人员列表*/
	public final String GCACTIVITY_SIGN_PLAYER = "gc_sign_player";
	
	/** 锦标赛参战人员列表*/
	public final String GCACTIVITY_JOIN_PLAYER = "gc_join_player";
	
	/** 锦标赛出战人员信息*/
	public final String GCACTIVITY_PLAYER_INFO = "gc_player_info";
	
	/** 锦标赛联盟可领取奖励玩家*/
	public final String GCACTIVITY_REWARD_PLAYER = "gc_reward_player";
	
	/** 锦标赛参战联盟列表*/
	public final String GCACTIVITY_JOIN_GUILD = "gc_join_guild";
	
	/** 锦标赛联盟信息*/
	public final String GCACTIVITY_GUILD_INFO = "gc_guild_info";
	
	/** 锦标赛联盟分组信息*/
	public final String GCACTIVITY_GROUP_INFO = "gc_group_info";
	
	/** 锦标赛联盟战斗数据*/
	public final String GCACTIVITY_GUILD_BATTLE_INFO = "gc_gbattle_info";
	
	/** 锦标赛玩家战斗数据*/
	public final String GCACTIVITY_PLAYER_BATTLE_INFO = "gc_pbattle_info";
	
	/**
	 * 修改内容时间记录
	 */
	public static final String CHANGE_CONTENT_TIME = "change_content_time";
	/**
	 * 修改内容CD时长记录
	 */
	public static final String CHANGE_CONTENT_TIME_CD = "change_content_time_cd";
	/**联盟帮助次数*/
	private final String TODAY_HOSPICE = "today_hpspice:";
	/** 超能试验室激活*/
	static final String SUPER_LAB_JIHUO =  "superlabjihuo:";
	/**
	 * db信息
	 */
	public static final String DB_INFO = "dbinfo";
	
	/**
	 * redis信息
	 */
	public static final String REDIS_INFO = "redisinfo";
	
	/**
	 * log信息
	 */
	public static final String LOG_INFO = "loginfo";
	
	/**
	 * 存储跨服守护问题.
	 */
	public static final String CROSS_GUARD = "cross_guard";
	/**
	 * 星球大战官职记录
	 */
	public static final String STAR_WARS_KING_RECORD = "star_wars_king_record_v2";
	/**
	 * 星球大战礼包记录.
	 */
	public static final String STAR_WARS_GIFT_RECORD = "star_wars_gift_record_v2";
	/**
	 * 礼包的颁发情况
	 */
	public static final String STAR_WARS_GIFT = "star_wars_gift_v2";
	/**
	 * 星球大战官职.
	 */
	public static final String STAR_WARS_OFFICER = "star_wars_officer_v2";
	/**
	 * 参赛联盟首领信息.
	 */
	public static final String STAR_WARS_JOIN_GUILD_LEADER = "star_wars_join_guild_leader_v2";
	/**
	 * 正在登陆的玩家.
	 */
	public static final String STAR_WARS_LOGINING_PLAYER = "star_wars_logining_player";
	/**
	 * 跨服玩家结构
	 */
	public static final String CROSS_PLAYER_STRUCT = "cross_player_struct";
	/**
	 * 国王.
	 */
	public static final String SERVER_KING_STRUCT = "server_king_struct";
	/**
	 * 合服卸载星球大战的官职.
	 */
	public static final String MERGE_SERVER_UNSET_OFFICER = "merge_server_unset_officer";
	/**
	 * 攻防模拟战活动.
	 */
	public static final String SIMULATE_WAR_ACTIVITY_DATA = "simulate_war_activity_data";
	/**
	 * 攻防模拟战 玩家助威.
	 */
	public static final String SIMULATE_WAR_PLAYER_EXT	= "simulate_war_player_ext";
	/**
	 * 报名的工会信息
	 */
	public static final String SIMULATE_WAR_GUILD_DATA = "simulate_war_guild_data";
	/**
	 * 参与匹配的工会信息.
	 */
	public static final String SIMULATE_WAR_MATCH_GUILD_ID = "simulate_war_match_guild_id";
	/**
	 * 攻防模拟战的玩家信息.
	 */
	public static final String SIMULATE_WAR_PLAYER_DATA ="simulate_war_player_data";
	/**
	 * 攻防模拟战的线路信息.
	 */
	public static final String SIMULATE_WAR_WAY = "simulate_war_way";
	/**
	 * 攻防模拟战的匹配信息.
	 */
	public static final String SIMULATE_WAR_MATCH_INFO = "simulate_war_match_info";
	/**
	 * 匹配状态.
	 */
	public static final String SIMULATE_WAR_MATCH_STATE = "simulate_war_match_state";
	/**
	 * 攻防模拟战匹配锁.
	 */
	public static final String SIMULATE_WAR_LOCK = "simulate_war_lock";
	/**
	 * 对战信息
	 */
	public static final String SIMULATE_WAR_GUILD_BATTLE = "simulate_war_guild_battle";
	/**
	 * 战斗记录.
	 */
	public static final String SIMULATE_WAR_BATTLE_RECORD = "simulate_war_battle_record";
	/**
	 * 记录已经出战的人员.
	 */
	public static final String SIMULATE_WAR_BATTLE_PLAYER = "simulate_war_battle_player";
	
	/** 玩家仇恨排行信息缓存 */
	public static final String PLAYER_HATRED_RANK_KEY = "player_hatred_rank";
	/** 玩家仇恨1-1信息缓存 */
	public static final String PLAYER_HATRED_SINGLE_KEY = "player_hatred_single";
	/**
	 * 守护赠送历史记录
	 */
	public static final String GUARD_DRESS_SEND_HISTORY = "guard_dress_send_history";
	/**
	 * 守护索要历史记录。
	 */
	public static final String GUARD_DRESS_BLAG_HISTORY = "guard_dress_blag_history";
	
	/**今日被决斗*/
	static final String TODAY_DEULED = "today_duel_edddd";
	
	/**
	 * 账号注销检测时间
	 */
	public static final String ACCOUNT_CANCELLATION_CHECK = "acc_cancel_check";
	
	/**
	 * 账号注销开始时间
	 */
	public static final String ACCOUNT_CANCELLATION_BEGIN = "acc_cancel_begin";
	/**
	 * 跨服国王的记录.
	 */
	public static final String CROSS_KING_PLAYER = "cross_king_player";
	/**
	 * 自动匹配写入工会数据
	 */
	public static final String CROSS_MATCH_SERVER_BATTLE = "cross_match_server_battle";
	/**
	 * 自动匹配写入的区服列表.
	 */
	public static final String CROSS_SERVER_LIST = "cross_server_list";
	/**
	 * 跨服匹配锁.
	 */
	public static final String CROSS_MATCH_LOCK = "cross_match_lock";
	
	/**
	 * 全局推送
	 */
	public static final String PLAYER_BACK_MESSAGE_PUS_KEY = "player_back_message_push";
	
	/** 
	 *好友推送
	 * 
	 */
	public static final String SEND_FRIEND_PUSH_KEY = "send_friend_push";
	
	/**
	 * 玩家回流
	 */
	public static final String PLAYER_BACK_FLOW_ACCOUNT = "player_back_flow_account";
	
	/**
	 * 回流角色
	 */
	public static final String PLAYER_BACK_FLOW_ROLE = "player_back_flow_role";
	
	public static final String LABORATORY_REMAKE_COST = "laboratory_remake_costoiu:";

	/**
	 * 装备科技外显
	 */
	public static final String SHOW_EQUIP_TECH = "show_equip_tech";
	/**
	 * 合服年兽k值处理
	 */
	public static final String MERGE_NIAN_K = "merge_nian_k";
	
	/**年兽上次的K值*/
	static final String NIAN_LAST_K = "nian_last_k";
	
	/** 决斗值 */
	static final String DUEL_CONTROL_POWER = "duel_control_powerr:";

	/**
	 * 装扮称号显示类型
	 */
	static final String DRESS_TITLE_TYPE = "dress_title_type";
	
	/**
	 * 学院信息展示
	 */
	static final String COLLEGE_NAME_SHOW = "college_name_show";

	/**
	 * 每日任务宝箱领取次数
	 */
	static final String DAILY_MISSION_BOX_COUNT = "daily_mission_box";
	
	/**
	 * 机甲建筑任务数据
	 */
	public static final String 	SUPERSOLDIER_TASK = "supersoldier_task";

	/**
	 * 禁止玩家操作
	 */
	public static final String 	BAN_PLAYER_OPER_KEY = "ban_player_oper";
	
	static final String CumulativeMission = "cumulative_missions";
	
	/** 旅行商人 */
	static final String TRAVEL_SHOP = "travel_shop";
	/** 黑市商人概率 */
	static final String TRAVEL_GIFT_PROB = "travel_gift_prob";
	/** 黑市商人VIP概率 */
	static final String VIP_TRAVEL_GIFT_PROB = "vip_travel_gift_prob";

	/**
	 * 特惠商人好友度
	 */
	public static final String TRAVELSHOP_FRIENDLY = "travel_shop_friendly";
	public static final String TRAVELSHOP_FRIENDLY_INFO = "travel_shop_friendly_info";
	/**
	 * 特惠商人特权卡购买时间
	 */
	public static final String TRAVELSHOP_CARD = "travel_shop_card";
	/**
	 * 至尊vip信息
	 */
	public static final String SUPER_VIP = "player_super_vip";
	
	/**
	 * 国家任务集合
	 */
	public static final String NATION_MISSIONS = "nation_mission";
	
	/**
	 * 国家任务检测增加时间
	 */
	public static final String NATION_MISSION_CHECK_ADD_TIME = "nation_mission_check_add";
	
	/**
	 * 国家哪天刷新的
	 */
	public static final String NATION_MISSION_REFRESH_DAY = "nation_mission_refresh_day";
	
	/**
	 * 迁服战力设置
	 */
	public static final String IMMGRATION_POWER_SET = "immgration_power_set";
	
	/**
	 * 迁服人数
	 */
	public static final String IMMGRATION_NUM = "immgration_num";
	
	/**
	 * 已经迁服的玩家
	 */
	public static final String IMMGRATION_PLAYER = "immgration_player";
	
	/**
	 * 排行榜key
	 */
	public static final String[] RANK_KEYS = new String[] {"pfight_rank", "pkill_rank", "pcity_rank", "plvl_rank", "gfight_rank", "gkill_rank", "pnoarmy_rank"};
	
	/**
	 * 国家科技
	 */
	public static final String NATION_TECH = "nation_tech";
	
	/**
	 * 国家科技技能
	 */
	public static final String NATION_TECH_SKILL = "nation_tech_skill";
	
	/**
	 * 国家科技研究
	 */
	public static final String NATION_TECH_RESEARCH = "nation_tech_research";
	
	/**
	 * 国家科技每日增加
	 */
	public static final String NATION_TECH_DAIL = "nation_tech_daily";
	
	/**
	 * 国家科技研究值
	 */
	public static final String NATION_TECH_VALUE = "nation_tech_value";
	
	/**
	 * 移民记录
	 */
	public static final String IMMGRATION_RECORD = "immgration_record";
	/**
	 * 回流移民记录
	 */
	public static final String BACK_IMMGRATION_RECORD = "back_immgration_record";

	/**
	 * 移民钻石处理
	 */
	public static final String IMMGRATION_DIAMONDS = "immgration_diamonds";
	
	/**
	 * 移民存根
	 */
	public static final String IMMGRATION_LOG = "immgration_log";
	
	
	/**
	 * 国家建筑等级
	 */
	public static final String NATION_BUILDING_LEVEL = "nation_building_level";
	
	/**
	 * 玩家官职数据
	 */
	public static final String PLAYER_OFFICER_CACHE = "player_officer";
	public static final String PLAYER_OFFICER_SET_CACHE = "player_officer_set";
	
	/**
	 * 跨服礼包信息
	 */
	private static final String CROSS_GIFT = "cross_gift";
	
	/**
	 * 跨服礼包记录
	 */
	private static final String CROSS_GIFT_RECORD = "cross_gift_record";
	
	/**
	 * 跨服礼包个人记录
	 */
	private static final String CROSS_GIFT_PLAYER_RECORD = "cross_gift_player_record";
	
	/**
	 * 远征要塞开启标记
	 */
	private static final String CROSS_FORTRESS_OPEN = "cross_fortress_open";
	
	/**
	 * 远征要塞占领信息
	 */
	private static final String CROSS_FORTRESS_OCC_COUNT = "cross_fortress_occ_count";
	
	/**
	 * 能量塔占领信息
	 */
	private static final String CROSS_PYLON_OCC_COUNT = "cross_pylon_occ_count";
	
	/**
	 * 跨服出战联盟信息
	 */
	private static final String CROSS_FIGHT_GUILD_INFO = "cross_fight_guild_info";
	
	/**
	 * 跨服可以出战联盟信息存储
	 */
	private static final String CROSS_CAN_FIGHT_GUILD = "cross_can_fight_guild";
	
	/**
	 * 战时总司令
	 */
	private static final String CROSS_FIGHT_PRESIDENT = "cross_fight_president";
	
	/**
	 * 航海远征战略点
	 */
	private static final String CROSS_TALENT_POINT = "cross_talent_point";
	
	/**
	 * 跨服征服排行榜
	 */
	private static final String CROSS_CONQUER_RANK = "cross_conquer_rank";
	private static final String CROSS_CONQUER_RANK_BAK = "cross_conquer_rank_bak";
	
	/**
	 * 跨服资源狂欢宝箱获取个数
	 */
	private static final String CROSS_RESOURCE_SPREE_BOX_GET_COUNT = "cross_resource_spree_box_get_count";
	
	/**
	 * 跨服活动记录
	 */
	private static final String CROSS_ACTIVITY_RECORD = "cross_activity_record";
	
	/**
	 * 刷新9级矿
	 */
	private static final String CROSS_REFRESH_SPECIAL_RES = "cross_refresh_special_res";

	/**
	 * 活动版本号
	 */
	private static final String ACTIVITY_VERSION = "activity_version";
	
	/**
	 * 跨服邀请
	 */
	private static final String CROSS_INVITE = "cross_invite";
	private static final String CROSS_INVITE_TIME = "cross_invite_time";
	
	/**
	 * 跨服申请
	 */
	private static final String CROSS_APPLY = "cross_apply";
	private static final String CROSS_APPLY_TIME = "cross_apply_time";

	/**
	 * 跨服盟总推进度条信息
	 */
	private static final String CROSS_RATE_INFO = "cross_rate_info";
	
	/**
	 * 跨服盟总占领时间信息
	 */
	private static final String CROSS_OCCUPY_INFO = "cross_Occupy_info";

	/**
	 * 赠送时间限制道具
	 */
	private static final String SEND_TIME_LIMIT_TOOL = "send_time_limit_tool";
	/**
	 * 红点开关
	 */
	private static final String RED_DOT_SWITCH = "red_dot_switch";
	/**
	 * 赠送装扮信使礼包
	 */
	private static final String SEND_DRESS_GIFT = "send_dress_gift";
	/**
	 * 超值礼包每月限购标记
	 */
	private static final String SUPER_GIFT_MONTH_MARK = "super_gift_month_mark";
	/**
	 * 英雄档案馆开放奖励
	 */
	private static final String HERO_ARCHIVE_OPEN_AWARD = "hero_archive_open_award";
	
	/**
	 * 获取联盟编队小红点
	 */
	private static final String GUILD_FORMATION_RED = "guild_formation_red";
	
	/**
	 * 跨服联盟编队
	 */
	private static final String CS_GUILD_FORMATION = "cs_guild_formation";
	
	/**
	 * 拆服守护补偿
	 */
	private static final String SEPARATE_GUARD = "separate_guard";
	/**
	 * IDIP每日统计量
	 */
	private static final String IDIP_DAILY_STATIS = "idip_daily_statis";
	
	/**
	 * 迁服后一段时间内禁止登录
	 */
	private static final String IMMGRATION_BAN_LOGIN = "immgration_ban_login";
	/**
	 * 删除玩家标识
	 */
	private static final String REMOVE_PLAYER_FLAG = "remove_player_flag";
	/**
	 * 玩家大本等级达成时间(40级以上才记录)
	 */
	private static final String PLAYER_CITY_RANK_TIME = "player_city_rank_time";
	/**
	 * 外部充值未完成标记
	 */
	private static final String RECHARGE_OUTTER_UNFINISH = "unfinished_recharge_outter";
	/**
	 * 转平台信息
	 */
	private static final String PLAT_TRANSFER_INFO = "plat_transter";
	
	/**
	 * 通过高迁修复部队时间戳
	 */
	private static final String MOVE_CITY_FIX_ARMY = "move_city_fix_army";
	
	/**
	 * 军事需要成员体力发放
	 */
	private static final String COLLEGE_MEMBER_VIT_SEND = "college_member_vit_send";
	
	/**
	 * 军事学院成员体力消耗
	 */
	private static final String COLLEGE_MEMBER_VIT_COST = "college_member_vit_cost";
	
	
	/**
	 * 反攻幽灵活动状态数据
	 */
	public static final String FGYL_ACTIVITY_STATE = "fgyl_activity_state_data";

	/**
	 * 反攻幽灵活动报名数据
	 */
	public static final String FGYL_ACTIVITY_SIGN = "fgyl_activity_sign_data";

	/**
	 * 反攻幽灵活动当前期排行榜
	 */
	public static final String FGYL_TERM_RANK = "fgyl_term_rank";

	/**
	 * 反攻幽灵活动历史荣耀榜
	 */
	public static final String FGYL_HONOR_RANK = "fgyl_honor_rank";

	/**
	 * 反攻幽灵活动历史荣耀榜联盟数据
	 */
	public static final String FGYL_HONOR_RANK_GUILD = "fgyl_honor_rank_guild";
	
	/**
	 * 航海远征中，跨过服的玩家
	 */
	public static final String CROSS_ACTIVITY_CROSSED_PLAYER = "CROSS_ACTIVITY_CROSSED_PLAYER:%d:%s";
	
	/**
	 * redis会话对象
	 */
	HawkRedisSession redisSession;
	HawkRedisSession oldRedisSession;
	
	/**
	 * redis访问的次数
	 */
	private AtomicLong redisOpCount;

	/**
	 * 全局实例对象
	 */
	private static RedisProxy instance = null;

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static RedisProxy getInstance() {
		if (instance == null) {
			instance = new RedisProxy();
		}

		instance.incRedisOp();
		return instance;
	}

	/**
	 * 构造
	 *
	 */
	private RedisProxy() {
		redisOpCount = new AtomicLong();
	}

	/**
	 * 初始化redis代理会话
	 *
	 * @param redisSession
	 * @return
	 */
	public boolean init() {
		// 判断是否配置redis的主机地址
		if (!HawkOSOperator.isEmptyString(GsConfig.getInstance().getGlobalRedis())) {
			HawkRedisPoolConfig config = new HawkRedisPoolConfig();
			config.setMaxTotal(GsConfig.getInstance().getRedisMaxActive());
			config.setMaxIdle(GsConfig.getInstance().getRedisMaxIdle());
			config.setMaxWaitMillis(GsConfig.getInstance().getRedisMaxWait());

			int redisPort = 6379;
			int timeout = GsConfig.getInstance().getRedisTimeout();
			String redisHost = GsConfig.getInstance().getGlobalRedis();
			String redisAuth = GsConfig.getInstance().getGlobalRedisAuth();
			if (!HawkOSOperator.isEmptyString(redisHost)) {
				String[] infos = redisHost.split(":");
				redisHost = infos[0];
				redisPort = Integer.valueOf(infos[1]);
			}

			redisSession = new HawkRedisSession();
			if (!redisSession.init(redisHost, redisPort, timeout, redisAuth, config)) {
				HawkLog.errPrintln("init global redis failed, ip: {}, port: {}", redisHost, redisPort);
				return false;
			}
			HawkLog.logPrintln("init global redis success, ip: {}, port: {}", redisHost, redisPort);
		}
		
		return true;
	}
	

	/**
	 * 获取会话对象, 便于脚本调用
	 * 
	 * @return
	 */
	public HawkRedisSession getRedisSession() {
		return redisSession;
	}
	
	public HawkRedisSession getOldRedisSession() {
		return oldRedisSession;
	}

	/**
	 * 增加redis操作
	 * 
	 * @return
	 */
	public long incRedisOp() {
		return redisOpCount.incrementAndGet();
	}

	/**
	 * 获取redis操作数
	 * 
	 * @return
	 */
	public long getRedisOpCount() {
		return redisOpCount.get();
	}

	/**
	 * 重置redis操作数
	 */
	public void resetRedisOpCount() {
		redisOpCount.set(0);
	}

	/**
	 * 获取列表元素
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public List<String> getList(String key, long start, long end) {
		return redisSession.lRange(key, start, end, 0);
	}

	/**
	 * 设置要一个列表索引值
	 * 
	 * @param key
	 * @param index
	 * @param value
	 * @return
	 */
	public String lset(String key, long index, String value) {
		return redisSession.lSet(key, index, value);
	}

	/**
	 * 向列表中添加元素
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public long rpush(String key, String... value) {
		return redisSession.rPush(key, 0, value);
	}

	/**
	 * 写性能监视数据
	 * 
	 * @param key
	 * @param value
	 */
	public boolean writeMonitorInfo(long maxLen, String type, String key, String... value) {
		String monitorKey = String.format("monitor:%s:%s", key, GsConfig.getInstance().getServerId());
		if ("hash".equals(type)) {
			redisSession.setString(monitorKey, value[0]);
		} else {
			long length = redisSession.lPush(monitorKey, 0, value);
			if (maxLen > 0 && length > maxLen) {
				redisSession.lTrim(monitorKey, 0, maxLen);
			}
		}

		return true;
	}

	/**
	 * 读性能监视数据
	 * 
	 * @param key
	 * @param count
	 */
	public List<String> readMonitorInfo(String serverId, String key, int start, long count, String type) {
		String monitorKey = String.format("monitor:%s", key);
		if (!HawkOSOperator.isEmptyString(serverId)) {
			monitorKey = String.format("%s:%s", monitorKey, serverId);
		}

		if ("hash".equals(type)) {
			return Arrays.asList(redisSession.getString(monitorKey));
		}

		List<String> list = redisSession.lRange(monitorKey, start, count + start, 0);
		Collections.reverse(list);
		return list;
	}

	/**
	 * 获取服务器列表
	 * 
	 * @return
	 */
	public List<ServerInfo> getServerList() {
		List<ServerInfo> serverList = new LinkedList<ServerInfo>();
		Map<String, String> serverInfoMap = redisSession.hGetAll(SERVER_LIST_KEY);
		if (serverInfoMap != null) {
			for (String value : serverInfoMap.values()) {
				JSONObject json = JSON.parseObject(value);
				ServerInfo serverInfo = new ServerInfo();
				if (serverInfo.updateFromJson(json)) {
					serverList.add(serverInfo);
				}
			}
		}

		return serverList;
	}

	/**
	 * 获取指定id的服务器信息
	 * 
	 * @param serverId
	 * @return
	 */
	public ServerInfo getServerInfo(String serverId) {
		String value = redisSession.hGet(SERVER_LIST_KEY, serverId);
		StatisManager.getInstance().incRedisKey(SERVER_LIST_KEY);
		if (!HawkOSOperator.isEmptyString(value)) {
			JSONObject json = JSON.parseObject(value);
			ServerInfo serverInfo = new ServerInfo();
			if (serverInfo.updateFromJson(json)) {
				return serverInfo;
			}
		}

		return null;
	}

	/**
	 * 更新服务器条件配置信息
	 * 
	 * @param serverId
	 * @param statusCfg
	 */
	public boolean updateServerCondCfg(String serverId, JSONObject condJson) {
		StatisManager.getInstance().incRedisKey(SERVER_COND_KEY);
		return redisSession.hSet(SERVER_COND_KEY, serverId, condJson.toJSONString()) >= 0;
	}

	/**
	 * 重置服务器的当前注册和在线人数
	 * 
	 * @param statusInfo
	 * @return
	 */
	protected boolean updateServerStatus(ServerStatus serverStatus) {
		StatisManager.getInstance().incRedisKey(SERVER_STATUS_KEY);
		String serverId = serverStatus.getServerId();
		return redisSession.hSet(SERVER_STATUS_KEY, serverId, serverStatus.toString()) >= 0;
	}

	/**
	 * 获取服务器的状态
	 * 
	 * @param serverId
	 * @return
	 */
	public ServerStatus getServerStatus(String serverId) {
		StatisManager.getInstance().incRedisKey(SERVER_STATUS_KEY);
		ServerStatus ss = new ServerStatus();
		String serverStatusStr = redisSession.hGet(SERVER_STATUS_KEY, serverId);
		ss.parseFrom(JSON.parseObject(serverStatusStr));

		return ss;
	}
	
	/**
	 * 玩家充值相关数据批量存储
	 * @param rechargeInfo
	 */
	public void rechargeBatchSave(RechargeInfo rechargeInfo) {
		String playerId = rechargeInfo.getPlayerId();
		int value = rechargeInfo.getCount();
		String timeYMD = HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), HawkTime.FORMAT_YMD);
		
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			// 1.RedisProxy.getInstance().incServerDailyInfo(DailyInfoField.DAY_RECHARGE, rechargeAmt);
			String svrDailyLocalKey = String.format("%s:%s:%s", SERVER_DAILY_KEY, timeYMD, GsConfig.getInstance().getServerId());
			pip.hincrBy(svrDailyLocalKey, DailyInfoField.DAY_RECHARGE, value);
			//
			String svrDailyGlobalKey = String.format("%s:%s", SERVER_DAILY_KEY, timeYMD); // 存储全局数据，不分哪个服
			pip.hincrBy(svrDailyGlobalKey, DailyInfoField.DAY_RECHARGE, value);
			
			// 2.RedisProxy.getInstance().incGlobalStatInfo(DailyInfoField.DAY_RECHARGE, rechargeAmt);
			String globalStatkey = String.format("%s:%s", GLOBAL_STAT_KEY, GsConfig.getInstance().getServerId());
			pip.hincrBy(globalStatkey, DailyInfoField.DAY_RECHARGE, value);
			//
			pip.hincrBy(GLOBAL_STAT_KEY, DailyInfoField.DAY_RECHARGE, value); // 存储全局数据，不分哪个服
			
			// 3.RedisProxy.getInstance().idipDailyStatisAdd(getId(), IDIPDailyStatisType.DIAMOND_RECHARGE, rechargeAmt);
			String idipDailyStatKey1 = IDIP_DAILY_STATIS + ":" + playerId + ":" + HawkTime.getYyyyMMddIntVal() + ":" + IDIPDailyStatisType.DIAMOND_RECHARGE.getTypeValue();
			pip.incrBy(idipDailyStatKey1, value);
			pip.expire(idipDailyStatKey1, GsConst.DAY_SECONDS);
			
			// 4.RedisProxy.getInstance().idipDailyStatisAdd(getId(), IDIPDailyStatisType.TOTAL_RECHARGE, rechargeAmt);
			String idipDailyStatKey2 = IDIP_DAILY_STATIS + ":" + playerId + ":" + HawkTime.getYyyyMMddIntVal() + ":" + IDIPDailyStatisType.TOTAL_RECHARGE.getTypeValue();
			pip.incrBy(idipDailyStatKey2, value);
			pip.expire(idipDailyStatKey2, GsConst.DAY_SECONDS);
			
			// 5.RedisProxy.getInstance().addRechargeInfo(rechargeInfo);
			String key = RedisKey.RECHARGE_INFO_2024 + ":" + rechargeInfo.getOpenid();
			pip.lpush(key, JSONObject.toJSONString(rechargeInfo));
			String totalKey = RedisKey.ROLE_RECHARGE_TOTAL + ":" + rechargeInfo.getOpenid();
			pip.hincrBy(totalKey, playerId, value);
			
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 查询单服每日统计信息，日注册，日活，日充值
	 * 
	 * @param field
	 *            "dayReg"日新增注册 "dayLogin"日活跃用户 "dayRecharge"日充值总额 "dayRechargePlayer"日充值玩家人数
	 * @param value
	 * @return
	 */
	public void incServerDailyInfo(String field, int value) {
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			String key = String.format("%s:%s:%s", SERVER_DAILY_KEY, HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), HawkTime.FORMAT_YMD),
					GsConfig.getInstance().getServerId());
			pip.hincrBy(key, field, value);
			// 存储全局数据，不分哪个服
			String globalKey = String.format("%s:%s", SERVER_DAILY_KEY, HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), HawkTime.FORMAT_YMD));
			pip.hincrBy(globalKey, field, value);
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		StatisManager.getInstance().incRedisKey(SERVER_DAILY_KEY);
	}

	/**
	 * 更新服务器总统计量
	 * 
	 * @param field
	 * @param value
	 * @return
	 */
	public void incGlobalStatInfo(String field, int value) {
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			String key = String.format("%s:%s", GLOBAL_STAT_KEY, GsConfig.getInstance().getServerId());
			pip.hincrBy(key, field, value);
			// 存储全局数据，不分哪个服
			pip.hincrBy(GLOBAL_STAT_KEY, field, value);
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		StatisManager.getInstance().incRedisKey(GLOBAL_STAT_KEY);
	}

	/**
	 * 查询单服每日统计信息，日注册，日活
	 * 
	 * @param field
	 * @param param
	 * @return
	 */
	public int getServerDailyInfo(String field) {
		String key = String.format("%s:%s:%s", SERVER_DAILY_KEY, HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), HawkTime.FORMAT_YMD),
				GsConfig.getInstance().getServerId());
		String count = redisSession.hGet(key, field);
		StatisManager.getInstance().incRedisKey(SERVER_DAILY_KEY);
		if (!HawkOSOperator.isEmptyString(count)) {
			return Integer.valueOf(count);
		}

		return 0;
	}

	/**
	 * 获取指定日期的日统计量数据
	 * 
	 * @param time
	 *            格式：yyyy-MM-dd
	 * @return
	 */
	public Map<String, String> getServerDailyInfoMap(String dayTime) {
		String key = String.format("%s:%s:%s", SERVER_DAILY_KEY, dayTime, GsConfig.getInstance().getServerId());
		Map<String, String> result = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(SERVER_DAILY_KEY);
		return result;
	}

	/**
	 * 获取全服日统计量数据
	 * 
	 * @return
	 */
	public Map<String, String> getGlobalDailyInfoMap(String dayTime) {
		String key = String.format("%s:%s", SERVER_DAILY_KEY, dayTime);
		Map<String, String> result = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(SERVER_DAILY_KEY);
		return result;
	}

	/**
	 * 获取单服总统计量数据
	 * 
	 * @return
	 */
	public Map<String, String> getServerTotalInfoMap() {
		String key = String.format("%s:%s", GLOBAL_STAT_KEY, GsConfig.getInstance().getServerId());
		Map<String, String> result = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(GLOBAL_STAT_KEY);
		return result;
	}

	/**
	 * 查询全服总统计量数据
	 * 
	 * @return
	 */
	public Map<String, String> getGlobalStatInfoMap() {
		Map<String, String> result = redisSession.hGetAll(GLOBAL_STAT_KEY);
		StatisManager.getInstance().incRedisKey(GLOBAL_STAT_KEY);
		return result;
	}

	/**
	 * 获取存储的鉴权信息
	 * 
	 * @param puid
	 * @return
	 */
	public String getAuthToken(String puid) {
		StatisManager.getInstance().incRedisKey(AUTH_TOKEN_KEY);
		return redisSession.getString(AUTH_TOKEN_KEY + ":" + puid);
	}

	/**
	 * 更新puid对应的玩家信息
	 * 
	 * @param puid
	 * @param profile
	 * @return
	 */
	public boolean updatePuidProfile(String puid, String profile) {
		if (HawkOSOperator.isEmptyString(profile)) {
			return false;
		}

		StatisManager.getInstance().incRedisKey(PUID_PROFILE_KEY);

		String key = PUID_PROFILE_KEY + ":" + puid;
		return redisSession.setString(key, profile);
	}

	/**
	 * 更新版本信息
	 * 
	 * @param key
	 * @param versionInfo
	 */
	public VersionInfo getVersionInfo(String areaId, String platform) {
		StatisManager.getInstance().incRedisKey(VERSION_INFO_KEY);
		String key = String.format("%s:%s", VERSION_INFO_KEY, areaId);
		String val = redisSession.hGet(key, platform);
		VersionInfo versionInfo = new VersionInfo();
		if (!HawkOSOperator.isEmptyString(val) && versionInfo.fromJson(JSON.parseObject(val))) {
			return versionInfo;
		}

		return null;
	}

	/**
	 * 获取账号的角色信息
	 * @param serverId
	 * @param platform
	 * @param openId
	 * @return
	 */
	public AccountRoleInfo getAccountRole(String serverId, String platform, String openId) {
		if (HawkOSOperator.isEmptyString(serverId) || HawkOSOperator.isEmptyString(platform)) {
			return null;
		}

		StatisManager.getInstance().incRedisKey(ACCOUNT_ROLE_KEY);
		String key = ACCOUNT_ROLE_KEY + ":" + openId;
		String innerKey = serverId + ":" + platform;
		String info = redisSession.hGet(key, innerKey);
		if (!HawkOSOperator.isEmptyString(info)) {
			return JSONObject.parseObject(info, AccountRoleInfo.class);
		}
		return null;
	}

	/**
	 * 获取一个账号在一个区服对应的所有角色信息
	 * 
	 * @param openId
	 * @return
	 */
	public Map<String, String> getAccountRole(String openId) {
		StatisManager.getInstance().incRedisKey(ACCOUNT_ROLE_KEY);
		String key = ACCOUNT_ROLE_KEY + ":" + openId;
		return redisSession.hGetAll(key);
	}

	/**
	 * 添加角色信息
	 * 
	 * @param accountRole
	 * @return
	 */
	public boolean addAccountRole(AccountRoleInfo accountRole) {
		if (accountRole == null) {
			return false;
		}
		StatisManager.getInstance().incRedisKey(ACCOUNT_ROLE_KEY);
		String key = ACCOUNT_ROLE_KEY + ":" + accountRole.getOpenId();
		
		String serverId = accountRole.getServerId();
		if (serverId.length() > 5) {
			if (serverId.length() > "2000000".length()) {
				serverId = serverId.substring(serverId.length() - "2000000".length());
			}
			int intSvrId = Integer.parseInt(serverId) % 2000000;
			accountRole.setServerId(String.valueOf(intSvrId));
		}
		
		String innerKey = accountRole.getServerId() + ":" + accountRole.getPlatform();
		return redisSession.hSet(key, innerKey, accountRole.toString()) >= 0;
	}

	/**
	 * 移除账号角色
	 * 
	 * @param openId
	 * @param serverId
	 */
	public void removeAccountRole(String openId, String serverId, String platform) {
		StatisManager.getInstance().incRedisKey(ACCOUNT_ROLE_KEY);
		String key = ACCOUNT_ROLE_KEY + ":" + openId;
		String innerKey = serverId;
		if (!HawkOSOperator.isEmptyString(platform)) {
			innerKey = innerKey + ":" + platform;
		}

		redisSession.hDel(key, innerKey);
	}

	/**
	 * 更新puid对应的玩家信息
	 * 
	 * @param puid
	 * @param profile
	 * @return
	 */
	public String getPuidProfile(String puid) {
		StatisManager.getInstance().incRedisKey(PUID_PROFILE_KEY);
		String key = PUID_PROFILE_KEY + ":" + puid;
		return redisSession.getString(key);
	}

	/**
	 * 更新玩家头像信息
	 * 
	 * @param puid
	 * @param pfIcon
	 * @return
	 */
	public boolean updatePfIcon(String puid, String pfIcon) {
		if (HawkOSOperator.isEmptyString(pfIcon)) {
			return false;
		}

		StatisManager.getInstance().incRedisKey(PUID_PFICON_KEY);
		String key = PUID_PFICON_KEY + ":" + puid; //puid_pfIcon:989F8DD768AF9DFCEAA80CF1D21E2B46#android
		return redisSession.setString(key, pfIcon);
	}

	/**
	 * 获取玩家头像信息
	 * 
	 * @param puid
	 * @return
	 */
	public String getPfIcon(String puid) {
		StatisManager.getInstance().incRedisKey(PUID_PFICON_KEY);
		String key = PUID_PFICON_KEY + ":" + puid;
		return redisSession.getString(key);
	}

	/**
	 * 最近登陆的服务器信息更新
	 * 
	 * @param serverId
	 * @param openid
	 * @param platform
	 * @return
	 */
	public boolean updateRecentServer(String serverId, String openid, String platform) {
		return updateRecentServer(serverId, openid, platform, String.valueOf(HawkTime.getSeconds()));
	}
	
	public boolean updateRecentServer(String serverId, String openid, String platform, String timeStr) {
		StatisManager.getInstance().incRedisKey(RECENT_SERVER_KEY);
		String key = String.format("%s:%s:%s", GsConfig.getInstance().getAreaId(), RECENT_SERVER_KEY, openid);
		String innerKey = serverId + ":" + platform;
		return redisSession.hSet(key, innerKey, timeStr) >= 0;
	}
	
	/**
	 * 更新服务器唯一标识
	 * 
	 * @param serverId
	 * @param identify
	 */
	public void updateServerIdentify(String serverId, String identify) {
		StatisManager.getInstance().incRedisKey(SERVER_IDENTIFY_KEY);
		redisSession.hSet(SERVER_IDENTIFY_KEY, serverId, identify);
	}

	/**
	 * 获取服务器唯一标识
	 * 
	 * @param serverId
	 * @return
	 */
	public String getServerIdentify(String serverId) {
		StatisManager.getInstance().incRedisKey(SERVER_IDENTIFY_KEY);
		return redisSession.hGet(SERVER_IDENTIFY_KEY, serverId);
	}

	/**
	 * 获取服务器专服标识
	 *
	 * @param serverId
	 * @return
	 */
	public String getServerProprietary(String serverId) {
		StatisManager.getInstance().incRedisKey(PROPRIETARY_SERVER);
		return redisSession.hGet(PROPRIETARY_SERVER, serverId);
	}

	/**
	 * 设置服务器专服标识
	 *
	 * @param serverId
	 * @return
	 */
	public void updateServerProprietary(String serverId, String proprietary) {
		StatisManager.getInstance().incRedisKey(PROPRIETARY_SERVER);
		redisSession.hSet(PROPRIETARY_SERVER, serverId, proprietary);
	}

	/**
	 * 更新玩家快照(由配置文件决定是二进制还是文本格式)
	 * 
	 * @param snapshot
	 * @return
	 */
	public boolean updatePlayerSnapshot(PlayerSnapshotPB snapshot) {
		StatisManager.getInstance().incRedisKey(SNAPSHOT_KEY);
		String key = SNAPSHOT_KEY + ":" + snapshot.getPlayerId();
		if (GsConfig.getInstance().isBinarySnapshot()) {
			byte[] value = snapshot.toByteArray();
			return redisSession.setBytes(key, value, GsConfig.getInstance().getPlayerRedisExpire());
		}

		String value = JsonFormat.printToString(snapshot);
		return redisSession.setString(key, value, GsConfig.getInstance().getPlayerRedisExpire());
	}

	/**
	 * 获取玩家快照信息(由配置文件决定是二进制还是文本格式)
	 * 
	 * @param playerId
	 * @return
	 */
	public PlayerSnapshotPB getPlayerSnapshot(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}

		StatisManager.getInstance().incRedisKey(SNAPSHOT_KEY);
		String key = SNAPSHOT_KEY + ":" + playerId;
		byte[] value = redisSession.getBytes(key.getBytes());
		return genSnapshotPB(value);
	}

	/**
	 * 生成快照数据
	 * 
	 * @param value
	 * @return
	 */
	private PlayerSnapshotPB genSnapshotPB(byte[] value) {
		PlayerSnapshotPB build = null;
		if (value == null) {
			return build;
		}
		try {
			if (value[0] == '{') {
				PlayerSnapshotPB.Builder builder = null;
				builder = PlayerSnapshotPB.newBuilder();
				JsonFormat.merge(new String(value, "UTF-8"), builder);
				build = builder.build();
			} else {
				build = PlayerSnapshotPB.parseFrom(value);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return build;
	}

	/**
	 * 删除玩家快照
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean deletePlayerSnapshot(String playerId) {
		StatisManager.getInstance().incRedisKey(SNAPSHOT_KEY);
		String key = SNAPSHOT_KEY + ":" + playerId;
		return redisSession.delete(key, GsConfig.getInstance().isBinarySnapshot());
	}

	/**
	 * 更新国王战信息
	 * 
	 * @param builder
	 * @return
	 */
	public boolean updatePresidentInfo(PresidentInfo.Builder builder) {
		StatisManager.getInstance().incRedisKey(PRESIDENT_INFO_KEY);
		String value = JsonFormat.printToString(builder.build());
		return redisSession.hSet(PRESIDENT_INFO_KEY, GsConfig.getInstance().getServerId(), value) >= 0;
	}

	/**
	 * 获取国王战信息
	 * 
	 * @return
	 */
	public PresidentInfo.Builder getPresidentInfo(String serverName) {
		if (HawkOSOperator.isEmptyString(serverName)) {
			serverName = GsConfig.getInstance().getServerId();
		}

		StatisManager.getInstance().incRedisKey(PRESIDENT_INFO_KEY);
		String value = redisSession.hGet(PRESIDENT_INFO_KEY, serverName);
		if (!HawkOSOperator.isEmptyString(value)) {
			PresidentInfo.Builder builder = PresidentInfo.newBuilder();
			try {
				JsonFormat.merge(new String(value.getBytes(), "UTF-8"), builder);
			} catch (ParseException | UnsupportedEncodingException e) {
				HawkException.catchException(e);
			}
			return builder;
		}

		return null;
	}

	/**
	 * 更新保存订单信息
	 * 
	 * @param orderId
	 * @param gameId
	 * @param serverId
	 * @param playerId
	 * @param platform
	 * @param channel
	 * @param deviceId
	 * @param goodsId
	 * @param goodsCount
	 * @param goodsPrice
	 * @param currency
	 * @param extra
	 * @return
	 */
	public boolean updateOrderInfo(String orderId, String serverId, String puid, String playerId,
			String platform, String channel, String country, String deviceId,
			String goodsId, int goodsCount, int goodsPrice, String currency, String extra) {
		JSONObject json = new JSONObject();
		json.put("orderId", orderId);
		json.put("serverId", serverId);
		json.put("puid", puid);
		json.put("playerId", playerId);
		json.put("platform", platform);
		json.put("channel", channel);
		json.put("country", country);
		json.put("deviceId", deviceId);
		json.put("goodsId", goodsId);
		json.put("goodsCount", goodsCount);
		json.put("goodsPrice", goodsPrice);
		json.put("currency", currency);
		if (!HawkOSOperator.isEmptyString(extra)) {
			json.put("extra", extra);
		}
		json.put("ts", HawkTime.formatNowTime());

		StatisManager.getInstance().incRedisKey(GENERATE_ORDER_KEY);
		String key = GENERATE_ORDER_KEY + ":" + orderId;
		int expireSeconds = GsConfig.getInstance().getOrderExpire();
		if (expireSeconds > 0) {
			return redisSession.setString(key, json.toJSONString(), expireSeconds);
		}

		return redisSession.setString(key, json.toJSONString());
	}

	/**
	 * 从缓存中获取回调的订单信息
	 * 
	 * @param orderId
	 * @return
	 */
	public JSONObject getCallBackOrderInfo(String puid, String billno) {
		StatisManager.getInstance().incRedisKey(RECHARGE_CB_KEY);
		String key = RECHARGE_CB_KEY + ":" + puid;
		String value = redisSession.hGet(key, billno);
		if (!HawkOSOperator.isEmptyString(value)) {
			return (JSONObject) JSONObject.parse(value);
		}

		return null;
	}

	/**
	 * 判断设备号是否被激活
	 * 
	 * @param deviceId
	 * @return
	 */
	public boolean isDeviceActived(String deviceId) {
		StatisManager.getInstance().incRedisKey(ACTIVE_DEVICE_KEY);
		return redisSession.hExists(ACTIVE_DEVICE_KEY, deviceId, 0);
	}

	/**
	 * 判断设备激活码是否可以使用(0: 可使用, -1: 不存在, 1: 已使用)
	 * 
	 * @param deviceId
	 * @return
	 */
	public int canUseDeviceActiveToken(String activeToken) {
		StatisManager.getInstance().incRedisKey(ACTIVE_TOKEN_KEY);
		String deviceId = redisSession.hGet(ACTIVE_TOKEN_KEY, activeToken);
		if (deviceId == null) {
			return -1;
		}

		if (deviceId.length() > 0) {
			return 1;
		}

		return 0;
	}

	/**
	 * 激活设备
	 * 
	 * @param deviceId
	 * @param activeToken
	 * @return
	 */
	public boolean activeDevice(String deviceId, String activeToken) {
		StatisManager.getInstance().incRedisKey(ACTIVE_DEVICE_KEY);
		redisSession.hSet(ACTIVE_DEVICE_KEY, deviceId, activeToken);

		StatisManager.getInstance().incRedisKey(ACTIVE_TOKEN_KEY);
		redisSession.hSet(ACTIVE_TOKEN_KEY, activeToken, deviceId);

		return true;
	}

	/**
	 * 创建激活码
	 * 
	 * @param activeToken
	 * @return
	 */
	public boolean createActiveToken(String activeToken) {
		StatisManager.getInstance().incRedisKey(ACTIVE_TOKEN_KEY);
		return redisSession.hSet(ACTIVE_TOKEN_KEY, activeToken, "") >= 0;
	}

	/**
	 * 获取设备的激活码
	 * 
	 * @param deviceId
	 * @return
	 */
	public String getDeviceActiveToken(String deviceId) {
		StatisManager.getInstance().incRedisKey(ACTIVE_DEVICE_KEY);
		return redisSession.hGet(ACTIVE_DEVICE_KEY, deviceId);
	}

	/**
	 * 查询激活码信息
	 * 
	 * @param getActiveTokens
	 * @return
	 */
	public Map<String, String> getTokenActiveDevices(String[] tokens) {
		Map<String, String> result = new HashMap<String, String>();
		StatisManager.getInstance().incRedisKey(ACTIVE_TOKEN_KEY);
		List<String> deviceIds = redisSession.hmGet(ACTIVE_TOKEN_KEY, tokens);
		for (int i = 0; i < tokens.length; i++) {
			if (!HawkOSOperator.isEmptyString(deviceIds.get(i))) {
				result.put(tokens[i], deviceIds.get(i));
			}
		}

		return result;
	}

	/**
	 * 累计型任务累计数量增长
	 * 
	 * @param missionFunTypeId
	 * @param playerId
	 * @param num
	 * @return
	 */
	public boolean addOverlayMissionAttr(int missionFunTypeId, String playerId, int num) {
		String key = OVERLAY_MISSION_KEY + ":" + playerId;
		StatisManager.getInstance().incRedisKey(OVERLAY_MISSION_KEY);
		return redisSession.hSet(key, String.valueOf(missionFunTypeId), String.valueOf(num), GsConfig.getInstance().getPlayerRedisExpire()) >= 0;
	}

	/**
	 * 获取累计型任务累计数量值
	 * 
	 * @param missionFunTypeId
	 * @param playerId
	 * @return
	 */
	public Integer getOverlayMissionAttr(int missionFunTypeId, String playerId) {
		String key = OVERLAY_MISSION_KEY + ":" + playerId;
		StatisManager.getInstance().incRedisKey(OVERLAY_MISSION_KEY);
		String accumulatedNum = redisSession.hGet(key, String.valueOf(missionFunTypeId));
		if (!HawkOSOperator.isEmptyString(accumulatedNum)) {
			return Integer.parseInt(accumulatedNum);
		}

		return null;
	}

	/**
	 * 获取玩家所有的累计型任务累计数量值
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<String, String> getOverlayMissionAttr(String playerId) {
		String key = OVERLAY_MISSION_KEY + ":" + playerId;
		StatisManager.getInstance().incRedisKey(OVERLAY_MISSION_KEY);
		return redisSession.hGetAll(key);
	}

	/**
	 * 更新玩家购买钻石礼包的时间
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean updateLatestRechargeTime(String playerId) {
		String key = RECHARGE_LATEST_TIME_KEY + ":" + playerId;
		StatisManager.getInstance().incRedisKey(RECHARGE_LATEST_TIME_KEY);
		return redisSession.setString(key, String.valueOf(HawkTime.getMillisecond()), GsConfig.getInstance().getPlayerRedisExpire());
	}

	/**
	 * 获取玩家最后一次购买钻石礼包的时间
	 * 
	 * @param playerId
	 * @return
	 */
	public long getLatestRechargeTime(String playerId) {
		StatisManager.getInstance().incRedisKey(RECHARGE_LATEST_TIME_KEY);
		String key = RECHARGE_LATEST_TIME_KEY + ":" + playerId;
		String lastTime = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(lastTime)) {
			return Long.parseLong(lastTime);
		}

		return 0;
	}

	/**
	 * 添加IDIP封禁信息
	 * 
	 * @param playerId
	 * @param forbidTime
	 * @param forbidReason
	 * @return
	 */
	public void addIDIPBanInfo(String targetId, IDIPBanInfo banInfo, IDIPBanType banType) {
		String key = BAN_INFO_KEY + ":" + banType.name() + ":" + targetId;
		StatisManager.getInstance().incRedisKey(BAN_INFO_KEY);
		redisSession.setString(key, JSONObject.toJSONString(banInfo), banInfo.getBanSecond());
	}

	/**
	 * 删除IDIP封禁信息
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean removeIDIPBanInfo(String targetId, IDIPBanType banType) {
		String key = BAN_INFO_KEY + ":" + banType.name() + ":" + targetId;
		StatisManager.getInstance().incRedisKey(BAN_INFO_KEY);
		return redisSession.delete(key, false);
	}

	/**
	 * 获取IDID封禁信息
	 * 
	 * @param playerId
	 * @return
	 */
	public IDIPBanInfo getIDIPBanInfo(String targetId, IDIPBanType banType) {
		String key = BAN_INFO_KEY + ":" + banType.name() + ":" + targetId;
		String banInfo = redisSession.getString(key);
		StatisManager.getInstance().incRedisKey(BAN_INFO_KEY);
		if (!HawkOSOperator.isEmptyString(banInfo)) {
			return JSONObject.parseObject(banInfo, IDIPBanInfo.class);
		}

		return null;
	}

	/**
	 * 添加排行榜封禁信息
	 * 
	 * @param targetId
	 *            玩家id或联盟id
	 * @param rankType
	 *            榜单类型
	 * @param banInfo
	 *            封禁信息，格式为 封禁开始时间：封禁结束时间：封禁原因：榜单类型
	 * 
	 * @return
	 */
	public void addBanRankInfo(String targetId, String rankType, String banInfo) {
		StatisManager.getInstance().incRedisKey(BAN_RANK_KEY);

		String key = BAN_RANK_KEY + ":" + rankType;
		redisSession.hSet(key, targetId, banInfo);
	}

	/**
	 * 获取指定榜单类型的所有封禁信息
	 * 
	 * @param rankType
	 *            榜单类型
	 * @return
	 */
	public Map<String, String> getBanRankInfoMap(String rankType) {
		StatisManager.getInstance().incRedisKey(BAN_RANK_KEY);

		String key = BAN_RANK_KEY + ":" + rankType;
		return redisSession.hGetAll(key);
	}

	/**
	 * 删除排行榜单封禁信息
	 * 
	 * @param targetId
	 *            玩家id或联盟id
	 * @param rankType
	 *            榜单类型
	 * @return
	 */
	public boolean delBanRankInfo(String targetId, String rankType) {
		StatisManager.getInstance().incRedisKey(BAN_RANK_KEY);

		String key = BAN_RANK_KEY + ":" + rankType;
		return redisSession.hDel(key, targetId) > 0;
	}

	/**
	 * 创建payItemsInfo
	 * @param puid
	 * @param token
	 * @return
	 */
	public boolean updatePayItemInfo(String tradeNo, JSONObject payItemsInfo) {
		StatisManager.getInstance().incRedisKey(RECHARGE_TOKEN);
		String key = RECHARGE_TOKEN + ":" + tradeNo;
		return redisSession.setString(key, payItemsInfo.toJSONString(), GsConst.DAY_SECONDS * 2);
	}
	
	/**
	 * 获取payItemsInfo
	 * @param tradeNo
	 * @return
	 */
	public String getPayItemInfo(String tradeNo) {
		StatisManager.getInstance().incRedisKey(RECHARGE_TOKEN);
		String key = RECHARGE_TOKEN + ":" + tradeNo;
		return redisSession.getString(key);
	}

	/**
	 * 更新玩家手机信息
	 * 
	 * @param playerId
	 * @param phoneInfo
	 * @return
	 */
	public boolean updatePlayerPhoneInfo(String playerId, String phoneInfo) {
		StatisManager.getInstance().incRedisKey(PHONE_INFO_KEY);
		String key = PHONE_INFO_KEY + ":" + playerId;
		return redisSession.setString(key, phoneInfo, GsConfig.getInstance().getPlayerRedisExpire());
	}

	/**
	 * 获取玩家手机信息
	 * 
	 * @param playerId
	 * @param statInfo
	 * @return
	 */
	public String getPlayerPhoneInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(PHONE_INFO_KEY);
		String key = PHONE_INFO_KEY + ":" + playerId;
		return redisSession.getString(key);
	}

	/**
	 * 更新数据
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean updateSettingData(String playerId, int settingId, int value) {
		StatisManager.getInstance().incRedisKey(SETTING_DATA);
		String key = SETTING_DATA + ":" + playerId;
		return redisSession.hSet(key, String.valueOf(settingId), String.valueOf(value), GsConfig.getInstance().getPlayerRedisExpire()) >= 0;
	}

	/**
	 * 获取设置数据
	 * 
	 * @param playerId
	 * @param settingId
	 * @return
	 */
	public String getSettingData(String playerId, int settingId) {
		StatisManager.getInstance().incRedisKey(SETTING_DATA);
		String key = SETTING_DATA + ":" + playerId;
		return redisSession.hGet(key, String.valueOf(settingId));
	}

	/**
	 * 获取设置数据
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<String, String> getAllSettingData(String playerId) {
		StatisManager.getInstance().incRedisKey(SETTING_DATA);
		String key = SETTING_DATA + ":" + playerId;
		return redisSession.hGetAll(key);
	}

	/**
	 * 更新移除建筑损失的经验值
	 * 
	 * @param playerId
	 * @param type
	 * @return
	 */
	public boolean updateBuildingRemoveExp(String playerId, String type, int exp) {
		StatisManager.getInstance().incRedisKey(BUILDING_REMOVE_EXP);
		String key = BUILDING_REMOVE_EXP + ":" + playerId;
		if (exp <= 0) {
			return redisSession.hDel(key, type) > 0;
		}

		return redisSession.hSet(key, type, String.valueOf(exp), GsConfig.getInstance().getPlayerRedisExpire()) >= 0;
	}

	/**
	 * 获取所有的建筑拆除损失经验
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<String, String> getAllBuildingRemoveExp(String playerId) {
		StatisManager.getInstance().incRedisKey(BUILDING_REMOVE_EXP);
		String key = BUILDING_REMOVE_EXP + ":" + playerId;
		return redisSession.hGetAll(key);
	}

	/**
	 * 保存未领取的vip福利礼包数据
	 * 
	 * @param playerId
	 * @param vipLevel
	 */
	public void pushUnreceivedBenefitBox(String playerId, String... vipLevel) {
		StatisManager.getInstance().incRedisKey(UNRECEIVED_BENEFIT_BOX);
		String key = UNRECEIVED_BENEFIT_BOX + ":" + playerId;
		redisSession.rPush(key, 0, vipLevel);
	}

	/**
	 * 移除已领取的vip福利礼包
	 * 
	 * @param playerId
	 * @param vipLevel
	 */
	public void removeUnreceivedBenefitBox(String playerId, String vipLevel) {
		StatisManager.getInstance().incRedisKey(UNRECEIVED_BENEFIT_BOX);
		String key = UNRECEIVED_BENEFIT_BOX + ":" + playerId;
		redisSession.lRem(key, 0, vipLevel);
	}

	/**
	 * 获取未领取的vip福利礼包
	 * 
	 * @param playerId
	 * @return
	 */
	public List<String> getUnreceivedBenefitBox(String playerId) {
		StatisManager.getInstance().incRedisKey(UNRECEIVED_BENEFIT_BOX);
		String key = UNRECEIVED_BENEFIT_BOX + ":" + playerId;
		return redisSession.lRange(key, 0, -1, 0);
	}

	/**
	 * 更新vip礼包信息
	 * 
	 * @param playerId
	 * @param vipLevel
	 *            vip等级, 传0时表示vip福利礼包的领取状态，>0表示vip专属礼包的购买状态
	 * @param status
	 *            true表示已领取或已购买
	 * @return
	 */
	public boolean updateVipBoxStatus(String playerId, int vipLevel, boolean status) {
		StatisManager.getInstance().incRedisKey(VIP_BOX_STATUS);
		String key = VIP_BOX_STATUS + ":" + playerId;
		return redisSession.hSet(key, String.valueOf(vipLevel), String.valueOf(status)) >= 0;
	}

	/**
	 * 获取vip礼包信息
	 * 
	 * @param playerId
	 * @param vipLevel
	 *            vip等级, 传0时表示获取vip福利礼包的领取状态，>0表示vip专属礼包的购买状态
	 * @return
	 */
	public boolean getVipBoxStatus(String playerId, int vipLevel) {
		StatisManager.getInstance().incRedisKey(VIP_BOX_STATUS);
		String key = VIP_BOX_STATUS + ":" + playerId;
		String value = redisSession.hGet(key, String.valueOf(vipLevel));
		if (!HawkOSOperator.isEmptyString(value)) {
			return Boolean.valueOf(value);
		}

		return false;
	}

	/**
	 * 获取一个玩家所有的vip礼包信息
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<Integer, Boolean> getAllVipBoxStatus(String playerId) {
		StatisManager.getInstance().incRedisKey(VIP_BOX_STATUS);
		Map<Integer, Boolean> map = new HashMap<>();
		String key = VIP_BOX_STATUS + ":" + playerId;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		for (Entry<String, String> entry : resultMap.entrySet()) {
			map.put(Integer.valueOf(entry.getKey()), Boolean.valueOf(entry.getValue()));
		}

		return map;
	}

	/**
	 * 更新vip商城物品已购买次数
	 * 
	 * @param playerId
	 * @param vipShopId
	 * @param times
	 * @return
	 */
	public boolean updateVipShopBuyTimes(String playerId, int vipShopId, int times) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_BOUGHT_TIMES);
		String key = VIP_SHOP_BOUGHT_TIMES + ":" + playerId;
		return redisSession.hSet(key, String.valueOf(vipShopId), String.valueOf(times), GsConfig.getInstance().getPlayerRedisExpire()) >= 0;
	}

	/**
	 * 获取vip商城物品已购买次数
	 * 
	 * @param playerId
	 * @param vipShopId
	 * @return
	 */
	public int getVipShopBuyTimes(String playerId, int vipShopId) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_BOUGHT_TIMES);
		String key = VIP_SHOP_BOUGHT_TIMES + ":" + playerId;
		String value = redisSession.hGet(key, String.valueOf(vipShopId));
		if (!HawkOSOperator.isEmptyString(value)) {
			return Integer.valueOf(value);
		}

		return 0;
	}

	/**
	 * 获取vip商城物品已购买次数
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<Integer, Integer> getPlayerVipShopAllBuyTimes(String playerId) {
		Map<Integer, Integer> map = new HashMap<>();
		StatisManager.getInstance().incRedisKey(VIP_SHOP_BOUGHT_TIMES);
		String key = VIP_SHOP_BOUGHT_TIMES + ":" + playerId;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		for (Entry<String, String> entry : resultMap.entrySet()) {
			map.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
		}

		return map;
	}

	/**
	 * vip商城物品已购买次数清零
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean clearVipShopBuyTimes(String playerId) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_BOUGHT_TIMES);
		String key = VIP_SHOP_BOUGHT_TIMES + ":" + playerId;
		return redisSession.delete(key, false);
	}

	/**
	 * vip商城物品刷新时间更新
	 * 
	 * @param playerId
	 * @param vip商城刷新时间
	 */
	public boolean vipGiftRefresh(String playerId, VipRelatedDateType dateType, long vipShopRefreshTime) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_REFRESH_TIME);
		String key = VIP_SHOP_REFRESH_TIME + ":" + dateType.name() + ":" + playerId;
		
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			pip.persist(key);
			pip.set(key, String.valueOf(vipShopRefreshTime));
			pip.sync();
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return false;
	}

	/**
	 * 获取vip商城物品刷新时间
	 * 
	 * @param playerId
	 * @return
	 */
	public long getVipGiftRefreshDate(String playerId, VipRelatedDateType dateType) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_REFRESH_TIME);
		String key = VIP_SHOP_REFRESH_TIME + ":" + dateType.name() + ":" + playerId;
		String result = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(result)) {
			return Long.valueOf(result);
		}

		return 0;
	}

	/**
	 * vip商城物品刷新
	 * 
	 * @param playerId
	 */
	public void updateVipShopIds(String playerId, String shopIds) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_IDS);
		String key = VIP_SHOP_IDS + ":" + playerId;
		redisSession.setString(key, shopIds, GsConfig.getInstance().getPlayerRedisExpire());
	}

	/**
	 * 获取vip商城物品刷新时间
	 * 
	 * @param playerId
	 * @return
	 */
	public String getVipShopIds(String playerId) {
		StatisManager.getInstance().incRedisKey(VIP_SHOP_IDS);
		String key = VIP_SHOP_IDS + ":" + playerId;
		return redisSession.getString(key);
	}

	/**
	 * 更新军演商店刷新时间
	 * 
	 * @param playerId
	 * @param time
	 */
	public void updateMilitaryShopRefreshTime(String playerId, long time) {
		StatisManager.getInstance().incRedisKey(MILITARY_SHOP_REFRESH);
		String key = MILITARY_SHOP_REFRESH + ":" + playerId;
		redisSession.setString(key, String.valueOf(time), GsConfig.getInstance().getPlayerRedisExpire());
	}

	/**
	 * 获取军演商店刷新时间
	 * 
	 * @param playerId
	 * @return
	 */
	public long getMilitaryShopRefreshTime(String playerId) {
		StatisManager.getInstance().incRedisKey(MILITARY_SHOP_REFRESH);
		String key = MILITARY_SHOP_REFRESH + ":" + playerId;
		String value = redisSession.getString(key, GsConfig.getInstance().getPlayerRedisExpire());
		if (HawkOSOperator.isEmptyString(value)) {
			return 0;
		}

		return Long.parseLong(value);
	}

	/**
	 * 更新军演商店物品购买次数
	 * 
	 * @param playerId
	 * @param shopId
	 * @param count
	 */
	public void incrMilitaryShopItemBuyCount(String playerId, int shopId, int count) {
		StatisManager.getInstance().incRedisKey(MILITARY_SHOP_BUY);
		String key = MILITARY_SHOP_BUY + ":" + playerId;
		redisSession.hIncrBy(key, String.valueOf(shopId), count, GsConfig.getInstance().getPlayerRedisExpire());
	}

	/**
	 * 清除军演商店商品购买次数
	 * 
	 * @param playerId
	 */
	public void clearMilitaryShopItemBuyCount(String playerId) {
		StatisManager.getInstance().incRedisKey(MILITARY_SHOP_BUY);
		String key = MILITARY_SHOP_BUY + ":" + playerId;
		redisSession.del(key);
	}

	/**
	 * 获取军演商店商品购买次数
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<Integer, Integer> getMilitaryShopItemBuyCount(String playerId) {
		StatisManager.getInstance().incRedisKey(MILITARY_SHOP_BUY);
		String key = MILITARY_SHOP_BUY + ":" + playerId;
		Map<String, String> values = redisSession.hGetAll(key, GsConfig.getInstance().getPlayerRedisExpire());
		Map<Integer, Integer> result = new HashMap<Integer, Integer>(values.size());
		for (Entry<String, String> entry : values.entrySet()) {
			result.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
		}

		return result;
	}
	
	public int getMilitaryShopItemBuyCount(String playerId, int shopId) {
		StatisManager.getInstance().incRedisKey(MILITARY_SHOP_BUY);
		String key = MILITARY_SHOP_BUY + ":" + playerId;
		String result = redisSession.hGet(key, shopId + "");
		return NumberUtils.toInt(result);
	}
	
		
	/**今日兑换总数*/
	public Map<Integer, Integer> getSupersoldierSkillExpExchangeCount(String playerId) {
		StatisManager.getInstance().incRedisKey(MILITARY_SHOP_BUY);
		int day = HawkTime.getYearDay();
		String key = SUPERSOLDIER_EXP_ITEN_EXCHANGE + ":" + playerId + ":" + day;
		Map<String, String> values = redisSession.hGetAll(key, (int)TimeUnit.DAYS.toSeconds(1));
		Map<Integer, Integer> result = new HashMap<Integer, Integer>(values.size());
		for (Entry<String, String> entry : values.entrySet()) {
			result.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
		}

		return result;
	}

	/**境加兑换数*/
	public void incrSupersoldierSkillExpExchangeCount(String playerId, int shopId, int count) {
		StatisManager.getInstance().incRedisKey(MILITARY_SHOP_BUY);
		int day = HawkTime.getYearDay();
		String key = SUPERSOLDIER_EXP_ITEN_EXCHANGE + ":" + playerId + ":" + day;
		redisSession.hIncrBy(key, String.valueOf(shopId), count, (int)TimeUnit.DAYS.toSeconds(1));
	}

	/** 今日兑换数 */
	public int getSupersoldierSkillExpExchangeCount(String playerId, int itemId) {
		StatisManager.getInstance().incRedisKey(MILITARY_SHOP_BUY);
		int day = HawkTime.getYearDay();
		String key = SUPERSOLDIER_EXP_ITEN_EXCHANGE + ":" + playerId + ":" + day;
		String result = redisSession.hGet(key, itemId + "");
		return NumberUtils.toInt(result);
	}


	/**
	 * 更新玩家数据到缓存
	 * 
	 * @param playerId
	 * @param dataJson
	 * @return
	 */
	public boolean updatePlayerData(String playerId, JSONObject dataJson) {
		if (HawkOSOperator.isEmptyString(playerId) || dataJson == null) {
			return false;
		}

		StatisManager.getInstance().incRedisKey(PLAYER_DATA_KEY);
		String key = PLAYER_DATA_KEY + ":" + playerId;
		String value = dataJson.toJSONString();
		int timeoutMS = GsConfig.getInstance().getMigrateExpireTime();
		if (GsConfig.getInstance().isGzPlayerData()) {
			byte[] dateBytes = HawkZlib.zlibDeflate(value.getBytes());
			if (!redisSession.setBytes(key, dateBytes, timeoutMS / 1000)) {
				HawkLog.logPrintln("player data cache success, playerId: {}, size: {}", playerId, value.length());
				return false;
			}
		} else {
			if (!redisSession.setString(key, value, timeoutMS / 1000)) {
				HawkLog.logPrintln("player data cache success, playerId: {}, size: {}", playerId, value.length());
				return false;
			}
		}

		return true;
	}

	/**
	 * 从缓存中获取玩家数据
	 * 
	 * @param playerId
	 * @return
	 */
	public JSONObject getPlayerData(String playerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_DATA_KEY);
		String key = PLAYER_DATA_KEY + ":" + playerId;
		byte[] dataBytes = redisSession.getBytes(key.getBytes());
		try {
			if (dataBytes[0] == '{') {
				return JSON.parseObject(new String(dataBytes));
			} else {
				byte[] jsonData = HawkZlib.zlibInflate(dataBytes);
				return JSON.parseObject(new String(jsonData));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("get player data failed, playerId: {}", playerId);
		}

		return null;
	}

	/**
	 * 从缓存中删除玩家数据
	 * 
	 * @param playerId
	 */
	public void deletePlayerData(String playerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_DATA_KEY);
		String key = PLAYER_DATA_KEY + ":" + playerId;
		redisSession.delete(key, false);
	}

	/**
	 * 添加跨服迁城记录
	 * 
	 * @param playerId
	 */
	public boolean addImmigrateRecord(String playerId, String toServerId, JSONObject data) {
		try {
			StatisManager.getInstance().incRedisKey(IMMIGRATE_RECORD_KEY);
			String key = IMMIGRATE_RECORD_KEY + ":" + playerId;
			JSONObject json = new JSONObject();
			json.put("fromServer", GsConfig.getInstance().getServerId());
			json.put("toServer", toServerId);
			json.put("ts", HawkTime.formatNowTime());
			if (data != null) {
				String value = data.toJSONString();
				if (GsConfig.getInstance().isGzPlayerData()) {
					byte[] dateBytes = HawkZlib.zlibDeflate(value.getBytes());
					json.put("data", dateBytes);
				} else {
					json.put("data", value);
				}
			}

			redisSession.lPush(key, 0, json.toJSONString());
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return false;
	}

	/**
	 * 获取玩家迁城记录
	 * 
	 * @param playerId
	 * @return
	 */
	public JSONArray getImmigrateRecord(String playerId) {
		JSONArray recordArray = new JSONArray();
		try {
			StatisManager.getInstance().incRedisKey(IMMIGRATE_RECORD_KEY);
			String key = IMMIGRATE_RECORD_KEY + ":" + playerId;
			List<String> recordList = redisSession.lRange(key, 0, -1, 0);
			if (recordList == null || recordList.size() <= 0) {
				return recordArray;
			}

			for (String record : recordList) {
				JSONObject recordJson = JSON.parseObject(record);
				// 转换玩家数据
				String value = recordJson.getString("data");
				if (!HawkOSOperator.isEmptyString(value)) {
					if (value.startsWith("{")) {
						JSONObject data = JSON.parseObject(value);
						recordJson.put("data", data);
					} else {
						byte[] jsonData = HawkZlib.zlibInflate(recordJson.getBytes("data"));
						JSONObject data = JSON.parseObject(new String(jsonData));
						recordJson.put("data", data);
					}
				}
				recordArray.add(recordJson);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return recordArray;
	}

	/**
	 * 客户端推送数据储存
	 * 
	 * @param serverId
	 * @param statusCfg
	 */
	public void savePushData(String pushKey, int pushVal) {
		String date = HawkTime.formatTime(HawkTime.getMillisecond(), HawkTime.FORMAT_YMD);
		StatisManager.getInstance().incRedisKey(CLIENT_PUSH_DATA);
		redisSession.hIncrBy(CLIENT_PUSH_DATA + ":" + date, pushKey, pushVal);
	}

	/**
	 * 获取客户端推送数据
	 * 
	 * @param date
	 * @return
	 */
	public String getPushData(String date) {
		try {
			StatisManager.getInstance().incRedisKey(CLIENT_PUSH_DATA);
			Map<String, String> pushData = redisSession.hGetAll(CLIENT_PUSH_DATA + ":" + date);
			return pushData.toString();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return null;

	}

	/**
	 * 获取使用过的cdk信息
	 * 
	 */
	public boolean checkCdkTypeUsed(String playerId, String type) {
		StatisManager.getInstance().incRedisKey(CDKEY_USED);
		String key = String.format("%s:%s", CDKEY_USED, playerId);
		return redisSession.hExists(key, type, 0);
	}

	/**
	 * 更新使用过的cdk信息
	 * 
	 */
	public boolean updateCdkUsed(String playerId, String cdkType, int count) {
		StatisManager.getInstance().incRedisKey(CDKEY_USED);
		String key = String.format("%s:%s", CDKEY_USED, playerId);
		return redisSession.hSet(key, cdkType, String.valueOf(count)) >= 0;
	}

	/**
	 * 更新调查问卷回调计数
	 * 
	 * @param surveyId
	 * @return
	 */
	public boolean addSurveyNotifyCnt(int surveyId) {
		String field = String.valueOf(surveyId);
		String val = redisSession.hGet(SURVEY_COUNT, field);
		int cnt = HawkOSOperator.isEmptyString(val) ? 0 : Integer.valueOf(val);
		StatisManager.getInstance().incRedisKey(SURVEY_COUNT);
		return redisSession.hSet(SURVEY_COUNT, field, String.valueOf(cnt++)) >= 0;
	}

	/**
	 * 获取指定问卷回调次数
	 * 
	 * @param surveyId
	 * @return
	 */
	public int getSurveyNotifyCnt(int surveyId) {
		String field = String.valueOf(surveyId);
		StatisManager.getInstance().incRedisKey(SURVEY_COUNT);
		String val = redisSession.hGet(SURVEY_COUNT, field);
		return HawkOSOperator.isEmptyString(val) ? 0 : Integer.valueOf(val);
	}

	/**
	 * 获取服务器对应主播服信息
	 * 
	 * @param serverId
	 * @return
	 */
	public String getAnchorServerInfo(String serverId) {
		StatisManager.getInstance().incRedisKey(ANCHOR_SERVER_MAP);
		return redisSession.hGet(ANCHOR_SERVER_MAP, serverId);
	}

	/**
	 * 存入玩家在主播服务器的token信息
	 */
	public boolean setPlayerAnchorToken(String token, String jsonStr) {
		StatisManager.getInstance().incRedisKey(ANCHOR_SERVER_PREFIX);
		return redisSession.setEx(ANCHOR_SERVER_PREFIX + ":" + token, 60, jsonStr);
	}

	/**
	 * 获取服务器对应主播预告信息
	 * 
	 * @param serverId
	 * @return
	 */
	public String getAnchorPreview(String serverId) {
		StatisManager.getInstance().incRedisKey(ANCHOR_SERVER_PREVIEW);
		return redisSession.hGet(ANCHOR_SERVER_PREVIEW, serverId);
	}

	/**
	 * 添加主播礼物信息
	 * 
	 * @return
	 */
	public long addAnchorGiftInfo(String anchorId, String info) {
		StatisManager.getInstance().incRedisKey(ANCHOR_SERVER_GIFT_INFO);
		return redisSession.rPush(ANCHOR_SERVER_GIFT_INFO + ":" + anchorId, 0, info);
	}

	/**
	 * 检查主播信息存在
	 * 
	 * @param anchorId
	 * @param anchorInfo
	 * @return
	 */
	public boolean checkAnchorInfo(String anchorId) {
		StatisManager.getInstance().incRedisKey(ANCHOR_SERVER_ANCHOR_INFO);
		return redisSession.hExists(ANCHOR_SERVER_ANCHOR_INFO, anchorId, 0);
	}

	/**
	 * 添加玩家主播关注
	 * 
	 * @param playerId
	 * @param anchorId
	 * @return
	 */
	public void addUserAnchorFollow(String playerId, String anchorId) {
		StatisManager.getInstance().incRedisKey(ANCHOR_FOLLOW_INFO);
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
				Pipeline pip = jedis.pipelined()) {
			String keyS = ANCHOR_FOLLOW_INFO + ":" + anchorId;
			String keyZ = ANCHOR_FOLLOW_INFO + ":active:" + anchorId;
			pip.sadd(keyS, playerId);
			pip.zadd(keyZ, HawkTime.getMillisecond(), playerId);
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 取消玩家主播关注
	 * 
	 * @param playerId
	 * @param anchorId
	 * @return
	 */
	public void delUserAnchorFollow(String playerId, String anchorId) {
		StatisManager.getInstance().incRedisKey(ANCHOR_FOLLOW_INFO);
		String key = ANCHOR_FOLLOW_INFO + ":" + anchorId;
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
				Pipeline pip = jedis.pipelined()) {
			String keyS = ANCHOR_FOLLOW_INFO + ":" + anchorId;
			String keyZ = ANCHOR_FOLLOW_INFO + ":active:" + anchorId;
			pip.srem(keyS, playerId);
			pip.zrem(keyZ, playerId);
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		redisSession.sRem(key, playerId);
	}

	/**
	 * 检查主播是否被关注
	 * 
	 * @param glamour
	 * @return
	 */
	public boolean checkAnchorFollow(String playerId, String anchorId) {
		StatisManager.getInstance().incRedisKey(ANCHOR_FOLLOW_INFO);
		String keyS = ANCHOR_FOLLOW_INFO + ":" + anchorId;
		boolean isMember = redisSession.sIsmember(keyS, playerId);
		if (isMember) { // 刷新活跃
			try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis();
					Pipeline pip = jedis.pipelined()) {
				String keyZ = ANCHOR_FOLLOW_INFO + ":active:" + anchorId;
				pip.zadd(keyZ, HawkTime.getMillisecond(), playerId);
				pip.sync();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return isMember;
	}

	/**
	 * 获取所有关注用户ID
	 * 
	 * @param anchorId
	 * @return
	 */
	public Set<String> getAllFollowsPlayer(String anchorId) {
		StatisManager.getInstance().incRedisKey(ANCHOR_FOLLOW_INFO);
		String keyZ = ANCHOR_FOLLOW_INFO + ":active:" + anchorId;
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis()) {
			Set<String> result = jedis.zrevrange(keyZ, 0, 888);
			return result;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return Collections.emptySet();
	}

	/**
	 * 获取主播房间信息
	 * 
	 * @param serverId
	 * @param roomId
	 * @param val
	 * @return
	 */
	public String getAnchorRoomInfo(String serverId, String roomId) {
		StatisManager.getInstance().incRedisKey(ANCHOR_ROOM_INFO);
		return redisSession.hGet(ANCHOR_ROOM_INFO + ":" + serverId, roomId);
	}

	/**
	 * 获取主播信息
	 * 
	 * @param anchorId
	 * @return
	 */
	public JSONObject getAnchorInfo(String anchorId) {
		StatisManager.getInstance().incRedisKey(ANCHOR_SERVER_ANCHOR_INFO);
		String anchorInfo = redisSession.hGet(ANCHOR_SERVER_ANCHOR_INFO, anchorId);
		if (Objects.isNull(anchorInfo)) {
			return null;
		}

		JSONObject anchorJson = JSONObject.parseObject(anchorInfo);
		return anchorJson;
	}

	/**
	 * 更新账号在线状态
	 * 
	 * @param openid
	 * @param platform
	 * @return
	 */
	public boolean updateOnlineInfo(String openid, String platform) {
		StatisManager.getInstance().incRedisKey(ACCOUNT_ONLINE_KEY);
		String key = ACCOUNT_ONLINE_KEY + ":" + openid;
		String value = GsConfig.getInstance().getServerId() + ":" + platform;
		return redisSession.setString(key, value, GameConstCfg.getInstance().getOnlineFlagExpire());
	}

	/**
	 * 清除账号在线状态
	 * 
	 * @param openid
	 */
	public void removeOnlineInfo(String openid) {
		StatisManager.getInstance().incRedisKey(ACCOUNT_ONLINE_KEY);
		String key = ACCOUNT_ONLINE_KEY + ":" + openid;
		redisSession.del(key);
	}

	/**
	 * 获取账号在线状态信息
	 * 
	 * @param openid
	 * @return
	 */
	public String getOnlineInfo(String openid) {
		StatisManager.getInstance().incRedisKey(ACCOUNT_ONLINE_KEY);
		String key = ACCOUNT_ONLINE_KEY + ":" + openid;
		return redisSession.getString(key);
	}

	/**
	 * 获取最近登陆服务器
	 * 
	 * @param openid
	 * @return
	 */
	public Map<String, String> getRecentServer(String openid) {
		String key = String.format("%s:%s:%s", GsConfig.getInstance().getAreaId(), RECENT_SERVER_KEY, openid);
		return redisSession.hGetAll(key);
	}

	/**
	 * 删除最近登录服务器
	 * @param serverId
	 * @param openid
	 * @param platform
	 */
	public void deleRecentServer(String serverId, String openid, String platform) {
		String key = String.format("%s:%s:%s", GsConfig.getInstance().getAreaId(), RECENT_SERVER_KEY, openid);
		String innerKey = serverId + ":" + platform;
		redisSession.hDel(key, innerKey);
	}
	
	/**
	 * 获取预设名字
	 * 
	 * @param openid
	 * @return
	 */
	public String getPreinstallName(String openid) {
		StatisManager.getInstance().incRedisKey(PREINSTALL_OPENID_NAME);
		String key = String.format("%s:%s", PREINSTALL_OPENID_NAME, GsConfig.getInstance().getServerId());
		return redisSession.hGet(key, openid);
	}

	/**
	 * 获取预设名字占有者
	 * 
	 * @param name
	 * @return
	 */
	public String getPreinstallNameUser(String name) {
		StatisManager.getInstance().incRedisKey(PREINSTALL_NAME_OPENID);
		String key = PREINSTALL_NAME_OPENID;
		return redisSession.hGet(key, name);
	}

	/**
	 * 删除预设名字
	 * 
	 * @param openid
	 * @param name
	 */
	public void removePreinstallName(String openid, String name) {
		StatisManager.getInstance().incRedisKey(PREINSTALL_OPENID_NAME);
		StatisManager.getInstance().incRedisKey(PREINSTALL_NAME_OPENID);

		String key1 = String.format("%s:%s", PREINSTALL_OPENID_NAME, GsConfig.getInstance().getServerId());
		String key2 = PREINSTALL_NAME_OPENID;
		redisSession.hDel(key1, openid);
		redisSession.hDel(key2, name);
	}

	/**
	 * 更新预设名字
	 * 
	 * @param openid
	 * @param name
	 */
	public void updatePreinstallName(String serverId, String openid, String name) {
		StatisManager.getInstance().incRedisKey(PREINSTALL_OPENID_NAME);
		StatisManager.getInstance().incRedisKey(PREINSTALL_NAME_OPENID);

		String key1 = String.format("%s:%s", PREINSTALL_OPENID_NAME, serverId);
		String key2 = PREINSTALL_NAME_OPENID;
		redisSession.hSet(key1, openid, name);
		redisSession.hSet(key2, name, openid);
	}

	/**
	 * 是否是puid白名单
	 * 
	 * @param openid
	 * @return
	 */
	public boolean checkPuidControl(String openid) {
		StatisManager.getInstance().incRedisKey(PUID_CONTROL);
		String server = redisSession.hGet(PUID_CONTROL, openid);
		String currentServer = GsConfig.getInstance().getServerId();
		// 对全服生效，或对当前服的账号
		if ("0".equals(server) || currentServer.equals(server)) {
			return true;
		}

		return false;
	}

	/**
	 * 判断是否时注册白名单账号
	 * 
	 * @param openid
	 * @return
	 */
	public boolean checkRegisterPuidControl(String openid) {
		String serverId = RedisProxy.getInstance().getRedisSession().hGet(IDIP_PUID_CONTROL, openid);
		// 对全服生效，或对当前服的账号
		if ("0".equals(serverId) || GsConfig.getInstance().getServerId().equals(serverId)) {
			return true;
		}

		return false;
	}

	/**
	 * 添加puid白名单
	 * 
	 * @param openid
	 */
	public void addPuidControl(String openid) {
		addPuidControl(openid, "0");
	}

	/**
	 * 添加puid白名单
	 * 
	 * @param openid
	 * @param serverId
	 *            传0对全服生效，非0针对指定服生效
	 */
	public void addPuidControl(String openid, String serverId) {
		StatisManager.getInstance().incRedisKey(PUID_CONTROL);
		redisSession.hSet(PUID_CONTROL, openid, serverId);
	}

	/**
	 * 添加充值订单信息
	 * 
	 * @param playerId
	 *            玩家id
	 * @param goodsId
	 *            商品id
	 * @param orderId
	 *            订单id
	 * @param expireSeconds
	 */
	public boolean addRechargeInfo(String playerId, String goodsId, String orderId, int expireSeconds) {
		StatisManager.getInstance().incRedisKey(RECHARGE_ORDER_ID);
		String key = String.format("%s:%s", RECHARGE_ORDER_ID, playerId);
		return redisSession.hSet(key, orderId, goodsId, expireSeconds) >= 0;
	}

	/**
	 * 根据订单id获取商品id
	 * 
	 * @param playerId
	 * @param orderId
	 * @return
	 */
	public String getGoodsIdByOrderId(String playerId, String orderId) {
		StatisManager.getInstance().incRedisKey(RECHARGE_ORDER_ID);
		String key = String.format("%s:%s", RECHARGE_ORDER_ID, playerId);
		return redisSession.hGet(key, orderId);
	}

	/**
	 * 删除订单信息
	 * 
	 * @param playerId
	 * @param orderId
	 */
	public void removeRechargeInfo(String playerId, String orderId) {
		StatisManager.getInstance().incRedisKey(RECHARGE_ORDER_ID);
		String key = String.format("%s:%s", RECHARGE_ORDER_ID, playerId);
		redisSession.hDel(key, orderId);
	}

	/**
	 * 获取一个玩家未处理的所有订单信息
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<String, String> getAllRechargeInfo(String playerId) {
		String key = String.format("%s:%s", RECHARGE_ORDER_ID, playerId);
		StatisManager.getInstance().incRedisKey(RECHARGE_ORDER_ID);
		return redisSession.hGetAll(key);
	}

	/**
	 * 清除所有的票据信息
	 * 
	 * @param playerId
	 */
	public void removeAllRechargeInfo(String playerId) {
		String key = String.format("%s:%s", RECHARGE_ORDER_ID, playerId);
		StatisManager.getInstance().incRedisKey(RECHARGE_ORDER_ID);
		redisSession.del(key);
	}

	/**
	 * 记录道具直购中，购买流程未结束的道具ID
	 * 
	 * @param playerId
	 * @param goodsId
	 * @return
	 */
	public boolean addUnfinishedRechargeGoods(String playerId, String goodsId, int expireSeconds) {
		String key = String.format("%s:%s", UNFINISHED_RECHARGE_GOODS, playerId);
		boolean result = redisSession.sAdd(key, expireSeconds, goodsId).longValue() > 0;
		StatisManager.getInstance().incRedisKey(UNFINISHED_RECHARGE_GOODS);
		return result;
	}

	/**
	 * 获取玩家所有购买流程未结束的直购道具ID
	 * 
	 * @param playerId
	 * @return
	 */
	public Set<String> getAllUnfinishedRechargeGoods(String playerId) {
		String key = String.format("%s:%s", UNFINISHED_RECHARGE_GOODS, playerId);
		StatisManager.getInstance().incRedisKey(UNFINISHED_RECHARGE_GOODS);
		return redisSession.sMembers(key);
	}

	/**
	 * 移除购买流程未结束的直购道具ID
	 * 
	 * @param playerId
	 * @param goodsId
	 */
	public void removeUnfinishedRechargeGoods(String playerId, String goodsId) {
		String key = String.format("%s:%s", UNFINISHED_RECHARGE_GOODS, playerId);
		redisSession.sRem(key, goodsId);
		StatisManager.getInstance().incRedisKey(UNFINISHED_RECHARGE_GOODS);
	}

	/**
	 * 玩家登录相关信息批量更新
	 * 
	 * @param player
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String batchUpdateLoginInfo(Player player) {
		String banInfo = null;
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			// 记录在线状态
			String key = ACCOUNT_ONLINE_KEY + ":" + player.getOpenId();
			String value = GsConfig.getInstance().getServerId() + ":" + player.getPlatform();
			pip.set(key, value);
			pip.expire(key, GameConstCfg.getInstance().getOnlineFlagExpire());
			// 记录最近登陆服信息
			String key1 = String.format("%s:%s:%s", GsConfig.getInstance().getAreaId(), RECENT_SERVER_KEY, player.getOpenId());
			String innerKey1 = player.getServerId() + ":" + player.getPlatform();
			pip.hset(key1, innerKey1, String.valueOf(HawkTime.getSeconds()));

			// 查询成长守护封禁信息
			String key2 = BAN_INFO_KEY + ":" + IDIPBanType.CARE_BAN_ACCOUNT.name() + ":" + player.getOpenId();
			pip.get(key2);

			List<Object> results = pip.syncAndReturnAll();
			banInfo = (String) results.get(3); // 下标从0开始
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return banInfo;
	}

	/**
	 * 玩家登出相关信息批量更新
	 * 
	 * @param player
	 * @param time
	 * @param isNewDay
	 */
	public void batchUpdateLogoutInfo(Player player, AccountRoleInfo accountRole, long time, boolean isNewDay) {
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			// 离线时清除在线状态信息
			String key = ACCOUNT_ONLINE_KEY + ":" + player.getOpenId();
			pip.del(key);

			// 日活新增
			if (isNewDay) {
				String key1 = String.format("%s:%s:%s", SERVER_DAILY_KEY, HawkTime.formatTime(time, HawkTime.FORMAT_YMD), GsConfig.getInstance().getServerId());
				pip.hincrBy(key1, DailyInfoField.DAY_LOGIN, 1);
				// 存储全局数据，不分哪个服
				String globalKey = String.format("%s:%s", SERVER_DAILY_KEY, HawkTime.formatTime(time, HawkTime.FORMAT_YMD));
				pip.hincrBy(globalKey, DailyInfoField.DAY_LOGIN, 1);
			}

			// 活跃账号更新账号信息
			if (accountRole != null) {
				String key2 = ACCOUNT_ROLE_KEY + ":" + accountRole.getOpenId();
				String innerKey = accountRole.getServerId() + ":" + accountRole.getPlatform();
				pip.hset(key2, innerKey, accountRole.toString());
			}

			pip.sync();

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 玩家注册相关信息批量添加
	 * 
	 * @param accountRole
	 * @param phoneInfo
	 */
	public void batchAddRegisterInfo(AccountRoleInfo accountRole, String phoneInfo) {
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			// 缓存角色信息
			String key = ACCOUNT_ROLE_KEY + ":" + accountRole.getOpenId();
			String innerKey = accountRole.getServerId() + ":" + accountRole.getPlatform();
			pip.hset(key, innerKey, accountRole.toString());
			String formatTime = HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), HawkTime.FORMAT_YMD);
			// 缓存单服日注册数据
			String key1 = String.format("%s:%s:%s", SERVER_DAILY_KEY, formatTime, GsConfig.getInstance().getServerId());
			pip.hincrBy(key1, DailyInfoField.DAY_REGISTER, 1);
			// 缓存全服日注册数据
			String key2 = String.format("%s:%s", SERVER_DAILY_KEY, formatTime);
			pip.hincrBy(key2, DailyInfoField.DAY_REGISTER, 1);
			// 缓存单服注册总量
			String key3 = String.format("%s:%s", GLOBAL_STAT_KEY, GsConfig.getInstance().getServerId());
			pip.hincrBy(key3, DailyInfoField.DAY_REGISTER, 1);
			// 缓存全服注册总量
			pip.hincrBy(GLOBAL_STAT_KEY, DailyInfoField.DAY_REGISTER, 1);

			// 缓存玩家手机信息
			if (!HawkOSOperator.isEmptyString(phoneInfo)) {
				String key4 = PHONE_INFO_KEY + ":" + accountRole.getPlayerId();
				pip.set(key4, phoneInfo);
				pip.expire(key4, GsConfig.getInstance().getPlayerRedisExpire());
			}

			pip.sync();

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * vip商城刷新信息批量更新
	 * 
	 * @param playerId
	 * @param vipShopRefreshTime
	 * @param vipShopInfo
	 */
	public void batchUpdateVipShopRefreshInfo(String playerId, long vipShopRefreshTime, String vipShopInfo) {
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			// 更新vip商城物品刷新时间
			String key = VIP_SHOP_REFRESH_TIME + ":" + VipRelatedDateType.VIP_SHOP_REFRESH.name() + ":" + playerId;
			pip.persist(key);
			pip.set(key, String.valueOf(vipShopRefreshTime));
			
			String key1 = VIP_SHOP_BOUGHT_TIMES + ":" + playerId;
			pip.del(key1);
			// 更新vip商城物品列表
			String key2 = VIP_SHOP_IDS + ":" + playerId;
			pip.set(key2, vipShopInfo);
			pip.expire(key2, GsConfig.getInstance().getPlayerRedisExpire());

			pip.sync();

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * vip福利礼包相关数据批量更新
	 * 
	 * @param playerId
	 */
	public void batchUpdateVipBenefitRefreshInfo(String playerId) {
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			// 更新vip礼包状态
			String key = VIP_BOX_STATUS + ":" + playerId;
			pip.hset(key, "0", "false");

			// 更新vip福利礼包领取时间
			String key1 = VIP_SHOP_REFRESH_TIME + ":" + VipRelatedDateType.VIP_BENEFIT_TAKEN.name() + ":" + playerId;
			pip.persist(key1);
			pip.set(key1, String.valueOf(GsApp.getInstance().getCurrentTime() - GsConst.DAY_MILLI_SECONDS));

			pip.sync();

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 批量更新任务数据
	 * 
	 * @param playerId
	 * @param missionData
	 */
	public void batchUpdateMissionData(String playerId, Map<String, String> missionData) {
		if (missionData.isEmpty()) {
			return;
		}

		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {

			String key = OVERLAY_MISSION_KEY + ":" + playerId;

			for (Entry<String, String> entry : missionData.entrySet()) {
				pip.hset(key, entry.getKey(), entry.getValue());
			}

			pip.expire(key, GsConfig.getInstance().getPlayerRedisExpire());
			pip.sync();
			StatisManager.getInstance().incRedisKey(OVERLAY_MISSION_KEY);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 批量获取排行榜封禁信息
	 * 
	 * @param playerId
	 * @return
	 */
	public List<Object> fetchBanRankInfoBatch(String playerId) {
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			for (RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
				String key = BAN_RANK_KEY + ":" + rankType.name().toLowerCase();
				pip.hget(key, playerId);
			}

			List<Object> returnObjs = pip.syncAndReturnAll();
			StatisManager.getInstance().incRedisKey(BAN_RANK_KEY);

			return returnObjs;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return Collections.emptyList();
	}

	/**
	 * 批量获取账号角色信息
	 * 
	 * @param recentLoginServers
	 * @return
	 */
	public List<AccountRoleInfo> batchGetAccountRole(Collection<String> openids) {
		if (openids == null || openids.isEmpty()) {
			return Collections.emptyList();
		}

		List<Object> retObjs = null;
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			for (String openid : openids) {
				String key = ACCOUNT_ROLE_KEY + ":" + openid;
				pip.hgetAll(key);
			}

			retObjs = pip.syncAndReturnAll();
		} catch (Exception e) {
			HawkException.catchException(e);
			return Collections.emptyList();
		}

		if (retObjs == null) {
			HawkLog.errPrintln("batchGetAccountRole failed, openid count: {}", openids.size());
			return Collections.emptyList();
		}

		List<AccountRoleInfo> accountRoleList = new ArrayList<AccountRoleInfo>();
		for (Object obj : retObjs) {
			@SuppressWarnings("unchecked")
			Map<String, String> resultMap = (Map<String, String>) obj;
			if (resultMap.isEmpty()) {
				continue;
			}

			long recentLoginTime = 0;
			AccountRoleInfo roleInfo = null;
			for (String value : resultMap.values()) {
				AccountRoleInfo roleInfoObj = JSONObject.parseObject(value, AccountRoleInfo.class);
				if (roleInfoObj.getLoginTime() <= recentLoginTime) {
					continue;
				}

				recentLoginTime = roleInfoObj.getLoginTime();
				roleInfo = roleInfoObj;
			}

			if (roleInfo != null) {
				accountRoleList.add(roleInfo);
			}
		}

		return accountRoleList;
	}

	/**
	 * 添加邀请好友信息
	 * 
	 * @param openid
	 * @param sopenid
	 */
	public void addInviteFriend(String playerId, String serverId, String sopenid) {
		String now = String.valueOf(HawkTime.getSeconds());
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			String inviteKey = INVITE_FRIEND_KEY + ":" + playerId;
			pip.hset(inviteKey, sopenid, now);
			pip.expire(inviteKey, ConstProperty.getInstance().getFriendInviteExpireTime());

			String beInvitedKey = BE_INVITED_KEY + ":" + sopenid;
			pip.hset(beInvitedKey, playerId, now + ":" + serverId);
			pip.expire(beInvitedKey, ConstProperty.getInstance().getFriendInviteExpireTime());

			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取对某个好友的邀请时间
	 * 
	 * @param openid
	 * @param sopenid
	 * @return
	 */
	public int getInviteTime(String playerId, String sopenid) {
		String inviteKey = INVITE_FRIEND_KEY + ":" + playerId;
		String inviteTime = redisSession.hGet(inviteKey, sopenid);
		if (!HawkOSOperator.isEmptyString(inviteTime)) {
			return Integer.parseInt(inviteTime);
		}

		return 0;
	}

	/**
	 * 获取玩家已发出邀请的所有好友的邀请时间
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<String, String> getAllInviteTime(String playerId) {
		String inviteKey = INVITE_FRIEND_KEY + ":" + playerId;
		return redisSession.hGetAll(inviteKey);
	}

	/**
	 * 删除邀请信息
	 * 
	 * @param playerId
	 * @param sopenid
	 */
	public void removeInviteInfo(String playerId, String sopenid, TaskAttrInfo taskAttr) {
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			String inviteKey = INVITE_FRIEND_KEY + ":" + playerId;
			pip.hdel(inviteKey, sopenid);

			String inviteSuccKey = INVITE_SUCC_KEY + ":" + playerId;
			pip.hset(inviteSuccKey, taskAttr.getOpenid(), JSONObject.toJSONString(taskAttr));

			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取邀请过自己的好友信息
	 * 
	 * @param beInvitedPlayerInfo
	 *            被邀请好友第一次进入游戏，传sopenid，后面传playerId
	 * @return
	 */
	public Map<String, String> getBeInviteFriend(String beInvitedPlayerInfo) {
		String beInvitedKey = BE_INVITED_KEY + ":" + beInvitedPlayerInfo;
		return redisSession.hGetAll(beInvitedKey);
	}

	/**
	 * 删除邀请过自己的好友信息
	 * 
	 * @param sopenid
	 */
	public void removeBeInviteFriend(String sopenid) {
		String beInvitedKey = BE_INVITED_KEY + ":" + sopenid;
		redisSession.del(beInvitedKey);
	}

	/**
	 * 刷新邀请人信息
	 * 
	 * @param beInvitedPlayerId
	 * @param invitePlayers
	 */
	public void refreshBeInviteFriends(String beInvitedPlayerId, Map<String, String> invitePlayers) {
		if (invitePlayers.isEmpty()) {
			return;
		}

		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			String beInvitedKey = BE_INVITED_KEY + ":" + beInvitedPlayerId;
			for (Entry<String, String> entry : invitePlayers.entrySet()) {
				pip.hset(beInvitedKey, entry.getKey(), entry.getValue());
			}

			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 更新邀请成功的密友信息
	 * 
	 * @param openid
	 * @param beInvitedOpenid
	 *            邀请成功之后，sopenid换成了openid，所以这里传openid而非sopenid
	 * @param taskAttr
	 */
	public void updateInviteSuccFriendInfo(String playerId, String beInvitedOpenid, TaskAttrInfo taskAttr) {
		String inviteSuccKey = INVITE_SUCC_KEY + ":" + playerId;
		redisSession.hSet(inviteSuccKey, beInvitedOpenid, JSONObject.toJSONString(taskAttr));
	}

	/**
	 * 获取所有邀请成功的密友信息
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<String, String> getAllInviteSuccFriends(String playerId) {
		String inviteSuccKey = INVITE_SUCC_KEY + ":" + playerId;
		return redisSession.hGetAll(inviteSuccKey);
	}

	/**
	 * 添加已领取奖励的密友邀请任务ID
	 * 
	 * @param playerId
	 * @param taskId
	 */
	public void addFriendInviteRewardTask(String playerId, int taskId) {
		String key = INVITE_TASK_KEY + ":" + playerId;
		redisSession.sAdd(key, 0, String.valueOf(taskId));
	}

	/**
	 * 获取所有已领取奖励的密友邀请任务ID
	 * 
	 * @param playerId
	 * @return
	 */
	public Set<String> getFriendInviteRewardTask(String playerId) {
		String key = INVITE_TASK_KEY + ":" + playerId;
		return redisSession.sMembers(key);
	}
	
	
	/**
	 * 添加已完成的任務ID
	 * 
	 * @param playerId
	 * @param taskId
	 */
	public void addFinishedInviteTask(String playerId, int taskId) {
		String key = FINISHED_TASK_KEY + ":" + playerId;
		redisSession.sAdd(key, 0, String.valueOf(taskId));
	}

	/**
	 * 获取所有已完成的任務ID
	 * 
	 * @param openid
	 * @return
	 */
	public Set<String> getFinishedInviteTask(String playerId) {
		String key = FINISHED_TASK_KEY + ":" + playerId;
		return redisSession.sMembers(key);
	}

	/**
	 * 更新服务器控制参数
	 * 
	 * @param serverSetting
	 */
	public void updateServerControlData(ServerSettingData serverSetting) {
		redisSession.hSet(SERVER_CONTROL_KEY, GsConfig.getInstance().getServerId(), JSONObject.toJSONString(serverSetting));
	}

	/**
	 * 获取服务器控制参数
	 */
	public ServerSettingData getServerControlData() {
		String serverSetting = redisSession.hGet(SERVER_CONTROL_KEY, GsConfig.getInstance().getServerId());
		if (!HawkOSOperator.isEmptyString(serverSetting)) {
			try {
				return JSONObject.parseObject(serverSetting, ServerSettingData.class);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		return null;
	}

	/**
	 * 获取个人信用分
	 * 
	 * @param openid
	 * @return
	 */
	public int getCredit(String openid) {
		String key = CREDIT_KEY + ":" + openid;
		String creditScore = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(creditScore)) {
			return Integer.parseInt(creditScore);
		}

		return -1;
	}

	/**
	 * 个人信用分更新
	 * 
	 * @param openid
	 * @param score
	 */
	public void updateCredit(String openid, String score) {
		String key = CREDIT_KEY + ":" + openid;
		redisSession.setString(key, score, 86400);
	}

	/**
	 * 手Q vip信息
	 * 
	 * @param openid
	 * @return
	 */
	public String getQQVip(String openid) {
		String key = QQ_VIP_KEY + ":" + openid;
		String vipInfo = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(vipInfo)) {
			return vipInfo;
		}

		return null;
	}

	/**
	 * 手Q vip信息更新
	 * 
	 * @param openid
	 * @param qqVip
	 */
	public void updateQQVip(String openid, int qqVip) {
		String key = QQ_VIP_KEY + ":" + openid;
		redisSession.setString(key, String.valueOf(qqVip), GsConfig.getInstance().getPlayerRedisExpire());
	}

	/**
	 * 获取玩家名字槽key
	 * 
	 * @param name
	 * @return
	 */
	private String getPlayerNameSlotKey(String name) {
		int slot = Math.abs(name.hashCode() % GsConst.NAME_SLOT_SIZE);
		return PLAYER_NAME_SLOT + ":" + slot;
	}

	/**
	 * 更新玩家名字
	 * 
	 * @param name
	 * @param playerId
	 */
	public boolean updatePlayerName(String name, String playerId) {
		String key = getPlayerNameSlotKey(name);
		return redisSession.hSetNx(key, name, playerId) > 0;
	}

	/**
	 * 删除玩家名字
	 * 
	 * @param name
	 * @return
	 */
	public void deletePlayerName(String name) {
		String key = getPlayerNameSlotKey(name);
		redisSession.hDel(key, name);
	}

	/**
	 * 根据玩家名字获取playerId
	 * 
	 * @param playerName
	 * @return
	 */
	public String getPlayerIdByName(String name) {
		String key = getPlayerNameSlotKey(name);
		return redisSession.hGet(key, name);
	}

	/**
	 * 增加当日加好友数
	 */
	public void dayAddFriendInc(String playerId, int count) {
		StatisManager.getInstance().incRedisKey(DAY_ADD_FRIEND_COUNT);

		final String key = DAY_ADD_FRIEND_COUNT + ":" + playerId + ":" + HawkTime.getYearDay();
		redisSession.increaseBy(key, count, (int) TimeUnit.DAYS.toSeconds(1));
	}

	/**
	 * 当日加好友数
	 */
	public int dayFriendAddCount(String playerId) {
		StatisManager.getInstance().incRedisKey(DAY_ADD_FRIEND_COUNT);
		final String key = DAY_ADD_FRIEND_COUNT + ":" + playerId + ":" + HawkTime.getYearDay();
		String cc = redisSession.getString(key);
		return NumberUtils.toInt(cc);

	}

	/**
	 * 获得联盟商店全部商品信息
	 * 
	 * @param guildId
	 * @return
	 */
	public Map<String, String> getGuildShopInfo(String guildId) {
		StatisManager.getInstance().incRedisKey(GUILD_SHOP_INFO_KEY);

		String key = GUILD_SHOP_INFO_KEY + ":" + guildId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		return redisSession.hGetAll(key, expireSecond);
	}

	/**
	 * 获取联盟商店指定商品数量
	 * 
	 * @param guildId
	 * @param itemId
	 * @return
	 */
	public int getGuildShopItemCount(String guildId, int itemId) {
		StatisManager.getInstance().incRedisKey(GUILD_SHOP_INFO_KEY);

		String key = GUILD_SHOP_INFO_KEY + ":" + guildId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		String value = redisSession.hGet(key, String.valueOf(itemId), expireSecond);
		if (!HawkOSOperator.isEmptyString(value)) {
			return Integer.parseInt(value);
		}

		return 0;
	}

	/**
	 * 刷新联盟商店指定商品信息
	 * 
	 * @param guildId
	 * @param itemId
	 * @param count
	 */
	public void updateGuildShopInfo(String guildId, int itemId, int count) {
		StatisManager.getInstance().incRedisKey(GUILD_SHOP_INFO_KEY);

		String key = GUILD_SHOP_INFO_KEY + ":" + guildId;
		int expireSecond = GameConstCfg.getInstance().getMailExpireSecond();
		redisSession.hSet(key, String.valueOf(itemId), String.valueOf(count), expireSecond);
	}

	/**
	 * 联盟解散删除该联盟所有商品信息及购买/补货记录
	 * 
	 * @param guildId
	 */
	public void removeGuildShopInfo(String guildId) {
		StatisManager.getInstance().incRedisKey(GUILD_SHOP_INFO_KEY);

		String key = GUILD_SHOP_INFO_KEY + ":" + guildId;
		redisSession.delete(key, false);
	}

	/**
	 * 添加全服同时在线人数
	 */
	public void addOnlineUserCount(int count) {
		StatisManager.getInstance().incRedisKey(ONLINE_USER_COUNT);
		String key = ONLINE_USER_COUNT + ":" + HawkTime.formatNowTime("yyyy-MM-dd");
		String minTs = HawkTime.formatNowTime("yyyy-MM-dd HH:mm");
		redisSession.hIncrBy(key, minTs, count);
	}
	
	/**
	 * 角色充值累计额度累加
	 * @param openid
	 * @param playerId
	 * @param amountAdd
	 */
	public void roleRechargeTotalAdd(String openid, String playerId, int amountAdd) {
		if (amountAdd <= 0) {
			return;
		}
		StatisManager.getInstance().incRedisKey(RedisKey.ROLE_RECHARGE_TOTAL);
		String key = RedisKey.ROLE_RECHARGE_TOTAL + ":" + openid;
		redisSession.hIncrBy(key, playerId, amountAdd);
	}
	
	/**
	 * 获取一个账号下各个角色的累计充值额度
	 * @param openid
	 * @return
	 */
	public Map<String, Integer> getAllRoleRechargeTotal(String openid) {
		StatisManager.getInstance().incRedisKey(RedisKey.ROLE_RECHARGE_TOTAL);
		String key = RedisKey.ROLE_RECHARGE_TOTAL + ":" + openid;
		Map<String, String> map = redisSession.hGetAll(key);
		Map<String, Integer> result = new HashMap<>();
		for (Entry<String, String> entry : map.entrySet()) {
			result.put(entry.getKey(), Integer.parseInt(entry.getValue()));
		}
		return result;
	}
	
	/**
	 * 添加充值信息
	 * 
	 * @param rechargeInfo
	 */
	public void addRechargeInfo(RechargeInfo rechargeInfo) {
		StatisManager.getInstance().incRedisKey(RedisKey.RECHARGE_INFO_2024);
		String key = RedisKey.RECHARGE_INFO_2024 + ":" + rechargeInfo.getOpenid();
		redisSession.lPush(key, 0, JSONObject.toJSONString(rechargeInfo));
	}
	
	/**
	 * 互通区上的手Q玩家，将其充值记录转储到另一个key中去
	 * @param player
	 */
	@Deprecated
	public void rechargeInfoMoveRestore(Player player) {
		String key = RedisKey.RECHARGE_INFO + ":" + player.getOpenId();
		List<String> infos = redisSession.lRange(key, 0, -1, 0);
		if (infos == null || infos.isEmpty()) {
			return;
		}
		
		//将互通区上qq账号的充值记录数据，从 RECHARGE_INFO 中转移到 RECHARGE_INFO_QQ 中去
		key = RedisKey.RECHARGE_INFO_QQ + ":" + player.getOpenId();
		int index = 0;
		while (index < infos.size()) {
			int endIndex = Math.min(infos.size(), index + 300);
			List<String> array = infos.subList(index, endIndex);
			redisSession.lPush(key, 0, array.toArray(new String[array.size()]));
			index = endIndex;
		}
		
		key = RedisKey.RECHARGE_INFO + ":" + player.getOpenId();
		redisSession.del(key);
	}
	
	/**
	 * 将互通区上qq账号的充值记录数据，从 RECHARGE_INFO_QQ 中转移到 RECHARGE_INFO_2024 中去（大区合并时忘了将相关代码去掉，导致后面qq号登录新区充值还是记录到 RECHARGE_INFO_QQ 中去了）
	 * @param player
	 */
	public void qqRechargeInfoRestore(Player player) {
		String key = RedisKey.RECHARGE_INFO_QQ + ":" + player.getOpenId();
		List<String> infos = redisSession.lRange(key, 0, -1, 0);
		if (infos == null || infos.isEmpty()) {
			return;
		}
		
		key = RedisKey.RECHARGE_INFO_2024 + ":" + player.getOpenId();
		int index = 0;
		while (index < infos.size()) {
			int endIndex = Math.min(infos.size(), index + 300);
			List<String> array = infos.subList(index, endIndex);
			redisSession.lPush(key, 0, array.toArray(new String[array.size()]));
			index = endIndex;
		}
		
		key = RedisKey.RECHARGE_INFO_QQ + ":" + player.getOpenId();
		redisSession.del(key);
		HawkLog.logPrintln("RedisProxy qqRechargeInfoRestore success, playerId: {}, size: {}", player.getId(), infos.size());
	}
	
	/**
	 * 获取一个账号的所有充值信息
	 * 
	 * @param openid
	 * @return
	 */
	public List<RechargeInfo> getAllRechargeInfoByOpenid(String openid) {
		StatisManager.getInstance().incRedisKey(RedisKey.RECHARGE_INFO);
		String key1 = RedisKey.RECHARGE_INFO + ":" + openid;      //2024年某月之前的
		String key2 = RedisKey.RECHARGE_INFO_2024 + ":" + openid; //2024年某月之后的
		List<String> list1 = redisSession.lRange(key1, 0, -1, 0);
		List<String> list2 = redisSession.lRange(key2, 0, -1, 0);
		if (list1.isEmpty() && list2.isEmpty()) {
			return Collections.emptyList();
		}
		
		if (list1.isEmpty()) {
			list1 = new ArrayList<>();
		}
		list1.addAll(list2);
		List<RechargeInfo> array = new ArrayList<>();
		try {
			for (String info : list1) {
				// 这里在转平台之后，如果新的平台信息跟创角时的平台信息不一致，需要将rechargeInfo中的平台信息转换一下；此处不方面转换，需要在具体的使用逻辑中转换
				RechargeInfo rechargeInfo = JSONObject.parseObject(info, RechargeInfo.class);
				array.add(rechargeInfo);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return array;
	}
	
	
	/**
	 * 添加充值信息
	 * 
	 * @param rechargeInfo
	 */
	public void addRechargeInfoBack(String playerId, RechargeInfo rechargeInfo) {
		StatisManager.getInstance().incRedisKey(RedisKey.RECHARGE_INFO_BACK);
		String key = RedisKey.RECHARGE_INFO_BACK + ":" + rechargeInfo.getOpenid() + ":" + playerId;
		redisSession.lPush(key, 0, JSONObject.toJSONString(rechargeInfo));
	}
	
	/**
	 * 清除RechargeInfo
	 * @param rechargeInfo
	 */
	public void removeRechargeInfoBack(String playerId, RechargeInfo rechargeInfo) {
		StatisManager.getInstance().incRedisKey(RedisKey.RECHARGE_INFO_BACK);
		String key = RedisKey.RECHARGE_INFO_BACK + ":" + rechargeInfo.getOpenid() + ":" + playerId;
		redisSession.lRem(key, 1, JSONObject.toJSONString(rechargeInfo));
	}

	public List<RechargeInfo> getAllRechargeInfoBack(String openid, String playerId) {
		StatisManager.getInstance().incRedisKey(RedisKey.RECHARGE_INFO_BACK);
		String key = RedisKey.RECHARGE_INFO_BACK + ":" + openid + ":" + playerId;
		List<String> list = redisSession.lRange(key, 0, -1, 0);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}

		List<RechargeInfo> array = new ArrayList<>();
		try {
			for (String info : list) {
				RechargeInfo rechargeInfo = JSONObject.parseObject(info, RechargeInfo.class);
				array.add(rechargeInfo);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return array;
	}

	/**
	 * 获取所有屏蔽平台头像的账号
	 * 
	 * @return
	 */
	public Map<String, Long> getAllBanPortraitAccount() {
		StatisManager.getInstance().incRedisKey(BAN_PORTRAIT);
		Set<String> openidSet = redisSession.sMembers(BAN_PORTRAIT);
		Map<String, String> map = redisSession.hGetAll(BAN_PORTRAIT_TIMESTAMP);
		long now = HawkTime.getMillisecond();
		Map<String, Long> resultMap = new HashMap<String, Long>();
		for (Entry<String, String> entry : map.entrySet()) {
			long endTime = Long.valueOf(entry.getValue());
			if (endTime > now) {
				resultMap.put(entry.getKey(), endTime);
			} else {
				redisSession.hDel(BAN_PORTRAIT_TIMESTAMP, entry.getKey());
			}
		}
		
		// 这里主要是兼容旧版本的，新版版加了时长条件
		if (!openidSet.isEmpty()) {
			long banEndTime = now + HawkTime.DAY_MILLI_SECONDS * 365;
			for (String openid : openidSet) {
				resultMap.put(openid, banEndTime);
			}
		}
		
		return resultMap;
	}

	/*
	 * 获取历史所在的联盟
	 */
	public String[] getPlayerHistoryGuildIds(String playerId) {
		String key = String.format(HISTORY_GUILDS, playerId);
		String retStr = getRedisSession().getString(key);
		if (null != retStr && !retStr.isEmpty()) {
			String[] strArray = retStr.split(";");
			return strArray;
		}
		return new String[0];
	}

	/*
	 * 更新历史所造的联盟
	 */
	public void updatePlayerHistoryGuildIds(String playerId, String[] guildIds) {

		String key = String.format(HISTORY_GUILDS, playerId);
		StringBuffer sb = new StringBuffer();
		for (int i = 0, len = guildIds.length; i < len; i++) {
			if (i == (len - 1)) {
				sb.append(guildIds[i]);
			} else {
				sb.append(guildIds[i]).append(";");
			}
		}
		String value = sb.toString();
		getRedisSession().setString(key, value);
	}

	/**
	 * 添加玩家预设行军信息
	 */
	public void addPlayerPresetWorldMarch(String playerId, JSONArray arr) {
		StatisManager.getInstance().incRedisKey(PLAYER_PRESET_MARCH);
		String key = PLAYER_PRESET_MARCH + ":" + playerId;
		redisSession.setString(key, arr.toJSONString());
		
		//老的key存储会造成线上大key问题： 1、大key访问可能会造成阻塞，2、大key造成的内存不均衡。
		//redisSession.hSet(PLAYER_PRESET_MARCH, playerId, arr.toJSONString());
	}

	/**
	 * 添加玩家预设行军信息
	 */
	public String getPlayerPresetWorldMarch(String playerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_PRESET_MARCH);
		String key = PLAYER_PRESET_MARCH + ":" + playerId;
		String newStoreInfo = redisSession.getString(key);
		// 新的key能取到，直接返回
		if (!HawkOSOperator.isEmptyString(newStoreInfo)) {
			return newStoreInfo;
		}
		
		// 老的key存储会造成线上大key问题： 1、大key访问可能会造成阻塞，2、大key造成的内存不均衡。
		String oldStoreInfo = redisSession.hGet(PLAYER_PRESET_MARCH, playerId);
		// 将信息转储到新的key上，同时从老的key上删除
		if (!HawkOSOperator.isEmptyString(oldStoreInfo)) {
			redisSession.setString(key, oldStoreInfo);
			redisSession.hDel(PLAYER_PRESET_MARCH, playerId);
		}
		
		return oldStoreInfo;
	}

	/**
	 * 更新签名
	 * 
	 * @param playerId
	 * @param signature
	 */
	public String getSignature(String playerId) {
		StatisManager.getInstance().incRedisKey(SIGNATURE);
		return redisSession.hGet(SIGNATURE, playerId);
	}

	/**
	 * 更新签名
	 * 
	 * @param playerId
	 * @param signature
	 */
	public void updateSignature(String playerId, String signature) {
		StatisManager.getInstance().incRedisKey(SIGNATURE);
		redisSession.hSet(SIGNATURE, playerId, signature);
	}

	/**
	 * 获取所有装扮信息
	 * 
	 * @return
	 */
	public String getDressShow(String playerId) {
		StatisManager.getInstance().incRedisKey(DRESS_SHOW);
		String key = DRESS_SHOW + ":" + playerId;
		String newStoreInfo = redisSession.getString(key);
		// 如果新的key上能取到数据，直接返回
		if (!HawkOSOperator.isEmptyString(newStoreInfo)) {
			return newStoreInfo;
		}
		
		// 老的key存储会造成线上大key问题： 1、大key访问可能会造成阻塞，2、大key造成的内存不均衡。
		String oldStoreInfo = redisSession.hGet(DRESS_SHOW, playerId);
		// 将信息转储到新的key上，同时从老的key上删除
		if (!HawkOSOperator.isEmptyString(oldStoreInfo)) {
			redisSession.setString(key, oldStoreInfo);
			redisSession.hDel(DRESS_SHOW, playerId);
		}
		
		return oldStoreInfo;
	}

	/**
	 * 更新装扮信息
	 * 
	 * @param playerId
	 * @param dressItem
	 */
	public void updateDressShow(String playerId, Map<Integer, DressItem> dressItem) {
		StatisManager.getInstance().incRedisKey(DRESS_SHOW);
		String key = DRESS_SHOW + ":" + playerId;
		redisSession.setString(key, SerializeHelper.mapToString(dressItem));
		
		// 老的key存储会造成线上大key问题： 1、大key访问可能会造成阻塞，2、大key造成的内存不均衡。
		//redisSession.hSet(DRESS_SHOW, playerId, SerializeHelper.mapToString(dressItem));
	}

	/**
	 * 获取玩家属性
	 * 
	 * @param playerId
	 * @param var
	 * @return
	 */
	public String getPlayerVar(String playerId, String var) {
		StatisManager.getInstance().incRedisKey(PLAYER_LOCAL_VAR);
		String key = PLAYER_LOCAL_VAR + ":" + playerId;
		return redisSession.hGet(key, var);
	}

	/**
	 * 更新玩家属性
	 * 
	 * @param playerId
	 * @param var
	 * @param value
	 */
	public void updatePlayerVar(String playerId, String var, String value) {
		StatisManager.getInstance().incRedisKey(PLAYER_LOCAL_VAR);
		String key = PLAYER_LOCAL_VAR + ":" + playerId;
		redisSession.hSet(key, var, value);
	}

	/**
	 * 更新玩家赏金追加信息
	 * 
	 * @Desc 防止玩家进行金币转移
	 * @param playerId
	 * @param addBounty
	 *            主动追加给别人的
	 * @param beAddBounty
	 *            被别人追加的
	 */
	public void updatePlayerBountyAddInfo(String playerId, PlayerAddBountyInfo info) {
		if (info.Effect()) {
			try {
				String key = String.format(PLAYER_BOUNTY_ADD_INFO, playerId);
				String value = JsonUtils.Object2Json(info);
				if (null != value && !value.isEmpty()) {
					redisSession.setString(key, value);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 获取玩家赏金追加信息
	 * 
	 * @Desca 从全局redis中获取 不设置过期时间
	 * @param playerId
	 * @return
	 */
	public PlayerAddBountyInfo getPlayerBountyAddInfo(String playerId) {
		try {
			String key = String.format(PLAYER_BOUNTY_ADD_INFO, playerId);
			String value = redisSession.getString(key);
			if (null != value && !value.isEmpty()) {
				PlayerAddBountyInfo ret = JsonUtils.String2Object(value, PlayerAddBountyInfo.class);
				if (null != ret) {
					return ret;
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return new PlayerAddBountyInfo();
	}

	/**
	 * 跨入的玩家
	 * 
	 * @author jm 2019 上午11:37:37
	 * @param serverId
	 * @param playerId
	 * @param fromServerId
	 * @return
	 */
	public boolean saveImmigrationPlayer(String serverId, String playerId, String fromServerId, int expire) {
		String key = IMMIGRATION_PLAYER + ":" + serverId;
		StatisManager.getInstance().incRedisKey(IMMIGRATION_PLAYER);
		// todo 需要根据活动的时间设置一个过期的时间.
		return redisSession.hSet(key, playerId, fromServerId, expire) > 0;
	}

	/**
	 * 跨出的玩家
	 * 
	 * @author jm 2019 上午11:37:45
	 * @param serverId
	 * @return
	 */
	public Map<String, String> getImmigrationPlayer(String serverId) {
		String key = IMMIGRATION_PLAYER + ":" + serverId;
		StatisManager.getInstance().incRedisKey(IMMIGRATION_PLAYER);
		Map<String, String> map = redisSession.hGetAll(key);

		return map;
	}

	/**
	 * 移除迁入的玩家.
	 * 
	 * @param serverId
	 * @param playerId
	 * @return
	 */
	public boolean removeImmigrationPlayer(String serverId, String playerId) {
		String key = IMMIGRATION_PLAYER + ":" + serverId;
		StatisManager.getInstance().incRedisKey(IMMIGRATION_PLAYER);

		return redisSession.hDel(key, playerId) > 0;
	}

	/**
	 * 跨出的玩家
	 * 
	 * @author jm 2019 上午11:38:03
	 * @param serverId
	 * @param playerId
	 * @param toCrossServerId
	 * @return
	 */
	public boolean saveEmigrationPlayer(String serverId, String playerId, String toCrossServerId, int expire) {
		String key = EMIGRATION_PLAYER + ":" + serverId;
		StatisManager.getInstance().incRedisKey(EMIGRATION_PLAYER);
		// todo 需要根据活动的时间设置一个过期的时间.
		return redisSession.hSet(key, playerId, toCrossServerId, expire) > 0;
	}

	/**
	 * 跨出的玩家
	 * 
	 * @author jm 2019 上午11:38:17
	 * @param serverId
	 * @return
	 */
	public Map<String, String> getEmigrationPlayers(String serverId) {
		String key = EMIGRATION_PLAYER + ":" + serverId;
		StatisManager.getInstance().incRedisKey(EMIGRATION_PLAYER);
		Map<String, String> map = redisSession.hGetAll(key);

		return map;
	}

	/**
	 * 移除迁出去的玩家信息
	 * 
	 * @param serverId
	 * @param playerId
	 * @return
	 */
	public boolean removeEmigrationPlayer(String serverId, String playerId) {
		String key = EMIGRATION_PLAYER + ":" + serverId;
		StatisManager.getInstance().incRedisKey(EMIGRATION_PLAYER);

		return redisSession.hDel(key, playerId) > 0;
	}

	/**
	 * 清理玩家的状态,防止死在redis里面。
	 * 
	 * @param serverId
	 * @param playerId
	 */
	public void removePlayerCrossStatus(String serverId, String playerId) {
		String key = CROSS_STATUS + ":" + serverId;
		StatisManager.getInstance().incRedisKey(CROSS_STATUS);

		redisSession.hDel(key, playerId);
	}

	/**
	 * 设置玩家的跨服状态
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean setPlayerCrossStatus(String serverId, String playerId, int status) {
		String key = CROSS_STATUS + ":" + serverId;
		StatisManager.getInstance().incRedisKey(CROSS_STATUS);
		try (Jedis jedis = redisSession.getJedis()) {
			jedis.hset(key, playerId, status + "");

			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return false;
	}

	/**
	 * 获取玩家的跨服状态.
	 * 
	 * @param playerId
	 * @return
	 */
	public int getPlayerCrossStatus(String serverId, String playerId) {
		String key = CROSS_STATUS + ":" + serverId;
		StatisManager.getInstance().incRedisKey(CROSS_STATUS);
		try (Jedis jedis = redisSession.getJedis()) {
			String status = jedis.hget(key, playerId);
			if (HawkOSOperator.isEmptyString(status)) {
				return GsConst.PlayerCrossStatus.NOTHING;
			} else {
				return Integer.parseInt(status);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return -1;
	}

	/**
	 * null 为访问异常了.
	 * 
	 * @param serverId
	 * @return
	 */
	public Map<String, Integer> getAllPlayerCrossStatus(String serverId) {
		String key = CROSS_STATUS + ":" + serverId;
		StatisManager.getInstance().incRedisKey(CROSS_STATUS);
		try (Jedis jedis = redisSession.getJedis()) {
			Map<String, String> redisMap = jedis.hgetAll(key);
			Map<String, Integer> map = new HashMap<>(redisMap.size());
			redisMap.entrySet().stream().forEach(entry -> {
				map.put(entry.getKey(), Integer.valueOf(entry.getValue()));
			});

			return map;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/**
	 * 增加跨服积分
	 * 
	 * @param rankType
	 * @param id
	 * @param termId
	 * @param score
	 * @return
	 */
	public boolean addCrossActivityScore(CrossRankType rankType, String id, int termId, long score) {
		String key = getCrossRankKey(rankType, termId);
		if (HawkOSOperator.isEmptyString(key)) {
			return false;
		}
		redisSession.zIncrby(key, id, score, 604800);
		StatisManager.getInstance().incRedisKey(key);
		return true;
	}

	/**
	 * 获取跨服活动排行key值
	 * 
	 * @param rankType
	 * @param termId
	 * @return
	 */
	public String getCrossRankKey(CrossRankType rankType, int termId) {
		String key = "";
		switch (rankType) {
		case C_SELF_RANK:
			key = CACTIVITY_SELF_RANK;
			break;
		case C_GUILD_RANK:
			key = CACTIVITY_GUILD_RANK;
			break;
		case C_SERVER_RANK:
			key = CACTIVITY_SERVER_RANK;
			break;
		case C_TALENT_RANK:
			key = CACTIVITY_TALENT_POINT_RANK;
			break;
		case C_TALENT_GUILD_RANK:
			key = CACTIVITY_TALENT_POINT_GUILD_RANK;
			break;
		}
		if (HawkOSOperator.isEmptyString(key)) {
			return key;
		}
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return "";
		}
		return key + ":" + crossId + ":" + termId;
	}

	public Set<Tuple> getCrossRanks(CrossRankType rankType, int termId, long start, long end) {
		String key = getCrossRankKey(rankType, termId);
		if (HawkOSOperator.isEmptyString(key)) {
			return Collections.emptySet();
		}
		StatisManager.getInstance().incRedisKey(key);
		return redisSession.zRevrangeWithScores(key, start, end, 0);
	}

	/**
	 * 获取指定对象的跨服排行
	 * 
	 * @param rankType
	 * @param termId
	 * @param memberId
	 * @return
	 */
	public HawkTuple2<Long, Double> getCrossRank(CrossRankType rankType, int termId, String memberId) {
		String key = getCrossRankKey(rankType, termId);
		if (HawkOSOperator.isEmptyString(key)) {
			return null;
		}
		Long index = redisSession.zRank(key, memberId);
		Double score = redisSession.zScore(key, memberId, 0);
		if (index != null && index >= 0 && Objects.nonNull(score)) {
			return new HawkTuple2<Long, Double>(index, score);
		}
		StatisManager.getInstance().incRedisKey(key);
		return null;
	}

	/**
	 * 将某个对象从跨服排行中移除
	 * 
	 * @param rankType
	 * @param termId
	 * @param memberId
	 * @return
	 */
	public Long removeFromCrossRank(CrossRankType rankType, int termId, String memberId) {
		String key = getCrossRankKey(rankType, termId);
		if (HawkOSOperator.isEmptyString(key)) {
			return 0L;
		}
		StatisManager.getInstance().incRedisKey(key);
		return redisSession.zRem(key, 0, memberId);
	}

	/**
	 * 获取跨服活动已领取奖励id
	 * 
	 * @param termId
	 * @param playerId
	 * @return
	 */
	public List<Integer> getCrossRewardedIds(int termId, String playerId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return Collections.emptyList();
		}
		String key = CACTIVITY_SELF_REWARDED_IDS + ":" + crossId + ":" + termId;
		String result = redisSession.hGet(key, playerId, 0);
		if (HawkOSOperator.isEmptyString(result)) {
			return Collections.emptyList();
		}
		List<Integer> rewardedList = new ArrayList<>();
		for (String idStr : result.split(",")) {
			rewardedList.add(Integer.valueOf(idStr));
		}
		StatisManager.getInstance().incRedisKey(CACTIVITY_SELF_REWARDED_IDS);
		return rewardedList;
	}

	/**
	 * 添加玩家已领奖
	 * 
	 * @param termId
	 * @param playerId
	 * @param rewardedIds
	 * @return
	 */
	public boolean updateCrossRreardedIds(int termId, String playerId, List<Integer> rewardedIds) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return false;
		}
		String key = CACTIVITY_SELF_REWARDED_IDS + ":" + crossId + ":" + termId;
		String value = Joiner.on(",").join(rewardedIds);
		redisSession.hSet(key, playerId, value);
		StatisManager.getInstance().incRedisKey(CACTIVITY_SELF_REWARDED_IDS);
		return true;
	}

	/**
	 * 更新跨服活动信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCActivityInfo(CActivityInfo activityInfo) {
		String jsonString = JSON.toJSONString(activityInfo);
		String key = CACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(CACTIVITY_SERVER_INFO);
		return true;
	}

	/**
	 * 获取跨服活动信息
	 * 
	 * @return
	 */
	public CActivityInfo getCActivityInfo() {
		String key = CACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		CActivityInfo activityInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			activityInfo = new CActivityInfo();
		} else {
			activityInfo = JSON.parseObject(dataStr, CActivityInfo.class);
		}
		StatisManager.getInstance().incRedisKey(CACTIVITY_SERVER_INFO);
		return activityInfo;
	}

	/**
	 * 更新玩家信息
	 * 
	 * @param playerInfo
	 * @param termId
	 * @return
	 */
	public boolean updateCrossPlayerInfo(CPlayerInfo.Builder playerInfo, int termId) {
		if (playerInfo == null || HawkOSOperator.isEmptyString(playerInfo.getId())) {
			return false;
		}

		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return false;
		}

		String key = CACTIVITY_PLAYER_INFO + ":" + crossId + ":" + termId;
		String playerId = playerInfo.getId();
		redisSession.hSetBytes(key, playerId, playerInfo.build().toByteArray(), CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(CACTIVITY_PLAYER_INFO);
		return true;
	}

	/**
	 * 获取跨服玩家基础信息
	 * 
	 * @param playerId
	 * @param termId
	 * @return
	 */
	public CPlayerInfo.Builder getCrossPlayerInfo(String playerId, int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return null;
		}
		String key = CACTIVITY_PLAYER_INFO + ":" + crossId + ":" + termId;
		byte[] val = redisSession.hGetBytes(key, playerId);
		if (val != null) {
			try {
				CPlayerInfo.Builder builder = CPlayerInfo.newBuilder();
				builder.mergeFrom(val);
				return builder;
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
		}
		StatisManager.getInstance().incRedisKey(CACTIVITY_PLAYER_INFO);
		return null;
	}

	/**
	 * 获取跨服联盟信息
	 * 
	 * @param termId
	 * @param guildIds
	 * @return
	 */
	public Map<String, CPlayerInfo.Builder> getCrossPlayerInfo(int termId, List<String> playerIds) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null || playerIds == null || playerIds.isEmpty()) {
			return new HashMap<>();
		}
		String key = CACTIVITY_PLAYER_INFO + ":" + crossId + ":" + termId;
		byte[][] idbytes = new byte[playerIds.size()][];
		for (int i = 0; i < playerIds.size(); i++) {
			idbytes[i] = playerIds.get(i).getBytes();
		}
		List<byte[]> values = redisSession.hmGet(key.getBytes(), idbytes);
		Map<String, CPlayerInfo.Builder> builderMap = new HashMap<>();
		int failedCnt = 0;
		for (byte[] value : values) {
			try {
				if (value == null) {
					failedCnt++;
					continue;
				}
				CPlayerInfo.Builder builder = CPlayerInfo.newBuilder();
				builder.mergeFrom(value);
				builderMap.put(builder.getId(), builder);
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
		}
		StatisManager.getInstance().incRedisKey(CACTIVITY_PLAYER_INFO);
		HawkLog.logPrintln("getCrossPlayerInfo finish,failedCnt :{}", failedCnt);
		return builderMap;
	}

	/**
	 * 添加跨服活动联盟基础信息
	 * 
	 * @param playerInfo
	 * @param termId
	 * @return
	 */
	public boolean addCrossGuildInfo(CGuildInfo.Builder guildInfo, int termId) {
		if (guildInfo == null) {
			return false;
		}

		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return false;
		}

		String key = CACTIVITY_GUILD_INFO + ":" + crossId + ":" + termId;
		redisSession.hSetBytes(key, guildInfo.getId(), guildInfo.build().toByteArray(), CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(CACTIVITY_GUILD_INFO);
		return true;
	}

	/**
	 * 添加跨服活动联盟基础信息
	 * 
	 * @param playerInfo
	 * @param termId
	 * @return
	 */
	public boolean addCrossGuildInfo(Map<byte[], byte[]> guildInfos, int termId) {
		if (guildInfos == null || guildInfos.isEmpty()) {
			return false;
		}

		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return false;
		}

		String key = CACTIVITY_GUILD_INFO + ":" + crossId + ":" + termId;
		redisSession.hmSetBytes(key, guildInfos, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(CACTIVITY_GUILD_INFO);
		return true;
	}

	/**
	 * 获取跨服活动联盟基础信息
	 * 
	 * @param playerId
	 * @param termId
	 * @return
	 */
	public CGuildInfo.Builder getCrossGuildInfo(String guildId, int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return null;
		}
		String key = CACTIVITY_GUILD_INFO + ":" + crossId + ":" + termId;
		byte[] val = redisSession.hGetBytes(key, guildId);
		if (val != null) {
			try {
				CGuildInfo.Builder builder = CGuildInfo.newBuilder();
				builder.mergeFrom(val);
				return builder;
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
		}
		StatisManager.getInstance().incRedisKey(CACTIVITY_GUILD_INFO);
		return null;
	}

	/**
	 * 获取跨服联盟信息
	 * 
	 * @param termId
	 * @param guildIds
	 * @return
	 */
	public Map<String, CGuildInfo.Builder> getCrossGuildInfo(int termId, List<String> guildIds) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null || guildIds == null || guildIds.isEmpty()) {
			return new HashMap<>();
		}
		String key = CACTIVITY_GUILD_INFO + ":" + crossId + ":" + termId;
		byte[][] idbytes = new byte[guildIds.size()][];
		for (int i = 0; i < guildIds.size(); i++) {
			idbytes[i] = guildIds.get(i).getBytes();
		}
		List<byte[]> values = redisSession.hmGet(key.getBytes(), idbytes);
		Map<String, CGuildInfo.Builder> builderMap = new HashMap<>();
		for (byte[] value : values) {
			try {
				CGuildInfo.Builder builder = CGuildInfo.newBuilder();
				builder.mergeFrom(value);
				builderMap.put(builder.getId(), builder);
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
		}
		StatisManager.getInstance().incRedisKey(CACTIVITY_GUILD_INFO);
		return builderMap;
	}

	/**
	 * 添加跨服联盟id
	 * 
	 * @param guildId
	 * @param termId
	 * @return
	 */
	public boolean addCrossGuild(String guildId, int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return false;
		}
		String serverId = GsConfig.getInstance().getServerId();
		String key = CCROSS_GUILDIDS + ":" + crossId + ":" + termId + ":" + serverId;
		redisSession.sAdd(key, CrossConstCfg.getInstance().getcActivityRedisExpire(), guildId);
		StatisManager.getInstance().incRedisKey(CCROSS_GUILDIDS);
		return true;
	}

	/**
	 * 获取本服所有跨服过来的联盟id
	 * 
	 * @param termId
	 * @return
	 */
	public Collection<String> getCrossGuilds(int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return Collections.emptySet();
		}
		String serverId = GsConfig.getInstance().getServerId();
		String key = CCROSS_GUILDIDS + ":" + crossId + ":" + termId + ":" + serverId;
		Set<String> jsonData = redisSession.sMembers(key);
		StatisManager.getInstance().incRedisKey(CCROSS_GUILDIDS);
		return jsonData;
	}

	/**
	 * 添加跨服玩家id
	 * 
	 * @param playerId
	 * @param termId
	 * @return
	 */
	public boolean addCrossPlayerId(String playerId, int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return false;
		}
		String serverId = GsConfig.getInstance().getServerId();
		String key = CCROSS_PLAYERS + ":" + crossId + ":" + termId + ":" + serverId;
		redisSession.sAdd(key, CrossConstCfg.getInstance().getcActivityRedisExpire(), playerId);
		StatisManager.getInstance().incRedisKey(CCROSS_PLAYERS);
		return true;
	}

	/**
	 * 移除跨服玩家id
	 * 
	 * @param playerId
	 * @param termId
	 * @return
	 */
	public boolean removeCrossPlayerId(String playerId, int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return false;
		}
		String serverId = GsConfig.getInstance().getServerId();
		String key = CCROSS_PLAYERS + ":" + crossId + ":" + termId + ":" + serverId;
		redisSession.sRem(key, playerId);
		StatisManager.getInstance().incRedisKey(CCROSS_PLAYERS);
		return true;
	}

	/**
	 * 获取本服所有跨服过来的玩家id
	 * 
	 * @param termId
	 * @return
	 */
	public Collection<String> getCrossPlayerIds(int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return Collections.emptySet();
		}
		String serverId = GsConfig.getInstance().getServerId();
		String key = CCROSS_PLAYERS + ":" + crossId + ":" + termId + ":" + serverId;
		Set<String> jsonData = redisSession.sMembers(key);
		StatisManager.getInstance().incRedisKey(CCROSS_PLAYERS);
		return jsonData;
	}

	/**
	 * 添加跨服活动排行buff信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean addCRankBuff(CRankBuff cRankBuff) {
		String jsonString = JSON.toJSONString(cRankBuff);
		String key = CACTIVITY_RANK_BUFF + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(CACTIVITY_RANK_BUFF);
		return true;
	}

	/**
	 * 移除跨服活动排行作用号
	 * 
	 * @return
	 */
	public boolean removeCRankBuff() {
		String key = CACTIVITY_RANK_BUFF + ":" + GsConfig.getInstance().getServerId();
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(CACTIVITY_RANK_BUFF);
		return true;
	}

	/**
	 * 获取跨服buff加成数据
	 * 
	 * @return
	 */
	public CRankBuff getCRankBuff() {
		String key = CACTIVITY_RANK_BUFF + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		CRankBuff cRankBuff = null;
		if (!HawkOSOperator.isEmptyString(dataStr)) {
			cRankBuff = JSON.parseObject(dataStr, CRankBuff.class);
		}
		StatisManager.getInstance().incRedisKey(CACTIVITY_RANK_BUFF);
		return cRankBuff;
	}

	public String getAccountRoleInfoKey(String openId) {
		return ACCOUNT_ROLE_KEY + ":" + openId;
	}

	public void addWin32Friend(String openId, String targetOpenId, String nickName) {
		String key = WIN32_FRIEND + ":" + openId;
		redisSession.hSet(key, targetOpenId, nickName);
	}

	public JSONObject getWin32Friend(String openId) {
		String key = WIN32_FRIEND + ":" + openId;
		Map<String, String> map = redisSession.hGetAll(key);
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		for (Entry<String, String> entry : map.entrySet()) {
			JSONObject friendObject = new JSONObject();
			friendObject.put("openid", entry.getKey());
			friendObject.put("nickName", entry.getValue());
			jsonArray.add(friendObject);
		}

		jsonObject.put("lists", jsonArray);
		return jsonObject;
	}

	/***
	 * 保存传承标识
	 * 
	 * @param info
	 */
	public void saveRoleInfoAfterOldServerComeBack(ActivityAccountRoleInfo info) {
		if (info == null) {
			return;
		}
		String key = INHERIT_IDENTIFY + ":" + info.getOpenId();
		redisSession.setString(key, JsonUtils.Object2Json(info)); // 保存传承标识
		HawkLog.logPrintln("saveRoleInfoAfterOldServerComeBack, account:{}", info);
	}

	/***
	 * 获取传承标识
	 * 
	 * @param openId
	 * @return
	 */
	public ActivityAccountRoleInfo getRoleInfoInheritIdentifyAndRemove(String openId) {
		if (openId == null) {
			return null;
		}
		String key = INHERIT_IDENTIFY + ":" + openId;
		String value = redisSession.getString(key);
		if (value == null || value.trim().equals("")) {
			return null;
		}
		redisSession.del(key); // 获取到了数据就从redis删除，确保传承只开启一次
		HawkLog.logPrintln("getRoleInfoInheritIdentifyAndRemove, account:{}", value);
		return JsonUtils.String2Object(value, ActivityAccountRoleInfo.class);
	}

	/*
	 * 获取客户端 已领取奖励的版本
	 */

	public List<String> getPlayerRewardVersions(String playerId) {
		try {
			if (!HawkOSOperator.isEmptyString(playerId)) {
				String key = PLAYER_UPDATE_VERSION_REWARD_HISTORY + ":" + playerId;
				String value = redisSession.getString(key);
				if (!HawkOSOperator.isEmptyString(value)) {
					String[] versionArray = value.split(",");
					return Arrays.asList(versionArray);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return new ArrayList<String>();
	}

	public void setPlayerRewardVersions(String playerId, String versions) {
		if (!HawkOSOperator.isEmptyString(playerId) && !HawkOSOperator.isEmptyString(versions)) {
			String key = PLAYER_UPDATE_VERSION_REWARD_HISTORY + ":" + playerId;
			redisSession.setString(key, versions);
		}
	}

	/***
	 * 获取军魂承接标识
	 * 
	 * @param openId
	 * @return
	 */
	public BackPlayerInfo getBackPlayerInfo(String openId) {
		if (HawkOSOperator.isEmptyString(openId)) {
			return null;
		}
		String key = PLAYER_BACK_INFO + ":" + openId;
		String value = redisSession.getString(key);
		StatisManager.getInstance().incRedisKey(PLAYER_BACK_INFO);
		if (value == null || value.trim().equals("")) {
			return null;
		}
		HawkLog.debugPrintln("getBackPlayerInfo, info:{}", value);
		return JsonUtils.String2Object(value, BackPlayerInfo.class);
	}

	/**
	 * 保存军魂承接回归标识
	 * 
	 * @param info
	 */
	public void updateBackPlayerInfo(BackPlayerInfo info) {
		if (info == null) {
			return;
		}
		String key = PLAYER_BACK_INFO + ":" + info.getOpenId();
		redisSession.setString(key, JsonUtils.Object2Json(info));
		StatisManager.getInstance().incRedisKey(PLAYER_BACK_INFO);
		HawkLog.debugPrintln("updateBackPlayerInfo, info:{}", info);
	}
	
	/**
	 * 添加已被承接角色记录
	 * @param info
	 */
	public void addInheritedInfo(AccountRoleInfo info, JSONObject inheritInfo) {
		if (info == null) {
			return;
		}
		StatisManager.getInstance().incRedisKey(INHERITED_INFOS);
		String key = INHERITED_INFOS + ":" + info.getOpenId();
		String inheritedKey = INHERITED_INFOS + "_record:" + info.getOpenId();
		redisSession.lPush(key, 0, info.getPlayerId());
		redisSession.lPush(inheritedKey, 0, inheritInfo.toJSONString());
	}

	/**
	 * 获取已被承接角色记录
	 * 
	 * @param openid
	 * @return
	 */
	public List<String> getIngheritedInfos(String openid) {
		String key = INHERITED_INFOS + ":" + openid;
		List<String> list = redisSession.lRange(key, 0, Long.MAX_VALUE, 0);
		//大区redis合并前，手Q、微信的redis可能都往新的集群中写入了同样的数据，所以这里需要去重
		List<String> retList = new ArrayList<>();
		for (String info : list) {
			if (!retList.contains(info)) {
				retList.add(info);
			} else {
				//count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
				redisSession.lRem(key, -1, info);
			}
		}
		return retList;
	}
	
	/**
	 * 获取传承记录数据
	 * @param openid
	 * @return
	 */
	public List<String> getInheritedRecords(String openid) {
		String inheritedKey = INHERITED_INFOS + "_record:" + openid;
		List<String> list = redisSession.lRange(inheritedKey, 0, Long.MAX_VALUE, 0);
		//大区redis合并前，手Q、微信的redis可能都往新的集群中写入了同样的数据，所以这里需要去重
		List<String> retList = new ArrayList<>();
		for (String info : list) {
			if (!retList.contains(info)) {
				retList.add(info);
			} else {
				//count < 0 : 从表尾开始向表头搜索，移除与 value 相等的元素，数量为 count 的绝对值。
				redisSession.lRem(inheritedKey, -1, info);
			}
		}
		return retList;
	}
	
	/*****************************/
	/***
	 * 获取军魂承接标识-新
	 * 
	 * @param openId
	 * @return
	 */
	public BackNewPlayerInfo getBackPlayerInfoNew(String openId) {
		if (HawkOSOperator.isEmptyString(openId)) {
			return null;
		}
		StatisManager.getInstance().incRedisKey(PLAYER_BACK_INFO_NEW);
		String key = PLAYER_BACK_INFO_NEW + ":" + openId;
		String value = redisSession.getString(key);
		if (HawkOSOperator.isEmptyString(value)) {
			return null;
		}
		HawkLog.debugPrintln("getBackPlayerInfoNew, info:{}", value);
		return JsonUtils.String2Object(value, BackNewPlayerInfo.class);
	}

	/**
	 * 保存军魂承接回归标识-新
	 * @param info
	 */
	public void updateBackPlayerInfoNew(BackNewPlayerInfo info) {
		if (info == null) {
			return;
		}

		StatisManager.getInstance().incRedisKey(PLAYER_BACK_INFO_NEW);
		String key = PLAYER_BACK_INFO_NEW + ":" + info.getOpenId();
		redisSession.setString(key, JsonUtils.Object2Json(info));
	}

	/**
	 * 添加伊娃红点提示信息
	 * 
	 * @param playerId
	 * @param tipMsg
	 */
	public void updateYiwaTipMsg(String playerId, String tipMsg) {
		redisSession.hSet(YIWA_RED_POINT, playerId, tipMsg);
	}

	/**
	 * 获取伊娃红点提示信息
	 * 
	 * @param playerId
	 */
	public String getYiwaTipMsg(String playerId) {
		return redisSession.hGet(YIWA_RED_POINT, playerId);
	}

	/**
	 * 删除伊娃红点提示信息
	 * 
	 * @param playerId
	 */
	public void deleteYiwaTipMsg(String playerId) {
		redisSession.hDel(YIWA_RED_POINT, playerId);
	}

	/** 取得当天英雄roll天赋数 */
	public int getHeroTalentDayRollCount(String playerId) {
		String key = HERO_TALENT_ROLL_DAY_COUNT + ":" + playerId + ":" + HawkTime.getYearDay();
		String str = redisSession.getString(key);
		return NumberUtils.toInt(str);
	}

	public void incHeroTalentDayRollCount(String playerId) {
		String key = HERO_TALENT_ROLL_DAY_COUNT + ":" + playerId + ":" + HawkTime.getYearDay();
		redisSession.increaseBy(key, 1, (int) TimeUnit.DAYS.toSeconds(1));
	}
	
	/**
	 * 更新新兵救援信息
	 * 
	 * @param protectSoldierInfo
	 */
	public void updateProtectSoldierInfo(ProtectSoldierInfo protectSoldierInfo, int expireSeconds) {
		StatisManager.getInstance().incRedisKey(PROTECT_SOLDIER);
		String key = PROTECT_SOLDIER + ":" + protectSoldierInfo.getPlayerId();
		redisSession.setString(key, JSONObject.toJSONString(protectSoldierInfo), expireSeconds);
	}
	
	/**
	 * 记录锦标赛中秒的兵
	 */
	public void jbsIncreaseCreateSoldier(String playerId, int armyId, int count) {
		String key = String.format("%s:%s", JBS_CREATE_ARMY, playerId);
		redisSession.hIncrBy(key, armyId + "", count);
		StatisManager.getInstance().incRedisKey(JBS_CREATE_ARMY);
	}
	
	/***
	 * 所有在锦标赛中造的兵
	 */
	public Map<String, String> jbsTakeOutAllCreateSoldier(String playerId){
		String key = String.format("%s:%s", JBS_CREATE_ARMY, playerId);
		Map<String,String> result = redisSession.hGetAll(key);
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(JBS_CREATE_ARMY);
		return result;
	}

	/**
	 * 获取新兵救援信息
	 * 
	 * @param playerId
	 * @return
	 */
	public ProtectSoldierInfo getProtectSoldierInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(PROTECT_SOLDIER);
		String key = PROTECT_SOLDIER + ":" + playerId;
		String info = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(info)) {
			return JSONObject.parseObject(info, ProtectSoldierInfo.class);
		}

		return null;
	}
	
	/**
	 * 更新大R复仇信息
	 * 
	 * @param revengeInfo
	 */
	public void updateRevengeInfo(RevengeInfo revengeInfo) {
		StatisManager.getInstance().incRedisKey(REVENGE_INFO_KEY);
		String key = REVENGE_INFO_KEY + ":" + revengeInfo.getPlayerId();
		long startTime = revengeInfo.getStartTime() > 0 ? revengeInfo.getStartTime() : HawkApp.getInstance().getCurrentTime();
		long expireTime = startTime + ConstProperty.getInstance().getRevengeShopRefresh() - HawkApp.getInstance().getCurrentTime();
		redisSession.setString(key, JSONObject.toJSONString(revengeInfo), (int) (expireTime/1000));
	}
	
	/**
	 * 获取大R复仇信息
	 * 
	 * @param playerId
	 * @return
	 */
	public RevengeInfo getRevengeInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(REVENGE_INFO_KEY);
		String key = REVENGE_INFO_KEY + ":" + playerId;
		String revengeInfo = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(revengeInfo)) {
			return JSONObject.parseObject(revengeInfo, RevengeInfo.class);
		}
		
		return null;
	}
	
	/**
	 * 添加大R复仇死兵信息
	 * 
	 * @param deadSoldierInfo
	 */
	public void addRevengeDeadSoldierInfo(RevengeSoldierInfo deadSoldierInfo, int expireSecond) {
		StatisManager.getInstance().incRedisKey(REVENGE_DEAD_SOLDIER_KEY);
		String key = REVENGE_DEAD_SOLDIER_KEY + ":" + deadSoldierInfo.getPlayerId();
		redisSession.hSet(key, deadSoldierInfo.getUuid(), JSONObject.toJSONString(deadSoldierInfo), expireSecond);
	}
	
	/**
	 * 删除大R复仇死兵信息
	 * 
	 * @param deadSoldierInfo
	 */
	public void removeRevengeDeadSoldierInfo(RevengeSoldierInfo deadSoldierInfo) {
		StatisManager.getInstance().incRedisKey(REVENGE_DEAD_SOLDIER_KEY);
		String key = REVENGE_DEAD_SOLDIER_KEY + ":" + deadSoldierInfo.getPlayerId();
		redisSession.hDel(key, deadSoldierInfo.getUuid());
	}
	
	/**
	 * 获取所有大R复仇死兵信息
	 * 
	 * @param playerId
	 */
	public List<RevengeSoldierInfo> getAllRevengeDeadSoldierInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(REVENGE_DEAD_SOLDIER_KEY);
		String key = REVENGE_DEAD_SOLDIER_KEY + ":" + playerId;
		Map<String, String> map = redisSession.hGetAll(key);
		List<RevengeSoldierInfo> deadSoldierInfo = new ArrayList<RevengeSoldierInfo>();
		for (Entry<String, String> entry : map.entrySet()) {
			deadSoldierInfo.add(JSONObject.parseObject(entry.getValue(), RevengeSoldierInfo.class));
		}
		
		return deadSoldierInfo;
	}
	
	/**
	 * 移除所有大R死兵复仇信息
	 * 
	 * @param playerId
	 */
	public void removeAllRevengeDeadSoldierInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(REVENGE_DEAD_SOLDIER_KEY);
		String key = REVENGE_DEAD_SOLDIER_KEY + ":" + playerId;
		redisSession.del(key);
	}
	
	/**
	 * 更新大R复仇商店购买信
	 * 
	 * @param shopId
	 * @param count
	 */
	public void updateRevengeShopBuyInfo(String playerId, int shopId, int count, int expireSecond) {
		StatisManager.getInstance().incRedisKey(REVENGE_SHOP_BUY);
		String key = REVENGE_SHOP_BUY + ":" + playerId;
		redisSession.hSet(key, String.valueOf(shopId), String.valueOf(count), expireSecond);
	}
	
	/**
	 * 获取所有大R复仇死兵信息
	 * 
	 * @param playerId
	 */
	public Map<Integer, Integer> getRevengeShopBuyInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(REVENGE_SHOP_BUY);
		String key = REVENGE_SHOP_BUY + ":" + playerId;
		Map<Integer, Integer> shopBuy = new HashMap<Integer, Integer>();
		Map<String, String> map = redisSession.hGetAll(key);
		for (Entry<String, String> entry : map.entrySet()) {
			shopBuy.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
		}
		
		return shopBuy;
	}
	
	/**
	 * 移除所有大R复仇商店购买信息
	 * 
	 * @param playerId
	 */
	public void removeRevengeShopBuyInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(REVENGE_SHOP_BUY);
		String key = REVENGE_SHOP_BUY + ":" + playerId;
		redisSession.del(key);
	}
	
	
	/**
	 * 更新限时商店条件达成信息
	 * 
	 * @param conditionInfo
	 */
	public void updateTimeLimitStoreCondition(TimeLimitStoreConditionInfo conditionInfo, int expireSeconds) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_CONDITION);
		String key = TIMELIMIT_STORE_CONDITION + ":" + conditionInfo.getPlayerId();
		redisSession.hSet(key, String.valueOf(conditionInfo.getTriggerType()), JSONObject.toJSONString(conditionInfo), expireSeconds);
	}
	
	/**
	 * 获取限时商店条件达成信息
	 * 
	 * @param playerId
	 * @param conditionType
	 * @return
	 */
	public TimeLimitStoreConditionInfo getTimeLimitStoreCondition(String playerId, int conditionType) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_CONDITION);
		String key = TIMELIMIT_STORE_CONDITION + ":" + playerId;
		String conditionInfo = redisSession.hGet(key, String.valueOf(conditionType));
		if (!HawkOSOperator.isEmptyString(conditionInfo)) {
			return JSONObject.parseObject(conditionInfo, TimeLimitStoreConditionInfo.class);
		}
		
		return null;
	}
	
	/**
	 * 获取所有的条件达成信息
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<Integer, TimeLimitStoreConditionInfo> getTimeLimitStoreCondition(String playerId) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_CONDITION);
		String key = TIMELIMIT_STORE_CONDITION + ":" + playerId;
		Map<String, String> map = redisSession.hGetAll(key);
		Map<Integer, TimeLimitStoreConditionInfo> conditionMap = new HashMap<Integer, TimeLimitStoreConditionInfo>();
		for (Entry<String, String> entry : map.entrySet()) {
			TimeLimitStoreConditionInfo obj = JSONObject.parseObject(entry.getValue(), TimeLimitStoreConditionInfo.class);
			conditionMap.put(Integer.valueOf(entry.getKey()), obj);
		}
		
		return conditionMap;
	}

	/**
	 * 删除所有限时商品库信息
	 * 
	 * @param playerId
	 */
	public void removeAllTimeLimitStoreCondition(String playerId) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_CONDITION);
		String key = TIMELIMIT_STORE_CONDITION + ":" + playerId;
		redisSession.del(key);
	}
	
	/**
	 * 限时商店购买信息更新
	 * 
	 * @param playerId
	 * @param shopId
	 * @param count
	 * @param expireSeconds
	 */
	public void updateTimeLimitStoreBoughtInfo(String playerId, int shopId, int count, int expireSeconds) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_BOUGHT);
		String key = TIMELIMIT_STORE_BOUGHT + ":" + playerId;
		redisSession.hSet(key, String.valueOf(shopId), String.valueOf(count), expireSeconds);
	}
	
	/**
	 * 获取商品购买数量
	 * 
	 * @param playerId
	 * @param shopId
	 * @return
	 */
	public int getTimeLimitStoreBoughtCount(String playerId, int shopId) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_BOUGHT);
		String key = TIMELIMIT_STORE_BOUGHT + ":" + playerId;
		String result = redisSession.hGet(key, String.valueOf(shopId));
		if (!HawkOSOperator.isEmptyString(result)) {
			return Integer.valueOf(result);
		}
		
		return 0;
	}
	
	/**
	 * 获取商品购买信息
	 * 
	 * @param playerId
	 * @return
	 */
	public Map<Integer, Integer> getTimeLimitStoreBoughtInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_BOUGHT);
		String key = TIMELIMIT_STORE_BOUGHT + ":" + playerId;
		Map<String, String> result = redisSession.hGetAll(key);
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (Entry<String, String> entry : result.entrySet()) {
			map.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
		}
		
		return map;
	}
	
	/**
	 * 删除所有商品购买信息
	 * 
	 * @param playerId
	 */
	public void removeAllTimeLimitStoreBoughtInfo(String playerId) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_BOUGHT);
		String key = TIMELIMIT_STORE_BOUGHT + ":" + playerId;
		redisSession.del(key);
	}
	
	
	/**
	 * 更新泰伯利亚之战活动信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTWActivityInfo(TWActivityData activityInfo) {
		String jsonString = JSON.toJSONString(activityInfo);
		String key = TWACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(TWACTIVITY_SERVER_INFO);
		return true;
	}

	/**
	 * 获取泰伯利亚之战活动信息
	 * 
	 * @return
	 */
	public TWActivityData getTWActivityInfo() {
		String key = TWACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		TWActivityData activityInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			activityInfo = new TWActivityData();
		} else {
			activityInfo = JSON.parseObject(dataStr, TWActivityData.class);
		}
		StatisManager.getInstance().incRedisKey(TWACTIVITY_SERVER_INFO);
		return activityInfo;
	}
	
	/**
	 * 更新泰伯利亚之战战斗状态信息
	 * 
	 * @param fightInfo
	 * @return
	 */
	public boolean updateTWFightInfo(TWFightState fightInfo) {
		String jsonString = JSON.toJSONString(fightInfo);
		String key = TWACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(TWACTIVITY_FIGHT_STATE);
		return true;
	}
	
	/**
	 * 获取泰伯利亚之战战斗状态信息
	 * 
	 * @return
	 */
	public TWFightState getTWFightInfo() {
		String key = TWACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		TWFightState fightInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			fightInfo = new TWFightState();
		} else {
			fightInfo = JSON.parseObject(dataStr, TWFightState.class);
		}
		StatisManager.getInstance().incRedisKey(TWACTIVITY_FIGHT_STATE);
		return fightInfo;
	}
	
	/**
	 * 添加泰伯利亚报名信息
	 * @param activityInfo
	 * @return
	 */
	public boolean addTWSignInfo(String guildId, int termId, int index) {
		String key = TWACTIVITY_SIGN_GUILD + ":" + termId + ":" + index;
		redisSession.lPush(key, 30 * 86400, guildId);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_SIGN_GUILD);
		return true;
	}
	
	/**
	 * 移除报名的联盟
	 * @param guildId
	 * @param termId
	 * @param index
	 * @return
	 */
	public boolean removeTWSignInfo(String guildId, int termId, int index){
		String key = TWACTIVITY_SIGN_GUILD + ":" + termId + ":" + index;
		redisSession.lRem(key, 0, guildId);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_SIGN_GUILD);
		return true;
	}
	
	
	/**
	 * 获取泰伯利亚报名信息
	 * @param activityInfo
	 * @return
	 */
	public List<String> getTWSignInfo(int termId, int index) {
		String key = TWACTIVITY_SIGN_GUILD + ":" + termId + ":" + index;
		List<String> result = redisSession.lRange(key, 0, -1, 0);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_SIGN_GUILD);
		return result;
	}
	
	/**
	 * 更新参战玩家列表
	 * @param guildId
	 * @param idList
	 * @return
	 */
	public boolean updateTWPlayerIds(String guildId, int type, String targetId) {
		String key = TWACTIVITY_JOIN_PLAYER + ":" + guildId;
		if(type == 1){
			redisSession.sAdd(key, 0, targetId);
		}
		else{
			redisSession.sRem(key, targetId);
		}
		StatisManager.getInstance().incRedisKey(TWACTIVITY_JOIN_PLAYER);
		return true;
	}
	
	/**
	 * 从参战斗列表中删除指定玩家
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public boolean removeTWPlayerId(String guildId, String playerId) {
		String key = TWACTIVITY_JOIN_PLAYER + ":" + guildId;
		redisSession.sRem(key, playerId);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_JOIN_PLAYER);
		return true;
	}
	
	/**
	 * 从参战斗列表中删除指定玩家
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public boolean removeTWPlayerIds(String guildId) {
		String key = TWACTIVITY_JOIN_PLAYER + ":" + guildId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_JOIN_PLAYER);
		return true;
	}
	
	/**
	 * 获取泰伯利亚参战玩家列表
	 * @param activityInfo
	 * @return
	 */
	public Set<String> getTWPlayerIds(String guildId) {
		String key = TWACTIVITY_JOIN_PLAYER + ":" + guildId;
		Set<String> result= redisSession.sMembers(key);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_JOIN_PLAYER);
		return result;
	}
	
	
	/**
	 * 更新泰伯利亚参战玩家信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTWPlayerData(TWPlayerData twPlayerData, int termId) {
		int expireSeconds = 24 * 3600 * 30;
		if(termId > TiberiumConst.SEASON_OFFSET){
			expireSeconds = 24 * 3600 * 120;
		}
		String jsonString = JSON.toJSONString(twPlayerData);
		String key = TWACTIVITY_PLAYER_INFO + ":" + termId + ":" + twPlayerData.getId();
		redisSession.setString(key, jsonString, expireSeconds);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_PLAYER_INFO);
		return true;
	}

	/**
	 * 获取泰伯利亚参战玩家信息
	 * 
	 * @return
	 */
	public TWPlayerData getTWPlayerData(String playerId, int termId) {
		String key = TWACTIVITY_PLAYER_INFO + ":" + termId + ":" + playerId;
		String dataStr = redisSession.getString(key);
		TWPlayerData twPlayerData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			twPlayerData = JSON.parseObject(dataStr, TWPlayerData.class);
		}
		StatisManager.getInstance().incRedisKey(TWACTIVITY_PLAYER_INFO);
		return twPlayerData;
	}
	
	public List<TWPlayerData> getAllTWPlayerData(String guildId, int termId) {
		try (Jedis jedis = redisSession.getJedis()) {
			Set<String> members = getTWPlayerIds(guildId);
			String[] keys = (String[]) members.stream().map(playerId -> TWACTIVITY_PLAYER_INFO + ":" + termId + ":" + playerId).toArray();
			List<String> mstrs = jedis.mget(keys);
			List<TWPlayerData> result = new ArrayList<>(members.size());
			for (String dataStr : mstrs) {
				result.add(JSON.parseObject(dataStr, TWPlayerData.class));
			}
			return result;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Collections.emptyList();
	}
	
	/**
	 * 更新泰伯利亚参与联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTWGuildData(TWGuildData twGuildData, int termId) {
		int expireSeconds = 24 * 3600 * 30;
		if(termId > TiberiumConst.SEASON_OFFSET){
			expireSeconds = 24 * 3600 * 120;
		}
		String jsonString = JSON.toJSONString(twGuildData);
		String key = TWACTIVITY_GUILD_INFO + ":" + termId;
		redisSession.hSet(key, twGuildData.getId(), jsonString, expireSeconds);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_INFO);
		return true;
	}
	
	/**
	 * 批量更新泰伯利亚参与联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTWGuildData(List<TWGuildData> twGuildDatas, int termId) {
		int expireSeconds = 24 * 3600 * 30;
		if(termId > TiberiumConst.SEASON_OFFSET){
			expireSeconds = 24 * 3600 * 120;
		}
		if (twGuildDatas == null || twGuildDatas.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (TWGuildData data : twGuildDatas) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = TWACTIVITY_GUILD_INFO + ":" + termId;
		redisSession.hmSet(key, dataMap, expireSeconds);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_INFO);
		return true;
	}

	/**
	 * 获取泰伯利亚参与联盟信息
	 * 
	 * @return
	 */
	public TWGuildData getTWGuildData(String guildId, int termId) {
		String key = TWACTIVITY_GUILD_INFO + ":" + termId;
		String dataStr = redisSession.hGet(key, guildId);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_INFO);
		TWGuildData guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, TWGuildData.class);
		}
		return guildData;
	}
	
	/**
	 * 获取泰伯利亚所有参与联盟信息
	 * @return
	 */
	public Map<String, TWGuildData> getAllTWGuildData(int termId) {
		String key = TWACTIVITY_GUILD_INFO + ":" + termId;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		Map<String, TWGuildData> dataMap = new HashMap<>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			TWGuildData guildData = JSON.parseObject(dataStr, TWGuildData.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_INFO);
		return dataMap;
	}
	
	/**
	 * 更新泰伯利亚房间信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTWRoomData(TWRoomData twRoomData, int termId) {
		int expireSeconds = 24 * 3600 * 30;
		if(termId > TiberiumConst.SEASON_OFFSET){
			expireSeconds = 24 * 3600 * 120;
		}
		String key = TWACTIVITY_ROOM_INFO + ":" + termId;
		redisSession.hSet(key, twRoomData.getId(), JSON.toJSONString(twRoomData), expireSeconds);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_ROOM_INFO);
		return true;
	}
	
	/**
	 * 批量更新泰伯利亚匹配房间信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTWRoomData(List<TWRoomData> twRoomDatas, int termId) {
		int expireSeconds = 24 * 3600 * 30;
		if(termId > TiberiumConst.SEASON_OFFSET){
			expireSeconds = 24 * 3600 * 120;
		}
		Map<String, String> dataMap = new HashMap<>();
		for(TWRoomData data : twRoomDatas){
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		if(dataMap.isEmpty()){
			return false;
		}
		String key = TWACTIVITY_ROOM_INFO + ":" + termId;
		redisSession.hmSet(key, dataMap, expireSeconds);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_ROOM_INFO);
		return true;
	}
	
	/**
	 * 删除泰伯利亚匹配房间信息
	 * @param activityInfo
	 * @return
	 */
	public boolean removeTWRoomData(int termId) {
		String key = TWACTIVITY_ROOM_INFO + ":" + termId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_ROOM_INFO);
		return true;
	}

	/**
	 * 获取泰伯利亚匹配房间信息
	 * 
	 * @return
	 */
	public TWRoomData getTWRoomData(String roomId, int termId) {
		String key = TWACTIVITY_ROOM_INFO + ":" + termId;
		String dataStr = redisSession.hGet(key, roomId);
		TWRoomData guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, TWRoomData.class);
		}
		StatisManager.getInstance().incRedisKey(TWACTIVITY_ROOM_INFO);
		return guildData;
	}
	
	/**
	 * 获取泰伯利亚匹配房间信息
	 * 
	 * @return
	 */
	public List<TWRoomData> getAllTWRoomData(int termId) {
		String key = TWACTIVITY_ROOM_INFO + ":" + termId;
		Map<String, String> dataMap = redisSession.hGetAll(key);
		List<TWRoomData> list = new ArrayList<>();
		for (Entry<String, String> entry : dataMap.entrySet()) {
			String dataStr = entry.getValue();
			if (!HawkOSOperator.isEmptyString(dataStr)) {
				TWRoomData roomData = JSON.parseObject(dataStr, TWRoomData.class);
				list.add(roomData);
			}
		}
		StatisManager.getInstance().incRedisKey(TWACTIVITY_ROOM_INFO);
		return list;
	}
	
	/**
	 * 记录泰伯利亚之战战报
	 * @param battleLog
	 * @param guildId
	 */
	public void addTWBattleLog(TWBattleLog battleLog, String guildId) {
		String key = TWACTIVITY_BATTLE_HISTORY + ":" + guildId;
		redisSession.lPush(key.getBytes(), 10 * 7 * 24 * 3600, battleLog.toByteArray());
		StatisManager.getInstance().incRedisKey(TWACTIVITY_BATTLE_HISTORY);
	}
	
	/**
	 * 获取泰伯利亚之战历史战报
	 * @param roomId
	 * @return
	 */
	public List<TWBattleLog> getTWBattleLog(String guildId,int size) {
		String key = TWACTIVITY_BATTLE_HISTORY + ":" + guildId;
		List<byte[]> result = redisSession.lRange(key.getBytes(), 0, size, 10 * 7 * 24 * 3600);
		if (result == null) {
			return Collections.emptyList();
		}
		
		List<TWBattleLog> list = new ArrayList<>();
		for(byte[] bytes : result){
			try {
				TWBattleLog.Builder builder = TWBattleLog.newBuilder();
				builder.mergeFrom(bytes);
				list.add(builder.build());
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
				continue;
			}
		}
		StatisManager.getInstance().incRedisKey(TWACTIVITY_BATTLE_HISTORY);
		return list;
	}
	
	/**
	 * 移除泰伯利亚联盟历史战斗记录
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public boolean removeTWBattleLog(String guildId) {
		String key = TWACTIVITY_BATTLE_HISTORY + ":" + guildId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_BATTLE_HISTORY);
		return true;
	}
	
	/**
	 * 更新泰伯利亚联盟小组信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTWGuildTeamData(TWGuildTeamData twGuildTeamData) {
		if (twGuildTeamData == null) {
			return false;
		}
		String guildId = twGuildTeamData.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			return false;
		}
		String jsonString = JSON.toJSONString(twGuildTeamData);
		String key = TWACTIVITY_GUILD_TEAM + ":" + guildId;
		redisSession.setString(key, jsonString, 24 * 3600 * 30);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_TEAM);
		return true;
	}
	
	/**
	 * 获取联盟小组信息
	 * @param guildId
	 * @return
	 */
	public TWGuildTeamData getTWGuildTeamData(String guildId) {
		if(HawkOSOperator.isEmptyString(guildId)){
			return null;
		}
		String key = TWACTIVITY_GUILD_TEAM + ":" + guildId;
		String dataStr = redisSession.getString(key);
		if(HawkOSOperator.isEmptyString(dataStr)){
			return null;
		}
		TWGuildTeamData guildTeamData = JSON.parseObject(dataStr, TWGuildTeamData.class);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_TEAM);
		return guildTeamData;
	}
	
	/**
	 * 移除联盟小组信息
	 * @param guildId
	 * @return
	 */
	public void removeTWGuildTeamData(String guildId) {
		if(HawkOSOperator.isEmptyString(guildId)){
			return ;
		}
		String key = TWACTIVITY_GUILD_TEAM + ":" + guildId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_TEAM);
	}
	
	/**
	 * 匹配及战斗阶段获取泰伯利亚联盟小组构建信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public TWGuildTeamInfo getTWGuildTeamBuild(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return null;
		}
		String key = TWACTIVITY_GUILD_TEAM_BUILD + ":" + guildId;
		byte[] result = redisSession.getBytes(key.getBytes());
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_TEAM_BUILD);
		TWGuildTeamInfo.Builder builder = TWGuildTeamInfo.newBuilder();
		if (result != null && result.length > 0) {
			try {
				builder.mergeFrom(result);
				return builder.build();
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * 更新泰伯利亚联盟小组构建信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTWGuildTeamBuild(String guildId, TWGuildTeamInfo guildTeamInfo) {
		if (guildTeamInfo == null) {
			return false;
		}
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		String key = TWACTIVITY_GUILD_TEAM_BUILD + ":" + guildId;
		redisSession.setBytes(key, guildTeamInfo.toByteArray(), 24 * 3600 * 7);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_TEAM_BUILD);
		return true;
	}
	
	/**
	 * 更新限时商店当天内打开次数
	 * 
	 * @param playerId
	 * 
	 * @param openTimes
	 */
	public void updateTimeLimitStoreDayOpenTimes(String playerId, int openTimes) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_BOUGHT);
		String key = TIMELIMIT_STORE_BOUGHT + ":" + playerId + ":" + HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), HawkTime.FORMAT_YMD);
		redisSession.setString(key, String.valueOf(openTimes), GsConst.DAY_SECONDS);
	}
	
	/**
	 * 限时商店当天内打开次数
	 * 
	 * @param playerId
	 */
	public int getTimeLimitStoreDayOpenTimes(String playerId) {
		StatisManager.getInstance().incRedisKey(TIMELIMIT_STORE_BOUGHT);
		String key = TIMELIMIT_STORE_BOUGHT + ":" + playerId + ":" + HawkTime.formatTime(HawkApp.getInstance().getCurrentTime(), HawkTime.FORMAT_YMD);
		String openTimeStr = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(openTimeStr)) {
			return Integer.valueOf(openTimeStr);
		}
		
		return 0;
	}

	/**
	 * 获取要塞占领信息
	 */
	public Map<String, FortressOccupyItem> getAllFortressOccupyInfo(Integer serverGroup) {
		StatisManager.getInstance().incRedisKey(FORTRESS_OCCUPY_INFO);
		
		Map<String, FortressOccupyItem> retInfo = new ConcurrentHashMap<>();
		
		String key = FORTRESS_OCCUPY_INFO + ":" + serverGroup;
		Map<String, String> occupyInfo = redisSession.hGetAll(key, GsConst.FORTRESS_OCCUPY_EXPIRE);
		if (occupyInfo == null || occupyInfo.isEmpty()) {
			return retInfo;
		}
		
		for (Entry<String, String> info : occupyInfo.entrySet()) {
			retInfo.put(info.getKey(), JSONObject.parseObject(info.getValue(), FortressOccupyItem.class));
		}
		return retInfo;
	}

	/**
	 * 更新要塞占领信息
	 */
	public void updateFortressOccupyInfo(Integer serverGroup, String fortressId, FortressOccupyItem occupyInfo) {
		StatisManager.getInstance().incRedisKey(FORTRESS_OCCUPY_INFO);
		String key = FORTRESS_OCCUPY_INFO + ":" + serverGroup;
		redisSession.hSet(key, fortressId, JSONObject.toJSONString(occupyInfo), GsConst.FORTRESS_OCCUPY_EXPIRE);
	}
	
	/**
	 * 清除要塞占领信息
	 */
	public void deleteFortressOccupyInfo(Integer serverGroup) {
		StatisManager.getInstance().incRedisKey(FORTRESS_OCCUPY_INFO);
		String key = FORTRESS_OCCUPY_INFO + ":" + serverGroup;
		redisSession.del(key);
	}
	
	/**
	 * 获取所有航海之星信息
	 */
	public Map<String, Integer> getAllFortressStar() {
		StatisManager.getInstance().incRedisKey(FORTRESS_STAR);
		
		Map<String, Integer> retMap = new ConcurrentHashMap<>();
		
		Map<String, String> starMap = redisSession.hGetAll(FORTRESS_STAR);
		if (starMap == null || starMap.isEmpty()) {
			return retMap;
		}
		
		for (Entry<String, String> star : starMap.entrySet()) {
			retMap.put(star.getKey(), Integer.valueOf(star.getValue()));
		}
		return retMap;
	}

	public int getStarAndConquerTerm(String serverId){
		String termStr = redisSession.hGet(CROSS_REFRESH_TERM,serverId);
		if(HawkOSOperator.isEmptyString(termStr)){
			return 1;
		}
		return Integer.parseInt(termStr);
	}

	public void updateStarAndConquerTerm(String serverId, int termId){
		redisSession.hSet(CROSS_REFRESH_TERM, serverId, String.valueOf(termId));
	}

	public void clearFortressStar() {
		StatisManager.getInstance().incRedisKey(FORTRESS_STAR);
		redisSession.del(FORTRESS_STAR);
	}

	public boolean backupAllFortressStar(int termId) {
		Map<String, String> starMap = redisSession.hGetAll(FORTRESS_STAR);
		if (starMap != null && !starMap.isEmpty()) {
			redisSession.hmSet(FORTRESS_STAR_BAK+":"+termId, starMap, 0);
		}
		return true;
	}

	public Map<String, Integer> getAllFortressStarBak(int termId) {
		StatisManager.getInstance().incRedisKey(FORTRESS_STAR_BAK);

		Map<String, Integer> retMap = new ConcurrentHashMap<>();

		Map<String, String> starMap = redisSession.hGetAll(FORTRESS_STAR_BAK+":"+termId);
		if (starMap == null || starMap.isEmpty()) {
			return retMap;
		}

		for (Entry<String, String> star : starMap.entrySet()) {
			retMap.put(star.getKey(), Integer.valueOf(star.getValue()));
		}
		return retMap;
	}
	
	/**
	 * 增加征服次数
	 */
	public void updateCrossConquerRank() {
		String serverId = GsConfig.getInstance().getServerId();
		redisSession.hIncrBy(CROSS_CONQUER_RANK, serverId, 1);
	}
	
	/**
	 * 获取跨服征服排行榜
	 */
	public Map<String, Integer> getCrossConquerRank() {
		Map<String, Integer> retMap = new ConcurrentHashMap<>();
		
		Map<String, String> rankMap = redisSession.hGetAll(CROSS_CONQUER_RANK);
		if (rankMap == null || rankMap.isEmpty()) {
			return retMap;
		}
		
		for (Entry<String, String> star : rankMap.entrySet()) {
			retMap.put(star.getKey(), Integer.valueOf(star.getValue()));
		}
		return retMap;
	}
	
	/**
	 * 增加航海之星数量
	 */
	public void increasaFortressStar(int count) {
		StatisManager.getInstance().incRedisKey(FORTRESS_STAR);
		String serverId = GsConfig.getInstance().getServerId();
		redisSession.hIncrBy(FORTRESS_STAR, serverId, count);
	}

	public void clearCrossConquerRank() {
		StatisManager.getInstance().incRedisKey(CROSS_CONQUER_RANK);
		redisSession.del(CROSS_CONQUER_RANK);
	}

	public boolean backupCrossConquerRank(int termId) {
		Map<String, String> rankMap = redisSession.hGetAll(CROSS_CONQUER_RANK);
		if (rankMap != null && !rankMap.isEmpty()) {
			redisSession.hmSet(CROSS_CONQUER_RANK_BAK+":"+termId, rankMap, 0);
		}
		return true;
	}

	/**
	 * 获取跨服征服排行榜
	 */
	public Map<String, Integer> getCrossConquerRankBak(int termId) {
		Map<String, Integer> retMap = new ConcurrentHashMap<>();

		Map<String, String> rankMap = redisSession.hGetAll(CROSS_CONQUER_RANK_BAK+":"+termId);
		if (rankMap == null || rankMap.isEmpty()) {
			return retMap;
		}

		for (Entry<String, String> star : rankMap.entrySet()) {
			retMap.put(star.getKey(), Integer.valueOf(star.getValue()));
		}
		return retMap;
	}
	
	/**
	 * 获取玩家的跨服类型.
	 * @param serverId
	 * @param playerId
	 * @param crossType
	 * @param expireTime
	 * @return
	 */
	public boolean setPlayerCrossType(String serverId, String playerId, int crossType) {
		String key = CROSS_TYPE + ":" + serverId;
		StatisManager.getInstance().incRedisKey(CROSS_TYPE);
		//两天
		int expireTime = 2 * 86400;
		
		return redisSession.hSet(key, playerId, crossType + "", expireTime) > 0;	
	}
	
	/**
	 * 获取玩家的跨服类型.
	 * @param playerId
	 * @return
	 */
	public int getPlayerCrossType(String fromServerId, String playerId) {
		String key = CROSS_TYPE + ":" + fromServerId;
		StatisManager.getInstance().incRedisKey(CROSS_TYPE);
		try (Jedis jedis = redisSession.getJedis()) {
			String crossType = jedis.hget(key, playerId);
			if (!HawkOSOperator.isEmptyString(crossType)) { 
				return Integer.parseInt(crossType);
			}				
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return -1;
	}
	
	/**
	 * 更新城防燃烧速度
	 * 
	 * @param playerId
	 * @param speed
	 * @param expireSecond
	 */
	public void updateWallFireSpeed(String playerId, double speed, int expireSecond) {
		StatisManager.getInstance().incRedisKey(WALL_FIRE_SPEED);
		String key = WALL_FIRE_SPEED + ":" + playerId;
		redisSession.setString(key, String.valueOf(speed), expireSecond);
	}
	
	/**
	 * 移除城防燃烧速度数据
	 * 
	 * @param playerId
	 */
	public void removeWallFireSpeed(String playerId) {
		StatisManager.getInstance().incRedisKey(WALL_FIRE_SPEED);
		String key = WALL_FIRE_SPEED + ":" + playerId;
		redisSession.del(key);
	}
	
	/**
	 * 获取城防燃烧速度
	 * 
	 * @param playerId
	 * @return
	 */
	public double getWallFireSpeed(String playerId) {
		StatisManager.getInstance().incRedisKey(WALL_FIRE_SPEED);
		String key = WALL_FIRE_SPEED + ":" + playerId;
		String speed = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(speed)) {
			return Double.valueOf(speed);
		}
		return 0D;
	}

	
	/**
	 * 获取玩家赠送装扮记录
	 * 
	 * @param playerId
	 * @return
	 * PLAYER_DRESS_SEND_LOG
	 */
	public List<PlayerDressPlayerInfo> getPlayerAllDressSendInfo(String playerId) {
		List<PlayerDressPlayerInfo> retList = new ArrayList<PlayerDressPlayerInfo>();
		try {
			String key = String.format("%s:%s",  PLAYER_DRESS_SEND_LOG, playerId);
			List<String> ret = redisSession.lRange(key, 0, -1, 0);
			for( String jsonStr : ret ){
				PlayerDressPlayerInfo.Builder builder = PlayerDressPlayerInfo.newBuilder();
				JsonFormat.merge(jsonStr, builder);
				retList.add(builder.build());
			}
		} catch (ParseException e) {
			HawkException.catchException(e);
		}
		return retList;
	}
	
	/**
	 * 添加玩家赠送装扮记录
	 * 
	 * @param playerId
	 * @param info
	 * @param max
	 * @return
	 * PLAYER_DRESS_SEND_LOG
	 */
	public void addPlayerAllDressSendInfo(String playerId, PlayerDressPlayerInfo info, int max) {
		try{
			String key = String.format("%s:%s", PLAYER_DRESS_SEND_LOG, playerId);
			String value = JsonFormat.printToString(info);
			redisSession.lPush(key, 0, value);
			redisSession.lTrim(key, 0, max);							
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取玩家请求赠送装扮记录
	 * 
	 * @param playerId
	 * @return
	 * PLAYER_DRESS_ASK_LOG
	 */
	public List<PlayerDressPlayerInfo> getPlayerAllDressAskInfo(String playerId) {
		List<PlayerDressPlayerInfo> retList = new ArrayList<PlayerDressPlayerInfo>();
		try {
			String key = String.format("%s:%s", PLAYER_DRESS_ASK_LOG, playerId);
			List<String> ret = redisSession.lRange(key, 0, -1, 0);
			for( String jsonStr : ret ){
				PlayerDressPlayerInfo.Builder builder = PlayerDressPlayerInfo.newBuilder();
				JsonFormat.merge(jsonStr, builder);
				retList.add(builder.build());
			}
		} catch (ParseException e) {
			HawkException.catchException(e);
		}
		return retList;
	}
	
	/**
	 * 添加玩家请求赠送装扮记录
	 * 
	 * @param playerId
	 * @param info
	 * @param max
	 * @return
	 * PLAYER_DRESS_ASK_LOG
	 */
	public void addPlayerAllDressAskInfo(String playerId, PlayerDressPlayerInfo info, int max) {
		try{
			String key = String.format("%s:%s", PLAYER_DRESS_ASK_LOG, playerId);
			String value = JsonFormat.printToString(info);
			redisSession.lPush(key, 0, value);
			redisSession.lTrim(key, 0, max);				
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 设置玩家的uid:openid 
	 */
	public void setPlayerSpreadInfo(String playerId, String openId, String serverId){
		try{
			PBSpreadBindRoleInfo.Builder builder = PBSpreadBindRoleInfo.newBuilder();
			builder.setPlayerId(playerId);
			builder.setOpenId(openId);
			builder.setServerId(serverId);
			String value = JsonFormat.printToString(builder.build());
			String key = String.format("%s:%s", PLAYER_UID_OPENID_PREFIX, playerId);
			redisSession.setString(key, value);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 根据玩家id获取openid
	 * @return 
	 */
	public PBSpreadBindRoleInfo getPlayerSpreadInfo(String playerId){
		try{
			String key = String.format("%s:%s", PLAYER_UID_OPENID_PREFIX, playerId);
			String value = redisSession.getString(key);
			if(!HawkOSOperator.isEmptyString(value)){
				PBSpreadBindRoleInfo.Builder builder = PBSpreadBindRoleInfo.newBuilder();
				JsonFormat.merge(value, builder);
				return builder.build();			
			}
		}catch(Exception e){
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 设置openid 绑定 推广码
	 * @param openId
	 */
	public void setSpreadOpenidBindFlag(String openId) {
		try{
			String key = String.format("%s:%s", SPREAD_OPENID_BIND_FLAG, openId);
			redisSession.setString(key,"1");	
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取openid 是否绑定过推广码
	 * @param openId
	 * @return
	 */
	public boolean getIsSpreadOpenidBindFlag(String openId) {
		try{
			String key = String.format("%s:%s", SPREAD_OPENID_BIND_FLAG, openId);
			return !HawkOSOperator.isEmptyString(redisSession.getString(key));
		}catch(Exception e){
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 活动模板8 腾讯url活动分享
	 * @param isShared 今日是否已经分享过
	 * @param isReward 今日是否已经领取
	 */
	
	public void setPlayerUrlModelEightActivityInfo(String playerId, boolean isShared, boolean isReward){
		try{
			String key = String.format("%s:%s", ACTIVITY_MODEL_8_PREFIX, playerId);
			
			PBEmptyModel8Info.Builder builder = PBEmptyModel8Info.newBuilder();
			builder.setIsReward(isReward);
			builder.setIsShared(isShared);
			String value = JsonFormat.printToString(builder.build());
			redisSession.setString(key, value);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 活动模板8 腾讯url活动分享 获取是否领取过
	 * @return
	 */
	public PBEmptyModel8Info getPlayerUrlModelEightActivityInfo(String playerId){
		try{
			String key = String.format("%s:%s", ACTIVITY_MODEL_8_PREFIX, playerId);
			String ret = redisSession.getString(key);
			PBEmptyModel8Info.Builder builder = PBEmptyModel8Info.newBuilder();
			JsonFormat.merge(ret, builder);
			return builder.build();
		}catch(Exception e){
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 活动模板9 腾讯url活动分享
	 * @param isShared 今日是否已经分享过
	 * @param isReward 今日是否已经领取
	 */
	
	public void setPlayerUrlModelNineActivityInfo(String playerId, boolean isShared, boolean isReward){
		try{
			String key = String.format("%s:%s", ACTIVITY_MODEL_9_PREFIX, playerId);
			
			PBEmptyModel9Info.Builder builder = PBEmptyModel9Info.newBuilder();
			builder.setIsReward(isReward);
			builder.setIsShared(isShared);
			String value = JsonFormat.printToString(builder.build());
			redisSession.setString(key, value);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 活动模板9 腾讯url活动分享 获取是否领取过
	 * @return
	 */
	public PBEmptyModel9Info getPlayerUrlModelNineActivityInfo(String playerId){
		try{
			String key = String.format("%s:%s", ACTIVITY_MODEL_9_PREFIX, playerId);
			String ret = redisSession.getString(key);
			PBEmptyModel9Info.Builder builder = PBEmptyModel9Info.newBuilder();
			JsonFormat.merge(ret, builder);
			return builder.build();
		}catch(Exception e){
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 活动模板10 腾讯url活动分享
	 * @param isShared 今日是否已经分享过
	 * @param isReward 今日是否已经领取
	 */
	
	public void setPlayerUrlModelTenActivityInfo(String playerId, boolean isShared, boolean isReward){
		try{
			String key = String.format("%s:%s", ACTIVITY_MODEL_10_PREFIX, playerId);
			
			PBEmptyModel10Info.Builder builder = PBEmptyModel10Info.newBuilder();
			builder.setIsReward(isReward);
			builder.setIsShared(isShared);
			String value = JsonFormat.printToString(builder.build());
			redisSession.setString(key, value);
		}catch(Exception e){
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 活动模板10 腾讯url活动分享 获取是否领取过
	 * @return
	 */
	public PBEmptyModel10Info getPlayerUrlModelTenActivityInfo(String playerId){
		try{
			String key = String.format("%s:%s", ACTIVITY_MODEL_10_PREFIX, playerId);
			String ret = redisSession.getString(key);
			PBEmptyModel10Info.Builder builder = PBEmptyModel10Info.newBuilder();
			JsonFormat.merge(ret, builder);
			return builder.build();
		}catch(Exception e){
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 获取铠甲套装名字
	 */
	public Map<Integer, String> getArmourSuitName(String playerId) {
		Map<Integer, String> retMap = new HashMap<>();
		
		StatisManager.getInstance().incRedisKey(ARMOUR_SUIT_NAME);
		String key = ARMOUR_SUIT_NAME + ":" + playerId;
		
		Map<String, String> suitName = redisSession.hGetAll(key);
		if (suitName != null) {
			for (Entry<String, String> name : suitName.entrySet()) {
				retMap.put(Integer.valueOf(name.getKey()), name.getValue());
			}
		}
		return retMap;
	}

	/**
	 * 设置铠甲套装名字
	 */
	public void setArmourSuitName(String playerId, int suit, String name) {
		StatisManager.getInstance().incRedisKey(ARMOUR_SUIT_NAME);
		String key = ARMOUR_SUIT_NAME + ":" + playerId;
		redisSession.hSet(key, String.valueOf(suit), name);
	}
	
	/**
	 * 更新福袋分享的时间
	 */
	public void updateBlessBagShareTime(String playerId, long time) {
		String key = BLESS_BAG_SHARE + ":" + playerId;
		redisSession.setString(key, String.valueOf(time), GsConst.DAY_SECONDS);
	}
	
	/**
	 * 获取福袋分享的时间
	 * 
	 * @return
	 */
	public long getBlessBagShareTime(String playerId) {
		String key = BLESS_BAG_SHARE + ":" + playerId;
		String time = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(time)) {
			return Long.valueOf(time);
		}
		
		return 0;
	}
	
	/**
	 * 更新锦标赛活动信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateGCActivityInfo(GCActivityData activityInfo) {
		String jsonString = JSON.toJSONString(activityInfo);
		String key = GCACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(GCACTIVITY_SERVER_INFO);
		return true;
	}

	/**
	 * 获取锦标赛活动信息
	 * 
	 * @return
	 */
	public GCActivityData getGCActivityData() {
		String key = GCACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		GCActivityData activityInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			activityInfo = new GCActivityData();
		} else {
			activityInfo = JSON.parseObject(dataStr, GCActivityData.class);
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_SERVER_INFO);
		return activityInfo;
	}
	
	/**
	 * 更新锦标赛战斗状态信息
	 * 
	 * @param fightInfo
	 * @return
	 */
	public boolean updateGCWarInfo(GCWarState warStateInfo) {
		String jsonString = JSON.toJSONString(warStateInfo);
		String key = GCACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(GCACTIVITY_FIGHT_STATE);
		return true;
	}
	
	/**
	 * 获取泰伯利亚之战战斗状态信息
	 * 
	 * @return
	 */
	public GCWarState getGCWarInfo() {
		String key = GCACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		GCWarState warStateInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			warStateInfo = new GCWarState();
		} else {
			warStateInfo = JSON.parseObject(dataStr, GCWarState.class);
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_FIGHT_STATE);
		return warStateInfo;
	}
	
	
	/**
	 * 更新锦标赛参战玩家
	 * @param termId
	 * @param guildId
	 * @param playerId
	 * @param battlePoint
	 * @return
	 */
	public boolean updateGCPlayerId(int termId, String guildId, String playerId, long battlePoint) {
		String key = GCACTIVITY_SIGN_PLAYER + ":" + termId + ":" + guildId;
		redisSession.zAdd(key, battlePoint, playerId, 604800);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_SIGN_PLAYER);
		return true;
	}
	
	/**
	 * 从锦标赛参战斗列表中删除指定玩家
	 * @param termId
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public boolean removeGCPlayerId(int termId, String guildId, String playerId) {
		String key = GCACTIVITY_SIGN_PLAYER + ":" + termId + ":" + guildId;
		redisSession.zRem(key, 0, playerId);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_SIGN_PLAYER);
		return true;
	}
	
	/**
	 * 从锦标赛参战斗列表中移除指定联盟
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public boolean removeGCPlayerIds(int termId, String guildId) {
		String key = GCACTIVITY_SIGN_PLAYER + ":" + termId + ":" + guildId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_SIGN_PLAYER);
		return true;
	}
	
	/**
	 * 获取锦标赛报名玩家列表
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public Set<String> getGCPlayerIds(int termId, String guildId) {
		String key = GCACTIVITY_SIGN_PLAYER + ":" + termId + ":" + guildId;
		Set<String> result= redisSession.zRevRange(key, 0, -1, 0);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_SIGN_PLAYER);
		return result;
	}
	
	/**
	 * 获取锦标赛报名玩家id及战力
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public Set<Tuple> getGCPlayerIdAndPower(int termId, String guildId){
		String key = GCACTIVITY_SIGN_PLAYER + ":" + termId + ":" + guildId;
		Set<Tuple> result =redisSession.zRevrangeWithScores(key, 0, -1, 0);
		return result;
	}
	
	/**
	 * 获取锦标赛报名玩家列表(指定数量)
	 * @param termId
	 * @param guildId
	 * @param count
	 * @return
	 */
	public Set<String> getGCPlayerIds(int termId, String guildId, int count) {
		String key = GCACTIVITY_SIGN_PLAYER + ":" + termId + ":" + guildId;
		Set<String> result = redisSession.zRevRange(key, 0, count - 1, 0);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_SIGN_PLAYER);
		return result;
	}
	
	/**
	 * 更新锦标赛联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateGCGuildData(GCGuildData gcGuildData) {
		String jsonString = JSON.toJSONString(gcGuildData);
		String key = GCACTIVITY_GUILD_INFO;
		redisSession.hSet(key, gcGuildData.getId(), jsonString, 24 * 3600 * 30);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GUILD_INFO);
		return true;
	}
	
	/**
	 * 批量更新锦标赛联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateGCGuildData(List<GCGuildData> gcGuildDatas) {
		if (gcGuildDatas == null || gcGuildDatas.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (GCGuildData data : gcGuildDatas) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = GCACTIVITY_GUILD_INFO;
		redisSession.hmSet(key, dataMap, 0);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GUILD_INFO);
		return true;
	}
	
	/**
	 * 移除锦标赛联盟信息
	 * 
	 * @param guildId
	 * @return
	 */
	public boolean removeGCGuildData(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		String key = GCACTIVITY_GUILD_INFO;
		redisSession.hDel(key, guildId);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GUILD_INFO);
		return true;
	}
	
	

	/**
	 * 获取锦标赛联盟信息
	 * 
	 * @return
	 */
	public GCGuildData getGCGuildData(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return null;
		}
		String key = GCACTIVITY_GUILD_INFO;
		String dataStr = redisSession.hGet(key, guildId);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GUILD_INFO);
		GCGuildData guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, GCGuildData.class);
		}
		return guildData;
	}
	
	/**
	 * 获取锦标赛所有联盟信息
	 * @return
	 */
	public Map<String, GCGuildData> getAllGCGuildData() {
		String key = GCACTIVITY_GUILD_INFO;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		Map<String, GCGuildData> dataMap = new HashMap<>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			GCGuildData guildData = JSON.parseObject(dataStr, GCGuildData.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GUILD_INFO);
		return dataMap;
	}
	
	/**
	 * 获取锦标赛指定联盟信息
	 * @return
	 */
	public Map<String, GCGuildData> getGCGuildsData(List<String> guildIds) {
		Map<String, GCGuildData> dataMap = new HashMap<>();
		if (guildIds == null || guildIds.isEmpty()) {
			return dataMap;
		}
		String key = GCACTIVITY_GUILD_INFO;
		List<String> result = redisSession.hmGet(key, guildIds.toArray(new String[guildIds.size()]));
		if (result == null || result.isEmpty()) {
			return dataMap;
		}
		for (String dataStr : result) {
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			GCGuildData guildData = JSON.parseObject(dataStr, GCGuildData.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GUILD_INFO);
		return dataMap;
	}
	
	/**
	 * 更新锦标赛参战玩家信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateGCPlayerData(int termId, GCPlayerInfo playerInfo) {
		if (playerInfo == null) {
			return false;
		}
		String key = GCACTIVITY_PLAYER_INFO + ":" + termId + ":" + playerInfo.getId();
		redisSession.setBytes(key, playerInfo.toByteArray(), 24 * 3600 * 30);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_PLAYER_INFO);
		return true;
	}
	/**
	 * 获取锦标赛参战玩家信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public GCPlayerInfo getGCPlayerData(int termId, String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}
		String key = GCACTIVITY_PLAYER_INFO + ":" + termId + ":" + playerId;
		byte[] result = redisSession.getBytes(key.getBytes());
		if(result == null || result.length == 0){
			return null;
		}
		GCPlayerInfo.Builder builder = GCPlayerInfo.newBuilder();
		try {
			builder.mergeFrom(result);
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
			return null;
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_PLAYER_INFO);
		return builder.build();
	}
	
	
	/**
	 * 批量存入锦标赛玩家领奖信息
	 * @param termId
	 * @param guildId
	 * @param playerIds
	 * @return
	 */
	public boolean updateGCRewardInfo(int termId, String guildId, Map<String, String> infoMap) {
		if (infoMap == null || infoMap.isEmpty()) {
			return false;
		}
		String key = GCACTIVITY_REWARD_PLAYER + ":" + termId + "" + guildId;
		redisSession.hmSet(key, infoMap, 1209600);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_REWARD_PLAYER);
		return true;
	}
	
	/**
	 * 获取玩家锦标赛领奖信息
	 * @param termId
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public String getGCRewardInfo(int termId, String guildId, String playerId) {
		String key = GCACTIVITY_REWARD_PLAYER + ":" + termId + "" + guildId;
		String result = redisSession.hGet(key, playerId);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_REWARD_PLAYER);
		return result;
	}
	
	/**
	 * 更新玩家锦标赛领奖信息
	 * @param termId
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public void updateGCRewardInfo(int termId, String guildId, String playerId, String value) {
		String key = GCACTIVITY_REWARD_PLAYER + ":" + termId + "" + guildId;
		redisSession.hSet(key, playerId, value);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_REWARD_PLAYER);
	}
	
	
	/**
	 * 更新锦标赛参战玩家
	 * @param termId
	 * @param guildId
	 * @param playerIds
	 * @return
	 */
	public boolean updateGCJoinPlayerInfos(int termId, String guildId, List<GCPlayerInfo> playerInfos) {
		if (playerInfos == null || playerInfos.isEmpty()) {
			return false;
		}
		String key = GCACTIVITY_JOIN_PLAYER + ":" + termId + ":" + guildId;
		byte[][] infoBytes = new byte[playerInfos.size()][];
		for (int i = 0; i < playerInfos.size(); i++) {
			infoBytes[i] = playerInfos.get(i).toByteArray();
		}
		redisSession.lPush(key.getBytes(), 604800, infoBytes);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_JOIN_PLAYER);
		return true;
	}
	
	/**
	 * 获取锦标赛参战玩家列表
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public List<GCPlayerInfo.Builder> getGCJoinPlayerIds(int termId, String guildId) {
		String key = GCACTIVITY_JOIN_PLAYER + ":" + termId + ":" + guildId;
		List<GCPlayerInfo.Builder> infoList = new ArrayList<>();
		List<byte[]> values = redisSession.lRange(key.getBytes(), 0, -1, 0);
		if (values == null || values.isEmpty()) {
			return infoList;
		}
		for (byte[] value : values) {
			try {
				GCPlayerInfo.Builder builder = GCPlayerInfo.newBuilder();
				builder.mergeFrom(value);
				infoList.add(builder);
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_JOIN_PLAYER);
		return infoList;
	}
	
	/**
	 * 添加锦标赛参战联盟
	 * @param termId
	 * @param guildId
	 * @param playerIds
	 * @return
	 */
	public boolean addGCJoinGuildId(int termId, String guildId, GCGuildGrade grade) {
		String key = GCACTIVITY_JOIN_GUILD + ":" + termId;
		redisSession.hSet(key, guildId, String.valueOf(grade));
		StatisManager.getInstance().incRedisKey(GCACTIVITY_JOIN_GUILD);
		return true;
	}
	
	/**
	 * 获取锦标赛参战联盟集合
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public Map<String, String> getGCJoinGuildIds(int termId) {
		String key = GCACTIVITY_JOIN_GUILD + ":" + termId;
		Map<String, String> result = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_JOIN_GUILD);
		return result;
	}
	
	/**
	 * 更新更新锦标赛小组信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateGCGroupData(int termId, GCGroupData gcGroupData) {
		String key = GCACTIVITY_GROUP_INFO + ":" + termId;
		redisSession.hSet(key, gcGroupData.getId(), JSON.toJSONString(gcGroupData));
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GROUP_INFO);
		return true;
	}
	
	/**
	 * 批量更新锦标赛小组信息
	 * @param termId
	 * @param guildId
	 * @param playerIds
	 * @return
	 */
	public boolean updateGCGroupData(int termId, List<GCGroupData> gcGroupDatas) {
		
		Map<String, String> dataMap = new HashMap<>();
		for(GCGroupData data : gcGroupDatas){
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		if(dataMap.isEmpty()){
			return false;
		}
		String key = GCACTIVITY_GROUP_INFO + ":" + termId;
		redisSession.hmSet(key, dataMap, 604800);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_JOIN_GUILD);
		return true;
	}
	
	/**
	 * 删除锦标赛小组信息
	 * @param activityInfo
	 * @return
	 */
	public boolean removeGCGroupData(int termId) {
		String key = GCACTIVITY_GROUP_INFO + ":" + termId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GROUP_INFO);
		return true;
	}
	
	/**
	 * 获取锦标赛小组信息
	 * 
	 * @return
	 */
	public GCGroupData getGCGroupData(String groupId, int termId) {
		String key = GCACTIVITY_GROUP_INFO + ":" + termId;
		String dataStr = redisSession.hGet(key, groupId);
		GCGroupData guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, GCGroupData.class);
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GROUP_INFO);
		return guildData;
	}
	
	/**
	 * 获取锦标赛所有小组信息
	 * 
	 * @return
	 */
	public List<GCGroupData> getAllGCGroupData(int termId) {
		String key = GCACTIVITY_GROUP_INFO + ":" + termId;
		Map<String, String> dataMap = redisSession.hGetAll(key);
		List<GCGroupData> list = new ArrayList<>();
		for (Entry<String, String> entry : dataMap.entrySet()) {
			String dataStr = entry.getValue();
			if (!HawkOSOperator.isEmptyString(dataStr)) {
				GCGroupData roomData = JSON.parseObject(dataStr, GCGroupData.class);
				list.add(roomData);
			}
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GROUP_INFO);
		return list;
	}
	
	/**
	 * 更新锦标赛玩家出战编队
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateGCPbattleData(int termId, String playerId, PBChampionPlayer.Builder builder) {
		if (builder == null) {
			return false;
		}
		String key = GCACTIVITY_PLAYER_BATTLE_INFO + ":" + termId + ":" + playerId;
		redisSession.setBytes(key, builder.build().toByteArray(), 1209600);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_PLAYER_BATTLE_INFO);
		return true;
	}
	
	/**
	 * 获取锦标赛玩家出战编队
	 * 
	 * @param activityInfo
	 * @return
	 */
	public PBChampionPlayer.Builder getGCPbattleData(int termId, String playerId) {
		String key = GCACTIVITY_PLAYER_BATTLE_INFO + ":" + termId + ":" + playerId;
		byte[] contentbytes = redisSession.getBytes(key.getBytes());
		if(contentbytes == null){
			return null;
		}
		PBChampionPlayer.Builder builder = null;
		try {
			builder = PBChampionPlayer.newBuilder().mergeFrom(contentbytes);
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_PLAYER_BATTLE_INFO);
		return builder;
	}
	
	/**
	 * 移除锦标赛玩家出战编队
	 * 
	 * @param activityInfo
	 * @return
	 */
	public void removeGCPbattleData(int termId, String playerId) {
		String key = GCACTIVITY_PLAYER_BATTLE_INFO + ":" + termId + ":" + playerId;
		redisSession.del(key.getBytes());
		StatisManager.getInstance().incRedisKey(GCACTIVITY_PLAYER_BATTLE_INFO);
	}
	
	
	
	/**
	 * 更新更新锦标赛联盟对战数据
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateGCGuildBattle(int termId, GCGuildBattle.Builder builder) {
		String gBattleId = builder.getGBattleId();
		String key = GCACTIVITY_GUILD_BATTLE_INFO + ":" + termId;
		redisSession.hSetBytes(key.getBytes(), gBattleId.getBytes(), builder.build().toByteArray(), 1209600);
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GROUP_INFO);
		return true;
	}
	
	/**
	 * 获取锦标赛联盟战斗数据
	 * 
	 * @param activityInfo
	 * @return
	 */
	public GCGuildBattle.Builder getGCGuildBattle(int termId, String gBattleId) {
		GCGuildBattle.Builder builder = GCGuildBattle.newBuilder();
		String key = GCACTIVITY_GUILD_BATTLE_INFO + ":" + termId;
		byte[] result = redisSession.hGetBytes(key, gBattleId);
		if (result == null) {
			return null;
		}
		try {
			builder.mergeFrom(result);
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
			return null;
		}
		StatisManager.getInstance().incRedisKey(GCACTIVITY_GROUP_INFO);
		return builder;
	}
	
	/**
	 * 更新锦标赛榜单数据
	 * @param key
	 * @param scoreMap
	 */
	public void updateGCRankInfo(String key, Map<String, Double> scoreMap) {
		if (HawkOSOperator.isEmptyString(key)) {
			return;
		}
		if (scoreMap == null || scoreMap.isEmpty()) {
			return;
		}
		redisSession.zAdd(key, scoreMap, 1209600);
		StatisManager.getInstance().incRedisKey(key);
	}
	
	/**
	 * 获取锦标赛榜单数据
	 * @param key
	 * @param scoreMap
	 */
	public Set<Tuple> getGCRankInfo(String key, int size) {
		Set<Tuple> result = new HashSet<>();
		if (HawkOSOperator.isEmptyString(key)) {
			return result;
		}
		result = redisSession.zRevrangeWithScores(key, 0, size - 1, 0);
		StatisManager.getInstance().incRedisKey(key);
		return result;
	}
	
	/**
	 * 获取锦标赛榜单分数
	 * @param key
	 * @param scoreMap
	 */
	public long getGCRankInfo(String key, String member) {
		Double result = redisSession.zScore(key, member, 0);
		StatisManager.getInstance().incRedisKey(key);
		return (long) (result == null ? 0 : result);
	}
	
	/**
	 * 更新修改内容的时间
	 * 
	 * @param objectId 玩家ID或联盟ID
	 * @param type 修改内容的类型
	 * @param time 修改时间
	 */
	public void updateChangeContentTime(String objectId, ChangeContentType type, long time) {
		String key = CHANGE_CONTENT_TIME + ":" + objectId;
		redisSession.hSet(key, String.valueOf(type.intVal()), String.valueOf(time));
	}
	
	/**
	 * 获取修改内容的时间
	 * 
	 * @param objectId 玩家ID或联盟ID
	 * @param type 修改内容的类型
	 * @return
	 */
	public long getChangeContentTime(String objectId, ChangeContentType type) {
		String key = CHANGE_CONTENT_TIME + ":" + objectId;
		String time = redisSession.hGet(key, String.valueOf(type.intVal()));
		if (!HawkOSOperator.isEmptyString(time)) {
			return Long.valueOf(time);
		}
		
		return 0;
	}
	
	/**
	 * 更新修改内容的cd时长
	 * 
	 * @param objectId 玩家ID或联盟ID
	 * @param type 修改内容的类型
	 * @param seconds cd时长
	 */
	public void updateChangeContentCDTime(String objectId, ChangeContentType type, int seconds) {
		String key = CHANGE_CONTENT_TIME_CD + ":" + objectId;
		redisSession.hSet(key, String.valueOf(type.intVal()), String.valueOf(seconds));
	}
	
	/**
	 * 获取修改内容的cd时长
	 * 
	 * @param objectId 玩家ID或联盟ID
	 * @param type 修改内容的类型
	 * @return
	 */
	public int getChangeContentCDTime(String objectId, ChangeContentType type) {
		String key = CHANGE_CONTENT_TIME_CD + ":" + objectId;
		String seconds = redisSession.hGet(key, String.valueOf(type.intVal()));
		if (!HawkOSOperator.isEmptyString(seconds)) {
			return Integer.valueOf(seconds);
		}
		
		return 0;
	}
	
	/**
	 * 移除修改内容的cd时长
	 * @param objectId 玩家ID或联盟ID
	 * @param type 修改内容的类型
	 * 
	 */
	public void removeChangeContentCDTime(String objectId, ChangeContentType type) {
		String key = CHANGE_CONTENT_TIME_CD + ":" + objectId;
		redisSession.hDel(key, String.valueOf(type.intVal()));
	}

	/**
	 * 更新db信息
	 */
	public void updateDBInfo(String gameId, String areaId, String serverId, String dbinfo) {
		String key = DB_INFO + ":" + gameId + ":" + areaId;
		redisSession.hSet(key, serverId, dbinfo);
	}
	
	/**
	 * 更新redis信息
	 */
	public void updateRedisInfo(String gameId, String areaId, String serverId, String info) {
		String key = REDIS_INFO + ":" + gameId + ":" + areaId;
		redisSession.hSet(key, serverId, info);
	}
	
	/**
	 * 更新log信息
	 */
	public void updateLogInfo(String gameId, String areaId, String serverId, String info) {
		String key = LOG_INFO + ":" + gameId + ":" + areaId;
		redisSession.hSet(key, serverId, info);
	}
	
	public void incPlayerHelped(String playerId) {
		final int dayOfYear = HawkTime.getYearDay();
		String key = TODAY_HOSPICE + playerId + dayOfYear;
		redisSession.increaseBy(key, 1, 24 * 60 * 60);
	}

	public int todayHelped(String playerId) {
		final int dayOfYear = HawkTime.getYearDay();
		String key = TODAY_HOSPICE + playerId + dayOfYear;
		String val = redisSession.getString(key);
		return NumberUtils.toInt(val);
	}
	
	/**本次超能试验室激活*/
	public void saveSuperLabJiHuo(String playerId, String labId){
		String key =  SUPER_LAB_JIHUO + playerId;
		redisSession.zAdd(key, HawkTime.getMillisecond(), labId);
	}
	
	public void delSuperLabJiHuo(String playerId, String labId) {
		String key = SUPER_LAB_JIHUO + playerId;
		redisSession.zRem(key, 0, labId);
	}
	
	public List<String> superLabJiHuoHis(String playerId) {
		String key = SUPER_LAB_JIHUO + playerId;
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				return new ArrayList<>(jedis.zrevrange(key, 0, 3));
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				jedis.close();
			}
		}
		return Collections.emptyList();
	}
	
	/**
	 * 玩家跨服的时候把守护信息写入到跨服。
	 * @param toServerId
	 * @param playerId
	 * @param tuple
	 */
	public void addCrossGuard(String toServerId, String playerId, HawkTuple3<String, Integer, Integer> tuple, int expireTime) {
		String jsonString = JSON.toJSONString(tuple);
		StatisManager.getInstance().incRedisKey(CROSS_GUARD);
		String key = CROSS_GUARD + ":" + toServerId;
		redisSession.hSet(key, playerId, jsonString, expireTime);
	}
	
	/**
	 * 获取玩家的守护信息.
	 * @param toServerId
	 * @param playerId
	 * @return
	 */
	public HawkTuple3<String, Integer, Integer> getCrossGuard(String toServerId, String playerId) {
		StatisManager.getInstance().incRedisKey(CROSS_GUARD);
		String key = CROSS_GUARD + ":" + toServerId;
		try {
			String redisData = redisSession.hGet(key, playerId);
			if (!HawkOSOperator.isEmptyString(redisData)) {
				@SuppressWarnings("unchecked")
				HawkTuple3<String, Integer, Integer> data = JSON.parseObject(redisData, new HawkTuple3<String, Integer, Integer>(null, null, null).getClass());
				
				return data;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return null;
	}
	
	/**
	 * 加载所有的跨服守护关系.
	 * @param toServerId
	 * @return
	 */
	public Map<String, HawkTuple3<String, Integer, Integer>> loadCrossGuard(String toServerId) {
		StatisManager.getInstance().incRedisKey(CROSS_GUARD);
		String key = CROSS_GUARD + ":" + toServerId;
		Map<String, String> redisMap = redisSession.hGetAll(key);
		if (redisMap == null) {
			return new HashMap<>();
		}
		
		Map<String, HawkTuple3<String, Integer, Integer>> tupleMap = new HashMap<>();
		for (Entry<String, String> redisEntry : redisMap.entrySet()) {
			try {
				@SuppressWarnings("unchecked")
				HawkTuple3<String, Integer, Integer> tupleData = JSON.parseObject(redisEntry.getValue(), 
						new HawkTuple3<String, Integer, Integer>(null, null, null).getClass());
				tupleMap.put(redisEntry.getKey(), tupleData);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		return tupleMap;
	}
	
	/**
	 * 删除好友守护.
	 * @param toServerId
	 * @param playerId
	 */
	public void deleteCrossGuard(String toServerId, String playerId) {
		StatisManager.getInstance().incRedisKey(CROSS_GUARD);
		String key = CROSS_GUARD + ":" + toServerId;
		redisSession.hDel(key, playerId);
	}
	
	/**
	 * 更新泰伯利亚联赛联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTLWGuildData(TLWGuildData tlwGuildData, int season) {
		String jsonString = JSON.toJSONString(tlwGuildData);
		String key = TLWACTIVITY_GUILD_INFO + ":" + season;
		redisSession.hSet(key, tlwGuildData.getId(), jsonString, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_INFO);
		return true;
	}
	
	/**
	 * 删除指定联盟泰伯利亚联赛联盟信息
	 * @param guildId
	 * @param season
	 * @return
	 */
	public boolean removeTLWGuildData(String guildId, int season) {
		String key = TLWACTIVITY_GUILD_INFO + ":" + season;
		redisSession.hDel(key, guildId);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_INFO);
		return true;
	}
	
	/**
	 * 批量更新泰伯利亚联赛联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTLWGuildData(List<TLWGuildData> tlwGuildDatas, int season) {
		if (tlwGuildDatas == null || tlwGuildDatas.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (TLWGuildData data : tlwGuildDatas) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = TLWACTIVITY_GUILD_INFO + ":" + season;
		redisSession.hmSet(key, dataMap, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_INFO);
		return true;
	}

	/**
	 * 获取泰伯利亚联赛联盟信息
	 * 
	 * @return
	 */
	public TLWGuildData getTLWGuildData(String guildId, int season) {
		String key = TLWACTIVITY_GUILD_INFO + ":" + season;
		String dataStr = redisSession.hGet(key, guildId);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_INFO);
		TLWGuildData guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, TLWGuildData.class);
		}
		return guildData;
	}
	
	/**
	 * 获取泰伯利亚联赛所有联盟信息
	 * @return
	 */
	public Map<String, TLWGuildData> getAllTLWGuildData(int season) {
		String key = TLWACTIVITY_GUILD_INFO + ":" + season;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		Map<String, TLWGuildData> dataMap = new HashMap<>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			TLWGuildData guildData = JSON.parseObject(dataStr, TLWGuildData.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_INFO);
		return dataMap;
	}
	
	/**
	 * 获取泰伯利亚联赛所有联盟信息
	 * @return
	 */
	public Map<String, TLWGuildData> getTLWGuildDatas(int season, List<String> guildIds) {
		String key = TLWACTIVITY_GUILD_INFO + ":" + season;
		if(guildIds.isEmpty()){
			return new HashMap<>();
		}
		List<String> results = redisSession.hmGet(key, guildIds.toArray(new String[guildIds.size()]));
		if(results == null || results.isEmpty()){
			return new HashMap<>();
		}
		Map<String, TLWGuildData> dataMap = new HashMap<>();
		for (String dataStr : results) {
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			TLWGuildData guildData = JSON.parseObject(dataStr, TLWGuildData.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_INFO);
		return dataMap;
	}
	
	/**
	 * 刷新泰伯利亚联赛联盟战力排名
	 * @param season
	 * @param guildId
	 * @param score
	 * @return
	 */
	public boolean addTLWGuildPowerRank(int season, String guildId, long score) {
		String key = TLWACTIVITY_GUILD_POWER_RANK + ":" + season;
		redisSession.zAdd(key, score, guildId, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_POWER_RANK);
		return true;
	}
	/**
	 * 批量刷新泰伯利亚联赛联盟战力排名
	 * @param season
	 * @param members
	 * @return
	 */
	public boolean addTLWGuildPowerRanks(int season, Map<String, Double> members) {
		String key = TLWACTIVITY_GUILD_POWER_RANK + ":" + season;
		redisSession.zAdd(key, members, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_POWER_RANK);
		return true;
	}
	
	/**
	 * 获取泰伯利亚联赛联盟战力排名
	 * @param season
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<Tuple> getTLWGuildPowerRanks(int season, long start, long end) {
		String key = TLWACTIVITY_GUILD_POWER_RANK + ":" + season;
		Set<Tuple> result = redisSession.zRevrangeWithScores(key, start, end, 0);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_POWER_RANK);
		return result;
	}
	
	/**
	 * 删除联盟的泰伯利亚联赛排名
	 * @param season
	 * @param guildId
	 */
	public void removeTLWGuildPowerRank(int season, String guildId){
		String key = TLWACTIVITY_GUILD_POWER_RANK + ":" + season;
		redisSession.zRem(key, 0, guildId);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_POWER_RANK);
	}


	/**
	 * 刷新泰伯利亚联赛联盟战力排名
	 * @param season
	 * @param guildId
	 * @param score
	 * @return
	 */
	public boolean addTLWGuildPowerRankNew(int season, String guildId, long score) {
		String key = TLWACTIVITY_GUILD_POWER_RANK + ":NEW:" + season;
		redisSession.zAdd(key, score, guildId, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_POWER_RANK);
		return true;
	}
	/**
	 * 批量刷新泰伯利亚联赛联盟战力排名
	 * @param season
	 * @param members
	 * @return
	 */
	public boolean addTLWGuildPowerRanksNew(int season, Map<String, Double> members) {
		String key = TLWACTIVITY_GUILD_POWER_RANK + ":NEW:" + season;
		redisSession.zAdd(key, members, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_POWER_RANK);
		return true;
	}

	/**
	 * 获取泰伯利亚联赛联盟战力排名
	 * @param season
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<Tuple> getTLWGuildPowerRanksNew(int season, long start, long end) {
		String key = TLWACTIVITY_GUILD_POWER_RANK + ":NEW:"+ season;
		Set<Tuple> result = redisSession.zRevrangeWithScores(key, start, end, 0);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_POWER_RANK);
		return result;
	}

	/**
	 * 删除联盟的泰伯利亚联赛排名
	 * @param season
	 * @param guildId
	 */
	public void removeTLWGuildPowerRankNew(int season, String guildId){
		String key = TLWACTIVITY_GUILD_POWER_RANK + ":NEW:" + season;
		redisSession.zRem(key, 0, guildId);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_POWER_RANK);
	}


	/**
	 * 新服报名老服泰伯联赛
	 * @param season
	 * @param guildId
	 * @param score
	 * @return
	 */
	public boolean addTLWNewSignupOld(int season, String guildId) {
		String key = TLW_NEW_SIGNUP_OLD + ":" + season;
		redisSession.sAdd(key, TiberiumConst.TLW_EXPIRE_SECONDS, guildId);
		StatisManager.getInstance().incRedisKey(TLW_NEW_SIGNUP_OLD);
		return true;
	}

	public Set<String> getTLWNewSignupOld(int season){
		String key = TLW_NEW_SIGNUP_OLD + ":" + season;
		Set<String> set = redisSession.sMembers(key);
		StatisManager.getInstance().incRedisKey(TLW_NEW_SIGNUP_OLD);
		return set;
	}

	/**
	 * 单服新服报名老服泰伯联赛
	 * @param season
	 * @param guildId
	 * @param score
	 * @return
	 */
	public boolean addTLWNewSignupOldServer(int season, String guildId) {
		String key = TLW_NEW_SIGNUP_OLD_SERVER + ":" + season + ":" + GsConfig.getInstance().getServerId();
		redisSession.sAdd(key, TiberiumConst.TLW_EXPIRE_SECONDS, guildId);
		StatisManager.getInstance().incRedisKey(TLW_NEW_SIGNUP_OLD_SERVER);
		return true;
	}

	public Set<String> getTLWNewSignupOldServer(int season){
		String key = TLW_NEW_SIGNUP_OLD_SERVER + ":" + season + ":" + GsConfig.getInstance().getServerId();
		Set<String> set = redisSession.sMembers(key);
		StatisManager.getInstance().incRedisKey(TLW_NEW_SIGNUP_OLD);
		return set;
	}
	
	/**
	 * 更新泰伯利亚联赛活动信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTLWActivityInfo(TLWActivityData activityInfo) {
		String jsonString = JSON.toJSONString(activityInfo);
		String key = TLWACTIVITY_INFO + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_INFO);
		return true;
	}

	/**
	 * 获取泰伯利亚联赛活动信息
	 * 
	 * @return
	 */
	public TLWActivityData getTLWActivityInfo() {
		String key = TLWACTIVITY_INFO + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		TLWActivityData activityInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			activityInfo = new TLWActivityData();
		} else {
			activityInfo = JSON.parseObject(dataStr, TLWActivityData.class);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_INFO);
		return activityInfo;
	}
	
	/**
	 * 更新泰伯利亚联赛战斗状态信息
	 * 
	 * @param fightInfo
	 * @return
	 */
	public boolean updateTLWFightInfo(TLWFightState fightInfo) {
		String jsonString = JSON.toJSONString(fightInfo);
		String key = TLWACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_FIGHT_STATE);
		return true;
	}
	
	/**
	 * 获取泰伯利亚联赛战斗状态信息
	 * 
	 * @return
	 */
	public TLWFightState getTLWFightInfo() {
		String key = TLWACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		TLWFightState fightInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			fightInfo = new TLWFightState();
		} else {
			fightInfo = JSON.parseObject(dataStr, TLWFightState.class);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_FIGHT_STATE);
		return fightInfo;
	}
	
	/**
	 * 更新泰伯利亚联赛出战联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTLWJoinGuild(TLWGuildJoinInfo joinInfo, int season) {
		String jsonString = JSON.toJSONString(joinInfo);
		String key = TLWACTIVITY_JOIN_GUILD + ":" + season;
		redisSession.hSet(key, joinInfo.getId(), jsonString, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_JOIN_GUILD);
		return true;
	}
	
	
	/**
	 * 删除泰伯利亚联赛出战联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean removeTLWJoinGuild(int season) {
		String key = TLWACTIVITY_JOIN_GUILD + ":" + season;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_JOIN_GUILD);
		return true;
	}
	
	/**
	 * 批量更新泰伯利亚联赛出战联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTLWJoinGuild(List<TLWGuildJoinInfo> joinInfos, int season) {
		if (joinInfos == null || joinInfos.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (TLWGuildJoinInfo data : joinInfos) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = TLWACTIVITY_JOIN_GUILD + ":" + season;
		redisSession.hmSet(key, dataMap, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_JOIN_GUILD);
		return true;
	}

	/**
	 * 获取泰伯利亚联赛出战联盟信息
	 * 
	 * @return
	 */
	public TLWGuildJoinInfo getTLWJoinGuild(int season, String guildId) {
		String key = TLWACTIVITY_JOIN_GUILD + ":" + season;
		String dataStr = redisSession.hGet(key, guildId);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_JOIN_GUILD);
		TLWGuildJoinInfo guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, TLWGuildJoinInfo.class);
		}
		return guildData;
	}
	
	/**
	 * 获取泰伯利亚联赛所有联盟信息
	 * @return
	 */
	public Map<String, TLWGuildJoinInfo> getAllTLWJoinGuild(int season) {
		String key = TLWACTIVITY_JOIN_GUILD + ":" + season;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		Map<String, TLWGuildJoinInfo> dataMap = new HashMap<>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			TLWGuildJoinInfo guildData = JSON.parseObject(dataStr, TLWGuildJoinInfo.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_JOIN_GUILD);
		return dataMap;
	}

	public Map<String, TLWGuildJoinInfo> getAllTLWJoinGuild(int season, int serverType) {
		String key = TLWACTIVITY_JOIN_GUILD + ":" + season;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		Map<String, TLWGuildJoinInfo> dataMap = new HashMap<>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			TLWGuildJoinInfo guildData = JSON.parseObject(dataStr, TLWGuildJoinInfo.class);
			if(guildData.getServerType() != serverType){
				continue;
			}
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_JOIN_GUILD);
		return dataMap;
	}
	
	/**
	 * 获取泰伯利亚联赛决赛赛程信息
	 * 
	 * @return
	 */
	public TLWFinalGroup getTLWFinalGroupInfo(int season, TLWGroupType groupType) {
		String key = TLWACTIVITY_FINAL_GROUP + ":" + season + ":" + groupType.getNumber();
		String dataStr = redisSession.getString(key);
		TLWFinalGroup groupData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			groupData = JSON.parseObject(dataStr, TLWFinalGroup.class);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_FINAL_GROUP);
		return groupData;
	}
	
	
	public TLWEliminationGroup getTLWEliminationGroupInfo(int season, TLWGroupType groupType) {
		String key = TLWACTIVITY_FINAL_GROUP + ":" + season + ":" + groupType.getNumber();
		String dataStr = redisSession.getString(key);
		TLWEliminationGroup groupData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			groupData = JSON.parseObject(dataStr, TLWEliminationGroup.class);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_FINAL_GROUP);
		return groupData;
	}
	
	/**
	 * 更新泰伯利亚联赛决赛赛程信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTLWFinalGroupInfo(int season, TLWFinalGroup tlwFinalGroup) {
		String key = TLWACTIVITY_FINAL_GROUP + ":" + season + ":" + tlwFinalGroup.getGroupType().getNumber();
		redisSession.setString(key, JSON.toJSONString(tlwFinalGroup), TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_FINAL_GROUP);
		return true;
	}
	
	public boolean updateTLWEliminationGroupInfo(int season, TLWEliminationGroup tlwFinalGroup) {
		String key = TLWACTIVITY_FINAL_GROUP + ":" + season + ":" + tlwFinalGroup.getGroupType().getNumber();
		redisSession.setString(key, JSON.toJSONString(tlwFinalGroup), TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_FINAL_GROUP);
		return true;
	}

	/**
	 * 获取泰伯利亚联赛联盟积分奖励信息
	 * 
	 * @return
	 */
	public TLWScoreData getTLWGuildScoreInfo(int season, String guildId) {
		String key = TLWACTIVITY_GUILD_SCORE_INFO + ":" + season + ":" + guildId;
		String dataStr = redisSession.getString(key);
		TLWScoreData groupData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			groupData = new TLWScoreData();
			groupData.setId(guildId);
		} else {
			groupData = JSON.parseObject(dataStr, TLWScoreData.class);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_SCORE_INFO);
		return groupData;
	}

	/**
	 * 更新泰伯利亚联赛联盟积分信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTLWGuildScoreInfo(int season, TLWScoreData tlwScoreData) {
		if (tlwScoreData == null) {
			return false;
		}
		String key = TLWACTIVITY_GUILD_SCORE_INFO + ":" + season + ":" + tlwScoreData.getId();
		redisSession.setString(key, JSON.toJSONString(tlwScoreData), TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_SCORE_INFO);
		return true;
	}
	/**
	 * 获取泰伯利亚联赛玩家积分奖励信息
	 * 
	 * @return
	 */
	public TLWScoreData getTLWPlayerScoreInfo(int season, String playerId) {
		String key = TLWACTIVITY_PLAYER_SCORE_INFO + ":" + season + ":" + playerId;
		String dataStr = redisSession.getString(key);
		TLWScoreData groupData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			groupData = new TLWScoreData();
			groupData.setId(playerId);
		} else {
			groupData = JSON.parseObject(dataStr, TLWScoreData.class);
		}
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_PLAYER_SCORE_INFO);
		return groupData;
	}
	
	/**
	 * 更新泰伯利亚联赛玩家积分信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTLWPlayerScoreInfo(int season, TLWScoreData tlwScoreData) {
		if (tlwScoreData == null) {
			return false;
		}
		String key = TLWACTIVITY_PLAYER_SCORE_INFO + ":" + season + ":" + tlwScoreData.getId();
		redisSession.setString(key, JSON.toJSONString(tlwScoreData), TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_PLAYER_SCORE_INFO);
		return true;
	}
	
	/**
	 * 批量存储泰伯利亚联赛入围正赛的联盟成员列表
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTLWMemberIds(String guildId, Map<String, String> dataMap, int season) {
		if (dataMap == null || dataMap.isEmpty()) {
			return true;
		}
		String key = TLWACTIVITY_MEMBERS + ":" + season + ":" + guildId;
		redisSession.hmSet(key, dataMap, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_MEMBERS);
		return true;
	}
	
	/**
	 *获取泰伯利亚联赛入围正赛的联盟成员列表
	 * @param season
	 */
	public Map<String, String> getTLWMemberIds(int season, String guildId){
		String key = TLWACTIVITY_MEMBERS + ":" + season + ":" + guildId;
		Map<String, String> memberIds = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_MEMBERS);
		return memberIds;
	}
	
	/**
	 * 刷新泰伯利亚联赛正赛联盟总排行信息
	 * @param rankInfo
	 * @param season
	 */
	public void updateTLWGuildTotalRank(Map<String, String> rankInfo, int season){
		String key = TLWACTIVITY_GUILD_TOTAL_RANK + ":" + season;
		redisSession.hmSet(key, rankInfo, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_TOTAL_RANK);
	}
	
	/**
	 *获取泰伯利亚联赛正赛联盟总排行信息
	 * @param season
	 */
	public Map<String, String> getTLWGuildTotalRank(int season) {
		String key = TLWACTIVITY_GUILD_TOTAL_RANK + ":" + season;
		Map<String, String> rankMap = redisSession.hGetAll(key);
		StatisManager.getInstance().incRedisKey(TLWACTIVITY_GUILD_TOTAL_RANK);
		return rankMap;
	}
	
	
	/**
	 * 更新泰伯利亚联盟ELO信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateTWGuildElo(TWGuildEloData eloData) {
		String jsonString = JSON.toJSONString(eloData);
		String key =  TWACTIVITY_GUILD_ELO_DATA;
		redisSession.hSet(key, eloData.getId(), jsonString);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_ELO_DATA);
		return true;
	}
	
	/**
	 * 批量更新泰伯利亚联盟ELO信息
	 * @param eloDatas
	 * @return
	 */
	public boolean updateTWGuildElo(List<TWGuildEloData> eloDatas) {
		if (eloDatas == null || eloDatas.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (TWGuildEloData data : eloDatas) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = TWACTIVITY_GUILD_ELO_DATA;
		redisSession.hmSet(key, dataMap, 0);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_ELO_DATA);
		return true;
	}

	/**
	 * 获取泰伯利亚参与联盟ELO信息
	 * 
	 * @return
	 */
	public TWGuildEloData getTWGuildElo(String guildId) {
		String key = TWACTIVITY_GUILD_ELO_DATA;
		String dataStr = redisSession.hGet(key, guildId);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_ELO_DATA);
		TWGuildEloData eloData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			eloData = JSON.parseObject(dataStr, TWGuildEloData.class);
		}
		return eloData;
	}
	
	/**
	 * 获取泰伯利亚所有联盟ELO信息
	 * @return
	 */
	public Map<String, TWGuildEloData> getAllEloData() {
		String key = TWACTIVITY_GUILD_ELO_DATA;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		Map<String, TWGuildEloData> dataMap = new HashMap<>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			TWGuildEloData eloData = JSON.parseObject(dataStr, TWGuildEloData.class);
			dataMap.put(eloData.getId(), eloData);
		}
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_ELO_DATA);
		return dataMap;
	}
	
	/**
	 * 删除指定联盟泰伯利亚ELO信息
	 * @param guildId
	 * @return
	 */
	public boolean removeTWGuildElo(String guildId) {
		String key = TWACTIVITY_GUILD_ELO_DATA;
		redisSession.hDel(key, guildId);
		StatisManager.getInstance().incRedisKey(TWACTIVITY_GUILD_ELO_DATA);
		return true;
	}
	
	
	/**
	 * 更新官职信息.
	 * @param part 
	 * @param playerId
	 * @param officerId
	 */
	public void updateStarWarsOfficer(int part, int team, StarWarsOfficerStruct starWarsOfficer,  int expireTime) {
		String key = new StringJoiner(":").add(STAR_WARS_OFFICER).add(part+"").toString();
		StatisManager.getInstance().incRedisKey(STAR_WARS_OFFICER);
		redisSession.hSetBytes(key, team+"", starWarsOfficer.toByteArray(), expireTime);
	}
	
	/**
	 * 加载当前期数分区的官职.
	 * @param termId
	 * @param part
	 */
	public Map<Integer, StarWarsOfficerStruct.Builder> getAllStarWarsOfficer(int part) {
		String key = new StringJoiner(":").add(STAR_WARS_OFFICER).add(part+"").toString();
		StatisManager.getInstance().incRedisKey(STAR_WARS_OFFICER);
		Map<byte[], byte[]> byteMap = redisSession.hGetAllBytes(key.getBytes());
		Map<Integer, StarWarsOfficerStruct.Builder> map = new HashMap<>();
		for (Entry<byte[],byte[]> byteEntry : byteMap.entrySet()) {
			try {
				StarWarsOfficerStruct.Builder structBuilder = StarWarsOfficerStruct.newBuilder().mergeFrom(byteEntry.getValue());
				map.put(Integer.parseInt(new String(byteEntry.getKey())), structBuilder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}			
		}
		
		return map;

	}
	
	/**
	 * 删除某个官职
	 * @param part
	 * @param officerId
	 */
	public void deleteStarWarsOfficer(int part, int teamNo) {
		String key = new StringJoiner(":").add(STAR_WARS_OFFICER).add(part+"").toString();
		StatisManager.getInstance().incRedisKey(STAR_WARS_OFFICER);
		redisSession.hDel(key, teamNo+"");
	}
	
	/**
	 * 删除分区的官职信息.
	 * @param part
	 */
	public void deleteStarWarsOfficer(int part) {
		String key = new StringJoiner(":").add(STAR_WARS_OFFICER).add(part+"").toString();
		StatisManager.getInstance().incRedisKey(STAR_WARS_OFFICER);
		redisSession.del(key);
	}
	
	/**
	 * 添加星球大战国王记录.
	 * @param termId
	 * @param part
	 * @param recordStruct
	 */
	public void addStarWarsKingRecord(int part, int team, StarWarsKingRecordStruct recordStruct) {		
		StatisManager.getInstance().incRedisKey(STAR_WARS_KING_RECORD);
		String key = new StringJoiner(":").add(STAR_WARS_KING_RECORD).add(String.valueOf(part)).add(String.valueOf(team)).toString();
		redisSession.lPush(key.getBytes(), 0, recordStruct.toByteArray());   
	}
	
	/**
	 * 获取星球大战国王记录.
	 * @param termId
	 * @param part
	 * @param maxCount
	 * @return
	 */
	public List<StarWarsKingRecordStruct> getStarWarsKingRecord(int part, int team, int maxCount) {
		StatisManager.getInstance().incRedisKey(STAR_WARS_KING_RECORD);
		String key = new StringJoiner(":").add(STAR_WARS_KING_RECORD).add(String.valueOf(part)).add(String.valueOf(team)).toString();
		List<StarWarsKingRecordStruct> builderList = new LinkedList<StarWarsKingRecordStruct>();
		try {
			List<byte[]> infoList = redisSession.lRange(key.getBytes(), 0, maxCount - 1, 0);
			if (infoList != null) {
				for (byte[] info : infoList) {					
					builderList.add(StarWarsKingRecordStruct.parseFrom(info));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return builderList;
	}
	
	/**
	 * 获取星球大战发奖信息.
	 * @param termId
	 * @param part
	 * @return
	 */
	public Map<Integer, Set<String>> getStarWarsGiftSendInfo(int part) {
		StatisManager.getInstance().incRedisKey(STAR_WARS_GIFT);
		String key = new StringJoiner(":").add(STAR_WARS_GIFT).add(part + "").toString();
		Map<Integer, Set<String>> rltMap = new HashMap<>();
		
		Map<String, String> redisMap = redisSession.hGetAll(key);
		for (Entry<String, String> entry : redisMap.entrySet()) {
			try {
				Set<String> playerSet = SerializeHelper.stringToSet(String.class, entry.getValue(), SerializeHelper.ATTRIBUTE_SPLIT);
				rltMap.put(Integer.parseInt(entry.getKey()), playerSet);
			} catch (Exception e) {
				HawkException.catchException(e);
			}						
		}
		
		return rltMap;
	}
	
	/**
	 * 更新某个礼包的发奖.
	 * @param termI
	 * @param part
	 * @param giftId
	 * @param playerSet
	 */
	public void updateStarWarsGiftSendInfo(int part, int giftId, Set<String> playerSet, int expireTime) {
		StatisManager.getInstance().incRedisKey(STAR_WARS_GIFT);
		String key = new StringJoiner(":").add(STAR_WARS_GIFT).add(part + "").toString();
		
		String playerSetString = SerializeHelper.collectionToString(playerSet, SerializeHelper.ATTRIBUTE_SPLIT);
		redisSession.hSet(key, giftId+"", playerSetString, expireTime);
	}
	
	/**
	 * 删除礼包的发送信息
	 * @param part
	 */
	public void deleteStarWarsGiftSendInfo(int part) {
		StatisManager.getInstance().incRedisKey(STAR_WARS_GIFT);
		String key = STAR_WARS_GIFT + ":" + part ;
		redisSession.del(key);
	}
	
	/**
	 * 添加星球大战记录礼包.
	 * @param termId
	 * @param part
	 * @param giftRrecordList
	 */
	public void addStarWarsGiftRecord(int part, List<StarWarsGiftRecordStruct> giftRrecordList) {
		StatisManager.getInstance().incRedisKey(STAR_WARS_GIFT_RECORD);
		String key = new StringJoiner(":").add(STAR_WARS_GIFT_RECORD).add(part+"").toString();
		byte[][] bytesArray = new byte[giftRrecordList.size()][];
		for (int i = 0; i < giftRrecordList.size(); i++) {
			bytesArray[i] = giftRrecordList.get(i).toByteArray();
		}
		redisSession.lPush(key.getBytes(), 0, bytesArray);
	}
	
	/**
	 * 获取星球大战礼包记录.
	 * @param termId
	 * @param part
	 * @param maxCount
	 * @return
	 */
	public List<StarWarsGiftRecordStruct> getStarWarsGiftRecord(int part, int maxCount) {
		StatisManager.getInstance().incRedisKey(STAR_WARS_GIFT_RECORD);
		String key = new StringJoiner(":").add(STAR_WARS_GIFT_RECORD).add(part + "").toString();
		List<StarWarsGiftRecordStruct> builderList = new LinkedList<StarWarsGiftRecordStruct>();
		try {
			List<byte[]> infoList = redisSession.lRange(key.getBytes(), 0, maxCount - 1, 0);
			if (infoList != null) {
				for (byte[] info : infoList) {								
					builderList.add(StarWarsGiftRecordStruct.parseFrom(info));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return builderList;
	}
	
	/**
	 * 删除礼包赠送记录.
	 * @param part
	 */
	public  void deleteStarWarsGiftRecord(int part) {
		StatisManager.getInstance().incRedisKey(STAR_WARS_GIFT_RECORD);
		String key = new StringJoiner(":").add(STAR_WARS_GIFT_RECORD).add(part + "").toString();
		redisSession.del(key);
	}
	
	/**
	 * 参与星球大战的联盟盟主信息
	 * @param serverId
	 * @param player
	 */
	public void addStarWarsJoinGuildLeader(String serverId, CrossPlayerStruct player) {
		StatisManager.getInstance().incRedisKey(STAR_WARS_JOIN_GUILD_LEADER);
		String key = new StringJoiner(":").add(STAR_WARS_JOIN_GUILD_LEADER).toString();
		redisSession.hSetBytes(key.getBytes(), serverId.getBytes(), player.toByteArray(), 0);
	}
	
	/**
	 * 参与星球大战的联盟盟主信息
	 * @param serverIds
	 * @return
	 */
	public List<CrossPlayerStruct> getStarWarsJoinGuildLeader(List<String> serverIds) {
		StatisManager.getInstance().incRedisKey(STAR_WARS_JOIN_GUILD_LEADER);
		String key = STAR_WARS_JOIN_GUILD_LEADER;
		Collection<byte[]> values = null;
		List<CrossPlayerStruct> playerStructList = new ArrayList<>();
		if (CollectionUtils.isEmpty(serverIds)) {
			Map<byte[], byte[]> map = redisSession.hGetAllBytes(key.getBytes());
			if (map != null) {
				values = map.values();
			}
		} else {
			byte[][] serverByteArray = new byte[serverIds.size()][];
			for (int i = 0; i < serverIds.size(); i ++) {
				serverByteArray[i] = serverIds.get(i).getBytes();
			}
			values = redisSession.hmGet(key.getBytes(), serverByteArray);
		}
		if (values != null) {
			for (byte[] bytes : values) {
				try {
					playerStructList.add(CrossPlayerStruct.newBuilder().mergeFrom(bytes).build());
				} catch (Exception e) {
					HawkException.catchException(e);
				}				
			}
		}
		
		return playerStructList;
	}
	
	/**
	 * 删除有参赛资格的联盟leader
	 */
	public void deleteStarWarsJoinGuildLeader() {
		StatisManager.getInstance().incRedisKey(STAR_WARS_JOIN_GUILD_LEADER);
		String key = STAR_WARS_JOIN_GUILD_LEADER;
		redisSession.del(key);
	} 
	
	
	
	/**
	 * 跨服玩家结构
	 * @param playerId
	 * @return
	 */
	public CrossPlayerStruct.Builder getCrossPlayerStruct(String playerId) {
		StatisManager.getInstance().incRedisKey(CROSS_PLAYER_STRUCT);
		String key = CROSS_PLAYER_STRUCT + ":" +playerId;
		try {
			byte[] bytes = redisSession.getBytes(key.getBytes());
			if (bytes != null) {
				CrossPlayerStruct.Builder crossBuilder = CrossPlayerStruct.newBuilder();
				crossBuilder.mergeFrom(bytes);
				
				return crossBuilder;
			}			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return null;
	}
	
	public Map<String, CrossPlayerStruct.Builder> getCrossPlayerStructs(Collection<String> playerIds) {
		Map<String, CrossPlayerStruct.Builder> map = new HashMap<>();
		if (playerIds.isEmpty()) {
			return map;
		}
		StatisManager.getInstance().incRedisKey(CROSS_PLAYER_STRUCT);
		
		try(Jedis jedis = redisSession.getJedis(); Pipeline pipeline = jedis.pipelined()) {
			
			List<Response<byte[]>> respList = new ArrayList<>();
			for(String playerId : playerIds) {
				String key = CROSS_PLAYER_STRUCT + ":" + playerId;
				respList.add(pipeline.get(key.getBytes()));
			}															
			
			pipeline.sync();
			for (Response<byte[]> resp : respList) {
				if (resp.get() == null) {
					continue;
				}
				CrossPlayerStruct.Builder crossBuilder = CrossPlayerStruct.newBuilder();
				crossBuilder.mergeFrom(resp.get());
				map.put(crossBuilder.getPlayerId(), crossBuilder);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return map;
	}
	
	/**
	 * 更新跨服玩家结构.
	 * @param playerId
	 * @param struct
	 */
	public void updateCrossPlayerStruct(String playerId, CrossPlayerStruct struct, int expireTime) {
		StatisManager.getInstance().incRedisKey(CROSS_PLAYER_STRUCT);
		String key = CROSS_PLAYER_STRUCT + ":" +playerId;
		redisSession.setBytes(key, struct.toByteArray(), expireTime);
	}
	
	/**
	 * 设置二级密码
	 * 
	 * @param playerId
	 * @param passwd
	 */
	public void setSecPasswd(String playerId, String passwd) {
		StatisManager.getInstance().incRedisKey(SEC_PASSWD);
		String key = String.format("%s:%s", SEC_PASSWD, playerId);
		try (Jedis jedis = redisSession.getJedis()){
			jedis.persist(key);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		redisSession.setString(key, passwd);
	}
	
	/**
	 * 关闭二级密码
	 * 
	 * @param playerId
	 * @param expireTime
	 */
	public void closeSecPasswd(String playerId, int expireTime) {
		StatisManager.getInstance().incRedisKey(SEC_PASSWD);
		String key = String.format("%s:%s", SEC_PASSWD, playerId);
		if (expireTime > 0) {
			redisSession.expire(key, expireTime);
		} else {
			redisSession.del(key);
		}
	}
	
	/**
	 * 读取二级密码
	 * 
	 * @param playerId
	 * @return
	 */
	public String readSecPasswd(String playerId) {
		StatisManager.getInstance().incRedisKey(SEC_PASSWD);
		String key = String.format("%s:%s", SEC_PASSWD, playerId);
		return redisSession.getString(key);
	}
	
	/**
	 * 撤销关闭二级密码
	 * 
	 * @param playerId
	 */
	public void undoClosePasswd(String playerId) {
		StatisManager.getInstance().incRedisKey(SEC_PASSWD);
		String key = String.format("%s:%s", SEC_PASSWD, playerId);
		try (Jedis jedis = redisSession.getJedis();){			
			jedis.persist(key);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取二级密码的生存时间
	 * 
	 * @param playerId
	 * @return
	 */
	public long getPasswdTTL(String playerId) {
		StatisManager.getInstance().incRedisKey(SEC_PASSWD);
		String key = String.format("%s:%s", SEC_PASSWD, playerId);
		try (Jedis jedis = redisSession.getJedis()) {
			long time = jedis.ttl(key);
			
			return time;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return 0;
	}
	
	/**
	 * 更新星球大战活动信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateSWActivityInfo(SWActivityData activityInfo) {
		String jsonString = JSON.toJSONString(activityInfo);
		String key = SWACTIVITY_INFO + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, StarWarsConst.SW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_INFO);
		return true;
	}

	/**
	 * 获取星球大战活动信息
	 * 
	 * @return
	 */
	public SWActivityData getSWActivityInfo() {
		String key = SWACTIVITY_INFO + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		SWActivityData activityInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			activityInfo = new SWActivityData();
		} else {
			activityInfo = JSON.parseObject(dataStr, SWActivityData.class);
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_INFO);
		return activityInfo;
	}
	
	/**
	 * 更新星球大战战斗状态信息
	 * 
	 * @param fightInfo
	 * @return
	 */
	public boolean updateSWFightData(SWFightData fightInfo) {
		String jsonString = JSON.toJSONString(fightInfo);
		String key = SWACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, StarWarsConst.SW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_FIGHT_STATE);
		return true;
	}
	
	/**
	 * 获取星球大战战斗状态信息
	 * 
	 * @return
	 */
	public SWFightData getSWFightData() {
		String key = SWACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		SWFightData fightInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			fightInfo = new SWFightData();
		} else {
			fightInfo = JSON.parseObject(dataStr, SWFightData.class);
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_FIGHT_STATE);
		return fightInfo;
	}
	
	/**
	 * 更新星球大战联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateSWGuildData(SWGuildData tlwGuildData, int termId) {
		String jsonString = JSON.toJSONString(tlwGuildData);
		String key = SWACTIVITY_GUILD_INFO + ":" + termId;
		redisSession.hSet(key, tlwGuildData.getId(), jsonString, StarWarsConst.SW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_GUILD_INFO);
		return true;
	}
	
	/**
	 * 删除指定联盟星球大战联盟信息
	 * @param guildId
	 * @param termId
	 * @return
	 */
	public boolean removeSWGuildData(String guildId, int termId) {
		String key = SWACTIVITY_GUILD_INFO + ":" + termId;
		redisSession.hDel(key, guildId);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_GUILD_INFO);
		return true;
	}
	
	/**
	 * 批量更新星球大战联盟信息
	 * @param swGuildDatas
	 * @param termId
	 * @return
	 */
	public boolean updateSWGuildData(List<SWGuildData> swGuildDatas, int termId) {
		if (swGuildDatas == null || swGuildDatas.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (SWGuildData data : swGuildDatas) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = SWACTIVITY_GUILD_INFO + ":" + termId;
		redisSession.hmSet(key, dataMap, StarWarsConst.SW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_GUILD_INFO);
		return true;
	}

	/**
	 * 获取星球大战联盟信息
	 * @param guildId
	 * @param termId
	 * @return
	 */
	public SWGuildData getSWGuildData(String guildId, int termId) {
		String key = SWACTIVITY_GUILD_INFO + ":" + termId;
		String dataStr = redisSession.hGet(key, guildId);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_GUILD_INFO);
		SWGuildData guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, SWGuildData.class);
		}
		return guildData;
	}
	
	/**
	 * 获取星球大战所有联盟信息
	 * @param termId
	 * @return
	 */
	public Map<String, SWGuildData> getAllSWGuildData(int termId) {
		String key = SWACTIVITY_GUILD_INFO + ":" + termId;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		Map<String, SWGuildData> dataMap = new HashMap<>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			SWGuildData guildData = JSON.parseObject(dataStr, SWGuildData.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_GUILD_INFO);
		return dataMap;
	}
	
	/**
	 * 获取星球大战所有联盟信息
	 * @param termId
	 * @param guildIds
	 * @return
	 */
	public Map<String, SWGuildData> getSWGuildDatas(int termId, List<String> guildIds) {
		String key = SWACTIVITY_GUILD_INFO + ":" + termId;
		if(guildIds.isEmpty()){
			return new HashMap<>();
		}
		List<String> results = redisSession.hmGet(key, guildIds.toArray(new String[guildIds.size()]));
		if(results == null || results.isEmpty()){
			return new HashMap<>();
		}
		Map<String, SWGuildData> dataMap = new HashMap<>();
		for (String dataStr : results) {
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			SWGuildData guildData = JSON.parseObject(dataStr, SWGuildData.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_GUILD_INFO);
		return dataMap;
	}
	
	/**
	 * 获取指定服务器参与联盟id
	 * @return
	 */
	public String getSWServerGuild(int termId, String serverId){
		String key = SWACTIVITY_SERVER_GUILD + ":" + termId;
		if(HawkOSOperator.isEmptyString(serverId)){
			return null;
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_SERVER_GUILD);
		return redisSession.hGet(key, serverId);
		
	}
	
	/**
	 * 存入本服的入围联盟id
	 * @param termId
	 * @param serverId
	 * @param guildId
	 */
	public void updateSWServerGuild(int termId, String serverId, String guildId) {
		String key = SWACTIVITY_SERVER_GUILD + ":" + termId;
		if (HawkOSOperator.isEmptyString(serverId) || HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_SERVER_GUILD);
		redisSession.hSet(key, serverId, guildId);
	}
	
	/**
	 * 更新星球大战房间信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateSWRoomData(SWRoomData swRoomData, int termId, int warType) {
		int expireSeconds = StarWarsConst.SW_EXPIRE_SECONDS;
		String key = SWACTIVITY_ROOM_INFO + ":" + termId + ":" + warType;
		redisSession.hSet(key, swRoomData.getId(), JSON.toJSONString(swRoomData), expireSeconds);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_ROOM_INFO);
		return true;
	}
	
	/**
	 * 批量更新星球大战匹配房间信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateSWRoomData(List<SWRoomData> swRoomDatas, int termId, int warMark) {
		int expireSeconds = StarWarsConst.SW_EXPIRE_SECONDS;
		Map<String, String> dataMap = new HashMap<>();
		for(SWRoomData data : swRoomDatas){
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		if(dataMap.isEmpty()){
			return false;
		}
		String key =  SWACTIVITY_ROOM_INFO + ":" + termId + ":"+warMark;
		redisSession.hmSet(key, dataMap, expireSeconds);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_ROOM_INFO);
		return true;
	}
	
	/**
	 * 删除星球大战匹配房间信息
	 * @param activityInfo
	 * @return
	 */
	public boolean removeSWRoomData(int termId, int warMark) {
		String key =  SWACTIVITY_ROOM_INFO + ":" + termId + ":"+warMark;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_ROOM_INFO);
		return true;
	}

	/**
	 * 获取星球大战匹配房间信息
	 * 
	 * @return
	 */
	public SWRoomData getSWRoomData(String roomId, int termId, int warMark) {
		String key = SWACTIVITY_ROOM_INFO + ":" + termId + ":" + warMark;
		String dataStr = redisSession.hGet(key, roomId);
		SWRoomData roomData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			roomData = JSON.parseObject(dataStr, SWRoomData.class);
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_ROOM_INFO);
		return roomData;
	}
	
	/**
	 * 获取星球大战匹配房间信息
	 * 
	 * @return
	 */
	public List<SWRoomData> getAllSWRoomData(int termId, int warType) {
		String key = SWACTIVITY_ROOM_INFO + ":" + termId + ":" + warType;
		Map<String, String> dataMap = redisSession.hGetAll(key);
		List<SWRoomData> list = new ArrayList<>();
		for (Entry<String, String> entry : dataMap.entrySet()) {
			String dataStr = entry.getValue();
			if (!HawkOSOperator.isEmptyString(dataStr)) {
				SWRoomData roomData = JSON.parseObject(dataStr, SWRoomData.class);
				list.add(roomData);
			}
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_ROOM_INFO);
		return list;
	}
	
	/**
	 * 更新星球大战出战联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateSWJoinGuild(SWGuildJoinInfo joinInfo, int termId) {
		String jsonString = JSON.toJSONString(joinInfo);
		String key = SWACTIVITY_JOIN_GUILD + ":" + termId;
		redisSession.hSet(key, joinInfo.getId(), jsonString, TiberiumConst.TLW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_JOIN_GUILD);
		return true;
	}
	
	
	/**
	 * 删除星球大战出战联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean removeSWJoinGuild(int termId) {
		String key = SWACTIVITY_JOIN_GUILD + ":" + termId;
		redisSession.del(key);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_JOIN_GUILD);
		return true;
	}
	
	/**
	 * 批量更新星球大战出战联盟信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateSWJoinGuild(List<SWGuildJoinInfo> joinInfos, int termId) {
		if (joinInfos == null || joinInfos.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (SWGuildJoinInfo data : joinInfos) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = SWACTIVITY_JOIN_GUILD + ":" + termId;
		redisSession.hmSet(key, dataMap, StarWarsConst.SW_EXPIRE_SECONDS);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_JOIN_GUILD);
		return true;
	}

	/**
	 * 获取星球大战出战联盟信息
	 * 
	 * @return
	 */
	public SWGuildJoinInfo getSWJoinGuild(int termId, String guildId) {
		String key = SWACTIVITY_JOIN_GUILD + ":" + termId;
		String dataStr = redisSession.hGet(key, guildId);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_JOIN_GUILD);
		SWGuildJoinInfo guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, SWGuildJoinInfo.class);
		}
		return guildData;
	}
	
	/**
	 * 获取星球大战所有联盟信息
	 * @return
	 */
	public Map<String, SWGuildJoinInfo> getAllSWJoinGuild(int termId) {
		String key = SWACTIVITY_JOIN_GUILD + ":" + termId;
		Map<String, String> resultMap = redisSession.hGetAll(key);
		Map<String, SWGuildJoinInfo> dataMap = new HashMap<>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			SWGuildJoinInfo guildData = JSON.parseObject(dataStr, SWGuildJoinInfo.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_JOIN_GUILD);
		return dataMap;
	}
	
	/**
	 * 更新星球大战可出战玩家信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateSWPlayerData(SWPlayerData swPlayerData, int termId, int warType) {
		int expireSeconds = StarWarsConst.SW_EXPIRE_SECONDS;
		String jsonString = JSON.toJSONString(swPlayerData);
		String key = SWACTIVITY_PLAYER_INFO + ":" + termId + ":" + warType + ":" + swPlayerData.getId();
		redisSession.setString(key, jsonString, expireSeconds);
		StatisManager.getInstance().incRedisKey(SWACTIVITY_PLAYER_INFO);
		return true;
	}

	/**
	 * 获取星球大战可出战玩家信息
	 * 
	 * @return
	 */
	public SWPlayerData getSWPlayerData(String playerId, int termId, int warType) {
		String key = SWACTIVITY_PLAYER_INFO + ":" + termId + ":" + warType + ":" + playerId;
		String dataStr = redisSession.getString(key);
		SWPlayerData twPlayerData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			twPlayerData = JSON.parseObject(dataStr, SWPlayerData.class);
		}
		StatisManager.getInstance().incRedisKey(SWACTIVITY_PLAYER_INFO);
		return twPlayerData;
	}
	
	/**
	 * 更新单个王的信息.
	 * @param kingStruct
	 */
	public void updateServerKing(CrossPlayerStruct kingStruct) {
		int keyExpireTime = 7*86400;
		String key = SERVER_KING_STRUCT;
		StatisManager.getInstance().incRedisKey(SERVER_KING_STRUCT);
		redisSession.hSetBytes(key, GsConfig.getInstance().getServerId(), kingStruct.toByteArray(), keyExpireTime);
	}
	
	/**
	 * 获取指定服务器的国王 
	 */
	public CrossPlayerStruct getServerKing(String serverId) {
		
		StatisManager.getInstance().incRedisKey(SERVER_KING_STRUCT);

		// 用主服的serverId
		serverId = GlobalData.getInstance().getMainServerId(serverId);
		
		byte[] bytes = redisSession.hGetBytes(SERVER_KING_STRUCT, serverId);
		if (bytes == null || bytes.length <= 0) {
			return null;
		}

		CrossPlayerStruct struct = null;
		
		try {
			struct = CrossPlayerStruct.parseFrom(bytes);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return struct;
	}
	
	/**
	 * 获取所有的王,或者某些服务器的王.
	 * @param serverIdList
	 * @return
	 */
	public List<CrossPlayerStruct> getServerKingList(List<String> serverIdList) {
		String key = SERVER_KING_STRUCT;
		StatisManager.getInstance().incRedisKey(SERVER_KING_STRUCT);
		List<CrossPlayerStruct> structList = new ArrayList<>();
		try {
			Collection<byte[]> byteList = null;
			if (CollectionUtils.isEmpty(serverIdList)) {
				byteList = redisSession.hGetAllBytes(key.getBytes()).values();
			} else {
				byte[][] serverByteArray = new byte[serverIdList.size()][];
				for (int i = 0; i < serverIdList.size(); i ++) {
					serverByteArray[i] = serverIdList.get(i).getBytes();
				}
				byteList = redisSession.hmGet(key.getBytes(), serverByteArray);
			}
						 
			if (CollectionUtils.isEmpty(byteList)) {
				return structList;
			}
						
			for (byte[] byteArray : byteList) {
				if (byteArray != null && byteArray.length > 0) {
					CrossPlayerStruct struct = CrossPlayerStruct.parseFrom(byteArray);
					structList.add(struct);
				}				
			}			
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		
		return structList;
	}
	
	/**
	 * 删除.
	 * @param serverId
	 */
	public void deleteServerKing(String serverId) {
		String key = SERVER_KING_STRUCT;
		StatisManager.getInstance().incRedisKey(SERVER_KING_STRUCT);
		
		redisSession.hDel(key, serverId);
	}
	
	/**
	 * 加载所有的.
	 * @return
	 */
	public Map<String, Integer> loadAllMergeServerUnsetOfficerInfo() {
		Map<String, String> redisMap = redisSession.hGetAll(MERGE_SERVER_UNSET_OFFICER);
		Map<String, Integer> memMap = MapUtil.map2Map(redisMap, MapUtil.genSelfTransform(String.class), MapUtil.STRING2INTEGER);
		
		return memMap;
	}
	
	 
	/**
	 * 
	 * @param map
	 */
	public void updateMergeServerUnsetOfficerInfo(Map<String, Integer> map) {
		Map<String, String> redisMap = MapUtil.map2Map(map, MapUtil.genSelfTransform(String.class), MapUtil.INTEGER2STRING);
		redisSession.hmSet(MERGE_SERVER_UNSET_OFFICER, redisMap, 3*86400);
	}
	
	
	/**
	 * 更新攻防模拟战信息.
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean addOrUpdateSimulateWarActivityInfo(SimulateWarActivityData activityInfo) {
		String jsonString = JSON.toJSONString(activityInfo);
		String key = SIMULATE_WAR_ACTIVITY_DATA + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_ACTIVITY_DATA);
		return true;
	}

	/**
	 * 获取锦标赛活动信息
	 * 
	 * @return
	 */
	public SimulateWarActivityData getSimulateWarActivityData() {
		String key = SIMULATE_WAR_ACTIVITY_DATA + ":" + GsConfig.getInstance().getServerId();
		String dataStr = redisSession.getString(key);
		SimulateWarActivityData activityInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			activityInfo = new SimulateWarActivityData();
		} else {
			activityInfo = JSON.parseObject(dataStr, SimulateWarActivityData.class);
		}
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_ACTIVITY_DATA);
		return activityInfo;
	}
	
	/**
	 * 获取玩家
	 * @param termId
	 * @param playerId
	 * @return
	 */
	public  SimulateWarPlayerExt getSimulateWarPlayerExt(int termId, String playerId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_PLAYER_EXT).add(termId + "").add(playerId+"").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_PLAYER_EXT);
		String value = redisSession.getString(key);
		if (HawkOSOperator.isEmptyString(value)) {
			return new SimulateWarPlayerExt();
		} else {
			return JSON.parseObject(value, SimulateWarPlayerExt.class);
		}
	}
	
	/**
	 *  保存玩家的部分信息.
	 * @param termId
	 * @param playerId
	 * @param playerExt
	 */
	public void addOrUpdateSimulateWarPlayerExt(int termId, String playerId, SimulateWarPlayerExt playerExt) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_PLAYER_EXT).add(termId + "").add(playerId+"").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_PLAYER_EXT);
		redisSession.setString(key, JSON.toJSONString(playerExt));
	}
			
	/**
	 * 加载工会里面在攻防模拟战里面的.
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public Set<String> getSimulateWarGuildPlayerIds(int termId, String guildId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_PLAYER_DATA).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_PLAYER_DATA);
		
		return redisSession.hKeys(key, -1);
	}
	
	/**
	 * 
	 * @param termId 期数
	 * @param guildId 工会ID
	 * @param builder 玩家信息
	 * @param expireTime 过期时间.
	 */
	public void addOrUpdateSimulateWarGuildPlayer(int termId, String guildId, PBSimulateWarPlayer.Builder builder, int expireTime) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_PLAYER_DATA).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_PLAYER_DATA);
		
		redisSession.hSetBytes(key, builder.getPlayerInfo().getPlayerId(), builder.build().toByteArray(), expireTime);
	}
	
	/**
	 *  队伍一个都没被悬赏所以清理了。.
	 * @param termId
	 * @param guidlId
	 * @param plaeyrId
	 */
	public void deleteSimualteWarGuildPlayer(int termId, String guildId, String plaeyrId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_PLAYER_DATA).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_PLAYER_DATA);
		redisSession.hDel(key, plaeyrId);
	}
	
	/**
	 * 清理攻防模拟战的出战信息.
	 * @param termId
	 * @param guildId
	 * @param playerId
	 */
	public void removeSimulateWarGuildPlayer(int termId, String guildId, String playerId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_PLAYER_DATA).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_PLAYER_DATA);
		
		redisSession.hDel(key, playerId);
	}
	
	/**
	 * 获取工会里面的玩家.
	 * @param termId 期数
	 * @param guildId 工会ID
	 * @param ids 玩家的id列表
	 * @return
	 */
	public Map<String, PBSimulateWarPlayer.Builder> getSimulateWarGuildPlayer(int termId, String guildId, String... ids) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_PLAYER_DATA).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_PLAYER_DATA);
		
		byte[][] idbytes = new byte[ids.length][];
		for (int i = 0; i < ids.length; i++) {
			idbytes[i] = ids[i].getBytes();
		}
		List<byte[]> resultList = redisSession.hmGet(key.getBytes(), idbytes);
		if (CollectionUtils.isEmpty(resultList)) {
			return new HashMap<>();
		}
		
		Map<String, PBSimulateWarPlayer.Builder> playerMap = new HashMap<>();
		for (byte[] bytes : resultList) {
			try {
				PBSimulateWarPlayer.Builder pbuilder = PBSimulateWarPlayer.newBuilder();
				pbuilder.mergeFrom(bytes);
				playerMap.put(pbuilder.getPlayerInfo().getPlayerId(), pbuilder);
			} catch (Exception e) {
				HawkException.catchException(e);
			}			
		}
		
		return playerMap;
	} 
	
	/**
	 * 获取工会的所有的玩家.
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public Map<String, PBSimulateWarPlayer.Builder> getSimulateWarGuildAllPlayer(int termId, String guildId) {
		Map<String, PBSimulateWarPlayer.Builder> guildPlayerMap = new HashMap<>();
		int batchSize = SimulateWarConstCfg.getInstance().getLoadPlayerSize();
		int size = 0; 
		Set<String> idSet = this.getSimulateWarGuildPlayerIds(termId, guildId);
		List<String> idList = new ArrayList<>();
		for (String playerId : idSet) {
			size ++;
			idList.add(playerId);
			if (size % batchSize == 0 || size >= idSet.size()) {
				Map<String, PBSimulateWarPlayer.Builder> singleMap = this.getSimulateWarGuildPlayer(termId, guildId, idList.toArray(new String[0]));
				guildPlayerMap.putAll(singleMap);
				idList.clear();
			}
		}
		
		return guildPlayerMap;
	}
	
	/**
	 * 获取 工会的分路行军信息
	 * @param termId
	 * @param guildId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<WayType, List<String>> getSimulateWarAllWayMarch(int termId, String guildId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_WAY).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_WAY);
		Map<String, String> redisMap = redisSession.hGetAll(key);
		Map<WayType, List<String>> wayIdMap = new HashMap<>();
		for (Entry<String, String> entry : redisMap.entrySet()) {
			try {
				wayIdMap.put(WayType.valueOf(Integer.parseInt(entry.getKey())), JSON.parseObject(entry.getValue(), new ArrayList<String>().getClass()));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		return wayIdMap;
	}
	
	/**
	 * 添加或更新工会的分路行军信息.
	 * @param termId
	 * @param guildId
	 */
	public void addOrUpdateSimulateWarWayMarch(int termId, String guildId, Map<WayType, List<String>> map, int expireTime) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_WAY).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_WAY);
		Map<String, String> redisMap = new HashMap<>();
		for (Entry<WayType, List<String>> entry : map.entrySet()) {
			redisMap.put(entry.getKey().getNumber() + "", JSON.toJSONString(entry.getValue()));
		}
		redisSession.hmSet(key, redisMap, expireTime);
	}
	
	/**
	 * 删除工会的报名信息.
	 * @param termId
	 * @param guildId
	 */
	public void deleteSimulateWarGuildData(int termId, String guildId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_GUILD_DATA).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_GUILD_DATA);
		
		redisSession.hDel(key, guildId);
	}
	
	/**
	 * 添加报名的工会.
	 * @param termId
	 * @param guildData
	 * @param expireTime
	 */
	public void addOrUpdateSimulateWarGuildData(int termId, SimulateWarGuildData guildData, int expireTime) {		
		String key = new StringJoiner(":").add(SIMULATE_WAR_GUILD_DATA).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_GUILD_DATA);
		redisSession.hSet(key, guildData.getGuildId(), JSON.toJSONString(guildData), expireTime);
	}
	
	public Map<String, SimulateWarGuildData> getSimulateWarAllGuildData(int termId) {		
		String key = new StringJoiner(":").add(SIMULATE_WAR_GUILD_DATA).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_GUILD_DATA);
		Map<String, String> redisMap = redisSession.hGetAll(key);
		Map<String, SimulateWarGuildData> memMap = new HashMap<>();
		for (Entry<String, String> entry : redisMap.entrySet()) {
			memMap.put(entry.getKey(), JSON.parseObject(entry.getValue(), SimulateWarGuildData.class));
		}
		
		return memMap;
	}
	
	/**
	 * 参加匹配的公会.
	 * @param termId
	 * @param guildIdMap
	 * @param expireTime
	 */
	public void addOrUpdateSimulateWarMatchGuildId(int termId, Map<String, String> guildIdMap, int expireTime) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_MATCH_GUILD_ID).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_MATCH_GUILD_ID);
		
		redisSession.hmSet(key, guildIdMap, expireTime);
	}
	
	/**
	 * 获取参加匹配的工会的id
	 * @param termId
	 * @return
	 */
	public Map<String, String> getSimulateWarMatchGuildId(int termId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_MATCH_GUILD_ID).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_MATCH_GUILD_ID);
		
		return redisSession.hGetAll(key);
	}
	
	/**
	 * 攻防模拟战的匹配信息
	 * @param termId
	 * @param matchMap
	 * @param expireTime
	 */
	public void addOrUpdateSimulateWarMatchInfo(int termId, Map<String, String> matchMap, int expireTime) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_MATCH_INFO).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_MATCH_INFO);
		
		redisSession.hmSet(key, matchMap, expireTime);
	}
	
	/**
	 * 匹配信息,然后战斗信息也写在这里面了.
	 * @param termId
	 * @param guildId
	 * @param SimulateWarMatchInfo
	 * @param expireTime
	 */
	public void addOrUpdateSimulateWarMatchInfo(int termId, String guildId, String SimulateWarMatchInfo) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_MATCH_INFO).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_MATCH_INFO);
		
		redisSession.hSet(key, guildId, SimulateWarMatchInfo);
	}
	
	/**
	 * 攻防模拟战的匹配信息.
	 * @param termId
	 * @return
	 */
	public Map<String, SimulateWarMatchInfo> getSimulateWarMatchInfo(int termId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_MATCH_INFO).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_MATCH_INFO);
		Map<String, SimulateWarMatchInfo> map = new HashMap<>();
		Map<String, String> redisMap = redisSession.hGetAll(key);
		if (MapUtils.isEmpty(redisMap)) {
			return map;
		}
		
		for (Entry<String, String> entry : redisMap.entrySet()) {
			map.put(entry.getKey(), JSON.parseObject(entry.getValue(), SimulateWarMatchInfo.class));
		}
		
		return map;
	}
	
	/**
	 * 判断攻防模拟战的匹配是否完成.
	 * @param termId
	 * @return
	 */
	public boolean isSimulateWarMatchFinish(int termId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_MATCH_STATE).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_MATCH_STATE);
		
		return !HawkOSOperator.isEmptyString(redisSession.getString(key));
	}
	
	/**
	 * 设置攻防模拟战的匹配完成
	 * @param termId
	 */
	public void setSimulateWarMatchFinish(int termId, int expireTime) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_MATCH_STATE).add(termId + "").toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_MATCH_STATE);
		
		redisSession.setString(key, "ok", expireTime);
	}
	
	/**
	 * 设置工会的战斗信息
	 * @param termId
	 * @param guildId
	 * @param battleData
	 * @param expireTime
	 */
	public void addOrUpdateSimulateWarGuildBattle(int termId, String guildId, SimulateWarGuildBattle battleData, int expireTime) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_GUILD_BATTLE).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_GUILD_BATTLE);
		
		redisSession.setBytes(key, battleData.toByteArray(), expireTime);
	}
	
	/**
	 * 获取工会的战斗信息.
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public SimulateWarGuildBattle.Builder getSimulateWarGuildBattle(int termId, String guildId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_GUILD_BATTLE).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_GUILD_BATTLE);
		
		byte[] data = redisSession.getBytes(key.getBytes());
		if (data != null && data.length > 0) {
			
			try {
				SimulateWarGuildBattle.Builder builder = SimulateWarGuildBattle.newBuilder();
				builder.mergeFrom(data);
				
				return builder;
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
			}					
		} 
		
		return null;
	}
	
	/**
	 * 更新
	 * @param termId
	 * @param guildId
	 * @param wayRecor
	 * @param expireTime
	 */
	public void addOrUpdateSimulateWarBattleRecord(int termId, String battleId, Map<WayType, SimulateWarBattleList> wayRecord, int expireTime) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_BATTLE_RECORD).add(termId + "").add(battleId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_BATTLE_RECORD);
		
		Map<byte[], byte[]> redisMap = new HashMap<>();
		for (Entry<WayType, SimulateWarBattleList> entry : wayRecord.entrySet()) {
			redisMap.put(entry.getKey().toString().getBytes(), entry.getValue().toByteArray());
		} 
		
		redisSession.hmSetBytes(key, redisMap, expireTime);
	}
	
	/**
	 * 担心一次操作太多数据容易有问题,
	 * @param termId
	 * @param guildId
	 * @param way
	 * @param wayRecord
	 * @param expireTime
	 */
	public void addOrUpdateSimulateWarBattleRecord(int termId, String battleId, WayType way, SimulateWarBattleList wayRecord, int expireTime) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_BATTLE_RECORD).add(termId + "").add(battleId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_BATTLE_RECORD);			
		
		redisSession.hSetBytes(key, way.name(), wayRecord.toByteArray(), expireTime);
	}
	
	/**
	 * 返回公会的三路战斗记录.
	 */
	public Map<WayType, SimulateWarBattleList> getSimulateWarBattleRecord(int termId, String battleId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_BATTLE_RECORD).add(termId + "").add(battleId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_BATTLE_RECORD);
		Map<byte[], byte[]> redisMap = redisSession.hGetAllBytes(key.getBytes());
		Map<WayType, SimulateWarBattleList> memMap = new HashMap<>();
		if (MapUtils.isEmpty(redisMap)) {
			return memMap;
		} 
		
		for (Entry<byte[], byte[]> entry : redisMap.entrySet()) {
			try {
				memMap.put(WayType.valueOf(new String(entry.getKey())), SimulateWarBattleList.parseFrom(entry.getValue()));
			} catch (Exception e) {
				HawkException.catchException(e);
			}			
		}
		
		return memMap;
	} 
	
	
	public SimulateWarBattleList getSimulateWarBattleRecord(int termId, String guildId, WayType wayType) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_BATTLE_RECORD).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_BATTLE_RECORD);
		byte[] bytes = redisSession.hGetBytes(key, wayType.name());
		
		if (bytes == null || bytes.length <= 0) {
			return null;
		}
		
		try {
			return SimulateWarBattleList.parseFrom(bytes);
		} catch (Exception e) {
			HawkException.catchException(e);
		}			
		
		return null;
	}
	
	/**
	 * 出战玩家
	 */
	public void addOrUpdateSimulateWarBattlePlayer(int termId, String guildId, Set<String> playerIdList, int keyExpireTime) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_BATTLE_PLAYER).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_BATTLE_PLAYER);
		
		redisSession.lPush(key, keyExpireTime, playerIdList.toArray(new String[0]));
	}
	
	/**
	 * 读取出战玩家.
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public Set<String> getSimulateWarBattlePlayer(int termId, String guildId) {
		String key = new StringJoiner(":").add(SIMULATE_WAR_BATTLE_PLAYER).add(termId + "").add(guildId).toString();
		StatisManager.getInstance().incRedisKey(SIMULATE_WAR_BATTLE_PLAYER);
		
		return new HashSet<>(redisSession.lRange(key, 0, -1, 0));
	}
	
	/**
	 * 获取玩家玩家跨服投票信息
	 */
	public String getCrossVote(int termId, String playerId) {
		String key = CACTIVITY_VOTE + ":" + termId + ":" + playerId;
		return redisSession.getString(key, 604800);
	}
	
	/**
	 * 设置玩家跨服投票信息
	 */
	public void setCrossVote(int termId, String playerId, String serverId) {
		String key = CACTIVITY_VOTE + ":" + termId + ":" + playerId;
		redisSession.setString(key, serverId, 604800);
	}
	
	
	/**
	 * 跨服充能状态key
	 */
	public String getCrossPresidentKey(int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			crossId = 0;
		}
		return CACTIVITY_PRE_OPEN + ":" + crossId + ":" + termId;
	}
	
	/**
	 * 设置跨服王战开启服务器
	 */
	public boolean setCrossPresidentServer(int termId, String serverId) {
		long state = redisSession.hSetNx(getCrossPresidentKey(termId), "lock", serverId);
		return state > 0L;
	}
	
	/**
	 * 获取跨服王战开启服务器
	 */
	public String getCrossPresidentServer(int termId) {
		return redisSession.hGet(getCrossPresidentKey(termId), "lock", 604800);
	}
	
	/**
	 * 获取跨服王战开启服务器
	 * @param termId
	 * @param crossId
	 * @return
	 */
	public String getCrossPresidentServer(int termId,int crossId){
		String key = CACTIVITY_PRE_OPEN + ":" + crossId + ":" + termId;
		return redisSession.hGet(key, "lock", 604800);
	}
	
	
	/**
	 * 设置跨服联盟盟主信息
	 */
	public void setCrossGuildLeaderInfo(String guildId, CrossPlayerStruct.Builder builder) {
		try{
			String value = JsonFormat.printToString(builder.build());
			String key = String.format("%s:%s", CROSS_GUILD_LEADER_INFO, guildId);
			redisSession.setString(key, value);
		}catch(Exception e){
			HawkException.catchException(e);
		}		
	}
	
	/**
	 * 获取跨服联盟盟主信息
	 */
	public CrossPlayerStruct getCrossGuildLeaderInfo(String guildId) {
		try{
			String key = String.format("%s:%s", CROSS_GUILD_LEADER_INFO, guildId);
			String value = redisSession.getString(key);
			if(!HawkOSOperator.isEmptyString(value)){
				CrossPlayerStruct.Builder builder = CrossPlayerStruct.newBuilder();
				JsonFormat.merge(value, builder);
				return builder.build();	
			}
		} catch(Exception e){
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 设置战时司令信息
	 */
	public void setFightPresidentInfo(String playerId, CrossPlayerStruct.Builder builder) {
		try{
			String value = JsonFormat.printToString(builder.build());
			String key = String.format("%s:%s", FIGHT_PRESIDENT_LEADER_INFO, playerId);
			redisSession.setString(key, value);
		}catch(Exception e){
			HawkException.catchException(e);
		}		
	}
	
	/**
	 * 获取战时司令
	 */
	public CrossPlayerStruct getFightPresidentInfo(String playerId) {
		try{
			String key = String.format("%s:%s", FIGHT_PRESIDENT_LEADER_INFO, playerId);
			String value = redisSession.getString(key);
			if(!HawkOSOperator.isEmptyString(value)){
				CrossPlayerStruct.Builder builder = CrossPlayerStruct.newBuilder();
				JsonFormat.merge(value, builder);
				return builder.build();	
			}
		} catch(Exception e){
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 获取跨服税收
	 */
	public Map<Integer, Long> getAllCrossTax(String serverId) {
		Map<Integer, Long> receiveTax = new HashMap<>();
		
		try {
			String key = String.format("%s:%s", CROSS_TAX, serverId);
			
			Map<String, String> tax = redisSession.hGetAll(key);
			for (Entry<String, String> otax : tax.entrySet()) {
				receiveTax.put(Integer.valueOf(otax.getKey()), Long.valueOf(otax.getValue()));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return receiveTax; 
	}
	
	/**
	 * 增加跨服税收
	 */
	public void incCrossTax(String targetServerId, long resType, long resValue) {
		try {
			String key = String.format("%s:%s", CROSS_TAX, targetServerId);
			hIncrBy(key, String.valueOf(resType), resValue);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 添加税收记录
	 */
	public void addTaxRecord(String serverId, CrossTaxRecord.Builder builder, int max) {
		try {
			String key = String.format("%s:%s", CROSS_TAX_RECORD, serverId);
			String value = JsonFormat.printToString(builder.build());
			redisSession.lPush(key, 0, value);
			redisSession.lTrim(key, 0, Math.max(0, max - 1));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取税收记录
	 */
	public List<CrossTaxRecord> getTaxRecord(String serverId) {
		List<CrossTaxRecord> retList = new ArrayList<>();
		try {
			String key = String.format("%s:%s", CROSS_TAX_RECORD, serverId);
			List<String> ret = redisSession.lRange(key, 0, -1, 0);
			for( String jsonStr : ret ){
				CrossTaxRecord.Builder builder = CrossTaxRecord.newBuilder();
				JsonFormat.merge(jsonStr, builder);
				retList.add(builder.build());
			}
		} catch (ParseException e) {
			HawkException.catchException(e);
		}
		return retList;
	}

	/**
	 * 添加跨服税收分配记录
	 */
	public void addTaxSendRecord(CrossTaxSendRecord.Builder builder) {
		try {
			int taxRecordLimit = CrossConstCfg.getInstance().getTaxRecordLimit();
			String key = String.format("%s:%s", CROSS_TAX_SEND_RECORD, GsConfig.getInstance().getServerId());
			String value = JsonFormat.printToString(builder.build());
			redisSession.lPush(key, 0, value);
			redisSession.lTrim(key, 0, Math.max(0, taxRecordLimit - 1));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取税收分配记录
	 */
	public List<CrossTaxSendRecord> getTaxSendRecord(String serverId) {
		List<CrossTaxSendRecord> retList = new ArrayList<>();
		try {
			String key = String.format("%s:%s", CROSS_TAX_SEND_RECORD, serverId);
			List<String> ret = redisSession.lRange(key, 0, -1, 0);
			for( String jsonStr : ret ){
				CrossTaxSendRecord.Builder builder = CrossTaxSendRecord.newBuilder();
				JsonFormat.merge(jsonStr, builder);
				retList.add(builder.build());
			}
		} catch (ParseException e) {
			HawkException.catchException(e);
		}
		return retList;
	}
	
	/**
	 * 获取跨服税收奖励接收次数 
	 */
	public int getCrossTaxReceiveTimes(int termId, String playerId) {
		int count = 0;
		try {
			String key = String.format("%s:%s:%s", CROSS_TAX_RECEIVE_TIMES, String.valueOf(termId), playerId);
			String strValue = redisSession.getString(key);
			if (!HawkOSOperator.isEmptyString(strValue)) {
				count = Integer.valueOf(strValue);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return count;
	}
	
	/**
	 * 添加跨服税收奖励接收次数 
	 */
	public void addCrossTaxReceiveTimes(int termId, String playerId) {
		try {
			String key = String.format("%s:%s:%s", CROSS_TAX_RECEIVE_TIMES, String.valueOf(termId), playerId);
			redisSession.increaseBy(key, 1, 86400 * 15);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取跨服任务
	 */
	public String getCrossMission(String playerId, int termId) {
		try {
			String key = String.format("%s:%s:%s", CROSS_MISSION, String.valueOf(termId), playerId);
			String missions = redisSession.getString(key);
			return missions;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 更新跨服任务
	 */
	public void updateCrossMission(String playerId, int termId, String missions) {
		try {
			String key = String.format("%s:%s:%s", CROSS_MISSION, String.valueOf(termId), playerId);
			redisSession.setString(key, missions, 86400 * 7);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public boolean hIncrBy(String key, String field, long value) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			boolean e;
			try {
				e = jedis.hincrBy(key, field, value).longValue() > 0L;
			} catch (Exception arg8) {
				HawkException.catchException(arg8, new Object[0]);
				return false;
			} finally {
				jedis.close();
			}

			return e;
		} else {
			return false;
		}
	}
	
	/**
	 * 获取跨服医院信息
	 */
	public Map<Integer, Integer> getCrossHospital(String playerId, int termId) {
		Map<Integer, Integer> result = new HashMap<>();
		try {
			String key = String.format("%s:%s:%s", CROSS_HOSPITAL, String.valueOf(termId), playerId);
			Map<String, String> infos = redisSession.hGetAll(key);
			if (infos != null) {
				for (Entry<String, String> info : infos.entrySet()) {
					result.put(Integer.valueOf(info.getKey()), Integer.valueOf(info.getValue()));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return result;
	}
	
	/**
	 * 更新跨服医院信息
	 */
	public void updateCrossHospital(String playerId, int termId, Map<Integer, Integer> infos) {
		try {
			String key = String.format("%s:%s:%s", CROSS_HOSPITAL, String.valueOf(termId), playerId);
			
			Map<String, String> result = new HashMap<>();
			for (Entry<Integer, Integer> hospital : infos.entrySet()) {
				result.put(String.valueOf(hospital.getKey()), String.valueOf(hospital.getValue()));
			}
			redisSession.hmSet(key, result, 604800);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取跨服道具检测期数 
	 */
	public int getCrossItemCheckTerm(String playerId) {
		try {
			String key = String.format("%s:%s", CROSS_ITEM_CHECK, playerId);
			String termId = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(termId)) {
				return 0;
			}
			return Integer.parseInt(termId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 设置跨服道具检测期数
	 */
	public void setCrossItemCheckTerm(String playerId, int termId) {
		try {
			String key = String.format("%s:%s", CROSS_ITEM_CHECK, playerId);
			redisSession.setString(key, String.valueOf(termId));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/** 添加仇恨排行对象
	 * @param key
	 * @param score
	 * @param member */
	public long updatePlayerHateTotal(String playerId, long score, String toPlayerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_HATRED_RANK_KEY);
		String key = PLAYER_HATRED_RANK_KEY + ":" + playerId;
		Double zScore = redisSession.zScore(key, toPlayerId, 0);
		long newScore = Objects.nonNull(zScore) ? zScore.longValue() + score : score;
		long rank = redisSession.zAdd(key, newScore, toPlayerId);
		if (redisSession.zCount(key, 0, Long.MAX_VALUE) >=  ConstProperty.getInstance().getHateRankNumLimit()) {
			redisSession.zRemrangeByRank(key, 0, 0);
		}
		return rank;
	}
	
	/**获取排行名次
	 * @param playerId
	 * @param toPlayerId
	 * @return
	 */
	public long getHateRank(String playerId, String toPlayerId){
		StatisManager.getInstance().incRedisKey(PLAYER_HATRED_RANK_KEY);
		String key = PLAYER_HATRED_RANK_KEY + ":" + playerId;
		Long zrevrank = redisSession.zrevrank(key, toPlayerId, 0);
		return Objects.nonNull(zrevrank) ? zrevrank.longValue() + 1 : -1;
	}
	
	/**
	 * 获取玩家对应另一个玩家的仇恨值
	 * @param playerId
	 * @param toPlayerId
	 * @return
	 */
	public long getPlayerHateTotal(String playerId,String toPlayerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_HATRED_RANK_KEY);
		String key = PLAYER_HATRED_RANK_KEY + ":" + playerId;
		Double zScore = redisSession.zScore(key, toPlayerId, 0);
		return Objects.nonNull(zScore) ? zScore.longValue() : 0;
	}

	/**获取玩家的仇恨总榜单
	 * @param playerId
	 * @param maxCount
	 * @return
	 */
	public Set<Tuple> getPlayerHateTotalRankList(String playerId, int maxCount) {
		StatisManager.getInstance().incRedisKey(PLAYER_HATRED_RANK_KEY);
		String key = PLAYER_HATRED_RANK_KEY + ":" + playerId;
		return redisSession.zRevrangeWithScores(key, 0, maxCount - 1, 0);
	}
	
	/** 添加单个击杀仇恨排行对象
	 * @param key
	 * @param score
	 * @param member */
	public void updatePlayerHateSingle(String playerId, long score, String toPlayerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_HATRED_SINGLE_KEY);
		String key = PLAYER_HATRED_SINGLE_KEY + ":" + playerId;
		Double zScore = redisSession.zScore(key, toPlayerId, 0);
		long newScore = Objects.nonNull(zScore) ? zScore.longValue() + score : score;
		redisSession.zAdd(key, newScore, toPlayerId);
		if (redisSession.zCount(key, 0, Long.MAX_VALUE) >= ConstProperty.getInstance().getHateRankNumLimit()) { 
			redisSession.zRemrangeByRank(key, 0, 0);
		}
	}
	
	/**
	 * 获取玩家对应另一个玩家的击杀仇恨值
	 * @param playerId
	 * @param toPlayerId
	 * @return
	 */
	public long getPlayerHateSingle(String playerId,String toPlayerId) {
		StatisManager.getInstance().incRedisKey(PLAYER_HATRED_SINGLE_KEY);
		String key = PLAYER_HATRED_SINGLE_KEY + ":" + playerId;
		Double zScore = redisSession.zScore(key, toPlayerId, 0);
		return Objects.nonNull(zScore) ? zScore.longValue() : 0;
	}
	
	/**
	 * 设置跨服王战结束
	 */
	public void setCrossPresidentFightOver() {
		try {
			if (!CrossActivityService.getInstance().isOpen()) {
				return;
			}
			int termId = CrossActivityService.getInstance().getTermId();
			Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
			if (crossId == null) {
				return;
			}
			String key = CROSS_PRESIDENT_FIGHT_OVER + ":" + termId + ":" + crossId;
			redisSession.setString(key, String.valueOf(HawkTime.getMillisecond()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服王战是否结束
	 */
	public boolean isCrossPresidentFightOver(int termId) {
		try {
			if (!CrossActivityService.getInstance().isOpen()) {
				return true;
			}
			Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
			if (crossId == null) {
				return true;
			}
			String key = CROSS_PRESIDENT_FIGHT_OVER + ":" + termId + ":" + crossId;
			String string = redisSession.getString(key);
			return !HawkOSOperator.isEmptyString(string);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}
	
	/**
	 * 获取跨服王战结束时间
	 */
	public long getCrossPresidentFightOverTime(int termId) {
		try {
			if (!CrossActivityService.getInstance().isOpen()) {
				return 0L;
			}
			Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
			if (crossId == null) {
				return 0L;
			}
			String key = CROSS_PRESIDENT_FIGHT_OVER + ":" + termId + ":" + crossId;
			String string = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(string)) {
				return 0L;
			}
			return Long.valueOf(string);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	/**
	 * 设置跨服发充能排行奖励 
	 */
	public void setCrossSendChargeReward(int termId, String serverId) {
		try {
			String key = CROSS_SEND_CHARGE_REWARD + ":" + termId + ":" + serverId;
			redisSession.setString(key, "1");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服发充能排行奖励是否已经发放
	 */
	public boolean isCrossSendChargeReward(int termId, String serverId) {
		try {
			String key = CROSS_SEND_CHARGE_REWARD + ":" + termId + ":" + serverId;
			String string = redisSession.getString(key);
			return !HawkOSOperator.isEmptyString(string);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}
	
	/**
	 * 获取竞争锁(泰伯利亚/锦标赛等跨服活动竞争匹配权)
	 * @param matchLockKey 对应功能的redisKey
	 * @return
	 */
	public long getMatchLock(String matchLockKey) {
		long lock = -1;
		String serverId = GsConfig.getInstance().getServerId();
		if (GsConfig.getInstance().getServerType() == 0) {
			lock = RedisProxy.getInstance().getRedisSession().hSetNx(matchLockKey, "mServer", serverId);
		} else {
			HawkLog.logPrintln("matchTickCannotGetLock , matchKey:{}, serverType: {}, serverId: {}", matchLockKey, GsConfig.getInstance().getServerType(), GsConfig.getInstance().getServerId());
		}
		return lock;
	}
	
	/**
	 * 每日重置类-当日已使用次数信息
	 * 
	 * @param type 类型
	 * @param playerId
	 * @return <最后一次使用的时间, 已使用次数>
	 */
	public HawkTuple2<Long, Integer> getDaiyResetUseTimesInfo(String type, String playerId) {
		String key = DAILY_RESET_USE_TIMES + ":" + playerId;
		String info = redisSession.hGet(key, type);
		if (!HawkOSOperator.isEmptyString(info)) {
			String[] arr = info.split(":");
			return new HawkTuple2<Long, Integer>(Long.valueOf(arr[0]), Integer.valueOf(arr[1]));
		}
		
		return new HawkTuple2<Long, Integer>(0L, 0);
	}
	
	/**
	 * 每日重置类-当日已使用次数信息更新
	 * 
	 * @param type 类型
	 * @param playerId
	 * @param times 当日已使用次数
	 */
	public void updateDaiyResetUseTimesInfo(String type, String playerId, int times) {
		String key = DAILY_RESET_USE_TIMES + ":" + playerId;
		String value = HawkTime.getMillisecond() + ":" + times;
		redisSession.hSet(key, type, value, GsConst.DAY_SECONDS);
	}
	
	/**
	 * 添加守护赠送记录。
	 * @param playerId
	 * @param historyInfo
	 * @param max
	 */
	public void addGuardDressSendHistory(String playerId, GuardSendAndBlagHistoryInfo historyInfo, int max) {
		StatisManager.getInstance().incRedisKey(GUARD_DRESS_SEND_HISTORY);
		String key = new StringJoiner(":").add(GUARD_DRESS_SEND_HISTORY).add(playerId).toString();			
		redisSession.lPush(key.getBytes(), 0, historyInfo.toByteArray());
		redisSession.lTrim(key, 0, max);							
	}
	
	/**
	 * 获取守护赠送记录。
	 * @param playerId
	 * @return
	 */
	public List<GuardSendAndBlagHistoryInfo> getGuardDressSendHistory(String playerId) {
		StatisManager.getInstance().incRedisKey(GUARD_DRESS_SEND_HISTORY);
		String key = new StringJoiner(":").add(GUARD_DRESS_SEND_HISTORY).add(playerId).toString();		
		List<byte[]> retArray = redisSession.lRange(key.getBytes(), 0, -1, 0);
		List<GuardSendAndBlagHistoryInfo> historyList = new ArrayList<>();
		try {
			for( byte[] bytes : retArray ){
				GuardSendAndBlagHistoryInfo.Builder builder = GuardSendAndBlagHistoryInfo.newBuilder();
				builder.mergeFrom(bytes);
				historyList.add(builder.build());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return historyList;
	}
	
	/**
	 * 添加守护索取记录。
	 * @param playerId
	 * @param historyInfo
	 * @param max
	 */
	public void addGuardDressBlagHistory(String playerId, GuardSendAndBlagHistoryInfo historyInfo, int max) {
		StatisManager.getInstance().incRedisKey(GUARD_DRESS_BLAG_HISTORY);
		String key = new StringJoiner(":").add(GUARD_DRESS_BLAG_HISTORY).add(playerId).toString();			
		redisSession.lPush(key.getBytes(), 0, historyInfo.toByteArray());
		redisSession.lTrim(key, 0, max);							
	}
	
	/**
	 * 获取守护索取记录。
	 * @param playerId
	 * @return
	 */
	public List<GuardSendAndBlagHistoryInfo> getGuardDressBlagHistory(String playerId) {
		StatisManager.getInstance().incRedisKey(GUARD_DRESS_BLAG_HISTORY);
		String key = new StringJoiner(":").add(GUARD_DRESS_BLAG_HISTORY).add(playerId).toString();		
		List<byte[]> retArray = redisSession.lRange(key.getBytes(), 0, -1, 0);
		List<GuardSendAndBlagHistoryInfo> historyList = new ArrayList<>();
		try {
			for( byte[] bytes : retArray ){
				GuardSendAndBlagHistoryInfo.Builder builder = GuardSendAndBlagHistoryInfo.newBuilder();
				builder.mergeFrom(bytes);
				historyList.add(builder.build());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return historyList;
	}
	
	/**按日重置类做用号, 使用计数 */
	public void effectTodayUseInc(String playerId,EffType effId, int num){
//		MILITARY_SHOP_BUY
		
		StatisManager.getInstance().incRedisKey(EFFECT_DAY_USECNT);
		int day = HawkTime.getYearDay();
		String key = EFFECT_DAY_USECNT + ":" + playerId + ":" + day;
		redisSession.hIncrBy(key, effId.name(), num, (int)TimeUnit.DAYS.toSeconds(1));
	}

	/**按日重置类做用号 今天是否生效过*/
	public boolean effectTodayUsed(String playerId,EffType effId) {
		StatisManager.getInstance().incRedisKey(EFFECT_DAY_USECNT);
		int day = HawkTime.getYearDay();
		String key = EFFECT_DAY_USECNT + ":" + playerId + ":" + day;
		return redisSession.hExists(key, effId.name(), (int)TimeUnit.DAYS.toSeconds(1));
	}
	
	/**按日重置类做用号 今天生效次数*/
	public int effectTodayUsedTimes(String playerId,EffType effId){
		StatisManager.getInstance().incRedisKey(EFFECT_DAY_USECNT);
		int day = HawkTime.getYearDay();
		String key = EFFECT_DAY_USECNT + ":" + playerId + ":" + day;
		String str =  redisSession.hGet(key, effId.name(), (int)TimeUnit.DAYS.toSeconds(1));
		if(HawkOSOperator.isEmptyString(str)){
			return 0;
		}
		return  NumberUtils.toInt(str);
	}
	
	/** 被决斗次数++ */
	public void incPlayerDueled(String playerId) {
		final int dayOfYear = HawkTime.getYearDay();
		String key = TODAY_DEULED + ":" + playerId + dayOfYear;
		redisSession.increaseBy(key, 1, 24 * 3600);
	}

	/** 被决斗次数 */
	public int todayDueled(String playerId) {
		final int dayOfYear = HawkTime.getYearDay();
		String key = TODAY_DEULED + ":" + playerId + dayOfYear;
		String val = redisSession.getString(key);
		return NumberUtils.toInt(val);
	}
	
	
	/**
	 * 获取账号注销检测时间
	 */
	public long getAccountCancellationCheckTime(String playerId) {
		try {
			String key = ACCOUNT_CANCELLATION_CHECK + ":" + playerId;
			String timeStr = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(timeStr)) {
				return 0L;
			} else {
				return Long.valueOf(timeStr);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	/**
	 * 更新账号注销检测时间
	 */
	public void updateAccountCancellationCheckTime(String playerId) {
		try {
			String key = ACCOUNT_CANCELLATION_CHECK + ":" + playerId;
			String timeStr = String.valueOf(HawkTime.getMillisecond());
			redisSession.setString(key, timeStr);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取服务器所有账号注销信息
	 */
	public Map<String, Long> getAllAccountCancellationInfo(String serverId) {
		Map<String, Long> ret = new ConcurrentHashMap<>();
		try {
			String key = ACCOUNT_CANCELLATION_BEGIN + ":" + serverId;
			Map<String, String> infos = redisSession.hGetAll(key);
			for (Entry<String, String> info : infos.entrySet()) {
				ret.put(info.getKey(), Long.valueOf(info.getValue()));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return ret;
	}
	
	/**
	 * 更新账号注销信
	 */
	public void updateAccountCancellationInfo(String serverId, String playerId) {
		try {
			String key = ACCOUNT_CANCELLATION_BEGIN + ":" + serverId;
			redisSession.hSet(key, playerId, String.valueOf(HawkTime.getMillisecond()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 移除账号注销信息
	 */
	public void rmAccountCancellationInfo(String serverId, String playerId) {
		try {
			String key = ACCOUNT_CANCELLATION_BEGIN + ":" + serverId;
			redisSession.hDel(key, playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新跨服总部占领过的状态
	 */
	public void updateCrossPresidentFirOccupy(int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return;
		}
		String key = CACTIVITY_FIRST_OCCUPY + ":" + crossId + ":" + termId;
		redisSession.setString(key, "1");
	}
	
	/**
	 * 是否是第一次占领跨服总部
	 */
	public boolean isCrossFirstOccupy(int termId) {
		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return false;
		}
		String key = CACTIVITY_FIRST_OCCUPY + ":" + crossId + ":" + termId;
		String ret = redisSession.getString(key);
		return HawkOSOperator.isEmptyString(ret);
	}

	

	/**
	 * 获取跨服国王
	 * @return
	 */
	public Map<String, String> getCrossServerKing() {
		String key = CROSS_KING_PLAYER;
		StatisManager.getInstance().incRedisKey(key);
		Map<String, String> serverPlayerIdMap = redisSession.hGetAll(key);
		Map<String, String> playerIdServerMap = new HashMap<>();
		for (Entry<String, String> entry : serverPlayerIdMap.entrySet()) {
			playerIdServerMap.put(entry.getValue(), entry.getKey());
		}
		
		return playerIdServerMap;
	}
	
	/**
	 *  添加跨服国王.
	 * @param serverId
	 * @param playerId
	 */
	public void addCrossServerKing(String serverId, String playerId, int expireTime) {
		String key = CROSS_KING_PLAYER;
		StatisManager.getInstance().incRedisKey(key);
		redisSession.hSet(key, serverId, playerId, expireTime);
	}
	
	/**
	 * 删除某个区服的跨服国王. 
	 * @param serverId
	 */
	public void deleteCrossServerKing(String serverId) {
		String key = CROSS_KING_PLAYER;
		StatisManager.getInstance().incRedisKey(key);
		redisSession.hDel(key, serverId);
	}
	
	/**
	 * 区服战力.
	 * @param termId
	 * @param serverId
	 * @param battleValue
	 * @param expireTime
	 */
	public boolean addCrossMatchServerBattle(int termId, CrossServerInfo serverInfo, int expireTime) {
		String key = CROSS_MATCH_SERVER_BATTLE + ":" + termId;
		StatisManager.getInstance().incRedisKey(CROSS_MATCH_SERVER_BATTLE);
		return redisSession.hSet(key, serverInfo.getServerId(), JSON.toJSONString(serverInfo), expireTime) > 0;
	}
	
	/**
	 * 获取写入的区服战力
	 * @param termId
	 * @return
	 */
	public Map<String, CrossServerInfo> getCrossMatchServerBattleMap(int termId) {
		String key = CROSS_MATCH_SERVER_BATTLE + ":" + termId;
		StatisManager.getInstance().incRedisKey(CROSS_MATCH_SERVER_BATTLE);
		Map<String, String> redisMap = redisSession.hGetAll(key);
		Map<String, CrossServerInfo> memMap = MapUtil.map2Map(redisMap, MapUtil.STRING2STRING, (String value)->JSON.parseObject(value, CrossServerInfo.class));
		return memMap;
	}
	
	/**
	 * 添加跨服匹配列表.
	 * @param termId
	 * @param idServersMap
	 * @param expireTime
	 */
	public void addCrossMatchList(int termId, Map<Integer, String> idServersMap, int expireTime) {
		String key = CROSS_SERVER_LIST + ":" + termId;
		StatisManager.getInstance().incRedisKey(CROSS_SERVER_LIST);
		Map<String, String> redisMap = MapUtil.map2Map(idServersMap, MapUtil.INTEGER2STRING, MapUtil.STRING2STRING);
		redisSession.hmSet(key, redisMap, expireTime);
	}
	
	/**
	 * 获取跨服匹配列表
	 * @param termId
	 * @return
	 */
	public Map<Integer, String> getCrossServerList(int termId) {
		String key = CROSS_SERVER_LIST + ":" + termId;
		StatisManager.getInstance().incRedisKey(CROSS_SERVER_LIST);
		Map<String, String> redisMap = redisSession.hGetAll(key);
		Map<Integer, String> memMap = MapUtil.map2Map(redisMap, MapUtil.STRING2INTEGER, MapUtil.STRING2STRING);
		return memMap;
	}

	/**
	 * 玩家回流数据保存
	 * @param openId
	 */
	public void savePlayerBackFlowAccount(String openId,String account){
		if (HawkOSOperator.isEmptyString(openId)) {
			return;
		}
		String key = PLAYER_BACK_FLOW_ACCOUNT+":"+openId;
		redisSession.setString(key, account);
	}
	
	/** 获取玩家回流数据*/
	public String getPlayerBackFlowAccount(String openId) {
		if (HawkOSOperator.isEmptyString(openId)) {
			return null;
		}
		String key = PLAYER_BACK_FLOW_ACCOUNT+":"+openId;
		String rlt = redisSession.getString(key);
		return rlt;
	}
	
	
	
	
	/**
	 * 保存回流角色
	 * @param playerId
	 * @param info
	 */
	public void savePlayerBackFlow(String playerId,String info){
		if (HawkOSOperator.isEmptyString(playerId)) {
			return;
		}
		StatisManager.getInstance().incRedisKey(PLAYER_BACK_FLOW_ROLE);
		String key = PLAYER_BACK_FLOW_ROLE+":"+playerId;
		redisSession.setString(key, info);
	}
	
	
	/**
	 * 获取回流角色
	 * @param playerId
	 * @return
	 */
	public String getPlayerBackFlow(String playerId){
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}
		StatisManager.getInstance().incRedisKey(PLAYER_BACK_FLOW_ROLE);
		String key = PLAYER_BACK_FLOW_ROLE+":"+playerId;
		return redisSession.getString(key);
	}
	
	
	/**
	 * 获取角色列表
	 * @param ids
	 * @return
	 */
	public Map<String,List<AccountRoleInfo>> getAccountRoleMap(Set<String> openIds){
		Map<String,List<AccountRoleInfo>> rolesMap = new HashMap<String,List<AccountRoleInfo>>();
		Map<String,Response<Map<String,String>>> piplineRes = new HashMap<String,Response<Map<String,String>>>();
		try(Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()){
			for(String openId : openIds ){
				String key = RedisProxy.getInstance().getAccountRoleInfoKey(openId);
				Response<Map<String,String>> onePiplineResp = pip.hgetAll(key);
				piplineRes.put(openId,onePiplineResp);
			}
			pip.sync();
			if( piplineRes.size() == openIds.size() ){
 	    		for(Entry<String,Response<Map<String,String>>> entry : piplineRes.entrySet()){
 	    			String key = entry.getKey();
 	    			Response<Map<String,String>> value = entry.getValue();
 	    			Map<String,String> roles = value.get();
 	    			List<AccountRoleInfo> list = new ArrayList<>();
 	    			if (roles != null && roles.size() > 0) {
 	    				for (String roleString : roles.values()) {
 	    					AccountRoleInfo roleInfoObj = JSONObject.parseObject(roleString, AccountRoleInfo.class);
 	    					list.add(roleInfoObj);
 	    				}
 	    			}
 	    			rolesMap.put(key, list);
 	    		}   		
			}
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		return rolesMap;
	}

	
	/**
	 * 获取好友推送时间
	 * @param ids
	 * @return
	 */
	public Map<String,Long> getFriendsPushCountMapToday(Set<String> openIds){
		Map<String,Long> timeMap = new HashMap<String,Long>();
		Map<String,Response<String>> piplineRes = new HashMap<String,Response<String>>();
		int day = HawkTime.getYearDay();
		try(Jedis jedis = getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()){
			for(String openId : openIds ){
				String key = SEND_FRIEND_PUSH_KEY  + ":" + day+":"+openId;
				Response<String> onePiplineResp = pip.get(key);
				piplineRes.put(openId,onePiplineResp );
			}
			pip.sync();
			if( piplineRes.size() == openIds.size() ){
 	    		for(Entry<String,Response<String>> entry : piplineRes.entrySet()){
 	    			String key = entry.getKey();
 	    			Response<String> value = entry.getValue();
 	    			String retStr = value.get();
 	    			if (!HawkOSOperator.isEmptyString(retStr)) {
 	    				timeMap.put(key, NumberUtils.toLong(retStr));
 	    			}else{
 	    				timeMap.put(key, 0l);
 	    			}
 	    		}   		
			}
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		return timeMap;
	}
	
	
	/**
	 * 添加发送好友推送记录记录
	 * @param saveSet
	 */
	public void saveSendFriendPushTimesToday(Set<String> openIds) {
		StatisManager.getInstance().incRedisKey(SEND_FRIEND_PUSH_KEY);
		int day = HawkTime.getYearDay();
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			for(String openId : openIds){
				StatisManager.getInstance().incRedisKey(SEND_FRIEND_PUSH_KEY);
				String key = SEND_FRIEND_PUSH_KEY  + ":" + day+":"+openId;
				pip.incrBy(key, 1);
				pip.expire(key, (int)TimeUnit.DAYS.toSeconds(2));
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	

	/**
	 * 获取好友推送时间
	 * @param playerId
	 * @return
	 */
	public long getSendFriendPushTimesToday(String openId){
		if (HawkOSOperator.isEmptyString(openId)) {
			return 0;
		}
		int day = HawkTime.getYearDay();
		StatisManager.getInstance().incRedisKey(SEND_FRIEND_PUSH_KEY);
		String key = SEND_FRIEND_PUSH_KEY  + ":" + day+":"+openId;
		String retStr = redisSession.getString(key);
		return  NumberUtils.toLong(retStr);
	} 
	
	
	/** 记录推送时间*/
	public void savePlayerBackMessagePush(Map<String,Long> pushTimes) {
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			for(Entry<String, Long> entry : pushTimes.entrySet()){
				String openId = entry.getKey();
				long time = entry.getValue();
				StatisManager.getInstance().incRedisKey(PLAYER_BACK_MESSAGE_PUS_KEY);
				String key = PLAYER_BACK_MESSAGE_PUS_KEY + ":" + openId;
				pip.set(key, String.valueOf(time));
				pip.expire(key, (int)TimeUnit.DAYS.toSeconds(30));
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	

	
	/**
	 * 获取推送时间
	 * @param ids
	 * @return
	 */
	public Map<String,Long> getPushTimeMap(Set<String> openIds){
		Map<String,Long> timeMap = new HashMap<String,Long>();
		Map<String,Response<String>> piplineRes = new HashMap<String,Response<String>>();
		try(Jedis jedis = getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()){
			for( String openId : openIds){
				String key = RedisProxy.PLAYER_BACK_MESSAGE_PUS_KEY+":"+openId;
				Response<String> onePiplineResp = pip.get(key);
				piplineRes.put(openId,onePiplineResp);
			}
			pip.sync();
			if( piplineRes.size() == openIds.size() ){
 	    		for(Entry<String,Response<String>> entry : piplineRes.entrySet()){
 	    			String key = entry.getKey();
 	    			Response<String> value = entry.getValue();
 	    			String retStr = value.get();
 	    			if (!HawkOSOperator.isEmptyString(retStr)) {
 	    				timeMap.put(key, NumberUtils.toLong(retStr));
 	    			}else{
 	    				timeMap.put(key, 0l);
 	    			}
 	    		}   		
			}
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		return timeMap;
	}
	
	/**
	 * 增加当日超能随机数
	 */
	public void dayLaboratoryRemakeInc(String playerId) {
		StatisManager.getInstance().incRedisKey(LABORATORY_REMAKE_COST);
		final String key = LABORATORY_REMAKE_COST + playerId + ":" + HawkTime.getYearDay();
		redisSession.increaseBy(key, 1, (int) TimeUnit.DAYS.toSeconds(1));
	}

	/**
	 * 当日超能随机数
	 */
	public int dayLaboratoryRemakeCount(String playerId) {
		StatisManager.getInstance().incRedisKey(LABORATORY_REMAKE_COST);
		final String key = LABORATORY_REMAKE_COST + playerId + ":" + HawkTime.getYearDay();
		String cc = redisSession.getString(key);
		return NumberUtils.toInt(cc);
	}
	
	/**
	 * 更新装备科技外显
	 */
	public void updateShowEquipTech(String playerId, int show) {
		String key = SHOW_EQUIP_TECH + ":" + playerId;
		redisSession.setString(key, String.valueOf(show));
	}
	
	/**
	 * 获取装备科技外显
	 */
	public int getShowEquipTech(String playerId) {
		try {
			String key = SHOW_EQUIP_TECH + ":" + playerId;
			String show = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(show)) {
				return 0;
			} else {
				return Integer.parseInt(show);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	public long decreaseStarWarsLogininPlayer(String stage) {	
		StatisManager.getInstance().incRedisKey(STAR_WARS_LOGINING_PLAYER);
		String key = STAR_WARS_LOGINING_PLAYER + ":" + stage;
		return redisSession.increaseBy(key, -1, 0);
	}
	
	public long increaseStarWarsLoginingPlayer(String stage) {
		String key = STAR_WARS_LOGINING_PLAYER + ":" + stage;		
		StatisManager.getInstance().incRedisKey(STAR_WARS_LOGINING_PLAYER);
		return redisSession.increase(key);
	}
	
	public long getStarWarsLoginingPlayer(String stage) {
		String key = STAR_WARS_LOGINING_PLAYER + ":" + stage;
		StatisManager.getInstance().incRedisKey(STAR_WARS_LOGINING_PLAYER);
		String value = redisSession.getString(key);
		if (HawkOSOperator.isEmptyString(value)) {
			return 0L;
		} else {
			return Long.valueOf(value);			
		}
	}
	
	/**
	 * 以防万一,删除计数。
	 */
	public void deleteStarWarsLoginingPlayer(String stage) {
		String key = STAR_WARS_LOGINING_PLAYER + ":" + stage;
		StatisManager.getInstance().incRedisKey(STAR_WARS_LOGINING_PLAYER);
		redisSession.del(key);
	}
	
	/**
	 * 获取合服年兽k值
	 */
	public long getMergeNianK() {
		String key = MERGE_NIAN_K + ":" + GsConfig.getInstance().getServerId();
		String stringK = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(stringK)) {
			return Long.parseLong(stringK);
		}
		return WorldMapConstProperty.getInstance().getMergerNianInitK() * GsConst.RANDOM_MYRIABIT_BASE; //mergeNianInitK = 50
	}

	/**
	 * 获取合服年兽k值
	 */
	public long getMergeNianK(String serverId) {
		String key = MERGE_NIAN_K + ":" + serverId;
		String stringK = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(stringK)) {
			return Long.parseLong(stringK);
		}
		return WorldMapConstProperty.getInstance().getMergerNianInitK() * GsConst.RANDOM_MYRIABIT_BASE; //mergeNianInitK = 50
	}
	
	/**
	 * 设置合服年兽k值
	 */
	public void setMergeNianK(int k) {
		String key = MERGE_NIAN_K + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, String.valueOf(k));
	}
	
	/**
	 * 获取年兽上次刷新K值 -- 改为存全局redis-202407171400从localredis挪过来的
	 */
	public int getNianLastK(String serverId) {
		StatisManager.getInstance().incRedisKey(NIAN_LAST_K);
		String key = NIAN_LAST_K + ":" + serverId;
		String stringK = redisSession.getString(key);
		int k = 0;
		if (!HawkOSOperator.isEmptyString(stringK)) {
			k = Integer.parseInt(stringK);
		}
		return k;
	}
	
	/**
	 * 设置年兽上次刷新K值 -- 改为存全局redis-202407171400从localredis挪过来的
	 */
	public void setNianLastK(String serverId, long k) {
		StatisManager.getInstance().incRedisKey(NIAN_LAST_K);
		String key = NIAN_LAST_K + ":" + serverId;
		redisSession.setString(key, String.valueOf(k));
	}
	
	public void saveDuelPower(HawkTuple2<Integer, Integer> duel) {
		String key = DUEL_CONTROL_POWER+ GsConfig.getInstance().getServerId();
		redisSession.setString(key, duel.first + "_" + duel.second);
	}

	public HawkTuple2<Integer, Integer> getDuelPower() {
		String key = DUEL_CONTROL_POWER+ GsConfig.getInstance().getServerId();
		String result = redisSession.getString(key);
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
	 * 获取装扮称号显示类型
	 * @param playerId
	 * @return
	 */
	public int getDressTitleType(String playerId) {
		String key = DRESS_TITLE_TYPE + ":" + playerId;
		String type = redisSession.getString(key);
		if (HawkOSOperator.isEmptyString(type)) {
			return 0;
		}
		return Integer.parseInt(type);
	}
	
	/**
	 * 更新装扮称号显示类型
	 * @param playerId
	 * @param type
	 */
	public void updateDressTitleType(String playerId, int type) {
		String key = DRESS_TITLE_TYPE + ":" + playerId;
		redisSession.setString(key, String.valueOf(type));
	}
	
	public void updateSupersoldierTask(String playerId, int taskId, int num) {
		String key = SUPERSOLDIER_TASK + ":" + playerId;
		StatisManager.getInstance().incRedisKey(SUPERSOLDIER_TASK);
		redisSession.hIncrBy(key, String.valueOf(taskId), num);
	}
	
	public void updateSupersoldierTaskTerminal(String playerId, int taskId, int num) {
		String key = SUPERSOLDIER_TASK + ":" + playerId;
		StatisManager.getInstance().incRedisKey(SUPERSOLDIER_TASK);
		redisSession.hSet(key, String.valueOf(taskId), String.valueOf(num));
	}
	
	public Map<Integer, Integer> getSupersoldierTaskInfo(String playerId) {
		String key = SUPERSOLDIER_TASK + ":" + playerId;
		StatisManager.getInstance().incRedisKey(SUPERSOLDIER_TASK);
		Map<String, String> map = redisSession.hGetAll(key);
		Map<Integer, Integer> taskInfo = new HashMap<Integer, Integer>();
		for (Entry<String, String> entry : map.entrySet()) {
			taskInfo.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
		}
		return taskInfo;
	}
	
	public void deleteSupersoldierTaskInfo(String playerId) {
		String key = SUPERSOLDIER_TASK + ":" + playerId;
		StatisManager.getInstance().incRedisKey(SUPERSOLDIER_TASK);
		redisSession.del(key);
	}
	
	/**
	 * 禁止玩家操作
	 * @param playerId
	 * @param banTimeSec
	 */
	public void setPlayerBanOper(String playerId, BanPlayerOperType type, int banTimeSec) {
		String key = BAN_PLAYER_OPER_KEY + ":" + playerId + ":" + type.intVal();
		long endTime = HawkTime.getMillisecond() + banTimeSec * 1000;
		redisSession.setString(key, String.valueOf(endTime), banTimeSec);
		StatisManager.getInstance().incRedisKey(BAN_PLAYER_OPER_KEY);
	}
	
	/**
	 * 获取玩家被禁止操作的结束时间
	 * @param playerId
	 * @param type
	 * @return
	 */
	public long getPlayerBanEndTime(String playerId, BanPlayerOperType type) {
		String key = BAN_PLAYER_OPER_KEY + ":" + playerId + ":" + type.intVal();
		StatisManager.getInstance().incRedisKey(BAN_PLAYER_OPER_KEY);
		String result = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(result)) {
			return Long.valueOf(result);
		}
		
		return 0;
	}
	
	/**
	 * 获取推送礼包触发次数
	 * @param playerId
	 * @return
	 */
	public Map<Integer, Integer> getTouchPushGiftTimesMap(String playerId) {
		StatisManager.getInstance().incRedisKey(TOUCH_PUSH_GIFT_TIMES);
		String key = TOUCH_PUSH_GIFT_TIMES + ":" + playerId;
		Map<String,String> map = redisSession.hGetAll(key);
		if (map == null) {
			return new HashMap<>();
		}
		
		Map<Integer, Integer> result = Maps.newHashMapWithExpectedSize(map.size());
		for(Entry<String,String> ent: map.entrySet()){
			result.put(NumberUtils.toInt(ent.getKey()), NumberUtils.toInt(ent.getValue()));
		}
		return result;
	}
	public void updateTouchPushGiftTimesMap(String playerId, int giftGroupId) {
		StatisManager.getInstance().incRedisKey(TOUCH_PUSH_GIFT_TIMES);
		String key = TOUCH_PUSH_GIFT_TIMES + ":" + playerId;
		redisSession.hIncrBy(key, String.valueOf(giftGroupId), 1);
	}
	
	/**
	 * 每日任务宝箱领取次数
	 */
	public void dailyMissionBox(String playerId) {
		StatisManager.getInstance().incRedisKey(DAILY_MISSION_BOX_COUNT);

		final String key = DAILY_MISSION_BOX_COUNT + ":" + playerId;
		redisSession.increaseBy(key, 1, 0);
	}
	
	public int getDailyMissionBox(String playerId) {
		StatisManager.getInstance().incRedisKey(DAILY_MISSION_BOX_COUNT);
		final String key = DAILY_MISSION_BOX_COUNT + ":" + playerId;		
		String value = redisSession.getString(key);
		if (HawkOSOperator.isEmptyString(value)) {
			return 0;
		}
		return Integer.parseInt(value);
	}

	public void incCumulativeMissionCount(String playerId, MissionType type, String field, int add) {
		StatisManager.getInstance().incRedisKey(CumulativeMission);
		final String key = CumulativeMission +":" + playerId+":"+ type.intValue();
		
		redisSession.hIncrBy(key, field, add);
	}
	
	public Map<String, Integer> getCumulativeMissionCount(String playerId, MissionType type){
		StatisManager.getInstance().incRedisKey(CumulativeMission);
		final String key = CumulativeMission +":" + playerId+":"+ type.intValue();
		Map<String,String> map = redisSession.hGetAll(key);
		Map<String,Integer> result = Maps.newHashMapWithExpectedSize(map.size());
		for(Entry<String,String> ent: map.entrySet()){
			result.put(ent.getKey(), NumberUtils.toInt(ent.getValue()));
		}
		return result;
	}

	
	/**
	 * 更新装备外显
	 * @param playerId
	 * @param show
	 */
	public void updateEquipStarShow(String playerId, int show) {
		try {
			StatisManager.getInstance().incRedisKey(EQUIP_STAR_SHOW);
			String key = EQUIP_STAR_SHOW + ":" + playerId;
			redisSession.setString(key, String.valueOf(show));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取装备外显
	 * @param playerId
	 */
	public int getEquipStarShow(String playerId) {
		try {
			StatisManager.getInstance().incRedisKey(EQUIP_STAR_SHOW);
			String key = EQUIP_STAR_SHOW + ":" + playerId;
			String show = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(show)) {
				return 0;
			}
			return Integer.parseInt(show);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 更新星能探索外显
	 * @param playerId
	 * @param show
	 */
	public void updateStarExploreShow(String playerId, int show) {
		try {
			StatisManager.getInstance().incRedisKey(STAR_EXPLORE_SHOW);
			String key = STAR_EXPLORE_SHOW + ":" + playerId;
			redisSession.setString(key, String.valueOf(show));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取星能探索外显
	 * @param playerId
	 */
	public int getStarExploreShow(String playerId) {
		try {
			StatisManager.getInstance().incRedisKey(STAR_EXPLORE_SHOW);
			String key = STAR_EXPLORE_SHOW + ":" + playerId;
			String show = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(show)) {
				return 0;
			}
			return Integer.parseInt(show);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 获取特惠商店好友度
	 * @param playerId
	 */
	@Deprecated
	public long getTravelShopFriendly(String playerId) {
		try {
			String key = TRAVELSHOP_FRIENDLY + ":" + playerId;
			String info = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(info)) {
				return 0L;
			}
			return Long.parseLong(info);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 更新特惠商店好友度
	 * @param playerId
	 * @param friendly
	 */
	@Deprecated
	public void updateTravelShopFriendly(String playerId, long friendly) {
		try {
			String key = TRAVELSHOP_FRIENDLY + ":" + playerId;
			redisSession.setString(key, String.valueOf(friendly));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 获取特惠商店好友度
	 * @param playerId
	 */
	public TravelShopFriendly getTravelShopFriendlyInfo(String playerId) {
		try {
			String key = TRAVELSHOP_FRIENDLY_INFO + ":" + playerId;
			String dataStr = redisSession.getString(key);
			TravelShopFriendly info = new TravelShopFriendly();
			if (!HawkOSOperator.isEmptyString(dataStr)) {
				info.mergeFrom(dataStr);
			}
			return info;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 更新特惠商店好友度
	 * @param playerId
	 * @param friendly
	 */
	public void updateTravelShopFriendlyInfo(String playerId, TravelShopFriendly info) {
		try {
			String key = TRAVELSHOP_FRIENDLY_INFO + ":" + playerId;
			redisSession.setString(key, info.serializ());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取特惠商店特权卡购买时间
	 * @param playerId
	 */
	@Deprecated
	public long getTravelShopCardTime(String playerId) {
		try {
			String key = TRAVELSHOP_CARD + ":" + playerId;
			String info = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(info)) {
				return 0L;
			}
			return Long.parseLong(info);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 更新特惠商店特权卡购买时间
	 * @param playerId
	 * @param friendly
	 */
	@Deprecated
	public void updateTravelShopCardTime(String playerId) {
		try {
			String key = TRAVELSHOP_CARD + ":" + playerId;
			redisSession.setString(key, String.valueOf(HawkTime.getMillisecond()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取国家任务
	 * @return
	 */
	public Map<String, NationMissionTemp> getNationMissions() {
		Map<String, NationMissionTemp> retMap = new ConcurrentHashMap<>();
		try {
			String redisKey = NATION_MISSIONS + ":" + GsConfig.getInstance().getServerId();
			Map<String, String> nationMissionInfos = redisSession.hGetAll(redisKey);
			if (nationMissionInfos != null) {
				for (Entry<String, String> missionInfo : nationMissionInfos.entrySet()) {
					retMap.put(missionInfo.getKey(), new NationMissionTemp(missionInfo.getValue()));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return retMap;
	}
	
	/**
	 * 重置国家任务
	 * @param nationMissions
	 */
	public void resetNationMissions(Map<String, NationMissionTemp> nationMissions) {
		try {
			String redisKey = NATION_MISSIONS + ":" + GsConfig.getInstance().getServerId();
			// 先清除之前的
			redisSession.del(redisKey);
			for (Entry<String, NationMissionTemp> missionInfo : nationMissions.entrySet()) {
				redisSession.hSet(redisKey, missionInfo.getKey(), missionInfo.getValue().toString());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取国家任务检测增加时间(每隔x秒,增加一个任务)
	 * @return
	 */
	public long getNationMissionCheckAddTime() {
		try {
			String redisKey = NATION_MISSION_CHECK_ADD_TIME + ":" + GsConfig.getInstance().getServerId();
			String info = redisSession.getString(redisKey);
			if (HawkOSOperator.isEmptyString(info)) {
				return 0L;
			}
			return Long.parseLong(info);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	/**
	 * 设置国家任务检测增加时间(每隔x秒,增加一个任务)
	 * @param checkAddTime
	 */
	public void updateNationMissionCheckAddTime(long checkAddTime) {
		try {
			String redisKey = NATION_MISSION_CHECK_ADD_TIME + ":" + GsConfig.getInstance().getServerId();
			redisSession.setString(redisKey, String.valueOf(checkAddTime));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取国家任务哪天刷新的(用于跨天检测)
	 * @return
	 */
	public int getNationMissionRefreshDay() {
		try {
			String redisKey = NATION_MISSION_REFRESH_DAY + ":" + GsConfig.getInstance().getServerId();
			String info = redisSession.getString(redisKey);
			if (HawkOSOperator.isEmptyString(info)) {
				return 0;
			}
			return Integer.parseInt(info);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 设置国家任务哪天刷新的(用于跨天检测)
	 * @param checkAddTime
	 */
	public void updateNationMissionRefreshDay(int yearDay) {
		try {
			String redisKey = NATION_MISSION_REFRESH_DAY + ":" + GsConfig.getInstance().getServerId();
			redisSession.setString(redisKey, String.valueOf(yearDay));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 设置迁服战力
	 * @param termId
	 * @param serverId
	 * @param power
	 */
	public void immgratioinPowerSet(int termId, String serverId, long power) {
		try {
			String redisKey = IMMGRATION_POWER_SET + ":" + termId;
			redisSession.hSet(redisKey, serverId, String.valueOf(power));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 增加迁服人数
	 * @param termId
	 * @param serverId
	 * @param power
	 */
	public void immgratioinNumAdd(int termId, String serverId) {
		try {
			String redisKey = IMMGRATION_NUM + ":" + termId;
			redisSession.hIncrBy(redisKey, serverId, 1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 玩家本期是否迁过服
	 * @param termId
	 * @param playerId
	 * @return
	 */
	public boolean isPlayerImmgration(int termId, String playerId) {
		try {
			String redisKey = IMMGRATION_PLAYER + ":" + termId + ":" + playerId;
			return !HawkOSOperator.isEmptyString(redisSession.getString(redisKey));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 设置玩家本期已经迁服
	 * @param termId
	 * @param playerId
	 * @return
	 */
	public boolean setPlayerImmgration(int termId, String playerId) {
		try {
			String redisKey = IMMGRATION_PLAYER + ":" + termId + ":" + playerId;
			redisSession.setString(redisKey, HawkTime.formatNowTime());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/** 获取排行榜列表
	 * 
	 * @param rankType
	 * @return */
	public Set<Tuple> getRankList(String rankKey, int maxCount) {
		return redisSession.zRevrangeWithScores(rankKey, 0, maxCount - 1, 0);
	}
	
	/**
	 * 获取国家科技数据
	 * @return
	 */
	public Map<Integer, Integer> getNationTechMap() {
		return getNationTechMap(GsConfig.getInstance().getServerId());
	}

	public Map<Integer, Integer> getNationTechMap(String serverId) {
		Map<Integer, Integer> retMap = new ConcurrentHashMap<>();
		try {
			String key = NATION_TECH + ":" + serverId;
			Map<String, String> techMap = redisSession.hGetAll(key);
			if (!MapUtils.isEmpty(techMap)) {
				for (Entry<String, String> tech : techMap.entrySet()) {
					retMap.put(Integer.valueOf(tech.getKey()), Integer.valueOf(tech.getValue()));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return retMap;
	}
	
	/**
	 * 更新国家科技数据
	 * @param techId
	 * @param level
	 */
	public void updateNationTech(int techId, int level) {
		try {
			String key = NATION_TECH + ":" + GsConfig.getInstance().getServerId();
			redisSession.hSet(key, String.valueOf(techId), String.valueOf(level));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 重置国家科技
	 */
	public void resetNationTech(Map<String, String> techMap) {
		try {
			String key = NATION_TECH + ":" + GsConfig.getInstance().getServerId();
			redisSession.del(key);
			redisSession.hmSet(key, techMap, 0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取国家科技技能数据
	 * @return
	 */
	public Map<Integer, Long> getNationTechSkillMap() {
		Map<Integer, Long> retMap = new ConcurrentHashMap<>();
		try {
			String key = NATION_TECH_SKILL + ":" + GsConfig.getInstance().getServerId();
			Map<String, String> techSkillMap = redisSession.hGetAll(key);
			if (!MapUtils.isEmpty(techSkillMap)) {
				for (Entry<String, String> tech : techSkillMap.entrySet()) {
					retMap.put(Integer.valueOf(tech.getKey()), Long.valueOf(tech.getValue()));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return retMap;
	}
	
	/**
	 * 更新国家科技技能数据
	 * @param techId
	 * @param level
	 */
	public void updateNationTechSkill(int techId, long useSkillTime) {
		try {
			String key = NATION_TECH_SKILL + ":" + GsConfig.getInstance().getServerId();
			redisSession.hSet(key, String.valueOf(techId), String.valueOf(useSkillTime));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 清除国家技能
	 */
	public void clearNationTechSkill() {
		try {
			String key = NATION_TECH_SKILL + ":" + GsConfig.getInstance().getServerId();
			redisSession.del(key);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取国家科技研究信息
	 * @return
	 */
	public NationTechResearchTemp getNationTechResearchInfo() {
		try {
			String key = NATION_TECH_RESEARCH + ":" + GsConfig.getInstance().getServerId();
			String researchInfo = redisSession.getString(key);
			if (!HawkOSOperator.isEmptyString(researchInfo)) {
				return SerializeHelper.getValue(NationTechResearchTemp.class, researchInfo, SerializeHelper.ATTRIBUTE_SPLIT);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 更新国家科技研究信息
	 * @return
	 */
	public void updateNationTechResearchInfo(NationTechResearchTemp info) {
		try {
			String key = NATION_TECH_RESEARCH + ":" + GsConfig.getInstance().getServerId();
			String infoStr = "";
			if (info != null) {
				infoStr = SerializeHelper.toSerializeString(info, SerializeHelper.ATTRIBUTE_SPLIT);
			}
			redisSession.setString(key, infoStr);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取国家科技研究值
	 * @return
	 */
	public int getNationTechValue(String serverId) {
		try {
			String key = NATION_TECH_VALUE + ":" + serverId;
			String value = redisSession.getString(key);
			if (!HawkOSOperator.isEmptyString(value)) {
				return Integer.parseInt(value);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 更新国家科技研究值
	 */
	public void updateNationTechValue(int value) {
		String key = NATION_TECH_VALUE + ":" + GsConfig.getInstance().getServerId();
		redisSession.setString(key, String.valueOf(value));
	}
	
	/**
	 * 获取每日科技值
	 * @return
	 */
	public HawkTuple2<Integer, Integer> getNationTechDailyInfo() {
		try {
			String key = NATION_TECH_DAIL + ":" + GsConfig.getInstance().getServerId();
			String value = redisSession.getString(key);
			if (!HawkOSOperator.isEmptyString(value)) {
				String[] split = value.split("_");
				return new HawkTuple2<Integer, Integer>(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return new HawkTuple2<Integer, Integer>(0, 0);
	}
	
	public void updateNationTechDaily(int dayMark, int tech) {
		String key = NATION_TECH_DAIL + ":" + GsConfig.getInstance().getServerId();
		String value = dayMark + "_" + tech;
		redisSession.setString(key, String.valueOf(value));
	}
	
	/**
	 * 更新国家建筑等级
	 * @param buildId
	 * @param lvl
	 */
	public void updateNationBuildLvl(int buildId, int lvl) {
		String key = NATION_BUILDING_LEVEL + ":" + GsConfig.getInstance().getServerId();
		redisSession.hSet(key, String.valueOf(buildId), String.valueOf(lvl));
	}
	
	/**
	 * 获取对应服的国家建筑等级
	 * @param serverId
	 * @param buildId
	 * @return
	 */
	public int getServerNationBuildLvl(String serverId, int buildId) {
		String key = NATION_BUILDING_LEVEL + ":" + serverId;
		String lvl = redisSession.hGet(key, String.valueOf(buildId));
		if(lvl == null) {
			return 0;
		}
		return Integer.parseInt(lvl);
	}
	
	/**
	 * 更新移民记录
	 * @param termId
	 * @param playerId
	 */
	public void updateImmgrationRecord(int termId, String playerId, String info) {
		try {
			String key = IMMGRATION_RECORD + ":" + termId;
			redisSession.hSet(key, playerId, info);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新移民记录
	 * @param termId
	 * @param playerId
	 */
	public void updateBackImmgrationRecord(int termId, String playerId, String info) {
		try {
			int day = HawkTime.getYyyyMMddIntVal();
			String key = BACK_IMMGRATION_RECORD + ":" + termId;
			String playerKey = playerId + ":" + day;
			redisSession.hSet(key, playerKey, info);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 更新移民记录
	 * @param termId
	 * @param playerId
	 */
	public String getBackImmgrationRecord(int termId, String playerId) {
		try {
			int day = HawkTime.getYyyyMMddIntVal();
			String key = BACK_IMMGRATION_RECORD + ":" + termId;
			String playerKey = playerId + ":" + day;
			String rlt = redisSession.hGet(key, playerKey);
			return rlt;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 获取移民钻石处理
	 * @param playerId
	 * @return
	 */
	public int getImmgrationDiamonds(String playerId) {
		int diamonds = 0;
		try {
			String key = IMMGRATION_DIAMONDS + ":" + playerId;
			String value = redisSession.getString(key);
			if (!HawkOSOperator.isEmptyString(value)) {
				diamonds = Integer.parseInt(value);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return diamonds;
	}
	
	/**
	 * 删除移民钻石
	 * @param playerId
	 */
	public void removeImmgrationDiamonds(String playerId) {
		String key = IMMGRATION_DIAMONDS + ":" + playerId;
		redisSession.del(key);
	}
	
	/**
	 * 更新移民钻石
	 * @param playerId
	 * @return
	 */
	public void updateImmgrationDiamonds(String playerId, int count) {
		try {
			String key = IMMGRATION_DIAMONDS + ":" + playerId;
			redisSession.setString(key, String.valueOf(count));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 移民日志
	 * @param termId
	 * @param log
	 */
	public void addImmgrationLog(int termId, JSONObject immgrationLog) {
		try {
			String key = IMMGRATION_LOG + ":" + termId;
			redisSession.lPush(key, 0, immgrationLog.toJSONString());
			addPlayerImmgrationLog(immgrationLog);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 存储个人移民记录
	 * @param immgrationLog
	 */
	public void addPlayerImmgrationLog(JSONObject immgrationLog) {
		String playerLogKey = IMMGRATION_LOG + ":" + immgrationLog.getString("playerId");
		redisSession.hSet(playerLogKey, immgrationLog.getString("tarServer"), immgrationLog.toJSONString());
	}
	
	/**
	 * 获取官职id 跨服使用
	 * @param playerId
	 * @return
	 */
	public int getPlayerOfficerId(String playerId) {
		try {
			String key = PLAYER_OFFICER_CACHE + ":" + playerId;
			String officerId = redisSession.getString(key);
			if (!HawkOSOperator.isEmptyString(officerId)) {
				return Integer.parseInt(officerId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 玩家官职 跨服使用
	 * @param playerId
	 * @return
	 */
	public void updatePlayerOfficerId(String playerId, int officerId) {
		try {
			String key = PLAYER_OFFICER_CACHE + ":" + playerId;
			redisSession.setString(key, String.valueOf(officerId), GsConst.DAY_SECONDS * 3);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 删除玩家官职
	 * @param playerId
	 */
	public void removePlayerOfficerId(String playerId) {
		try {
			String key = PLAYER_OFFICER_CACHE + ":" + playerId;
			redisSession.del(key);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取官职id 跨服使用
	 * @param playerId
	 * @return
	 */
	public Set<Integer> getPlayerOfficerIdSet(String playerId) {
		Set<Integer> rlt = new HashSet<>();
		try {
			String key = PLAYER_OFFICER_SET_CACHE + ":" + playerId;
			Set<String> set = redisSession.sMembers(key);
			if (Objects.nonNull(set) && !set.isEmpty()) {
				for (String str : set) {
					rlt.add(Integer.parseInt(str));
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return rlt;
	}
	
	/**
	 * 玩家官职 跨服使用
	 * @param playerId
	 * @return
	 */
	public void addPlayerOfficerIdSet(String playerId, int officerId) {
		try {
			String key = PLAYER_OFFICER_SET_CACHE + ":" + playerId;
			redisSession.sAdd(key, GsConst.DAY_SECONDS * 3, String.valueOf(officerId));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 玩家官职 跨服使用
	 * @param playerId
	 * @return
	 */
	public void updatePlayerOfficerIdSet(String playerId, Set<Integer> officerIds) {
		if (Objects.isNull(officerIds) || officerIds.isEmpty()) {
			return;
		}
		try {
			List<String> list = new ArrayList<>();
			officerIds.forEach(o->list.add(String.valueOf(o)));
			String key = PLAYER_OFFICER_SET_CACHE + ":" + playerId;
			redisSession.sAdd(key, GsConst.DAY_SECONDS * 3, list.toArray(new String[list.size()]));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 删除玩家官职
	 * @param playerId
	 */
	public void removePlayerOfficerIdSet(String playerId,int officerId) {
		try {
			String key = PLAYER_OFFICER_SET_CACHE + ":" + playerId;
			redisSession.sRem(key, String.valueOf(officerId));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 删除玩家官职
	 * @param playerId
	 */
	public void removePlayerOfficerIdSet(String playerId) {
		try {
			String key = PLAYER_OFFICER_SET_CACHE + ":" + playerId;
			redisSession.del(key);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新至尊vip信息
	 * 
	 * @param superVipInfo
	 */
	public void updateSuperVipInfo(PlayerSuperVipInfo superVipInfo) {
		if (superVipInfo == null || HawkOSOperator.isEmptyString(superVipInfo.getPlayerId())) {
			return;
		}
		
		String key = SUPER_VIP + ":" + superVipInfo.getPlayerId();
		redisSession.setString(key, JSONObject.toJSONString(superVipInfo), GsConst.MONTH_SECONDS);
	}
	
	/**
	 * 获取Vip至尊信息
	 * @param playerId
	 * @return
	 */
	public PlayerSuperVipInfo getSuperVipInfo(String playerId) {
		String key = SUPER_VIP + ":" + playerId;
		String info = redisSession.getString(key);
		if (HawkOSOperator.isEmptyString(info)) {
			return null;
		}
		
		return JSONObject.parseObject(info, PlayerSuperVipInfo.class);
	}

	/**
	 * 获取跨服礼包信息
	 * @return
	 */
	public Map<Integer, Integer> getCrossGiftInfos() {
		Map<Integer, Integer> retMap = new HashMap<>();
		int termId = CrossActivityService.getInstance().getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		int groupId = CrossActivityService.getInstance().getGroupId(serverId);
		String key = CROSS_GIFT + ":" + termId + ":" + groupId;
		
		Map<String, String> infos = redisSession.hGetAll(key);
		if (infos == null) {
			return retMap;
		}
		for (Entry<String, String> info : infos.entrySet()) {
			retMap.put(Integer.valueOf(info.getKey()), Integer.valueOf(info.getValue()));
		}
		return retMap;
	}
	
	/**
	 * 获取跨服礼包已经发放的数量
	 * @param giftId
	 * @return
	 */
	public int getCrossGiftSendCount(int giftId) {
		int termId = CrossActivityService.getInstance().getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		int groupId = CrossActivityService.getInstance().getGroupId(serverId);
		String key = CROSS_GIFT + ":" + termId + ":" + groupId;
		
		String countStr = redisSession.hGet(key, String.valueOf(giftId));
		if (HawkOSOperator.isEmptyString(countStr)) {
			return 0;
		}
		return Integer.parseInt(countStr);
	}
	
	/**
	 * 更新跨服礼包信息
	 * @param giftId
	 * @param sendCount
	 */
	public void updateCrossGiftInfo(int giftId, int sendCount) {
		int termId = CrossActivityService.getInstance().getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		int groupId = CrossActivityService.getInstance().getGroupId(serverId);
		String key = CROSS_GIFT + ":" + termId + ":" + groupId;
		redisSession.hIncrBy(key, String.valueOf(giftId), sendCount);
	}
	
	/**
	 * 添加礼包记录
	 */
	public void addCrossGiftRecord(CrossGiftRecord.Builder record) {
		int termId = CrossActivityService.getInstance().getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		int groupId = CrossActivityService.getInstance().getGroupId(serverId);
		String key = CROSS_GIFT_RECORD + ":" + termId + ":" + groupId;
		
		redisSession.lPush(key, 0, JsonFormat.printToString(record.build()));		
	}
	
	/**
	 * 跨服礼包记录
	 */
	public List<CrossGiftRecord.Builder> getAllCrossGiftRecord() {
		int termId = CrossActivityService.getInstance().getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		int groupId = CrossActivityService.getInstance().getGroupId(serverId);
		String key = CROSS_GIFT_RECORD + ":" + termId + ":" + groupId;
		
		List<CrossGiftRecord.Builder> records = new ArrayList<>();
		List<String> infos = redisSession.lRange(key, 0, -1, 0);
		for (String info : infos) {
			CrossGiftRecord.Builder record = CrossGiftRecord.newBuilder();
			try {
				JsonFormat.merge(info, record);
				records.add(record);
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
		}
		return records;
	}
	
	/**
	 * 添加礼包记录
	 */
	public void addCrossGiftPlayerRecord(int giftType, String targetPlayerId) {
		int termId = CrossActivityService.getInstance().getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		int groupId = CrossActivityService.getInstance().getGroupId(serverId);
		String key = CROSS_GIFT_PLAYER_RECORD + ":" + termId + ":" + groupId + ":" + giftType;
		redisSession.hSet(key, targetPlayerId, HawkTime.formatNowTime());		
	}
	
	/**
	 * 跨服礼包记录
	 */
	public boolean existCrossGiftPlayerRecord(int giftType, String targetPlayerId) {
		int termId = CrossActivityService.getInstance().getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		int groupId = CrossActivityService.getInstance().getGroupId(serverId);
		String key = CROSS_GIFT_PLAYER_RECORD + ":" + termId + ":" + groupId + ":" + giftType;
		return redisSession.hExists(key, targetPlayerId, 0);
	}
	
	public Set<String> getAllCrossGiftPlayer(int giftType) {
		int termId = CrossActivityService.getInstance().getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		int groupId = CrossActivityService.getInstance().getGroupId(serverId);
		String key = CROSS_GIFT_PLAYER_RECORD + ":" + termId + ":" + groupId + ":" + giftType;
		Map<String, String> hGetAll = redisSession.hGetAll(key);
		if (hGetAll == null) {
			return new HashSet<>();
		}
		return hGetAll.keySet();
	}
	
	/**
	 * 远征要塞开启标记
	 */
	public Map<Integer, Integer> getCrossFortressOpenMap() {
		String serverId = GsConfig.getInstance().getServerId();
		String key = CROSS_FORTRESS_OPEN + ":" + serverId;
		
		Map<Integer, Integer> retMap = new HashMap<>();
		Map<String, String> hGetAll = redisSession.hGetAll(key);
		if (hGetAll != null) {
			for (Entry<String, String> info : hGetAll.entrySet()) {
				retMap.put(Integer.valueOf(info.getKey()), Integer.valueOf(info.getValue()));
			}
		}
		return retMap;
	}
	
	/**
	 * 更新远征要塞开启标记
	 */
	public void updateCrossFortressOpen(int state) {
		int termId = CrossActivityService.getInstance().getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		String key = CROSS_FORTRESS_OPEN + ":" + serverId;
		
		redisSession.hSet(key, String.valueOf(termId), String.valueOf(state));
	}
	
	/**
	 * 更新远征要塞占领数量
	 * @param pointId
	 * @param serverId
	 */
	public void updateCrossFortressOccupyCount(int pointId, String ownerServerId) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String serverId = GsConfig.getInstance().getServerId();
			int groupId = CrossActivityService.getInstance().getGroupId(serverId);
			String key = CROSS_FORTRESS_OCC_COUNT + ":" + termId + ":" + groupId;
			String key2 = serverId + ":" + pointId;
			if (ownerServerId == null) {
				ownerServerId = "";
			}
			redisSession.hSet(key, key2, ownerServerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取远征要塞占领服
	 * @param pointId
	 * @return
	 */
	public String getCrossFortressOccupyServerId(int pointId) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String serverId = GsConfig.getInstance().getServerId();
			int groupId = CrossActivityService.getInstance().getGroupId(serverId);
			String key = CROSS_FORTRESS_OCC_COUNT + ":" + termId + ":" + groupId;
			String key2 = serverId + ":" + pointId;
			String info = redisSession.hGet(key, key2);
			if (info == null) {
				info = "";
			}
			return info;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return "";
	}
	
	/**
	 * 获取要塞占领数量
	 * @return
	 */
	public Map<String, Integer> getCrossFortressOccupyCount() {
		Map<String, Integer> retMap = new HashMap<>();
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String serverId = GsConfig.getInstance().getServerId();
			int groupId = CrossActivityService.getInstance().getGroupId(serverId);
			String key = CROSS_FORTRESS_OCC_COUNT + ":" + termId + ":" + groupId;;
			Map<String, String> hGetAll = redisSession.hGetAll(key);
			if (hGetAll == null) {
				return retMap;
			}
			for (Entry<String, String> info : hGetAll.entrySet()) {
				String thisServerId = info.getValue();
				if (HawkOSOperator.isEmptyString(thisServerId)) {
					continue;
				}
				int beforeCount = retMap.getOrDefault(thisServerId, 0);
				retMap.put(thisServerId, beforeCount + 1);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return retMap;
	}
	
	/**
	 * 更新能量塔占领数量
	 * @param pointId
	 * @param serverId
	 */
	public void addCrossPylonOccupyCount(String serverId, int add) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_PYLON_OCC_COUNT + ":" + termId;
			redisSession.hIncrBy(key, serverId, add);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取能量塔占领数量
	 * @param pointId
	 * @return
	 */
	public int getCrossPylonOccupyCount(String serverId) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_PYLON_OCC_COUNT + ":" + termId;
			String retStr = redisSession.hGet(key, serverId);
			if (retStr == null) {
				return 0;
			}
			return Integer.parseInt(retStr);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 添加跨服出战联盟
	 * @param guildId
	 */
	public void addCrossFightGuild(String guildId) {
		try {
			String serverId = GsConfig.getInstance().getServerId();
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_FIGHT_GUILD_INFO + ":" + termId + ":" + serverId;
			redisSession.hSet(key, guildId, "1");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 删除跨服出战联盟
	 * @param guildId
	 */
	public void delCrossFightGuild(String guildId) {
		try {
			String serverId = GsConfig.getInstance().getServerId();
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_FIGHT_GUILD_INFO + ":" + termId + ":" + serverId;
			redisSession.hDel(key, guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 是否是跨服出战联盟
	 * @param serverId
	 * @param guildId
	 */
	public boolean isCrossFightGuild(String serverId, String guildId) {
		try {
			if (HawkOSOperator.isEmptyString(serverId) || HawkOSOperator.isEmptyString(guildId)) {
				return false;
			}
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_FIGHT_GUILD_INFO + ":" + termId + ":" + serverId;
			String info = redisSession.hGet(key, guildId);
			return !HawkOSOperator.isEmptyString(info);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 获取所有跨服出战联盟
	 * @return
	 */
	public List<String> getAllCrossFightGuild() {
		List<String> guildIdList = new ArrayList<>();
		try {
			String serverId = GsConfig.getInstance().getServerId();
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_FIGHT_GUILD_INFO + ":" + termId + ":" + serverId;
			Map<String, String> hGetAll = redisSession.hGetAll(key);
			if (hGetAll != null) {
				for (String guildId : hGetAll.keySet()) {
					guildIdList.add(guildId);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return guildIdList;
	}
	
	/**
	 * 添加战时总司令
	 * @param playerId
	 */
	public void addCrossFightPresident(String playerId) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String serverId = GsConfig.getInstance().getServerId();
			int groupId = CrossActivityService.getInstance().getGroupId(serverId);
			String key = CROSS_FIGHT_PRESIDENT + ":" + termId + ":" + groupId;
			redisSession.hSet(key, serverId, playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取战时总司令
	 * @return
	 */
	public String getCrossFightPresident() {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String serverId = GsConfig.getInstance().getServerId();
			int groupId = CrossActivityService.getInstance().getGroupId(serverId);
			String key = CROSS_FIGHT_PRESIDENT + ":" + termId + ":" + groupId;
			return redisSession.hGet(key, serverId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	public String getCrossFightPresident(String serverId) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			int groupId = CrossActivityService.getInstance().getGroupId(serverId);
			String key = CROSS_FIGHT_PRESIDENT + ":" + termId + ":" + groupId;
			return redisSession.hGet(key, serverId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 删除战时总司令
	 * @return
	 */
	public void delCrossFightPresident() {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String serverId = GsConfig.getInstance().getServerId();
			int groupId = CrossActivityService.getInstance().getGroupId(serverId);
			String key = CROSS_FIGHT_PRESIDENT + ":" + termId + ":" + groupId;
			redisSession.hDel(key, serverId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取跨服战略点
	 * @param serverId
	 * @return
	 */
	public long getCrossTalentPoint(String serverId) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_TALENT_POINT + ":" + termId + ":" + serverId;
			String getStr = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(getStr)) {
				return 0L;
			}
			return Long.parseLong(getStr);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	/**
	 * 添加跨服战略点
	 * @param serverId
	 * @param addPoint
	 */
	public void addCrossTalentPoint(String serverId, int addPoint) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_TALENT_POINT + ":" + termId + ":" + serverId;
			redisSession.increaseBy(key, addPoint, 0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * hinc接口，直接返回最新值
	 * 
	 * @param key
	 * @param field
	 * @param addValue
	 * @return
	 */
	public long hIncBy(String key, String field, int addValue) {
		Jedis jedis = getRedisSession().getJedis();
		if (jedis != null) {
			try {
				return jedis.hincrBy(key, field, addValue);
			} catch (Exception e) {
				HawkException.catchException(e);
			} finally {
				jedis.close();
			}
		}
		
		return 0;
	}

	/**
	 * 跨服资源宝箱获取个数增加
	 * @param playerId
	 * @param termId
	 * @param count
	 */
	public void incrCrossResourceBoxGetCount(String playerId, int termId,int count) {
		StatisManager.getInstance().incRedisKey(CROSS_RESOURCE_SPREE_BOX_GET_COUNT);
		String key = CROSS_RESOURCE_SPREE_BOX_GET_COUNT + ":" + termId + ":" + playerId;
		redisSession.increaseBy(key, count,  (int)TimeUnit.DAYS.toSeconds(30));
	}
	
	
	/**
	 * 跨服资源宝箱获取数量
	 * @param playerId
	 * @param termId
	 * @return
	 */
	public int getCrossResourceBoxGetCount(String playerId, int termId){
		StatisManager.getInstance().incRedisKey(CROSS_RESOURCE_SPREE_BOX_GET_COUNT);
		String key = CROSS_RESOURCE_SPREE_BOX_GET_COUNT + ":" + termId + ":" + playerId;
		String count = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(count)) {
			return Integer.valueOf(count);
		}
		return 0;
	}
	
	/**
	 * 保存跨服活动记录
	 * @param record
	 */
	public void saveCrossActivityRecord(CrossActivityRecord record){
		StatisManager.getInstance().incRedisKey(CROSS_ACTIVITY_RECORD);
		String key = CROSS_ACTIVITY_RECORD + ":" + record.getServerId();
		redisSession.lPush(key, 90 * 24 * 3600, record.serializ());
	}
	
	/**
	 * 获取跨服活动记录
	 * @param roomId
	 * @return
	 */
	public List<CrossActivityRecord> getCrossActivityRecord(String serverId,int size) {
		StatisManager.getInstance().incRedisKey(CROSS_ACTIVITY_RECORD);
		String key = CROSS_ACTIVITY_RECORD + ":" + serverId;
		List<String> result = redisSession.lRange(key, 0, size, 90 * 24 * 3600);
		if (result == null) {
			return Collections.emptyList();
		}
		
		List<CrossActivityRecord> list = new ArrayList<>();
		for(String str : result){
			try {
				CrossActivityRecord record = new CrossActivityRecord();
				record.mergeFrom(str);
				list.add(record);
			} catch (Exception e) {
				HawkException.catchException(e);
				continue;
			}
		}
		StatisManager.getInstance().incRedisKey(CROSS_ACTIVITY_RECORD);
		return list;
	}
	
	/**
	 * 更新9级矿刷新结束时间
	 * @param serverId
	 * @param endTime
	 */
	public void updateSpecialResRefresh(String serverId, long endTime) {
		String key = CROSS_REFRESH_SPECIAL_RES + ":" + serverId;
		redisSession.setString(key, String.valueOf(endTime));
	}
	
	/**
	 * 获取9级矿刷新结束时间
	 * @param serverId
	 * @return
	 */
	public long getSpecialResRefresh(String serverId) {
		String key = CROSS_REFRESH_SPECIAL_RES + ":" + serverId;
		String value = redisSession.getString(key);
		if (HawkOSOperator.isEmptyString(value)) {
			return 0L;
		}
		return Long.parseLong(value);
	}
	
	/**
	 * 删除9级矿刷新结束时间
	 * @param serverId
	 * @param endTime
	 */
	public void deleteSpecialResRefresh(String serverId) {
		String key = CROSS_REFRESH_SPECIAL_RES + ":" + serverId;
		redisSession.del(key);
	}
	
	/**
	 * 设置王战胜利区服
	 * @param serverId
	 * @return
	 */
	public void updateCrossWinServer(String serverId) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_WIN_SERVER + ":" + termId;
			redisSession.hSet(key, serverId, HawkTime.formatNowTime());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 设置王战胜利区服
	 * @param serverId
	 * @return
	 */
	public Map<String, String> getCrossWinServer(int termId) {
		try {
			String key = CROSS_WIN_SERVER + ":" + termId;
			Map<String, String> map = redisSession.hGetAll(key);
			return map;
		} catch (Exception e) {
			HawkException.catchException(e);
			return null;
		}
	}
	
	/**
	 * 是否是王战胜利区服
	 * @param serverId
	 * @return
	 */
	public boolean isCrossWinServer(String serverId) {
		try {
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_WIN_SERVER + ":" + termId;
			return redisSession.hExists(key, serverId, 0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 设置跨服可出战联盟
	 * @param guilds
	 * @return
	 */
	public void setCrossCanFightGuilds(Map<String, String> guilds) {
		try {
			String serverId = GsConfig.getInstance().getServerId();
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_CAN_FIGHT_GUILD + ":" + termId + ":" + serverId;
			redisSession.hmSet(key, guilds, 0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取跨服可出战联盟
	 */
	public Set<String> getCrossCanFightGuilds() {
		try {
			String serverId = GsConfig.getInstance().getServerId();
			int termId = CrossActivityService.getInstance().getTermId();
			String key = CROSS_CAN_FIGHT_GUILD + ":" + termId + ":" + serverId;
			Map<String, String> hGetAll = redisSession.hGetAll(key);
			if (hGetAll == null) {
				return new HashSet<>();
			}
			return hGetAll.keySet();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return new HashSet<>();
	}
	
	/**
	 * 获取活动版本号
	 * @param activityId
	 * @return
	 */
	public Map<String, String> getAllActiviyCfgVersion() {
		Map<String, String> retMap = new ConcurrentHashMap<>();
		String key = ACTIVITY_VERSION + ":" + GsConfig.getInstance().getServerId();
		Map<String, String> versionMap = redisSession.hGetAll(key);
		if (versionMap == null) {
			return new ConcurrentHashMap<>();
		}
		for (Entry<String, String> versionInfo : versionMap.entrySet()) {
			retMap.put(versionInfo.getKey(), versionInfo.getValue());
		}
		return retMap;
	}
	
	/**
	 * 更新活动配置版本信息
	 * @param activityId
	 * @param version
	 * @return
	 */
	public void updateActivityCfgVersion(String activityId, String version) {
		String key = ACTIVITY_VERSION + ":" + GsConfig.getInstance().getServerId();
		redisSession.hSet(key, activityId, version);
	}

	/**
	 * 获取跨服邀请
	 * @param serverId
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public long getCrossInvite(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_INVITE + ":" + serverId + ":" + termId;
			String hGet = redisSession.hGet(key, guildId);
			if (!HawkOSOperator.isEmptyString(hGet)) {
				return Long.parseLong(hGet);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	/**
	 * 获取所有跨服申请
	 * @param serverId
	 * @param termId
	 * @return
	 */
	public Map<String, String> getAllCrossInviteTime(String serverId, int termId) {
		try {
			String key = CROSS_INVITE_TIME + ":" + serverId + ":" + termId;
			Map<String, String> hGetAll = redisSession.hGetAll(key);
			if (hGetAll != null) {
				return hGetAll;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return new HashMap<>();
	}
	
	/**
	 * 添加跨服邀请
	 * @param serverId
	 * @param termId
	 * @param guildId
	 */
	public void addCrossInvite(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_INVITE + ":" + serverId + ":" + termId;
			redisSession.hSet(key, guildId, String.valueOf(HawkTime.getMillisecond()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 移除跨服邀请
	 * @param serverId
	 * @param termId
	 * @param guildId
	 */
	public void removeCrossInvite(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_INVITE + ":" + serverId + ":" + termId;
			redisSession.hDel(key, guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新跨服邀请时间
	 */
	public void updateCrossInviteTime(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_INVITE_TIME + ":" + serverId + ":" + termId;
			redisSession.hSet(key, guildId, String.valueOf(HawkTime.getMillisecond()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取跨服邀请时间
	 * @param serverId
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public long getCrossInviteTime(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_INVITE_TIME + ":" + serverId + ":" + termId;
			String hGet = redisSession.hGet(key, guildId);
			if (!HawkOSOperator.isEmptyString(hGet)) {
				return Long.parseLong(hGet);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	/**
	 * 获取跨服申请
	 * @param serverId
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public long getCrossApply(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_APPLY + ":" + serverId + ":" + termId;
			String hGet = redisSession.hGet(key, guildId);
			if (!HawkOSOperator.isEmptyString(hGet)) {
				return Long.parseLong(hGet);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	/**
	 * 获取所有跨服申请
	 * @param serverId
	 * @param termId
	 * @return
	 */
	public Set<String> getAllCrossApply(String serverId, int termId) {
		try {
			String key = CROSS_APPLY + ":" + serverId + ":" + termId;
			Set<String> hKeys = redisSession.hKeys(key, 0);
			if (hKeys != null) {
				return hKeys;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return new HashSet<>();
	}
	
	/**
	 * 添加跨服申请
	 * @param serverId
	 * @param termId
	 * @param guildId
	 */
	public void addCrossApply(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_APPLY + ":" + serverId + ":" + termId;
			redisSession.hSet(key, guildId, String.valueOf(HawkTime.getMillisecond()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 移除跨服申请
	 * @param serverId
	 * @param termId
	 * @param guildId
	 */
	public void removeCrossApply(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_APPLY + ":" + serverId + ":" + termId;
			redisSession.hDel(key, guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新跨服申请时间
	 */
	public void updateCrossApplyTime(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_APPLY_TIME + ":" + serverId + ":" + termId;
			redisSession.hSet(key, guildId, String.valueOf(HawkTime.getMillisecond()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取跨服申请时间
	 * @param serverId
	 * @param termId
	 * @param guildId
	 * @return
	 */
	public long getCrossApplyTime(String serverId, int termId, String guildId) {
		try {
			String key = CROSS_APPLY_TIME + ":" + serverId + ":" + termId;
			String hGet = redisSession.hGet(key, guildId);
			if (!HawkOSOperator.isEmptyString(hGet)) {
				return Long.parseLong(hGet);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	/**
	 * 获取跨服盟总推进度条信息
	 */
	public PresidentCrossRateInfo getCrossRateInfo(String serverId, int termId) {
		String key = CROSS_RATE_INFO + ":" + serverId + ":" + termId;
		return new PresidentCrossRateInfo(redisSession.getString(key));
	}
	
	/**
	 * 更新跨服盟总推进度条信息
	 * @param serverId
	 * @param termId
	 * @param rateInfo
	 */
	public void updateCrossRateInfo(String serverId, int termId, PresidentCrossRateInfo rateInfo) {
		String key = CROSS_RATE_INFO + ":" + serverId + ":" + termId;
		redisSession.setString(key, rateInfo.serialize());
	}
	
	
	/**
	 * 获取跨服盟总占领时间信息
	 */
	public PresidentCrossAccumulateInfo getPresidentCrossAccumulateInfo(String serverId, int termId) {
		String key = CROSS_OCCUPY_INFO + ":" + serverId + ":" + termId;
		return new PresidentCrossAccumulateInfo(redisSession.getString(key));
	}
	
	/**
	 * 更新跨服盟总占领时间信息
	 * @param serverId
	 * @param termId
	 * @param info
	 */
	public void updatePresidentCrossAccumulateInfo(String serverId, int termId, PresidentCrossAccumulateInfo info) {
		String key = CROSS_OCCUPY_INFO + ":" + serverId + ":" + termId;
		redisSession.setString(key, info.serialize());
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

	/**
	 * 获取赠送时间限制道具
	 * @param playerId
	 * @return
	 */
	public Map<Integer, Long> getSendTimeLimitTool(String playerId) {
		Map<Integer, Long> retMap = new HashMap<>();
		try {
			String key = SEND_TIME_LIMIT_TOOL + ":" + playerId;
			Map<String, String> hGetAll = redisSession.hGetAll(key);
			if (hGetAll != null) {
				retMap = MapUtil.map2Map(hGetAll, MapUtil.STRING2INTEGER, MapUtil.STRING2LONG);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return retMap;
	}
	
	/**
	 * 更新赠送时间限制道具
	 * @param playerId
	 * @param tools
	 */
	public void updateSendTimeLimitTool(String playerId, Map<Integer, Long> tools) {
		try {
			String key = SEND_TIME_LIMIT_TOOL + ":" + playerId;
			Map<String, String> map2Map = MapUtil.map2Map(tools, MapUtil.INTEGER2STRING, MapUtil.LONG2STRING);
			redisSession.hmSet(key, map2Map, 0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 添加红点开关
	 * @param type
	 */
	public void addRedDotSwitch(int type) {
		StatisManager.getInstance().incRedisKey(RED_DOT_SWITCH);
		String key = RED_DOT_SWITCH + ":" + GsConfig.getInstance().getServerId();
		redisSession.hSet(key, String.valueOf(type), String.valueOf(HawkTime.getMillisecond()));
	}
	
	/**
	 * 删除红点开关
	 * @param type
	 */
	public void delRedDotSwitch(int type) {
		StatisManager.getInstance().incRedisKey(RED_DOT_SWITCH);
		String key = RED_DOT_SWITCH + ":" + GsConfig.getInstance().getServerId();
		redisSession.hDel(key, String.valueOf(type));
	}
	
	/**
	 * 获取所有的红点开关
	 */
	public Map<Integer, Long> getAllRedDotSwitch() {
		StatisManager.getInstance().incRedisKey(RED_DOT_SWITCH);
		String key = RED_DOT_SWITCH + ":" + GsConfig.getInstance().getServerId();
		Map<String, String> map = redisSession.hGetAll(key);
		if (map.isEmpty()) {
			return Collections.emptyMap();
		} 
		
		Map<Integer, Long> result = new HashMap<Integer, Long>();
		for (Entry<String, String> entry : map.entrySet()) {
			result.put(Integer.parseInt(entry.getKey()), Long.valueOf(entry.getValue()));
		}
		
		return result;
	}
	
	/**
	 * 获取赠送信使礼包信息
	 * @param playerId
	 * @return
	 */
	public Map<Integer, Integer> getSendDressGiftInfo(String playerId) {
		try {
			String key = SEND_DRESS_GIFT + ":" + HawkTime.getYearWeek() + ":" + playerId;
			Map<String, String> info = redisSession.hGetAll(key);
			if (!MapUtils.isEmpty(info)) {
				return MapUtil.toIntegerInteger(info);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return new HashMap<>();
	}
	
	/**
	 * 更新赠送信使礼包信息
	 * @param playerId
	 */
	public void updateSendDressGiftInfo(String playerId, int giftId) {
		Map<Integer, Integer> sendDressGiftInfo = getSendDressGiftInfo(playerId);
		int beforeCount = sendDressGiftInfo.getOrDefault(giftId, 0);
		sendDressGiftInfo.put(giftId, beforeCount + 1);
		try {
			String key = SEND_DRESS_GIFT + ":" + HawkTime.getYearWeek() + ":" + playerId;
			redisSession.hmSet(key, MapUtil.toStringString(sendDressGiftInfo), GsConst.DAY_SECONDS * 7);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取超值礼包每月限购标记
	 * @param playerId
	 * @return
	 */
	public int getSuperGiftMonthMark(String playerId) {
		try {
			String key = SUPER_GIFT_MONTH_MARK + ":" +  playerId;
			String value = redisSession.getString(key);
			if (!HawkOSOperator.isEmptyString(value)) {
				return Integer.parseInt(value);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 更新超值礼包每月限购标记
	 * @param playerId
	 */
	public void updateSuperGiftMonthMark(String playerId) {
		try {
			int month = HawkTime.getCalendar(false).get(Calendar.MONTH);
			String key = SUPER_GIFT_MONTH_MARK + ":" +  playerId;
			redisSession.setString(key, String.valueOf(month));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取英雄档案开启奖励时间
	 * @param playerId
	 * @return
	 */
	public String getHeroArchiveOpenAward(String playerId) {
		String key = HERO_ARCHIVE_OPEN_AWARD + ":" + playerId;
		return redisSession.getString(key);
	}
	
	/**
	 * 更新获取英雄档案开启奖励时间
	 * @param playerId
	 */
	public void updateHeroArchiveOpenAward(String playerId) {
		String key = HERO_ARCHIVE_OPEN_AWARD + ":" + playerId;
		redisSession.setString(key, HawkTime.formatNowTime());
	}
	
	/**
	 * 获取联盟编队红点
	 * @param playerId
	 * @return
	 */
	public Set<Integer> getGuildFormationRed(String playerId) {
		String key = GUILD_FORMATION_RED + ":" + playerId;
		Map<String, String> value = redisSession.hGetAll(key);
		if (value == null) {
			return new ConcurrentHashSet<>();
		}
		return value.keySet()
			.stream()
			.map(Integer::parseInt)
			.collect(Collectors.toCollection(ConcurrentHashSet::new));
	}
	
	/**
	 * 更新联盟红点
	 * @param playerId
	 * @param redSet
	 */
	public void addGuildFormationRed(String playerId, int index) {
		String key = GUILD_FORMATION_RED + ":" + playerId;
		redisSession.hSet(key, String.valueOf(index), "");
		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if (player != null) {
			player.setGuildFormationChangeMark(1);
		}
	}
	
	/**
	 * 删除联盟红点
	 * @param playerId
	 * @param redSet
	 */
	public void delGuildFormationRed(String playerId, int index) {
		String key = GUILD_FORMATION_RED + ":" + playerId;
		redisSession.hDel(key, String.valueOf(index));
		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if (player != null) {
			player.setGuildFormationChangeMark(1);
		}
	}
	
	/**
	 * 删除联盟红点
	 * @param playerId
	 * @param redSet
	 */
	public void delGuildFormationRed(String playerId) {
		String key = GUILD_FORMATION_RED + ":" + playerId;
		redisSession.del(key);
		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if (player != null) {
			player.setGuildFormationChangeMark(1);
		}
	}
	
	/**
	 * 更新跨服联盟编队
	 * @param guildId
	 * @param formation
	 */
	public void updateCsGuildFormation(String guildId, String formation) {
		try {
			String key = CS_GUILD_FORMATION + ":" + guildId;
			redisSession.setString(key, formation);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取跨服联盟编队
	 * @param guildId
	 */
	public GuildFormationObj getCsGuildFormation(String guildId) {
		try {
			String key = CS_GUILD_FORMATION + ":" + guildId;
			String value = redisSession.getString(key);
			GuildFormationObj obj = GuildFormationObj.load(null, value);
			obj.initCheck();
			return obj;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**
	 * 是否需要拆服守护补偿
	 * @return
	 */
	public int getSeparateGuardValue(String playerId) {
		String key = SEPARATE_GUARD + ":" + playerId;
		String ret = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(ret)) {
			return Integer.parseInt(ret);
		}
		return 0;
	}
	
	/**
	 * 删除拆服守护补偿标记
	 */
	public void delSeparateGuard(String playerId) {
		String key = SEPARATE_GUARD + ":" + playerId;
		redisSession.del(key);
	}
	
	/**
	 * 更新迁服后禁止登录时间
	 */
	public void updateImmgrationBanLogin(String openId, String serverId, String tarServerId) {
		try {
			String key = IMMGRATION_BAN_LOGIN + ":" +openId;
			JSONObject info = new JSONObject();
			info.put("serverId", serverId);
			info.put("tarServerId", tarServerId);
			info.put("time", HawkTime.getMillisecond());
			redisSession.setString(key, info.toJSONString(), GsConfig.getInstance().getImmgrationBanLogin());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取迁服后禁止登录时间
	 * @param openId
	 * @return
	 */
	public JSONObject getImmgrationBanLogin(String openId) {
		try {
			String key = IMMGRATION_BAN_LOGIN + ":" +openId;
			String info = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(info)) {
				return null;
			}
			return JSONObject.parseObject(info);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	/**
	 * 统计量添加
	 * @param playerId
	 * @param statisType
	 * @param count
	 */
	public void idipDailyStatisAdd(String playerId, IDIPDailyStatisType statisType, int count) {
		String key = IDIP_DAILY_STATIS + ":" + playerId + ":" + HawkTime.getYyyyMMddIntVal() + ":" +statisType.getTypeValue();
		redisSession.increaseBy(key, count, GsConst.DAY_SECONDS);
	}
	
	/**
	 * 获取统计量
	 * @param playerId
	 * @param statisType
	 * @return
	 */
	public int getIdipDailyStatis(String playerId, IDIPDailyStatisType statisType) {
		String key = IDIP_DAILY_STATIS + ":" + playerId + ":" + HawkTime.getYyyyMMddIntVal() + ":" +statisType.getTypeValue();
		String value = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(value)) {
			return Integer.parseInt(value);
		}
		
		return 0;
	}
	
	/**
	 * 删除玩家的标识（表示删除玩家的处理正在进行中，不允许登录游戏）
	 * @param playerId
	 */
	public void addRemovePlayerFlag(String playerId) {
		String key = REMOVE_PLAYER_FLAG + ":" + playerId;
		redisSession.setString(key, "1", 300);
	}
	
	/**
	 * 判断是否是一个正在被删除的玩家
	 * @param playerId
	 * @return
	 */
	public boolean isRemovePlayer(String playerId) {
		String key = REMOVE_PLAYER_FLAG + ":" + playerId;
		String result = redisSession.getString(key);
		return !HawkOSOperator.isEmptyString(result);
	} 
	
	/**
	 * 清楚删除玩家的标识
	 * @param playerId
	 */
	public void clearRemovePlayerFlag(String playerId) {
		String key = REMOVE_PLAYER_FLAG + ":" + playerId;
		redisSession.del(key);
	}

	/**
	 * 获取大本等级达成时间
	 * @param playerId
	 * @return
	 */
	public long getCityRankTime(String playerId) {
		String key = PLAYER_CITY_RANK_TIME + ":" + playerId;
		try {
			String value = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(value)) {
				return 0L;
			}
			return Long.parseLong(value);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	/**
	 * 更新大本等级达成时间
	 * @param playerId
	 * @return
	 */
	public void updateCityRankTime(String playerId) {
		String key = PLAYER_CITY_RANK_TIME + ":" + playerId;
		try {
			redisSession.setString(key, String.valueOf(HawkTime.getMillisecond()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新大本等级达成时间(指定时间)
	 * @param playerId
	 * @param time
	 */
	public void updateCityRankTime(String playerId, long time) {
		String key = PLAYER_CITY_RANK_TIME + ":" + playerId;
		try {
			redisSession.setString(key, String.valueOf(time));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取未完成状态的外部充值商品ID
	 * @param playerId
	 * @return
	 */
	public Set<String> getRechargeOutterGoodsId(String playerId) {
		String redisKey = RECHARGE_OUTTER_UNFINISH + ":" + playerId;
		Set<String> goodsIds = redisSession.sMembers(redisKey);
		return goodsIds;
	}
	
	/**
	 * 从未完成状态的外部充值商品ID中移除
	 * @param playerId
	 * @param goodsId
	 */
	public void removeRechargeOutterGoodsId(Player player, String... goodsId) {
		String redisKey = RECHARGE_OUTTER_UNFINISH + ":" + player.getId();
		redisSession.sRem(redisKey, goodsId);
	}
	
	/**
	 * 添加未完成状态的外部充值商品ID
	 * @param playerId
	 * @param goodsId
	 */
	public void addRechargeOutterGoodsId(Player player, String... goodsId) {
		String redisKey = RECHARGE_OUTTER_UNFINISH + ":" + player.getId();
		redisSession.sAdd(redisKey, 60 * 16, goodsId);
	}
	
	/**
	 * 获取转平台信息
	 * @param playerId
	 * @return
	 */
	public PlatTransferInfo getPlatTransferInfo(String playerId) {
		String key = PLAT_TRANSFER_INFO + ":" + playerId;
		String info = redisSession.getString(key);
		if (!HawkOSOperator.isEmptyString(info)) {
			return PlatTransferInfo.valueOf(info);
		}
		
		return null;
	}
	
	public void updatePlatTransferInfo(PlatTransferInfo info) {
		String key = PLAT_TRANSFER_INFO + ":" + info.getPlayerId();
		redisSession.setString(key, info.toJsonStr());
	}
	
	/**
	 * idip请求序列号存储
	 * @param serialID
	 */
	public boolean saveIdipSerialID(String serialID) {
		String key = RedisKey.IDIP_SERIAL_ID + ":" + serialID;
		long result = redisSession.hSetNx(key, GsConfig.getInstance().getServerId(), "1");
		if (result > 0) {
			redisSession.expire(key, GsConst.DAY_SECONDS * 7);
			return true;
		}
		return false;
	}
	
	
	/**
	 * 设置高迁修复时间
	 * @param playerId
	 * @param time
	 */
	public void updateMoveCityFixArmyTime(String playerId, long time) {
		String key = MOVE_CITY_FIX_ARMY + ":" + playerId;
		try {
			redisSession.setString(key, String.valueOf(time));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取高迁修复时间
	 * @param playerId
	 * @return
	 */
	public long getMoveCityFixArmyTime(String playerId) {
		String key = MOVE_CITY_FIX_ARMY + ":" + playerId;
		try {
			String value = redisSession.getString(key);
			if (HawkOSOperator.isEmptyString(value)) {
				return 0L;
			}
			return Long.parseLong(value);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0L;
	}
	
	
	
	
	/**
	 * 获取学院名称显示
	 * @param playerId
	 * @return
	 */
	public String getCollegeNameShow(String playerId) {
		String key = COLLEGE_NAME_SHOW + ":" + playerId;
		String name = redisSession.getString(key);
		return name;
	}
	
	/**
	 * 更新学院名称显示
	 * @param playerId
	 * @param type
	 */
	public void updateCollegeNameShow(String playerId, String collegeName) {
		String key = COLLEGE_NAME_SHOW + ":" + playerId;
		redisSession.setString(key, collegeName);
	}
	
	
	/**
	 * 删除学院名称显示
	 * @param playerId
	 * @param type
	 */
	public void delCollegeNameShow(String playerId, String collegeName) {
		String key = COLLEGE_NAME_SHOW + ":" + playerId;
		redisSession.del(key);
	}
	
	
	public void addCollegeMemberVitCost(String serverId,String collegeId,String playerId,int vitCost){
		String uuid = HawkUUIDGenerator.genUUID();
		String key = COLLEGE_MEMBER_VIT_COST + ":" + serverId;
		String field = collegeId+"_"+playerId+"_"+uuid+"_"+vitCost;
		redisSession.hSet(key, field, String.valueOf(vitCost));
	}
	
	public Map<String,Integer> getCollegeMemberVitCostAnddelete(String serverId){
		String key = COLLEGE_MEMBER_VIT_COST + ":" + serverId;
		Map<String,String> rltMap = redisSession.hGetAll(key);
		if(rltMap.size() <= 0){
			return null;
		}
		Map<String,Integer> map = new HashMap<>();
		for (Entry<String, String> entry : rltMap.entrySet()) {
			try {
				String rltKey = entry.getKey();
				String[] rltKeyArr = rltKey.split("_");
				String collegeId = rltKeyArr[0];
				int value = Integer.parseInt(entry.getValue());
				int count = map.getOrDefault(collegeId, 0);
				count += value;
				map.put(collegeId, count);
				HawkLog.logPrintln("getCollegeMemberVitCostAnddelete vit cost, rltKey: {}, value: {}", entry.getKey(), entry.getValue());
			} catch (Exception e) {
				HawkLog.logPrintln("getCollegeMemberVitCostAnddelete err, rltKey: {}, value: {}", entry.getKey(), entry.getValue());
				continue;
			}
		}
		redisSession.del(key);
		return map;
	}
	
	public void addCollegeMemberVitSendToday(Map<String, Integer> sendData) {
		if (sendData.isEmpty()) {
			return;
		}
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			int day = HawkTime.getYearDay();
			for (Entry<String, Integer> entry : sendData.entrySet()) {
				String playerId = entry.getKey();
				int value = entry.getValue();
				String key = COLLEGE_MEMBER_VIT_SEND + ":" + playerId+ ":" + day;
				pip.incrBy(key, value);
				pip.expire(key,  (int)TimeUnit.DAYS.toSeconds(10));
			}
			pip.sync();
			StatisManager.getInstance().incRedisKey(OVERLAY_MISSION_KEY);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	public Map<String,Integer> getCollegeMemberVitSendToday(Set<String> playerIds){
		Map<String,Integer> sendMap = new HashMap<String,Integer>();
		Map<String,Response<String>> piplineRes = new HashMap<String,Response<String>>();
		int day = HawkTime.getYearDay();
		try(Jedis jedis = getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()){
			for(String playerId : playerIds ){
				String key = COLLEGE_MEMBER_VIT_SEND + ":" + playerId+ ":" + day;
				Response<String> onePiplineResp = pip.get(key);
				piplineRes.put(playerId,onePiplineResp );
			}
			pip.sync();
			if( piplineRes.size() == playerIds.size() ){
 	    		for(Entry<String,Response<String>> entry : piplineRes.entrySet()){
 	    			String key = entry.getKey();
 	    			Response<String> value = entry.getValue();
 	    			String retStr = value.get();
 	    			if (!HawkOSOperator.isEmptyString(retStr)) {
 	    				sendMap.put(key, NumberUtils.toInt(retStr));
 	    			}else{
 	    				sendMap.put(key, 0);
 	    			}
 	    		}   		
			}
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		return sendMap;
	}
	
	/**
	 * 添加禁止发放的邮件id
	 * @param mailId
	 */
	public void addForBiddenMailId(int mailId) {
		String key = RedisKey.FORBIDDEN_MAILID + ":" + GsConfig.getInstance().getServerId();
		redisSession.sAdd(key, 0, String.valueOf(mailId));
	}
	
	public void removeForBiddenMailId(int mailId) {
		String key = RedisKey.FORBIDDEN_MAILID + ":" + GsConfig.getInstance().getServerId();
		redisSession.sRem(key, String.valueOf(mailId));
	}
	
	public Set<Integer> getForBiddenMailIds() {
		String key = RedisKey.FORBIDDEN_MAILID + ":" + GsConfig.getInstance().getServerId();
		Set<String> mailIds = redisSession.sMembers(key);
		if (mailIds.isEmpty()) {
			return Collections.emptySet();
		}
		Set<Integer> set = new HashSet<>();
		mailIds.forEach(e -> set.add(Integer.parseInt(e)));
		return set;
	}
	
	/**
	 * 添加或更新待办事项信息
	 * @param scheduleInfo
	 * @param type
	 */
	public void addSchedule(ScheduleInfo scheduleInfo, int type) {
		String key = RedisKey.SCHEDULE_INFO + ":" + GsConfig.getInstance().getServerId();
		if (type > 0) {
			key = RedisKey.SCHEDULE_INFO + ":" + scheduleInfo.getGuildId();
		}
		redisSession.hSet(key, scheduleInfo.getUuid(), JSONObject.toJSONString(scheduleInfo));
	}
	
	/**
	 * 删除待办事项信息
	 * @param scheduleId
	 * @param guildId: guildId为空表示全服待办事项
	 */
	public void removeSchedule(String scheduleId, String guildId) {
		String key = RedisKey.SCHEDULE_INFO + ":" + GsConfig.getInstance().getServerId();
		if (!HawkOSOperator.isEmptyString(guildId)) {
			key = RedisKey.SCHEDULE_INFO + ":" + guildId;
		}
		redisSession.hDel(key, scheduleId);
	}
	
	/**
	 * 获取待办事项信息
	 * @param guildId: guildId为空表示全服待办事项
	 * @return
	 */
	public List<ScheduleInfo> getAllSchedule(String guildId) {
		String key = RedisKey.SCHEDULE_INFO + ":" + GsConfig.getInstance().getServerId();
		if (!HawkOSOperator.isEmptyString(guildId)) {
			key = RedisKey.SCHEDULE_INFO + ":" + guildId;
		}
		Map<String, String> result = redisSession.hGetAll(key);
		if (result.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<ScheduleInfo> list = new ArrayList<>();
		for (String str : result.values()) {
			list.add(JSONObject.parseObject(str, ScheduleInfo.class));
		}
		
		return list;
	}
	
	/**
	 * 添加航海跨服记录
	 * @param player
	 * @param termId
	 */
	public void addCrossActivityCrossPlayerRecord(Player player,int termId){
		String key = String.format(CROSS_ACTIVITY_CROSSED_PLAYER, termId,player.getMainServerId());
		redisSession.sAdd(key, (int)TimeUnit.DAYS.toSeconds(10), player.getId());
	}
	
	
	/**
	 * 获取航海跨服记录
	 * @param termId
	 * @param serverId
	 * @return
	 */
	public Set<String> getCrossActivityCrossPlayerRecord(int termId,String serverId){
		String key = String.format(CROSS_ACTIVITY_CROSSED_PLAYER, termId,serverId);
		return redisSession.sMembers(key);
	}
	
}