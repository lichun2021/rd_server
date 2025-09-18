package com.hawk.game.module.lianmengyqzz.march.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkTime;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hawk.game.GsConfig;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZBattleData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZGameData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinGuild;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinPlayerData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinServer;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZKickoutLock;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZMatchData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZMatchLock;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZMatchRoomData;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZRoomPlayerData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZActivityStateData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZRecordData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZSeasonGiftRecordData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZSeasonStateData;
import com.hawk.game.module.lianmengyqzz.march.data.local.YQZZStatisticsData;

public class YQZZDataManager {
	/** 活动状态数据*/
	private YQZZActivityStateData stateData;
	/** 房间匹配信息*/
	private YQZZMatchRoomData roomData;
	/** 参与服务器信息*/
	private Map<String,YQZZJoinServer> roomServers;
	/**参与联盟信息*/
	private Map<String,YQZZJoinGuild> roomGuilds;
	/** 结算信息*/
	private YQZZRecordData recordData;
	/** 战场统计数据*/
	private YQZZBattleData battleData;
	/** 统计数据*/
	private YQZZStatisticsData statisticsData;
	/** 战绩记录*/
	private Map<Integer,YQZZBattleData> history;
	private Cache<String, Boolean> joinExtarPlayers;

	private YQZZSeasonStateData seasonStateData;

	private Map<String, YQZZSeasonGiftRecordData> giftRecordDataMap = new HashMap<>();

	private List<YQZZSeasonGiftRecordData> giftRecordDataList = new ArrayList<>();


	public void init(){
		//初始化赛季状态
		this.loadToCacheYQZZSeasonStateData();
		//初始化活动状态数据
		this.loadToCacheYQZZActivityStateData();
		//初始化匹配房间数据
		this.loadToCacheRoomDdata();
		//服务器参与数据
		this.loadToCacheYQZZJoinServerDataForRoom();
		//联盟参与数据
		this.loadToCacheYQZZJoinGuildDataForRoom();
		//结算信息
		this.loadToCacheYQZZRecordData();
		//初始化战场数据
		this.loadToCacheYQZZBattleData();
		//初始化统计数据
		this.loadToCacheYQZZStatisticsData();
		//本赛季分发奖励
		this.loadSeasonGiftData();
		//初始化记录
		this.history = new HashMap<>();
		//受限玩家加入副本
		this.joinExtarPlayers = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build();
	}
	
	public void clearData(){
		this.roomData = null;
		this.roomServers = null;
		this.roomGuilds = null;
		this.battleData = null;
		this.joinExtarPlayers = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build();
	}
	
	/**
	 * 加载活动状态
	 * @return
	 */
	public YQZZActivityStateData loadToCacheYQZZActivityStateData(){
		String serverId = GsConfig.getInstance().getServerId();
		YQZZActivityStateData data = YQZZActivityStateData.loadData(serverId);
		if(data == null){
			data = new YQZZActivityStateData();
			data.setServerId(serverId);
			data.saveRedis();
		}
		this.stateData = data;
		return this.stateData;
	}

	public YQZZSeasonStateData loadToCacheYQZZSeasonStateData(){
		String serverId = GsConfig.getInstance().getServerId();
		YQZZSeasonStateData data = YQZZSeasonStateData.loadData(serverId);
		if(data == null){
			data = new YQZZSeasonStateData();
			data.saveRedis();
		}
		this.seasonStateData = data;
		return this.seasonStateData;
	}
	
	
	/**
	 * 加载本服所在战场相关参与服数据
	 * @return
	 */
	public Map<String,YQZZJoinServer> loadToCacheYQZZJoinServerDataForRoom(){
		int termId = this.stateData.getTermId();
		List<String> serverIds = new ArrayList<>();
		if(this.roomData!= null){
			serverIds.addAll(this.roomData.getServers());
		}
		Map<String,YQZZJoinServer> map = YQZZJoinServer.loadAll(termId,serverIds);
		this.roomServers = map;
		return this.roomServers;
	}
	
