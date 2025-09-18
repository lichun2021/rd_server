package com.hawk.game.module.lianmengfgyl.march.data;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.GsConfig;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLConstCfg;
import com.hawk.game.module.lianmengfgyl.march.entity.FGYLGuildEntity;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLMatchService;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarGuildRankMember;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Tuple;

public class FGYLHonorRank {
	
	private List<PBFGYLWarGuildRankMember> rankInfos =  new ArrayList<>();
	
	private static final int posParam = 10000;
	private static final long timeParm = 9999999999l;
	
	
	public List<PBFGYLWarGuildRankMember> getRankInfos() {
		return rankInfos;
	}

	
	/**
	 * 刷新排行列表
	 */
	public void refreshRank() {
		try {
			FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
			int rankSize = constCfg.getHonorRankSize();
			if (rankSize > 0) {
				rankSize -= 1;
			}
			Set<Tuple> tuples = this.getFGYLHonorRanks(0, rankSize);
			List<PBFGYLWarGuildRankMember> list = new ArrayList<>();
			int rank = 1;
			for (Tuple tuple : tuples) {
				try {
					String param = tuple.getElement();
					double score = tuple.getScore();
					String[] parr = param.split("_");
					String serverId = parr[0];
					String guildId =  parr[1];
					this.checkRankMember(serverId, guildId, score);
					this.checkRankMemberGuild(serverId, guildId);
					PBFGYLWarGuildRankMember.Builder memberBuilder = this.getFGYLHonorRankGuild(serverId, guildId);
					if(Objects.isNull(memberBuilder)){
						continue;
					}
					HawkTuple2<Integer, Integer> scoretuple = this.calParamFromRankScore((long)score);
					memberBuilder.setGuildRank(rank);
					memberBuilder.setPassLevel(scoretuple.first);
					memberBuilder.setTimeUse(scoretuple.second);
					list.add(memberBuilder.build());
					rank++;
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			this.rankInfos = list;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		//只留下前500名 就行
		this.removeFGYLHonorTailRanks(500);
	}
	
	
	public void checkRankMember(String serverId,String guildId,double score){
		//不是本服不管
		if(!GlobalData.getInstance().isLocalServer(serverId)){
			return;
		}
		//本服是否存在这个联盟，不存在则删除
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if(Objects.isNull(guild)  || guild.isCrossGuild()){
			this.removeFGYLHonorRanks(serverId, guildId);
			return;
		}
		//此联盟存在，但是serverId  还是 和服或者拆服之前的serverId,移出新添加一下
		String localServerId = GsConfig.getInstance().getServerId();
		if(!localServerId.equals(serverId)){
			this.removeFGYLHonorRanks(serverId, guildId);
			this.addFGYLHonorRanks(localServerId, guildId, score);
		}
	}
	
	
	public void checkRankMemberGuild(String serverId,String guildId){
		//不是本服不管 并且是主服
		String curServerId = GsConfig.getInstance().getServerId();
		if(!curServerId.equals(serverId)){
			return;
		}
		//本服是否存在这个联盟，不存在则删除
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if(Objects.isNull(guild)  || guild.isCrossGuild()){
			return;
		}
		//此联盟存在，更新一下
		this.addFGYLHonorRankGuild(curServerId, guildId);
	}
	
	
	
	public void checkReload(List<String> reloadList){
		if(Objects.isNull(reloadList)){
			return;
		}
		if(reloadList.size() <= 0){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		for(String guildId : reloadList){
			FGYLGuildEntity entity = FGYLMatchService.getInstance().getDataManger().getFGYLGuildEntity(guildId);
			if(Objects.isNull(entity)){
				continue;
			}
			this.addFGYLHonorRanks(serverId, guildId, entity.getPassLevel(),entity.getUseTime(),entity.getPassTime());
		}
	}
	
	
	public void addFGYLHonorRanks(String serverId,String guildId,int level,int timeUse,long passTime){
		double score = this.calRankScore(level, timeUse, passTime);
		if(score > 0){
			this.addFGYLHonorRanks(serverId, guildId, score);
		}
	}
	
	
	/**
	 * 清除排行信息
	 */
	public void clearRank(int termId){
		this.rankInfos = new ArrayList<>();
	}

	
	public double calRankScore(int level,int timeUse,long rankTime){
		if(timeUse >= posParam){
			return 0;
		}
		if(timeUse <= 0){
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

	
	
	public Set<Tuple> getFGYLHonorRanks(long start, long end) {
		String key = RedisProxy.FGYL_HONOR_RANK;
		StatisManager.getInstance().incRedisKey(key);
		return RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(key, start, end, 0);
	}
	
	public void addFGYLHonorRanks(String serverId,String guildId,double score){
		String key = RedisProxy.FGYL_HONOR_RANK;
		String field = serverId+"_"+guildId;
		StatisManager.getInstance().incRedisKey(key);
		RedisProxy.getInstance().getRedisSession().zAdd(key, score, field, 0);
	}
	

	public void removeFGYLHonorRanks(String serverId,String guildId){
		String key = RedisProxy.FGYL_HONOR_RANK;
		String field = serverId+"_"+guildId;
		StatisManager.getInstance().incRedisKey(key);
		RedisProxy.getInstance().getRedisSession().zRem(key, 0, field);
	}
	
	
	public void removeFGYLHonorTailRanks(int size){
		String key = RedisProxy.FGYL_HONOR_RANK;
		StatisManager.getInstance().incRedisKey(key);
		RedisProxy.getInstance().getRedisSession().zRemrangeByRank(key, 0, -size);
	}
	
	
	
	
	public PBFGYLWarGuildRankMember.Builder getFGYLHonorRankGuild(String serverId,String guildId){
		String key = RedisProxy.FGYL_HONOR_RANK_GUILD+":"+ serverId + ":"+ guildId;
		StatisManager.getInstance().incRedisKey(key);
		String str = RedisProxy.getInstance().getRedisSession().getString(key, (int)TimeUnit.HOURS.toSeconds(1));
		if(HawkOSOperator.isEmptyString(str)){
			return null;
		}
		PBFGYLWarGuildRankMember.Builder mbuilder = PBFGYLWarGuildRankMember.newBuilder();
		try {
			JsonFormat.merge(new String(str.getBytes(), "UTF-8"), mbuilder);
			return  mbuilder;
		} catch (ParseException | UnsupportedEncodingException e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	
	public void addFGYLHonorRankGuild(String serverId,String guildId){
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if(Objects.isNull(guild)){
			return;
		}
		if(!serverId.equals(guild.getServerId())){
			return;
		}
		PBFGYLWarGuildRankMember.Builder mbuilder = PBFGYLWarGuildRankMember.newBuilder();
		mbuilder.setTermId(0);
		mbuilder.setServerId(serverId);
		mbuilder.setId(guildId);
		mbuilder.setName(guild.getName());
		mbuilder.setTag(guild.getTag());
		mbuilder.setGuildFlag(guild.getFlagId());
		mbuilder.setGuildRank(0);
		mbuilder.setPassLevel(0);
		mbuilder.setTimeUse(0);
		String str = JsonFormat.printToString(mbuilder.build());
		String key = RedisProxy.FGYL_HONOR_RANK_GUILD+":"+ serverId + ":"+ guildId;
		StatisManager.getInstance().incRedisKey(key);
		RedisProxy.getInstance().getRedisSession().setString(key, str, (int)TimeUnit.HOURS.toSeconds(1));
	}

	
}
