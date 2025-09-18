package com.hawk.game.module.dayazhizhan.playerteam.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;

import com.alibaba.fastjson.JSON;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.protocol.DYZZ.PBDYZZGameInfoSync;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

public class DYZZRedisData {

	
	
	private static DYZZRedisData instance = null;
	public static DYZZRedisData getInstance() {
		if (instance == null) {
			instance = new DYZZRedisData();
		}
		return instance;
	}
	private DYZZRedisData() {
		
	}
	
	
	
	/**
	 * 活动阶段信息KEY
	 * @return
	 */
	private String getDYZZInfoKey() {
		String key =  LocalRedis.getInstance().getLocalIdentify() + ":DYZZ_INFO";
		StatisManager.getInstance().incRedisKey(key);
		return key;
	}
	
	
	/**
	 * 战局信息KEY
	 * @return
	 */
	private String getDYZZGameDataKey(int termdId,String gameId) {
		String key = "DYZZ_GAME";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":"+termdId+":"+gameId;
	}
	
	
	private String getDYZZPlayerDataKey(int termId,String playerId){
		String key = "DYZZ_PLAYER_DATA";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":"+playerId+":"+termId;
	}
	
	
	private String getDYZZMatchLockKey(int termId){
		String key = "DYZZ_MATCH_LOCK";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + termId ;
	}
	
	
	private String getDYZZShopBuyInfoKey(int shopTerm,String playerId){
		String key = "DYZZ_SHOP_BUY";
		StatisManager.getInstance().incRedisKey(key);
		return key+":" + shopTerm+":"+playerId;
	}
	
	private String getDYZZGuildDataKey(int termId){
		String key = "DYZZ_GUILD_INFO";
		StatisManager.getInstance().incRedisKey(key);
		return key+":" + termId;
	}
	
	
	private String getDYZZBilingInformation(int termId,String gameId){
		String key = "DYZZ_BILING_INFO";
		StatisManager.getInstance().incRedisKey(key);
		return key+":" + termId + ":" + gameId;
	}
	
	
	private String getDYZZAwardCountKey(String playerId,int day){
		String key = "DYZZ_AWARD_COUNT";
		StatisManager.getInstance().incRedisKey(key);
		return key+":" + playerId + ":" + day;
	}
	
	
	private String getDYZZPlayerScoreKey(String playerId){
		String key = "DYZZ_PLAYER_SCORE";
		StatisManager.getInstance().incRedisKey(key);
		return key+":" + playerId;
	}
	
	
	
	private String getDYZZPlayerBattleHistoryKey(String playerId){
		String key = "DYZZ_PLAYER_BATTLE_HISTORY";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + playerId;
	}
	
	
	private String getDYZZBattleAwardKey(String serverId){
		String key = "DYZZ_BATTLE_AWARD";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + serverId;
	}
	
	private String getDYZZHistoryDayShareKey(String playerId){
		String key = "DYZZ_HISTORY_SHARE_DAY";
		StatisManager.getInstance().incRedisKey(key);
		int day = HawkTime.getYearDay();
		return key + ":" + playerId+":"+day;
	}
	
	
	private String getDYZZResultDealKey(String serverId,String gameId){
		String key = "DYZZ_RESULT_DEAL";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + serverId+":"+gameId;
	}
	
	
	/**
	 * 当天胜利次数key
	 * @param playerId
	 * @return
	 */
	private String getDYZZWinCountToday(String playerId){
		String key = "DYZZ_WIN_COUNT_DAY";
		StatisManager.getInstance().incRedisKey(key);
		int day = HawkTime.getYearDay();
		return key + ":" + day+":"+playerId;
	}
	
	
	public int getDay(long time){
		Calendar calendar = HawkTime.getCalendar(true);
		calendar.setTimeInMillis(time);
		int yearDay = calendar.get(Calendar.DAY_OF_YEAR);
		return yearDay;
	}
	
	
	public String getDYZZMatchServer(int termId){
		final String lock= this.getDYZZMatchLockKey(termId);
		String rlt = RedisProxy.getInstance().getRedisSession().hGet(lock,lock);
		return rlt;
	}
	
	
	public void achiveMatchServer(int termId,String serverId){
		final String lock= this.getDYZZMatchLockKey(termId);
		RedisProxy.getInstance().getRedisSession().hSetNx(lock,lock, serverId);
		this.extendMatchServerTime(termId, (int)TimeUnit.MINUTES.toSeconds(5));
	}
		
