
package com.hawk.game.module.lianmengyqzz.march.data.global;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZBuildCfg;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.protocol.YQZZ.PBYQZZBuildHonor;
import com.hawk.game.protocol.YQZZ.PBYQZZBuildTime;
import com.hawk.game.protocol.YQZZ.PBYQZZFoggyHonor;
import com.hawk.game.protocol.YQZZ.PBYQZZGameInfoSync;
import com.hawk.game.protocol.YQZZ.PBYQZZGuildInfo;
import com.hawk.game.protocol.YQZZ.PBYQZZMonsterHonor;
import com.hawk.game.protocol.YQZZ.PBYQZZNationInfo;
import com.hawk.game.protocol.YQZZ.PBYQZZPlayerInfo;
import com.hawk.game.protocol.YQZZ.PBYQZZPylonHonor;
import com.hawk.game.protocol.YQZZ.PBYQZZResHonor;
import com.hawk.game.protocol.YQZZ.PBYQZZSecondMapResp;

public class YQZZBattleData {
	private static final String redisKey = "YQZZ_ACTIVITY_BATTLE_DATA";
	private static final String field1 = "GAME_INFO_SYNC";
	private static final String field2 = "SECOND_MAP_RESP";
	
	private PBYQZZGameInfoSync yqzzSyncData;
	//解析数据
	private Map<String,YQZZCountryGameData> countryDatas;
	private Map<String,YQZZGuildGameData> guildDatas;
	private Map<String,YQZZPlayerGameData> playerDatas;
	//结束时间
	private long startTime;
	private long finishTime;
	
	
	//小地图数据
	private Map<String,PBYQZZSecondMapResp.Builder> secondMap;
	
	
	public YQZZBattleData() {
	}
	
	
	public int getServerHoldBuildsById(String serverId,int bid){
		if(this.countryDatas == null){
			return 0;
		}
		YQZZCountryGameData data = this.countryDatas.get(serverId);
		if(data == null){
			return 0;
		}
		return data.getBuildControl().getOrDefault(bid, 0);
	}
	
	
	
	public int getGuildHoldBuildsById(String guild,int bid){
		if(this.guildDatas == null){
			return 0;
		}
		YQZZGuildGameData data = this.guildDatas.get(guild);
		if(data == null){
			return 0;
		}
		return data.getControlBuildTypes().getOrDefault(bid, 0);
	}
	
	
	public Set<Integer> getGuildOccupyBuilds(String guild){
		Set<Integer> set = new HashSet<>();
		if(this.guildDatas == null){
			return set;
		}
		YQZZGuildGameData data = this.guildDatas.get(guild);
		if(data == null){
			return set;
		}
		set.addAll(data.getOccupyBuildTypes().keySet());
		return set;
	}
	
	
	public long getCountryScore(String serverId){
		if(this.countryDatas == null){
			return 0;
		}
		YQZZCountryGameData data = this.countryDatas.get(serverId);
		if(data == null){
			return 0;
		}
		return data.getScore();
	}
	
	public long getGuildScore(String guild){
		if(this.guildDatas == null){
			return 0;
		}
		YQZZGuildGameData data = this.guildDatas.get(guild);
		if(data == null){
			return 0;
		}
		return data.getScore();
	}
	
	public long getGuildKillFoggy(String guildId) {
		if(this.guildDatas == null){
			return 0;
		}
		YQZZGuildGameData data = this.guildDatas.get(guildId);
		if(data == null){
			return 0;
		}
		return data.getFoggyCnt();
	}
	
	public long getGuildKillMonster(String guildId) {
		if(this.guildDatas == null){
			return 0;
		}
		YQZZGuildGameData data = this.guildDatas.get(guildId);
		if(data == null){
			return 0;
		}
		return data.getMonsterCnt();
	}
	
	public long getGuildKillPylon(String guildId) {
		if(this.guildDatas == null){
			return 0;
		}
		YQZZGuildGameData data = this.guildDatas.get(guildId);
		if(data == null){
			return 0;
		}
		return data.getPylonCnt();
	}
	
