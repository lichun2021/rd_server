package com.hawk.game.crossactivity.resourcespree;

import java.util.ArrayList;
import java.util.List;

import org.hawk.serializer.HawkSerializer;

import com.google.protobuf.ByteString;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointPB.Builder;
import com.hawk.game.protocol.WorldPoint.PBSerializeData;
import com.hawk.game.protocol.WorldPoint.PointData;
import com.hawk.game.world.WorldPoint;


/**
 * 资源狂欢宝箱
 * @author chechangda
 *
 */
public class ResourceSpreeBoxWorldPoint extends WorldPoint{

	/** 宝箱ID*/
	private int resourceSpreeBoxId;
	
	/** 活动期数*/
	private int crossTermId;
	
	/** 胜利方*/
	private List<String> winner;
	
	
	public ResourceSpreeBoxWorldPoint() {
		this.resourceSpreeBoxId = 0;
		this.crossTermId = 0;
		this.winner = new ArrayList<String>();
	}
	
	
	public ResourceSpreeBoxWorldPoint(int x, int y, int areaId, int zoneId, int pointType) {
		super(x, y, areaId, zoneId, pointType);
		this.resourceSpreeBoxId = 0;
		this.crossTermId = 0;
		this.winner = new ArrayList<String>();
	}
	
	
	
	public boolean achiveServer(String server){
		if(this.winner ==null){
			return false;
		}
		if(!this.winner.contains(server)){
			return false;
		}
		return true;
	}
	

	@Override
	public WorldPointPB.Builder toBuilder(Builder builder, String viewerId) {
		super.toBuilder(builder, viewerId);
		builder.setResourceSpreeBoxId(this.resourceSpreeBoxId);
		return builder;
	}
	
	
	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(String viewerId) {
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewerId);
		builder.setResourceSpreeBoxId(this.resourceSpreeBoxId);
		return builder;
	}
	
	
	
	@Override
	public PointData.Builder buildPointData() {
		PointData.Builder builder = super.buildPointData();
		builder.setExtryData(this.serializ());
		return builder;
	}
	
	
	@Override
	public void mergeFromPointData(PointData.Builder builder) {
		super.mergeFromPointData(builder);
		this.mergeFrom(builder.getExtryData());
	}

	public PBSerializeData serializ() {
		PBSerializeData.Builder obj = PBSerializeData.newBuilder();
		obj.setParam1(serialize(this.resourceSpreeBoxId));
		obj.setParam2(serialize(this.crossTermId));
		if (!this.winner.isEmpty()) {
			obj.setParam3(serialize(this.winner));
		}
		return obj.build();
	}
	
	
	@SuppressWarnings("unchecked")
	public void mergeFrom(PBSerializeData data) {
		this.resourceSpreeBoxId = deserialize(data.getParam1(), int.class);
		this.crossTermId = deserialize(data.getParam2(), int.class);
		if(data.hasParam3()){
			this.winner = deserialize(data.getParam3(), ArrayList.class);
		}
		
	}

	
	private <T> ByteString serialize(T value) {
		return ByteString.copyFrom(HawkSerializer.serialize(value));
	}

	private <T> T deserialize(ByteString bytes, Class<T> type) {
		return HawkSerializer.deserialize(bytes.toByteArray(), type);
	}
	
	
	public int getResourceSpreeBoxId() {
		return resourceSpreeBoxId;
	}
	
	
	public void setResourceSpreeBoxId(int resourceSpreeBoxId) {
		this.resourceSpreeBoxId = resourceSpreeBoxId;
	}


	public int getCrossTermId() {
		return crossTermId;
	}


	public void setCrossTermId(int crossTermId) {
		this.crossTermId = crossTermId;
	}


	public List<String> getWinner() {
		return winner;
	}


	public void addWinner(List<String> winner) {
		this.winner.addAll(winner);
	}

	
	
	
	
	
}
