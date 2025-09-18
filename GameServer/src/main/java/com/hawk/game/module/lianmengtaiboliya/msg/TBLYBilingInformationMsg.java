package com.hawk.game.module.lianmengtaiboliya.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.msg.HawkMsg;

import com.google.common.base.Objects;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;
import com.hawk.game.protocol.TBLY.PBGuildInfo;
import com.hawk.game.protocol.TBLY.PBPlayerInfo;
import com.hawk.game.protocol.TBLY.PBTBLYGameInfoSync;

/***
 * 结算信息
 */
public class TBLYBilingInformationMsg extends HawkMsg {
	private String campAGuild = "";
	private String campBGuild = "";
	private int campAPlayerCnt;
	private int campBPlayerCnt;
	private String roomId;
	private boolean isLeaguaWar;
	private int season;
	private PBTBLYGameInfoSync lastSyncpb;
	/** 建筑占领记录 */
	private List<BuildingControlRecord> buildRecords = new ArrayList<>();
	/** 玩家个人积分 */
	private List<PlayerGameRecord> playerRecords = new ArrayList<>();
	private String winGuild;
	public String first5000Honor;// 首无5000
	public String firstKillNian; // 首杀nian
	public String firstControlHeXin; // 首控制核心

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

	public void init(PBTBLYGameInfoSync lastSyncpb, String campAGuild, String campBGuild) {
		this.lastSyncpb = lastSyncpb;
		this.campAGuild = campAGuild;
		this.campBGuild = campBGuild;

		for (PBPlayerInfo player : lastSyncpb.getPlayerInfoList()) {
			addPlayerRecord(player);
		}

	}

	public void addBuildRecord(ITBLYBuilding build) {
		BuildingControlRecord record = new BuildingControlRecord();
		record.setBuildId(build.getPointType().getNumber());
		record.setIndex(build.getIndex());
		record.controlGuildTimeMap.putAll(build.getControlGuildTimeMap().asMap());
		for (Entry<String, Double> ent : build.getControlGuildHonorMap().entrySet()) {
			record.controlGuildHonorMap.put(ent.getKey(), ent.getValue().longValue());
		}

		buildRecords.add(record);
	}

	private void addPlayerRecord(PBPlayerInfo player) {
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
		List<PBGuildInfo> guildList = lastSyncpb.getGuildInfoList();
		for (PBGuildInfo ginfo : guildList) {
			if (Objects.equal(guildid, ginfo.getGuildId())) {
				return ginfo.getHonor();
			}
		}
		return 0;
	}
	
	public PBGuildInfo getGuildInfo(String guildid){
		List<PBGuildInfo> guildList = lastSyncpb.getGuildInfoList();
		for (PBGuildInfo ginfo : guildList) {
			if (Objects.equal(guildid, ginfo.getGuildId())) {
				return ginfo;
			}
		}
		return null;
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
		/** 建筑中击杀. 同积分计算*/
		private long killPower;

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

		public long getKillPower() {
			return killPower;
		}

		public void setKillPower(long killPower) {
			this.killPower = killPower;
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

	public String getWinGuild() {
		return winGuild;
	}

	public void setWinGuild(String winGuild) {
		this.winGuild = winGuild;
	}

	public String getFirst5000Honor() {
		return first5000Honor;
	}

	public void setFirst5000Honor(String first5000Honor) {
		this.first5000Honor = first5000Honor;
	}

	public String getFirstKillNian() {
		return firstKillNian;
	}

	public void setFirstKillNian(String firstKillNian) {
		this.firstKillNian = firstKillNian;
	}

	public String getFirstControlHeXin() {
		return firstControlHeXin;
	}

	public void setFirstControlHeXin(String firstControlHeXin) {
		this.firstControlHeXin = firstControlHeXin;
	}

}