	public long getPlayerScore(String playerId){
		if(this.playerDatas == null){
			return 0;
		}
		YQZZPlayerGameData data = this.playerDatas.get(playerId);
		if(data == null){
			return 0;
		}
		return data.getScore();
	}
	
	
	public int getCountryRank(String serverId){
		if(this.countryDatas == null){
			return 0;
		}
		YQZZCountryGameData data = this.countryDatas.get(serverId);
		if(data == null){
			return 0;
		}
		return data.getRank();
	}
	
	public int getGuildRank(String guild){
		if(this.guildDatas == null){
			return 0;
		}
		YQZZGuildGameData data = this.guildDatas.get(guild);
		if(data == null){
			return 0;
		}
		return data.getRank();
	}
	
	public int getPlayerRank(String playerId){
		if(this.playerDatas == null){
			return 0;
		}
		YQZZPlayerGameData data = this.playerDatas.get(playerId);
		if(data == null){
			return 0;
		}
		return data.getRank();
	}
	
	public long getPlayerInBuildTimeByType(String playerId,int type){
		if(this.playerDatas == null){
			return 0;
		}
		YQZZPlayerGameData data = this.playerDatas.get(playerId);
		if(data == null){
			return 0;
		}
		return data.getBuildingTypeTime().getOrDefault(type, 0l);
	}
	
	
	public int getCountryCount(){
		if(this.countryDatas == null){
			return 0;
		}
		return countryDatas.size();
	}
	
