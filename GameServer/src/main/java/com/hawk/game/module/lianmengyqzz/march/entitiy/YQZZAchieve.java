package com.hawk.game.module.lianmengyqzz.march.entitiy;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZAchieveCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZAllianceAchieveCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZCountryAchieveCfg;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZPlayerAchieveCfg;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveState;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.YQZZWar.PBYQZZAchieveItem;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarAchieveState;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarAchieveType;

public class YQZZAchieve  implements SerializJsonStrAble {

	private PlayerYQZZData parent;
	
	private int achieveId;
	
	private int state;
	
	private YQZZAchievecomponent component1;
	private YQZZAchievecomponent component2;
	
	
	
	public boolean init(YQZZAchieveCfg cfg,PlayerYQZZData data){
		int type = cfg.getAchieveType();
		if(type == 0){
			return false;
		}
		int con1 = cfg.getConditionType1();
		if(con1 != 0 && YQZZAchieveType.valueOf(con1) == null){
			return false;
		}
		int con2 = cfg.getConditionType2();
		if(con2 != 0 && YQZZAchieveType.valueOf(con2) == null){
			return false;
		}
		this.achieveId = cfg.getAchieveId();
		this.state = YQZZAchieveState.PROGRESS.getValue();
		this.parent = data;
		if(con1 != 0 && YQZZAchieveType.valueOf(con1) != null){
			this.component1 = new YQZZAchievecomponent(cfg.getAchieveId(), 
					YQZZAchieveState.PROGRESS.getValue(), 0,this);
		}
		if(con2 != 0 && YQZZAchieveType.valueOf(con2) != null){
			this.component2 = new YQZZAchievecomponent(cfg.getAchieveId(), 
					YQZZAchieveState.PROGRESS.getValue(), 0,this);
		}
		return true;
	}
	
	
	
	
	
	public YQZZAchieveCfg getAchieveCfg(){
		YQZZAchieveCfg playerAchive =  HawkConfigManager.getInstance()
				.getConfigByKey(YQZZPlayerAchieveCfg.class, this.achieveId);
		if(playerAchive!= null){
			return playerAchive;
		}
		YQZZAchieveCfg guildAchive =  HawkConfigManager.getInstance()
				.getConfigByKey(YQZZAllianceAchieveCfg.class, this.achieveId);
		if(guildAchive!= null){
			return guildAchive;
		}
		YQZZAchieveCfg countryAchive =  HawkConfigManager.getInstance()
				.getConfigByKey(YQZZCountryAchieveCfg.class, this.achieveId);
		if(countryAchive!= null){
			return countryAchive;
		}
		return null;
	}

	public int getAchieveId() {
		return achieveId;
	}

	public void setAchieveId(int achieveId) {
		this.achieveId = achieveId;
	}

	public YQZZAchieveState getState() {
		return YQZZAchieveState.valueOf(this.state);
	}

	public void setState(YQZZAchieveState state) {
		this.state = state.getValue();
	}

	public YQZZAchievecomponent getComponent1() {
		return component1;
	}
	
	public void setComponent1(YQZZAchievecomponent component1) {
		this.component1 = component1;
	}
	
	
	public YQZZAchievecomponent getComponent2() {
		return component2;
	}
	
	public void setComponent2(YQZZAchievecomponent component2) {
		this.component2 = component2;
	}
	
	
	
	
	
	public PlayerYQZZData getParent() {
		return parent;
	}
	
	public void setParent(PlayerYQZZData parent) {
		this.parent = parent;
	}
	
	
	public PBYQZZAchieveItem.Builder genYQZZAchieveItemBuilder(){
		YQZZAchieveCfg cfg = this.getAchieveCfg();
		PBYQZZAchieveItem.Builder builder = PBYQZZAchieveItem.newBuilder();
		builder.setAchieveId(this.achieveId);
		builder.setType(PBYQZZWarAchieveType.valueOf(cfg.getAchieveType()));
		builder.setState(PBYQZZWarAchieveState.valueOf(this.state));
		if(this.component1 != null){
			builder.addValue(this.component1.getValue());
		}
		if(this.component2 != null){
			builder.addValue(this.component2.getValue());
		}
		return builder;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("achieveId", this.achieveId);
		obj.put("state", this.state);
		if(this.component1 != null){
			obj.put("component1", this.component1.serializ());
		}
		if(this.component2 != null){
			obj.put("component2", this.component2.serializ());
		}
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			this.achieveId = 0;
			this.state = YQZZAchieveState.PROGRESS.getValue();
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.achieveId = obj.getIntValue("achieveId");
		this.state = obj.getIntValue("state");
		if(obj.containsKey("component1")){
			String componentStr1 = obj.getString("component1");
			YQZZAchievecomponent component = new YQZZAchievecomponent();
			component.mergeFrom(componentStr1);
			this.component1 = component;
			this.component1.setParent(this);
		}
		if(obj.containsKey("component2")){
			String componentStr2 = obj.getString("component2");
			YQZZAchievecomponent component = new YQZZAchievecomponent();
			component.mergeFrom(componentStr2);
			this.component2 = component;
			this.component2.setParent(this);
		}
		
	}
	
	
	
	
}
