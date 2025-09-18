package com.hawk.activity.redis;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.hawk.os.HawkException;

import com.hawk.activity.ActivityManager;

public abstract class ActivityRedisKey {

	/**
	 * 最强指挥官阶段排名
	 */
	public static String STRONGEST_STAGE_RANK = ":activity_strongest_stage_rank";
	/**
	 * 最强指挥官总排名
	 */
	public static String STRONGEST_TOTAL_RANK = ":activity_strongest_total_rank";
	/**
	 * 最强指挥官历史最强数据
	 */
	public static String STRONGEST_ACTIVITY_HISTORY_RANK_LIST = ":activity_strongest_history_rank_list";
	/**
	 * 最强指挥官活动全局数据
	 */
	public static String STRONGEST_ACTIVITY_GLOBAL_DATA = ":activity_strongest_global_data";
	/**
	 * 讨伐尤里活动排行榜
	 */
	public static String MONSTER4_RANK_LIST = ":monster4_rank_list";
	
	/**
	 * 跨服团购活动本服积分
	 */
	public static String GROUP_PURCHASE_SCORE = ":activity:group_purchase_score";
	
	/**
	 * 记录活动的信息
	 */
	public static String HELL_FIRE = ":hell_fire";
	/**
	 * 记录完成的阶段
	 */
	public static String HELL_FIRE_FINISH = ":hell_fire_finish";
	/**
	 * 排行榜
	 */
	public static String HELL_FIRE_RANK = ":hell_fire_rank";
	/**
	 * 地狱火活动的信息
	 */
	public static String HELL_FIRE_2 = ":hell_fire_2";
	/**
	 * 记录完成的阶段
	 */
	public static String HELL_FIRE_FINISH_2 = ":hell_fire_finish_2";
	/**
	 * 排行榜
	 */
	public static String HELL_FIRE_RANK_2 = ":hell_fire_rank_2";
	/**
	 * 地狱火 3活动信息
	 */
	public static String HELL_FIRE_3 = ":hell_fire_3";
	/**
	 * 记录完成的阶段
	 */
	public static String HELL_FIRE_FINISH_3 = ":hell_fire_finish_3";
	
	/**
	 * 铁血军团排行榜信息
	 */
	public static String BLOOD_CORPS_RANK = ":activity_blood_corps_rank";
	
	/**
	 * 铁血军团排行榜信息
	 */
	public static String BLOOD_CORPS_CHECK_TIME = ":activity_blood_checktime";
	
	/**
	 * 活动成就相关配置，暂存redis
	 */
	private static final String ACHIEVE_ACTIVITY_CFG = ":achieve_activity_cfg";
	
	/**
	 * 过期离线活动成就奖励
	 */
	private static final String OFFLINE_ACHIEVE_REWARD = ":offline_achieve_reward";
	
	/**
	 *限时掉落排行.
	 */
	public static String TIME_LIMIT_DROP_RANK = ":time_limit_drop_rank";  
	
	/**
	 * 联盟总动员排行榜
	 */
	public static String ALLIANCE_CARNIVAL_RANK = ":alliance_carnival_rank";
	
	/**
	 * 机甲觉醒伤害个人排行榜信息
	 */
	public static String MACHINE_AWAKE_DAMAGE_SELF_RANK = ":ma_damage_self_rank";
	
	/**
	 * 机甲觉醒伤害联盟排行榜信息
	 */
	public static String MACHINE_AWAKE_DAMAGE_GUILD_RANK = ":ma_damage_guild_rank";
	
	/**
	 * 王者联盟玩家个人阶段排行 :termId:stageId
	 */
	public static String STRONGEST_GUILD_PERSONAL_RANK = ":strongest_guild_personal_rank:%s:%s";
	
	/***
	 * 王者联盟玩家总排名:termId
	 */
	public static String STRONGEST_GUILD_PERSONAL_TOTAL_RANK = ":strongest_guild_personal_total_rank:%s";
	
	/**
	 * 王者联盟 联盟阶段排行 :termId:stageId
	 */
	public static String STRONGEST_GUILD_STAGE_RANK = ":strongest_guild_stage_rank:%s:%s";
	
	/**
	 * 王者联盟 联盟总排行 :termId
	 */
	public static String STRONGEST_GUILD_TOTAL_RANK = ":strongest_guild_total_rank:%s";
	
