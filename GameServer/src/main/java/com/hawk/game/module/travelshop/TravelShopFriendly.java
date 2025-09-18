package com.hawk.game.module.travelshop;

import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class TravelShopFriendly implements SerializJsonStrAble{

	
	private long friendly;
		
	private long friendlyCommAwardCount;
	
	private long friendlyPrivilegeAwardCount;
	
	private int privilegeAwardGroup;
	
	private long privilegeStartTime;
	
	private long updateFlag;
	
	public void addFriendlyScore(long add,boolean privilege){
		this.friendly += add;
		int friendlyCost = ConstProperty.getInstance().getTravelShopFriendlyAwardCost();
		long addCount = this.friendly / friendlyCost;
		if(addCount > 0){
			if(privilege){
				this.friendlyPrivilegeAwardCount += addCount;
			}else{
				this.friendlyCommAwardCount += addCount;
			}
			this.friendly = this.friendly % friendlyCost;
		}
	}
	
	public void costFriendlyAwardCount(long add,boolean privilege){
		if(privilege){
			this.friendlyPrivilegeAwardCount -= add;
			this.friendlyPrivilegeAwardCount = Math.max(this.friendlyPrivilegeAwardCount, 0);
			return;
		}
		this.friendlyCommAwardCount -= add;
		this.friendlyCommAwardCount = Math.max(this.friendlyCommAwardCount, 0);
	}
	
	
	
	public boolean privilegeEffect(){
		if (this.privilegeStartTime + ConstProperty.getInstance().getTravelShopFriendlyCardTime() > HawkTime.getMillisecond()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取特权玩家选取组别
	 * @return
	 */
	public int getPrivilegeAwardPoolChoose(){
		if(this.privilegeAwardGroup > 0){
			return this.privilegeAwardGroup;
		}
		return ConstProperty.getInstance().getTravelShopFriendlyAwardPrivilegeGroup();
	}
	
	
	
	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("1", friendly);
		obj.put("2", friendlyCommAwardCount);
		obj.put("3", friendlyPrivilegeAwardCount);
		obj.put("4", privilegeAwardGroup);
		obj.put("5", privilegeStartTime);
		obj.put("6", updateFlag);
		
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.friendly = obj.getLongValue("1");
		this.friendlyCommAwardCount = obj.getLongValue("2");
		this.friendlyPrivilegeAwardCount = obj.getLongValue("3");
		this.privilegeAwardGroup = obj.getIntValue("4");
		this.privilegeStartTime = obj.getLongValue("5");
		this.updateFlag =  obj.getLongValue("6");
	}

	
	
	public long getFriendly() {
		return friendly;
	}
	
	public long getFriendlyCommAwardCount() {
		return friendlyCommAwardCount;
	}
	
	
	public long getFriendlyPrivilegeAwardCount() {
		return friendlyPrivilegeAwardCount;
	}
	

	

	public void setPrivilegeAwardGroup(int privilegeAwardGroup) {
		this.privilegeAwardGroup = privilegeAwardGroup;
	}
	
	
	public int getPrivilegeAwardGroup() {
		return privilegeAwardGroup;
	}
	
	
	public void setPrivilegeStartTime(long privilegeStartTime) {
		this.privilegeStartTime = privilegeStartTime;
	}
	
	public long getPrivilegeStartTime() {
		return privilegeStartTime;
	}
	
	
	public void setUpdateFlag(long updateFlag) {
		this.updateFlag = updateFlag;
	}
	
	public long getUpdateFlag() {
		return updateFlag;
	}
}
