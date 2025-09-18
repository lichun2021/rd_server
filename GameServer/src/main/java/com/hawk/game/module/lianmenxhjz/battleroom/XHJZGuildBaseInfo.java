package com.hawk.game.module.lianmenxhjz.battleroom;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager.XHJZ_CAMP;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.XHJZ.XHJZDistributeGasolineRecord;
import com.hawk.game.service.xhjzWar.XHJZWarPlayerData;

public class XHJZGuildBaseInfo {
	private XHJZ_CAMP camp = XHJZ_CAMP.NONE;
	private String guildId = "";
	private String guildName = "";
	private String guildTag = "";
	private String serverId = "";
	private int guildFlag;
	private double gasoline;
	private double allianceFuelAdd;
	private double playerFuelAdd;// ="0.016667"
	private int coolDownReducePercentage;
	private int occupyMarchFuel;
	private String teamName;
	private List<XHJZWarPlayerData> commonder = new ArrayList<>();
	private EvictingQueue<XHJZDistributeGasolineRecord> fuelDisRecords = EvictingQueue.create(200);
	public ImmutableMap<EffType, Integer> battleEffVal = ImmutableMap.of();
	public Table<XHJZBuildType, EffType, Integer> specialEffectTable = ImmutableTable.of();
	
	public final static XHJZGuildBaseInfo defaultInstance = new XHJZGuildBaseInfo();
	public static XHJZGuildBaseInfo getDefaultinstance() {
		return defaultInstance;
	}


	public XHJZ_CAMP getCamp() {
		return camp;
	}

	public void setCamp(XHJZ_CAMP camp) {
		this.camp = camp;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String campGuild) {
		this.guildId = campGuild;
	}

	public String getGuildName() {
		return guildName;
	}

	public void setGuildName(String campGuildName) {
		this.guildName = campGuildName;
	}

	public String getGuildTag() {
		return guildTag;
	}

	public void setGuildTag(String campGuildTag) {
		this.guildTag = campGuildTag;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String campServerId) {
		this.serverId = campServerId;
	}

	public int getGuildFlag() {
		return guildFlag;
	}

	public void setGuildFlag(int campguildFlag) {
		this.guildFlag = campguildFlag;
	}

	public double getGasoline() {
		return gasoline;
	}

	public void setGasoline(double gasoline) {
		this.gasoline = gasoline;
	}

	public List<XHJZWarPlayerData> getCommonder() {
		return commonder;
	}

	public void setCommonder(List<XHJZWarPlayerData> commonder) {
		this.commonder = commonder;
	}

	public double getPlayerFuelAdd() {
		return playerFuelAdd;
	}

	public void setPlayerFuelAdd(double playerFuelAdd) {
		this.playerFuelAdd = playerFuelAdd;
	}

	public double getAllianceFuelAdd() {
		return allianceFuelAdd;
	}

	public void setAllianceFuelAdd(double allianceFuelAdd) {
		this.allianceFuelAdd = allianceFuelAdd;
	}

	public EvictingQueue<XHJZDistributeGasolineRecord> getFuelDisRecords() {
		return fuelDisRecords;
	}

	public void setFuelDisRecords(EvictingQueue<XHJZDistributeGasolineRecord> fuelDisRecords) {
		this.fuelDisRecords = fuelDisRecords;
	}


	public int getCoolDownReducePercentage() {
		return coolDownReducePercentage;
	}


	public void setCoolDownReducePercentage(int coolDownReducePercentage) {
		this.coolDownReducePercentage = coolDownReducePercentage;
	}


	public int getOccupyMarchFuel() {
		return occupyMarchFuel;
	}


	public void setOccupyMarchFuel(int occupyMarchFuel) {
		this.occupyMarchFuel = occupyMarchFuel;
	}


	public String getTeamName() {
		return teamName;
	}


	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}


}
