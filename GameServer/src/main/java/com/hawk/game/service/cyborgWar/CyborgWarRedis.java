package com.hawk.game.service.cyborgWar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.protocol.CyborgWar.CWBattleLog;
import com.hawk.game.service.cyborgWar.CWConst.CWMemverMangeType;

import redis.clients.jedis.Tuple;

public class CyborgWarRedis {
	
	/** 赛博之战本服战队战力排名*/
	public final String CWACTIVITY_TEAM_POWER_RANK = "cw_power_rank";
	
	/** 赛博之战之战区服活动阶段数据 */
	public final String CWACTIVITY_SERVER_INFO = "cw_activity_info";
	
	/** 联盟对应战队列表映射*/
	public final String CWACTIVITY_GUILD_TEAMS = "cw_guild_team";
	
	/** 战队基础信息*/
	public final String CWACTIVITY_TEAM_DATA = "cw_team_data";
	
	/** 赛博之战之战报名战队 期数-报名分组*/
	public final String CWACTIVITY_SIGN_TEAM = "cw_sign_teams";
	
	/** 赛博之战之战战队人员列表*/
	public final String CWACTIVITY_JOIN_PLAYER = "cw_join_player";
	
	/** 赛博之战之战房间信息*/
	public final String CWACTIVITY_ROOM_INFO = "cw_room_info";
	
	/** 赛博之战之战出战战队信息*/
	public final String CWACTIVITY_JOIN_TEAM = "cw_join_team";
	
	/** 赛博之战之战匹配状态*/
	public final String CWACTIVITY_MATCH_STATE = "cw_match_state";

	/** 赛博之战之战匹配权限锁*/
	public final String CWACTIVITY_MATCH_LOCK = "cw_match_lock";
	
	/** 赛博之战之战战斗开启阶段状态*/
	public final String CWACTIVITY_FIGHT_STATE = "cw_fight_state";
	
	/** 赛博之战之战玩家信息*/
	public final String CWACTIVITY_PLAYER_INFO = "cw_player_info";
	
	/** 赛博之战之战历史联盟战报*/
	public final String CWACTIVITY_BATTLE_HISTORY = "cw_battle_log";
	
	/**
	 * 商店商品购买次数
	 */
	public final String CYBORG_SHOP_BUY = "cw_item_buy";
	
	/**
	 * 出战战队联盟信息
	 */
	public final String CWACTIVITY_GUILD_INFO = "cw_guild_info";
	
	/** 赛博之战上一期出战列表*/
	public final String CWACTIVITY_HISTORY_JOIN = "cw_history_join";
	
	
	
	/** 赛博之战赛季段位排名*/
	public final String CLWACTIVITY_TEAM_STAR_RANK = "clw_star_rank";
	
	/** 赛博之战赛季区服活动阶段数据 */
	public final String CLWACTIVITY_SERVER_INFO = "clw_activity_info";
	
	/** 赛博之战赛季联盟积分信息*/
	public final String CLWACTIVITY_GUILD_SCORE = "clw_guild_score";
	
	/** 赛博之战赛季个人已领取奖励列表*/
	public final String CLWACTIVITY_REWARDED_LIST = "clw_rewarded_info";
	
	/**
	 * 全局实例对象
	 */
	private static CyborgWarRedis instance = null;

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static CyborgWarRedis getInstance() {
		if (instance == null) {
			instance = new CyborgWarRedis();
		}
		return instance;
	}

