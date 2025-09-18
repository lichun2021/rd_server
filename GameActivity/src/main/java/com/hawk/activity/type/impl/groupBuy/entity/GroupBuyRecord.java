package com.hawk.activity.type.impl.groupBuy.entity;
import java.util.List;
import com.hawk.game.protocol.Activity.GroupBuyRecordInfo;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 团购购买记录
 */
public class GroupBuyRecord implements SplitEntity {
	//礼包Id
	private int id;
	//折扣
	private int cfgId;
	//时间
	private long time;
	//个数
	private int num;

	public GroupBuyRecord() {
		
	}
	
	public static GroupBuyRecord valueOf(int id, int cfgId, long time, int num) {
		GroupBuyRecord record = new GroupBuyRecord();
		record.id = id;
		record.cfgId = cfgId;
		record.time = time;
		record.num = num;
		return record;
	}
	
	public GroupBuyRecordInfo.Builder createBuilder(){
		GroupBuyRecordInfo.Builder builder = GroupBuyRecordInfo.newBuilder();
		builder.setId(this.id);
		builder.setCfgId(this.cfgId);
		builder.setTime(this.time);
		builder.setNum(this.num);
		return builder;
	}
	
	@Override
	public SplitEntity newInstance() {
		return new GroupBuyRecord();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(this.id);
		dataList.add(this.cfgId);
		dataList.add(this.time);
		dataList.add(this.num);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(4);
		this.id = dataArray.getInt();
		this.cfgId = dataArray.getInt();
		this.time = dataArray.getLong();
		this.num = dataArray.getInt();
	}

	@Override
	public String toString() {
		return "[id=" + id + ",cfgId=" + cfgId + ", time=" + time +  ", num=" + num +  "]";
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCfgId() {
		return cfgId;
	}

	public void setCfgId(int cfgId) {
		this.cfgId = cfgId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	

}