	public void extendMatchServerTime(int termId,int time){
		final String lock= this.getDYZZMatchLockKey(termId);
		RedisProxy.getInstance().getRedisSession().expire(lock, time);
	}
	
	/**
	 * 获取达雅之战阶段信息
	 * @return
	 */
	public DYZZActivityInfo loadDYZZServiceInfo() {
		HawkRedisSession redisSession = LocalRedis.getInstance().getRedisSession();
		String str = redisSession.getString(getDYZZInfoKey());
		DYZZActivityInfo result = new DYZZActivityInfo();
		if (Objects.nonNull(str)) {
			try {
				result.mergeFrom(str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	/**
	 * 更新达雅之战阶段信息
	 * @param info
	 */
	public void updateXZQServiceInfo(DYZZActivityInfo info) {
		LocalRedis.getInstance().getRedisSession().setString(getDYZZInfoKey(), info.serializ());
	}
	
	
	/**
	 * 添加匹配信息
	 * @param teamRoom
	 */
	public void saveDYZZGameData(int termId,DYZZGameRoomData data){
		String dataString = data.serializ();
		RedisProxy.getInstance().getRedisSession().setString(getDYZZGameDataKey(termId,data.getGameId()),
				dataString);
	}
	
	
	/**
	 * 添加匹配信息
	 * @param teamRoom
	 */
	public void saveDYZZGameData(int termId,List<DYZZGameRoomData> dataList){
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
			for(DYZZGameRoomData data : dataList){
				String key = this.getDYZZGameDataKey(termId,data.getGameId());
				pip.set(key, data.serializ());
				pip.expire(key, (int)TimeUnit.DAYS.toSeconds(30));
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 添加匹配信息
	 * @param teamRoom
	 */
	public DYZZGameRoomData getDYZZGameData(int termId,String gameId){
		String dataString = RedisProxy.getInstance().getRedisSession().getString(getDYZZGameDataKey(termId,gameId));
		if(HawkOSOperator.isEmptyString(dataString)){
			return null;
		}
		DYZZGameRoomData data = new DYZZGameRoomData();
		data.mergeFrom(dataString);
		return data;
	}
	
	
	public void updateDYZZPlayerData(DYZZPlayerData data){
		String key = this.getDYZZPlayerDataKey(data.getTermId(), data.getPlayerId());
		RedisProxy.getInstance().getRedisSession().setString(key, data.serializ(),(int)TimeUnit.DAYS.toSeconds(30));
	}
	
	
	public void updateDYZZPlayerData(List<DYZZPlayerData> list){
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
			for(DYZZPlayerData data : list){
				String key = this.getDYZZPlayerDataKey(data.getTermId(), data.getPlayerId());
				pip.set(key, data.serializ());
				pip.expire(key, (int)TimeUnit.DAYS.toSeconds(30));
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	public DYZZPlayerData getDYZZPlayerData(int termId,String playerId){
		String key = this.getDYZZPlayerDataKey(termId, playerId);
		String str = RedisProxy.getInstance().getRedisSession().getString(key, (int)TimeUnit.DAYS.toSeconds(30));
		if(HawkOSOperator.isEmptyString(str)){
			return null;
		}
		DYZZPlayerData data = new DYZZPlayerData();
		data.mergeFrom(str);
		return data;
	}
	
	
	/**
	 * 获取商店商品购买次数
	 * 
	 * @param playerId
	 * @param seasonStr
	 * @return
	 */
	public Map<Integer, Integer> getDYZZShopItemBuyCount(String playerId, int shopTerm) {
		String key = this.getDYZZShopBuyInfoKey(shopTerm, playerId);
		Map<String, String> values = RedisProxy.getInstance().getRedisSession().hGetAll(key, (int)TimeUnit.DAYS.toSeconds(90));
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
	public int getDYZZShopItemBuyCount(String playerId, int shopId, int shopTerm) {
		String key = this.getDYZZShopBuyInfoKey(shopTerm,playerId);
		String result = RedisProxy.getInstance().getRedisSession().hGet(key, shopId + "",(int)TimeUnit.DAYS.toSeconds(90));
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
	public void incDYZZShopItemBuyCount(String playerId, int shopId, int count, int shopTerm) {
		String key = this.getDYZZShopBuyInfoKey(shopTerm,playerId);
		RedisProxy.getInstance().getRedisSession().hIncrBy(key, String.valueOf(shopId), count, (int)TimeUnit.DAYS.toSeconds(90));
	}
	
	
	
	
	/**
	 * 批量更新赛博之战出战联盟信息
	 * @param swGuildDatas
	 * @param termId
	 * @return
	 */
	public boolean updateDYZZGuildData(int termId,Collection<DYZZGuildData> dyzzGuildDatas) {
		if (dyzzGuildDatas == null || dyzzGuildDatas.isEmpty()) {
			return true;
		}
		Map<String, String> dataMap = new HashMap<>();
		for (DYZZGuildData data : dyzzGuildDatas) {
			dataMap.put(data.getId(), JSON.toJSONString(data));
		}
		String key = this.getDYZZGuildDataKey(termId);
		RedisProxy.getInstance().getRedisSession().hmSet(key,dataMap,(int)TimeUnit.DAYS.toSeconds(30));
		return true;
	}

	/**
	 * 获取赛博之战出战联盟信息
	 * @param guildId
	 * @param termId
	 * @return
	 */
	public DYZZGuildData getDYZZGuildData(String guildId, int termId) {
		String key = this.getDYZZGuildDataKey(termId);
		String dataStr = RedisProxy.getInstance().getRedisSession().hGet(key, guildId,(int)TimeUnit.DAYS.toSeconds(30));
		DYZZGuildData guildData = null;
		if (HawkOSOperator.isEmptyString(dataStr)) {
			return null;
		} else {
			guildData = JSON.parseObject(dataStr, DYZZGuildData.class);
		}
		return guildData;
	}
	
	/**
	 * 保存达雅对战信息
	 * @param termId
	 * @param gameId
	 * @param info
	 */
	public void saveDYZZBilingInformationData(int termId,String gameId,PBDYZZGameInfoSync info){
		String key  = this.getDYZZBilingInformation(termId, gameId);
		String data = JsonFormat.printToString(info);
		RedisProxy.getInstance().getRedisSession().setString(key, data,(int)TimeUnit.DAYS.toSeconds(30));
	}
	
	
	/**
	 * 获取达雅对战信息
	 * @param termId
	 * @param gameId
	 * @return
	 */
	public PBDYZZGameInfoSync.Builder getDYZZBilingInformationData(int termId,String gameId){
		String key  = this.getDYZZBilingInformation(termId, gameId);
		String data = RedisProxy.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(data)){
			return null;
		}
		try {
			PBDYZZGameInfoSync.Builder builder = PBDYZZGameInfoSync.newBuilder();
			JsonFormat.merge(data, builder);
			return builder;
		} catch (ParseException e) {
			HawkException.catchException(e);
			return null;
		}
	}
	
	
	
	
	
	public void addPlayerBattleHistory(Set<String> playerIds,PBDYZZGameInfoSync info) {
		String data = JsonFormat.printToString(info);
		long time = HawkTime.getMillisecond();
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
			for(String playerId : playerIds){
				String key = getDYZZPlayerBattleHistoryKey(playerId);
				pip.zadd(key, time, data);
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	public List<PBDYZZGameInfoSync> getPlayerBattleHistory(String playerId) {
		DYZZWarCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		int count = cfg.getRecordtimes();
		List<PBDYZZGameInfoSync> list = new ArrayList<>();
		if(count > 0){
			String key = getDYZZPlayerBattleHistoryKey(playerId);
			RedisProxy.getInstance().getRedisSession().zRemrangeByRank(key, 0, -(count+1));
			Set<Tuple> set = RedisProxy.getInstance().getRedisSession().
					zRevrangeWithScores(key,0, count,(int)TimeUnit.DAYS.toSeconds(30));
			
			for(Tuple tuple : set){
				String data = tuple.getElement();
				PBDYZZGameInfoSync.Builder builder = PBDYZZGameInfoSync.newBuilder();
				try {
					JsonFormat.merge(data, builder);
					list.add(builder.build());
				} catch (ParseException e) {
					HawkException.catchException(e);
				}
			}
		}
		return list;
	}
	
	
	
	/**
	 * 累积次数
	 * @param playerId
	 */
	public void awrdCountAddToday(String playerId,long time){
		int day = this.getDay(time);
		String key = this.getDYZZAwardCountKey(playerId,day);
		RedisProxy.getInstance().getRedisSession().increaseBy(key, 1, (int)TimeUnit.DAYS.toSeconds(2));
	}

	
	public void awrdCountAddToday(Set<String> playerIds,long time) {
		int day = this.getDay(time);
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
			for(String playerId : playerIds){
				String key = this.getDYZZAwardCountKey(playerId,day);
				pip.incrBy(key, 1);
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取奖励次数
	 * @param playerId
	 * @param effId
	 * @return
	 */
	public int getAwrdCountToday(String playerId,long time){
		int day = this.getDay(time);
		String key = this.getDYZZAwardCountKey(playerId,day);
		String str = RedisProxy.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(str)){
			return 0;
		}
		return  NumberUtils.toInt(str);
	}
	
	
	public Map<String,Integer> getAwrdCountToday(Set<String> playerIds,long time){
		int day = this.getDay(time);
		Map<String,Integer> scoreMap = new HashMap<String,Integer>();
		Map<String,Response<String>> piplineRes = new HashMap<String,Response<String>>();
		try(Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()){
			for(String playerId : playerIds ){
				String key = this.getDYZZAwardCountKey(playerId,day);
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
 	    				scoreMap.put(key, Integer.parseInt(retStr));
 	    			}else{
 	    				scoreMap.put(key, 0);
 	    			}
 	    		}   		
			}
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		return scoreMap;
		
		
	}
	/**
	 * 获取玩家记分记录
	 * @param playerId
	 * @return
	 */
	public DYZZPlayerScore getDYZZPlayerScore(String playerId){
		String key = this.getDYZZPlayerScoreKey(playerId);
		DYZZPlayerScore score = new DYZZPlayerScore();
		String str = RedisProxy.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(str)){
			score.setPlayerId(playerId);
			return score;
		}
		score.mergeFrom(str);
		return score;
	}
	
	/**
	 * 保存玩家记分记录
	 * @param score
	 */
	public void updateDYZZPlayerScore(DYZZPlayerScore score){
		String key = this.getDYZZPlayerScoreKey(score.getPlayerId());
		RedisProxy.getInstance().getRedisSession().setString(key, score.serializ());
	}
	
	
	
	
	
	
	/**
	 * 获取好友推送时间
	 * @param ids
	 * @return
	 */
	public Map<String,DYZZPlayerScore> getDYZZPlayerScore(Set<String> playerIds){
		Map<String,DYZZPlayerScore> scoreMap = new HashMap<String,DYZZPlayerScore>();
		Map<String,Response<String>> piplineRes = new HashMap<String,Response<String>>();
		try(Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()){
			for(String playerId : playerIds ){
				String key = this.getDYZZPlayerScoreKey(playerId);
				Response<String> onePiplineResp = pip.get(key);
				piplineRes.put(playerId,onePiplineResp );
			}
			pip.sync();
			if( piplineRes.size() == playerIds.size() ){
 	    		for(Entry<String,Response<String>> entry : piplineRes.entrySet()){
 	    			String key = entry.getKey();
 	    			Response<String> value = entry.getValue();
 	    			String retStr = value.get();
 	    			DYZZPlayerScore score = new DYZZPlayerScore();
 	    			if (!HawkOSOperator.isEmptyString(retStr)) {
 	    				score.mergeFrom(retStr);
 	    			}else{
 	    				score.setPlayerId(key);
 	    			}
 	    			scoreMap.put(score.getPlayerId(), score);
 	    		}   		
			}
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		return scoreMap;
	}
	
	
	/**
	 * 添加发送好友推送记录记录
	 * @param saveSet
	 */
	public void updateDYZZPlayerScore(Collection<DYZZPlayerScore> scores) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
			for(DYZZPlayerScore score : scores){
				String key = this.getDYZZPlayerScoreKey(score.getPlayerId());
				pip.set(key, score.serializ());
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	public void addDYZZBattleAward(String serverId,Map<String,String> awards){
		String key = this.getDYZZBattleAwardKey(serverId);
		RedisProxy.getInstance().getRedisSession().hmSet(key, awards, (int)TimeUnit.DAYS.toSeconds(30));
	}
	
	
	
	public Map<String,Integer> getDYZZBattleAward(String serverId){
		String key = this.getDYZZBattleAwardKey(serverId);
		Map<String,Integer> awards = new HashMap<>();
		Map<String,String> map = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		for(Map.Entry<String,String> entry : map.entrySet()){
			String entryKey = entry.getKey();
			String entryVal = entry.getValue();
			awards.put(entryKey, Integer.parseInt(entryVal));
		}
		return awards;
	}
	
	
	public void delDYZZBattleAward(String serverId,List<String> awards){
		String key = this.getDYZZBattleAwardKey(serverId);
		RedisProxy.getInstance().getRedisSession().hDel(key, awards.toArray(new String[awards.size()]));
	}
	
	public long getDYZZHistoryDayShare(String playerId){
		String key = this.getDYZZHistoryDayShareKey(playerId);
		String val = RedisProxy.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(val)){
			return 0;
		}
		return Long.parseLong(val);
	}
	
	public long incDYZZHistoryDayShare(String playerId){
		String key = this.getDYZZHistoryDayShareKey(playerId);
		long count = RedisProxy.getInstance().getRedisSession()
				.increaseBy(key, 1, (int)TimeUnit.DAYS.toSeconds(1));
		return count;
	}
	
	
	

	/**
	 * 记录胜利场次
	 * @param playerId
	 * @param count
	 * @return
	 */
	public long incDYZZWincountToday(String playerId,int count){
		String key = this.getDYZZWinCountToday(playerId);
		long aftCount = RedisProxy.getInstance().getRedisSession()
				.increaseBy(key, count,  (int)TimeUnit.DAYS.toSeconds(1));
		return aftCount;
	}

	/**
	 * 获取胜利场次
	 * @param playerId
	 * @param count
	 * @return
	 */
	public int getDYZZWincountToday(String playerId){
		String key = this.getDYZZWinCountToday(playerId);
		String val = RedisProxy.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(val)){
			return 0;
		}
		return Integer.parseInt(val);
	}
	
	/**
	 * 删除当天记录
	 * @param playerId
	 */
	public void removeDYZZWincountToday(String playerId){
		String key = this.getDYZZWinCountToday(playerId);
		RedisProxy.getInstance().getRedisSession().del(key);
	}
	
	/**
	 * 增加胜利次数
	 * @param playerIds
	 * @param time
	 */
	public void incDYZZWincountToday(Set<String> playerIds) {
		try (Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()) {
			for(String playerId : playerIds){
				String key = this.getDYZZWinCountToday(playerId);
				pip.incrBy(key, 1);
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	/**
	 * 获取胜利次数
	 * @param playerIds
	 * @param time
	 * @return
	 */
	public Map<String,Integer> getDYZZWincountToday(Set<String> playerIds){
		Map<String,Integer> scoreMap = new HashMap<String,Integer>();
		Map<String,Response<String>> piplineRes = new HashMap<String,Response<String>>();
		try(Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()){
			for(String playerId : playerIds ){
				String key = this.getDYZZWinCountToday(playerId);
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
 	    				scoreMap.put(key, Integer.parseInt(retStr));
 	    			}else{
 	    				scoreMap.put(key, 0);
 	    			}
 	    		}   		
			}
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		return scoreMap;
	}
	
	
	public long getDYZZResultDeal(String serverId,String gameId){
		String key = this.getDYZZResultDealKey(serverId, gameId);
		long aftCount = RedisProxy.getInstance().getRedisSession()
				.increaseBy(key, 1,  (int)TimeUnit.DAYS.toSeconds(3));
		return aftCount;
	}
}
