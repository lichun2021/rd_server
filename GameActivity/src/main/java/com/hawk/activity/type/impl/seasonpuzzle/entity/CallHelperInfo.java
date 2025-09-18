package com.hawk.activity.type.impl.seasonpuzzle.entity;

import java.util.List;

import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.game.protocol.Activity.CallHelpInfoPB;
import com.hawk.game.protocol.Activity.CallHelpType;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

public class CallHelperInfo implements SplitEntity {
	/**
	 * 求助类型
	 */
	private int type;
	/**
	 * uuid
	 */
	private String uuid;
	/**
	 * 发起求助的时间
	 */
	private long time;
	/**
	 * 求助的道具ID
	 */
	private int itemId;
	/**
	 * 是否已获得帮助（1是0否）
	 */
	private int complete;
	/**
	 * tarPlayerId
	 */
	private String tarPlayerId;
	
	/** 发起求助者的玩家id */
	private String playerId;
	/** 发起求助者的玩家名字 */
	private String playerName;
	/** 发起求助者的玩家头像 */
	private int icon;
	/** 发起求助者的玩家平台头像 */
	private String pfIcon;
	
	public CallHelperInfo() {
	}
	
	public CallHelperInfo(int type, long time, int itemId, String tarPlayerId, String playerName, int icon, String pfIcon, String playerId) {
		this.type = type;
		this.time = time;
		this.itemId = itemId;
		this.tarPlayerId = tarPlayerId;
		this.uuid = HawkUUIDGenerator.genUUID();
		this.playerName = playerName;
		this.icon = icon;
		this.pfIcon = pfIcon;
		this.playerId = playerId;
	}
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public long getTime() {
		return time;
	}
	
	public void setTime(long time) {
		this.time = time;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	public int getComplete() {
		return complete;
	}
	
	public void setComplete(int complete) {
		this.complete = complete;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public SplitEntity newInstance() {
		return new CallHelperInfo();
	}

	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(type);
		dataList.add(uuid);
		dataList.add(time);
		dataList.add(itemId);
		dataList.add(complete);
		dataList.add(tarPlayerId);
		dataList.add(playerName);
		dataList.add(icon);
		dataList.add(pfIcon == null ? "" : pfIcon);
		dataList.add(playerId);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(9);
		type = dataArray.getInt();
		uuid = dataArray.getString();
		time = dataArray.getLong();
		itemId = dataArray.getInt();
		complete = dataArray.getInt();
		tarPlayerId = dataArray.getString();
		playerName = dataArray.getString();
		icon = dataArray.getInt();
		pfIcon = dataArray.getString();
		playerId = dataArray.getString();
	}
	
	public CallHelpInfoPB.Builder toBuilder() {
		CallHelpInfoPB.Builder builder = CallHelpInfoPB.newBuilder();
		builder.setType(CallHelpType.valueOf(type));
		builder.setUuid(uuid);
		builder.setTime(time);
		builder.setItemId(itemId);
		builder.setComplete(complete);
		builder.setTarPlayer(tarPlayerId);
		builder.setPlayerName(playerName);
		builder.setIcon(icon);
		builder.setPfIcon(pfIcon == null ? "" : pfIcon);
		builder.setPlayerId(playerId);
		return builder;
	}
	
}
