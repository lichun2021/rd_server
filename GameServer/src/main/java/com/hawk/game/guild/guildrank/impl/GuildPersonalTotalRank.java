package com.hawk.game.guild.guildrank.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildPersonalRank;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.guild.guildrank.data.GuildRankTuple;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;


/**
 *  个人总榜
 *  @Desc 
 *	@author RickMei
 *  @Date 2018年11月15日 下午3:34:00
 */
public class GuildPersonalTotalRank extends GuildPersonalRank {
	
	Logger logger = LoggerFactory.getLogger("Server");
	
	public GuildPersonalTotalRank (GRankType rankType){
		super(rankType);
	}

	@Override
	public boolean delRankKey(String guildid, String playerId) {
		return false;
	}

	@Override
	public void addRankVal(String guildId, String playerId, long val) {
		if(!isClosed()){
			String key = String.format("%s:%s", rankType.getTypeName(), playerId);
			try{
				long afterAdd = RedisProxy.getInstance().getRedisSession().increaseBy(key, val, rankType.GetOverdueTime()); // 数据保存30天
				logger.info("guildrank_log {}, guildId:{} player:{} add:{} after:{}",rankType.getTypeName(), guildId, playerId, val, afterAdd);
			}
			catch(Exception e){
				HawkException.catchException(e);			
			}		
		}
	}


	@Override
	public Set<Tuple> getRankList(String guildId) {
	    Collection<String> members = GuildService.getInstance().getGuildMembers(guildId);
   		final int count = members.size();
		TreeSet<Tuple> rankSet = new TreeSet<>();
    	if(count > 0){
    		//获取members的得分
    		String[] memberIds =(String[]) members.toArray(new String[0]);
    		List<Response<String>> piplineRes = new ArrayList<>();
    		try(Jedis jedis = RedisProxy.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()){
    			for( String playerId : memberIds ){
    				String key = String.format("%s:%s", rankType.getTypeName(), playerId);;
    				Response<String> onePiplineResp = pip.get(key);
    				piplineRes.add(onePiplineResp );
    			}
    			pip.sync();
    			if( piplineRes.size() == count ){
	 	    		for(int i = 0; i < count; i++){
	 	    			String retStr = piplineRes.get(i).get();
	 	    			
						if(null != retStr)
							rankSet.add(new GuildRankTuple( memberIds[i], (double)Long.parseLong(retStr)));
	 	    		}   		
    			}
    			
    		}catch (Exception e) {
    			HawkException.catchException(e);
    		}
    	}
    	if(rankType.isOrderSmallToLarge())
    		return rankSet;
    	else
    		return rankSet.descendingSet();
	}

	@Override
	public Set<Tuple> getYesterDayRankList(String guildId) {
		return getRankList(guildId);
	}

}