	public int getGuildCount(){
		if(this.guildDatas == null){
			return 0;
		}
		return guildDatas.size();
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getFinishTime() {
		return finishTime;
	}

	public PBYQZZGameInfoSync getYqzzSyncData() {
		return yqzzSyncData;
	}


	public Map<String, YQZZCountryGameData> getCountryDatas() {
		return countryDatas;
	}
	
	public Map<String, YQZZGuildGameData> getGuildDatas() {
		return guildDatas;
	}
	
	public Map<String, YQZZPlayerGameData> getPlayerDatas() {
		return playerDatas;
	}
	
	public void setSecondMap(Map<String, PBYQZZSecondMapResp.Builder> secondMap) {
		this.secondMap = secondMap;
	}
	
	public Map<String, PBYQZZSecondMapResp.Builder> getSecondMap() {
		return secondMap;
	}
	
	public void parserData(PBYQZZGameInfoSync builder,String roomId){
		try {
			this.yqzzSyncData = builder;
			if(builder.hasGameStartTime()){
				this.startTime = builder.getGameStartTime();
			}
			if(builder.hasGameOverTime()){
				this.finishTime = builder.getGameOverTime();
			}
			//国家信息
			Map<String,YQZZCountryGameData> countryMap = new HashMap<>();
			//联盟信息
			Map<String,YQZZGuildGameData> guildMap = new HashMap<>();
			//玩家数据
			Map<String,YQZZPlayerGameData> playerMap = new HashMap<>();
			//填充国家数据
			List<PBYQZZNationInfo> nationInfoList = builder.getNationInfoList();
			for(PBYQZZNationInfo nationInfo : nationInfoList){
				YQZZCountryGameData data = new YQZZCountryGameData();
				data.setServerId(nationInfo.getServerId());
				data.setScore(nationInfo.getNationHonor());
				data.setPylonCnt(nationInfo.getPylonCnt());
				countryMap.put(data.getServerId(), data);
			}
			
			List<PBYQZZGuildInfo> guildInfoList = builder.getGuildInfoList();
			for(PBYQZZGuildInfo guildInfo : guildInfoList){
			  	YQZZGuildGameData data = new YQZZGuildGameData();
				data.setServerId(guildInfo.getServerId());
				data.setGuildId(guildInfo.getGuildId());
				data.setGuildName(guildInfo.getGuildName());
				data.setGuildFlag(guildInfo.getGuildFlag());
				data.setGuildTag(guildInfo.getGuildTag());
				data.setLeaderId(guildInfo.getLeaderId());
				data.setLeaderName(guildInfo.getLeaderName());
				data.setTeamPower(guildInfo.getTeamPower());
				data.setScore(guildInfo.getHonor());
				data.setPylonCnt(guildInfo.getPylonCnt());
				data.setMonsterCnt(guildInfo.getKillMonster());
				//当时控制ID
				Set<Integer> controlBuilds = data.getControlBuilds();
				controlBuilds.addAll(guildInfo.getControlBuildIdList());
				//类型-数量
				Map<Integer,Integer> controlBuildTypes = data.getControlBuildTypes();
				for(int buildId : controlBuilds){
					YQZZBuildCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YQZZBuildCfg.class, buildId);
					if(cfg == null){
						continue;
					}
					int typeId = cfg.getBuildTypeId();
					int count = controlBuildTypes.getOrDefault(typeId, 0);
					count ++;
					controlBuildTypes.put(typeId, count);
					//国家控制数据量添加
					YQZZCountryGameData countryData = countryMap.get(guildInfo.getServerId());
					if(countryData!= null){
						countryData.addControlByType(typeId, 1);
					}
				}
				//攻占过建筑  类型-数量
				Map<Integer,Integer> occupyBuildTypes = data.getOccupyBuildTypes();
				YQZZCountryGameData selfCountryData = countryMap.get(guildInfo.getServerId());
				//建筑提供记分
				Map<Integer,YQZZGameDataBuildScore> guildBuildScores = data.getGuildBuildScore();
				Map<Integer,YQZZGameDataBuildScore> playerBuildScores = data.getPlayerBuildScore();
				Map<Integer,YQZZGameDataBuildScore> countryBuildScores = new HashMap<>();
				if(selfCountryData != null){
					countryBuildScores = selfCountryData.getBuildScore();
				}
				List<PBYQZZBuildHonor> buildHonors =  guildInfo.getBuildHonorsList();
				for(PBYQZZBuildHonor buildHonor : buildHonors){
					int buildId = buildHonor.getBuildId();
					long guildScore = buildHonor.getGuildHonor();
					long playerScore = buildHonor.getPlayerHonor();
					if(guildScore <= 0){
						continue;
					}
					YQZZBuildCfg cfg = HawkConfigManager.getInstance().getConfigByKey(YQZZBuildCfg.class, buildId);
					if(cfg == null){
						continue;
					}
					int buildType = cfg.getBuildTypeId();
					//累积联盟建筑记分
					YQZZGameDataBuildScore guildScoreParam = guildBuildScores.get(buildType);
					if(guildScoreParam == null){
						guildScoreParam = new YQZZGameDataBuildScore(buildType, 0, 0l);
						guildBuildScores.put(buildType, guildScoreParam);
					}
					int guildControlCount = guildScoreParam.getCount() + 1;
					long guildControlScore = guildScoreParam.getScore() + guildScore;
					guildScoreParam.setCount(guildControlCount);
					guildScoreParam.setScore(guildControlScore);
					//累积联盟玩家建筑记分
					YQZZGameDataBuildScore playerParam = playerBuildScores.get(buildType);
					if(playerParam == null){
						playerParam = new YQZZGameDataBuildScore(buildType, 0, 0l);
						playerBuildScores.put(buildType, playerParam);
					}
					int playerControlCount = playerParam.getCount() + 1;
					long playerControlScore = playerParam.getScore() + playerScore;
					playerParam.setCount(playerControlCount);
					playerParam.setScore(playerControlScore);


					YQZZGameDataBuildScore countryParam = countryBuildScores.get(buildType);
					if(countryParam == null){
						countryParam = new YQZZGameDataBuildScore(buildType, 0, 0l);
						countryBuildScores.put(buildType, countryParam);
					}
					int countryControlCount = countryParam.getCount() + 1;
					long countryControlScore = countryParam.getScore() + buildHonor.getNationHonor();
					countryParam.setCount(countryControlCount);
					countryParam.setScore(countryControlScore);
					//攻占过建筑类型
					int count = occupyBuildTypes.getOrDefault(buildType, 0);
					count ++;
					occupyBuildTypes.put(buildType, count);
					//所在服务器攻占过建筑类型
					YQZZCountryGameData countryData = countryMap.get(guildInfo.getServerId());
					if(countryData!= null){
						countryData.addOccupyByType(buildType, 1);
					}
				}
				guildMap.put(data.getGuildId(), data);
			}
			
			List<PBYQZZPlayerInfo> playerInfoList = builder.getPlayerInfoList();
			for(PBYQZZPlayerInfo playerInfo : playerInfoList){
				YQZZPlayerGameData data = new YQZZPlayerGameData();
				YQZZGuildGameData guildData = guildMap.get(playerInfo.getGuildId());
				if(guildData == null){
					continue;
				}
				data.setServerId(guildData.getServerId());
				data.setRoomId(roomId);
				data.setPlayerId(playerInfo.getPlayerId());
				data.setPlayerName(playerInfo.getName());
				data.setPlayerGuild(guildData.getGuildId());
				data.setPlayerGuildName(guildData.getGuildName());
				data.setScore(playerInfo.getHonor());
				data.setKillPower(playerInfo.getKillPower());
				data.setPylonCnt((int) playerInfo.getPylonHonorsList().stream().mapToLong(PBYQZZPylonHonor::getPylonCount).sum());
				YQZZCountryGameData countryData = countryMap.get(guildData.getServerId());
				//刷怪记录
				Map<Integer,YQZZGameDataMonsterKill> monsterScore = data.getMonsterScore();
				List<PBYQZZMonsterHonor> monsters = playerInfo.getMonsterHonorsList();
				for(PBYQZZMonsterHonor monsterHonor : monsters){
					int monsterId = monsterHonor.getMonsterId();
					WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance()
							.getConfigByKey(WorldEnemyCfg.class, monsterId);
					if(monsterCfg == null){
						continue;
					}
					int level = monsterCfg.getLevel();
					int count = monsterHonor.getKillCount();
					long score = monsterHonor.getPlayerHonor();
					YQZZGameDataMonsterKill dataParam = monsterScore.get(level);
					if(dataParam == null){
						dataParam = new YQZZGameDataMonsterKill(level,0,0l);
						monsterScore.put(level, dataParam);
					}
					int updateCount = dataParam.getCount() + count;
					long updateScore = dataParam.getScore() + score;
					dataParam.setCount(updateCount);
					dataParam.setScore(updateScore);
				}
				//幽灵基地记录
				Map<Integer,YQZZGameDataYuriKill> yuriScore = data.getYuriScore();
				List<PBYQZZFoggyHonor> yuris = playerInfo.getFoggyHonorsList();
				for(PBYQZZFoggyHonor yuri : yuris){
					int yuriId = yuri.getFoggyFortressId();
					FoggyFortressCfg foggyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, yuriId);
					if(foggyCfg == null){
						continue;
					}
					int level = foggyCfg.getLevel();
					long score = yuri.getPlayerHonor();
					YQZZGameDataYuriKill dataParam = yuriScore.get(level);
					if(dataParam == null){
						dataParam = new YQZZGameDataYuriKill(level,0,0l);
						yuriScore.put(level, dataParam);
					}
					int updateCount = dataParam.getCount() + yuri.getKillCount();
					long updateScore = dataParam.getScore() + score;
					dataParam.setCount(updateCount);
					dataParam.setScore(updateScore);
					guildData.setFoggyCnt(guildData.getFoggyCnt()+ yuri.getKillCount());
					if(countryData!= null){
						Map<Integer,YQZZGameDataYuriKill> countryDataYuriScore = countryData.getYuriScore();
						YQZZGameDataYuriKill countryDataParam = countryDataYuriScore.get(level);
						if(countryDataParam == null){
							countryDataParam = new YQZZGameDataYuriKill(level,0,0l);
							countryDataYuriScore.put(level, countryDataParam);
						}
						int countryUpdateCount = countryDataParam.getCount() + yuri.getKillCount();
						long countryUpdateScore = countryDataParam.getScore() + yuri.getNationHonor();
						countryDataParam.setCount(countryUpdateCount);
						countryDataParam.setScore(countryUpdateScore);
					}
				}
				//采矿记录
				Map<Integer,YQZZGameDataResCollect> resScore = data.getResScore();
				List<PBYQZZResHonor> ress = playerInfo.getResHonorsList();
				for(PBYQZZResHonor resHonor : ress){
					int resId = resHonor.getResourceId();
					long count = resHonor.getResourceCount();
					long score = resHonor.getPlayerHonor();
					YQZZGameDataResCollect dataParam = resScore.get(resId);
					if(dataParam == null){
						dataParam = new YQZZGameDataResCollect(resId,0l,0l);
						resScore.put(resId, dataParam);
					}
					long updateCount = dataParam.getCount() + count;
					long updateScore = dataParam.getScore() + score;
					dataParam.setCount(updateCount);
					dataParam.setScore(updateScore);
				}
				Map<Integer,Long> buildTimes = data.getBuildingTypeTime();
				List<PBYQZZBuildTime> buildTimeList = playerInfo.getBuildTimesList();
				for(PBYQZZBuildTime buildTime : buildTimeList){
					int buildType = buildTime.getBuildType();
					long time = buildTime.getBuildTimes();
					long timeCount = buildTimes.getOrDefault(buildType, 0l);
					timeCount += time;
					buildTimes.put(buildType, timeCount);
				}
				playerMap.put(data.getPlayerId(), data);
			}
			//排序
			List<YQZZCountryGameData> countryList = new ArrayList<>();
			countryList.addAll(countryMap.values());
			Collections.sort(countryList, new Comparator<YQZZCountryGameData>() {
				@Override
				public int compare(YQZZCountryGameData o1, YQZZCountryGameData o2) {
					if(o1.getScore() != o2.getScore()){
						return o1.getScore() > o2.getScore() ? -1 : 1;
					}else if (o1.getServerIdNum() != o2.getServerIdNum()) {
						return o1.getServerIdNum() > o2.getServerIdNum() ? 1 : -1;
					} 
					return 0;
				}
			} );
			int countryRank = 1;
			for(YQZZCountryGameData data : countryList){
				data.setRank(countryRank);
				countryRank ++;
				HawkLog.logPrintln("YQZZBattleData parserData YQZZCountryGameData,serverId:{},score:{},rank:{}",
						data.getServerId(),data.getScore(),data.getRank());
			}
			//排序
			List<YQZZGuildGameData> guildList = new ArrayList<>();
			guildList.addAll(guildMap.values());
			Collections.sort(guildList, new Comparator<YQZZGuildGameData>() {
				@Override
				public int compare(YQZZGuildGameData o1, YQZZGuildGameData o2) {
					if(o1.getScore() != o2.getScore()){
						return o1.getScore() > o2.getScore() ? -1 : 1;
					}else if (o1.getTeamPower() != o2.getTeamPower()) {
						return o1.getTeamPower() > o2.getTeamPower() ?-1 : 1;
					}
					return 0;
				}
			} );
			int guildRank = 1;
			for(YQZZGuildGameData data : guildList){
				data.setRank(guildRank);
				guildRank ++;
				HawkLog.logPrintln("YQZZBattleData parserData YQZZGuildGameData,serverId:{},guildId:{},score:{},rank:{}",
						data.getServerId(),data.getGuildId(),data.getScore(),data.getRank());
				Map<Integer,Integer>ControlBuildTypes = data.getControlBuildTypes();
				for(Map.Entry<Integer,Integer> entry : ControlBuildTypes.entrySet()){
					HawkLog.logPrintln("YQZZBattleData parserData YQZZGuildGameData controls,serverId:{},guildId:{},score:{},rank:{},btype:{},count:{}",
							data.getServerId(),data.getGuildId(),data.getScore(),data.getRank(),entry.getKey(),entry.getValue());
				}
			}
			//排序
			List<YQZZPlayerGameData> playerList = new ArrayList<>();
			playerList.addAll(playerMap.values());
			Collections.sort(playerList, new Comparator<YQZZPlayerGameData>() {
				@Override
				public int compare(YQZZPlayerGameData o1, YQZZPlayerGameData o2) {
					if(o1.getScore() != o2.getScore()){
						return o1.getScore() > o2.getScore() ? -1 : 1;
					}else if (o1.getKillPower() != o2.getKillPower()) {
						return o1.getKillPower() > o2.getKillPower() ?-1 : 1;
					}
					return 0;
				}
			} );
			int playerRank = 1;
			for(YQZZPlayerGameData data : playerList){
				data.setRank(playerRank);
				playerRank ++;
			}
			this.countryDatas = countryMap;
			this.guildDatas = guildMap;
			this.playerDatas = playerMap;
			HawkLog.logPrintln("YQZZBattleData parserData player size:{},guildSize:{},contrySize:{}",playerMap.size(),guildMap.size(),countryMap.size());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	
	
	public static void saveSourceData(PBYQZZGameInfoSync data,Map<String,PBYQZZSecondMapResp> secondMap,String gameId){
		String key = redisKey  + ":" + gameId;
		Map<byte[], byte[]> map = new HashMap<>();
		if(data != null){
			map.put(field1.getBytes(), data.toByteArray());
		}
		if(secondMap!= null){
			for(Map.Entry<String, PBYQZZSecondMapResp> entry : secondMap.entrySet()){
				String guildId = entry.getKey();
				PBYQZZSecondMapResp second = entry.getValue();
				String guildMapField = field2 + "_" + guildId;
				map.put(guildMapField.getBytes(), second.toByteArray());
			}
		}
		if(map.isEmpty()){
			return;
		}
		RedisProxy.getInstance().getRedisSession().hmSetBytes(key, map, YQZZConst.REDIS_DATA_EXPIRE_TIME);
		StatisManager.getInstance().incRedisKey(redisKey);
	}
	
	
	
	public static YQZZBattleData loadDataWithoutSecondMap(String roomId){
		String key = redisKey  + ":" + roomId;
		StatisManager.getInstance().incRedisKey(redisKey);
		byte[] byteData = RedisProxy.getInstance().getRedisSession().hGetBytes(key, field1);
		if(byteData == null || byteData.length <= 0){
			return null;
		}
		YQZZBattleData data = new YQZZBattleData();
		PBYQZZGameInfoSync.Builder builder = PBYQZZGameInfoSync.newBuilder();
		try {
			builder.mergeFrom(byteData);
			data.parserData(builder.build(),roomId);
			return data;
		} catch (InvalidProtocolBufferException e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	
	public static YQZZBattleData loadData(String roomId){
		String key = redisKey  + ":" + roomId;
		StatisManager.getInstance().incRedisKey(redisKey);
		Map<byte[], byte[]> map = RedisProxy.getInstance().getRedisSession().hGetAllBytes(key.getBytes());	
		if(map == null || map.size() <= 0){
			return null;
		}
		YQZZBattleData data = new YQZZBattleData();
		Map<String,PBYQZZSecondMapResp.Builder> secondMap = new HashMap<>();
		data.setSecondMap(secondMap);
		for(Map.Entry<byte[], byte[]> entry : map.entrySet()){
			byte[] keyArr = entry.getKey();
			byte[] dataArr = entry.getValue();
			String dataKey  = new String(keyArr);
			if(dataKey.startsWith(field1)){
				PBYQZZGameInfoSync.Builder builder = PBYQZZGameInfoSync.newBuilder();
				try {
					builder.mergeFrom(dataArr);
					data.parserData(builder.build(),roomId);
				} catch (InvalidProtocolBufferException e) {
					HawkException.catchException(e);
				}
			}
			
			if(dataKey.startsWith(field2)){
				try {
					String[] dataKeyArr = dataKey.split("_");
					String guildId = dataKeyArr[dataKeyArr.length - 1];
					PBYQZZSecondMapResp.Builder builder = PBYQZZSecondMapResp.newBuilder();
					builder.mergeFrom(dataArr);
					secondMap.put(guildId, builder);
				} catch (InvalidProtocolBufferException e) {
					HawkException.catchException(e);
				}
			}
		}
		
		
		return data;
	}
	
	
	public static class YQZZPlayerGameData{
		private String serverId;
		private String roomId;
		private String playerId;
		private String playerName;
		private String playerGuild;
		private String playerGuildName;
		private long killPower;
		private long score;
		private int rank;
		private int pylonCnt;
		private Map<Integer,YQZZGameDataMonsterKill> monsterScore = new HashMap<>();
		private Map<Integer,YQZZGameDataYuriKill> yuriScore = new HashMap<>();
		private Map<Integer,YQZZGameDataResCollect> resScore = new HashMap<>();
		private Map<Integer,Long> buildingTypeTime = new HashMap<>();
		public String getServerId() {
			return serverId;
		}
		public void setServerId(String serverId) {
			this.serverId = serverId;
		}
		public String getRoomId() {
			return roomId;
		}
		public void setRoomId(String roomId) {
			this.roomId = roomId;
		}
		public String getPlayerId() {
			return playerId;
		}
		public void setPlayerId(String playerId) {
			this.playerId = playerId;
		}
		public String getPlayerName() {
			return playerName;
		}
		public void setPlayerName(String playerName) {
			this.playerName = playerName;
		}
		public String getPlayerGuild() {
			return playerGuild;
		}
		public void setPlayerGuild(String playerGuild) {
			this.playerGuild = playerGuild;
		}
		public String getPlayerGuildName() {
			return playerGuildName;
		}
		public void setPlayerGuildName(String playerGuildName) {
			this.playerGuildName = playerGuildName;
		}
		public long getKillPower() {
			return killPower;
		}
		public void setKillPower(long killPower) {
			this.killPower = killPower;
		}
		public long getScore() {
			return score;
		}
		public void setScore(long score) {
			this.score = score;
		}
		public int getRank() {
			return rank;
		}
		public void setRank(int rank) {
			this.rank = rank;
		}
		public int getPylonCnt() {
			return pylonCnt;
		}
		public void setPylonCnt(int pylonCnt) {
			this.pylonCnt = pylonCnt;
		}

		public Map<Integer, YQZZGameDataMonsterKill> getMonsterScore() {
			return monsterScore;
		}
		
		public Map<Integer, YQZZGameDataResCollect> getResScore() {
			return resScore;
		}
		
		public Map<Integer, YQZZGameDataYuriKill> getYuriScore() {
			return yuriScore;
		}
		
		public Map<Integer, Long> getBuildingTypeTime() {
			return buildingTypeTime;
		}
	}
	
	public static class YQZZGuildGameData{
		private String serverId;
		
		private String guildId;
		
		private String guildName;
		
		private String guildTag;
		
		private int guildFlag;
		
		private String leaderId;
		
		private String leaderName;
		
		private long teamPower;
		
		private long score;
		
		private int rank;
		private int pylonCnt;
		private int foggyCnt;
		private int monsterCnt;
		//建筑类型 -  数量  -  记分
		private Map<Integer,YQZZGameDataBuildScore> guildBuildScore = new HashMap<>();
		//建筑类型 -  数量  -  记分
		private Map<Integer,YQZZGameDataBuildScore> playerBuildScore = new HashMap<>();
		private Set<Integer> controlBuilds = new HashSet<>();
		private Map<Integer,Integer> controlBuildTypes = new HashMap<>();
		private Map<Integer,Integer> occupyBuildTypes = new HashMap<>();
		
		public String getServerId() {
			return serverId;
		}

		public void setServerId(String serverId) {
			this.serverId = serverId;
		}

		public String getGuildId() {
			return guildId;
		}

		public void setGuildId(String guildId) {
			this.guildId = guildId;
		}

		public String getGuildName() {
			return guildName;
		}

		public void setGuildName(String guildName) {
			this.guildName = guildName;
		}

		public String getGuildTag() {
			return guildTag;
		}

		public void setGuildTag(String guildTag) {
			this.guildTag = guildTag;
		}

		public int getGuildFlag() {
			return guildFlag;
		}

		public void setGuildFlag(int guildFlag) {
			this.guildFlag = guildFlag;
		}

		public String getLeaderId() {
			return leaderId;
		}

		public void setLeaderId(String leaderId) {
			this.leaderId = leaderId;
		}

		public String getLeaderName() {
			return leaderName;
		}

		public void setLeaderName(String leaderName) {
			this.leaderName = leaderName;
		}

		public long getTeamPower() {
			return teamPower;
		}
		public void setTeamPower(long teamPower) {
			this.teamPower = teamPower;
		}
		
		public long getScore() {
			return score;
		}
		public void setScore(long score) {
			this.score = score;
		}
		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

		public Map<Integer, YQZZGameDataBuildScore> getGuildBuildScore() {
			return guildBuildScore;
		}
		
		public Map<Integer, YQZZGameDataBuildScore> getPlayerBuildScore() {
			return playerBuildScore;
		}
		
		public Set<Integer> getControlBuilds() {
			return controlBuilds;
		}
		
		public Map<Integer, Integer> getControlBuildTypes() {
			return controlBuildTypes;
		}
		
		public Map<Integer, Integer> getOccupyBuildTypes() {
			return occupyBuildTypes;
		}

		public int getPylonCnt() {
			return pylonCnt;
		}

		public void setPylonCnt(int pylonCnt) {
			this.pylonCnt = pylonCnt;
		}

		public int getFoggyCnt() {
			return foggyCnt;
		}

		public void setFoggyCnt(int foggyCnt) {
			this.foggyCnt = foggyCnt;
		}

		public int getMonsterCnt() {
			return monsterCnt;
		}

		public void setMonsterCnt(int monsterCnt) {
			this.monsterCnt = monsterCnt;
		}
		
	}
	public static class YQZZCountryGameData{
		public String serverId;
		public long score;
		public int rank;
		private int pylonCnt;
		private Map<Integer,Integer> buildControl = new HashMap<>();
		private Map<Integer,Integer> buildOccupy = new HashMap<>();

		private Map<Integer,YQZZGameDataYuriKill> yuriScore = new HashMap<>();
		private Map<Integer,YQZZGameDataBuildScore> buildScore = new HashMap<>();

		public void addControlByType(int type,int add){
			int count = buildControl.getOrDefault(type, 0);
			buildControl.put(type, count + add);
		}
		
		public void addOccupyByType(int type,int add){
			int count = buildOccupy.getOrDefault(type, 0);
			buildOccupy.put(type, count + add);
		}
		
		public void setServerId(String serverId) {
			this.serverId = serverId;
		}
		
		public String getServerId() {
			return serverId;
		}
		
		public void setScore(long score) {
			this.score = score;
		}
		
		public long getScore() {
			return score;
		}
		public int getRank() {
			return rank;
		}
		
		public void setRank(int rank) {
			this.rank = rank;
		}

		public int getPylonCnt() {
			return pylonCnt;
		}

		public void setPylonCnt(int pylonCnt) {
			this.pylonCnt = pylonCnt;
		}

		public Map<Integer, Integer> getBuildControl() {
			return buildControl;
		}
		
		public void setBuildControl(Map<Integer, Integer> buildControl) {
			this.buildControl = buildControl;
		}
		
		public Map<Integer, Integer> getBuildOccupy() {
			return buildOccupy;
		}
		
		public void setBuildOccupy(Map<Integer, Integer> buildOccupy) {
			this.buildOccupy = buildOccupy;
		}
		
		public int getServerIdNum(){
			return Integer.parseInt(this.serverId);
		}

		public Map<Integer, YQZZGameDataYuriKill> getYuriScore() {
			return yuriScore;
		}

		public Map<Integer, YQZZGameDataBuildScore> getBuildScore() {
			return buildScore;
		}
	}
	
	
	public static class YQZZGameDataBuildScore{
		private int buildType;
		private int count;
		private long score;
		public YQZZGameDataBuildScore(int buildType, int count, long score) {
			super();
			this.buildType = buildType;
			this.count = count;
			this.score = score;
		}
		public int getBuildType() {
			return buildType;
		}
		public void setBuildType(int buildType) {
			this.buildType = buildType;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public long getScore() {
			return score;
		}
		public void setScore(long score) {
			this.score = score;
		}
	}
	
	public static class YQZZGameDataMonsterKill{
		private int monsterLevel;
		private int count;
		private long score;
		
		public YQZZGameDataMonsterKill(int monsterLevel, int count, long score) {
			super();
			this.monsterLevel = monsterLevel;
			this.count = count;
			this.score = score;
		}
		public int getMonsterLevel() {
			return monsterLevel;
		}
		public void SetMonsterLevel(int monsterLevel) {
			this.monsterLevel = monsterLevel;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public long getScore() {
			return score;
		}
		public void setScore(long score) {
			this.score = score;
		}
	}
	
	public static class YQZZGameDataYuriKill{
		private int yuriLevel;
		private int count;
		private long score;
		
		public YQZZGameDataYuriKill(int yuriLevel, int count, long score) {
			super();
			this.yuriLevel = yuriLevel;
			this.count = count;
			this.score = score;
		}
		public int getYuriLevel() {
			return yuriLevel;
		}
		public void SetYuriLevel(int yuriLevel) {
			this.yuriLevel = yuriLevel;
		}
		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}
		public long getScore() {
			return score;
		}
		public void setScore(long score) {
			this.score = score;
		}
	}
	
	public static class YQZZGameDataResCollect{
		private int resType;
		private long count;
		private long score;
		public YQZZGameDataResCollect(int resType, long count, long score) {
			super();
			this.resType = resType;
			this.count = count;
			this.score = score;
		}
		public int getResType() {
			return resType;
		}
		public void setResType(int resType) {
			this.resType = resType;
		}
		public long getCount() {
			return count;
		}
		public void setCount(long count) {
			this.count = count;
		}
		public long getScore() {
			return score;
		}
		public void setScore(long score) {
			this.score = score;
		}
	}
	
	
}