	/**
	 * 加载房间所以参数联盟
	 * @return
	 */
	public Map<String,YQZZJoinGuild> loadToCacheYQZZJoinGuildDataForRoom(){
		int termId = this.stateData.getTermId();
		List<String> serverIds = new ArrayList<>();
		if(this.roomServers!= null){
			for(YQZZJoinServer server : this.roomServers.values()){
				serverIds.addAll(server.getJoinGuilds());
			}
		}
		Map<String,YQZZJoinGuild> map = YQZZJoinGuild.loadAllData(termId,serverIds);
		this.roomGuilds = map;
		return this.roomGuilds;
	}
	
	/**
	 * 获取全部参与服数据
	 * @return
	 */
	public Map<String,YQZZJoinServer> loadAllYQZZJoinServerData(){
		int termId = this.stateData.getTermId();
		return YQZZJoinServer.loadAll(termId);
	}
	
	/**
	 * 加载结算数据
	 * @param termId
	 * @param serverId
	 * @return
	 */
	public YQZZRecordData loadToCacheYQZZRecordData(){
		int termId = this.stateData.getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		YQZZRecordData data = YQZZRecordData.loadData(serverId, termId);
		this.recordData = data;
		return this.recordData;
	}
	
	
	/**
	 * 加载战场数据
	 * @return
	 */
	public YQZZBattleData loadToCacheYQZZBattleData(){
		if(this.roomData == null){
			return null;
		}
		String roomId = this.roomData.getRoomId();
		YQZZBattleData data = YQZZBattleData.loadData(roomId);
		this.battleData = data;
		return this.battleData;
	}
	
	public YQZZStatisticsData loadToCacheYQZZStatisticsData(){
		String serverId = GsConfig.getInstance().getServerId();
		YQZZStatisticsData data = YQZZStatisticsData.loadData(serverId);
		if(data == null){
			data = new YQZZStatisticsData();
			data.setServerId(serverId);
			data.saveRedis();
		}
		this.statisticsData = data;
		return this.statisticsData;
	}
	
	
	public YQZZMatchRoomData loadToCacheRoomDdata(){
		int termId = this.stateData.getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		Map<String,YQZZMatchRoomData> dataMap = YQZZMatchRoomData.loadAllData(termId);
		for(YQZZMatchRoomData data : dataMap.values()){
			if(data.getServers().contains(serverId)){
				this.roomData = data;
				return this.roomData;
			}
		}
		this.roomData = null;
		return this.roomData;
	}

	private void loadSeasonGiftData(){
		int season = this.seasonStateData.getSeason();
		Map<String, YQZZSeasonGiftRecordData> recordDataMap = YQZZSeasonGiftRecordData.loadAll(season, GsConfig.getInstance().getServerId());
		List<YQZZSeasonGiftRecordData> recordDataList = new ArrayList<>();
		recordDataList.addAll(recordDataMap.values());
		Collections.sort(recordDataList, new Comparator<YQZZSeasonGiftRecordData>(){
			@Override
			public int compare(YQZZSeasonGiftRecordData o1, YQZZSeasonGiftRecordData o2) {
				if(o1.getSendTime() != o2.getSendTime()){
					return o1.getSendTime() < o2.getSendTime() ? -1 : 1;
				}
				return 0;
			}
		});
		this.giftRecordDataMap = recordDataMap;
		this.giftRecordDataList = recordDataList;
	}
	
	public YQZZActivityStateData getStateData() {
		return stateData;
	}

	public YQZZSeasonStateData getSeasonStateData() {
		return seasonStateData;
	}

	public YQZZMatchLock createYQZZMatchLock(int expireTime){
		int termId = this.stateData.getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		YQZZMatchLock lock = new YQZZMatchLock(termId, serverId, expireTime);
		return lock;
	}

	public YQZZKickoutLock createYQZZKickoutLock(int expireTime){
		int termId = this.stateData.getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		YQZZKickoutLock lock = new YQZZKickoutLock(termId, serverId, expireTime);
		return lock;
	}
	
	
	public Map<String, YQZZJoinServer> getRoomServerDataMap() {
		return this.roomServers;
	}
	
