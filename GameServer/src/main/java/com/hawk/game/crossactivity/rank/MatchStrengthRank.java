package com.hawk.game.crossactivity.rank;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.player.Player;

import redis.clients.jedis.Tuple;

public class MatchStrengthRank {
	
	private static MatchStrengthRank ins;
	public static MatchStrengthRank getInstance(){
		if(ins == null){
			ins = new  MatchStrengthRank();
		}
		return ins;
	}
	
	private MatchStrengthRank() {
		
	}
	
	public void updateStrength(Player player){
		String serverId = player.getMainServerId();
		String key = this.getRankKey(serverId);
		RedisProxy.getInstance().getRedisSession().zAdd(key, player.getStrength(), player.getId(),(int)TimeUnit.DAYS.toSeconds(14));
	}
	
	public Set<Tuple>  getStrengthList(int size){
		String serverId = GsConfig.getInstance().getServerId();
		String key = this.getRankKey(serverId);
		Set<Tuple> rankSet = RedisProxy.getInstance().getRedisSession().zRevrangeWithScores(key, 0, size, 0);
		return rankSet;
	}
	
	public String getRankKey(String serverId){
		String key = "MATCH_STRENGTH_RANK";
		StatisManager.getInstance().incRedisKey(key);
		return key+":"+ serverId;
	}
	
}
