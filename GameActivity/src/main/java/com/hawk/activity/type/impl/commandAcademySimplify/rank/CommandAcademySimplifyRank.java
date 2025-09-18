package com.hawk.activity.type.impl.commandAcademySimplify.rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.impl.commandAcademySimplify.CommandAcademySimplifyActivity;
import com.hawk.activity.type.impl.commandAcademySimplify.CommandAcademySimplifyConst;

import redis.clients.jedis.Tuple;

public class CommandAcademySimplifyRank{
	
	/** 排行榜类型*/
	private CommandAcademySimplifyConst.RankType rankType; 
	
	/** 成员缓存*/
	private List<CommandAcademySimplifyRankMember> rankList = new ArrayList<CommandAcademySimplifyRankMember>();
	
	public CommandAcademySimplifyRank(CommandAcademySimplifyConst.RankType rankType){
		this.rankType = rankType;
	}
	
	public void loadRank() {
		this.rankSort();
	}

	public void rankSort() {
		Optional<CommandAcademySimplifyActivity> opActivity = ActivityManager.getInstance().
				getGameActivityByType(com.hawk.activity.type.ActivityType.COMMAND_ACADEMY_SIMPLIFY_ACTIVITY.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		
		int rankSize = getRankSize();
		Set<Tuple> rankSet = ActivityLocalRedis.getInstance().zrevrange(getRedisKey(), 0, Math.max((rankSize - 1), 0));		
		List<CommandAcademySimplifyRankMember> newRankList = new ArrayList<>(rankSize);
		int index = 1;
		for (Tuple rank : rankSet) {
			CommandAcademySimplifyRankMember member = new CommandAcademySimplifyRankMember();
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
		Optional<CommandAcademySimplifyActivity> opActivity = ActivityManager.getInstance().
				getGameActivityByType(com.hawk.activity.type.ActivityType.COMMAND_ACADEMY_SIMPLIFY_ACTIVITY.intValue());
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

	
	public boolean insertRank(CommandAcademySimplifyRankMember rankInfo) {
		String playerId = rankInfo.getPlayerId();
		Double rankScore = rankInfo.getScore();
		ActivityLocalRedis.getInstance().zadd(getRedisKey(), rankScore, playerId);
		return true;
	}

	
	public CommandAcademySimplifyRankMember getRank(String id) {
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(getRedisKey(), id);
		int rank = 0;
		double score = 0;
		if (index != null) {
			rank = index.getIndex().intValue() + 1;
			score = index.getScore();
		}
		CommandAcademySimplifyRankMember srank = new CommandAcademySimplifyRankMember(id,score,rank);
		return srank;
	}
	
	public CommandAcademySimplifyRankMember getCacheRank(String id) {
		List<CommandAcademySimplifyRankMember> list = this.rankList;
		for(CommandAcademySimplifyRankMember m : list){
			if(id.equals(m.getPlayerId())){
				return m;
			}
		}
		return null;
	}
		
	private String getRedisKey(){
		Optional<CommandAcademySimplifyActivity> opActivity = 
				ActivityManager.getInstance().getGameActivityByType(
						com.hawk.activity.type.ActivityType.COMMAND_ACADEMY_SIMPLIFY_ACTIVITY.intValue());
		if (opActivity.isPresent()) {
			 return opActivity.get().getRankRedisKey(this.rankType);
		} else {
			return "";
		}
		
	}

	public void cleanRankList() {
		this.rankList = new ArrayList<>();
	}
	
	public List<CommandAcademySimplifyRankMember> getClientShowList(){
		List<CommandAcademySimplifyRankMember> rankList = this.rankList;
		List<CommandAcademySimplifyRankMember> list = new ArrayList<CommandAcademySimplifyRankMember>();
		for(int i =0;i<CommandAcademySimplifyConst.RANK_SHOW_SIZE;i++){
			if(i>= rankList.size()){
				break;
			}
			CommandAcademySimplifyRankMember m = rankList.get(i);
			list.add(m);
		}
		return list;
	}


	public CommandAcademySimplifyConst.RankType getRankType() {
		return rankType;
	}

	public void setRankType(CommandAcademySimplifyConst.RankType rankType) {
		this.rankType = rankType;
	}

	public void setRankList(List<CommandAcademySimplifyRankMember> rankList) {
		this.rankList = rankList;
	}

	public List<CommandAcademySimplifyRankMember> getRankList() {
		return rankList;
	}
	
}
