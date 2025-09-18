package com.hawk.game.player.queue;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.Queue.PBPaidQueueOpenDataResp;

public class QueueCustomData {
	private boolean freeUse;
	private long openTime;
	private long endTime;
	
	
	public long getEndTime() {
		return endTime;
	}
	
	public long getOpenTime() {
		return openTime;
	}
	
	public boolean isFreeUse() {
		return freeUse;
	}
	
	
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public void setOpenTime(long openTime) {
		this.openTime = openTime;
	}
	
	public void setFreeUse(boolean freeUse) {
		this.freeUse = freeUse;
	}
	
	
	public void parseObject(String value){
        if(HawkOSOperator.isEmptyString(value)){
            return;
        }
        JSONObject obj = JSONObject.parseObject(value);
        freeUse = obj.getBooleanValue("freeUse");
        openTime = obj.getLongValue("openTime");
        endTime = obj.getLongValue("endTime");
	}
	
	/**
	 * 保存数据
	 */
	public String toDataString(){
        JSONObject obj = new JSONObject();
        obj.put("freeUse", this.freeUse);
        obj.put("openTime", this.openTime);
        obj.put("endTime", this.endTime);
        return obj.toJSONString();
	}
	
	
	public PBPaidQueueOpenDataResp.Builder toBuilder(){
		PBPaidQueueOpenDataResp.Builder builder = PBPaidQueueOpenDataResp.newBuilder();
		builder.setFreeUse(this.freeUse);
		return builder;
	}
}
