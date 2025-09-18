package com.hawk.activity.type.impl.mergecompetition.rank.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionConst;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionConstCfg;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionRankCfg;
import com.hawk.activity.type.impl.mergecompetition.rank.MergeCompetitionRank;
import com.hawk.activity.type.impl.mergecompetition.rank.MergeCompetitionRankProvider;
import com.hawk.activity.type.impl.mergecompetition.rank.RankGuildInfo;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.game.protocol.Activity.MCRankPB;
import com.hawk.game.protocol.Activity.MergeCompeteRank;
import com.hawk.gamelib.rank.RankScoreHelper;

/**
 * 联盟去兵战力排行榜
 */
public class GuildPowerRankProvider extends MergeCompetitionRankProvider {
	/**
	 * 战力发生变化的联盟
	 */
	volatile Set<String> guildIdSet = new ConcurrentHashSet<>();
	
	@Override
	public ActivityRankType getRankType() {
		return ActivityRankType.MERGE_COMPETITION_GUILD_POWER_RANK;
	}
	
	@Override
	public int getActivityRankType() {
		return MergeCompetitionConst.RANK_TYPE_GUILD_POWER;
	}

	@Override
	public boolean insertRank(MergeCompetitionRank rankInfo) {
		String guildId = rankInfo.getId();
		if (rankInfo.getScore() <= 0) {
			String elementId = getRankElementId(rankInfo.getId());
			this.remMember(elementId); //从榜单移除
		} else {
			guildIdSet.add(guildId);
		}
		return true;
	}
	
	@Override
	public void refreshCacheData() {
		if (guildIdSet.isEmpty()) {
			return;
		}
		Set<String> guildSet = guildIdSet;
		guildIdSet = new ConcurrentHashSet<>();
		String serverId = ActivityManager.getInstance().getDataGeter().getServerId();
		Map<String, Double> noArmyMap = new HashMap<>();
		for (String guildId : guildSet) {
			String elementId = getRankElementId(guildId);
			long power = ActivityManager.getInstance().getDataGeter().getGuildNoArmyPower(guildId);
			if (power > 0) {
				long rankScore = RankScoreHelper.calcSpecialRankScore(power);
				noArmyMap.put(elementId, Double.valueOf(rankScore));
			} else {
				this.remMember(elementId); //从榜单移除
			}
		}
		
		if (!noArmyMap.isEmpty()) {
			getRedis().zAdd(getRedisKey(), noArmyMap, getRedisExpire());
			getRedis().zAdd(getLocalRedisKey(serverId), noArmyMap, getRedisExpire());
		}
	}
	
	@Override
	public int getRankSize() {
		return MergeCompetitionConstCfg.getInstance().getRankType2ShowMax();
	}
	
	@Override
	public int getLocalRankSize() {
		return MergeCompetitionConstCfg.getInstance().getRankType2LocalNum();
	}

	@Override
	public String getRedisKey() {
		return MergeCompetitionConst.RANK_POWER_GUILD + getActivity().getServerGroup();
	}

	@Override
	public String getLocalRedisKey(String serverId) {
		return MergeCompetitionConst.RANK_POWER_GUILD_LOCAL + serverId;
	}
	
	/**
	 * 构建榜单数据
	 * @param memberId
	 * @param builder
	 */
	public void buildActivityRank(String playerId, MergeCompeteRank.Builder builder, long selfScore) {
		List<MergeCompetitionRank> rankList = this.getRankList();
		for (MergeCompetitionRank rank : rankList) {
			MCRankPB.Builder rankBuilder = buildRankPB(rank.getId(), rank, rank.getScore());
			builder.addRankData(rankBuilder);
		}
		
		String guildId = ActivityManager.getInstance().getDataGeter().getGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			MergeCompetitionRank rankData = this.getRank(guildId);
			MCRankPB.Builder rankBuilder = buildRankPB(guildId, rankData, selfScore);
			builder.setMyRank(rankBuilder);
		}
	}

	/**
	 * 组装排名协议数据
	 * @param playerRank
	 * @return
	 */
	public MCRankPB.Builder buildRankPB(String guildId, MergeCompetitionRank rank, long score) {
		ActivityDataProxy dataGeter = ActivityManager.getInstance().getDataGeter();
		String serverId = dataGeter.getServerId();
		boolean ownServer = dataGeter.isGuildLocalExist(guildId);
		
		List<MergeCompetitionRankCfg> configList = MergeCompetitionRankCfg.getConfigByType(this.getActivityRankType());
		Optional<MergeCompetitionRankCfg> optional = configList.stream().filter(e -> e.getRankUpper() <= rank.getRank() && e.getRankLower() >= rank.getRank()).findFirst();
		
		MCRankPB.Builder rankPBBuilder = MCRankPB.newBuilder();
		rankPBBuilder.setGuildId(rank.getId());
		rankPBBuilder.setRank(rank.getRank());
		rankPBBuilder.setScore(score);
		rankPBBuilder.setServerScoreRank(optional.isPresent() ? optional.get().getServerPoint() : 0);
		rankPBBuilder.setAddServerScore(getContributeList().contains(rank.getId()) ? 1 : 0);
		if (ownServer) {
			String leaderId = dataGeter.getGuildLeaderId(guildId);
			rankPBBuilder.setGuildName(dataGeter.getGuildName(guildId));
			rankPBBuilder.setGuildTag(dataGeter.getGuildTag(guildId));
			rankPBBuilder.setPlayerId(leaderId);
			rankPBBuilder.setPlayerName(dataGeter.getGuildLeaderName(guildId));
			rankPBBuilder.setServerId(serverId);
			
			rankPBBuilder.setIcon(dataGeter.getIcon(leaderId));
			String pfIcon = dataGeter.getPfIcon(leaderId);
			if (!HawkOSOperator.isEmptyString(pfIcon)) {
				rankPBBuilder.setPfIcon(pfIcon);
			}
		} else {
			RankGuildInfo guildInfo = this.getGuildInfo(guildId, false);
			rankPBBuilder.setGuildName(guildInfo.getGuildName());
			rankPBBuilder.setGuildTag(guildInfo.getGuildTag());
			rankPBBuilder.setPlayerId(guildInfo.getLeaderId());
			rankPBBuilder.setPlayerName(guildInfo.getLeaderName());
			rankPBBuilder.setServerId(guildInfo.getServerId());
			rankPBBuilder.setIcon(guildInfo.getIcon());
			if (!HawkOSOperator.isEmptyString(guildInfo.getPfIcon())) {
				rankPBBuilder.setPfIcon(guildInfo.getPfIcon());
			}
		}
		
		return rankPBBuilder;
	}
	
	@Override
	public String getRankTypeStr() {
		return "guildNoArmyPower";
	}
}
