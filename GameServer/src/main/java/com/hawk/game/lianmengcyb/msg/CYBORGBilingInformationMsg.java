package com.hawk.game.lianmengcyb.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.msg.HawkMsg;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

import com.google.common.base.Objects;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.protocol.CYBORG.PBCYBORGGameInfoSync;
import com.hawk.game.protocol.CYBORG.PBCYBORGGuildInfo;
import com.hawk.game.protocol.CYBORG.PBCYBORGPlayerInfo;
import com.hawk.game.protocol.Mail.PBCyborgContributePlayer;

/***
 * 结算信息
 */
public class CYBORGBilingInformationMsg extends HawkMsg {
	private String campAGuild = "";
	private String campBGuild = "";
	private String campCGuild = "";
	private String campDGuild = "";
	private int campAPlayerCnt;
	private int campBPlayerCnt;
	private String roomId;
	private boolean isLeaguaWar;
	private int season;
	private PBCYBORGGameInfoSync lastSyncpb;
	/** 建筑占领记录 */
	private List<BuildingControlRecord> buildRecords = new ArrayList<>();
	/** 玩家个人积分 */
	private List<PlayerGameRecord> playerRecords = new ArrayList<>();

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public boolean isLeaguaWar() {
		return isLeaguaWar;
	}

	public void setLeaguaWar(boolean isLeaguaWar) {
		this.isLeaguaWar = isLeaguaWar;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public void init(PBCYBORGGameInfoSync lastSyncpb, String campAGuild, String campBGuild, String campCGuild, String campDGuild) {
		this.lastSyncpb = lastSyncpb;
		this.campAGuild = campAGuild;
		this.campBGuild = campBGuild;
		this.campCGuild = campCGuild;
		this.campDGuild = campDGuild;

		for (PBCYBORGPlayerInfo player : lastSyncpb.getPlayerInfoList()) {
			addPlayerRecord(player);
		}

	}

	public void addBuildRecord(ICYBORGBuilding build) {
		BuildingControlRecord record = new BuildingControlRecord();
		record.setBuildId(build.getPointType().getNumber());
		record.setIndex(build.getIndex());
		record.controlGuildTimeMap.putAll(build.getControlGuildTimeMap().asMap());
		for (Entry<String, Double> ent : build.getControlGuildHonorMap().entrySet()) {
			record.controlGuildHonorMap.put(ent.getKey(), ent.getValue().longValue());
		}

		buildRecords.add(record);
	}

	private void addPlayerRecord(PBCYBORGPlayerInfo player) {
		PlayerGameRecord record = new PlayerGameRecord();
		record.playerId = player.getPlayerId();
		record.collectHonor = player.getHonor();
		record.collectGuildHonor = player.getGuildHonor();
		if (player.getCamp() == 1) {
			campAPlayerCnt++;
		} else {
			campBPlayerCnt++;
		}
		playerRecords.add(record);
	}

	/**
	 * 联盟总分
	 */
	public long getGuildHonor(String guildid) {
		List<PBCYBORGGuildInfo> guildList = lastSyncpb.getGuildInfoList();
		for (PBCYBORGGuildInfo ginfo : guildList) {
			if (Objects.equal(guildid, ginfo.getGuildId())) {
				return ginfo.getHonor();
			}
		}
		return 0;
	}

	public List<BuildingControlRecord> getBuildRecords() {
		return buildRecords;
	}

	public void setBuildRecords(List<BuildingControlRecord> buildRecords) {
		this.buildRecords = buildRecords;
	}

	public List<PlayerGameRecord> getPlayerRecords() {
		return playerRecords;
	}

	public void setPlayerRecords(List<PlayerGameRecord> playerRecords) {
		this.playerRecords = playerRecords;
	}

	public class BuildingControlRecord {
		private int buildId;
		private int index; // 建筑序号
		/** 联盟控制时间 */
		private Map<String, Long> controlGuildTimeMap = new HashMap<>();
		/** 联盟控制取得积分 */
		private Map<String, Long> controlGuildHonorMap = new HashMap<>();

		public int getBuildId() {
			return buildId;
		}

		public void setBuildId(int buildId) {
			this.buildId = buildId;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public Map<String, Long> getControlGuildTimeMap() {
			return controlGuildTimeMap;
		}

		public void setControlGuildTimeMap(Map<String, Long> controlGuildTimeMap) {
			this.controlGuildTimeMap = controlGuildTimeMap;
		}

		public Map<String, Long> getControlGuildHonorMap() {
			return controlGuildHonorMap;
		}

		public void setControlGuildHonorMap(Map<String, Long> controlGuildHonorMap) {
			this.controlGuildHonorMap = controlGuildHonorMap;
		}

	}

	public class PlayerGameRecord {
		private String playerId;
		/** 个人采集积分 */
		private int collectHonor;
		/** 联盟采集积分 */
		private int collectGuildHonor;

		public String getPlayerId() {
			return playerId;
		}

		public void setPlayerId(String playerId) {
			this.playerId = playerId;
		}

		public int getHonor() {
			return collectHonor;
		}

		public void setHonor(int honor) {
			this.collectHonor = honor;
		}

		public int getGuildHonor() {
			return collectGuildHonor;
		}

		public void setGuildHonor(int guildHonor) {
			this.collectGuildHonor = guildHonor;
		}

	}

	public String getCampAGuild() {
		return campAGuild;
	}

	public void setCampAGuild(String campAGuild) {
		this.campAGuild = campAGuild;
	}

	public String getCampBGuild() {
		return campBGuild;
	}

	public void setCampBGuild(String campBGuild) {
		this.campBGuild = campBGuild;
	}

	public int getCampAPlayerCnt() {
		return campAPlayerCnt;
	}

	public void setCampAPlayerCnt(int campAPlayerCnt) {
		this.campAPlayerCnt = campAPlayerCnt;
	}

	public int getCampBPlayerCnt() {
		return campBPlayerCnt;
	}

	public void setCampBPlayerCnt(int campBPlayerCnt) {
		this.campBPlayerCnt = campBPlayerCnt;
	}

	public String getCampCGuild() {
		return campCGuild;
	}

	public void setCampCGuild(String campCGuild) {
		this.campCGuild = campCGuild;
	}

	public String getCampDGuild() {
		return campDGuild;
	}

	public void setCampDGuild(String campDGuild) {
		this.campDGuild = campDGuild;
	}
	
	
	
	
	public int getBattlePlayerCount(String guildId){
		for(PBCYBORGGuildInfo guild : this.lastSyncpb.getGuildInfoList()){
			if(guild.getGuildId().equals(guildId)){
				return guild.getPlayerCount();
			}
		}
		return 0;
	}
	
	public int getScoreOverPlayerCount(String guildId,int score){
		int camp = -1;
		int count = 0;
		for(PBCYBORGGuildInfo guild : this.lastSyncpb.getGuildInfoList()){
			if(guild.getGuildId().equals(guildId)){
				camp = guild.getCamp();
				break;
			}
		}

		if(camp < 0){
			return count;
		}
		
		for(PBCYBORGPlayerInfo player: this.lastSyncpb.getPlayerInfoList()){
			if(player.getCamp() == camp && player.getHonor() >= score){
				count ++;
			}
		}
		return count;
	}
	
	public HawkTuple2<Integer, Integer> getHonorRateAndReward(String guildId){
		List<PBCYBORGGuildInfo> guildList = lastSyncpb.getGuildInfoList();
		for (PBCYBORGGuildInfo ginfo : guildList) {
			if (Objects.equal(guildId, ginfo.getGuildId())) {
				return new HawkTuple2<Integer, Integer>(ginfo.getHonorRate(), ginfo.getCyborgItemTotal());
			}
		}
		return null;
	}
	
	
	public HawkTuple2<String, String> getKillPowerMaxPlayer(String guildId){
		List<PBCYBORGGuildInfo> guildList = lastSyncpb.getGuildInfoList();
		for (PBCYBORGGuildInfo ginfo : guildList) {
			if (Objects.equal(guildId, ginfo.getGuildId())) {
				if(!ginfo.hasKillPowerMax()){
					return null;
				}
				PBCYBORGPlayerInfo playerInfo = ginfo.getKillPowerMax();
				if(playerInfo != null){
					PBCyborgContributePlayer.Builder killMaxBuilder = PBCyborgContributePlayer.newBuilder();
					killMaxBuilder.setPlayerId(playerInfo.getPlayerId());
					killMaxBuilder.setName(playerInfo.getName());
					killMaxBuilder.setIcon(playerInfo.getIcon());
					String pficon = playerInfo.getPfIcon();
					if(!HawkOSOperator.isEmptyString(pficon)){
						killMaxBuilder.setPfIcon(pficon);
					}
					killMaxBuilder.setKillPower(playerInfo.getKillPower());
					String data = JsonFormat.printToString(killMaxBuilder.build());
					return new HawkTuple2<String, String>(playerInfo.getPlayerId(), data);
				}
			}
		}
		return null;
	}
	
	
	public HawkTuple2<String, String> getDamagedMaxPlayer(String guildId){
		List<PBCYBORGGuildInfo> guildList = lastSyncpb.getGuildInfoList();
		for (PBCYBORGGuildInfo ginfo : guildList) {
			if (Objects.equal(guildId, ginfo.getGuildId())) {
				if(!ginfo.hasLostPowerMax()){
					return null;
				}
				PBCYBORGPlayerInfo playerInfo = ginfo.getLostPowerMax();
				if(playerInfo != null){
					PBCyborgContributePlayer.Builder damagedMaxBuilder = PBCyborgContributePlayer.newBuilder();
					damagedMaxBuilder.setPlayerId(playerInfo.getPlayerId());
					damagedMaxBuilder.setName(playerInfo.getName());
					damagedMaxBuilder.setIcon(playerInfo.getIcon());
					String pficon = playerInfo.getPfIcon();
					if(!HawkOSOperator.isEmptyString(pficon)){
						damagedMaxBuilder.setPfIcon(pficon);
					}
					damagedMaxBuilder.setLostPower(playerInfo.getLostPower());
					String data = JsonFormat.printToString(damagedMaxBuilder.build());
					return new HawkTuple2<String, String>(playerInfo.getPlayerId(), data);
				}
			}
		}
		return null;
	}
	
	
	public HawkTuple2<String, String> getKillMonsterMaxPlayer(String guildId){
		List<PBCYBORGGuildInfo> guildList = lastSyncpb.getGuildInfoList();
		for (PBCYBORGGuildInfo ginfo : guildList) {
			if (Objects.equal(guildId, ginfo.getGuildId())) {
				if(!ginfo.hasKillMonsterMax()){
					return null;
				}
				PBCYBORGPlayerInfo playerInfo = ginfo.getKillMonsterMax();
				if(playerInfo != null){
					PBCyborgContributePlayer.Builder monsterMaxBuilder = PBCyborgContributePlayer.newBuilder();
					monsterMaxBuilder.setPlayerId(playerInfo.getPlayerId());
					monsterMaxBuilder.setName(playerInfo.getName());
					monsterMaxBuilder.setIcon(playerInfo.getIcon());
					String pficon = playerInfo.getPfIcon();
					if(!HawkOSOperator.isEmptyString(pficon)){
						monsterMaxBuilder.setPfIcon(pficon);
					}
					monsterMaxBuilder.setKillMonster(playerInfo.getKillMonster());
					String data = JsonFormat.printToString(monsterMaxBuilder.build());
					return new HawkTuple2<String, String>(playerInfo.getPlayerId(), data);
				}
			}
		}
		return null;
	}
	
	
}
