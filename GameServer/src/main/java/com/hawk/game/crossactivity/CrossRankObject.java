package com.hawk.game.crossactivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.CrossActivity.CGuildInfo;
import com.hawk.game.protocol.CrossActivity.CPlayerInfo;
import com.hawk.game.protocol.CrossActivity.CrossRankInfo;
import com.hawk.game.protocol.CrossActivity.CrossRankType;
import com.hawk.game.protocol.CrossActivity.GetCrossRankResp;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Tuple;

public class CrossRankObject {
	
	public CrossRankType rankType;
	
	private List<CrossRankInfo> rankInfos;
	
	private Set<Tuple> rankTuples;

	public CrossRankObject(CrossRankType rankType) {
		this.rankType = rankType;
		this.rankInfos = new ArrayList<>();;
		this.rankTuples = new HashSet<Tuple>();
	}
	
	public List<CrossRankInfo> getRankInfos() {
		return rankInfos;
	}

	public Set<Tuple> getRankTuples() {
		return rankTuples;
	}
	
	
	/**
	 * 获取榜单数量限制
	 * @return
	 */
	public int getRankLimit() {
		int limit = 1;
		switch (rankType) {
		case C_SELF_RANK:
			limit = CrossConstCfg.getInstance().getCross_personalLimit();
			break;
		case C_GUILD_RANK:
			limit = CrossConstCfg.getInstance().getCross_unionLimit();
			break;
		case C_SERVER_RANK:
			limit = CrossConstCfg.getInstance().getCross_serverLimit();
			break;
		case C_TALENT_RANK:
			limit = CrossConstCfg.getInstance().getTalentRankLimit();
			break;
		case C_TALENT_GUILD_RANK:
			limit = CrossConstCfg.getInstance().getTalentGuildRankLimit();
			break;
		}
		return limit;
	}
	
	
	/**
	 * 刷新排行列表
	 */
	public void refreshRank(int termId) {
		try {
			int rankSize = getRankLimit();
			if (rankSize > 0) {
				rankSize -= 1;
			}
			this.rankTuples = RedisProxy.getInstance().getCrossRanks(rankType, termId, 0, rankSize);
			List<CrossRankInfo> list = new ArrayList<>();
			int rank = 1;
			for (Tuple tuple : this.rankTuples) {
				CrossRankInfo.Builder builder = buildRankInfo(rank, tuple, termId);
				if (builder == null) {
					continue;
				}
				list.add(builder.build());
				rank++;
			}
			this.rankInfos = list;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 清除排行信息
	 */
	public void clearRank(int termId){
		this.rankInfos = new ArrayList<>();
		this.rankTuples = new HashSet<>();
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
		String guildId = "";
		String guildTag = "";
		String name = "";
		String serverId = "";
		Player player = null;
		switch (rankType) {
		case C_SELF_RANK:
		case C_TALENT_RANK:
			CPlayerInfo.Builder playerInfo = RedisProxy.getInstance().getCrossPlayerInfo(id, termId);
			if (playerInfo != null) {
				name = playerInfo.getName();
				if (playerInfo.hasGuildId()) {
					guildId = playerInfo.getGuildId();
				}
				serverId = playerInfo.getServerId();
				player = GlobalData.getInstance().makesurePlayer(playerInfo.getId());
			}else{
				HawkLog.errPrintln("cross build selfRank erroe, playerId : {}", guildId);
				return null;
			}
			if(!HawkOSOperator.isEmptyString(guildId)){
				CGuildInfo.Builder guildInfo = RedisProxy.getInstance().getCrossGuildInfo(guildId, termId);
				if(guildInfo!=null){
					guildTag = guildInfo.getTag();
				}
			}
			break;
		case C_GUILD_RANK:
		case C_TALENT_GUILD_RANK:
			guildId = id;
			CGuildInfo.Builder guildInfo = RedisProxy.getInstance().getCrossGuildInfo(guildId, termId);
			if(guildInfo!=null){
				name = guildInfo.getName();
				guildTag = guildInfo.getTag();
				serverId = guildInfo.getServerId();
			}else{
				HawkLog.errPrintln("cross build guildRank erroe, guildId : {}", guildId);
				return null;
			}
			break;
		case C_SERVER_RANK:
			serverId = id;
			break;
		}
		if(!HawkOSOperator.isEmptyString(name)){
			builder.setName(name);
		}
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			builder.setGuildTag(guildTag);
		}
		if (player != null) {
			builder.setPlayerId(player.getId());
			builder.addAllPersonalProtectSwitch(player.getData().getPersonalProtectListVals());
		}
		builder.setServerId(serverId);
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
		String name = "";
		String rankerId = "";
		String guildTag = "";
		String playerId = "";
		String serverId = player.getMainServerId();
		String guildId = player.getGuildId();
		String guildName = guildId;
		if(!HawkOSOperator.isEmptyString(guildId)){
			guildName = GuildService.getInstance().getGuildName(guildId);
			guildTag = GuildService.getInstance().getGuildTag(guildId);
		}
		
		switch (rankType) {
		case C_SELF_RANK:
		case C_TALENT_RANK:
			name = player.getName();
			rankerId = player.getId();
			playerId = player.getId();
			break;
		case C_GUILD_RANK:
		case C_TALENT_GUILD_RANK:
			rankerId = guildId;
			name = guildName;
			break;
		case C_SERVER_RANK:
			rankerId = serverId;
			break;
		default:
			break;
		}
		int rank = -1;
		long selfScore = 0;
		if (!HawkOSOperator.isEmptyString(rankerId)) {
			rank = getSelfRank(rankerId);
			selfScore = getSelfScore(rankerId, termId);
		}

		CrossRankInfo.Builder builder = CrossRankInfo.newBuilder();
		if (!HawkOSOperator.isEmptyString(name)) {
			builder.setName(name);
		}
		if (!HawkOSOperator.isEmptyString(guildTag)) {
			builder.setGuildTag(guildTag);
		}
		
		if (!HawkOSOperator.isEmptyString(playerId)) {
			builder.setPlayerId(playerId);
			builder.addAllPersonalProtectSwitch(player.getData().getPersonalProtectListVals());
		}
		builder.setServerId(serverId);
		builder.setScore(selfScore);
		builder.setRank(rank);
		return builder;
	}
	
	/**
	 * 获取自己/本联盟排名
	 * @param rankId
	 * @return
	 */
	public int getSelfRank(String rankerId){
		int selfRank = -1;
		int rank = 1;
		for(Tuple tuple : this.rankTuples){
			if(tuple.getElement().equals(rankerId)){
				selfRank = rank;
				break;
			}
			rank ++;
		}
		return selfRank;
	}
	
	/**
	 * 构建排行榜返回信息
	 * @param entity
	 * @return
	 */
	public GetCrossRankResp.Builder buildRankInfoResp(Player player, int termId) {
		GetCrossRankResp.Builder builder = GetCrossRankResp.newBuilder();
		builder.addAllRankInfo(this.rankInfos);
		builder.setSelfRank(buildSelfRankInfo(player, termId));
		builder.setRankType(rankType);
		return builder;
	}
	
	/**
	 * 移除指定成员排行数据
	 * @param memberId
	 * @param termId
	 */
	public void removeRank(String rankerId, int termId) {
		RedisProxy.getInstance().removeFromCrossRank(rankType, termId, rankerId);
	}
	
	/**
	 * 获取指定对象积分
	 * @param rankerId
	 * @param termId
	 * @return
	 */
	public long getSelfScore(String rankerId, int termId) {
		HawkTuple2<Long, Double> index = null;
		if (rankerId != null) {
			index = RedisProxy.getInstance().getCrossRank(rankType, termId, rankerId);
		}
		long selfScore = 0;
		if (index != null) {
			selfScore = (long) Math.floor(index.second);
		}
		return selfScore;
	}
	
}
