package com.hawk.activity.type.impl.fireworks.entity;
import java.util.List;
import com.hawk.game.protocol.Activity.FireBuffInfo;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 烟花盛典buf信息
 */
public class FireBuff implements SplitEntity {
	//bufId
	private int buffId;
	//结束时间
	private long endTime;

	public FireBuff() {
		
	}
	
	public static FireBuff valueOf(int buffId, long endTime) {
		FireBuff fireBuff = new FireBuff();
		fireBuff.buffId = buffId;
		fireBuff.endTime = endTime;
		return fireBuff;
	}
	
	public FireBuffInfo.Builder createBuilder(){
		FireBuffInfo.Builder builder = FireBuffInfo.newBuilder();
		builder.setBuffId(this.buffId);
		builder.setEndTime(this.endTime);
		return builder;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new FireBuff();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.buffId);
		dataList.add(this.endTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		this.buffId = dataArray.getInt();
		this.endTime = dataArray.getLong();
	}

	@Override
	public String toString() {
		return "[buffId=" + buffId + ", endTime=" + endTime + "]";
	}

	public int getBuffId() {
		return buffId;
	}

	public void setBuffId(int buffId) {
		this.buffId = buffId;
	}


	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
}
