package com.hawk.activity.type.impl.starInvest;

import org.hawk.os.HawkOSOperator;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;

public class StarInvestExploreRecord {
	
	private String id;
	
	private int box;
	
	private int rewardIndx;
	
	private int advance;
	
	private long refreshTime;
	
	
	
	public static StarInvestExploreRecord valueOf(int box,int rewardIndx,int advance,long refreshTime){
		StarInvestExploreRecord re = new StarInvestExploreRecord();
		re.id = HawkUUIDGenerator.genUUID();
		re.box = box;
		re.rewardIndx = rewardIndx;
		re.advance = advance;
		re.refreshTime = refreshTime;
		return re;
	}
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("id", this.id);
		obj.put("box", this.box);
		obj.put("rewardIndx", this.rewardIndx);
		obj.put("advance", this.advance);
		obj.put("refreshTime", this.refreshTime);
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.id = obj.getString("id");
		this.box = obj.getIntValue("box");
		this.rewardIndx = obj.getIntValue("rewardIndx");
		this.advance = obj.getIntValue("advance");
		this.refreshTime = obj.getLongValue("refreshTime");
		
	}
	
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getBox() {
		return box;
	}

	public void setBox(int box) {
		this.box = box;
	}

	public int getAdvance() {
		return advance;
	}

	public void setAdvance(int advance) {
		this.advance = advance;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}
	
	public int getRewardIndx() {
		return rewardIndx;
	}
	public void setRewardIndx(int rewardIndx) {
		this.rewardIndx = rewardIndx;
	}
	

}