	/**
	 * 构造
	 *
	 */
	private CyborgWarRedis() {
	}
	
	
	/**
	 * 刷新赛博之战战队战力排名
	 * @param season
	 * @param zoneId
	 * @param guildId
	 * @param score
	 * @return
	 */
	public boolean addCWTeamPowerRank(String teamId, long score) {
		String serverId = GsConfig.getInstance().getServerId();
		String key = CWACTIVITY_TEAM_POWER_RANK + ":" + serverId;
		RedisProxy.getInstance().getRedisSession().zAdd(key, score, teamId, 0);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_POWER_RANK);
		return true;
	}
	
	/**
	 * 获取战队战力排名
	 * @param teamId
	 * @return
	 */
	public int getCWTeamPowerRank(String teamId, String serverId) {
		String key = CWACTIVITY_TEAM_POWER_RANK + ":" + serverId;
		Long rank = RedisProxy.getInstance().getRedisSession().zrevrank(key, teamId, 0);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_POWER_RANK);
		if(rank == null ||rank == -1){
			return -1;
		}else{
			return (int) rank.intValue() + 1;
		}
	}
	// 获取联盟战队战力
	public long getCWTeamPower(String teamId, String serverId){
		String key = CWACTIVITY_TEAM_POWER_RANK + ":" + serverId;
		Double power = RedisProxy.getInstance().getRedisSession().zScore(key, teamId, 0);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_POWER_RANK);
		if(power == null){
			return 0;
		}else{
			return power.longValue();
		}
	}

	/**
	 * 批量刷新赛博之战战队战力排名
	 * @param season
	 * @param zoneId
	 * @param members
	 * @return
	 */
	public boolean addCWTeamPowerRanks(Map<String, Double> members, String serverId) {
		String key = CWACTIVITY_TEAM_POWER_RANK + ":" + serverId;
		RedisProxy.getInstance().getRedisSession().zAdd(key, members, 0);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_POWER_RANK);
		return true;
	}
	
	/**
	 * 获取赛博之战战队战力排名
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<Tuple> getCWTeamPowerRanks(long start, long end, String serverId) {
		String key = CWACTIVITY_TEAM_POWER_RANK + ":" + serverId;
		Set<Tuple> result = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(key, start, end, 0);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_POWER_RANK);
		return result;
	}
	
	/**
	 * 删除战队的赛博之战战力排名
	 * @param teamId
	 */
	public void removeCWTeamPowerRank(String teamId){
		String serverId = GsConfig.getInstance().getServerId();
		String key = CWACTIVITY_TEAM_POWER_RANK + ":" + serverId;
		RedisProxy.getInstance().getRedisSession().zRem(key, 0, teamId);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_POWER_RANK);
	}
	
	/**
	 * 更新赛博之战之战活动信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCWActivityInfo(CWActivityData activityInfo) {
		String jsonString = JSON.toJSONString(activityInfo);
		String key = CWACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		RedisProxy.getInstance().getRedisSession().setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(CWACTIVITY_SERVER_INFO);
		return true;
	}

	/**
	 * 获取赛博之战之战活动信息
	 * 
	 * @return
	 */
	public CWActivityData getCWActivityInfo() {
		String key = CWACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		String dataStr = RedisProxy.getInstance().getRedisSession().getString(key);
		CWActivityData activityInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			activityInfo = new CWActivityData();
		} else {
			activityInfo = JSON.parseObject(dataStr, CWActivityData.class);
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_SERVER_INFO);
		return activityInfo;
	}
	
	/**
	 * 更新赛博之战之战战斗状态信息
	 * 
	 * @param fightInfo
	 * @return
	 */
	public boolean updateCWFightInfo(CWFightState fightInfo) {
		String jsonString = JSON.toJSONString(fightInfo);
		String key = CWACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		RedisProxy.getInstance().getRedisSession().setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(CWACTIVITY_FIGHT_STATE);
		return true;
	}
	
	/**
	 * 获取赛博之战之战战斗状态信息
	 * 
	 * @return
	 */
	public CWFightState getCWFightInfo() {
		String key = CWACTIVITY_FIGHT_STATE + ":" + GsConfig.getInstance().getServerId();
		String dataStr = RedisProxy.getInstance().getRedisSession().getString(key);
		CWFightState fightInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			fightInfo = new CWFightState();
		} else {
			fightInfo = JSON.parseObject(dataStr, CWFightState.class);
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_FIGHT_STATE);
		return fightInfo;
	}
	
	
	
	/**
	 * 添加赛博之战联盟-战队映射
	 * @param activityInfo
	 * @return
	 */
	public boolean addCWGuildTeam(String guildId,  String teamId) {
		String key = CWACTIVITY_GUILD_TEAMS + ":" + guildId;
		RedisProxy.getInstance().getRedisSession().sAdd(key, 0, teamId);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_GUILD_TEAMS);
		return true;
	}
	
	/**
	 * 移除赛博之战联盟-战队映射
	 * @param activityInfo
	 * @return
	 */
	public boolean removeCWGuildTeam(String guildId,  String teamId) {
		String key = CWACTIVITY_GUILD_TEAMS + ":" + guildId;
		RedisProxy.getInstance().getRedisSession().sRem(key, teamId);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_GUILD_TEAMS);
		return true;
	}
	
	/**
	 * 获取赛博之战联盟-战队映射
	 * @param activityInfo
	 * @return
	 */
	public List<String> getCWGuildTeams(String guildId) {
		String key = CWACTIVITY_GUILD_TEAMS + ":" + guildId;
		Set<String> members = RedisProxy.getInstance().getRedisSession().sMembers(key);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_GUILD_TEAMS);
		if(members == null || members.isEmpty()){
			return new ArrayList<>();
		}
		return new ArrayList<>(members);
	}
	
	
	/**
	 * 更新赛博之战战队信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCWTeamData(CWTeamData cwTeamData) {
		String jsonString = JSON.toJSONString(cwTeamData);
		String key = CWACTIVITY_TEAM_DATA;
		RedisProxy.getInstance().getRedisSession().hSet(key, cwTeamData.getId(), jsonString);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_DATA);
		return true;
	}
	
	/**
	 * 移除赛博之战战队信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean removeCWTeamData(String teamId) {
		String key = CWACTIVITY_TEAM_DATA;
		RedisProxy.getInstance().getRedisSession().hDel(key, teamId);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_DATA);
		return true;
	}
	
	/**
	 * 批量更新赛博之战战队信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCWTeamData(List<CWTeamData> cwTeamDatas) {
		if (cwTeamDatas == null || cwTeamDatas.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (CWTeamData data : cwTeamDatas) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = CWACTIVITY_TEAM_DATA;
		RedisProxy.getInstance().getRedisSession().hmSet(key, dataMap, 0);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_DATA);
		return true;
	}

	/**
	 * 获取赛博之战战队信息
	 * 
	 * @return
	 */
	public CWTeamData getCWTeamData(String teamId) {
		String key = CWACTIVITY_TEAM_DATA;
		String dataStr = RedisProxy.getInstance().getRedisSession().hGet(key, teamId);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_DATA);
		CWTeamData teamData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			teamData = JSON.parseObject(dataStr, CWTeamData.class);
		}
		return teamData;
	}
	
	/**
	 * 批量获取赛博之战战队信息
	 * 
	 * @return
	 */
	public Map<String, CWTeamData> getCWTeamData(List<String> teamIds) {
		Map<String, CWTeamData> dataMap = new HashMap<>();
		if (teamIds == null || teamIds.isEmpty()) {
			return dataMap;
		}
		String key = CWACTIVITY_TEAM_DATA;
		List<String> result = RedisProxy.getInstance().getRedisSession().hmGet(key, teamIds.toArray(new String[teamIds.size()]));
		if (result == null || result.isEmpty()) {
			return dataMap;
		}
		for (String dataStr : result) {
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			CWTeamData teamData = JSON.parseObject(dataStr, CWTeamData.class);
			dataMap.put(teamData.getId(), teamData);
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_TEAM_DATA);
		return dataMap;
	}
	
	
	
	
	
	
	
	/**
	 * 添加赛博之战报名信息
	 * @param activityInfo
	 * @return
	 */
	public boolean addCWSignInfo(String teamId, int termId, int index) {
		String key = CWACTIVITY_SIGN_TEAM + ":" + termId + ":" + index;
		RedisProxy.getInstance().getRedisSession().lPush(key, CWConst.EXPIRE_TIME_30, teamId);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_SIGN_TEAM);
		return true;
	}
	
	/**
	 * 移除报名的联盟
	 * @param guildId
	 * @param termId
	 * @param index
	 * @return
	 */
	public boolean removeCWSignInfo(String teamId, int termId, int index){
		String key = CWACTIVITY_SIGN_TEAM + ":" + termId + ":" + index;
		RedisProxy.getInstance().getRedisSession().lRem(key, 0, teamId);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_SIGN_TEAM);
		return true;
	}
	
	
	/**
	 * 获取赛博之战报名信息
	 * @param activityInfo
	 * @return
	 */
	public List<String> getCWSignInfo(int termId, int index) {
		String key = CWACTIVITY_SIGN_TEAM + ":" + termId + ":" + index;
		List<String> result = RedisProxy.getInstance().getRedisSession().lRange(key, 0, -1, CWConst.EXPIRE_TIME_30);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_SIGN_TEAM);
		return result;
	}
	
	/**
	 * 更新参战玩家列表
	 * @param guildId
	 * @param idList
	 * @return
	 */
	public boolean updateCWPlayerIds(String teamId, CWMemverMangeType type, String playerId) {
		String key = CWACTIVITY_JOIN_PLAYER + ":" + teamId;
		if(type == CWMemverMangeType.JOIN){
			RedisProxy.getInstance().getRedisSession().sAdd(key, 0, playerId);
		}
		else{
			RedisProxy.getInstance().getRedisSession().sRem(key, playerId);
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_JOIN_PLAYER);
		return true;
	}
	
	/**
	 * 从参战斗列表中删除指定玩家
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public boolean removeCWPlayerId(String teamId, String playerId) {
		String key = CWACTIVITY_JOIN_PLAYER + ":" + teamId;
		RedisProxy.getInstance().getRedisSession().sRem(key, playerId);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_JOIN_PLAYER);
		return true;
	}
	
	/**
	 * 移除指定战队的出战人员列表
	 * @param teamId
	 * @return
	 */
	public boolean removeCWPlayerIds(String teamId) {
		String key = CWACTIVITY_JOIN_PLAYER + ":" + teamId;
		RedisProxy.getInstance().getRedisSession().del(key);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_JOIN_PLAYER);
		return true;
	}
	
	/**
	 * 获取赛博之战参战玩家列表
	 * @param activityInfo
	 * @return
	 */
	public Set<String> getCWPlayerIds(String teamId) {
		String key = CWACTIVITY_JOIN_PLAYER + ":" + teamId;
		Set<String> result= RedisProxy.getInstance().getRedisSession().sMembers(key);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_JOIN_PLAYER);
		return result;
	}
	
	
	/**
	 * 更新赛博之战参战玩家信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCWPlayerData(CWPlayerData twPlayerData, int termId) {
		String jsonString = JSON.toJSONString(twPlayerData);
		String key = CWACTIVITY_PLAYER_INFO + ":" + termId + ":" + twPlayerData.getId();
		RedisProxy.getInstance().getRedisSession().setString(key, jsonString, CWConst.EXPIRE_TIME_30);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_PLAYER_INFO);
		return true;
	}

	/**
	 * 获取赛博之战参战玩家信息
	 * 
	 * @return
	 */
	public CWPlayerData getCWPlayerData(String playerId, int termId) {
		String key = CWACTIVITY_PLAYER_INFO + ":" + termId + ":" + playerId;
		String dataStr = RedisProxy.getInstance().getRedisSession().getString(key, CWConst.EXPIRE_TIME_30);
		CWPlayerData twPlayerData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			twPlayerData = JSON.parseObject(dataStr, CWPlayerData.class);
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_PLAYER_INFO);
		return twPlayerData;
	}
	
	/**
	 * 更新赛博之战参与战队信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCWJoinTeamData(CWTeamJoinData cwTeamData, int termId) {
		String jsonString = JSON.toJSONString(cwTeamData);
		String key = CWACTIVITY_JOIN_TEAM + ":" + termId;
		RedisProxy.getInstance().getRedisSession().hSet(key, cwTeamData.getId(), jsonString, CWConst.EXPIRE_TIME_30);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_JOIN_TEAM);
		return true;
	}
	
	/**
	 * 批量更新赛博之战参与战队信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCWJoinTeamData(List<CWTeamJoinData> cwTeamDatas, int termId) {
		if (cwTeamDatas == null || cwTeamDatas.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (CWTeamJoinData data : cwTeamDatas) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = CWACTIVITY_JOIN_TEAM + ":" + termId;
		RedisProxy.getInstance().getRedisSession().hmSet(key, dataMap, CWConst.EXPIRE_TIME_30);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_JOIN_TEAM);
		return true;
	}

	/**
	 * 获取赛博之战参与战队信息
	 * 
	 * @return
	 */
	public CWTeamJoinData getCWJoinTeamData(String teamId, int termId) {
		String key = CWACTIVITY_JOIN_TEAM + ":" + termId;
		String dataStr = RedisProxy.getInstance().getRedisSession().hGet(key, teamId, CWConst.EXPIRE_TIME_30);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_JOIN_TEAM);
		CWTeamJoinData teamData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			teamData = JSON.parseObject(dataStr, CWTeamJoinData.class);
		}
		return teamData;
	}
	
	/**
	 * 批量获取赛博之战参与战队信息
	 * 
	 * @return
	 */
	public Map<String, CWTeamJoinData> getCWJoinTeamDatas(List<String> teamIds, int termId) {
		String key = CWACTIVITY_JOIN_TEAM + ":" + termId;
		Map<String, CWTeamJoinData> dataMap = new HashMap<>();
		if (CollectionUtils.isEmpty(teamIds)) {
			return dataMap;
		}
		List<String> result = RedisProxy.getInstance().getRedisSession().hmGet(key, teamIds.toArray(new String[teamIds.size()]));
		if (result == null || result.isEmpty()) {
			return dataMap;
		}
		for (String dataStr : result) {
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			CWTeamJoinData teamData = JSON.parseObject(dataStr, CWTeamJoinData.class);
			dataMap.put(teamData.getId(), teamData);
		}
		return dataMap;
	}
	
	/**
	 * 获取赛博之战所有参与战队信息
	 * @return
	 */
	public Map<String, CWTeamJoinData> getAllCWJoinTeamData(int termId) {
		String key = CWACTIVITY_JOIN_TEAM + ":" + termId;
		Map<String, String> resultMap = RedisProxy.getInstance().getRedisSession().hGetAll(key, CWConst.EXPIRE_TIME_30);
		Map<String, CWTeamJoinData> dataMap = new HashMap<>();
		for (Entry<String, String> entry : resultMap.entrySet()) {
			String dataStr = entry.getValue();
			if (HawkOSOperator.isEmptyString(dataStr)) {
				continue;
			}
			CWTeamJoinData guildData = JSON.parseObject(dataStr, CWTeamJoinData.class);
			dataMap.put(guildData.getId(), guildData);
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_JOIN_TEAM);
		return dataMap;
	}
	
	/**
	 * 更新赛博之战房间信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCWRoomData(CWRoomData twRoomData, int termId) {
		String key = CWACTIVITY_ROOM_INFO + ":" + termId;
		RedisProxy.getInstance().getRedisSession().hSet(key, twRoomData.getId(), JSON.toJSONString(twRoomData), CWConst.EXPIRE_TIME_30);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_ROOM_INFO);
		return true;
	}
	
	/**
	 * 批量更新赛博之战匹配房间信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCWRoomData(List<CWRoomData> twRoomDatas, int termId) {
		Map<String, String> dataMap = new HashMap<>();
		for(CWRoomData data : twRoomDatas){
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		if(dataMap.isEmpty()){
			return false;
		}
		String key = CWACTIVITY_ROOM_INFO + ":" + termId;
		RedisProxy.getInstance().getRedisSession().hmSet(key, dataMap, CWConst.EXPIRE_TIME_30);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_ROOM_INFO);
		return true;
	}
	
	/**
	 * 删除赛博之战匹配房间信息
	 * @param activityInfo
	 * @return
	 */
	public boolean removeCWRoomData(int termId) {
		String key = CWACTIVITY_ROOM_INFO + ":" + termId;
		RedisProxy.getInstance().getRedisSession().del(key);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_ROOM_INFO);
		return true;
	}

	/**
	 * 获取赛博之战匹配房间信息
	 * 
	 * @return
	 */
	public CWRoomData getCWRoomData(String roomId, int termId) {
		String key = CWACTIVITY_ROOM_INFO + ":" + termId;
		String dataStr = RedisProxy.getInstance().getRedisSession().hGet(key, roomId, CWConst.EXPIRE_TIME_30);
		CWRoomData guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, CWRoomData.class);
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_ROOM_INFO);
		return guildData;
	}
	
	/**
	 * 获取赛博之战匹配房间信息
	 * 
	 * @return
	 */
	public List<CWRoomData> getAllCWRoomData(int termId) {
		String key = CWACTIVITY_ROOM_INFO + ":" + termId;
		Map<String, String> dataMap = RedisProxy.getInstance().getRedisSession().hGetAll(key, CWConst.EXPIRE_TIME_30);
		List<CWRoomData> list = new ArrayList<>();
		for (Entry<String, String> entry : dataMap.entrySet()) {
			String dataStr = entry.getValue();
			if (!HawkOSOperator.isEmptyString(dataStr)) {
				CWRoomData roomData = JSON.parseObject(dataStr, CWRoomData.class);
				list.add(roomData);
			}
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_ROOM_INFO);
		return list;
	}
	
	/**
	 * 记录赛博之战之战战报
	 * @param battleLog
	 * @param guildId
	 */
	public void addCWBattleLog(CWBattleLog battleLog, String teamId) {
		String key = CWACTIVITY_BATTLE_HISTORY + ":" + teamId;
		RedisProxy.getInstance().getRedisSession().lPush(key.getBytes(), CWConst.EXPIRE_TIME_180, battleLog.toByteArray());
		StatisManager.getInstance().incRedisKey(CWACTIVITY_BATTLE_HISTORY);
	}
	
	/**
	 * 获取赛博之战之战历史战报
	 * @param roomId
	 * @return
	 */
	public List<CWBattleLog> getCWBattleLog(String teamId,int count) {
		String key = CWACTIVITY_BATTLE_HISTORY + ":" + teamId;
		List<byte[]> result = RedisProxy.getInstance().getRedisSession().lRange(key.getBytes(), 0, count, CWConst.EXPIRE_TIME_180);
		if (result == null) {
			return Collections.emptyList();
		}
		
		List<CWBattleLog> list = new ArrayList<>();
		for(byte[] bytes : result){
			try {
				CWBattleLog.Builder builder = CWBattleLog.newBuilder();
				builder.mergeFrom(bytes);
				list.add(builder.build());
			} catch (InvalidProtocolBufferException e) {
				HawkException.catchException(e);
				continue;
			}
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_BATTLE_HISTORY);
		return list;
	}
	
	/**
	 * 移除赛博之战战队历史战斗记录
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public boolean removeCWBattleLog(String teamId) {
		String key = CWACTIVITY_BATTLE_HISTORY + ":" + teamId;
		RedisProxy.getInstance().getRedisSession().del(key);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_BATTLE_HISTORY);
		return true;
	}
	
	/**
	 * 清除商店商品购买次数
	 * 
	 * @param playerId
	 */
	public void clearCyborgShopItemBuyCount(String playerId) {
		StatisManager.getInstance().incRedisKey(CYBORG_SHOP_BUY);
		String key = CYBORG_SHOP_BUY + ":" + playerId;
		RedisProxy.getInstance().getRedisSession().del(key);
	}

	/**
	 * 获取商店商品购买次数
	 * 
	 * @param playerId
	 * @param seasonStr
	 * @return
	 */
	public Map<Integer, Integer> getCyborgShopItemBuyCount(String playerId, String seasonStr) {
		StatisManager.getInstance().incRedisKey(CYBORG_SHOP_BUY);
		String key = CYBORG_SHOP_BUY + ":" + seasonStr + playerId;
		Map<String, String> values = RedisProxy.getInstance().getRedisSession().hGetAll(key, CWConst.EXPIRE_TIME_90);
		Map<Integer, Integer> result = new HashMap<Integer, Integer>(values.size());
		for (Entry<String, String> entry : values.entrySet()) {
			result.put(Integer.valueOf(entry.getKey()), Integer.valueOf(entry.getValue()));
		}

		return result;
	}
	
	/**
	 * 获取商店指定商品购买次数
	 * @param playerId
	 * @param shopId
	 * @param seasonStr
	 * @return
	 */
	public int getCyborgShopItemBuyCount(String playerId, int shopId, String seasonStr) {
		StatisManager.getInstance().incRedisKey(CYBORG_SHOP_BUY);
		String key = CYBORG_SHOP_BUY + ":" + seasonStr + playerId;
		String result = RedisProxy.getInstance().getRedisSession().hGet(key, shopId + "", CWConst.EXPIRE_TIME_90);
		return NumberUtils.toInt(result);
	}
	
	/**
	 * 更新赛博商店物品购买次数
	 * 
	 * @param playerId
	 * @param shopId
	 * @param seasonStr
	 * @param count
	 */
	public void incrCyborgShopItemBuyCount(String playerId, int shopId, int count, String seasonStr) {
		StatisManager.getInstance().incRedisKey(CYBORG_SHOP_BUY);
		String key = CYBORG_SHOP_BUY + ":" + seasonStr + playerId;
		RedisProxy.getInstance().getRedisSession().hIncrBy(key, String.valueOf(shopId), count, CWConst.EXPIRE_TIME_90);
	}
	
	/**
	 * 批量更新赛博之战出战联盟信息
	 * @param swGuildDatas
	 * @param termId
	 * @return
	 */
	public boolean updateCWGuildData(List<CWGuildData> cwGuildDatas, int termId) {
		if (cwGuildDatas == null || cwGuildDatas.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (CWGuildData data : cwGuildDatas) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = CWACTIVITY_GUILD_INFO + ":" + termId;
		RedisProxy.getInstance().getRedisSession().hmSet(key, dataMap, CWConst.EXPIRE_TIME_30);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_GUILD_INFO);
		return true;
	}

	/**
	 * 获取赛博之战出战联盟信息
	 * @param guildId
	 * @param termId
	 * @return
	 */
	public CWGuildData getCWGuildData(String guildId, int termId) {
		String key = CWACTIVITY_GUILD_INFO + ":" + termId;
		String dataStr = RedisProxy.getInstance().getRedisSession().hGet(key, guildId, CWConst.EXPIRE_TIME_30);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_GUILD_INFO);
		CWGuildData guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, CWGuildData.class);
		}
		return guildData;
	}
	
	
	/**
	 * 批量更新记录赛博历史出战人员列表
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCWTeamLastJoins(List<CWTeamLastJoins> joinList) {
		Map<String, String> dataMap = new HashMap<>();
		for(CWTeamLastJoins data : joinList){
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		if(dataMap.isEmpty()){
			return false;
		}
		String key = CWACTIVITY_HISTORY_JOIN;
		RedisProxy.getInstance().getRedisSession().hmSet(key, dataMap, CWConst.EXPIRE_TIME_180);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_HISTORY_JOIN);
		return true;
	}
	
	/**
	 * 删除赛博之战历史出战人员列表
	 * @param activityInfo
	 * @return
	 */
	public boolean removeCWTeamLastJoins(String teamId) {
		String key = CWACTIVITY_HISTORY_JOIN;
		RedisProxy.getInstance().getRedisSession().hDel(key, teamId);
		StatisManager.getInstance().incRedisKey(CWACTIVITY_HISTORY_JOIN);
		return true;
	}
	
	/**
	 * 删除赛博之战历史出战人员列表
	 * @param activityInfo
	 * @return
	 */
	public boolean removeCWTeamLastJoins(List<String> teamIds) {
		if(CollectionUtils.isEmpty(teamIds)){
			return true;
		}
		String key = CWACTIVITY_HISTORY_JOIN;
		RedisProxy.getInstance().getRedisSession().hDel(key, teamIds.toArray(new String[teamIds.size()]));
		StatisManager.getInstance().incRedisKey(CWACTIVITY_HISTORY_JOIN);
		return true;
	}

	/**
	 * 获取赛博之战历史出战人员列表
	 * 
	 * @return
	 */
	public CWTeamLastJoins getCWTeamLastJoins(String teamId) {
		String key = CWACTIVITY_HISTORY_JOIN;
		String dataStr = RedisProxy.getInstance().getRedisSession().hGet(key, teamId, CWConst.EXPIRE_TIME_180);
		CWTeamLastJoins joinData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return new CWTeamLastJoins();
		} else {
			joinData = JSON.parseObject(dataStr, CWTeamLastJoins.class);
		}
		StatisManager.getInstance().incRedisKey(CWACTIVITY_HISTORY_JOIN);
		return joinData;
	}
	
	/**
	 * 更新赛博之战之战活动信息
	 * 
	 * @param activityInfo
	 * @return
	 */
	public boolean updateCLWActivityInfo(CLWActivityData activityInfo) {
		String jsonString = JSON.toJSONString(activityInfo);
		String key = CLWACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		RedisProxy.getInstance().getRedisSession().setString(key, jsonString, CrossConstCfg.getInstance().getcActivityRedisExpire());
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_SERVER_INFO);
		return true;
	}

	/**
	 * 获取赛博之战联赛活动信息
	 * 
	 * @return
	 */
	public CLWActivityData getCLWActivityInfo() {
		String key = CLWACTIVITY_SERVER_INFO + ":" + GsConfig.getInstance().getServerId();
		String dataStr = RedisProxy.getInstance().getRedisSession().getString(key);
		CLWActivityData activityInfo = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			activityInfo = new CLWActivityData();
		} else {
			activityInfo = JSON.parseObject(dataStr, CLWActivityData.class);
		}
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_SERVER_INFO);
		return activityInfo;
	}
	
	
	/**
	 * 获取赛博之战联赛联盟积分
	 * 
	 * @return
	 */
	public long getCLWGuildScore(String guildId, int season) {
		String key = CLWACTIVITY_GUILD_SCORE + ":" + season;
		Double score = RedisProxy.getInstance().getRedisSession().zScore(key, guildId, CWConst.EXPIRE_TIME_90);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_GUILD_SCORE);
		return (long) (score == null ? 0 : score);
	}
	
	/**
	 * 添加赛博之战联赛联盟积分
	 * 
	 * @return
	 */
	public long addCLWGuildScore(String guildId, int season, long score) {
		String key = CLWACTIVITY_GUILD_SCORE + ":" + season;
		Double result = RedisProxy.getInstance().getRedisSession().zIncrby(key, guildId, score, CWConst.EXPIRE_TIME_90);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_GUILD_SCORE);
		return (long) (result == null ? 0 : result);
	}
	
	/**
	 * 删除赛博之战联赛联盟积分
	 * 
	 * @return
	 */
	public void removeCLWGuildScore(String guildId, int season) {
		String key = CLWACTIVITY_GUILD_SCORE + ":" + season;
		 RedisProxy.getInstance().getRedisSession().zRem(key, CWConst.EXPIRE_TIME_90, guildId);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_GUILD_SCORE);
	}
	
	
	/**
	 * 刷新赛博之战战队段位排名
	 * @param season
	 * @param zoneId
	 * @param guildId
	 * @param score
	 * @return
	 */
	public boolean addCLWTeamStarRank(String teamId, long score, int season) {
		String key = CLWACTIVITY_TEAM_STAR_RANK + ":" + season;
		RedisProxy.getInstance().getRedisSession().zAdd(key, score, teamId, CWConst.EXPIRE_TIME_90);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_TEAM_STAR_RANK);
		return true;
	}
	
	/**
	 * 获取战队段位排名
	 * @param teamId
	 * @return
	 */
	public int getCLWTeamStarRank(String teamId, int season) {
		String key = CLWACTIVITY_TEAM_STAR_RANK + ":" + season;
		Long rank = RedisProxy.getInstance().getRedisSession().zrevrank(key, teamId, CWConst.EXPIRE_TIME_90);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_TEAM_STAR_RANK);
		if(rank == null ||rank == -1){
			return -1;
		}else{
			return (int) rank.intValue() + 1;
		}
	}
	
	/**
	 *  获取联盟战队段位积分
	 * @param teamId
	 * @return
	 */
	public long getCLWTeamStarScore(String teamId, int season){
		String key = CLWACTIVITY_TEAM_STAR_RANK + ":" + season;
		Double power = RedisProxy.getInstance().getRedisSession().zScore(key, teamId, CWConst.EXPIRE_TIME_90);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_TEAM_STAR_RANK);
		if(power == null){
			return 0;
		}else{
			return power.longValue();
		}
	}

	/**
	 * 批量刷新赛博之战战队段位排名
	 * @param season
	 * @param zoneId
	 * @param members
	 * @return
	 */
	public boolean addCLWTeamStarRanks(Map<String, Double> members, int season) {
		String key = CLWACTIVITY_TEAM_STAR_RANK + ":" + season;
		RedisProxy.getInstance().getRedisSession().zAdd(key, members, CWConst.EXPIRE_TIME_90);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_TEAM_STAR_RANK);
		return true;
	}
	
	/**
	 * 获取赛博之战战队段位排名
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<Tuple> getCLWTeamStarRanks(long start, long end, int season) {
		String key = CLWACTIVITY_TEAM_STAR_RANK + ":" + season;
		Set<Tuple> result = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(key, start, end, CWConst.EXPIRE_TIME_90);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_TEAM_STAR_RANK);
		return result;
	}
	
	/**
	 * 删除战队的赛博之战段位排名
	 * @param teamId
	 */
	public void removeCLWTeamStarRank(String teamId, int season){
		String key = CLWACTIVITY_TEAM_STAR_RANK + ":" + season;
		RedisProxy.getInstance().getRedisSession().zRem(key, 0, teamId);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_TEAM_STAR_RANK);
	}
	
	/**
	 * 获取赛博之战奖励领取信息
	 * 
	 * @return
	 */
	public List<Integer> getCLWRewardedList(String playerId, int season) {
		String key = CLWACTIVITY_REWARDED_LIST + ":" + season + ":" + playerId;
		Set<String> result = RedisProxy.getInstance().getRedisSession().sMembers(key);
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_REWARDED_LIST);
		List<Integer> idList = new ArrayList<>();
		for (String idStr : result) {
			idList.add(Integer.valueOf(idStr));
		}
		return idList;
	}
	
	/**
	 * 添加赛博之战奖励领取信息
	 * 
	 * @return
	 */
	public void addCLWRewardedIds(String playerId, int season, List<String> idList) {
		if(CollectionUtils.isEmpty(idList)){
			return;
		}
		String key = CLWACTIVITY_REWARDED_LIST + ":" + season + ":" + playerId;
		RedisProxy.getInstance().getRedisSession().sAdd(key, CWConst.EXPIRE_TIME_90, idList.toArray(new String[idList.size()]));
		StatisManager.getInstance().incRedisKey(CLWACTIVITY_REWARDED_LIST);
	}
	
}
