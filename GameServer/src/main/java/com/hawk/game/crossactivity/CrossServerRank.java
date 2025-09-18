package com.hawk.game.crossactivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.CrossServerListCfg;
import com.hawk.game.config.CrossTimeCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.CrossActivity.CGuildInfo;
import com.hawk.game.protocol.CrossActivity.CPlayerInfo;
import com.hawk.game.protocol.CrossActivity.CrossActivityPylonRankResp;
import com.hawk.game.protocol.CrossActivity.CrossRankInfo;

import redis.clients.jedis.Tuple;

public class CrossServerRank {
	
	private String rankName;
	
	private Map<String,List<CrossRankInfo>> rankInfos;
	
	private Map<String,Set<Tuple>> rankTuples;

	public CrossServerRank(String rankName) {
		this.rankName = rankName;
		this.rankInfos = new HashMap<>();
		this.rankTuples = new HashMap<>();
	}
	
	public List<CrossRankInfo> getRankInfos(String servereId) {
		return rankInfos.get(servereId);
	}

	public Set<Tuple> getRankTuples(String servereId) {
		return rankTuples.get(servereId);
	}
	
	
	/**
	 * 获取榜单数量限制
	 * @return
	 */
	public int getRankLimit() {
		return CrossConstCfg.getInstance().getFortressRankMax();
	}
	
	
	public void addScore(int termId,Player player,int add){
		int curSeconds = HawkTime.getSeconds();
		CrossTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(CrossTimeCfg.class, termId);
		if(Objects.isNull(timeCfg)){
			return;
		}
		String serverId = player.getMainServerId();
		CrossServerListCfg cfg = AssembleDataManager.getInstance().getCrossServerListCfg(serverId);
		if (cfg == null) {
			return;
		}
		String key = this.getRankRedisKey( cfg.getId(), termId, serverId);
		long cnt = 0;
		Double score = RedisProxy.getInstance().getRedisSession().zScore(key,  player.getId(), 604800);
		if(Objects.nonNull(score)){
			cnt = score.longValue();
		}
		cnt += add;
		long param = (int)(timeCfg.getHiddenTimeValue()/1000) - curSeconds;
		String valStr = String.format("%d.%d", cnt,param);
		double val = Double.parseDouble(valStr);
		RedisProxy.getInstance().getRedisSession().zAdd(key,val, player.getId(),604800);
	}
	
	
	
	public void refreshRank(int termId){
		CrossServerListCfg cfg = AssembleDataManager.getInstance().getCrossServerListCfg(GsConfig.getInstance().getServerId());
		if (cfg == null) {
			return;
		}
		
		for(String serverId : cfg.getServerList()){
			this.refreshRank(termId, cfg.getId(), serverId);
		}
	}
	
