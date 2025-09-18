package com.hawk.game.module.dayazhizhan.playerteam.season;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;

import redis.clients.jedis.Tuple;

public class DYZZSeasonRedisData {

	
	
	private static DYZZSeasonRedisData instance = null;
	public static DYZZSeasonRedisData getInstance() {
		if (instance == null) {
			instance = new DYZZSeasonRedisData();
		}
		return instance;
	}
	private DYZZSeasonRedisData() {
		
	}
	
	
	/**
	 * 赛季状态Key
	 * @param serverId
	 * @return
	 */
	private String getDYZZSeasonInfoKey(String serverId){
		String key = "DYZZ_SEASON_INFO";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + serverId;
	}
	
	/**
	 * 赛季积分榜Key
	 * @param termId
	 * @return
	 */
	private String getDYZZSeasonScoreRankKey(int termId){
		String key = "DYZZ_SEASON_SCORE_RANK";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + termId;
	}
	
	/**
	 * 赛季玩家数据KEY
	 * @param termId
	 * @param playerId
	 * @return
	 */
	private String getDYZZSeasonPlayerDataKey(int termId,String playerId){
		String key = "DYZZ_SEASON_PLAYER_DATA";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + termId+":"+playerId;
	}
	
	/**
	 * 赛季榜显示key
	 * @param termId
	 * @return
	 */
	private String getDYZZSeasonScoreRankShowKey(int termId){
		String key = "DYZZ_SEASON_SCORE_RANK_SHOW";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + termId;
	}
	
	/**
	 * 赛季榜刷新锁key
	 * @param termId
	 * @return
	 */
	private String getDYZZSeasonScoreRankRefreshLockKey(int termId){
		String key = "DYZZ_SEASON_SCORE_RANK_RFRESH_LOCK";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + termId;
	}
	
	
	/**
	 * 获取赛季战令key
	 * @param termId
	 * @param playerId
	 * @return
	 */
	private String getDYZZSeasonOrderKey(int termId,String playerId){
		String key = "DYZZ_SEASON_PLAYER_ORDER";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + termId+":"+playerId;
	}

	private String getDYZZSeasonBattleKey(String playerId){
		String key = "DYZZ_SEASON_PLAYER_BATTLE";
		StatisManager.getInstance().incRedisKey(key);
		return key + ":" + playerId;
	}
	
	
	
	/**
	 * 获取赛季状态信息
	 * @param serverId
	 * @return
	 */
	public DYZZSeasonInfo getDYZZSeasonInfo(String serverId){
		String key = this.getDYZZSeasonInfoKey(serverId);
		String val = RedisProxy.getInstance().getRedisSession().getString(key);
		DYZZSeasonInfo info = new DYZZSeasonInfo();
		if(!HawkOSOperator.isEmptyString(val)){
			info.mergeFrom(val);
		}
		return info;
	}
	
	/**
	 * 更新呢赛季状态信息
	 * @param serverId
	 * @param info
	 */
	public void updateDYZZSeasonInfo(String serverId,DYZZSeasonInfo info){
		String key = this.getDYZZSeasonInfoKey(serverId);
		RedisProxy.getInstance().getRedisSession().setString(key, info.serializ());
	}
	
	
	/**
	 * 更新积分榜
	 * @param termId
	 * @param score
	 * @param playerId
	 */
	public void updateDYZZSeasonScoreRank(int termId,double score,String playerId){
		String key = this.getDYZZSeasonScoreRankKey(termId);
		RedisProxy.getInstance().getRedisSession().zAdd(key, score, playerId,(int)TimeUnit.DAYS.toSeconds(90));
	}
	
	
	/**
	 * 获取积分榜成员
	 * @param termId
	 * @param size
	 * @return
	 */
	public Set<Tuple> getDYZZSeasonScoreRank(int termId,int size){
		String key = this.getDYZZSeasonScoreRankKey(termId);
		Set<Tuple> rankSet = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(key, 0, size, 0);
		return rankSet;
	}
	
	
	/**
	 * 更新赛季玩家信息
	 * @param termId
	 * @param playerId
	 */
	public void updateDYZZSeasonPlayerData(DYZZSeasonPlayerData data){
		String key = this.getDYZZSeasonPlayerDataKey(data.getTermId(),data.getPlayerId());
		RedisProxy.getInstance().getRedisSession().setString(key, data.serializ());
	}
	