	public YQZZJoinServer getRoomServerById(String serverId){
		return this.roomServers.get(serverId);
	}
	
	public Map<String, YQZZJoinGuild> getRoomGuilds() {
		return roomGuilds;
	}
	
	
	public Map<String, YQZZJoinGuild> getRoomGuildsByServer(String serverId) {
		Map<String, YQZZJoinGuild> rlt = new HashMap<>();
		YQZZJoinServer server = this.roomServers.get(serverId);
		if(server == null){
			return rlt;
		}
		for(String guildId: server.getJoinGuilds()){
			YQZZJoinGuild guild = this.roomGuilds.get(guildId);
			if(guild != null){
				rlt.put(guild.getGuildId(), guild);
			}
		}
		return rlt;
	}
	
	public YQZZRecordData getRecordData() {
		return recordData;
	}
	
	public YQZZBattleData getBattleData() {
		return battleData;
	}
	
	public YQZZMatchRoomData getRoomData() {
		return roomData;
	}
	
	public YQZZStatisticsData getStatisticsData() {
		return statisticsData;
	}
	
	
	
	
	/**
	 * 加载匹配数据
	 * @return
	 */
	public YQZZMatchData loadYQZZMatchData(){
		int termId = this.stateData.getTermId();
		YQZZMatchData data = YQZZMatchData.loadData(termId);
		return data;
	}
	
	public Map<String,YQZZRoomPlayerData> loadYQZZPlayerDataForRoom(int termId,String roomId){
		Map<String,YQZZRoomPlayerData> map = YQZZRoomPlayerData.loadAllRoomPlayer(termId, roomId);
		return map;
	}
	
	
	
	
	public YQZZJoinPlayerData loadYQZZJoinPlayerData(int termId,String playerId){
		YQZZJoinPlayerData data = YQZZJoinPlayerData.loadData(termId, playerId);
		return data;
	}
	
	public YQZZGameData loadYQZZGameData(int termId,String roomId){
		YQZZGameData data = YQZZGameData.loadData(termId, roomId);
		return data;
	}
	
	public void updateYQZZGameDataActiveTime(String roomId){
		int termId = this.stateData.getTermId();
		YQZZGameData data = YQZZGameData.loadData(termId, roomId);
		if(data != null){
			data.setLastActiveTime(HawkTime.getMillisecond());
			data.saveRedis();
		}
	}
	
	public void updateYQZZGameDataFinishTime(int termId,String roomId){
		YQZZGameData data = YQZZGameData.loadData(termId, roomId);
		if(data != null){
			data.setFinishTime(HawkTime.getMillisecond());
			data.saveRedis();
		}
	}
	
	public YQZZBattleData getHistoyrYQZZBattleData(int termId,String roomId){
		if(this.history.size() > 30){
			this.history = new HashMap<>();
		}
		YQZZBattleData data = this.history.get(termId);
		if(data != null){
			return data;
		}
		YQZZBattleData battleData = YQZZBattleData.loadDataWithoutSecondMap(roomId);
		this.history.put(termId, battleData);
		return battleData;
	}
	
	public synchronized boolean addJoinExtraPlayer(String playerId, int limitSize) {
		if (this.joinExtarPlayers.getIfPresent(playerId) != null) {
			return true;
		}
		long size = this.joinExtarPlayers.size();
		if (size >= limitSize) {
			return false;
		}
		this.joinExtarPlayers.put(playerId, Boolean.TRUE);
		return true;
	}
	
	public void removeJoinExtraPlayer(String playerId){
//		this.joinExtarPlayers.remove(playerId); 进入的玩家在过期时间内,仍占有位置,可以随意进入, 不删除
	}

	public void addSeasonGiftRecord(YQZZSeasonGiftRecordData recordData){
		giftRecordDataMap.put(recordData.getPlayerId(), recordData);
		giftRecordDataList.add(recordData);
		recordData.saveRedis();
	}

	public List<YQZZSeasonGiftRecordData> getGiftRecordDataList() {
		return giftRecordDataList;
	}

	public Map<String, YQZZSeasonGiftRecordData> getGiftRecordDataMap() {
		return giftRecordDataMap;
	}
}