	/***
	 * 王者联盟历史榜单 list数据结构
	 */
	public static String STRONGEST_GUILD_HISTORY_RANK = ":strongest_guild_history_rank";
	
	/***
	 * 王者联盟个人总积分map集合数据
	 */
	public static String STRONGEST_GUILD_PERSON_TOTAL_SCORE_MAP = ":strongest_guild_person_total_map_data:%s";
	
	/***
	 * 王者联盟联盟总积分map集合数据 
	 */
	public static String STRONGEST_GUILD_GUILD_TOTAL_SCORE_MAP = ":strongest_guild_guild_total_map_data:%s";
	
	/** 能量收集个人榜单 **/
	public static String POWER_COLLECT_PERSON_RANK = ":power_collect_person_rank_info:%s";
	
	/** 能量收集联盟榜单 **/
	public static String POWER_COLLECT_GUILD_RANK = ":power_collect_guild_rank_info:%s";
	
	/** 星能探索积分个人榜单 **/
	public static String PLANET_EXPLORE_SCORE_RANK = ":planet_explore_score_rank_info:%s";
	public static String PLANET_EXPLORE_SERVER_SCORE = ":planet_explore_server_score:%s";
	public static String PLANET_EXPLORE_TREAREFRESH_INFO = ":planet_explore_trearefresh_info:%s";
	
	/** 红包信息  termId:id **/
	public static String RED_ENVELOPE = ":red_envelope_info:%s:%s";
	
	/** 玩家发出来的红包 **/
	public static String RED_ENVELIPE_PLAYER = ":red_envelope_player:%s";
	
	/** 个人领取红包数量 **/
	public static String RED_ENVELIPE_PLAYER_RECIEVE = ":red_envelope_receive_count:%s:%s:%d";
	
	/** 玩家系统红包领取历史记录 : playerId **/
	public static String RED_ENVELOPE_PLAYER_HISTORY = ":red_envelope_player_system_history:%s";
	
	/**
	 * 机甲觉醒2(年兽)伤害个人排行榜信息
	 */
	public static String MACHINE_AWAKE_TWO_DAMAGE_SELF_RANK = ":ma2_damage_self_rank";
	
	/**
	 * 机甲觉醒2(年兽)伤害联盟排行榜信息
	 */
	public static String MACHINE_AWAKE_TWO_DAMAGE_GUILD_RANK = ":ma2_damage_guild_rank";
	
	/** 送花个人排行榜信息 */
	public static String SONG_HUA_RANK = ":song_hua_rank";
	
	/** 收花排行榜信息 */
	public static String SHOU_HUA_RANK = ":shou_hua_rank";
	
	/** 送花记录 */
	public static String SONG_HUA_JILU = ":song_hua_jilu";
	
	/** 收花记录 */
	public static String SHOU_HUA_JILU = ":shou_hua_jilu";
	/**
	 * 战神降临活动排行榜
	 */
	public static String BANNER_KILL_RANK = ":banner_kill_rank";
	/**
	 * 插旗活动排行榜
	 */
	public static String GUILD_BANNER_RANK = ":guild_banner_rank";
	/**
	 * 一元购额外奖励
	 */
	public static String ONE_RMB_EXTRA_REWARD = ":one_rmb_reward";
	
	/**
	 * 源计划排行榜
	 */
	public static String PLAN_ACTIVITY_RANK = ":plan_activity_rank";

	/**
	 * 玫瑰赠礼本服数据
	 */
	public static String ROSE_GIFT_SERVER_NUM = ":rose_gift_server_num";
	public static String ROSE_GIFT_SERVER_LAST = ":rose_gift_server_last";

