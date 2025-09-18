package com.hawk.game.module.lianmengyqzz.march.data.global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.global.StatisManager;
import com.hawk.game.module.lianmengyqzz.march.data.IYQZZData;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarGuild;

public class YQZZJoinGuild  implements IYQZZData{

	private static final String redisKey = "YQZZ_ACTIVITY_JOIN_GUILD";
	
	private int termId;
	
	private String serverId;
	
	private String guildId;
	
	private String guildName;
	
	private String guildTag;
	
	private int guildFlag;
	
	private String leaderId;
	
	private String leaderName;
	
	private int guildRank;
	
	private long power;
	
	
	
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

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String guildName) {
		this.guildName = guildName;
	}
	
	public int getGuildRank() {
		return guildRank;
	}
	
	

	public String getGuildTag() {
		return guildTag;
	}

	public void setGuildTag(String guildTag) {
		this.guildTag = guildTag;
	}

	public int getGuildFlag() {
		return guildFlag;
	}

	public void setGuildFlag(int guildFlag) {
		this.guildFlag = guildFlag;
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

	public void setGuildRank(int guildRank) {
		this.guildRank = guildRank;
	}
	
	public long getPower() {
		return power;
	}
	
	public void setPower(long power) {
		this.power = power;
	}
	
	public PBYQZZWarGuild.Builder genYQZZWarGuildBuilder(){
		PBYQZZWarGuild.Builder builder = PBYQZZWarGuild.newBuilder();
		builder.setServerId(this.serverId);
		builder.setId(this.guildId);
		builder.setName(this.guildName);
		if(!HawkOSOperator.isEmptyString(this.guildTag)){
			builder.setTag(this.guildTag);
		}else{
			builder.setTag("");
		}
		builder.setGuildFlag(this.guildFlag);
		builder.setLeaderId(this.leaderId);
		builder.setLeaderName(this.leaderName);
		return builder;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", this.termId);
		obj.put("serverId", this.serverId);
		obj.put("guildId", this.guildId);
		obj.put("guildName", this.guildName);
		obj.put("guildTag", this.guildTag);
		obj.put("guildFlag", this.guildFlag);
		obj.put("leaderId", this.leaderId);
		obj.put("leaderName", this.leaderName);
		obj.put("guildRank", this.guildRank);
		obj.put("power", this.power);
		return obj.toString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.termId = 0;
			this.serverId = null;
			this.guildId = null;
			this.guildName = null;
			this.guildTag = null;
			this.guildFlag = 0;
			this.leaderId = null;
			this.leaderName = null;
			this.guildRank = 0;
			this.power = 0;
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.termId = obj.getIntValue("termId");
		this.serverId = obj.getString("serverId");
		this.guildId = obj.getString("guildId");
		this.guildName = obj.getString("guildName");
		this.guildTag = obj.getString("guildTag");
		this.guildFlag = obj.getIntValue("guildFlag");
		this.leaderId = obj.getString("leaderId");
		this.leaderName = obj.getString("leaderName");
		this.guildRank = obj.getIntValue("guildRank");
		this.power = obj.getIntValue("power");
	}

	public void saveRedis() {
		String key = redisKey  + ":" + this.termId;
		RedisProxy.getInstance().getRedisSession().hSet(key, this.guildId, this.serializ(),YQZZConst.REDIS_DATA_EXPIRE_TIME);	
		StatisManager.getInstance().incRedisKey(redisKey);
	}
	
	public static YQZZJoinGuild loadData(int termId,String guildId){
		String key = redisKey  + ":" + termId;
		String str = RedisProxy.getInstance().getRedisSession().hGet(key, guildId);
		if(!HawkOSOperator.isEmptyString(str)){
			YQZZJoinGuild data = new YQZZJoinGuild();
			data.mergeFrom(str);
			return data;
		}
		return null;
	}

	public static void saveAllData(int termId,Map<String,YQZZJoinGuild> map){
		String key = redisKey  + ":" + termId;
		Map<String,String> saveMap = new HashMap<>();
		for(YQZZJoinGuild guild : map.values()){
			saveMap.put(guild.getGuildId(), guild.serializ());
		}
		RedisProxy.getInstance().getRedisSession().hmSet(key, saveMap, YQZZConst.REDIS_DATA_EXPIRE_TIME);
		
	}

	
	public static Map<String,YQZZJoinGuild> loadAllData(int termId,List<String> guilds){
		String key = redisKey  + ":" + termId;
		Map<String,YQZZJoinGuild> rlt = new HashMap<>();
		if(guilds == null ||guilds.isEmpty()){
			return rlt;
		}
		List<String> list = RedisProxy.getInstance().getRedisSession()
				.hmGet(key, guilds.toArray(new String[guilds.size()]));
		for(String str : list){
			YQZZJoinGuild join = new YQZZJoinGuild();
			join.mergeFrom(str);
			rlt.put(join.getGuildId(), join);
		}
		return rlt;
	}
	
}
