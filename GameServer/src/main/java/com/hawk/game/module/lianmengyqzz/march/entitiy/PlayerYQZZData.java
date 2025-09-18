package com.hawk.game.module.lianmengyqzz.march.entitiy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.google.common.base.MoreObjects;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveState;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.YQZZWar.PBYQZZWarAchieve;
import com.hawk.log.Action;

public class PlayerYQZZData {
	/** 数据实体*/
	private PlayerYQZZEntity dataEntity;
	/** 成就列表*/
	private Map<Integer,YQZZAchieve> achieves = new HashMap<>();

	
	private PlayerYQZZData(PlayerYQZZEntity entity) {
		this.dataEntity = entity;
	}
	
	
	public static PlayerYQZZData create(PlayerYQZZEntity entity) {
		PlayerYQZZData data = new PlayerYQZZData(entity);
		data.init();
		entity.recordYQZZObj(data);
		return data;
	}

	private void init() {
		this.achieves = this.loadAchieves();
	}
	
	public void notifyChange(){
		this.dataEntity.notifyUpdate();
	}
	
	public boolean achieveReward(int achieveId){
		YQZZAchieve achive = this.achieves.get(achieveId);
		if(achive == null){
			return false;
		}
		if(achive.getState() != YQZZAchieveState.FINISH){
			return false;
		}
		achive.setState(YQZZAchieveState.REWARD);
		this.notifyChange();
		List<ItemInfo> awardList = achive.getAchieveCfg().getRewardList();
		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItemInfos(awardList);
		awardItem.rewardTakeAffectAndPush(this.getParent(), Action.YQZZ_ACHIEVE_REWARD,true);
		return true;
	}
	
	
	private Map<Integer,YQZZAchieve> loadAchieves(){
		Map<Integer,YQZZAchieve> map = new ConcurrentHashMap<Integer, YQZZAchieve>();
		String componentSerialized = this.dataEntity.getAchieveSerialized();
		if(HawkOSOperator.isEmptyString(componentSerialized)){
			return map;
		}
		JSONArray arr = JSONArray.parseArray(componentSerialized);
		arr.forEach(obj->{
			YQZZAchieve achieve = new YQZZAchieve();
			achieve.mergeFrom(obj.toString());
			achieve.setParent(this);
			map.put(achieve.getAchieveId(), achieve);
		});
		return map;
	}

	
	public int getTermId(){
		return this.dataEntity.getTermId();
	}
	
	public void setTermId(int termId){
		this.dataEntity.setTermId(termId);
	}
	

	public String getPlayerId(){
		return this.dataEntity.getPlayerId();
	}

	public Player getParent() {
		return GlobalData.getInstance().makesurePlayer(getPlayerId());
	}
	
	public Map<Integer, YQZZAchieve> getAchieves() {
		return achieves;
	}
	
	
	public void restAchieves(Map<Integer, YQZZAchieve> achieves){
		this.achieves = achieves;
		this.notifyChange();
	}
	
	public long getLeaveBattleTime(){
		return this.dataEntity.getLeaveBattleTime();
	}
	
	public void setLeaveBattleTime(long leaveBattleTime){
		this.dataEntity.setLeaveBattleTime(leaveBattleTime);
	}
	
	public String getPlayerGuild(){
		return this.dataEntity.getPlayerGuild();
	}
	
	
	public void setPlayerGuild(String guildId){
		if(HawkOSOperator.isEmptyString(guildId)){
			this.dataEntity.setPlayerGuild("");
		}else{
			this.dataEntity.setPlayerGuild(guildId);
		}
	}

	public PBYQZZWarAchieve.Builder genAchieveBuilder(){
		PBYQZZWarAchieve.Builder builder = PBYQZZWarAchieve.newBuilder();
		for(YQZZAchieve achieve : this.achieves.values()){
			if (achieve.getAchieveCfg() != null) {
				builder.addAchieves(achieve.genYQZZAchieveItemBuilder());
			}
		}
		return builder;
	}
	
	
	/** 序列化achieve */
	public String serializAchieve() {
		JSONArray arr = new JSONArray();
		for(YQZZAchieve achieve : this.achieves.values()){
			arr.add(achieve.serializ());
		}
		return arr.toJSONString();
	}

	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("playerId", getPlayerId())
				.add("achieves", serializAchieve())
				.toString();
	}

}
