package com.hawk.game.service.cyborgWar;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

public class CyborgWarCacheData {

	//联盟ID-成员:小队ID
	private Map<String,Map<String,String>> guildTeams = new ConcurrentHashMap<>();
	//更新时间
	private Map<String,Long> guildTeamUpdateTime = new ConcurrentHashMap<>();
	
	
	/**
	 * 获取玩家所在小队ID
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public String getPlayerTeam(String guildId,String playerId){
		if(HawkOSOperator.isEmptyString(guildId) ||
				HawkOSOperator.isEmptyString(playerId) ){
			return "";
		}
		
		Map<String,String> map = getGuildPlayerTeam(guildId);
		if(Objects.isNull(map)){
			return "";
		}
		if(!map.containsKey(playerId)){
			return "";
		}
		return map.get(playerId);
	}
	
	/**
	 * 获取缓存数据
	 * @param guildId
	 * @return
	 */
	public Map<String,String> getGuildPlayerTeam(String guildId){
		long curTime = HawkTime.getMillisecond();
		long updateTime = guildTeamUpdateTime.getOrDefault(guildId, 0l);
		if(curTime > updateTime + HawkTime.MINUTE_MILLI_SECONDS * 3){
			 Map<String, String> map = CyborgWarService.getInstance().getMemberTeamMap(guildId);
			 this.updateGuildPlayerTeam(guildId, map);
		}
		return this.guildTeams.get(guildId);
	}
	
	
	/**
	 * 更新数据
	 * @param guildId
	 * @param map
	 */
	public void updateGuildPlayerTeam(String guildId,Map<String,String> map){
		long curTime = HawkTime.getMillisecond();
		this.guildTeams.put(guildId, map);
		this.guildTeamUpdateTime.put(guildId,curTime);
	}
}