	/**
	 * 移除玩家赛季数据
	 * @param termId
	 * @param playerId
	 */
	public void expireDYZZSeasonPlayerData(int termId,String playerId){
		String key = this.getDYZZSeasonPlayerDataKey(termId,playerId);
		RedisProxy.getInstance().getRedisSession().expire(key,(int)TimeUnit.DAYS.toSeconds(30));
	}
	
	
	/**
	 * 获取玩家赛季数据
	 * @param termId
	 * @param playerId
	 * @return
	 */
	public DYZZSeasonPlayerData getDYZZSeasonPlayerData(int termId,String playerId){
		String key = this.getDYZZSeasonPlayerDataKey(termId,playerId);
		String val = RedisProxy.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(val)){
			return null;
		}
		DYZZSeasonPlayerData data = new DYZZSeasonPlayerData();
		data.mergeFrom(val);
		return data;
	}
	
	
	/**
	 * 更新榜显示数据
	 * @param rankData
	 */
	public void updateDYZZSeasonScoreRankShowData(DYZZSeasonScoreRank rankData){
		String key = this.getDYZZSeasonScoreRankShowKey(rankData.getTermId());
		RedisProxy.getInstance().getRedisSession().setString(key, rankData.serializ());
	}
	
	
	/**
	 * 获取积分榜显示数据
	 * @param termId
	 * @return
	 */
	public DYZZSeasonScoreRank getDYZZSeasonScoreRankShowData(int termId){
		String key = this.getDYZZSeasonScoreRankShowKey(termId);
		String val = RedisProxy.getInstance().getRedisSession().getString(key);
		DYZZSeasonScoreRank rank = new DYZZSeasonScoreRank(termId);
		if(!HawkOSOperator.isEmptyString(val)){
			rank.mergeFrom(val);
		}else{
			rank.resetTerm(termId);
		}
		return rank;
	}
	
	
	
	
	/**
	 * 尝试获取刷新榜的锁
	 * @param termId
	 * @param refreshTime
	 * @param serverId
	 */
	public boolean achiveDYZZSeasonScoreRankRefreshLock(int termId,String serverId,int expire){
		String key= this.getDYZZSeasonScoreRankRefreshLockKey(termId);
		long rlt = RedisProxy.getInstance().getRedisSession().hSetNx(key,key, serverId);
		if(rlt > 0){
			this.extendDYZZSeasonScoreRankRefreshLockTime(termId,expire);
			return true;
		}
		return false;
	}
	
	/**
	 * 获取刷新服
	 * @param termId
	 * @return
	 */
	public String getDYZZSeasonScoreRankRefreshLock(int termId){
		String key= this.getDYZZSeasonScoreRankRefreshLockKey(termId);
		String rlt = RedisProxy.getInstance().getRedisSession().hGet(key,key);
		return rlt;
	}
	/**
	 * 延长锁使用时间
	 * @param termId
	 * @param refreshTime
	 */
	public void extendDYZZSeasonScoreRankRefreshLockTime(int termId,int expire){
		String key = this.getDYZZSeasonScoreRankRefreshLockKey(termId);
		RedisProxy.getInstance().getRedisSession().expire(key, expire);
	}
	
	

	
	/**
	 * 更新赛季战令
	 * @param order
	 */
	public void updateDYZZSeasonOrder(DYZZSeasonOrder order){
		String key = this.getDYZZSeasonOrderKey(order.getTermId(), order.getPlayerId());
		RedisProxy.getInstance().getRedisSession().setString(key,order.serializ(), (int)TimeUnit.DAYS.toSeconds(90));
	}
	
	/**
	 * 获取战令
	 * @param termId
	 * @param playerId
	 * @return
	 */
	public DYZZSeasonOrder getDYZZSeasonOrder(int termId,String playerId){
		String key = this.getDYZZSeasonOrderKey(termId, playerId);
		String val = RedisProxy.getInstance().getRedisSession().getString(key, (int)TimeUnit.DAYS.toSeconds(90));
		if(!HawkOSOperator.isEmptyString(val)){
			DYZZSeasonOrder rank = new DYZZSeasonOrder();
			rank.mergeFrom(val);
			return rank;
		}
		return null;
	}

	/**
	 * 战斗信息
	 * @param battleInfo
	 */
	public void updateDYZZSeasonBattle(DYZZSeasonBattleInfo battleInfo){
		String key = this.getDYZZSeasonBattleKey(battleInfo.getPlayerId());
		RedisProxy.getInstance().getRedisSession().setString(key, battleInfo.serializ());
	}

	/**
	 * 战斗信息
	 * @param playerId
	 * @return
	 */
	public DYZZSeasonBattleInfo getDYZZSeasonBattle(String playerId){
		String key = this.getDYZZSeasonBattleKey(playerId);
		String val = RedisProxy.getInstance().getRedisSession().getString(key);
		if(HawkOSOperator.isEmptyString(val)){
			return null;
		}
		DYZZSeasonBattleInfo battleInfo = new DYZZSeasonBattleInfo();
		battleInfo.mergeFrom(val);
		return battleInfo;
	}

	/**
	 * 战斗信息
	 * @param playerId
	 */
	public void delDYZZSeasonBattle(String playerId){
		String key = this.getDYZZSeasonBattleKey(playerId);
		RedisProxy.getInstance().getRedisSession().del(key);
	}

}
