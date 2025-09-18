package com.hawk.game.module.lianmengyqzz.march.data.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;

public class YQZZJoinServer implements IYQZZData{
	
	private static final String redisKey = "YQZZ_ACTIVITY_JOIN_SERVER";
	
	/** 期数*/
	private int termId;
	/** 服务器ID*/
	private String serverId;
	/** 参战战力*/
	private long power;
	
	private String leaderId;
	private String leaderName;
	private String leaderGuild;//联盟ID
	private String leaderGuildName;  //名称
	private String leaderGuildTag;  //简称
	private int leaderGuildFlag;  //联盟旗帜
	private List<String> joinGuilds;
	
	private Map<String,Integer> freePlayers;
	// 当前进程使用内存
	private long totalMem;
	// 当前进程可使用的最大内存
	private long usedMem;
	private double cpuUsage;
	private int openDayW;
	public int getGuildCount(){
		return this.joinGuilds.size();
	}
	
	
	public int getTermId() {
		return termId;
	}
	
	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	
	public String getServerId() {
		return serverId;
	}
	
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	public long getPower() {
		return power;
	}
	
	public void setPower(long power) {
		this.power = power;
	}
	
	
	
	public String getLeaderId() {
		return leaderId;
	}


	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}


	public String getLeaderName() {
		return leaderName;
	}

	public void setLeaderName(String leaderName) {
		this.leaderName = leaderName;
	}

	
	public String getLeaderGuild() {
		return leaderGuild;
	}


	public void setLeaderGuild(String leaderGuild) {
		this.leaderGuild = leaderGuild;
	}


	public String getLeaderGuildName() {
		return leaderGuildName;
	}


	public void setLeaderGuildName(String leaderGuildName) {
		this.leaderGuildName = leaderGuildName;
	}


	public String getLeaderGuildTag() {
		return leaderGuildTag;
	}


	public void setLeaderGuildTag(String leaderGuildTag) {
		this.leaderGuildTag = leaderGuildTag;
	}


	public int getLeaderGuildFlag() {
		return leaderGuildFlag;
	}


	public void setLeaderGuildFlag(int leaderGuildFlag) {
		this.leaderGuildFlag = leaderGuildFlag;
	}


	public void setJoinGuilds(List<String> joinGuilds) {
		this.joinGuilds = joinGuilds;
	}
	
	public List<String> getJoinGuilds() {
		return joinGuilds;
	}
	
	
	public void setFreePlayers(Map<String, Integer> freePlayers) {
		this.freePlayers = freePlayers;
	}
	
	public Map<String, Integer> getFreePlayers() {
		return freePlayers;
	}

	public long getTotalMem() {
		return totalMem;
	}

	public void setTotalMem(long totalMem) {
		this.totalMem = totalMem;
	}

	public long getUsedMem() {
		return usedMem;
	}


	public void setUsedMem(long usedMem) {
		this.usedMem = usedMem;
	}

	public double getCpuUsage() {
		return cpuUsage;
	}

	public void setCpuUsage(double cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public int getOpenDayW() {
		return openDayW;
	}

	public void setOpenDayW(int openDayW) {
		this.openDayW = openDayW;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", this.termId);
		obj.put("serverId", this.serverId);
		obj.put("power", this.power);
		obj.put("leaderId", this.leaderId);
		obj.put("leaderName", this.leaderName);
		obj.put("leaderGuild", this.leaderGuild);
		obj.put("leaderGuildName", this.leaderGuildName);
		obj.put("leaderGuildTag", this.leaderGuildTag);
		obj.put("leaderGuildFlag", this.leaderGuildFlag);
		
		JSONArray arr = new JSONArray();
		if(this.joinGuilds!= null && !this.joinGuilds.isEmpty()){
			for(String guild : this.joinGuilds){
				arr.add(guild);
			}
		}
		obj.put("joinGuilds", arr.toJSONString());
		
		JSONArray arrfree = new JSONArray();
		if(this.freePlayers!= null && !this.freePlayers.isEmpty()){
			for(Map.Entry<String, Integer> entry : this.freePlayers.entrySet()){
				JSONObject entryObj = new JSONObject();
				entryObj.put("playerId", entry.getKey());
				entryObj.put("rank", entry.getValue());
				arrfree.add(entryObj);
			}
		}
		obj.put("freePlayers", arrfree.toJSONString());
		obj.put("usedMem", usedMem);
		obj.put("totalMem", totalMem);
		obj.put("cpuUsage", cpuUsage);
		obj.put("openDayW", openDayW);
		return obj.toJSONString();
	}


	@Override
	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.termId = 0;
			this.serverId = null;
			this.power = 0;
			this.leaderId= null;
			this.leaderName= null;
			this.leaderGuild= null;
			this.leaderGuildName= null;
			this.leaderGuildTag= null;
			this.leaderGuildFlag= 0;
			this.joinGuilds = new ArrayList<>();
			this.freePlayers = new HashMap<>();
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		if(obj.containsKey("termId")){
			this.termId = obj.getIntValue("termId");
		}
		if(obj.containsKey("serverId")){
			this.serverId = obj.getString("serverId");
		}
		if(obj.containsKey("power")){
			this.power = obj.getLongValue("power");
		}
		
		if(obj.containsKey("leaderId")){
			this.leaderId = obj.getString("leaderId");
		}
		if(obj.containsKey("leaderName")){
			this.leaderName = obj.getString("leaderName");
		}
		if(obj.containsKey("leaderGuild")){
			this.leaderGuild = obj.getString("leaderGuild");
		}
		if(obj.containsKey("leaderGuildName")){
			this.leaderGuildName = obj.getString("leaderGuildName");
		}
		if(obj.containsKey("leaderGuildTag")){
			this.leaderGuildTag = obj.getString("leaderGuildTag");
		}
		if(obj.containsKey("leaderGuildFlag")){
			this.leaderGuildFlag = obj.getIntValue("leaderGuildFlag");
		}
		
		this.joinGuilds = new ArrayList<>();
		if(obj.containsKey("joinGuilds")){
			String joinGuildsStr = obj.getString("joinGuilds");
			JSONArray arr = JSONArray.parseArray(joinGuildsStr);
			for(int i=0;i<arr.size();i++){
				String guildId = arr.getString(i);
				this.joinGuilds.add(guildId);
			}
					
		}
		
		this.freePlayers = new HashMap<>();
		if(obj.containsKey("freePlayers")){
			String playerIdsStr = obj.getString("freePlayers");
			JSONArray arr = JSONArray.parseArray(playerIdsStr);
			for(int i=0;i<arr.size();i++){
				String str = arr.getString(i);
				JSONObject playerObj = JSONObject.parseObject(str);
				String playerId = playerObj.getString("playerId");
				int rank = playerObj.getIntValue("rank");
				this.freePlayers.put(playerId, rank);
			}
		}
		this.usedMem = obj.getLongValue("usedMem");
		this.totalMem = obj.getLongValue("totalMem");
		this.cpuUsage = obj.getDoubleValue("cpuUsage");
		this.openDayW = obj.getIntValue("openDayW");
	}


	@Override
	public void saveRedis() {
		String key = redisKey  + ":" + this.termId;
		RedisProxy.getInstance().getRedisSession().hSet(key, this.serverId, this.serializ(),YQZZConst.REDIS_DATA_EXPIRE_TIME);	
		StatisManager.getInstance().incRedisKey(redisKey);
	}

	public static YQZZJoinServer load(int termId, String serverId){
		String key = redisKey  + ":" + termId;
		YQZZJoinServer rlt = null;
		String value  = RedisProxy.getInstance().getRedisSession().hGet(key, serverId, YQZZConst.REDIS_DATA_EXPIRE_TIME);
		if(!HawkOSOperator.isEmptyString(value)){
			rlt = new YQZZJoinServer();
			rlt.mergeFrom(value);
		}
		return rlt;
	}

	public static Map<String,YQZZJoinServer> loadAll(int termId){
		String key = redisKey  + ":" + termId;
		Map<String,YQZZJoinServer> rlt = new HashMap<>();
		Map<String,String> map = RedisProxy.getInstance().getRedisSession().hGetAll(key, YQZZConst.REDIS_DATA_EXPIRE_TIME);
		for(Map.Entry<String, String> entry : map.entrySet()){
			String value = entry.getValue();
			YQZZJoinServer join = new YQZZJoinServer();
			join.mergeFrom(value);
			rlt.put(join.getServerId(), join);
		}
		return rlt;
	}
	
	public static Map<String,YQZZJoinServer> loadAll(int termId,List<String> servers){
		String key = redisKey  + ":" + termId;
		Map<String,YQZZJoinServer> rlt = new HashMap<>();
		if(servers == null || servers.isEmpty()){
			return rlt;
		}
		List<String> list = RedisProxy.getInstance().getRedisSession()
				.hmGet(key, servers.toArray(new String[servers.size()]));
		for(String str : list){
			YQZZJoinServer join = new YQZZJoinServer();
			join.mergeFrom(str);
			rlt.put(join.getServerId(), join);
		}
		return rlt;
	}
	
}
