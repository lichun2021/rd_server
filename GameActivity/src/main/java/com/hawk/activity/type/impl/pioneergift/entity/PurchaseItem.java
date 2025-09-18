package com.hawk.activity.type.impl.pioneergift.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.app.HawkApp;
import org.hawk.os.HawkTime;
import org.hibernate.util.StringHelper;

import com.hawk.game.protocol.Activity.PioneerGiftInfo;
import com.hawk.serialize.string.DataArray;
import com.hawk.serialize.string.SplitEntity;

/**
 * 先锋豪礼购买项数据
 * 
 * @author lating
 *
 */
public class PurchaseItem implements SplitEntity {
	/**
	 * 礼包档次类型
	 */
	private int type = 0;
	
	/** 礼包id*/
	private int giftId = 0;
	
	/** 购买时间 */
	private long purchaseTime = 0;
	
	public PurchaseItem() {
		
	}
	
	public static PurchaseItem valueOf(int type, int giftId) {
		PurchaseItem data = new PurchaseItem();
		data.type = type;
		data.giftId = giftId;
		return data;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public int getGiftId() {
		return giftId;
	}

	public void setGiftId(int giftId) {
		this.giftId = giftId;
	}

	public long getPurchaseTime() {
		return purchaseTime;
	}

	public void setPurchaseTime(long purchaseTime) {
		this.purchaseTime = purchaseTime;
	}

	@Override
	public SplitEntity newInstance() {
		return new PurchaseItem();
	}
	
	@Override
	public void serializeData(List<Object> dataList) {
		dataList.add(type);
		dataList.add(giftId);
		dataList.add(purchaseTime);
	}

	@Override
	public void fullData(DataArray dataArray) {
		dataArray.setSize(3);
		type = dataArray.getInt();
		giftId = dataArray.getInt();
		purchaseTime = dataArray.getLong();
	}

	@Override
	public String toString() {
		return "[giftId=" + giftId + ", purchaseTime=" + purchaseTime + ", type=" + type + "]";
	}
	
	public PioneerGiftInfo.Builder toBuilder() {
		PioneerGiftInfo.Builder builder = PioneerGiftInfo.newBuilder();
		builder.setType(type);
		builder.setGiftId(giftId);
		builder.setPurchased(HawkTime.isSameDay(purchaseTime, HawkApp.getInstance().getCurrentTime()));
		return builder;
	}
	
	public static String serializeToString(PurchaseItem info){
		return String.format("%d_%d_%d", info.getType(), info.getGiftId(), info.getPurchaseTime());
	}
	
	public static void parseFromString( PurchaseItem info, String d ){
		String[] strAry = StringHelper.split("_", d);
		if(3 == strAry.length){
			info.setType(Integer.valueOf(strAry[0]));
			info.setGiftId(Integer.valueOf(strAry[1]));
			info.setPurchaseTime(Long.valueOf(strAry[2]));
		}
	}
	
	public static String serializeToString( Map<Integer, List<PurchaseItem>> itemMap ){
		StringBuilder ret = new StringBuilder();
		for( Map.Entry<Integer, List<PurchaseItem>> entry : itemMap.entrySet() ){
			String k = String.valueOf(entry.getKey());
			String v = serializeToString(entry.getValue());
			if(!k.isEmpty() && !v.isEmpty()){
				ret.append(k);
				ret.append(":");
				ret.append(v);
				ret.append("|");
			}
		}
		if(ret.length() > 0){
			ret.deleteCharAt(ret.length() - 1);
		}
		return ret.toString();
	}
	
	public static String serializeToString(List<PurchaseItem> itemList){
		StringBuilder ret = new StringBuilder();
		for( PurchaseItem t : itemList ){
			ret.append(serializeToString(t));
			ret.append(",");
		}
		if(ret.length() > 0){
			ret.deleteCharAt(ret.length() - 1);
		}
		return ret.toString();
	}
	
	
	public static <K,V> void parseFromString( Map<Integer, List<PurchaseItem>> itemMap , String str ){
		//使用|先分割
		String[] entrySetAry = StringHelper.split("|", str);
		for( int i = 0; i < entrySetAry.length; i++ ){
			//使用:分割 K:V
			String[] kvAry = StringHelper.split(":", entrySetAry[i]);
			if(kvAry.length == 2){
				Integer k = Integer.valueOf(kvAry[0]);
				List<PurchaseItem> v = new ArrayList<PurchaseItem>();
				parseFromString(v, kvAry[1]);
				itemMap.put(k, v);				
			}
		}
	}
	
	public static void parseFromString(List<PurchaseItem> itemList, String str){
		//使用,分割
		String [] itemAry = StringHelper.split(",", str);
		for(int i = 0; i < itemAry.length; i++){
			PurchaseItem item = new PurchaseItem();
			parseFromString(item, itemAry[i]);
			itemList.add(item);
		}
	}
}
