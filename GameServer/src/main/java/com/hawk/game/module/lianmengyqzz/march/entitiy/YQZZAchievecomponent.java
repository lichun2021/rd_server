package com.hawk.game.module.lianmengyqzz.march.entitiy;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveState;
import com.hawk.game.player.hero.SerializJsonStrAble;

public class YQZZAchievecomponent implements SerializJsonStrAble {

	private YQZZAchieve parent;
	private int achieveId;
	private int state;
	private long value;

	
	public YQZZAchievecomponent() {
		this.achieveId = 0;
		this.state = YQZZAchieveState.PROGRESS.getValue();
		this.value = 0;
	}
	
	public YQZZAchievecomponent(int achieveId,int state,int value,YQZZAchieve achive) {
		this.achieveId = achieveId;
		this.state = state;
		this.value = value;
		this.parent = achive;
	}
	
	
	

	public int getAchieveId() {
		return achieveId;
	}

	public void setAchieveId(int achieveId) {
		this.achieveId = achieveId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	
	
	
	
	
	
	public YQZZAchieve getParent() {
		return parent;
	}
	
	public void setParent(YQZZAchieve parent) {
		this.parent = parent;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("achieveId", this.achieveId);
		obj.put("state", this.state);
		obj.put("value", this.value);
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.achieveId = 0;
			this.state = YQZZAchieveState.PROGRESS.getValue();
			this.value = 0;
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.achieveId = obj.getIntValue("achieveId");
		this.state = obj.getIntValue("state");
		this.value = obj.getLongValue("value");
	}
	
	
	
}
