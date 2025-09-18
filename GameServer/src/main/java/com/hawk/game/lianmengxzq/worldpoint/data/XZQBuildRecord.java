package com.hawk.game.lianmengxzq.worldpoint.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hawk.serializer.HawkSerializer;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.game.lianmengxzq.XZQRedisData;
import com.hawk.game.protocol.WorldPoint.PBSerializeData;
import com.hawk.game.protocol.XZQ.PBXZQBuildCommander;
import com.hawk.game.protocol.XZQ.PBXZQBuildGuild;

public class XZQBuildRecord {
	/** 点ID*/
	private int pointId;
	/** 服务器ID*/
	private String serverId;
	/** 攻破刻字  永不变*/
	private PBXZQBuildCommander occupyPlayerRecord;
	/** 攻破伤害前3 刻字 永不变*/
	private List<PBXZQBuildCommander> damagesRecord = new ArrayList<>();
	/** 攻破刻字  联盟*/
	private PBXZQBuildGuild controlGuild;
	
	
	
	
	public int getPointId() {
		return pointId;
	}
	public void setPointId(int pointId) {
		this.pointId = pointId;
	}
	public String getServerId() {
		return serverId;
	}
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	public PBXZQBuildCommander getOccupyPlayerRecord() {
		return occupyPlayerRecord;
	}
	public void setOccupyPlayerRecord(PBXZQBuildCommander occupyPlayerRecord) {
		this.occupyPlayerRecord = occupyPlayerRecord;
	}
	public List<PBXZQBuildCommander> getDamagesRecord() {
		return damagesRecord;
	}
	public void setDamagesRecord(List<PBXZQBuildCommander> damagesRecord) {
		this.damagesRecord = damagesRecord;
	}
	
	public PBXZQBuildGuild getControlGuild() {
		return controlGuild;
	}
	public void setControlGuild(PBXZQBuildGuild controlGuild) {
		this.controlGuild = controlGuild;
	}
	private <T> ByteString serialize(T value) {
		return ByteString.copyFrom(HawkSerializer.serialize(value));
	}

	public <T> T deserialize(ByteString bytes, Class<T> type) {
		return HawkSerializer.deserialize(bytes.toByteArray(), type);
	}
	
	public PBSerializeData serializ() {
		PBSerializeData.Builder obj = PBSerializeData.newBuilder();
		obj.setParam1(serialize(pointId));
		if (Objects.nonNull(serverId)) {
			obj.setParam2(serialize(serverId));
		}
		if (Objects.nonNull(occupyPlayerRecord)) {
			obj.setParam3(occupyPlayerRecord.toByteString());
		}
		if(Objects.nonNull(controlGuild)){
			obj.setParam4(controlGuild.toByteString());
		}
		
		for (PBXZQBuildCommander com : damagesRecord) {
			obj.addParam21(com.toByteString());
		}
		return obj.build();
	}

	public void mergeFrom(PBSerializeData data) {
		try {
			if(data.hasParam1()){
				this.pointId =  deserialize(data.getParam1(), Integer.class);
			}
			if (data.hasParam2()) {
				this.serverId = deserialize(data.getParam2(), String.class);
			}
			if (data.hasParam3()) {
				occupyPlayerRecord = PBXZQBuildCommander.newBuilder().mergeFrom(data.getParam3()).build();
			}
			if (data.hasParam4()) {
				controlGuild = PBXZQBuildGuild.newBuilder().mergeFrom(data.getParam4()).build();
			}
			for (ByteString arr : data.getParam21List()) {
				damagesRecord.add(PBXZQBuildCommander.newBuilder().mergeFrom(arr).build());
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	
	public void notifyUpdate(){
		PBSerializeData data = this.serializ();
		XZQRedisData.getInstance().updateXZQBuildRecord(this.serverId,this.pointId, data.toByteArray());
	}
	
	
	public static XZQBuildRecord load(String serverId,int pointId){
		byte[] byteData = XZQRedisData.getInstance().getXZQBuildRecord(serverId, pointId);
		if(byteData == null){
			return null;
		}
		if(byteData.length <= 0){
			return null;
		}
		try {
			PBSerializeData date = PBSerializeData.parseFrom(byteData);
			XZQBuildRecord record = new XZQBuildRecord();
			record.mergeFrom(date);
			return record;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void clear(String serverId,int pointId){
		 XZQRedisData.getInstance().deleteXZQBuildRecord(serverId, pointId);
	}
	

	
	
}