	/** 瓜分金币活动 索要福字的信息 uuid : toPlayerId **/
	public static String DIVIDE_ASK_FOR_FUZI = ":ask_for_fuzi:%s";
	/**
	 * 瓜分金币活动 本服红包跑马灯记录key
	 */
	public static String DIVIDE_GOLD_LOCAL_NOTICE_KEY = ":divide_gold_local_notice";
	/**
	 * 瓜分金币活动 当天收到的福字
	 */
	public static String DIVIDE_GOLD_RECEIVE_FUZI_KEY = ":divide_gold_receive_fuzi_num";
	/**
	 * 雪球大战进球信息
	 */
	public static String SNOWBALL_GOAL_INFO = ":snowball_goal_info:%s";
	/**
	 * 雪球大战个人排行榜
	 */
	public static String SNOWBALL_SELF_RANK = ":snowball_selef_rank";
	/**
	 * 雪球大战联盟排行榜
	 */
	public static String SNOWBALL_GUILD_RANK = ":snowball_guild_rank";
	/**
	 * 圣诞大战个人击杀排行榜。
	 */
	public static String CHRISTMAS_WAR_KILL_RANK = ":christmas_war_kill_rank";
	/**
	 * 圣诞大战个人伤害排行榜
	 */
	public static String CHRISTMAS_WAR_PERSONAL_DAMAGE = ":christmas_war_personal_damage";
	/**
	 * 圣诞大战工会伤害排行榜
	 */
	public static String CHRISTMAS_WAR_GUILD_DAMAGE = ":christmas_war_guild_damage";
	/**
	 * 圣诞大战的一些活动数据.
	 */
	public static String CHRISTMAS_WAR_DATA		= ":christmas_war_data";
	/**
	 * 雪球大战进球奖励次数
	 */
	public static String SNOWBALL_GOAL_REWARD = ":snowball_goal_reward";
	
	/**
	 * 命运左轮抽奖5次
	 */
	public static String DESTINY_REVOLVER_FIVE = ":destiny_revolver_five";
			
	/**
	 * 离线重发事件记录
	 */
	public static String EVENT_RECORD = ":ACTIVITY_EVENT_RECORD";
	
	/**
	 * 资源保卫战记录
	 */
	public static String RESOURCE_DEFENSE_RECORD = ":resource_record:%s:%s";
	
	
	
	/** 领取赠送信件奖励次数*/
	public static  final String SEND_POWER_RECEIVE_COUNT_KEY =  ":send_power_revieve_count";
	
	/** 信件回礼奖励次数*/
	public static final String SEND_POWER_BACK_RECEIVE_COUNT_KEY =  ":send_power_back_revieve_count";

	/** 发送的信件*/
	public static final String SEND_POWER_MESSAGE_KEY =  ":send_power_message";
	
	/** 信件催回*/
	public static final String SEND_POWER_MESSAGE_PRESSED = ":send_power_message_pressed";
	
	/** 赠送体力信息*/
	public static final String SEND_POWER_ACTIVITY_INFO = ":send_power_activity_info";
	
	/**
	 * 能源滚滚个人排行榜信息
	 */
	public static String ENERGIES_SELF_RANK = ":energies_self_rank";
	
	/**
	 * 能源滚滚联盟排行榜信息
	 */
	public static String ENERGIES_GUILD_RANK = ":energies_guild_rank";
	
	/**
	 * 能源滚滚联盟每日积分
	 */
	public static String ENERGIES_GUILD_DAILY_SCORE = ":energies_guild_score";
	
	/**
	 * 端午节-联盟庆典
	 */
	public static final String DRAGON_BOAT_CELETRATION_RANK = ":dragonboat_celebration_rank";
	/**
	 * 端午节-联盟庆典
	 */
	public static final String DRAGON_BOAT_CELETRATION_EXP = ":dragonboat_celebration_exp";
	/**
	 * 端午节-联盟庆典
	 */
	public static final String DRAGON_BOAT_CELETRATION_GUILD_AWARD = ":dragonboat_celebration_guild_award";
	/**
	 * 端午节-联盟庆典
	 */
	public static final String DRAGON_BOAT_CELETRATION_MEMBER_AWARD = ":dragonboat_celebration_member_award";

	/**
	 * 端午节龙船领奖记录
	 */
	public static final String DRAGON_BOAT_GIFT_RECORD = "dragonboat_gift_record";


	/**
	 * 周年庆蛋糕分享领奖记录
	 */
	public static final String CAKE_SHARE_RECORD = "cake_share_record";
	
	
	/**
	 * 双11弹幕
	 */
	public static String GLOBAL_SIGN_BULLET_CHAT = "global_sign_bullet_chat";
	
	/**
	 * 双11 全服签到人数
	 */
	public static final String GLOBAL_SIGN_COUNT = "global_sign_count";

	/**
	 * 双11 全服签到注水人数
	 */
	public static final String GLOBAL_SIGN_COUNT_ASSIST = "global_sign_count_assist";
	
