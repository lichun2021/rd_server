package com.hawk.activity.type.impl.newbietrain.entity;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.protocol.Activity.NoviceTrainInfoPB;
import com.hawk.game.protocol.Activity.NoviceTrainType;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.serialize.string.SplitEntity;

/**
 * 新兵作训信息
 */
public class NewbieTrainInfo implements SplitEntity {
	/**
	 * 类型
	 */
	private int trainType;
	/**
	 * 英雄招募或装备打造的次数累计（转换成作训次数后剩余的零头次数）
	 */
	private int gachaTimes;
	/**
	 * 英雄招募或装备打造的总次数
	 */
	private int gachaTimesTotal;
	/**
	 * 总的作训次数
	 */
	private int trainTimesTotal;
	/**
	 * 剩余作训次数
	 */
	private int trainTimesRemain;
	/**
	 * 选择的英雄/装备兵种对应的id
	 */
	private int trainObjectId;
	/**
	 * 已买过的礼包
	 */
	private List<Integer> giftIdList = new ArrayList<>();
	/**
	 * 作训记录数据
	 */
	private List<TrainRecordInfo> trainRecordList = new ArrayList<>();
	
	
	public int getTrainType() {
		return trainType;
	}

	public void setTrainType(int trainType) {
		this.trainType = trainType;
	}
	
	public int getGachaTimes() {
		return gachaTimes;
	}

	public void setGachaTimes(int gachaTimes) {
		this.gachaTimes = gachaTimes;
	}
	
	public void addGachaTimes(int addTimes) {
		this.gachaTimes += addTimes;
	}
	
	public int getGachaTimesTotal() {
		return gachaTimesTotal;
	}

	public void setGachaTimesTotal(int gachaTimesTotal) {
		this.gachaTimesTotal = gachaTimesTotal;
	}
	
	public void addGachaTimesTotal(int addTimes) {
		this.gachaTimesTotal += addTimes;
	}
	
	public int getTrainTimesTotal() {
		return trainTimesTotal;
	}
	
	public void setTrainTimesTotal(int trainTimesTotal) {
		this.trainTimesTotal = trainTimesTotal;
	}
	
	public void addTrainTimesTotal(int addTimes) {
		this.trainTimesTotal += addTimes;
	}
	
	public int getTrainTimesRemain() {
		return trainTimesRemain;
	}
	
	public void setTrainTimesRemain(int trainTimesRemain) {
		this.trainTimesRemain = trainTimesRemain;
	}
	
	public void addTrainTimesRemain(int addTimes) {
		this.trainTimesRemain += addTimes;
	}
	
	public int getTrainObjectId() {
		return trainObjectId;
	}
	
	public void setTrainObjectId(int trainObjectId) {
		this.trainObjectId = trainObjectId;
	}
	
	public List<Integer> getGiftIdList() {
		return giftIdList;
	}
	
	public void setGiftIdList(List<Integer> giftIdList) {
		this.giftIdList = giftIdList;
	}
	
	public List<TrainRecordInfo> getTrainRecordList() {
		return trainRecordList;
	}
	
	public void setTrainRecordList(List<TrainRecordInfo> trainRecordList) {
		this.trainRecordList = trainRecordList;
	}
	
	public String toString() {
		String giftIds = SerializeHelper.collectionToString(giftIdList, "_");
		return String.format("%d,%d,%d,%d,%d,%d,%s", trainType, gachaTimes, gachaTimesTotal, trainTimesTotal, trainTimesRemain, trainObjectId, giftIds);
	}
	
	@Override
	public NewbieTrainInfo newInstance() {
		return new NewbieTrainInfo();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(trainType);
		dataList.add(gachaTimes);
		dataList.add(gachaTimesTotal);
		dataList.add(trainTimesTotal);
		dataList.add(trainTimesRemain);
		dataList.add(trainObjectId);
		String giftIds = SerializeHelper.collectionToString(giftIdList, "_");
		dataList.add(giftIds);
		String records = SerializeHelper.collectionToString(trainRecordList, ",");
		dataList.add(records);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(8);
		trainType = dataArray.getInt();
		gachaTimes = dataArray.getInt();
		gachaTimesTotal = dataArray.getInt();
		trainTimesTotal = dataArray.getInt();
		trainTimesRemain = dataArray.getInt();
		trainObjectId = dataArray.getInt();
		String giftIds = dataArray.getString();
		String records = dataArray.getString();
		giftIdList = SerializeHelper.stringToList(Integer.class, giftIds, "_");
		trainRecordList = SerializeHelper.stringToList(TrainRecordInfo.class, records, ",");
	}
	
	public NoviceTrainInfoPB.Builder toBuilder() {
		NoviceTrainInfoPB.Builder builder = NoviceTrainInfoPB.newBuilder();
		builder.setSelectId(trainObjectId);
		builder.setTotalTimes(trainTimesTotal);
		builder.setTimes(trainTimesRemain);
		builder.setType(NoviceTrainType.valueOf(trainType));
		builder.addAllBoughtGiftId(giftIdList);
		builder.setGachaTimes(gachaTimes);
		builder.setGachaTimesTotal(gachaTimesTotal);
		return builder;
	}

}
