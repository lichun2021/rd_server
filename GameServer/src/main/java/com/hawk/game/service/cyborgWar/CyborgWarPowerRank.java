package com.hawk.game.service.cyborgWar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.hawk.game.GsConfig;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.protocol.CyborgWar.CWTeamInfo;
import com.hawk.game.protocol.CyborgWar.CWTeamRank;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Tuple;

public class CyborgWarPowerRank {

	private List<CWTeamRank.Builder> ranks = new ArrayList<>();
	
	private Map<String,Integer> rankNumMap = new HashMap<>();
	
	private int rankSortLimit = 500;
	
	private int rankShowLimit = 100;
	
	
	public void updateRank(){
		Set<Tuple> tuples = CyborgWarRedis.getInstance().getCWTeamPowerRanks(0, rankSortLimit - 1, GsConfig.getInstance().getServerId());
		if(Objects.isNull(tuples) || tuples.isEmpty()){
			return;
		}
		List<String> teamIds = tuples.stream().map(t -> t.getElement()).collect(Collectors.toList());
		Map<String, CWTeamData> teamDatas = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
		
		List<CWTeamRank.Builder> ranksTemp = new ArrayList<>();
		Map<String,Integer> rankNumMapTemp = new HashMap<>();
		int rank = 1;
		for (Tuple tuple : tuples) {
			String teamId = tuple.getElement();
			long power = (long) tuple.getScore();
			CWTeamData teamData = teamDatas.get(teamId);
			if (teamData == null) {
				this.removeCWTeamPowerRank(teamId);
				continue;
			}
			String guildId = teamData.getGuildId();
			GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guildObj == null) {
				this.removeCWTeamPowerRank(teamId);
				continue;
			}
			//显示榜
			if(rank <= rankShowLimit){
				CWTeamRank.Builder rankInfo = CWTeamRank.newBuilder();
				CWTeamInfo.Builder teamBuilder = CWTeamInfo.newBuilder();
				teamBuilder.setId(teamData.getId());
				teamBuilder.setGuildId(guildId);
				teamBuilder.setGuildName(guildObj.getName());
				teamBuilder.setGuildTag(guildObj.getTag());
				teamBuilder.setGuildFlag(guildObj.getFlagId());
				teamBuilder.setName(teamData.getName());
				teamBuilder.setBattlePoint(power);
				rankInfo.setTeamInfo(teamBuilder);
				rankInfo.setRank(rank);
				ranksTemp.add(rankInfo);
			}
			//排名
			rankNumMapTemp.put(teamData.getId(), rank);
			rank++;
		}
		//重新赋值
		this.ranks = ranksTemp;
		this.rankNumMap = rankNumMapTemp;
	}
	
	/**
	 * 获取排行成员
	 * @return
	 */
	public List<CWTeamRank.Builder> getGuildPowerRanks() {
		return this.ranks;
	}
	
	
	/**
	 * 获取名次
	 * @param teamId
	 * @param serverId
	 * @return
	 */
	public int getCWTeamPowerRank(String teamId,String serverId){
		return this.rankNumMap.getOrDefault(teamId, -1);
	}
	
	
	
	/**
	 * 获取联盟战队战力
	 * @param teamId
	 * @param serverId
	 * @return
	 */
	public long getCWTeamPower(String teamId, String serverId){
		return CyborgWarRedis.getInstance().getCWTeamPower(teamId, serverId);
	}

	
	/**
	 * 添加成员
	 * @param members
	 * @param serverId
	 * @return
	 */
	public boolean addCWTeamPowerRanks(Map<String, Double> members, String serverId){
		return CyborgWarRedis.getInstance().addCWTeamPowerRanks(members, serverId);
	}

	
	
	/**
	 * 删除战队的赛博之战战力排名
	 * @param teamId
	 */
	public void removeCWTeamPowerRank(String teamId){
		CyborgWarRedis.getInstance().removeCWTeamPowerRank(teamId);
	}
}
