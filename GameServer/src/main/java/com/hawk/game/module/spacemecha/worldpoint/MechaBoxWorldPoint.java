package com.hawk.game.module.spacemecha.worldpoint;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.serializer.HawkSerializer;
import com.google.protobuf.ByteString;
import com.hawk.game.protocol.SpaceMecha.MechaBoxPB;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.WorldPoint.PBSerializeData;
import com.hawk.game.protocol.WorldPoint.PointData;
import com.hawk.game.world.WorldPoint;

/**
 * 星甲召唤宝箱
 * 
 * @author lating
 *
 */
public class MechaBoxWorldPoint extends WorldPoint {
	/**
	 * 宝箱ID
	 */
	private int boxId;
	
	private String collectPlayerId = "";
	
	private String collectPlayerName = "";
	
	private long collectEndTime;
	
	public MechaBoxWorldPoint() {
	}
	
	public MechaBoxWorldPoint(int x, int y, int areaId, int zoneId, int pointType) {
		super(x, y, areaId, zoneId, pointType);
	}
	
	@Override
	public WorldPointPB.Builder toBuilder(WorldPointPB.Builder builder,String viewerId) {
		super.toBuilder(builder,viewerId);
		builder.setMechaBox(toBuilder());
		builder.setGuildId(this.getGuildId());
		if (collectPlayerId.equals(viewerId)) {
			builder.setHasMarchStop(true);
		}
		return builder;
	}

	@Override
	public WorldPointDetailPB.Builder toDetailBuilder(String viewerId) {
		WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewerId);
		builder.setMechaBox(toBuilder());
		builder.setGuildId(this.getGuildId());
		if (collectPlayerId.equals(viewerId)) {
			builder.setHasMarchStop(true);
		}
		return builder;
	}
	
	private MechaBoxPB.Builder toBuilder() {
		MechaBoxPB.Builder builder = MechaBoxPB.newBuilder();
		builder.setId(boxId);
		builder.setPosX(this.getX());
		builder.setPosY(this.getY());
		if (HawkOSOperator.isEmptyString(collectPlayerId)) {
			builder.setStatus(0);
			builder.setEndTime(0);
		} else {
			builder.setStatus(1);
			builder.setPlayerId(collectPlayerId);
			builder.setPlayerName(collectPlayerName);
			builder.setEndTime(collectEndTime);
		}
		return builder;
	}

	@Override
	public PointData.Builder buildPointData() {
		PointData.Builder builder = super.buildPointData();
		PBSerializeData.Builder extraBuilder = PBSerializeData.newBuilder();
		extraBuilder.setParam1(serialize(boxId));
		extraBuilder.setParam2(serialize(collectPlayerId));
		extraBuilder.setParam3(serialize(collectPlayerName));
		extraBuilder.setParam4(serialize(collectEndTime));
		builder.setExtryData(extraBuilder.build());
		return builder;
	}

	@Override
	public void mergeFromPointData(PointData.Builder builder) {
		super.mergeFromPointData(builder);
		try {
			PBSerializeData data = builder.getExtryData();
			this.boxId = deserialize(data.getParam1(), Integer.class);
			this.collectPlayerId = deserialize(data.getParam2(), String.class);
			this.collectPlayerName = deserialize(data.getParam3(), String.class);
			this.collectEndTime = deserialize(data.getParam4(), Long.class);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	private <T> ByteString serialize(T value) {
		return ByteString.copyFrom(HawkSerializer.serialize(value));
	}

	private <T> T deserialize(ByteString bytes, Class<T> type) {
		return HawkSerializer.deserialize(bytes.toByteArray(), type);
	}

	public int getBoxId() {
		return boxId;
	}

	public void setBoxId(int boxId) {
		this.boxId = boxId;
	}

	public String getCollectPlayerId() {
		return collectPlayerId;
	}

	public void setCollectPlayerId(String collectPlayerId) {
		this.collectPlayerId = collectPlayerId;
	}

	public String getCollectPlayerName() {
		return collectPlayerName;
	}

	public void setCollectPlayerName(String collectPlayerName) {
		this.collectPlayerName = collectPlayerName;
	}

	public long getCollectEndTime() {
		return collectEndTime;
	}

	public void setCollectEndTime(long collectEndTime) {
		this.collectEndTime = collectEndTime;
	}

}
