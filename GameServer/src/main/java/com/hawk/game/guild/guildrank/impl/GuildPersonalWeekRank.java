package com.hawk.game.guild.guildrank.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hawk.os.HawkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.guildrank.GuildPersonalRank;
import com.hawk.game.guild.guildrank.GuildRankTypedef.GRankType;
import com.hawk.game.guild.guildrank.data.GuildRankTuple;
import com.hawk.game.service.GuildService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.Tuple;

/**
 *  个人周榜 
 *  @Desc 
 *	@author RickMei
 *  @Date 2018年11月15日 下午3:34:23
 */
class GuildPersonalWeekRank extends GuildPersonalRank {
	
	Logger logger = LoggerFactory.getLogger("Server");
	
	GuildPersonalWeekRank (GRankType rankType){
		super(rankType);
	}

	@Override
	public boolean delRankKey(String guildId, String playerId) {
		return false;
	}

	private String getPlayerWeekKey( String playerId, int pastDays ){
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek( Calendar.MONDAY );
		cal.setTime(new Date());
		cal.add(Calendar.DATE, (0 - pastDays));
		int weekIndex = cal.get(Calendar.WEEK_OF_YEAR);
		// fmt y-m-d
		String key = String.format("%s:%s:%d", rankType.getTypeName(), playerId, weekIndex );
		return key;
	}
	
	@Override
	public void addRankVal(String guildId, String playerId, long val) {
		try{
			if(!isClosed()){
				String key = getPlayerWeekKey(playerId,0);
				//哪年的第几周
				long afterAdd = LocalRedis.getInstance().getRedisSession().increaseBy( key, val, rankType.GetOverdueTime()); // 数据保存8天
				logger.info("guildrank_log {}, guildId:{} player:{} add:{} after:{}",rankType.getTypeName(), guildId, playerId, val, afterAdd);
			}
		}catch (Exception e) {
    		HawkException.catchException(e);
    	}
	}
	
	public Set<Tuple> getRankList(String guildId, int passDays) {
		// 个人周榜单存本地redis
		// 获取联盟内玩家id
    	Collection<String> members = GuildService.getInstance().getGuildMembers(guildId);
   		final int count = members.size();
		TreeSet<Tuple> rankSet = new TreeSet<>();
    	if(count > 0){
    		//获取members的得分
    		String[] memberIds =(String[]) members.toArray(new String[0]);
    		List<Response<String>> piplineRes = new ArrayList<>();
    		
    		try(Jedis jedis = LocalRedis.getInstance().getRedisSession().getJedis(); Pipeline pip = jedis.pipelined()){
    			for( String playerId : memberIds ){
    				
    				String key = getPlayerWeekKey(playerId,passDays);
    				Response<String> onePiplineRes = pip.get(key);
    				 piplineRes.add(onePiplineRes);
    			}
    			pip.sync();
    			
    			if( piplineRes.size() == count ){
	 	    		for(int i = 0; i < count; i++){
   						String retStr = piplineRes.get(i).get();
						if(null != retStr)
							rankSet.add(new GuildRankTuple( memberIds[i], (double)Long.parseLong( retStr )));
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
	public Set<Tuple> getRankList(String guildId) {
		return getRankList(guildId, 0 );
	}

	@Override
	public Set<Tuple> getYesterDayRankList(String guildId) {
		return getRankList(guildId, 1);
	}
}