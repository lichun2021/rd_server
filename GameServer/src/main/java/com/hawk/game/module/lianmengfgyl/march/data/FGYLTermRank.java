package com.hawk.game.module.lianmengfgyl.march.data;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.os.HawkException;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.GsConfig;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLRankRewardCfg;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarGuildRankMember;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Tuple;

public class FGYLTermRank {
	
	private List<PBFGYLWarGuildRankMember> rankInfos =  new ArrayList<>();
	private Map<String,PBFGYLWarGuildRankMember> rankMap = new HashMap<>();
	
	private static final int posParam = 100000;
	private static final long timeParm = 9999999999l;
	
	
	public List<PBFGYLWarGuildRankMember> getRankInfos() {
		return rankInfos;
	}

	
	public Map<String,PBFGYLWarGuildRankMember> getRankMapInfos() {
		return rankMap;
	}

	

	public void addRank(int termId,String guildId,int level,int timeUse,long rankTime){
		String serverId = GsConfig.getInstance().getServerId();
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if(Objects.isNull(guild)){
			return;
		}
		if(!serverId.equals(guild.getServerId())){
			return;
		}
		double score = this.calRankScore(level, timeUse, rankTime);
		if(score <=0){
			return;
		}
		PBFGYLWarGuildRankMember.Builder mbuilder = PBFGYLWarGuildRankMember.newBuilder();
		mbuilder.setTermId(termId);
		mbuilder.setServerId(serverId);
		mbuilder.setId(guildId);
		mbuilder.setName(guild.getName());
		mbuilder.setTag(guild.getTag());
		mbuilder.setGuildFlag(guild.getFlagId());
		mbuilder.setGuildRank(0);
		mbuilder.setPassLevel(level);
		mbuilder.setTimeUse(timeUse);
		String field = JsonFormat.printToString(mbuilder.build());
		this.addFGYLTermRanks(termId, field, score);
	}

	
	/**
	 * 刷新排行列表
	 */
	public void refreshRank(int termId) {
		int rankSize = FGYLRankRewardCfg.getRankCount();
		if (rankSize > 0) {
			rankSize -= 1;
		}
		try {
			Set<Tuple> tuples = this.getFGYLTermRanks(termId, 0, rankSize);
			List<PBFGYLWarGuildRankMember> list = new ArrayList<>();
			Map<String,PBFGYLWarGuildRankMember> map = new HashMap<>();
			int rank = 1;
			for (Tuple tuple : tuples) {
				String param = tuple.getElement();
				PBFGYLWarGuildRankMember.Builder mbuilder = PBFGYLWarGuildRankMember.newBuilder();
				try {
					JsonFormat.merge(new String(param.getBytes(), "UTF-8"), mbuilder);
				    mbuilder.setGuildRank(rank);
				    PBFGYLWarGuildRankMember member = mbuilder.build();
					list.add(member);
					map.put(member.getId(), member);
				} catch (ParseException | UnsupportedEncodingException e) {
					HawkException.catchException(e);
				}
				rank++;
			}
			this.rankInfos = list;
			this.rankMap = map;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		//只留下前1000名 就行
		this.removeFGYLTermTailRanks(termId, rankSize * 2);
	}
	
	
	public double calRankScore(int level,int timeUse,long rankTime){
		if(timeUse >= posParam){
			return 0;
		}
		if(timeUse <= 0){
			return 0;
		}
		if(level <= 0){
			return 0;
		}
		rankTime = rankTime / 1000;
		int levelParam = level * posParam + (posParam - timeUse);
		long timeParam = timeParm - rankTime;
		String scoreParm =levelParam+ "." + timeParam;
		return Double.valueOf(scoreParm);
	}
	
	
	public HawkTuple2<Integer, Integer> calParamFromRankScore(long score){
		int level = (int) (score / posParam);
		int timeUse = (int) (score % posParam);
		timeUse =(posParam - timeUse);
		HawkTuple2<Integer, Integer> tuple = HawkTuples.tuple(level, timeUse);
		return tuple;
	}
	
	
	
	/**
	 * 清除排行信息
	 */
	public void clearRank(int termId){
		this.rankInfos = new ArrayList<>();
		this.rankMap = new HashMap<>();
	}
	
	

	public Set<Tuple> getFGYLTermRanks(int termId, long start, long end) {
		String key = RedisProxy.FGYL_TERM_RANK + ":" + termId;
		StatisManager.getInstance().incRedisKey(key);
		return RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(key, start, end, (int)TimeUnit.DAYS.toSeconds(60));
	}
	
	
	public void addFGYLTermRanks(int termId,String field, double score){
		String key = RedisProxy.FGYL_TERM_RANK + ":" + termId;
		StatisManager.getInstance().incRedisKey(key);
		RedisProxy.getInstance().getRedisSession().zAdd(key, score, field, (int)TimeUnit.DAYS.toSeconds(60));
	}
	
	
	
	public void removeFGYLTermTailRanks(int termId,int size){
		String key = RedisProxy.FGYL_TERM_RANK + ":" + termId;
		StatisManager.getInstance().incRedisKey(key);
		RedisProxy.getInstance().getRedisSession().zRemrangeByRank(key, 0, -size);
	}
	
	

	

	
}