	/**
	 * 双11 全服签到注水初始化
	 */
	public static final String GLOBAL_SIGN_COUNT_ASSIST_INIT = "global_sign_count_assist_init";


	/**
	 * 联盟欢庆联盟经验信息
	 */
	public static final String ALLIANCE_CELEBRATE_EXP = "alliance_celebrate_exp";

	/**
	 * 双11 全服签到注水锁
	 */
	public static final String GLOBAL_SIGN_COUNT_ASSIST_LOCK = "global_sign_count_assist_lock";

	/**
	 * 好友召回活动,联盟成就任务 有serverId 存本服
	 */
	public static String RECALL_GUILD_ACHIEVE = ":recall_guild_achieve";
	/**
	 * 好友召回活动,联盟可召回玩家 有serverId 存本服
	 */
	public static String RECALL_ALL_BACK_FLOW = ":recall_all_back_flow";

	public static final String SHARE_GLORY = "share_glory";
	/**
	 * 军魂传承活动触发
	 */
	public static String INHERIT_NEW_START = ":inherit_new_start:%s";

	/**
	 * 新辐射战争击杀数量
	 */
	public static final String RADIATION_WAR_TWO_KILL_COUNT = "radiation_war_two_kill_count";
	
	/**
	 * 刮刮乐代刮
	 */
	public static final String LOTTERY_TICKET_RECOURSE = "lottery_ticket_recourse";
	public static final String LOTTERY_TICKET_RECOURSE_RECORD = "lottery_ticket_recourse_record";
	public static final String LOTTERY_TICKET_USE_COUNT = "lottery_ticket_use_count";
	public static final String LOTTERY_TICKET_ASSIST_APPLY = "lottery_ticket_assist_apply";
	public static final String LOTTERY_TICKET_BARRAGE = "lottery_ticket_barrage";
	
	/**
	 * 回流转服
	 */
	public static final String BACK_IMMGRATION_POW_RANGE = "back_immgration_pow_range";
	
	/**
	 * 打靶活动积分排行榜
	 */
	public static String SHOOTING_PRACTICE_SCORE_RANK = ":shooting_practice_score_rank:%s";
	
	/**
	 * 星海投资探索宝箱记录
	 */
	public static final String STAR_INVEST_EXPLORE_RECORD = ":star_invest_explore_record:%s:%s";
	

	/**
	 * 潜艇大大战排行榜计算锁
	 */
	public static final String SUBMARINE_WAR_RANK_GROUP_LOCK = "submarine_war_rank_group_lock:%s:%s:%s";
	public static final String SUBMARINE_WAR_JOIN_SERVER = "submarine_war_join_server:%s";
	public static final String SUBMARINE_WAR_RANK_GROUP = "submarine_war_rank_group:%s";
	
	public static final String SUBMARINE_WAR_SCORE_RANK = "submarine_war_score_rank:%s:%s";
	public static final String SUBMARINE_WAR_SCORE_RANK_PLAYER_DATA = "submarine_war_score_rank_player_data:%s:%s";
	public static final String SUBMARINE_WAR_SCORE_RANK_CACHE = "submarine_war_score_rank_cahche:%s:%s:%s:%s";
	public static final String SUBMARINE_WAR_SCORE_RANK_REWARD_RECORD = "submarine_war_score_rank_reward_record:%s:%s";
	
	
	
	
	
	public static void init() {
		// 自动为本地redis的key添加标识
		String serverId = ActivityManager.getInstance().getDataGeter().getLocalIdentify();
		Field[] declaredFields = ActivityRedisKey.class.getDeclaredFields();
		for (Field field : declaredFields) {
			if (field.getType() != String.class) {
				continue;
			}
			field.setAccessible(true);
			try {
				if(Modifier.isFinal(field.getModifiers())){
					continue;
				}
				Object value = field.get(null);
				field.set(null, serverId + value.toString());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				HawkException.catchException(e);
			}
			field.setAccessible(false);
		}
	}
	
	public static String getOFFLINE_ACHIEVE_REWARD_KEY(){
		return  ActivityManager.getInstance().getDataGeter().getLocalIdentify() + OFFLINE_ACHIEVE_REWARD;
	}

	public static String getACHIEVE_ACTIVITY_CFG_KEY(){
		return ActivityManager.getInstance().getDataGeter().getLocalIdentify() + ACHIEVE_ACTIVITY_CFG;
	}
}