	/**
	 * 刷新排行列表
	 */
	public void refreshRank(int termId,int crossId,String serverId) {
		try {
			int rankSize = getRankLimit();
			if (rankSize > 0) {
				rankSize -= 1;
			}
			String key =  this.getRankRedisKey(crossId, termId, serverId);
			Set<Tuple> tuples =  RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(key, 0, rankSize, 604800);
			if(Objects.isNull(tuples) || tuples.isEmpty()){
				return;
			}
			List<CrossRankInfo> list = new ArrayList<>();
			int rank = 1;
			for (Tuple tuple : tuples) {
				CrossRankInfo.Builder builder = buildRankInfo(rank, tuple, termId);
				if (builder == null) {
					continue;
				}
				list.add(builder.build());
				rank++;
			}
			this.rankTuples.put(serverId, tuples);
			this.rankInfos.put(serverId, list);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 清除排行信息
	 */
	public void clearRank(){
		this.rankInfos = new HashMap<>();
		this.rankTuples = new HashMap<>();
	}

	/**
	 * 构建排行单元信息
	 * @param rank
	 * @param tuple
	 * @return
	 */
	private CrossRankInfo.Builder buildRankInfo(int rank, Tuple tuple, int termId) {
		CrossRankInfo.Builder builder = CrossRankInfo.newBuilder();
		String id = tuple.getElement();
		long score = (long) tuple.getScore();

		CPlayerInfo.Builder playerInfo = RedisProxy.getInstance().getCrossPlayerInfo(id, termId);
		if (playerInfo == null) {
			return null;
		}
		builder.setServerId(playerInfo.getServerId());
		builder.setName(playerInfo.getName());
		builder.setIcon(playerInfo.getIcon());
		if(playerInfo.hasPfIcon()){
			builder.setPfIcon(playerInfo.getPfIcon());
		}
		if (playerInfo.hasGuildId()) {
			String guildId = playerInfo.getGuildId();
			CGuildInfo.Builder guildInfo = RedisProxy.getInstance().getCrossGuildInfo(guildId, termId);
			if(guildInfo!=null){
				builder.setGuildTag(guildInfo.getTag());
				builder.setPguild(guildInfo.getName());
			}
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerInfo.getId());
		if (player != null) {
			builder.setPlayerId(player.getId());
			builder.addAllPersonalProtectSwitch(player.getData().getPersonalProtectListVals());
		}
		builder.setScore(score);
		builder.setRank(rank);
		return builder;
	}
	
	/**
	 * 构建自己的排行信息
	 * @param entity
	 * @return
	 */
	private CrossRankInfo.Builder buildSelfRankInfo(Player player, int termId) {
		CrossRankInfo.Builder builder = CrossRankInfo.newBuilder();
		HawkTuple2<Integer, Long>  tuple = this.getSelfRank(termId,player.getMainServerId(),player.getId());
		int rank = tuple.first;
		long score = tuple.second;
		
		builder.setServerId(player.getMainServerId());
		builder.setName(player.getName());
		builder.setIcon(player.getIcon());
		String pfICon = player.getPfIcon();
		if(!HawkOSOperator.isEmptyString(pfICon)){
			builder.setPfIcon(pfICon);
		}
		
		String guildId = player.getGuildId();
		if(!HawkOSOperator.isEmptyString(guildId)){
			CGuildInfo.Builder guildInfo = RedisProxy.getInstance().getCrossGuildInfo(guildId, termId);
			if(guildInfo!=null){
				builder.setGuildTag(guildInfo.getTag());
				builder.setPguild(guildInfo.getName());
			}
		}
		builder.setPlayerId(player.getId());
		builder.addAllPersonalProtectSwitch(player.getData().getPersonalProtectListVals());
		builder.setScore(score);
		builder.setRank(rank);
		return builder;
	}
	
	/**
	 * 获取自己/本联盟排名
	 * @param rankId
	 * @return
	 */
	public HawkTuple2<Integer, Long> getSelfRank(int termId,String serverId,String playerId){
		CrossServerListCfg cfg = AssembleDataManager.getInstance().getCrossServerListCfg(serverId);
		if (cfg == null) {
			return HawkTuples.tuple(0, 0l);
		}
		String key = this.getRankRedisKey(cfg.getId(), termId, serverId);
		
		Long index = RedisProxy.getInstance().getRedisSession().zrevrank(key, playerId, 604800);
		Double score = RedisProxy.getInstance().getRedisSession().zScore(key, playerId, 604800);
		
		if (Objects.isNull(index) || index < 0 || Objects.isNull(score)) {
			return HawkTuples.tuple(0, 0l);
		}
		return HawkTuples.tuple(index.intValue()+1, score.longValue());
	}
	
	/**
	 * 构建排行榜返回信息
	 * @param entity
	 * @return
	 */
	public CrossActivityPylonRankResp.Builder buildRankInfoResp(Player player, int termId, String serverId) {
		CrossActivityPylonRankResp.Builder builder = CrossActivityPylonRankResp.newBuilder();
		List<CrossRankInfo> list = this.rankInfos.get(serverId);
		if(Objects.nonNull(list)){
			builder.addAllRankInfo(list);
		}
		if(serverId.equals(player.getMainServerId())){
			builder.setSelfRank(buildSelfRankInfo(player, termId));
		}
		builder.setServerId(serverId);
		return builder;
	}
	
	
	public String getRankRedisKey(int crossId,int termId,String serverId){
		String key =  this.rankName + ":" + crossId + ":" + termId +":" + serverId;
		return key;
	}
	
}
