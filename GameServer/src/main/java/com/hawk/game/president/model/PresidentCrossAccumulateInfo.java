package com.hawk.game.president.model;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.service.GuildService;

/**
 * 跨服盟总推进度条信息
 * @author Golden
 *
 */
public class PresidentCrossAccumulateInfo {

	private String guild;
	/**
	 * 开始占领时间
	 */
	private long startOccupyTime;
	
	/**
	 * 攻守
	 */
	private boolean isAtker;
	
	/**
	 * 联盟占领盟总时间累计
	 */
	private Map<String,Long> accumulateGuildOccupyTime = new ConcurrentHashMap<String, Long>();
	

	
	private CrossPlayerStruct winner;
	
	
	
	/**
	 * 更新时间
	 */
	private long updateTime;
	
	
	/**
	 * 构造
	 * @param jsonInfo
	 */
	public PresidentCrossAccumulateInfo(String jsonInfo) {
		if (HawkOSOperator.isEmptyString(jsonInfo)) {
			return;
		}
		JSONObject json = JSONObject.parseObject(jsonInfo);
		if(json.containsKey("guild")){
			this.guild = json.getString("guild");
		}
		if(json.containsKey("startOccupyTime")){
			this.startOccupyTime = json.getLongValue("startOccupyTime");
		}
		Map<String,Long> timeTemp = new ConcurrentHashMap<String, Long>();
		if(json.containsKey("accumulateGuildOccupyTime")){
			String str = json.getString("accumulateGuildOccupyTime");
			JSONObject timeJson = JSONObject.parseObject(str);
			for(String key :timeJson.keySet()){
				timeTemp.put(key, timeJson.getLongValue(key));
			}
		}
		this.accumulateGuildOccupyTime = timeTemp;
		if(json.containsKey("winner")){
			try {
				String winnerStr = json.getString("winner");
				CrossPlayerStruct.Builder builder = CrossPlayerStruct.newBuilder();
				JsonFormat.merge(winnerStr, builder);
				this.winner = builder.build();
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
			
		}
		if(json.containsKey("updateTime")){
			this.updateTime = json.getLongValue("updateTime");
		}
		if(json.containsKey("isAtker")){
			this.isAtker = json.getBooleanValue("isAtker");
		}
		
		
	}
	
	

	/**
	 * 序列化
	 * @return
	 */
	public String serialize() {
		JSONObject json = new JSONObject();
		if(!HawkOSOperator.isEmptyString(this.guild)){
			json.put("guild", this.guild);
		}
		json.put("startOccupyTime", this.startOccupyTime);
		
		JSONObject timeJson = new JSONObject();
		for(Map.Entry<String, Long> entry : this.accumulateGuildOccupyTime.entrySet()){
			timeJson.put(entry.getKey(), entry.getValue());
		}
		json.put("accumulateGuildOccupyTime", timeJson.toJSONString());
		if(Objects.nonNull(this.winner)){
			String winnerStr = JsonFormat.printToString(this.winner);
			json.put("winner", winnerStr);
		}
		json.put("updateTime", this.updateTime);
		json.put("isAtker", this.isAtker);
		return json.toJSONString();
	}
	
	
	
	/**
	 * 更换占领方
	 * @param speed
	 * @param isAtker
	 */
	public void changeOccupy(String guild,boolean atk) {
		long curTime = HawkTime.getMillisecond();
		if(!HawkOSOperator.isEmptyString(this.guild)){
			long occupyTime = (curTime - startOccupyTime) / 1000;
			this.addGuildOccupyTime(this.guild, occupyTime);
		}
		this.guild = guild;
		this.startOccupyTime = curTime;
		this.isAtker = atk;
		this.updateTime = curTime;
		this.saveRedis();
	}
	
	public void saveRedis(){
		String serverId = GsConfig.getInstance().getServerId();
		int termId = CrossActivityService.getInstance().getTermId();
		RedisProxy.getInstance().updatePresidentCrossAccumulateInfo(serverId, termId, this);
	}
	
	
	
	public void addGuildOccupyTime(String guild,long time){
		long otime = this.accumulateGuildOccupyTime.getOrDefault(guild, 0l);
		otime += time;
		this.accumulateGuildOccupyTime.put(guild, otime);
	}
	
	
	/**
	 * 是否占领完成
	 * @return
	 */
	public boolean isOccupySuccess(long fightEnd) {
		HawkLog.logPrintln("cross occupy, guild:{},start:{},winner:{},accumulate:{}", this.guild,this.startOccupyTime,this.winner,
				JSONObject.toJSONString(this.accumulateGuildOccupyTime));
		if(Objects.nonNull(this.winner)){
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		long curOccupySecond = 0;
		if(!HawkOSOperator.isEmptyString(this.guild)){
			curOccupySecond = (curTime - startOccupyTime) / 1000;
		}
		//持续占领时间够了，则胜利
		if(curOccupySecond > CrossConstCfg.getInstance().getPresidentOccupyTime()){
			this.setWinGuild(this.guild);
			this.updateTime = curTime;
			this.saveRedis();
			return true;
		}
		//累计时间够了，则胜利
		String maxGuild = "";
		long maxTime = 0;
		for(Map.Entry<String, Long> entry : this.accumulateGuildOccupyTime.entrySet()){
			String gId = entry.getKey();
			long otime = entry.getValue();
			if(otime > maxTime){
				maxGuild = gId;
				maxTime = otime;
			}
		}
		if(curOccupySecond > 0){
			//在比较一下 当前正在占领的联盟
			long otime = this.accumulateGuildOccupyTime.getOrDefault(this.guild, 0l);
			otime += curOccupySecond;
			if(otime >= maxTime){
				maxGuild = this.guild;
				maxTime = otime;
			}
		}
		if(!HawkOSOperator.isEmptyString(maxGuild) &&
				maxTime > CrossConstCfg.getInstance().getCrossWinAccumulateOccupyTime()){
			this.setWinGuild(maxGuild);
			this.updateTime = curTime;
			this.saveRedis();
			return true;
		}
		//如果战斗结束时间到了，则选累计时间最大的联盟为胜利者
		if(curTime >= fightEnd){
			if(!HawkOSOperator.isEmptyString(maxGuild)){
				this.setWinGuild(maxGuild);
				this.updateTime = curTime;
				this.saveRedis();
				return true;
			}else{
				String openServer = CrossActivityService.getInstance().getPresidentOpenServer();
				String crossFightPresident = RedisProxy.getInstance().getCrossFightPresident(openServer);
				CrossPlayerStruct fightPresidentInfo = RedisProxy.getInstance().getFightPresidentInfo(crossFightPresident);
				if(Objects.nonNull(fightPresidentInfo)){
					this.setWinPlayer(fightPresidentInfo);
					this.updateTime = curTime;
					this.saveRedis();
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * 设置胜利者
	 * @param fightPresidentInfo
	 */
	public void setWinPlayer(CrossPlayerStruct fightPresidentInfo){
		this.winner = fightPresidentInfo;
	}
	
	
	/**
	 * 根据联盟占领设置胜利者
	 * @param guildId
	 */
	public void setWinGuild(String guildId){
		String serverId = GuildService.getInstance().getGuildServerId(guildId);
		String localServer = GsConfig.getInstance().getServerId();
		if(localServer.equals(serverId)){
			//本服
			String winLeaderId = GuildService.getInstance().getGuildLeaderId(guildId);
			if (HawkOSOperator.isEmptyString(winLeaderId)) {
				return;
			}
			Player gleader = GlobalData.getInstance().makesurePlayer(winLeaderId);
			if(Objects.isNull(gleader)){
				return;
			}
			CrossPlayerStruct.Builder builder = CrossPlayerStruct.newBuilder();
			builder.setPlayerId(winLeaderId);
			builder.setName(gleader.getName());
			builder.setIcon(gleader.getIcon());
			builder.setPfIcon(gleader.getPfIcon());
			builder.setGuildID(gleader.getGuildId());
			builder.setGuildName(GuildService.getInstance().getGuildName(gleader.getGuildId()));
			builder.setGuildTag(GuildService.getInstance().getGuildTag(gleader.getGuildId()));
			builder.setGuildFlag(GuildService.getInstance().getGuildFlag(gleader.getGuildId()));
			builder.setServerId(gleader.getMainServerId());
			this.winner = builder.build();
		}else{
			CrossPlayerStruct leaderInfo = RedisProxy.getInstance().getCrossGuildLeaderInfo(guildId);
			if(Objects.isNull(leaderInfo)){
				return;
			}
			this.winner = leaderInfo;
		}
		
	}
	
	public CrossPlayerStruct getCrossPresidentGuildLeaderInfo(String serverId,String guidlId){
		if(HawkOSOperator.isEmptyString(serverId) || 
				HawkOSOperator.isEmptyString(guidlId)){
			return null;
		}
		String localServer = GsConfig.getInstance().getServerId();
		if(localServer.equals(serverId)){
			//本服
			String winLeaderId = GuildService.getInstance().getGuildLeaderId(guidlId);
			if (HawkOSOperator.isEmptyString(winLeaderId)) {
				return null;
			}
			Player gleader = GlobalData.getInstance().makesurePlayer(winLeaderId);
			if(Objects.isNull(gleader)){
				return null;
			}
			CrossPlayerStruct.Builder builder = CrossPlayerStruct.newBuilder();
			builder.setPlayerId(winLeaderId);
			builder.setName(gleader.getName());
			builder.setIcon(gleader.getIcon());
			builder.setPfIcon(gleader.getPfIcon());
			builder.setGuildID(gleader.getGuildId());
			builder.setGuildName(GuildService.getInstance().getGuildName(gleader.getGuildId()));
			builder.setGuildTag(GuildService.getInstance().getGuildTag(gleader.getGuildId()));
			builder.setGuildFlag(GuildService.getInstance().getGuildFlag(gleader.getGuildId()));
			builder.setServerId(gleader.getMainServerId());
			return builder.build();
		}else{
			CrossPlayerStruct leaderInfo = RedisProxy.getInstance().getCrossGuildLeaderInfo(guidlId);
			return leaderInfo;
		}
	}
	
	
	
	
	public Map<String, Long> getAccumulateGuildOccupyTime() {
		return accumulateGuildOccupyTime;
	}
	
	public String getGuild() {
		return guild;
	}
	
	public boolean isAtker() {
		return isAtker;
	}
	
	
	public long getStartOccupyTime() {
		return startOccupyTime;
	}
	
	public CrossPlayerStruct getWinner() {
		return winner;
	}
	
}
