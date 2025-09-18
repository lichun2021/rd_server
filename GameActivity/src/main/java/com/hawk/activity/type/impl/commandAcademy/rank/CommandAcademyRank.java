package com.hawk.activity.type.impl.commandAcademy.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.commandAcademy.CommandAcademyActivity;
import com.hawk.activity.type.impl.commandAcademy.CommandAcademyConst;

import redis.clients.jedis.Tuple;

public class CommandAcademyRank{
	
	
	/** 排行榜类型*/
	private CommandAcademyConst.RankType rankType; 
	
	/** 成员缓存*/
	private List<CommandAcademyRankMember> rankList = new ArrayList<CommandAcademyRankMember>();
	
	

	public CommandAcademyRank(CommandAcademyConst.RankType rankType){
		this.rankType = rankType;
	}

	
	public void loadRank() {
		this.rankSort();
	}
	


	public void rankSort() {
		Optional<CommandAcademyActivity> opActivity = ActivityManager.getInstance().
				getGameActivityByType(com.hawk.activity.type.ActivityType.COMMAND_ACADEMY_ACTIVITY.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		
		int rankSize = getRankSize();
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), 0, Math.max((rankSize - 1), 0));		
		List<CommandAcademyRankMember> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			CommandAcademyRankMember member = new CommandAcademyRankMember();
			member.setPlayerId(rank.getElement());
			member.setRank(index);
			member.setScore(rank.getScore());
			newRankList.add(member);
			index++;
		}
		rankList = newRankList;		
	}


	public void clean() {
		rankList  = new ArrayList<>(this.getRankSize());
	}


	private int getRankSize() {
		Optional<CommandAcademyActivity> opActivity = ActivityManager.getInstance().
				getGameActivityByType(com.hawk.activity.type.ActivityType.COMMAND_ACADEMY_ACTIVITY.intValue());
		if (!opActivity.isPresent()) {
			return 0;
		}
		return opActivity.get().getRankSize(this.rankType);
	}


	public void addScore(String id, int score) {		
		
	}


	public void remMember(String id) {
		ActivityLocalRedis.getInstance().zrem(getRedisKey(), id);
	}

	
	public boolean insertRank(CommandAcademyRankMember rankInfo) {
		String playerId = rankInfo.getPlayerId();
		Double rankScore = rankInfo.getScore();
		ActivityLocalRedis.getInstance().zadd(getRedisKey(), rankScore, playerId);
		return true;
	}


	
	public CommandAcademyRankMember getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		double score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore();
		}
		CommandAcademyRankMember srank = new CommandAcademyRankMember(id,score,rank);
		return srank;
	}
	
	
	
	public CommandAcademyRankMember getCacheRank(String id) {
		List<CommandAcademyRankMember> list = this.rankList;
		for(CommandAcademyRankMember m : list){
			if(id.equals(m.getPlayerId())){
				return m;
			}
		}
		return null;
	}
		
	
		
	
	
	private String getRedisKey(){
		Optional<CommandAcademyActivity> opActivity = 
				ActivityManager.getInstance().getGameActivityByType(
						com.hawk.activity.type.ActivityType.COMMAND_ACADEMY_ACTIVITY.intValue());
		if (opActivity.isPresent()) {
			 return opActivity.get().getRankRedisKey(this.rankType);
		} else {
			return "";
		}
		
	}

	
	
	public void cleanRankList() {
		this.rankList = new ArrayList<>();
	}
	
	
	public List<CommandAcademyRankMember> getClientShowList(){
		List<CommandAcademyRankMember> rankList = this.rankList;
		List<CommandAcademyRankMember> list = new ArrayList<CommandAcademyRankMember>();
		for(int i =0;i<CommandAcademyConst.RANK_SHOW_SIZE;i++){
			if(i>= rankList.size()){
				break;
			}
			CommandAcademyRankMember m = rankList.get(i);
			list.add(m);
		}
		return list;
	}


	public CommandAcademyConst.RankType getRankType() {
		return rankType;
	}


	public void setRankType(CommandAcademyConst.RankType rankType) {
		this.rankType = rankType;
	}


	public void setRankList(List<CommandAcademyRankMember> rankList) {
		this.rankList = rankList;
	}


	public List<CommandAcademyRankMember> getRankList() {
		return rankList;
	}

	
	


	
	
	
}
