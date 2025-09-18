package com.hawk.game.global;

/**
 * rediskey统一安放
 * @author lating
 */
public class RedisKey {
	/**
	 * 心悦角色交易卖方数据备份
	 */
	public static final String ROLE_EXCHANGE_DATA = "seller_player_data";
	/**
	 * 心悦角色交易转换记录
	 */
	public static final String ROLE_EXCHANGE_RECORD = "role_exchange_record";
	public static final String ROLE_EXCHANGE_ACCOUNT = "role_exchange_account";
	public static final String ROLE_EXCHANGE_STATUS = "role_exchange_status";
	public static final String ROLE_EXCHANGE_DOING = "role_exchange_ing";
	/**
	 * 玩家充值信息备份
	 */
	public static final String RECHARGE_INFO_BACK = "recharge_info_back";
	/**
	 * 服务器状态（0: 未停服 1: 已停服）
	 */
	public static final String SERVER_STATE = "server_running_state";
	/**
	 * idip gm充值
	 */
	public static final String IDIP_GM_RECHARGE = "gm_rechage_daily";
	/**
	 * 玩家充值信息
	 */
	public static final String RECHARGE_INFO = "rechargeInfo";
	public static final String RECHARGE_INFO_2024 = "2024rechargeInfo"; //rechargeInfo是按list存储的，一个账号（openid）的数据量随着时间一直在增长，到后面可能出现大key的问题，所以这里新起一个key
	public static final String RECHARGE_INFO_QQ = "qqrechargeInfo"; //只针对互通区上的手Q玩家
	public static final String ROLE_RECHARGE_TOTAL = "roleRechargeTotal";
	/**
	 * idip请求流水单号
	 */
	public static final String IDIP_SERIAL_ID = "idip_serial_id";
	/**
	 * 合服信息存储，给idip中转服（GM服）提供信息
	 */
	public static final String MERGE_SLAVE_MASTER = "mergeSlaveMasterMap";
	/**
	 * 指挥官等级升级时间
	 */
	public static final String PLAYER_LEVELUP_TIME = "playerLevelTime";
	
	/**
	 * 集结进攻幽灵基地发放联盟礼包
	 */
	public static final String MASS_FOGGY_ALLI_GIFT = "massFoggyAllianceGift";
	
	/**
	 * 禁止发放的邮件
	 */
	public static final String FORBIDDEN_MAILID = "forbiddenMailId";
	/**
	 * 家园系统
	 */
	public static final String HOME_LAND_RANK = "HOME_LAND_RANK:";
	/**
	 * 家园系统玩家简要信息
	 */
	public static final String HOME_LAND_RANK_PLAYER = "HOME_LAND_RANK:PLAYER_INFO";
	/** 待办事项信息 */
	public static final String SCHEDULE_INFO = "schedule_info";
	/**
	 * 机甲核心增产信息
	 */
	public static final String GACHA_MODULE_ADD_PRODUCT = "gachaModuleAddPro";
	
}
