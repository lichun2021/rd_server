package com.hawk.game.module.material.data;

import java.util.ArrayList;
import java.util.List;

import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.protocol.GuildChampionship.PBChampionPlayer;
import com.hawk.game.protocol.MaterialTransport.PBMTTruckRob;

public class MTTruckRob {
	private String id = HawkUUIDGenerator.genUUID();
	private String tarTruckId;
	private String guildId;
	private long startTime;
	private long endTime;
	private List<MTMember> mass = new ArrayList<>();
	private boolean over;
	private MTTruck tarTruck;

	public PBMTTruckRob toPBObj() {
		PBMTTruckRob.Builder result = PBMTTruckRob.newBuilder();
		result.setRobId(id);
		result.setTarget(tarTruck.toPBObj());
		result.setStartTime(startTime);
		result.setEndTime(endTime);
		for(MTMember me : mass){
			result.addMembers(me.toPBObj());
		}
		return result.build();
	}

	public List<PBChampionPlayer> getBattleDataList() {
		List<PBChampionPlayer> ids = new ArrayList<>();
		for (MTMember mem : mass) {
			ids.add(mem.getBattleData());
		}
		return ids;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public List<MTMember> getMass() {
		return mass;
	}

	public void setMass(List<MTMember> mass) {
		this.mass = mass;
	}

	public String getTarTruckId() {
		return tarTruckId;
	}

	public void setTarTruckId(String tarTruckId) {
		this.tarTruckId = tarTruckId;
	}

	public boolean isOver() {
		return over;
	}

	public void setOver(boolean over) {
		this.over = over;
	}

	public MTTruck getTarTruck() {
		return tarTruck;
	}

	public void setTarTruck(MTTruck tarTruck) {
		this.tarTruck = tarTruck;
	}

}
